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

package org.netbeans.modules.j2ee.dd.api.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import org.netbeans.modules.j2ee.dd.impl.web.WebAppProxy;
import org.netbeans.modules.j2ee.dd.impl.web.WebParseUtils;
import org.netbeans.modules.j2ee.dd.impl.common.DDUtils;
import org.openide.filesystems.*;
import org.openide.util.NbBundle;
import org.xml.sax.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Provides access to Deployment Descriptor root ({@link org.netbeans.modules.j2ee.dd.api.web.WebApp} object)
 *
 * @author  Milan Kuchtiak
 */
@org.netbeans.api.annotations.common.SuppressWarnings(value="DMI_COLLECTION_OF_URLS", justification="File URLs only")
public final class DDProvider {
    private static DDProvider ddProvider;
    private final Map<URL, WeakReference<WebAppProxy>> ddMap;
    private final Map<URL, WeakReference<WebApp>> baseBeanMap;
    private final Map<URL, SAXParseException> errorMap;
    private final FCA fileChangeListener;
  
    private static final Logger LOGGER = Logger.getLogger(DDProvider.class.getName());
    
    /** 
     * Creates a new instance of DDProvider. 
     */
    private DDProvider() {
        ddMap = new HashMap<URL, WeakReference<WebAppProxy>>(5);
        baseBeanMap = new HashMap<URL, WeakReference<WebApp>>(5);
        errorMap = new HashMap<URL, SAXParseException>(5);
        fileChangeListener = new FCA();
    }
    
    /**
     * Accessor method for DDProvider singleton
     * @return DDProvider object
     */
    public static synchronized DDProvider getDefault() {
        if (ddProvider == null) {
            ddProvider = new DDProvider();
        }
        return ddProvider;
    }
    
    /**
     * Gets the root bean graph representing the given web.xml deployment descriptor
     * file. 
     * 
     * @param fo the file object representing a web.xml file. Must not be null.
     * @return the <code>WebApp</code> representing the given <code>fo</code>.
     * @throws IOException if the given <code>fo</code> could not be read
     * or if parsing it failed.
     */ 
    public WebApp getDDRoot(FileObject fo) throws IOException {
        Parameters.notNull("fo", fo); //NOI18N
        WebAppProxy webApp = null;
        
        synchronized (ddMap) {
            webApp = getFromCache(fo);
            if (webApp!=null) {
                return webApp;
            }
        }
        
        fo.addFileChangeListener(fileChangeListener);
        
        String version = null;
        SAXParseException error = null;
        try {
            WebApp original = null;
            synchronized (baseBeanMap) {
                original = getOriginalFromCache(fo);
                if (original == null) {
                    version = WebParseUtils.getVersion(fo);
                    if (version != null) {
                        // preparsing
                        error = parse(fo);
                        original = DDUtils.createWebApp(fo, version);
                        baseBeanMap.put(fo.toURL(), new WeakReference<>(original));
                        errorMap.put(fo.toURL(), error);
                    }
                } else {
                    version = original.getVersion();
                    error = errorMap.get(fo.toURL());
                }
            }
            if (version != null) {
                webApp = new WebAppProxy(original, version);
                if (original == null) {
                    webApp.setStatus(WebApp.STATE_INVALID_OLD_VERSION);
                } else if (error != null) {
                    webApp.setStatus(WebApp.STATE_INVALID_PARSABLE);
                    webApp.setError(error);
                }
            } else {
                // #169465 - need to handle the case when version was not determined
                webApp = new WebAppProxy(null, null);
                webApp.setStatus(WebApp.STATE_INVALID_UNPARSABLE);
                webApp.setError(new SAXParseException(NbBundle.getMessage(DDProvider.class, "MSG_cannotDetermineVersion"), null));
            }
        } catch (SAXException ex) {
            webApp = new WebAppProxy(null, version);
            webApp.setStatus(WebApp.STATE_INVALID_UNPARSABLE);
            if (ex instanceof SAXParseException) {
                webApp.setError((SAXParseException) ex);
            } else if (ex.getException() instanceof SAXParseException) {
                webApp.setError((SAXParseException) ex.getException());
            }
        } catch (FileNotFoundException fnfe) {
            // see #116571. not pretty, but at least allows users to open a web.xml similar 
            // to the one in the aforementioned issue without NPEs. handling of the declared IOE in the
            // clients of this method is a can of worms that i do not want to open right now. moreover,
            // the situation when the FNFE is thrown is similar to when a SAXException is thrown.
            LOGGER.log(Level.INFO,
                    "A file referenced from [" +
                    FileUtil.getFileDisplayName(fo) + "] could not be found",
                    fnfe); // NO18N
            webApp = new WebAppProxy(null, version);
            webApp.setStatus(WebApp.STATE_INVALID_UNPARSABLE);
            webApp.setError(new SAXParseException(fnfe.getMessage(), null, fnfe));
        }
        synchronized(ddMap){
            WebApp cached = getFromCache(fo);
            if (cached != null) {
                return cached;
            }
            ddMap.put(fo.toURL(), new WeakReference<>(webApp));
        }
        return webApp;
    }

