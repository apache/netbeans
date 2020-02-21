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

import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.openide.filesystems.FileObject;

/**
 * Combines file events (as in FileChangeListener)
 * and native project item events (as in NativeProjectListener)
 */
/*package*/ class CsmEvent {

    public static final Logger LOG = Logger.getLogger("CsmEvent"); // NOI18N

    public enum Kind {

        FILE_DELETED(FileObject.class),
        FILE_CREATED(FileObject.class),
        FILE_RENAMED_CREATED(FileObject.class),
        FILE_RENAMED_DELETED(FileObject.class),
        FOLDER_CREATED(FileObject.class),
        FILE_CHANGED(FileObject.class),
        FILE_ATTRIBUTE_CHANGED(FileObject.class),
        ITEM_ADDED(NativeFileItem.class),
        ITEM_REMOVED(NativeFileItem.class),
        ITEM_PROPERTY_CHANGED(NativeFileItem.class),
        ITEMS_ALL_PROPERTY_CHANGED(NativeProject.class),
        ITEM_RENAMED_DELETED(NativeFileItem.class),
        ITEM_RENAMED_CREATED(NativeFileItem.class),
        PROJECT_DELETED(NativeProject.class),
        FILES_IN_SOURCE_ROOT_DELETED(FileObject.class),
        FILE_INDEXED(FileObject.class),
        NULL(null);

        private final Class cls;

        private Kind(Class cls) {
            this.cls = cls;
        }

        public Class getObjectClass() {
            return cls;
        }
    }

    private final Kind kind;
    private final Object object;
    private final String oldPath;

    /*package*/ static CsmEvent create(Kind kind, CsmEvent event) {
        return new CsmEvent(kind, event.object, event.oldPath);
    }

    /*package*/ static CsmEvent createItemEvent(Kind kind, NativeFileItem item) {
        return new CsmEvent(kind, item, null);
    }

    /*package*/ static CsmEvent createItemEvent(Kind kind, NativeFileItem item, String oldPath) {
        return new CsmEvent(kind, item, oldPath);
    }

    /*package*/ static CsmEvent createFileEvent(Kind kind, FileObject fileObject) {
        return new CsmEvent(kind, fileObject, null);
    }

    /*package*/ static CsmEvent createFileEvent(Kind kind, FileObject fileObject, String oldPath) {
        return new CsmEvent(kind, fileObject, oldPath);
    }

    /*package*/ static CsmEvent createEmptyEvent(Kind kind) {
        return new CsmEvent(kind, null, null);
    }

    /*package*/ static CsmEvent createProjectEvent(Kind kind, NativeProject project) {
        return new CsmEvent(kind, project, null);
    }

    private CsmEvent(Kind kind, Object object, String oldPath) {
        if (object != null) {
            assert kind.getObjectClass().isAssignableFrom(object.getClass()) :
                    "Wrong object class " + object.getClass().getName() + ", should be " + kind.getObjectClass().getName(); //NOI18N
        }
        if ((object instanceof NativeFileItem)) {
            NativeFileItem item = (NativeFileItem) object;
        }
        this.kind = kind;
        this.object = object;
        this.oldPath = oldPath;
    }

    public Kind getKind() {
        return kind;
    }

    public String getPath() {
        if (object instanceof FileObject) {
            return ((FileObject) object).getPath();
        } else if (object instanceof NativeFileItem) {
            return ((NativeFileItem) object).getAbsolutePath();
        } else if (object instanceof NativeProject) {
            return ((NativeProject) object).getProjectRoot();
        } else {
            return "";
        }
    }

    public String getOldPath() {
        return oldPath;
    }

    public FileObject getFileObject() {
        if (object instanceof FileObject) {
            return (FileObject) object;
        } else if (object instanceof NativeFileItem) {
            return ((NativeFileItem) object).getFileObject();
        }
        return null;
    }

    public NativeFileItem getNativeFileItem() {
        if (object instanceof NativeFileItem) {
            return (NativeFileItem) object;
        }
        return null;
    }

    public NativeProject getNativeProject() {
        if (object instanceof NativeProject) {
            return (NativeProject) object;
        } else if (object instanceof NativeFileItem) {
            return ((NativeFileItem) object).getNativeProject();
        }
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ' ' + kind + ' ' + object + ' ' + (oldPath == null ? "" : ("oldPath=" + oldPath)); // NOI18N
    }

    public static void trace(String format, Object... args) {
        if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
            System.out.printf("CsmEvent: %s%n", String.format(format, args)); // NOI18N
        }
    }
}
