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

package org.netbeans.modules.j2ee.sun.dd.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.impl.DTDRegistry;
import org.netbeans.modules.j2ee.sun.dd.impl.RootInterfaceImpl;
import org.netbeans.modules.j2ee.sun.dd.impl.app.SunApplicationProxy;
import org.netbeans.modules.j2ee.sun.dd.impl.client.SunApplicationClientProxy;
import org.netbeans.modules.j2ee.sun.dd.impl.cmp.SunCmpMappingsProxy;
import org.netbeans.modules.j2ee.sun.dd.impl.common.DDProviderDataObject;
import org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean;
import org.netbeans.modules.j2ee.sun.dd.impl.ejb.SunEjbJarProxy;
import org.netbeans.modules.j2ee.sun.dd.impl.serverresources.ResourcesProxy;
import org.netbeans.modules.j2ee.sun.dd.impl.web.SunWebAppProxy;
import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.schema2beans.GraphManager;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Provides access to Deployment Descriptor root objects.
 *
 * @author Peter Williams, Nitya Doraisamy
 */
public final class DDProvider {
   
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Connector 1.0//EN"               , "sun-connector_1_0-0.dtd" ,
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Application Client Container 1.0//EN" 	, "sun-application-client-container_1_0.dtd" ,
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 OR Mapping //EN"                 , "sun-cmp-mapping_1_0.dtd" ,
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Application Server 8.0 OR Mapping//EN"                          , "sun-cmp-mapping_1_1.dtd" ,
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Application Server 8.1 OR Mapping//EN"                          , "sun-cmp-mapping_1_2.dtd" ,
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Application Client Container //EN" 	, "sun-application-client-container_1_0.dtd" ,
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Application Server 8.1 Application Client Container //EN" 	, "sun-application-client-container_1_1.dtd" ,
    
    private static final DDProvider ddProvider = new DDProvider();
    
    private DDProvider() {
    }
    
    /**
     * Accessor method for DDProvider singleton
     * @return DDProvider object
     */
    public static DDProvider getDefault() {
        return ddProvider;
    }
    
     /**
      * Returns the root of deployment descriptor bean graph for java.io.File object.
      *
      * @param is source representing the sun-ejb-jar.xml file
      * @return Ejb object - root of the deployment descriptor bean graph
      */    
    public SunEjbJar getEjbDDRoot(InputSource is) throws IOException, SAXException {
        DDParse parse = new DDParse(is);
        SunEjbJar ejbRoot = createEjbJar(parse);
        SunEjbJarProxy proxy = new SunEjbJarProxy(ejbRoot, ejbRoot.getVersion().toString());
        setErrorStatus(proxy, parse);
        return proxy;
    }
    
