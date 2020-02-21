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

package org.netbeans.modules.cnd.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.utils.CndLanguageStandards.CndLanguageStandard;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.WeakSet;


/**
 * we use own manager to unify work with extensions and support default extension
 * + unfortunately implementation of FileUtil.getMIMETypeExtensions is extremely slow
 */
@MIMEResolver.Registration(displayName = "#ExtBasedResolver", position = 215, resource = "resources/mime-resolver-ext-based.xml") // NOI18N
@Messages({
    "ExtBasedResolver=Common C/C++ Extensions"
})
public final class MIMEExtensions {
    private static final String STANDARD_SUFFIX = "/standard"; //NOI18N
    private final static Preferences preferences = NbPreferences.forModule(MIMEExtensions.class);
    private final static Manager manager = new Manager();
    private final ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();
    private final WeakSet<ChangeListener> listeners = new WeakSet<ChangeListener>();

    @MIMEResolver.Registration(displayName="#CExtResolver", position=214, resource="resources/mime-resolver-ext-based-c.xml", // NOI18N
        showInFileChooser={"#FILECHOOSER_C_SOURCES_FILEFILTER"}) // NOI18N
    @Messages({
        "CExtResolver=C MIME Resolver (ext-based)",
        "FILECHOOSER_C_SOURCES_FILEFILTER=C Source Files"
    })
    public static MIMEExtensions get(String mimeType) {
        return manager.get(mimeType);
    }

    @MIMEResolver.Registration(displayName="#FortranExtResolver", position=219, resource="resources/mime-resolver-ext-based-fortran.xml", // NOI18N
        showInFileChooser={"#FILECHOOSER_FORTRAN_SOURCES_FILEFILTER"}) // NOI18N
    @Messages({
        "FortranExtResolver=Fortran MIME Resolver (ext-based)",
        "FILECHOOSER_FORTRAN_SOURCES_FILEFILTER=Fortran Source Files"
    })
    public static List<MIMEExtensions> getCustomizable() {
        return manager.getOrderedExtensions();
    }

    @MIMEResolver.Registration(displayName="#HExtResolver", position=217, resource="resources/mime-resolver-ext-based-h.xml", // NOI18N
        showInFileChooser={"#FILECHOOSER_HEADER_SOURCES_FILEFILTER"}) // NOI18N
    @Messages({
        "HExtResolver=H MIME Resolver (ext-based)",
        "FILECHOOSER_HEADER_SOURCES_FILEFILTER=Header Files"
    })
    public static boolean isCustomizableExtensions(String mimeType) {
        return get(mimeType) != null;
    }

    @MIMEResolver.Registration(displayName="#CCExtResolver", position=216, resource="resources/mime-resolver-ext-based-cpp.xml", // NOI18N
        showInFileChooser={"#FILECHOOSER_CC_SOURCES_FILEFILTER"}) // NOI18N
    @Messages({
        "CCExtResolver=C++ MIME Resolver (ext-based)",
        "FILECHOOSER_CC_SOURCES_FILEFILTER=C++ Source Files"
    })
    public static boolean isRegistered(String mimeType, String ext) {
        if (ext == null || ext.length() == 0) {
            return false;
        }
        // try cache
        MIMEExtensions out = get(mimeType);
        if (out == null) {
            return FileUtil.getMIMETypeExtensions(mimeType).contains(ext);
        } else {
            return out.contains(ext);
        }
    }
    /**
     * Add a listener to changes.
     * @param l a listener to add
     */
    @MIMEResolver.Registration(displayName="#ContentResolver", position=470, resource="resources/mime-resolver-content-based.xml") // NOI18N
    @Messages({
        "ContentResolver=H MIME Resolver (content-based)"
    })
    public void addChangeListener(ChangeListener l) {
        listenersLock.writeLock().lock();
        try {
            listeners.add(l);
        } finally {
            listenersLock.writeLock().unlock();
        }
    }

    /**
     * Stop listening to changes.
     * @param l a listener to remove
     */
    @MIMEResolver.Registration(displayName = "#QtNameExtResolver.Name", position = 218, resource = "resources/mime-resolver-ext-based-qt.xml", // NOI18N
        showInFileChooser={"#QtNameExtResolver.FileChooserName"}) // NOI18N
    @Messages({
        "QtNameExtResolver.Name=Qt Files",
        "QtNameExtResolver.FileChooserName=Qt Files"
    })
    public void removeChangeListener(ChangeListener l) {
        listenersLock.writeLock().lock();
        try {
            listeners.remove(l);
        } finally {
            listenersLock.writeLock().unlock();
        }
    }
    
    private void fireChange() {
        ArrayList<ChangeListener> list = new ArrayList<ChangeListener>();
        listenersLock.readLock().lock();
        try {
            list.addAll(listeners);
        } finally {
            listenersLock.readLock().unlock();
        }
        ChangeEvent changeEvent = new ChangeEvent(this);
        for(ChangeListener listener : list) {
            try {
                listener.stateChanged(changeEvent);
            } catch (RuntimeException x) {
                Exceptions.printStackTrace(x);
            }
        }
    }

