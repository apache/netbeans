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


package org.netbeans.modules.web.jsf;

import java.util.List;
import java.util.ArrayList;

import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.xml.catalog.spi.*;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 *
 * @author  Petr Pisl
 */
public class JSFCatalog implements CatalogReader, CatalogDescriptor2, org.xml.sax.EntityResolver {

    private static final String JSF_ID_1_0 = "-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.0//EN"; // NOI18N
    private static final String JSF_ID_1_1 = "-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.1//EN"; // NOI18N

    private static final String URL_JSF_1_0 ="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_1_0.dtd"; // NOI18N
    private static final String URL_JSF_1_1 ="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_1_1.dtd"; // NOI18N
    private static final String URL_JSF_1_2="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_1_2.xsd"; // NOI18N
    private static final String URL_JSF_2_0="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_2_0.xsd"; // NOI18N
    private static final String URL_JSF_2_1="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_2_1.xsd"; // NOI18N
    private static final String URL_JSF_2_2="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_2_2.xsd"; // NOI18N
    private static final String URL_JSF_2_3="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_2_3.xsd"; // NOI18N
    private static final String URL_JSF_3_0="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_3_0.xsd"; // NOI18N
    private static final String URL_JSF_4_0="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_4_0.xsd"; // NOI18N
    private static final String URL_JSF_4_1="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_4_1.xsd"; // NOI18N

    public static final String JAVAEE_NS = "http://java.sun.com/xml/ns/javaee";  // NOI18N
    public static final String NEW_JAVAEE_NS = "http://xmlns.jcp.org/xml/ns/javaee"; //NOI18N
    public static final String JAKARTAEE_NS = "https://jakarta.ee/xml/ns/jakartaee"; //NOI18N
    private static final String JSF_1_2_XSD="web-facesconfig_1_2.xsd"; // NOI18N
    private static final String JSF_2_0_XSD="web-facesconfig_2_0.xsd"; // NOI18N
    private static final String JSF_2_1_XSD="web-facesconfig_2_1.xsd"; // NOI18N
    private static final String JSF_2_2_XSD="web-facesconfig_2_2.xsd"; // NOI18N
    private static final String JSF_2_3_XSD="web-facesconfig_2_3.xsd"; // NOI18N
    private static final String JSF_3_0_XSD="web-facesconfig_3_0.xsd"; // NOI18N
    private static final String JSF_4_0_XSD="web-facesconfig_4_0.xsd"; // NOI18N
    private static final String JSF_4_1_XSD="web-facesconfig_4_1.xsd"; // NOI18N
    private static final String JSF_1_2=JAVAEE_NS+"/"+JSF_1_2_XSD; // NOI18N
    private static final String JSF_2_0=JAVAEE_NS+"/"+JSF_2_0_XSD; // NOI18N
    private static final String JSF_2_1=JAVAEE_NS+"/"+JSF_2_1_XSD; // NOI18N
    private static final String JSF_2_2=NEW_JAVAEE_NS+"/"+JSF_2_2_XSD; // NOI18N
    private static final String JSF_2_3=NEW_JAVAEE_NS+"/"+JSF_2_3_XSD; // NOI18N
    private static final String JSF_3_0=JAKARTAEE_NS+"/"+JSF_3_0_XSD; // NOI18N
    private static final String JSF_4_0=JAKARTAEE_NS+"/"+JSF_4_0_XSD; // NOI18N
    private static final String JSF_4_1=JAKARTAEE_NS+"/"+JSF_4_1_XSD; // NOI18N
    public static final String JSF_ID_1_2="SCHEMA:"+JSF_1_2; // NOI18N
    public static final String JSF_ID_2_0="SCHEMA:"+JSF_2_0; // NOI18N
    public static final String JSF_ID_2_1="SCHEMA:"+JSF_2_1; // NOI18N
    public static final String JSF_ID_2_2="SCHEMA:"+JSF_2_2; // NOI18N
    public static final String JSF_ID_2_3="SCHEMA:"+JSF_2_3; // NOI18N
    public static final String JSF_ID_3_0="SCHEMA:"+JSF_3_0; // NOI18N
    public static final String JSF_ID_4_0="SCHEMA:"+JSF_4_0; // NOI18N
    public static final String JSF_ID_4_1="SCHEMA:"+JSF_4_1; // NOI18N


