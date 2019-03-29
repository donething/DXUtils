package net.donething.android.dxutils

import android.os.Build
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.donething.android.dxutils.utils.Constants
import net.donething.android.dxutils.utils.Debug
import net.donething.android.dxutils.utils.Utils
import net.donething.android.dxutils.xp.*
import net.donething.android.dxutils.xp.gb.ModVolumeKeySkipTrackOreo
import net.donething.android.dxutils.xp.gb.ModVolumeKeySkipTrackPie

class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 虚拟WiFi
        if (lpparam.appInfo != null && !Utils.isSysApp(lpparam.appInfo)) {
            Debug.log(Debug.D, "启用虚拟WiFi：" + lpparam.packageName)
            FakeWiFi.dealFakeWiFi()
        }

        // Toast显示应用图标
        ToastIcon.dealToastIcon()

        if (lpparam.packageName == "android" && lpparam.processName == "android") {
            // 音量键切歌
            Debug.log(Debug.D, "启用音量键切切歌：" + lpparam.packageName)
            when (Build.VERSION.SDK_INT) {
                Build.VERSION_CODES.O -> ModVolumeKeySkipTrackOreo.initAndroid(lpparam.classLoader)
                Build.VERSION_CODES.P -> ModVolumeKeySkipTrackPie.initAndroid(lpparam.classLoader)
                else -> Debug.log(Debug.E, "音量键切歌还未适配当前Android版本：" + Build.VERSION.CODENAME)
            }
        }

        // 指定的应用
        when (lpparam.packageName) {
            BuildConfig.APPLICATION_ID -> {
                Debug.log(Debug.D, "开始Hook：" + lpparam.packageName)
                XChecker.dealXChecker(lpparam)
            }
            Constants.PK_LEARNPTH -> {
                // 普通话学习
                Debug.log(Debug.D, "开始Hook：" + lpparam.packageName)
                LearnPTH.dealLearnPTH(lpparam)
            }
            Constants.PK_QTRUN -> {
                // 网络信号大师
                Debug.log(Debug.D, "开始Hook：" + lpparam.packageName)
                Qtrun.dealQtrun(lpparam)
            }
            Constants.PK_TXVIDEO -> {
                // 腾讯视频
                Debug.log(Debug.D, "开始Hook：" + lpparam.packageName)
                TXVideo.dealTXVideo(lpparam)
            }
            Constants.PK_YOUKU -> {
                // 优酷
                Debug.log(Debug.D, "开始Hook：" + lpparam.packageName)
                Youku.dealYouku(lpparam)
            }
        }
    }

    // Xposed模块开发——在hook之后使用module的drawable资源
    // 实现的接口参考：https://blog.csdn.net/zhangmiaoping23/article/details/54891457
    // 使用module的drawable资源参考：https://blog.csdn.net/u010746456/article/details/80084209
}