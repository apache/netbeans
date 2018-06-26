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

package org.netbeans.modules.websvc.rest.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author PeterLiu
 */
public class PersistenceHelper {
    private static final String PERSISTENCE_UNIT_TAG = "persistence-unit";      //NOI18N
    
    private static final String PROPERTIES_TAG = "properties";      //NOI18N
    
    private static final String NAME_ATTR = "name";                 //NOI18N
    
    private static final String EXCLUDE_UNLISTED_CLASSES_TAG = "exclude-unlisted-classes";      //NOI18N
    
    private static final String TRANSACTION_TYPE_ATTR = "transaction-type";         //NOI18N
    
    private static final String RESOURCE_LOCAL_VALUE = "RESOURCE_LOCAL";        //NOI18N
    
    private static final String JTA_DATA_SOURCE_TAG = "jta-data-source";        //NOI18N
    
    private static final String NON_JTA_DATA_SOURCE_TAG = "non-jta-data-source";        //NOI18N
    
    private static final String CLASS_TAG = "class";    //NOI18N
    
    private static final String PROVIDER_TAG = "provider";  //NOI18N
    
    private static final String DEFAULT_GFV2_PROVIDER = "oracle.toplink.essentials.PersistenceProvider"; //NOI18N

    private static final String DEFAULT_GFV3_PROVIDER = "org.eclipse.persistence.jpa.PersistenceProvider";  //NOI18N
   
    private Project project;
    private DOMHelper helper;
    
    public PersistenceHelper(Project project) {
        this.project = project;
        
        FileObject fobj = getPersistenceXML();
        
        if (fobj != null) {
            helper = new DOMHelper(fobj);
        }
    }
    
    public PersistenceUnit getPersistenceUnit() {
        if (helper != null) {
            Element puElement = helper.findElement(PERSISTENCE_UNIT_TAG);  
            
            if (puElement != null) {
                Attr puNameNode = puElement.getAttributeNode(NAME_ATTR);
                String puName = puNameNode == null ? null : puNameNode.getValue();
                
                String provider;
                NodeList nodes = puElement.getElementsByTagName(PROVIDER_TAG);
                if (nodes.getLength() > 0) {
                    provider = helper.getValue((Element) nodes.item(0));
                } else {
                    if (RestUtils.isServerGFV3(project)) {
                        provider = DEFAULT_GFV3_PROVIDER;
                    } else {
                        provider = DEFAULT_GFV3_PROVIDER;
                    }
                }
                
                Datasource datasource = null;
                
                NodeList nodeList = puElement.getElementsByTagName(JTA_DATA_SOURCE_TAG);
                if (nodeList.getLength() > 0) {
                    Element dsElement = (Element) nodeList.item(0);
                    String jndiName = helper.getValue(dsElement);      
                    datasource = RestUtils.getDatasource(project, jndiName);
                }
                
                return new PersistenceUnit(puName, provider, datasource);
            }
        }
        
        return null;
    }
    
    
    public void configure(Collection<String> classNames, boolean useResourceLocalTx) throws IOException {
        if (helper == null) return;

        /* Required by Spring
         * Fix for BZ#195973 -  EE6 RESTful WS in Spring app fails to deploy to GF 3.1
        if (RestUtils.hasSpringSupport(project)) {
            setDefaultProvider();
        }*/

        // Need to do this for Tomcat
        if (RestUtils.isServerTomcat(project)) {
            unsetExcludeEnlistedClasses();
            addEntityClasses(classNames);
        }
        
        if (useResourceLocalTx)
            switchToResourceLocalTransaction();
       
        helper.save();
    }
    
    private void unsetExcludeEnlistedClasses() throws IOException {
        Element puElement = helper.findElement(PERSISTENCE_UNIT_TAG);
        NodeList nodes = puElement.getElementsByTagName(EXCLUDE_UNLISTED_CLASSES_TAG);
    
        if (nodes.getLength() > 0) {
            helper.setValue((Element) nodes.item(0), "false");  //NOI18N
        } else {
            puElement.insertBefore(helper.createElement(EXCLUDE_UNLISTED_CLASSES_TAG, "false"),  //NOI18N
                    helper.findElement(PROPERTIES_TAG));
        }
    }
     
    private void switchToResourceLocalTransaction()  throws IOException {
        Element puElement = helper.findElement(PERSISTENCE_UNIT_TAG);
        puElement.setAttribute(TRANSACTION_TYPE_ATTR, RESOURCE_LOCAL_VALUE);
        
        NodeList nodes = puElement.getElementsByTagName(JTA_DATA_SOURCE_TAG);
        String dataSource = null;
        
        if (nodes.getLength() > 0) {
            Element oldElement = (Element) nodes.item(0);
            dataSource = helper.getValue(oldElement);
            Element newElement = helper.createElement(NON_JTA_DATA_SOURCE_TAG, dataSource);
            puElement.replaceChild(newElement, oldElement);
        }
    }
    
    private void addEntityClasses(Collection<String> classNames) throws IOException {
        List<String> toAdd = new ArrayList<String>(classNames);   
        Element puElement = helper.findElement(PERSISTENCE_UNIT_TAG);
        NodeList nodes = puElement.getElementsByTagName(CLASS_TAG);
        int length = nodes.getLength();
        
        for (int i = 0; i < length; i++) {
            toAdd.remove(helper.getValue((Element) nodes.item(i)));
        }
        
        for (String className : toAdd) {   
            puElement.insertBefore(helper.createElement(CLASS_TAG, className),
                    helper.findElement(EXCLUDE_UNLISTED_CLASSES_TAG));
        }
    }
    
    /*
     * Commented out as fix for BZ#195973 -  EE6 RESTful WS in Spring app fails to deploy to GF 3.1
     * In case of usage this method hardcoded strings DEFAULT_GFV3_PROVIDER and 
     * DEFAULT_GFV2_PROVIDER should be chanhged to 
     * org.netbeans.modules.j2ee.persistence.wizard.Util.getPreferredProvider() 
    private void setDefaultProvider() throws IOException {
        Element puElement = helper.findElement(PERSISTENCE_UNIT_TAG);
        NodeList nodes = puElement.getElementsByTagName(PROVIDER_TAG);
        
        if (nodes.getLength() == 0) {

            puElement.insertBefore(helper.createElement(PROVIDER_TAG, 
                    (RestUtils.isServerGFV3(project) ? DEFAULT_GFV3_PROVIDER : DEFAULT_GFV2_PROVIDER)),
                    puElement.getFirstChild());
        }
    }*/
    
    private FileObject getPersistenceXML() {
        RestSupport rs = RestUtils.getRestSupport(project);
        if (rs != null) {
            return rs.getPersistenceXml();
        }
        return null;
    }
    
    public static class PersistenceUnit {
        private String name;
        private String provider;
        private Datasource datasource;
        
        public PersistenceUnit(String name, String provider, Datasource datasource) {
            this.name = name;
            this.provider = provider;
            this.datasource = datasource;
        }
        
        public String getName() {
            return name;
        }
        
        public String getProvider() {
            return provider;
        }
        
        public Datasource getDatasource() {
            return datasource;
        }
    }
}
