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
package org.netbeans.modules.cnd.editor.filecreation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
//import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Support for creating logical views.
 */
public class PhysicalView {

    public static boolean isProjectDirNode(Node n) {
        return n instanceof GroupNode && ((GroupNode) n).isProjectDir;
    }

    public static Node[] createNodesForProject(Project p) {
        Sources s = ProjectUtils.getSources(p);
        SourceGroup[] groups = s.getSourceGroups(Sources.TYPE_GENERIC);

        FileObject projectDirectory = p.getProjectDirectory();
        SourceGroup projectDirGroup = null;

        // First find the source group which will represent the project
        for (int i = 0; i < groups.length; i++) {
            FileObject groupRoot = groups[i].getRootFolder();
            if (projectDirectory.equals(groupRoot) ||
                    FileUtil.isParentOf(groupRoot, projectDirectory)) {
                if (projectDirGroup != null) {
                    // more than once => Illegal
                    projectDirGroup = null;
                    break;
                } else {
                    projectDirGroup = groups[i];
                }
            }
        }

        if (projectDirGroup == null) {
            // Illegal project
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Project " + p + // NOI18N
                    "either does not contain it's project directory under the " + // NOI18N
                    "Generic source groups or the project directory is under " + // NOI18N
                    "more than one source group");                                  // NOI18N
            return new Node[0];
        }


        // Create the nodes
        ArrayList<Node> nodesList = new ArrayList<Node>(groups.length);
        nodesList.add(/*new GroupContainmentFilterNode(*/new GroupNode(p, projectDirGroup, true, DataFolder.findFolder(projectDirGroup.getRootFolder()))/*, projectDirGroup)*/);

        for (int i = 0; i < groups.length; i++) {

            if (groups[i] == projectDirGroup) {
                continue;
            }

            nodesList.add(/*new GroupContainmentFilterNode(*/new GroupNode(p, groups[i], false, DataFolder.findFolder(groups[i].getRootFolder()))/*, groups[i])*/);
        }

