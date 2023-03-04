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
package org.netbeans.modules.javascript.cdnjs.ui.logicalview;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.cdnjs.Library;
import org.netbeans.modules.javascript.cdnjs.LibraryCustomizer;
import org.netbeans.modules.javascript.cdnjs.LibraryListener;
import org.netbeans.modules.javascript.cdnjs.LibraryPersistence;
import org.netbeans.modules.javascript.cdnjs.LibraryUtils;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * Factory for "CDNJS Libraries" node accessible in Projects view.
 */
public final class CdnjsLibraries {

    private CdnjsLibraries() {
    }

    @NodeFactory.Registration(projectType = "org-netbeans-modules-web-clientproject", position = 650)
    public static NodeFactory forHtml5Project() {
        return new CdnjsLibrariesNodeFactory();
    }

    @NodeFactory.Registration(projectType = "org-netbeans-modules-php-project", position = 450)
    public static NodeFactory forPhpProject() {
        return new CdnjsLibrariesNodeFactory();
    }

    @NodeFactory.Registration(projectType = "org-netbeans-modules-web-project", position = 330)
    public static NodeFactory forWebProject() {
        return new CdnjsLibrariesNodeFactory();
    }

    @NodeFactory.Registration(projectType = "org-netbeans-modules-maven", position = 630)
    public static NodeFactory forMavenProject() {
        return new CdnjsLibrariesNodeFactory();
    }

    //~ Inner classes

    private static final class CdnjsLibrariesNodeFactory implements NodeFactory {

        @Override
        public NodeList<?> createNodes(Project project) {
            assert project != null;
            return new CdnjsLibrariesNodeList(project);
        }

    }

    private static final class CdnjsLibrariesNodeList implements NodeList<Node>, LibraryListener, FileChangeListener {

        private final Project project;
        private final CdnjsLibrariesChildren cdnjsLibrariesChildren;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        // @GuardedBy("thread")
        private Node cdnjsLibrariesNode;


        CdnjsLibrariesNodeList(Project project) {
            assert project != null;
            this.project = project;
            cdnjsLibrariesChildren = new CdnjsLibrariesChildren(project);
        }

        @Override
        public List<Node> keys() {
            if (!cdnjsLibrariesChildren.hasLibraries()) {
                return Collections.<Node>emptyList();
            }
            if (cdnjsLibrariesNode == null) {
                cdnjsLibrariesNode = new CdnjsLibrariesNode(project, cdnjsLibrariesChildren);
            }
            return Collections.<Node>singletonList(cdnjsLibrariesNode);
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        @Override
        public Node node(Node key) {
            return key;
        }

        @Override
        public void addNotify() {
            LibraryPersistence libraryPersistence = LibraryPersistence.getDefault();
            libraryPersistence.addLibraryListener(WeakListeners.create(LibraryListener.class, this, libraryPersistence));
            FileUtil.addFileChangeListener(this, new File(LibraryUtils.getWebRoot(project), LibraryUtils.getLibraryFolder(project)));
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void librariesChanged(Project project) {
            if (project.equals(this.project)) {
                fireChange();
            }
        }

        private void fireChange() {
            cdnjsLibrariesChildren.refreshLibraries();
            changeSupport.fireChange();
        }

        //~ FS changes

        @Override
        public void fileFolderCreated(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            // noop
        }

        @Override
        public void fileChanged(FileEvent fe) {
            // noop
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            fireChange();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }

    }

    private static final class CdnjsLibrariesNode extends AbstractNode {

        @StaticResource
        private static final String LIBRARIES_BADGE = "org/netbeans/modules/javascript/cdnjs/ui/resources/libraries-badge.png"; // NOI18N

        private final Project project;
        private final Node iconDelegate;


        CdnjsLibrariesNode(Project project, CdnjsLibrariesChildren cdnjsLibrariesChildren) {
            super(cdnjsLibrariesChildren);
            assert project != null;
            this.project = project;
            iconDelegate = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
        }

        @NbBundle.Messages("CdnjsLibrariesNode.name=CDNJS Libraries")
        @Override
        public String getDisplayName() {
            return Bundle.CdnjsLibrariesNode_name();
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.mergeImages(iconDelegate.getIcon(type), ImageUtilities.loadImage(LIBRARIES_BADGE), 7, 7);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {
                new CustomizeLibrariesAction(project),
            };
        }

    }

    private static final class CdnjsLibrariesChildren extends Children.Keys<Library.Version> {

        private final Project project;


        CdnjsLibrariesChildren(Project project) {
            super(true);
            assert project != null;
            this.project = project;
        }

        public boolean hasLibraries() {
            return LibraryPersistence.getDefault().loadLibraries(project).length > 0;
        }

        public void refreshLibraries() {
            setKeys();
        }

        @Override
        protected Node[] createNodes(Library.Version key) {
            return new Node[] {new CdnjsLibraryNode(project, key)};
        }

        @Override
        protected void addNotify() {
            setKeys();
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.<Library.Version>emptyList());
        }

        private void setKeys() {
            Library.Version[] libraries = LibraryPersistence.getDefault().loadLibraries(project);
            if (libraries.length == 0) {
                setKeys(Collections.<Library.Version>emptyList());
                return;
            }
            setKeys(Arrays.asList(libraries));
        }

    }

    private static final class CdnjsLibraryNode extends AbstractNode {

        @StaticResource
        private static final String LIBRARIES_ICON = "org/netbeans/modules/javascript/cdnjs/ui/resources/libraries.gif"; // NOI18N
        @StaticResource
        private static final String BROKEN_BADGE = "org/netbeans/modules/javascript/cdnjs/ui/resources/broken-badge.gif"; // NOI18N

        private final Project project;
        private final Library.Version library;


        CdnjsLibraryNode(Project project, Library.Version library) {
            super(Children.LEAF);
            assert project != null;
            assert library != null;
            this.project = project;
            this.library = library;
        }

        @Override
        public String getName() {
            return library.getLibrary().getName();
        }

        @NbBundle.Messages({
            "# {0} - library name",
            "# {1} - library version",
            "CdnjsLibraryNode.description={0}: {1}",
            "# {0} - library name",
            "# {1} - library version",
            "CdnjsLibraryNode.description.broken={0}: {1} (broken)",
        })
        @Override
        public String getShortDescription() {
            if (LibraryUtils.isBroken(project, library)) {
                return Bundle.CdnjsLibraryNode_description_broken(getName(), library.getName());
            }
            return Bundle.CdnjsLibraryNode_description(getName(), library.getName());
        }

        @Override
        public Image getIcon(int type) {
            return getIcon();
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon();
        }

        private Image getIcon() {
            Image icon = ImageUtilities.loadImage(LIBRARIES_ICON, false);
            if (LibraryUtils.isBroken(project, library)) {
                return ImageUtilities.mergeImages(icon, ImageUtilities.loadImage(BROKEN_BADGE, false), 0, 7);
            }
            return ImageUtilities.loadImage(LIBRARIES_ICON, false);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }

    }

    private static final class CustomizeLibrariesAction extends AbstractAction {

        private final Project project;


        @NbBundle.Messages("CustomizeLibrariesAction.name=Properties")
        CustomizeLibrariesAction(Project project) {
            assert project != null;

            this.project = project;

            String name = Bundle.CustomizeLibrariesAction_name();
            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            project.getLookup().lookup(CustomizerProvider2.class).showCustomizer(LibraryCustomizer.CATEGORY_NAME, null);
        }

    }

}
