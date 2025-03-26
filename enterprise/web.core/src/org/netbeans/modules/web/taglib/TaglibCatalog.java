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

package org.netbeans.modules.web.taglib;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.NbBundle;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Catalog for taglib DTDs and schemas that enables code completion and XML validation in editor.
 *
 * @author Milan Kuchtiak
 */
public class TaglibCatalog implements CatalogReader, CatalogDescriptor2, EntityResolver  {
    private static final String TAGLIB_1_1="-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN"; // NOI18N
    private static final String TAGLIB_1_2="-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN"; // NOI18N
    
    public static final String J2EE_NS = "http://java.sun.com/xml/ns/j2ee"; // NOI18N
    public static final String JAVAEE_NS = "http://java.sun.com/xml/ns/javaee"; // NOI18N
    public static final String JAKARTAEE_NS = "http://jakarta.ee/xml/ns/jakartaee"; // NOI18N
    private static final String TAGLIB_2_0_XSD="web-jsptaglibrary_2_0.xsd"; // NOI18N
    private static final String TAGLIB_2_1_XSD="web-jsptaglibrary_2_1.xsd"; // NOI18N
    private static final String TAGLIB_3_0_XSD="web-jsptaglibrary_3_0.xsd"; // NOI18N
    private static final String TAGLIB_3_1_XSD="web-jsptaglibrary_3_1.xsd"; // NOI18N
    private static final String TAGLIB_4_0_XSD="web-jsptaglibrary_4_0.xsd"; // NOI18N
    private static final String TAGLIB_2_0=J2EE_NS+"/"+TAGLIB_2_0_XSD; // NOI18N
    private static final String TAGLIB_2_1=JAVAEE_NS+"/"+TAGLIB_2_1_XSD; // NOI18N
    private static final String TAGLIB_3_0=JAKARTAEE_NS+"/"+TAGLIB_3_0_XSD; // NOI18N
    private static final String TAGLIB_3_1=JAKARTAEE_NS+"/"+TAGLIB_3_1_XSD; // NOI18N
    private static final String TAGLIB_4_0=JAKARTAEE_NS+"/"+TAGLIB_4_0_XSD; // NOI18N
    public static final String TAGLIB_2_0_ID="SCHEMA:"+TAGLIB_2_0; // NOI18N
    public static final String TAGLIB_2_1_ID="SCHEMA:"+TAGLIB_2_1; // NOI18N
    public static final String TAGLIB_3_0_ID="SCHEMA:"+TAGLIB_3_0; // NOI18N
    public static final String TAGLIB_3_1_ID="SCHEMA:"+TAGLIB_3_1; // NOI18N
    public static final String TAGLIB_4_0_ID="SCHEMA:"+TAGLIB_4_0; // NOI18N
    
    private static final String URL_WEB_SERVICES_CLIENT_IBM = "http://www.ibm.com/webservices/xsd/j2ee_web_services_client_1_1.xsd"; // NOI18N
    private static final String URL_WEB_SERVICES_CLIENT_1_1 = "nbres:/org/netbeans/modules/web/taglib/resources/j2ee_web_services_client_1_1.xsd"; // NOI18N
    private static final String URL_WEB_SERVICES_CLIENT_1_2 = "nbres:/org/netbeans/modules/web/taglib/resources/javaee_web_services_client_1_2.xsd"; // NOI18N
    
    private static final String URL_TAGLIB_1_1="nbres:/org/netbeans/modules/web/taglib/resources/web-jsptaglibrary_1_1.dtd"; // NOI18N
    private static final String URL_TAGLIB_1_2="nbres:/org/netbeans/modules/web/taglib/resources/web-jsptaglibrary_1_2.dtd"; // NOI18N
    private static final String URL_TAGLIB_2_0="nbres:/org/netbeans/modules/web/taglib/resources/web-jsptaglibrary_2_0.xsd"; // NOI18N
    private static final String URL_TAGLIB_2_1="nbres:/org/netbeans/modules/web/taglib/resources/web-jsptaglibrary_2_1.xsd"; // NOI18N
    private static final String URL_TAGLIB_3_0="nbres:/org/netbeans/modules/web/taglib/resources/web-jsptaglibrary_3_0.xsd"; // NOI18N
    private static final String URL_TAGLIB_3_1="nbres:/org/netbeans/modules/web/taglib/resources/web-jsptaglibrary_3_1.xsd"; // NOI18N
    private static final String URL_TAGLIB_4_0="nbres:/org/netbeans/modules/web/taglib/resources/web-jsptaglibrary_4_0.xsd"; // NOI18N
    
    private static final String XML_XSD="http://www.w3.org/2001/xml.xsd"; // NOI18N
    private static final String XML_XSD_DEF="<?xml version='1.0'?>"
            + "<xs:schema targetNamespace=\"http://www.w3.org/XML/1998/namespace\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xml:lang=\"en\">"
            + "<xs:attribute name=\"lang\" type=\"xs:language\">"
            + "<xs:annotation>"
            + "<xs:documentation>In due course, we should install the relevant ISO 2- and 3-letter codes as the enumerated possible values . . .</xs:documentation>"
            + "</xs:annotation></xs:attribute></xs:schema>"; // NOI18N
    
