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
package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.impl.ejb.EjbJarProxy;
import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.openide.filesystems.FileObject;

/**
 * @author pfiala
 */
public class DDUtils {
    
    public static EjbJarProxy createEjbJarProxy(InputStream inputStream) throws IOException {
        return createEjbJarProxy(new InputSource(inputStream));
    }

    public static EjbJarProxy createEjbJarProxy(FileObject fo) throws IOException {
        try (InputStream inputStream = fo.getInputStream()) {
            return createEjbJarProxy(new InputSource(inputStream));
        }
    }

    public static EjbJarProxy createEjbJarProxy(Reader reader) throws IOException {
        return createEjbJarProxy(new InputSource(reader));
    }

    public static EjbJarProxy createEjbJarProxy(InputSource inputSource) throws IOException {
        try {
            return (EjbJarProxy) DDProvider.getDefault().getDDRoot(inputSource);
        } catch (SAXException ex) {
            // XXX lets throw an exception here
            EjbJar ejbJar = org.netbeans.modules.j2ee.dd.impl.ejb.model_2_1.EjbJar.createGraph();
            EjbJarProxy ejbJarProxy = new EjbJarProxy(ejbJar, ejbJar.getVersion().toString());
            ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
            if (ex instanceof SAXParseException) {
                ejbJarProxy.setError((SAXParseException) ex);
            } else if (ex.getException() instanceof SAXParseException) {
                ejbJarProxy.setError((SAXParseException) ex.getException());
            }
            return ejbJarProxy;
        }
    }


    public static void merge(EjbJarProxy ejbJarProxy, Reader reader) {
        try {
            EjbJarProxy newEjbJarProxy = createEjbJarProxy(reader);
            if (newEjbJarProxy.getStatus() == EjbJar.STATE_INVALID_UNPARSABLE) {
                ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
                ejbJarProxy.setError(newEjbJarProxy.getError());
                return;
            }
            ejbJarProxy.merge(newEjbJarProxy, EjbJar.MERGE_UPDATE);
            ejbJarProxy.setStatus(newEjbJarProxy.getStatus());
            ejbJarProxy.setError(newEjbJarProxy.getError());
        } catch (IOException ex) {
            ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
            // cbw if the state of the xml file transitions from
            // parsable to unparsable this could be due to a user
            // change or cvs change. We would like to still
            // receive events when the file is restored to normal
            // so lets not set the original to null here but wait
            // until the file becomes parsable again to do a merge
            //ejbJarProxy.setOriginal(null);
        } catch (Schema2BeansRuntimeException s2bre){ // see #70286
            ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
            ejbJarProxy.setError(new SAXParseException(null, null, s2bre));
        } catch (RuntimeException re){ // see #99047
            if (re.getCause() instanceof Schema2BeansException){
                ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
                ejbJarProxy.setError(new SAXParseException(null, null, (Schema2BeansException) re.getCause()));
            } else if (re instanceof IllegalArgumentException){ // see #104180
                ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
                Logger.getLogger(DDUtils.class.getName()).log(Level.FINE, "IAE thrown during merge, see #104180.", re); //NO18N
            } else {
                throw re;
            }
        }
    }

    public static WebApp createWebApp(InputStream is, String version) throws IOException, SAXException {
        try {
            if (WebApp.VERSION_2_4.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_2_4.WebApp.createGraph(is);
            } else if (WebApp.VERSION_2_5.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_2_5.WebApp.createGraph(is);
            } else if (WebApp.VERSION_3_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_3_0.WebApp.createGraph(is);
            } else if (WebApp.VERSION_3_1.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_3_1.WebApp.createGraph(is);
            } else if (WebApp.VERSION_4_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_4_0.WebApp.createGraph(is);
            } else if (WebApp.VERSION_5_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_5_0.WebApp.createGraph(is);
            } else if (WebApp.VERSION_6_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_6_0.WebApp.createGraph(is);
            } else if (WebApp.VERSION_6_1.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_6_1.WebApp.createGraph(is);
            } else {
                return null;
            }
        } catch (RuntimeException ex) {
            throw new SAXException(ex);
        }
    }

    public static WebApp createWebApp(FileObject fo, String version) throws IOException, SAXException {
        try (InputStream inputStream = fo.getInputStream()) {
            return createWebApp(inputStream, version);
        }
    }

    public static AppClient createAppClient(InputStream is, String version) throws IOException, SAXException {
        try {
            if (AppClient.VERSION_1_4.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_1_4.ApplicationClient.createGraph(is);
            } else if (AppClient.VERSION_5_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_5_0.ApplicationClient.createGraph(is);
            } else if (AppClient.VERSION_6_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_6_0.ApplicationClient.createGraph(is);
            } else if (AppClient.VERSION_7_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_7_0.ApplicationClient.createGraph(is);
            } else if (AppClient.VERSION_8_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_8_0.ApplicationClient.createGraph(is);
            } else if (AppClient.VERSION_9_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_9_0.ApplicationClient.createGraph(is);
            } else if (AppClient.VERSION_10_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_10_0.ApplicationClient.createGraph(is);
            } else if (AppClient.VERSION_11_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_11_0.ApplicationClient.createGraph(is);
            }
        } catch (RuntimeException ex) {
            throw new SAXException(ex);
        }
        return null;
    }

    public static AppClient createAppClient(FileObject fo, String version) throws IOException, SAXException {
        try (InputStream inputStream = fo.getInputStream()) {
            return createAppClient(inputStream, version);
        }
    }
}
