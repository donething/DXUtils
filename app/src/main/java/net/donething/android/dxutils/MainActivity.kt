package net.donething.android.dxutils

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import net.donething.android.dxutils.utils.Constants
import net.donething.android.dxutils.utils.Utils
import net.donething.android.dxutils.xp.XChecker

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        if (!XChecker.isEnabled()) {
            Log.d(Constants.TAG, "本模块还未启用")
            Utils.buildDialog(this, "请注意", "本模块还未启用", "知道了").show()
        }
    }
}
