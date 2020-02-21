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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.RequestProcessor;

/**
 * Adapter which ability to suspend delivery of remove events. They will be 
 * merged with add events into change event when in suspend mode.
 * Also it redirects all notifications into other thread.
 * fileAttributeChanged is not delivered by this class.
 */
public final class SuspendableFileChangeListener implements FileChangeListener {
    private static final RequestProcessor RP = new RequestProcessor("CND FileEvents redispatcher"); // NOI18N

    private final boolean remoteOnly;
    private final RequestProcessor.Task task;
    private final FileChangeListener external;
    private int suspendCount = 0;
    private final Object eventsLock = new Object();
    private HashMap<FSPath, EventWrapper> events = new LinkedHashMap<FSPath, EventWrapper>();
    private final Runnable taskScheduler = new Runnable() {

        @Override
        public void run() {
            task.schedule(0);
        }
    };
            
    private final class Worker implements Runnable {
      
        @Override
        public void run() {
            HashMap<FSPath, EventWrapper> curEvents;
            synchronized (eventsLock) {
                if (events.isEmpty()) {
                    return;
                }
                if (suspendCount == 0) {
                    curEvents = events;
                    events = new LinkedHashMap<FSPath, EventWrapper>();
                } else {
                    curEvents = events;
                    HashMap<FSPath, EventWrapper> suspendedRemoves = new LinkedHashMap<FSPath, EventWrapper>();
                    for (Iterator<Map.Entry<FSPath, EventWrapper>> it = curEvents.entrySet().iterator(); it.hasNext();) {
                        Map.Entry<FSPath, EventWrapper> entry = it.next();
                        EventWrapper value = entry.getValue();
                        // hold on with delete events and delete/create pair from rename event
                        if ((value.kind == EventKind.FILE_DELETED) ||
                            (value.kind == EventKind.FILE_CREATED && value.event instanceof FileRenameEvent)) {
                            suspendedRemoves.put(entry.getKey(), value);
                            it.remove();
                        } 
                    }
                    events = suspendedRemoves;
                }
            }
            for (EventWrapper eventWrapper : curEvents.values()) {
                FileEvent fe = eventWrapper.event;
                switch (eventWrapper.kind) {
                    case FILE_DELETED:
                        external.fileDeleted(fe);
                        break;
                    case FILE_CREATED:
                        external.fileDataCreated(fe);
                        break;
                    case FILE_RENAMED_CREATED:
                        external.fileRenamed((FileRenameEvent)fe);
                        break;
                    case FILE_CHANGED:
                        external.fileChanged(fe);
                        break;
                    case FILE_ATTRIBUTE_CHANGED:
                        external.fileAttributeChanged((FileAttributeEvent)fe);
                        break;
                    case FOLDER_CREATED:
                        external.fileFolderCreated(fe);
                        break;
                    case NULL:
                        break;
                    case FILE_RENAMED_DELETED:
                        break;
                    default:
                        throw new AssertionError(eventWrapper.kind.name());
                }
            }            
        }
    }

    public SuspendableFileChangeListener(FileChangeListener external, boolean remoteOnly) {
        this.task = RP.create(new Worker(), true);
        this.external = external;
        this.remoteOnly = remoteOnly;
    }
    
    public void suspendRemoves() {
        synchronized (eventsLock) {
            CndUtils.assertTrue(suspendCount >= 0, "suspendRemoves with " + suspendCount);
            suspendCount++;
        }
    }
    
