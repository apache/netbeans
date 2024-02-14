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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.modules.project.ant.ProjectLibraryProvider;
import org.netbeans.modules.project.ant.ProjectXMLCatalogReader;
import org.netbeans.modules.project.ant.ProjectXMLKnownChecksums;
import org.netbeans.modules.project.spi.intern.ProjectIDEServices;
import org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation.UserQuestionExceptionCallback;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Support class for implementing Ant-based projects.
 * <p>As of 1.24, <code>project.xml</code> or <code>private.xml</code> reads or writes
 * are first validated against any registered XML schemas.
 * You must register a schema using the target namespace <code>http://www.netbeans.org/ns/foo/1</code>
 * as <code>ProjectXMLCatalog/foo/1.xsd</code> in your layer for it to be found.
 * @author Jesse Glick
 */
public final class AntProjectHelper {
    
    /**
     * Relative path from project directory to the customary shared properties file.
     */
    public static final String PROJECT_PROPERTIES_PATH = "nbproject/project.properties"; // NOI18N
    
    /**
     * Relative path from project directory to the customary private properties file.
     */
    public static final String PRIVATE_PROPERTIES_PATH = "nbproject/private/private.properties"; // NOI18N
    
    /**
     * Relative path from project directory to the required shared project metadata file.
     */
    public static final String PROJECT_XML_PATH = AntBasedProjectFactorySingleton.PROJECT_XML_PATH;
    
    /**
     * Relative path from project directory to the required private project metadata file.
     */
    public static final String PRIVATE_XML_PATH = "nbproject/private/private.xml"; // NOI18N
    
    /**
     * XML namespace of Ant projects.
     */
    static final String PROJECT_NS = AntBasedProjectFactorySingleton.PROJECT_NS;
    
    /**
     * XML namespace of private component of Ant projects.
     */
    static final String PRIVATE_NS = "http://www.netbeans.org/ns/project-private/1"; // NOI18N
    
    static {
        AntBasedProjectFactorySingleton.HELPER_CALLBACK = new AntBasedProjectFactorySingleton.AntProjectHelperCallback() {
            public AntProjectHelper createHelper(FileObject dir, Document projectXml, ProjectState state, AntBasedProjectType type) {
                return new AntProjectHelper(dir, projectXml, state, type);
            }
            public void save(AntProjectHelper helper) throws IOException {
                helper.save();
            }
        };
    }

    private static final Logger LOG = Logger.getLogger(AntProjectHelper.class.getName());
    
    private static RequestProcessor RP;
    
    /**
     * Project base directory.
     */
    private final FileObject dir;
    
    /**
     * State object permitting modifications.
     */
    private final ProjectState state;
    
    /**
     * Ant-based project type factory.
     */
    private final AntBasedProjectType type;

    /** Used as a marker that project/privateXml was not found at all, rather than found but malformed. */
    private static final Document NONEXISTENT = XMLUtil.createDocument("does-not-exist", null, null, null); // NOI18N

    /**
     * Cached project.xml parse (null if not loaded).
     * Access within {@link #modifiedMetadataPaths} monitor.
     */
    private Document projectXml;
    private boolean projectXmlValid;
    
    /**
     * Cached private.xml parse (null if not loaded).
     * Access within {@link #modifiedMetadataPaths} monitor.
     */
    private Document privateXml;
    private boolean privateXmlValid;
    
    /**
     * Set of relative paths to metadata files which have been modified
     * and which need to be saved.
     * Also server as a monitor for {@link #projectXml} and {@link #privateXml} accesses;
     * Xerces' DOM is not thread-safe <em>even for reading<em> (#50198).
     */
    private final Set<String> modifiedMetadataPaths = new HashSet<String>();
    private Throwable addedProjectXmlPath; // #155010
    
    /**
     * Registered listeners.
     * Access must be directly synchronized.
     */
    private final List<AntProjectListener> listeners = new ArrayList<AntProjectListener>();
    
    /**
     * List of loaded properties.
     */
    private final ProjectProperties properties;
    
    /** Listener to XML files; needs to be held as an instance field so it is not GC'd */
    private final FileChangeListener fileListener;
    private final AtomicBoolean fileListenerSet = new AtomicBoolean(false);
    
    
    /** Atomic actions in use to save XML files. */
    private final Set<AtomicAction> saveActions = new WeakSet<AtomicAction>();
    
    /**
     * Hook waiting to be called. See issue #57794.
     */
    private Collection<? extends ProjectXmlSavedHook> pendingHook = null;
    /**
     * Number of metadata files remaining to be written before {@link #pendingHook} can be called.
     * Javadoc for {@link ProjectXmlSavedHook} only guarantees that project.xml will be written,
     * but best to be safe and make sure also private.xml and *.properties are too.
     */
    private int pendingHookCount;
    
    // XXX lock any loaded XML files while the project is modified, to prevent manual editing,
    // and reload any modified files if the project is unmodified
    
    private AntProjectHelper(FileObject dir, Document projectXml, ProjectState state, AntBasedProjectType type) {
        this.dir = dir;
        assert dir != null && FileUtil.toFile(dir) != null;
        this.state = state;
        assert state != null;
        this.type = type;
        assert type != null;
        this.projectXml = projectXml;
        projectXmlValid = true;
        assert projectXml != null;
        properties = new ProjectProperties(this);
        fileListener = new FileListener();
    }

    private void lazyAttachFileListener() {
        if (fileListenerSet.compareAndSet(false, true)) {
            FileUtil.addFileChangeListener(fileListener, resolveFile(PROJECT_XML_PATH));
            FileUtil.addFileChangeListener(fileListener, resolveFile(PRIVATE_XML_PATH));
        }
    }
    
    /**
     * Get the corresponding Ant-based project type factory.
     */
    AntBasedProjectType getType() {
        return type;
    }

