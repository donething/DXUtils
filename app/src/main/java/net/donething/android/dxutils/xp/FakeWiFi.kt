package net.donething.android.dxutils.xp

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import net.donething.android.dxutils.utils.Debug

// 虚拟WiFi
object FakeWiFi {
    fun dealFakeWiFi() {
        try {
            hookGetType()
            hookHasTransport()
            hookIsActiveNetworkMetered()
        } catch (t: Throwable) {
            Debug.log(Debug.E, "虚拟WiFi状态时出错：$t")
            t.printStackTrace()
        }

    }

    /**
     * NetworkInfo.getType()在Android Oreo之后 depreated
     */
    private fun hookGetType() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            XposedHelpers.findAndHookMethod(NetworkInfo::class.java, "getType", object : XC_MethodHook() {
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                    if (param.result as Int == ConnectivityManager.TYPE_MOBILE) {
                        param.result = ConnectivityManager.TYPE_WIFI
                    }
                }
            })

            XposedHelpers.findAndHookMethod(NetworkInfo::class.java, "getTypeName", object : XC_MethodHook() {
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                    if (param.result == "MOBILE") {
                        param.result = "WIFI"
                    }
                }
            })
        }
    }

    /**
     * NetworkCapabilities.hasTransport()为现在推荐的获取方式
     */
    private fun hookHasTransport() {
        XposedHelpers.findAndHookMethod(
            NetworkCapabilities::class.java, "hasTransport", Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                    // TRANSPORT_CELLULAR为蜂窝网络
                    if (param.args[0] as Int == NetworkCapabilities.TRANSPORT_WIFI) {
                        param.result = java.lang.Boolean.TRUE
                    }/* 只hook检测WiFi的请求
                else {
                    param.setResult(Boolean.FALSE);
                }
                */
                }
            })
    }

    /**
     * 有些应用会检测当前连接是否计费（比如哔哩哔哩可能提示为计费WiFi），需要返回false
     */
    private fun hookIsActiveNetworkMetered() {
        XposedHelpers.findAndHookMethod(ConnectivityManager::class.java, "isActiveNetworkMetered",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                    param!!.result = false
                }
            })
    }
}