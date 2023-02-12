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

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.NAME;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.PORT_OFFSET;
import static org.netbeans.modules.javaee.wildfly.ide.commands.Constants.SOCKET_BINDING_GROUP;
import org.xml.sax.Attributes;
import static junit.framework.TestCase.assertEquals;

public class WildflySocketBindingGroupHandlerTest {

    @Test
    public void testHandling() throws Exception {
        WildflySocketBindingGroupHandler handler = new WildflySocketBindingGroupHandler();
        final TestAttributes testAttributes = new TestAttributes();
        testAttributes.addAttribute(NAME, "standard-sockets");
        testAttributes.addAttribute(PORT_OFFSET, "${jboss.management.http.port:3455}");
        handler.startElement("", "", SOCKET_BINDING_GROUP, testAttributes);
        int standardSocketsPortOffset = handler.getStandardSocketsPortOffset();
        assertEquals(3455, standardSocketsPortOffset);
    }

    private static final class TestAttributes implements Attributes {

        private final Map<String, String> attributes = new HashMap<>();

        void addAttribute(String localName, String value) {
            attributes.put(localName, value);
        }

        @Override
        public int getLength() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public String getURI(int index) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public String getLocalName(int index) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public String getQName(int index) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public String getType(int index) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public String getValue(int index) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public int getIndex(String uri, String localName) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public int getIndex(String qName) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public String getType(String uri, String localName) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public String getType(String qName) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public String getValue(String uri, String localName) {
            return attributes.get(localName);
        }

        @Override
        public String getValue(String qName) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

    }

}
