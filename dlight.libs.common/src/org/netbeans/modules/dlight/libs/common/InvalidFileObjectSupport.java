/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.libs.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.WeakSet;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.dlight.libs.common.invalid.InvalidFileObject;

/**
 *
 */
public class InvalidFileObjectSupport {

    public static FileObject getInvalidFileObject(FileSystem fileSystem, CharSequence path) {
        InvalidFileObjectSupport instance;
        synchronized (instances) {
            instance = instances.get(fileSystem);
            if (instance == null) {
                instance = new InvalidFileObjectSupport(fileSystem);
                instances.put(fileSystem, instance);
            }
        }
        return instance.getInvalidFileObject(path);
    }

    public static FileObject getInvalidFileObject(File file) {
        file = FileUtil.normalizeFile(file);
        return getInvalidFileObject(getFileSystem(file), file.getAbsolutePath());
    }
    
    private static FileSystem getFileSystem(File file) {
        // NB: file should be normalized!
//        while (file != null) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            try {
                return fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
                return getDummyFileSystem();
            }
        }
//            file = FileUtil.normalizeFile(file.getParentFile());
//        }
        return getDummyFileSystem();
    }

    public static FileSystem getDummyFileSystem() {
        return dummyFileSystem;
    }

    private InvalidFileObjectSupport(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    private FileObject getInvalidFileObject(CharSequence path) {
        // From performance perspective it's a pity that we need to use toString()
        // But anyhow InvalidFileObject will do so, so it does not matter here
        String normPath = PathUtilities.normalizeUnixPath(path.toString().replace('\\', '/')); // NOI18N
        synchronized (this) {
            if (DLightLibsCommonLogger.isDebugMode() && new File(normPath.toString()).exists()) {
                DLightLibsCommonLogger.getInstance().log(Level.INFO, "Creating an invalid file object for existing file {0}", path);
            }
            FileObject fo = fileObjects.putIfAbsent(new InvalidFileObject((fileSystem == null) ? InvalidFileObjectSupport.dummyFileSystem : fileSystem, normPath));
            return fo;
        }
    }
    
    
    private final FileSystem fileSystem;
    private final WeakSet<FileObject> fileObjects = new WeakSet<FileObject>();

    private static final Map<FileSystem, InvalidFileObjectSupport> instances = new WeakHashMap<FileSystem, InvalidFileObjectSupport>();
    private static final DummyFileSystem dummyFileSystem = new DummyFileSystem();
    
    private static class DummyFileSystem extends FileSystem {

        @Override
        public FileObject findResource(String name) {
            return InvalidFileObjectSupport.getInvalidFileObject(this, name);
        }

        @Override
        public String getDisplayName() {
            return "Dummy"; //NOI18N
        }

        @Override
        public FileObject getRoot() {
            return InvalidFileObjectSupport.getInvalidFileObject(this, "");
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    }

}