    /**
     * Gets the root bean graph representing the given web.xml deployment descriptor
     * file.
     *
     * @param fo the file object representing a web.xml file. Must not be null.
     * @param useCache true if the cache should be used
     * @return the <code>WebApp</code> representing the given <code>fo</code>.
     * @throws IOException if the given <code>fo</code> could not be read
     * or if parsing it failed.
     */
    public WebApp getDDRoot(FileObject fo, boolean useCache) throws IOException {
        if (!useCache)
            removeFromCache(fo);
        return getDDRoot(fo);
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clients planning to modify the deployment descriptor.
     * Finally the {@link org.netbeans.modules.j2ee.dd.api.web.WebApp#write(org.openide.filesystems.FileObject)} should be used
     * for writing the changes.
     * @param fo FileObject representing the web.xml file
     * @return WebApp object - root of the deployment descriptor bean graph
     */
    public WebApp getDDRootCopy(FileObject fo) throws IOException {
        return (WebApp)getDDRoot(fo).clone();
    }
    
    private WebAppProxy getFromCache(FileObject fo) throws IOException {
        WeakReference<WebAppProxy> wr = ddMap.get(fo.toURL());
        if (wr == null) {
            return null;
        }
        WebAppProxy webApp = (WebAppProxy) wr.get();
        if (webApp == null) {
            ddMap.remove(fo.toURL());
        }
        return webApp;
    }
    
    private WebApp getOriginalFromCache(FileObject fo) throws IOException {
        WeakReference<WebApp> wr = baseBeanMap.get(fo.toURL());
        if (wr == null) {
            return null;
        }        
        WebApp webApp = (WebApp) wr.get();
        if (webApp == null) {
            baseBeanMap.remove(fo.toURL());
            errorMap.remove(fo.toURL());
        }
        return webApp;
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param f File representing the web.xml file
     * @return WebApp object - root of the deployment descriptor bean graph
     */
    public WebApp getDDRoot(File f) throws IOException, SAXException {
        FileObject fileObject = FileUtil.toFileObject(f);
        if (fileObject == null) {
            throw new IOException(String.format("File: %s does not exist.", f.getAbsolutePath())); //NOI18N
        }
        return DDUtils.createWebApp(fileObject, WebParseUtils.getVersion(fileObject));
    }
    
    /**  Convenient method for getting the BaseBean object from CommonDDBean object.
     * The j2eeserver module needs BaseBean to implement jsr88 API.
     * This is a temporary workaround until the implementation of jsr88 moves into ddapi
     * or the implementation in j2eeserver gets changed.
     * @deprecated do not use - temporary workaround that exposes the schema2beans implementation
     */
    @Deprecated
    public org.netbeans.modules.schema2beans.BaseBean getBaseBean(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean bean) {
        if (bean instanceof org.netbeans.modules.schema2beans.BaseBean) {
            return (org.netbeans.modules.schema2beans.BaseBean) bean;
        } else if (bean instanceof WebAppProxy) {
            return (org.netbeans.modules.schema2beans.BaseBean) ((WebAppProxy) bean).getOriginal();
        }
        return null;
    }
    
    public SAXParseException parse(FileObject fo)
    throws org.xml.sax.SAXException, java.io.IOException {
        return WebParseUtils.parse(fo);
    }


    /**
     * Removes the entries associated with the given <code>fo</code> from
     * the various caches that this class utilizes.
     * @param fo
     */
    private void removeFromCache(FileObject fo){
        URL foUrl = fo.toURL();
        synchronized (ddMap){
            ddMap.remove(foUrl);
        }
        baseBeanMap.remove(foUrl);
        errorMap.remove(foUrl);
    }
    
    private class FCA extends FileChangeAdapter {
        @Override
        public void fileChanged(FileEvent evt) {
            FileObject fo=evt.getFile();
            try {
                synchronized (ddMap) {
                    synchronized (baseBeanMap) {
                        WebAppProxy webApp = getFromCache(fo);
                        WebApp orig = getOriginalFromCache(fo);
                        if (webApp!=null) {
                            String version = null;
                            try {
                                version = WebParseUtils.getVersion(fo);
                                // preparsing
                                SAXParseException error = parse(fo);
                                if (error!=null) {
                                    webApp.setError(error);
                                    webApp.setStatus(WebApp.STATE_INVALID_PARSABLE);
                                } else {
                                    webApp.setError(null);
                                    webApp.setStatus(WebApp.STATE_VALID);
                                }
                                WebApp original = DDUtils.createWebApp(fo, version);
                                if (original == null) {
                                    webApp.setStatus(WebApp.STATE_INVALID_OLD_VERSION);
                                    webApp.setError(null);
                                }
                                baseBeanMap.put(fo.toURL(), new WeakReference<>(original));
                                errorMap.put(fo.toURL(), webApp.getError());
                                webApp.merge(original, WebApp.MERGE_UPDATE);
                            } catch (SAXException ex) {
                                if (ex instanceof SAXParseException) {
                                    webApp.setError((SAXParseException)ex);
                                } else if ( ex.getException() instanceof SAXParseException) {
                                    webApp.setError((SAXParseException)ex.getException());
                                }
                                webApp.setStatus(WebApp.STATE_INVALID_UNPARSABLE);
                                webApp.setOriginal(null);
                                webApp.setProxyVersion(version);
                            }
                        } else if (orig != null) {
                            String version = null;
                            try {
                                version = WebParseUtils.getVersion(fo);
                                WebApp original = DDUtils.createWebApp(fo, version);
                                if (original == null) {
                                    baseBeanMap.remove(fo.toURL());
                                } else {
                                    if (original.getClass().equals(orig.getClass())) {
                                        orig.merge(original,WebApp.MERGE_UPDATE);
                                    } else {
                                        baseBeanMap.put(fo.toURL(), new WeakReference<>(original));
                                    }
                                }
                            } catch (SAXException ex) {
                                baseBeanMap.remove(fo.toURL());
                            }
                        }
                    }
                }
            } catch (IOException ex){
                LOGGER.log(Level.INFO, "Merging of webApp graphs failed.", ex);//NOI18N
            }
        }
        
        @Override
        public void fileDeleted(FileEvent fe) {
            // need to remove cache entries, see #76431.
            removeFromCache(fe.getFile());
        }
        
        
    }
    
}
