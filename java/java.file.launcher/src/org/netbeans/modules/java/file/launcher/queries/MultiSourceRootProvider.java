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
package org.netbeans.modules.java.file.launcher.queries;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.file.launcher.SingleSourceFileUtil;
import org.netbeans.modules.java.file.launcher.SingleSourceFileUtil.ParsedFileOptions;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;

import static org.netbeans.spi.java.classpath.ClassPathImplementation.PROP_RESOURCES;

import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders({
    @ServiceProvider(service=ClassPathProvider.class, position=9_999),
    @ServiceProvider(service=MultiSourceRootProvider.class)
})
public class MultiSourceRootProvider implements ClassPathProvider {

    private static final RequestProcessor WORKER = new RequestProcessor(MultiSourceRootProvider.class.getName(), 1, false, false);
    private static final Logger LOG = Logger.getLogger(MultiSourceRootProvider.class.getName());

    public static boolean DISABLE_MULTI_SOURCE_ROOT = Boolean.getBoolean("java.disable.multi.source.root");
    public static boolean SYNCHRONOUS_UPDATES = false;

    private static final Set<String> MODULAR_DIRECTORY_OPTIONS = Set.of("--module-path", "-p");
    private static final Set<String> CLASSPATH_OPTIONS = Set.of("--class-path", "-cp", "-classpath");

    //TODO: the cache will probably be never cleared, as the ClassPath/value refers to the key(?)
    private Map<FileObject, ClassPath> file2SourceCP = new WeakHashMap<>();
    private Map<FileObject, ClassPath> root2SourceCP = new WeakHashMap<>();
    private Map<FileObject, Runnable> root2RegistrationRefresh = new WeakHashMap<>();
    private final Set<FileObject> registeredRoots = Collections.newSetFromMap(new WeakHashMap<>());
    private Map<FileObject, ClassPath> file2AllPath = new WeakHashMap<>();
    private Map<FileObject, ClassPath> file2ClassPath = new WeakHashMap<>();
    private Map<FileObject, ClassPath> file2ModulePath = new WeakHashMap<>();

