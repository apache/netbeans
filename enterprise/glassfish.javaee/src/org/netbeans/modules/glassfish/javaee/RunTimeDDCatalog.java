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

package org.netbeans.modules.glassfish.javaee;

import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.netbeans.modules.xml.api.model.DTDUtil;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.xml.api.model.GrammarQueryManager;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/** Catalog for GlassFish V3 DTDs that enables completion support in editor.
 *  This is basically a copy of the class in org.netbeans.modules.j2ee.sun.ide.j2ee
 *  with a few changes for maintaining 2 instances and for getting the root in a
 *  slightly different way.
 *
 * @author Ludo
 */

public class RunTimeDDCatalog extends GrammarQueryManager implements CatalogReader, CatalogDescriptor2,org.xml.sax.EntityResolver  {
    
    private static final String XML_XSD="http://www.w3.org/2001/xml.xsd"; // NOI18N
    private static final String XML_XSD_DEF="<?xml version='1.0'?><xs:schema targetNamespace=\"http://www.w3.org/XML/1998/namespace\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xml:lang=\"en\"><xs:attribute name=\"lang\" type=\"xs:language\"><xs:annotation><xs:documentation>In due course, we should install the relevant ISO 2- and 3-letter codes as the enumerated possible values . . .</xs:documentation></xs:annotation></xs:attribute></xs:schema>"; // NOI18N
    private static final String TypeToURLMap[] = {
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 J2EE Application 1.3//EN" 	, "sun-application_1_3-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 J2EE Application 1.4//EN" 	, "sun-application_1_4-0.dtd" , ///[THIS IS DEPRECATED]
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 J2EE Application 1.4//EN"                , "sun-application_1_4-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 J2EE Application 1.4//EN"                , "sun-application_1_4-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 Java EE Application 5.0//EN"             , "sun-application_5_0-0.dtd",
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 EJB 2.0//EN"                     , "sun-ejb-jar_2_0-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 EJB 2.1//EN"                     , "sun-ejb-jar_2_1-0.dtd" , ///[THIS IS DEPRECATED]
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 EJB 2.1//EN"                             , "sun-ejb-jar_2_1-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 EJB 2.1//EN"                             , "sun-ejb-jar_2_1-1.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 EJB 3.0//EN"                             , "sun-ejb-jar_3_0-0.dtd",
        "-//Sun Microsystems, Inc.//DTD Application Server 9.1.1 EJB 3.0//EN"                           , "sun-ejb-jar_3_0-1.dtd",
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Application Client 1.3//EN" 	, "sun-application-client_1_3-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 Application Client 1.4//EN" 	, "sun-application-client_1_4-0.dtd" , ///[THIS IS DEPRECATED]
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Application Client 1.4//EN"              , "sun-application-client_1_4-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 Application Client 1.4//EN"              , "sun-application-client_1_4-1.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 Application Client 5.0//EN"              , "sun-application-client_5_0-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Connector 1.0//EN"               , "sun-connector_1_0-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 Connector 1.5//EN"                       , "sun-connector_1_5-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Servlet 2.3//EN"                 , "sun-web-app_2_3-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 Servlet 2.4//EN"                 , "sun-web-app_2_4-0.dtd" , ///[THIS IS DEPRECATED]
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Servlet 2.4//EN"                         , "sun-web-app_2_4-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Web Server 6.1 Servlet 2.3//EN"                         , "sun-web-app_2_3-1.dtd" ,                
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 Servlet 2.4//EN"                         , "sun-web-app_2_4-1.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 Servlet 2.5//EN"                         , "sun-web-app_2_5-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Application Client Container 1.0//EN" 	, "sun-application-client-container_1_0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 OR Mapping //EN"                 , "sun-cmp-mapping_1_0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 OR Mapping//EN"                          , "sun-cmp-mapping_1_1.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 OR Mapping//EN"                          , "sun-cmp-mapping_1_2.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Domain//EN"                              , "sun-domain_1_0.dtd" ,
        "-//Sun Microsystems Inc.//DTD Application Server 8.0 Application Client Container//EN" 	, "sun-application-client-container_1_2.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 Application Client Container //EN" 	, "sun-application-client-container_1_1.dtd" ,
        "-//Sun Microsystems Inc.//DTD Application Server 8.0 Domain//EN"                              ,"sun-domain_1_1.dtd",
        "-//Sun Microsystems Inc.//DTD Application Server 8.1 Domain//EN"                              ,"sun-domain_1_1.dtd",
        "-//Sun Microsystems Inc.//DTD Application Server 9.0 Domain//EN"                              ,"sun-domain_1_2.dtd",
        "-//Sun Microsystems Inc.//DTD Application Server 9.1 Domain//EN"                              ,"sun-domain_1_3.dtd",
        "-//Sun Microsystems Inc.//DTD GlassFish Communications Server 1.5 Domain//EN"      ,"sun-domain_1_4.dtd",
        "-//Sun Microsystems Inc.//DTD GlassFish Communications Server 2.0 Domain//EN"      ,"sun-domain_1_5.dtd",
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 SIP Servlet 1.1//EN"                    , "sun-sip-app_1_1-0.dtd",
        
        "-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN"                                       , "application_1_3.dtd",
        "-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN"                                       , "application_1_2.dtd",
        "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN"                                   , "ejb-jar_2_0.dtd",
        "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN"                                   , "ejb-jar_1_1.dtd",
        "-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.3//EN"                                , "application-client_1_3.dtd",
        "-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.2//EN"                                , "application-client_1_2.dtd",
        "-//Sun Microsystems, Inc.//DTD Connector 1.0//EN"                                              , "connector_1_0.dtd",
        "-//Java Community Process//DTD SIP Application 1.0//EN"                                    , "sip-app_1_0.dtd",
        "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"                                        , "web-app_2_3.dtd",
        "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN"                                        , "web-app_2_2.dtd",
        "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN"                                        , "web-jsptaglibrary_1_2.dtd",
        "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN"                                        , "web-jsptaglibrary_1_1.dtd",
    };
    
    private static final String JavaEE6TypeToURLMap[] = {
        "-//Sun Microsystems, Inc.//DTD GlassFish Application Server 3.0 Java EE Application 6.0//EN"   , "sun-application_6_0-0.dtd",
        "-//GlassFish.org//DTD GlassFish Application Server 3.1 Java EE Application 6.0//EN"   , "glassfish-application_6_0-1.dtd",
        "-//Sun Microsystems, Inc.//DTD GlassFish Application Server 3.0 Application Client 6.0//EN"    , "sun-application-client_6_0-0.dtd" ,
        "-//GlassFish.org//DTD GlassFish Application Server 3.1 Java EE Application Client 6.0//EN"    , "glassfish-application-client_6_0-1.dtd" ,
        "-//Sun Microsystems, Inc.//DTD GlassFish Application Server 3.0 EJB 3.1//EN"                   , "sun-ejb-jar_3_1-0.dtd",
        "-//GlassFish.org//DTD GlassFish Application Server 3.1 EJB 3.1//EN"                   , "glassfish-ejb-jar_3_1-1.dtd",
        "-//Sun Microsystems, Inc.//DTD GlassFish Application Server 3.0 Servlet 3.0//EN"                         , "sun-web-app_3_0-0.dtd" ,
        "-//GlassFish.org//DTD GlassFish Application Server 3.1 Servlet 3.0//EN"                         , "glassfish-web-app_3_0-1.dtd" ,
        "-//GlassFish.org//DTD GlassFish Application Server 3.1 Resource Definitions //EN", "glassfish-resources_1_5.dtd",
        "-//GlassFish.org//DTD GlassFish Application Server 3.1 Resource Definitions//EN", "glassfish-resources_1_5.dtd"
    };

