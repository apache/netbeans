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
package org.netbeans.modules.gradle.execute;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Handles (de)serialization of {@link GradleExecConfiguration} and the default config selection.
 * @author sdedic
 */
public final class ConfigPersistenceUtils {
    /**
     * The namespace shared for gradle project information
     */
    public static final String CONFIG_NAMESPACE = "http://www.netbeans.org/ns/gradle-config/data/1";
    
    private static final String CONFIG_ELEMENT_CONFIGURATIONS = "configurations"; // NOI18N
    private static final String CONFIG_ELEMENT_CONFIGURATION = "configuration"; // NOI18N
    private static final String CONFIG_ATTRIBUTE_ACTIVE = "active"; // NOI18N
    
    // these are also used by XML reader.
    public static final String CONFIG_ELEMENT_ARGS = "args"; // NOI18N
    public static final String CONFIG_ELEMENT_PROPERTY = "property"; // NOI18N
    public static final String CONFIG_ATTRIBUTE_ID = "id"; // NOI18N
    public static final String CONFIG_ATTRIBUTE_NAME = "name"; // NOI18N
    public static final String CONFIG_ATTRIBUTE_DISPLAY = "displayName"; // NOI18N
    
    public static void writeConfigurations(List<GradleExecConfiguration> configs, AuxiliaryConfiguration aux, String configId, boolean shared) {
        writeConfigurations0(configs, aux, configId, shared);
    }

    // for testing purposes
    static Element writeConfigurations0(List<GradleExecConfiguration> configs, AuxiliaryConfiguration aux, String configId, boolean shared) {
        Element el = aux.getConfigurationFragment(CONFIG_ELEMENT_CONFIGURATIONS, CONFIG_NAMESPACE, shared);
        boolean defConfig = shared || (null == configId || GradleExecConfiguration.DEFAULT.equals(configId));
        if (el == null) {
            if (configs.isEmpty() && defConfig) {
                return null;
            }
            el = XMLUtil.createDocument(CONFIG_ELEMENT_CONFIGURATIONS, CONFIG_NAMESPACE, null, null).getDocumentElement();
            if (!defConfig) {
                el.setAttribute(CONFIG_ATTRIBUTE_ACTIVE, configId);
            }
        } else {
            if (configs.isEmpty() && defConfig) {
                aux.removeConfigurationFragment(CONFIG_ELEMENT_CONFIGURATIONS, CONFIG_NAMESPACE, shared);
                return null;
            }
        }
        
        // remove all:
        NodeList list = el.getElementsByTagNameNS(CONFIG_NAMESPACE, CONFIG_ELEMENT_CONFIGURATION);
        for (int i = 0; i < list.getLength(); i++) {
            el.removeChild(list.item(i));
        }
        for (GradleExecConfiguration cfg : configs) {
            Element child  = el.getOwnerDocument().createElementNS(CONFIG_NAMESPACE, CONFIG_ELEMENT_CONFIGURATION);
            el.appendChild(child);
            child.setAttribute(CONFIG_ATTRIBUTE_ID, cfg.getId());
            if (cfg.getName() != null) {
                child.setAttribute(CONFIG_ATTRIBUTE_DISPLAY, cfg.getName());
            }
            Map<String, String> projectProps = cfg.getProjectProperties();
            if (!(projectProps == null || projectProps.isEmpty())) {
                for (Map.Entry<String, String> it : projectProps.entrySet()) {
                    String s = it.getKey();
                    if (s == null || s.trim().isEmpty()) {
                        continue;
                    }
                    String k = s.trim();
                    String v = it.getValue();
                    if (v == null) {
                        v = ""; // NOI18N
                    }
                    Element prop  = el.getOwnerDocument().createElementNS(CONFIG_NAMESPACE, CONFIG_ELEMENT_PROPERTY);
                    prop.setAttribute(CONFIG_ATTRIBUTE_NAME, k);
                    prop.setTextContent(v);
                    child.appendChild(prop);
                }
            }
            String args = cfg.getCommandLineArgs();
            if (!(args == null || args.trim().isEmpty())) {
                Element argsEl  = el.getOwnerDocument().createElementNS(CONFIG_NAMESPACE, CONFIG_ELEMENT_ARGS);
                argsEl.setTextContent(args.trim());
                child.appendChild(argsEl);
            }
        }
        
        aux.putConfigurationFragment(el, shared);
        return null;
    }
    