    // faces-config resources
    public static final String RES_FACES_CONFIG_DEFAULT = "faces-config.xml";
    public static final String RES_FACES_CONFIG_1_2 = "faces-config_1_2.xml";
    public static final String RES_FACES_CONFIG_2_0 = "faces-config_2_0.xml";
    public static final String RES_FACES_CONFIG_2_1 = "faces-config_2_1.xml";
    public static final String RES_FACES_CONFIG_2_2 = "faces-config_2_2.xml";
    public static final String RES_FACES_CONFIG_2_3 = "faces-config_2_3.xml";
    public static final String RES_FACES_CONFIG_3_0 = "faces-config_3_0.xml";
    public static final String RES_FACES_CONFIG_4_0 = "faces-config_4_0.xml";
    public static final String RES_FACES_CONFIG_4_1 = "faces-config_4_1.xml";

    //facelets
    private static final String FILE_FACELETS_TAGLIB_SCHEMA_41="web-facelettaglibrary_4_1.xsd"; //NOI18N
    private static final String FILE_FACELETS_TAGLIB_SCHEMA_40="web-facelettaglibrary_4_0.xsd"; //NOI18N
    private static final String FILE_FACELETS_TAGLIB_SCHEMA_30="web-facelettaglibrary_3_0.xsd"; //NOI18N
    private static final String FILE_FACELETS_TAGLIB_SCHEMA_23="web-facelettaglibrary_2_3.xsd"; //NOI18N
    private static final String FILE_FACELETS_TAGLIB_SCHEMA_22="web-facelettaglibrary_2_2.xsd"; //NOI18N
    private static final String FILE_FACELETS_TAGLIB_SCHEMA_20="web-facelettaglibrary_2_0.xsd"; //NOI18N
    private static final String FILE_FACELETS_TAGLIB_DTD_10="facelet-taglib_1_0.dtd"; //NOI18N

    private static final String URL_FACELETS_TAGLIB_SCHEMA_41 = JAKARTAEE_NS + "/" + FILE_FACELETS_TAGLIB_SCHEMA_41; // NOI18N
    private static final String ID_FACELETS_TAGLIB_SCHEMA_41 ="SCHEMA:" + URL_FACELETS_TAGLIB_SCHEMA_41;
    private static final String URL_FACELETS_TAGLIB_SCHEMA_40 = JAKARTAEE_NS + "/" + FILE_FACELETS_TAGLIB_SCHEMA_40; // NOI18N
    private static final String ID_FACELETS_TAGLIB_SCHEMA_40 ="SCHEMA:" + URL_FACELETS_TAGLIB_SCHEMA_40;
    private static final String URL_FACELETS_TAGLIB_SCHEMA_30 = JAKARTAEE_NS + "/" + FILE_FACELETS_TAGLIB_SCHEMA_30; // NOI18N
    private static final String ID_FACELETS_TAGLIB_SCHEMA_30 ="SCHEMA:" + URL_FACELETS_TAGLIB_SCHEMA_30;
    private static final String URL_FACELETS_TAGLIB_SCHEMA_23 = NEW_JAVAEE_NS + "/" + FILE_FACELETS_TAGLIB_SCHEMA_23; // NOI18N
    private static final String ID_FACELETS_TAGLIB_SCHEMA_23 ="SCHEMA:" + URL_FACELETS_TAGLIB_SCHEMA_23;
    private static final String URL_FACELETS_TAGLIB_SCHEMA_22 = NEW_JAVAEE_NS + "/" + FILE_FACELETS_TAGLIB_SCHEMA_22; // NOI18N
    private static final String ID_FACELETS_TAGLIB_SCHEMA_22 ="SCHEMA:" + URL_FACELETS_TAGLIB_SCHEMA_22;
    private static final String URL_FACELETS_TAGLIB_SCHEMA_20 = JAVAEE_NS + "/" + FILE_FACELETS_TAGLIB_SCHEMA_20; // NOI18N
    private static final String ID_FACELETS_TAGLIB_SCHEMA_20 ="SCHEMA:" + URL_FACELETS_TAGLIB_SCHEMA_20;
    private static final String ID_FACELETS_TAGLIB_DTD_10 = "-//Sun Microsystems, Inc.//DTD Facelet Taglib 1.0//EN"; //NOI18N

    private static final String RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_41 ="nbres:/org/netbeans/modules/web/jsf/resources/" + FILE_FACELETS_TAGLIB_SCHEMA_41; // NOI18N
    private static final String RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_40 ="nbres:/org/netbeans/modules/web/jsf/resources/" + FILE_FACELETS_TAGLIB_SCHEMA_40; // NOI18N
    private static final String RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_30 ="nbres:/org/netbeans/modules/web/jsf/resources/" + FILE_FACELETS_TAGLIB_SCHEMA_30; // NOI18N
    private static final String RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_23 ="nbres:/org/netbeans/modules/web/jsf/resources/" + FILE_FACELETS_TAGLIB_SCHEMA_23; // NOI18N
    private static final String RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_22 ="nbres:/org/netbeans/modules/web/jsf/resources/" + FILE_FACELETS_TAGLIB_SCHEMA_22; // NOI18N
    private static final String RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_20 ="nbres:/org/netbeans/modules/web/jsf/resources/" + FILE_FACELETS_TAGLIB_SCHEMA_20; // NOI18N
    private static final String RESOURCE_URL_FACELETS_TAGLIB_DTD_10 ="nbres:/org/netbeans/modules/web/jsf/resources/" + FILE_FACELETS_TAGLIB_DTD_10; // NOI18N


