package io.unbyte.sandbox.infrastructure.adapter;

import io.unbyte.sandbox.application.port.Port;

/**
 * Marker interface for adapters in hexagonal architecture
 * Adapters implement the ports defined in the application layer
 */
public interface Adapter extends Port {
    // Marker interface - no methods required
}
