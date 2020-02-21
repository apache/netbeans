/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
