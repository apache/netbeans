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

package org.netbeans.modules.java.j2seplatform.queries;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 * Returns the source level for the non projectized java/class files (those
 * file for which the classpath is provided by the {@link DefaultClassPathProvider}
 * @author Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceLevelQueryImplementation.class, position=10000)
public class DefaultSourceLevelQueryImpl implements SourceLevelQueryImplementation, FileChangeListener {

    private static final Logger LOG = Logger.getLogger(DefaultSourceLevelQueryImpl.class.getName());
    private static final String JAVA_EXT = "java";  //NOI18N
    private static final String MODULE_INFO = "module-info";  //NOI18N
    private static final SpecificationVersion JDK9 = new SpecificationVersion("9");

    private final AtomicReference<Pair<Reference<FileObject>,Reference<FileObject>>> rootCache;
    private final AtomicReference<Pair<Pair<Reference<FileObject>,File>,Boolean>> modCache;

    public DefaultSourceLevelQueryImpl() {
        this.rootCache = new AtomicReference<>();
        this.modCache = new AtomicReference<>();
    }

    public String getSourceLevel(final FileObject javaFile) {
        assert javaFile != null : "javaFile has to be non null";   //NOI18N
        String ext = javaFile.getExt();
        if (JAVA_EXT.equalsIgnoreCase (ext)) {
            final JavaPlatform jp = JavaPlatformManager.getDefault().getDefaultPlatform();
            assert jp != null : "JavaPlatformManager.getDefaultPlatform returned null";     //NOI18N
            SpecificationVersion ver = jp.getSpecification().getVersion();
            if (JDK9.compareTo(ver) > 0 && isModular(javaFile)) {
                return JDK9.toString();
            } else {
                return ver.toString();
            }
        }
        return null;
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        resetModCache();
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        resetModCache();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        resetModCache();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        resetModCache();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    @Override
    public void fileChanged(FileEvent fe) {
    }

    private void resetModCache() {
        final Pair<Pair<Reference<FileObject>, File>, Boolean> entry = modCache.getAndSet(null);
        if (entry != null && entry.first().second() != null) {
            FileUtil.removeFileChangeListener(this, entry.first().second());
        }
    }

    private boolean isModular(final FileObject javaFile) {
        //Module-info always modular
        if (MODULE_INFO.equals(javaFile.getName())) {
            return true;
        }
        //Try to find module-info
        final Pair<Reference<FileObject>,Reference<FileObject>> entry = rootCache.get();
        FileObject file, root;
        if (entry == null ||
                (file = entry.first().get()) == null || !file.equals(javaFile) ||
                (root = entry.second().get()) == null) {
            root = Optional.ofNullable(ClassPath.getClassPath(javaFile, ClassPath.SOURCE))
                    .map((scp) -> scp.findOwnerRoot(javaFile))
                    .orElseGet(() -> {
                        final String pkg = parsePackage(javaFile);
                        final String[] pkgElements = pkg.isEmpty() ?
                                new String[0] :
                                pkg.split("\\.");   //NOI18N
                        FileObject owner = javaFile.getParent();
                        for (int i = 0; owner != null && i < pkgElements.length; i++) {
                            owner = owner.getParent();
                        }
                        return owner;
                    });
            rootCache.set(Pair.of(
                    new WeakReference<>(javaFile),
                    new WeakReference<>(root)));
            LOG.log(Level.FINE, "rootCache updated: {0}", root);  //NOI18N
        }
        if (root == null) {
            return false;
        }
        final Pair<Pair<Reference<FileObject>,File>,Boolean> modEntry = modCache.get();
        FileObject meKye;
        if (modEntry == null ||
                (meKye = modEntry.first().first().get()) == null || !meKye.equals(root)) {
            final FileObject modInfo = root.getFileObject(MODULE_INFO, JAVA_EXT);
            final boolean res =  modInfo != null && modInfo.isData();
            final Pair<Pair<Reference<FileObject>,File>,Boolean> newModEntry = Pair.of(
                    Pair.of(new WeakReference<>(root), FileUtil.toFile(root)),
                    res);
            if (modCache.compareAndSet(modEntry, newModEntry)) {
                if (modEntry != null && modEntry.first().second() != null) {
                    FileUtil.removeFileChangeListener(this, modEntry.first().second());
                }
                if (newModEntry != null && newModEntry.first().second() != null) {
                    FileUtil.addFileChangeListener(this, newModEntry.first().second());
                }
                LOG.log(Level.FINE, "modCache updated: {0}", res);  //NOI18N
            }
            return res;
        } else {
            return modEntry.second();
        }
    }

    @NonNull
    private static String parsePackage(@NonNull final FileObject javaFile) {
        String pkg = "";    //NOI18N
        try {
            JavacTask jt = (JavacTask) ToolProvider.getSystemJavaCompiler().getTask(
                    null,
                    null,
                    null,
                    Collections.<String>emptyList(),
                    Collections.<String>emptyList(),
                    Collections.singleton(new JFO(javaFile)));
            final Iterator<? extends CompilationUnitTree> cus = jt.parse().iterator();
            if (cus.hasNext()) {
                pkg = Optional.ofNullable(cus.next().getPackage())
                    .map((pt) -> pt.getPackageName())
                    .map((xt) -> xt.toString())
                    .orElse(pkg);
            }
        } catch (Exception e) {
            //TODO: Log & pass
        }
        return pkg;
    }

    private static final class JFO implements JavaFileObject {
        private final FileObject delegate;

        JFO(@NonNull final FileObject delegate) {
            Parameters.notNull("delegate", delegate);   //NOI18N
            this.delegate = delegate;
        }

        @Override
        public Kind getKind() {
            return Kind.SOURCE;
        }

        @Override
        public boolean isNameCompatible(String simpleName, Kind kind) {
            return delegate.getName().equals(simpleName) && getKind() == kind;
        }

        @Override
        public NestingKind getNestingKind() {
            return NestingKind.TOP_LEVEL;
        }

        @Override
        public Modifier getAccessLevel() {
            return null;
        }

        @Override
        public URI toUri() {
            return delegate.toURI();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return delegate.getInputStream();
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return delegate.getOutputStream();
        }

        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            return new InputStreamReader(openInputStream(), encoding());
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            final StringBuilder content = new StringBuilder();
            char[] data = new char[1<<12];
            try(Reader in = openReader(ignoreEncodingErrors)) {
                for (int len = in.read(data, 0, data.length); len > 0; len = in.read(data, 0, data.length)) {
                    content.append(data, 0, len);
                }
            }
            return content;
        }

        @Override
        public Writer openWriter() throws IOException {
            return new OutputStreamWriter(openOutputStream(), encoding());
        }

        @Override
        public long getLastModified() {
            return delegate.lastModified().getTime();
        }

        @Override
        public boolean delete() {
            try {
                delegate.delete();
                return true;
            } catch (IOException ioe) {
                return false;
            }
        }

        private Charset encoding() {
            return FileEncodingQuery.getEncoding(delegate);
        }
    }
}
