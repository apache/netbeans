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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Tomas Zezula
 */
public class FolderArchive implements Archive {

    private static final Logger LOG = Logger.getLogger(FolderArchive.class.getName());
    private static final boolean normalize = Boolean.getBoolean("FolderArchive.normalize"); //NOI18N
    
    final File root;
    volatile Charset encoding;
    
    private boolean sourceRootInitialized;
    private URL sourceRoot;

    /** Creates a new instance of FolderArchive */
    public FolderArchive (final File root) {
        assert root != null;
        this.root = root;
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "creating FolderArchive for {0}", root.getAbsolutePath());
        }
    }
    
    private Charset encoding() {
        Charset e = encoding;
        if (e == null) {
            FileObject file = FileUtil.toFileObject(root);
            if (file != null) {
                e = FileEncodingQuery.getEncoding(file);
            } else {
                // avoid further checks
                e = UNKNOWN_CHARSET;
            }
            encoding = e;
        }
        return e != UNKNOWN_CHARSET ? e : null;
    }
    
    private static final Charset UNKNOWN_CHARSET = new Charset("UNKNOWN", null) {
        @Override
        public boolean contains(Charset cs) {
            throw new UnsupportedOperationException("Unexpected call");
        }

        @Override
        public CharsetDecoder newDecoder() {
            throw new UnsupportedOperationException("Unexpected call");
        }

        @Override
        public CharsetEncoder newEncoder() {
            throw new UnsupportedOperationException("Unexpected call");
        }
    };
    
    @Override
    public Iterable<JavaFileObject> getFiles(
            @NonNull String folderName,
            @NullAllowed final ClassPath.Entry entry,
            @NullAllowed final Set<JavaFileObject.Kind> kinds,
            @NullAllowed final JavaFileFilterImplementation filter,
            final boolean recursive) throws IOException {
        assert folderName != null;
        if (folderName.length()>0) {
            folderName+='/';    //NOI18N
        }
        if (entry == null || entry.includes(folderName)) {
            File folder = new File (this.root, folderName.replace('/', File.separatorChar));      //NOI18N
            //Issue: #126392 on Mac
            //The problem when File ("A/").listFiles()[0].equals(new File("a/").listFiles[0]) returns false
            //Normalization is slow - turn on this workaround only for users which require it.
            //The problem only happens in case when there is file with wrong case in import.
            if (normalize) {
                folder = FileUtil.normalizeFile(folder);
            }
            return visit(folder, root, encoding(), filter, recursive, (f) -> {
                final JavaFileObject.Kind fKind = FileObjects.getKind(FileObjects.getExtension(f.getName()));
                try {
                    if ((kinds == null || kinds.contains(fKind)) &&
                        f.isFile() &&
                        (entry == null || entry.includes(BaseUtilities.toURI(f).toURL()))) {
                        return fKind;
                    }
                } catch (MalformedURLException e) {
                    //pass
                }
                return null;
            });
        }
        return Collections.<JavaFileObject>emptyList();
    }

    @Override
    public JavaFileObject create (String relativePath, final JavaFileFilterImplementation filter) throws UnsupportedOperationException {
        if (File.separatorChar != '/') {    //NOI18N
            relativePath = relativePath.replace('/', File.separatorChar);
        }
        final File file = new File (root, relativePath);
        return FileObjects.fileFileObject(file, root, filter, encoding());
    }

    @Override
    public void clear () {
    }

    @Override
    public JavaFileObject getFile(final @NonNull String name) {
        final String path = name.replace('/', File.separatorChar);        //NOI18N
        File file = new File (this.root, path);
        if (file.exists()) {
            return FileObjects.fileFileObject(file,this.root,null,encoding());
        }
        try {
            final URL srcRoot = getBaseSourceRoot(BaseUtilities.toURI(this.root).toURL());
            if (srcRoot != null && JavaIndex.hasSourceCache(srcRoot, false)) {
                if ("file".equals(srcRoot.getProtocol())) {         //NOI18N
                    final File folder = BaseUtilities.toFile(srcRoot.toURI());
                    file = new File (folder,path);
                    if (file.exists()) {
                        return FileObjects.fileFileObject(file,folder,null,encoding());
                    }
                } else {
                    final FileObject srcRootFo = URLMapper.findFileObject(srcRoot);
                    if (srcRootFo != null) {
                        final FileObject resource = srcRootFo.getFileObject(name);
                        if (resource != null) {
                            return  FileObjects.sourceFileObject(resource, srcRootFo);
                        }
                    }
                }
            } else {
                LOG.log(
                    Level.FINE,
                    "No source in: {0}.",    //NOI18N
                    srcRoot);
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        } catch (URISyntaxException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    @Override
    public URI getDirectory(String dirName) throws IOException {
        final String path = dirName.replace('/', File.separatorChar);        //NOI18N
        File dir = new File (this.root, path);

        if (dir.isDirectory()) {
            return dir.toURI();
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format(
            "%s[folder: %s]",   //NOI18N
            getClass().getSimpleName(),
            root.getAbsolutePath()
        );
    }

    @Override
    public boolean isMultiRelease() {
        return false;
    }

    private URL getBaseSourceRoot(final URL binRoot) {
        synchronized (this) {
            if (sourceRootInitialized) {
                return sourceRoot;
            }
        }
        final URL tmpSourceRoot = JavaIndex.getSourceRootForClassFolder(binRoot);
        synchronized (this) {
            sourceRoot = tmpSourceRoot;
            sourceRootInitialized = true;
            return sourceRoot;
        }
    }

    @NonNull
    private static List<JavaFileObject> visit(
            @NonNull final File folder,
            @NonNull final File root,
            @NonNull final Charset encoding,
            @NullAllowed final JavaFileFilterImplementation filter,
            final boolean recursive,
            @NonNull Function<File,JavaFileObject.Kind> accept) {
        final List<JavaFileObject> res = new ArrayList<>();
        final Deque<File> todo = new ArrayDeque<>();
        todo.offer(folder);
        while (!todo.isEmpty()) {
            final File active = todo.removeLast();  //DFS
            final File[] content = active.listFiles();
            if (content != null) {
                for (File f : content) {
                    final JavaFileObject.Kind fKind = accept.apply(f);
                    if (fKind != null) {
                        res.add(FileObjects.fileFileObject(
                            f,
                            root,
                            filter,
                            fKind == JavaFileObject.Kind.CLASS ?
                                    UNKNOWN_CHARSET :
                                    encoding));
                    } else if (recursive && f.isDirectory()) {
                        todo.offer(f);
                    }
                }
            }
        }
        return Collections.unmodifiableList(res);
    }

}
