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
package org.netbeans.modules.cnd.modelimpl.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import org.netbeans.api.project.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.api.project.NativeProjectSettings;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryEvent;
import org.netbeans.modules.cnd.modelimpl.spi.LowMemoryAlerter;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.cnd.utils.SuspendableFileChangeListener;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 */
public class ModelSupport implements PropertyChangeListener {

    private static final AtomicBoolean hasOpenedProjects = new AtomicBoolean(false);
    private static final ModelSupport instance = new ModelSupport();
    private volatile ModelImpl theModel;
    private final Set<Lookup.Provider> openedProjects = new HashSet<>();
    final ModifiedObjectsChangeListener modifiedListener = TraceFlags.USE_PARSER_API ? null : new ModifiedObjectsChangeListener();
    private SuspendableFileChangeListener fileChangeListener;
    private static final boolean TRACE_STARTUP = Boolean.getBoolean("cnd.modelsupport.startup.trace");// NOI18N
    private volatile boolean postponeParse = false;
    private final RequestProcessor.Task openProjectsTask = 
            new RequestProcessor("ModelSupport processor", 1).create( // NOI18N
                new Runnable() {
                    @Override
                    public void run() {
                        openProjectsIfNeeded();
                    }
                });

    private ModelSupport() {
    }

    public static ModelSupport instance() {
        return instance;
    }

    public static int getTabSize() {
        return 8;
    }

    public ModelImpl getModel() {
        return this.theModel;
    }

    public void setModel(ModelImpl model) {
        this.theModel = model;
        synchronized (this) {
            if (TraceFlags.MERGE_EVENTS) {
                if (model == null) {
                    CsmEventDispatcher.getInstance().shutdown();
                } else {
                    CsmEventDispatcher.getInstance().startup();
                }
            } else {
                if (fileChangeListener != null) {
                    CndFileSystemProvider.removeFileChangeListener(fileChangeListener);
                    fileChangeListener = null;
                }
                if (model != null) {
                    boolean remoteOnly = CndTraceFlags.USE_INDEXING_API;
                    fileChangeListener = new SuspendableFileChangeListener(new ExternalUpdateListener(this), remoteOnly);
                    CndFileSystemProvider.addFileChangeListener(fileChangeListener);
                }
            }
        }
    }

    /** copy pasted version from CndUtils to prevent load of CndUtils during startup */
    private static boolean getBoolean(String name, boolean result) {
        String text = System.getProperty(name);
        if (text != null) {
            result = Boolean.parseBoolean(text);
        }
        return result;
    }

    /** copy pasted version from CndUtils to prevent load of CndUtils during startup */
    public static boolean isStandalone() {
        if ("true".equals(System.getProperty("cnd.command.line.utility"))) { // NOI18N
            return true;
        }
        // headless is the same as standalone
        if (getBoolean("java.awt.headless", false)) { // NOI18N
            return true;
        }
        return !ModelSupport.class.getClassLoader().getClass().getName().startsWith("org.netbeans."); // NOI18N
    }
    