        /*******NetBeans 3.6 is NOT ready yet to support schemas for code completion... What a pity!:        */
    private static final String SchemaToURLMap[] = {
        
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd"                    , "ejb-jar_2_1",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/ejb-jar_3_0.xsd"                  , "ejb-jar_3_0",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd"                  , "ejb-jar_3_1",
        "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/ejb-jar_3_2.xsd"                 , "ejb-jar_3_2",
        "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/ejb-jar_4_0.xsd"                , "ejb-jar_4_0",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/application-client_1_4.xsd"         , "application-client_1_4",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/application_1_4.xsd"                , "application_1_4",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/application-client_5.xsd"         , "application-client_5",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/application_5.xsd"                , "application_5",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/application-client_6.xsd"         , "application-client_6",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/application_6.xsd"                , "application_6",
        "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/application-client_7.xsd"        , "application-client_7",
        "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/application_7.xsd"               , "application_7",
        "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/application-client_8.xsd"        , "application-client_8",
        "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/application_8.xsd"               , "application_8",
        "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/application-client_9.xsd"       , "application-client_9",
        "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/application_9.xsd"              , "application_9",
        "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/application-client_10.xsd"       , "application-client_10",
        "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/application_10.xsd"              , "application_10",
        "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/application-client_11.xsd"       , "application-client_11",
        "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/application_11.xsd"              , "application_11",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/jax-rpc-ri-config.xsd"              , "jax-rpc-ri-config",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/connector_1_5.xsd"                  , "connector_1_5",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/connector_1_6.xsd"                , "connector_1_6",
        "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/connector_1_7.xsd"               , "connector_1_7",
        "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/connector_2_0.xsd"              , "connector_2_0",
        "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/connector_2_1.xsd"              , "connector_2_1",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/jsp_2_0.xsd"                        , "jsp_2_0.xsd",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/datatypes.dtd"                      , "datatypes",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"                    , "web-app_2_4",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/web-jsptaglibrary_2_0.xsd"          , "web-jsptaglibrary_2_0",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/j2ee_1_4.xsd"                       , "j2ee_1_4",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/j2ee_jaxrpc_mapping_1_1.xsd"              , "j2ee_jaxrpc_mapping_1_1",
        "SCHEMA:http://www.ibm.com/webservices/xsd/j2ee_web_services_1_1.xsd"             ,"j2ee_web_services_1_1",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/j2ee_web_services_client_1_1.xsd"         ,"j2ee_web_services_client_1_1",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/javaee_web_services_1_2.xsd"            ,"javaee_web_services_1_2",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/javaee_web_services_client_1_2.xsd"     ,"javaee_web_services_client_1_2",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/javaee_web_services_1_3.xsd"            ,"javaee_web_services_1_3",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/javaee_web_services_client_1_3.xsd"     ,"javaee_web_services_client_1_3",
        "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/javaee_web_services_1_4.xsd"           ,"javaee_web_services_1_4",
        "SCHEMA:http://xmlns.jcp.org/xml/ns/javaee/javaee_web_services_client_1_4.xsd"    ,"javaee_web_services_client_1_4",
        "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/jakartaee_web_services_2_0.xsd"          ,"jakartaee_web_services_2_0",
        "SCHEMA:https://jakarta.ee/xml/ns/jakartaee/jakartaee_web_services_client_2_0.xsd"   ,"jakartaee_web_services_client_2_0",
        "SCHEMA:http://java.sun.com/xml/ns/persistence/orm_1_0.xsd"                          , "orm_1_0",
        "SCHEMA:http://java.sun.com/xml/ns/persistence/orm_2_0.xsd"                          , "orm_2_0",
        "SCHEMA:http://xmlns.jcp.org/xml/ns/persistence/orm_2_1.xsd"                         , "orm_2_1",
        "SCHEMA:http://xmlns.jcp.org/xml/ns/persistence/orm_2_2.xsd"                         , "orm_2_2",
        "SCHEMA:https://jakarta.ee/xml/ns/persistence/orm/orm_3_0.xsd"                       , "orm_3_0",
        "SCHEMA:https://jakarta.ee/xml/ns/persistence/orm/orm_3_1.xsd"                       , "orm_3_1",
        "SCHEMA:https://jakarta.ee/xml/ns/persistence/orm/orm_3_2.xsd"                       , "orm_3_2",
        "SCHEMA:http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd"                  , "persistence_1_0",
        "SCHEMA:http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd"                  , "persistence_2_0",
        "SCHEMA:http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd"                 , "persistence_2_1",
        "SCHEMA:http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd"                 , "persistence_2_2",
        "SCHEMA:https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"                   , "persistence_3_0",
        "SCHEMA:https://jakarta.ee/xml/ns/persistence/persistence_3_2.xsd"                   , "persistence_3_2",
    };
    
    private static final String JavaEE6SchemaToURLMap[] = {

        "SCHEMA:http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd"              , "ejb-jar_3_1",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/jsp_2_2.xsd"                    , "jsp_2_2",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"              , "web-app_3_0",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/web-common_3_0.xsd"           , "web-common_3_0",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/web-fragment_3_0.xsd"         , "web-fragment_3_0",

        "SCHEMA:http://xmlns.oracle.com/weblogic/weblogic-application-client/1.0/weblogic-application-client.xsd", "weblogic-application-client",
        "SCHEMA:http://xmlns.oracle.com/weblogic/weblogic-application/1.0/weblogic-application.xsd", "weblogic-application",
        "SCHEMA:http://xmlns.oracle.com/weblogic/weblogic-connector/1.0/weblogic-connector.xsd", "weblogic-connector",
        "SCHEMA:http://xmlns.oracle.com/weblogic/weblogic-ejb-jar/1.0/weblogic-ejb-jar.xsd", "weblogic-ejb-jar",
        "SCHEMA:http://xmlns.oracle.com/weblogic/weblogic-javaee/1.0/weblogic-javaee.xsd", "weblogic-javaee",
        "SCHEMA:http://xmlns.oracle.com/weblogic/weblogic-jms/1.0/weblogic-jms.xsd", "weblogic-jms",
        "SCHEMA:http://xmlns.oracle.com/weblogic/weblogic-web-app/1.0/weblogic-web-app.xsd", "weblogic-web-app",
        "SCHEMA:http://xmlns.oracle.com/weblogic/weblogic-webservices/1.0/weblogic-webservices.xsd", "weblogic-webservices",
        "SCHEMA:http://xmlns.oracle.com/weblogic/jdbc-data-source/1.0/jdbc-data-source.xsd", "jdbc-data-source",
    };

    private static Map<ServerInstanceProvider, RunTimeDDCatalog> ddCatalogMap = new HashMap<>();
//    private static RunTimeDDCatalog preludeDDCatalog;
    private static RunTimeDDCatalog javaEE6DDCatalog;

    private File platformRootDir=null;
    private String displayNameKey;
    private String shortDescriptionKey;
    private boolean hasAdditionalMap = false;

    /** Creates a new instance of RunTimeDDCatalog */
    public RunTimeDDCatalog() {
    }
    
    public void setInstanceProvider(ServerInstanceProvider ip) {
        if (ddCatalogMap.get(ip) == null) {
            ddCatalogMap.put(ip, this);
        }
    }
    /** Factory method providing catalog for XML completion of DD */
    public static RunTimeDDCatalog getRunTimeDDCatalog(ServerInstanceProvider ip){
        return ddCatalogMap.get(ip);
    }