    public JSFCatalog() {
    }

    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public java.util.Iterator getPublicIDs() {
        List<String> list = new ArrayList<>();
        list.add(JSF_ID_1_0);
        list.add(JSF_ID_1_1);
        list.add(JSF_ID_1_2);
        list.add(JSF_ID_2_0);
        list.add(JSF_ID_2_1);
        list.add(JSF_ID_2_2);
        list.add(JSF_ID_2_3);
        list.add(JSF_ID_3_0);
        list.add(JSF_ID_4_0);
        list.add(JSF_ID_4_1);
        list.add(ID_FACELETS_TAGLIB_DTD_10);
        list.add(ID_FACELETS_TAGLIB_SCHEMA_20);
        list.add(ID_FACELETS_TAGLIB_SCHEMA_22);
        list.add(ID_FACELETS_TAGLIB_SCHEMA_23);
        list.add(ID_FACELETS_TAGLIB_SCHEMA_30);
        list.add(ID_FACELETS_TAGLIB_SCHEMA_40);
        list.add(ID_FACELETS_TAGLIB_SCHEMA_41);
        return list.listIterator();
    }

    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {
        if (null == publicId) {
            return null;
        }
        switch (publicId) {
            case JSF_ID_1_0:
                return URL_JSF_1_0;
            case JSF_ID_1_1:
                return URL_JSF_1_1;
            case JSF_ID_1_2:
                return URL_JSF_1_2;
            case JSF_ID_2_0:
                return URL_JSF_2_0;
            case JSF_ID_2_1:
                return URL_JSF_2_1;
            case JSF_ID_2_2:
                return URL_JSF_2_2;
            case JSF_ID_2_3:
                return URL_JSF_2_3;
            case JSF_ID_3_0:
                return URL_JSF_3_0;
            case JSF_ID_4_0:
                return URL_JSF_4_0;
            case JSF_ID_4_1:
                return URL_JSF_4_1;
            case ID_FACELETS_TAGLIB_DTD_10:
                return RESOURCE_URL_FACELETS_TAGLIB_DTD_10;
            case ID_FACELETS_TAGLIB_SCHEMA_20:
                return RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_20;
            case ID_FACELETS_TAGLIB_SCHEMA_22:
                return RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_22;
            case ID_FACELETS_TAGLIB_SCHEMA_23:
                return RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_23;
            case ID_FACELETS_TAGLIB_SCHEMA_30:
                return RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_30;
            case ID_FACELETS_TAGLIB_SCHEMA_40:
                return RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_40;
            case ID_FACELETS_TAGLIB_SCHEMA_41:
                return RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_41;
            default:
                return null;
        }
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

     /** Unregister the listener.  */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }

    /**
     * @return I18N display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage (JSFCatalog.class, "LBL_JSFCatalog");
    }

    /**
     * Return visuaized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public String getIconResource(int type) {
        return "org/netbeans/modules/web/jsf/resources/JSFCatalog.png"; // NOI18N
    }

    /**
     * @return I18N short description
     */
    public String getShortDescription() {
        return NbBundle.getMessage (JSFCatalog.class, "DESC_JSFCatalog");
    }

