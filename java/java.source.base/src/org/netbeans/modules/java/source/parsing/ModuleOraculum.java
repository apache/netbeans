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
package org.netbeans.modules.java.source.parsing;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.PackageTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = CompilerOptionsQueryImplementation.class, position = Integer.MAX_VALUE)
public final class ModuleOraculum implements CompilerOptionsQueryImplementation, FileChangeListener, Closeable {
    private static  final Logger LOG = Logger.getLogger(ModuleOraculum.class.getName());

    private final ThreadLocal<String> moduleName = new ThreadLocal<>();
    private AtomicReference<Pair<Reference<FileObject>,Reference<FileObject>>> rootCache;
    private AtomicReference<Pair<Pair<Reference<FileObject>,File>,String>> modNameCache;

    public ModuleOraculum() {
        this.rootCache = new AtomicReference<>();
        this.modNameCache = new AtomicReference<>();
    }

    @Override
    @CheckForNull
    public Result getOptions(@NonNull final FileObject file) {
        final String name = moduleName.get();
        return name == null ?
                null :
                new R(name);
    }

    @Override
    public void close() {
        moduleName.remove();
    }

    boolean installModuleName(
            @NullAllowed FileObject root,
            @NullAllowed final FileObject fo) {
        if (fo != null && fo.isData() &&
                (FileObjects.MODULE_INFO.equals(fo.getName()) || FileObjects.CLASS.equals(fo.getExt()))) {
            //No needed to install module name for module-info or class file
            return false;
        }
        if (root == null && fo != null) {
            root = computeRootIfAbsent(fo, (f) -> {
                final ClassPath src = ClassPath.getClassPath(f, ClassPath.SOURCE);
                FileObject owner =  src != null ?
                        src.findOwnerRoot(f) :
                        null;
                if (owner == null && f.isData() && FileUtil.getMIMEType(f, null, JavacParser.MIME_TYPE) != null) {
                    String pkg = parsePackage(f);
                    String[] pkgElements = pkg.isEmpty() ?
                            new String[0] :
                            pkg.split("\\.");   //NOI18N
                    owner = f.getParent();
                    for (int i = 0; owner != null && i < pkgElements.length; i++) {
                        owner = owner.getParent();
                    }
                }
                return owner;
            });
        }
        if (root == null || JavaIndex.hasSourceCache(root.toURL(), false)) {
            return false;
        }
        String name = computeModuleIfAbsent(root, (r) -> {
            final FileObject moduleInfo = r.getFileObject(FileObjects.MODULE_INFO,FileObjects.JAVA);
            if (moduleInfo == null || !moduleInfo.isData() || !moduleInfo.canRead()) {
                return null;
            }
            return SourceUtils.parseModuleName(moduleInfo);
        });
        moduleName.set(name);
        return true;
    }

    @CheckForNull
    private FileObject computeRootIfAbsent(
            @NullAllowed final FileObject key,
            @NonNull final Function<FileObject,FileObject> provider) {
        final Pair<Reference<FileObject>,Reference<FileObject>> entry = rootCache.get();
        FileObject owner, value;
        if (entry == null ||
                (owner = entry.first().get()) == null ||
                !owner.equals(key) ||
                (value = entry.second().get()) == null) {
            value = provider.apply(key);
            rootCache.set(value == null ?
                    null :
                    Pair.of(
                            new WeakReference<>(key),
                            new WeakReference<>(value)));
            LOG.log(Level.FINE, "rootCache updated: {0}", value);   //NOI18N
        }
        return value;
    }

    @CheckForNull
    private String computeModuleIfAbsent(
            @NonNull FileObject root,
            @NonNull final Function<FileObject,String> provider) {
        final Pair<Pair<Reference<FileObject>,File>,String> entry = modNameCache.get();
        FileObject owner;
        String modName;
        if (entry == null ||
                (owner = entry.first().first().get()) == null ||
                !owner.equals(root)) {
            modName = provider.apply(root);
            final File modInfo = Optional.ofNullable(FileUtil.toFile(root))
                    .map((rf) -> new File(rf, String.format("%s.%s", FileObjects.MODULE_INFO, FileObjects.JAVA)))   //NOI18N
                    .orElse(null);
            final Pair<Pair<Reference<FileObject>,File>,String> newEntry = Pair.of(
                    Pair.of(new WeakReference<>(root), modInfo),
                    modName);
            if (modNameCache.compareAndSet(entry, newEntry)) {
                if (entry != null && entry.first().second() != null) {
                    FileUtil.removeFileChangeListener(this, entry.first().second());
                }
                if (newEntry.first().second() != null) {
                    FileUtil.addFileChangeListener(this, newEntry.first().second());
                }
            }
            LOG.log(Level.FINE, "modNameCache updated: {0}", modName);  //NOI18N
        } else {
            modName = entry.second();
        }
        return modName;
    }

    @NonNull
    private static String parsePackage(FileObject file) {
        String pkg = "";    //NOI18N
            final JavacTaskImpl jt = JavacParser.createJavacTask(
                    new ClasspathInfo.Builder(ClassPath.EMPTY).build(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Collections.singletonList(FileObjects.fileObjectFileObject(
                    file,
                    file.getParent(),
                    null,
                    FileEncodingQuery.getEncoding(file))));
            final CompilationUnitTree cu =  jt.parse().iterator().next();
            pkg = Optional.ofNullable(cu.getPackage())
                    .map((pt) -> pt.getPackageName())
                    .map((xt) -> xt.toString())
                    .orElse(pkg);
        return pkg;
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        resetModNameCache();
    }

    @Override
    public void fileChanged(FileEvent fe) {
        resetModNameCache();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        resetModNameCache();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        resetModNameCache();
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        resetModNameCache();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    private void resetModNameCache() {
        final Pair<Pair<Reference<FileObject>, File>, String> entry = modNameCache.getAndSet(null);
        if (entry != null && entry.first().second() != null) {
            FileUtil.removeFileChangeListener(this, entry.first().second());
        }
    }

    private static final class R extends CompilerOptionsQueryImplementation.Result {
        private final List<? extends String> ops;

        R(@NonNull final String moduleName) {
            Parameters.notNull("moduleName", moduleName);   //NOI18N
            this.ops = Collections.singletonList(
                    JavacParser.NB_X_MODULE + moduleName);
        }

        @Override
        public List<? extends String> getArguments() {
            return ops;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
        }
    }

    @CheckForNull
    static ModuleOraculum getInstance() {
        for (CompilerOptionsQueryImplementation impl : Lookup.getDefault().lookupAll(CompilerOptionsQueryImplementation.class)) {
            if (impl.getClass() == ModuleOraculum.class) {
                return (ModuleOraculum) impl;
            }
        }
        return null;
    }
}
