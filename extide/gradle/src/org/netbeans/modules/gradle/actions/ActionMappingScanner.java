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

package org.netbeans.modules.gradle.actions;

import org.netbeans.modules.gradle.api.execute.ActionMapping;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class ActionMappingScanner {

    private static final EntityResolver DTD_RESOLVER = new EntityResolver() {
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if (systemId.contains("action-mapping.dtd")) {
                return new InputSource(ActionMappingScanner.class.getResourceAsStream("action-mapping.dtd"));
            }
            return null;
        }
    };
    /**
     * Document document
     */
    Document document;
    Set<String> withPlugins = Collections.<String>emptySet();
    DefaultActionMapping mapping;

    Set<ActionMapping> mappings = new HashSet<>();

    /**
     * Create new ActionMappingScanner with Document.
     */
    ActionMappingScanner(Document document) {
        this.document = document;
    }

    /**
     * Scan through Document document.
     */
    void visitDocument() {
        Element element = document.getDocumentElement();
        if ((element != null) && element.getTagName().equals("actions")) {
            visitElement_actions(element);
        }
    }

    public static Set<ActionMapping> loadMappings(InputStream is) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        builder.setEntityResolver(DTD_RESOLVER);
        Document document = builder.parse(is);
        ActionMappingScanner scanner = new ActionMappingScanner(document);
        scanner.visitDocument();
        return Collections.unmodifiableSet(scanner.mappings);
    }

    /**
     * Scan through Element named actions.
     */
    void visitElement_actions(Element element) {
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element nodeElement = (Element) node;
                switch (nodeElement.getTagName()) {
                    case "apply-for":
                        visitElement_apply_for(nodeElement);
                        break;
                    case "action":
                        visitElement_action(nodeElement);
                        break;
                }
            }
        }
    }

    /**
     * Scan through Element named apply-for.
     */
    void visitElement_apply_for(Element element) {
        // <apply-for>
        // element.getValue();
        withPlugins = new LinkedHashSet<>();
        String plugins = element.getAttribute("plugins");
        if (plugins != null) {
            withPlugins.addAll(Arrays.asList(plugins.split(",\\s*")));
        }
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element nodeElement = (Element) node;
                if (nodeElement.getTagName().equals("action")) {
                    visitElement_action(nodeElement);
                }
            }
        }
        withPlugins = Collections.<String>emptySet();
    }

    /**
     * Scan through Element named action.
     */
    void visitElement_action(Element element) {
        String name = element.getAttribute("name");
        mapping = new DefaultActionMapping(name);
        mapping.withPlugins = withPlugins;

        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            String value = attr.getValue();
            switch (attr.getName()) {
                case "displayName":
                    mapping.displayName = value;
                    break;
                case "repeatable":
                    mapping.repeatableAction = Boolean.parseBoolean(value);
                    break;
                case "priority":
                    try {
                        mapping.priority = Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                    }
                    break;
            }
        }
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element nodeElement = (Element) node;
                switch (nodeElement.getTagName()) {
                    case "reload":
                        visitElement_reload(nodeElement);
                        break;
                    case "args":
                        mapping.args = visitElement_args(nodeElement);
                        break;
                }
            }
        }
        mappings.add(mapping);
    }

    /**
     * Scan through Element named reload.
     */
    void visitElement_reload(Element element) {
        // <reload>
        // element.getValue();
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            if (attr.getName().equals("rule")) {
                mapping.reloadRule = ActionMapping.ReloadRule.valueOf(attr.getValue());
            }
        }
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    Element nodeElement = (Element) node;
                    if (nodeElement.getTagName().equals("args")) {
                        mapping.reloadArgs = visitElement_args(nodeElement);
                    }
                    break;
            }
        }
    }

    /**
     * Scan through Element named args.
     */
    String visitElement_args(Element element) {
        return element.hasChildNodes() ? ((Text) element.getFirstChild()).getData() : null;
    }

}
