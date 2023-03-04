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
package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * The {@link CompilerOptionsQueryImplementation} for automatic module name stored in manifest.
 * @author Tomas Zezula
 */
final class AutomaticModuleNameCompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {

    private static final Logger LOG = Logger.getLogger(AutomaticModuleNameCompilerOptionsQueryImpl.class.getName());
    private static final String OPT_AUTOMATIC_MODULE_NAME = "-XDautomatic-module-name";  //NOI18N
    private static final String ATTR_AUTOMATIC_MOD_NAME = "Automatic-Module-Name";   //NOI18N
    private static final String MODULE_INFO_JAVA = "module-info.java";                  //NOI18N

    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final SourceRoots sources;
    private final String manifestProp;
    private final AtomicReference<R> result;

    AutomaticModuleNameCompilerOptionsQueryImpl(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator eval,
            @NonNull final SourceRoots sources,
            @NonNull final String manifestProp) {
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("eval", eval);       //NOI18N
        Parameters.notNull("sources", sources); //NOI18N
        Parameters.notNull("manifestProp", manifestProp);   //NOI18N
        this.helper = helper;
        this.eval = eval;
        this.sources = sources;
        this.manifestProp = manifestProp;
        this.result = new AtomicReference<>();
    }

    @CheckForNull
    @Override
    public Result getOptions(FileObject file) {
        if (isOwned(file, sources)) {
            R r = result.get();
            if (r == null) {
                r = new R(helper, eval, sources, manifestProp);
                if (!result.compareAndSet(null, r)) {
                    r = result.get();
                }
            }
            assert r != null;
            return r;
        }
        return null;
    }

