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

package org.netbeans.modules.payara.jakartaee;

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


/** Catalog for Payara DTDs that enables completion support in editor.
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
        "-//Sun Microsystems Inc.//DTD Payara Communications Server 1.5 Domain//EN"      ,"sun-domain_1_4.dtd",
        "-//Sun Microsystems Inc.//DTD Payara Communications Server 2.0 Domain//EN"      ,"sun-domain_1_5.dtd",
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
        "-//GlassFish.org//DTD GlassFish Application Server 3.1 Resource Definitions//EN", "glassfish-resources_1_5.dtd",
        "-//Payara.fish//DTD Payara Server 4 Servlet 3.0//EN", "payara-web-app_4.dtd"
    };

        /*******NetBeans 3.6 is NOT ready yet to support schemas for code completion... What a pity!:        */
    private static final String SchemaToURLMap[] = {
        
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd"                    , "ejb-jar_2_1",
        
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/application-client_1_4.xsd"         , "application-client_1_4",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/application_1_4.xsd"                , "application_1_4",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/jax-rpc-ri-config.xsd"              , "jax-rpc-ri-config",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/connector_1_5.xsd"                  , "connector_1_5",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/jsp_2_0.xsd"                        , "jsp_2_0.xsd",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/datatypes.dtd"                      , "datatypes",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"                    , "web-app_2_4",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/web-jsptaglibrary_2_0.xsd"          , "web-jsptaglibrary_2_0",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/j2ee_1_4.xsd"                       , "j2ee_1_4",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/j2ee_jaxrpc_mapping_1_1.xsd"        , "j2ee_jaxrpc_mapping_1_1",
        "SCHEMA:http://www.ibm.com/webservices/xsd/j2ee_web_services_1_1.xsd"             ,"j2ee_web_services_1_1",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/j2ee_web_services_client_1_1.xsd"          ,"j2ee_web_services_client_1_1",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/ejb-jar_3_0.xsd"                    , "ejb-jar_3_0",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/application-client_5.xsd"         , "application-client_5",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/application_5.xsd"         , "application_5",
        "SCHEMA:http://java.sun.com/xml/ns/persistence/orm_1_0.xsd"         , "orm_1_0",
        "SCHEMA:http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd"         , "persistence_1_0",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/javaee_web_services_1_2.xsd"          ,"javaee_web_services_1_2",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/javaee_web_services_client_1_2.xsd"          ,"javaee_web_services_client_1_2",

    };
    
    private static final String JavaEE6SchemaToURLMap[] = {

        "SCHEMA:http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd"                    , "ejb-jar_3_1",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/jsp_2_2.xsd"                        , "jsp_2_2",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"                    , "web-app_3_0",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/web-common_3_0.xsd"                    , "web-common_3_0",
        "SCHEMA:http://java.sun.com/xml/ns/javaee/web-fragment_3_0.xsd"                    , "web-fragment_3_0",
        "SCHEMA:http://xmlns.oracle.com/weblogic/jdbc-data-source/1.0/jdbc-data-source.xsd", "jdbc-data-source",
    };

    private static Map<ServerInstanceProvider, RunTimeDDCatalog> ddCatalogMap = new HashMap<ServerInstanceProvider, RunTimeDDCatalog>();
