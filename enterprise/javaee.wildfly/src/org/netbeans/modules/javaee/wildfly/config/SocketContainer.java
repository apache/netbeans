package org.netbeans.modules.javaee.wildfly.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.netbeans.modules.j2ee.deployment.common.api.SocketBinding;

/**
 * A container for {@link SocketBindings}.
 */
public class SocketContainer {

    private final String name;
    private final String defaultInterface;
    private final int portOffset;
    private final Set<SocketBinding> socketBindings = new HashSet<>();

    /**
     * Creates a new container from the given configuration values.
     *
     * @param name The container name.
     * @param defaultInterface The default interface.
     * @param portOffset The port offset.
     * @param socketBindings The {@link SocketBinding}s.
     */
    public SocketContainer(String name, String defaultInterface, int portOffset, Set<SocketBinding> socketBindings) {
        this.name = name;
        this.defaultInterface = defaultInterface;
        this.portOffset = portOffset;
        this.socketBindings.addAll(socketBindings);
    }

    public String getName() {
        return name;
    }

    public String getDefaultInterface() {
        return defaultInterface;
    }

    public int getPortOffset() {
        return portOffset;
    }

    /**
     * Retrieve all available {@link SocketBinding}s.
     *
     * @return The set of socket bindings.
     */
    public Set<SocketBinding> getSocketBindings() {
        return Collections.unmodifiableSet(socketBindings);
    }

    /**
     * Retrieve an {@link Optional} holding the {@link SocketBinding} with the given name.
     *
     * @param name The name of the socket binding.
     * @return The optional.
     */
    public Optional<SocketBinding> getSocketByName(String name) {
        return socketBindings.stream()
                .filter(s -> name.equals(s.getName()))
                .findFirst();

    }
}
