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

package org.netbeans.modules.java.api.common.project.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.impl.DefaultProjectModulesModifier;
import org.netbeans.modules.java.api.common.util.CommonModuleUtils;
import org.netbeans.modules.java.api.common.project.ui.customizer.AntArtifactItem;
import org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.ui.CustomizerUtilities;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.modules.SpecificationVersion;
import org.openide.util.BaseUtilities;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * LibrariesNode displays the content of classpath and optionaly Java platform.
 * @author Tomas Zezula
 * @since org.netbeans.modules.java.api.common/1 1.5
*/
public final class LibrariesNode extends AbstractNode {

    private static final Image ICON_BADGE = ImageUtilities.loadImage("org/netbeans/modules/java/api/common/project/ui/resources/libraries-badge.png");    //NOI18N
    public static final RequestProcessor rp = new RequestProcessor ();
    private static Icon folderIconCache;
    private static Icon openedFolderIconCache;

    private final String displayName;
    private final Action[] librariesNodeActions;


    /**
     * Creates new LibrariesNode named displayName displaying classPathProperty classpath
     * and optionaly Java platform.
     * @param displayName the display name of the node
     * @param eval {@link PropertyEvaluator} used for listening
     * @param helper {@link UpdateHelper} used for reading and updating project's metadata
     * @param refHelper {@link ReferenceHelper} used for destroying unused references
     * @param classPathProperty the ant property name of classpath which should be visualized
     * @param classPathIgnoreRef the array of ant property names which should not be displayed, may be
     * an empty array but not null
     * @param platformProperty the ant name property holding the Web platform system name or null
     * if the platform should not be displayed
     * @param librariesNodeActions actions which should be available on the created node.
     */
    public LibrariesNode (String displayName, Project project, PropertyEvaluator eval, UpdateHelper helper, ReferenceHelper refHelper,
                   String classPathProperty, String[] classPathIgnoreRef, String platformProperty,
                   Action[] librariesNodeActions, String webModuleElementName, ClassPathSupport cs,
                   Callback extraKeys) {
        this(
            displayName,
            project,
            eval,
            helper,
            refHelper,
            Collections.singletonList(classPathProperty),
            Arrays.asList(classPathIgnoreRef),
            platformProperty == null ?
                null :
                Pair.<Pair<String,String>,ClassPath>of(Pair.<String,String>of(platformProperty, null),null),
            null,
            Collections.emptySet(),
            librariesNodeActions,
            webModuleElementName,
            cs,
            extraKeys,
            null,
            null);
    }

    private LibrariesNode(
            @NonNull final String displayName,
            @NonNull final Project project,
            @NonNull final PropertyEvaluator eval,
            @NonNull final UpdateHelper helper,
            @NonNull final ReferenceHelper refHelper,
            @NonNull final List<String> classPathProperties,
            @NonNull final Collection<String> classPathIgnoreRef,
            @NullAllowed final Pair<Pair<String,String>, ClassPath> boot,
            @NullAllowed final Pair<Set<String>,ClassPath> modulePath,
            @NonNull final Collection<String> modulePathIgnoreRef,
            @NullAllowed final Action[] librariesNodeActions,
            @NullAllowed final String webModuleElementName,
            @NonNull final ClassPathSupport cs,
            @NullAllowed final Callback extraKeys,
            @NullAllowed final ClassPath sourcePath,
            @NullAllowed final ClassPath moduleSourcePath) {
        super (new LibrariesChildren (project, eval, helper, refHelper, classPathProperties,
                    classPathIgnoreRef, boot, modulePath, modulePathIgnoreRef,
                    webModuleElementName, cs, extraKeys, sourcePath, moduleSourcePath),
                Lookups.fixed(project, new PathFinder()));
        this.displayName = displayName;
        this.librariesNodeActions = librariesNodeActions;
    }

    /**
     * Builder for {@link LibrariesNode}.
     * @since 1.63
     */
    public static final class Builder {

        private final Project project;
        private final PropertyEvaluator eval;
        private final UpdateHelper helper;
        private final ReferenceHelper refHelper;
        private final ClassPathSupport cs;
        private final Set<String> classPathIgnoreRef = new HashSet<String>();
        private final Set<String> modulePathIgnoreRef = new HashSet<>();
        private final List<Action> librariesNodeActions = new ArrayList<Action>();
        private final List<String> classPathProperties = new ArrayList<String>();
        private String name = NbBundle.getMessage(LibrariesNode.class, "TXT_LibrariesNode");
        private Pair<Pair<String,String>,ClassPath> boot = Pair.<Pair<String,String>,ClassPath>of(Pair.<String,String>of(null,null),null);
        private Pair<Set<String>,ClassPath> modulePath;
        private String webModuleElementName;
        private NodeList<Key> extraNodes;
        private ClassPath sourcePath;
        private ClassPath moduleSourcePath;

        public Builder(
            @NonNull final Project project,
            @NonNull final PropertyEvaluator eval,
            @NonNull final UpdateHelper helper,
            @NonNull final ReferenceHelper refHelper,
            @NonNull final ClassPathSupport cs) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("helper", helper);   //NOI18N
            Parameters.notNull("refHelper", refHelper); //NOI18N
            Parameters.notNull("cs", cs);
            this.project = project;
            this.eval = eval;
            this.helper = helper;
            this.refHelper = refHelper;
            this.cs = cs;
        }

        /**
         * Sets node name.
         * @param name the node name
         * @return the {@link Builder}
         */
        @NonNull
        public Builder setName(@NonNull final String name) {
            Parameters.notNull("name", name);   //NOI18N
            this.name = name;
            return this;
        }

        /**
         * Adds references to ignore from libraries.
         * @param refs the references to ignore
         * @return the {@link Builder}
         */
        @NonNull
        public Builder addClassPathIgnoreRefs(@NonNull final String... refs) {
            Parameters.notNull("refs", refs);   //NOI18N
            Collections.addAll(classPathIgnoreRef, refs);
            return this;
        }

        /**
         * Adds classpaths to display.
         * @param propNames the names of properties holding the paths to be displayed
         * @return the {@link Builder}
         */
        @NonNull
        public Builder addClassPathProperties(@NonNull final String... propNames) {
            Parameters.notNull("propNames", propNames);    //NOI18N
            Collections.addAll(classPathProperties, propNames);
            return this;
        }

        /**
         * Sets platform type.
         * @param platformType the type of platform, by default "j2se"
         * @return the {@link Builder}
         */
        @NonNull
        public Builder setPlatformType(@NonNull final String platformType) {
            Parameters.notNull("platformType", platformType);   //NOI18N
            this.boot = Pair.<Pair<String,String>,ClassPath>of(
                Pair.<String,String>of(
                    boot.first().first(),
                    platformType),
                boot.second());
            return this;
        }

        /**
         * Sets platform property.
         * @param platformProperty the property holding the reference on {@link JavaPlatform}
         * @return the {@link Builder}
         */
        @NonNull
        public Builder setPlatformProperty(@NonNull final String platformProperty) {
            Parameters.notNull("platformProperty", platformProperty);   //NOI18N            
            this.boot = Pair.<Pair<String,String>,ClassPath>of(
                Pair.<String,String>of(
                    platformProperty,
                    boot.first().second()),
                boot.second());
            return this;
        }

        /**
         * Sets bootstrap libraries to display under the platform node.
         * In case when project's bootstrap libraries differ from {@link JavaPlatform}'s bootstrap libraries
         * this method can be used to override the shown {@link JavaPlatform}'s libraries.
         * @param bootPath the libraries to show
         * @return the {@link Builder}
         * @since 1.68
         */
        @NonNull
        public Builder setBootPath(@NonNull final ClassPath bootPath) {
            Parameters.notNull("bootPath", bootPath);   //NOI18N            
            this.boot = Pair.<Pair<String,String>,ClassPath>of(boot.first(),bootPath);
            return this;
        }

        /**
         * Adds actions to libraries node.
         * @param actions the actions to be added.
         * @return the {@link Builder}
         */
        @NonNull
        public Builder addLibrariesNodeActions(@NonNull final Action... actions) {
            Parameters.notNull("actions", actions); //NOI18N
            Collections.addAll(librariesNodeActions, actions);
            return this;
        }

        /**
         * Sets web module element.
         * @param elementName the web module element name
         * @return the {@link Builder}
         */
        @NonNull
        public Builder setWebModuleElementName(@NonNull final String elementName) {
            Parameters.notNull("elementName", elementName);
            this.webModuleElementName = elementName;
            return this;
        }

