package net.donething.android.dxutils.xp

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.donething.android.dxutils.utils.Debug

// 优酷
object Youku {
    fun dealYouku(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 拦截Activity的onPause，可阻止播放器暂停
        try {
            // 之前应该被hook的DetailActivity现在只是继承了MainDetailActivity，并没有做更多事情，所以需要hook其超类
            val detailClz = XposedHelpers.findClass(
                "com.youku.ui.activity.MainDetailActivity",
                lpparam.classLoader
            )
            XposedHelpers.findAndHookMethod(detailClz, "onPause", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                    // 此句不能省略，否则会导致被hook的应用崩溃
                    XposedHelpers.callMethod(param.thisObject, "onResume")
                    param.result = null
                }
            })
        } catch (t: Throwable) {
            Debug.log(Debug.E, "保持优酷后台播放时出错：$t")
            t.printStackTrace()
        }
    }
}