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
package org.netbeans.modules.javascript.bower.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.bower.file.BowerJson;
import org.netbeans.modules.javascript.bower.ui.libraries.LibraryCustomizer;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

public final class BowerLibraries {

    private BowerLibraries() {
    }

    @NodeFactory.Registration(projectType = "org-netbeans-modules-web-clientproject", position = 610)
    public static NodeFactory forHtml5Project() {
        return new BowerLibrariesNodeFactory();
    }

    @NodeFactory.Registration(projectType = "org-netbeans-modules-php-project", position = 410)
    public static NodeFactory forPhpProject() {
        return new BowerLibrariesNodeFactory();
    }

    @NodeFactory.Registration(projectType = "org-netbeans-modules-web-project", position = 320)
    public static NodeFactory forWebProject() {
        return new BowerLibrariesNodeFactory();
    }

    @NodeFactory.Registration(projectType = "org-netbeans-modules-maven", position = 620)
    public static NodeFactory forMavenProject() {
        return new BowerLibrariesNodeFactory();
    }

    //~ Inner classes

    private static final class BowerLibrariesNodeFactory implements NodeFactory {

        @Override
        public NodeList<?> createNodes(Project project) {
            assert project != null;
            return new BowerLibrariesNodeList(project);
        }

    }

    private static final class BowerLibrariesNodeList implements NodeList<Node>, PropertyChangeListener {

        private final Project project;
        private final BowerJson bowerJson;
        private final BowerLibrariesChildren bowerLibrariesChildren;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        // @GuardedBy("thread")
        private Node bowerLibrariesNode;


        BowerLibrariesNodeList(Project project) {
            assert project != null;
            this.project = project;
            bowerJson = new BowerJson(project.getProjectDirectory());
            bowerLibrariesChildren = new BowerLibrariesChildren(bowerJson);
        }

        @Override
        public List<Node> keys() {
            if (!bowerLibrariesChildren.hasDependencies()) {
                return Collections.<Node>emptyList();
            }
            if (bowerLibrariesNode == null) {
                bowerLibrariesNode = new BowerLibrariesNode(project, bowerLibrariesChildren);
            }
            return Collections.<Node>singletonList(bowerLibrariesNode);
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
            bowerJson.addPropertyChangeListener(WeakListeners.propertyChange(this, bowerJson));
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (BowerJson.PROP_DEPENDENCIES.equals(propertyName)
                    || BowerJson.PROP_DEV_DEPENDENCIES.equals(propertyName)) {
                fireChange();
            }
        }

        private void fireChange() {
            bowerLibrariesChildren.refreshDependencies();
            changeSupport.fireChange();
        }

    }

    private static final class BowerLibrariesNode extends AbstractNode {

        @StaticResource
        private static final String LIBRARIES_BADGE = "org/netbeans/modules/javascript/bower/ui/resources/libraries-badge.png"; // NOI18N

        private final Project project;
        private final Node iconDelegate;


        BowerLibrariesNode(Project project, BowerLibrariesChildren bowerLibrariesChildren) {
            super(bowerLibrariesChildren);
            assert project != null;
            this.project = project;
            iconDelegate = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
        }