        /**
         * Sets a factory to create additional nodes.
         * @param extraNodes the {@link NodeList} to create additional nodes
         * @return the {@link Builder}
         */
        @NonNull
        public Builder setExtraNodes(@NonNull final NodeList<Key> extraNodes) {
            Parameters.notNull("extraNodes", extraNodes);
            this.extraNodes = extraNodes;
            return this;
        }

        /**
         * Sets module-info base module path.
         * @param moduleInfoBasedClassPath the effective module info based path used for
         * filtering the module path entries.
         * @return the {@link Builder}
         * @since 1.87
         */
        @NonNull
        public Builder setModuleInfoBasedPath(
                @NonNull final ClassPath moduleInfoBasedClassPath) {
            Parameters.notNull("moduleInfoBasedClassPath", moduleInfoBasedClassPath);   //NOI18N
            this.modulePath = Pair.<Set<String>,ClassPath>of(
                    this.modulePath == null ?
                            new HashSet<>():
                            modulePath.first(),
                    moduleInfoBasedClassPath);
            return this;
        }

        /**
         * Adds module path properties.
         * @param props the properties to be added
         * @return the {@link Builder}
         * @since 1.87
         */
        @NonNull
        public Builder addModulePathProperties(@NonNull final String... props) {
            Parameters.notNull("props", props); //NOI18N
            if (this.modulePath == null) {
                this.modulePath = Pair.<Set<String>,ClassPath>of(
                        new HashSet<>(),
                        null);
            }
            Collections.addAll(this.modulePath.first(), props);
            return this;
        }
        
        /**
         * Adds references to ignore from the module path.
         * @param refs the references to ignore
         * @return the {@link Builder}
         * @since 1.88
         */
        @NonNull
        public Builder addModulePathIgnoreRefs(@NonNull final String... refs) {
            Parameters.notNull("refs", refs);   //NOI18N
            Collections.addAll(modulePathIgnoreRef, refs);
            return this;
        }
        
        /**
         * Sets the main source path for the {@link LibrariesNode}.
         * The given source path is used as a hint for module-info lookup.
         * @param sourcePath the source {@link ClassPath}
         * @return the {@link Builder}
         * @since 1.114
         */
        @NonNull
        public Builder setSourcePath(@NonNull final ClassPath sourcePath) {
            Parameters.notNull("sourcePath", sourcePath); //NOI18N
            this.sourcePath = sourcePath;
            return this;
        }

        /**
         * Sets the module source path for multi module project.
         * The module source path is used to lookup project own modules.
         * @param moduleSourcePath  the project module source path
         * @return the {@link Builder}
         * @since 1.114
         */
        @NonNull
        public Builder setModuleSourcePath(@NonNull final ClassPath moduleSourcePath) {
            Parameters.notNull("moduleSourcePath", moduleSourcePath);
            this.moduleSourcePath = moduleSourcePath;
            return this;
        }

        /**
         * Creates configured {@link LibrariesNode}.
         * @return the {@link LibrariesNode}.
         */
        @NonNull
        public LibrariesNode build() {
            Pair<Pair<String,String>,ClassPath> _boot = boot;
            if (_boot.first().first() == null) {
                if (_boot.second() != null || _boot.first().second() != null) {
                    throw new IllegalStateException("PlatformType or bootPath given but no platformProperty");  //NOI18N
                } else {
                    _boot = null;
                }
            }
            return new LibrariesNode(
                name,
                project,
                eval,
                helper,
                refHelper,
                classPathProperties,
                classPathIgnoreRef,
                _boot,
                modulePath,
                modulePathIgnoreRef,
                librariesNodeActions.toArray(new Action[0]),
                webModuleElementName,
                cs,
                extraNodes != null ? new CallBackImpl(extraNodes) : null,
                sourcePath,
                moduleSourcePath);
        }

        private static final class CallBackImpl implements Callback {

            private final NodeList<Key> delegate;

            CallBackImpl(@NonNull final NodeList<Key> nodeList) {
                Parameters.notNull("nodeList", nodeList);   //NOI18N
                this.delegate = nodeList;
            }

            @Override
            @NonNull
            public List<Key> getExtraKeys() {
                return delegate.keys();
            }

