/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javaee.wildfly.config.xml.sockets;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.common.api.SocketBinding;
import org.netbeans.modules.javaee.wildfly.config.SocketContainer;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.DEFAULT_INTERFACE;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.INTERFACE;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.NAME;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.PORT;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.PORT_OFFSET;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.SOCKET_BINDING;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.SOCKET_BINDING_GROUP;
import org.netbeans.modules.javaee.wildfly.util.WildflyVariableResolver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A {@link DefaultHandler} for Wildfly socket bindings.
 */
public class WildflySocketBindingsHandler extends DefaultHandler {

    private static final Logger LOG = Logger.getLogger(WildflySocketBindingsHandler.class.getName());

    private static final int DEFAULT_PORT_OFFSET = 0;

    private final Set<SocketBinding> socketBindings = new HashSet<>();

    private boolean isSocketBinding = false;
    private String name;
    private String defaultInterface;
    private int portOffset;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        isSocketBinding = isSocketBinding || SOCKET_BINDING_GROUP.equals(qName);
        if (isSocketBinding) {
            if (SOCKET_BINDING_GROUP.equals(qName)) {
                name = attributes.getValue(uri, NAME);
                defaultInterface = attributes.getValue(uri, DEFAULT_INTERFACE);
                portOffset = parsePort(resolveExpression(attributes.getValue(uri, PORT_OFFSET)), DEFAULT_PORT_OFFSET);
            } else if (SOCKET_BINDING.equals(qName)) {
                WildflySocketBinding socketBinding = new WildflySocketBinding();
                socketBinding.setName(attributes.getValue(uri, NAME));
                socketBinding.setInterfaceName(valueOrDefaultInterface(attributes.getValue(uri, INTERFACE)));
                int port = WildflySocketBindingsHandler.parsePort(resolveExpression(attributes.getValue(uri, PORT)), null);
                socketBinding.setPort(port + portOffset);
                socketBindings.add(socketBinding);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // no need to track end of group
    }

    public SocketContainer getSocketContainer() {
        return new SocketContainer(name, defaultInterface, portOffset, socketBindings);
    }

    private static int parsePort(String value, Integer fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            if (fallback == null) {
                throw e;
            }
            LOG.warning(String.format("The socket binding contained a non-parseable port: '%s', using fallback '%s'", value, fallback));
        }
        return fallback;
    }

    private static String resolveExpression(String expression) {
        return WildflyVariableResolver.resolve(expression)
                .orElse(null);
    }

    private String valueOrDefaultInterface(String parsedInterface) {
        if (parsedInterface == null) {
            return defaultInterface;
        }
        return parsedInterface;
    }
}