    public static Map<String, GradleExecConfiguration> readConfigurations(Map<String, GradleExecConfiguration> result, AuxiliaryConfiguration aux, boolean shared) {
        Element el = aux.getConfigurationFragment(CONFIG_ELEMENT_CONFIGURATIONS, CONFIG_NAMESPACE, shared);
        if (el != null) {
            NodeList nl = el.getElementsByTagNameNS(CONFIG_NAMESPACE, CONFIG_ELEMENT_CONFIGURATION);
            for (int i = 0; i < nl.getLength(); i++) {
                Element c = (Element)nl.item(i);
                String id = c.getAttribute(CONFIG_ATTRIBUTE_ID);
                if (id == null) {
                    continue;
                }
                String dispString = c.getAttribute(CONFIG_ATTRIBUTE_DISPLAY);
                Map<String, String> projectProps = new LinkedHashMap<>();
                String cmdArgs = null;
                
                NodeList props = c.getElementsByTagNameNS(CONFIG_NAMESPACE, CONFIG_ELEMENT_PROPERTY);
                for (int pi = 0; pi < props.getLength(); pi++) {
                    Element p = (Element)props.item(pi);
                    String n = p.getAttribute(CONFIG_ATTRIBUTE_NAME);
                    String v = p.getTextContent();
                    if (n != null && !n.isEmpty()) {
                        if (v == null) {
                            v = ""; // NOI18N
                        }
                        projectProps.put(n, v);
                    }
                }
                Element argsEl = XMLUtil.findElement(c, CONFIG_ELEMENT_ARGS, CONFIG_NAMESPACE);
                if (argsEl != null) {
                    cmdArgs = argsEl.getTextContent();
                }
                result.put(id, GradleExecAccessor.instance().create(id, dispString, projectProps, cmdArgs));
            }
        }
        return result;
    }

    public static void writeActiveConfiguration(AuxiliaryConfiguration aux, String s, boolean emptyConfigs) {
        Element el = aux.getConfigurationFragment(CONFIG_ELEMENT_CONFIGURATIONS, CONFIG_NAMESPACE, false);
        
        if (s == null || GradleExecConfiguration.DEFAULT.equals(s)) {
            // the default configuration is selected. 
            if (el != null && emptyConfigs) {
                aux.removeConfigurationFragment(CONFIG_ELEMENT_CONFIGURATIONS, CONFIG_NAMESPACE, false);
                return;
            } else {
                el.removeAttribute(CONFIG_ATTRIBUTE_ACTIVE);
            }
        } else {
            if (el == null) {
                el = XMLUtil.createDocument(CONFIG_ELEMENT_CONFIGURATIONS, CONFIG_NAMESPACE, null, null).getDocumentElement();
            }
            el.setAttribute(CONFIG_ATTRIBUTE_ACTIVE, s);
        }
        aux.putConfigurationFragment(el, false);
    }

    public static String readActiveConfiguration(AuxiliaryConfiguration aux) {
        Element el = aux.getConfigurationFragment(CONFIG_ELEMENT_CONFIGURATIONS, CONFIG_NAMESPACE, false);
        if (el == null) {
            return GradleExecConfiguration.DEFAULT;
        }
        String c = el.getAttribute(CONFIG_ATTRIBUTE_ACTIVE);
        return c == null ? GradleExecConfiguration.DEFAULT : c;
    }
    
}