    /**
     * Retrieve project.xml or private.xml, loading from disk as needed.
     * private.xml is created as a skeleton on demand.
     */
    private Document getConfigurationXml(boolean shared) {
        assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        assert Thread.holdsLock(modifiedMetadataPaths);
        lazyAttachFileListener();
        if (!(shared ? projectXmlValid : privateXmlValid)) {
            String path = shared ? PROJECT_XML_PATH : PRIVATE_XML_PATH;
            Document _xml = loadXml(path);
            if (_xml != null && _xml != NONEXISTENT) {
                if (shared) {
                    projectXml = _xml;
                } else {
                    privateXml = _xml;
                }
            } else if (_xml == NONEXISTENT && !shared) {
                privateXml = null;
            }
        }
        if (!shared && privateXml == null) {
            // Missing or broken; create a skeleton.
            // (projectXml must have been given a valid value when APH was constructed.)
            privateXml = XMLUtil.createDocument("project-private", PRIVATE_NS, null, null); // NOI18N
        }
        // Mark valid even if had parse errors, so we do not try to reparse until corrected:
        if (shared) {
            projectXmlValid = true;
        } else {
            privateXmlValid = true;
        }
        Document xml = shared ? projectXml : privateXml;
        assert xml != null : "shared=" + shared + " projectXml=" + projectXml + " privateXml=" + privateXml + " projectXmlValid=" + projectXmlValid + " privateXmlValid=" + privateXmlValid;
        return xml;
    }
    
    /**
     * If true, do not report XML load errors.
     * For use only by unit tests.
     */
    static boolean QUIETLY_SWALLOW_XML_LOAD_ERRORS = false;
    