    boolean isSupportedFile(FileObject file) {
        // MultiSourceRootProvider assumes it can convert FileObject to
        // java.io.File, so filter here
        if (!Objects.equals("file", file.toURI().getScheme())) {
            return false;
        }

        if (SingleSourceFileUtil.isSingleSourceFile(file)) {
            return true;
        }

        List<FileObject> registeredRootsCopy;

        synchronized (registeredRoots) {
            registeredRootsCopy = new ArrayList<>(registeredRoots);
        }

        for (FileObject existingRoot : registeredRootsCopy) {
            if (file.equals(existingRoot) || FileUtil.isParentOf(existingRoot, file)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if (SYNCHRONOUS_UPDATES) {
            WORKER.post(() -> {}).waitFinished(); // flush tasks for tests (assumes threadpool size == 1)
        }
        if (!isSupportedFile(file)) {
            return null;
        }
        switch (type) {
            case ClassPath.SOURCE: return getSourcePath(file);
            case ClassPath.COMPILE:
                return attributeBasedPath(file, file2AllPath, "-classpath", "-cp", "--class-path", "--module-path", "-p");
            case JavaClassPathConstants.MODULE_CLASS_PATH:
                return attributeBasedPath(file, file2ClassPath, "-classpath", "-cp", "--class-path");
            case JavaClassPathConstants.MODULE_COMPILE_PATH:
                return attributeBasedPath(file, file2ModulePath, "--module-path", "-p");
            case ClassPath.BOOT:
            case JavaClassPathConstants.MODULE_BOOT_PATH:
                return getBootPath(file);
        }
        return null;
    }

    private ClassPath getSourcePath(FileObject file) {
        if (!SingleSourceFileUtil.isSupportedFile(file)) return null;
        synchronized (this) {
            //XXX: what happens if there's a Java file in user's home???
            if (file.isValid() && file.isData() && "text/x-java".equals(file.getMIMEType())) {
                return file2SourceCP.computeIfAbsent(file, f -> {
                    try {
                        String content = new String(file.asBytes(), FileEncodingQuery.getEncoding(file));
                        String packName = findPackage(content);
                        FileObject root = file.getParent();

                        if (packName != null) {
                            List<String> packageParts = Arrays.asList(packName.split("\\."));

                            Collections.reverse(packageParts);

                            for (String packagePart : packageParts) {
                                if (!root.getNameExt().equalsIgnoreCase(packagePart)) {
                                    //ignore files outside of proper package structure,
                                    //those may too easily lead to using a too general
                                    //directory as a root, leading to too much indexing:
                                    return null;
                                }
                                root = root.getParent();
                            }
                        }

                        ClassPath srcCP = root2SourceCP.computeIfAbsent(root, r -> {
                            return ClassPathSupport.createClassPath(Arrays.asList(new RootPathResourceImplementation(r)));
                        });

                        ParsedFileOptions options = SingleSourceFileUtil.getOptionsFor(root);

                        if (options != null) {
                            WORKER.post(root2RegistrationRefresh.computeIfAbsent(root, r -> new RegistrationRefresh(srcCP, options, r)));
                        }

                        return srcCP;
                    } catch (IOException ex) {
                        LOG.log(Level.FINE, "Failed to read sourcefile " + file, ex);
                    }
                    return null;
                });
            } else {
                FileObject folder = file;

                while (!folder.isRoot()) {
                    ClassPath cp = root2SourceCP.get(folder);

                    if (cp != null) {
                        return cp;
                    }

                    folder = folder.getParent();
                }

                return null;
            }
        }
    }

    private synchronized FileObject getSourceRootImpl(FileObject file) {
        for (FileObject root : root2SourceCP.keySet()) {
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return root;
            }
        }

        return null;
    }

    public FileObject getSourceRoot(FileObject file) {
        FileObject root = getSourceRootImpl(file);

        if (root == null) {
            getSourcePath(file);
            root = getSourceRootImpl(file);
        }

        return root;
    }

    public boolean isSourceLauncher(FileObject file) {
        return getSourceRoot(file) != null;
    }

    public boolean isRegisteredSourceLauncher(FileObject file) {
        FileObject root = getSourceRoot(file);

        if (root == null) {
            return false;
        }

        synchronized (registeredRoots) {
            return registeredRoots.contains(root);
        }
    }

    private ClassPath getBootPath(FileObject file) {
        if (isSourceLauncher(file)) {
            return JavaPlatformManager.getDefault()
                                      .getDefaultPlatform()
                                      .getBootstrapLibraries();
        }

        return null;
    }

    private static final Set<JavaTokenId> IGNORED_TOKENS = EnumSet.of(
        JavaTokenId.BLOCK_COMMENT,
        JavaTokenId.JAVADOC_COMMENT,
        JavaTokenId.LINE_COMMENT,
        JavaTokenId.WHITESPACE
    );

    private static final Set<JavaTokenId> STOP_TOKENS = EnumSet.of(
        JavaTokenId.IMPORT,
        JavaTokenId.PUBLIC,
        JavaTokenId.PROTECTED,
        JavaTokenId.PRIVATE,
        JavaTokenId.CLASS,
        JavaTokenId.LBRACE
    );

    static String findPackage(String fileContext) {
        TokenHierarchy<String> th = TokenHierarchy.create(fileContext, true, JavaTokenId.language(), IGNORED_TOKENS, null);
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());

        ts.moveStart();

        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.PACKAGE) {
                StringBuilder packageName = new StringBuilder();
                while (ts.moveNext() && (ts.token().id() == JavaTokenId.DOT || ts.token().id() == JavaTokenId.IDENTIFIER)) {
                    packageName.append(ts.token().text());
                }
                return packageName.toString();
            } else if (STOP_TOKENS.contains(ts.token().id())) {
                break;
            }
        }

        return null;
    }

    private ClassPath attributeBasedPath(FileObject file, Map<FileObject, ClassPath> file2ClassPath, String... optionKeys) {
        if (!isSourceLauncher(file)) {
            return null;
        }

        synchronized (this) {
        return file2ClassPath.computeIfAbsent(file, f -> {
            ParsedFileOptions delegate = SingleSourceFileUtil.getOptionsFor(f);

            if (delegate == null) {
                return null;
            }
            AttributeBasedClassPathImplementation cpi = new AttributeBasedClassPathImplementation(delegate, optionKeys);

            return ClassPathFactory.createClassPath(cpi);
        });
        }
    }

    private static final class AttributeBasedClassPathImplementation extends FileChangeAdapter implements ChangeListener, ClassPathImplementation {
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final Task updateDelegatesTask = WORKER.create(this::doUpdateDelegates);
        private final Set<String> directoriesWithListener = new HashSet<>();
        private final ParsedFileOptions delegate;
        private final Set<String> optionKeys;
        private Set<URL> currentURLs;
        private List<? extends PathResourceImplementation> delegates = Collections.emptyList();

        public AttributeBasedClassPathImplementation(ParsedFileOptions delegate, String... optionKeys) {
            this.delegate = delegate;
            this.optionKeys = new HashSet<>(Arrays.asList(optionKeys));
            delegate.addChangeListener(this);
            updateDelegates();
        }

        @Override
        public void stateChanged(ChangeEvent ce) {
            updateDelegates();
        }

        private void updateDelegates() {
            if (SYNCHRONOUS_UPDATES) {
                doUpdateDelegates();
            } else {
                updateDelegatesTask.schedule(0);
            }
        }

        private void doUpdateDelegates() {
            Set<URL> newURLs = new HashSet<>();
            List<PathResourceImplementation> newDelegates = new ArrayList<>();
            List<? extends String> parsed = delegate.getArguments();
            File workDirectory = Utilities.toFile(delegate.getWorkDirectory());
            Set<String> toRemoveFSListeners = new HashSet<>();
            Set<String> addedFSListeners = new HashSet<>();

            synchronized (this) {
                toRemoveFSListeners.addAll(directoriesWithListener);
            }

            for (int i = 0; i < parsed.size() - 1; i++) {
                String currentOption = parsed.get(i);

                if (optionKeys.contains(currentOption)) {
                    for (String piece : parsed.get(i + 1).split(File.pathSeparator)) {
                        boolean hasStar = false;
                        boolean isClassPath = CLASSPATH_OPTIONS.contains(currentOption);

                        if (isClassPath && piece.endsWith("*") && piece.length() > 1) {
                            char sep = piece.charAt(piece.length() - 2);

                            if (sep == File.separatorChar ||
                                sep == '/') {
                                hasStar = true;
                                piece = piece.substring(0, piece.length() - 2);
                            }
                        }

                        File pieceFile = new File(piece);

                        if (!pieceFile.isAbsolute()) {
                            pieceFile = new File(workDirectory, piece);
                        }

                        File f = FileUtil.normalizeFile(pieceFile);
                        Iterable<File> expandedPaths;

                        if (MODULAR_DIRECTORY_OPTIONS.contains(currentOption)) {
                            if (!toRemoveFSListeners.remove(f.getAbsolutePath()) &&
                                addedFSListeners.add(f.getAbsolutePath())) {
                                FileUtil.addFileChangeListener(this, f);
                            }
                        }

                        if (MODULAR_DIRECTORY_OPTIONS.contains(currentOption) &&
                            f.isDirectory() &&
                            !new File(f, "module-info.class").exists()) {
                            File[] children = f.listFiles();

                            if (children != null) {
                                expandedPaths = Arrays.asList(children);
                            } else {
                                expandedPaths = Collections.emptyList();
                            }
                        } else if (hasStar && isClassPath) {
                            if (!toRemoveFSListeners.remove(f.getAbsolutePath()) &&
                                addedFSListeners.add(f.getAbsolutePath())) {
                                FileUtil.addFileChangeListener(this, f);
                            }

                            File[] children = f.listFiles();

                            if (children != null) {
                                expandedPaths = Arrays.stream(children)
                                                      .filter(c -> c.getName()
                                                                    .toLowerCase(Locale.ROOT)
                                                                    .endsWith(".jar"))
                                                      .toList();
                            } else {
                                expandedPaths = Collections.emptyList();
                            }
                        } else {
                            expandedPaths = Arrays.asList(f);
                        }

                        for (File expanded : expandedPaths) {
                            URL u = FileUtil.urlForArchiveOrDir(expanded);
                            if (u == null) {
                                LOG.log(Level.INFO,
                                        "While parsing command line option '{0}' with parameter '{1}', path entry looks to be invalid: '{2}'",
                                        new Object[] {currentOption, parsed.get(i + 1), piece});
                                continue;
                            }
                            newURLs.add(u);
                            newDelegates.add(ClassPathSupport.createResource(u));
                        }
                    }
                }
            }

            for (String removeFSListener : toRemoveFSListeners) {
                FileUtil.removeFileChangeListener(this, new File(removeFSListener));
            }

            synchronized (this) {
                if (Objects.equals(currentURLs, newURLs)) {
                    return ;
                }
                this.currentURLs = newURLs;
                this.delegates = newDelegates;
                this.directoriesWithListener.removeAll(toRemoveFSListeners);
                this.directoriesWithListener.addAll(addedFSListeners);
            }

            pcs.firePropertyChange(PROP_RESOURCES, null, null);
        }

        @Override
        public synchronized List<? extends PathResourceImplementation> getResources() {
            return delegates;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            updateDelegates();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            updateDelegates();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            updateDelegates();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            updateDelegates();
        }

    }

    private static final class RootPathResourceImplementation implements FilteringPathResourceImplementation {

        private final URL root;
        private final URL[] roots;
        private final AtomicReference<String> lastCheckedAsIncluded = new AtomicReference<>();

        public RootPathResourceImplementation(FileObject root) {
            this.root = root.toURL();
            this.roots = new URL[] {this.root};
        }
        
        @Override
        public boolean includes(URL root, String resource) {
            if (!resource.endsWith("/")) {
                int lastSlash = resource.lastIndexOf('/');
                if (lastSlash != (-1)) {
                    resource = resource.substring(0, lastSlash + 1);
                }
            }
            if (resource.equals(lastCheckedAsIncluded.get())) {
                return true;
            }
            FileObject fo = URLMapper.findFileObject(root);
            fo = fo != null ? fo.getFileObject(resource) : null;
            boolean included = fo == null || FileOwnerQuery.getOwner(fo) == null;
            if (included) {
                lastCheckedAsIncluded.set(resource);
            }
            return included;
        }

        @Override
        public URL[] getRoots() {
            return roots;
        }

        @Override
        public ClassPathImplementation getContent() {
            return null;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        
    }

    private class RegistrationRefresh implements ChangeListener, Runnable {
        private final ClassPath srcCP;
        private final ParsedFileOptions options;
        private final FileObject root;

        public RegistrationRefresh(ClassPath srcCP,
                                   ParsedFileOptions options,
                                   FileObject root) {
            this.srcCP = srcCP;
            this.options = options;
            this.root = root;
            options.addChangeListener(this);
        }

        @Override
        public void run() {
            GlobalPathRegistry registry = GlobalPathRegistry.getDefault();
            if (options.registerRoot()) {
                synchronized (registeredRoots) {
                    registeredRoots.add(root);
                }
                registry.register(ClassPath.SOURCE, new ClassPath[] {srcCP});
            } else {
                synchronized (registeredRoots) {
                    registeredRoots.remove(root);
                }
                if (registry.getPaths(ClassPath.SOURCE).contains(srcCP)) {
                    registry.unregister(ClassPath.SOURCE, new ClassPath[] {srcCP});
                }
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            WORKER.post(this);
        }
    }

}