    private final String mimeType;
    private final String description;
    private final Set<String> exts;

    private MIMEExtensions(String mimeType, String description) {
        this.mimeType = mimeType;
        this.description = description;
        exts = new TreeSet<String>();
        exts.addAll(FileUtil.getMIMETypeExtensions(mimeType));
    }

    private MIMEExtensions(String mimeType, MIMEExtensions primary) {
        // own
        this.mimeType = mimeType;
        // share
        this.description = primary.description;
        exts = primary.exts;
    }
    /**
     * assign extensions and default one to specified mime type
     * @param newExts extensions associated with mimeType
     * @param defaultExt default extension for mimeType
     * @throws IllegalArgumentException if input list doesn't contain default extension
     */
    @MIMEResolver.Registration(displayName="#NameExtResolver", position=138, resource="resources/mime-resolver-name-ext.xml") // NOI18N
    @Messages({
        "NameExtResolver=C Make Files"
    })
    public void setExtensions(List<String> newExts, String defaultExt) {
        if (newExts.isEmpty()) {
            return;
        }
        if (!newExts.contains(defaultExt)) {
            throw new IllegalArgumentException("input list " + newExts + " doesn't contain default element:" + defaultExt); // NOI18N
        }
        Collection<String> old = getValues();
        List<String> toRemove = new ArrayList<String>(old);
        toRemove.removeAll(newExts);
        List<String> toAdd = new ArrayList<String>(newExts);
        toAdd.removeAll(old);
        // TODO: do we need isSystemCaseInsensitive() check?
        for (String ext : toRemove) {
            FileUtil.setMIMEType(ext, null);
        }
        for (String ext : toAdd) {
            FileUtil.setMIMEType(ext, mimeType);
        }
        if (!toRemove.isEmpty() || !toAdd.isEmpty()) {
            exts.clear();
            exts.addAll(newExts);
            fireChange();
        }
        preferences.put(getMIMEType(), defaultExt);
    }

    @MIMEResolver.Registration(displayName="#HexBasedResolver", position=500, resource="resources/mime-resolver-hex-based.xml") // NOI18N
    @Messages({
        "HexBasedResolver=Magic C/C++ Headers"
    })
    public String getMIMEType() {
        return mimeType;
    }
    
    @MIMEResolver.Registration(displayName="#MakeResolver.Name", position=140, resource="resources/mime-resolver-make.xml", // NOI18N
        showInFileChooser="#MakeResolver.FileChooserName") // NOI18N
    @Messages({
        "MakeResolver.Name=Makefile Resolver",
        "MakeResolver.FileChooserName=Makefiles"
    })
    public String getDefaultExtension() {
        String defaultExt = preferences.get(getMIMEType(), "");
        if (defaultExt.length() == 0) {
            Collection<String> vals = getValues();
            return vals.isEmpty() ? "" : vals.iterator().next(); // NOI18N
        } else {
            return defaultExt;
        }
    }

    @MIMEResolver.Registration(displayName="#ShellResolver", position=139, resource="resources/mime-resolver.xml")
    @Messages({
        "ShellResolver=Common Shell Extensions"
    })
    public CndLanguageStandard getDefaultStandard() {
        return CndLanguageStandards.StringToLanguageStandard(preferences.get(getMIMEType()+STANDARD_SUFFIX, ""));
    }

    public String getLocalizedDescription() {
        return description;
    }

    public Collection<String> getValues() {
        return Collections.unmodifiableSet(exts);
    }

    private boolean contains(String ext) {
        return exts.contains(ext);
    }
    
    @Override
    public String toString() {
        return description + "[" + mimeType + ":" + getDefaultExtension() + "]"; // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MIMEExtensions other = (MIMEExtensions) obj;
        return this.mimeType.equals(other.mimeType);
    }

    @Override
    public int hashCode() {
        int hash = this.mimeType.hashCode();
        return hash;
    }

    public void addExtension(String ext) {
        if (!getValues().contains(ext)) {
            exts.add(ext);
            FileUtil.setMIMEType(ext, mimeType);
        }
    }

    public void setDefaultExtension(String defaultExt) {
        addExtension(defaultExt);
        preferences.put(getMIMEType(), defaultExt);
    }

    public void setDefaultStandard(CndLanguageStandard standard) {
        String oldValue = preferences.get(getMIMEType()+STANDARD_SUFFIX, ""); //NOI18N
        String newValue;
        if (standard != null) {
            newValue = standard.getID();
        } else {
            newValue = "";  //NOI18N
        }
        preferences.put(getMIMEType()+STANDARD_SUFFIX, newValue);
        if (!oldValue.equals(newValue)) {
            fireChange();
        }
    }