        @NbBundle.Messages("BowerLibrariesNode.name=Bower Libraries")
        @Override
        public String getDisplayName() {
            return Bundle.BowerLibrariesNode_name();
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

    private static final class BowerLibrariesChildren extends Children.Keys<BowerLibraryInfo> {

        @StaticResource
        private static final String LIBRARIES_ICON = "org/netbeans/modules/javascript/bower/ui/resources/libraries.gif"; // NOI18N
        @StaticResource
        private static final String DEV_BADGE = "org/netbeans/modules/javascript/bower/ui/resources/libraries-dev-badge.gif"; // NOI18N


        private final BowerJson bowerJson;
        private final java.util.Map<String, Image> icons = new HashMap<>();


        public BowerLibrariesChildren(BowerJson bowerJson) {
            super(true);
            assert bowerJson != null;
            this.bowerJson = bowerJson;
        }

        public boolean hasDependencies() {
            return !bowerJson.getDependencies().isEmpty();
        }

        public void refreshDependencies() {
            setKeys();
        }

        @Override
        protected Node[] createNodes(BowerLibraryInfo key) {
            return new Node[] {new BowerLibraryNode(key)};
        }

        @NbBundle.Messages("BowerLibrariesChildren.library.dev=dev")
        @Override
        protected void addNotify() {
            setKeys();
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.<BowerLibraryInfo>emptyList());
        }


        private void setKeys() {
            BowerJson.BowerDependencies dependencies = bowerJson.getDependencies();
            if (dependencies.isEmpty()) {
                setKeys(Collections.<BowerLibraryInfo>emptyList());
                return;
            }
            List<BowerLibraryInfo> keys = new ArrayList<>(dependencies.getCount());
            keys.addAll(getKeys(dependencies.dependencies, null, null));
            keys.addAll(getKeys(dependencies.devDependencies, DEV_BADGE, Bundle.BowerLibrariesChildren_library_dev()));
            setKeys(keys);
        }

        @NbBundle.Messages({
            "# {0} - library name",
            "# {1} - library version",
            "BowerLibrariesChildren.description.short={0}: {1}",
            "# {0} - library name",
            "# {1} - library version",
            "# {2} - library type",
            "BowerLibrariesChildren.description.long={0}: {1} ({2})",
        })
        private List<BowerLibraryInfo> getKeys(java.util.Map<String, String> dependencies, String badge, String libraryType) {
            if (dependencies.isEmpty()) {
                return Collections.emptyList();
            }
            List<BowerLibraryInfo> keys = new ArrayList<>(dependencies.size());
            for (java.util.Map.Entry<String, String> entry : dependencies.entrySet()) {
                String description;
                if (libraryType != null) {
                    description = Bundle.BowerLibrariesChildren_description_long(entry.getKey(), entry.getValue(), libraryType);
                } else {
                    description = Bundle.BowerLibrariesChildren_description_short(entry.getKey(), entry.getValue());
                }
                keys.add(new BowerLibraryInfo(geIcon(badge), entry.getKey(), description));
            }
            Collections.sort(keys);
            return keys;
        }

        private Image geIcon(String badge) {
            Image icon = icons.get(badge);
            if (icon == null) {
                icon = ImageUtilities.loadImage(LIBRARIES_ICON);
                if (badge != null) {
                    icon = ImageUtilities.mergeImages(icon, ImageUtilities.loadImage(badge), 8, 8);
                }
                icons.put(badge, icon);
            }
            return icon;
        }

    }

    private static final class BowerLibraryNode extends AbstractNode {

        private final BowerLibraryInfo libraryInfo;


        BowerLibraryNode(BowerLibraryInfo libraryInfo) {
            super(Children.LEAF);
            this.libraryInfo = libraryInfo;
        }

        @Override
        public String getName() {
            return libraryInfo.name;
        }

        @Override
        public String getShortDescription() {
            return libraryInfo.description;
        }

        @Override
        public Image getIcon(int type) {
            return libraryInfo.icon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return libraryInfo.icon;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }

    }

    private static final class BowerLibraryInfo implements Comparable<BowerLibraryInfo> {

        final Image icon;
        final String name;
        final String description;


        BowerLibraryInfo(Image icon, String name, String descrition) {
            assert icon != null;
            assert name != null;
            assert descrition != null;
            this.icon = icon;
            this.name = name;
            this.description = descrition;
        }

        @Override
        public int compareTo(BowerLibraryInfo other) {
            return name.compareToIgnoreCase(other.name);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.name);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BowerLibraryInfo other = (BowerLibraryInfo) obj;
            return name.equalsIgnoreCase(other.name);
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
