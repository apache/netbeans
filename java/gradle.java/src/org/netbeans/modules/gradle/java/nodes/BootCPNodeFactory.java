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
package org.netbeans.modules.gradle.java.nodes;

import org.netbeans.modules.gradle.api.NbGradleProject;
import static org.netbeans.modules.gradle.java.nodes.Bundle.BootCPNode_displayName;
import org.netbeans.modules.gradle.spi.nodes.AbstractGradleNodeList;
import org.netbeans.modules.gradle.spi.nodes.NodeUtils;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType;
import org.netbeans.modules.gradle.java.execute.JavaRunUtils;
import org.netbeans.modules.gradle.java.spi.support.JavaToolchainSupport;
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
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

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
                return new BootCPNode(p);
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
        BootCPNode(Project p) {
            super(Children.create(new BootCPChildren(p), false), Lookups.singleton(PathFinders.createPathFinder()));
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
    private record PlatformSourceSet(JavaPlatform platform, Set<GradleJavaSourceSet> sourceSets) {}
    private static class BootCPChildren extends ChildFactory.Detachable<PlatformSourceSet> {

        private final Project project;

        BootCPChildren(Project project) {
            this.project = project;
        }

        @Override
        protected void addNotify() {
            NbGradleProject.addPropertyChangeListener(project, this::projectChange);
        }

        @Override
        protected void removeNotify() {
            NbGradleProject.removePropertyChangeListener(project, this::projectChange);
        }

        @Override
        protected boolean createKeys(List<PlatformSourceSet> keys) {
            var toolchains = JavaToolchainSupport.getDefault();
            var pss = new HashMap<JavaPlatform, Set<GradleJavaSourceSet>>();
            var ss = GradleJavaProject.get(project).getSourceSets().values();
            for (GradleJavaSourceSet s : ss) {
                var home = s.getCompilerJavaHome(SourceType.JAVA);
                var platform = home != null ? toolchains.platformByHome(home) : JavaRunUtils.getActivePlatform(project).second();
                var groups = pss.computeIfAbsent(platform, (k) -> new TreeSet<GradleJavaSourceSet>((s1, s2) -> s1.getName().compareTo(s2.getName())));
                groups.add(s);
            }
            pss.forEach((platform, groups) -> keys.add(new PlatformSourceSet(platform, groups)));
            return true;
        }

        @Override
        protected Node createNodeForKey(PlatformSourceSet platform) {
            return  new JRENode(platform);
        }

        private void projectChange(PropertyChangeEvent evt) {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                refresh(false);
            }
        }

    }

    @NbBundle.Messages({
        "# {0} - Platform Display name",
        "FMT_BrokenPlatform=Broken platform ''{0}''",
    })
    private static class JRENode extends AbstractNode {

        private final PlatformSourceSet pss;

        @SuppressWarnings("OverridableMethodCallInConstructor")
        private JRENode(PlatformSourceSet pss) {
            super(new CPChildren(), Lookups.singleton(PathFinders.createPathFinder()));
            this.pss = pss;
            setIconBaseWithExtension(PLATFORM_ICON);
        }

        @Override
        public String getName() {
            return pss.platform().getDisplayName();
        }

        @Override
        public String getDisplayName() {
            String name = pss.platform.isValid() ? pss.platform.getDisplayName(): Bundle.FMT_BrokenPlatform(pss.platform.getDisplayName());
            String groups = pss.sourceSets.stream().map(GradleJavaSourceSet::getName).collect(Collectors.joining(", ", "[", "]"));
            return name + " " + groups;
        }

        @Override
        public String getHtmlDisplayName() {
            return null;
        }

        @Override
        public boolean canCopy() {
            return false;
        }
        
        @Override
        @Messages({
                "# {0} - The path of the Java Platform home",
                "# {1} - The list of the sourcesets wher the platform is used",
                "TOOLTIP_Platform=<html>Home: {0}<br/>Used in: {1}"
        })
        public String getShortDescription() {
            if (pss.platform.isValid()) {
                FileObject installFolder = pss.platform.getInstallFolders().iterator().next();
                String groups = pss.sourceSets.stream().map(GradleJavaSourceSet::getName).collect(Collectors.joining(", "));

                return Bundle.TOOLTIP_Platform(FileUtil.getFileDisplayName(installFolder), groups);
            } else {
                return super.getShortDescription();
            }
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
            final FileObject[] roots = ((JRENode)this.getNode()).pss.platform.getBootstrapLibraries().getRoots();
            final List<SourceGroup> result = new ArrayList<>(roots.length);
            for (FileObject root : roots) {
                var protocol = root.toURL().getProtocol();
                FileObject file = "jar".equals(protocol) ? FileUtil.getArchiveRoot(root) : root;
                if (file.isValid()) {
                    Icon icon = switch (protocol) {
                        case "jar" -> ImageUtilities.loadImageIcon(ARCHIVE_ICON, false);
                        case "nbjrt" -> ImageUtilities.loadImageIcon(MODULE_ICON, false);
                        default -> null;
                    };
                    result.add (new LibrariesSourceGroup(root,file.getNameExt(), icon, icon));
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
}
