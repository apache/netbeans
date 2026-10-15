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

package org.netbeans.modules.maven.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.spi.project.ui.PathFinder;
import static org.netbeans.modules.maven.nodes.Bundle.*;
import org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList;
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
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

@NodeFactory.Registration(projectType="org-netbeans-modules-maven", position=520)
public class BootCPNodeFactory implements NodeFactory {
    
    private static final @StaticResource String LIBS_BADGE = "org/netbeans/modules/maven/libraries-badge.png";
    

    @Override public NodeList<?> createNodes(final Project p) {
        return new AbstractMavenNodeList<Void>() {
            @Override public List<Void> keys() {
                return ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA).length == 0 ?
                        Collections.<Void>emptyList() :
                        Collections.<Void>singletonList(null);
            }
            @Override public Node node(Void v) {
                return new BootCPNode(p);
            }
        };
    }

    private static class BootCPNode extends AbstractNode {

        @Messages("BootCPNode_displayName=Java Dependencies")
        BootCPNode(Project p) {
            super(Children.create(new BootCPChildren(p), false), Lookups.singleton(PathFinders.createPathFinder()));
            setName("BootCPNode");
            setDisplayName(BootCPNode_displayName());
        }

        @Override public Image getIcon(int param) {
            return ImageUtilities.mergeImages(DependenciesNode.getTreeFolderIcon(false), ImageUtilities.loadImage(LIBS_BADGE), 8, 8);
        }

        @Override public Image getOpenedIcon(int param) {
            return ImageUtilities.mergeImages(DependenciesNode.getTreeFolderIcon(true), ImageUtilities.loadImage(LIBS_BADGE), 8, 8);
        }

    }

    // XXX PlatformNode and ActionFilterNode does some of what we want, but cannot be reused

    private static class BootCPChildren extends ChildFactory.Detachable<FileObject> implements PropertyChangeListener {

        private final Project p;
        private ClassPath[] endorsed;
        private static final FileObject BOOT = FileUtil.createMemoryFileSystem().getRoot();

        BootCPChildren(Project p) {
            this.p = p;
        }

        @Override protected void addNotify() {
            endorsed = p.getLookup().lookup(ProjectSourcesClassPathProvider.class).getProjectClassPaths(ClassPathSupport.ENDORSED);
            for (ClassPath cp : endorsed) {
                cp.addPropertyChangeListener(this);
            }
        }

        @Override protected void removeNotify() {
            for (ClassPath cp : endorsed) {
                cp.removePropertyChangeListener(this);
            }
            endorsed = null;
        }

        @Override protected boolean createKeys(List<FileObject> roots) {
            roots.add(BOOT);
            for (ClassPath cp : endorsed) {
                roots.addAll(Arrays.asList(cp.getRoots()));
            }
            return true;
        }

        @Override protected Node createNodeForKey(FileObject root) {
            if (root == BOOT) {
                return new JRENode(p);
            } else {
                return jarNode(p, root, true);
            }
        }

        @Override public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ClassPath.PROP_ROOTS)) {
                refresh(false);
            }
        }

    }

    private static class JRENode extends AbstractNode {

        JRENode(Project p) {
            this(p, p.getLookup().lookup(ActiveJ2SEPlatformProvider.class).getJavaPlatform());
        }
        private JRENode(Project p, JavaPlatform jp) {
            super(Children.create(new CPChildren(p, jp.getBootstrapLibraries()), true), Lookups.singleton(PathFinders.createPathFinder()));
            // XXX how to refresh this after a platform change?
            setDisplayName(jp.getDisplayName());
            setIconBaseWithExtension("org/netbeans/modules/java/api/common/project/ui/resources/platform.gif");
        }

    }

    private static class CPChildren extends ChildFactory.Detachable<FileObject> implements PropertyChangeListener {

        private final Project p;
        private final ClassPath cp;

        CPChildren(Project p, ClassPath cp) {
            this.p = p;
            this.cp = cp;
        }

        @Override protected void addNotify() {
            cp.addPropertyChangeListener(this);
        }

        @Override protected void removeNotify() {
            cp.removePropertyChangeListener(this);
        }

        @Override protected boolean createKeys(List<FileObject> roots) {
            roots.addAll(Arrays.asList(cp.getRoots()));
            return true;
        }

        @Override protected Node createNodeForKey(FileObject root) {
            return jarNode(p, root, false);
        }

        @Override public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ClassPath.PROP_ROOTS)) {
                refresh(false);
            }
        }

    }

    private static Node jarNode(
            final Project p,
            final FileObject root,
            final boolean includeEnclosingArchivePath) {
        final Node delegate = PackageView.createPackageView(new SourceGroup() {
            private final boolean isJar = "jar".equals(root.toURL().getProtocol()); //NOI18N
            @Override public FileObject getRootFolder() {
                return root;
            }
            @Override public String getName() {
                return root.getNameExt();
            }
            @Override public String getDisplayName() {
                File f =  FileUtil.archiveOrDirForURL(root.toURL());
                if (f != null) {
                    return f.getName();
                } else {
                    return includeEnclosingArchivePath ?
                            FileUtil.getFileDisplayName(root) :
                            root.getNameExt();
                }
            }
            @Override public Icon getIcon(boolean opened) {
                return isJar ? ImageUtilities.loadImageIcon("org/netbeans/modules/java/api/common/project/ui/resources/jar.gif", true):
                        null;
            }
            @Override public boolean contains(FileObject file) {
                return true;
            }
            @Override public void addPropertyChangeListener(PropertyChangeListener listener) {}
            @Override public void removePropertyChangeListener(PropertyChangeListener listener) {}
        });
        final PathFinder pathFinder = PathFinders.createDelegatingPathFinder(delegate.getLookup().lookup(PathFinder.class));
        final Lookup lkp = new ProxyLookup(
                Lookups.exclude(delegate.getLookup(), PathFinder.class),
                Lookups.singleton(pathFinder));
        return new FilterNode(
                delegate,
                null,
                lkp){};

    }

}
