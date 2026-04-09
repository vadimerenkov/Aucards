package vadimerenkov.aucards.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_USER_UNLOCKED
        ) {
            val scope = MainScope()
            scope.launch {
                CardWidget().updateAll(context)
            }
        }
    }
}