//    private static RunTimeDDCatalog preludeDDCatalog;
    private static RunTimeDDCatalog defaultDDCatalog;

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
    public static synchronized RunTimeDDCatalog getDefaultRunTimeDDCatalog(){
        if (defaultDDCatalog==null) {
            defaultDDCatalog = new RunTimeDDCatalog();
            defaultDDCatalog.displayNameKey = "LBL_RunTimeDDCatalog"; // NOI18N
            defaultDDCatalog.shortDescriptionKey = "DESC_RunTimeDDCatalog"; // NOI18N
            defaultDDCatalog.hasAdditionalMap = true;
        }
        return defaultDDCatalog;
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
        
        List<String> list = new ArrayList<String>();
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
                    Logger.getLogger("payara-jakartaee").log(Level.INFO, file.getAbsolutePath(), e); // NOI18N
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
                        Logger.getLogger("payara-jakartaee").log(Level.INFO, file.getAbsolutePath(), e); // NOI18N
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
                    Logger.getLogger("payara-jakartaee").log(Level.INFO, file.getAbsolutePath(), e); // NOI18N
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
                        Logger.getLogger("payara-jakartaee").log(Level.INFO, file.getAbsolutePath(), e); // NOI18N
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
    
    private List<CatalogListener> catalogListeners = new ArrayList<CatalogListener>(1);
    
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
        if (catalogListeners.contains(l))
            catalogListeners.remove(l);
    }
    
    public  void fireCatalogListeners() {
        Iterator iter = catalogListeners.iterator();
        while (iter.hasNext()) {
            CatalogListener l = (CatalogListener) iter.next();
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
        return "org/netbeans/modules/payara/javaee/resources/server.png"; // NOI18N
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
    
    private static final String APP_TAG="application"; //NOI18N
    private static final String APP_1_4_XSD="application_1_4.xsd"; // NOI18N
    private static final String APP_1_4= J2EE_NS+"/"+APP_1_4_XSD; // NOI18N
    public static final String APP_1_4_ID = "SCHEMA:"+APP_1_4; // NOI18N
 
    private static final String APP_5_XSD="application_5.xsd"; // NOI18N
    private static final String APP_5= JAVAEE_NS+"/"+APP_5_XSD; // NOI18N
    public static final String APP_5_ID = "SCHEMA:"+APP_5; // NOI18N
 
    
    private static final String APPCLIENT_TAG="application-client"; //NOI18N
    private static final String APPCLIENT_1_4_XSD="application-client_1_4.xsd"; // NOI18N
    private static final String APPCLIENT_1_4= J2EE_NS+"/"+APPCLIENT_1_4_XSD; // NOI18N
    public static final String APPCLIENT_1_4_ID = "SCHEMA:"+APPCLIENT_1_4; // NOI18N
 
    private static final String APPCLIENT_5_XSD="application-client_5.xsd"; // NOI18N
    private static final String APPCLIENT_5= JAVAEE_NS+"/"+APPCLIENT_5_XSD; // NOI18N
    public static final String APPCLIENT_5_ID = "SCHEMA:"+APPCLIENT_5; // NOI18N
    
    
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

    private static final String WEBSERVICES_CLIENT_1_2_XSD="javaee_web_services_client_1_2.xsd"; // NOI18N
    private static final String WEBSERVICES_CLIENT_1_2= JAVAEE_NS+"/"+WEBSERVICES_CLIENT_1_2_XSD; // NOI18N
    public static final String WEBSERVICES_CLIENT_1_2_ID = "SCHEMA:"+WEBSERVICES_CLIENT_1_2; // NOI18N

    private static final String WEBAPP_TAG="web-app"; //NOI18N
    private static final String WEBAPP_2_5_XSD="web-app_2_5.xsd"; // NOI18N
    private static final String WEBAPP_2_5 = JAVAEE_NS+"/"+WEBAPP_2_5_XSD; // NOI18N
    public static final String WEBAPP_2_5_ID = "SCHEMA:"+WEBAPP_2_5; // NOI18N

    private static final String WEBAPP_3_0_XSD="web-app_3_0.xsd"; // NOI18N

    private static final String WEBFRAGMENT_3_0_XSD="web-fragment_3_0.xsd"; // NOI18N

    private static final String WEBCOMMON_3_0_XSD="web-common_3_0.xsd"; // NOI18N

    public static final String PERSISTENCE_NS = "http://java.sun.com/xml/ns/persistence"; // NOI18N
    private static final String PERSISTENCE_TAG="persistence"; //NOI18N
    private static final String PERSISTENCE_XSD="persistence_1_0.xsd"; // NOI18N
    private static final String PERSISTENCE = PERSISTENCE_NS+"/"+PERSISTENCE_XSD; // NOI18N
    public static final String PERSISTENCE_ID = "SCHEMA:"+PERSISTENCE; // NOI18N    
    
    public static final String PERSISTENCEORM_NS = "http://java.sun.com/xml/ns/persistence/orm"; // NOI18N
    private static final String PERSISTENCEORM_TAG="entity-mappings"; //NOI18N
    private static final String PERSISTENCEORM_XSD="orm_1_0.xsd"; // NOI18N
    private static final String PERSISTENCEORM = PERSISTENCE_NS+"/"+PERSISTENCEORM_XSD; // NOI18N  yes not ORM NS!!!
    public static final String PERSISTENCEORM_ID = "SCHEMA:"+PERSISTENCEORM; // NOI18N


    
    
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
                Logger.getLogger("payara-jakartaee").log(Level.INFO, file.getAbsolutePath(), e); // NOI18N
            }

            
        }
        if (systemId!=null && systemId.endsWith(EJBJAR_2_1_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+EJBJAR_2_1_XSD);
        }
        else  if (systemId!=null && systemId.endsWith(EJBJAR_3_0_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+EJBJAR_3_0_XSD);
        }            
        else if (systemId!=null && systemId.endsWith(APP_1_4_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+APP_1_4_XSD);
        }
        else if (systemId!=null && systemId.endsWith(APPCLIENT_1_4_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+APPCLIENT_1_4_XSD);
        }
        else if (systemId!=null && systemId.endsWith(WEBAPP_2_5_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBAPP_2_5_XSD);
        }
        else if (systemId!=null && systemId.endsWith(WEBAPP_3_0_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBAPP_3_0_XSD);
        }
        else if (systemId!=null && systemId.endsWith(WEBFRAGMENT_3_0_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBFRAGMENT_3_0_XSD);
        }
        else if (systemId!=null && systemId.endsWith(WEBCOMMON_3_0_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBCOMMON_3_0_XSD);
        }
        else if (systemId!=null && systemId.endsWith(APP_5_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+APP_5_XSD);
        }
        else if (systemId!=null && systemId.endsWith(APPCLIENT_5_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+APPCLIENT_5_XSD);
        }
        else if (systemId!=null && systemId.endsWith(PERSISTENCEORM_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCEORM_XSD);
        }
        else if (systemId!=null && systemId.endsWith(PERSISTENCE_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+PERSISTENCE_XSD);
        }
        else if (systemId!=null && systemId.endsWith(WEBSERVICES_1_1_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_1_1_XSD);
        }
        else if (systemId!=null && systemId.endsWith(WEBSERVICES_1_2_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_1_2_XSD);
        } else if (XML_XSD.equals(systemId)) {
            return new org.xml.sax.InputSource(new java.io.StringReader(XML_XSD_DEF));
        }
        else if (systemId!=null && systemId.endsWith(WEBSERVICES_CLIENT_1_1_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_CLIENT_1_1_XSD);
        }
        else if (systemId!=null && systemId.endsWith(WEBSERVICES_CLIENT_1_2_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_CLIENT_1_2_XSD);
        } else if (XML_XSD.equals(systemId)) {
            return new org.xml.sax.InputSource(new java.io.StringReader(XML_XSD_DEF));
        } else if (systemId != null && systemId.endsWith("jdbc-data-source.xsd")) { //NOI18N
            return new org.xml.sax.InputSource(SCHEMASLOCATION+"jdbc-data-source.xsd"); //NOI18N
        } else {
            return null;
        }
    }
    
    
    
    @Override
    public Enumeration enabled(GrammarEnvironment ctx) {
        if (ctx.getFileObject() == null) return null;
        Enumeration en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            if (next.getNodeType() == next.DOCUMENT_TYPE_NODE) {
                return null; // null for web.xml specified by DTD
            } else if (next.getNodeType() == next.ELEMENT_NODE) {
                Element element = (Element) next;
                String tag = element.getTagName();
                if (EJB_JAR_TAG.equals(tag)) {  // NOI18N
                    String xmlns = element.getAttribute(XMLNS_ATTR);
                    if (xmlns!=null && J2EE_NS.equals(xmlns)) {
                        Vector<Node> v = new Vector<Node>();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }
                    else  if (xmlns!=null && JAVAEE_NS.equals(xmlns)) {
                        Vector<Node> v = new Vector<Node>();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }
                }
                
                if (APP_TAG.equals(tag)) {  // NOI18N
                    String xmlns = element.getAttribute(XMLNS_ATTR);
                    if (xmlns!=null && J2EE_NS.equals(xmlns)) {
                        Vector<Node> v = new Vector<Node>();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }
                    else   if (xmlns!=null && JAVAEE_NS.equals(xmlns)) {
                        Vector<Node> v = new Vector<Node>();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }
                }
                if (WEBAPP_TAG.equals(tag)) {  // NOI18N
                    String xmlns = element.getAttribute(XMLNS_ATTR);
                    if (xmlns!=null && JAVAEE_NS.equals(xmlns)) {
                        Vector<Node> v = new Vector<Node>();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }

                }
                if (APPCLIENT_TAG.equals(tag)) {  // NOI18N
                    String xmlns = element.getAttribute(XMLNS_ATTR);
                    if (xmlns!=null && J2EE_NS.equals(xmlns)) {
                        Vector<Node> v = new Vector<Node>();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }
                    else   if (xmlns!=null && JAVAEE_NS.equals(xmlns)) {
                        Vector<Node> v = new Vector<Node>();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }
                }                
                if (PERSISTENCEORM_TAG.equals(tag)) {  // NOI18N
                    String xmlns = element.getAttribute(XMLNS_ATTR);
                    if (xmlns!=null && PERSISTENCEORM_NS.equals(xmlns)) {
                        Vector<Node> v = new Vector<Node>();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }

                }
                
                if (PERSISTENCE_TAG.equals(tag)) {  // NOI18N
                    String xmlns = element.getAttribute(XMLNS_ATTR);
                    if (xmlns!=null && PERSISTENCE_NS.equals(xmlns)) {
                        Vector<Node> v = new Vector<Node>();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }

                }
                
                if (WEBSERVICES_TAG.equals(tag)) {  // NOI18N
                    String xmlns = element.getAttribute(XMLNS_ATTR);
                    if (xmlns!=null && J2EE_NS.equals(xmlns)) {
                        Vector<Node> v = new Vector<Node>();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    } else   if (xmlns!=null && JAVAEE_NS.equals(xmlns)) {
                        Vector<Node> v = new Vector<Node>();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }
                    else   if (xmlns!=null && IBM_J2EE_NS.equals(xmlns)) {
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
                    if (mimeType==null){
                        return null;
                    }
                    if (mimeType.equals("text/x-dd-ejbjar3.0")){// NOI18N
                        inputSource = resolver.resolveEntity(EJBJAR_3_0_ID, "");
                    } else if (mimeType.equals("text/x-dd-ejbjar2.1")) {// NOI18N
                        inputSource = resolver.resolveEntity(EJBJAR_2_1_ID, "");
                    } else if (mimeType.equals("text/x-dd-application5.0")) {// NOI18N
                        inputSource = resolver.resolveEntity(APP_5_ID, "");
                    }else if (mimeType.equals("text/x-dd-application1.4")) {// NOI18N
                        inputSource = resolver.resolveEntity(APP_1_4_ID, "");
                    }else if (mimeType.equals("text/x-dd-client5.0")) {// NOI18N
                        inputSource = resolver.resolveEntity(APPCLIENT_5_ID, "");
                    }else if (mimeType.equals("text/x-dd-client1.4")) {// NOI18N
                        inputSource = resolver.resolveEntity(APPCLIENT_1_4_ID, "");
                    }else if (mimeType.equals("text/x-persistence1.0")) {// NOI18N
                        inputSource = resolver.resolveEntity(PERSISTENCE_ID, "");
                    }else if (mimeType.equals("text/x-orm1.0")) {// NOI18N
                        inputSource = resolver.resolveEntity(PERSISTENCEORM_ID, "");
                    }

                    if (inputSource!=null) {
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
            Logger.getLogger("payara-jakartaee").log(Level.INFO, file.getAbsolutePath(), e); // NOI18N
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
