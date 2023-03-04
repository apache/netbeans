/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.webkit.debugging.api.dom;

import java.net.URL;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.TransportStateException;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;

/**
 * Tests of class {@code DOM}.
 *
 * @author Jan Stola
 */
public class DOMTest {
    /** ID of the root node. */
    private static final int ROOT_NODE_ID = 1;

    /**
     * Test of {@code handleDocumentUpdated} method.
     */
    @Test
    public void testHandleDocumentUpdated() {
        TransportImplementation transport = new DummyTransportImplementation();
        DOM dom = new DOM(new TransportHelper(transport), null);
        final int[] eventsFired = new int[1];
        DOM.Listener listener = new DOMAdapter() {
            @Override
            public void documentUpdated() {
                eventsFired[0]++;
            }
        };
        dom.addListener(listener);
        assertEquals(0, eventsFired[0]);
        dom.handleDocumentUpdated();
        assertEquals(1, eventsFired[0]);
        dom.removeListener(listener);
        dom.handleDocumentUpdated();
        assertEquals(1, eventsFired[0]);
    }

    /**
     * Test of {@code handleAttributeModified} method.
     */
    @Test
    public void testHandleAttributeModified() throws ParseException {
        TransportImplementation transport = new DummyTransportImplementation();
        DOM dom = new DOM(new TransportHelper(transport), null);
        final Node root = dom.getDocument();
        final String ATTR_NAME = "class"; // NOI18N
        final String ATTR_VALUE = "myclass"; // NOI18N
        final int[] eventsFired = new int[1];
        DOM.Listener listener = new DOMAdapter() {
            @Override
            public void attributeModified(Node node, String attrName, String attrValue) {
                eventsFired[0]++;
                assertEquals(ATTR_NAME, attrName);
                assertEquals(root, node);
                Node.Attribute attr = node.getAttribute(attrName);
                assertNotNull(attr);
                assertEquals(ATTR_VALUE, attr.getValue());
                assertEquals(ATTR_VALUE, attrValue);
            }
        };
        dom.addListener(listener);
        JSONParser parser = new JSONParser();

        // Modification of a known node
        Object json = parser.parse("{\"nodeId\":" + ROOT_NODE_ID + ",\"name\":\"" + // NOI18N
                ATTR_NAME + "\",\"value\":\"" + ATTR_VALUE + "\"}"); // NOI18N
        dom.handleAttributeModified((JSONObject)json);
        assertEquals(1, eventsFired[0]);

        // Modification of an unknown node
        json = parser.parse("{\"nodeId\":" + (ROOT_NODE_ID+1) + ",\"name\":\"someName\",\"value\":\"someValue\"}"); // NOI18N
        dom.handleAttributeModified((JSONObject)json);
        assertEquals(1, eventsFired[0]);
    }

    /**
     * Test of {@code handleAttributeRemoved} method.
     */
    @Test
    public void testHandleAttributeRemoved() throws ParseException {
        TransportImplementation transport = new DummyTransportImplementation();
        DOM dom = new DOM(new TransportHelper(transport), null);
        final Node root = dom.getDocument();
        final String ATTR_NAME = "class"; // NOI18N
        final int[] eventsFired = new int[1];
        DOM.Listener listener = new DOMAdapter() {
            @Override
            public void attributeRemoved(Node node, String attrName) {
                eventsFired[0]++;
                assertEquals(ATTR_NAME, attrName);
                assertEquals(root, node);
                Node.Attribute attr = node.getAttribute(attrName);
                assertNull(attr);
            }
        };
        dom.addListener(listener);
        JSONParser parser = new JSONParser();

        // Modification of a known node
        Object json = parser.parse("{\"nodeId\":" + ROOT_NODE_ID + ",\"name\":\"" + ATTR_NAME + "\"}"); // NOI18N
        dom.handleAttributeRemoved((JSONObject)json);
        assertEquals(1, eventsFired[0]);

        // Modification of an unknown node
        json = parser.parse("{\"nodeId\":" + (ROOT_NODE_ID+1) + ",\"name\":\"someName\"}"); // NOI18N
        dom.handleAttributeRemoved((JSONObject)json);
        assertEquals(1, eventsFired[0]);
    }

