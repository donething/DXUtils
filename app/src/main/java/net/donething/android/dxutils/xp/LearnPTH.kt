package net.donething.android.dxutils.xp

import android.app.Activity
import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.donething.android.dxutils.utils.Debug

// 普通话学习
object LearnPTH {
    fun dealLearnPTH(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // 应用使用360加固，需要先获取360的classloader
            // 参考：http://martinhan.site/2018-12-13/Android逆向之路---脱壳360加固、与xposed%20hook注意事项.html
            XposedHelpers.findAndHookMethod(
                "com.stub.StubApp", lpparam.classLoader,
                "ᵢˋ", Context::class.java, handleHookedMethod
            )
        } catch (t: Throwable) {
            Debug.log(Debug.E, "Hook普通话学习的应用时出错：$t")
        }
    }

    private val handleHookedMethod = object : XC_MethodHook() {
        override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
            try {
                //获取到360的Context对象，通过这个对象来获取classloader
                val context = param.args[0] as Context
                val classLoader = context.classLoader

                // 开始Hook方法
                // 积分
                XposedHelpers.findAndHookMethod(
                    "com.huahua.utils.PointManager", classLoader,
                    "getCurrentPoint", XC_MethodReplacement.returnConstant(10000)
                )

                // 会员
                XposedHelpers.findAndHookMethod(
                    "com.huahua.utils.PointManager", classLoader, "isProUser",
                    XC_MethodReplacement.returnConstant(true)
                )

                // 收费课程
                val courseClz = classLoader.loadClass("com.huahua.bean.Course")
                XposedHelpers.findAndHookConstructor("com.huahua.course.adapter.CourseContentAdapter",
                    classLoader, Activity::class.java, courseClz, Boolean::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                            param.args[2] = true
                        }
                    })
                XposedHelpers.findAndHookMethod("com.huahua.course.adapter.CourseContentAdapter",
                    classLoader, "setBuyCourse", Boolean::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                            param.args[0] = true
                        }
                    })
            } catch (t: Throwable) {
                Debug.log(Debug.E, "Hook普通话学习的方法时出错：$t")
            }
        }
    }
}