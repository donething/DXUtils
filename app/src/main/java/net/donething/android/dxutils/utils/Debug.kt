package net.donething.android.dxutils.utils

import android.util.Log

enum class Debug {
    V, D, I, W, E, A;

    companion object {
        fun log(level: Debug, msg: String) {
            Log.d(Constants.TAG, msg)
        }
    }
}
