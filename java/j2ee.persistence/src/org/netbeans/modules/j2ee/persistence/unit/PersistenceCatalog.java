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


package org.netbeans.modules.j2ee.persistence.unit;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.openide.util.NbBundle;

/**
 * Catalog for persistence related schemas.
 *
 * @author Erno Mononen
 */
public class PersistenceCatalog implements CatalogReader, CatalogDescriptor2, org.xml.sax.EntityResolver {
    
    private static final String PERSISTENCE_OLD_NS = "http://java.sun.com/xml/ns/persistence"; // NOI18N
    private static final String PERSISTENCE_NS = "http://xmlns.jcp.org/xml/ns/persistence"; // NOI18N
    private static final String PERSISTENCE_JAKARTA_NS = "https://jakarta.ee/xml/ns/persistence"; // NOI18N
    private static final String ORM_OLD_NS = PERSISTENCE_OLD_NS +  "/orm"; // NOI18N
    private static final String ORM_NS = PERSISTENCE_NS +  "/orm"; // NOI18N
    private static final String ORM_JAKARTA_NS = PERSISTENCE_JAKARTA_NS +  "/orm"; // NOI18N
    private static final String RESOURCE_PATH = "nbres:/org/netbeans/modules/j2ee/persistence/dd/resources/"; //NOI18N 
    
    private List<SchemaInfo> schemas = new ArrayList<>();

    public PersistenceCatalog() {
        initialize();
    }

    private void initialize(){
        // persistence
        schemas.add(new SchemaInfo("persistence_1_0.xsd", RESOURCE_PATH, PERSISTENCE_OLD_NS));
        schemas.add(new SchemaInfo("persistence_2_0.xsd", RESOURCE_PATH, PERSISTENCE_OLD_NS));
        schemas.add(new SchemaInfo("persistence_2_1.xsd", RESOURCE_PATH, PERSISTENCE_NS));
        schemas.add(new SchemaInfo("persistence_2_2.xsd", RESOURCE_PATH, PERSISTENCE_NS));
        schemas.add(new SchemaInfo("persistence_3_0.xsd", RESOURCE_PATH, PERSISTENCE_JAKARTA_NS));
        schemas.add(new SchemaInfo("persistence_3_2.xsd", RESOURCE_PATH, PERSISTENCE_JAKARTA_NS));
        // orm
        schemas.add(new SchemaInfo("orm_1_0.xsd", RESOURCE_PATH, ORM_OLD_NS));
        schemas.add(new SchemaInfo("orm_2_0.xsd", RESOURCE_PATH, ORM_OLD_NS));
        schemas.add(new SchemaInfo("orm_2_1.xsd", RESOURCE_PATH, ORM_NS));
        schemas.add(new SchemaInfo("orm_2_2.xsd", RESOURCE_PATH, ORM_NS));
        schemas.add(new SchemaInfo("orm_3_0.xsd", RESOURCE_PATH, ORM_JAKARTA_NS));
        schemas.add(new SchemaInfo("orm_3_1.xsd", RESOURCE_PATH, ORM_JAKARTA_NS));
        schemas.add(new SchemaInfo("orm_3_2.xsd", RESOURCE_PATH, ORM_JAKARTA_NS));
    }
    
    @Override
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
    
    @Override
    public Iterator getPublicIDs() {
        List<String> result = new ArrayList<>();
        for (SchemaInfo each : schemas){
            result.add(each.getPublicId());
        }
        return result.iterator();
    }
    
    @Override
    public void refresh() {
    }
    
    @Override
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
    
    @Override
    public String resolveURI(String name) {
        return null;
    }
    
    @Override
    public String resolvePublic(String publicId) {
        return null;
    }
    
    @Override
    public void addCatalogListener(CatalogListener l) {
    }
    
    @Override
    public void removeCatalogListener(CatalogListener l) {
    }
    
    @Override
    public String getIconResource(int type) {
        return "org/netbeans/modules/j2ee/persistence/dd/resources/persistenceCatalog.gif"; // NOI18N
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(PersistenceCatalog.class, "LBL_PersistenceCatalog");
    }
    
    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(PersistenceCatalog.class, "DESC_PersistenceCatalog");
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }
    
    @Override
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
