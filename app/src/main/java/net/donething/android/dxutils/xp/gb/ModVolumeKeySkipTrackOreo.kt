package net.donething.android.dxutils.xp.gb

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.SystemClock
import android.view.KeyEvent
import android.view.ViewConfiguration
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import net.donething.android.dxutils.utils.Debug

// Oreo
// 源码：https://github.com/GravityBox/GravityBox/blob/oreo/src/com/ceco/oreo/gravitybox/ModVolumeKeySkipTrack.java
object ModVolumeKeySkipTrackOreo {
    private const val CLASS_PHONE_WINDOW_MANAGER = "com.android.server.policy.PhoneWindowManager"
    private const val CLASS_IWINDOW_MANAGER = "android.view.IWindowManager"
    private const val CLASS_WINDOW_MANAGER_FUNCS = "android.view.WindowManagerPolicy.WindowManagerFuncs"

    private var mIsLongPress = false
    private var mAudioManager: AudioManager? = null

    fun initAndroid(classLoader: ClassLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                CLASS_PHONE_WINDOW_MANAGER, classLoader, "init",
                Context::class.java, CLASS_IWINDOW_MANAGER, CLASS_WINDOW_MANAGER_FUNCS,
                handleConstructPhoneWindowManager
            )

            XposedHelpers.findAndHookMethod(
                CLASS_PHONE_WINDOW_MANAGER,
                classLoader,
                "interceptKeyBeforeQueueing",
                KeyEvent::class.java,
                Int::class.javaPrimitiveType,
                handleInterceptKeyBeforeQueueing
            )
        } catch (t: Throwable) {
            Debug.log(Debug.E, "初始化class ModVolumeKeySkipTrack时出错：$t")
            t.printStackTrace()
        }
    }

    private val handleInterceptKeyBeforeQueueing = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
            val event = param.args[0] as KeyEvent
            val keyCode = event.keyCode
            initManagers(XposedHelpers.getObjectField(param.thisObject, "mContext") as Context)
            if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) &&
                event.flags and KeyEvent.FLAG_FROM_SYSTEM != 0 &&
                mAudioManager != null && isMusicActive()
            ) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    mIsLongPress = false
                    handleVolumeLongPress(param.thisObject, keyCode)
                } else {
                    handleVolumeLongPressAbort(param.thisObject)
                    if (!mIsLongPress) {
                        mAudioManager!!.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                                AudioManager.ADJUST_RAISE
                            else
                                AudioManager.ADJUST_LOWER, 0
                        )
                    }
                }
                param.result = 0
            }
        }
    }

    private val handleConstructPhoneWindowManager = object : XC_MethodHook() {
        override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
            /*
             * When a volumeup-key longpress expires, skip songs based on key press
             */
            val mVolumeUpLongPress = {
                // set the long press flag to true
                mIsLongPress = true

                // Shamelessly copied from Kmobs LockScreen controls, works for Pandora, etc...
                sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_NEXT)
            }

            /*
             * When a volumedown-key longpress expires, skip songs based on key press
             */
            val mVolumeDownLongPress = {
                // set the long press flag to true
                mIsLongPress = true

                // Shamelessly copied from Kmobs LockScreen controls, works for Pandora, etc...
                sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            }

            XposedHelpers.setAdditionalInstanceField(param.thisObject, "mVolumeUpLongPress", mVolumeUpLongPress)
            XposedHelpers.setAdditionalInstanceField(param.thisObject, "mVolumeDownLongPress", mVolumeDownLongPress)
        }
    }

    private fun initManagers(ctx: Context) {
        if (mAudioManager == null) {
            mAudioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }
    }

    @Suppress("DEPRECATION")
    private fun isMusicActive(): Boolean {
        val am = mAudioManager
        am ?: return false

        // check local
        if (am.isMusicActive)
            return true
        // check remote
        try {
            if (XposedHelpers.callMethod(am, "isMusicActiveRemotely") as Boolean)
                return true
        } catch (t: Throwable) {
            Debug.log(Debug.E, "检查是否正在播放远程音频时出错：$t")
        }

        // bluetooth A2DP? (not sure here)
        return am.isBluetoothA2dpOn
    }

    private fun sendMediaButtonEvent(code: Int) {
        val eventtime = SystemClock.uptimeMillis()
        val keyIntent = Intent(Intent.ACTION_MEDIA_BUTTON, null)
        var keyEvent = KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, code, 0)
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent)
        dispatchMediaButtonEvent(keyEvent)

        keyEvent = KeyEvent.changeAction(keyEvent, KeyEvent.ACTION_UP)
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent)
        dispatchMediaButtonEvent(keyEvent)
    }

    private fun dispatchMediaButtonEvent(keyEvent: KeyEvent) {
        try {
            mAudioManager!!.dispatchMediaKeyEvent(keyEvent)
        } catch (t: Throwable) {
            Debug.log(Debug.E, "dispatch按钮事件时出错：$t")
        }

    }

    private fun handleVolumeLongPress(phoneWindowManager: Any, keycode: Int) {
        val mHandler = XposedHelpers.getObjectField(phoneWindowManager, "mHandler") as Handler
        val mVolumeUpLongPress =
            XposedHelpers.getAdditionalInstanceField(phoneWindowManager, "mVolumeUpLongPress") as Runnable
        val mVolumeDownLongPress =
            XposedHelpers.getAdditionalInstanceField(phoneWindowManager, "mVolumeDownLongPress") as Runnable

        mHandler.postDelayed(
            if (keycode == KeyEvent.KEYCODE_VOLUME_UP)
                mVolumeUpLongPress
            else
                mVolumeDownLongPress, ViewConfiguration.getLongPressTimeout().toLong()
        )
    }

    private fun handleVolumeLongPressAbort(phoneWindowManager: Any) {
        val mHandler = XposedHelpers.getObjectField(phoneWindowManager, "mHandler") as Handler
        val mVolumeUpLongPress =
            XposedHelpers.getAdditionalInstanceField(phoneWindowManager, "mVolumeUpLongPress") as Runnable
        val mVolumeDownLongPress =
            XposedHelpers.getAdditionalInstanceField(phoneWindowManager, "mVolumeDownLongPress") as Runnable

        mHandler.removeCallbacks(mVolumeUpLongPress)
        mHandler.removeCallbacks(mVolumeDownLongPress)
    }
}