    /** Factory method providing catalog for XML completion of DD */
    public static synchronized RunTimeDDCatalog getEE6RunTimeDDCatalog(){
        if (javaEE6DDCatalog==null) {
            javaEE6DDCatalog = new RunTimeDDCatalog();
            javaEE6DDCatalog.displayNameKey = "LBL_V3RunTimeDDCatalog"; // NOI18N
            javaEE6DDCatalog.shortDescriptionKey = "DESC_V3RunTimeDDCatalog"; // NOI18N
            javaEE6DDCatalog.hasAdditionalMap = true;
        }
        return javaEE6DDCatalog;
    }

    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    @Override
    public Iterator getPublicIDs() {
        if (platformRootDir == null) {
            return null;
        }
        if (!platformRootDir.exists()) {
            return null;
        }
        
        String installRoot = platformRootDir.getAbsolutePath(); 
        if (installRoot == null) {
            return null;
        }
        
        List<String> list = new ArrayList<>();
        for (int i=0;i<TypeToURLMap.length;i = i+2){
            list.add(TypeToURLMap[i]);
        }
        if (hasAdditionalMap) {
            for (int i=0;i<JavaEE6TypeToURLMap.length;i = i+2){
                list.add(JavaEE6TypeToURLMap[i]);
            }
        }
        for (int i=0;i<SchemaToURLMap.length;i = i+2){
            list.add(SchemaToURLMap[i]);
        }
        if (hasAdditionalMap) {
            for (int i=0;i<JavaEE6SchemaToURLMap.length;i = i+2){
                list.add(JavaEE6SchemaToURLMap[i]);
            }
        }
        
        return list.listIterator();
    }

    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    @Override
    public String getSystemID(String publicId) {
        if (platformRootDir == null) {
            return null;
        }
        if (!platformRootDir.exists()) {
            return null;
        }
        
        String  installRoot = platformRootDir.getAbsolutePath(); //System.getProperty("com.sun.aas.installRoot");
        if (installRoot == null) {
            return null;
        }
        String loc="dtds";
        for (int i=0;i<TypeToURLMap.length;i = i+2){
            if (TypeToURLMap[i].equals(publicId)){
                File file = new File(installRoot+"/lib/"+loc+"/"+TypeToURLMap[i+1]);
                try{
                    return file.toURI().toURL().toExternalForm();  
                }catch(Exception e){
                    Logger.getLogger("glassfish-javaee").log(Level.INFO, file.getAbsolutePath(), e); // NOI18N
                    return "";
                }
            }
        }
        if (hasAdditionalMap) {
            for (int i=0;i<JavaEE6TypeToURLMap.length;i = i+2){
                if (JavaEE6TypeToURLMap[i].equals(publicId)){
                    File file = new File(installRoot+"/lib/"+loc+"/"+JavaEE6TypeToURLMap[i+1]);
                    try{
                        return file.toURI().toURL().toExternalForm();
                    }catch(Exception e){
                        Logger.getLogger("glassfish-javaee").log(Level.INFO, file.getAbsolutePath(), e); // NOI18N
                        return "";
                    }
                }
            }
        }
        loc="schemas";
        for (int i=0;i<SchemaToURLMap.length;i = i+2){
            if (SchemaToURLMap[i].equals(publicId)){
                File file = new File(installRoot+"/lib/"+loc+"/"+SchemaToURLMap[i+1]);
                try{
                    return file.toURI().toURL().toExternalForm();
                }catch(Exception e){
                    Logger.getLogger("glassfish-javaee").log(Level.INFO, file.getAbsolutePath(), e); // NOI18N
                    return "";
                }
            }
        }
        if (hasAdditionalMap) {
            for (int i=0;i<JavaEE6SchemaToURLMap.length;i = i+2){
                if (JavaEE6SchemaToURLMap[i].equals(publicId)){

                    // xsds are in the server and NB can now use them for code completion
                    // old code required dtds and would have done something like this:
                    // return "nbres:/org/netbeans/modules/j2ee/sun/ide/resources/"+JavaEE6SchemaToURLMap[i+1]+".dtd";
                    // because before NB could use xsd for code completion, the module had a
                    // hacked copy of the dtd to deal with that
                    File file = new File(installRoot+"/lib/"+loc+"/"+JavaEE6SchemaToURLMap[i+1]+".xsd");
                    try{
                        return file.toURI().toURL().toExternalForm();
                    }catch(Exception e){
                        Logger.getLogger("glassfish-javaee").log(Level.INFO, file.getAbsolutePath(), e); // NOI18N
                        return "";
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Refresh content according to content of mounted catalog.
     */
    @Override
    public void refresh() {
        fireCatalogListeners();
    }

    /**
     * Refresh content according to content of mounted catalog.
     */
    public void refresh(File newLoc) {
        if (platformRootDir!=newLoc){
            platformRootDir = newLoc;
            refresh();
        }
    
    }
    
    private List<CatalogListener> catalogListeners = new ArrayList<>(1);
    
    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    @Override
    public void addCatalogListener(CatalogListener l) {
        if (null == l)
            return;
        if (catalogListeners.contains(l))
            return;
        catalogListeners.add(l);
    }
    
    /**
     * Optional operation couled with addCatalogListener.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    @Override
    public void removeCatalogListener(CatalogListener l) {
        if (null == l)
            return;
        catalogListeners.remove(l);
    }
    
    public  void fireCatalogListeners() {
        for (CatalogListener l : catalogListeners) {
            l.notifyInvalidate();
        }
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
        return NbBundle.getMessage(RunTimeDDCatalog.class, displayNameKey);
    }
    
    /**
     * Return visuaized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    @Override
    public String getIconResource(int type) {
        return "org/netbeans/modules/glassfish/javaee/resources/ServerInstanceIcon.png"; // NOI18N
    }
    
    /**
     * @return I18N short description
     */
    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(RunTimeDDCatalog.class, shortDescriptionKey);
    }
    
    /** Unregister the listener.  */
    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
    public static final String J2EE_NS = "http://java.sun.com/xml/ns/j2ee"; // NOI18N
    public static final String JAVAEE_NS = "http://java.sun.com/xml/ns/javaee"; // NOI18N
    public static final String NEW_JAVAEE_NS = "http://xmlns.jcp.org/xml/ns/javaee"; // NOI18N
    public static final String JAKARTAEE_NS = "https://jakarta.ee/xml/ns/jakartaee"; // NOI18N
    public static final String RI_CONFIG_NS = "http://java.sun.com/xml/ns/jax-rpc/ri/config"; // NOI18N

    public static final String IBM_J2EE_NS = "http://www.ibm.com/webservices/xsd"; // NOI18N
    private static final String XMLNS_ATTR="xmlns"; //NOI18N
    //  public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) throws org.xml.sax.SAXException, java.io.IOException {
    //      return null;
    //  }
    private static final String EJB_JAR_TAG="ejb-jar"; //NOI18N
    private static final String EJBJAR_2_1_XSD="ejb-jar_2_1.xsd"; // NOI18N
    private static final String EJBJAR_2_1 = J2EE_NS+"/"+EJBJAR_2_1_XSD; // NOI18N
    public static final String EJBJAR_2_1_ID = "SCHEMA:"+EJBJAR_2_1; // NOI18N
    
    private static final String EJBJAR_3_0_XSD="ejb-jar_3_0.xsd"; // NOI18N
    private static final String EJBJAR_3_0 = JAVAEE_NS+"/"+EJBJAR_3_0_XSD; // NOI18N
    public static final String EJBJAR_3_0_ID = "SCHEMA:"+EJBJAR_3_0; // NOI18N
    
    private static final String EJBJAR_3_1_XSD="ejb-jar_3_1.xsd"; // NOI18N
    private static final String EJBJAR_3_1 = JAVAEE_NS+"/"+EJBJAR_3_1_XSD; // NOI18N
    public static final String EJBJAR_3_1_ID = "SCHEMA:"+EJBJAR_3_1; // NOI18N
    
    private static final String EJBJAR_3_2_XSD="ejb-jar_3_2.xsd"; // NOI18N
    private static final String EJBJAR_3_2 = NEW_JAVAEE_NS+"/"+EJBJAR_3_2_XSD; // NOI18N
    public static final String EJBJAR_3_2_ID = "SCHEMA:"+EJBJAR_3_2; // NOI18N
    
    private static final String EJBJAR_4_0_XSD="ejb-jar_4_0.xsd"; // NOI18N
    private static final String EJBJAR_4_0 = JAKARTAEE_NS+"/"+EJBJAR_4_0_XSD; // NOI18N
    public static final String EJBJAR_4_0_ID = "SCHEMA:"+EJBJAR_4_0; // NOI18N
    
    private static final String APP_TAG="application"; //NOI18N
    private static final String APP_1_4_XSD="application_1_4.xsd"; // NOI18N
    private static final String APP_1_4= J2EE_NS+"/"+APP_1_4_XSD; // NOI18N
    public static final String APP_1_4_ID = "SCHEMA:"+APP_1_4; // NOI18N
 
    private static final String APP_5_XSD="application_5.xsd"; // NOI18N
    private static final String APP_5= JAVAEE_NS+"/"+APP_5_XSD; // NOI18N
    public static final String APP_5_ID = "SCHEMA:"+APP_5; // NOI18N
 
    private static final String APP_6_XSD="application_6.xsd"; // NOI18N
    private static final String APP_6= JAVAEE_NS+"/"+APP_6_XSD; // NOI18N
    public static final String APP_6_ID = "SCHEMA:"+APP_6; // NOI18N
    
    private static final String APP_7_XSD="application_7.xsd"; // NOI18N
    private static final String APP_7= NEW_JAVAEE_NS+"/"+APP_7_XSD; // NOI18N
    public static final String APP_7_ID = "SCHEMA:"+APP_7; // NOI18N
    
    private static final String APP_8_XSD="application_8.xsd"; // NOI18N
    private static final String APP_8= NEW_JAVAEE_NS+"/"+APP_8_XSD; // NOI18N
    public static final String APP_8_ID = "SCHEMA:"+APP_8; // NOI18N
    
    private static final String APP_9_XSD="application_9.xsd"; // NOI18N
    private static final String APP_9= JAKARTAEE_NS+"/"+APP_9_XSD; // NOI18N
    public static final String APP_9_ID = "SCHEMA:"+APP_9; // NOI18N
    
    private static final String APP_10_XSD="application_10.xsd"; // NOI18N
    private static final String APP_10= JAKARTAEE_NS+"/"+APP_10_XSD; // NOI18N
    public static final String APP_10_ID = "SCHEMA:"+APP_10; // NOI18N
    
    private static final String APP_11_XSD="application_11.xsd"; // NOI18N
    private static final String APP_11= JAKARTAEE_NS+"/"+APP_11_XSD; // NOI18N
    public static final String APP_11_ID = "SCHEMA:"+APP_11; // NOI18N
    
    private static final String APPCLIENT_TAG="application-client"; //NOI18N
    private static final String APPCLIENT_1_4_XSD="application-client_1_4.xsd"; // NOI18N
    private static final String APPCLIENT_1_4= J2EE_NS+"/"+APPCLIENT_1_4_XSD; // NOI18N
    public static final String APPCLIENT_1_4_ID = "SCHEMA:"+APPCLIENT_1_4; // NOI18N
 
    private static final String APPCLIENT_5_XSD="application-client_5.xsd"; // NOI18N
    private static final String APPCLIENT_5= JAVAEE_NS+"/"+APPCLIENT_5_XSD; // NOI18N
    public static final String APPCLIENT_5_ID = "SCHEMA:"+APPCLIENT_5; // NOI18N
    
    private static final String APPCLIENT_6_XSD="application-client_6.xsd"; // NOI18N
    private static final String APPCLIENT_6= JAVAEE_NS+"/"+APPCLIENT_6_XSD; // NOI18N
    public static final String APPCLIENT_6_ID = "SCHEMA:"+APPCLIENT_6; // NOI18N
    
    private static final String APPCLIENT_7_XSD="application-client_7.xsd"; // NOI18N
    private static final String APPCLIENT_7= NEW_JAVAEE_NS+"/"+APPCLIENT_7_XSD; // NOI18N
    public static final String APPCLIENT_7_ID = "SCHEMA:"+APPCLIENT_7; // NOI18N
    
    private static final String APPCLIENT_8_XSD="application-client_8.xsd"; // NOI18N
    private static final String APPCLIENT_8= NEW_JAVAEE_NS+"/"+APPCLIENT_8_XSD; // NOI18N
    public static final String APPCLIENT_8_ID = "SCHEMA:"+APPCLIENT_8; // NOI18N
    
    private static final String APPCLIENT_9_XSD="application-client_9.xsd"; // NOI18N
    private static final String APPCLIENT_9= JAKARTAEE_NS+"/"+APPCLIENT_9_XSD; // NOI18N
    public static final String APPCLIENT_9_ID = "SCHEMA:"+APPCLIENT_9; // NOI18N

    private static final String APPCLIENT_10_XSD="application-client_10.xsd"; // NOI18N
    private static final String APPCLIENT_10= JAKARTAEE_NS+"/"+APPCLIENT_10_XSD; // NOI18N
    public static final String APPCLIENT_10_ID = "SCHEMA:"+APPCLIENT_10; // NOI18N
    
    private static final String APPCLIENT_11_XSD="application-client_11.xsd"; // NOI18N
    private static final String APPCLIENT_11= JAKARTAEE_NS+"/"+APPCLIENT_11_XSD; // NOI18N
    public static final String APPCLIENT_11_ID = "SCHEMA:"+APPCLIENT_11; // NOI18N

    private static final String WEBSERVICES_TAG="webservices"; //NOI18N
    private static final String WEBSERVICES_1_1_XSD="j2ee_web_services_1_1.xsd"; // NOI18N
    private static final String WEBSERVICES_1_1= IBM_J2EE_NS+"/"+WEBSERVICES_1_1_XSD; // NOI18N
    public static final String WEBSERVICES_1_1_ID = "SCHEMA:"+WEBSERVICES_1_1; // NOI18N

    private static final String WEBSERVICES_CLIENT_1_1_XSD="j2ee_web_services_client_1_1.xsd"; // NOI18N
    private static final String WEBSERVICES_CLIENT_1_1= J2EE_NS+"/"+WEBSERVICES_CLIENT_1_1_XSD; // NOI18N
    public static final String WEBSERVICES_CLIENT_1_1_ID = "SCHEMA:"+WEBSERVICES_CLIENT_1_1; // NOI18N

    private static final String WEBSERVICES_1_2_XSD="javaee_web_services_1_2.xsd"; // NOI18N
    private static final String WEBSERVICES_1_2= JAVAEE_NS+"/"+WEBSERVICES_1_2_XSD; // NOI18N
    public static final String WEBSERVICES_1_2_ID = "SCHEMA:"+WEBSERVICES_1_2; // NOI18N
    
    private static final String WEBSERVICES_1_3_XSD="javaee_web_services_1_3.xsd"; // NOI18N
    private static final String WEBSERVICES_1_3= JAVAEE_NS+"/"+WEBSERVICES_1_3_XSD; // NOI18N
    public static final String WEBSERVICES_1_3_ID = "SCHEMA:"+WEBSERVICES_1_3; // NOI18N
    
    private static final String WEBSERVICES_1_4_XSD="javaee_web_services_1_4.xsd"; // NOI18N
    private static final String WEBSERVICES_1_4= NEW_JAVAEE_NS+"/"+WEBSERVICES_1_4_XSD; // NOI18N
    public static final String WEBSERVICES_1_4_ID = "SCHEMA:"+WEBSERVICES_1_4; // NOI18N
    
    private static final String WEBSERVICES_2_0_XSD="jakartaee_web_services_2_0.xsd"; // NOI18N
    private static final String WEBSERVICES_2_0= JAKARTAEE_NS+"/"+WEBSERVICES_2_0_XSD; // NOI18N
    public static final String WEBSERVICES_2_0_ID = "SCHEMA:"+WEBSERVICES_2_0; // NOI18N

    private static final String WEBSERVICES_CLIENT_1_2_XSD="javaee_web_services_client_1_2.xsd"; // NOI18N
    private static final String WEBSERVICES_CLIENT_1_2= JAVAEE_NS+"/"+WEBSERVICES_CLIENT_1_2_XSD; // NOI18N
    public static final String WEBSERVICES_CLIENT_1_2_ID = "SCHEMA:"+WEBSERVICES_CLIENT_1_2; // NOI18N
    
    private static final String WEBSERVICES_CLIENT_1_3_XSD="javaee_web_services_client_1_3.xsd"; // NOI18N
    private static final String WEBSERVICES_CLIENT_1_3= JAVAEE_NS+"/"+WEBSERVICES_CLIENT_1_3_XSD; // NOI18N
    public static final String WEBSERVICES_CLIENT_1_3_ID = "SCHEMA:"+WEBSERVICES_CLIENT_1_3; // NOI18N
    
    private static final String WEBSERVICES_CLIENT_1_4_XSD="javaee_web_services_client_1_4.xsd"; // NOI18N
    private static final String WEBSERVICES_CLIENT_1_4= NEW_JAVAEE_NS+"/"+WEBSERVICES_CLIENT_1_4_XSD; // NOI18N
    public static final String WEBSERVICES_CLIENT_1_4_ID = "SCHEMA:"+WEBSERVICES_CLIENT_1_4; // NOI18N
    
    private static final String WEBSERVICES_CLIENT_2_0_XSD="jakartaee_web_services_client_2_0.xsd"; // NOI18N
    private static final String WEBSERVICES_CLIENT_2_0= JAKARTAEE_NS+"/"+WEBSERVICES_CLIENT_2_0_XSD; // NOI18N
    public static final String WEBSERVICES_CLIENT_2_0_ID = "SCHEMA:"+WEBSERVICES_CLIENT_2_0; // NOI18N

    private static final String WEBAPP_TAG="web-app"; //NOI18N
    private static final String WEBAPP_2_5_XSD="web-app_2_5.xsd"; // NOI18N
    private static final String WEBAPP_2_5 = JAVAEE_NS+"/"+WEBAPP_2_5_XSD; // NOI18N
    public static final String WEBAPP_2_5_ID = "SCHEMA:"+WEBAPP_2_5; // NOI18N

    private static final String WEBAPP_3_0_XSD="web-app_3_0.xsd"; // NOI18N
    private static final String WEBAPP_3_0 = JAVAEE_NS+"/"+WEBAPP_3_0_XSD; // NOI18N
    public static final String WEBAPP_3_0_ID = "SCHEMA:"+WEBAPP_3_0; // NOI18N
    
    private static final String WEBCOMMON_3_0_XSD="web-common_3_0.xsd"; // NOI18N
    private static final String WEBCOMMON_3_0 = JAVAEE_NS+"/"+WEBCOMMON_3_0_XSD; // NOI18N
    public static final String WEBCOMMON_3_0_ID = "SCHEMA:"+WEBCOMMON_3_0; // NOI18N
    
    private static final String WEBFRAGMENT_3_0_XSD="web-fragment_3_0.xsd"; // NOI18N
    private static final String WEBFRAGMENT_3_0 = JAVAEE_NS+"/"+WEBFRAGMENT_3_0_XSD; // NOI18N
    public static final String WEBFRAGMENT_3_0_ID = "SCHEMA:"+WEBFRAGMENT_3_0; // NOI18N
    
    private static final String WEBAPP_3_1_XSD="web-app_3_1.xsd"; // NOI18N
    private static final String WEBAPP_3_1 = NEW_JAVAEE_NS+"/"+WEBAPP_3_1_XSD; // NOI18N
    public static final String WEBAPP_3_1_ID = "SCHEMA:"+WEBAPP_3_1; // NOI18N
    
    private static final String WEBCOMMON_3_1_XSD="web-common_3_1.xsd"; // NOI18N
    private static final String WEBCOMMON_3_1 = NEW_JAVAEE_NS+"/"+WEBCOMMON_3_1_XSD; // NOI18N
    public static final String WEBCOMMON_3_1_ID = "SCHEMA:"+WEBCOMMON_3_1; // NOI18N
    
    private static final String WEBFRAGMENT_3_1_XSD="web-fragment_3_1.xsd"; // NOI18N
    private static final String WEBFRAGMENT_3_1 = NEW_JAVAEE_NS+"/"+WEBFRAGMENT_3_1_XSD; // NOI18N
    public static final String WEBFRAGMENT_3_1_ID = "SCHEMA:"+WEBFRAGMENT_3_1; // NOI18N
    
    private static final String WEBAPP_4_0_XSD="web-app_4_0.xsd"; // NOI18N
    private static final String WEBAPP_4_0 = NEW_JAVAEE_NS+"/"+WEBAPP_4_0_XSD; // NOI18N
    public static final String WEBAPP_4_0_ID = "SCHEMA:"+WEBAPP_4_0; // NOI18N
    
    private static final String WEBCOMMON_4_0_XSD="web-common_4_0.xsd"; // NOI18N
    private static final String WEBCOMMON_4_0 = NEW_JAVAEE_NS+"/"+WEBCOMMON_4_0_XSD; // NOI18N
    public static final String WEBCOMMON_4_0_ID = "SCHEMA:"+WEBCOMMON_4_0; // NOI18N
    
    private static final String WEBFRAGMENT_4_0_XSD="web-fragment_4_0.xsd"; // NOI18N
    private static final String WEBFRAGMENT_4_0 = NEW_JAVAEE_NS+"/"+WEBFRAGMENT_4_0_XSD; // NOI18N
    public static final String WEBFRAGMENT_4_0_ID = "SCHEMA:"+WEBFRAGMENT_4_0; // NOI18N
    
    private static final String WEBAPP_5_0_XSD="web-app_5_0.xsd"; // NOI18N
    private static final String WEBAPP_5_0 = JAKARTAEE_NS+"/"+WEBAPP_5_0_XSD; // NOI18N
    public static final String WEBAPP_5_0_ID = "SCHEMA:"+WEBAPP_5_0; // NOI18N
    
    private static final String WEBCOMMON_5_0_XSD="web-common_5_0.xsd"; // NOI18N
    private static final String WEBCOMMON_5_0 = JAKARTAEE_NS+"/"+WEBCOMMON_5_0_XSD; // NOI18N
    public static final String WEBCOMMON_5_0_ID = "SCHEMA:"+WEBCOMMON_5_0; // NOI18N
    
    private static final String WEBFRAGMENT_5_0_XSD="web-fragment_5_0.xsd"; // NOI18N
    private static final String WEBFRAGMENT_5_0 = JAKARTAEE_NS+"/"+WEBFRAGMENT_5_0_XSD; // NOI18N
    public static final String WEBFRAGMENT_5_0_ID = "SCHEMA:"+WEBFRAGMENT_5_0; // NOI18N

    private static final String WEBAPP_6_0_XSD="web-app_6_0.xsd"; // NOI18N
    private static final String WEBAPP_6_0 = JAKARTAEE_NS+"/"+WEBAPP_6_0_XSD; // NOI18N
    public static final String WEBAPP_6_0_ID = "SCHEMA:"+WEBAPP_6_0; // NOI18N
    
    private static final String WEBCOMMON_6_0_XSD="web-common_6_0.xsd"; // NOI18N
    private static final String WEBCOMMON_6_0 = JAKARTAEE_NS+"/"+WEBCOMMON_6_0_XSD; // NOI18N
    public static final String WEBCOMMON_6_0_ID = "SCHEMA:"+WEBCOMMON_6_0; // NOI18N
    
    private static final String WEBFRAGMENT_6_0_XSD="web-fragment_6_0.xsd"; // NOI18N
    private static final String WEBFRAGMENT_6_0 = JAKARTAEE_NS+"/"+WEBFRAGMENT_6_0_XSD; // NOI18N
    public static final String WEBFRAGMENT_6_0_ID = "SCHEMA:"+WEBFRAGMENT_6_0; // NOI18N
    
    private static final String WEBAPP_6_1_XSD="web-app_6_1.xsd"; // NOI18N
    private static final String WEBAPP_6_1 = JAKARTAEE_NS+"/"+WEBAPP_6_1_XSD; // NOI18N
    public static final String WEBAPP_6_1_ID = "SCHEMA:"+WEBAPP_6_1; // NOI18N
    
    private static final String WEBCOMMON_6_1_XSD="web-common_6_1.xsd"; // NOI18N
    private static final String WEBCOMMON_6_1 = JAKARTAEE_NS+"/"+WEBCOMMON_6_1_XSD; // NOI18N
    public static final String WEBCOMMON_6_1_ID = "SCHEMA:"+WEBCOMMON_6_1; // NOI18N
    
    private static final String WEBFRAGMENT_6_1_XSD="web-fragment_6_1.xsd"; // NOI18N
    private static final String WEBFRAGMENT_6_1 = JAKARTAEE_NS+"/"+WEBFRAGMENT_6_1_XSD; // NOI18N
    public static final String WEBFRAGMENT_6_1_ID = "SCHEMA:"+WEBFRAGMENT_6_1; // NOI18N

    public static final String PERSISTENCE_NS = "http://java.sun.com/xml/ns/persistence"; // NOI18N
    public static final String NEW_PERSISTENCE_NS = "http://xmlns.jcp.org/xml/ns/persistence"; // NOI18N
    public static final String JAKARTA_PERSISTENCE_NS = "https://jakarta.ee/xml/ns/persistence"; // NOI18N
    
    private static final String PERSISTENCE_TAG="persistence"; //NOI18N
    
    private static final String PERSISTENCE_XSD="persistence_1_0.xsd"; // NOI18N
    private static final String PERSISTENCE = PERSISTENCE_NS+"/"+PERSISTENCE_XSD; // NOI18N
    public static final String PERSISTENCE_ID = "SCHEMA:"+PERSISTENCE; // NOI18N    
    
    private static final String PERSISTENCE_2_0_XSD="persistence_2_0.xsd"; // NOI18N
    private static final String PERSISTENCE_2_0 = PERSISTENCE_NS+"/"+PERSISTENCE_2_0_XSD; // NOI18N
    public static final String PERSISTENCE_2_0_ID = "SCHEMA:"+PERSISTENCE_2_0; // NOI18N 
    
    private static final String PERSISTENCE_2_1_XSD="persistence_2_1.xsd"; // NOI18N
    private static final String PERSISTENCE_2_1 = NEW_PERSISTENCE_NS+"/"+PERSISTENCE_2_1_XSD; // NOI18N
    public static final String PERSISTENCE_2_1_ID = "SCHEMA:"+PERSISTENCE_2_1; // NOI18N 
    
    private static final String PERSISTENCE_2_2_XSD="persistence_2_2.xsd"; // NOI18N
    private static final String PERSISTENCE_2_2 = NEW_PERSISTENCE_NS+"/"+PERSISTENCE_2_2_XSD; // NOI18N
    public static final String PERSISTENCE_2_2_ID = "SCHEMA:"+PERSISTENCE_2_2; // NOI18N 
    
    private static final String PERSISTENCE_3_0_XSD="persistence_3_0.xsd"; // NOI18N
    private static final String PERSISTENCE_3_0 = JAKARTA_PERSISTENCE_NS+"/"+PERSISTENCE_3_0_XSD; // NOI18N
    public static final String PERSISTENCE_3_0_ID = "SCHEMA:"+PERSISTENCE_3_0; // NOI18N 
    
    private static final String PERSISTENCE_3_1_XSD="persistence_3_0.xsd"; // NOI18N
    private static final String PERSISTENCE_3_1 = JAKARTA_PERSISTENCE_NS+"/"+PERSISTENCE_3_1_XSD; // NOI18N
    public static final String PERSISTENCE_3_1_ID = "SCHEMA:"+PERSISTENCE_3_1; // NOI18N 
    
    private static final String PERSISTENCE_3_2_XSD="persistence_3_2.xsd"; // NOI18N
    private static final String PERSISTENCE_3_2 = JAKARTA_PERSISTENCE_NS+"/"+PERSISTENCE_3_2_XSD; // NOI18N
    public static final String PERSISTENCE_3_2_ID = "SCHEMA:"+PERSISTENCE_3_2; // NOI18N 
    
    public static final String PERSISTENCEORM_NS = "http://java.sun.com/xml/ns/persistence/orm"; // NOI18N
    public static final String NEW_PERSISTENCEORM_NS = "http://xmlns.jcp.org/xml/ns/persistence/orm"; // NOI18N
    public static final String JAKARTA_PERSISTENCEORM_NS = "https://jakarta.ee/xml/ns/persistence/orm"; // NOI18N
    
    private static final String PERSISTENCEORM_TAG="entity-mappings"; //NOI18N
    
    private static final String PERSISTENCEORM_XSD="orm_1_0.xsd"; // NOI18N
    private static final String PERSISTENCEORM = PERSISTENCE_NS+"/"+PERSISTENCEORM_XSD; // NOI18N  yes not ORM NS!!!
    public static final String PERSISTENCEORM_ID = "SCHEMA:"+PERSISTENCEORM; // NOI18N

    private static final String PERSISTENCEORM_2_0_XSD="orm_2_0.xsd"; // NOI18N
    private static final String PERSISTENCEORM_2_0 = PERSISTENCE_NS+"/"+PERSISTENCEORM_2_0_XSD; // NOI18N  yes not ORM NS!!!
    public static final String PERSISTENCEORM_2_0_ID = "SCHEMA:"+PERSISTENCEORM_2_0; // NOI18N
    
    private static final String PERSISTENCEORM_2_1_XSD="orm_2_1.xsd"; // NOI18N
    private static final String PERSISTENCEORM_2_1 = NEW_PERSISTENCEORM_NS+"/"+PERSISTENCEORM_2_1_XSD; // NOI18N  yes not ORM NS!!!
    public static final String PERSISTENCEORM_2_1_ID = "SCHEMA:"+PERSISTENCEORM_2_1; // NOI18N
    
    private static final String PERSISTENCEORM_2_2_XSD="orm_2_2.xsd"; // NOI18N
    private static final String PERSISTENCEORM_2_2 = NEW_PERSISTENCEORM_NS+"/"+PERSISTENCEORM_2_2_XSD; // NOI18N  yes not ORM NS!!!
    public static final String PERSISTENCEORM_2_2_ID = "SCHEMA:"+PERSISTENCEORM_2_2; // NOI18N
    
    private static final String PERSISTENCEORM_3_0_XSD="orm_3_0.xsd"; // NOI18N
    private static final String PERSISTENCEORM_3_0 = JAKARTA_PERSISTENCEORM_NS+"/"+PERSISTENCEORM_3_0_XSD; // NOI18N  yes not ORM NS!!!
    public static final String PERSISTENCEORM_3_0_ID = "SCHEMA:"+PERSISTENCEORM_3_0; // NOI18N
    
    private static final String PERSISTENCEORM_3_1_XSD="orm_3_1.xsd"; // NOI18N
    private static final String PERSISTENCEORM_3_1 = JAKARTA_PERSISTENCEORM_NS+"/"+PERSISTENCEORM_3_1_XSD; // NOI18N  yes not ORM NS!!!
    public static final String PERSISTENCEORM_3_1_ID = "SCHEMA:"+PERSISTENCEORM_3_1; // NOI18N
    
    private static final String PERSISTENCEORM_3_2_XSD="orm_3_2.xsd"; // NOI18N
    private static final String PERSISTENCEORM_3_2 = JAKARTA_PERSISTENCEORM_NS+"/"+PERSISTENCEORM_3_2_XSD; // NOI18N  yes not ORM NS!!!
    public static final String PERSISTENCEORM_3_2_ID = "SCHEMA:"+PERSISTENCEORM_3_2; // NOI18N
    
    public String getFullURLFromSystemId(String systemId){
        return null;
        
    }
    
    private static String SCHEMASLOCATION=null;
    /**
     * Resolves schema definition file for deployment descriptor (spec.2_4)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        
        if (SCHEMASLOCATION == null) {
            if (platformRootDir == null) {
                return null;
            }
            if (!platformRootDir.exists()) {
                return null;
            }
        
            String  installRoot = platformRootDir.getAbsolutePath(); //System.getProperty("com.sun.aas.installRoot");
            if (installRoot==null)
                return null;
            File f = new File(installRoot);
            if (f.exists()==false)
                return null;
            File file = new File(installRoot+"/lib/schemas/");
            SCHEMASLOCATION = "";
            try{
                SCHEMASLOCATION= file.toURI().toURL().toExternalForm();
            }catch(Exception e){
                Logger.getLogger("glassfish-javaee").log(Level.INFO, file.getAbsolutePath(), e); // NOI18N
            }   
        }
        
        if (systemId != null) {
            // ejb
            if ( systemId.endsWith(EJBJAR_2_1_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+EJBJAR_2_1_XSD);
            } else if ( systemId.endsWith(EJBJAR_3_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+EJBJAR_3_0_XSD);
            } else if ( systemId.endsWith(EJBJAR_3_1_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+EJBJAR_3_1_XSD);
            } else if ( systemId.endsWith(EJBJAR_3_2_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+EJBJAR_3_2_XSD);
            } else if ( systemId.endsWith(EJBJAR_4_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+EJBJAR_4_0_XSD);
            }
            // application & application-client
            else if ( systemId.endsWith(APP_1_4_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APP_1_4_XSD);
            } else if ( systemId.endsWith(APPCLIENT_1_4_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APPCLIENT_1_4_XSD);
            } else if ( systemId.endsWith(APP_5_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APP_5_XSD);
            } else if ( systemId.endsWith(APPCLIENT_5_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APPCLIENT_5_XSD);
            } else if ( systemId.endsWith(APP_6_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APP_6_XSD);
            } else if ( systemId.endsWith(APPCLIENT_6_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APPCLIENT_6_XSD);
            } else if ( systemId.endsWith(APP_7_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APP_7_XSD);
            } else if ( systemId.endsWith(APPCLIENT_7_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APPCLIENT_7_XSD);
            } else if ( systemId.endsWith(APP_8_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APP_8_XSD);
            } else if ( systemId.endsWith(APPCLIENT_8_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APPCLIENT_8_XSD);
            } else if ( systemId.endsWith(APP_9_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APP_9_XSD);
            } else if ( systemId.endsWith(APPCLIENT_9_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APPCLIENT_9_XSD);
            } else if ( systemId.endsWith(APP_10_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APP_10_XSD);
            } else if ( systemId.endsWith(APPCLIENT_10_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APPCLIENT_10_XSD);
            } else if ( systemId.endsWith(APP_11_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APP_11_XSD);
            } else if ( systemId.endsWith(APPCLIENT_11_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+APPCLIENT_11_XSD);
            }
            //web-app, web-common & web-fragment
            else if ( systemId.endsWith(WEBAPP_2_5_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBAPP_2_5_XSD);
            } else if ( systemId.endsWith(WEBAPP_3_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBAPP_3_0_XSD);
            } else if ( systemId.endsWith(WEBFRAGMENT_3_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBFRAGMENT_3_0_XSD);
            } else if ( systemId.endsWith(WEBCOMMON_3_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBCOMMON_3_0_XSD);
            } else if ( systemId.endsWith(WEBAPP_3_1_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBAPP_3_1_XSD);
            } else if ( systemId.endsWith(WEBFRAGMENT_3_1_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBFRAGMENT_3_1_XSD);
            } else if ( systemId.endsWith(WEBCOMMON_3_1_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBCOMMON_3_1_XSD);
            } else if ( systemId.endsWith(WEBAPP_4_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBAPP_4_0_XSD);
            } else if ( systemId.endsWith(WEBFRAGMENT_4_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBFRAGMENT_4_0_XSD);
            } else if ( systemId.endsWith(WEBCOMMON_4_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBCOMMON_4_0_XSD);
            } else if ( systemId.endsWith(WEBAPP_5_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBAPP_5_0_XSD);
            } else if ( systemId.endsWith(WEBFRAGMENT_5_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBFRAGMENT_5_0_XSD);
            } else if ( systemId.endsWith(WEBCOMMON_5_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBCOMMON_5_0_XSD);
            } else if ( systemId.endsWith(WEBAPP_6_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBAPP_6_0_XSD);
            } else if ( systemId.endsWith(WEBFRAGMENT_6_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBFRAGMENT_6_0_XSD);
            } else if ( systemId.endsWith(WEBCOMMON_6_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBCOMMON_6_0_XSD);
            } else if ( systemId.endsWith(WEBAPP_6_1_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBAPP_6_1_XSD);
            } else if ( systemId.endsWith(WEBFRAGMENT_6_1_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBFRAGMENT_6_1_XSD);
            } else if ( systemId.endsWith(WEBCOMMON_6_1_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBCOMMON_6_1_XSD);
            }
            //persistence & orm
            else if ( systemId.endsWith(PERSISTENCEORM_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCEORM_XSD);
            } else if ( systemId.endsWith(PERSISTENCE_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCE_XSD);
            } else if ( systemId.endsWith(PERSISTENCEORM_2_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCEORM_2_0_XSD);
            } else if ( systemId.endsWith(PERSISTENCE_2_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCE_2_0_XSD);
            } else if ( systemId.endsWith(PERSISTENCEORM_2_1_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCEORM_2_1_XSD);
            } else if ( systemId.endsWith(PERSISTENCE_2_1_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCE_2_1_XSD);
            } else if ( systemId.endsWith(PERSISTENCEORM_2_2_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCEORM_2_2_XSD);
            } else if ( systemId.endsWith(PERSISTENCE_2_2_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCE_2_2_XSD);
            } else if ( systemId.endsWith(PERSISTENCEORM_3_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCEORM_3_0_XSD);
            } else if ( systemId.endsWith(PERSISTENCEORM_3_1_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCEORM_3_1_XSD);
            } else if ( systemId.endsWith(PERSISTENCE_3_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCE_3_0_XSD);
            } else if ( systemId.endsWith(PERSISTENCEORM_3_2_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCEORM_3_2_XSD);
            } else if ( systemId.endsWith(PERSISTENCEORM_3_2_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCEORM_3_2_XSD);
            } else if ( systemId.endsWith(PERSISTENCE_3_2_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCE_3_2_XSD);
            }
            //webservice & webservice-client
            else if ( systemId.endsWith(WEBSERVICES_1_1_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_1_1_XSD);
            } else if ( systemId.endsWith(WEBSERVICES_1_2_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_1_2_XSD);
            } else if ( systemId.endsWith(WEBSERVICES_1_3_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_1_3_XSD);
            } else if ( systemId.endsWith(WEBSERVICES_1_4_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_1_4_XSD);
            } else if ( systemId.endsWith(WEBSERVICES_2_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_2_0_XSD);
            } else if ( systemId.endsWith(WEBSERVICES_CLIENT_1_1_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_CLIENT_1_1_XSD);
            } else if ( systemId.endsWith(WEBSERVICES_CLIENT_1_2_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_CLIENT_1_2_XSD);
            } else if ( systemId.endsWith(WEBSERVICES_CLIENT_1_3_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_CLIENT_1_3_XSD);
            } else if ( systemId.endsWith(WEBSERVICES_CLIENT_1_4_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_CLIENT_1_4_XSD);
            } else if ( systemId.endsWith(WEBSERVICES_CLIENT_2_0_XSD)) {
                return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_CLIENT_2_0_XSD);
            }
            // weblogic
            else if ( systemId.endsWith("weblogic-web-app.xsd")) { //NOI18N
                return new org.xml.sax.InputSource(SCHEMASLOCATION+"weblogic-web-app.xsd");  //NOI18N
            } else if ( systemId.endsWith("weblogic-ejb-jar.xsd")) {  //NOI18N
                return new org.xml.sax.InputSource(SCHEMASLOCATION+"weblogic-ejb-jar.xsd");  //NOI18N
            } else if ( systemId.endsWith("weblogic-application.xsd")) {  //NOI18N
                return new org.xml.sax.InputSource(SCHEMASLOCATION+"weblogic-application.xsd");  //NOI18N
            } else if ( systemId.endsWith("weblogic-application-client.xsd")) {  //NOI18N
                return new org.xml.sax.InputSource(SCHEMASLOCATION+"weblogic-application-client.xsd");  //NOI18N
            } else if ( systemId.endsWith("weblogic-connector.xsd")) {  //NOI18N
                return new org.xml.sax.InputSource(SCHEMASLOCATION+"weblogic-connector.xsd"); //NOI18N
            } else if ( systemId.endsWith("weblogic-javaee.xsd")) { //NOI18N
                return new org.xml.sax.InputSource(SCHEMASLOCATION+"weblogic-javaee.xsd"); //NOI18N
            } else if ( systemId.endsWith("weblogic-jms.xsd")) { //NOI18N
                return new org.xml.sax.InputSource(SCHEMASLOCATION+"weblogic-jms.xsd"); //NOI18N
            } else if ( systemId.endsWith("weblogic-webservices.xsd")) { //NOI18N
                return new org.xml.sax.InputSource(SCHEMASLOCATION+"weblogic-webservices.xsd"); //NOI18N
            } else if ( systemId.endsWith("jdbc-data-source.xsd")) { //NOI18N
                return new org.xml.sax.InputSource(SCHEMASLOCATION+"jdbc-data-source.xsd"); //NOI18N
            } else if (XML_XSD.equals(systemId)) {
                return new org.xml.sax.InputSource(new java.io.StringReader(XML_XSD_DEF));
            }
        }
        return null;
    }
    
    @Override
    public Enumeration enabled(GrammarEnvironment ctx) {
        if (ctx.getFileObject() == null) return null;
        Enumeration<Node> en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = en.nextElement();
            if (next.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
                return null; // null for web.xml specified by DTD
            } else if (next.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) next;
                String tag = element.getTagName();
                String xmlns = element.getAttribute(XMLNS_ATTR);
                if ( xmlns != null && ( EJB_JAR_TAG.equals(tag) || APP_TAG.equals(tag) 
                        || WEBAPP_TAG.equals(tag) || APPCLIENT_TAG.equals(tag) 
                        || PERSISTENCEORM_TAG.equals(tag) || PERSISTENCE_TAG.equals(tag) 
                        || WEBSERVICES_TAG.equals(tag) ) ) {  // NOI18N
                    
                    if ( J2EE_NS.equals(xmlns) 
                            || JAVAEE_NS.equals(xmlns) 
                            || NEW_JAVAEE_NS.equals(xmlns) 
                            || JAKARTAEE_NS.equals(xmlns) ) {  // NOI18N
                        Vector<Node> v = new Vector<Node>();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor();
    }
    
    /** Returns pseudo DTD for code completion
     */
    @Override
    public GrammarQuery getGrammar(GrammarEnvironment ctx) {
        UserCatalog catalog = UserCatalog.getDefault();
        ///System.out.println("bbb");
        InputSource is= ctx.getInputSource();
        //System.out.println(is.getPublicId());
        //System.out.println(is.getSystemId());
        //System.out.println(is);
        if (catalog != null) {
            
            EntityResolver resolver = catalog.getEntityResolver();
            if (resolver != null) {
                try {
                    
                    if (ctx.getFileObject() == null) {
                        return null;
                    }
                    InputSource inputSource = null;                    
                    
                    String mimeType = ctx.getFileObject().getMIMEType();
                    if (mimeType == null){
                        return null;
                    }
                    switch (mimeType) {
                        case "text/x-dd-ejbjar4.0":  // NOI18N
                            inputSource = resolver.resolveEntity(EJBJAR_4_0_ID, "");
                            break;
                        case "text/x-dd-ejbjar3.2":  // NOI18N
                            inputSource = resolver.resolveEntity(EJBJAR_3_2_ID, "");
                            break;
                        case "text/x-dd-ejbjar3.1":  // NOI18N
                            inputSource = resolver.resolveEntity(EJBJAR_3_1_ID, "");
                            break;
                        case "text/x-dd-ejbjar3.0":  // NOI18N
                            inputSource = resolver.resolveEntity(EJBJAR_3_0_ID, "");
                            break;
                        case "text/x-dd-ejbjar2.1":  // NOI18N
                            inputSource = resolver.resolveEntity(EJBJAR_2_1_ID, "");
                            break;
                        case "text/x-dd-application11.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APP_11_ID, "");
                            break;
                        case "text/x-dd-application10.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APP_10_ID, "");
                            break;
                        case "text/x-dd-application9.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APP_9_ID, "");
                            break;
                        case "text/x-dd-application8.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APP_8_ID, "");
                            break;
                        case "text/x-dd-application7.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APP_7_ID, "");
                            break;
                        case "text/x-dd-application6.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APP_6_ID, "");
                            break;
                        case "text/x-dd-application5.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APP_5_ID, "");
                            break;
                        case "text/x-dd-application1.4":  // NOI18N
                            inputSource = resolver.resolveEntity(APP_1_4_ID, "");
                            break;
                        case "text/x-dd-client11.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APPCLIENT_11_ID, "");
                            break;
                        case "text/x-dd-client10.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APPCLIENT_10_ID, "");
                            break;
                        case "text/x-dd-client9.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APPCLIENT_9_ID, "");
                            break;
                        case "text/x-dd-client8.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APPCLIENT_8_ID, "");
                            break;
                        case "text/x-dd-client7.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APPCLIENT_7_ID, "");
                            break;
                        case "text/x-dd-client6.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APPCLIENT_6_ID, "");
                            break;
                        case "text/x-dd-client5.0":  // NOI18N
                            inputSource = resolver.resolveEntity(APPCLIENT_5_ID, "");
                            break;
                        case "text/x-dd-client1.4":  // NOI18N
                            inputSource = resolver.resolveEntity(APPCLIENT_1_4_ID, "");
                            break;
                        case "text/x-dd-servlet6.1":  // NOI18N
                            inputSource = resolver.resolveEntity(WEBAPP_6_1_ID, "");
                            break;
                        case "text/x-dd-servlet6.0":  // NOI18N
                            inputSource = resolver.resolveEntity(WEBAPP_6_0_ID, "");
                            break;
                        case "text/x-dd-servlet5.0":  // NOI18N
                            inputSource = resolver.resolveEntity(WEBAPP_5_0_ID, "");
                            break;
                        case "text/x-dd-servlet4.0":  // NOI18N
                            inputSource = resolver.resolveEntity(WEBAPP_4_0_ID, "");
                            break;
                        case "text/x-dd-servlet3.1":  // NOI18N
                            inputSource = resolver.resolveEntity(WEBAPP_3_1_ID, "");
                            break;
                        case "text/x-dd-servlet3.0":  // NOI18N
                            inputSource = resolver.resolveEntity(WEBAPP_3_0_ID, "");
                            break;
                        case "text/x-dd-servlet2.5":  // NOI18N
                            inputSource = resolver.resolveEntity(WEBAPP_2_5_ID, "");
                            break;
                        case "text/x-dd-servlet-fragment6.1":  // NOI18N
                            inputSource = resolver.resolveEntity(WEBFRAGMENT_6_1_ID, "");
                            break;
                        case "text/x-dd-servlet-fragment6.0":  // NOI18N
                            inputSource = resolver.resolveEntity(WEBFRAGMENT_6_0_ID, "");
                            break;
                        case "text/x-dd-servlet-fragment5.0":  // NOI18N
                            inputSource = resolver.resolveEntity(WEBFRAGMENT_5_0_ID, "");
                            break;
                        case "text/x-dd-servlet-fragment4.0":  // NOI18N
                            inputSource = resolver.resolveEntity(WEBFRAGMENT_4_0_ID, "");
                            break;
                        case "text/x-dd-servlet-fragment3.1":  // NOI18N
                            inputSource = resolver.resolveEntity(WEBFRAGMENT_3_1_ID, "");
                            break;
                        case "text/x-dd-servlet-fragment3.0":  // NOI18N
                            inputSource = resolver.resolveEntity(WEBFRAGMENT_3_0_ID, "");
                            break;
                        case "text/x-persistence3.2":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCE_3_2_ID, "");
                            break;
                        case "text/x-persistence3.1":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCE_3_1_ID, "");
                            break;
                        case "text/x-persistence3.0":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCE_3_0_ID, "");
                            break;
                        case "text/x-persistence2.2":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCE_2_2_ID, "");
                            break;
                        case "text/x-persistence2.1":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCE_2_1_ID, "");
                            break;
                        case "text/x-persistence2.0":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCE_2_0_ID, "");
                            break;
                        case "text/x-persistence1.0":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCE_ID, "");
                            break;
                        case "text/x-orm3.2":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCEORM_3_2_ID, "");
                            break;
                        case "text/x-orm3.1":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCEORM_3_1_ID, "");
                            break;
                        case "text/x-orm3.0":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCEORM_3_0_ID, "");
                            break;
                        case "text/x-orm2.2":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCEORM_2_2_ID, "");
                            break;
                        case "text/x-orm2.1":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCEORM_2_1_ID, "");
                            break;
                        case "text/x-orm2.0":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCEORM_2_0_ID, "");
                            break;
                        case "text/x-orm1.0":  // NOI18N
                            inputSource = resolver.resolveEntity(PERSISTENCEORM_ID, "");
                            break;
                        default:
                            break;
                    }

                    if (inputSource != null) {
                        return DTDUtil.parseDTD(true, inputSource);
                    }
                    
                    if (is.getSystemId().endsWith("webservices.xml") ) {  // NOI18N
                        // System.out.println("webservices tag");
                        inputSource = resolver.resolveEntity(WEBSERVICES_1_1_ID, "");
                        if (inputSource!=null) {
                            return DTDUtil.parseDTD(true, inputSource);
                        }
                    }
                    
                } catch(SAXException e) {
                } catch(java.io.IOException e) {
                    //System.out.println("eeee");
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    @Override
    public String resolveURI(String name) {
        // System.out.println("resolveURI(String name)="+name);
        if (platformRootDir == null) {
            return null;
        }
        if (!platformRootDir.exists()) {
            return null;
        }
        String  installRoot = platformRootDir.getAbsolutePath(); 
        String prefix ="";
        File file = new File(installRoot+"/lib/schemas/");
        try{
            prefix= file.toURI().toURL().toExternalForm();
        }catch(Exception e){
            Logger.getLogger("glassfish-javaee").log(Level.INFO, file.getAbsolutePath(), e); // NOI18N
        }
        if (name.equals("http://java.sun.com/xml/ns/jax-rpc/ri/config")){
            return prefix +"jax-rpc-ri-config.xsd";
        }
//        if (name.equals("http://java.sun.com/xml/ns/persistence")){
//            System.out.println("prefix +persistence.xsd="+ prefix +"persistence.xsd");
//            return prefix +"persistence.xsd";
//        }        
        // ludo: this is meant to be this way.
        if (name.equals("http://java.sun.com/xml/ns/j2eeppppppp")){
            return prefix +"j2ee_web_services_1_1.xsd";
        }
        
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
