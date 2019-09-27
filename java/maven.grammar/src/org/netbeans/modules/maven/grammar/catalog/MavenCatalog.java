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


package org.netbeans.modules.maven.grammar.catalog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.xml.catalog.spi.*;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
/**
 *
 * @author Milos Kleint
 */
public class MavenCatalog implements CatalogReader, CatalogDescriptor2, org.xml.sax.EntityResolver {
    private static final String MAVEN2_ICON = "org/netbeans/modules/maven/resources/Maven2Icon.gif";

    private static final String POM_4_0_0 = "http://maven.apache.org/maven-v4_0_0.xsd"; // NOI18N
    private static final String POM_ALT_4_0_0 = "http://maven.apache.org/xsd/maven-4.0.0.xsd"; // NOI18N
    private static final String ID_POM_4_0_0 = "SCHEMA:" + POM_4_0_0; // NOI18N
    private static final String SETTINGS_1_0_0 = "http://maven.apache.org/xsd/settings-1.0.0.xsd"; // NOI18N
    private static final String ID_SETTINGS_1_0_0 = "SCHEMA:" + SETTINGS_1_0_0; // NOI18N
    public static final String SETTINGS_1_1_0 = "http://maven.apache.org/xsd/settings-1.1.0.xsd"; // NOI18N
    private static final String ID_SETTINGS_1_1_0 = "SCHEMA:" + SETTINGS_1_1_0; // NOI18N
    private static final String ASSEMBLY_1_0_0 = "http://maven.apache.org/xsd/assembly-1.0.0.xsd"; // NOI18N
    private static final String ID_ASSEMBLY_1_0_0 = "SCHEMA:" + ASSEMBLY_1_0_0; // NOI18N
    private static final String ASSEMBLY_1_1_0 = "http://maven.apache.org/xsd/assembly-1.1.0.xsd"; // NOI18N
    private static final String ID_ASSEMBLY_1_1_0 = "SCHEMA:" + ASSEMBLY_1_1_0; // NOI18N
    private static final String ASSEMBLY_1_1_1 = "http://maven.apache.org/xsd/assembly-1.1.1.xsd"; // NOI18N
    private static final String ID_ASSEMBLY_1_1_1 = "SCHEMA:" + ASSEMBLY_1_1_1; // NOI18N
    private static final String ARCHETYPE_1_0_0 = "http://maven.apache.org/xsd/archetype-1.0.0.xsd"; // NOI18N
    private static final String ID_ARCHETYPE_1_0_0 = "SCHEMA:" + ARCHETYPE_1_0_0; // NOI18N
    private static final String ARCHETYPE_CATALOG_1_0_0 = "http://maven.apache.org/xsd/archetype-catalog-1.0.0.xsd"; // NOI18N
    private static final String ID_ARCHETYPE_CATALOG_1_0_0 = "SCHEMA:" + ARCHETYPE_CATALOG_1_0_0; // NOI18N
    private static final String ARCHETYPE_DESCRIPTOR_1_0_0 = "http://maven.apache.org/xsd/archetype-descriptor-1.0.0.xsd"; // NOI18N
    private static final String ID_ARCHETYPE_DESCRIPTOR_1_0_0 = "SCHEMA:" + ARCHETYPE_DESCRIPTOR_1_0_0; // NOI18N
            
    private static final String URL_POM_4_0_0 ="nbres:/org/netbeans/modules/maven/grammar/maven-4.0.0.xsd"; // NOI18N
    private static final String URL_SETTINGS_1_0_0 ="nbres:/org/netbeans/modules/maven/grammar/settings-1.0.0.xsd"; // NOI18N
    private static final String URL_SETTINGS_1_1_0 ="nbres:/org/netbeans/modules/maven/grammar/settings-1.1.0.xsd"; // NOI18N
    private static final String URL_ASSEMBLY_1_0_0 ="nbres:/org/netbeans/modules/maven/grammar/assembly-1.0.0.xsd"; // NOI18N
    private static final String URL_ASSEMBLY_1_1_0 ="nbres:/org/netbeans/modules/maven/grammar/assembly-1.1.0.xsd"; // NOI18N
    private static final String URL_ASSEMBLY_1_1_1 ="nbres:/org/netbeans/modules/maven/grammar/assembly-1.1.1.xsd"; // NOI18N
    private static final String URL_ARCHETYPE_1_0_0 ="nbres:/org/netbeans/modules/maven/grammar/archetype-1.0.0.xsd"; // NOI18N
    private static final String URL_ARCHETYPE_CATALOG_1_0_0 ="nbres:/org/netbeans/modules/maven/grammar/archetype-catalog-1.0.0.xsd"; // NOI18N
    private static final String URL_ARCHETYPE_DESCRIPTOR_1_0_0 ="nbres:/org/netbeans/modules/maven/grammar/archetype-descriptor-1.0.0.xsd"; // NOI18N
    
