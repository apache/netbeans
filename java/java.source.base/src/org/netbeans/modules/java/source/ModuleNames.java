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
package org.netbeans.modules.java.source;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTaskImpl;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.Module;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 * Computes module names.
 * Computes and caches module names.
 * @author Tomas Zezula
 */
public final class ModuleNames {
    private static final Logger LOG = Logger.getLogger(ModuleNames.class.getName());
    private static final java.util.regex.Pattern AUTO_NAME_PATTERN = java.util.regex.Pattern.compile("-(\\d+(\\.|$))"); //NOI18N
    private static final String RES_MANIFEST = "META-INF/MANIFEST.MF";              //NOI18N
    private static final String ATTR_AUTOMATIC_MOD_NAME = "Automatic-Module-Name";   //NOI18N
    private static final Pattern AUTOMATIC_MODULE_NAME_MATCHER = Pattern.compile("-XDautomatic-module-name:(.*)");  //NOI18N
    private static final ModuleNames INSTANCE = new ModuleNames();

    private final Map<URL,CacheLine> cache;

    private ModuleNames() {
        this.cache = new ConcurrentHashMap<>();
    }

    @CheckForNull
    public String getModuleName(
            @NonNull final URL rootUrl,
            final boolean canUseSources) {
        try {
            final CacheLine cl = cache.get(rootUrl);
            if (cl != null) {
                return cl.getValue();
            }
        } catch (InvalidCacheLine icl) {
            //pass and recompute
        }
        LOG.log(Level.FINE, "No cache for: {0}", rootUrl);
        if (FileObjects.PROTO_NBJRT.equals(rootUrl.getProtocol())) {
            //Platform
            final String path = rootUrl.getPath();
            int endIndex = path.length() - 1;
            int startIndex = path.lastIndexOf('/', endIndex - 1);   //NOI18N
            return register(
                    rootUrl,
                    new CacheLine(rootUrl, path.substring(startIndex+1, endIndex)));
        }
        final URL srcRootURL = JavaIndex.getSourceRootForClassFolder(rootUrl);
        if (srcRootURL != null) {
            //Cache folder
            return register(rootUrl, getProjectModuleName(rootUrl, Collections.singletonList(srcRootURL), canUseSources));
        }
        final SourceForBinaryQuery.Result2 sfbqRes = SourceForBinaryQuery.findSourceRoots2(rootUrl);
        if (sfbqRes.preferSources()) {
            //Project binary
            final CacheLine cl = getProjectModuleName(
                    rootUrl,
                    Arrays.stream(sfbqRes.getRoots()).map(FileObject::toURL).collect(Collectors.toList()),
                    canUseSources);
            if (cl.getValueNoCheck() != null) {
                return register(rootUrl, cl);
            }
        }
        //Binary
        if (FileUtil.isArchiveArtifact(rootUrl)) {
            //Archive
            final FileObject root = URLMapper.findFileObject(rootUrl);
            if (root != null) {
                final FileObject file = FileUtil.getArchiveFile(root);
                FileObject moduleInfo = null;
                //try versioned module-infos, as the source/target level is not available here,
                //use the most up-to-date version:
                FileObject versions = root.getFileObject("META-INF/versions");
                if (versions != null) {
                    int version = -1;
                    for (FileObject c : versions.getChildren()) {
                        try {
                            int currentVersion = Integer.parseInt(c.getNameExt());
                            FileObject currentMI = c.getFileObject(FileObjects.MODULE_INFO, FileObjects.CLASS);
                            if (currentVersion > version && currentMI != null) {
                                moduleInfo = currentMI;
                                version = currentVersion;
                            }
                        } catch (NumberFormatException ex) {
                            //ok, ignore
                        }
                    }
                }
                if (moduleInfo == null) {
                    moduleInfo = root.getFileObject(FileObjects.MODULE_INFO, FileObjects.CLASS);
                }
                if (moduleInfo != null) {
                    try {
                        final String modName = readModuleName(moduleInfo);
                        final File path = Optional.ofNullable(file)
                                .map(FileUtil::toFile)
                                .orElse(null);
                        return register(
                                rootUrl,
                                path != null ?
                                    new FileCacheLine(rootUrl, modName, path):
                                    new FileObjectCacheLine(rootUrl, modName, moduleInfo));
                    } catch (IOException ioe) {
                        //Behave as javac: Pass to automatic module
                    }
                }
                final FileObject manifest = root.getFileObject(RES_MANIFEST);
                if (manifest != null) {
                    try {
                        try (final InputStream in = new BufferedInputStream(manifest.getInputStream())) {
                            final Manifest mf = new Manifest(in);
                            final String autoModName = mf.getMainAttributes().getValue(ATTR_AUTOMATIC_MOD_NAME);
                            if (autoModName != null) {
                                final File path = Optional.ofNullable(file)
                                        .map(FileUtil::toFile)
                                        .orElse(null);
                                return register(
                                    rootUrl,
                                    path != null ?
                                        new FileCacheLine(rootUrl, autoModName, path):
                                        new FileObjectCacheLine(
                                                rootUrl,
                                                autoModName,
                                                file != null ?
                                                        file :
                                                        manifest));
                            }
                        }
                    } catch (IOException ioe) {
                        //Behave as javac: Pass to automatic module
                    }
                }
                //Automatic module
                if (file != null) {
                    final String modName = autoName(file.getName());
                    final File path = FileUtil.toFile(file);
                    return register(
                            rootUrl,
                            path != null ?
                                new FileCacheLine(rootUrl, modName, path):
                                new FileObjectCacheLine(rootUrl, modName, file));
                }
            }
        } else {
            //Regular module folder or folder
            final FileObject root = URLMapper.findFileObject(rootUrl);
            FileObject moduleInfo;
            if (root != null && (moduleInfo = root.getFileObject(FileObjects.MODULE_INFO, FileObjects.CLASS)) != null) {
                try {
                    final String modName = readModuleName(moduleInfo);
                    final File path = FileUtil.toFile(moduleInfo);
                    return register(
                            rootUrl,
                            path != null ?
                                    new FileCacheLine(rootUrl, modName, path):
                                    new FileObjectCacheLine(rootUrl, modName, moduleInfo));
                } catch (IOException ioe) {
                    //pass to null
                }
            }
        }
        return null;
    }

