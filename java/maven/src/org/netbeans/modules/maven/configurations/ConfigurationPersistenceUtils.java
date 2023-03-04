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
package org.netbeans.modules.maven.configurations;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author mkleint
 */
public final class ConfigurationPersistenceUtils {
    public static final String NAMESPACE = "http://www.netbeans.org/ns/maven-config-data/1"; 
    public static final String ROOT = "config-data"; 
    public static final String ENABLED = "enabled"; 
    public static final String ACTIVATED = "activated";
    public static final String CONFIGURATIONS = "configurations"; 
    public static final String CONFIG = "configuration"; 
    public static final String PROPERTY = "property"; 
    public static final String PROPERTY_NAME_ATTR = "name"; 
    public static final String CONFIG_PROFILES_ATTR = "profiles"; 
    public static final String CONFIG_ID_ATTR = "id"; 
    
    private ConfigurationPersistenceUtils() {}
    
    public static String readActiveConfigurationName(AuxiliaryConfiguration config) throws DOMException {
        String active = null;
        Element el = config.getConfigurationFragment(ROOT, NAMESPACE, false);
        if (el != null) {
            NodeList list = el.getElementsByTagNameNS(NAMESPACE, ACTIVATED);
            if (list.getLength() > 0) {
                Element enEl = (Element)list.item(0);
                active = enEl.getTextContent();
            }
        }
        return active;
    }
    
    public static SortedSet<M2Configuration> readConfigurations(AuxiliaryConfiguration aux, FileObject projectDirectory, boolean shared) {
        Element el = aux.getConfigurationFragment(ROOT, NAMESPACE, shared);
        if (el != null) {
            NodeList list = el.getElementsByTagNameNS(NAMESPACE, CONFIG);
            if (list.getLength() > 0) {
                SortedSet<M2Configuration> toRet = new TreeSet<M2Configuration>();
                int len = list.getLength();
                for (int i = 0; i < len; i++) {
                    Element enEl = (Element)list.item(i);
                    
                    M2Configuration c = new M2Configuration(enEl.getAttribute(CONFIG_ID_ATTR), projectDirectory);
                    String profs = enEl.getAttribute(CONFIG_PROFILES_ATTR);
                    if (profs != null) {
                        String[] s = profs.split(" ");
                        List<String> prf = new ArrayList<String>();
                        for (String s2 : s) {
                            if (s2.trim().length() > 0) {
                                prf.add(s2.trim());
                            }
                        }
                        c.setActivatedProfiles(prf);
                    }
                    NodeList ps = enEl.getElementsByTagName(PROPERTY);
                    for (int y = 0; y < ps.getLength(); y++) {
                        Element propEl = (Element) ps.item(y);
                        String key = propEl.getAttribute(PROPERTY_NAME_ATTR);
                        String value = propEl.getTextContent();
                        if (key != null && value != null) {
                            c.getProperties().put(key, value);
                        }
                    }
                    toRet.add(c);
                }
                return toRet;
            }
        }
        return new TreeSet<M2Configuration>();
    }    
    
}
