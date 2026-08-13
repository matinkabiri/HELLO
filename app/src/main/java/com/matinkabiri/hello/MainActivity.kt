package com.matinkabiri.hello

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val state = findViewById<TextView>(R.id.state)
        val details = findViewById<TextView>(R.id.details)
        val refresh = findViewById<TextView>(R.id.refresh)

        fun check() {
            state.text = "Checking…"
            details.text = ""
            executor.execute {
                val result = ConnectivityChecker.check(applicationContext)
                runOnUiThread {
                    state.text = result.state.label
                    details.text = buildString {
                        append("Transport: ${result.transport}\n")
                        append("VPN detected: ${if (result.vpnActive) "yes" else "no"}\n\n")
                        append("International: ${reachable(result.internationalBaseline)}/${result.internationalBaseline.size}\n")
                        append("Filtered: ${reachable(result.filteredInternational)}/${result.filteredInternational.size}\n")
                        append("Iranian: ${reachable(result.iranian)}/${result.iranian.size}\n")
                        append("Local: ${reachable(result.local)}/${result.local.size}")
                    }
                }
            }
        }

        refresh.setOnClickListener { check() }
        check()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun reachable(results: List<ProbeResult>): Int = results.count { it.reachable }
}
