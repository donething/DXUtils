package net.donething.android.dxutils.xp

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import net.donething.android.dxutils.utils.Debug
import net.donething.android.dxutils.utils.Utils

// Toast显示应用图标
object ToastIcon {
    // 注入资源文件，参考：https://blog.csdn.net/u010746456/article/details/80084209
    private const val toastIconTag = "dxutils_toast_icon_tag"

    fun dealToastIcon() {
        try {
            XposedHelpers.findAndHookMethod(Toast::class.java, "show", handleHookedMethod)
        } catch (t: Throwable) {
            Debug.log(Debug.E, "Hook Toast出错：$t")
            t.printStackTrace()
        }
    }

    private val handleHookedMethod = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
            try {
                // 获取Toast中的Context实例
                val mContext = XposedHelpers.findField(Toast::class.java, "mContext")
                    .get(param.thisObject) as Context
                // Toast弹出的View，此为修改目标
                val mNextViewField = XposedHelpers.findField(Toast::class.java, "mNextView")
                val mNextView = mNextViewField.get(param.thisObject) as View

                // 因为本模块的操作是将应用图标ImageView和Toast中的View合并后存到mNextView中
                // 这可能导致mNextView已存在应用图标，而之后仍会增加图片的问题
                // 所以此时判断，已存在应用图标时，先将图标移除
                var nextChild: View?
                if (mNextView is ViewGroup) {
                    for (i in 0 until mNextView.childCount) {
                        nextChild = mNextView.getChildAt(i)
                        if (nextChild != null && nextChild.tag != null &&
                            nextChild.tag.toString() == toastIconTag
                        ) {
                            mNextView.removeViewAt(i)
                        }
                    }
                }

                // 动态添加view参考：https://www.cnblogs.com/liqw/p/4084282.html
                val layout = LinearLayout(mContext)
                layout.orientation = LinearLayout.VERTICAL

                // 添加应用图标
                val imgView = ImageView(mContext)
                imgView.tag = toastIconTag
                imgView.adjustViewBounds = true
                // ImageView的maxWidth默认单位为px，要设置dp的值，需要先将dp转为px
                imgView.maxHeight = Utils.dpToPx(30, mContext)
                imgView.setImageDrawable(
                    mContext.packageManager
                        .getApplicationIcon(mContext.packageName)
                )
                layout.addView(imgView)

                layout.addView(mNextView)

                mNextViewField.set(param.thisObject, layout)
            } catch (t: Throwable) {
                Debug.log(Debug.E, "Hook Toast显示图标时出错：$t")
                t.printStackTrace()
            }
        }
    }
}
