# Contributing to Hello?

Thank you for contributing to Hello?.

## Development principles

- Keep the project open source.
- Keep changes reproducible from the public source tree.
- Preserve existing copyright and attribution notices.
- Add accurate credit for new contributors.
- Do not silently remove or replace previous developer attribution.
- Prefer small, reviewable changes.

## Connectivity probes

Probe endpoints are part of the application's behavior. When adding or replacing a probe, document:

- what it tests;
- which network class it represents;
- why it is a useful reference;
- what failure modes are expected; and
- whether it introduces privacy, availability, or regional reliability concerns.

A single endpoint should not be treated as authoritative for an entire class of connectivity.

## Classifier changes

The six user-facing states are:

- OPEN
- WORLD
- FILTER
- IRAN ONLY
- LOCAL ONLY
- DEAD

Changes to classification rules should include examples showing why a probe combination maps to a particular state.

## Attribution

Contributors retain copyright in their original contributions unless they separately agree otherwise. By submitting a contribution for inclusion, you confirm that you have the necessary rights to contribute it under the project's licensing terms.

See `LICENSE`, `ATTRIBUTION-TERMS.md`, and `AUTHORS.md`.
