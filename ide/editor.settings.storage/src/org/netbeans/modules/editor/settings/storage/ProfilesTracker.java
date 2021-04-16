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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.netbeans.modules.editor.settings.storage.SettingsType.Locator;
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
 *
 * @author Vita Stejskal
 */
public final class ProfilesTracker {
    
    /**
     * The property name for notifying changes in the tracked profiles.
     */
    public static final String PROP_PROFILES = "profiles"; //NOI18N
    
    private static final Map<String, Map<StorageDescription, ProfilesTracker>> settingProfiles = new HashMap<String, Map<StorageDescription, ProfilesTracker>>();
    
    public static ProfilesTracker get(String settingsTypeId, String basePath) {
        assert settingsTypeId != null : "The parameter settingsTypeId must not be null"; //NOI18N
        assert basePath != null : "The parameter basePath must not be null"; //NOI18N
        
        StorageDescription sd = SettingsType.find(settingsTypeId);
        assert sd != null : "Invalid editor settings type id: '" + settingsTypeId + "'"; //NOI18N
        
        synchronized (settingProfiles) {
            Map<StorageDescription, ProfilesTracker> map = settingProfiles.get(basePath);
            if (map == null) {
                map = new WeakHashMap<>();
                settingProfiles.put(basePath, map);
            }
            
            ProfilesTracker tracker = map.get(sd);
            if (tracker == null) {
                SettingsType.Locator locator = SettingsType.getLocator(sd);
                assert locator.isUsingProfiles() : "No need to track profiles for settings that do not use profiles."; //NOI18N
                
                tracker = new ProfilesTracker(locator, MimeTypesTracker.get(null, basePath));
                map.put(sd, tracker);
            }
            
            return tracker;
        }
    }
    
