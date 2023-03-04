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

package org.netbeans.modules.editor.settings.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.spi.StorageReader;
import org.netbeans.modules.editor.settings.storage.spi.StorageWriter;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.BaseUtilities;
import org.openide.util.NbBundle;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


/**
 * This class contains support static methods for loading / saving and 
 * translating coloring (fontsColors.xml) files. It calls XMLStorage utilities.
 *
 * @author Jan Jancura
 */
public class Utils {
    /** The name of the default profile. */
    public static final String DEFAULT_PROFILE = "NetBeans"; //NOI18N
    
    public static final String TEXT_BASE_MIME_TYPE = "text/base"; //NOI18N
    
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());
    
    public static String getLocalizedName(FileObject fo, String defaultValue) {
        try {
            return fo.getFileSystem().getDecorator().annotateName(defaultValue, Collections.singleton(fo));
        } catch (FileStateInvalidException ex) {
            if (LOG.isLoggable(Level.FINE)) {
                logOnce(LOG, Level.FINE, "Can't find localized name of " + fo, ex); //NOI18N
            }
            return defaultValue;
        }
    }
    
    public static String getLocalizedName(FileObject fo, String key, String defaultValue) {
        return getLocalizedName(fo, key, defaultValue, false);
    }
    
    public static String getLocalizedName(FileObject fo, String key, String defaultValue, boolean silent) {
        assert key != null : "The key can't be null"; //NOI18N

        Object [] bundleInfo = findResourceBundle(fo, silent);
        if (bundleInfo[1] != null) {
            try {
                return ((ResourceBundle) bundleInfo[1]).getString(key);
            } catch (MissingResourceException ex) {
                if (!silent && LOG.isLoggable(Level.FINE)) {
                    logOnce(LOG, Level.FINE, "The bundle '" + bundleInfo[0] + "' is missing key '" + key + "'.", ex); //NOI18N
                }
            }
        }
        
        return defaultValue;
    }

//    private static final WeakHashMap<FileObject, Object []> bundleInfos = new WeakHashMap<FileObject, Object []>();
//    private static final FileChangeListener listener = new FileChangeAdapter() {
//        @Override
//        public void fileDeleted(FileEvent fe) {
//            synchronized (bundleInfos) {
//                bundleInfos.remove(fe.getFile());
//            }
//        }
//
//        @Override
//        public void fileAttributeChanged(FileAttributeEvent fe) {
//            if (fe.getName() != null && fe.getName().equals("SystemFileSystem.localizingBundle")) { //NOI18N
//                synchronized (bundleInfos) {
//                    bundleInfos.remove(fe.getFile());
//                }
//            }
//        }
//    };
//    private static final FileChangeListener weakListener = WeakListeners.create(FileChangeListener.class, listener, null);
    private static Object [] findResourceBundle(FileObject fo, boolean silent) {
        assert fo != null : "FileObject can't be null"; //NOI18N

        Object [] bundleInfo = null;
//        synchronized (bundleInfos) {
//            Object [] bundleInfo = bundleInfos.get(fo);
//            if (bundleInfo == null) {
                String bundleName = null;
                Object attrValue = fo.getAttribute("SystemFileSystem.localizingBundle"); //NOI18N
                if (attrValue instanceof String) {
                    bundleName = (String) attrValue;
                }

                if (bundleName != null) {
                    try {
                        bundleInfo = new Object [] { bundleName, NbBundle.getBundle(bundleName) };
                    } catch (MissingResourceException ex) {
                        if (!silent && LOG.isLoggable(Level.FINE)) {
                            logOnce(LOG, Level.FINE, "Can't find resource bundle for " + fo.getPath(), ex); //NOI18N
                        }
                    }
                } else {
                    if (!silent && LOG.isLoggable(Level.FINE)) {
                        logOnce(LOG, Level.FINE, "The file " + fo.getPath() + " does not specify its resource bundle.", null); //NOI18N
                    }
                }

                if (bundleInfo == null) {
                   bundleInfo = new Object [] { bundleName, null }; 
                }

//                bundleInfos.put(fo, bundleInfo);
//                fo.removeFileChangeListener(weakListener);
//                fo.addFileChangeListener(weakListener);
//            }

            return bundleInfo;
//        }
    }
    
    private static final Set<String> ALREADY_LOGGED = Collections.synchronizedSet(new HashSet<String>());
    public static void logOnce(Logger logger, Level level, String msg, Throwable t) {
        if (!ALREADY_LOGGED.contains(msg)) {
            ALREADY_LOGGED.add(msg);
            if (t != null) {
                logger.log(level, msg, t);
            } else {
                logger.log(level, msg);
            }
            
            if (ALREADY_LOGGED.size() > 100) {
                ALREADY_LOGGED.clear();
            }
        }
    }
    
    /**
     * Converts an array of mime types to a <code>MimePath</code> instance.
     */
    public static MimePath mimeTypes2mimePath(String[] mimeTypes) {
        MimePath mimePath = MimePath.EMPTY;
        
        for (int i = 0; i < mimeTypes.length; i++) {
            mimePath = MimePath.get(mimePath, mimeTypes[i]);
        }
        
        return mimePath;
    }

    public static <A, B> void diff(Map<A, B> oldMap, Map<A, B> newMap, Map<A, B> addedEntries, Map<A, B> removedEntries) {
        for(A key : oldMap.keySet()) {
            if (!newMap.containsKey(key)) {
                removedEntries.put(key, oldMap.get(key));
            } else {
                if (!BaseUtilities.compareObjects(oldMap.get(key), newMap.get(key))) {
                    addedEntries.put(key, newMap.get(key));
                }
            }
        }
        
        for(A key : newMap.keySet()) {
            if (!oldMap.containsKey(key)) {
                addedEntries.put(key, newMap.get(key));
            }
        }
    }

    public static <A, B> boolean quickDiff(Map<A, B> oldMap, Map<A, B> newMap) {
        for(A key : oldMap.keySet()) {
            if (!newMap.containsKey(key)) {
                return true;
            } else {
                if (!BaseUtilities.compareObjects(oldMap.get(key), newMap.get(key))) {
                    return true;
                }
            }
        }
        
        for(A key : newMap.keySet()) {
            if (!oldMap.containsKey(key)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static void save(FileObject fo, StorageWriter writer) {
        assert fo != null : "FileObject can't be null"; //NOI18N
        assert writer != null : "StorageWriter can't be null"; //NOI18N
        
        try {
            FileLock lock = fo.lock();
            try {
                OutputStream os = fo.getOutputStream(lock);
                try {
                    XMLUtil.write(writer.getDocument(), os, "UTF-8"); //NOI18N
                } finally {
                    os.close();
                }
            } finally {
                lock.releaseLock();
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Can't save editor settings to " + fo.getPath(), ex); //NOI18N
        }
    }
    
    public static void load(FileObject fo, StorageReader handler, boolean validate) {
        assert fo != null : "Settings file must not be null"; //NOI18N
        assert handler != null : "StorageReader can't be null"; //NOI18N
        
        try {
            XMLReader reader = XMLUtil.createXMLReader(validate);
            reader.setEntityResolver(EntityCatalog.getDefault());
            reader.setContentHandler(handler);
            reader.setErrorHandler(handler);
            reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler); //NOI18N
            
            InputStream is = fo.getInputStream();
            try {
                reader.parse(new InputSource(is));
            } finally {
                is.close();
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Invalid or corrupted file: " + fo.getPath(), ex); //NOI18N
        }
    }
}