   /**
     * Resolves schema definition file for taglib descriptor (spec.1_1, 1_2, 2_0, 2_1, 2_2, 2_3, 3_0, 4_0, 4_1)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for publisId,
     */
    public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) throws org.xml.sax.SAXException, java.io.IOException {
       if (JSF_ID_1_0.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_JSF_1_0);
        } else if (JSF_ID_1_1.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_JSF_1_1);
        } else if(ID_FACELETS_TAGLIB_DTD_10.equals(publicId)) {
            return new org.xml.sax.InputSource(RESOURCE_URL_FACELETS_TAGLIB_DTD_10);
        } else if (JSF_1_2.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_JSF_1_2);
        } else if (JSF_2_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_JSF_2_0);
        } else if (JSF_2_1.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_JSF_2_1);
        } else if (JSF_2_2.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_JSF_2_2);
        } else if (JSF_2_3.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_JSF_2_3);
        } else if (JSF_3_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_JSF_3_0);
        } else if (JSF_4_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_JSF_4_0);
        } else if (JSF_4_1.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_JSF_4_1);
        } else if (URL_FACELETS_TAGLIB_SCHEMA_20.equals(systemId)) {
            return new org.xml.sax.InputSource(RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_20);
        } else if (URL_FACELETS_TAGLIB_SCHEMA_22.equals(systemId)) {
            return new org.xml.sax.InputSource(RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_22);
        } else if (URL_FACELETS_TAGLIB_SCHEMA_23.equals(systemId)) {
            return new org.xml.sax.InputSource(RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_23);
        } else if (URL_FACELETS_TAGLIB_SCHEMA_30.equals(systemId)) {
            return new org.xml.sax.InputSource(RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_30);
        } else if (URL_FACELETS_TAGLIB_SCHEMA_40.equals(systemId)) {
            return new org.xml.sax.InputSource(RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_40);
        } else if (URL_FACELETS_TAGLIB_SCHEMA_41.equals(systemId)) {
            return new org.xml.sax.InputSource(RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_41);
        } else if (systemId!=null && systemId.endsWith(JSF_1_2_XSD)) {
            return new org.xml.sax.InputSource(URL_JSF_1_2);
        } else if (systemId!=null && systemId.endsWith(JSF_2_0_XSD)) {
            return new org.xml.sax.InputSource(URL_JSF_2_0);
        } else if (systemId!=null && systemId.endsWith(JSF_2_1_XSD)) {
            return new org.xml.sax.InputSource(URL_JSF_2_1);
        } else if (systemId!=null && systemId.endsWith(JSF_2_2_XSD)) {
            return new org.xml.sax.InputSource(URL_JSF_2_2);
        } else if (systemId!=null && systemId.endsWith(JSF_2_3_XSD)) {
            return new org.xml.sax.InputSource(URL_JSF_2_3);
        } else if (systemId!=null && systemId.endsWith(JSF_3_0_XSD)) {
            return new org.xml.sax.InputSource(URL_JSF_3_0);
        } else if (systemId!=null && systemId.endsWith(JSF_4_0_XSD)) {
            return new org.xml.sax.InputSource(URL_JSF_4_0);
        } else if (systemId!=null && systemId.endsWith(JSF_4_1_XSD)) {
            return new org.xml.sax.InputSource(URL_JSF_4_1);
        } else if (systemId!=null && systemId.endsWith(FILE_FACELETS_TAGLIB_SCHEMA_20)) {
            return new org.xml.sax.InputSource(RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_20);
        } else if (systemId!=null && systemId.endsWith(FILE_FACELETS_TAGLIB_SCHEMA_22)) {
            return new org.xml.sax.InputSource(RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_22);
        } else if (systemId!=null && systemId.endsWith(FILE_FACELETS_TAGLIB_SCHEMA_23)) {
            return new org.xml.sax.InputSource(RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_23);
        } else if (systemId!=null && systemId.endsWith(FILE_FACELETS_TAGLIB_SCHEMA_30)) {
            return new org.xml.sax.InputSource(RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_30);
        } else if (systemId!=null && systemId.endsWith(FILE_FACELETS_TAGLIB_SCHEMA_40)) {
            return new org.xml.sax.InputSource(RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_40);
        } else if (systemId!=null && systemId.endsWith(FILE_FACELETS_TAGLIB_SCHEMA_41)) {
            return new org.xml.sax.InputSource(RESOURCE_URL_FACELETS_TAGLIB_SCHEMA_41);
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

    public static JsfVersion extractVersion(Document document) {
        // first check the doc type to see if there is one
        DocumentType dt = document.getDoctype();
        JsfVersion value = JsfVersion.JSF_1_0;
        // This is the default version
        if (dt != null) {
            switch (dt.getPublicId()) {
                case JSF_ID_1_0:
                    value = JsfVersion.JSF_1_0;
                    break;
                case JSF_ID_1_1:
                    value = JsfVersion.JSF_1_1;
                    break;
                case JSF_ID_1_2:
                    value = JsfVersion.JSF_1_2;
                    break;
                case JSF_ID_2_0:
                    value = JsfVersion.JSF_2_0;
                    break;
                case JSF_ID_2_1:
                    value = JsfVersion.JSF_2_1;
                    break;
                case JSF_ID_2_2:
                    value = JsfVersion.JSF_2_2;
                    break;
                case JSF_ID_2_3:
                    value = JsfVersion.JSF_2_3;
                    break;
                case JSF_ID_3_0:
                    value = JsfVersion.JSF_3_0;
                    break;
                case JSF_ID_4_0:
                    value = JsfVersion.JSF_4_0;
                    break;
                case JSF_ID_4_1:
                    value = JsfVersion.JSF_4_1;
                    break;
                default:
                    break;
            }
        }
        return value;

    }

}