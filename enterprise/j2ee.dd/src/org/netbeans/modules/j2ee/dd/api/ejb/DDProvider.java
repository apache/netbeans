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

package org.netbeans.modules.j2ee.dd.api.ejb;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.j2ee.dd.impl.ejb.EjbJarProxy;
import org.netbeans.modules.j2ee.dd.impl.common.DDProviderDataObject;
import org.netbeans.modules.j2ee.dd.impl.common.DDUtils;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import org.openide.util.Exceptions;

/**
 * Provides access to Deployment Descriptor root ({@link org.netbeans.modules.j2ee.dd.api.ejb.EjbJar} object)
 *
 * @author  Milan Kuchtiak
 */

public final class DDProvider {
    private static final String EJB_21_DOCTYPE = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd"; //NOI18N
    private static final String EJB_30_DOCTYPE = "http://java.sun.com/xml/ns/javaee/ejb-jar_3_0.xsd"; //NOI18N
    private static final String EJB_31_DOCTYPE = "http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd"; //NOI18N
    private static final String EJB_32_DOCTYPE = "http://xmlns.jcp.org/xml/ns/javaee/ejb-jar_3_2.xsd"; //NOI18N
    private static final String EJB_40_DOCTYPE = "https://jakarta.ee/xml/ns/jakartaee/ejb-jar_4_0.xsd"; //NOI18N
    private static final DDProvider ddProvider = new DDProvider();
    private final Map<Object, EjbJarProxy> ddMap;

    /** 
     * Creates a new instance of DDProvider.
     */
    private DDProvider() {
        ddMap = new HashMap<>(5);
    }

    /**
    * Accessor method for DDProvider singleton
    * @return DDProvider object
    */
    public static DDProvider getDefault() {
        return ddProvider;
    }

    // used in: ddapi, ddloaders, ejbfreeform
    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clints planning to read only the deployment descriptor
     * or to listen to the changes.
     * @param fo FileObject representing the ejb-jar.xml file
     * @return EjbJar object - root of the deployment descriptor bean graph
     * @throws IOException
     */
    public synchronized EjbJar getDDRoot(FileObject fo) throws java.io.IOException {
        if (fo == null) {
            return null;
        }
        try {
            DataObject dataObject = DataObject.find(fo);
            if(dataObject instanceof DDProviderDataObject){
                return getDDRoot0((DDProviderDataObject) dataObject);
            }
        } catch (DataObjectNotFoundException e) {
            return null; // should not occur
        }
        EjbJarProxy ejbJarProxy = null;
        synchronized (ddMap) {
            ejbJarProxy = getFromCache(fo);
            if (ejbJarProxy != null) {
                return ejbJarProxy;
            }
        }
        
        fo.addFileChangeListener(new DDFileChangeListener());

        ejbJarProxy = DDUtils.createEjbJarProxy(fo);
        synchronized(ddMap){
            EjbJarProxy cached = getFromCache(fo);
            if (cached != null){
                return cached;
            }
            putToCache(fo, ejbJarProxy);
        }
        
        return ejbJarProxy;
    }