    /** Creates a new instance of MavenCatalog */
    public MavenCatalog() {
    }
    
    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    @Override
    public Iterator getPublicIDs() {
        List<String> list = new ArrayList<String>();
        list.add(ID_POM_4_0_0);
        list.add(ID_SETTINGS_1_0_0);
        list.add(ID_SETTINGS_1_1_0);
        list.add(ID_ASSEMBLY_1_0_0);
        list.add(ID_ASSEMBLY_1_1_0);
        list.add(ID_ASSEMBLY_1_1_1);
        list.add(ID_ARCHETYPE_1_0_0);
        list.add(ID_ARCHETYPE_CATALOG_1_0_0);
        list.add(ID_ARCHETYPE_DESCRIPTOR_1_0_0);
        return list.iterator();
    }
    
    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    @Override
    public String getSystemID(String publicId) {
        if (ID_POM_4_0_0.equals(publicId))
            return URL_POM_4_0_0;
        else if (ID_SETTINGS_1_0_0.equals(publicId))
            return URL_SETTINGS_1_0_0;
        else if (ID_SETTINGS_1_1_0.equals(publicId))
            return URL_SETTINGS_1_1_0;
        else if (ID_ASSEMBLY_1_0_0.equals(publicId))
            return URL_ASSEMBLY_1_0_0;
        else if (ID_ASSEMBLY_1_1_0.equals(publicId))
            return URL_ASSEMBLY_1_1_0;
        else if (ID_ASSEMBLY_1_1_1.equals(publicId))
            return URL_ASSEMBLY_1_1_1;
        else if (ID_ARCHETYPE_1_0_0.equals(publicId))
            return URL_ARCHETYPE_1_0_0;
        else if (ID_ARCHETYPE_CATALOG_1_0_0.equals(publicId))
            return URL_ARCHETYPE_CATALOG_1_0_0;
        else if (ID_ARCHETYPE_DESCRIPTOR_1_0_0.equals(publicId))
            return URL_ARCHETYPE_DESCRIPTOR_1_0_0;
        else return null;
    }
    
    /**
     * Refresh content according to content of mounted catalog.
     */
    @Override
    public void refresh() {
    }
    
    /**
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    @Override
    public void addCatalogListener(CatalogListener l) {
    }
    
    /**
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    @Override
    public void removeCatalogListener(CatalogListener l) {
    }
    
    /** Registers new listener.  */
    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
     /** Unregister the listener.  */
    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
    /**
     * @return I18N display name
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage (MavenCatalog.class, "LBL_MavenCatalog");  //NOI18N
    }
    
    /**
     * Return visuaized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    @Override
    public String getIconResource(int type) {
        return MAVEN2_ICON; // NOI18N
    }
    
    /**
     * @return I18N short description
     */
    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (MavenCatalog.class, "DESC_MavenCatalog");     //NOI18N
    }
    
   /**
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for publicId/systemId 
     */    
    @Override
    public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) throws org.xml.sax.SAXException, java.io.IOException {
        if (POM_4_0_0.equals(systemId) || POM_ALT_4_0_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_POM_4_0_0);
        } else if (SETTINGS_1_0_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_SETTINGS_1_0_0);
        } else if (SETTINGS_1_1_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_SETTINGS_1_1_0);
        } else if (ASSEMBLY_1_0_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_ASSEMBLY_1_0_0);
        } else if (ASSEMBLY_1_1_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_ASSEMBLY_1_1_0);
        } else if (ASSEMBLY_1_1_1.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_ASSEMBLY_1_1_1);
        } else if (ARCHETYPE_1_0_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_ARCHETYPE_1_0_0);
        } else if (ARCHETYPE_CATALOG_1_0_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_ARCHETYPE_CATALOG_1_0_0);
        } else if (ARCHETYPE_DESCRIPTOR_1_0_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_ARCHETYPE_DESCRIPTOR_1_0_0);
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