            @Override
            @NonNull
            public Node[] createNodes(Key key) {
                final Node node = delegate.node(key);
                return node != null ?
                    new Node[] {node} :
                    new Node[0];
            }

        }
    }

    @Override
    public String getDisplayName () {
        return this.displayName; 
    }

    @Override
    public String getName () {
        return this.getDisplayName();
    }    

    @Override
    public Image getIcon( int type ) {        
        return computeIcon( false, type );
    }
        
    @Override
    public Image getOpenedIcon( int type ) {
        return computeIcon( true, type );
    }

    @Override
    public Action[] getActions(boolean context) {        
        return this.librariesNodeActions;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    //Static Action Factory Methods
    public static Action createAddProjectAction (Project p, SourceRoots sources) {
        return new AddProjectAction(p, sources::getRoots);
    }

    public static Action createAddLibraryAction (ReferenceHelper helper,
            SourceRoots sources, LibraryChooser.Filter filter) {
        return new AddLibraryAction(
                helper,
                sources::getRoots,
                filter != null ? filter : EditMediator.createLibraryFilter());
    }

    public static Action createAddFolderAction (AntProjectHelper p, SourceRoots sources) {
        return new AddFolderAction(p, sources::getRoots);
    }
    
    /**
     * Returns Icon of folder on active platform
     * @param opened should the icon represent opened folder
     * @return the folder icon
     */
    static synchronized Icon getFolderIcon (boolean opened) {
        if (openedFolderIconCache == null) {
            Node n = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
            openedFolderIconCache = new ImageIcon(n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
            folderIconCache = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
        }
        if (opened) {
            return openedFolderIconCache;
        }
        else {
            return folderIconCache;
        }
    }

    private Image computeIcon( boolean opened, int type ) {        
        Icon icon = getFolderIcon(opened);
        Image image = ((ImageIcon)icon).getImage();
        image = ImageUtilities.mergeImages(image, ICON_BADGE, 7, 7 );
        return image;        
    }

    private static void handlePCPMUnsupported (
            @NonNull final Supplier<FileObject[]> sourceRoots,
            @NonNull final UnsupportedOperationException e) {
        final FileObject[] roots = sourceRoots.get();
        if (roots.length == 0) {
            return;
        }
        final StringBuilder sb = new StringBuilder().
                append("roots: ").
                append(Arrays.toString(roots)).
                append("\n");
        Exceptions.printStackTrace(
            Exceptions.attachMessage(e, sb.toString()));
    }

    //Static inner classes
    private static class LibrariesChildren extends Children.Keys<Key> implements PropertyChangeListener, ChangeListener {

        
        /**
         * Constant represneting a prefix of library reference generated by {@link org.netbeans.modules.java.j2seplatform.libraries.J2SELibraryTypeProvider}
         */
        private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
        
        /**
         * Constant representing a prefix of artifact reference generated by {@link ReferenceHelper}
         */
        private static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N
        /**
         * Constant representing a prefix of file reference generated by {@link ReferenceHelper}
         */
        private static final String FILE_REF_PREFIX = "${file.reference."; //NOI18N
        /**
         * Constant representing a prefix of ant property reference
         */
        private static final String REF_PREFIX = "${"; //NOI18N
        @StaticResource
        private static final String LIBRARIES_ICON = "org/netbeans/modules/java/api/common/project/ui/resources/libraries.gif"; //NOI18N
        @StaticResource
        private static final String ARCHIVE_ICON = "org/netbeans/modules/java/api/common/project/ui/resources/jar.gif";//NOI18N

        private final PropertyEvaluator eval;
        private final UpdateHelper helper;
        private final ReferenceHelper refHelper;
        private final Set<String> classPathProperties;
        private final Pair<Pair<String,String>, ClassPath> boot;
        private final Pair<Set<String>,ClassPath> modulePath;
        private final Set<String> classPathIgnoreRef;
        private final Set<String> modulePathIgnoreRef;
        private final String webModuleElementName;
        private final ClassPathSupport cs;
        private final SourceLevelQuery.Result slResult;
        private final ClassPath sourcePath;
        private final ClassPath moduleSourcePath;
        
        private Callback extraKeys;
        private Project project;
        
        //XXX: Workaround: classpath is used only to listen on non existent files.
        // This should be removed when there will be API for it
        // See issue: http://www.netbeans.org/issues/show_bug.cgi?id=33162
        private RootsListener fsListener;
        private final AtomicReference<CompilerOptionsQuery.Result> coResult;
        private final AtomicReference<PropertyChangeListener[]> sourceRootsListener;


        LibrariesChildren (
                @NonNull final Project project,
                @NonNull final PropertyEvaluator eval,
                @NonNull final UpdateHelper helper,
                @NonNull final ReferenceHelper refHelper,
                @NonNull final List<String> classPathProperties,
                @NonNull final Collection<String> classPathIgnoreRef,
                @NullAllowed final Pair<Pair<String,String>, ClassPath> boot,
                @NullAllowed final Pair<Set<String>,ClassPath> modulePath,
                @NonNull final Collection<String> modulePathIgnoreRef,
                @NullAllowed final String webModuleElementName,
                @NonNull final ClassPathSupport cs,
                @NullAllowed final Callback extraKeys,
                @NullAllowed final ClassPath sourcePath,
                @NullAllowed final ClassPath moduleSourcePath) {
            this.eval = eval;
            this.helper = helper;
            this.refHelper = refHelper;
            this.classPathProperties = new LinkedHashSet<>(classPathProperties);
            this.classPathIgnoreRef = new HashSet<String>(classPathIgnoreRef);
            this.boot = boot;
            this.modulePath = modulePath != null ?
                    modulePath :
                    Pair.<Set<String>,ClassPath>of(Collections.emptySet(), null);
            this.modulePathIgnoreRef = new HashSet<>(modulePathIgnoreRef);
            this.webModuleElementName = webModuleElementName;
            this.cs = cs;
            this.extraKeys = extraKeys;
            this.project = project;
            this.slResult = SourceLevelQuery.getSourceLevel2(
                    this.helper.getAntProjectHelper().getProjectDirectory());
            this.sourcePath = sourcePath;
            this.moduleSourcePath = moduleSourcePath;
            this.coResult = new AtomicReference<>();
            this.sourceRootsListener = new AtomicReference<>();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            final boolean propRoots = RootsListener.PROP_ROOTS.equals(propName);
            if (classPathProperties.contains(propName)
                    || modulePath.first().contains(propName)
                    || propRoots
                    || LibraryManager.PROP_LIBRARIES.equals(propName)
                    || ClassPath.PROP_ENTRIES.equals(propName)) {
                reset(propRoots);
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            reset(false);
        }

        @Override
        protected void addNotify() {
            this.eval.addPropertyChangeListener (WeakListeners.propertyChange(this, this.eval));
            this.slResult.addChangeListener(WeakListeners.change(this, this.slResult));
            LibraryManager lm = refHelper.getProjectLibraryManager();
            if (lm == null) {
                lm = LibraryManager.getDefault();
            }
            lm.addPropertyChangeListener(WeakListeners.propertyChange(this, lm));
            if (modulePath.second() != null) {
                modulePath.second().addPropertyChangeListener(WeakListeners.propertyChange(this, modulePath.second()));
            }
            listenOnCompilerOptions();
            if (sourcePath != null) {
                sourcePath.addPropertyChangeListener(WeakListeners.propertyChange(this, sourcePath));
            }
            this.setKeys(getKeys ());
        }

        @Override
        protected void removeNotify() {
            this.eval.removePropertyChangeListener(this);
            if (refHelper.getProjectLibraryManager() != null) {
                refHelper.getProjectLibraryManager().removePropertyChangeListener(this);
            } else {
                LibraryManager.getDefault().removePropertyChangeListener(this);
            }
            synchronized (this) {
                if (fsListener!=null) {
                    fsListener.removePropertyChangeListener (this);
                    fsListener = null;
                }
            }
            this.setKeys(Collections.<Key>emptySet());
        }

        @Override
        protected Node[] createNodes(Key key) {
            Node[] result = null;
            switch (key.getType()) {
                case Key.TYPE_PLATFORM:
                    result = new Node[] {PlatformNode.create(project, eval, boot, cs)};
                    break;
                case Key.TYPE_PROJECT:
                    result = new Node[] {new ProjectNode(key.getProject(), key.getArtifactLocation(), helper, key.getClassPathId(),
                        key.getEntryId(), webModuleElementName, cs, refHelper, key.getPreRemoveAction(), key.getPostRemoveAction(), !key.shared)};
                    break;
                case Key.TYPE_LIBRARY:
                {
                    final Node afn = ActionFilterNode.forLibrary(
                        PackageView.createPackageView(key.getSourceGroup()),
                        helper,
                        key.getClassPathId(),
                        key.getEntryId(),
                        webModuleElementName,
                        cs,
                        refHelper,
                        key.getPreRemoveAction(),
                        key.getPostRemoveAction(),
                        !key.shared);
                    result = afn == null ? new Node[0] : new Node[] {afn};
                    break;
                }
                case Key.TYPE_FILE_REFERENCE:
                {
                    final Node afn = ActionFilterNode.forArchive(
                        PackageView.createPackageView(key.getSourceGroup()),
                        helper,
                        eval,
                        key.getClassPathId(),
                        key.getEntryId(),
                        webModuleElementName,
                        cs,
                        refHelper,
                        key.getPreRemoveAction(),
                        key.getPostRemoveAction(),
                        !key.shared);
                    result = afn == null ? new Node[0] : new Node[] {afn};
                    break;
                }
                case Key.TYPE_FILE:
                {
                    final Node afn = ActionFilterNode.forRoot(
                        PackageView.createPackageView(key.getSourceGroup()),
                        helper,
                        key.getClassPathId(),
                        key.getEntryId(),
                        webModuleElementName,
                        cs,
                        refHelper,
                        key.getPreRemoveAction(),
                        key.getPostRemoveAction(),
                        !key.shared);
                    result = afn == null ? new Node[0] : new Node[] {afn};
                    break;
                }
                case Key.TYPE_MODULE:
                {
                    result = new Node[] {
                        new ModuleNode(
                                key.getEntryId(),
                                key.getArtifactLocation(),
                                key.getPreRemoveAction(),
                                key.getPostRemoveAction())
                    };
                    break;
                }
                case Key.TYPE_OTHER:
                    result = extraKeys.createNodes(key);
                    break;
            }
            if (result == null) {
                assert false : "Unknown key type";  //NOI18N
                result = new Node[0];
            }
            return result;
        }
        
        private List<Key> getKeys () {
            EditableProperties projectSharedProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            EditableProperties projectPrivateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            EditableProperties privateProps = PropertyUtils.getGlobalProperties();
            List<URL> rootsList = new ArrayList<>();
            List<Key> result = new ArrayList<>();
            final String sl = slResult.getSourceLevel();
            final boolean supportsModules = sl != null && CommonModuleUtils.JDK9.compareTo(new SpecificationVersion(sl)) <= 0;
            if (supportsModules && moduleSourcePath != null && sourcePath != null) {
                final Collection<URL> srcRoots = sourcePath.entries().stream()
                        .map((e) -> e.getURL())
                        .collect(Collectors.toList());
                final Collection<Pair<String,URL>> modRoots = Arrays.stream(moduleSourcePath.getRoots())
                        .flatMap((fo) -> Arrays.stream(fo.getChildren()))
                        .map((fo) -> {
                            if (fo.isFolder() && !fo.getName().startsWith(".")) {   //NOI18N
                                return Pair.of(fo.getNameExt(),fo.toURL());
                            } else {
                                return null;
                            }
                        })
                        .filter((p) -> p != null)
                        .sorted((a,b) -> a.first().compareTo(b.first()))
                        .collect(Collectors.toList());
                final Set<String> relPaths = new HashSet<>();
                for (Pair<String,URL> modRoot : modRoots) {
                    for (URL srcRoot : srcRoots) {
                        final String sef = srcRoot.toExternalForm();
                        final String mef = modRoot.second().toExternalForm();
                        if (sef.startsWith(mef)) {
                            relPaths.add(sef.substring(mef.length()));
                        }
                    }
                }
                if (!relPaths.isEmpty()) {
                    modRoots.stream()
                            .collect(Collectors.groupingBy((p) -> p.first()))
                            .entrySet().stream()
                            .map((e) -> Pair.of(
                                    e.getKey(),
                                    e.getValue().stream()
                                        .map(Pair::second)
                                        .collect(Collectors.toList())))
                            .map((p) -> {
                                try {
                                    final URL srcRoot = new URL(p.second().get(0).toExternalForm() + relPaths.iterator().next());
                                    final URL bin = Arrays.stream(BinaryForSourceQuery.findBinaryRoots(srcRoot).getRoots())
                                            .filter(FileUtil::isArchiveArtifact)
                                            .findFirst()
                                            .orElse(null);
                                    if (bin != null) {
                                        final String modName = p.first();
                                        return Key.module(
                                                modName,
                                                bin.toURI(),
                                                null,
                                                (atfc) -> {
                                                    final FileObject modInfo = findModuleInfo(sourcePath.getRoots());
                                                    if (modInfo != null) {
                                                        DefaultProjectModulesModifier.removeRequiredModules(
                                                                modInfo,
                                                                Collections.singleton(modName));
                                                    }
                                                });
                                    }
                                } catch (MalformedURLException | URISyntaxException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                                return null;
                            })
                            .filter((k) -> k != null)
                            .forEach(result::add);
                }
            }
            for (String classPathProperty : classPathProperties) {
                result.addAll(getKeys(
                        projectSharedProps,
                        projectPrivateProps,
                        privateProps,
                        classPathProperty,
                        classPathIgnoreRef,
                        CPMapper.INSTANCE,
                        null,
                        null,
                        rootsList));
            }
            if (supportsModules) {
                final RemoveFromModuleInfo rfmi = new RemoveFromModuleInfo(
                        helper,
                        eval,
                        sourcePath);
                for (String modulePathProperty : modulePath.first()) {
                    result.addAll(getKeys(
                            projectSharedProps,
                            projectPrivateProps,
                            privateProps,
                            modulePathProperty,
                            modulePathIgnoreRef,
                            MPMapper.INSTANCE,
                            rfmi,
                            null,
                            rootsList));
                }
                final CompilerOptionsQuery.Result cor = coResult.get();
                if (cor != null) {
                    final java.util.Map<String,List<URL>> patches = CommonModuleUtils.getPatches(cor);
                    if (!patches.isEmpty()) {
                        final java.util.Map<Key,List<Key>> patchesByKey = new IdentityHashMap<>();
                        for (Key key : result) {
                            try {
                                final URI uri = key.toURI();
                                if (uri != null) {
                                    final String moduleName = SourceUtils.getModuleName(uri.toURL());
                                    final List<URL> modulePatches = patches.get(moduleName);
                                    if (modulePatches != null) {
                                        patchesByKey.put(key, modulePatches.stream()
                                            .map((url) -> {
                                                final File f = FileUtil.archiveOrDirForURL(url);
                                                if (f != null) {
                                                    final Collection<SourceGroup> sgs = CPMapper.INSTANCE.apply(Pair.of(
                                                            f,
                                                            new ArrayList<>()));
                                                    return sgs.isEmpty() ?
                                                            null :
                                                            Key.file(sgs.iterator().next(), "", "", null, null, true);    //NOI18N
                                                }
                                                return null;
                                            })
                                            .filter((k) -> k != null)
                                            .collect(Collectors.toList()));
                                    }
                                }
                            } catch (MalformedURLException e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                        final List<Key> patchedResult = new ArrayList<>();
                        for (Key k : result) {
                            final List<Key> p = patchesByKey.get(k);
                            if (p != null) {
                                patchedResult.addAll(p);
                            }
                            patchedResult.add(k);
                        }
                        result = patchedResult;
                    }
                }
                if (modulePath.second() != null) {
                    final Set<URI> filter = modulePath.second().entries().stream()
                            .map((e) -> {
                                try {
                                    return e.getURL().toURI();
                                }catch (URISyntaxException exc) {
                                    Exceptions.printStackTrace(exc);
                                    return null;
                                }
                            })
                            .filter((uri) -> uri != null)
                            .collect(Collectors.toSet());
                    for (Iterator<Key> it = result.iterator(); it.hasNext();) {
                        final Key key = it.next();
                        if (!filter.contains(key.toURI())) {
                            it.remove();
                        }
                    }
                }
            }
            if (boot != null) {
                result.add (Key.platform());
            }
            final RootsListener rootsListener = new RootsListener(rootsList);
            rootsListener.addPropertyChangeListener(this);
            synchronized (this) {
                fsListener = rootsListener;
            }
            if (extraKeys != null) {
                result.addAll(extraKeys.getExtraKeys());
            }
            return result;
        }
        
        private Set<URL> computeOtherRootLibraries() {
            Sources s = ProjectUtils.getSources(project);
            if (s == null || sourcePath == null) {
                return Collections.emptySet();
            }
            Set<URL> refs = new HashSet<>();
            for (SourceGroup sg : s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                // ignore our own SG:
                ClassPath sPath = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.SOURCE);
                if (Arrays.equals(this.sourcePath.getRoots(), sPath.getRoots())) {
                    continue;
                }
                ClassPath cp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.COMPILE);
                for (ClassPath.Entry e : cp.entries()) {
                    URL u = e.getURL();
                    refs.add(u);
                    if ("jar".equals(u.getProtocol()) && u.getPath().contains("!")) { // NOI18N
                        String str = u.toExternalForm().substring(4);
                        int index = str.indexOf("!");
                        try {
                            refs.add(new URL(str.substring(0, index)));
                        } catch (MalformedURLException ex) {
                            // ignore, should not happen
                        }
                    }
                }
            }
            return refs;
        }

        private List<Key> getKeys (
                @NonNull final EditableProperties projectSharedProps,
                @NonNull final EditableProperties projectPrivateProps,
                @NonNull final EditableProperties privateProps,
                @NonNull final String currentClassPath,
                @NonNull final Set<String> toIgnre,
                @NonNull final Function<Pair<File,Collection<? super URL>>,Collection<SourceGroup>> fileRefMapper,
                @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
                @NullAllowed final Consumer<Pair<String,String>> postRemoveAction,
                @NonNull final List<URL> rootsList) {
            List<Key> result = new ArrayList<>();
            String raw = projectSharedProps.getProperty (currentClassPath);
            if (raw == null) {
                raw = projectPrivateProps.getProperty(currentClassPath);
            }
            if (raw == null) {
                raw = privateProps.getProperty(currentClassPath);
            }
            if (raw == null) {
                return result;
            }
            List<String> pe = new ArrayList<String>(Arrays.asList(PropertyUtils.tokenizePath( raw )));
            Set<URL> otherSourceRoots = null;
            
            while (pe.size()>0){
                String prop = pe.remove(0);
                String propName = CommonProjectUtils.getAntPropertyName (prop);
                if (toIgnre.contains(propName)) {
                    continue;
                } else if (prop.startsWith( LIBRARY_PREFIX )) {
                    //Library reference
                    String eval = prop.substring( LIBRARY_PREFIX.length(), prop.lastIndexOf('.') ); //NOI18N
                    Library lib = refHelper.findLibrary(eval);
                    if (lib != null) {
                        if (otherSourceRoots == null) {
                            otherSourceRoots = computeOtherRootLibraries();
                        }
                        Icon libIcon = ImageUtilities.loadImageIcon(LIBRARIES_ICON, false);
                        boolean shared = false;
                        for (URL rootUrl : lib.getContent("classpath")) {
                            rootsList.add (rootUrl);
                            FileObject root = URLMapper.findFileObject (rootUrl);
                            if (root != null && root.isFolder()) {
                                shared |= otherSourceRoots.contains(URLMapper.findURL(root, URLMapper.INTERNAL));
                                String displayName;
                                if ("jar".equals(rootUrl.getProtocol())) {  //NOI18N
                                    FileObject file = FileUtil.getArchiveFile (root);
                                    displayName = file.getNameExt();
                                } else {
                                    File file = FileUtil.toFile (root);
                                    if (file != null) {
                                        displayName = file.getAbsolutePath();
                                    } else {
                                        displayName = root.getNameExt();
                                    }
                                }
                                displayName = MessageFormat.format (
                                    NbBundle.getMessage (LibrariesNode.class,"TXT_LibraryPartFormat"),
                                    new Object[] {lib.getDisplayName(), displayName});
                                SourceGroup sg = new LibrariesSourceGroup (root, displayName, libIcon, libIcon);
                                result.add (Key.library(sg,currentClassPath, propName, preRemoveAction, postRemoveAction, shared));
                            }
                        }
                    }
                    //Todo: May try to resolve even broken library
                } else if (prop.startsWith(ANT_ARTIFACT_PREFIX)) {
                    //Project reference
                    Object[] ref = refHelper.findArtifactAndLocation(prop);
                    if (otherSourceRoots == null) {
                        otherSourceRoots = computeOtherRootLibraries();
                    }
                    if (ref[0] != null && ref[1] != null) {
                        AntArtifact artifact = (AntArtifact)ref[0];
                        URI uri = (URI)ref[1];
                        URL root = null;
                        try {
                            final URI absoluteURI = uri.isAbsolute() ?
                                    uri :
                                    artifact.getProject().getProjectDirectory().toURI().resolve(uri);
                            root = absoluteURI.toURL();
                        } catch (MalformedURLException ex) {
                            // ignore
                        }
                        result.add(Key.project(artifact, uri, currentClassPath, propName, preRemoveAction, postRemoveAction,
                                otherSourceRoots.contains(root)));
                    }
                } else if (prop.startsWith(FILE_REF_PREFIX)) {
                    //File reference
                    if (otherSourceRoots == null) {
                        otherSourceRoots = computeOtherRootLibraries();
                    }
                    String evaluatedRef = eval.getProperty(propName);
                    if (evaluatedRef != null) {
                        File file = helper.getAntProjectHelper().resolveFile(evaluatedRef);
                        final Collection<SourceGroup> sgs = fileRefMapper.apply(Pair.of(file,rootsList));
                        for (SourceGroup sg : sgs) {
                            result.add (Key.fileReference(sg,currentClassPath, propName, preRemoveAction, postRemoveAction, 
                                    otherSourceRoots.contains(sg.getRootFolder().toURL())));
                        }
                    }
                } else if (prop.startsWith(REF_PREFIX)) {
                    //Path reference
                    result.addAll(getKeys(
                            projectSharedProps,
                            projectPrivateProps,
                            privateProps,
                            propName,
                            toIgnre,
                            fileRefMapper,
                            preRemoveAction,
                            postRemoveAction,
                            rootsList));
                } else {
                    //file
                    if (otherSourceRoots == null) {
                        otherSourceRoots = computeOtherRootLibraries();
                    }
                    File file = helper.getAntProjectHelper().resolveFile(prop);
                    final Collection<SourceGroup> sgs = fileRefMapper.apply(Pair.of(file,rootsList));
                    for (SourceGroup sg : sgs) {
                        result.add (Key.file(sg,currentClassPath, propName, preRemoveAction, postRemoveAction, 
                                otherSourceRoots.contains(sg.getRootFolder())));
                    }
                }
            }
            return result;
        }

        private void listenOnCompilerOptions() {
            if (coResult.get() == null && sourcePath != null) {
                final FileObject[] rootFos = sourcePath.getRoots();
                if (rootFos.length > 0) {
                    final PropertyChangeListener[] ls = sourceRootsListener.getAndSet(null);
                    if (ls != null) {
                        sourcePath.removePropertyChangeListener(ls[1]);
                    }
                    final CompilerOptionsQuery.Result cor = CompilerOptionsQuery.getOptions(rootFos[0]);
                    if (coResult.compareAndSet(null, cor)) {
                        cor.addChangeListener(WeakListeners.change(this, cor));
                    }
                } else {
                    //Defer to point when source roots exist
                    PropertyChangeListener[] ls = sourceRootsListener.get();
                    if (ls == null) {
                        ls = new PropertyChangeListener[2];
                        ls[0] = (e) -> {
                            if (ClassPath.PROP_ROOTS.equals(e.getPropertyName())) {
                                listenOnCompilerOptions();
                                reset(false);
                            }
                        };
                        ls[1] = WeakListeners.propertyChange(ls[0],sourcePath);
                        if (sourceRootsListener.compareAndSet(null,ls)) {
                            sourcePath.addPropertyChangeListener(ls[1]);
                        }
                    }
                }
            }
        }

        private static SourceGroup createFileSourceGroup (File file, Collection<? super URL> rootsList) {
            Icon icon;
            Icon openedIcon;
            String displayName;
            final URL url = FileUtil.urlForArchiveOrDir(file);
            if (url == null) {
                return null;
            }
            else if ("jar".equals(url.getProtocol())) {  //NOI18N
                icon = openedIcon = ImageUtilities.loadImageIcon(ARCHIVE_ICON, false);
                displayName = file.getName();
            }
            else {                                
                icon = getFolderIcon (false);
                openedIcon = getFolderIcon (true);
                displayName = file.getAbsolutePath();
            }
            rootsList.add (url);
            FileObject root = URLMapper.findFileObject (url);
            if (root != null) {
                return new LibrariesSourceGroup (root,displayName,icon,openedIcon);
            }
            return null;
        }

        private void reset(final boolean testBroken) {
            synchronized (this) {
                if (fsListener!=null) {
                    fsListener.removePropertyChangeListener (this);
                    fsListener = null;
                }
            }
            rp.post (() -> {
                setKeys(getKeys());
                if (testBroken) {
                    final LogicalViewProvider2 lvp = project.getLookup().lookup(LogicalViewProvider2.class);
                    if (lvp != null) {
                        lvp.testBroken();
                    }
                }
            });   
        }
        
        private static final class CPMapper implements Function<Pair<File,Collection<? super URL>>,Collection<SourceGroup>> {
            static final Function<Pair<File,Collection<? super URL>>,Collection<SourceGroup>> INSTANCE = new CPMapper();
        
            private CPMapper() {}

            @Override
            @NonNull
            public Collection<SourceGroup> apply(@NonNull final Pair<File,Collection<? super URL>> param) {
                final File file = param.first();
                final Collection<? super URL> rootsList = param.second();
                final SourceGroup sg = createFileSourceGroup(file,rootsList);
                return sg != null ?
                        Collections.singleton(sg) :
                        Collections.emptySet();
            }
        }
        
        private static final class ModulesFinder implements Function<File,Collection<File>> {
            static Function<File,Collection<File>> INSTANCE = new ModulesFinder();
            
            private ModulesFinder() {}

            @NonNull
            @Override
            public Collection<File> apply(@NonNull final File file) {
                Collection<File> entries = new ArrayList<>();
                if (file.isDirectory()) {
                    if (new File(file,"module-info.class").exists()) {  //NOI18N
                        entries.add(file);
                    } else {
                        Optional.ofNullable(file.listFiles((f) -> {
                            try {
                                return FileUtil.isArchiveFile(BaseUtilities.toURI(f).toURL());
                            } catch (MalformedURLException e) {
                                Exceptions.printStackTrace(e);
                                return false;
                            }
                        }))
                            .ifPresent((fs) -> Collections.addAll(entries, fs));
                    }
                } else {
                    entries.add(file);
                }
                return entries;
            }
            
        }

        private static final class MPMapper implements Function<Pair<File,Collection<? super URL>>,Collection<SourceGroup>> {
            static final Function<Pair<File,Collection<? super URL>>,Collection<SourceGroup>> INSTANCE = new MPMapper();

            private MPMapper() {}

            @Override
            @NonNull
            public Collection<SourceGroup> apply(@NonNull final Pair<File,Collection<? super URL>> param) {
                final Collection<File> modules = ModulesFinder.INSTANCE.apply(param.first());
                return modules.stream()
                        .map((f) -> createFileSourceGroup(f, param.second()))
                        .filter((sg) -> sg != null)
                        .collect(Collectors.toList());
            }
        }
        
        private static final class RemoveFromModuleInfo implements Consumer<Pair<String,String>> {
            private final UpdateHelper helper;
            private final PropertyEvaluator eval;
            private final ClassPath sourcePath;
            
            RemoveFromModuleInfo(
                    @NonNull final UpdateHelper helper,
                    @NonNull final PropertyEvaluator eval,
                    @NullAllowed final ClassPath sourcePath) {
                this.helper = helper;
                this.eval = eval;
                this.sourcePath = sourcePath;
            }

            @Override
            public void accept(@NonNull final Pair<String, String> t) {
                final String ref = eval.evaluate(t.second());
                if (ref != null) {
                    for (FileObject[] srcRoots : findSourceRoots()) {
                        final FileObject moduleInfo = findModuleInfo(srcRoots);
                        if (moduleInfo != null) {
                            final Set<String> modules = Arrays.stream(PropertyUtils.tokenizePath(ref))
                                    .flatMap((pe) -> ModulesFinder.INSTANCE.apply(
                                            helper.getAntProjectHelper().resolveFile(pe)).stream())
                                    .map(FileUtil::urlForArchiveOrDir)
                                    .filter((url) -> url != null)
                                    .map((url) -> SourceUtils.getModuleName(url, true))
                                    .filter((name) -> name != null)
                                    .collect(Collectors.toSet());
                            DefaultProjectModulesModifier.removeRequiredModules(moduleInfo, modules);
                        }
                    }
                }
            }

            @NonNull
            private FileObject[][] findSourceRoots() {
                final List<FileObject[]> res = new ArrayList<>();
                final Predicate<FileObject> accepts;
                if (sourcePath != null) {
                    res.add(sourcePath.getRoots());
                    final Set<URL> rs = new HashSet<>();
                    sourcePath.entries().stream()
                            .map(ClassPath.Entry::getURL)
                            .forEach(rs::add);
                    accepts = (p) -> {
                        final List<URL> s4t = Arrays.asList(UnitTestForSourceQuery.findSources(p));
                        return !s4t.isEmpty() && rs.containsAll(s4t);
                    };
                } else {
                    accepts = (p) -> true;
                }
                final Project p = FileOwnerQuery.getOwner(helper.getAntProjectHelper().getProjectDirectory());
                if (p != null) {
                    final Set<ClassPath> seen = new HashSet<>();
                    for (SourceGroup sg : ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                        final ClassPath src = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.SOURCE);
                        if (src != null && seen.add(src)) {
                            final FileObject[] sr = src.getRoots();
                            if (Arrays.stream(sr).allMatch(accepts)) {
                                res.add(sr);
                            }
                        }
                    }
                }
                return res.toArray(new FileObject[0][]);
            }
        }
    }

    //XXX: Leaking of implementation, should be pkg private
    //the reason why it's public is wrongly designed Callback interface
    public static final class Key {
        static final int TYPE_PLATFORM = 0;         //platform
        static final int TYPE_LIBRARY = 1;          //library
        static final int TYPE_FILE_REFERENCE = 2;   //file added by ReferenceHelper ${reference.
        static final int TYPE_PROJECT = 3;          //project
        static final int TYPE_OTHER = 4;            //extension provided by Callback
        static final int TYPE_FILE = 5;             //direct file not added by ReferenceHelper
        static final int TYPE_MODULE = 6;           //module form same multi module proejct

        private int type;
        private String classPathId;
        private String entryId;
        private SourceGroup sg;
        private AntArtifact antArtifact;
        private URI uri;
        private String anID;
        private final Consumer<Pair<String,String>> preRemoveAction;
        private final Consumer<Pair<String,String>> postRemoveAction;
        private final boolean shared;

        private static Key platform() {
            return new Key();
        }
        
        private static Key project(
                @NonNull final AntArtifact a,
                @NonNull final URI uri,
                @NonNull final String classPathId,
                @NonNull final String entryId,
                @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
                @NullAllowed final Consumer<Pair<String,String>> postRemoveAction,
                boolean shared) {
            return new Key(a, uri, classPathId, entryId, preRemoveAction, postRemoveAction, shared);
        }
        
        private static Key library(
                @NonNull final SourceGroup sg,
                @NonNull final String classPathId,
                @NonNull final String entryId,
                @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
                @NullAllowed final Consumer<Pair<String,String>> postRemoveAction, boolean shared) {
            return new Key(TYPE_LIBRARY, sg, classPathId, entryId, preRemoveAction, postRemoveAction, shared);
        }
        
        private static Key fileReference(
                @NonNull final SourceGroup sg,
                @NonNull final String classPathId,
                @NonNull final String entryId,
                @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
                @NullAllowed final Consumer<Pair<String,String>> postRemoveAction, boolean shared) {
            return new Key(TYPE_FILE_REFERENCE, sg, classPathId, entryId, preRemoveAction, postRemoveAction, shared);
        }

        private static Key file(
                @NonNull final SourceGroup sg,
                @NonNull final String classPathId,
                @NonNull final String entryId,
                @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
                @NullAllowed final Consumer<Pair<String,String>> postRemoveAction,
                boolean shared) {
            return new Key(TYPE_FILE, sg, classPathId, entryId, preRemoveAction, postRemoveAction, shared);
        }

        @NonNull
        private static Key module(
                @NonNull final String moduleName,
                @NonNull final URI uri,
                @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
                @NullAllowed final Consumer<Pair<String,String>> postRemoveAction) {
            return new Key(moduleName, uri, preRemoveAction, postRemoveAction);
        }

        public Key (String anID) {
            this.type = TYPE_OTHER;
            this.anID = anID;
            preRemoveAction = postRemoveAction = null;
            shared = false;
        }

        private Key () {
            type = TYPE_PLATFORM;
            preRemoveAction = postRemoveAction = null;
            shared = false;
        }

        private Key(
                @NonNull final String moduleName,
                @NonNull final URI uri,
                @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
                @NullAllowed final Consumer<Pair<String,String>> postRemoveAction) {
            assert moduleName != null;
            assert uri != null;
            this.type = TYPE_MODULE;
            this.entryId = moduleName;
            this.uri = uri;
            this.preRemoveAction = preRemoveAction;
            this.postRemoveAction = postRemoveAction;
            shared = false;
        }

        private Key (
                final int type,
                @NonNull final SourceGroup sg,
                @NonNull final String classPathId,
                @NonNull final String entryId,
                @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
                @NullAllowed final Consumer<Pair<String,String>> postRemoveAction,
                boolean shared) {
            assert type == TYPE_LIBRARY || type == TYPE_FILE_REFERENCE || type == TYPE_FILE;
            this.type = type;
            this.sg = sg;
            this.classPathId = classPathId;
            this.entryId = entryId;
            this.preRemoveAction = preRemoveAction;
            this.postRemoveAction = postRemoveAction;
            this.shared = shared;
        }

        private Key (
                @NonNull final AntArtifact a,
                @NonNull final URI uri,
                @NonNull final String classPathId,
                @NonNull final String entryId,
                @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
                @NullAllowed final Consumer<Pair<String,String>> postRemoveAction, 
                boolean shared) {
            this.type = TYPE_PROJECT;
            this.antArtifact = a;
            this.uri = uri;
            this.classPathId = classPathId;
            this.entryId = entryId;
            this.preRemoveAction = preRemoveAction;
            this.postRemoveAction = postRemoveAction;
            this.shared = shared;
        }

        public int getType () {
            return this.type;
        }

        public String getClassPathId () {
            return this.classPathId;
        }

        public String getEntryId () {
            return this.entryId;
        }

        public SourceGroup getSourceGroup () {
            return this.sg;
        }

        public AntArtifact getProject() {
            return this.antArtifact;
        }
        
        public URI getArtifactLocation() {
            return this.uri;
        }

        public String getID() {
            return anID;
        }
        
        @CheckForNull
        URI toURI() {
            if (sg != null) {
                return sg.getRootFolder().toURI();
            } else if (antArtifact != null) {
                return Optional.ofNullable(resolveAntArtifact(antArtifact, uri))
                        .map((u) -> {
                            try {
                                return u.toURI();
                            } catch (URISyntaxException ex) {
                                Exceptions.printStackTrace(ex);
                                return null;
                            }
                        })
                        .orElse(null);
            } else if (type == TYPE_MODULE){
                return uri;
            } else {
                return null;
            }
        }

        @CheckForNull
        Consumer<Pair<String, String>> getPreRemoveAction() {
            return preRemoveAction;
        }

        @CheckForNull
        Consumer<Pair<String, String>> getPostRemoveAction() {
            return postRemoveAction;
        }

        @Override
        public int hashCode() {
            int hashCode = this.type<<16;
            switch (this.type) {
                case TYPE_LIBRARY:
                case TYPE_FILE_REFERENCE:
                case TYPE_FILE:
                    hashCode ^= this.sg == null ? 0 : this.sg.hashCode();
                    break;
                case TYPE_PROJECT:
                    hashCode ^= this.antArtifact == null ? 0 : this.antArtifact.hashCode();
                    break;
                case TYPE_MODULE:
                    hashCode ^= entryId.hashCode();
                    break;
                case TYPE_OTHER:
                    hashCode ^= anID.hashCode();
                    break;
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            if (other.type != type) {
                return false;
            }
            switch (type) {
                case TYPE_LIBRARY:
                case TYPE_FILE_REFERENCE:
                case TYPE_FILE:
                    return (this.sg == null ? other.sg == null : this.sg.equals(other.sg)) &&
                        (this.classPathId == null ? other.classPathId == null : this.classPathId.equals (other.classPathId)) &&
                        (this.entryId == null ? other.entryId == null : this.entryId.equals (other.entryId));
                case TYPE_PROJECT:
                    return (this.antArtifact == null ? other.antArtifact == null : this.antArtifact.equals(other.antArtifact)) &&
                        (this.classPathId == null ? other.classPathId == null : this.classPathId.equals (other.classPathId)) &&
                        (this.entryId == null ? other.entryId == null : this.entryId.equals (other.entryId));
                case TYPE_PLATFORM:
                    return true;
                case TYPE_MODULE:
                    return entryId.equals(other.entryId) &&
                            uri.equals(other.uri);
                case TYPE_OTHER:
                    return anID.equals(other.anID);
                default:
                    throw new IllegalStateException();
            }
        }
    }
    
    private static class AddProjectAction extends AbstractAction implements ContextAwareAction {

        private final Project project;
        private final Supplier<FileObject[]> sources;

        public AddProjectAction (Project project, Supplier<FileObject[]> sources) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddProject_Action" ) );
            this.project = project;
            this.sources = sources;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AntArtifactItem ai[] = AntArtifactItem.showAntArtifactItemChooser(
                    new String[] {JavaProjectConstants.ARTIFACT_TYPE_JAR, JavaProjectConstants.ARTIFACT_TYPE_FOLDER},
                    project, null);
                if ( ai != null ) {
                    addArtifacts( ai );
                }
        }

        @Override
        public boolean isEnabled() {
            return this.sources.get().length > 0;
        }

        private void addArtifacts (AntArtifactItem[] artifactItems) {
            final FileObject[] roots = this.sources.get();
            if (roots.length == 0) {
                return;
            }
            final FileObject projectSourcesArtifact = roots[0];
            AntArtifact[] artifacts = new AntArtifact[artifactItems.length];
            URI[] artifactURIs = new URI[artifactItems.length];
            for (int i = 0; i < artifactItems.length; i++) {
                artifacts[i] = artifactItems[i].getArtifact();
                artifactURIs[i] = artifactItems[i].getArtifactURI();
            }
            try {
                final FileObject moduleInfo = findModuleInfo(roots);
                final String cpType =  moduleInfo != null ?
                        JavaClassPathConstants.MODULE_COMPILE_PATH :
                        ClassPath.COMPILE;
                ProjectClassPathModifier.addAntArtifacts(artifacts, artifactURIs,
                        projectSourcesArtifact, cpType);
                DefaultProjectModulesModifier.extendModuleInfo(moduleInfo, Arrays.asList(toURLs(artifactItems)));
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } catch (UnsupportedOperationException e) {
                handlePCPMUnsupported(sources, e);
            }
        }

        @NonNull
        @Override
        public Action createContextAwareInstance(@NonNull final Lookup actionContext) {
            final ClassPath scp = actionContext.lookup(ClassPath.class);
            final Supplier<FileObject[]> ctx = scp != null ?
                    scp::getRoots :
                    sources;
            return new AddProjectAction(project, ctx);
        }
    }

    private static class AddLibraryAction extends AbstractAction implements ContextAwareAction {

        private final LibraryChooser.Filter filter;
        private final Supplier<FileObject[]> sources;
        private ReferenceHelper refHelper;

        public AddLibraryAction(ReferenceHelper refHelper, Supplier<FileObject[]> sources, LibraryChooser.Filter filter) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddLibrary_Action" ) );
            this.refHelper = refHelper;
            this.sources = sources;
            this.filter = filter;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Set<Library> added = LibraryChooser.showDialog(
                    refHelper.getProjectLibraryManager(),
                    filter,
                    CustomizerUtilities.getLibraryChooserImportHandler(refHelper));
            if (added != null) {
                addLibraries(added.toArray(new Library[0]));
            }
        }

        @Override
        public boolean isEnabled() {
            return this.sources.get().length > 0;
        }

        private void addLibraries (Library[] libraries) {
            final FileObject[] roots = this.sources.get();
            if (roots.length == 0) {
                return;
            }
            final FileObject projectSourcesArtifact = roots[0];
            try {
                final FileObject moduleInfo = findModuleInfo(roots);
                final String cpType =  moduleInfo != null ?
                        JavaClassPathConstants.MODULE_COMPILE_PATH :
                        ClassPath.COMPILE;
                ProjectClassPathModifier.addLibraries(libraries,
                        projectSourcesArtifact, cpType);
                DefaultProjectModulesModifier.extendModuleInfo(moduleInfo, Arrays.asList(toURLs(libraries)));
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } catch (UnsupportedOperationException e) {
                handlePCPMUnsupported(sources, e);
            }
        }

        @NonNull
        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            final ClassPath scp = actionContext.lookup(ClassPath.class);
            final Supplier<FileObject[]> ctx = scp != null ?
                    scp::getRoots :
                    sources;
            return new AddLibraryAction(refHelper, ctx, filter);
        }
    }

    private static class AddFolderAction extends AbstractAction implements ContextAwareAction {

        private final AntProjectHelper helper;
        private final Supplier<FileObject[]> sources;

        public AddFolderAction (AntProjectHelper helper, Supplier<FileObject[]> sources) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddFolder_Action" ) );
            this.helper = helper;
            this.sources = sources;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            org.netbeans.api.project.ant.FileChooser chooser;
            if (helper.isSharableProject()) {
                chooser = new org.netbeans.api.project.ant.FileChooser(helper, true);
            } else {
                chooser = new org.netbeans.api.project.ant.FileChooser(FileUtil.toFile(helper.getProjectDirectory()), null);
            }
            chooser.enableVariableBasedSelection(true);
            chooser.setFileHidingEnabled(false);
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
            chooser.setMultiSelectionEnabled( true );
            chooser.setDialogTitle( NbBundle.getMessage( LibrariesNode.class, "LBL_AddJar_DialogTitle" ) ); // NOI18N
            //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
            chooser.setAcceptAllFileFilterUsed( false );
            FileFilter fileFilter = new SimpleFileFilter (
                    NbBundle.getMessage( LibrariesNode.class, "LBL_ZipJarFolderFilter" )); // NOI18N
            chooser.setFileFilter(fileFilter);
            File curDir = EditMediator.getLastUsedClassPathFolder();
            chooser.setCurrentDirectory (curDir);
            int option = chooser.showOpenDialog( WindowManager.getDefault().getMainWindow() );
            if ( option == JFileChooser.APPROVE_OPTION ) {
                String filePaths[];
                try {
                    filePaths = chooser.getSelectedPaths();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return;
                }
                addJarOrFolder ( filePaths, chooser.getSelectedPathVariables(), fileFilter, FileUtil.toFile(helper.getProjectDirectory()));
                curDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
                EditMediator.setLastUsedClassPathFolder(curDir);
            }
        }

        @Override
        public boolean isEnabled() {
            return this.sources.get().length > 0;
        }

        private void addJarOrFolder (String[] filePaths, final String[] pathBasedVariables, FileFilter fileFilter, File base) {
            final FileObject[] roots = this.sources.get();
            if (roots.length == 0) {
                return;
            }
            try {
                final FileObject projectSourcesArtifact = roots[0];
                final List<URI> toAdd = new ArrayList<>(filePaths.length);            
                final List<URL> toAddURLs = new ArrayList<>(toAdd.size());
                final Function<File,Collection<File>> moduleFinder = LibrariesChildren.ModulesFinder.INSTANCE;
                for (int i=0; i<filePaths.length;i++) {
                    //Check if the file is acceted by the FileFilter,
                    //user may enter the name of non displayed file into JFileChooser
                    File fl = PropertyUtils.resolveFile(base, filePaths[i]);
                    FileObject fo = FileUtil.toFileObject(fl);
                    assert fo != null || !fl.canRead(): fl;
                    if (fo != null && fileFilter.accept(fl)) {
                        boolean isFolderOfModules = false;
                        if (fo.isFolder()) {
                            final Collection<File> sgs = moduleFinder.apply(fl);
                            if (sgs.size() != 1 || !fl.equals(sgs.iterator().next())) {
                                isFolderOfModules = true;
                            }
                        }
                        if (!isFolderOfModules) {
                            Optional.ofNullable(FileUtil.urlForArchiveOrDir(fl))
                                    .ifPresent(toAddURLs::add);
                        }
                        URI u;
                        boolean isArchiveFile = FileUtil.isArchiveFile(fo);
                        if (pathBasedVariables == null) {
                            u = LibrariesSupport.convertFilePathToURI(filePaths[i]);
                        } else {
                            try {
                                String path = pathBasedVariables[i];
                                // append slash before creating relative URI:
                                if (!isArchiveFile && !path.endsWith("/")) { // NOI18N
                                    path += "/"; // NOI18N
                                }
                                // create relative URI
                                u = new URI(null, null, path, null);
                            } catch (URISyntaxException ex) {
                                Exceptions.printStackTrace(ex);
                                u = LibrariesSupport.convertFilePathToURI(filePaths[i]);
                            }
                        }
                        if (isArchiveFile) {
                            try {
                                new JarFile (fl);
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog (
                                    WindowManager.getDefault ().getMainWindow (),
                                    NbBundle.getMessage (LibrariesNode.class, "LBL_Corrupted_JAR", fl),
                                    NbBundle.getMessage (LibrariesNode.class, "LBL_Corrupted_JAR_title"),
                                    JOptionPane.WARNING_MESSAGE
                                );
                                continue;
                            }
                            u = LibrariesSupport.getArchiveRoot(u);
                        } else if (!u.toString().endsWith("/")) { // NOI18N
                            try {
                                u = new URI(u.toString() + "/"); // NOI18N
                            } catch (URISyntaxException ex) {
                                throw new AssertionError(ex);
                            }
                        }
                        assert u != null;
                        toAdd.add(u);
                    }                    
                }
                final Project prj = FileOwnerQuery.getOwner(helper.getProjectDirectory());
                final ClassPathModifier modifierImpl = prj.getLookup().lookup(ClassPathModifier.class);
                if (modifierImpl == null) {
                    throw new IllegalStateException(
                        String.format("Project: %s (located in: %s) does not provide ClassPathModifier in Lookup.",   //NOI18N
                        prj,
                        FileUtil.getFileDisplayName(prj.getProjectDirectory())));
                } else {
                    final FileObject moduleInfo = findModuleInfo(roots);
                    final String cpType =  moduleInfo != null ?
                        JavaClassPathConstants.MODULE_COMPILE_PATH :
                        ClassPath.COMPILE;
                    modifierImpl.addRoots(toAdd.toArray(new URI[0]),
                        findSourceGroup(projectSourcesArtifact, modifierImpl),
                        cpType,
                        ClassPathModifier.ADD_NO_HEURISTICS);
                    DefaultProjectModulesModifier.extendModuleInfo(moduleInfo, toAddURLs);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            final ClassPath scp = actionContext.lookup(ClassPath.class);
            final Supplier<FileObject[]> ctx = scp != null ?
                    scp::getRoots :
                    sources;
            return new AddFolderAction(helper, ctx);
        }
    }

    @CheckForNull
    private static FileObject findModuleInfo(@NonNull final FileObject... roots) {
        for (FileObject root : roots) {
            final FileObject fo = root.getFileObject("module-info.java");   //NOI18N
            if (fo != null) {   //NOI18N
                return fo;
            }
        }
        return null;
    }
    
    @CheckForNull
    private static URL resolveAntArtifact(
            @NonNull final AntArtifact art,
            @NonNull final URI loc) {
        final FileObject prj = art.getProject().getProjectDirectory();
        final File f = BaseUtilities.toFile(prj.toURI().resolve(loc));
        return FileUtil.urlForArchiveOrDir(f);
    }
    
    @NonNull
    private static URL[] toURLs(@NonNull final Library... libraries) {
        final List<URL> res = new ArrayList<>();
        for (Library library : libraries) {
            res.addAll(library.getContent("classpath"));    //NOI18N
        }
        return res.toArray(new URL[0]);
    }
    
    @NonNull
    private static URL[] toURLs(@NonNull final AntArtifactItem... artifacts) {
        final List<URL> res = new ArrayList<>(artifacts.length);
        for (AntArtifactItem ai : artifacts) {
            final URL resolved = resolveAntArtifact(ai.getArtifact(), ai.getArtifactURI());
            if (resolved != null) {
                res.add(resolved);
            }
        }
        return res.toArray(new URL[0]);
    }
    
    private static SourceGroup findSourceGroup(FileObject fo, ClassPathModifier modifierImpl) {
        SourceGroup[]sgs = modifierImpl.getExtensibleSourceGroups();
        for (SourceGroup sg : sgs) {
            if ((fo == sg.getRootFolder() || FileUtil.isParentOf(sg.getRootFolder(),fo)) && sg.contains(fo)) {
                return sg;
            }
        }
        throw new AssertionError("Cannot find source group for '"+fo+"' in "+Arrays.asList(sgs)); // NOI18N
    }

    private static class SimpleFileFilter extends FileFilter {

        private String description;

        private final Set<String> extensions;


        public SimpleFileFilter (String description) {
            this.description = description;
            this.extensions = new HashSet<String>();
            this.extensions.addAll(FileUtil.getMIMETypeExtensions("application/x-java-archive"));    //NOI18N
        }

        @Override
        public boolean accept(final File f) {
            if (f.isDirectory()) {
                return true;
            }
            try {
                //Can use FileUtil.getMIMEType(fo, withinMIMETypes), but this should be even faster no FO is created
                if (!this.extensions.isEmpty()) {
                    final String fileName = f.getName();
                    int index = fileName.lastIndexOf('.');  //NOI18N
                    if (index > 0 && index < fileName.length()-1) {
                        return extensions.contains(fileName.substring(index+1));
                    }
                }
                else {
                    //No MimeResolver fallback
                    return FileUtil.isArchiveFile(BaseUtilities.toURI(f).toURL());
                }
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);                
            }
            return false;
        }

        @Override
        public String getDescription() {
            return this.description;
        }
    }

    /**
     * Optional extension point to enhance LibrariesNode with additional nodes,
     * for example J2EE project type may add J2EE platform node.
     */
    public static interface Callback {

        /** Enhance LibrariesNode with additional <code>Key</code>s.*/
        List<Key> getExtraKeys();

        /** Creates nodes for extra key. */
        Node[] createNodes(Key key);
    }

    private static final class PathFinder implements org.netbeans.spi.project.ui.PathFinder {
        

        PathFinder() {
        }

        @Override
        public Node findPath(Node root, Object target) {
            Node result = null;
            for (Node  node : root.getChildren().getNodes(true)) {
                final org.netbeans.spi.project.ui.PathFinder pf =
                    node.getLookup().lookup(org.netbeans.spi.project.ui.PathFinder.class);
                if (pf == null) {
                    continue;
                }
                result = pf.findPath(node, target);
                if (result != null) {
                    break;
                }
            }
            return result;
        }

    }

    private static class RootsListener implements FileChangeListener  {

        static final String PROP_ROOTS = "roots";   //NOI18N

        private final PropertyChangeSupport support = new PropertyChangeSupport(this);
        private final Collection<File> listensOn;
        private final AtomicInteger state = new AtomicInteger();

        RootsListener(List<? extends URL> roots) {
            listensOn = new HashSet<File>();
            for (URL root : roots) {
                try {
                    final URL archiveURL = FileUtil.getArchiveFile(root);
                    if (archiveURL != null) {
                        root = archiveURL;
                    }
                    listensOn.add(BaseUtilities.toFile(root.toURI()));
                } catch (IllegalArgumentException e) {
                    //Ignore - not a local file
                } catch (URISyntaxException e) {
                    //Ignore - not a local file
                }
            }
        }


        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            if (!state.compareAndSet(0, 1)) {
                throw new IllegalStateException("Already in state: " + state.get());    //NOI18N
            }
            support.addPropertyChangeListener(listener);
            for (File f : listensOn) {
                FileUtil.addFileChangeListener(this, f);
            }
        }

        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            if (!state.compareAndSet(1, 2)) {
                throw new IllegalStateException("Already in state: " + state.get());    //NOI18N
            }
            support.removePropertyChangeListener(listener);
            for (File f : listensOn) {
                FileUtil.removeFileChangeListener(this, f);
            }
        }        

        @Override
        public void fileFolderCreated(FileEvent fe) {
            fire();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            fire();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            fire();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            fire();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            fire();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        private void fire() {
            support.firePropertyChange(PROP_ROOTS, null, null);
        }

    }
}