        Node nodes[] = new Node[nodesList.size()];
        nodesList.toArray(nodes);
        return nodes;
    }

    private static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {

        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener(this);
        }

        @Override
        public boolean acceptDataObject(DataObject obj) {
            FileObject fo = obj.getPrimaryFile();
            return VisibilityQuery.getDefault().isVisible(fo);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            changeSupport.fireChange();
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }
    }

    static final class GroupNode extends FilterNode implements PropertyChangeListener {

        private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();
        static final String GROUP_NAME_PATTERN = NbBundle.getMessage(
                PhysicalView.class, "FMT_PhysicalView_GroupName"); // NOI18N
        private final ProjectInformation pi;
        private final SourceGroup group;
        private boolean isProjectDir;

        public GroupNode(Project project, SourceGroup group, boolean isProjectDir, DataFolder dataFolder) {
            super(dataFolder.getNodeDelegate(),
                    dataFolder.createNodeChildren(VISIBILITY_QUERY_FILTER),
                    createLookup(project, group, dataFolder));

            this.pi = ProjectUtils.getInformation(project);
            this.group = group;
            this.isProjectDir = isProjectDir;
            pi.addPropertyChangeListener(WeakListeners.propertyChange(this, pi));
            group.addPropertyChangeListener(WeakListeners.propertyChange(this, group));
        }

        // XXX May need to change icons as well
        @Override
        public String getName() {
            if (isProjectDir) {
                return pi.getName();
            } else {
                String n = group.getName();
                if (n == null) {
                    n = "???"; // NOI18N
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "SourceGroup impl of type " + group.getClass().getName() + " specified a null getName(); this is illegal"); // NOI18N
                }
                return n;
            }
        }

        @Override
        public String getDisplayName() {
            if (isProjectDir) {
                return pi.getDisplayName();
            } else {
                return MessageFormat.format(GROUP_NAME_PATTERN,
                        new Object[]{group.getDisplayName(), pi.getDisplayName(), getOriginal().getDisplayName()                });
            }
        }

        @Override
        public String getShortDescription() {
            FileObject gdir = group.getRootFolder();
            String dir = FileUtil.getFileDisplayName(gdir);
            return NbBundle.getMessage(PhysicalView.class,
                    isProjectDir ? "HINT_project" : "HINT_group", // NOI18N
                    dir);
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public boolean canCut() {
            return false;
        }

        @Override
        public boolean canCopy() {
            // At least for now.
            return false;
        }

        @Override
        public boolean canDestroy() {
            return false;
        }

        /* public Action[] getActions( boolean context ) {

        if ( context ) {
        return super.getActions( true );
        }
        else {
        Action[] folderActions = super.getActions( false );
        Action[] projectActions;

        if ( isProjectDir ) {
        // If this is project dir then the properties action
        // has to be replaced to invoke project customizer
        projectActions = new Action[ folderActions.length ];
        for ( int i = 0; i < folderActions.length; i++ ) {
        if ( folderActions[i] instanceof org.openide.actions.PropertiesAction ) {
        projectActions[i] = CommonProjectActions.customizeProjectAction();
        }
        else {
        projectActions[i] = folderActions[i];
        }
        }
        }
        else {
        projectActions = folderActions;
        }

        return projectActions;
        }
        }*/

        // Private methods -------------------------------------------------
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();
            if (ProjectInformation.PROP_DISPLAY_NAME.equals(prop)) {
                fireDisplayNameChange(null, null);
            } else if (ProjectInformation.PROP_NAME.equals(prop)) {
                fireNameChange(null, null);
            } else if (ProjectInformation.PROP_ICON.equals(prop)) {
                // OK, ignore
            } else if ("name".equals(prop)) { // NOI18N
                fireNameChange(null, null);
            } else if ("displayName".equals(prop)) { // NOI18N
                fireDisplayNameChange(null, null);
            } else if ("icon".equals(prop)) { // NOI18N
                // OK, ignore
            } else if ("rootFolder".equals(prop)) { // NOI18N
                // XXX Do something to children and lookup
                fireNameChange(null, null);
                fireDisplayNameChange(null, null);
                fireShortDescriptionChange(null, null);
            } else if (SourceGroup.PROP_CONTAINERSHIP.equals(prop)) {
                // OK, ignore
            } else {
                assert false : "Attempt to fire an unsupported property change event from " + pi.getClass().getName() + ": " + prop;
            }
        }

        private static Lookup createLookup(Project p, SourceGroup group, DataFolder dataFolder) {
            return new ProxyLookup(new Lookup[]{
                dataFolder.getNodeDelegate().getLookup(),
                Lookups.fixed(new Object[]{p, new PathFinder(group)        }),
                p.getLookup(),
            });
        }
    }

    /* XXX disabled for now pending resolution of interaction with planned VCS annotations (color only):
    /**
     * Specially displays nodes corresponding to files which are not contained in this source group.
     * /
    private static final class GroupContainmentFilterNode extends FilterNode {

    private final SourceGroup g;

    public GroupContainmentFilterNode(Node orig, SourceGroup g) {
    super(orig, orig.isLeaf() ? Children.LEAF : new GroupContainmentFilterChildren(orig, g));
    this.g = g;
    }

    public String getHtmlDisplayName() {
    Node orig = getOriginal();
    DataObject d = (DataObject) orig.getCookie(DataObject.class);
    assert d != null : orig;
    FileObject f = d.getPrimaryFile();
    String barename = orig.getHtmlDisplayName();
    if (!FileUtil.isParentOf(g.getRootFolder(), f) || g.contains(f)) {
    // Leave it alone.
    return barename;
    }
    // Try to grey it out.
    if (barename == null) {
    try {
    barename = XMLUtil.toElementContent(orig.getDisplayName());
    } catch (CharConversionException e) {
    // Never mind.
    return null;
    }
    }
    return "<font color='!Label.disabledForeground'>" + barename + "</font>"; // NOI18N
    }

    private static final class GroupContainmentFilterChildren extends FilterNode.Children {

    private final SourceGroup g;

    public GroupContainmentFilterChildren(Node orig, SourceGroup g) {
    super(orig);
    this.g = g;
    }

    protected Node copyNode(Node node) {
    if (original.getCookie(DataFolder.class) != null && node.getCookie(DataObject.class) != null) {
    return new GroupContainmentFilterNode(node, g);
    } else {
    return super.copyNode(node);
    }
    }

    }

    }
     */
    public static class PathFinder {

        private final SourceGroup group;

        public PathFinder(SourceGroup group) {
            this.group = group;
        }

        public Node findPath(Node root, Object object) {

            if (!(object instanceof FileObject)) {
                return null;
            }

            FileObject fo = (FileObject) object;
            FileObject groupRoot = group.getRootFolder();
            if (FileUtil.isParentOf(groupRoot, fo) /* && group.contains( fo ) */) {
                // The group contains the object

                String relPath = FileUtil.getRelativePath(groupRoot, fo);

                ArrayList<String> path = new ArrayList<String>();
                StringTokenizer strtok = new StringTokenizer(relPath, "/"); // NOI18N
                while (strtok.hasMoreTokens()) {
                    path.add(strtok.nextToken());
                }

                if (path.size() > 0) {
                    path.remove(path.size() - 1);
                } else {
                    return null;
                }
                try {
                    //#75205
                    Node parent = NodeOp.findPath(root, Collections.enumeration(path));
                    if (parent != null) {
                        //not nice but there isn't a findNodes(name) method.
                        Node[] nds = parent.getChildren().getNodes(true);
                        for (int i = 0; i < nds.length; i++) {
                            DataObject dobj = nds[i].getLookup().lookup(DataObject.class);
                            if (dobj != null && fo.equals(dobj.getPrimaryFile())) {
                                return nds[i];
                            }
                        }
                        String name = fo.getName();
                        try {
                            DataObject dobj = DataObject.find(fo);
                            name = dobj.getNodeDelegate().getName();
                        } catch (DataObjectNotFoundException ex) {
                        }
                        return parent.getChildren().findChild(name);
                    }
                } catch (NodeNotFoundException e) {
                    return null;
                }
            } else if (groupRoot.equals(fo)) {
                return root;
            }

            return null;
        }
    }
}
