package com.matinkabiri.hello

/** User-facing connectivity states. */
enum class NetworkState(val label: String) {
    OPEN("OPEN"),
    WORLD("WORLD"),
    FILTER("FILTER"),
    IRAN_ONLY("IRAN ONLY"),
    LOCAL_ONLY("LOCAL ONLY"),
    DEAD("DEAD")
}

data class ProbeResult(
    val name: String,
    val url: String,
    val reachable: Boolean,
    val httpCode: Int? = null,
    val latencyMs: Long? = null,
    val error: String? = null
)

data class DiagnosticResult(
    val state: NetworkState,
    val internationalBaseline: List<ProbeResult>,
    val filteredInternational: List<ProbeResult>,
    val iranian: List<ProbeResult>,
    val local: List<ProbeResult>,
    val vpnActive: Boolean,
    val transport: String
)