    /**
     * Test of {@code handleCharacterDataModified} method.
     */
    @Test
    public void testHandleCharacterDataModified() throws ParseException {
        TransportImplementation transport = new DummyTransportImplementation();
        DOM dom = new DOM(new TransportHelper(transport), null);
        final Node root = dom.getDocument();
        final String DATA = "myData"; // NOI18N
        final int[] eventsFired = new int[1];
        DOM.Listener listener = new DOMAdapter() {
            @Override
            public void characterDataModified(Node node) {
                eventsFired[0]++;
                assertEquals(root, node);
                String value = root.getNodeValue();
                assertEquals(DATA, value);
            }
        };
        dom.addListener(listener);
        JSONParser parser = new JSONParser();

        // Modification of a known node
        Object json = parser.parse("{\"nodeId\":" + ROOT_NODE_ID + ",\"characterData\":\"" + DATA + "\"}"); // NOI18N
        dom.handleCharacterDataModified((JSONObject)json);
        assertEquals(1, eventsFired[0]);

        // Modification of an unknown node
        json = parser.parse("{\"nodeId\":" + (ROOT_NODE_ID+1) + ",\"characterData\":\"someData\"}"); // NOI18N
        dom.handleCharacterDataModified((JSONObject)json);
        assertEquals(1, eventsFired[0]);
    }

    /**
     * Test of {@code handleSetChildNodes} method.
     */
    @Test
    public void testHandleSetChildNodes1() throws ParseException {
        TransportImplementation transport = new DummyTransportImplementation();
        DOM dom = new DOM(new TransportHelper(transport), null);
        final Node root = dom.getDocument();
        final int[] eventsFired = new int[1];
        DOM.Listener listener = new DOMAdapter() {
            @Override
            public void childNodesSet(Node parent) {
                eventsFired[0]++;
                assertEquals(root, parent);
                List<Node> children = parent.getChildren();
                assertNotNull(children);
                assertEquals(0, children.size());
            }
        };
        dom.addListener(listener);
        JSONParser parser = new JSONParser();

        // Modification of a known node
        Object json = parser.parse("{\"parentId\":" + ROOT_NODE_ID + ",\"nodes\":[]}"); // NOI18N
        assertNull(root.getChildren());
        dom.handleSetChildNodes((JSONObject)json);
        assertEquals(1, eventsFired[0]);

        // Modification of an unknown node
        json = parser.parse("{\"parentId\":" + (ROOT_NODE_ID+1) + ",\"nodes\":[]}"); // NOI18N
        dom.handleSetChildNodes((JSONObject)json);
        assertEquals(1, eventsFired[0]);
    }

