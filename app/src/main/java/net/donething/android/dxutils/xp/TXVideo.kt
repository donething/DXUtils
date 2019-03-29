package net.donething.android.dxutils.xp

import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.donething.android.dxutils.utils.Debug

// 腾讯视频
object TXVideo {
    fun dealTXVideo(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            removeSplashAD(lpparam)
            skipVideoAD(lpparam)
        } catch (t: Throwable) {
            Debug.log(Debug.E, "Hook 腾讯视频时出错：$t")
            t.printStackTrace()
        }
    }

    /**
     * 腾讯视频的启动广告
     * com.tencent.qqlive.ona.activity.WelcomeActivity.onCreate()
     * 启动广告由m3465j()的返回值觉得，返回true，则不加载；返回false则加载
     * try {
     * m3465j() renamed from: j
     * // 启动广告的载入条件
     * if (m3465j()) {
     * m3472q();
     * finish();
     * C2639b.m5501b();
     * return;
     * }
     * C2639b.m5501b();
     * C2516a.m5012a(this, this);
     * QQLiveLog.m5552i("WelcomeActivity", "flavor = , buildType = google, isDebug = false, applicationId = com.tencent.qqlive");
     * } finally {
     * C2639b.m5501b();
     * }
     */
    private fun removeSplashAD(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            "com.tencent.qqlive.ona.activity.WelcomeActivity",
            lpparam.classLoader, "j", XC_MethodReplacement.returnConstant(true)
        )
    }

    /**
     * 播放视频时跳过广告
     * videoInfo.getPayState()的返回值为用户等级（）
     * 入口：com.tencent.qqlive.ona.player.plugin.PayVipController
     * private String getTryWatchingTips(VideoInfo videoInfo) {
     * switch (videoInfo.getPayState()) {
     * case 4:
     * if (!LoginManager.getInstance().isLogined() || !LoginManager.getInstance().isVip()) {
     * return C1163p.m2611a((int) C3198R.string.try_watching_player_top_tips_no_ticket_not_login, r0);
     * } else if (LoginManager.getInstance().getTicketTotal() > 0) {
     * return C1163p.m2611a((int) C3198R.string.try_watching_player_top_tips_have_ticket, r0);
     * } else {
     * return C1163p.m2611a((int) C3198R.string.try_watching_player_top_tips_repay_ticket, r0);
     * }
     * case 5:
     * case 6:
     * return C1163p.m2611a((int) C3198R.string.try_watching_player_top_tips_vip_only, r0);
     * case 7:
     * return C1163p.m2611a((int) C3198R.string.try_watching_player_top_tips_cash_only, r0);
     * default:
     * return "";
     * }
     * }
     */
    private fun skipVideoAD(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            "com.tencent.qqlive.ona.player.VideoInfo",
            lpparam.classLoader, "isAdSkip", XC_MethodReplacement.returnConstant(true)
        )
    }
}