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

package org.netbeans.spi.project.support.ant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.spi.intern.ProjectIDEServices;
import org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation.UserQuestionExceptionCallback;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.BaseUtilities;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;

/**
 * Manages the loaded property files for {@link AntProjectHelper}.
 * @author Jesse Glick
 */
final class ProjectProperties {

    /** Associated helper. */
    private final AntProjectHelper helper;
    
    /**
     * Properties loaded from metadata files on disk.
     * Keys are project-relative paths such as {@link #PROJECT_PROPERTIES_PATH}.
     * Values are loaded property providers.
     */
    private final Map<String,PP> properties = new HashMap<String,PP>();
    
    /** @see #getStockPropertyPreprovider */
    private PropertyProvider stockPropertyPreprovider = null;
    
    /** @see #getStandardPropertyEvaluator */
    private PropertyEvaluator standardPropertyEvaluator = null;
    
    /**
     * Create a project properties helper object.
     * @param helper the associated helper
     */
    public ProjectProperties(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    /**
     * Get properties from a given path.
     * @param path the project-relative path
     * @return the applicable properties (created if empty; never null)
     */
    public EditableProperties getProperties(String path) {
        EditableProperties ep = getPP(path).getEditablePropertiesOrNull();
        if (ep != null) {
            return ep.cloneProperties();
        } else {
            return new EditableProperties(true);
        }
    }
    
    /**
     * Store properties in memory.
     * @param path the project-relative path
     * @param props the new properties, or null to remove the properties file
     * @return true if an actual change was made
     */
    public boolean putProperties(String path, EditableProperties props) {
        return getPP(path).put(props);
    }
    
    /**
     * Write cached properties to disk.
     * @param the project-relative path
     * @throws IOException if the file could not be written
     */
    public FileLock write(String path) throws IOException {
        assert properties.containsKey(path);
        return getPP(path).write();
    }
    
    /**
     * Make a property provider that loads from this file
     * and fires changes when it is written to (even in memory).
     */
    public PropertyProvider getPropertyProvider(String path) {
        return getPP(path);
    }
    
    private PP getPP(String path) {
        PP pp = properties.get(path);
        if (pp == null) {
            pp = new PP(path, helper);
            properties.put(path, pp);
        }
        return pp;
    }
    
    private static final class PP implements PropertyProvider, FileChangeListener {
        
        private static final RequestProcessor RP = new RequestProcessor("ProjectProperties.PP.RP"); // NOI18N
        
        // XXX lock any loaded property files while the project is modified, to prevent manual editing,
        // and reload any modified files if the project is unmodified

        private final String path;
        private final AntProjectHelper helper;
        private EditableProperties properties = null;
        private boolean loaded = false;
        private Throwable reloadedStackTrace;
        private final ChangeSupport cs = new ChangeSupport(this);
        /** Atomic actions in use to save XML files. */
        private final Set<AtomicAction> saveActions = Collections.newSetFromMap(new WeakHashMap<>());
        private final AtomicBoolean fileListenerSet = new AtomicBoolean(false);
        
        //#239999 - preventing properties file from saving, when no changes are done

        private boolean filePropertiesChanged;
        private EditableProperties cachedPropertiesFromFile;
        
        public PP(String path, AntProjectHelper helper) {
            this.path = path;
            this.helper = helper;
        }

        private void lazyAttachListener() {
            if (fileListenerSet.compareAndSet(false, true)) {
                File fl = new File(FileUtil.toFile(dir()), path.replace('/', File.separatorChar));
                FileUtil.addFileChangeListener(this, FileUtil.normalizeFile(fl));
            }
        }
        
        private FileObject dir() {
            return helper.getProjectDirectory();
        }

        /**
         * Returns EditableProperties.
         * @return {@link EditableProperties} or null
         * Threading: called under shared lock
         */
        public EditableProperties getEditablePropertiesOrNull() {
            lazyAttachListener();
            if (!loaded) {
                properties = null;
                FileObject fo = dir().getFileObject(path);
                if (fo != null) {
                    try {
                        EditableProperties p;
                        InputStream is = fo.getInputStream();
                        try {
                            p = new EditableProperties(true);
                            p.load(is);
                        } finally {
                            is.close();
                        }
                        properties = p;
                        cachedPropertiesFromFile = p;
                    } catch (IOException e) {
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
                    }
                }
                loaded = true;
                reloadedStackTrace = null;
            }
            return properties;
        }

        /**
         * Sets EditableProperties.
         * @param nue the new {@link EditableProperties}
         * @return true if changed
         * Threading: called under exclusive lock
         */
        public boolean put(EditableProperties nue) {
            loaded = true;
            reloadedStackTrace = null;
            filePropertiesChanged = filePropertiesChanged ||
                !Objects.equals(nue, cachedPropertiesFromFile);
            boolean modifying = !BaseUtilities.compareObjects(nue, properties);
            if (modifying) {
                if (nue != null) {
                    properties = nue.cloneProperties();
                } else {
                    properties = null;
                }
                fireChange();
            }
            return modifying;
        }
        
        private void runSaveAA(AtomicAction action) throws IOException {
            synchronized (saveActions) {
                saveActions.add(action);
            }
            dir().getFileSystem().runAtomicAction(action);
        }

        /**
         * Stores properties.
         * @return the taken lock
         * @throws IOException
         * Threading: called under exclusive lock
         */
        public FileLock write() throws IOException {
            lazyAttachListener();            
            if (!loaded) {
                Logger.getLogger(ProjectProperties.class.getName()).log(Level.INFO, null,
                        new IOException("#167784: changes on disk for " + path + " in " + dir() + " clobbered by in-memory data").
                        initCause(reloadedStackTrace));
                loaded = true;
                reloadedStackTrace = null;
            }
            final FileObject f = dir().getFileObject(path);
            final FileLock[] _lock = new FileLock[1];
            try {
                if (properties != null && filePropertiesChanged) {
                    filePropertiesChanged = false;
                    // Supposed to create/modify the file.
                    // Need to use an atomic action - otherwise listeners will first
                    // receive an event that the file has been written to zero length
                    // (which for *.properties means no keys), which is wrong.
                    runSaveAA(new AtomicAction() {
                        public void run() throws IOException {
                            final FileObject _f;
                            if (f == null) {
                                _f = FileUtil.createData(dir(), path);
                                assert _f != null : "FU.cD must not return null; called on " + dir() + " + " + path; // #50802
                            } else {
                                _f = f;
                            }
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            properties.store(baos);
                            final byte[] data = baos.toByteArray();
                            try {
                                _lock[0] = _f.lock(); // released by {@link AntProjectHelper#save}
                                OutputStream os = _f.getOutputStream(_lock[0]);
                                try {
                                    os.write(data);
                                    cachedPropertiesFromFile = properties;
                                } finally {
                                    os.close();
                                }
                            } catch (IOException ioe) { // #46089
                                helper.needPendingHook();
                                
                                if(!ProjectIDEServices.isUserQuestionException(ioe)) {
                                    throw ioe;
                                }
                                ProjectIDEServices.handleUserQuestionException(ioe, new UserQuestionExceptionCallback() {
                                    public void accepted() {
                                        // Try again.
                                        try {
                                            runSaveAA(new AtomicAction() {
                                                public void run() throws IOException {
                                                    OutputStream os = _f.getOutputStream();
                                                    try {
                                                        os.write(data);
                                                        cachedPropertiesFromFile = properties;
                                                    } finally {
                                                        os.close();
                                                    }
                                                    helper.maybeCallPendingHook();
                                                }
                                            });
                                        } catch (IOException e) {
                                            // Oh well.
                                            Logger.getLogger(PP.this.getClass().getName()).log(Level.SEVERE, null, e);
                                            reload();
                                        }
                                    }
                                    public void denied() {
                                        reload();
                                    }
                                    public void error(IOException e) {
                                        Logger.getLogger(PP.this.getClass().getName()).log(Level.SEVERE, null, e);
                                        reload();
                                    }
                                    private void reload() {
                                        helper.cancelPendingHook();
                                        // Revert the save.
                                        diskChange(null);
                                    }
                                });
                            }
                        }
                    });
                } else if (properties == null) {
                    // We are supposed to remove any existing file.
                    if (f != null) {
                        f.delete();
                    }
                }
            } catch (IOException e) {
                if (_lock[0] != null) {
                    // Release it now, since no one else will.
                    _lock[0].releaseLock();
                }
                throw e;
            }
            return _lock[0];
        }
        
        public Map<String,String> getProperties() {
            Map<String,String> props = getEditablePropertiesOrNull();
            if (props != null) {
                return Collections.unmodifiableMap(props);
            } else {
                return Collections.emptyMap();
            }
        }
        
        public synchronized void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }
        
        public synchronized void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }
        
