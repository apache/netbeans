/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.dd.api.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.impl.client.ClientParseUtils;
import org.netbeans.modules.j2ee.dd.impl.client.AppClientProxy;
import org.netbeans.modules.j2ee.dd.impl.common.DDUtils;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Provides access to Deployment Descriptor root
 * ({@link org.netbeans.modules.j2ee.dd.api.client.AppClient} object).
 *
 * @author  Milan Kuchtiak
 */
@org.netbeans.api.annotations.common.SuppressWarnings(value="DMI_COLLECTION_OF_URLS", justification="File URLs only")
public final class DDProvider {
    
    private static final DDProvider ddProvider = new DDProvider();
    private final FCA fileChangeListener;
    private final Map<URL, WeakReference<AppClientProxy>> ddMap;
    private final Map<URL, WeakReference<AppClient>> baseBeanMap;
    private final Map<URL, SAXParseException> errorMap;
    
    private DDProvider() {
        ddMap = new HashMap<URL, WeakReference<AppClientProxy>>(5);
        baseBeanMap = new HashMap<URL, WeakReference<AppClient>>(5);
        errorMap = new HashMap<URL, SAXParseException>(5);
        fileChangeListener = new FCA();
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
     * @param f File representing the web.xml file
     * @return appClient object - root of the deployment descriptor bean graph
     */
    public AppClient getDDRoot(File f) throws IOException, SAXException {
        FileObject fo = FileUtil.toFileObject(f);
        if (fo == null) {
            throw new IOException(String.format("File: %s does not exist.", f.getAbsolutePath())); //NOI18N
        }
        return DDUtils.createAppClient(fo, ClientParseUtils.getVersion(fo));
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clints planning to read only the deployment descriptor
     * or to listen to the changes.
     * @param fo FileObject representing the application-client.xml file
     * @return AppClient object - root of the deployment descriptor bean graph
     */
    public synchronized AppClient getDDRoot(FileObject fo) throws IOException {
        if (fo == null) {
            return null;
        }
        AppClientProxy appClient = null;
        
        synchronized (ddMap) {
            appClient = getFromCache(fo);
            if (appClient!=null) {
                return appClient;
            }
        }
        
        fo.addFileChangeListener(fileChangeListener);
        
        String version = null;
        SAXParseException error = null;
        try {
            AppClient original = null;
            synchronized (baseBeanMap) {
                original = getOriginalFromCache(fo);
                if (original == null) {
                    version = ClientParseUtils.getVersion(fo);
                    // preparsing
                    error = ClientParseUtils.parse(fo);
                    original = DDUtils.createAppClient(fo, version);
                    baseBeanMap.put(fo.toURL(), new WeakReference<AppClient>(original));
                    errorMap.put(fo.toURL(), error);
                } else {
                    BigDecimal orgVersion = original.getVersion();
                    if (orgVersion != null){
                        version = orgVersion.toPlainString();
                    }
                    error = errorMap.get(fo.toURL());
                }
            }
            appClient = new AppClientProxy(original, version);
            if (error != null) {
                appClient.setStatus(AppClient.STATE_INVALID_PARSABLE);
                appClient.setError(error);
            }
        } catch (SAXException ex) {
            appClient = new AppClientProxy(null, version);
            appClient.setStatus(AppClient.STATE_INVALID_UNPARSABLE);
            if (ex instanceof SAXParseException) {
                appClient.setError((SAXParseException) ex);
            } else if (ex.getException() instanceof SAXParseException) {
                appClient.setError((SAXParseException) ex.getException());
            }
        }
        synchronized(ddMap){
            AppClient cached = getFromCache(fo);
            if (cached != null) {
                return cached;
            }
            ddMap.put(fo.toURL(), new WeakReference<AppClientProxy>(appClient));
        }
        return appClient;
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clients planning to modify the deployment descriptor.
     * Finally the {@link org.netbeans.modules.j2ee.dd.api.ejb.EjbJar#write(org.openide.filesystems.FileObject)} should be used
     * for writing the changes.
     * @param fo FileObject representing the ejb-jar.xml file
     * @return EjbJar object - root of the deployment descriptor bean graph
     */
    public AppClient getDDRootCopy(FileObject fo) throws IOException {
        return (AppClient)getDDRoot(fo).clone();
    }
    
    private AppClientProxy getFromCache(FileObject fo) throws IOException {
        if (fo == null) {
            return null;
        }
        WeakReference<AppClientProxy> wr = ddMap.get(fo.toURL());
        if (wr == null) {
            return null;
        }
        AppClientProxy appClient = wr.get();
        if (appClient == null) {
            ddMap.remove(fo.toURL());
        }
        return appClient;
    }
    
    private AppClient getOriginalFromCache(FileObject fo) throws IOException {
        WeakReference<AppClient> wr = baseBeanMap.get(fo.toURL());
        if (wr == null) {
            return null;
        }
        AppClient appClient = wr.get();
        if (appClient == null) {
            baseBeanMap.remove(fo.toURL());
            errorMap.remove(fo.toURL());
        }
        return appClient;
    }
    
    /**  Convenient method for getting the BaseBean object from CommonDDBean object.
     * The j2eeserver module needs BaseBean to implement jsr88 API.
     * This is a temporary workaround until the implementation of jsr88 moves into ddapi
     * or the implementation in j2eeserver gets changed.
     * @deprecated do not use - temporary workaround that exposes the schema2beans implementation
     */
    @Deprecated
    public BaseBean getBaseBean(CommonDDBean bean) {
        BaseBean result;
        if (bean instanceof BaseBean) {
            result = (BaseBean) bean;
        } else if (bean instanceof AppClientProxy) {
            result = (BaseBean) ((AppClientProxy)bean).getOriginal();
        } else {
            result = null;
        }
        return result;
    }
    
    
    private class FCA extends FileChangeAdapter {
        
        public void fileChanged(FileEvent evt) {
            FileObject fo=evt.getFile();
            try {
                synchronized (ddMap) {
                    synchronized (baseBeanMap) {
                        AppClientProxy appClient = getFromCache(fo);
                        AppClient orig = getOriginalFromCache(fo);
                        if (appClient!=null) {
                            String version = null;
                            try {
                                version = ClientParseUtils.getVersion(fo);
                                // preparsing
                                SAXParseException error = ClientParseUtils.parse(fo);
                                if (error!=null) {
                                    appClient.setError(error);
                                    appClient.setStatus(AppClient.STATE_INVALID_PARSABLE);
                                } else {
                                    appClient.setError(null);
                                    appClient.setStatus(AppClient.STATE_VALID);
                                }
                                AppClient original = DDUtils.createAppClient(fo, version);
                                baseBeanMap.put(fo.toURL(), new WeakReference<AppClient>(original));
                                errorMap.put(fo.toURL(), appClient.getError());
                                appClient.merge(original, AppClient.MERGE_UPDATE);
                            } catch (SAXException ex) {
                                if (ex instanceof SAXParseException) {
                                    appClient.setError((SAXParseException)ex);
                                } else if ( ex.getException() instanceof SAXParseException) {
                                    appClient.setError((SAXParseException)ex.getException());
                                }
                                appClient.setStatus(AppClient.STATE_INVALID_UNPARSABLE);
                                appClient.setOriginal(null);
                                appClient.setProxyVersion(version);
                            }
                        } else if (orig != null) {
                            String version = null;
                            try {
                                version = ClientParseUtils.getVersion(fo);
                                AppClient original = DDUtils.createAppClient(fo, version);
                                if (original.getClass().equals(orig.getClass())) {
                                    orig.merge(original,AppClient.MERGE_UPDATE);
                                } else {
                                    baseBeanMap.put(fo.toURL(), new WeakReference<AppClient>(original));
                                }
                            } catch (SAXException ex) {
                                baseBeanMap.remove(fo.toURL());
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
    
}
