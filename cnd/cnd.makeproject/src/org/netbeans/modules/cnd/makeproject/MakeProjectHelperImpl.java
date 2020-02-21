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
package org.netbeans.modules.cnd.makeproject;

import org.netbeans.modules.cnd.makeproject.api.support.SmartOutputStream;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.SharabilityQuery.Sharability;
import org.netbeans.modules.cnd.api.project.NativeProjectType;
import org.netbeans.modules.cnd.api.xml.LineSeparatorDetector;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectEvent;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectListener;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;
import org.openide.util.WeakSet;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 */
public final class MakeProjectHelperImpl implements MakeProjectHelper {

    /**
     * XML namespace of private component of Make projects.
     */
    static final String PRIVATE_NS = "http://www.netbeans.org/ns/project-private/1"; // NOI18N

    private static final Logger LOG = Logger.getLogger(MakeProjectHelperImpl.class.getName());
    private static RequestProcessor RP;
    /**
     * Project base directory.
     */
    private final FileObject dir;
    
    /**
     * File system project directory belongs to
     */
    private FileSystem fileSystem;
    /**
     * State object permitting modifications.
     */
    private final ProjectState state;
    /**
     * Make-based project type factory.
     */
    private final MakeProjectTypeImpl type;
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
    private final Set<String> modifiedMetadataPaths = new HashSet<>();
    private Throwable addedProjectXmlPath; // #155010
    /**
     * Registered listeners.
     * Access must be directly synchronized.
     */
    private final List<MakeProjectListener> listeners = new ArrayList<>();
    /** Listener to XML files; needs to be held as an instance field so it is not GC'd */
    private final FileChangeListener fileListener;
    /** Atomic actions in use to save XML files. */
    private final Set<AtomicAction> saveActions = new WeakSet<>();

    public static MakeProjectHelperImpl create(FileObject dir, Document projectXml, ProjectState state, MakeProjectTypeImpl type) {
        return new MakeProjectHelperImpl(dir, projectXml, state, type);
    }
    
    // XXX lock any loaded XML files while the project is modified, to prevent manual editing,
    // and reload any modified files if the project is unmodified
    private MakeProjectHelperImpl(FileObject dir, Document projectXml, ProjectState state, MakeProjectTypeImpl type) {
        this.dir = dir;        
        try {
            this.fileSystem = dir.getFileSystem();
        } catch (FileStateInvalidException ex) {
            throw new IllegalStateException(ex);
        }
        this.state = state;
        assert state != null;
        this.type = type;
        assert type != null;
        this.projectXml = projectXml;
        projectXmlValid = true;
        assert projectXml != null;
        fileListener = new FileListener();
    }

    private void attachProjectFilesListener() {
        FileObject resolveFileObject = resolveFileObject(PROJECT_XML_PATH);
        if (resolveFileObject != null) {
            resolveFileObject.removeFileChangeListener(fileListener);
            resolveFileObject.addFileChangeListener(fileListener);
        } else {
            FileSystemProvider.addFileChangeListener(fileListener, fileSystem, PROJECT_XML_PATH);
        }
        resolveFileObject = resolveFileObject(PRIVATE_XML_PATH);
        if (resolveFileObject != null) {
            resolveFileObject.removeFileChangeListener(fileListener);
            resolveFileObject.addFileChangeListener(fileListener);
        } else {
            FileSystemProvider.addFileChangeListener(fileListener, fileSystem, PRIVATE_XML_PATH);
        }        
    }

    private void detachProjectFilesListener() {
        FileObject resolveFileObject = resolveFileObject(PROJECT_XML_PATH);
        if (resolveFileObject != null) {
            resolveFileObject.removeFileChangeListener(fileListener);
        }
        resolveFileObject = resolveFileObject(PRIVATE_XML_PATH);
        if (resolveFileObject != null) {
            resolveFileObject.removeFileChangeListener(fileListener);
        }        
    }
    
    public FileSystem getFileSystem() {
        return fileSystem;
    }
    
    @Override
    public FileObject resolveFileObject(String filename) {
        FSPath resolveFSPath = resolveFSPath(filename);
        if (resolveFSPath != null) {
            return resolveFSPath.getFileObject();
        }
        return null;
    }