        private void fireChange() {
            if (!cs.hasListeners()) {
                return;
            }
            final Mutex.Action<Void> action = new Mutex.Action<Void>() {
                public Void run() {
                    cs.fireChange();
                    return null;
                }
            };
            if (ProjectManager.mutex().isWriteAccess()) {
                // Run it right now. postReadRequest would be too late.
                ProjectManager.mutex().readAccess(action);
            } else if (ProjectManager.mutex().isReadAccess()) {
                // Run immediately also. No need to switch to read access.
                action.run();
            } else {
                // Not safe to acquire a new lock, so run later in read access.
                RP.post(new Runnable() {
                    public void run() {
                        ProjectManager.mutex().readAccess(action);
                    }
                });
            }
        }
        
        private void diskChange(FileEvent fe) {
            boolean writing = false;
            if (fe != null) {
                synchronized (saveActions) {
                    for (AtomicAction a : saveActions) {
                        if (fe.firedFrom(a)) {
                            writing = true;
                            break;
                        }
                    }
                }
            }
            if (!writing) {
                loaded = false;
                reloadedStackTrace = new Throwable("noticed disk change here");
            }
            fireChange();
            if (!writing) {
                helper.fireExternalChange(path);
            }
        }

        public void fileFolderCreated(FileEvent fe) {
            diskChange(fe);
        }