    /**
     * Test of {@code handleSetChildNodes} method.
     */
    @Test
    public void testHandleSetChildNodes2() throws ParseException {
        TransportImplementation transport = new DummyTransportImplementation();
        DOM dom = new DOM(new TransportHelper(transport), null);
        final String childName = "HTML"; // NOI18N
        final String childChildName1 = "HEAD"; // NOI18N
        final String childChildName2 = "BODY"; // NOI18N
        final Node root = dom.getDocument();
        final int[] eventsFired = new int[1];
        DOM.Listener listener = new DOMAdapter() {
            @Override
            public void childNodesSet(Node parent) {
                eventsFired[0]++;

                // Root
                assertEquals(root, parent);
                List<Node> children = parent.getChildren();
                assertNotNull(children);
                assertEquals(1, children.size());

                // Child
                Node child = children.get(0);
                assertEquals(childName, child.getNodeName());
                assertEquals(org.w3c.dom.Node.ELEMENT_NODE, child.getNodeType());
                children = child.getChildren();
                assertNotNull(children);
                assertEquals(2, children.size());

                // 1st child of the child
                child = children.get(0);
                assertEquals(childChildName1, child.getNodeName());
                assertEquals(org.w3c.dom.Node.ELEMENT_NODE, child.getNodeType());
                assertNull(child.getChildren());

                // 2nd child
                child = children.get(1);
                assertEquals(childChildName2, child.getNodeName());
                assertEquals(org.w3c.dom.Node.ELEMENT_NODE, child.getNodeType());
                assertNull(child.getChildren());
            }
        };
        dom.addListener(listener);
        JSONParser parser = new JSONParser();

        Object json = parser.parse("{\"parentId\":" + ROOT_NODE_ID + // NOI18N
                ",\"nodes\":[{\"childNodeCount\":2,\"nodeId\":2,\"localName\":\"html\",\"nodeValue\":\"\",\"nodeName\":\"" + // NOI18N
                childName + "\",\"children\":[{\"childNodeCount\":0,\"nodeId\":3,\"localName\":\"head\",\"nodeValue\":\"\",\"nodeName\":\"" + // NOI18N
                childChildName1 + "\",\"attributes\":[],\"nodeType\":1},{\"childNodeCount\":0,\"nodeId\":4,\"localName\":\"body\",\"nodeValue\":\"\",\"nodeName\":\"" + // NOI18N
                childChildName2 +"\",\"attributes\":[],\"nodeType\":1}],\"nodeType\":1}]}"); // NOI18N
        assertNull(root.getChildren());
        dom.handleSetChildNodes((JSONObject)json);
        assertEquals(1, eventsFired[0]);
    }

    /**
     * Test of {@code handleChildNodeInserted} method.
     */
    @Test
    public void testHandleChildNodeInserted() throws ParseException {
        TransportImplementation transport = new DummyTransportImplementation();
        DOM dom = new DOM(new TransportHelper(transport), null);
        final Node root = dom.getDocument();
        final int CHILD_ID = 2;
        final String CHILD_NAME = "DIV"; // NOI18N
        final int[] eventsFired = new int[1];
        DOM.Listener listener = new DOMAdapter() {
            @Override
            public void childNodeInserted(Node parent, Node child) {
                eventsFired[0]++;
                assertEquals(root, parent);
                List<Node> children = parent.getChildren();
                assertNotNull(children);
                assertEquals(1, children.size());
                assertEquals(child, children.get(0));
                assertEquals(CHILD_ID, child.getNodeId());
                assertEquals(CHILD_NAME, child.getNodeName());
            }
        };
        dom.addListener(listener);
        JSONParser parser = new JSONParser();

        // Modification of a known node
        Object json = parser.parse("{\"node\":{\"childNodeCount\":0,\"localName\":\"div\",\"nodeId\":" + // NOI18N
                CHILD_ID + ",\"nodeValue\":\"\",\"nodeName\":\"" + // NOI18N
                CHILD_NAME + "\",\"attributes\":[],\"nodeType\":1},\"parentNodeId\":" + // NOI18N
                ROOT_NODE_ID + ",\"previousNodeId\":0}"); // NOI18N
        assertNull(root.getChildren());
        dom.handleChildNodeInserted((JSONObject)json);
        assertEquals(1, eventsFired[0]);

        // Modification of an unknown node
        json = parser.parse("{\"node\":{\"childNodeCount\":0,\"localName\":\"div\",\"nodeId\":5,\"nodeValue\":\"\",\"nodeName\":\"DIV\",\"attributes\":[],\"nodeType\":1},\"parentNodeId\":5,\"previousNodeId\":0}"); // NOI18N
        dom.handleChildNodeInserted((JSONObject)json);
        assertEquals(1, eventsFired[0]);
    }

