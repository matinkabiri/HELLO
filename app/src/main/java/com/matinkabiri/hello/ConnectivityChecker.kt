package com.matinkabiri.hello

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * Small, dependency-free probe engine for the first Hello? prototype.
 *
 * A HTTP 4xx/5xx response is considered reachable: the server answered.
 * Timeouts, DNS failures and connection failures are considered unreachable.
 */
object ConnectivityChecker {
    private const val TIMEOUT_MS = 3500

    private data class Endpoint(val name: String, val url: String)

    private val internationalBaseline = listOf(
        Endpoint("Google", "https://www.google.com/generate_204"),
        Endpoint("Google static", "https://www.gstatic.com/generate_204"),
        Endpoint("IP info", "https://api.ip.sb/geoip")
    )

    private val filteredInternational = listOf(
        Endpoint("YouTube", "https://www.youtube.com/generate_204"),
        Endpoint("Telegram", "https://telegram.org/"),
        Endpoint("Facebook", "https://www.facebook.com/")
    )

    private val iranian = listOf(
        Endpoint("Iran.ir", "https://iran.ir/"),
        Endpoint("Soft98", "https://soft98.ir/"),
        Endpoint("Blubank", "https://blubank.com/"),
        Endpoint("Digikala", "https://www.digikala.com/"),
        Endpoint("Aparat", "https://www.aparat.com/")
    )

    fun check(context: Context): DiagnosticResult {
        val executor = Executors.newFixedThreadPool(6)
        return try {
            val groups = listOf(internationalBaseline, filteredInternational, iranian).map { endpoints ->
                executor.submit(Callable { endpoints.map(::probe) })
            }

            val baseline = groups[0].get()
            val filtered = groups[1].get()
            val iran = groups[2].get()
            val local = localProbes(context)

            val baselineWorks = baseline.any { it.reachable }
            val iranWorks = iran.any { it.reachable }
            val filteredWorks = filtered.any { it.reachable }
            val localWorks = local.any { it.reachable }

            val state = when {
                baselineWorks && !iranWorks -> NetworkState.OPEN
                baselineWorks && iranWorks && filteredWorks -> NetworkState.WORLD
                baselineWorks && iranWorks -> NetworkState.FILTER
                !baselineWorks && iranWorks -> NetworkState.IRAN_ONLY
                !baselineWorks && !iranWorks && localWorks -> NetworkState.LOCAL_ONLY
                else -> NetworkState.DEAD
            }

            DiagnosticResult(
                state = state,
                internationalBaseline = baseline,
                filteredInternational = filtered,
                iranian = iran,
                local = local,
                vpnActive = hasVpn(context),
                transport = transport(context)
            )
        } finally {
            executor.shutdownNow()
        }
    }

    private fun probe(endpoint: Endpoint): ProbeResult {
        val start = System.nanoTime()
        return try {
            val connection = (URL(endpoint.url).openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                instanceFollowRedirects = false
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Hello?/0.1")
            }
            val code = connection.responseCode
            connection.disconnect()
            ProbeResult(endpoint.name, endpoint.url, true, code, elapsedMs(start))
        } catch (e: Exception) {
            ProbeResult(endpoint.name, endpoint.url, false, null, elapsedMs(start), e.javaClass.simpleName)
        }
    }

    private fun localProbes(context: Context): List<ProbeResult> {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val link = cm.getLinkProperties(cm.activeNetwork)
        val probes = mutableListOf<ProbeResult>()

        link?.gateway?.hostAddress?.let { gateway ->
            probes += socketProbe("Gateway", gateway, 53)
            if (!probes.last().reachable) probes += socketProbe("Gateway HTTP", gateway, 80)
        }

        link?.dnsServers?.firstOrNull()?.hostAddress?.let { dns ->
            probes += socketProbe("Local DNS", dns, 53)
        }

        return probes
    }

    private fun socketProbe(name: String, host: String, port: Int): ProbeResult {
        val start = System.nanoTime()
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), TIMEOUT_MS)
            }
            ProbeResult(name, "$host:$port", true, latencyMs = elapsedMs(start))
        } catch (e: Exception) {
            ProbeResult(name, "$host:$port", false, latencyMs = elapsedMs(start), error = e.javaClass.simpleName)
        }
    }

    private fun hasVpn(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        return cm.allNetworks.any { network ->
            cm.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        }
    }

    private fun transport(context: Context): String {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return "NONE"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "Other"
        }
    }

    private fun elapsedMs(start: Long): Long = (System.nanoTime() - start) / 1_000_000
}
