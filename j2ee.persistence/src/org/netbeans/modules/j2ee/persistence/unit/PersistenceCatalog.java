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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.modules.j2ee.persistence.unit;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.ImageUtilities;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Catalog for persistence related schemas.
 *
 * @author Erno Mononen
 */
public class PersistenceCatalog implements CatalogReader, CatalogDescriptor2, org.xml.sax.EntityResolver {
    
    private static final String PERSISTENCE_OLD_NS = "http://java.sun.com/xml/ns/persistence"; // NOI18N
    private static final String PERSISTENCE_NS = "http://xmlns.jcp.org/xml/ns/persistence"; // NOI18N
    private static final String ORM_OLD_NS = PERSISTENCE_OLD_NS +  "/orm"; // NOI18N
    private static final String ORM_NS = PERSISTENCE_NS +  "/orm"; // NOI18N
    private static final String RESOURCE_PATH = "nbres:/org/netbeans/modules/j2ee/persistence/dd/resources/"; //NOI18N 
    
    private List<SchemaInfo> schemas = new ArrayList<SchemaInfo>();

    public PersistenceCatalog() {
        initialize();
    }

    private void initialize(){
        // persistence
        schemas.add(new SchemaInfo("persistence_1_0.xsd", RESOURCE_PATH, PERSISTENCE_OLD_NS));
        schemas.add(new SchemaInfo("persistence_2_0.xsd", RESOURCE_PATH, PERSISTENCE_OLD_NS));
        schemas.add(new SchemaInfo("persistence_2_1.xsd", RESOURCE_PATH, PERSISTENCE_NS));
        // orm
        schemas.add(new SchemaInfo("orm_1_0.xsd", RESOURCE_PATH, ORM_OLD_NS));
        schemas.add(new SchemaInfo("orm_2_0.xsd", RESOURCE_PATH, ORM_OLD_NS));
        schemas.add(new SchemaInfo("orm_2_1.xsd", RESOURCE_PATH, ORM_NS));
    }
    
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (systemId == null){
            return null;
        }
        for (SchemaInfo each : schemas){
            if (systemId.endsWith(each.getSchemaName())){
                return new InputSource(each.getResourcePath());
            }
        }
        return null;
    }
    
    public Iterator getPublicIDs() {
        List<String> result = new ArrayList<String>();
        for (SchemaInfo each : schemas){
            result.add(each.getPublicId());
        }
        return result.iterator();
    }
    
    public void refresh() {
    }
    
    public String getSystemID(String publicId) {
        if (publicId == null){
            return null;
        }
        for (SchemaInfo each : schemas){
            if (each.getPublicId().equals(publicId)){
                return each.getResourcePath();
            }
        }
        return null;
    }
    
    public String resolveURI(String name) {
        return null;
    }
    
    public String resolvePublic(String publicId) {
        return null;
    }
    
    public void addCatalogListener(CatalogListener l) {
    }
    
    public void removeCatalogListener(CatalogListener l) {
    }
    
    public String getIconResource(int type) {
        return "org/netbeans/modules/j2ee/persistence/dd/resources/persistenceCatalog.gif"; // NOI18N
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(PersistenceCatalog.class, "LBL_PersistenceCatalog");
    }
    
    public String getShortDescription() {
        return NbBundle.getMessage(PersistenceCatalog.class, "DESC_PersistenceCatalog");
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    /**
     * A simple holder for the information needed
     * for resolving the resource path and public id of a schema.
     * <i>copied from j2ee/ddloaders EnterpriseCatalog</i>
     */ 
    private static class SchemaInfo {
        
        private final String schemaName;
        private final String resourcePath;
        private final String namespace;

        public SchemaInfo(String schemaName, String resourcePath, String namespace) {
            this.schemaName = schemaName;
            this.resourcePath = resourcePath + schemaName;
            this.namespace = namespace;
        }

        public String getResourcePath() {
            return resourcePath;
        }

        public String getSchemaName() {
            return schemaName;
        }
        
        public String getPublicId(){
            return "SCHEMA:" + namespace + "/" + schemaName; //NOI18N
        }
        
    }
    
}
