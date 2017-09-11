/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
        try {
            final JavacTaskImpl jt = JavacParser.createJavacTask(
                    new ClasspathInfo.Builder(ClassPath.EMPTY).build(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
            final CompilationUnitTree cu =  jt.parse(FileObjects.fileObjectFileObject(
                    file,
                    file.getParent(),
                    null,
                    FileEncodingQuery.getEncoding(file))).iterator().next();
            pkg = Optional.ofNullable(cu.getPackage())
                    .map((pt) -> pt.getPackageName())
                    .map((xt) -> xt.toString())
                    .orElse(pkg);
        } catch (IOException ioe) {
            LOG.log(
                    Level.INFO,
                    "Cannot parse: {0}",
                    FileUtil.getFileDisplayName(file));
        }
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
            this.ops = Collections.singletonList(String.format(
                    "-Xmodule:%s",  //NOI18N
                    moduleName));
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
