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

import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.NAME;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.PORT_OFFSET;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.SOCKET_BINDING_GROUP;
import static org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties.DEFAULT_PORT_OFFSET;
import org.netbeans.modules.javaee.wildfly.util.WildflyDefaultValueExtractor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A {@link DefaultHandler} for Wildfly socket binding groups.
 * <p>
 * Currently, only the port offset of the standard sockets section is read. In case more information from the
 * socket binding group or the sockets defined there is needed, this class must be aádapted.
 */
public class WildflySocketBindingGroupHandler extends DefaultHandler {

    private static final String STANDARD_SOCKETS = "standard-sockets";

    private int portOffset = DEFAULT_PORT_OFFSET;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (SOCKET_BINDING_GROUP.equals(qName) && STANDARD_SOCKETS.equals(attributes.getValue(uri, NAME))) {
            String portOffsetVariable = attributes.getValue(uri, PORT_OFFSET);
            portOffset = WildflyDefaultValueExtractor.extract(portOffsetVariable)
                    .map(WildflySocketBindingGroupHandler::parsePort)
                    .orElse(DEFAULT_PORT_OFFSET);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // no need to track end of group
    }

    /**
     * Retrieve the port offset from the standard ports section of the configuration file.
     *
     * @return The offset.
     */
    public int getStandardSocketsPortOffset() {
        return portOffset;
    }

    private static Integer parsePort(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