    public void resumeRemoves() {
        boolean schedule;
        synchronized (eventsLock) {
            CndUtils.assertTrue(suspendCount > 0, "resumeRemoves without suspendRemoves " + suspendCount);
            suspendCount--;
            if (suspendCount < 0) {
                suspendCount = 0;
            }
            schedule = (suspendCount == 0);
        }
        if (schedule) {
            taskScheduler.run();
        }
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
    public void fileChanged(FileEvent fe) {
        if (accept(fe)) {
            register(EventKind.FILE_CHANGED, fe);
        }
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        if (accept(fe)) {
            register(EventKind.FILE_CREATED, fe);
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        if (accept(fe)) {
            register(EventKind.FILE_DELETED, fe);
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        if (accept(fe)) {
            register(EventKind.FOLDER_CREATED, fe);
        }
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
//        if (accept(fe)) {
//        register(EventKind.FILE_ATTRIBUTE_CHANGED, fe);
//        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        if (accept(fe)) {
            FSPath newPath = FSPath.toFSPath(fe.getFile());
            String strPrevExt = (fe.getExt() == null || fe.getExt().isEmpty()) ? "" : "." + fe.getExt(); // NOI18N
            String strPrevPath = CndPathUtilities.getDirName(newPath.getPath()) + '/' + fe.getName() + strPrevExt; // NOI18N
            FSPath prevPath = new FSPath(newPath.getFileSystem(), strPrevPath);
            synchronized (eventsLock) {
                EventWrapper prevPathEvent = events.get(prevPath);
                FileObject removedFO = InvalidFileObjectSupport.getInvalidFileObject(prevPath.getFileSystem(), prevPath.getPath());
                FileEvent deleteFE = new FileEvent((FileObject)fe.getSource(), removedFO, fe.isExpected(), fe.getTime());
                events.put(prevPath, convert(prevPathEvent, EventKind.FILE_DELETED, deleteFE));

                EventWrapper prevNewEvent = events.get(newPath);
                events.put(newPath, convert(prevNewEvent, EventKind.FILE_CREATED, fe));
            }
            fe.runWhenDeliveryOver(taskScheduler);
        }
    }

    private void register(EventKind kind, FileEvent fe) {
        FSPath path = FSPath.toFSPath(fe.getFile());
        synchronized (eventsLock) {
            EventWrapper prev = events.get(path);
            events.put(path, convert(prev, kind, fe));
        }
        fe.runWhenDeliveryOver(taskScheduler);
    }
    
    /*package*/void flush() {
        task.schedule(0);
        task.waitFinished();
    }
    
    private static EventWrapper doAssert(EventWrapper prev, EventWrapper cur) {
        CndUtils.assertTrueInConsole(false, "invalid states " + prev + " " + cur);
        return cur;
    }

    private static EventWrapper doNull(EventWrapper prev, EventWrapper cur) {
        return new EventWrapper(EventKind.NULL, cur.event);
    }

    private static EventWrapper doChanged(EventWrapper prev, EventWrapper cur) {
        return new EventWrapper(EventKind.FILE_CHANGED, cur.event);
    }    
    
    private static EventWrapper doNullOnRename(EventWrapper prev, EventWrapper cur) {
        throw new UnsupportedOperationException(); 
    }
    
    // NOTE: sometimes events are delivered twice from FO refresh and refresh on window activation
    // 
    // (curKind)                            (prevKind)
    //       states |   DELETED |   CREATED |   RENAMED_CREATED |   RENAMED_DELETED | FOLDER_CREATED|   CHANGED     |   ATTRIBS     |   
    // -----------------------------------------------------------------------------------------------------------------------------|
    //  DELETED     |   DELETED |   null    |   null            |   assert          |   null        |   DELETED     |   DELETED     |   
    //  CREATED     |   CHANGED |   CREATED |   assert          |   CHANGED         |   assert      |   CHANGED     |   assert      |   
    //RENAME_CREATED|   CHANGED |   assert  |   assert          |   CHANGED         |   assert      |   assert      |   assert      |
    //RENAME_DELETED|   assert  |   null (?)|   null (?)        |   assert          |   null (?)    | RENAME_DELETED| RENAME_DELETED|
    //FOLDER_CREATED|   CHANGED |   assert  |   assert          |   CHANGED         | FOLDER_CREATED|   CHNAGED     |   assert      |
    //  CHANGED     |   assert  |   CREATED |   RENAMED_CREATED |   assert          | FOLDER_CREATED|   CHANGED     |   CHANGED     |
    //  ATTRIBS     |   assert  |   CREATED |   RENAMED_CREATED |   assert          | FOLDER_CREATED|   CHANGED     |   ATTRIBS     |
    //         
    private static EventWrapper convert(EventWrapper prev, EventKind kind, FileEvent fe) {
        EventWrapper cur = new EventWrapper(kind, fe);
        if (prev == null || prev.kind == EventKind.NULL) {
            return cur;
        }
        switch (cur.kind) {
            case FILE_DELETED: //<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.kind) {
                    case FILE_DELETED:          return prev; 
                    case FILE_CREATED:          return doNull(prev, cur);
                    case FILE_RENAMED_CREATED:  return doNull(prev, cur);
                    case FILE_RENAMED_DELETED:  return doAssert(prev, cur);
                    case FOLDER_CREATED:        return doNull(prev, cur);
                    case FILE_CHANGED:          return cur;
                    case FILE_ATTRIBUTE_CHANGED:return cur;
                    default:    throw new IllegalArgumentException("unexpected " + prev.kind); // NOI18N
                }   //</editor-fold>
            case FILE_CREATED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.kind) {
                    case FILE_DELETED:          return doChanged(prev, cur);
                    case FILE_CREATED:          return prev; 
                    case FILE_RENAMED_CREATED:  return doAssert(prev, cur);
                    case FILE_RENAMED_DELETED:  return doChanged(prev, cur);
                    case FOLDER_CREATED:        return doAssert(prev, cur);
                    case FILE_CHANGED:          return prev;
                    case FILE_ATTRIBUTE_CHANGED:return doAssert(prev, cur);
                    default:    throw new AssertionError(prev.kind);
                }//</editor-fold>
            case FILE_RENAMED_CREATED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.kind) {
                    case FILE_DELETED:          return doChanged(prev, cur);
                    case FILE_CREATED:          return doAssert(prev, cur);
                    case FILE_RENAMED_CREATED:  return doAssert(prev, cur);
                    case FILE_RENAMED_DELETED:  return doChanged(prev, cur);
                    case FOLDER_CREATED:        return doAssert(prev, cur);
                    case FILE_CHANGED:          return doAssert(prev, cur);
                    case FILE_ATTRIBUTE_CHANGED:return doAssert(prev, cur);
                    default:    throw new AssertionError(prev.kind);
                }//</editor-fold>
            case FILE_RENAMED_DELETED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.kind) {
                    case FILE_DELETED:          return doAssert(prev, cur);
                    case FILE_CREATED:          return doNullOnRename(prev, cur);
                    case FILE_RENAMED_CREATED:  return doNullOnRename(prev, cur);
                    case FILE_RENAMED_DELETED:  return doAssert(prev, cur);
                    case FOLDER_CREATED:        return doNullOnRename(prev, cur);
                    case FILE_CHANGED:          return cur;
                    case FILE_ATTRIBUTE_CHANGED:return cur;
                    default:    throw new AssertionError(prev.kind);
                }//</editor-fold>
            case FOLDER_CREATED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.kind) {
                    case FILE_DELETED:          return doChanged(prev, cur);
                    case FILE_CREATED:          return doAssert(prev, cur);
                    case FILE_RENAMED_CREATED:  return doAssert(prev, cur);
                    case FILE_RENAMED_DELETED:  return doChanged(prev, cur);
                    case FOLDER_CREATED:        return cur;
                    case FILE_CHANGED:          return prev;
                    case FILE_ATTRIBUTE_CHANGED:return doAssert(prev, cur);
                    default:    throw new IllegalArgumentException("unexpected " + prev.kind); // NOI18N
                }//</editor-fold>
            case FILE_CHANGED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.kind) {
                    case FILE_DELETED:          return doAssert(prev, cur);
                    case FILE_CREATED:          return prev;
                    case FILE_RENAMED_CREATED:  return prev;
                    case FILE_RENAMED_DELETED:  return doAssert(prev, cur);
                    case FOLDER_CREATED:        return prev;
                    case FILE_CHANGED:          return cur;
                    case FILE_ATTRIBUTE_CHANGED:return cur;
                    default:    throw new IllegalArgumentException("unexpected " + prev.kind); // NOI18N
                }//</editor-fold>
            case FILE_ATTRIBUTE_CHANGED://<editor-fold defaultstate="collapsed" desc="...">
                switch (prev.kind) {
                    case FILE_DELETED:          return doAssert(prev, cur);
                    case FILE_CREATED:          return prev;
                    case FILE_RENAMED_CREATED:  return prev;
                    case FILE_RENAMED_DELETED:  return doAssert(prev, cur);
                    case FOLDER_CREATED:        return prev;
                    case FILE_CHANGED:          return prev;
                    case FILE_ATTRIBUTE_CHANGED:return cur;
                    default:    throw new IllegalArgumentException("unexpected " + prev.kind); // NOI18N
                }//</editor-fold>
            default:    throw new AssertionError(prev.kind);
        }
    }

    private enum EventKind {

        FILE_DELETED,
        FILE_CREATED,
        FILE_RENAMED_CREATED,
        FILE_RENAMED_DELETED,
        FOLDER_CREATED,
        FILE_CHANGED,
        FILE_ATTRIBUTE_CHANGED,
        NULL;
    }

    private static final class EventWrapper {

        private final EventKind kind;
        private final FileEvent event;
        private final boolean folder;
        private final EventWrapper prev;

        public EventWrapper(EventKind kind, FileEvent event) {
            this(kind, event, event.getFile().isFolder(), null);
        }

        public EventWrapper(EventKind kind, FileEvent event, boolean folder, EventWrapper prev) {
            this.kind = kind;
            this.event = event;
            this.folder = folder;
            this.prev = prev;
        }
        
        @Override
        public String toString() {
            return "EventWrapper{" + "kind=" + kind + ", event=" + event + '}'; // NOI18N
        }
    }
    
}
