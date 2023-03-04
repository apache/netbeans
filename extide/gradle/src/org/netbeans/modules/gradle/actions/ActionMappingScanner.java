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

package org.netbeans.modules.gradle.actions;

import org.netbeans.modules.gradle.api.execute.ActionMapping;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.execute.ConfigPersistenceUtils;
import org.netbeans.modules.gradle.execute.GradleExecAccessor;
import org.openide.xml.XMLUtil;
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
    
    Map<GradleExecConfiguration, Set<ActionMapping>> configs;

    /**
     * Create new ActionMappingScanner with Document.
     */
    ActionMappingScanner(Document document, Map<GradleExecConfiguration, Set<ActionMapping>> configs) {
        this.document = document;
        this.configs = configs;
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
        return loadMappings(is, new HashMap<>());
    }
    
    public static Set<ActionMapping> loadMappings(InputStream is, Map<GradleExecConfiguration, Set<ActionMapping>> configs) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        builder.setEntityResolver(DTD_RESOLVER);
        Document document = builder.parse(is);
        ActionMappingScanner scanner = new ActionMappingScanner(document, configs);
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
                    case "profiles":
                        visitElement_profiles(nodeElement);
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
    
    void visitElement_profiles(Element element) {
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element nodeElement = (Element) node;
                switch (nodeElement.getTagName()) {
                    case "profile":
                        visitElement_profile(nodeElement);
                        break;
                }
            }
        }
    }
    
    void visitElement_profile(Element profileEl) {
        String id = profileEl.getAttribute(ConfigPersistenceUtils.CONFIG_ATTRIBUTE_ID);
        if (id == null) {
            return;
        } else if ("".equals(id)) {
            id = GradleExecConfiguration.DEFAULT;
        }
        String displayName = profileEl.getAttribute(ConfigPersistenceUtils.CONFIG_ATTRIBUTE_DISPLAY);
        Map<String, String> props = new HashMap<>();
        NodeList propNodes = profileEl.getElementsByTagName(ConfigPersistenceUtils.CONFIG_ELEMENT_PROPERTY);
        for (int i = 0; i < propNodes.getLength(); i++) {
            Element p = (Element)propNodes.item(i);
            String pn = p.getAttribute(ConfigPersistenceUtils.CONFIG_ATTRIBUTE_NAME);
            if (pn == null || pn.trim().isEmpty()) {
                continue;
            }
            String pv = p.getTextContent();
            props.put(pn.trim(), pv.trim());
        }
        Element argsEl = XMLUtil.findElement(profileEl, ConfigPersistenceUtils.CONFIG_ELEMENT_ARGS, null);
        String args = argsEl == null ? "" : argsEl.getTextContent().trim();
        
        GradleExecConfiguration exec = GradleExecAccessor.instance().create(id, displayName, props, args);
        Set<ActionMapping> m = new HashSet<>();
        configs.put(exec, m);
        
        Element actionsEl = XMLUtil.findElement(profileEl, "actions", null); // NOI18N
        if (actionsEl == null) {
            return;
        }
        Set<ActionMapping> saved = mappings;
        try {
            mappings = m;
            visitElement_actions(actionsEl);
        } finally {
            mappings = saved;
        }
    }
}