    /**
      * Returns the root of deployment descriptor bean graph for java.io.File object.
      *
      * @param is source representing the sun-web.xml file
      * @return Web object - root of the deployment descriptor bean graph
      */    
    public SunWebApp getWebDDRoot(InputSource is) throws IOException, SAXException, DDException {
        return processWebAppParseTree(new DDParse(is));
    }

    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param is stream representing the sun-web.xml file
     * @return Web object - root of the deployment descriptor bean graph
     */    
    public SunWebApp getWebDDRoot(InputStream is) throws IOException, SAXException, DDException {
        return processWebAppParseTree(new DDParse(is));
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param doc XML document representing the sun-web.xml file
     * @return Web object - root of the deployment descriptor bean graph
     */    
    public SunWebApp getWebDDRoot(Document doc) throws DDException {
        return processWebAppParseTree(new DDParse(doc, null));
    }
    
    private SunWebApp processWebAppParseTree(DDParse parse) throws DDException {
        SunWebApp webRoot = createWebApp(parse);
        SunWebAppProxy proxy = new SunWebAppProxy(webRoot, webRoot.getVersion().toString());
        setErrorStatus(proxy, parse);
        return proxy;
    }    
    
    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param is source representing the sun-application.xml file
     * @return Application object - root of the deployment descriptor bean graph
     */    
    public SunApplication getAppDDRoot(InputSource is) throws IOException, SAXException {
        DDParse parse = new DDParse(is);
        SunApplication appRoot = createApplication(parse);
        SunApplicationProxy proxy = new SunApplicationProxy(appRoot, appRoot.getVersion().toString());
        setErrorStatus(proxy, parse);
        return proxy;
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param is source representing the sun-application-client.xml file
     * @return Application object - root of the deployment descriptor bean graph
     */    
    public SunApplicationClient getAppClientDDRoot(InputSource is) throws IOException, SAXException {
        DDParse parse = new DDParse(is);
        SunApplicationClient appClientRoot = createApplicationClient(parse);
        SunApplicationClientProxy proxy = new SunApplicationClientProxy(appClientRoot, appClientRoot.getVersion().toString());
        setErrorStatus(proxy, parse);
        return proxy;
    }
        
    private static void setErrorStatus(RootInterfaceImpl rootProxy, DDParse parse) {
        SAXParseException error = parse.getWarning();
        rootProxy.setError(error);
        if (error!=null) {
            rootProxy.setStatus(RootInterface.STATE_INVALID_PARSABLE);
        } else {
            rootProxy.setStatus(RootInterface.STATE_VALID);
        }
    }
    
    private final Map<Object, RootInterface> ddMap = new WeakHashMap<Object, RootInterface>();
    private Map<FileObject, DDProviderDataObject> dObjMap = new WeakHashMap<FileObject, DDProviderDataObject>();

    /** This method retrieves the root of the XML DOM for a sun-*
     *  deployment descriptor file.
     * 
     *  Caveat: Calling code should hold a strong reference to the FileObject
     *  used in this call for as long as the RootInterface is needed.  Otherwise,
     *  it is possible that the DataObject managing the XML graph will be GC'd
     *  If this happens, subsequent reads & writes to the graph will be invalid
     *  and/or throw unexpected exceptions and there is a risk of dataloss.
     * 
     * @param fo FileObject of sun descriptor file for desired graph
     * @return Root of XML DOM, if any, for this descriptor file.
     * @throws java.io.IOException if there is a problem reading the file.
     */
    public RootInterface getDDRoot(FileObject fo) throws IOException {
        if (fo == null) {
            return null;
        }

        try {
            DataObject dataObject = DataObject.find(fo);
            if(dataObject instanceof DDProviderDataObject){
                return getDDRoot0((DDProviderDataObject) dataObject, fo);
            }
        } catch (DataObjectNotFoundException e) {
            return null; // should not occur
        }

        RootInterface rootProxy = null;
        synchronized (ddMap) {
            rootProxy = ddMap.get(fo);
            if(rootProxy != null) {
                return rootProxy;
            }
        }

        // XXX Where should this listener be removed?  Isn't removed anywhere presently.
        fo.addFileChangeListener(new SunDDFileChangeListener());

        InputStream is = null;
        try {
            is = fo.getInputStream();
            DDParse parse = new DDParse(is);
            RootInterface tmpRootProxy = parse.createProxy();
            
            synchronized (ddMap) {
                rootProxy = ddMap.get(fo);
                if(rootProxy == null) {
                    rootProxy = tmpRootProxy;
                    ddMap.put(fo, rootProxy);
                }
            }   
        } catch(Schema2BeansException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch(Schema2BeansRuntimeException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch(SAXException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } finally {
            if(is != null) {
                try { is.close(); } catch(IOException ex) {}
            }
        }
        
        return rootProxy;
    }

    private RootInterface getDDRoot0(final DDProviderDataObject ddProviderDataObject, final FileObject refFO) throws IOException {
        RootInterface rootProxy = null;
        synchronized (ddMap) {
            rootProxy = ddMap.get(ddProviderDataObject);
            if (rootProxy == null) {
                try {
                    rootProxy = getDDRoot(ddProviderDataObject.createReader());
                    if(rootProxy != null) {
                        ddMap.put(ddProviderDataObject, rootProxy);
                    }
                } catch(Schema2BeansException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                } catch(Schema2BeansRuntimeException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                } catch(SAXException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                } catch(IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            
            if(rootProxy != null) {
                dObjMap.put(refFO, ddProviderDataObject);
            }
        }

        return rootProxy;
    }

    public RootInterface getDDRoot(Reader reader) throws IOException, SAXException, Schema2BeansException {
        return getDDRoot(new InputSource(reader));
    }
    
    public RootInterface getDDRoot(InputSource inputSource) throws IOException, SAXException, Schema2BeansException {
        return getDDRoot(inputSource, null);
    }

    private RootInterface getDDRoot(Reader reader, String defaultPublicId) 
            throws IOException, SAXException, Schema2BeansException {
        return getDDRoot(new InputSource(reader), defaultPublicId);
    }
    
    private RootInterface getDDRoot(InputSource inputSource, String defaultPublicId) throws IOException, SAXException, Schema2BeansException {
        // TODO j2ee providers can have a proxy here w/ bogus impl that stores the SAX
        // Exception.  Do we need to do that, or can we throw it direct to the caller?
        DDParse parse = new DDParse(inputSource, defaultPublicId);
        return parse.createProxy();
    }
    
    private static class VersionInfo {
        private Class implClass;
        private Class proxyClass;
        private String publicId;
        private String systemId;
        
        public VersionInfo(Class implClass, Class proxyClass, String publicId, String systemId) {
            this.implClass = implClass;
            this.proxyClass = proxyClass;
            this.publicId = publicId;
            this.systemId = systemId;
        }

        public Class getImplClass() {
            return implClass;
        }

        public Class getProxyClass() {
            return proxyClass;
        }

        public String getPublicId() {
            return publicId;
        }

        public String getSystemId() {
            return systemId;
        }
    }
    
    private static final Map<Class, Map<String, VersionInfo>> apiToVersionMap = new HashMap<Class, Map<String, VersionInfo>>(11);
    private static final Map<String, VersionInfo> sunWebAppVersionMap = new HashMap<String, VersionInfo>(11);
    private static final Map<String, VersionInfo> sunEjbJarVersionMap = new HashMap<String, VersionInfo>(11);
    private static final Map<String, VersionInfo> sunApplicationVersionMap = new HashMap<String, VersionInfo>(11);
    private static final Map<String, VersionInfo> sunAppClientVersionMap = new HashMap<String, VersionInfo>(11);
    private static final Map<String, VersionInfo> sunResourcesVersionMap = new HashMap<String, VersionInfo>(11);
    
    static {
        sunWebAppVersionMap.put(SunWebApp.VERSION_2_3_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp.class, SunWebAppProxy.class,
                DTDRegistry.SUN_WEBAPP_230_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_230_DTD_SYSTEM_ID
            ));
        sunWebAppVersionMap.put(SunWebApp.VERSION_2_4_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp.class, SunWebAppProxy.class,
                DTDRegistry.SUN_WEBAPP_240_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_240_DTD_SYSTEM_ID
            ));
        sunWebAppVersionMap.put(SunWebApp.VERSION_2_4_1, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp.class, SunWebAppProxy.class,
                DTDRegistry.SUN_WEBAPP_241_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_241_DTD_SYSTEM_ID
            ));
        sunWebAppVersionMap.put(SunWebApp.VERSION_2_5_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp.class, SunWebAppProxy.class,
                DTDRegistry.SUN_WEBAPP_250_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_250_DTD_SYSTEM_ID
            ));
        sunWebAppVersionMap.put(SunWebApp.VERSION_3_0_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_0.SunWebApp.class, SunWebAppProxy.class,
                DTDRegistry.SUN_WEBAPP_300_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_300_DTD_SYSTEM_ID
            ));
        sunWebAppVersionMap.put(SunWebApp.VERSION_3_0_1, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_1.GlassFishWebApp.class, SunWebAppProxy.class,
                DTDRegistry.GLASSFISH_WEBAPP_301_DTD_PUBLIC_ID, DTDRegistry.GLASSFISH_WEBAPP_301_DTD_SYSTEM_ID
            ));

        sunEjbJarVersionMap.put(SunEjbJar.VERSION_2_0_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_0_0.SunEjbJar.class, SunEjbJarProxy.class,
                DTDRegistry.SUN_EJBJAR_200_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_200_DTD_SYSTEM_ID
            ));
        sunEjbJarVersionMap.put(SunEjbJar.VERSION_2_1_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_0.SunEjbJar.class, SunEjbJarProxy.class,
                DTDRegistry.SUN_EJBJAR_210_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_210_DTD_SYSTEM_ID
            ));
        sunEjbJarVersionMap.put(SunEjbJar.VERSION_2_1_1, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_1.SunEjbJar.class, SunEjbJarProxy.class,
                DTDRegistry.SUN_EJBJAR_211_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_211_DTD_SYSTEM_ID
            ));
        sunEjbJarVersionMap.put(SunEjbJar.VERSION_3_0_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_0.SunEjbJar.class, SunEjbJarProxy.class,
                DTDRegistry.SUN_EJBJAR_300_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_300_DTD_SYSTEM_ID
            ));
        sunEjbJarVersionMap.put(SunEjbJar.VERSION_3_1_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_1_0.SunEjbJar.class, SunEjbJarProxy.class,
                DTDRegistry.SUN_EJBJAR_310_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_310_DTD_SYSTEM_ID
            ));
        sunEjbJarVersionMap.put(SunEjbJar.VERSION_3_1_1, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_1_1.GlassFishEjbJar.class, SunEjbJarProxy.class,
                DTDRegistry.GLASSFISH_EJBJAR_311_DTD_PUBLIC_ID, DTDRegistry.GLASSFISH_EJBJAR_311_DTD_SYSTEM_ID
            ));

        sunApplicationVersionMap.put(SunApplication.VERSION_1_3_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication.class, SunApplicationProxy.class,
                DTDRegistry.SUN_APPLICATION_130_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_130_DTD_SYSTEM_ID
            ));
        sunApplicationVersionMap.put(SunApplication.VERSION_1_4_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication.class, SunApplicationProxy.class,
                DTDRegistry.SUN_APPLICATION_140_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_140_DTD_SYSTEM_ID
            ));
        sunApplicationVersionMap.put(SunApplication.VERSION_5_0_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication.class, SunApplicationProxy.class,
                DTDRegistry.SUN_APPLICATION_50_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_50_DTD_SYSTEM_ID
            ));
        sunApplicationVersionMap.put(SunApplication.VERSION_6_0_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_0.SunApplication.class, SunApplicationProxy.class,
                DTDRegistry.SUN_APPLICATION_60_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_60_DTD_SYSTEM_ID
            ));
        sunApplicationVersionMap.put(SunApplication.VERSION_6_0_1, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_1.GlassFishApplication.class, SunApplicationProxy.class,
                DTDRegistry.GLASSFISH_APPLICATION_601_DTD_PUBLIC_ID, DTDRegistry.GLASSFISH_APPLICATION_601_DTD_SYSTEM_ID
            ));

        sunAppClientVersionMap.put(SunApplicationClient.VERSION_1_3_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_3_0.SunApplicationClient.class, SunApplicationClientProxy.class,
                DTDRegistry.SUN_APPCLIENT_130_DTD_PUBLIC_ID, DTDRegistry.SUN_APPCLIENT_130_DTD_SYSTEM_ID
            ));
        sunAppClientVersionMap.put(SunApplicationClient.VERSION_1_4_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_0.SunApplicationClient.class, SunApplicationClientProxy.class,
                DTDRegistry.SUN_APPCLIENT_140_DTD_PUBLIC_ID, DTDRegistry.SUN_APPCLIENT_140_DTD_SYSTEM_ID
            ));
        sunAppClientVersionMap.put(SunApplicationClient.VERSION_1_4_1, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_1.SunApplicationClient.class, SunApplicationClientProxy.class,
                DTDRegistry.SUN_APPCLIENT_141_DTD_PUBLIC_ID, DTDRegistry.SUN_APPCLIENT_141_DTD_SYSTEM_ID
            ));
        sunAppClientVersionMap.put(SunApplicationClient.VERSION_5_0_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_5_0_0.SunApplicationClient.class, SunApplicationClientProxy.class,
                DTDRegistry.SUN_APPCLIENT_50_DTD_PUBLIC_ID, DTDRegistry.SUN_APPCLIENT_50_DTD_SYSTEM_ID
            ));
        sunAppClientVersionMap.put(SunApplicationClient.VERSION_6_0_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_6_0_0.SunApplicationClient.class, SunApplicationClientProxy.class,
                DTDRegistry.SUN_APPCLIENT_60_DTD_PUBLIC_ID, DTDRegistry.SUN_APPCLIENT_60_DTD_SYSTEM_ID
            ));
        sunAppClientVersionMap.put(SunApplicationClient.VERSION_6_0_1, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_6_0_1.GlassFishApplicationClient.class, SunApplicationClientProxy.class,
                DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_PUBLIC_ID, DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_SYSTEM_ID
            ));
    
        sunResourcesVersionMap.put(Resources.VERSION_1_3, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources.class, ResourcesProxy.class,
                DTDRegistry.SUN_RESOURCE_13_DTD_PUBLIC_ID, DTDRegistry.SUN_RESOURCE_13_DTD_SYSTEM_ID
            ));
        
        sunResourcesVersionMap.put(Resources.VERSION_1_5, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources.class, ResourcesProxy.class,
                DTDRegistry.GLASSFISH_RESOURCE_15_DTD_PUBLIC_ID, DTDRegistry.GLASSFISH_RESOURCE_15_DTD_SYSTEM_ID
            ));

        apiToVersionMap.put(org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp.class, sunWebAppVersionMap);
        apiToVersionMap.put(org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar.class, sunEjbJarVersionMap);
        apiToVersionMap.put(org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication.class, sunApplicationVersionMap);
        apiToVersionMap.put(org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient.class, sunAppClientVersionMap);
        apiToVersionMap.put(org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources.class, sunResourcesVersionMap);
    }
    
    @SuppressWarnings("unchecked")
    public RootInterface newGraph(Class rootType, String version) {
        RootInterface result = null;
        SunBaseBean graphRoot = null;
        Class graphRootClass = null;
        
        Map<String, VersionInfo> versionMap = apiToVersionMap.get(rootType);
        if(versionMap != null) {
            VersionInfo vInfo = versionMap.get(version);
            if(vInfo != null) {
                try {
                    // Formerly invoked static 'createGraph()' method, but that is merely a wrapper 
                    // for the default constructor so we'll call it directly.
                    graphRoot = (SunBaseBean) vInfo.getImplClass().getDeclaredConstructor().newInstance();
                    graphRoot.graphManager().setDoctype(vInfo.getPublicId(), vInfo.getSystemId());
                    
                    Class proxyClass = vInfo.getProxyClass();
                    Constructor proxyConstructor = proxyClass.getConstructor(new Class [] { rootType, String.class });
                    result = (RootInterface) proxyConstructor.newInstance(new Object [] { graphRoot, version });
                } catch(IllegalArgumentException ex) {
                    // These five exceptions will be caught and logged either in StorageBeanFactory static
                    // initializer or in Base.addToGraphs().  They all represent some type of coding error
                    // on our part and should not occur under normal conditions (unless there is a bug).
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                } catch(InvocationTargetException ex) {
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                } catch(InstantiationException ex) {
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                } catch(IllegalAccessException ex) {
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                } catch(NoSuchMethodException ex) {
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                }
            } else {
                throw new IllegalStateException("No version information for " + version + " of type " + rootType.getName());
            }
        } else {
            throw new IllegalStateException("No version map for " + rootType.getName());
        }
        
        return result;
    }
    
    private static SunEjbJar createEjbJar(DDParse parse) {        
          SunEjbJar jar = null;
          String version = parse.getVersion();
          if (SunEjbJar.VERSION_3_1_1.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_1_1.GlassFishEjbJar(parse.getDocument(), Common.NO_DEFAULT_VALUES);
          } else if (SunEjbJar.VERSION_3_1_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_1_0.SunEjbJar(parse.getDocument(), Common.NO_DEFAULT_VALUES);
          } else if (SunEjbJar.VERSION_3_0_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_0.SunEjbJar(parse.getDocument(), Common.NO_DEFAULT_VALUES); 
          } else if (SunEjbJar.VERSION_2_1_1.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_1.SunEjbJar(parse.getDocument(), Common.NO_DEFAULT_VALUES); 
          } else if (SunEjbJar.VERSION_2_1_0.equals(version)) {//ludo fix that!!!2.1.0 below
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_0.SunEjbJar(parse.getDocument(), Common.NO_DEFAULT_VALUES);
          } else if (SunEjbJar.VERSION_2_0_0.equals(version)) {//ludo fix that!!!2.1.0 below
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_0_0.SunEjbJar(parse.getDocument(), Common.NO_DEFAULT_VALUES);
          } //LUDO CHANGE LATER!!!
          else{
              //What should we do there? ludo throws somethig or try with 3.0.0? FIXTIT
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_0.SunEjbJar(parse.getDocument(), Common.NO_DEFAULT_VALUES);
          }
          
        //  return jar;
    }
    
    private static SunWebApp createWebApp(DDParse parse) throws DDException {
        SunWebApp webRoot = null;
        String version = parse.getVersion();
        if (SunWebApp.VERSION_3_0_1.equals(version)) {
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_1.GlassFishWebApp(parse.getDocument(), Common.NO_DEFAULT_VALUES);
        } else if (SunWebApp.VERSION_3_0_0.equals(version)) {
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_0.SunWebApp(parse.getDocument(), Common.NO_DEFAULT_VALUES);
        } else if (SunWebApp.VERSION_2_5_0.equals(version)) {
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp(parse.getDocument(), Common.NO_DEFAULT_VALUES); 
        } else if (SunWebApp.VERSION_2_4_1.equals(version)) {
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp(parse.getDocument(), Common.NO_DEFAULT_VALUES); 
        } else if (SunWebApp.VERSION_2_4_0.equals(version)){ //ludo fix that!!!2_4_0 below
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp(parse.getDocument(), Common.NO_DEFAULT_VALUES); 
        } else if (SunWebApp.VERSION_2_3_0.equals(version)){ 
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp(parse.getDocument(), Common.NO_DEFAULT_VALUES); 
        }else
            throw new DDException(
                    MessageFormat.format(ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/dd/api/Bundle").getString("MSG_UnknownWebXml"),new Object[]  {version} ));
        
    }
  
    private static SunApplication createApplication(DDParse parse) {        
          SunApplication app = null;
          String version = parse.getVersion();
          if (SunApplication.VERSION_6_0_1.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_1.GlassFishApplication(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (SunApplication.VERSION_6_0_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_0.SunApplication(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (SunApplication.VERSION_5_0_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (SunApplication.VERSION_1_4_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if(SunApplication.VERSION_1_3_0.equals(version)){
              return new org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          }
          
          return app;
    }
    
    private static SunApplicationClient createApplicationClient(DDParse parse) {        
          SunApplicationClient appClient = null;
          String version = parse.getVersion();
          if (SunApplicationClient.VERSION_6_0_1.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.client.model_6_0_1.GlassFishApplicationClient(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (SunApplicationClient.VERSION_6_0_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.client.model_6_0_0.SunApplicationClient(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (SunApplicationClient.VERSION_5_0_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.client.model_5_0_0.SunApplicationClient(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (SunApplicationClient.VERSION_1_4_1.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_1.SunApplicationClient(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (SunApplicationClient.VERSION_1_4_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_0.SunApplicationClient(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (SunApplicationClient.VERSION_1_3_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_3_0.SunApplicationClient(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          }
          
          return appClient;
    }
    
    /**
     * Returns the root of Resources bean graph for java.io.File object.
     *
     * @param doc XML document representing the .sun-resource file    
     */
    public Resources getResourcesGraph(String version) {
        Resources resourcesRoot =  (Resources)newGraph(Resources.class, version);
        ResourcesProxy proxy = new ResourcesProxy(resourcesRoot);
        return proxy;
    }
    
    /**
     * Returns the root of Resources bean graph for java.io.File object.
     *
     * @param doc XML document representing the .sun-resource file    
     */
    public Resources getResourcesGraph(InputStream in) throws IOException, SAXException {
        return getResourcesRoot(new InputSource(in));
    }

    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param is source representing the sun-application.xml file
     * @return Application object - root of the deployment descriptor bean graph
     */
    public Resources getResourcesRoot(InputSource is) throws IOException, SAXException {
        ResourcesProxy proxy = null;
        DDParse parse = new DDParse(is);
        Resources appRoot = createResources(parse);
        if (appRoot == null) {
            proxy = new ResourcesProxy(appRoot);
        } else {
            proxy = new ResourcesProxy(appRoot, appRoot.getVersion().toString());
        }
        setErrorStatus(proxy, parse);
        return proxy;
    }

    private static Resources createResources(DDParse parse) {
          Resources app = null;
          String version = parse.getVersion();
          if (Resources.VERSION_1_5.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model_1_5.Resources(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (Resources.VERSION_1_3.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          }

          return app;
    }
    
    private class SunDDFileChangeListener extends FileChangeAdapter {
        @Override
        public void fileChanged(FileEvent evt) {
            FileObject fo = evt.getFile();
            try {
                synchronized (ddMap) {
                    RootInterface rootProxy = ddMap.get(fo);
                    if(rootProxy != null) {
                        InputStream inputStream = null;
                        try {
                            inputStream = fo.getInputStream();
                            String encoding = EncodingUtil.detectEncoding(new BufferedInputStream(inputStream));
                            if (encoding == null) {
                                encoding = "UTF8";
                            }
                            merge(rootProxy, new InputStreamReader(inputStream, encoding));
//                            merge(rootProxy, fo);
                        } finally {
                            if(inputStream != null) {
                                try { inputStream.close(); } catch(IOException ex) {}
                            }
                        }
                    }
                }
            } catch(IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    private String getPublicIdFromImpl(RootInterfaceImpl rootProxyImpl) {
        String result = null;
        
        GraphManager gm = rootProxyImpl.graphManager();
        if(gm != null) {
            Document d = gm.getXmlDocument();
            if(d != null) {
                DocumentType dt = d.getDoctype();
                if(dt != null) {
                    result = dt.getPublicId();
                }
            }
        }
        
        return result;
    }
    
    // TODO this method will get refactored as I find new requirements...
    // TODO RootInterfaceImpl not being related to RootInterface makes this code
    // rather messy.  Maybe we can fix that somehow?
    public void merge(RootInterface rootProxy, Reader reader) {
        RootInterfaceImpl rootProxyImpl = (RootInterfaceImpl) rootProxy;
        try {
            String oldPublicId = getPublicIdFromImpl(rootProxyImpl);
            RootInterface newRootProxy = getDDRoot(reader, oldPublicId);
            RootInterfaceImpl newRootProxyImpl = (RootInterfaceImpl) newRootProxy;
            
            // If we can't parse, keep the old tree, but migrate the new parse state.
            if(newRootProxy == null) {
                rootProxyImpl.setStatus(RootInterface.STATE_INVALID_UNPARSABLE);
                rootProxyImpl.setError(null);
            } else if(newRootProxy.getStatus() == RootInterface.STATE_INVALID_UNPARSABLE) {
                rootProxyImpl.setStatus(RootInterface.STATE_INVALID_UNPARSABLE);
                rootProxyImpl.setError(newRootProxyImpl.getError());
            } else {
                // If doctype changed, just use the new tree.
                // TODO do we need a special event for this?
                
                // Otherwise, try to merge the new tree with the old one.
                rootProxy.merge(newRootProxy, RootInterface.MERGE_UPDATE);
                rootProxyImpl.setStatus(newRootProxy.getStatus());
                rootProxyImpl.setError(newRootProxyImpl.getError());
            }
        } catch(DOMException ex) {
            // Received when DOCTYPE is changing through illegal states.
            rootProxyImpl.setStatus(RootInterface.STATE_INVALID_UNPARSABLE);
            rootProxyImpl.setError(new SAXParseException(null, null, ex));
        } catch(SAXParseException ex) {
            rootProxyImpl.setStatus(RootInterface.STATE_INVALID_UNPARSABLE);
            rootProxyImpl.setError(ex);
        } catch(SAXException ex) {
            rootProxyImpl.setStatus(RootInterface.STATE_INVALID_UNPARSABLE);
            if(ex.getException() instanceof SAXParseException) {
                rootProxyImpl.setError((SAXParseException) ex.getException());
            } else {
                rootProxyImpl.setError(new SAXParseException(null, null, ex));
            }
        } catch(IOException ex) {
            rootProxyImpl.setStatus(RootInterface.STATE_INVALID_UNPARSABLE);
            // cbw if the state of the xml file transitions from parsable to
            // unparsable this could be due to a user change or cvs change.  
            // We would like to still receive events when the file is restored
            // to normal so lets not set the original to null here but wait
            // until the file becomes parsable again to do a merge.
            // rootProxyImpl.setOriginal(null);
        } catch(Schema2BeansException ex) {
            rootProxyImpl.setStatus(RootInterface.STATE_INVALID_UNPARSABLE);
            rootProxyImpl.setError(new SAXParseException(null, null, ex));
//        } catch(Schema2BeansRuntimeException ex){ // see #70286    
//            rootProxyImpl.setStatus(RootInterface.STATE_INVALID_UNPARSABLE);
//            rootProxyImpl.setError(new SAXParseException(null, null, ex));
        }
    }

    /**
     * @param rootDD Graph to obtain the version for.
     * @return ASDDVersion for the current graph or null if it cannot be determined.
     * @deprecated
     */
    @Deprecated
    public static ASDDVersion getASDDVersion(RootInterface rootDD) {
        return getASDDVersion(rootDD, null);
    }
    
    /**
     * @param rootDD Graph to obtain the version for.
     * @param default ASDDVersion to return in case true version is unavailable or unknown.
     * @return ASDDVersion for the current graph or null if it cannot be determined.
     */
    public static ASDDVersion getASDDVersion(RootInterface rootDD, ASDDVersion defaultVersion) {
        ASDDVersion version = null;
        
        if(rootDD instanceof RootInterfaceImpl) {
            RootInterfaceImpl rootDDImpl = (RootInterfaceImpl) rootDD;
            version = rootDDImpl.getASDDVersion();
        }
        
        return version != null ? version : defaultVersion;
    }
    
    private static class SunDDResolver implements EntityResolver {
        
        static volatile SunDDResolver resolver;
        
        static SunDDResolver getInstance() {
            if(resolver == null) {
                synchronized(SunDDResolver.class) {
                    if(resolver == null) {
                        resolver = new SunDDResolver();
                    }
                }
            }
            return resolver;
        }
        
        @Override
        public InputSource resolveEntity(String publicId, String systemId) {
            InputSource source = null;
            
            DocTypeInfo info = publicIdToInfoMap.get(publicId);
            if(info != null) {
                String resource = info.getResourceDtd();
                java.net.URL url = this.getClass().getResource(resource);
                source = new InputSource(url.toString());
            } 
            
            return source;
        }
    }
    
    private static class SunDDErrorHandler implements ErrorHandler {
        
        private int errorType = -1;
        SAXParseException error;

        @Override
        public void warning(SAXParseException sAXParseException) throws SAXException {
            if (errorType < 0) {
                errorType = 0;
                error = sAXParseException;
            }
            //throw sAXParseException;
        }
        
        @Override
        public void error(SAXParseException sAXParseException) throws SAXException {
            if (errorType < 1) {
                errorType = 1;
                error = sAXParseException;
            }
            //throw sAXParseException;
        }    
        
        @Override
        public void fatalError(SAXParseException sAXParseException) throws SAXException {
            errorType = 2;
            throw sAXParseException;
        }
        
        public int getErrorType() {
            return errorType;
        }
        
        public SAXParseException getError() {
            return error;
        }        
    }

    /** Parsed SJSAS deployment descriptor file including errors and/or version thereof.
     */
    private static class DDParse {
    
        private Document document;
        private SAXParseException saxException;
        private String version;
        private DocTypeInfo documentInfo;
        
        public DDParse(InputStream is) throws SAXException, IOException {
            this(new InputSource(is));
        }
        
        public DDParse(Reader reader) throws SAXException, IOException {
            this(new InputSource(reader));
        }
        
        public DDParse(InputSource is) throws SAXException, IOException {
            this(is, null);
        }
        
        public DDParse(InputSource is, String defaultPublicId) throws SAXException, IOException {
            try {
                SunDDErrorHandler errorHandler = new SunDDErrorHandler();
                DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder parser = parserFactory.newDocumentBuilder();
                parser.setErrorHandler(errorHandler);
                parser.setEntityResolver(SunDDResolver.getInstance());
                Document d = parser.parse(is);
                initialize(d, errorHandler.getError(), defaultPublicId);
            } catch(NullPointerException ex) {
                throw new SAXException(ex.getMessage());
            } catch(ParserConfigurationException ex) {
                throw new SAXException(ex.getMessage());
            }
        }
        
        public DDParse(Document d, SAXParseException saxEx) {
            initialize(d, saxEx, null);
        }
        
        private void initialize(Document d, SAXParseException saxEx, String defaultPublicId) {
            document = d;
            saxException = saxEx;
            documentInfo = null;
            // TODO Handle default version better.
            version = "unknown"; // NOI18N
            
            // first check the doc type to see if there is one
            DocumentType dt = document.getDoctype();
            if(dt != null) {
                documentInfo = publicIdToInfoMap.get(dt.getPublicId());
            } else if(defaultPublicId != null) {
                documentInfo = publicIdToInfoMap.get(defaultPublicId);
            }
            
            if(documentInfo != null) {
                version = documentInfo.getVersion();
            }
        }

        
        /**
         * @return document from last parse
         */
        public Document getDocument() {
            return document;
        }

        /**
         * @return proxy object for parsed bean tree.
         */
        public RootInterface createProxy() throws Schema2BeansException, Schema2BeansRuntimeException {
            RootInterface result = null;
            
            if(documentInfo != null) {
                try {
                    RootInterface implDD = documentInfo.createImplementation(document);
                    RootInterfaceImpl proxyDD = documentInfo.createProxy(implDD);
                    proxyDD.setError(saxException);
                    proxyDD.setStatus(saxException != null ? RootInterface.STATE_INVALID_PARSABLE : RootInterface.STATE_VALID);
                    result = proxyDD.getRootInterface();
                } catch (InstantiationException ex) {
                    // These five exceptions must be caught and logged by a higher caller.
                    // They all represent some type of coding error on our part and should not
                    // occur under normal conditions (unless there is a bug).
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                } catch (InvocationTargetException ex) {
                    // If the document is parsable (well-formed XML), but not valid
                    // then the constructor will throw a schema2beans exception because
                    // the document does not match the schema2beans graph.  This arrives
                    // here as an InvocationTargetException because of our use of reflection
                    // to construct the schema2beans graphs.
                    Throwable cause = ex;
                    while(cause.getCause() != null) {
                        cause = cause.getCause();
                    }
                    
                    if(cause instanceof Schema2BeansException) {
                        throw (Schema2BeansException) cause;
                    }

                    if(cause instanceof Schema2BeansRuntimeException) {
                        throw (Schema2BeansRuntimeException) cause;
                    }

                    throw new RuntimeException(ex.getMessage(), ex);
                } catch (NoSuchMethodException ex) {
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                }
            }
            
            return result;
        }
        
        /**
         * @return version string.
         */
        public String getVersion() {
            return version;
        }
        
        /** 
         * @return validation error encountered during the parse
         */
        public SAXParseException getWarning() {
            return saxException;
        }
    }
    
    private static class DocTypeInfo {
        
        private final String version;
        private final Class proxyClass;
        private final Class interfaceClass;
        private final Class implClass;
        private final String resourceDtd;
        
        public DocTypeInfo(final String version, final Class proxyClass, 
                final Class interfaceClass, final Class implClass, final String resourceDtd) {
            this.version = version;
            this.proxyClass = proxyClass;
            this.interfaceClass = interfaceClass;
            this.implClass = implClass;
            this.resourceDtd = resourceDtd;
        }

        public String getVersion() {
            return version;
        }

        public String getResourceDtd() {
            return resourceDtd;
        }
        
        public RootInterface createImplementation(Document doc) 
                throws InstantiationException, IllegalAccessException, IllegalArgumentException, 
                InvocationTargetException, NoSuchMethodException {
            Constructor implCtor = implClass.getConstructor(org.w3c.dom.Node.class, int.class);
            return (RootInterface) implCtor.newInstance(doc, Common.NO_DEFAULT_VALUES);
        }
        
        public RootInterfaceImpl createProxy(RootInterface original)
                throws InstantiationException, IllegalAccessException, IllegalArgumentException, 
                InvocationTargetException, NoSuchMethodException {
            Constructor proxyCtor = proxyClass.getConstructor(interfaceClass, String.class);
            return (RootInterfaceImpl) proxyCtor.newInstance(interfaceClass.cast(original), version);
        }
    }
    
    /* Maps DOCTYPE to { version, proxy class, impl class, dtd path } info.
     */
    private static final Map<String, DocTypeInfo> publicIdToInfoMap = new HashMap<String, DocTypeInfo>(37);
    
    static {
        publicIdToInfoMap.put(DTDRegistry.SUN_EJBJAR_211_DTD_PUBLIC_ID, new DocTypeInfo(
                SunEjbJar.VERSION_2_1_1, SunEjbJarProxy.class, SunEjbJar.class, 
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_1.SunEjbJar.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_2_1-1.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_EJBJAR_210_DTD_PUBLIC_ID, new DocTypeInfo(
                SunEjbJar.VERSION_2_1_0, SunEjbJarProxy.class, SunEjbJar.class,
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_0.SunEjbJar.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_2_1-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_EJBJAR_300_DTD_PUBLIC_ID, new DocTypeInfo(
                SunEjbJar.VERSION_3_0_0, SunEjbJarProxy.class, SunEjbJar.class,
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_0.SunEjbJar.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_3_0-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_EJBJAR_301_DTD_PUBLIC_ID, new DocTypeInfo(
                SunEjbJar.VERSION_3_0_1, SunEjbJarProxy.class, SunEjbJar.class,
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_1.SunEjbJar.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_3_0-1.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_EJBJAR_210beta_DTD_PUBLIC_ID, new DocTypeInfo(
                SunEjbJar.VERSION_2_1_0, SunEjbJarProxy.class, SunEjbJar.class,
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_0.SunEjbJar.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_2_1-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_EJBJAR_200_DTD_PUBLIC_ID, new DocTypeInfo(
                SunEjbJar.VERSION_2_0_0, SunEjbJarProxy.class, SunEjbJar.class,
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_0_0.SunEjbJar.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_2_0-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_EJBJAR_310_DTD_PUBLIC_ID, new DocTypeInfo(
                SunEjbJar.VERSION_3_1_0, SunEjbJarProxy.class, SunEjbJar.class,
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_1_0.SunEjbJar.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_3_1-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.GLASSFISH_EJBJAR_311_DTD_PUBLIC_ID, new DocTypeInfo(
                SunEjbJar.VERSION_3_1_1, SunEjbJarProxy.class, SunEjbJar.class,
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_1_1.GlassFishEjbJar.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/glassfish-ejb-jar_3_1-1.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.GLASSFISH_WEBAPP_301_DTD_PUBLIC_ID, new DocTypeInfo(
                SunWebApp.VERSION_3_0_1, SunWebAppProxy.class, SunWebApp.class,
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_1.GlassFishWebApp.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/glassfish-web-app_3_0-1.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_WEBAPP_300_DTD_PUBLIC_ID, new DocTypeInfo(
                SunWebApp.VERSION_3_0_0, SunWebAppProxy.class, SunWebApp.class,
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_3_0_0.SunWebApp.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_3_0-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_WEBAPP_250_DTD_PUBLIC_ID, new DocTypeInfo(
                SunWebApp.VERSION_2_5_0, SunWebAppProxy.class, SunWebApp.class,
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_5-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_WEBAPP_241_DTD_PUBLIC_ID, new DocTypeInfo(
                SunWebApp.VERSION_2_4_1, SunWebAppProxy.class, SunWebApp.class,
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_4-1.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_WEBAPP_240_DTD_PUBLIC_ID, new DocTypeInfo(
                SunWebApp.VERSION_2_4_0, SunWebAppProxy.class, SunWebApp.class,
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_4-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_WEBAPP_240beta_DTD_PUBLIC_ID, new DocTypeInfo(
                SunWebApp.VERSION_2_4_0, SunWebAppProxy.class, SunWebApp.class,
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_4-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_WEBAPP_230_DTD_PUBLIC_ID, new DocTypeInfo(
                SunWebApp.VERSION_2_3_0, SunWebAppProxy.class, SunWebApp.class,
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_3-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.GLASSFISH_APPLICATION_601_DTD_PUBLIC_ID, new DocTypeInfo(
                SunApplication.VERSION_6_0_1, SunApplicationProxy.class, SunApplication.class,
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_1.GlassFishApplication.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/glassfish-application_6_0-1.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_APPLICATION_60_DTD_PUBLIC_ID, new DocTypeInfo(
                SunApplication.VERSION_6_0_0, SunApplicationProxy.class, SunApplication.class,
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_6_0_0.SunApplication.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application_6_0-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_APPLICATION_50_DTD_PUBLIC_ID, new DocTypeInfo(
                SunApplication.VERSION_5_0_0, SunApplicationProxy.class, SunApplication.class,
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application_5_0-0.dtd")); // NOI18N
        
//        publicIdToInfoMap.put(DTDRegistry.SUN_APPLICATION_141_DTD_PUBLIC_ID, new DocTypeInfo(
//                SunApplication.VERSION_1_4_0, SunApplicationProxy.class, SunApplication.class,
//                org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication.class,
//                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application_1_4-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_APPLICATION_140_DTD_PUBLIC_ID, new DocTypeInfo(
                SunApplication.VERSION_1_4_0, SunApplicationProxy.class, SunApplication.class,
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application_1_4-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_APPLICATION_140beta_DTD_PUBLIC_ID, new DocTypeInfo(
                SunApplication.VERSION_1_4_0, SunApplicationProxy.class, SunApplication.class,
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application_1_4-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_APPLICATION_130_DTD_PUBLIC_ID, new DocTypeInfo(
                SunApplication.VERSION_1_3_0, SunApplicationProxy.class, SunApplication.class,
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application_1_3-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.GLASSFISH_APPCLIENT_601_DTD_PUBLIC_ID, new DocTypeInfo(
                SunApplicationClient.VERSION_6_0_1, SunApplicationClientProxy.class, SunApplicationClient.class,
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_6_0_1.GlassFishApplicationClient.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/glassfish-application-client_6_0-1.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_APPCLIENT_60_DTD_PUBLIC_ID, new DocTypeInfo(
                SunApplicationClient.VERSION_6_0_0, SunApplicationClientProxy.class, SunApplicationClient.class,
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_6_0_0.SunApplicationClient.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application-client_6_0-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_APPCLIENT_50_DTD_PUBLIC_ID, new DocTypeInfo(
                SunApplicationClient.VERSION_5_0_0, SunApplicationClientProxy.class, SunApplicationClient.class,
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_5_0_0.SunApplicationClient.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application-client_5_0-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_APPCLIENT_141_DTD_PUBLIC_ID, new DocTypeInfo(
                SunApplicationClient.VERSION_1_4_1, SunApplicationClientProxy.class, SunApplicationClient.class,
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_1.SunApplicationClient.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application-client_1_4-1.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_APPCLIENT_140_DTD_PUBLIC_ID, new DocTypeInfo(
                SunApplicationClient.VERSION_1_4_0, SunApplicationClientProxy.class, SunApplicationClient.class,
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_0.SunApplicationClient.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application-client_1_4-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_APPCLIENT_140beta_DTD_PUBLIC_ID, new DocTypeInfo(
                SunApplicationClient.VERSION_1_4_0, SunApplicationClientProxy.class, SunApplicationClient.class,
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_0.SunApplicationClient.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application-client_1_4-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_APPCLIENT_130_DTD_PUBLIC_ID, new DocTypeInfo(
                SunApplicationClient.VERSION_1_3_0, SunApplicationClientProxy.class, SunApplicationClient.class,
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_3_0.SunApplicationClient.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application-client_1_3-0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_CMP_MAPPING_810_DTD_PUBLIC_ID, new DocTypeInfo(
                SunCmpMappings.VERSION_1_2, SunCmpMappingsProxy.class, SunCmpMappings.class,
                org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_2.SunCmpMappings.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-cmp-mapping_1_2.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_CMP_MAPPING_800_DTD_PUBLIC_ID, new DocTypeInfo(
                SunCmpMappings.VERSION_1_1, SunCmpMappingsProxy.class, SunCmpMappings.class,
                org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_1.SunCmpMappings.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-cmp-mapping_1_1.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_CMP_MAPPING_700_DTD_PUBLIC_ID, new DocTypeInfo(
                SunCmpMappings.VERSION_1_0, SunCmpMappingsProxy.class, SunCmpMappings.class,
                org.netbeans.modules.j2ee.sun.dd.impl.cmp.model_1_0.SunCmpMappings.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-cmp-mapping_1_0.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.SUN_RESOURCE_13_DTD_PUBLIC_ID, new DocTypeInfo(
                Resources.VERSION_1_3, ResourcesProxy.class, Resources.class,
                org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-resources_1_3.dtd")); // NOI18N
        publicIdToInfoMap.put(DTDRegistry.GLASSFISH_RESOURCE_15_DTD_PUBLIC_ID, new DocTypeInfo(
                Resources.VERSION_1_5, ResourcesProxy.class, Resources.class,
                org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources.class,
                "/org/netbeans/modules/j2ee/sun/dd/impl/resources/glassfish-resources_1_5.dtd")); // NOI18N
        
    }
}