    /**
     * Try to load a config XML file from a named path.
     * If the file does not exist, return NONEXISTENT; or if there is any load error, return null.
     */
    private Document loadXml(String path) {
        assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        assert Thread.holdsLock(modifiedMetadataPaths);
        FileObject xml = dir.getFileObject(path);
        if (xml == null || !xml.isData()) {
            return NONEXISTENT;
        }
        File f = FileUtil.toFile(xml);
        assert f != null;
        try {
            Document doc = XMLUtil.parse(new InputSource(BaseUtilities.toURI(f).toString()), false, true, XMLUtil.defaultErrorHandler(), null);
            ProjectXMLCatalogReader.validate(doc.getDocumentElement());
            return doc;
        } catch (IOException e) {
            if (!QUIETLY_SWALLOW_XML_LOAD_ERRORS) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
            }
        } catch (SAXException e) {
            if (!QUIETLY_SWALLOW_XML_LOAD_ERRORS) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
            }
        }
        return null;
    }

    private void runSaveAA(AtomicAction action) throws IOException {
        synchronized (saveActions) {
            saveActions.add(action);
        }
        dir.getFileSystem().runAtomicAction(action);
    }
    /**
     * Save an XML config file to a named path.
     * If the file does not yet exist, it is created.
     */
    private FileLock saveXml(final Document doc, final String path) throws IOException {
        lazyAttachFileListener();
        assert ProjectManager.mutex().isWriteAccess();
        assert Thread.holdsLock(modifiedMetadataPaths);
        try {
            ProjectXMLCatalogReader.validate(doc.getDocumentElement());
        } catch (SAXException x) {
            Exceptions.attachMessage(x, "Saving " + path + " in " + FileUtil.getFileDisplayName(dir));
            throw (IOException) new IOException(x.getMessage()).initCause(x);
        }
        final FileLock[] _lock = new FileLock[1];
        runSaveAA(new AtomicAction() {
            public void run() throws IOException {
                // Keep a copy of xml *while holding modifiedMetadataPaths monitor*.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                XMLUtil.write(doc, baos, "UTF-8"); // NOI18N
                final byte[] data = baos.toByteArray();
                ProjectXMLKnownChecksums checksums = new ProjectXMLKnownChecksums(); // #195029
                if (!checksums.check(data)) {
                    checksums.save();
                }
                final FileObject xml = FileUtil.createData(dir, path);
                try {
                    _lock[0] = xml.lock(); // unlocked by {@link #save}
                    OutputStream os = xml.getOutputStream(_lock[0]);
                    try {
                        os.write(data);
                    } finally {
                        os.close();
                    }
                } catch (IOException ioe) { // #46089
                    if(!ProjectIDEServices.isUserQuestionException(ioe)) {
                        throw ioe;
                    }
                    
                    needPendingHook();
                    
                    ProjectIDEServices.handleUserQuestionException(ioe, new UserQuestionExceptionCallback() {
                        public void accepted() {
                            // Try again.
                            try {
                                runSaveAA(new AtomicAction() {
                                    public void run() throws IOException {
                                        OutputStream os = xml.getOutputStream();
                                        try {
                                            os.write(data);
                                        } finally {
                                            os.close();
                                        }
                                        maybeCallPendingHook();
                                    }
                                });
                            } catch (IOException e) {
                                // Oh well.
                                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                                reload();
                            }
                        }
                        public void denied() {
                            reload();
                        }
                        public void error(IOException e) {
                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                            reload();
                        }
                        private void reload() {
                            // Revert the save.
                            if (path.equals(PROJECT_XML_PATH)) {
                                synchronized (modifiedMetadataPaths) {
                                    projectXmlValid = false;
                                }
                            } else {
                                assert path.equals(PRIVATE_XML_PATH) : path;
                                synchronized (modifiedMetadataPaths) {
                                    privateXmlValid = false;
                                }
                            }
                            fireExternalChange(path);
                            cancelPendingHook();
                        }
                    });
                }
            }
        });
        return _lock[0];
    }
    
    /**
     * Get the <code>&lt;configuration&gt;</code> element of project.xml
     * or the document element of private.xml.
     * Beneath this point you can load and store configuration fragments.
     * @param shared if true, use project.xml, else private.xml
     * @return the data root
     */
    private Element getConfigurationDataRoot(boolean shared) {
        assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        assert Thread.holdsLock(modifiedMetadataPaths);
        Document doc = getConfigurationXml(shared);
        if (shared) {
            Element project = doc.getDocumentElement();
            Element config = XMLUtil.findElement(project, "configuration", PROJECT_NS); // NOI18N
            assert config != null;
            return config;
        } else {
            return doc.getDocumentElement();
        }
    }

    /**
     * Add a listener to changes in the project configuration.
     * <p>Thread-safe.
     * @param listener a listener to add
     */
    public void addAntProjectListener(AntProjectListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a listener to changes in the project configuration.
     * <p>Thread-safe.
     * @param listener a listener to remove
     */
    public void removeAntProjectListener(AntProjectListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Fire a change of external provenance to all listeners.
     * When run under read or write access to <code>ProjectManager.mutex()</code>
     * property change is fired synchronously, otherwise fire asynchronously
     * under acquired read lock.
     * @param path path to the changed file (XML or properties)
     */
    void fireExternalChange(final String path) {
        final Mutex.Action<Void> action = new ActionImpl(this, path);
        if (ProjectManager.mutex().isWriteAccess() || ProjectLibraryProvider.FIRE_CHANGES_SYNCH) {
            // Run it right now. postReadRequest would be too late.
            ProjectManager.mutex().readAccess(action);
        } else if (ProjectManager.mutex().isReadAccess()) {
            // Run immediately also. No need to switch to read access.
            action.run();
        } else {
            // Not safe to acquire a new lock, so run later in read access.
            rp().post(new RunnableImpl(action));
        }
    }
    private static synchronized RequestProcessor rp() {
        if (RP == null) {
            RP = new RequestProcessor("AntProjectHelper.RP"); // NOI18N
        }
        return RP;
    }

    /**
     * Fire a change to all listeners.
     * Must be called from write access; enters read access while firing.
     * @param path path to the changed file (XML or properties)
     * @param expected true if the result of an API-initiated change, false if from external causes
     */
    private void fireChange(String path, boolean expected) {
        assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        final AntProjectListener[] _listeners;
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            _listeners = listeners.toArray(new AntProjectListener[0]);
        }
        final AntProjectEvent ev = new AntProjectEvent(this, path, expected);
        final boolean xml = path.equals(PROJECT_XML_PATH) || path.equals(PRIVATE_XML_PATH);
        ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
            public Void run() {
                for (AntProjectListener l : _listeners) {
                    try {
                        if (xml) {
                            l.configurationXmlChanged(ev);
                        } else {
                            l.propertiesChanged(ev);
                        }
                    } catch (RuntimeException e) {
                        // Don't prevent other listeners from being notified.
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                    }
                }
                return null;
            }
        });
    }
    
    /**
     * Call when explicitly modifying some piece of metadata.
     */
    private void modifying(String path) {
        assert ProjectManager.mutex().isWriteAccess();
        state.markModified();
        addModifiedMetadataPath(path);
        fireChange(path, true);
    }
    private void addModifiedMetadataPath(String path) {
        synchronized (modifiedMetadataPaths) {
            boolean added = modifiedMetadataPaths.add(path);
            if (added && path.equals(PROJECT_XML_PATH)) {
                addedProjectXmlPath = new Throwable();
            }
        }
    }
    
    /**
     * Get the top-level project directory.
     * @return the project directory beneath which everything in the project lies
     */
    public FileObject getProjectDirectory() {
        return dir;
    }
    
    /**Notification that this project has been deleted.
     * @see org.netbeans.spi.project.ProjectState#notifyDeleted
     *
     * @since 1.8
     */
    public void notifyDeleted() {
        state.notifyDeleted();
    }
    
    
    /**
     * Mark this project as being modified without actually changing anything in it.
     * Should only be called from {@link ProjectGenerator#createProject}.
     */
    void markModified() {
        assert ProjectManager.mutex().isWriteAccess();
        state.markModified();
        // To make sure projectXmlSaved is called:
        addModifiedMetadataPath(PROJECT_XML_PATH);
    }
    
    /**
     * Check whether this project is currently modified including modifications
     * to <code>project.xml</code>.
     * Access from GeneratedFilesHelper.
     */
    void ensureProjectXmlUnmodified(String msg, boolean doSave) {
        assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        synchronized (modifiedMetadataPaths) {
        if (modifiedMetadataPaths.contains(PROJECT_XML_PATH)) {
            IllegalStateException ise = new IllegalStateException(msg);
            if (addedProjectXmlPath != null) {
                ise.initCause(addedProjectXmlPath);
            }
            LOG.log(Level.INFO, null, ise);
            if (doSave) {
                try {
                    save();
                } catch (IOException x) {
                    LOG.log(Level.INFO, null, x);
                }
            }
        }
        }
    }
    
    /**
     * Save all cached project metadata.
     * If <code>project.xml</code> was one of the modified files, then
     * {@link AntBasedProjectType#projectXmlSaved} is called, presumably
     * creating <code>build-impl.xml</code> and/or <code>build.xml</code>.
     */
    private void save() throws IOException {
        assert ProjectManager.mutex().isWriteAccess();
        if (!getProjectDirectory().isValid()) {
            //ProjectManager.saveProject() is called when project is deleted externally..
            return;
        }
        Set<FileLock> locks = new HashSet<FileLock>();
        try {
            synchronized (modifiedMetadataPaths) {
                assert !modifiedMetadataPaths.isEmpty();
                assert pendingHook == null;
                if (modifiedMetadataPaths.contains(PROJECT_XML_PATH)) {
                    // Saving project.xml so look for that hook.
                    Project p = AntBasedProjectFactorySingleton.getProjectFor(this);
                    pendingHook = p.getLookup().lookupAll(ProjectXmlSavedHook.class);
                    // might still be null
                }
                Set<String> toBeCleared = new HashSet<String>();
                try {
                for (String path : new TreeSet<String>(modifiedMetadataPaths)) {
                    try {
                        if (path.equals(PROJECT_XML_PATH)) {
                            assert projectXml != null;
                            locks.add(saveXml(projectXml, path));
                        } else if (path.equals(PRIVATE_XML_PATH)) {
                            assert privateXml != null;
                            locks.add(saveXml(privateXml, path));
                        } else {
                            // All else is assumed to be a properties file.
                            locks.add(properties.write(path));
                        }
                    } catch (FileAlreadyLockedException x) { // #155037
                        LOG.log(Level.INFO, null, x);
                    }
                    // As metadata files are saved, take them off the modified list.
                    toBeCleared.add(path);
                }
                } finally {
                    modifiedMetadataPaths.removeAll(toBeCleared);
                    LOG.log(Level.FINE, "saved {0} and have left {1}", new Object[] {toBeCleared, modifiedMetadataPaths});
                }
                if (pendingHook != null && pendingHookCount == 0) {
                    try {
                        for (ProjectXmlSavedHook hook : pendingHook) {
                            hook.projectXmlSaved();
                        }
                    } catch (IOException e) {
                        // Treat it as still modified.
                        addModifiedMetadataPath(PROJECT_XML_PATH);
                        throw e;
                    }
                }
                if (pendingHookCount == 0) {
                    pendingHook = null;
                }
            }
        } finally {
            // #57791: release locks outside synchronized block.
            locks.remove(null);
            for (FileLock lock : locks) {
                lock.releaseLock();
            }
            // More #57794.
            if (pendingHookCount == 0) {
                pendingHook = null;
            }
        }
    }
    
    /** See issue #57794. */
    void maybeCallPendingHook() {
        // XXX synchronization of this method?
        assert pendingHookCount > 0;
        pendingHookCount--;
        //#67465: the pendingHook may be null if project.xml is not being written
        //eg. only project.properties is being saved:
        if (pendingHookCount == 0 && pendingHook != null) {
            try {
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                    public Void run() throws IOException {
                        for (ProjectXmlSavedHook hook : pendingHook) {
                            hook.projectXmlSaved();
                        }
                        return null;
                    }
                });
            } catch (MutexException e) {
                // XXX mark project modified again??
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            } finally {
                pendingHook = null;
            }
        }
    }
    void cancelPendingHook() {
        assert pendingHookCount > 0;
        pendingHookCount--;
        if (pendingHookCount == 0) {
            pendingHook = null;
        }
    }
    void needPendingHook() {
        pendingHookCount++;
    }
    
    /**
     * Load a property file from some location in the project.
     * The returned object may be edited but you must call {@link #putProperties}
     * to save any changes you make.
     * If the file does not (yet) exist or could not be loaded for whatever reason,
     * an empty properties list is returned instead.
     * @param path a relative URI in the project directory, e.g.
     *             {@link #PROJECT_PROPERTIES_PATH} or {@link #PRIVATE_PROPERTIES_PATH}
     * @return a set of properties
     */
    public EditableProperties getProperties(@NonNull final String path) {
        if (AntProjectHelper.PROJECT_XML_PATH.equals(path) || AntProjectHelper.PRIVATE_XML_PATH.equals(path)) {
            throw new IllegalArgumentException("Attempt to load properties from a project XML file"); // NOI18N
        }
        return ProjectManager.mutex().readAccess(new Mutex.Action<EditableProperties>() {
            @Override
            public EditableProperties run() {
                return properties.getProperties(path);
            }
        });
    }
    
    /**
     * Store a property file to some location in the project.
     * A clone will be made of the supplied properties file so as to snapshot it.
     * The new properties are not actually stored to disk immediately, but the project
     * is marked modified so that they will be later.
     * You can store to a path that does not yet exist and the file will be created
     * if and when the project is saved.
     * If the old value is the same as the new, nothing is done.
     * Otherwise an expected properties change event is fired.
     * <p>Acquires write access from {@link ProjectManager#mutex}. However, you are well
     * advised to explicitly enclose a <em>complete</em> operation within write access,
     * starting with {@link #getProperties}, to prevent race conditions.
     * @param path a relative URI in the project directory, e.g.
     *             {@link #PROJECT_PROPERTIES_PATH} or {@link #PRIVATE_PROPERTIES_PATH}
     * @param props a set of properties to store, or null to delete any existing properties file there
     */
    public void putProperties(@NonNull final String path, final EditableProperties props) {
        if (AntProjectHelper.PROJECT_XML_PATH.equals(path) || AntProjectHelper.PRIVATE_XML_PATH.equals(path)) {
            throw new IllegalArgumentException("Attempt to store properties from a project XML file"); // NOI18N
        }
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                if (properties.putProperties(path, props)) {
                    modifying(path);
                }
                return null;
            }
        });
    }
    
    /**
     * Get a property provider that works with loadable project properties.
     * Its current values should match {@link #getProperties}, and calls to
     * {@link #putProperties} should cause it to fire changes.
     * @param path a relative URI in the project directory, e.g.
     *             {@link #PROJECT_PROPERTIES_PATH} or {@link #PRIVATE_PROPERTIES_PATH}
     * @return a property provider implementation
     */
    public PropertyProvider getPropertyProvider(final String path) {
        if (path.equals(AntProjectHelper.PROJECT_XML_PATH) || path.equals(AntProjectHelper.PRIVATE_XML_PATH)) {
            throw new IllegalArgumentException("Attempt to store properties from a project XML file"); // NOI18N
        }
        return ProjectManager.mutex().readAccess(new Mutex.Action<PropertyProvider>() {
            public PropertyProvider run() {
                return properties.getPropertyProvider(path);
            }
        });
    }
    
    /**
     * Get the primary configuration data for this project.
     * The returned element will be named according to
     * {@link AntBasedProjectType#getPrimaryConfigurationDataElementName} and
     * {@link AntBasedProjectType#getPrimaryConfigurationDataElementNamespace}.
     * The project may read this document fragment to get custom information
     * from <code>nbproject/project.xml</code> and <code>nbproject/private/private.xml</code>.
     * The fragment will have no parent node and while it may be modified, you must
     * use {@link #putPrimaryConfigurationData} to store any changes.
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>
     * @return the configuration data that is available
     */
    public Element getPrimaryConfigurationData(final boolean shared) {
        final String name = type.getPrimaryConfigurationDataElementName(shared);
        assert name.indexOf(':') == -1;
        final String namespace = type.getPrimaryConfigurationDataElementNamespace(shared);
        assert namespace != null && namespace.length() > 0;
        return ProjectManager.mutex().readAccess(new Mutex.Action<Element>() {
            public Element run() {
                synchronized (modifiedMetadataPaths) {
                    Element el = getConfigurationFragment(name, namespace, shared);
                    if (el != null) {
                        return el;
                    } else {
                        // No such data, corrupt file.
                        return cloneSafely(getConfigurationXml(shared).createElementNS(namespace, name));
                    }
                }
            }
        });
    }
    
    /**
     * Store the primary configuration data for this project.
     * The supplied element must be named according to
     * {@link AntBasedProjectType#getPrimaryConfigurationDataElementName} and
     * {@link AntBasedProjectType#getPrimaryConfigurationDataElementNamespace}.
     * The project may save this document fragment to set custom information
     * in <code>nbproject/project.xml</code> and <code>nbproject/private/private.xml</code>.
     * The fragment will be cloned and so further modifications will have no effect.
     * <p>Acquires write access from {@link ProjectManager#mutex}. However, you are well
     * advised to explicitly enclose a <em>complete</em> operation within write access,
     * starting with {@link #getPrimaryConfigurationData}, to prevent race conditions.
     * @param data the desired new configuration data
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>
     * @throws IllegalArgumentException if the element is not correctly named
     */
    public void putPrimaryConfigurationData(Element data, boolean shared) throws IllegalArgumentException {
        String name = type.getPrimaryConfigurationDataElementName(shared);
        assert name.indexOf(':') == -1;
        String namespace = type.getPrimaryConfigurationDataElementNamespace(shared);
        assert namespace != null && namespace.length() > 0;
        if (!name.equals(data.getLocalName()) || !namespace.equals(data.getNamespaceURI())) {
            throw new IllegalArgumentException("Wrong name/namespace: expected {" + namespace + "}" + name + " but was {" + data.getNamespaceURI() + "}" + data.getLocalName()); // NOI18N
        }
        putConfigurationFragment(data, shared);
    }
    
    private final class FileListener implements FileChangeListener {
        
        public FileListener() {}
        
        private void change(FileEvent fe) {
            synchronized (saveActions) {
                for (AtomicAction a : saveActions) {
                    if (fe.firedFrom(a)) {
                        return;
                    }
                }
            }
            String path;
            File f = FileUtil.toFile(fe.getFile());
            synchronized (modifiedMetadataPaths) {
                if (f.equals(resolveFile(PROJECT_XML_PATH))) {
                    if (modifiedMetadataPaths.contains(PROJECT_XML_PATH)) {
                        //#68872: don't do anything if the given file has non-saved changes:
                        return ;
                    }
                    path = PROJECT_XML_PATH;
                    projectXmlValid = false;
                } else if (f.equals(resolveFile(PRIVATE_XML_PATH))) {
                    if (modifiedMetadataPaths.contains(PRIVATE_XML_PATH)) {
                        //#68872: don't do anything if the given file has non-saved changes:
                        return ;
                    }
                    path = PRIVATE_XML_PATH;
                    privateXmlValid = false;
                } else {
                    LOG.log(Level.WARNING, "#184132: unexpected file change in {0}; possibly deleted project?", f);
                    return;
                }
            }
            fireExternalChange(path);
        }

        public void fileFolderCreated(FileEvent fe) {
            change(fe);
        }

        public void fileDataCreated(FileEvent fe) {
            change(fe);
        }

        public void fileChanged(FileEvent fe) {
            change(fe);
        }

        public void fileDeleted(FileEvent fe) {
            change(fe);
        }

        public void fileRenamed(FileRenameEvent fe) {
            change(fe);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }
    }
    
    /**
     * Get a piece of the configuration subtree by name.
     * @param elementName the simple XML element name expected
     * @param namespace the XML namespace expected
     * @param shared to use project.xml vs. private.xml
     * @return (a clone of) the named configuration fragment, or null if it does not exist
     */
    Element getConfigurationFragment(final String elementName, final String namespace, final boolean shared) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Element>() {
            public Element run() {
                synchronized (modifiedMetadataPaths) {
                    Element root = getConfigurationDataRoot(shared);
                    Element data = XMLUtil.findElement(root, elementName, namespace);
                    if (data != null) {
                        return cloneSafely(data);
                    } else {
                        return null;
                    }
                }
            }
        });
    }
    
    private static final DocumentBuilder db;
    static {
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }
    private static Element cloneSafely(Element el) {
        // #50198: for thread safety, use a separate document.
        // Using XMLUtil.createDocument is much too slow.
        synchronized (db) {
            Document dummy = db.newDocument();
            return (Element) dummy.importNode(el, true);
        }
    }
    
    /**
     * Store a piece of the configuration subtree by name.
     * @param fragment a piece of the subtree to store (overwrite or add)
     * @param shared to use project.xml vs. private.xml
     */
    void putConfigurationFragment(final Element fragment, final boolean shared) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                synchronized (modifiedMetadataPaths) {
                    Element root = getConfigurationDataRoot(shared);
                    Element existing = XMLUtil.findElement(root, fragment.getLocalName(), fragment.getNamespaceURI());
                    // XXX first compare to existing and return if the same
                    if (existing != null) {
                        root.removeChild(existing);
                    }
                    // the children are alphabetize: find correct place to insert new node
                    Node ref = null;
                    NodeList list = root.getChildNodes();
                    for (int i=0; i<list.getLength(); i++) {
                        Node node  = list.item(i);
                        if (node.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        int comparison = node.getNodeName().compareTo(fragment.getNodeName());
                        if (comparison == 0) {
                            comparison = node.getNamespaceURI().compareTo(fragment.getNamespaceURI());
                        }
                        if (comparison > 0) {
                            ref = node;
                            break;
                        }
                    }
                    root.insertBefore(root.getOwnerDocument().importNode(fragment, true), ref);
                    modifying(shared ? PROJECT_XML_PATH : PRIVATE_XML_PATH);
                }
                return null;
            }
        });
    }
    
    /**
     * Remove a piece of the configuration subtree by name.
     * @param elementName the simple XML element name expected
     * @param namespace the XML namespace expected
     * @param shared to use project.xml vs. private.xml
     * @return true if anything was actually removed
     */
    boolean removeConfigurationFragment(final String elementName, final String namespace, final boolean shared) {
        return ProjectManager.mutex().writeAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                synchronized (modifiedMetadataPaths) {
                    Element root = null;
                    Element data = null;
                    try {
                        root = getConfigurationDataRoot(shared);
                        data = XMLUtil.findElement(root, elementName, namespace);
                    } catch (IllegalArgumentException iae) {
                        //thrown from XmlUtil.findElement when more than 1 equal elements are present.
                        LOG.log(Level.INFO, iae.getMessage(), iae);
                    }
                    if(shared) {
                        findDuplicateElements(projectXml.getDocumentElement(), dir.getFileObject(PROJECT_XML_PATH), shared);
                    } else {
                        findDuplicateElements(privateXml.getDocumentElement(), dir.getFileObject(PRIVATE_XML_PATH), shared);
                    }
                    if (data != null) {
                        root.removeChild(data);
                        modifying(shared ? PROJECT_XML_PATH : PRIVATE_XML_PATH);
                        return true;
                    } else {
                        return false;
                    }
                    
                }
            }
        });
    }
    
    /**
     * Create an object permitting this project to store auxiliary configuration.
     * Would be placed into the project's lookup.
     * @return an auxiliary configuration provider object suitable for the project lookup
     */
    public AuxiliaryConfiguration createAuxiliaryConfiguration() {
        return new ExtensibleMetadataProviderImpl(this);
    }
    
    /**
     * Create an object permitting this project to expose a cache directory.
     * Would be placed into the project's lookup.
     * @return a cache directory provider object suitable for the project lookup
     */
    public CacheDirectoryProvider createCacheDirectoryProvider() {
        return new ExtensibleMetadataProviderImpl(this);
    }
    
    /**
     * Create an object permitting this project to expose {@link AuxiliaryProperties}.
     * Would be placed into the project's lookup.
     * 
     * This implementation places the properties into {@link #PROJECT_PROPERTIES_PATH}
     * or {@link #PRIVATE_PROPERTIES_PATH} (depending on shared value). The properties are
     * prefixed with "<code>auxiliary.</code>".
     * 
     * @return an instance of {@link AuxiliaryProperties} suitable for the project lookup
     * @since 1.21
     */
    public AuxiliaryProperties createAuxiliaryProperties() {
        return new AuxiliaryPropertiesImpl(this);
    }
    
    /**
     * Create an implementation of {@link org.netbeans.api.queries.FileBuiltQuery} that works with files
     * within the project based on simple glob pattern mappings.
     * <p>
     * It is intended to be
     * placed in {@link org.netbeans.api.project.Project#getLookup}.
     * <p>
     * It will return status objects for any files in the project matching a source
     * glob pattern - this must include exactly one asterisk (<code>*</code>)
     * representing a variable portion of a source file path (always slash-separated
     * and relative to the project directory) and may include some Ant property
     * references which will be resolved as per the property evaluator.
     * A file is considered out of date if there is no file represented by the
     * matching target pattern (which has the same format), or the target file is older
     * than the source file, or the source file is modified as per
     * <a href="@org-openide-loaders@/org/openide/loaders/DataObject.html#isModified--" >DataObject#isModified()</a>.
     * An attempt is made to fire changes from the status object whenever the result
     * should change from one call to the next.
     * <p>
     * The (evaluated) source and target patterns may be relative, resolved against
     * the project directory (perhaps going outside it), or absolute.
     * </p>
     * <div class="nonnormative">
     * <p>
     * A typical set of source and target patterns would be:
     * </p>
     * <ol>
     * <li><em>${src.dir}/*.java</em>
     * <li><em>${test.src.dir}/*.java</em>
     * </ol>
     * <ol>
     * <li><em>${build.classes.dir}/*.class</em>
     * <li><em>${test.build.classes.dir}/*.class</em>
     * </ol>
     * </div>
     * @param eval a property evaluator to interpret the patterns with
     * @param from a list of glob patterns for source files
     * @param to a matching list of glob patterns for built files
     * @return a query implementation
     * @throws IllegalArgumentException if either from or to patterns
     *                                  have zero or multiple asterisks,
     *                                  or the arrays are not of equal lengths
     */
    public FileBuiltQueryImplementation createGlobFileBuiltQuery(PropertyEvaluator eval, String[] from, String[] to) throws IllegalArgumentException {
        return new GlobFileBuiltQuery(this, eval, from, to);
    }

    /**
     * Create a basic implementation of {@link AntArtifact} which assumes everything of interest
     * is in a fixed location under a standard Ant-based project.
     * @param type the type of artifact, e.g. <a href="@org-netbeans-modules-java-project@/org/netbeans/api/java/project/JavaProjectConstants.html#ARTIFACT_TYPE_JAR"><code>JavaProjectConstants.ARTIFACT_TYPE_JAR</code></a>
     * @param locationProperty an Ant property name giving the project-relative
     *                         location of the artifact, e.g. <em>dist.jar</em>
     * @param eval a way to evaluate the location property (e.g. {@link #getStandardPropertyEvaluator})
     * @param targetName the name of an Ant target which will build the artifact,
     *                   e.g. <em>jar</em>
     * @param cleanTargetName the name of an Ant target which will delete the artifact
     *                        (and maybe other build products), e.g. <em>clean</em>
     * @return an artifact
     */
    public AntArtifact createSimpleAntArtifact(String type, String locationProperty, PropertyEvaluator eval, String targetName, String cleanTargetName) {
        return createSimpleAntArtifact(type, locationProperty, eval, targetName, cleanTargetName, null);
    }
    
    /**
     * Create a basic implementation of {@link AntArtifact} which assumes everything of interest
     * is in a fixed location under a standard Ant-based project.
     * @param type the type of artifact, e.g. <a href="@org-netbeans-modules-java-project@/org/netbeans/api/java/project/JavaProjectConstants.html#ARTIFACT_TYPE_JAR"><code>JavaProjectConstants.ARTIFACT_TYPE_JAR</code></a>
     * @param locationProperty an Ant property name giving the project-relative
     *                         location of the artifact, e.g. <em>dist.jar</em>
     * @param eval a way to evaluate the location property (e.g. {@link #getStandardPropertyEvaluator})
     * @param targetName the name of an Ant target which will build the artifact,
     *                   e.g. <em>jar</em>
     * @param cleanTargetName the name of an Ant target which will delete the artifact
     *                        (and maybe other build products), e.g. <em>clean</em>
     * @param buildScriptProperty an Ant property name giving the project-relative
     *      location and name of the build.xml or null if default one (<em>build.xml</em>) 
     *      should be used; default value is also used if property is given but its value is null
     * @return an artifact
     * @since org.netbeans.modules.project.ant/1 1.25
     */
    public AntArtifact createSimpleAntArtifact(String type, String locationProperty, PropertyEvaluator eval, 
            String targetName, String cleanTargetName, String buildScriptProperty) {
        return new SimpleAntArtifact(this, type, locationProperty, eval, targetName, cleanTargetName, buildScriptProperty);
    }
    
    /**
     * Create an implementation of the file sharability query.
     * You may specify a list of source roots to include that should be considered sharable,
     * as well as a list of build directories that should not be considered sharable.
     * <p>
     * The project directory itself is automatically included in the list of sharable directories
     * so you need not explicitly specify it.
     * Similarly, the <code>nbproject/private</code> subdirectory is automatically excluded
     * from VCS, so you do not need to explicitly specify it.
     * </p>
     * <p>
     * Any file (or directory) mentioned (explicitly or implicity) in the source
     * directory list but not in any of the build directory lists, and not containing
     * any build directories inside it, will be given as sharable. If a directory itself
     * is sharable but some directory inside it is not, it will be given as mixed.
     * A file or directory inside some build directory will be listed as not sharable.
     * A file or directory matching neither the source list nor the build directory list
     * will be treated as of unknown status, but in practice such a file should never
     * have been passed to this implementation anyway - {@link org.netbeans.api.queries.SharabilityQuery} will
     * normally only call an implementation in project lookup if the file is owned by
     * that project.
     * </p>
     * <p>
     * Each entry in either list should be a string evaluated first for Ant property
     * escapes (if any), then treated as a file path relative to the project directory
     * (or it may be absolute).
     * </p>
     * <p>
     * It is permitted, and harmless, to include items that overlap others. For example,
     * you can have both a directory and one of its children in the include list.
     * </p>
     * <p>
     * Whether or not you use this method, all files named <code>*-private.properties</code>
     * outside the project are marked unsharable, as are such files inside the project if currently referenced
     * as project libraries. (See {@link #getProjectLibrariesPropertyProvider}.)
     * </p>
     * <div class="nonnormative">
     * <p>
     * Typical usage would be:
     * </p>
     * <pre>
     * helper.createSharabilityQuery2(helper.getStandardPropertyEvaluator(),
     *                                new String[] {"${src.dir}", "${test.src.dir}"},
     *                                new String[] {"${build.dir}", "${dist.dir}"})
     * </pre>
     * <p>
     * A quick rule of thumb is that the include list should contain any
     * source directories which <em>might</em> reside outside the project directory;
     * and the exclude list should contain any directories which you would want
     * to add to a <em>.cvsignore</em> file if using CVS (for example).
     * </p>
     * <p>
     * Note that in this case <em>${src.dir}</em> and <em>${test.src.dir}</em>
     * may be relative paths inside the project directory; relative paths pointing
     * outside of the project directory; or absolute paths (generally outside of the
     * project directory). If they refer to locations inside the project directory,
     * including them does nothing but is harmless - since the project directory itself
     * is always treated as sharable. If they refer to external locations, you will
     * need to also make sure that {@link org.netbeans.api.project.FileOwnerQuery} actually maps files in those
     * directories to this project, or else {@link org.netbeans.api.queries.SharabilityQuery} will never find
     * this implementation in your project lookup and may return <code>UNKNOWN</code>.
     * </p>
     * </div>
     * @param eval a property evaluator to interpret paths with
     * @param sourceRoots a list of additional paths to treat as sharable
     * @param buildDirectories a list of paths to treat as not sharable
     * @return a sharability query implementation suitable for the project lookup
     * @see Project#getLookup
     * @since 1.47
     */
    public SharabilityQueryImplementation2 createSharabilityQuery2(PropertyEvaluator eval, String[] sourceRoots, String[] buildDirectories) {
        String[] includes = new String[sourceRoots.length + 1];
        System.arraycopy(sourceRoots, 0, includes, 0, sourceRoots.length);
        includes[sourceRoots.length] = ""; // NOI18N
        String[] excludes = new String[buildDirectories.length + 1];
        System.arraycopy(buildDirectories, 0, excludes, 0, buildDirectories.length);
        excludes[buildDirectories.length] = "nbproject/private"; // NOI18N
        return new SharabilityQueryImpl(this, eval, includes, excludes);
    }
    /**
     * @deprecated since 1.47 use {@link #createSharabilityQuery2} instead
     */
    @Deprecated
    public SharabilityQueryImplementation createSharabilityQuery(PropertyEvaluator eval, String[] sourceRoots, String[] buildDirectories) {
        final SharabilityQueryImplementation2 sq2 = createSharabilityQuery2(eval, sourceRoots, buildDirectories);
        return new SharabilityQueryImplementation() {
            @Override public int getSharability(File file) {
                return sq2.getSharability(BaseUtilities.toURI(file)).ordinal();
            }
        };
    }
    
    /**
     * Get a property provider which defines <code>basedir</code> according to
     * the project directory and also copies all system properties in the current VM.
     * It may also define <code>ant.home</code> and <code>ant.core.lib</code> if it is able.
     * @return a stock property provider for initial Ant-related definitions
     * @see PropertyUtils#sequentialPropertyEvaluator
     */
    public PropertyProvider getStockPropertyPreprovider() {
        return properties.getStockPropertyPreprovider();
    }

    /**
     * Creates a property provider which can load definitions of project libraries.
     * If this project refers to any project library definition files, they will
     * be included, with <code>${base}</code> replaced by the appropriate value.
     * @return a property provider
     * @since org.netbeans.modules.project.ant/1 1.19
     * @see <a href="http://www.netbeans.org/ns/ant-project-libraries/1.xsd">Schema for project library references</a>
     */
    public PropertyProvider getProjectLibrariesPropertyProvider() {
        return ProjectLibraryProvider.createPropertyProvider(this);
    }
    
    /**
     * Is this project shared with other or not, that is is it using shrared 
     * libraries or not.
     * @return <code>true</code> for shared project
     * @since org.netbeans.modules.project.ant/1 1.19
     */
    public boolean isSharableProject()
    {
        return getLibrariesLocation() != null;
    }

    /**
     * Returns location of shared libraries associated with this project or null.
     * @return relative or absolute OS path or null
     * @since org.netbeans.modules.project.ant/1 1.19
     */
    public String getLibrariesLocation()
    {
        return ProjectLibraryProvider.getLibrariesLocationText(this.createAuxiliaryConfiguration());
    }
    
    /**
     * Change project's associated shared libraries location. If location is 
     * <code>null</code> then project will not have shared libraries and will
     * be considered as not being shared.
     * 
     * @param location project relative or absolute OS path or null
     * @since org.netbeans.modules.project.ant/1 1.18
     */
    public void setLibrariesLocation(String location)
    {
        ProjectLibraryProvider.setLibrariesLocation(this, location);
    }
    
    /**
     * Get a property evaluator that can evaluate properties according to the default
     * file layout for Ant-based projects.
     * First, {@link #getStockPropertyPreprovider stock properties} are predefined.
     * Then {@link #PRIVATE_PROPERTIES_PATH} is loaded via {@link #getPropertyProvider},
     * then {@link #getProjectLibrariesPropertyProvider},
     * then global definitions from {@link PropertyUtils#globalPropertyProvider}
     * (though these may be overridden using the property <code>user.properties.file</code>
     * in <code>private.properties</code>), then {@link #PROJECT_PROPERTIES_PATH}.
     * @return a standard property evaluator
     */
    public PropertyEvaluator getStandardPropertyEvaluator() {
        return properties.getStandardPropertyEvaluator();
    }
    
    /**
     * Find an absolute file path from a possibly project-relative path.
     * @param filename a pathname which may be project-relative or absolute and may
     *                 use / or \ as the path separator
     * @return an absolute file corresponding to it
     */
    public @NonNull File resolveFile(@NonNull String filename) {
        if (filename == null) {
            throw new NullPointerException("Attempted to pass a null filename to resolveFile"); // NOI18N
        }
        return PropertyUtils.resolveFile(FileUtil.toFile(dir), filename);
    }
    
    /**
     * Same as {@link #resolveFile}, but produce a <code>FileObject</code> if possible.
     * @param filename a pathname according to Ant conventions
     * @return a file object it represents, or null if there is no such file object in known filesystems
     */
    public @CheckForNull FileObject resolveFileObject(@NonNull String filename) {
        if (filename == null) {
            throw new NullPointerException("Must pass a non-null filename"); // NOI18N
        }
        return PropertyUtils.resolveFileObject(dir, filename);
    }
    
    /**
     * Take an Ant-style path specification and convert it to a platform-specific absolute path.
     * The path separator characters are converted to the local convention, and individual
     * path components are resolved and cleaned up as for {@link #resolveFile}.
     * @param path an Ant-style abstract path
     * @return an absolute, locally usable path
     */
    public @NonNull String resolvePath(@NonNull String path) {
        if (path == null) {
            throw new NullPointerException("Must pass a non-null path"); // NOI18N
        }
        // XXX consider memoizing results since this is probably called a lot
        return PropertyUtils.resolvePath(FileUtil.toFile(dir), path);
    }
    
    @Override
    public String toString() {
        return "AntProjectHelper[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    @NbBundle.Messages({
        "# {0} - broken file", 
        "DESC_Problem_Broken_Config=The {0} file contains some elements multiple times. "
            + "That can happen when concurrent changes get merged by version control for example. The IDE however cannot decide which one to use. "
            + "So until the problem is resolved manually, the affected configuration will be ignored."
    })
    static void findDuplicateElements(@NonNull Element parent, FileObject config, boolean shared) {
        NodeList l = parent.getChildNodes();
        int nodeCount = l.getLength();
        Set<String> known = new HashSet<String>();
        for (int i = 0; i < nodeCount; i++) {
            if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Node node = l.item(i);
                String localName = node.getLocalName();
                localName = localName == null ? node.getNodeName() : localName;
                String id = localName + "|" + node.getNamespaceURI();
                if (!known.add(id)) {
                    //we have a duplicate;
                    String message = "";
                    if(shared) {
                        message = Bundle.DESC_Problem_Broken_Config("$project_basedir/nbproject/project.xml");
                    } else {
                        message = Bundle.DESC_Problem_Broken_Config("$project_basedir/nbproject/private/private.xml");
                    }
                    ProjectIDEServices.notifyWarning(message);
                    Logger.getLogger(AntProjectHelper.class.getName()).log(Level.WARNING, message);
                }
            }
        }
    }

    private static class RunnableImpl implements Runnable {
        private final Action<Void> action;

        public RunnableImpl(Action<Void> action) {
            this.action = action;
        }

        public void run() {
            ProjectManager.mutex().readAccess(action);
        }
    }

    private static class ActionImpl implements Action<Void> {

        private final String path;
        private AntProjectHelper helper;

        public ActionImpl(AntProjectHelper helper, String path) {
            this.path = path;
            this.helper = helper;
        }

        public Void run() {
            helper.fireChange(path, false);
            helper = null;
            return null;
        }
    }

}