    private synchronized EjbJar getDDRoot0(final DDProviderDataObject ddProviderDataObject) throws java.io.IOException {
        EjbJarProxy ejbJarProxy = null;
        synchronized (ddMap) {
            ejbJarProxy = getFromCache(ddProviderDataObject) ;
            if (ejbJarProxy == null) {
                ejbJarProxy = DDUtils.createEjbJarProxy(ddProviderDataObject.createReader());
                putToCache(ddProviderDataObject, ejbJarProxy);
            }
        }
        return ejbJarProxy;
    }

    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clients planning to modify the deployment descriptor.
     * Finally the {@link org.netbeans.modules.j2ee.dd.api.ejb.EjbJar#write(org.openide.filesystems.FileObject)} should be used
     * for writing the changes.
     * @param fo FileObject representing the ejb-jar.xml file
     * @return EjbJar object - root of the deployment descriptor bean graph
     * @throws IOException
     */
    public EjbJar getDDRootCopy(FileObject fo) throws java.io.IOException {
        return (EjbJar)getDDRoot(fo).clone();
    }

    private EjbJarProxy getFromCache (Object o) {
        return ddMap.get(o);
    }

    private void putToCache(Object o, EjbJarProxy ejbJarProxy) {
        ddMap.put(o, /*new WeakReference*/ (ejbJarProxy));
    }

    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param inputSource source representing the ejb-jar.xml file
     * @return EjbJar object - root of the deployment descriptor bean graph
     * @throws IOException
     * @throws SAXException
     */
    public EjbJar getDDRoot(InputSource inputSource) throws IOException, SAXException {
        ErrorHandler errorHandler = new ErrorHandler();
        DocumentBuilder parser = createParser(errorHandler);
        parser.setEntityResolver(DDResolver.getInstance());
        Document document = parser.parse(inputSource);
        SAXParseException error = errorHandler.getError();
        String version = extractVersion(document);
        EjbJar original = createEjbJar(version, document);
        EjbJarProxy ejbJarProxy = new EjbJarProxy(original, version);
        ejbJarProxy.setError(error);
        if (error != null) {
            ejbJarProxy.setStatus(EjbJar.STATE_INVALID_PARSABLE);
        } else {
            ejbJarProxy.setStatus(EjbJar.STATE_VALID);
        }
        return ejbJarProxy;
    }

    // PENDING j2eeserver needs BaseBean - this is a temporary workaround to avoid dependency of web project on DD impl
    /**  Convenient method for getting the BaseBean object from CommonDDBean object
     *
     * @param bean
     * @return 
     */
    public BaseBean getBaseBean(CommonDDBean bean) {
        if (bean instanceof BaseBean) {
            return (BaseBean) bean;
        } else if (bean instanceof EjbJarProxy) {
            return (BaseBean) ((EjbJarProxy) bean).getOriginal();
        }
        return null;
    }

    private static EjbJar createEjbJar(String version, Document document) {
        if (EjbJar.VERSION_4_0.equals(version)) {
            return new org.netbeans.modules.j2ee.dd.impl.ejb.model_4_0.EjbJar(document, Common.USE_DEFAULT_VALUES);
        } else if (EjbJar.VERSION_3_2.equals(version)) {
            return new org.netbeans.modules.j2ee.dd.impl.ejb.model_3_2.EjbJar(document, Common.USE_DEFAULT_VALUES);
        } else if (EjbJar.VERSION_3_1.equals(version)) {
            return new org.netbeans.modules.j2ee.dd.impl.ejb.model_3_1.EjbJar(document, Common.USE_DEFAULT_VALUES);
        } else if (EjbJar.VERSION_3_0.equals(version)) {
            return new org.netbeans.modules.j2ee.dd.impl.ejb.model_3_0.EjbJar(document, Common.USE_DEFAULT_VALUES);
        } else if (EjbJar.VERSION_2_1.equals(version)) {
            return new org.netbeans.modules.j2ee.dd.impl.ejb.model_2_1.EjbJar(document, Common.USE_DEFAULT_VALUES);
        } else {
            return null;
        }
    }

    /**
     * Extracts version of deployment descriptor.
     */
    private static String extractVersion(Document document) {
        // first check the doc type to see if there is one
        String id = null;
        DocumentType dt = document.getDoctype();
        if (dt != null) {
            // it is DTD-based
            id = dt.getPublicId();
        } else {
            // it is XSD-based
            String schemaLocation = document.getDocumentElement().getAttribute("xsi:schemaLocation");
            if (schemaLocation != null) {
                id = schemaLocation.substring(schemaLocation.lastIndexOf(" ") + 1);
            }
        }
        if (id != null) {
            if(EJB_40_DOCTYPE.equals(id)) {
                return EjbJar.VERSION_4_0;
            } else if(EJB_32_DOCTYPE.equals(id)) {
                return EjbJar.VERSION_3_2;
            } else if (EJB_31_DOCTYPE.equals(id)) {
                return EjbJar.VERSION_3_1;
            } else if (EJB_30_DOCTYPE.equals(id)) {
                return EjbJar.VERSION_3_0;
            } else if (EJB_21_DOCTYPE.equals(id)) {
                return EjbJar.VERSION_2_1;
            }
        }
        return EjbJar.VERSION_3_2;

    }

    private static DocumentBuilder createParser(ErrorHandler errorHandler)
            throws SAXException {
        DocumentBuilder parser=null;
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            parser = fact.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new SAXException(ex.getMessage());
        }
        parser.setErrorHandler(errorHandler);
        return parser;
    }
    
    private static class DDResolver implements EntityResolver {
        static DDResolver resolver;
        static synchronized DDResolver getInstance() {
            if (resolver==null) {
                resolver=new DDResolver();
            }
            return resolver;
        }

        /**
         * Return a proper input source
         * @param publicId
         * @param systemId
         * @return 
         */
        @Override
        public InputSource resolveEntity(String publicId, String systemId) {
            String resource;
            if (EJB_40_DOCTYPE.equals(systemId)) {
                resource = "/org/netbeans/modules/j2ee/dd/impl/resources/ejb-jar_4_0.xsd"; //NOI18N
            } else if (EJB_32_DOCTYPE.equals(systemId)) {
                resource = "/org/netbeans/modules/j2ee/dd/impl/resources/ejb-jar_3_2.xsd"; //NOI18N
            } else if (EJB_31_DOCTYPE.equals(systemId)) {
                resource = "/org/netbeans/modules/j2ee/dd/impl/resources/ejb-jar_3_1.xsd"; //NOI18N
            } else if (EJB_30_DOCTYPE.equals(systemId)) {
                resource = "/org/netbeans/modules/j2ee/dd/impl/resources/ejb-jar_3_0.xsd"; //NOI18N
            } else if (EJB_21_DOCTYPE.equals(systemId)) {
                resource = "/org/netbeans/modules/j2ee/dd/impl/resources/ejb-jar_2_1.xsd"; //NOI18N
            } else {
                return null;
            }
            URL url = this.getClass().getResource(resource);
            return new InputSource(url.toString());
        }
    }
    
    private static class ErrorHandler implements org.xml.sax.ErrorHandler {
        private int errorType=-1;
        SAXParseException error;

        @Override
        public void warning(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            if (errorType<0) {
                errorType=0;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }
        @Override
        public void error(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            if (errorType<1) {
                errorType=1;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }
        @Override
        public void fatalError(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            errorType=2;
            throw sAXParseException;
        }

        public int getErrorType() {
            return errorType;
        }
        public SAXParseException getError() {
            return error;
        }
    }

    private class DDFileChangeListener extends FileChangeAdapter {
        @Override
        public void fileChanged(FileEvent evt) {
            FileObject fo = evt.getFile();
            try {
                synchronized (ddMap) {
                    EjbJarProxy ejbJarProxy = getFromCache(fo);
                    if (ejbJarProxy != null) {
                        try (InputStream inputStream = fo.getInputStream()) {
                            String encoding = EncodingUtil.detectEncoding(new BufferedInputStream(inputStream));
                            if (encoding == null) {
                                encoding = "UTF8";
                            }
                            DDUtils.merge(ejbJarProxy, new InputStreamReader(inputStream, encoding));
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