    private static boolean isOwned(
            @NonNull final FileObject file,
            @NonNull final SourceRoots roots) {
        for (FileObject root : roots.getRoots()) {
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return true;
            }
        }
        return false;
    }

    private static final class R extends Result implements PropertyChangeListener {

        private final AntProjectHelper helper;
        private final PropertyEvaluator eval;
        private final SourceRoots sources;
        private final String manifestProp;
        private final ChangeSupport listeners;
        private final FileChangeListener manifestListener;
        private final FileChangeListener modInfoListener;
        //@GuardedBy("this")
        private List<? extends String> cache;
        //@GuardedBy("this")
        private File currentManifestFile;
        //@GuardedBy("this")
        private Collection<? extends File> currentModuleInfos = Collections.emptySet();

        R(
                @NonNull final AntProjectHelper helper,
                @NonNull final PropertyEvaluator eval,
                @NonNull final SourceRoots sources,
                @NonNull final String manifestProp) {
            this.helper = helper;
            this.eval = eval;
            this.sources = sources;
            this.manifestProp = manifestProp;
            this.listeners = new ChangeSupport(this);
            Set<FCL.Op> filter = EnumSet.allOf(FCL.Op.class);
            filter.remove(FCL.Op.FILE_ATTR_CHANGED);
            this.manifestListener = new FCL(this::reset, filter);
            filter = EnumSet.allOf(FCL.Op.class);
            filter.remove(FCL.Op.FILE_ATTR_CHANGED);
            filter.remove(FCL.Op.FILE_CHANGED);
            this.modInfoListener = new FCL(this::reset, filter);
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
            this.sources.addPropertyChangeListener(WeakListeners.propertyChange(this, this.sources));
        }

        @Override
        public List<? extends String> getArguments() {
            List<? extends String> res;
            File lastManifestFile;
            Collection<? extends File> lastModuleInfos;
            synchronized (this) {
                res = cache;
                lastManifestFile = currentManifestFile;
                lastModuleInfos = currentModuleInfos;
            }
            File manifestFile = null;
            final Collection<File> moduleInfos = new ArrayList<>();
            if (res == null) {
                res = Collections.emptyList();
                if (!hasModuleInfo(moduleInfos)) {
                    manifestFile = Optional.ofNullable(eval.getProperty(manifestProp))
                            .map(helper::resolveFile)
                            .orElse(null);
                    if (manifestFile != null) {
                        if (manifestFile.isFile() && manifestFile.canRead()) {
                            try {
                                try(InputStream in = new BufferedInputStream(new FileInputStream(manifestFile))) {
                                    final Manifest manifest = new Manifest(in);
                                    final String moduleName = manifest.getMainAttributes().getValue(ATTR_AUTOMATIC_MOD_NAME);
                                    if (moduleName != null) {
                                        res = Collections.singletonList(String.format(
                                                "%s:%s",    //NOI18N
                                                OPT_AUTOMATIC_MODULE_NAME,
                                                moduleName));
                                    }
                                }
                            } catch (IOException ioe) {
                                LOG.log(
                                        Level.WARNING,
                                        "Cannot read: {0}, reason: {1}",    //NOI18N
                                        new Object[]{
                                            manifestFile.getAbsolutePath(),
                                            ioe.getMessage()
                                        });
                            }
                        }
                    }
                }
                synchronized (this) {
                    if (cache == null) {
                        cache = res;
                        final boolean sameManifests = Objects.equals(lastManifestFile,manifestFile);
                        if (lastManifestFile != null && !sameManifests) {
                            FileUtil.removeFileChangeListener(this.manifestListener, lastManifestFile);
                        }
                        if (manifestFile != null && !sameManifests) {
                            FileUtil.addFileChangeListener(this.manifestListener, manifestFile);
                        }
                        currentManifestFile = manifestFile;
                        for (File f : lastModuleInfos) {
                            FileUtil.removeFileChangeListener(this.modInfoListener, f);
                        }
                        for (File f : moduleInfos) {
                            FileUtil.addFileChangeListener(this.modInfoListener, f);
                        }
                        currentModuleInfos = moduleInfos;
                    } else {
                        res = cache;
                    }
                }
            }
            return res;
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener listener) {
            this.listeners.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            this.listeners.removeChangeListener(listener);
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            final Object from = evt.getSource();
            if (from == sources && SourceRoots.PROP_ROOTS.equals(propName)) {
                reset();
            } else if (from == eval && (propName == null || propName.equals(this.manifestProp))) {
                reset();
            }
        }

        private boolean hasModuleInfo(final Collection<? super File> moduleInfos) {
            boolean vote = false;
            for (FileObject root : sources.getRoots()) {
                if (root.getFileObject(MODULE_INFO_JAVA) != null) {
                    vote = true;
                }
                Optional.ofNullable(FileUtil.toFile(root))
                        .map((f) -> new File(f, MODULE_INFO_JAVA))
                        .ifPresent(moduleInfos::add);
            }
            return vote;
        }

        private void reset() {
            synchronized (this) {
                this.cache = null;
            }
            this.listeners.fireChange();
        }
    }

    private static final class FCL implements FileChangeListener {

        private static enum Op {
            FILE_CREATED,
            FOLDER_CREATED,
            FILE_CHANGED,
            FILE_DELETED,
            FILE_RENAMED,
            FILE_ATTR_CHANGED
        }

        private final Runnable action;
        private final Set<? extends Op> filter;

        FCL(
                @NonNull final Runnable action,
                @NonNull final Set<? extends Op> filter) {
            this.action = action;
            this.filter = filter;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            if (filter.contains(Op.FOLDER_CREATED)) {
                action.run();
            }
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            if (filter.contains(Op.FILE_CREATED)) {
                action.run();
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            if (filter.contains(Op.FILE_CHANGED)) {
                action.run();
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (filter.contains(Op.FILE_DELETED)) {
                action.run();
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            if (filter.contains(Op.FILE_RENAMED)) {
                action.run();
            }
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            if (filter.contains(Op.FILE_ATTR_CHANGED)) {
                action.run();
            }
        }
    }
}
