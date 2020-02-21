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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 */
/*package*/ final class CsmEventListener {

    private final ProjectBase project;
    private final NativeProject nativeProject;
    private volatile boolean enabledEventsHandling = true;

    //private static final RequestProcessor RP = new RequestProcessor(CsmEventListener.class.getSimpleName());
    private final RequestProcessor.Task task;
    private final Object eventsLock = new Object();
    private int suspendCount = 0;
    private Exception suspendResumes = null;

    private HashMap<String, CsmEvent> events = new LinkedHashMap<>();

    private static final CsmEvent NULL = CsmEvent.createEmptyEvent(CsmEvent.Kind.NULL);

    public CsmEventListener(ProjectBase project) {
        assert project.getPlatformProject() instanceof NativeProject;
        this.project = project;
        this.nativeProject = (NativeProject) project.getPlatformProject();
        this.task = ModelSupport.instance().getModel().createModelTask(new Worker(), getClass().getSimpleName());
    }

    public ProjectBase getProject() {
        return project;
    }

    public NativeProject getNativeProject() {
        return nativeProject;
    }
    
    public void enableListening(boolean enable) {
        if (TraceFlags.TIMING) {
            CsmEvent.LOG.log(Level.INFO, "\n%{0} ProjectListeners {1}...", new Object[] {enable ? "enable" : "disable",
                    nativeProject.getProjectDisplayName()});
        }
        enabledEventsHandling = enable;
    }

    void fireEvent(CsmEvent event) {
        switch (event.getKind()) {
            case FILE_INDEXED:
            case FOLDER_CREATED:
            case FILE_ATTRIBUTE_CHANGED:
            case NULL:
                return;
        }
        CsmEvent.trace("dispatched %s to %s", event, project); // NOI18N
        checkEvent(event);
        String path = event.getPath();
        synchronized (eventsLock) {
            CsmEvent prev = events.get(path);
            CsmEvent converted = convert(prev, event);
            if (TraceFlags.TRACE_EXTERNAL_CHANGES && prev != null && prev != NULL) {
                if (prev.getKind() != converted.getKind() || !prev.getPath().equals(converted.getPath())) {
                    CsmEvent.trace("converted %s to %s", prev, converted); // NOI18N
                }
            }
            events.put(path, converted);
            if (converted.getKind() == CsmEvent.Kind.NULL) {
                return;
            }
        }
        task.schedule(0); // TODO: fe.runWhenDeliveryOver(taskScheduler); ???
    }
    
    void checkEvent(CsmEvent event) {
        if (CndUtils.isDebugMode()) {
            FileObject fo = event.getFileObject();
            if (fo != null) {
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (!fs.equals(InvalidFileObjectSupport.getDummyFileSystem()) && !fs.equals(nativeProject.getFileSystem())) {
                        CndUtils.assertTrue(false, "Filesystem differs CsmEvent filesystem is " + fs + //NOI18N
                                ", project filesystem is " + nativeProject.getFileSystem()); //NOI18N
                    }
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            NativeProject np = event.getNativeProject();
            if (np == null) {
                NativeFileItem item = event.getNativeFileItem();
                if (item != null) {
                    np = item.getNativeProject();
                }
            }
            if (np != null) {
                assert np.equals(nativeProject);
            }
        }
    }

    void resume() {
        boolean schedule;
        synchronized (eventsLock) {
            if (CndUtils.isDebugMode()) {
                if (suspendCount == 1) {
                    CndUtils.assertTrue(suspendResumes != null, "resume without suspend " + suspendCount);
                    suspendResumes = new Exception("2: resume with suspendCount==1 [" + project + "] enabledEventsHandling=" + enabledEventsHandling, suspendResumes); // NOI18N
                } else if (suspendCount <= 0) {
                    CndUtils.printStackTraceOnce(new Exception("3: resume with suspendCount==" + suspendCount + " [" + project + "] enabledEventsHandling=" + enabledEventsHandling, suspendResumes)); // NOI18N
                }
            }
            suspendCount--;
            if (suspendCount < 0) {
                suspendCount = 0;
            }
            schedule = (suspendCount == 0);
        }
        if (schedule) {
            task.schedule(0);
        }
    }

    void suspend() {
        synchronized (eventsLock) {
            if (CndUtils.isDebugMode()) {
                if (suspendCount == 0) {
                    suspendResumes = new Exception("1: suspend when suspendCount==0 [" + project + "] enabledEventsHandling=" + enabledEventsHandling); // NOI18N
                }
                CndUtils.assertTrue(suspendCount >= 0, "suspend with " + suspendCount);
            }            
            suspendCount++;
        }
    }

    /*package*/void flush() {
        task.schedule(0);
        task.waitFinished();
    }

    //<editor-fold defaultstate="collapsed" desc="old impl">
    /***
   private void processEvents(Collection<CsmEvent> events) {
        if (!enabledEventsHandling) {
            CsmEvent.trace("events processing disabled, skipping %d events", events.size());
            return;
        }
        CsmEvent.trace("processing %d events", events.size());
        boolean checkForRemoved = false;
        for (CsmEvent event : events) {
            CsmEvent.trace("processing %s", event);
            switch (event.getKind()) {
                case FILE_DELETED:
                case ITEM_REMOVED:
                case FILE_RENAMED_DELETED:
                case ITEM_RENAMED_DELETED:
                    checkForRemoved = true;
                    break;
                case FILE_CREATED:
                case FILE_RENAMED_CREATED:
                    project.onFileObjectExternalCreate(event.getFileObject());
                    break;
                case ITEM_ADDED:
                    project.onFileItemsAdded(Arrays.asList(event.getNativeFileItem()));
                    break;
                case FOLDER_CREATED:
                    // nothing
                    break;
                case FILE_CHANGED:
                    project.findFile(event.getPath(), false, false);
                    break;
                case FILE_ATTRIBUTE_CHANGED:
                    // nothing
                    break;
                case ITEM_PROPERTY_CHANGED:
                    project.onFileItemsPropertyChanged(Arrays.asList(event.getNativeFileItem()), false);
                    break;
                case ITEMS_ALL_PROPERTY_CHANGED:
                    List<NativeFileItem> items = new ArrayList<>();
                    for (NativeFileItem item : nativeProject.getAllFiles()) {
                        if (!item.isExcluded()) {
                            switch (item.getLanguage()) {
                                case C:
                                case CPP:
                                case FORTRAN:
                                    items.add(item);
                                    break;
                                default:
                                    break;
                            }
                        }
                        project.onFileItemsPropertyChanged(items, true);
                    }
                    break;
                case ITEM_RENAMED_CREATED:
                    project.onFileItemRenamed(event.getOldPath(), event.getNativeFileItem());
                    break;
                case PROJECT_DELETED:
                    RepositoryUtils.onProjectDeleted(nativeProject);
                    break;
                case FILES_IN_SOURCE_ROOT_DELETED:
                    checkForRemoved = true;
                    break;
                case FILE_INDEXED:
                    CndUtils.assertTrue(false, "FILE_INDEXED event should never reach end listener"); //NOI18N
                    break;
                case NULL:
                    // nothing
                    break;
                default:
                    throw new AssertionError(event.getKind().name());
            }
            if (checkForRemoved) {
                project.checkForRemoved();
            }
        }
    }
     */
    //</editor-fold>

    private void processEvents(Collection<CsmEvent> events) {
        if (!enabledEventsHandling) {
            CsmEvent.trace("events processing disabled, skipping %d events", events.size()); // NOI18N
            return;
        }
        CsmEvent.trace("processing %d events", events.size()); // NOI18N

        boolean projectDeleted = false;
        boolean checkForRemoved = false;
        boolean projectRemoved = false;
        boolean allPropertiesChanged = false;
        List<FileObject> createdFiles = new ArrayList<>();
        List<NativeFileItem> addedItems = new ArrayList<>();
        List<NativeFileItem> changedItemProps = new ArrayList<>();
        List<String> changedFiles = new ArrayList<>();
        List<CsmEvent> renamedCreatedItems = new ArrayList<>();

        for (CsmEvent event : events) {
            CsmEvent.trace("processing %s", event); // NOI18N
            switch (event.getKind()) {
                case FILE_DELETED:
                case ITEM_REMOVED:
                case FILE_RENAMED_DELETED:
                case ITEM_RENAMED_DELETED:
                    checkForRemoved = true;
                    break;
                case FILE_CREATED:
                case FILE_RENAMED_CREATED:
                    createdFiles.add(event.getFileObject());
                    break;
                case ITEM_ADDED:
                    addedItems.add(event.getNativeFileItem());
                    break;
                case FOLDER_CREATED:
                    // nothing
                    break;
                case FILE_CHANGED:
                    changedFiles.add(event.getPath());
                    break;
                case FILE_ATTRIBUTE_CHANGED:
                    // nothing
                    break;
                case ITEM_PROPERTY_CHANGED:
                    changedItemProps.add(event.getNativeFileItem());
                    break;
                case ITEMS_ALL_PROPERTY_CHANGED:
                    allPropertiesChanged = true;
                    break;
                case ITEM_RENAMED_CREATED:
                    renamedCreatedItems.add(event);
                    break;
                case PROJECT_DELETED:
                    projectDeleted = true;
                    break;
                case FILES_IN_SOURCE_ROOT_DELETED:
                    checkForRemoved = true;
                    break;
                case FILE_INDEXED:
                    CndUtils.assertTrue(false, "FILE_INDEXED event should never reach end listener"); //NOI18N
                    break;
                case NULL:
                    // nothing
                    break;
                default:
                    throw new AssertionError(event.getKind().name());
            }
        }

        if (projectDeleted) {
            RepositoryUtils.onProjectDeleted(nativeProject);
            // all other events aren't relevant any more
            return;
        }

        if (!renamedCreatedItems.isEmpty()) {
            for (CsmEvent e : renamedCreatedItems) {
                project.onFileItemRenamed(e.getOldPath(), e.getNativeFileItem());
            }
        }

        if (allPropertiesChanged) {
            List<NativeFileItem> items = new ArrayList<>();
            for (NativeFileItem item : nativeProject.getAllFiles()) {
                if (!item.isExcluded()) {
                    switch (item.getLanguage()) {
                        case C:
                        case CPP:
                        case FORTRAN:
                            items.add(item);
                            break;
                        default:
                            break;
                    }
                }
            }
            if (false/*part of AllFiles*/) items.addAll(nativeProject.getStandardHeadersIndexers());
            project.onFileItemsPropertyChanged(items, true);
            changedItemProps.clear();
        }

        if(!changedFiles.isEmpty()) {
            for (String path : changedFiles) {
                CsmFile csmFile = project.findFile(path, false, false);
                if (csmFile != null) {
                    project.onFileImplExternalChange((FileImpl) csmFile);
                }
            }
        }
        if (!changedItemProps.isEmpty()) {
            project.onFileItemsPropertyChanged(changedItemProps, false);
        }
        if (!createdFiles.isEmpty()) {
            project.onFileObjectExternalCreate(createdFiles);
        }
        if (!addedItems.isEmpty()) {
            project.onFileItemsAdded(addedItems);
        }
        if (checkForRemoved) {
            project.checkForRemoved();
        }
    }

/*-------------------------------------------------------------------------------------------------------------------------------------------------
                                     (prevKind)
(curKind) | F/DEL   | I/RM    | F/CR     | I/ADD    | F/REN_CR | I/REN_CR | F/REN_DL | I/REN_DL | F/CH     | I/PROP   | I/ALLPROP | F/RT_DEL | P/DEL |
-----------------------------------------------------------------------------------------------------------------------------------------------------
F/DEL     | F/DEL   | F/DEL   | null     | null     | null     | null     | assert   | assert   | F/DEL    | F/DEL    | assert    | assert   | P/DEL |
I/RM      | I/RM    | I/RM    | null     | null     | null     | null     | I/RM     | I/RM     | I/RM     | I/RM     | assert    | assert   | P/DEL |
F/CR      | F/CH    | F/CH    | F/CR     | I/ADD    | assert   | I_REN_CR | F/CH     | F/CH     | F/CH     | I/PROP   | assert    | assert   | P/DEL |
I/ADD     | I/ADD   | I/PROP  | I/ADD    | I/ADD    | I/ADD    | I/ADD    | I/ADD    | I/ADD    | I/ADD    | I/ADD    | assert    | assert   | P/DEL |
F/REN_CR  | F/CH    | F/CH    | assert   | I/ADD    | F/REN_CR | I/REN_CR | F/CH     | F/CH     | assert   | I/REN_CR | assert    | assert   | P/DEL |
I/REN_CR  | I/PROP  | I/PROP  | I/REN_CR | I/ADD    | I/REN_CR | I/REN_CR | I/PROP   | I/PROP   | I/REN_CR | I/REN_CR | assert    | assert   | P/DEL |
F/REN_DL  | assert  | F/REN_DL| null     | null     | null     | null     | assert   | assert   | F/REN_DL | F/REN_DL | assert    | assert   | P/DEL |
I/REN_DL  | I/REN_DL| I/REN_DL| null     | null     | null     | null     | I/REN_DL | I/REN_DL | I/REN_DL | I/REN_DL | assert    | assert   | P/DEL |
F/CH      | assert  | F/CH?   | F/CR     | I/ADD    | F/REN_CR | I/REN_CR | assert   | assert   | F/CH     | I/PROP   | assert    | assert   | P/DEL |
I/PROP    | F/DEL   | I/RM    | I/PROP   | I/ADD    | I/PROP   | I/REN_CR | F/REN_DL | F/REN_DL | I/PROP   | I/PROP   | assert    | assert   | P/DEL |
I/ALPROP  | assert  | assert  | assert   | assert   | assert   | assert   | assert   | assert   | assert   | assert   | I/ALLPROP | I/ALLPROP| P/DEL |
F/RT_DEL  | assert  | assert  | assert   | assert   | assert   | assert   | assert   | assert   | assert   | assert   | I/ALLPROP | F/RT_DEL | P/DEL |
P/DEL     | P/DEL   | P/DEL   | P/DEL    | P/DEL    | P/DEL    | P/DEL    | P/DEL    | P/DEL    | P/DEL    | P/DEL    | P/DEL     | P/DEL    | P/DEL |
---------------------------------------------------------------------------------------------------------------------------------------------------*/

    private static CsmEvent convert(CsmEvent prev, CsmEvent cur) {
        if (prev == null || prev.getKind() == CsmEvent.Kind.NULL) {
            return cur;
        }

        if (prev.getKind() == CsmEvent.Kind.PROJECT_DELETED) {
            return prev;
        } else if (cur.getKind() == CsmEvent.Kind.PROJECT_DELETED) {
            return cur;
        }

        switch (cur.getKind()) {
            case FILE_DELETED: //<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return cur;
                    case ITEM_REMOVED:                  return cur; // doesn't matter, processing is the same
                    case FILE_CREATED:                  return doNull();
                    case ITEM_ADDED:                    return doNull();
                    case FILE_RENAMED_CREATED:          return doNull();
                    case ITEM_RENAMED_CREATED:          return doNull();
                    case FILE_RENAMED_DELETED:          return doAssert(prev, cur); // any of these cause ProjcetBase.checkForRemoved()
                    case ITEM_RENAMED_DELETED:          return doAssert(prev, cur); // any of these cause ProjcetBase.checkForRemoved()
                    case FILE_CHANGED:                  return cur; // pozdno pit' borjomi
                    case ITEM_PROPERTY_CHANGED:         return cur; // --""--
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); /// prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); /// prev event path is a project path!
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }   //</editor-fold>   
            case FILE_CREATED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return doFileChanged(CsmEvent.Kind.FILE_CHANGED, cur.getFileObject());
                    case ITEM_REMOVED:                  return doFileChanged(CsmEvent.Kind.FILE_CHANGED, cur.getFileObject());
                    case FILE_CREATED:                  return cur;
                    case ITEM_ADDED:                    return prev; // item events are stronger
                    case FILE_RENAMED_CREATED:          return doAssert(prev, cur);
                    case ITEM_RENAMED_CREATED:          return prev; // ITEM_RENAMED_CREATED will finally cause checkForRemove and nativeItemAdded
                    case FILE_RENAMED_DELETED:          return doFileChanged(CsmEvent.Kind.FILE_CHANGED, cur.getFileObject());
                    case ITEM_RENAMED_DELETED:          return doFileChanged(CsmEvent.Kind.FILE_CHANGED, cur.getFileObject());
                    case FILE_CHANGED:                  return prev; //?
                    case ITEM_PROPERTY_CHANGED:         return prev; //?
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur); // prev event path is a project path!
                    default:    throw new AssertionError(prev.getKind());
                }//</editor-fold>
            case FILE_RENAMED_CREATED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return doFileChanged(CsmEvent.Kind.FILE_CHANGED, cur.getFileObject());
                    case ITEM_REMOVED:                  return doFileChanged(CsmEvent.Kind.FILE_CHANGED, cur.getFileObject());
                    case FILE_CREATED:                  return doAssert(prev, cur);
                    case ITEM_ADDED:                    return prev;
                    case FILE_RENAMED_CREATED:          return cur; // twice?
                    case ITEM_RENAMED_CREATED:          return prev; // the same, but item events are stonger
                    case FILE_RENAMED_DELETED:          return doFileChanged(CsmEvent.Kind.FILE_CHANGED, cur.getFileObject());
                    case ITEM_RENAMED_DELETED:          return doFileChanged(CsmEvent.Kind.FILE_CHANGED, cur.getFileObject());
                    case FILE_CHANGED:                  return doAssert(prev, cur);
                    case ITEM_PROPERTY_CHANGED:         return cur; //?
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new AssertionError(prev.getKind());
                }//</editor-fold>
            case FILE_RENAMED_DELETED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return doAssert(prev, cur);
                    case ITEM_REMOVED:                  return cur;
                    case FILE_CREATED:                  return doNull();
                    case ITEM_ADDED:                    return doNull();
                    case FILE_RENAMED_CREATED:          return doNull();
                    case ITEM_RENAMED_CREATED:          return doNull();
                    case FILE_RENAMED_DELETED:          return doAssert(prev, cur);
                    case ITEM_RENAMED_DELETED:          return doAssert(prev, cur);
                    case FILE_CHANGED:                  return cur;
                    case ITEM_PROPERTY_CHANGED:         return cur;
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new AssertionError(prev.getKind());
                }//</editor-fold>
            case FILE_CHANGED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return doAssert(prev, cur);
                    case ITEM_REMOVED:                  return cur;
                    case FILE_CREATED:                  return prev;
                    case ITEM_ADDED:                    return prev;
                    case FILE_RENAMED_CREATED:          return prev;
                    case ITEM_RENAMED_CREATED:          return prev;
                    case FILE_RENAMED_DELETED:          return doAssert(prev, cur);
                    case ITEM_RENAMED_DELETED:          return doAssert(prev, cur);
                    case FILE_CHANGED:                  return cur;
                    case ITEM_PROPERTY_CHANGED:         return prev;
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case ITEM_ADDED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return cur; //item events are stronger
                    case ITEM_REMOVED:                  return doItemChanged(CsmEvent.Kind.ITEM_PROPERTY_CHANGED,  cur.getNativeFileItem());
                    case FILE_CREATED:                  return cur; //item events are stronger
                    case ITEM_ADDED:                    return cur;
                    case FILE_RENAMED_CREATED:          return cur;
                    case ITEM_RENAMED_CREATED:          return cur; //?
                    case FILE_RENAMED_DELETED:          return cur; //?
                    case ITEM_RENAMED_DELETED:          return cur; //?
                    case FILE_CHANGED:                  return cur;
                    case ITEM_PROPERTY_CHANGED:         return cur; //?
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case ITEM_REMOVED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return cur; // procssing is the same
                    case ITEM_REMOVED:                  return cur;
                    case FILE_CREATED:                  return doNull();
                    case ITEM_ADDED:                    return doNull();
                    case FILE_RENAMED_CREATED:          return doNull();
                    case ITEM_RENAMED_CREATED:          return doNull();
                    case FILE_RENAMED_DELETED:          return cur; // processing is the same
                    case ITEM_RENAMED_DELETED:          return cur; // processing is the same
                    case FILE_CHANGED:                  return cur; //?
                    case ITEM_PROPERTY_CHANGED:         return cur;
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case ITEM_PROPERTY_CHANGED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return prev;    
                    case ITEM_REMOVED:                  return prev;    
                    case FILE_CREATED:                  return cur;
                    case ITEM_ADDED:                    return prev;
                    case FILE_RENAMED_CREATED:          return cur;
                    case ITEM_RENAMED_CREATED:          return prev;
                    case FILE_RENAMED_DELETED:          return prev;
                    case ITEM_RENAMED_DELETED:          return prev;
                    case FILE_CHANGED:                  return cur; // item event is stronger
                    case ITEM_PROPERTY_CHANGED:         return cur;
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case ITEMS_ALL_PROPERTY_CHANGED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  // fallthrough
                    case ITEM_REMOVED:                  // fallthrough
                    case FILE_CREATED:                  // fallthrough
                    case FILE_RENAMED_CREATED:          // fallthrough
                    case FILE_RENAMED_DELETED:          // fallthrough
                    case FILE_CHANGED:                  // fallthrough
                    case ITEM_ADDED:                    // fallthrough
                    case ITEM_PROPERTY_CHANGED:         // fallthrough
                    case ITEM_RENAMED_CREATED:          // fallthrough
                    case ITEM_RENAMED_DELETED:          return doAssert(prev, cur); // cur path is project, prev path is file
                    case ITEMS_ALL_PROPERTY_CHANGED:    return cur;
                    case FILES_IN_SOURCE_ROOT_DELETED:  return cur; // should we create a synthetic event for this?
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case ITEM_RENAMED_DELETED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return cur; //processing is the same
                    case ITEM_REMOVED:                  return cur; //processing is the same
                    case FILE_CREATED:                  return doNull();
                    case ITEM_ADDED:                    return doNull();
                    case FILE_RENAMED_CREATED:          return doNull();
                    case ITEM_RENAMED_CREATED:          return doNull();
                    case FILE_RENAMED_DELETED:          return cur;
                    case ITEM_RENAMED_DELETED:          return cur;
                    case FILE_CHANGED:                  return cur;
                    case ITEM_PROPERTY_CHANGED:         return cur; //???
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case ITEM_RENAMED_CREATED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  return doItemChanged(CsmEvent.Kind.ITEM_PROPERTY_CHANGED, cur.getNativeFileItem());
                    case ITEM_REMOVED:                  return doItemChanged(CsmEvent.Kind.ITEM_PROPERTY_CHANGED, cur.getNativeFileItem());
                    case FILE_CREATED:                  return cur;
                    case ITEM_ADDED:                    return prev;// or cur... doesn/t really matter
                    case FILE_RENAMED_CREATED:          return cur;
                    case ITEM_RENAMED_CREATED:          return cur;
                    case FILE_RENAMED_DELETED:          return doItemChanged(CsmEvent.Kind.ITEM_PROPERTY_CHANGED, cur.getNativeFileItem());
                    case ITEM_RENAMED_DELETED:          return doItemChanged(CsmEvent.Kind.ITEM_PROPERTY_CHANGED, cur.getNativeFileItem());
                    case FILE_CHANGED:                  return cur;
                    case ITEM_PROPERTY_CHANGED:         return cur;
                    case ITEMS_ALL_PROPERTY_CHANGED:    return doAssert(prev, cur, prev); // prev event path is a project path!
                    case FILES_IN_SOURCE_ROOT_DELETED:  return doAssert(prev, cur, prev); // prev event path is a project path!
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            case FILES_IN_SOURCE_ROOT_DELETED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.getKind()) {
                    case FILE_DELETED:                  // fallthrough
                    case ITEM_REMOVED:                  // fallthrough
                    case FILE_CREATED:                  // fallthrough
                    case ITEM_ADDED:                    // fallthrough
                    case FILE_RENAMED_CREATED:          // fallthrough
                    case ITEM_RENAMED_CREATED:          // fallthrough
                    case FILE_RENAMED_DELETED:          // fallthrough
                    case ITEM_RENAMED_DELETED:          // fallthrough
                    case FILE_CHANGED:                  // fallthrough
                    case ITEM_PROPERTY_CHANGED:         return doAssert(prev, cur); // cur path is project, prev path is item ?!
                    case ITEMS_ALL_PROPERTY_CHANGED:    return prev; // ??? should we create a combined event?
                    case FILES_IN_SOURCE_ROOT_DELETED:  return cur;
                    default:    throw new IllegalArgumentException("unexpected " + prev.getKind()); // NOI18N
                }//</editor-fold>
            default:    throw new AssertionError(prev.getKind());
        }
    }

    private static CsmEvent doAssert(CsmEvent prev, CsmEvent cur) {
        return doAssert(prev, cur, cur);
    }

    private static CsmEvent doAssert(CsmEvent prev, CsmEvent cur, CsmEvent correct ){
        CndUtils.assertTrueInConsole(false, "invalid states " + prev + " " + cur);
        return correct;
    }

    private static CsmEvent doItemChanged(CsmEvent.Kind kind, NativeFileItem item) {
        CndUtils.assertNotNullInConsole(item, "null NativeFileItem"); //NOI18N
        return CsmEvent.createItemEvent(kind, item);
    }

    private static CsmEvent doFileChanged(CsmEvent.Kind kind, FileObject fo) {
        CndUtils.assertNotNullInConsole(fo, "null FileObject"); //NOI18N
        return CsmEvent.createFileEvent(CsmEvent.Kind.FILE_CHANGED, fo);
    }

    private static CsmEvent doNull() {
        return NULL;
    }

    private static boolean toBeSuspended(CsmEvent value) {
        switch (value.getKind()) {
            case FILE_DELETED:
            case ITEM_REMOVED:
            case FILES_IN_SOURCE_ROOT_DELETED:
                return true;
            case FILE_CREATED:
            case FILE_RENAMED_CREATED:
            case FILE_RENAMED_DELETED:
            case FOLDER_CREATED:
            case FILE_CHANGED:
            case FILE_ATTRIBUTE_CHANGED:
            case ITEM_ADDED:
            case ITEM_PROPERTY_CHANGED:
            case ITEMS_ALL_PROPERTY_CHANGED:
            case ITEM_RENAMED_DELETED:
            case ITEM_RENAMED_CREATED:
            case PROJECT_DELETED:
            case FILE_INDEXED:
            case NULL:
                return false;
            default:
                throw new AssertionError(value.getKind().name());
        }
    }

    private final class Worker implements Runnable {
        @Override
        public void run() {
            if (!CsmModelAccessor.isModelAlive()) {
                return;
            }
            HashMap<String, CsmEvent> curEvents;
            synchronized (eventsLock) {
                if (events.isEmpty()) {
                    return;
                }
                curEvents = events;
                if (suspendCount == 0) {
                    events = new LinkedHashMap<>();
                } else {
                    HashMap<String, CsmEvent> suspendedRemoves = new LinkedHashMap<>();
                    for (Iterator<Map.Entry<String, CsmEvent>> it = curEvents.entrySet().iterator(); it.hasNext();) {
                        Map.Entry<String, CsmEvent> entry = it.next();
                        CsmEvent value = entry.getValue();
                        // hold on with delete events and delete/create pair from rename event
                        if (toBeSuspended(value)) {
                            //(value.getKind() == CsmEvent.Kind.FILE_CREATED && value.event instanceof FileRenameEvent)) {
                            suspendedRemoves.put(entry.getKey(), value);
                            it.remove();
                        }
                    }
                    events = suspendedRemoves;
                }
            }
            processEvents(curEvents.values());
        }
    }
}
