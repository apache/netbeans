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

package org.netbeans.modules.project.libraries;

import org.netbeans.spi.project.libraries.WritableLibraryProvider;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.project.libraries.LibraryProvider.class)
public class LibrariesStorage extends FileChangeAdapter
implements WritableLibraryProvider<LibraryImplementation>, ChangeListener {

    private static final String NB_HOME_PROPERTY = "netbeans.home";  //NOI18N
    private static final String LIBRARIES_REPOSITORY = "org-netbeans-api-project-libraries/Libraries";  //NOI18N
    private static final String TIME_STAMPS_FILE = "libraries-timestamps.properties"; //NOI18B
    private static final String XML_EXT = "xml";    //NOI18N

    static final Logger LOG = Logger.getLogger(LibrariesStorage.class.getName());

    //Lock to prevent FileAlreadyLocked exception.
    private static final Object TIMESTAMPS_LOCK = new Object ();

    // persistent storage, it may be null for before first library is store into storage
    private FileObject storage = null;
    private Libs libs;
    // Library declaraion public ID
    // i18n bundle
    private ResourceBundle bundle;
    private PropertyChangeSupport support;
    //Flag if the storage is initialized
    //The storage needs to be lazy initialized, it is in lookup
    //@GuardedBy("this")
    private boolean initialized;
    //@GuardedBy("this")
    private boolean inchange;
    private Properties timeStamps;
    private final LibraryTypeRegistry ltRegistry;


    /**
     * Create libraries that need to be populated later.
     */
    public LibrariesStorage() {
        this.support = new PropertyChangeSupport(this);
        this.ltRegistry = LibraryTypeRegistry.getDefault();
        this.ltRegistry.addChangeListener(this);
    }

    /**
     * Constructor for tests
     */
    LibrariesStorage (FileObject storage) {
        this ();
        this.storage = storage;
    }



    /**
     * Initialize the default storage.
     * @return new storage or null on I/O error.
     */
    private static final FileObject createStorage () {
        try {
            return FileUtil.createFolder(FileUtil.getConfigRoot(), LIBRARIES_REPOSITORY);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }


    // scans over storage and fetchs it ((fileset persistence files) into memory
    // ... note that providers can read their data during getVolume call
    private void loadFromStorage(
            final Map<? super String, ? super LibraryImplementation> libraries,
            final Map<? super String, ? super LibraryImplementation> librariesByFileNames) {
        // configure parser
        //We are in unit test with no storage
        if (storage == null) {
            return;
        }
        final LibraryDeclarationConvertorImpl convertor = new LibraryDeclarationConvertorImpl();    //Immutable
        // parse
        for (FileObject descriptorFile : storage.getChildren()) {
            if (XML_EXT.equalsIgnoreCase(descriptorFile.getExt())) {
                try {
                    final LibraryDeclarationHandlerImpl handler = new LibraryDeclarationHandlerImpl();
                    final LibraryDeclarationParser parser = new LibraryDeclarationParser(handler,convertor);
                    readLibrary (descriptorFile, parser);
                    LibraryImplementation impl = handler.getLibrary ();
                    if (impl != null) {
                        LibraryTypeProvider provider = ltRegistry.getLibraryTypeProvider (impl.getType());
                        if (provider == null) {
                            LOG.warning("LibrariesStorage: Can not invoke LibraryTypeProvider.libraryCreated(), the library type provider is unknown.");  //NOI18N
                        }
                        else if (libraries.containsKey(impl.getName())) {
                            LOG.log(
                                Level.WARNING,
                                "LibrariesStorage: Library \"{0}\" is already defined, skeeping the definition from: {1}",  //NOI18N
                                new Object[]{
                                    impl.getName(),
                                    FileUtil.getFileDisplayName(descriptorFile)
                                });
                        }
                        else {
                            if (!isUpToDate(descriptorFile)) {
                                provider.libraryCreated (impl);
                                updateTimeStamp(descriptorFile);
                            }
                            librariesByFileNames.put(descriptorFile.getPath(),impl);
                            libraries.put (impl.getName(),impl);
                            LibrariesModule.registerSource(impl, descriptorFile);
                        }
                    }
                } catch (SAXException e) {
                    //The library is broken, probably edited by user
                    //just log
                    logBrokenLibraryDescripor(descriptorFile, e);
                } catch (CharConversionException e) { {
                    //The library is broken, probably edited by user
                    //just log
                    logBrokenLibraryDescripor(descriptorFile, e);
                }
                } catch (ParserConfigurationException e) {
                    Exceptions.printStackTrace(e);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                } catch (RuntimeException e) {
                    // Other problem.
                    Exceptions.printStackTrace(e);
                }
            }
        }
        try {
            saveTimeStamps();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    @NonNull
    @SuppressWarnings("NestedAssignment")
    private Libs initStorage (final boolean reset) {
        synchronized (this) {
            if (!initialized) {
                if (this.storage == null) {
                    this.storage = createStorage();
                }
                if (storage != null) {
                    this.storage.addFileChangeListener (this);
                }
                initialized = true;
            }
            if (libs != null && !reset && !(inchange && ProjectManager.mutex().isWriteAccess())) {
                return libs;
            }
            if (reset) {
                inchange = true;
            }
        }
        boolean success = false;
        final Map<String,LibraryImplementation> libraries = new HashMap<>();
        final Map<String,LibraryImplementation> librariesByFileNames = new HashMap<>();
        Libs res;
        try {
            this.loadFromStorage(libraries, librariesByFileNames);
            success = true;
        } finally {
            synchronized (this) {
                if (reset) {
                    inchange = false;
                }
                if (success) {
                    res = this.libs = new Libs(libraries,librariesByFileNames);
                } else {
                    res = new Libs(Collections.<String, LibraryImplementation>emptyMap(),Collections.<String, LibraryImplementation>emptyMap());
                }
            }
        }
        return res;
    }

    private static LibraryImplementation readLibrary (FileObject descriptorFile) throws SAXException, ParserConfigurationException, IOException{
        return readLibrary (descriptorFile, (LibraryImplementation) null);
    }

    private static LibraryImplementation readLibrary (FileObject descriptorFile, LibraryImplementation impl) throws SAXException, ParserConfigurationException, IOException {
        final LibraryDeclarationHandlerImpl handler = new LibraryDeclarationHandlerImpl();
        final LibraryDeclarationConvertorImpl convertor = new LibraryDeclarationConvertorImpl();
        final LibraryDeclarationParser parser = new LibraryDeclarationParser(handler,convertor);
        handler.setLibrary (impl);
        readLibrary (descriptorFile, parser);
        LibrariesModule.registerSource(impl, descriptorFile);
        return handler.getLibrary();
    }

    private static void readLibrary (
            @NonNull final FileObject descriptorFile,
            @NonNull final LibraryDeclarationParser parser) throws SAXException, ParserConfigurationException, IOException {
        try {
            FileLockManager.getDefault().readAction(
                descriptorFile,
                new Callable<Void>() {
                    @Override
                    public Void call() throws IOException, ParserConfigurationException, SAXException {
                        final URL baseURL = descriptorFile.toURL();
                        final InputSource input = new InputSource(baseURL.toExternalForm());
                        try (InputStream in = descriptorFile.getInputStream()) {
                            input.setByteStream(in); // #33554 workaround
                            parser.parse(input);
                        } catch (SAXException e) {
                            throw Exceptions.attachMessage(e, "From: " + baseURL);  //NOI18N
                        } catch (IOException e) {
                            throw Exceptions.attachMessage(e, "From: " + baseURL);  //NOI18N
                        }
                        return null;
                    }
                });
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeLibrary (final FileObject storage, final LibraryImplementation library) throws IOException {
        storage.getFileSystem().runAtomicAction(
                new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        String libraryType = library.getType ();
                        LibraryTypeProvider libraryTypeProvider = ltRegistry.getLibraryTypeProvider (libraryType);
                        if (libraryTypeProvider == null) {
                            LOG.warning("LibrariesStorage: Cannot store library, the library type is not recognized by any of installed LibraryTypeProviders.");	//NOI18N
                            return;
                        }
                        final FileObject fo = FileUtil.createData(
                            storage,
                            String.format(
                                "%s.%s",
                                library.getName(),
                                "xml"));   //NOI18N
                        LibraryDeclarationParser.writeLibraryDefinition (fo, library, libraryTypeProvider);
                    }
                }
        );
    }
    
    private void fireLibrariesChanged () {
        this.support.firePropertyChange(PROP_LIBRARIES,null,null);
    }


    @Override
    public final void addPropertyChangeListener (PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }


    @Override
    public final void removePropertyChangeListener (PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }

    /**
     * Return all libraries in memory.
     */
    @Override
    public final LibraryImplementation[] getLibraries() {
        final Libs res = initStorage(false);
        assert res != null;
        return res.getImpls();
    } // end getLibraries


    @Override
    public boolean addLibrary (LibraryImplementation library) throws IOException {
        this.initStorage(false);
        assert this.storage != null : "Storage is not initialized";
        writeLibrary(this.storage,library);
        return true;
    }

    @Override
    public boolean removeLibrary (LibraryImplementation library) throws IOException {
        final Libs data = this.initStorage(false);
        assert this.storage != null : "Storage is not initialized";
        final String path = data.findPath(library);
        if (path != null) {
            final FileObject fo = this.storage.getFileSystem().findResource (path);
            if (fo != null) {
                fo.delete();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean updateLibrary(final LibraryImplementation oldLibrary, final LibraryImplementation newLibrary) throws IOException {
        final Libs data = this.initStorage(false);
        assert this.storage != null : "Storage is not initialized";
        final String path = data.findPath(oldLibrary);
        if (path != null) {
            final FileObject fo = this.storage.getFileSystem().findResource(path);
            if (fo != null) {
                String libraryType = newLibrary.getType ();
                final LibraryTypeProvider libraryTypeProvider = ltRegistry.getLibraryTypeProvider (libraryType);
                if (libraryTypeProvider == null) {
                    LOG.warning("LibrariesStorageL Cannot store library, the library type is not recognized by any of installed LibraryTypeProviders.");	//NOI18N
                } else {
                    this.storage.getFileSystem().runAtomicAction(
                            new FileSystem.AtomicAction() {
                                public void run() throws IOException {
                                    LibraryDeclarationParser.writeLibraryDefinition (fo, newLibrary, libraryTypeProvider);
                                }
                            }
                    );
                }
                return true;
            }
        }
        return false;
    }


    public void fileDataCreated(FileEvent fe) {
        FileObject fo = fe.getFile();
        try {
            final Libs data = this.initStorage(false);
            final LibraryImplementation impl = readLibrary (fo);
            if (impl != null) {
                LibraryTypeProvider provider = ltRegistry.getLibraryTypeProvider (impl.getType());
                if (provider == null) {
                    LOG.warning("LibrariesStorage: Can not invoke LibraryTypeProvider.libraryCreated(), the library type provider is unknown.");  //NOI18N
                }
                else {
                    data.add (impl.getName(), fo.getPath(), impl);
                    //Has to be called outside the synchronized block,
                    // The code is provided by LibraryType implementator and can fire events -> may cause deadlocks
                    try {
                        provider.libraryCreated (impl);
                        updateTimeStamp(fo);
                        saveTimeStamps();
                    } catch (RuntimeException e) {
                        String message = NbBundle.getMessage(LibrariesStorage.class,"MSG_libraryCreatedError");
                        Exceptions.printStackTrace(Exceptions.attachMessage(e,message));
                    }
                    this.fireLibrariesChanged();
                }
            }
        } catch (SAXException e) {
            //The library is broken, probably edited by user or unknown provider (FoD), log as warning
            logBrokenLibraryDescripor(fo, e);
        } catch (ParserConfigurationException e) {
            Exceptions.printStackTrace(e);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public void fileDeleted(FileEvent fe) {
        String fileName = fe.getFile().getPath();
        final Libs data = this.initStorage(false);
        LibraryImplementation impl = data.remove(fileName);
        if (impl != null) {
            LibraryTypeProvider provider = ltRegistry.getLibraryTypeProvider (impl.getType());
            if (provider == null) {
                LOG.warning("LibrariesStorage: Cannot invoke LibraryTypeProvider.libraryDeleted(), the library type provider is unknown.");  //NOI18N
            }
            else {
                //Has to be called outside the synchronized block,
                // The code is provided by LibraryType implementator and can fire events -> may cause deadlocks
                try {
                    provider.libraryDeleted (impl);
                } catch (RuntimeException e) {
                    String message = NbBundle.getMessage(LibrariesStorage.class,"MSG_libraryDeletedError");
                    Exceptions.printStackTrace(Exceptions.attachMessage(e,message));
                }
            }
            this.fireLibrariesChanged();
        }
    }

    public void fileChanged(FileEvent fe) {
        FileObject definitionFile = fe.getFile();
        String fileName = definitionFile.getPath();
        final Libs data = this.initStorage(false);
        final LibraryImplementation impl = data.get(fileName);
        if (impl != null) {
            try {
                readLibrary (definitionFile, impl);
                LibraryTypeProvider provider = ltRegistry.getLibraryTypeProvider (impl.getType());
                if (provider == null) {
                    LOG.warning("LibrariesStorage: Can not invoke LibraryTypeProvider.libraryCreated(), the library type provider is unknown.");  //NOI18N
                }
                try {
                    //TODO: LibraryTypeProvider should be extended by libraryUpdated method
                    provider.libraryCreated (impl);
                    updateTimeStamp(definitionFile);
                    saveTimeStamps();
                } catch (RuntimeException e) {
                    String message = NbBundle.getMessage(LibrariesStorage.class,"MSG_libraryCreatedError");
                    Exceptions.printStackTrace(Exceptions.attachMessage(e,message));
                }
            } catch (SAXException se) {
                //The library is broken, probably edited by user, log as warning
                logBrokenLibraryDescripor(definitionFile, se);
            } catch (ParserConfigurationException pce) {
                Exceptions.printStackTrace(pce);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }

    protected final ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = NbBundle.getBundle(LibrariesStorage.class);
        }
        return bundle;
    }

    private boolean isUpToDate (FileObject libraryDefinition) {
        Properties timeStamps = getTimeStamps();
        String ts = (String) timeStamps.get (libraryDefinition.getNameExt());
        return ts == null ? false : Long.parseLong(ts) >= libraryDefinition.lastModified().getTime();
    }

    private void updateTimeStamp (FileObject libraryDefinition) {
        Properties timeStamps = getTimeStamps();
        timeStamps.put(libraryDefinition.getNameExt(), Long.toString(libraryDefinition.lastModified().getTime()));
    }

    private void saveTimeStamps () throws IOException {
        if (this.storage != null) {
            synchronized (TIMESTAMPS_LOCK) {
                Properties timeStamps = getTimeStamps();
                if (timeStamps.get(NB_HOME_PROPERTY) == null) {
                    String currNbLoc = getNBRoots();
                    timeStamps.put(NB_HOME_PROPERTY,currNbLoc);
                }
                FileObject parent = storage.getParent();
                FileObject timeStampFile = FileUtil.createData(parent,TIME_STAMPS_FILE);
                FileLock lock = timeStampFile.lock();
                try (final OutputStream out = timeStampFile.getOutputStream(lock)) {
                    timeStamps.store (out, null);
                } finally {
                    lock.releaseLock();
                }
            }
        }
    }

    private synchronized Properties getTimeStamps () {
        if (this.timeStamps == null) {
            this.timeStamps = new Properties();
            if (this.storage != null) {
                FileObject timeStampFile = storage.getParent().getFileObject(TIME_STAMPS_FILE);
                if (timeStampFile != null) {
                    try {
                        InputStream in = timeStampFile.getInputStream();
                        try {
                            this.timeStamps.load (in);
                        } finally {
                            in.close();
                        }
                        String nbLoc = (String) this.timeStamps.get (NB_HOME_PROPERTY);
                        String currNbLoc = getNBRoots ();
                        if (nbLoc == null || !nbLoc.equals (currNbLoc)) {
                            this.timeStamps.clear();
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            }
        }
        return this.timeStamps;
    }

    private static String getNBRoots () {
        Set<String> result = new TreeSet<String>();
        String currentNbLoc = System.getProperty ("netbeans.home");   //NOI18N
        if (currentNbLoc != null) {
            File f = FileUtil.normalizeFile(new File (currentNbLoc));
            if (f.isDirectory()) {
                result.add (f.getAbsolutePath());
            }
        }
        currentNbLoc = System.getProperty ("netbeans.dirs");        //NOI18N
        if (currentNbLoc != null) {
            StringTokenizer tok = new StringTokenizer(currentNbLoc, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                 File f = FileUtil.normalizeFile(new File(tok.nextToken()));
                 result.add(f.getAbsolutePath());
            }
        }
        StringBuffer sb = new StringBuffer ();
        for (Iterator<String> it = result.iterator(); it.hasNext();) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(":");  //NOI18N
            }
        }
        return sb.toString();
    }

    @Override
    public void stateChanged(ChangeEvent e) {        
        initStorage(true);
        fireLibrariesChanged();
    }

    private void logBrokenLibraryDescripor(
        @NonNull FileObject descriptorFile,
        @NonNull Exception cause){
        Level level = Level.WARNING;
        if (cause instanceof LibraryDeclarationHandlerImpl.UnknownLibraryTypeException) {
            if (((LibraryDeclarationHandlerImpl.UnknownLibraryTypeException) cause).type.equals("j2se")) {
                // possibly in a unit test, be quiet
                level = Level.FINE;
            } else {
                //log unknown library type as INFO common with FoD
                level = Level.INFO;
            }
        }
        LOG.log(
            level,
            "Cannot load library from file {0}, reason: {1}",   //NOI18N
            new Object[] {
                FileUtil.getFileDisplayName(descriptorFile),
                cause.getMessage()
           });
    }

    private static final class Libs {
        private final Map<String,LibraryImplementation> librariesByName;
        private final Map<String,LibraryImplementation> librariesByPath;

        Libs(final Map<String,LibraryImplementation> librariesByName, Map<String,LibraryImplementation> librariesByPath) {
            assert librariesByName != null;
            assert librariesByPath != null;
            this.librariesByName = librariesByName;
            this.librariesByPath = librariesByPath;
        }

        synchronized LibraryImplementation[] getImpls() {
            return librariesByName.values().toArray(new LibraryImplementation[librariesByName.size()]);
        }

        synchronized String findPath(final LibraryImplementation library) {
            for (Map.Entry<String, LibraryImplementation> entry : librariesByPath.entrySet()) {
                String key = entry.getKey();
                LibraryImplementation lib = entry.getValue();
                if (library.equals (lib)) {
                    return key;
                }
            }
            return null;
        }

        synchronized void add(final String name, final String path, final LibraryImplementation impl) {
            assert name != null;
            assert path != null;
            assert impl != null;
            this.librariesByName.put(name, impl);
            this.librariesByPath.put(path, impl);
        }

        synchronized LibraryImplementation remove(final String path) {
            assert path != null;
            final LibraryImplementation impl = librariesByPath.remove(path);
            if (impl != null) {
                librariesByName.remove (impl.getName());
            }
            return impl;
        }

        synchronized LibraryImplementation get(final String path) {
            assert path != null;
            return librariesByPath.get(path);
        }
    }
}
