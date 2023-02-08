package org.netbeans.modules.javaee.wildfly.config.xml.sockets;

import org.netbeans.modules.j2ee.deployment.common.api.SocketBinding;

/**
 * A wildfly specific {@link SocketBinding}.
 */
public class WildflySocketBinding implements SocketBinding {

    private String name;
    private String interfaceName;
    private int port;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    @Override
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
