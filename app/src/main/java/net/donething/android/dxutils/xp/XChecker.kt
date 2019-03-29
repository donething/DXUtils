package net.donething.android.dxutils.xp

import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

// 检测本模块是否已启用
object XChecker {
    fun isEnabled(): Boolean {
        return false
    }

    fun dealXChecker(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 注意不能使用 XChecker.class，否则将永远hook不到目标方法
        XposedHelpers.findAndHookMethod(
            XChecker::class.java.name, lpparam.classLoader, "isEnabled",
            XC_MethodReplacement.returnConstant(true)
        )
    }
}