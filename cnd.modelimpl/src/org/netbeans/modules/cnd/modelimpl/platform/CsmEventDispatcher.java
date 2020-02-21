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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 * File events come from
 *  1) FileChangeListener 
 *  2) NativeProjectItemsListener 
 *  3) CndIndexer
 * 
 */
public final class CsmEventDispatcher {
    
    // static (singleton) members
    
    private static final CsmEventDispatcher instance = new CsmEventDispatcher();

    public static CsmEventDispatcher getInstance() {
        return instance;
    }
    
    // instance members
    
    private final FileListener fileListener;
    private final ItemListener itemListener;
    private final CndIndexer.Delegate indexListener;

    private final Object listenersLock = new Object();
    private final HashMap<NativeProject, CsmEventListener> listeners = new HashMap<>();

    private final RequestProcessor RP = new RequestProcessor(getClass().getSimpleName(), 1);
    private final RequestProcessor.Task task = RP.create(new Worker());

    private final Object eventsLock = new Object();
    private LinkedList<CsmEvent> eventsQueue = new LinkedList<>();

    private CsmEventDispatcher() {
        itemListener = new ItemListener();
        boolean listenOnlyRemoteFiles = CndTraceFlags.USE_INDEXING_API;
        fileListener = new FileListener(listenOnlyRemoteFiles);
        indexListener = new IndexListener();
    }

    /*package*/ void startup() {
        if (CndTraceFlags.USE_INDEXING_API) {
            CndIndexer.setDelegate(indexListener);
        }
        // in USE_INDEXING_API mode we still need listener for remote
        CndFileSystemProvider.addFileChangeListener(fileListener);
    }

    /*package*/ void shutdown() {
        if (CndTraceFlags.USE_INDEXING_API) {
            CndFileSystemProvider.removeFileChangeListener(fileListener);
        } else {
            CndIndexer.setDelegate(null);
        }
        // todo: clean up events ,etc
    }

    // shouldn't it be ProjectImpl ?
    public void registerProject(ProjectBase project) {        
        Object pp = project.getPlatformProject();
        if (pp instanceof NativeProject) {
            NativeProject nativeProject = (NativeProject) pp;
            synchronized (listenersLock) {
                CndUtils.assertNull(listeners.get(nativeProject), "Listener already exist for " + nativeProject); //NOI18N
                CsmEventListener listener = new CsmEventListener(project);
                listeners.put(nativeProject, listener);                
                nativeProject.addProjectItemsListener(itemListener);
            }
        } else {
            // I wouldn't say its totally incorrect, just not we expect => something goes wrong => warn
            CndUtils.assertTrue(false, "platform ptoject should be NativeProject, but is " + pp + " for " + project); //NOI18N
        }
    }
    
    // shouldn't it be ProjectImpl ?
    public void unregisterProject(ProjectBase project) {
        Object pp = project.getPlatformProject();
        if (pp instanceof NativeProject) {
            NativeProject nativeProject = (NativeProject) pp;
            synchronized (listenersLock) {
                nativeProject.removeProjectItemsListener(itemListener);
                listeners.remove(nativeProject);
            }
        } else {
            // I wouldn't say its totally incorrect, just not we expect => something goes wrong => warn
            CndUtils.assertTrue(false, "platform ptoject should be NativeProject, but is " + pp + " for " + project); //NOI18N
        }
    }
    
    public void enableListening(ProjectBase project, boolean enable) {
        Object pp = project.getPlatformProject();
        if (pp instanceof NativeProject) {
            synchronized (listenersLock) {
                CsmEventListener listener = listeners.get((NativeProject) pp);
                if (listener != null) {
                    listener.enableListening(enable);
                }
            }
        }
    }

    private void registerEvent(CsmEvent.Kind kind, FileObject fileObject) {
        synchronized (eventsLock) {
            eventsQueue.addLast(CsmEvent.createFileEvent(kind, fileObject));
        }
        task.schedule(0);
    }
    
    private void registerEvents(CsmEvent... events) {
        synchronized (eventsLock) {
            for (CsmEvent e : events) {
                eventsQueue.addLast(e);
            }            
        }
        task.schedule(0);
    }