    /**
     * Creates a new instance of ProfilesTracker.
     * 
     * @param type 
     * @param mimeTypes 
     * @param strict 
     */
    /* package */ProfilesTracker(Locator locator, MimeTypesTracker mimeTypes) {
        this.locator = locator;
        this.mimeTypes = mimeTypes;

        rebuild();

        // Start listening
        this.listener = new Listener();
        FileSystem sfs = null;
        try {
            sfs = FileUtil.getConfigRoot().getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        this.systemFileSystem = sfs;
        this.systemFileSystem.addFileChangeListener(WeakListeners.create(FileChangeListener.class, listener, this.systemFileSystem));
        this.mimeTypes.addPropertyChangeListener(listener);
    }
    
    /**
     * Gets the list of profiles for the tracked setting type.
     * 
     * @return Profiles as a map of profile name -&gt; profile display name.
     */
    public Set<String> getProfilesDisplayNames() {
        synchronized (LOCK) {
            return profilesByDisplayName.keySet();
        }
    }

    /**
     * Gets description for a profile by its name.
     * 
     * @param displayName The display name of the profile to get the description for.
     * @retutn The profile's description or <code>null</code> if there is no
     *   profile with the display name.
     */
    public ProfileDescription getProfileByDisplayName(String displayName) {
        synchronized (LOCK) {
            return profilesByDisplayName.get(displayName);
        }
    }
    
    /**
     * Adds a listener that will be receiving <code>PROP_PROFILES</code> notifcations.
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

    public static final class ProfileDescription {
        private final String id;
        private final String displayName;
        private final boolean isRollbackAllowed;
        // for logging only
        private final String profileOrigin;
        
        private ProfileDescription(String id, String displayName, boolean isRollbackAllowed, String profileOrigin) {
            this.id = id;
            this.displayName = displayName;
            this.isRollbackAllowed = isRollbackAllowed;
            this.profileOrigin = profileOrigin;
        }
        
        public boolean isRollbackAllowed() {
            return isRollbackAllowed;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getId() {
            return id;
        }

    } // End of ProfileDescription class
    
    // ------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(ProfilesTracker.class.getName());

    private final SettingsType.Locator locator;
    private final MimeTypesTracker mimeTypes;
    
    private final FileSystem systemFileSystem;
    private final Listener listener;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private final String LOCK = new String("ProfilesTracker.LOCK"); //NOI18N
    private Map<String, ProfileDescription> profiles = Collections.<String, ProfileDescription>emptyMap();
    private Map<String, ProfileDescription> profilesByDisplayName = Collections.<String, ProfileDescription>emptyMap();

    //for tests only:
    static boolean synchronous = false;
    
    private final RequestProcessor.Task task = MimeTypesTracker.RP.create(() -> {
        rebuild();
    });
    
    // #172043 - this is here to keep all folder FileObjects that we have traversed in the memory
    // so that FileSystems would know about them and fired events correctly
    private final Set<FileObject> trackedFolders = new HashSet<>();
    
    private void rebuild() {
        PropertyChangeEvent event = null;

        synchronized (LOCK) {
            Map<String, List<Object[]>> scan = new HashMap<>();

            FileObject baseFolder = FileUtil.getConfigFile(mimeTypes.getBasePath());
            if (baseFolder != null && baseFolder.isFolder()) {
                // Scan base folder
                locator.scan(baseFolder, null, null, false, true, true, false, scan);

                // Scan mime type folders
                Collection<String> mimes = mimeTypes.getMimeTypes();
                for (String mime : mimes) {
                    locator.scan(baseFolder, mime, null, false, true, true, false, scan);
                }
            }

            HashMap<String, ProfileDescription> newProfiles = new HashMap<>();
            HashMap<String, ProfileDescription> newProfilesByDisplayName = new HashMap<>();
            for(String id : scan.keySet()) {
                List<Object []> profileInfos = scan.get(id);

                // Determine profile's display name and if it can roll back user changes
                String displayName  = null;
                boolean canRollback = false;
                String profileOrigin = null;
                for(Object [] info : profileInfos) {
                    FileObject profileHome = (FileObject) info[0];
                    FileObject settingFile = (FileObject) info[1];
                    boolean modulesFile = ((Boolean) info[2]);

                    if (profileHome != null) {
                        trackedFolders.add(profileHome.getParent());
                        profileOrigin = profileHome.getPath();

                        if (displayName == null) {
                            // First try the standard way for filesystem annotations
                            displayName = Utils.getLocalizedName(profileHome, null);

                            // Then try the crap way introduced with Tools-Options
                            if (displayName == null) {
                                displayName = Utils.getLocalizedName(profileHome, id, null);
                            }
                        }
                    } else {
                        profileOrigin = settingFile.getPath();
                    }

                    if (!canRollback) {
                        canRollback = modulesFile;
                    }

                    if (displayName != null && canRollback) {
                        break;
                    }
                }
                displayName = displayName == null ? id : displayName;

                // Check for duplicate display names
                ProfileDescription maybeDupl = newProfilesByDisplayName.get(displayName);
                if (maybeDupl != null) {
                    // writable file for all languages in the profile's home folder
                    String writableFile = baseFolder.getPath() + "/" + locator.getWritableFileName(null, id, null, false); //NOI18N
                    if (writableFile.startsWith(profileOrigin)) {
                        // the profile comes from the empty mimepath (all languages) and we will prefer it
                        newProfiles.remove(maybeDupl.getId());
                        newProfilesByDisplayName.remove(displayName);
                        LOG.warning("Ignoring profile '" + maybeDupl.getId() + "' (" + maybeDupl.profileOrigin + ") in favor of '" + id + "' (" + profileOrigin + ") with the same display name."); //NOI18N
                    } else {
                        LOG.warning("Ignoring profile '" + id + "' (" + profileOrigin + "), it's got the same display name as '" + maybeDupl.getId() + "' (" + maybeDupl.profileOrigin + ")."); //NOI18N
                        continue;
                    }
                }

                ProfileDescription desc = reuseOrCreate(id, displayName, canRollback, profileOrigin);
                newProfiles.put(id, desc);
                newProfilesByDisplayName.put(displayName, desc);
            }

            // Just a sanity check
            assert newProfilesByDisplayName.size() == newProfiles.size() : "Inconsistent profile maps"; //NOI18N

            if (!profiles.equals(newProfiles)) {
                event = new PropertyChangeEvent(this, PROP_PROFILES, profiles, newProfiles);
                profiles = newProfiles;
                profilesByDisplayName = newProfilesByDisplayName;
            }

            for(Iterator<FileObject> i = trackedFolders.iterator(); i.hasNext(); ) {
                if (!i.next().isValid()) {
                    i.remove();
                }
            }
        }

        if (event != null) {
            pcs.firePropertyChange(event);
        }
    }

    private ProfileDescription reuseOrCreate(String id, String displayName, boolean rollback, String profileOrigin) {
        ProfileDescription desc = profiles.get(id);
        if (desc != null) {
            if (desc.getDisplayName().equals(displayName) && desc.isRollbackAllowed() == rollback) {
                return desc;
            }
        }
        return new ProfileDescription(id, displayName, rollback, profileOrigin);
    }
    
    private final class Listener extends FileChangeAdapter implements PropertyChangeListener {
        
        public Listener() {
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            rebuild();
        }
        
        @Override
        public void fileDataCreated(FileEvent fe) {
            notifyRebuild(fe.getFile());
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
        
        private void notifyRebuild(FileObject file) {
            String path = file.getPath();
            if (path.startsWith(mimeTypes.getBasePath())) {
                if (synchronous) rebuild();
                else task.schedule(1000);
            }
        }
    } // End of Listener class
}
