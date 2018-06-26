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

package org.netbeans.modules.web.project.classpath;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.javaee.project.api.ant.AntProjectConstants;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.javaee.project.api.ant.AntProjectUtil;
import org.netbeans.modules.web.project.WebProjectType;
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
    
    public static final String PATH_IN_DEPLOYMENT = "pathInDeployment";

    public ClassPathSupportCallbackImpl(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    public void readAdditionalProperties(List<Item> items, String projectXMLElement) {
        Map<String, String> warIncludesMap = createWarIncludesMap(helper, projectXMLElement);
        for (Item item : items) {
            String property = CommonProjectUtils.getAntPropertyName( item.getReference() );
            String deploymentPath = warIncludesMap.get(property);
            item.setAdditionalProperty(PATH_IN_DEPLOYMENT, deploymentPath);
            item.setAdditionalProperty(AntProjectConstants.DESTINATION_DIRECTORY,
                warIncludesMap.get(property+"."+AntProjectConstants.DESTINATION_DIRECTORY));
        }
    }

    public void storeAdditionalProperties(List<Item> items, String projectXMLElement) {
        putIncludedLibraries(items, helper, projectXMLElement);
    }

    private static final String TAG_PATH_IN_WAR = "path-in-war"; //NOI18N
    private static final String TAG_FILE = "file"; //NOI18N
    private static final String TAG_LIBRARY = "library"; //NOI18N
    //private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N
    
    public final static String TAG_WEB_MODULE_LIBRARIES = "web-module-libraries"; // NOI18N
    public final static String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; // NOI18N

    public static final String PATH_IN_WAR_LIB = "WEB-INF/lib"; //NOI18N
    public static final String PATH_IN_WAR_DIR = "WEB-INF/classes"; //NOI18N
    public static final String PATH_IN_WAR_NONE = null;
    
    private static Map<String, String> createWarIncludesMap(AntProjectHelper uh, String webModuleLibraries) {
        Map<String, String> warIncludesMap = new LinkedHashMap<String, String>();
        //try all supported namespaces starting with the newest one
        for(int idx = WebProjectType.getConfigurationNamespaceList().length - 1; idx >= 0; idx--) {
            String ns = WebProjectType.getConfigurationNamespaceList()[idx];
            Element data = uh.createAuxiliaryConfiguration().getConfigurationFragment("data",ns,true);
            if(data != null) {
                Element webModuleLibs = (Element) data.getElementsByTagNameNS(ns, webModuleLibraries).item(0);
                if (webModuleLibs != null) {
                    NodeList ch = webModuleLibs.getChildNodes();
                    for (int i = 0; i < ch.getLength(); i++) {
                        if (ch.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Element library = (Element) ch.item(i);
                            String dirs = library.getAttribute(ATTR_DIRS);
                            if (!AntProjectConstants.DESTINATION_DIRECTORY_ROOT.equals(dirs) &&
                                !AntProjectConstants.DESTINATION_DIRECTORY_LIB.equals(dirs) &&
                                !AntProjectConstants.DESTINATION_DIRECTORY_DO_NOT_COPY.equals(dirs)) {
                                dirs = null;
                            }
                            Node webFile = library.getElementsByTagNameNS(ns, TAG_FILE).item(0);
                            NodeList pathInWarElements = library.getElementsByTagNameNS(ns, TAG_PATH_IN_WAR);
                            //remove ${ and } from the beginning and end
                            String webFileText = XMLUtil.findText(webFile);
                            webFileText = webFileText.substring(2, webFileText.length() - 1);
                            
                            //#86522
                            if (webModuleLibraries.equals(TAG_WEB_MODULE__ADDITIONAL_LIBRARIES)) {
                                String pathInWar = PATH_IN_WAR_NONE;
                                if (pathInWarElements.getLength() > 0) {
                                    pathInWar = XMLUtil.findText((Element) pathInWarElements.item(0));
                                    if (pathInWar == null)
                                        pathInWar = "";
                                }
                                warIncludesMap.put(webFileText, pathInWar);
                            } else {
                                warIncludesMap.put(webFileText, pathInWarElements.getLength() > 0 ? XMLUtil.findText((Element) pathInWarElements.item(0)) : PATH_IN_WAR_NONE);
                            }
                            if (dirs != null) {
                                warIncludesMap.put(webFileText+"."+AntProjectConstants.DESTINATION_DIRECTORY, dirs);
                            }
                        }
                    }
                    return warIncludesMap;
                }
            }
        }
        return warIncludesMap; //return an empy map
    }

    /**
     * Updates the project helper with the list of classpath items which are to be
     * included in deployment.
     */
    private static void putIncludedLibraries( List<Item> classpath, AntProjectHelper antProjectHelper, String webModuleLibraries ) {
        assert antProjectHelper != null;
        assert webModuleLibraries != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        Document doc = data.getOwnerDocument();
        Element webModuleLibs = (Element) data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, webModuleLibraries).item(0);
        if (webModuleLibs == null) {
            webModuleLibs = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, webModuleLibraries); //NOI18N
            data.appendChild(webModuleLibs);
        }
        while (webModuleLibs.hasChildNodes()) {
            webModuleLibs.removeChild(webModuleLibs.getChildNodes().item(0));
        }
        
        for (Item item : classpath) {
            webModuleLibs.appendChild(createLibraryElement(antProjectHelper, doc, 
                CommonProjectUtils.getAntPropertyName( item.getReference() ), item));
        }

        antProjectHelper.putPrimaryConfigurationData( data, true );
    }
    
    private static Element createLibraryElement(AntProjectHelper antProjectHelper, Document doc, String pathItem, Item item) {
        Element libraryElement = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, TAG_LIBRARY);
        Element webFile = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, TAG_FILE);
        libraryElement.appendChild(webFile);
        webFile.appendChild(doc.createTextNode("${" + pathItem + "}"));
        if (item.getAdditionalProperty(PATH_IN_DEPLOYMENT) != null) {
            Element pathInWar = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, TAG_PATH_IN_WAR);
            pathInWar.appendChild(doc.createTextNode(item.getAdditionalProperty(PATH_IN_DEPLOYMENT)));
            libraryElement.appendChild(pathInWar);
        }
        AntProjectUtil.updateDirsAttributeInCPSItem(item, libraryElement);
        return libraryElement;
    }

    
}

