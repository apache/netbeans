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

package org.netbeans.modules.j2ee.ejbjarproject.classpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.javaee.project.api.ant.AntProjectConstants;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProjectType;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.javaee.project.api.ant.AntProjectUtil;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Petr Hrebejk
 * @author Andrei Badea
 */
public class ClassPathSupportCallbackImpl implements ClassPathSupport.Callback {
    
    public final static String ELEMENT_INCLUDED_LIBRARIES = "included-library"; // NOI18N
    
    private static String[] ejbjarElemOrder = new String[] { "name", "minimum-ant-version", "explicit-platform", "use-manifest", "included-library", "web-services", "source-roots", "test-roots" }; //NOI18N
    
//    private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N
    
    public static final String INCLUDE_IN_DEPLOYMENT = "includeInDeployment";
    
    private AntProjectHelper helper;

    public ClassPathSupportCallbackImpl(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    /** 
     * Returns a list with the classpath items which are to be included 
     * in deployment.
     */
    private static List<String> getIncludedLibraries( AntProjectHelper antProjectHelper, String includedLibrariesElement, Map<String, String> destination) {
        assert antProjectHelper != null;
        assert includedLibrariesElement != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        NodeList libs = data.getElementsByTagNameNS( EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        List<String> libraries = new ArrayList<String>(libs.getLength());
        for ( int i = 0; i < libs.getLength(); i++ ) {
            Element item = (Element)libs.item( i );
            // ejbjar is different from other j2ee projects - it stores reference without ${ and }
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
    private static void putIncludedLibraries(List<ClassPathSupport.Item> classpath, AntProjectHelper antProjectHelper, String includedLibrariesElement ) {
        assert antProjectHelper != null;
        assert includedLibrariesElement != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        NodeList libs = data.getElementsByTagNameNS( EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        while ( libs.getLength() > 0 ) {
            Node n = libs.item( 0 );
            n.getParentNode().removeChild( n );
        }

        Document doc = data.getOwnerDocument();
        for (ClassPathSupport.Item item : classpath) {
            if("true".equals(item.getAdditionalProperty(INCLUDE_IN_DEPLOYMENT))) { // NOI18N
                XMLUtil.appendChildElement(data,
                    createLibraryElement(antProjectHelper, doc, item, includedLibrariesElement), 
                    ejbjarElemOrder);
            }
        }
        
        antProjectHelper.putPrimaryConfigurationData( data, true );
    }
    
    private static Element createLibraryElement(AntProjectHelper antProjectHelper, Document doc, Item item, String includedLibrariesElement) {
        Element libraryElement = doc.createElementNS( EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        // ejbjar is different from other j2ee projects - it stores reference without ${ and }
        libraryElement.appendChild( doc.createTextNode( CommonProjectUtils.getAntPropertyName(item.getReference()) ) );
        AntProjectUtil.updateDirsAttributeInCPSItem(item, libraryElement);
        return libraryElement;
    }
       
    public void readAdditionalProperties(List<Item> items, String projectXMLElement) {
        Map<String, String> destination = new HashMap<String, String>();
        List<String> l = getIncludedLibraries(helper, projectXMLElement, destination);
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
        putIncludedLibraries(items, helper, projectXMLElement);
    }

}