    public void startup() {
        if (!TraceFlags.USE_PARSER_API) {
            modifiedListener.clean();
            DataObject.getRegistry().addChangeListener(modifiedListener);
        }

        if (!isStandalone()) {
            openedProjects.clear();
            hasOpenedProjects.set(false);
            if (TRACE_STARTUP) {
                System.out.println("Model support: Inited"); // NOI18N
            }
            if (TopComponent.getRegistry().getOpened().size() > 0) {
                if (TRACE_STARTUP) {
                    System.out.println("Model support: Open projects in Init"); // NOI18N
                }
                postponeParse = false;
                NativeProjectRegistry.getDefault().addPropertyChangeListener(this);
                openProjectsTask.schedule(0);
            } else {
                if (TRACE_STARTUP) {
                    System.out.println("Model support: Postpone open projects"); // NOI18N
                }
                postponeParse = true;
                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

                    @Override
                    public void run() {
                        if (TRACE_STARTUP) {
                            System.out.println("Model support: invoked after ready UI"); // NOI18N
                        }
                        postponeParse = false;
                        NativeProjectRegistry.getDefault().addPropertyChangeListener(ModelSupport.this);
                        openProjectsTask.schedule(0);
                    }
                });
            }
        }
    }

    public boolean hasOpenedProjects() {
        return hasOpenedProjects.get();
    }

    public void notifyClosing() {
        if (!TraceFlags.USE_PARSER_API) {
            DataObject.getRegistry().removeChangeListener(modifiedListener);
            modifiedListener.clean();
        }
        ModelImpl model = theModel;
        if (model != null) {
            CsmCorePackageAccessor.get().notifyClosing(model);
        }
    }

    public void shutdown() {
        if (!TraceFlags.USE_PARSER_API) {
            DataObject.getRegistry().removeChangeListener(modifiedListener);
            modifiedListener.clean();
        }
        ModelImpl model = theModel;
        if (model != null) {
            CsmCorePackageAccessor.get().notifyClosing(model);
            // we have to wait openedProjects to be empty
            // because opened projects should be closed by closeProjectsIfNeeded
            // otherwise project metadata can be cleaned up and csm project
            // will not get information for validator
            synchronized (openedProjects) {
                while (!openedProjects.isEmpty()) {
                    try {
                        openedProjects.wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            hasOpenedProjects.set(false);
            model.shutdown();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try { //FIXUP #109105 OpenProjectList does not get notification about adding a project if the project is stored in the repository
            if (TRACE_STARTUP) {
                System.out.println("Model support event:" + evt.getPropertyName() + " postponeParse=" + postponeParse);
            }
            if (evt.getPropertyName().equals(NativeProjectRegistry.PROPERTY_OPEN_NATIVE_PROJECT)||
                evt.getPropertyName().equals(NativeProjectRegistry.PROPERTY_CLOSE_NATIVE_PROJECT)||
                evt.getPropertyName().equals(NativeProjectRegistry.PROPERTY_DELETE_NATIVE_PROJECT)) {
                if (!postponeParse) {
                    if (TRACE_STARTUP) {
                        System.out.println("Model support: Open projects on OpenProjects.PROPERTY_OPEN_PROJECTS"); // NOI18N
                    }
                    openProjectsTask.schedule(0);
                    closeProjectsIfNeeded(evt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void openProjectsIfNeeded() {
        synchronized (openedProjects) {
            Collection<NativeProject> projects = NativeProjectRegistry.getDefault().getOpenProjects();
            if (TRACE_STARTUP) {
                System.out.println("Model support: openProjects size=" + projects.size() + " modelState=" + CsmModelAccessor.getModelState()); // NOI18N
            }
            if (TRACE_STARTUP) {
                System.out.println("Model support: openProjects new=" + projects.size() + " now=" + openedProjects.size()); // NOI18N
            }
            for(NativeProject project : projects) {
                Provider makeProject = project.getProject();
                if (!openedProjects.contains(makeProject)) {
                    addProject(makeProject);
                }
            }
        }
    }

    private void closeProjectsIfNeeded(PropertyChangeEvent evt) {
        synchronized (openedProjects) {
            Collection<NativeProject> projects = NativeProjectRegistry.getDefault().getOpenProjects();
            if (TRACE_STARTUP) {
                System.out.println("Model support: closeProjects size=" + projects.size() + " modelState=" + CsmModelAccessor.getModelState()); // NOI18N
            }
            if (TRACE_STARTUP) {
                System.out.println("Model support: closeProjects new=" + projects.size() + " now=" + openedProjects.size()); // NOI18N
            }
            Set<Lookup.Provider> nowOpened = new HashSet<>();
            for(NativeProject project : projects) {
                Provider makeProject = project.getProject();
                nowOpened.add(makeProject);
            }

            Set<Lookup.Provider> toClose = new HashSet<>();
            for (Lookup.Provider project : openedProjects) {
                if (!nowOpened.contains(project)) {
                    toClose.add(project);
                }
            }

            for (Lookup.Provider project : toClose) {
                if (evt.getPropertyName().equals(NativeProjectRegistry.PROPERTY_DELETE_NATIVE_PROJECT)) {
                    closeProject(project, true);
                } else {
                    closeProject(project, false);
                }
            }
            hasOpenedProjects.set(!openedProjects.isEmpty());
            openedProjects.notifyAll();
        }
    }

    public static void trace(NativeFileItem nativeFile) {
        try {
            Diagnostic.trace("  native file item" + nativeFile.getAbsolutePath()); // NOI18N
            Diagnostic.trace("    user includes: " + nativeFile.getUserIncludePaths()); // NOI18N
            Diagnostic.trace("    user macros: " + nativeFile.getUserMacroDefinitions()); // NOI18N
            Diagnostic.trace("    system includes: " + nativeFile.getSystemIncludePaths()); // NOI18N
            Diagnostic.trace("    system macros: " + nativeFile.getSystemMacroDefinitions()); // NOI18N
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void dumpNativeProject(NativeProject nativeProject) {
        System.err.println("\n\n\nDumping project " + nativeProject.getProjectDisplayName());
        System.err.println("\nSystem include paths");
        for (Iterator<IncludePath> it = nativeProject.getSystemIncludePaths().iterator(); it.hasNext();) {
            System.err.println("    " + it.next().getFSPath().getPath());
        }
        System.err.println("\nUser include paths");
        for (Iterator<IncludePath> it = nativeProject.getUserIncludePaths().iterator(); it.hasNext();) {
            System.err.println("    " + it.next().getFSPath().getPath());
        }
        System.err.println("\nSystem macros");
        for (Iterator<String> it = nativeProject.getSystemMacroDefinitions().iterator(); it.hasNext();) {
            System.err.println("    " + it.next());
        }
        System.err.println("\nUser macros");
        for (Iterator<String> it = nativeProject.getUserMacroDefinitions().iterator(); it.hasNext();) {
            System.err.println("    " + it.next());
        }
        List<NativeFileItem> sources = new ArrayList<>();
        List<NativeFileItem> headers = new ArrayList<>();
        for (NativeFileItem item : nativeProject.getAllFiles()) {
            if (!item.isExcluded()) {
                switch (item.getLanguage()) {
                    case C:
                    case CPP:
                    case FORTRAN:
                        sources.add(item);
                        break;
                    case C_HEADER:
                        headers.add(item);
                        break;
                    default:
                        break;
                }
            }
        }
        System.err.println("\nSources: (" + sources.size() + " files )");
        for (NativeFileItem elem : sources) {
            System.err.println(elem.getAbsolutePath());
        }
        System.err.println("\nHeaders: (" + headers.size() + " files )");
        for (NativeFileItem elem : headers) {
            System.err.println(elem.getAbsolutePath());
        }

        System.err.println("End of project dump\n\n\n");
    }

    public static NativeProject getNativeProject(Object platformProject) {
        NativeProject nativeProject = platformProject instanceof NativeProject ? (NativeProject) platformProject : null;
        if (platformProject instanceof Project) {
            Project project = (Project) platformProject;
            nativeProject = project.getLookup().lookup(NativeProject.class);
        }
        return nativeProject;
    }

    private String toString(Lookup.Provider project) {
        StringBuilder sb = new StringBuilder();
        ProjectInformation pi = ProjectUtils.getInformation((Project) project);
        if (pi != null) {
            sb.append(" Name=").append(pi.getName()); // NOI18N
            sb.append(" DisplayName=").append(pi.getDisplayName()); // NOI18N
        }
//        SourceGroup[] sg = ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC);
//        for( int i = 0; i < sg.length; i++ ) {
//            sb.append(" SG DisplayName=" + sg[i].getDisplayName() + " rootFolder=" + sg[i].getRootFolder());
//        }
        return sb.toString();
    }

    private void addProject(final Lookup.Provider project) {
        if (TraceFlags.DEBUG) {
            Diagnostic.trace("### ModelSupport.addProject: " + toString(project)); // NOI18N
        }

        final NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
        if (nativeProject != null) {

            CsmModelAccessor.getModel(); // just to ensure it's created
            final ModelImpl model = theModel;
            if (model == null) {
                return;
            }

            openedProjects.add(project);
            hasOpenedProjects.set(true);
            if (TraceFlags.DEBUG) {
                dumpProjectFiles(nativeProject);
            }

            String taskName = NbBundle.getMessage(getClass(), "MSG_CodeAssistanceInitializationTask",
                    nativeProject.getProjectDisplayName());
            NamedRunnable task = new NamedRunnable(taskName) {
                @Override
                protected void runImpl() {
                    if (!CsmModelAccessor.isModelAlive()) {
                        return;
                    }
                    ProgressHandle handle = ProgressHandleFactory.createHandle(getName());
                    handle.start();
                    try {
                        NativeProjectSettings settings = project.getLookup().lookup(NativeProjectSettings.class);
                        // enable by default
                        boolean enableModel = (settings == null) ? true : settings.isCodeAssistanceEnabled();
                        model.addProject(nativeProject, nativeProject.getProjectDisplayName(), enableModel);
                    } finally {
                        handle.finish();
                    }
                }
            };
            nativeProject.runOnProjectReadiness(task);
        }
    }

    private void dumpProjectFiles(NativeProject nativeProject) {
        if (TraceFlags.DEBUG) {
            Diagnostic.trace("+++ Sources:"); // NOI18N
            List<NativeFileItem> sources = new ArrayList<>();
            List<NativeFileItem> headers = new ArrayList<>();
            for (NativeFileItem item : nativeProject.getAllFiles()) {
                if (!item.isExcluded()) {
                    switch (item.getLanguage()) {
                        case C:
                        case CPP:
                        case FORTRAN:
                            sources.add(item);
                            break;
                        case C_HEADER:
                            headers.add(item);
                            break;
                        default:
                            break;
                    }
                }
            }
            for (NativeFileItem elem : sources) {
                trace(elem);
            }
            Diagnostic.trace("+++ Headers:"); // NOI18N
            for (NativeFileItem elem : headers) {
                trace(elem);
            }
        }
    }

    private void closeProject(Lookup.Provider project, boolean isDeleted) {
        assert Thread.holdsLock(openedProjects);
        if (TraceFlags.DEBUG) {
            Diagnostic.trace("### ModelSupport.closeProject: " + toString(project)); // NOI18N
        }
        ModelImpl model = theModel;
        if (model == null) {
            return;
        }
        NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
        if (nativeProject != null) {
            model.closeProject(nativeProject, isDeleted);
        }
        openedProjects.remove(project);
    }

    private static FileBuffer createFileBuffer(DataObject dao) {
        FileObject fo = dao.getPrimaryFile();
        if (fo.isValid()) {
            if (dao.isModified()) {
                EditorCookie editor = dao.getLookup().lookup(EditorCookie.class);
                if (editor != null) {
                    Document doc = editor.getDocument();
                    if (doc != null) {
                        return new FileBufferDoc(fo, doc);
                    }
                }
            }
        }
        return new FileBufferFile(fo);
    }

    public static FileBuffer createFileBuffer(FileObject fo) {
        Parameters.notNull("null file object", fo); // NOI18N
        if (fo.isValid()) {
            try {
                DataObject dao = DataObject.find(fo);
                if (dao.isModified()) {
                    EditorCookie editor = dao.getLookup().lookup(EditorCookie.class);
                    if (editor != null) {
                        Document doc = editor.getDocument();
                        if (doc != null) {
                            return new FileBufferDoc(fo, doc);
                        }
                    }
                }
            } catch (DataObjectNotFoundException e) {
                // nothing
            }
            return new FileBufferFile(fo);
        } else {
            FileSystem fs;
            try {
                fs = fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
                fs = InvalidFileObjectSupport.getDummyFileSystem();
            }
            return new FileBufferFile(InvalidFileObjectSupport.getInvalidFileObject(fs, fo.getPath()));
        }
    }

    public void onMemoryLow(LowMemoryEvent event, boolean fatal) {
        LowMemoryAlerter alerter = Lookup.getDefault().lookup(LowMemoryAlerter.class);
        if (alerter != null) {
            alerter.alert(event, fatal);
        }
    }

    /*package*/ Collection<APTFileBuffer> getUnsavedBuffers() {
        Set<APTFileBuffer> res = new HashSet<>();
        for (Collection<BufAndProj> coll : buffers.values()) {
            for (BufAndProj bnp : coll) {
                res.add(bnp.buffer);
            }
        }
        return res;
    }

    private static final class BufAndProj {

        public BufAndProj(FileBuffer buffer, ProjectBase project, long lastModified) {
            assert buffer != null : "null buffer";
            this.buffer = buffer;
            assert project != null : "null project";
            this.project = project;
            this.lastModified = lastModified;
        }
        public final FileBuffer buffer;
        public final ProjectBase project;
        public final long lastModified;
    }

    private final Map<DataObject, Collection<BufAndProj>> buffers = new ConcurrentHashMap<>();

    // remove as soon as TraceFlags.USE_PARSER_API becomes always true
    private class ModifiedObjectsChangeListener implements ChangeListener {

        private Collection<BufAndProj> getBufNP(DataObject dao) {
            Collection<BufAndProj> bufNPcoll = buffers.get(dao);
            return (bufNPcoll == null) ? Collections.<BufAndProj>emptyList() : bufNPcoll;
        }

        private void addBufNP(DataObject dao, BufAndProj bufNP) {
            Collection<BufAndProj> bufNPcoll = buffers.get(dao);
            if (bufNPcoll == null) {
                bufNPcoll = new ArrayList<>();
                buffers.put(dao, bufNPcoll);
            }
            bufNPcoll.add(bufNP);
        }

        // TODO: need to change implementation when ataObject will contain correct cookie
        private void editStart(DataObject curObj) {
            ModelImpl model = theModel;
            if (model == null) {
                return;
            }
            if (!curObj.isValid()) {//IZ#114182
                return;
            }
            NativeFileItemSet set = curObj.getLookup().lookup(NativeFileItemSet.class);
            if (set == null) {
                set = findCanonicalSet(curObj);
            }

            if (set != null) {
                if (set.isEmpty()) {
                    // we have native file, but with empty set
                    if (CndUtils.isDebugMode() || CndUtils.isUnitTestMode()) {
                        CsmFile csmFile = CsmModelAccessor.getModel().findFile(FSPath.toFSPath(curObj.getPrimaryFile()), false, false);
                        CndUtils.assertTrueInConsole(csmFile == null, "WARNING: can not switch buffer due to empty NativeFileItemSet for being edited ", csmFile);
                    }
                    EditorCookie editor = curObj.getLookup().lookup(EditorCookie.class);
                    Document doc = editor != null ? editor.getDocument() : null;
                    if (doc != null && doc.getProperty(CsmUtilities.CND_REFACTORING_MARKER) != Boolean.TRUE) {
                        FileObject primaryFile = curObj.getPrimaryFile();
                        long lastModified = primaryFile.lastModified().getTime();
                        final FileBufferDoc buffer = new FileBufferDoc(primaryFile, doc);
                        ProjectBase csmProject = (ProjectBase) CsmUtilities.getCsmProject(doc);
                        if (csmProject != null) { // this could be null when code assistance is turned off for project
                            addBufNP(curObj, new BufAndProj(buffer, csmProject, lastModified));
                            csmProject.onFileEditStart(buffer, null);
                        }
                    }
                } else {
                    EditorCookie editor = curObj.getLookup().lookup(EditorCookie.class);
                    Document doc = editor != null ? editor.getDocument() : null;
                    if (doc != null && doc.getProperty(CsmUtilities.CND_REFACTORING_MARKER) != Boolean.TRUE) {
                        FileObject primaryFile = curObj.getPrimaryFile();
                        long lastModified = primaryFile.lastModified().getTime();
                        final FileBufferDoc buffer = new FileBufferDoc(primaryFile, doc);

                        for (NativeFileItem nativeFile : set.getItems()) {
                            ProjectBase csmProject = (ProjectBase) model.getProject(nativeFile.getNativeProject());
                            if (csmProject != null) { // this could be null when code assistance is turned off for project
                                addBufNP(curObj, new BufAndProj(buffer, csmProject, lastModified));
                                csmProject.onFileEditStart(buffer, nativeFile);
                            }
                        }
                    } else {
    //                    System.err.println("skip unnecessary switch of buffers");
                    }
                }
            }
        }

        private boolean isCndDataObject(FileObject fo) {
            String type = fo.getMIMEType();
            return MIMENames.isFortranOrHeaderOrCppOrC(type);
        }

        private NativeFileItemSet findCanonicalSet(DataObject curObj) {
            FileObject fo = curObj.getPrimaryFile();
            if (fo != null && isCndDataObject(fo)) {
                try {
                    fo = CndFileUtils.getCanonicalFileObject(fo);
                    curObj = DataObject.find(fo);
                    return curObj.getLookup().lookup(NativeFileItemSet.class);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return null;
        }

//        private void editEnd(DataObject curObj) {
//            // TODO: some weird logic. New FileBufferFile should be created
//            // instead.
//
//	    BufAndProj bufNP = (BufAndProj) buffers.get(curObj);
//	    if( bufNP != null ) {
//                bufNP.project.onFileEditEnd(bufNP.buffer);
//            }
//        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (TraceFlags.DEBUG) {
                traceDataObjectRegistryStateChanged(e);
            }
            if (e != null) {

                DataObject[] objs = DataObject.getRegistry().getModified();

                Set<DataObject> toDelete = new HashSet<>();

                // find all files, which stopped editing
                for (Iterator<DataObject> iter = buffers.keySet().iterator(); iter.hasNext();) {
                    DataObject dao = iter.next();
                    if (!contains(objs, dao)) {
                        for (BufAndProj bufNP : getBufNP(dao)) {
                            if (bufNP != null) {
                                final FileBuffer fileBuffer = createFileBuffer(dao);
                                long lastModified = fileBuffer.lastModified();
                                // removing old doc buffer and creating new one
                                bufNP.project.onFileEditEnd(fileBuffer, null, bufNP.lastModified == lastModified);
                            } else {
                                System.err.println("no buffer for " + dao);
                            }
                        }
                        toDelete.add(dao);
                    }
                }

                // now remove these files from bufres map
                for (Iterator<DataObject> iter = toDelete.iterator(); iter.hasNext();) {
                    buffers.remove(iter.next());
                }

                // add new buffers
                for (int i = 0; i < objs.length; i++) {
                    if (!buffers.containsKey(objs[i])) {
                        editStart(objs[i]);
                    }
                }
            }
        }

        private boolean contains(Object[] objs, Object o) {
            for (int i = 0; i < objs.length; i++) {
                if (objs[i].equals(o)) {
                    return true;
                }
            }
            return false;
        }

        private void clean() {
            buffers.clear();
        }
    }

    public static void traceDataObjectRegistryStateChanged(ChangeEvent e) {
        Diagnostic.trace("state of registry changed:"); // NOI18N
        Diagnostic.indent();
        if (e != null) {
            DataObject[] objs = DataObject.getRegistry().getModified();
            if (objs.length == 0) {
                Diagnostic.trace("all objects are saved"); // NOI18N
            } else {
                Diagnostic.trace("set of edited objects:"); // NOI18N
                for (int i = 0; i < objs.length; i++) {
                    DataObject curObj = objs[i];
                    Diagnostic.trace("object " + i + ":" + curObj.getName()); // NOI18N
                    Diagnostic.indent();
                    Diagnostic.trace("with file: " + curObj.getPrimaryFile()); // NOI18N
                    NativeFileItemSet set = curObj.getLookup().lookup(NativeFileItemSet.class);
                    if (set == null) {
                        Diagnostic.trace("NativeFileItemSet == null"); // NOI18N
                    } else {
                        Diagnostic.trace("NativeFileItemSet:"); // NOI18N
                        for (NativeFileItem item : set.getItems()) {
                            Diagnostic.trace("\t" + item.getNativeProject().getProjectDisplayName()); // NOI18N
                        }
                    }
                    EditorCookie editor = curObj.getLookup().lookup(EditorCookie.class);
                    Diagnostic.trace("has editor support: " + editor); // NOI18N
                    Document doc = editor != null ? editor.getDocument() : null;
                    Diagnostic.trace("with document: " + doc); // NOI18N
                    Diagnostic.unindent();
                }
            }
        } else {
            Diagnostic.trace("no additional info from event object"); // NOI18N
        }
        Diagnostic.unindent();
    }

    public void suspendDeleteEvents() {
        if (!TraceFlags.MERGE_EVENTS) {
            if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                ExternalUpdateListener.LOG.info("External updates: suspendDeleteEvents");
            }
            if (fileChangeListener != null) {
                fileChangeListener.suspendRemoves();
            }
        }
    }

    public void resumeDeleteEvents() {
        if (!TraceFlags.MERGE_EVENTS) {
            if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                ExternalUpdateListener.LOG.info("External updates: resumeDeleteEvents");
            }
            if (fileChangeListener != null) {
                fileChangeListener.resumeRemoves();
            }
        }
    }
}