    @Override
    public FSPath resolveFSPath(String filename) {
        if (filename == null) {
            throw new NullPointerException("null filename passed to resolveFile"); // NOI18N
        }
        if (CndPathUtilities.isPathAbsolute(fileSystem, filename)) {
            return new FSPath(fileSystem, filename) ;
        } else {
            FSPath root = FSPath.toFSPath(dir);
            return root.getChild(filename);
        }
    }

    private String resolvePath(String filename) throws IllegalArgumentException {
        if (filename == null) {
            throw new NullPointerException("null filename passed to resolveFile"); // NOI18N
        }
        String result = filename;
        if (!CndPathUtilities.isPathAbsolute(fileSystem, filename)) {
            result = dir.getPath() + CndFileUtils.getFileSeparatorChar(fileSystem) + filename;
        }
        return FileSystemProvider.normalizeAbsolutePath(result, fileSystem);
    }
    
    /**
     * Get the corresponding Make-based project type factory.
     */
    @Override
    public NativeProjectType getType() {
        return type;
    }

    /**
     * Retrieve project.xml or private.xml, loading from disk as needed.
     * private.xml is created as a skeleton on demand.
     */
    private Document getConfigurationXml(boolean shared) {
        assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        assert Thread.holdsLock(modifiedMetadataPaths);
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
        try {
            Document doc = XMLUtil.parse(new InputSource(xml.getInputStream()), false, true, XMLUtil.defaultErrorHandler(), null);
            return doc;
        } catch (IOException e) {
            if (!QUIETLY_SWALLOW_XML_LOAD_ERRORS) {
                LOG.log(Level.INFO, "Load XML: {0}", xml.getPath()); //NOI18N
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        } catch (SAXException e) {
            if (!QUIETLY_SWALLOW_XML_LOAD_ERRORS) {
                LOG.log(Level.INFO, "Load XML: {0}", xml.getPath()); //NOI18N
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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

    private byte[] convertLineSeparator(ByteArrayOutputStream in, final String path) {
        return SmartOutputStream.convertLineSeparator(in, dir.getFileObject(path), dir);
    }

    /**
     * Save an XML config file to a named path.
     * If the file does not yet exist, it is created.
     */
    private FileLock saveXml(final Document doc, final String path) throws IOException {
        assert ProjectManager.mutex().isWriteAccess();
        assert Thread.holdsLock(modifiedMetadataPaths);
        final FileLock[] _lock = new FileLock[1];
        _lock[0] = null;
        runSaveAA(() -> {
            // Keep a copy of xml *while holding modifiedMetadataPaths monitor*.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLUtil.write(doc, baos, "UTF-8"); // NOI18N
            final byte[] data = convertLineSeparator(baos, path);
            final FileObject xml = FileUtil.createData(dir, path);
            try {
                try {
                    _lock[0] = xml.lock(); // unlocked by {@link #save}
                } catch (IOException ex) {
                    LOG.log(Level.INFO, "Cannot save project metadata "+dir.getPath()+"/"+path, ex); //NOI18N
                    return;
                }
                OutputStream os = SmartOutputStream.getSmartOutputStream(xml, _lock[0]);
                try {
                    os.write(data);
                } finally {
                    os.close();
                }
            } catch (UserQuestionException uqe) { // #46089
                ErrorManager.getDefault().notify(uqe);
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
    @Override
    public void addMakeProjectListener(MakeProjectListener listener) {
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                attachProjectFilesListener();
            }
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener to changes in the project configuration.
     * <p>Thread-safe.
     * @param listener a listener to remove
     */
    @Override
    public void removeMakeProjectListener(MakeProjectListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                detachProjectFilesListener();
            }
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
        if (ProjectManager.mutex().isWriteAccess()) {
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
            RP = new RequestProcessor("MakeProjectHelper.RP"); // NOI18N
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
        final MakeProjectListener[] _listeners;
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            _listeners = listeners.toArray(new MakeProjectListener[listeners.size()]);
        }
        final MakeProjectEvent ev = new MakeProjectEvent(this, path, expected);
        final boolean xml = path.equals(PROJECT_XML_PATH) || path.equals(PRIVATE_XML_PATH);
        ProjectManager.mutex().readAccess((Mutex.Action<Void>) () -> {
            for (MakeProjectListener l : _listeners) {
                try {
                    if (xml) {
                        l.configurationXmlChanged(ev);
                    } else {
                        l.propertiesChanged(ev);
                    }
                } catch (RuntimeException e) {
                    // Don't prevent other listeners from being notified.
                    ErrorManager.getDefault().notify(e);
                }
            }
            return null;
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
    @Override
    public FileObject getProjectDirectory() {
        return dir;
    }

    /**Notification that this project has been deleted.
     * @see org.netbeans.spi.project.ProjectState#notifyDeleted
     *
     * @since 1.8
     */
    @Override
    public void notifyDeleted() {
        state.notifyDeleted();
        synchronized (listeners) {
            detachProjectFilesListener();
        }
    }

    /**
     * Mark this project as being modified without actually changing anything in it.
     * Should only be called from {@link org.netbeans.modules.cnd.makeproject.api.ui.ProjectGenerator#createProject}.
     */
    public void markModified() {
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
    public void save() throws IOException {
        assert ProjectManager.mutex().isWriteAccess();
        if (!getProjectDirectory().isValid()) {
            //ProjectManager.saveProject() is called when project is deleted externally..
            return;
        }
        Set<FileLock> locks = new HashSet<>();
        try {
            synchronized (modifiedMetadataPaths) {
                assert !modifiedMetadataPaths.isEmpty();
                Set<String> toBeCleared = new HashSet<>();
                try {
                    for (String path : new TreeSet<>(modifiedMetadataPaths)) {
                        try {
                            if (path.equals(PROJECT_XML_PATH)) {
                                assert projectXml != null;
                                final FileLock lock = saveXml(projectXml, path);
                                if (lock != null) {
                                    locks.add(lock);
                                }
                            } else if (path.equals(PRIVATE_XML_PATH)) {
                                assert privateXml != null;
                                final FileLock lock = saveXml(privateXml, path);
                                if (lock != null) {
                                    locks.add(lock);
                                }
                            }
                        } catch (FileAlreadyLockedException x) { // #155037
                            LOG.log(Level.INFO, null, x);
                        }
                        // As metadata files are saved, take them off the modified list.
                        toBeCleared.add(path);
                    }
                } finally {
                    modifiedMetadataPaths.removeAll(toBeCleared);
                    LOG.log(Level.FINE, "saved {0} and have left {1}", new Object[]{toBeCleared, modifiedMetadataPaths});
                }
            }
        } finally {
            // #57791: release locks outside synchronized block.
            locks.forEach((lock) -> {
                lock.releaseLock();
            });
        }
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
    @Override
    public Element getPrimaryConfigurationData(final boolean shared) {
        final String name = type.getPrimaryConfigurationDataElementName(shared);
        assert name.indexOf(':') == -1;
        final String namespace = type.getPrimaryConfigurationDataElementNamespace(shared);
        assert namespace != null && namespace.length() > 0;
        return ProjectManager.mutex().readAccess((Mutex.Action<Element>) () -> {
            synchronized (modifiedMetadataPaths) {
                Element el = getConfigurationFragment(name, namespace, shared);
                if (el != null) {
                    return el;
                } else {
                    // No such data, corrupt file.
                    return cloneSafely(getConfigurationXml(shared).createElementNS(namespace, name));
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
    @Override
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

    private final class FileListener implements FileChangeListener, Runnable {

        private final List<FileObject> changedFileObjects = new ArrayList<>();

        public FileListener() {
        }

        private void change(FileEvent fe) {
            synchronized (saveActions) {
                for (AtomicAction a : saveActions) {
                    if (fe.firedFrom(a)) {
                        return;
                    }
                }
            }
            synchronized (changedFileObjects) {
                changedFileObjects.add(fe.getFile());
            }
            rp().post(this);
        }

        @Override
        public void run() {
            List<FileObject> l;
            synchronized (changedFileObjects) {
                l = new ArrayList<>(changedFileObjects);
                changedFileObjects.clear();
            }
            l.forEach((fo) -> {
                changeImpl(fo);
            });
        }

        private void changeImpl(FileObject f) {
            String path;
            synchronized (modifiedMetadataPaths) {
                if (f.equals(resolveFileObject(PROJECT_XML_PATH))) {
                    if (modifiedMetadataPaths.contains(PROJECT_XML_PATH)) {
                        //#68872: don't do anything if the given file has non-saved changes:
                        return;
                    }
                    path = PROJECT_XML_PATH;
                    projectXmlValid = false;
                } else if (f.equals(resolveFileObject(PRIVATE_XML_PATH))) {
                    if (modifiedMetadataPaths.contains(PRIVATE_XML_PATH)) {
                        //#68872: don't do anything if the given file has non-saved changes:
                        return;
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

        @Override
        public void fileFolderCreated(FileEvent fe) {
            change(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            change(fe);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            change(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            change(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            change(fe);
        }

        @Override
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
        return ProjectManager.mutex().readAccess((Mutex.Action<Element>) () -> {
            synchronized (modifiedMetadataPaths) {
                Element root = getConfigurationDataRoot(shared);
                Element data = XMLUtil.findElement(root, elementName, namespace);
                if (data != null) {
                    return cloneSafely(data);
                } else {
                    return null;
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
        ProjectManager.mutex().writeAccess((Mutex.Action<Void>) () -> {
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
                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);
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
        return ProjectManager.mutex().writeAccess((Mutex.Action<Boolean>) () -> {
            synchronized (modifiedMetadataPaths) {
                Element root = getConfigurationDataRoot(shared);
                Element data = XMLUtil.findElement(root, elementName, namespace);
                if (data != null) {
                    root.removeChild(data);
                    modifying(shared ? PROJECT_XML_PATH : PRIVATE_XML_PATH);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    /**
     * Create an object permitting this project to store auxiliary configuration.
     * Would be placed into the project's lookup.
     * @return an auxiliary configuration provider object suitable for the project lookup
     */
    @Override
    public AuxiliaryConfiguration createAuxiliaryConfiguration() {
        return new ExtensibleMetadataProviderImpl(this);
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
     * helper.createSharabilityQuery(helper.getStandardPropertyEvaluator(),
     *                               new String[] {"${src.dir}", "${test.src.dir}"},
     *                               new String[] {"${build.dir}", "${dist.dir}"})
     * </pre>
     * <p>
     * A quick rule of thumb is that the include list should contain any
     * source directories which <em>might</em> reside outside the project directory;
     * and the exclude list should contain any directories which you would want
     * to add to a <samp>.cvsignore</samp> file if using CVS (for example).
     * </p>
     * <p>
     * Note that in this case <samp>${src.dir}</samp> and <samp>${test.src.dir}</samp>
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
     */
    @Override
    public SharabilityQueryImplementation2 createSharabilityQuery(String[] sourceRoots, String[] buildDirectories) {
        String[] includes = new String[sourceRoots.length + 1];
        System.arraycopy(sourceRoots, 0, includes, 0, sourceRoots.length);
        includes[sourceRoots.length] = ""; // NOI18N
        String[] excludes = new String[buildDirectories.length + 1];
        System.arraycopy(buildDirectories, 0, excludes, 0, buildDirectories.length);
        excludes[buildDirectories.length] = MakeConfiguration.NBPROJECT_PRIVATE_FOLDER;
        return new SharabilityQueryImpl(this, includes, excludes);
    }

    @Override
    public String toString() {
        return "MakeProjectHelper[" + getProjectDirectory() + "]"; // NOI18N
    }

    private static class RunnableImpl implements Runnable {

        private final Action<Void> action;

        public RunnableImpl(Action<Void> action) {
            this.action = action;
        }

        @Override
        public void run() {
            ProjectManager.mutex().readAccess(action);
        }
    }

    private static class ActionImpl implements Action<Void> {

        private final String path;
        private MakeProjectHelperImpl helper;

        public ActionImpl(MakeProjectHelperImpl helper, String path) {
            this.path = path;
            this.helper = helper;
        }

        @Override
        public Void run() {
            helper.fireChange(path, false);
            helper = null;
            return null;
        }
    }

    private static final class ExtensibleMetadataProviderImpl implements AuxiliaryConfiguration {

        /**
         * Relative path from project directory to the required private cache directory.
         */
        private final MakeProjectHelperImpl helper;

        ExtensibleMetadataProviderImpl(MakeProjectHelperImpl helper) {
            this.helper = helper;
        }

        @Override
        public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
            if (elementName == null || elementName.indexOf(':') != -1 || namespace == null) {
                throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
            }
            return helper.getConfigurationFragment(elementName, namespace, shared);
        }

        @Override
        public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
            if (fragment.getNamespaceURI() == null || fragment.getNamespaceURI().length() == 0) {
                throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
            }
            if (fragment.getLocalName().equals(helper.getType().getPrimaryConfigurationDataElementName(shared))
                    && fragment.getNamespaceURI().equals(helper.getType().getPrimaryConfigurationDataElementNamespace(shared))) {
                throw new IllegalArgumentException("elementName + namespace reserved for project's primary configuration data"); // NOI18N
            }
            helper.putConfigurationFragment(fragment, shared);
        }

        @Override
        public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
            if (elementName == null || elementName.indexOf(':') != -1 || namespace == null) {
                throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
            }
            if (elementName.equals(helper.getType().getPrimaryConfigurationDataElementName(shared))
                    && namespace.equals(helper.getType().getPrimaryConfigurationDataElementNamespace(shared))) {
                throw new IllegalArgumentException("elementName + namespace reserved for project's primary configuration data"); // NOI18N
            }
            return helper.removeConfigurationFragment(elementName, namespace, shared);
        }
    }

    private static final class SharabilityQueryImpl implements SharabilityQueryImplementation2, PropertyChangeListener, MakeProjectListener {

        private final MakeProjectHelperImpl h;
        private final String[] includes;
        private final String[] excludes;
        /** Absolute paths of directories or files to treat as sharable (except for the excludes). */
        private String[] includePaths;
        /** Absolute paths of directories or files to treat as not sharable. */
        private String[] excludePaths;

        SharabilityQueryImpl(MakeProjectHelperImpl h, String[] includes, String[] excludes) {
            this.h = h;
            this.includes = includes;
            this.excludes = excludes;
            computeFiles();
            h.addMakeProjectListener(this);
        }

        /** Compute the absolute paths which are and are not sharable. */
        private void computeFiles() {
            String[] _includePaths = computeFrom(includes, false);
            String[] _excludePaths = computeFrom(excludes, true);
            synchronized (this) {
                includePaths = _includePaths;
                excludePaths = _excludePaths;
            }
        }

        /** Compute a list of absolute paths based on some abstract names. */
        private String[] computeFrom(String[] list, boolean excludeProjectLibraryPrivate) {
            List<String> result = new ArrayList<>(list.length);
            for (String val : list) {
                result.add(h.resolvePath(val));
            }
            // XXX should remove overlaps somehow
            return result.toArray(new String[result.size()]);
        }

        @Override
        public Sharability getSharability(URI file) {
            String path = file.getPath();
            synchronized (this) {
                if (contains(path, excludePaths, false)) {
                    return Sharability.NOT_SHARABLE;
                }
                return contains(path, includePaths, false)
                        ? (contains(path, excludePaths, true) ? Sharability.MIXED : Sharability.SHARABLE)
                        : Sharability.UNKNOWN;
            }
        }

        /**
         * Check whether a file path matches something in the supplied list.
         * @param a file path to test
         * @param list a list of file paths
         * @param reverse if true, check if the file is an ancestor of some item; if false,
         *                check if some item is an ancestor of the file
         * @return true if the file matches some item
         */
        private static boolean contains(String path, String[] list, boolean reverse) {
            for (String s : list) {
                if (path.equals(s)) {
                    return true;
                } else {
                    if (reverse ? s.startsWith(path + File.separatorChar) : path.startsWith(s + File.separatorChar)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            computeFiles();
        }

        @Override
        public void configurationXmlChanged(MakeProjectEvent ev) {
            computeFiles();
        }

        @Override
        public void propertiesChanged(MakeProjectEvent ev) {
        }
    }    
}
