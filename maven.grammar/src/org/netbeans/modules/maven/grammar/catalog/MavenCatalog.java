/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
