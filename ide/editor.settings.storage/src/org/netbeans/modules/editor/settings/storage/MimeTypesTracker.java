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

package org.netbeans.modules.editor.settings.storage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.spi.StorageDescription;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * The tracker of mime types registered as folders under a common root. This class
 * will listen on a hierarchical structure of folders under a <code>baseFolder</code>
 * and will interpret its subfolders as mime type definitions. For example the
 * following structure of folders will be interpreted as two mime types 'text/x-java'
 * and 'application/pdf'.
 * 
 * <pre>
 *   &lt;baseFolder&gt;/text/x-java
 *   &lt;baseFolder&gt;/application/pdf
 * </pre>
 * 
 * @author Vita Stejskal
 */
public final class MimeTypesTracker {
        
    private static final Logger LOG = Logger.getLogger(MimeTypesTracker.class.getName());

    /** The property for notifying changes in mime types tracked by this tracker. */
    public static final String PROP_MIME_TYPES = "mime-types"; //NOI18N

    private static final Map<String, Map<StorageDescription, MimeTypesTracker>> settingMimeTypes = new HashMap<>();
    
    public static MimeTypesTracker get(String settingsTypeId, String basePath) {
        assert basePath != null : "The parameter basePath must not be null"; //NOI18N

        StorageDescription sd = null;
        
        if (settingsTypeId != null) {
            sd = SettingsType.find(settingsTypeId);
            assert sd != null : "Invalid editor settings type id: '" + settingsTypeId + "'"; //NOI18N
        }
        
        synchronized (settingMimeTypes) {
            Map<StorageDescription, MimeTypesTracker> map = settingMimeTypes.get(basePath);
            if (map == null) {
                map = new WeakHashMap<>();
                settingMimeTypes.put(basePath, map);
            }
            
            MimeTypesTracker tracker = map.get(sd);
            if (tracker == null) {
                tracker = new MimeTypesTracker(sd == null ? null : SettingsType.getLocator(sd), basePath);
                map.put(sd, tracker);
            }
            
            return tracker;
        }
    }
    
