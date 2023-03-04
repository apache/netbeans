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

package org.netbeans.modules.versioning.util.projects;

import java.awt.BorderLayout;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * JPanel showing project nodes
 * @author Ondra Vrabec
 */
class ProjectsView extends JPanel implements ExplorerManager.Provider, VetoableChangeListener {

    private final AbstractNode rootNode;
    private final ExplorerManager em;
    private final ProjectOutlineView view;
    private Node[] selectedNodes;
    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; //NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; //NOI18N
    private static Image FOLDER_ICON;

    /**
     *
     * @param projectsToBeOpened projects to showm, root projects are under null key
     */
    public ProjectsView(final Map<Project, Set<Project>> projectsToBeOpened) {
        rootNode = new AbstractNode(new ProjectsChildren(projectsToBeOpened));
        em = new ExplorerManager();
        view = new ProjectOutlineView();
        
        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);
        em.addVetoableChangeListener(this);
    }

    public ExplorerManager getExplorerManager() {
        return em;
    }

    /**
     * Adds l to listeners collection. These listeners are notified when project nodes selection is changed.
     * @param l
     */
    public void addSelectionChangeListener (PropertyChangeListener l) {
        em.addPropertyChangeListener(l);
    }

    /**
     * Removes the listener
     * @param l
     */
    public void removeSelectionChangeListener (PropertyChangeListener l) {
        em.removePropertyChangeListener(l);
    }

    public Set<Project> getSelectedProjects () {
        HashSet<Project> projects = new HashSet<Project>();
        Node[] nodes = selectedNodes;
        // each selected project is returned
        for (Node node : nodes) {
            Project p = node.getLookup().lookup(Project.class);
            if (p != null) {
                projects.add(p);
            }
        }
        return projects;
    }

    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            // vetos wrong selection
            Node[] nodes = (Node[]) evt.getNewValue();
            for (Node node : nodes) {
                if (!(node instanceof ProjectNode)) {
                    // only project nodes can be selected, not the root abstract node
                    throw new PropertyVetoException("", evt); // NOI18N
                }
            }
            if (nodes.length > 0) {
                this.selectedNodes = nodes;
            }
        }
    }

    /**
     * Outline view showing project nodes with their subprojects
     */
    private class ProjectOutlineView extends OutlineView {
        public ProjectOutlineView() {
            getOutline().setShowHorizontalLines(true);
            getOutline().setShowVerticalLines(false);
            getOutline().setRootVisible(false);
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            setBorder(UIManager.getBorder("Nb.ScrollPane.border")); // NOI18N
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ProjectsView.class, "ACSD_ProjectOutlineView")); // NOI18N
            getAccessibleContext().setAccessibleName(NbBundle.getMessage(ProjectsView.class, "ACSN_ProjectOutlineView")); // NOI18N
            setPopupAllowed(true);
            setupColumns();
            setDefaultColumnSizes();
        }

        private void setupColumns() {
            Node.Property[] columns = new Node.Property[1];
            String columnName = NbBundle.getMessage(ProjectsView.class, "LBL_Path");
            String columnDesc = NbBundle.getMessage(ProjectsView.class, "LBL_PathDesc");
            columns[0] = new ColumnDescriptor<String>(ProjectNode.PROPERTY_NAME_PATH, String.class, columnName, columnDesc);  // NOI18N

            setProperties(columns);
        }

        private void setDefaultColumnSizes() {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    int width = getWidth();
                    getOutline().getColumnModel().getColumn(0).setPreferredWidth(width * 40 / 100);
                    getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 60 / 100);
                }
            });
        }

        @Override
        public void addNotify() {
            ExplorerManager em = ExplorerManager.find(this);
            em.setRootContext(rootNode);
            em.setExploredContext(rootNode);
            expandNode(rootNode);
            super.addNotify();
        }
    }

    private static class ColumnDescriptor<T> extends PropertySupport.ReadOnly<T> {

        public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public T getValue() {
            return null;
        }
    }
    
    private static Image getFolderIcon () {
        if (FOLDER_ICON == null) {
            Icon baseIcon = UIManager.getIcon(ICON_KEY_UIMANAGER);
            Image base;
            if (baseIcon != null) {
                base = ImageUtilities.icon2Image(baseIcon);
            } else {
                base = (Image) UIManager.get(ICON_KEY_UIMANAGER_NB);
                if (base == null) { // fallback to our owns
                    base = ImageUtilities.loadImage("org/openide/loaders/defaultFolder.gif"); //NOI18N
                }
            }
            FOLDER_ICON = base;
        }
        return FOLDER_ICON;
    }

    /**
     * Children.Keys extension with ProjectNode' children, contains another ProjectNodes
     */
    private class ProjectsChildren extends Children.Keys<ProjectNode> {

        private java.util.Map<Project, Set<Project>> projects;

        /**
         * Map of all visible projects sorted by their parents
         * @param projects
         */
        public ProjectsChildren (java.util.Map<Project, Set<Project>> projects) {
            this.projects = projects;
        }

        @Override
        protected Node[] createNodes(ProjectNode key) {
            return new ProjectNode[]{key};
        }

        @Override
        protected void addNotify() {
            refreshKeys();
        }

        /**
         * Calculates nodes children. No need to call outside of AWT
         */
        public void refreshKeys() {
            Project parent = getNode().getLookup().lookup(Project.class);
            Set<Project> childrenProjects = projects.get(parent);
            LinkedList<ProjectNode> keys = new LinkedList<ProjectNode>();
            for (Project p : childrenProjects) {
                keys.add(new ProjectNode(p, projects.get(p).size() == 0 ? LEAF : new ProjectsChildren(projects)));
            }
            setKeys(keys);
        }
    }

    /**
     * Project node
     */
    private final class ProjectNode extends AbstractNode {
        static final String PROPERTY_NAME_PATH = "path";    // NOI18N
        private final ProjectInformation info;

        public ProjectNode(Project p, Children children) {
            super(children, Lookups.singleton(p));
            info = ProjectUtils.getInformation(p);

            setProperties();
        }

        private void setProperties () {
            Sheet sheet = Sheet.createDefault();
            Sheet.Set ps = Sheet.createPropertiesSet();
            ps.put(new LocalPathProperty());
            sheet.put(ps);
            setSheet(sheet);
        }

        @Override
        public Image getIcon(int type) {
            Icon icon = info.getIcon();
            if (icon == null) {
                Logger.getLogger(ProjectsView.class.getName()).log(Level.WARNING, "Null project icon for {0}:{1}", //NOI18N
                        new Object[] { info.getDisplayName(), info.getProject() });
                return getFolderIcon();
            } else {
                return ImageUtilities.icon2Image(icon);
            }
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
            return info.getDisplayName();
        }

        @Override
        public String getName() {
            return info.getName();
        }

        /**
         * Local path to the project
         */
        private class LocalPathProperty extends PropertySupport.ReadOnly<String> {

            public LocalPathProperty() {
                super(PROPERTY_NAME_PATH, String.class, PROPERTY_NAME_PATH, PROPERTY_NAME_PATH);
            }

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return getLookup().lookup(Project.class).getProjectDirectory().getPath();
            }
        }
    }
}
