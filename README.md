# Hello?

**Hello?** is a small, open-source Android connectivity detector that answers a deceptively simple question:

> **Hello?**

Instead of merely checking whether a device is connected to Wi-Fi or mobile data, Hello? tests what parts of the network are actually reachable and classifies the connection into a simple human-readable state.

## Connectivity states

Hello? uses six states:

### OPEN

Open international access.

International services are reachable normally.

### WORLD

Both Iranian and international services are reachable simultaneously.

This can occur when traffic is routed differently depending on its destination, for example through a rule-based VPN or proxy.

### FILTER

International Internet is available, but some major international services are inaccessible.

For example, general international connectivity may work while services such as YouTube, Telegram, or Facebook do not.

### IRAN ONLY

Only Iranian or national-network services are reachable.

International Internet is unavailable, while services hosted within the Iranian network remain accessible.

### LOCAL ONLY

The Internet is unavailable, but the local network is still functioning.

This includes networks created through:

- Wi-Fi routers
- Phone hotspots
- Ethernet
- USB tethering
- Other local-network connections

Local devices, gateways, and local DNS services may still be reachable.

### DEAD

No meaningful network connectivity is available.

Neither the wider Internet nor the local network can be reached.

---

## The idea

Hello? does not simply ask Android:

> "Are you connected?"

It asks:

> "What can you actually reach?"

The classification is based on several groups of connectivity tests.

### International services

Examples include:

- Cloudflare
- Google
- Google connectivity endpoints
- IP information services
- YouTube
- Telegram
- Facebook

### Iranian services

Examples include:

- Iran.ir
- Soft98
- Blubank
- Digikala
- Aparat
- Other carefully selected Iranian services

### Local network

Hello? can also test the local network itself, including:

- Default gateway
- Local IP addresses
- Local DNS
- Local hostnames
- Local devices and services
- Hotspot-connected devices

The exact probe list may change as the project develops.

---

## Why multiple probes?

A single website is not enough to determine whether the Internet is working.

A website can be:

- temporarily unavailable
- returning an error
- blocking a particular IP
- behind a CDN
- experiencing an outage
- refusing a request while the network itself is perfectly functional

Hello? therefore uses multiple independent probes and evaluates them as groups rather than treating one failed website as proof that the Internet is unavailable.

HTTP status codes are also recorded separately from actual network failures.

For example:

- `2xx` — successful response
- `3xx` — reachable, redirected
- `4xx` — reachable, but request denied/not found/etc.
- `5xx` — reachable, but server-side failure
- timeout — no response within the configured timeout
- DNS failure — hostname could not be resolved
- connection failure — connection could not be established

A server returning `404` is therefore not equivalent to a server being unreachable.

---

## The widget

The primary interface is intentionally minimal.

The widget asks:

> **Hello?**

Tapping it gives the current state:

> **World.**

or:

> **Filter.**

or:

> **Iran Only.**

or:

> **Local Only.**

or:

> **Dead.**

The detailed diagnostic information is available after opening the full application.

The goal is to keep the widget useful at a glance without turning it into another network-monitoring dashboard.

---

## Connectivity classification

The basic classification model is:

```mermaid
flowchart TD
    A[START] --> B{International websites work?}

    B -->|YES| C{Iranian websites work?}
    B -->|NO| D{Iranian websites work?}

    C -->|NO| E[OPEN]
    C -->|YES| F{Blocked international sites work?}

    F -->|YES| G[WORLD]
    F -->|NO| J[FILTER]

    D -->|YES| I[IRAN ONLY]
    D -->|NO| H{Local network works?}

    H -->|YES| K[LOCAL ONLY]
    H -->|NO| L[DEAD]