    public void reset(@NonNull final URL binRootURL) {
        Optional.ofNullable(cache.get(binRootURL))
                .ifPresent(CacheLine::invalidate);
    }

    private String register(
            @NonNull final URL rootUrl,
            @NonNull final CacheLine cacheLine) {
            cache.put(rootUrl, cacheLine);
            return cacheLine.getValueNoCheck();
    }

    @NonNull
    private static CacheLine getProjectModuleName(
            @NonNull final URL artefact,
            @NonNull final List<URL> srcRootURLs,
            final boolean canUseSources) {
        if (srcRootURLs.isEmpty()) {
            return new CacheLine(artefact, null);
        }
        if (srcRootURLs.stream().allMatch((srcRootURL)->JavaIndex.hasSourceCache(srcRootURL,false))) {
            //scanned
            String modName = null;
            for (URL srcRootURL : srcRootURLs) {
                try {
                    modName = JavaIndex.getAttribute(srcRootURL, JavaIndex.ATTR_MODULE_NAME, null);
                    if (modName != null) {
                        break;
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
            if (modName != null) {
                //Has module-info
                return new CacheLine(artefact, modName);
            }
            //No module -> automatic module
            return autoName(artefact, srcRootURLs);
        } else if (canUseSources) {
            FileObject moduleInfo = null;
            FileObject root = null;
            for (URL srcRootUrl : srcRootURLs) {
                final FileObject srcRoot = URLMapper.findFileObject(srcRootUrl);
                if (srcRoot != null) {
                    moduleInfo = srcRoot.getFileObject(FileObjects.MODULE_INFO, FileObjects.JAVA);
                    if (moduleInfo != null) {
                        root = srcRoot;
                        break;
                    }
                }
            }
            if (moduleInfo != null) {
                final String modName = parseModuleName(moduleInfo);
                final File path = FileUtil.toFile(moduleInfo);
                return path != null ?
                        new FileCacheLine(artefact, modName, path):
                        new FileObjectCacheLine(artefact, modName, moduleInfo);
            } else {
                //No module -> automatic module
                return autoName(artefact, srcRootURLs);
            }
        }
        return new CacheLine(artefact, null);
    }

    @CheckForNull
    public static String parseModuleName(
            @NonNull final FileObject moduleInfo) {
        final JavacTaskImpl jt = JavacParser.createJavacTask(
                new ClasspathInfo.Builder(ClassPath.EMPTY).build(),
                null,
                "1.3",  //min sl to prevent validateSourceLevel warning
                null,
                null,
                null,
                null,
                null,
                Collections.singletonList(FileObjects.fileObjectFileObject(
                    moduleInfo,
                    moduleInfo.getParent(),
                    null,
                    FileEncodingQuery.getEncoding(moduleInfo))));
        final CompilationUnitTree cu =  jt.parse().iterator().next();
        final ModuleTree module = cu.getModule();
        if (module != null) {
            return module.getName().toString();
        }
        return null;
    }

    @NonNull
    private static CacheLine autoName(
            @NonNull final URL artefact,
            @NonNull final List<? extends URL> srcRootURLs) {
        CompilerOptionsQuery.Result cops = null;
        String amn = null;
        for (URL srcRootURL : srcRootURLs) {
            final FileObject fo = URLMapper.findFileObject(srcRootURL);
            if (fo != null) {
                cops  = CompilerOptionsQuery.getOptions(fo);
                for (String opt : cops.getArguments()) {
                    final Matcher m = AUTOMATIC_MODULE_NAME_MATCHER.matcher(opt);
                    if (m.matches()) {
                        amn = m.group(1);
                        break;
                    }
                }
                break;
            }
        }
        if (amn != null) {
            return new BinCacheLine(artefact, amn, cops, null);
        }
        final BinaryForSourceQuery.Result res = BinaryForSourceQuery.findBinaryRoots(srcRootURLs.get(0));
        for (URL binRoot : res.getRoots()) {
            if (FileObjects.JAR.equals(binRoot.getProtocol())) {
                final String modName = autoName(FileObjects.stripExtension(FileUtil.archiveOrDirForURL(binRoot).getName()));
                return new BinCacheLine(artefact, modName, cops, res);
            }
        }
        return new CacheLine(artefact, null);
    }

    @CheckForNull
    private static String autoName(@NonNull String moduleName) {
        final java.util.regex.Matcher matcher = AUTO_NAME_PATTERN.matcher(moduleName);
        if (matcher.find()) {
            int start = matcher.start();
            moduleName = moduleName.substring(0, start);
        }
        moduleName =  moduleName
            .replaceAll("[^A-Za-z0-9]", ".")  // replace non-alphanumeric
            .replaceAll("(\\.)(\\1)+", ".")   // collapse repeating dots
            .replaceAll("^\\.", "")           // drop leading dots
            .replaceAll("\\.$", "");          // drop trailing dots
        return moduleName.isEmpty() ?
            null :
            moduleName;
    }

    @CheckForNull
    private static String readModuleName(@NonNull FileObject moduleInfo) throws IOException {
        try (final InputStream in = new BufferedInputStream(moduleInfo.getInputStream())) {
            final ClassFile clz = new ClassFile(in, false);
            final Module modle = clz.getModule();
            return modle != null ?
                    modle.getName() :
                    null;
        }
    }


    @NonNull
    public static ModuleNames getInstance() {
        return INSTANCE;
    }

    private static final class InvalidCacheLine extends Exception {
        static final InvalidCacheLine INSTANCE = new InvalidCacheLine();

        private InvalidCacheLine() {
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    private static class CacheLine {
        private final URL artefact;
        private final String value;
        private volatile boolean  invalid;

        CacheLine(
                @NonNull final URL artefact,
                @NullAllowed final String value) {
            this.artefact = artefact;
            this.value = value;
            this.invalid = false;
        }

        @CheckForNull
        final String getValue() throws InvalidCacheLine {
            if (invalid) {
                throw InvalidCacheLine.INSTANCE;
            } else {
                return value;
            }
        }

        @CheckForNull
        final String getValueNoCheck() {
            return value;
        }

        void invalidate() {
            LOG.log(Level.FINE, "Invalidated cache for: {0}", artefact);
            this.invalid = true;
        }
    }

    private static final class FileCacheLine extends CacheLine implements FileChangeListener {
        private final File path;
        private final AtomicBoolean listens = new AtomicBoolean(true);
        FileCacheLine(
                @NonNull final URL artefact,
                @NullAllowed final String modName,
                @NonNull final File path) {
            super(artefact, modName);
            this.path = path;
            FileUtil.addFileChangeListener(this, path);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            invalidate();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            invalidate();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            invalidate();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            invalidate();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            invalidate();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        void invalidate() {
            super.invalidate();
            if (listens.compareAndSet(true, false)) {
                FileUtil.removeFileChangeListener(this, path);
            }
        }
    }

    private static final class FileObjectCacheLine extends CacheLine implements FileChangeListener {
        private final FileObject file;
        private final FileChangeListener wl;

        FileObjectCacheLine(
                @NonNull final URL artefact,
                @NullAllowed final String modName,
                @NonNull final FileObject file) {
            super(artefact, modName);
            this.file = file;
            this.wl = FileUtil.weakFileChangeListener(this, this.file);
            this.file.addFileChangeListener(this.wl);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            invalidate();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            invalidate();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            invalidate();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            invalidate();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            invalidate();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        void invalidate() {
            super.invalidate();
            this.file.removeFileChangeListener(this.wl);
        }
    }

    private static final class BinCacheLine extends CacheLine implements ChangeListener {
        private final CompilerOptionsQuery.Result cops;
        private final BinaryForSourceQuery.Result res;
        private final ChangeListener copsCl;
        private final ChangeListener resCl;

        BinCacheLine(
                @NonNull final URL artefact,
                @NonNull final String modName,
                @NullAllowed final CompilerOptionsQuery.Result cops,
                @NullAllowed final BinaryForSourceQuery.Result res) {
            super(artefact, modName);
            this.cops = cops;
            this.res =  res;
            if (this.cops != null) {
                this.copsCl = WeakListeners.change(this, this.cops);
                this.cops.addChangeListener(copsCl);
            } else {
                this.copsCl = null;
            }
            if (this.res != null) {
                this.resCl = WeakListeners.change(this, this.res);
                this.res.addChangeListener(resCl);
            } else {
                this.resCl = null;
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            invalidate();
        }

        @Override
        void invalidate() {
            super.invalidate();
            if (this.copsCl != null) {
                this.cops.removeChangeListener(this.copsCl);
            }
            if (this.resCl != null) {
                this.res.removeChangeListener(this.resCl);
            }
        }
    }
}
