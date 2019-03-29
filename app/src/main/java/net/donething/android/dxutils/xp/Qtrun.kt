package net.donething.android.dxutils.xp

import android.os.Bundle
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.donething.android.dxutils.utils.Debug

// 网络信号大师
object Qtrun {
    fun dealQtrun(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 外边的try能捕获findAndHookMethod()的错误，如NoSuchMethodException。
        // 而不能捕获到afterHookedMethod()中的Exception
        try {
            val clz = XposedHelpers.findClass("com.qtrun.QuickTest.AdvancedActivity", lpparam.classLoader)
            XposedHelpers.findAndHookMethod(clz, "onCreate", Bundle::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                    val fieldName = "b"
                    // AdvancedActivity中的Boolean类型，名为"b"的变量，保存了购买信息
                    // afterHookedMethod()中的Throwable需要独立捕获
                    try {
                        val bField = XposedHelpers.findField(clz, fieldName)
                        bField.set(param.thisObject, true)
                    } catch (t: Throwable) {
                        Debug.log(Debug.W, "没有发现字段'$fieldName'")
                        t.printStackTrace()
                    }

                }
            })
        } catch (t: Throwable) {
            Debug.log(Debug.E, "Hook网络信号大师时出错：$t")
            t.printStackTrace()
        }
    }
}