    /**
     * Test of {@code handleChildNodeRemoved} method.
     */
    @Test
    public void testHandleChildNodeRemoved() throws ParseException {
        TransportImplementation transport = new DummyTransportImplementation();
        DOM dom = new DOM(new TransportHelper(transport), null);
        final Node root = dom.getDocument();
        final int CHILD_ID = 2;
        final String CHILD_NAME = "DIV"; // NOI18N
        final int[] eventsFired = new int[1];
        DOM.Listener listener = new DOMAdapter() {
            @Override
            public void childNodeRemoved(Node parent, Node child) {
                eventsFired[0]++;
                assertEquals(root, parent);
                List<Node> children = parent.getChildren();
                assertNotNull(children);
                assertEquals(0, children.size());
                assertEquals(CHILD_ID, child.getNodeId());
                assertEquals(CHILD_NAME, child.getNodeName());
            }
        };
        dom.addListener(listener);
        JSONParser parser = new JSONParser();

        // Node insertion
        Object json = parser.parse("{\"node\":{\"childNodeCount\":0,\"localName\":\"div\",\"nodeId\":" + // NOI18N
                CHILD_ID + ",\"nodeValue\":\"\",\"nodeName\":\"" + // NOI18N
                CHILD_NAME + "\",\"attributes\":[],\"nodeType\":1},\"parentNodeId\":" + // NOI18N
                ROOT_NODE_ID + ",\"previousNodeId\":0}"); // NOI18N
        dom.handleChildNodeInserted((JSONObject)json);
        List<Node> children = root.getChildren();
        assertNotNull(children);
        assertEquals(1, children.size());

        // Node removal
        json = parser.parse("{\"nodeId\":" + CHILD_ID + ",\"parentNodeId\":" + ROOT_NODE_ID + "}");
        dom.handleChildNodeRemoved((JSONObject)json);
        assertEquals(1, eventsFired[0]);
    }

    /**
     * Adapter for {@code DOM.Listener}. All methods are implemented by empty bodies.
     */
    static class DOMAdapter implements DOM.Listener {

        @Override
        public void documentUpdated() {}

        @Override
        public void childNodesSet(Node parent) {}

        @Override
        public void childNodeRemoved(Node parent, Node child) {}

        @Override
        public void childNodeInserted(Node parent, Node child) {}

        @Override
        public void attributeModified(Node node, String attrName, String attrValue) {}

        @Override
        public void attributeRemoved(Node node, String attrName) {}

        @Override
        public void characterDataModified(Node node) {}

        @Override
        public void shadowRootPushed(Node host, Node shadowRoot) {}

        @Override
        public void shadowRootPopped(Node host, Node shadowRoot) {}

    }

    /**
     * Dummy {@code TransportImplementation} for testing purposes.
     */
    static class DummyTransportImplementation implements TransportImplementation {
        /** Callback for receiving responses from WebKit. */
        private ResponseCallback callback;

        @Override
        public boolean attach() {
            return true;
        }

        @Override
        public boolean detach() {
            return true;
        }

        @Override
        @org.netbeans.api.annotations.common.SuppressWarnings(value="UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification="callback field should be always initialized by registerResponseCallback() method. And if not then it is fine for the method to fail. It is a test anyway.") // NOI18N
        public void sendCommand(Command command) throws TransportStateException {
            JSONObject jsonCommand = command.getCommand();
            Object id = jsonCommand.get(Command.COMMAND_ID);
            Object method = jsonCommand.get(Command.COMMAND_METHOD);
            if ("DOM.getDocument".equals(method)) { // NOI18N
                // Hack that allows initialization of the root node.
                JSONObject root = new JSONObject();
                root.put("nodeId", ROOT_NODE_ID); // NOI18N
                JSONObject result = new JSONObject();
                result.put("root", root); // NOI18N
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put(Command.COMMAND_ID, id);
                jsonResponse.put(Command.COMMAND_RESULT, result);
                Response response = new Response(jsonResponse);
                callback.handleResponse(response);
            }
        }

        @Override
        public void registerResponseCallback(ResponseCallback callback) {
            this.callback = callback;
        }

        @Override
        public String getConnectionName() {
            return "connectionName"; // NOI18N
        }

        @Override
        public URL getConnectionURL() {
            return null;
        }

        @Override
        public String getVersion() {
            return VERSION_1;
        }
        
    }

}
