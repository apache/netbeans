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
package org.netbeans.modules.j2ee.ddloaders.catalog;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.NbBundle;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A catalog that provides web, application, web services, web services client, javaee, j2ee,
 * ejb-jar and application client deployment descriptor schemas for code completion and validation.
 *
 * @author Erno Mononen
 */
public final class EnterpriseCatalog implements CatalogReader, CatalogDescriptor2, EntityResolver  {

    private static final String J2EE_NS = "http://java.sun.com/xml/ns/j2ee"; //NOI18N
    private static final String JAVAEE_NS = "http://java.sun.com/xml/ns/javaee"; //NOI18N
    private static final String XML_NS = "http://www.w3.org/2001/XMLSchema"; //NOI18N
    private static final String NEW_JAVAEE_NS = "http://xmlns.jcp.org/xml/ns/javaee"; //NOI18N
    private static final String JAKARTAEE_NS = "https://jakarta.ee/xml/ns/jakartaee"; //NOI18N
    private static final String RESOURCE_PATH = "nbres:/org/netbeans/modules/j2ee/dd/impl/resources/"; //NO18N

    private List<SchemaInfo> schemas = new ArrayList<>(256);

    private static final Logger LOGGER = Logger.getLogger(EnterpriseCatalog.class.getName());

    public EnterpriseCatalog() {
        initialize();
    }

    private void initialize(){
        // xml.xsd
        schemas.add(new SchemaInfo("xml.xsd", XML_NS));
        // Application Client schema
        schemas.add(new SchemaInfo("application-client_1_4.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("application-client_5.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("application-client_6.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("application-client_7.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("application-client_8.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("application-client_9.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("application-client_10.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("application-client_11.xsd", JAKARTAEE_NS));
        // Application schema
        schemas.add(new SchemaInfo("application_1_4.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("application_5.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("application_6.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("application_7.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("application_8.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("application_9.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("application_10.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("application_11.xsd", JAKARTAEE_NS));
        // Web services schema
        schemas.add(new SchemaInfo("j2ee_web_services_1_1.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("javaee_web_services_1_2.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("javaee_web_services_1_3.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("javaee_web_services_1_4.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("jakartaee_web_services_2_0.xsd", JAKARTAEE_NS));
        // Web services client schema
        schemas.add(new SchemaInfo("j2ee_web_services_client_1_1.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("javaee_web_services_client_1_2.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("javaee_web_services_client_1_3.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("javaee_web_services_client_1_4.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("jakartaee_web_services_client_2_0.xsd", JAKARTAEE_NS));
        // Java EE Connector schema
        schemas.add(new SchemaInfo("connector_1_5.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("connector_1_6.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("connector_1_7.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("connector_2_0.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("connector_2_1.xsd", JAKARTAEE_NS));
        // Enterprise JavaBeans Deployment Descriptor Schema
        schemas.add(new SchemaInfo("ejb-jar_2_1.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("ejb-jar_3_0.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("ejb-jar_3_1.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("ejb-jar_3_2.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("ejb-jar_4_0.xsd", JAKARTAEE_NS));
        // Web Application Deployment Descriptor schema
        schemas.add(new SchemaInfo("web-app_2_4.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("web-app_2_5.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("web-app_3_0.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("web-app_3_1.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("web-app_4_0.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("web-app_5_0.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("web-app_6_0.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("web-app_6_1.xsd", JAKARTAEE_NS));
        // Web Application Deployment Descriptor common definitions schema
        schemas.add(new SchemaInfo("web-common_3_0.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("web-common_3_1.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("web-common_4_0.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("web-common_5_0.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("web-common_6_0.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("web-common_6_1.xsd", JAKARTAEE_NS));
        // Web Application Deployment Descriptor fragment schema
        schemas.add(new SchemaInfo("web-fragment_3_0.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("web-fragment_3_1.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("web-fragment_4_0.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("web-fragment_5_0.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("web-fragment_6_0.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("web-fragment_6_1.xsd", JAKARTAEE_NS));
        // JavaServer Pages Deployment Descriptor schema
        schemas.add(new SchemaInfo("jsp_2_0.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("jsp_2_1.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("jsp_2_2.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("jsp_2_3.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("jsp_3_0.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("jsp_3_1.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("jsp_4_0.xsd", JAKARTAEE_NS));
        // J2EE and Java EE definitions file that contains common schema components
        schemas.add(new SchemaInfo("j2ee_1_4.xsd", J2EE_NS));
        schemas.add(new SchemaInfo("javaee_5.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("javaee_6.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("javaee_7.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("javaee_8.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("jakartaee_9.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("jakartaee_10.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("jakartaee_11.xsd", JAKARTAEE_NS));
        // web 2.2 and 2.3 dtds
        schemas.add(new SchemaInfo("web-app_2_2.dtd", "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN", true)); //NO18N
        schemas.add(new SchemaInfo("web-app_2_3.dtd", "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN", true)); //NO18N
        // Contexts and Dependency Injection schema
        schemas.add(new SchemaInfo("beans_1_0.xsd", JAVAEE_NS));
        schemas.add(new SchemaInfo("beans_1_1.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("beans_2_0.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("beans_3_0.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("beans_4_0.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("beans_4_1.xsd", JAKARTAEE_NS));
        // Java EE application permissions schema
        schemas.add(new SchemaInfo("permissions_7.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("permissions_9.xsd", JAKARTAEE_NS));
        schemas.add(new SchemaInfo("permissions_10.xsd", JAKARTAEE_NS));
        // Schema for batch.xml-based artifact loading in Java Batch
        schemas.add(new SchemaInfo("batchXML_1_0.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("batchXML_2_0.xsd", JAKARTAEE_NS));
        // Batch Job Specification Language (JSL) schema
        schemas.add(new SchemaInfo("jobXML_1_0.xsd", NEW_JAVAEE_NS));
        schemas.add(new SchemaInfo("jobXML_2_0.xsd", JAKARTAEE_NS));

    }

    @Override
    public String getIconResource(int type) {
        return "org/netbeans/modules/j2ee/ddloaders/catalog/resources/DDCatalog.gif"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage (EnterpriseCatalog.class, "LBL_EnterpriseCatalog");
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (EnterpriseCatalog.class, "DESC_EnterpriseCatalog");
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        // additional logging for #127276
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Resolving entity [publicId: ''{0}'', systemId: ''{1}'']", new Object[]{publicId, systemId});
        }
        if (systemId == null){
            return null;
        }
        for (SchemaInfo each : schemas){
            if (systemId.endsWith(each.getSchemaName())){
                // additional logging for #127276
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Got resource: {0}", each.getResourcePath());
                }
                return new InputSource(each.getResourcePath());
            }
        }
        // additional logging for #127276
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "No resource found for publicId: {0}", publicId);
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

    /**
     * A simple holder for the information needed
     * for resolving the resource path and public id of a schema.
     */
    private static class SchemaInfo {

        private final String schemaName;
        private final String namespace;
        private final boolean dtd;

        public SchemaInfo(String schemaName, String namespace) {
            this(schemaName, namespace, false);
        }

        public SchemaInfo(String schemaName, String namespace, boolean dtd){
            this.schemaName = schemaName;
            this.namespace = namespace;
            this.dtd = dtd;
        }

        public String getResourcePath() {
            return RESOURCE_PATH + getSchemaName();
        }

        public String getSchemaName() {
            return schemaName;
        }

        public String getPublicId(){
            if (dtd){
                return namespace;
            }
            return "SCHEMA:" + namespace + "/" + schemaName; //NO18N
        }

    }
}
