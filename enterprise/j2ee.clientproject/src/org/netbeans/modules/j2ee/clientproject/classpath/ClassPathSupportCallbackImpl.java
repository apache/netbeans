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

package org.netbeans.modules.j2ee.clientproject.classpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.j2ee.clientproject.AppClientProjectType;
import org.netbeans.modules.javaee.project.api.ant.AntProjectConstants;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.javaee.project.api.ant.AntProjectUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Petr Hrebejk
 */
public class ClassPathSupportCallbackImpl implements ClassPathSupport.Callback {
     
    public static final String ELEMENT_INCLUDED_LIBRARIES = "included-library"; // NOI18N
    public static final String INCLUDE_IN_DEPLOYMENT = "includeInDeployment";
    
//    private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N
    
    private AntProjectHelper antProjectHelper;
        
    /** Creates a new instance of ClassPathSupport */
    public  ClassPathSupportCallbackImpl(AntProjectHelper antProjectHelper) {
        this.antProjectHelper = antProjectHelper;
    }
    
    
    /** 
     * Returns a list with the classpath items which are to be included 
     * in deployment.
     */
    private static List<String> getIncludedLibraries( AntProjectHelper antProjectHelper, String includedLibrariesElement, Map<String, String> destination) {
        assert antProjectHelper != null;
        assert includedLibrariesElement != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        NodeList libs = data.getElementsByTagNameNS( AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        List<String> libraries = new ArrayList<String>(libs.getLength());
        for ( int i = 0; i < libs.getLength(); i++ ) {
            Element item = (Element)libs.item( i );
            // appclient is different from other j2ee projects - it stores reference without ${ and }
            String ref = "${"+XMLUtil.findText( item )+"}";
            libraries.add(ref); // NOI18N
            String dirs = item.getAttribute(ATTR_DIRS);
            if (AntProjectConstants.DESTINATION_DIRECTORY_ROOT.equals(dirs) ||
                AntProjectConstants.DESTINATION_DIRECTORY_LIB.equals(dirs) ||
                AntProjectConstants.DESTINATION_DIRECTORY_DO_NOT_COPY.equals(dirs)) {
                destination.put(ref, dirs);
            }
        }
        return libraries;
    }
    
    /**
     * Updates the project helper with the list of classpath items which are to be
     * included in deployment.
     */
    private static void putIncludedLibraries(List<Item> classpath,
            AntProjectHelper antProjectHelper, String includedLibrariesElement) {
        assert antProjectHelper != null;
        assert includedLibrariesElement != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        NodeList libs = data.getElementsByTagNameNS( AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        while ( libs.getLength() > 0 ) {
            Node n = libs.item( 0 );
            n.getParentNode().removeChild( n );
        }

        Document doc = data.getOwnerDocument();
        //find a correcponding classpath item for the library
        for (ClassPathSupport.Item item : classpath) {
            if("true".equals(item.getAdditionalProperty(INCLUDE_IN_DEPLOYMENT))) { // NOI18N
                data.appendChild(createLibraryElement(antProjectHelper, doc, item, 
                        includedLibrariesElement));
            }
        }
        
        antProjectHelper.putPrimaryConfigurationData( data, true );
    }
    
    private static Element createLibraryElement(AntProjectHelper antProjectHelper, Document doc, Item item, 
            String includedLibrariesElement) {
        Element libraryElement = doc.createElementNS( AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        // appclient is different from other j2ee projects - it stores reference without ${ and }
        libraryElement.appendChild( doc.createTextNode( CommonProjectUtils.getAntPropertyName(item.getReference()) ) );
        AntProjectUtil.updateDirsAttributeInCPSItem(item, libraryElement);
        return libraryElement;
    }
       
    public void readAdditionalProperties(List<Item> items, String projectXMLElement) {
        Map<String, String> destination = new HashMap<String, String>();
        List<String> l = getIncludedLibraries(antProjectHelper, projectXMLElement, destination);
        for (Item item : items) {
            boolean b = l.contains(item.getReference());
            item.setAdditionalProperty(INCLUDE_IN_DEPLOYMENT, Boolean.toString(b));
            String dest = destination.get(item.getReference());
            if (b && dest != null) {
                item.setAdditionalProperty(AntProjectConstants.DESTINATION_DIRECTORY, dest);
            }
        }
    }

    public void storeAdditionalProperties(List<Item> items, String projectXMLElement) {
        putIncludedLibraries(items, antProjectHelper, projectXMLElement);
    }

}
