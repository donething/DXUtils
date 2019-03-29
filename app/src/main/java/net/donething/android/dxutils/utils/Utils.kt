package net.donething.android.dxutils.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ApplicationInfo

object Utils {
    /**
     * 判断是否为系统应用
     */
    fun isSysApp(appInfo: ApplicationInfo): Boolean {
        return appInfo.flags and ApplicationInfo.FLAG_SYSTEM > 0
    }

    /**
     * 弹出对话框
     */
    fun buildDialog(
        ctx: Context, title: String, msg: String,
        posText: String, posMethod: DialogInterface.OnClickListener? = null,
        negText: String? = null, negMethod: DialogInterface.OnClickListener? = null
    ): Dialog {
        val builder = AlertDialog.Builder(ctx)
        builder.setTitle(title)
            .setMessage(msg)
            .setPositiveButton(posText, posMethod)
            .setNegativeButton(negText, negMethod)
        return builder.create()
    }

    /**
     * dp转px
     * ImageView的maxWidth默认单位为px，要设置dp的值，需要先调用此方法转换
     * 参考：https://stackoverflow.com/a/35803372/8179418
     */
    fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return Math.round(dp * density)
    }
}