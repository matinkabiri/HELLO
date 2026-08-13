package com.matinkabiri.hello

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.concurrent.Executors

class HelloWidgetProvider : AppWidgetProvider() {
    companion object {
        const val ACTION_CHECK = "com.matinkabiri.hello.CHECK"
        private val executor = Executors.newCachedThreadPool()

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, ids: IntArray, label: String) {
            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.hello_widget)
                views.setTextViewText(R.id.hello_title, "Hello?")
                views.setTextViewText(R.id.hello_state, label)

                val intent = Intent(context, HelloWidgetProvider::class.java).apply {
                    action = ACTION_CHECK
                }
                val pending = PendingIntent.getBroadcast(
                    context,
                    id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.hello_root, pending)
                appWidgetManager.updateAppWidget(id, views)
            }
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        updateWidget(context, manager, ids, "Checking…")
        checkAndUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_CHECK) checkAndUpdate(context)
    }

    private fun checkAndUpdate(context: Context) {
        val pendingResult = goAsync()
        executor.execute {
            try {
                val result = ConnectivityChecker.check(context.applicationContext)
                val manager = AppWidgetManager.getInstance(context)
                val component = ComponentName(context, HelloWidgetProvider::class.java)
                val ids = manager.getAppWidgetIds(component)
                updateWidget(context, manager, ids, result.state.label)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
