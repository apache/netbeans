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

package org.netbeans.modules.web.taglib;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Catalog for taglib DTDs and schemas that enables code completion and XML validation in editor.
 *
 * @author Milan Kuchtiak
 */
public class TaglibCatalog implements CatalogReader, CatalogDescriptor, org.xml.sax.EntityResolver  {
    private static final String TAGLIB_1_1="-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN"; // NOI18N
    private static final String TAGLIB_1_2="-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN"; // NOI18N
    
    public static final String J2EE_NS = "http://java.sun.com/xml/ns/j2ee"; // NOI18N
    public static final String JAVAEE_NS = "http://java.sun.com/xml/ns/javaee"; // NOI18N
    private static final String TAGLIB_2_0_XSD="web-jsptaglibrary_2_0.xsd"; // NOI18N
    private static final String TAGLIB_2_1_XSD="web-jsptaglibrary_2_1.xsd"; // NOI18N
    private static final String TAGLIB_2_0=J2EE_NS+"/"+TAGLIB_2_0_XSD; // NOI18N
    private static final String TAGLIB_2_1=JAVAEE_NS+"/"+TAGLIB_2_1_XSD; // NOI18N
    public static final String TAGLIB_2_0_ID="SCHEMA:"+TAGLIB_2_0; // NOI18N
    public static final String TAGLIB_2_1_ID="SCHEMA:"+TAGLIB_2_1; // NOI18N
    private static final String WEB_SERVICES_CLIENT_XSD = "http://www.ibm.com/webservices/xsd/j2ee_web_services_client_1_1.xsd"; // NOI18N
    
    private static final String URL_TAGLIB_1_1="nbres:/org/netbeans/modules/web/taglib/resources/web-jsptaglibrary_1_1.dtd"; // NOI18N
    private static final String URL_TAGLIB_1_2="nbres:/org/netbeans/modules/web/taglib/resources/web-jsptaglibrary_1_2.dtd"; // NOI18N
    private static final String URL_TAGLIB_2_0="nbres:/org/netbeans/modules/web/taglib/resources/web-jsptaglibrary_2_0.xsd"; // NOI18N
    private static final String URL_TAGLIB_2_1="nbres:/org/netbeans/modules/web/taglib/resources/web-jsptaglibrary_2_1.xsd"; // NOI18N
    private static final String URL_WEB_SERVICES_CLIENT = "nbres:/org/netbeans/modules/web/taglib/resources/j2ee_web_services_client_1_1.xsd"; // NOI18N
    
    private static final String XML_XSD="http://www.w3.org/2001/xml.xsd"; // NOI18N
    private static final String XML_XSD_DEF="<?xml version='1.0'?><xs:schema targetNamespace=\"http://www.w3.org/XML/1998/namespace\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xml:lang=\"en\"><xs:attribute name=\"lang\" type=\"xs:language\"><xs:annotation><xs:documentation>In due course, we should install the relevant ISO 2- and 3-letter codes as the enumerated possible values . . .</xs:documentation></xs:annotation></xs:attribute></xs:schema>"; // NOI18N
    
    /** Creates a new instance of TaglibCatalog */
    public TaglibCatalog() {
    }
    
    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public java.util.Iterator getPublicIDs() {
        List<String> list = new ArrayList<>();
        list.add(TAGLIB_1_1);
        list.add(TAGLIB_1_2);
        list.add(TAGLIB_2_0_ID);
        list.add(TAGLIB_2_1_ID);
        return list.listIterator();
    }
    
    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {
        if (TAGLIB_1_2.equals(publicId))
            return URL_TAGLIB_1_2;
        else if (TAGLIB_1_1.equals(publicId))
            return URL_TAGLIB_1_1;
        else if (TAGLIB_2_0_ID.equals(publicId))
            return URL_TAGLIB_2_0;
        else if (TAGLIB_2_1_ID.equals(publicId))
            return URL_TAGLIB_2_1;
        else return null;
    }
    
    /**
     * Refresh content according to content of mounted catalog.
     */
    public void refresh() {
    }
    
    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void addCatalogListener(CatalogListener l) {
    }
    
    /**
     * Optional operation couled with addCatalogListener.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void removeCatalogListener(CatalogListener l) {
    }
    
    /** Registers new listener.  */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
    /**
     * @return I18N display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage (TaglibCatalog.class, "LBL_TaglibCatalog");
    }
    
    /**
     * Return visuaized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public java.awt.Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/web/taglib/resources/TaglibCatalog.gif"); // NOI18N
    }
    
    /**
     * @return I18N short description
     */
    public String getShortDescription() {
        return NbBundle.getMessage (TaglibCatalog.class, "DESC_TaglibCatalog");
    }
    
    /** Unregister the listener.  */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
    /**
     * Resolves schema definition file for taglib descriptor (spec.1_1, 1_2, 2_0)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for publisId, 
     */    
    public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) throws org.xml.sax.SAXException, java.io.IOException {
        if (TAGLIB_2_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_TAGLIB_2_0);
        } else if (TAGLIB_2_1.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_TAGLIB_2_1);
        } else if (systemId!=null && systemId.endsWith(TAGLIB_2_0_XSD)) {
            return new org.xml.sax.InputSource(URL_TAGLIB_2_0);
        } else if (WEB_SERVICES_CLIENT_XSD.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_WEB_SERVICES_CLIENT);
        } else if (XML_XSD.equals(systemId)) {
            return new org.xml.sax.InputSource(new java.io.StringReader(XML_XSD_DEF));
        } else {
            return null;
        }
    }
    
    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    public String resolveURI(String name) {
        return null;
    }
    /**
     * Get registered URI for the given publicId or null if not registered.
     * @return null if not registered
     */ 
    public String resolvePublic(String publicId) {
        return null;
    }
    
}