    /**
     * Create a new tracker for tracking mime types under the <code>basePath</code>
     * folder.
     * 
     * @param settingsType The type of settings to track mime types for. If not
     *   <code>null</code> the tracker will only list mime types that declare
     *   settings of this type.
     * @param basePath The path on the system <code>FileSystem</code> where the
     *   mime types should be tracked.
     */
    /* package */ MimeTypesTracker(SettingsType.Locator locator, String basePath) {
        this.locator = locator;
        this.basePath = basePath;
        this.basePathElements = basePath.split("/"); //NOI18N
        
        rebuild();
        
        // Start listening
        this.listener = new Listener();
        FileSystem sfs = null;
        try {
            sfs = FileUtil.getConfigRoot().getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        sfs.addFileChangeListener(WeakListeners.create(FileChangeListener.class,listener, sfs));
    }
    
    /**
     * Gets the root of the mime types hierarchy watched by this tracker.
     * 
     * @return The <code>basePath</code> passed to the constructor.
     */
    public String getBasePath() {
        return basePath;
    }
    
    /**
     * Gets the list of mime types (<code>String</code>s) located under this
     * tracker's <code>basePath</code>.
     * 
     * @return The list of mime types.
     */
    public Set<String> getMimeTypes() {
        synchronized (LOCK) {
            return mimeTypes.keySet();
        }
    }

    /**
     * Gets a display name for a mime type. The display name is read from the
     * localizing bundle associated to the mime type's folder (<code>FileObject</code>).
     * The value of the <code>mimeType</code> parameter will be used as bundle
     * key to read the display name.
     * 
     * @param mimeType The mime type to get the display name for.
     * @return The display (localized) name of the mime type or the <code>mimeType</code>
     *   if the display name can't be found.
     */
    public String getMimeTypeDisplayName(String mimeType) {
        String displayName = mimeTypes.get(mimeType);
        return displayName == null ? mimeType : displayName;
    }
    
    /**
     * Adds a listener that will be receiving <code>PROP_MIME_TYPES</code> notifcations.
     * 
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    /**
     * Removes a previously added listener.
     * 
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    // ------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------

    private final String LOCK = new String("MimeTypesTracker.LOCK"); //NOI18N
    
    private final String basePath;
    private final String [] basePathElements;
    private final SettingsType.Locator locator;
    
    private FileObject folder;
    private boolean isBaseFolder;
    
    private Map<String, String> mimeTypes = Collections.<String, String>emptyMap();

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final FileChangeListener listener;

    //for tests only:
    static boolean synchronous = false;
    
    /* package */ static final RequestProcessor RP = new RequestProcessor(MimeTypesTracker.class.getName()); //NOI18N
    private final RequestProcessor.Task task = RP.create(() -> {
        rebuild();
    });

    // #172043 - this is here to keep all folder FileObjects that we have traversed in the memory
    // so that FileSystems would know about them and fired events correctly
    private final Set<FileObject> trackedFolders = new HashSet<>();
    
    private void rebuild() {
        PropertyChangeEvent event = null;
        
        synchronized (LOCK) {
            Object [] ret = findTarget(basePathElements);
            FileObject f = (FileObject) ret[0];
            boolean isBase = ((Boolean) ret[1]);

            // The base folder or some folder up in the hierarchy has been created/deleted
            if (f != folder) {
                // Set the current folder and its is-target-flag
                folder = f;
                isBaseFolder = isBase;

                LOG.finest("folder = '" + folder.getPath() + "'"); //NOI18N
                LOG.finest("isBaseFolder = '" + isBaseFolder + "'"); //NOI18N
            }

            Map<String, String> newMimeTypes;
            
            if (isBaseFolder) {
                // Clear the cache
                newMimeTypes = new HashMap<>();

                // Go through mime type types
                FileObject [] types = folder.getChildren();
                for(int i = 0; i < types.length; i++) {
                    if (!isValidType(types[i])) {
                        continue;
                    }

                    // keep track of the type folder
                    trackedFolders.add(types[i]);

                    // Go through mime type subtypes
                    FileObject [] subTypes = types[i].getChildren();
                    for(int j = 0; j < subTypes.length; j++) {
                        if (!isValidSubtype(subTypes[j])) {
                            continue;
                        }

                        String mimeType = types[i].getNameExt() + "/" + subTypes[j].getNameExt(); //NOI18N

                        boolean add;
                        if (locator != null) {
                            Map<String, List<Object []>> scan = new HashMap<>();
                            locator.scan(folder, mimeType, null, false, true, true, false, scan);
                            add = !scan.isEmpty();

                            // we are interested in the subtype folder's content, so keep track of the subtype folder too
                            trackedFolders.add(subTypes[j]);
                        } else {
                            add = true;
                        }
                        
                        if (add) {
                            // First try the standard way for filesystem annotations
                            String displayName = Utils.getLocalizedName(subTypes[j], null);

                            // Then try the crap way introduced with Tools-Options
                            if (displayName == null) {
                                displayName = Utils.getLocalizedName(subTypes[j], mimeType, mimeType);
                            }
                            newMimeTypes.put(mimeType, displayName);
                        }
                    }
                }

                newMimeTypes = Collections.unmodifiableMap(newMimeTypes);
            } else {
                newMimeTypes = Collections.<String, String>emptyMap();
            }
            
            if (!mimeTypes.equals(newMimeTypes)) {
                event = new PropertyChangeEvent(this, PROP_MIME_TYPES, mimeTypes, newMimeTypes);
                mimeTypes = newMimeTypes;
            }

            for(Iterator<FileObject> i = trackedFolders.iterator(); i.hasNext(); ) {
                if (!i.next().isValid()) {
                    i.remove();
                }
            }
        }
        
        if (event != null) {
            pcs.firePropertyChange(event);
            EditorSettingsImpl.getInstance().notifyMimeTypesChange(event.getOldValue(), event.getNewValue());
        }
    }

    private static boolean isValidType(FileObject typeFile) {
        if (!typeFile.isFolder()) {
            return false;
        }

        String typeName = typeFile.getNameExt();
        return MimePath.validate(typeName, null);
    }

    private static boolean isValidSubtype(FileObject subtypeFile) {
        if (!subtypeFile.isFolder()) {
            return false;
        }

        String typeName = subtypeFile.getNameExt();
        return MimePath.validate(null, typeName) && !typeName.equals("base"); //NOI18N
    }        
    
    private static Object [] findTarget(String [] path) {
        FileObject target = FileUtil.getConfigRoot();
        boolean isTarget = 0 == path.length;
        
        for (int i = 0; i < path.length; i++) {
            FileObject f = target.getFileObject(path[i]);

            if (f == null || !f.isFolder() || !f.isValid() || f.isVirtual()) {
                break;
            } else {
                target = f;
                isTarget = i + 1 == path.length;
            }
        }
        
        return new Object [] { target, isTarget};
    }

    private final class Listener extends FileChangeAdapter {
        
        public Listener() {
        }
        
        @Override
        public void fileFolderCreated(FileEvent fe) {
            notifyRebuild(fe.getFile());
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            notifyRebuild(fe.getFile());
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            notifyRebuild(fe.getFile());
        }
        
        private void notifyRebuild(FileObject f) {
            String path = f.getPath();
            if (path.startsWith(basePath)) {
                if (synchronous) rebuild();
                else task.schedule(1000);
            }
        }
    } // End of Listener class
}