    /** Creates a new instance of TaglibCatalog */
    public TaglibCatalog() {
    }
    
    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    @Override
    public Iterator getPublicIDs() {
        List<String> list = new ArrayList<>(16);
        list.add(TAGLIB_1_1);
        list.add(TAGLIB_1_2);
        list.add(TAGLIB_2_0_ID);
        list.add(TAGLIB_2_1_ID);
        list.add(TAGLIB_3_0_ID);
        list.add(TAGLIB_3_1_ID);
        list.add(TAGLIB_4_0_ID);
        return list.listIterator();
    }
    
    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    @Override
    public String getSystemID(String publicId) {
        if (null == publicId) {
            return null;
        } else {
            switch (publicId) {
                case TAGLIB_1_1:
                    return URL_TAGLIB_1_1;
                case TAGLIB_1_2:
                    return URL_TAGLIB_1_2;
                case TAGLIB_2_0_ID:
                    return URL_TAGLIB_2_0;
                case TAGLIB_2_1_ID:
                    return URL_TAGLIB_2_1;
                case TAGLIB_3_0_ID:
                    return URL_TAGLIB_3_0;
                case TAGLIB_3_1_ID:
                    return URL_TAGLIB_3_1;
                case TAGLIB_4_0_ID:
                    return URL_TAGLIB_4_0;
                default:
                    return null;
            }
        }
    }
    
    /**
     * Refresh content according to content of mounted catalog.
     */
    @Override
    public void refresh() {
    }
    
    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    @Override
    public void addCatalogListener(CatalogListener l) {
    }
    
    /**
     * Optional operation could with addCatalogListener.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    @Override
    public void removeCatalogListener(CatalogListener l) {
    }
    
    /** Registers new listener.  */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }
    
    /**
     * @return I18N display name
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage (TaglibCatalog.class, "LBL_TaglibCatalog");
    }
    
    /**
     * Return visualized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    @Override
    public String getIconResource(int type) {
        return "org/netbeans/modules/web/taglib/resources/TaglibCatalog.gif"; // NOI18N
    }
    
    /**
     * @return I18N short description
     */
    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (TaglibCatalog.class, "DESC_TaglibCatalog");
    }
    
    /** Unregister the listener.  */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
    
    /**
     * Resolves schema definition file for taglib descriptor 
     * (spec. 1_1, 1_2, 2_0, 2_1, 3_0, 3_1, 4_0)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for publisId, 
     */    
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (TAGLIB_2_0.equals(systemId)) {
            return new InputSource(URL_TAGLIB_2_0);
        } else if (TAGLIB_2_1.equals(systemId)) {
            return new InputSource(URL_TAGLIB_2_1);
        } else if (TAGLIB_3_0.equals(systemId)) {
            return new InputSource(URL_TAGLIB_3_0);
        } else if (TAGLIB_3_1.equals(systemId)) {
            return new InputSource(URL_TAGLIB_3_1);
        } else if (TAGLIB_4_0.equals(systemId)) {
            return new InputSource(URL_TAGLIB_4_0);
        } else if (systemId!=null && systemId.endsWith(TAGLIB_2_0_XSD)) {
            return new InputSource(URL_TAGLIB_2_0);
        } else if (systemId!=null && systemId.endsWith(TAGLIB_2_1_XSD)) {
            return new InputSource(URL_TAGLIB_2_1);
        } else if (systemId!=null && systemId.endsWith(TAGLIB_3_0_XSD)) {
            return new InputSource(URL_TAGLIB_3_0);
        } else if (systemId!=null && systemId.endsWith(TAGLIB_3_1_XSD)) {
            return new InputSource(URL_TAGLIB_3_1);
        } else if (systemId!=null && systemId.endsWith(TAGLIB_4_0_XSD)) {
            return new InputSource(URL_TAGLIB_4_0);
        } else if (URL_WEB_SERVICES_CLIENT_IBM.equals(systemId)) {
            return new InputSource(URL_WEB_SERVICES_CLIENT_1_1);
        } else if (URL_WEB_SERVICES_CLIENT_1_2.equals(systemId)) {
            return new InputSource(URL_WEB_SERVICES_CLIENT_1_2);
        } else if (XML_XSD.equals(systemId)) {
            return new InputSource(new StringReader(XML_XSD_DEF));
        } else {
            return null;
        }
    }
    
    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    @Override
    public String resolveURI(String name) {
        return null;
    }
    /**
     * Get registered URI for the given publicId or null if not registered.
     * @return null if not registered
     */ 
    @Override
    public String resolvePublic(String publicId) {
        return null;
    }
    
}