    private void registerEvents(CsmEvent.Kind kind, List<NativeFileItem> items) {
        synchronized (eventsLock) {
            for (NativeFileItem item : items) {
                eventsQueue.addLast(CsmEvent.createItemEvent(kind, item));
            }            
        }
        task.schedule(0);
    }
    
    private class Worker implements Runnable {
        @Override
        public void run() {
            while (true) {
                LinkedList<CsmEvent> curEvents;
                synchronized (eventsLock) {
                    if (eventsQueue.isEmpty()) {
                        return;
                    }
                    curEvents = eventsQueue;
                    eventsQueue = new LinkedList<>();
                }
                for (CsmEvent event : curEvents) {
                    processEvent(event);
                }
            }            
        }        
    }

    private void processEvent(CsmEvent event) {
        final ModelImpl model = ModelSupport.instance().getModel();
        if (model == null) {
            return;
        }
        CsmEvent.trace("dispatching %s", event); // NOI18N
        switch (event.getKind()) {
            case ITEM_ADDED:
            case ITEM_REMOVED:
            case ITEM_PROPERTY_CHANGED:
            case ITEM_RENAMED_DELETED:
            case ITEM_RENAMED_CREATED:
            {
                NativeFileItem item = event.getNativeFileItem();
                CndUtils.assertNotNullInConsole(item, "NativeFileItem should not be null: " + event); //NOI18N
                if (item != null) {
                    dispatch(event, item.getNativeProject());
                }
                break;
            }
            case FILE_DELETED:
            case FILE_CREATED:
            case FILE_RENAMED_CREATED:
            case FILE_RENAMED_DELETED:
            case FILE_CHANGED:
            case FILE_INDEXED:
            {
                FileObject fo = event.getFileObject();
                CndUtils.assertNotNullInConsole(fo, "FileObject should not be null: " + event); //NOI18N
                if (fo != null) {
                    CsmFile[] files = model.findFiles(FSPath.toFSPath(fo), false, false);
                    Set<ProjectBase> handledProjects = new HashSet<>();
                    for (int i = 0; i < files.length; ++i) {
                        FileImpl file = (FileImpl) files[i];
                        ProjectBase project = file.getProjectImpl(true);
                        if (project != null) {
                            handledProjects.add(project);
                            CsmEvent.Kind kind = (event.getKind() == CsmEvent.Kind.FILE_INDEXED) ? CsmEvent.Kind.FILE_CHANGED : event.getKind();
                            dispatch(CsmEvent.createFileEvent(kind, fo), project);
                        }
                    }
                    if (event.getKind() == CsmEvent.Kind.FILE_CREATED || event.getKind() == CsmEvent.Kind.FILE_INDEXED) {
                        Collection<CsmProject> ownerCsmProjects = CsmUtilities.getOwnerCsmProjects(fo);
                        for (CsmProject prj : ownerCsmProjects) {
                            if (prj instanceof ProjectBase) {
                                ProjectBase project = (ProjectBase) prj;
                                if (!handledProjects.contains(project)) {
                                    dispatch(event.getKind() == CsmEvent.Kind.FILE_INDEXED
                                            ? CsmEvent.createFileEvent(CsmEvent.Kind.FILE_CREATED, fo)
                                            : event, project);
                                }
                            }
                        }
                    }
                }
                break;
            }
            case ITEMS_ALL_PROPERTY_CHANGED:
                CndUtils.assertNotNullInConsole(event.getNativeProject(), "NativeProject should not be null: " + event); //NOI18N
                dispatch(event, event.getNativeProject());
                break;
            case PROJECT_DELETED:
            {
                NativeProject nativeProject = event.getNativeProject();
                CndUtils.assertNotNullInConsole(nativeProject, "NativeProject should not be null: " + event); //NOI18N
                if (nativeProject != null) {
                    dispatch(event, nativeProject);
                }
                break;
            }
            case FILES_IN_SOURCE_ROOT_DELETED:
            {
                FileObject fo = event.getFileObject();
                CndUtils.assertNotNullInConsole(fo, "FileObject should not be null: " + event); //NOI18N
                if (fo != null) {
                    Collection<CsmProject> projects = CsmUtilities.getOwnerCsmProjects(fo);
                    for (CsmProject project : projects) {
                        dispatch(event, project);
                    }
                }
                break;
            }
            case FOLDER_CREATED:
            case FILE_ATTRIBUTE_CHANGED:
            case NULL:
                // ignore assert?
                break;
            default:
                throw new AssertionError(event.getKind().name());
        }

        //<editor-fold defaultstate="collapsed" desc="Previous Implementation">
        /*
        NativeFileItem item = event.getNativeFileItem();
        if (item != null) {
            // this is an item event
            dispatch(event, item.getNativeProject());
        } else {
            // this is a file event
            FileObject fo = event.getFileObject();
            CndUtils.assertNotNull(fo, "Event does not contain neither FileObject nor NativeFileItem"); //NOI18N

            if (event.getKind() == CsmEvent.Kind.FILES_IN_SOURCE_ROOT_DELETED) {
                Collection<CsmProject> projects = CsmUtilities.getOwnerCsmProjects(fo);
                for (CsmProject project : projects) {
                    dispatch(event, project);
                }
            } else {
                CsmFile[] files = model.findFiles(FSPath.toFSPath(fo), false, false);
                Set<ProjectBase> handledProjects = new HashSet<>();
                for (int i = 0; i < files.length; ++i) {
                    FileImpl file = (FileImpl) files[i];
                    ProjectBase project = file.getProjectImpl(true);
                    if (project != null) {
                        handledProjects.add(project);
                        dispatch(event, project);
                    }
                }
                if (event.getKind() == CsmEvent.Kind.FILE_CREATED || event.getKind() == CsmEvent.Kind.FILE_INDEXED) {
                    Collection<CsmProject> ownerCsmProjects = CsmUtilities.getOwnerCsmProjects(fo);
                    for (CsmProject prj : ownerCsmProjects) {
                        if (prj instanceof ProjectBase) {
                            ProjectBase project = (ProjectBase) prj;
                            if (!handledProjects.contains(project)) {
                                dispatch(event.getKind() == CsmEvent.Kind.FILE_INDEXED ?
                                        CsmEvent.createFileEvent(CsmEvent.Kind.FILE_CREATED, fo) :
                                        event, project);
                            }
                        }
                    }
                }
            }
        }
        */
        //</editor-fold>
    }

