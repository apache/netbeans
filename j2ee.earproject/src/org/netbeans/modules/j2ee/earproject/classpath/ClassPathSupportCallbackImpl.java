/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

