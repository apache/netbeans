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

/*
 * StrutsCatalog.java
 *
 * Created on April 24, 2002, 10:38 PM
 */

package org.netbeans.modules.web.struts;

import java.util.List;
import java.util.ArrayList;
import org.netbeans.modules.xml.catalog.spi.*;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Petr Pisl
 */
public class StrutsCatalog implements CatalogReader, CatalogDescriptor2, org.xml.sax.EntityResolver {
    
    private static final String STRUTS_ID_1_0 = "-//Apache Software Foundation//DTD Struts Configuration 1.0//EN"; // NOI18N
    private static final String STRUTS_ID_1_1 = "-//Apache Software Foundation//DTD Struts Configuration 1.1//EN"; // NOI18N
    private static final String STRUTS_ID_1_2 = "-//Apache Software Foundation//DTD Struts Configuration 1.2//EN"; // NOI18N
    private static final String STRUTS_ID_1_3 = "-//Apache Software Foundation//DTD Struts Configuration 1.3//EN"; // NOI18N
    private static final String TILES_ID_1_1 = "-//Apache Software Foundation//DTD Tiles Configuration 1.1//EN"; // NOI18N
    private static final String TILES_ID_1_3 = "-//Apache Software Foundation//DTD Tiles Configuration 1.3//EN"; // NOI18N
    private static final String VALIDATOR_ID_1_1 = "-//Apache Software Foundation//DTD Commons Validator Rules Configuration 1.1//EN"; // NOI18N
    private static final String VALIDATOR_ID_1_1_3 = "-//Apache Software Foundation//DTD Commons Validator Rules Configuration 1.1.3//EN"; // NOI18N
    private static final String VALIDATOR_ID_1_2_0 = "-//Apache Software Foundation//DTD Commons Validator Rules Configuration 1.2.0//EN"; // NOI18N
    private static final String VALIDATOR_ID_1_3_0 = "-//Apache Software Foundation//DTD Commons Validator Rules Configuration 1.3.0//EN"; // NOI18N
    
    private static final String URL_STRUTS_1_0 ="nbres:/org/netbeans/modules/web/struts/resources/struts-config_1_0.dtd"; // NOI18N
    private static final String URL_STRUTS_1_1 ="nbres:/org/netbeans/modules/web/struts/resources/struts-config_1_1.dtd"; // NOI18N
    private static final String URL_STRUTS_1_2 ="nbres:/org/netbeans/modules/web/struts/resources/struts-config_1_2.dtd"; // NOI18N
    private static final String URL_STRUTS_1_3 ="nbres:/org/netbeans/modules/web/struts/resources/struts-config_1_3.dtd"; // NOI18N
    private static final String URL_TILES_1_1 = "nbres:/org/netbeans/modules/web/struts/resources/tiles-config_1_1.dtd"; // NOI18N
    private static final String URL_TILES_1_3 = "nbres:/org/netbeans/modules/web/struts/resources/tiles-config_1_1.dtd"; // NOI18N
    private static final String URL_VALIDATOR_1_1 = "nbres:/org/netbeans/modules/web/struts/resources/validator_1_1.dtd"; // NOI18N
    private static final String URL_VALIDATOR_1_1_3 = "nbres:/org/netbeans/modules/web/struts/resources/validator_1_1_3.dtd"; // NOI18N
    private static final String URL_VALIDATOR_1_2_0 = "nbres:/org/netbeans/modules/web/struts/resources/validator_1_2_0.dtd"; // NOI18N
    private static final String URL_VALIDATOR_1_3_0 = "nbres:/org/netbeans/modules/web/struts/resources/validator_1_3_0.dtd"; // NOI18N
    
    /** Creates a new instance of StrutsCatalog */
    public StrutsCatalog() {
    }
    
    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public java.util.Iterator getPublicIDs() {
        List<String> list = new ArrayList<String>();
        list.add(STRUTS_ID_1_0);
        list.add(STRUTS_ID_1_1);
        list.add(STRUTS_ID_1_2);
        list.add(STRUTS_ID_1_3);
        list.add(TILES_ID_1_1);
        list.add(TILES_ID_1_3);
        list.add(VALIDATOR_ID_1_1);
        list.add(VALIDATOR_ID_1_1_3);
        list.add(VALIDATOR_ID_1_2_0);
        list.add(VALIDATOR_ID_1_3_0);
        return list.listIterator();
    }
    
    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {
        if (STRUTS_ID_1_0.equals(publicId))
            return URL_STRUTS_1_0;
        else if (STRUTS_ID_1_1.equals(publicId))
            return URL_STRUTS_1_1;
        else if (STRUTS_ID_1_2.equals(publicId))
            return URL_STRUTS_1_2;
        else if (STRUTS_ID_1_3.equals(publicId))
            return URL_STRUTS_1_3;
        else if (TILES_ID_1_1.equals(publicId))
            return URL_TILES_1_1;
        else if (TILES_ID_1_3.equals(publicId))
            return URL_TILES_1_3;
        else if (VALIDATOR_ID_1_1.equals(publicId))
            return URL_VALIDATOR_1_1;
        else if (VALIDATOR_ID_1_1_3.equals(publicId))
            return URL_VALIDATOR_1_1_3;
        else if (VALIDATOR_ID_1_2_0.equals(publicId))
            return URL_VALIDATOR_1_2_0;
        else if (VALIDATOR_ID_1_3_0.equals(publicId))
            return URL_VALIDATOR_1_3_0;
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
    
    /** Unregister the listener.  */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
    /**
     * @return I18N display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage(StrutsCatalog.class, "LBL_StrutsCatalog");  //NOI18N
    }
    
    /**
     * Return visuaized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public String getIconResource(int type) {
        return "org/netbeans/modules/web/struts/resources/StrutsCatalog.png"; // NOI18N
    }
    
    /**
     * @return I18N short description
     */
    public String getShortDescription() {
        return NbBundle.getMessage(StrutsCatalog.class, "DESC_StrutsCatalog");     //NOI18N
    }
    
    /**
     * Resolves schema definition file for taglib descriptor (spec.1_1, 1_2, 2_0)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for publisId,
     */
    public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) throws org.xml.sax.SAXException, java.io.IOException {
        if (STRUTS_ID_1_0.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_STRUTS_1_0);
        } else if (STRUTS_ID_1_1.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_STRUTS_1_1);
        } else if (STRUTS_ID_1_2.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_STRUTS_1_2);
        } else if (STRUTS_ID_1_3.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_STRUTS_1_3);
        } else if (TILES_ID_1_1.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_TILES_1_1);
        } else if (TILES_ID_1_3.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_TILES_1_3);
        } else if (VALIDATOR_ID_1_1.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_VALIDATOR_1_1);
        } else if (VALIDATOR_ID_1_1_3.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_VALIDATOR_1_1_3);
        } else if (VALIDATOR_ID_1_2_0.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_VALIDATOR_1_2_0);
        } else if (VALIDATOR_ID_1_3_0.equals(publicId)) {
            return new org.xml.sax.InputSource(URL_VALIDATOR_1_3_0);
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
