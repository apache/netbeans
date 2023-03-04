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
package org.netbeans.modules.gradle.java.nodes;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.java.api.ProjectSourcesClassPathProvider;
import static org.netbeans.modules.gradle.java.nodes.Bundle.BootCPNode_displayName;
import org.netbeans.modules.gradle.spi.nodes.AbstractGradleNodeList;
import org.netbeans.modules.gradle.spi.nodes.NodeUtils;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gradle.java.execute.JavaRunUtils;
import org.netbeans.spi.project.ui.PathFinder;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.xml.XMLUtil;

@NodeFactory.Registration(projectType = NbGradleProject.GRADLE_PROJECT_TYPE, position = 520)
public class BootCPNodeFactory implements NodeFactory {

    public static final String ENDORSED = "classpath/endorsed";

    private static final @StaticResource
    String LIBS_BADGE = "org/netbeans/modules/gradle/java/resources/libraries-badge.png";

    private static final String PLATFORM_ICON = "org/netbeans/modules/java/api/common/project/ui/resources/platform.gif";    //NOI18N
    private static final String ARCHIVE_ICON = "org/netbeans/modules/java/api/common/project/ui/resources/jar.gif"; //NOI18N
    private static final String MODULE_ICON = "org/netbeans/modules/java/api/common/project/ui/resources/module.png"; //NOI18N

