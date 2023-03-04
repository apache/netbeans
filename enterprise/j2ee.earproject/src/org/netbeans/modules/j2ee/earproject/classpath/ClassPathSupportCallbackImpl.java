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

package org.netbeans.modules.j2ee.earproject.classpath;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.j2ee.earproject.EarProjectType;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Defines the various class paths for a web project.
 * @author Petr Hrebejk, Radko Najman, David Konecny
 */
public final class ClassPathSupportCallbackImpl implements org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Callback {

    private AntProjectHelper helper;
    
    private static final String TAG_PATH_IN_EAR = "path-in-war"; //NOI18N
    private static final String TAG_FILE = "file"; //NOI18N
    private static final String TAG_LIBRARY = "library"; //NOI18N
    private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N

    /** Path of item in additional EAR content panel */
    public static final String PATH_IN_DEPLOYMENT = "pathInDeployment"; //NOI18N
    
    public ClassPathSupportCallbackImpl(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    public void readAdditionalProperties(List<Item> items, String projectXMLElement) {
        Map<String, String> earIncludesMap = createEarIncludesMap(projectXMLElement);
        for (Item item : items) {
            String deploymentPath = earIncludesMap.get(item.getReference());
            item.setAdditionalProperty(PATH_IN_DEPLOYMENT, deploymentPath);
        }
    }

    public void storeAdditionalProperties(List<Item> items, String projectXMLElement) {
        writeWebLibraries(items, helper, projectXMLElement);
    }

    private static void writeWebLibraries(List<Item> classpath, AntProjectHelper helper, String elementName ) {
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element webModuleLibs = (Element) data.getElementsByTagNameNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                elementName).item(0); //NOI18N

        //prevent NPE thrown from older projects
        if (webModuleLibs == null) {
            webModuleLibs = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, elementName); //NOI18N
            data.appendChild(webModuleLibs);
        }

        while (webModuleLibs.hasChildNodes()) {
            webModuleLibs.removeChild(webModuleLibs.getChildNodes().item(0));
        }

        for (Item item : classpath) {
            Element library = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, TAG_LIBRARY);
            webModuleLibs.appendChild(library);
            Element webFile = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, TAG_FILE);
            library.appendChild(webFile);
            String pathItem = CommonProjectUtils.getAntPropertyName(item.getReference());
            webFile.appendChild(doc.createTextNode("${" + pathItem + "}"));
            String piw = item.getAdditionalProperty(PATH_IN_DEPLOYMENT);
            if (piw != null) {
                Element pathInEar = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, TAG_PATH_IN_EAR);
                pathInEar.appendChild(doc.createTextNode(piw));
                library.appendChild(pathInEar);
            }
        }
        helper.putPrimaryConfigurationData(data, true);
    }
    
    private Map<String, String> createEarIncludesMap(String webLibraryElementName) {
        Map<String, String> earIncludesMap = new LinkedHashMap<String, String>();
        if (webLibraryElementName != null) {
            Element data = helper.getPrimaryConfigurationData(true);
            final String ns = EarProjectType.PROJECT_CONFIGURATION_NAMESPACE;
            Element webModuleLibs = (Element) data.getElementsByTagNameNS(ns, webLibraryElementName).item(0);
            if(webModuleLibs != null) {
                NodeList ch = webModuleLibs.getChildNodes();
                for (int i = 0; i < ch.getLength(); i++) {
                    if (ch.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element library = (Element) ch.item(i);
                        Node webFile = library.getElementsByTagNameNS(ns, TAG_FILE).item(0);
                        NodeList pathInEarElements = library.getElementsByTagNameNS(ns, TAG_PATH_IN_EAR);
                        earIncludesMap.put(XMLUtil.findText(webFile), pathInEarElements.getLength() > 0 ?
                            XMLUtil.findText(pathInEarElements.item(0)) : null);
                    }
                }
            }
        }
        return earIncludesMap;
    }

}