    private static class Manager {
        private final Map<String, MIMEExtensions> mime2ext = new LinkedHashMap<String, MIMEExtensions>(5);
        private final FileObject configFolder;
        private final FileChangeListener listener;

        private Manager() {
            configFolder = FileUtil.getConfigFile("CND/Extensions"); // NOI18N
            if (configFolder != null) {
                listener = new L();
                configFolder.addFileChangeListener(FileUtil.weakFileChangeListener(listener, configFolder));
                initialize(configFolder);
            } else {
                listener = null;
            }
        }

        public MIMEExtensions get(String mimeType) {
            MIMEExtensions out = mime2ext.get(mimeType);
            if (out == null) {
                out = new MIMEExtensions(mimeType, "DEFAULT " + mimeType); // NOI18N
                mime2ext.put(mimeType, out);
            }
            return out;
        }

        public List<MIMEExtensions> getOrderedExtensions() {
            Map<String, MIMEExtensions> out = new LinkedHashMap<String, MIMEExtensions>(mime2ext);
            out.remove(MIMENames.SHELL_MIME_TYPE);
            out.remove(MIMENames.C_HEADER_MIME_TYPE);
            return new ArrayList<MIMEExtensions>(out.values());
        }
        
        private void initialize(FileObject configFolder) {
            mime2ext.clear();
            if (configFolder != null) {
                for (FileObject fo : FileUtil.getOrder(Arrays.asList(configFolder.getChildren()), false)) {
                    MIMEExtensions data = create(fo);
                    if (!mime2ext.containsKey(data.getMIMEType())) {
                        mime2ext.put(data.getMIMEType(), data);
                        if (MIMENames.HEADER_MIME_TYPE.equals(data.getMIMEType())) {
                            MIMEExtensions cHeader = new MIMEExtensions(MIMENames.C_HEADER_MIME_TYPE, data);
                            // check if newly created or already has custom value in prefs
                            String defExt = preferences.get(MIMENames.C_HEADER_MIME_TYPE, ""); // NOI18N
                            if (defExt.length() == 0) {
                                // for newly created use normal headers extension
                                cHeader.setDefaultExtension(data.getDefaultExtension());
                            }
                            mime2ext.put(MIMENames.C_HEADER_MIME_TYPE, cHeader);
                        }
                    }
                }
                // also cache shell files
                MIMEExtensions shell = new MIMEExtensions(MIMENames.SHELL_MIME_TYPE, ""); // NOI18N
                mime2ext.put(MIMENames.SHELL_MIME_TYPE, shell);
            }
        }

        private MIMEExtensions create(FileObject configFile) throws MissingResourceException {
            Object attr = configFile.getAttribute("mimeType"); // NOI18N
            if (!(attr instanceof String)) {
                throw new MissingResourceException(configFile.getPath(), configFile.getClass().getName(), "no stringvalue attribute \"mimeType\""); // NOI18N
            }
            String mimeType = (String) attr;
            attr = configFile.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
            if (!(attr instanceof String)) {
                throw new MissingResourceException(configFile.getPath(), configFile.getClass().getName(), "no stringvalue attribute \"SystemFileSystem.localizingBundle\""); // NOI18N
            }
            ResourceBundle rb = NbBundle.getBundle((String) attr);
            String localizedName = rb.getString(configFile.getPath());
            attr = configFile.getAttribute("default"); // NOI18N
            if (attr != null && !(attr instanceof String)) {
                throw new MissingResourceException(configFile.getPath(), configFile.getClass().getName(), "no stringvalue attribute \"default\""); // NOI18N
            }
            String defaultExt = (String) (attr == null ? "" : attr); // NOI18N
            attr = configFile.getAttribute("standard"); // NOI18N
            if (attr != null && !(attr instanceof String)) {
                throw new MissingResourceException(configFile.getPath(), configFile.getClass().getName(), "no stringvalue attribute \"standard\""); // NOI18N
            }
            String standard = (String) (attr == null ? "" : attr); // NOI18N
            MIMEExtensions out = new MIMEExtensions(mimeType, localizedName);
            // default extension could be in preferences
            defaultExt = preferences.get(mimeType, defaultExt);
            out.setDefaultExtension(defaultExt);
            standard = preferences.get(mimeType+STANDARD_SUFFIX, standard);
            out.setDefaultStandard(CndLanguageStandards.StringToLanguageStandard(standard));
            return out;
        }
        // file change listener

        private final class L implements FileChangeListener {

            private L() {
            }

            @Override
            public void fileFolderCreated(FileEvent fe) {
                initialize(configFolder);
            }

            @Override
            public void fileDataCreated(FileEvent fe) {
                initialize(configFolder);
            }

            @Override
            public void fileChanged(FileEvent fe) {
                initialize(configFolder);
            }

            @Override
            public void fileDeleted(FileEvent fe) {
                initialize(configFolder);
            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
                initialize(configFolder);
            }

            @Override
            public void fileAttributeChanged(FileAttributeEvent fe) {
                initialize(configFolder);
            }
        }
    }
}
