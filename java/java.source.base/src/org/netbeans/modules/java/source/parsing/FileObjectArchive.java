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

package org.netbeans.modules.java.source.parsing;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.Enumerations;

/**
 *
 * @author tom
 */
public class FileObjectArchive implements Archive {
    private static final Logger LOG = Logger.getLogger(FileObjectArchive.class.getName());
    
    private final FileObject root;
    private volatile Boolean multiRelease;
    
    /** Creates a new instance of FileObjectArchive */
    public FileObjectArchive (final FileObject root) {
        this.root = root;
    }

    @NonNull
    @Override
    public Iterable<JavaFileObject> getFiles(
            @NonNull final String folderName,
            @NullAllowed final ClassPath.Entry entry,
            @NullAllowed final Set<JavaFileObject.Kind> kinds,
            @NullAllowed final JavaFileFilterImplementation filter,
            final boolean recursive) throws IOException {
        final FileObject folder = root.getFileObject(folderName);
        if (folder == null || !(entry == null || entry.includes(folder))) {
            return Collections.<JavaFileObject>emptySet();
        }
        final Enumeration<? extends FileObject> children;
        final List<JavaFileObject> result;
        if (recursive) {
            children = Enumerations.filter(
                    folder.getChildren(recursive),
                    (p,x)->{
                        return  !p.isFolder() && isInJavaPackage(folder,p) ?
                                p :
                                null;
                    });
            result = new ArrayList<>(/*unknown size*/);
        } else {
            final FileObject[] chlds = folder.getChildren();
            children = Enumerations.array(chlds);
            result = new ArrayList<>(chlds.length);
        }
        while (children.hasMoreElements()) {
            final FileObject fo = children.nextElement();
            if (fo.isData() && (entry == null || entry.includes(fo))) {
                final Kind kind = FileObjects.getKind(fo.getExt());
                if (kinds == null || kinds.contains (kind)) {
                    JavaFileObject file;
                    if (kind == Kind.CLASS) {
                        file = FileObjects.fileObjectFileObject(fo, root, filter, null);
                    } else {
                        file = FileObjects.sourceFileObject(fo, root, filter,false);
                    }
                    result.add(file);
                }
            }
        }
        return result;
    }

    @Override
    public JavaFileObject create (final String relativePath, final JavaFileFilterImplementation filter) {
        throw new UnsupportedOperationException("Write not supported");   //NOI18N
    }

    @Override
    public void clear() {
        multiRelease = null;
    }

    @Override
    public JavaFileObject getFile(String name) throws IOException {
        final FileObject file = root.getFileObject(name);
        return file == null ? null : FileObjects.sourceFileObject(file, root, null, false);
    }

    @Override
    public URI getDirectory(String dirName) throws IOException {
        FileObject dir = root.getFileObject(dirName);
        if (dir != null && dir.isFolder()) {
            return dir.toURI();
        }
        return null;
    }

    @Override
    public boolean isMultiRelease() {
        Boolean res = multiRelease;
        if (res == null) {
            res = Boolean.FALSE;
            try {
                if (root.getFileSystem() instanceof JarFileSystem) {
                    final FileObject manifest = root.getFileObject("META-INF/MANIFEST.MF"); //NOI18N
                    if (manifest != null) {
                        try(InputStream in = new BufferedInputStream(manifest.getInputStream())) {
                            res = FileObjects.isMultiVersionArchive(in);
                        }
                    }
                }
            } catch (IOException ioe) {
                LOG.log(
                        Level.WARNING,
                        "Cannot read: {0} manifest",    //NOI18N
                        FileUtil.getFileDisplayName(root));
            }
            multiRelease = res;
        }
        return res;
    }

    @Override
    public String toString() {
        return String.format(
            "%s[folder: %s]",   //NOI18N
            getClass().getSimpleName(),
            FileUtil.getFileDisplayName(root));
    }

    private static boolean isInJavaPackage(
            @NonNull final FileObject root,
            @NonNull final FileObject file) {
        FileObject fld = file.getParent();
        while (fld != null && !fld.equals(root)) {
            if (!SourceVersion.isIdentifier(fld.getNameExt())) {
                return false;
            }
            fld = fld.getParent();
        }
        return true;
    }

}
