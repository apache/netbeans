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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
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
    
    public static Map<String, GradleExecConfiguration> readConfigurations(Map<String, GradleExecConfiguration> result, AuxiliaryConfiguration aux, boolean shared) {
        Element el = aux.getConfigurationFragment(CONFIG_ELEMENT_CONFIGURATIONS, CONFIG_NAMESPACE, false);
        if (el != null) {
            NodeList nl = el.getElementsByTagNameNS(CONFIG_NAMESPACE, CONFIG_ELEMENT_CONFIGURATION);
            for (int i = 0; i < nl.getLength(); i++) {
                Element c = (Element)nl.item(i);
                String id = c.getAttribute(CONFIG_ATTRIBUTE_ID);
                if (id == null) {
                    continue;
                }
                String dispString = c.getAttribute(CONFIG_ATTRIBUTE_DISPLAY);
                Map<String, String> projectProps = new HashMap<>();
                String cmdArgs = null;
                
                NodeList props = el.getElementsByTagNameNS(CONFIG_NAMESPACE, CONFIG_ELEMENT_PROPERTY);
                for (int pi = 0; i < props.getLength(); pi++) {
                    Element p = (Element)nl.item(pi);
                    String n = p.getAttribute(CONFIG_ATTRIBUTE_NAME);
                    String v = p.getTextContent();
                    if (n != null) {
                        if (v == null) {
                            v = ""; // NOI18N
                        }
                        projectProps.put(n, v);
                    }
                }
                Element argsEl = XMLUtil.findElement(el, CONFIG_ELEMENT_ARGS, CONFIG_NAMESPACE);
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