    @Override
    public NodeList<?> createNodes(final Project p) {
        return new AbstractGradleNodeList<Void>() {
            ChangeListener listener = (evt) -> fireChange();

            @Override
            public List<Void> keys() {
                return ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA).length == 0
                        ? Collections.<Void>emptyList()
                        : Collections.<Void>singletonList(null);
            }

            @Override
            public Node node(Void key) {
                return new BootCPNode(new PlatformProvider(p, null));
            }

            @Override
            public void addNotify() {
                Sources srcs = ProjectUtils.getSources(p);
                srcs.addChangeListener(listener);
            }

            @Override
            public void removeNotify() {
                Sources srcs = ProjectUtils.getSources(p);
                srcs.removeChangeListener(listener);
            }
            
        };
    }

    private static class BootCPNode extends AbstractNode {

        @Messages("BootCPNode_displayName=Java Dependencies")
        @SuppressWarnings("OverridableMethodCallInConstructor")
        BootCPNode(PlatformProvider pp) {
            super(Children.create(new BootCPChildren(pp), false), Lookups.singleton(PathFinders.createPathFinder()));
            setName("BootCPNode");
            setDisplayName(BootCPNode_displayName());
        }

        @Override
        public Image getIcon(int param) {
            return ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(false), ImageUtilities.loadImage(LIBS_BADGE), 8, 8);
        }

        @Override
        public Image getOpenedIcon(int param) {
            return ImageUtilities.mergeImages(NodeUtils.getTreeFolderIcon(true), ImageUtilities.loadImage(LIBS_BADGE), 8, 8);
        }

    }

    // XXX PlatformNode and ActionFilterNode does some of what we want, but cannot be reused
    private static class BootCPChildren extends ChildFactory.Detachable<FileObject> implements ChangeListener, PropertyChangeListener {

        private final PlatformProvider pp;
        private ClassPath[] endorsed;
        private static final FileObject BOOT = FileUtil.createMemoryFileSystem().getRoot();

        BootCPChildren(PlatformProvider pp) {
            this.pp = pp;
        }

        @Override
        protected void addNotify() {
            pp.addChangeListener(this);
            ProjectSourcesClassPathProvider pvd = pp.project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            endorsed = pvd != null ? pvd.getProjectClassPath(ENDORSED) : new ClassPath[0];
            for (ClassPath cp : endorsed) {
                cp.addPropertyChangeListener(this);
            }
        }

        @Override
        protected void removeNotify() {
            pp.removeChangeListener(this);
            for (ClassPath cp : endorsed) {
                cp.removePropertyChangeListener(this);
            }
            endorsed = null;
        }

        @Override
        protected boolean createKeys(List<FileObject> roots) {
            roots.add(BOOT);
            for (ClassPath cp : endorsed) {
                roots.addAll(Arrays.asList(cp.getRoots()));
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(FileObject root) {
            return  root == BOOT ? new JRENode(pp) : jarNode(new LibrariesSourceGroup(root, root.getNameExt()));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ClassPath.PROP_ROOTS)) {
                refresh(false);
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

    }

    @NbBundle.Messages({
        "# {0} - Platform Display name",
        "FMT_BrokenPlatform=Broken platform ''{0}''",
        "TXT_BrokenPlatform=Broken platform",
        "TXT_UnknownPlatform=Loading..."
    })
    private static class JRENode extends AbstractNode implements ChangeListener {

        private final PlatformProvider pp;

        @SuppressWarnings("OverridableMethodCallInConstructor")
        private JRENode(PlatformProvider pp) {
            super(new CPChildren(), Lookups.singleton(PathFinders.createPathFinder()));
            this.pp = pp;
            pp.addChangeListener(this);
            setIconBaseWithExtension(PLATFORM_ICON);
        }

        @Override
        public String getName() {
            return this.getDisplayName();
        }

        @Override
        public String getDisplayName() {
            final Pair<String, JavaPlatform> platHolder = pp.getPlatform();
            if (platHolder == null) {
                return Bundle.TXT_UnknownPlatform();
            }
            String name;
            final JavaPlatform jp = platHolder.second();
            if (jp != null) {
                if (jp.isValid()) {
                    name = jp.getDisplayName();
                } else {
                    name = Bundle.FMT_BrokenPlatform(jp.getDisplayName());
                }
            } else {
                String platformId = platHolder.first();
                if (platformId == null) {
                    name = Bundle.TXT_BrokenPlatform();
                } else {
                    name = Bundle.FMT_BrokenPlatform(platformId);
                }
            }
            return name;
        }

        @Override
        public String getHtmlDisplayName() {
            final Pair<String, JavaPlatform> platHolder = pp.getPlatform();
            if (platHolder == null) {
                return null;
            }
            final JavaPlatform jp = platHolder.second();
            if (jp == null || !jp.isValid()) {
                String displayName = this.getDisplayName();
                try {
                    displayName = XMLUtil.toElementContent(displayName);
                } catch (CharConversionException ex) {
                    // OK, no annotation in this case
                    return null;
                }
                return "<font color=\"#A40000\">" + displayName + "</font>"; //NOI18N
            } else {
                return null;
            }
        }

        @Override
        public boolean canCopy() {
            return false;
        }
        
        @Override
        public String getShortDescription() {
            final Pair<String,JavaPlatform> platHolder = pp.getPlatform();
            if (platHolder != null && platHolder.second() != null && !platHolder.second().getInstallFolders().isEmpty()) {
                final FileObject installFolder = platHolder.second().getInstallFolders().iterator().next();
                return FileUtil.getFileDisplayName(installFolder);
            } else {
                return super.getShortDescription();
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            this.fireNameChange(null,null);
            this.fireDisplayNameChange(null,null);
            ((CPChildren) getChildren()).addNotify();
        }


    }

    private static class CPChildren extends Children.Keys<SourceGroup> {

        CPChildren () {}

        @Override
        protected void addNotify() {
            this.setKeys (this.getKeys());
        }

        @Override
        protected void removeNotify() {
            this.setKeys(Collections.<SourceGroup>emptySet());
        }

        @Override
        protected Node[] createNodes(SourceGroup sg) {
            final Node afn = jarNode(sg);
            return afn == null ? new Node[0] : new Node[] {afn};
        }

        private List<SourceGroup> getKeys () {
            final FileObject[] roots = ((JRENode)this.getNode()).pp.getBootstrapLibraries();
            if (roots.length == 0) {
                return Collections.<SourceGroup>emptyList();
            }
            final List<SourceGroup> result = new ArrayList<>(roots.length);
            for (FileObject root : roots) {
                    FileObject file;
                    Icon icon;
                    Icon openedIcon;
                    switch (root.toURL().getProtocol()) {
                        case "jar":
                            file = FileUtil.getArchiveFile (root);
                            icon = openedIcon = ImageUtilities.loadImageIcon(ARCHIVE_ICON, false);
                            break;
                        case "nbjrt":
                            file = root;
                            icon = openedIcon = ImageUtilities.loadImageIcon(MODULE_ICON, false);
                            break;
                        default:
                            file = root;
                            icon = openedIcon = null;
                    }
                    if (file.isValid()) {
                        result.add (new LibrariesSourceGroup(root,file.getNameExt(),icon, openedIcon));
                    }
            }
            return result;
        }

    }

    private static Node jarNode(SourceGroup sg) {
        final Node delegate = PackageView.createPackageView(sg);
        final PathFinder pathFinder = PathFinders.createDelegatingPathFinder(delegate.getLookup().lookup(PathFinder.class));
        final Lookup lkp = new ProxyLookup(
                Lookups.exclude(delegate.getLookup(), PathFinder.class),
                Lookups.singleton(pathFinder));
        return new FilterNode(
                delegate,
                null,
                lkp) {
                    @Override
                    public Action[] getActions(boolean context) {
                        return new Action[0];
                    }
        };

    }
    
    private static final class PlatformProvider implements PropertyChangeListener, PreferenceChangeListener {

        private static final Pair<String,JavaPlatform> BUSY = Pair.<String,JavaPlatform>of(null,null);
        private static final RequestProcessor RP = new RequestProcessor(PlatformProvider.class);

        private final Project project;
        private final ClassPath boot;
        private final AtomicReference<Pair<String,JavaPlatform>> platformCache = new AtomicReference<Pair<String,JavaPlatform>>();
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        
        public PlatformProvider (
                @NonNull final Project project,
                @NonNull final ClassPath boot) {
            this.project = project;
            this.boot = boot;
            final JavaPlatformManager jps = JavaPlatformManager.getDefault();
            jps.addPropertyChangeListener(WeakListeners.propertyChange(this, jps));
            Preferences prefs = NbGradleProject.getPreferences(project, false);
            prefs.addPreferenceChangeListener(
                    WeakListeners.create(PreferenceChangeListener.class, this, prefs));
            NbGradleProject.addPropertyChangeListener(project, WeakListeners.propertyChange(this, NbGradleProject.get(project)));
            
            if (this.boot != null) {
                this.boot.addPropertyChangeListener(WeakListeners.propertyChange(this, this.boot));
            }
        }
                
        @CheckForNull
        public Pair<String,JavaPlatform> getPlatform () {
            if (platformCache.compareAndSet(null, BUSY)) {
                RP.execute(() -> {
                    platformCache.set(JavaRunUtils.getActivePlatform(project));
                    changeSupport.fireChange ();
                });
            }
            Pair<String,JavaPlatform> res = platformCache.get();
            return res == BUSY ? null : res;
        }

        @NonNull
        public FileObject[] getBootstrapLibraries() {
            final Pair<String, JavaPlatform> jp = getPlatform();
            if (jp == null || jp.second() == null) {
                return new FileObject[0];
            }
            ClassPath cp = boot;
            if (cp == null) {
                cp = jp.second().getBootstrapLibraries();
            }
            return cp.getRoots();
        }
        
        public void addChangeListener (ChangeListener l) {
            changeSupport.addChangeListener(l);
        }
        
        public void removeChangeListener (ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (NbGradleProject.PROP_PROJECT_INFO.equals(propName) ||
                ClassPath.PROP_ROOTS.equals(propName) ||
                JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(propName)) {
                platformCache.set(null);
                getPlatform();
            }
        }

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String prefName = evt.getKey();
            if (RunUtils.PROP_JDK_PLATFORM.equals(prefName)) {
                platformCache.set(null);
                getPlatform();
            }
        }
        
    }

}