    private void dispatch(CsmEvent event, CsmProject project) {
        CsmEventListener listener = getListener(project);
        if (listener != null) {
            listener.fireEvent(event);
        } else {
            CsmEvent.LOG.log(Level.FINEST, "Skipping event {0}", event);
        }
    }
    
    private void dispatch(CsmEvent event, NativeProject project) {
        CsmEventListener listener = getListener(project);
        if (listener != null) {
            listener.fireEvent(event);
        } else {
            CsmEvent.LOG.log(Level.FINEST, "Skipping event {0}", event);
        }
    }
    
    private CsmEventListener getListener(CsmProject project) {
        Object pp = project.getPlatformProject();
        if (pp instanceof NativeProject) {
            synchronized (listenersLock) {
                return listeners.get((NativeProject) pp);
            }
        } 
        return null;
    }

    private CsmEventListener getListener(NativeProject project) {
        if (project != null) {
            synchronized (listenersLock) {
                return listeners.get(project);
            }
        }
        return null;
    }

    private boolean isCOrCpp(FileObject fo) {
        String mime = fo.getMIMEType();
        if (mime == null) {
            mime = FileUtil.getMIMEType(fo);
            if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                CsmEvent.LOG.log(Level.INFO, "External updates: MIME resolved: {0}", mime);
            }
        }
        return MIMENames.isFortranOrHeaderOrCppOrC(mime);
    }
    
    private boolean isCOrCppOrInvalid(FileObject fo) {
        return !fo.isValid() || isCOrCpp(fo);
    }
    
    private class ItemListener implements NativeProjectItemsListener {

        @Override
        public void filesAdded(List<NativeFileItem> items) {
            ArrayList<NativeFileItem> list = new ArrayList<>();
            for (NativeFileItem item : items) {
                // MakeProject sends such events for excluded items, but model never reacts
                // So it's better to filter them out at the ver beginning.
                // (Otherwise on LLVM we add 500 or more events to the queue)
                // And that's how old NativeProjectListenerImpl behaved
                if (!item.isExcluded()) {
                    list.add(item);
                }
            }            
            if (!list.isEmpty()) {
                registerEvents(CsmEvent.Kind.ITEM_ADDED, list);
            }            
        }

        @Override
        public void filesRemoved(List<NativeFileItem> items) {
            registerEvents(CsmEvent.Kind.ITEM_REMOVED, items);
        }

        @Override
        public void filesPropertiesChanged(List<NativeFileItem> items) {
            registerEvents(CsmEvent.Kind.ITEM_PROPERTY_CHANGED, items);
        }

        @Override
        public void filesPropertiesChanged(NativeProject nativeProject) {
            registerEvents(CsmEvent.createProjectEvent(CsmEvent.Kind.ITEMS_ALL_PROPERTY_CHANGED, nativeProject));
        }

        @Override
        public void fileRenamed(String oldPath, NativeFileItem newFileIetm) {
            registerEvents(
                    CsmEvent.createItemEvent(CsmEvent.Kind.ITEM_RENAMED_DELETED, newFileIetm, oldPath),
                    CsmEvent.createItemEvent(CsmEvent.Kind.ITEM_RENAMED_CREATED, newFileIetm, oldPath));
        }

        @Override
        public void projectDeleted(NativeProject nativeProject) {
            registerEvents(CsmEvent.createProjectEvent(CsmEvent.Kind.PROJECT_DELETED, nativeProject));
        }

        @Override
        public void fileOperationsStarted(NativeProject nativeProject) {
            CsmEventListener listener = getListener(nativeProject);
            if (listener != null) {
                listener.suspend();
            }
        }

        @Override
        public void fileOperationsFinished(NativeProject nativeProject) {
            CsmEventListener listener = getListener(nativeProject);
            if (listener != null) {
                listener.resume();
            }
        }        
    }
    
    private class FileListener implements FileChangeListener {

        private final boolean remoteOnly;

        public FileListener(boolean remoteOnly) {
            this.remoteOnly = remoteOnly;
        }

        private boolean accept(FileEvent fe) {
            if (remoteOnly) {
                FileObject fo = fe.getFile();
                if (fo != null) { // that's a real paranoia
                    return CndFileUtils.isRemoteFileSystem(fo);
                }
            }
            return true;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            // ignore
            //if (accept(fe)) {
            //}
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            if (accept(fe)) {
                if (isCOrCppOrInvalid(fe.getFile())) {
                    registerEvent(CsmEvent.Kind.FILE_CREATED, fe.getFile());
                }
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            if (accept(fe)) {
                if (isCOrCppOrInvalid(fe.getFile())) {
                    registerEvent(CsmEvent.Kind.FILE_CHANGED, fe.getFile());
                }
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (accept(fe)) {
                if (isCOrCppOrInvalid(fe.getFile())) {
                    registerEvent(CsmEvent.Kind.FILE_DELETED, fe.getFile());
                }
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            if (accept(fe)) {
                FileObject fo = fe.getFile();
                if (isCOrCppOrInvalid(fo)) {
                    FSPath newPath = FSPath.toFSPath(fo);
                    String strPrevExt = (fe.getExt() == null || fe.getExt().isEmpty()) ? "" : "." + fe.getExt(); // NOI18N
                    String strPrevPath = CndPathUtilities.getDirName(newPath.getPath()) + '/' + fe.getName() + strPrevExt; // NOI18N
                    FSPath prevPath = new FSPath(newPath.getFileSystem(), strPrevPath);
                    FileObject removedFO = InvalidFileObjectSupport.getInvalidFileObject(prevPath.getFileSystem(), prevPath.getPath());
                    registerEvents(
                            CsmEvent.createFileEvent(CsmEvent.Kind.FILE_RENAMED_DELETED, removedFO),
                            CsmEvent.createFileEvent(CsmEvent.Kind.FILE_RENAMED_CREATED, fo));
                }
            }
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // nothing
            //if (accept(fe)) {
            //}
        }        
    }
    
    private class IndexListener implements CndIndexer.Delegate {

        @Override
        public void index(FileObject file) {
            registerEvent(CsmEvent.Kind.FILE_INDEXED, file);
        }

        @Override
        public void removed(FileObject root) {
            registerEvent(CsmEvent.Kind.FILES_IN_SOURCE_ROOT_DELETED, root); 
        }
    }    
}