        public void fileDataCreated(FileEvent fe) {
            diskChange(fe);
        }

        public void fileChanged(FileEvent fe) {
            diskChange(fe);
        }

        public void fileRenamed(FileRenameEvent fe) {
            diskChange(fe);
        }

        public void fileDeleted(FileEvent fe) {
            diskChange(fe);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {}

    }
    
    static File antJar;
    private static synchronized File antJar() {
        if (antJar == null) {
            antJar = InstalledFileLocator.getDefault().locate("ant/lib/ant.jar", "org.apache.tools.ant.module", false); // NOI18N
        }
        return antJar;
    }

    /**
     * See {@link AntProjectHelper#getStockPropertyPreprovider}.
     */
    @SuppressWarnings("SleepWhileInLoop")
    public PropertyProvider getStockPropertyPreprovider() {
        if (stockPropertyPreprovider == null) {
            Map<String,String> m = null;
            while (m == null) {
                try {
                    m = NbCollections.checkedMapByCopy(System.getProperties(), String.class, String.class, false);
                } catch (ConcurrentModificationException x) { // #194904, but do not synchronize (#212007)
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException x2) {}
                }
            }
            m.put("basedir", FileUtil.toFile(helper.getProjectDirectory()).getAbsolutePath()); // NOI18N
            File _antJar = antJar();
            if (_antJar != null) {
                File antHome = _antJar.getParentFile().getParentFile();
                m.put("ant.home", antHome.getAbsolutePath()); // NOI18N
                m.put("ant.core.lib", _antJar.getAbsolutePath()); // NOI18N
            }
            stockPropertyPreprovider = PropertyUtils.fixedPropertyProvider(m);
        }
        return stockPropertyPreprovider;
    }
    
    /**
     * See {@link AntProjectHelper#getStandardPropertyEvaluator}.
     */
    public PropertyEvaluator getStandardPropertyEvaluator() {
        if (standardPropertyEvaluator == null) {
            PropertyEvaluator findUserPropertiesFile = PropertyUtils.sequentialPropertyEvaluator(
                getStockPropertyPreprovider(),
                getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
            PropertyProvider globalProperties = PropertyUtils.userPropertiesProvider(findUserPropertiesFile,
                    "user.properties.file", FileUtil.toFile(helper.getProjectDirectory())); // NOI18N
            standardPropertyEvaluator = PropertyUtils.sequentialPropertyEvaluator(
                getStockPropertyPreprovider(),
                getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                helper.getProjectLibrariesPropertyProvider(),
                globalProperties,
                getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
        }
        return standardPropertyEvaluator;
    }
    
}
