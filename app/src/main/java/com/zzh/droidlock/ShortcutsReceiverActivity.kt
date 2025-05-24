package com.zzh.droidlock

import android.app.Activity
import android.os.Bundle
import com.zzh.droidlock.dpm.getDPM

class ShortcutsReceiverActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if(intent.action == "com.zzh.droidlock.action.LOCK") {
                getDPM().lockNow()
            }
        } catch(_: Exception) {}
        finish()
    }
}