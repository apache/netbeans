/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.java.api.common.project.ui;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.queries.MultiModuleGroupQuery;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.actions.FileSystemAction;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUIUtils;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Multi Module logical view content.
 * @author Tomas Zezula
 * @since 1.115
 */
public final class MultiModuleNodeFactory implements NodeFactory {
    private static final Logger LOG = Logger.getLogger(MultiModuleNodeFactory.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(MultiModuleNodeFactory.class);
    private final MultiModule sourceModules;
    private final MultiModule testModules;
    private final LibrariesSupport libsSupport;

    private MultiModuleNodeFactory(
            @NullAllowed final MultiModule sourceModules,
            @NullAllowed final MultiModule testModules,
            @NullAllowed final LibrariesSupport libsSupport) {
        this.sourceModules = sourceModules;
        this.testModules = testModules;
        this.libsSupport = libsSupport;
    }

    @Override
    public NodeList<?> createNodes(@NonNull final Project project) {
        return new Nodes(
                project,
                sourceModules,
                testModules,
                libsSupport);
    }

    /**
     * A builder of the {@link MultiModuleNodeFactory}.
     */
    public static final class Builder {
        private LibrariesSupport libSupport;
        private MultiModule mods;
        private MultiModule testMods;

        private Builder() {
        }

        /**
         * Adds project's source modules into the logical view.
         * @param sourceModules the module roots
         * @param srcRoots the source roots
         * @return the {@link Builder}
         */
        @NonNull
        public Builder setSources(
                @NonNull final SourceRoots sourceModules,
                @NonNull final SourceRoots srcRoots) {
           mods = MultiModule.getOrCreate(sourceModules, srcRoots);
           return this;
        }

        /**
         * Adds project's test modules into the logical view.
         * @param sourceModules the module roots
         * @param srcRoots the source roots
         * @return the {@link Builder}
         */
        @NonNull
        public Builder setTests(
                @NonNull final SourceRoots testModules,
                @NonNull final SourceRoots testRoots) {
            testMods = MultiModule.getOrCreate(testModules, testRoots);
            return this;
        }

        /**
         * Adds libraries nodes into the logical view.
         * @param helper the {@link UpdateHelper} to resolve paths
         * @param evaluator the {@link PropertyEvaluator} to access project properties
         * @param refHelper the {@link ReferenceHelper} to resolve project references
         * @return the {@link Builder}
         */
        @NonNull
        public Builder addLibrariesNodes(
                @NonNull final UpdateHelper helper,
                @NonNull final PropertyEvaluator evaluator,
                @NonNull final ReferenceHelper refHelper) {
            libSupport = new LibrariesSupport(helper, evaluator, refHelper);
            return this;
        }

        /**
         * Adds actions into the modules libraries nodes.
         * If the {@link Action} is an instance of {@link ContextAwareAction}
         * the {@link Lookup} with source path is injected into the {@link Action}.
         * The source path can be used to identify actual module of an multi module project.
         * @param actions the {@link Action}s to add
         * @return the {@link Builder}
         */
        @NonNull
        public Builder addLibrariesNodeActions(@NonNull final Action... actions) {
            if (libSupport == null) {
                throw new IllegalStateException("Libraries are not enabled");   //NOI18N
            }
            libSupport.addActions(false, actions);
            return this;
        }

        /**
         * Adds actions into the modules test libraries nodes.
         * If the {@link Action} is an instance of {@link ContextAwareAction}
         * the {@link Lookup} with source path is injected into the {@link Action}.
         * The source path can be used to identify actual module of an multi module project.
         * @param actions the {@link Action}s to add
         * @return the {@link Builder}
         */
        @NonNull
        public Builder addTestLibrariesNodeActions(@NonNull final Action... actions) {
            if (libSupport == null) {
                throw new IllegalStateException("Libraries are not enabled");   //NOI18N
            }
            libSupport.addActions(true, actions);
            return this;
        }

        /**
         * Builds the {@link MultiModuleNodeFactory}.
         * @return the new {@link MultiModuleNodeFactory} instance
         */
        @NonNull
        public MultiModuleNodeFactory build() {
            return new MultiModuleNodeFactory(
                    mods,
                    testMods,
                    libSupport);
        }

        /**
         * Creates a new {@link Builder}.
         * @return the {@link Builder}
         */
        @NonNull
        public static Builder create() {
            return new Builder();
        }
    }

    private static final class Nodes implements NodeList<ModuleKey>, PropertyChangeListener {
        private final Project project;
        private final MultiModule sourceModules;
        private final MultiModule testModules;
        private final LibrariesSupport libsSupport;
        private final ChangeSupport listeners;

        Nodes(
                @NonNull final Project project,
                @NullAllowed final MultiModule sourceModules,
                @NullAllowed final MultiModule testModules,
                @NullAllowed final LibrariesSupport libsSupport) {
            Parameters.notNull("project", project);     //NOI18N
            this.project = project;
            this.sourceModules = sourceModules;
            this.testModules = testModules;
            this.libsSupport = libsSupport;
            this.listeners = new ChangeSupport(this);
        }

        @Override
        public List<ModuleKey> keys() {
            return Stream.concat(
                    this.sourceModules == null ? Stream.empty() : this.sourceModules.getModuleNames().stream(),
                    this.testModules == null ? Stream.empty() : this.testModules.getModuleNames().stream())
                .sorted()
                .distinct()
                .map((name) -> new ModuleKey(project, name, sourceModules, testModules, libsSupport))
                .collect(Collectors.toList());
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener l) {
            this.listeners.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            this.listeners.removeChangeListener(l);
        }

        @Override
        public Node node(@NonNull final ModuleKey key) {
            return new ModuleNode(key);
        }

        @Override
        public void addNotify() {
            if (this.sourceModules != null) {
                this.sourceModules.addPropertyChangeListener(this);
            }
            if (this.testModules != null) {
                this.testModules.addPropertyChangeListener(this);
            }
        }

        @Override
        public void removeNotify() {
            if (this.sourceModules != null) {
                this.sourceModules.removePropertyChangeListener(this);
            }
            if (this.testModules != null) {
                this.testModules.removePropertyChangeListener(this);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (MultiModule.PROP_MODULES.equals(evt.getPropertyName())) {
                this.listeners.fireChange();
            }
        }
    }

    private static final class ModuleKey {
        private final Project project;
        private final MultiModule sourceModules;
        private final MultiModule testModules;
        private final String moduleName;
        private final LibrariesSupport libsSupport;

        ModuleKey(
                @NonNull final Project project,
                @NonNull final String moduleName,
                @NullAllowed final MultiModule sourceModules,
                @NullAllowed final MultiModule testModules,
                @NullAllowed final LibrariesSupport libsSupport) {
            Parameters.notNull("project", project);             //NOI18N
            Parameters.notNull("moduleName", moduleName);       //NOI18N
            this.project = project;
            this.moduleName = moduleName;
            this.sourceModules = sourceModules;
            this.testModules = testModules;
            this.libsSupport = libsSupport;
        }

        @NonNull
        String getModuleName() {
            return moduleName;
        }

        @CheckForNull
        MultiModule getSourceModules() {
            return sourceModules;
        }

        @CheckForNull
        MultiModule getTestModules() {
            return testModules;
        }

        @NonNull
        Project getProject() {
            return project;
        }

        @CheckForNull
        LibrariesSupport getLibrariesSupport() {
            return libsSupport;
        }

        @Override
        public int hashCode() {
            return moduleName.hashCode();
        }

        @Override
        public boolean equals(@NullAllowed final Object other) {
            if (other == this) {
                return true;
            }
            if (other.getClass() != ModuleKey.class) {
                return false;
            }
            return ((ModuleKey)other).moduleName.equals(this.moduleName);
        }
    }

    private static final class ModuleNode extends AbstractNode implements PropertyChangeListener, FileChangeListener, FileStatusListener {
        @StaticResource
        private static final String ICON = "org/netbeans/modules/java/api/common/project/ui/resources/module.png";
        private final Project prj;
        private final MultiModule modules;
        private final MultiModule testModules;
        private final String moduleName;
        private final RequestProcessor.Task annotationChangeTask;
        //@GuardedBy("this")
        private ClassPath srcModPath;
        //@GuardedBy("this")
        private ClassPath testModPath;
        //@GuardedBy("this")
        private Set<? extends File> fosListensOn;
        //@GuardedBy("this")
        private Collection<? extends FileObject> fosCache;
        //@GuardedBy("this")
        private Collection<? extends Pair<FileSystem,FileStatusListener>> fsListensOn;
        private Action[] actions;
        private volatile boolean iconChanged;
        private volatile boolean nameChanged;

        ModuleNode(@NonNull final ModuleKey key) {
            this(
                    key,
                    new DynLkp());
        }

        private ModuleNode(@NonNull final ModuleKey key, @NonNull final DynLkp lookup) {
            super(new ModuleChildren (key), lookup);
            this.prj = key.getProject();
            this.modules = key.getSourceModules();
            this.testModules = key.getTestModules();
            this.moduleName = key.getModuleName();
            this.annotationChangeTask = RP.create(this::processAnnotationChange);
            synchronized (this) {
                fosListensOn = Collections.emptySet();
                fsListensOn = Collections.emptySet();
            }
            setIconBaseWithExtension(ICON);
            setName(moduleName);
            lookup.update(new ContentLkp(this, key.getProject()));
            updateFileStatusListeners();
        }

        @Override
        public String getShortDescription() {
            final Collection<? extends FileObject> locs = getFileObjects();
            final StringBuilder sb = new StringBuilder("<html>");   //NOI18N
            boolean cadr = false;
            for (FileObject fo : locs) {
                if (cadr) {
                    sb.append("<br>\n");    //NOI18N
                } else {
                    cadr = true;
                }
                sb.append(FileUtil.getFileDisplayName(fo));
            }
            return sb.toString();
        }

        @Override
        public Image getIcon(int type) {
            Image res = super.getIcon(type);
            final Collection<? extends FileObject> fos = new HashSet<>(getFileObjects());
            if (!fos.isEmpty()) {
                final Pair<FileSystem,Set<? extends FileObject>> p = findAnnotableFiles(fos);
                if (p != null) {
                    res = FileUIUtils.getImageDecorator(p.first()).annotateIcon(res, type, p.second());
                }
            }
            return res;
        }

        @Override
        public  Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public  String getDisplayName() {
            String dn = super.getDisplayName ();
            final Collection<? extends FileObject> fos = new HashSet<>(getFileObjects());
            if (!fos.isEmpty()) {
                final Pair<FileSystem,Set<? extends FileObject>> p = findAnnotableFiles(fos);
                if (p != null) {
                    dn = p.first().getDecorator ().annotateName (dn, p.second());
                }
            }
            return dn;
        }

        @Override
        public String getHtmlDisplayName() {
            String dn = super.getDisplayName ();
            final Collection<? extends FileObject> fos = new HashSet<>(getFileObjects());
            if (!fos.isEmpty()) {
                final Pair<FileSystem,Set<? extends FileObject>> p = findAnnotableFiles(fos);
                if (p != null) {
                    dn = p.first().getDecorator ().annotateNameHtml(dn, p.second());
                }
            }
            if (dn != null && !super.getDisplayName().equals(dn)) {
                return dn;
            }
            return super.getHtmlDisplayName();
        }

        @NonNull
        @Override
        public Action[] getActions(final boolean context) {
            if (context) {
                return super.getActions(context);
            } else {
                if (actions == null) {
                    actions = new Action[] {
                        CommonProjectActions.newFileAction(),
                        null,
                        SystemAction.get(FindAction.class),
                        null,
                        SystemAction.get(PasteAction.class ),
                        null,
                        SystemAction.get(FileSystemAction.class ),
                        null,
                        SystemAction.get(ToolsAction.class )
                    };
                }
                return actions;
            }
        }

        @Override
        protected void createPasteTypes(
                @NonNull final Transferable t,
                @NonNull final List<PasteType> s) {
            final List<Pair<FileObject,PasteType[]>> res = new ArrayList<>();
            for (FileObject fo : getFileObjects()) {
                if (fo.canWrite()) {
                    res.add(Pair.of(
                            fo,
                            DataFolder.findFolder(fo).getNodeDelegate().getPasteTypes(t)));
                }
            }
            switch (res.size()) {
                case 0:
                    break;
                case 1:
                    Collections.addAll(s, res.iterator().next().second());
                    break;
                default:
                    for (Pair<FileObject,PasteType[]> ptByFo : res) {
                        final FileObject fo = ptByFo.first();
                        for (PasteType pt : ptByFo.second()) {
                            final FileObject pdir = prj.getProjectDirectory();
                            String name = FileUtil.getRelativePath(pdir, fo);
                            if (name != null) {
                                name.replace('/', File.separatorChar);  //NOI18N
                            } else {
                                name = FileUtil.getFileDisplayName(fo);
                            }
                            name = NbBundle.getMessage(
                                    MultiModuleNodeFactory.class,
                                    "TXT_PasteInto",
                                    name);
                            s.add(new PasteInto(pt, name));
                        }
                    }
            }
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
                reset();
            }
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            reset();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            reset();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            reset();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reset();
        }

        @Override
        public void fileChanged(FileEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        public void annotationChanged(final FileStatusEvent ev) {
            if ((!iconChanged && ev.isIconChange())  || (!nameChanged && ev.isNameChange())) {
                for (FileObject fo : getFileObjects()) {
                    if (ev.hasChanged(fo)) {
                        iconChanged |= ev.isIconChange();
                        nameChanged |= ev.isNameChange();
                        break;
                    }
                }
            }
            annotationChangeTask.schedule(100);  // batch by 100 ms
        }

        private void processAnnotationChange() {
            if (iconChanged) {
                iconChanged = false;
                fireIconChange();
                fireOpenedIconChange();
            }
            if (nameChanged) {
                nameChanged = false;
                fireDisplayNameChange(null, null);
            }
        }

        private void reset() {
            synchronized (this) {
                fosCache = null;
            }
            updateFileStatusListeners();
            fireShortDescriptionChange(null, null);
        }

        @NonNull
        private Collection<? extends FileObject> getFileObjects() {
            Collection<? extends FileObject> res;
            ClassPath smp, tmp;
            synchronized(this) {
                res = fosCache;
                smp = srcModPath;
                if (smp == null) {
                    smp = srcModPath = modules == null ? ClassPath.EMPTY : modules.getSourceModulePath();
                    smp.addPropertyChangeListener(WeakListeners.propertyChange(this, smp));
                }
                tmp = testModPath;
                if (tmp == null) {
                    tmp = testModPath = testModules == null ? ClassPath.EMPTY : testModules.getSourceModulePath();
                    tmp.addPropertyChangeListener(WeakListeners.propertyChange(this, tmp));
                }
            }
            if (res == null) {
                final Set<? extends File> newFosListensOn = Stream.concat(
                        smp.entries().stream(),
                        tmp.entries().stream())
                        .map((e)->FileUtil.archiveOrDirForURL(e.getURL()))
                        .filter((f) -> f != null)
                        .map((f) -> new File(f, moduleName))
                        .collect(Collectors.toSet());
                final Comparator<FileObject> pathComparator = (a,b)->a.getPath().compareTo(b.getPath());
                final Set<FileObject> allLocs = new HashSet<>();
                final List<FileObject> srcLocs = new ArrayList<>();
                final List<FileObject> testLocs = new ArrayList<>();
                for (FileObject loc : smp.findAllResources(moduleName)) {
                    if (!allLocs.contains(loc) && loc.isFolder()) {
                        srcLocs.add(loc);
                        allLocs.add(loc);
                    }
                }
                Collections.sort(srcLocs, pathComparator);
                for (FileObject loc : tmp.findAllResources(moduleName)) {
                    if (!allLocs.contains(loc) && loc.isFolder()) {
                        testLocs.add(loc);
                        allLocs.add(loc);
                    }
                }
                Collections.sort(testLocs, pathComparator);
                srcLocs.addAll(testLocs);
                res = srcLocs;
                synchronized (this) {
                    fosCache = res;
                    for (File fosl : fosListensOn) {
                        FileUtil.removeFileChangeListener(this, fosl);
                    }
                    for (File fosl : newFosListensOn) {
                        FileUtil.addFileChangeListener(this, fosl);
                    }
                    fosListensOn = newFosListensOn;
                }
            }
            return res;
        }

        private void updateFileStatusListeners() {
            final Collection<FileSystem> fileSystems = new HashSet<>();
            for (FileObject fo : getFileObjects()) {
                try {
                    fileSystems.add(fo.getFileSystem());
                } catch (FileStateInvalidException e) {
                    LOG.log(
                            Level.WARNING,
                            "Ignoring invalid file: {0}",   //NOI18N
                            FileUtil.getFileDisplayName(fo));
                }
            }
            synchronized (this) {
                for (Pair<FileSystem,FileStatusListener> p : fsListensOn) {
                    p.first().removeFileStatusListener(p.second());
                }
                final List<Pair<FileSystem,FileStatusListener>> newFsListensOn = new ArrayList<>();
                for (FileSystem fs : fileSystems) {
                    FileStatusListener l = FileUtil.weakFileStatusListener(this, fs);
                    fs.addFileStatusListener(l);
                    newFsListensOn.add(Pair.of(fs,l));
                }
                fsListensOn = newFsListensOn;
            }
        }

        @CheckForNull
        private static Pair<FileSystem,Set<? extends FileObject>> findAnnotableFiles(Collection<? extends FileObject> fos) {
            FileSystem fs = null;
            final Set<FileObject> toAnnotate = new HashSet<>();
            for (FileObject fo : fos) {
                try {
                    FileSystem tmp = fo.getFileSystem();
                    if (fs == null) {
                        fs = tmp;
                        toAnnotate.add(fo);
                    } else if (fs.equals(tmp)) {
                        toAnnotate.add(fo);
                    }
                } catch (FileStateInvalidException e) {
                LOG.log(
                    Level.WARNING,
                    "Cannot determine annotations for invalid file: {0}",   //NOI18N
                    FileUtil.getFileDisplayName(fo));
                }
            }
            return fs == null ?
                    null :
                    Pair.of(fs, toAnnotate);
        }

        private static final class DynLkp extends ProxyLookup {
            void update(Lookup... lkps) {
                setLookups(lkps);
            }
        }

        private static final class ContentLkp extends ProxyLookup {
            private final AtomicReference<Pair<InstanceContent,Collection<? extends FileObject>>> fos;
            private final AtomicReference<Pair<InstanceContent,Collection<? extends FileObject>>> dos;
            private final ModuleNode node;

            ContentLkp(
                    @NonNull final ModuleNode node,
                    @NonNull final Object... fixedContent) {
                Parameters.notNull("node", node);                 //NOI18N
                Parameters.notNull("fixedContent", fixedContent); //NOI18N
                this.node = node;
                this.fos = new AtomicReference<>(Pair.of(new InstanceContent(),Collections.emptyList()));
                this.dos = new AtomicReference<>(Pair.of(new InstanceContent(),Collections.emptyList()));
                this.setLookups(
                        new AbstractLookup(fos.get().first()),
                        new AbstractLookup(dos.get().first()),
                        Lookups.fixed(fixedContent));
            }

            @Override
            protected void beforeLookup(Template<?> template) {
                super.beforeLookup(template);
                final Class<?> clz = template.getType();
                if (clz == FileObject.class) {
                    final Pair<InstanceContent,Collection<? extends FileObject>> p = fos.get();
                    Collection<? extends FileObject> currentFos = p.second();
                    Collection<? extends FileObject> newFos = node.getFileObjects();
                    if (currentFos != newFos) {
                        p.first().set(newFos, null);
                        fos.set(Pair.of(p.first(),newFos));
                    }
                } else if (clz == DataObject.class) {
                    final Pair<InstanceContent,Collection<? extends FileObject>> p = dos.get();
                    Collection<? extends FileObject> currentFos = p.second();
                    Collection<? extends FileObject> newFos = node.getFileObjects();
                    if (currentFos != newFos) {
                        p.first().set(
                                new ArrayList<>(newFos),
                                new DObjConvertor());
                    }
                }
            }

            private static  final class DObjConvertor implements InstanceContent.Convertor<FileObject, DataObject> {

                @Override
                public DataObject convert(FileObject obj) {
                    try {
                        return DataObject.find(obj);
                    } catch (DataObjectNotFoundException e) {
                        return null;
                    }
                }

                @Override
                public Class<? extends DataObject> type(FileObject obj) {
                    return DataObject.class;
                }

                @Override
                public String id(FileObject obj) {
                    return obj.getPath();
                }

                @Override
                public String displayName(FileObject obj) {
                    return FileUtil.getFileDisplayName(obj);
                }
            }
        }

        private static final class PasteInto extends PasteType {
            private final PasteType delegate;
            private final String name;

            PasteInto(
                    @NonNull final PasteType delegate,
                    @NonNull final String name) {
                this.delegate = delegate;
                this.name = name;
            }

            @Override
            public HelpCtx getHelpCtx() {
                return delegate.getHelpCtx();
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Transferable paste() throws IOException {
                return delegate.paste();
            }
        }
    }

    private static final class ModuleChildren extends Children.Keys<ModuleChildren.Key> implements PropertyChangeListener {
        private final String moduleName;
        private final Project project;
        private final Sources sources;
        private final MultiModuleGroupQuery groupQuery;
        private final MultiModule srcModule;
        private final MultiModule testModule;
        private final LibrariesSupport libsSupport;
        private final RequestProcessor.Task refresh;
        private final AtomicReference<ClassPath> srcPath;
        private final AtomicReference<ClassPath> testPath;

        private ModuleChildren(final ModuleKey key) {
            Parameters.notNull("key", key);
            this.moduleName = key.getModuleName();
            this.project = key.getProject();
            this.sources = project.getLookup().lookup(Sources.class);
            this.groupQuery = project.getLookup().lookup(MultiModuleGroupQuery.class);
            this.srcModule = key.getSourceModules();
            this.testModule = key.getTestModules();
            this.libsSupport = key.getLibrariesSupport();
            this.srcPath = new AtomicReference<>();
            this.testPath = new AtomicReference<>();
            refresh = RP.create(()->setKeys(createKeys()));
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            ClassPath cp = Optional.ofNullable(srcModule)
                    .map((m) -> m.getModuleSources(moduleName))
                    .orElse(ClassPath.EMPTY);
            if (srcPath.compareAndSet(null, cp)) {
                cp.addPropertyChangeListener(this);
            }
            cp = Optional.ofNullable(testModule)
                    .map((m) -> m.getModuleSources(moduleName))
                    .orElse(ClassPath.EMPTY);
            if (testPath.compareAndSet(null, cp)) {
                cp.addPropertyChangeListener(this);
            }
            setKeys(createKeys());
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            ClassPath cp = srcPath.get();
            if (cp != null && srcPath.compareAndSet(cp, null)) {
                cp.removePropertyChangeListener(this);
            }
            cp = testPath.get();
            if (cp != null && testPath.compareAndSet(cp, null)) {
                cp.removePropertyChangeListener(this);
            }
            setKeys(Collections.emptySet());
        }

        @Override
        @NonNull
        protected Node[] createNodes(@NonNull final Key key) {
            if (key.isSource()) {
                Node n = PackageView.createPackageView(key.getSourceGroup());
                MultiModuleGroupQuery.Result r = groupQuery.findModuleInfo(key.getSourceGroup());
                if (r == null) {
                    if (key.isTests()) {
                        n = new TestRootNode(n, null);
                    }
                } else {
                    if (key.isTests()) {
                        n = new TestRootNode(n, r.getPathFromModule());
                    } else {
                        n = new SimpleLabelNode(n, r.getPathFromModule());
                    }
                }
                return new Node[] {n};
            } else if (libsSupport != null) {
                final FileObject[] roots = key.getSourceRoots();
                if (roots.length > 0) {
                    final ClassPath scp = key.getSourcePath();
                    final Lookup lkp = Lookups.fixed(project, scp);
                    if (key.isTests()) {
                        return new Node[] {
                            new LibrariesNode.Builder(this.project,
                                    libsSupport.getPropertyEvaluator(),
                                    libsSupport.getUpdateHelper(),
                                    libsSupport.getReferenceHelper(),
                                    libsSupport.getClassPathSupport())
                                .setName(NbBundle.getMessage(MultiModuleNodeFactory.class,"CTL_TestLibrariesNode"))
                                .addClassPathProperties(ProjectProperties.RUN_TEST_CLASSPATH)
                                .addModulePathProperties(ProjectProperties.RUN_TEST_MODULEPATH)
                                .setModuleInfoBasedPath(ClassPath.getClassPath(roots[0], ClassPath.COMPILE))
                                .setSourcePath(scp)
                                .addLibrariesNodeActions(libsSupport.getActions(true).stream()
                                    .map((a) -> {
                                        return a instanceof ContextAwareAction ?
                                                ((ContextAwareAction)a).createContextAwareInstance(lkp) :
                                                a;
                                    })
                                    .toArray((len) -> new Action[len]))
                                .build()
                        };
                    } else {
                        return new Node[] {
                            new LibrariesNode.Builder(this.project,
                                    libsSupport.getPropertyEvaluator(),
                                    libsSupport.getUpdateHelper(),
                                    libsSupport.getReferenceHelper(),
                                    libsSupport.getClassPathSupport())
                                .addClassPathProperties(ProjectProperties.RUN_CLASSPATH)
                                .addClassPathIgnoreRefs(ProjectProperties.BUILD_MODULES_DIR)
                                .addModulePathProperties(ProjectProperties.RUN_MODULEPATH)
                                .addModulePathIgnoreRefs(ProjectProperties.BUILD_MODULES_DIR)
                                .setBootPath(ClassPath.getClassPath(roots[0], ClassPath.BOOT))
                                .setModuleInfoBasedPath(ClassPath.getClassPath(roots[0], ClassPath.COMPILE))
                                .setPlatformProperty(ProjectProperties.PLATFORM_ACTIVE)
                                .setSourcePath(scp)
                                .setModuleSourcePath(ClassPath.getClassPath(roots[0], JavaClassPathConstants.MODULE_SOURCE_PATH))
                                .addLibrariesNodeActions(libsSupport.getActions(false).stream()
                                    .map((a) -> {
                                        return a instanceof ContextAwareAction ?
                                                ((ContextAwareAction)a).createContextAwareInstance(lkp) :
                                                a;
                                    })
                                    .toArray((len) -> new Action[len]))
                                .build()
                        };
                    }
                } else {
                    return new Node[0];
                }
            } else {
                return new Node[0];
            }
        }

        @NonNull
        private Collection<? extends Key> createKeys() {
            final ClassPath sourceP = srcPath.get();
            final ClassPath testP = testPath.get();
            if (sourceP == null || testP == null) {
                return Collections.emptyList();
            }
            final java.util.Map<FileObject,SourceGroup> grpsByRoot = new HashMap<>();
            for (SourceGroup g : sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                grpsByRoot.put(g.getRootFolder(), g);
            }
            final Comparator<FileObject> foc = (a,b) -> a.getNameExt().compareTo(b.getNameExt());
            return Stream.concat(Stream.concat(
                    Arrays.stream(sourceP.getRoots())
                        .sorted(foc)
                        .map((fo) -> Pair.of(fo,false)),
                    Arrays.stream(testPath.get().getRoots())
                        .sorted(foc)
                        .map((fo) -> Pair.of(fo,true)))
                    .map((p) -> {
                        final SourceGroup g = grpsByRoot.get(p.first());
                        return g == null ?
                                null :
                                new Key(g,p.second());
                     })
                    .filter((p) -> p != null),
                    Stream.of(
                        new Key(sourceP, false),
                        new Key(testP, true)
                    ))
                    .collect(Collectors.toList());
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
                refresh.schedule(100);
            }
        }

        private final static class Key {
            private final boolean sources;
            private final boolean tests;
            private final SourceGroup sg;
            private final ClassPath sourcePath;
            private final FileObject[] sourceRoots;

            Key(
                    @NonNull final SourceGroup sg,
                    final boolean tests) {
                assert sg != null;
                this.sources = true;
                this.sg = sg;
                this.sourcePath = null;
                this.sourceRoots = new FileObject[0];
                this.tests = tests;
            }

            Key(
                    @NonNull final ClassPath sourcePath,
                    final boolean tests) {
                assert sourcePath != null;
                this.sources = false;
                this.sg = null;
                this.sourcePath = sourcePath;
                this.sourceRoots = this.sourcePath.getRoots();
                this.tests = tests;
            }

            boolean isSource() {
                return sources;
            }

            boolean isTests() {
                return tests;
            }

            @NonNull
            SourceGroup getSourceGroup() {
                if (!sources) {
                    throw new IllegalStateException("Not a source key.");   //NOI18N
                }
                return sg;
            }

            @NonNull
            private ClassPath getSourcePath() {
                if (sources) {
                    throw new IllegalStateException("Not a dependency key.");   //NOI18N
                }
                return sourcePath;
            }

            @NonNull
            private FileObject[] getSourceRoots() {
                if (sources) {
                    throw new IllegalStateException("Not a dependency key.");   //NOI18N
                }
                return sourceRoots;
            }

            @Override
            public int hashCode() {
                int res = 17;
                res = res * 31 + (sources ? 0 : 1);
                res = res * 31 + (tests ? 0 : 1);
                res = res * 31 + Optional.ofNullable(sg).map(SourceGroup::getRootFolder).map(Object::hashCode).orElse(0);
                res = res * 31 + (sourceRoots.length == 0 ? 0 : 1);
                return res;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (!(obj instanceof Key)) {
                    return false;
                }
                final Key other = (Key) obj;
                return  (sources == other.sources) &&
                        (tests == other.tests) &&
                        sgEq(sg, other.sg) &&
                        (sourceRoots.length == 0 ? other.sourceRoots.length == 0 : other.sourceRoots.length != 0);
            }

            private static boolean sgEq(
                    @NullAllowed final SourceGroup a,
                    @NullAllowed final SourceGroup b) {
                if (a == null) {
                    return b == null;
                } else if (b == null) {
                    return false;
                }
                final FileObject afo = a.getRootFolder();
                final FileObject bfo = b.getRootFolder();
                return afo == null ?
                        bfo == null :
                        afo.equals(bfo);
            }
        }
    }

    private static class LibrariesSupport {
        private final UpdateHelper helper;
        private final PropertyEvaluator evaluator;
        private final ReferenceHelper refHelper;
        private final ClassPathSupport cs;
        private final List<Action> actions;
        private final List<Action> testActions;

        LibrariesSupport(
                @NonNull final UpdateHelper helper,
                @NonNull final PropertyEvaluator evaluator,
                @NonNull final ReferenceHelper refHelper) {
            Parameters.notNull("helper", helper);   //NOI18N
            Parameters.notNull("evaluator", evaluator); //NOI18N
            Parameters.notNull("refHelper", refHelper); //NOI18N
            this.helper = helper;
            this.evaluator = evaluator;
            this.refHelper = refHelper;
            this.cs = new ClassPathSupport(evaluator, refHelper, helper.getAntProjectHelper(), helper, null);
            this.actions = new ArrayList<>();
            this.testActions = new ArrayList<>();
        }

        @NonNull
        UpdateHelper getUpdateHelper() {
            return helper;
        }

        @NonNull
        PropertyEvaluator getPropertyEvaluator() {
            return evaluator;
        }

        @NonNull
        ReferenceHelper getReferenceHelper() {
            return refHelper;
        }

        @NonNull
        ClassPathSupport getClassPathSupport() {
            return cs;
        }

        @NonNull
        Collection<? extends Action>getActions(final boolean tests) {
            return tests ? testActions : actions;
        }

        void addActions(
                final boolean tests,
                @NonNull final Action... actions) {
            Collections.addAll(tests ? this.testActions : this.actions, actions);
        }
    }

    private static class SimpleLabelNode extends FilterNode {
        public SimpleLabelNode(Node original, String dispName) {
            super(original);
            if (dispName != null) {
                disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME);
                setDisplayName(dispName);
            }
        }
    }
    
    private static final class TestRootNode extends SimpleLabelNode {
        @StaticResource
        private static final String TEST_BADGE = "org/netbeans/modules/java/api/common/project/ui/resources/test-badge.png";

        TestRootNode(@NonNull final Node original, String dispName) {
            super(original, dispName);
        }

        @Override
        public Image getIcon(int type) {
            return computeIcon(false, type);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return computeIcon(true, type);
        }

        private Image computeIcon(boolean opened, int type) {
            Image image = opened ?
                    getDataFolderNodeDelegate().getOpenedIcon(type) :
                    getDataFolderNodeDelegate().getIcon(type);
            image = ImageUtilities.mergeImages(
                    image,
                    ImageUtilities.loadImage(TEST_BADGE),
                    4, 5);
            return image;
        }

        @NonNull
        private Node getDataFolderNodeDelegate() {
            final DataFolder df = getLookup().lookup(DataFolder.class);
            try {
                if (df.isValid()) {
                    return df.getNodeDelegate();
                }
            } catch (IllegalStateException e) {
                if (df.isValid()) {
                    throw e;
                }
            }
            return new AbstractNode(Children.LEAF);
        }
    }
}
