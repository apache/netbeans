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

package org.netbeans.modules.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import static org.netbeans.modules.project.ui.Bundle.*;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUIUtils;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Support for creating logical views.
 * @author Jesse Glick, Petr Hrebejk
 */
public class PhysicalView {

    private PhysicalView() {}

    private static final Logger LOG = Logger.getLogger(PhysicalView.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(PhysicalView.class);

    private static final class GroupNodeInfo {
        public final boolean isProjectDir;
        public GroupNodeInfo(boolean isProjectDir) {
            this.isProjectDir = isProjectDir;
        }
    }
        
    public static boolean isProjectDirNode( Node n ) {
        GroupNodeInfo i = n.getLookup().lookup(GroupNodeInfo.class);
        return i != null && i.isProjectDir;
    }
    
    public static Node[] createNodesForProject( Project p ) {
        Sources s = ProjectUtils.getSources(p);
        SourceGroup[] groups = s.getSourceGroups(Sources.TYPE_GENERIC);                
        final List<Node> nodesList = new ArrayList<>( groups.length );        
        for (SourceGroup group : groups) {
            final Node n = createNodeForSourceGroup(group, p);
            if (n != null) {
                nodesList.add(n);
            }
        }
        Node nodes[] = new Node[ nodesList.size() ];
        nodesList.toArray( nodes );
        return nodes;
    }
    
    @CheckForNull
    static Node createNodeForSourceGroup(
            @NonNull final SourceGroup group,
            @NonNull final Project project) {
        if ("sharedlibraries".equals(group.getName())) { //NOI18N
            //HACK - ignore shared libs group in UI, it's only useful for version control commits.
            return null;
        }
        final FileObject rootFolder = group.getRootFolder();
        if (!rootFolder.isValid() || !rootFolder.isFolder()) {
            return null;
        }
        final FileObject projectDirectory = project.getProjectDirectory();
        return new ProjectIconNode(new GroupNode(
                project,
                group,
                projectDirectory.equals(rootFolder) || FileUtil.isParentOf(rootFolder, projectDirectory),
                DataFolder.findFolder(rootFolder)),
                true);
    }
   
    static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter, DataFilter.FileBased {
        
        private final ChangeSupport changeSupport = new ChangeSupport( this );
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener( this );
        }
                
        public @Override boolean acceptDataObject(DataObject obj) {
            return acceptFileObject(obj.getPrimaryFile());
        }
        
        public @Override void stateChanged(ChangeEvent e) {
            final Runnable r = new Runnable () {
                public @Override void run() {
                    changeSupport.fireChange();
                }
            };            
            SwingUtilities.invokeLater(r);            
        }        
    
        public @Override void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener( listener );
        }        
                        
        public @Override void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener( listener );
        }

        public @Override boolean acceptFileObject(FileObject fo) {
            return VisibilityQuery.getDefault().isVisible(fo);
        }
        
    }
    
    static final class GroupNode extends FilterNode implements PropertyChangeListener {
        
        private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();
        
        private ProjectInformation pi;
        private SourceGroup group;
        private boolean isProjectDir;
        private Boolean initialized;
        private final Node projectDelegateNode;

        public GroupNode(Project project, SourceGroup group, boolean isProjectDir, DataFolder dataFolder ) {
            super( dataFolder.getNodeDelegate(),
                   dataFolder.createNodeChildren( VISIBILITY_QUERY_FILTER ),                       
                   createLookup(project, group, dataFolder, isProjectDir));

            this.pi = ProjectUtils.getInformation( project );
            this.group = group;
            this.isProjectDir = isProjectDir;
            
            if(isProjectDir) {
                LogicalViewProvider lvp = project.getLookup().lookup(LogicalViewProvider.class);
                // used to retrieve e.g. actions in case of a folder representing a project,
                // so that a projects context menu is the same is in a logical view
                this.projectDelegateNode = lvp != null ? lvp.createLogicalView() : null;
            } else {
                this.projectDelegateNode = null;
            }
            
            pi.addPropertyChangeListener(WeakListeners.propertyChange(this, pi));
            group.addPropertyChangeListener( WeakListeners.propertyChange( this, group ) );
        }

        private boolean initialized() {
            synchronized (RP) {
                if (initialized != null) {
                    return initialized;
                } else {
                    initialized = false;
                    RP.post(new Runnable() {
                        @Override public void run() {
                            pi.getDisplayName();
                            synchronized (RP) {
                                initialized = true;
                            }
                            fireNameChange(null, null);
                            fireDisplayNameChange(null, null);
                        }
                    });
                    return false;
                }
            }
        }

        // XXX May need to change icons as well
        
        public @Override String getName() {
            if (isProjectDir && initialized()) {
                return pi.getName();
            }
            else {
                String n = group.getName();
                if (n == null) {
                    n = "???"; // NOI18N
                    LOG.log(Level.WARNING, "SourceGroup impl of type {0} specified a null getName(); this is illegal", group.getClass().getName());
                }
                return n;
            }
        }

        @Messages({"# {0} - display name of the group", "# {1} - display name of the project", "# {2} - original name of the folder", "FMT_PhysicalView_GroupName={1} - {0}"})
        public @Override String getDisplayName() {
            if ( isProjectDir ) {
                return initialized() ? pi.getDisplayName() : group.getDisplayName();
            }
            else {
                return FMT_PhysicalView_GroupName(group.getDisplayName(), pi.getDisplayName(), getOriginal().getDisplayName());
            }
        }

        @Messages({"HINT_project=Project in {0}", "HINT_group=Source folder in {0}"})
        public @Override String getShortDescription() {
            FileObject gdir = group.getRootFolder();
            String dir = FileUtil.getFileDisplayName(gdir);
            return isProjectDir ? HINT_project(dir) : HINT_group(dir);
        }

        public @Override boolean canRename() {
            return false;
        }

        @Override public Node.PropertySet[] getPropertySets() {
            return new Node.PropertySet[0];
        }

        public @Override boolean canCut() {
            return false;
        }

        public @Override boolean canCopy() {
            // At least for now.
            return false;
        }

        public @Override boolean canDestroy() {
            return false;
        }

        public @Override Action[] getActions(boolean context) {

            if ( context ) {
                return super.getActions( true );
            }
            else { 
                Action[] folderActions = super.getActions( false );
                Action[] projectActions;
                
                if ( isProjectDir ) {
                    if( projectDelegateNode != null ) {
                        projectActions = projectDelegateNode.getActions( false );
                    }
                    else {
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
                }
                else {
                    projectActions = folderActions;
                }
                
                return projectActions;
            }                                            
        }

        // Private methods -------------------------------------------------    

        public @Override void propertyChange(final PropertyChangeEvent evt) {
            final Runnable r = new Runnable () {
                public @Override void run() {
                    String prop = evt.getPropertyName();
                    boolean ok = false;
                    if (prop == null || ProjectInformation.PROP_DISPLAY_NAME.equals(prop)) {
                        fireDisplayNameChange(null, null);
                        ok = true;
                    }
                    if (prop == null || ProjectInformation.PROP_NAME.equals(prop)) {
                        fireNameChange(null, null);
                        ok = true;
                    }

                    if (prop == null || ProjectInformation.PROP_ICON.equals(prop)) {
                        // OK, ignore
                        ok = true;
                    }

                    if (prop == null || "name".equals(prop) ) { // NOI18N
                        fireNameChange(null, null);
                        ok = true;
                    }

                    if (prop == null || "displayName".equals(prop) ) { // NOI18N
                        fireDisplayNameChange(null, null);
                        ok = true;
                    }

                    if (prop == null || "icon".equals(prop) ) { // NOI18N
                        // OK, ignore
                        ok = true;
                    }

                    if (prop == null || "rootFolder".equals(prop) ) { // NOI18N
                        // XXX Do something to children and lookup 
                        fireNameChange(null, null);
                        fireDisplayNameChange(null, null);
                        fireShortDescriptionChange(null, null);
                        ok = true;
                    }

                    if (prop == null || SourceGroup.PROP_CONTAINERSHIP.equals(prop)) {
                        // OK, ignore
                        ok = true;
                    }

                    if (!ok) {
                        assert false : "Attempt to fire an unsupported property change event from " + pi.getClass().getName() + ": " + prop;
                    }
                }
            };            
            SwingUtilities.invokeLater(r);            
        }
        
        private static Lookup createLookup(Project p, SourceGroup group, DataFolder dataFolder, boolean isProjectDir) {
            return new ProxyLookup(
                dataFolder.getNodeDelegate().getLookup(),
                Lookups.fixed(p, new PathFinder(group), new GroupNodeInfo(isProjectDir)),
                p.getLookup());
        }

    }

    static final class ProjectIconNode extends FilterNode implements NodeListener { // #194068
        private final boolean root;
        public ProjectIconNode(Node orig, boolean root) {
            super(orig, orig.isLeaf() ? Children.LEAF : new ProjectBadgingChildren(orig));
            this.root = root;
            setValue("VCS_PHYSICAL", Boolean.TRUE); //#159543 
            addNodeListener(this);
        }

        @Override
        protected NodeListener createNodeListener() {
            return new NodeAdapter(this) {
                @Override
                protected void propertyChange(FilterNode fn, PropertyChangeEvent ev) {
                    super.propertyChange(fn, ev);
                    if (Node.PROP_LEAF.equals(ev.getPropertyName())) {
                        Node orig = getOriginal();
                        setChildren(orig.isLeaf() ? Children.LEAF : new ProjectBadgingChildren(orig));
                    }
                }
            };
        }
        
        public @Override Image getIcon(int type) {
            return swap(super.getIcon(type), type);
        }
        public @Override Image getOpenedIcon(int type) {
            return swap(super.getOpenedIcon(type), type);
        }
        private Image swap(Image base, int type) {
            if (!root) { // do not use icon on root node in Files tab
                FileObject folder = getOriginal().getLookup().lookup(FileObject.class);
                if (folder != null && folder.isFolder()) {
                    ProjectManager.Result r = ProjectManager.getDefault().isProject2(folder);
                    if (r != null) {
                        Icon icon = r.getIcon();
                        
                        if (icon != null) {
                            Image img = ImageUtilities.icon2Image(icon);
                            try {
                                //#217008
                                DataFolder df = getOriginal().getLookup().lookup(DataFolder.class);
                                img = FileUIUtils.getImageDecorator(folder.getFileSystem()).annotateIcon(img, type, df.files());
                            } catch (FileStateInvalidException e) {
                                // no fs, do nothing
                            }
                            return img;
                        }
                    }
                }
            }
            return base;
        }
        public @Override String getShortDescription() {
            FileObject folder = getOriginal().getLookup().lookup(FileObject.class);
            if (folder != null && folder.isFolder()) {
                try {
                    Project p = ProjectManager.getDefault().findProject(folder);
                    if (p != null) {
                        return ProjectUtils.getInformation(p).getDisplayName();
                    }
                } catch (IOException x) {
                    LOG.log(Level.FINE, null, x);
                }
            }
            return super.getShortDescription();
        }

        @Override
        public void childrenAdded(NodeMemberEvent ev) {
        }

        @Override
        public void childrenRemoved(NodeMemberEvent ev) {
        }

        @Override
        public void childrenReordered(NodeReorderEvent ev) {
        }

        @Override
        public void nodeDestroyed(NodeEvent ev) {
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
        }
    }
    private static final class ProjectBadgingChildren extends FilterNode.Children {
        public ProjectBadgingChildren(Node orig) {
            super(orig);
        }
        protected @Override Node copyNode(Node orig) {
            return new ProjectIconNode(orig, false);
        }
    }
    
    public static class PathFinder {
        
        private SourceGroup group;
        
        public PathFinder( SourceGroup group ) {
            this.group = group;
        }
        
        public Node findPath( Node root, Object object ) {
                 
            if ( !( object instanceof FileObject ) ) {
                return null;
            }
            
            FileObject fo = (FileObject)object;        
            FileObject groupRoot = group.getRootFolder();
            if ( FileUtil.isParentOf( groupRoot, fo ) /* && group.contains( fo ) */ ) {
                // The group contains the object

                String relPath = FileUtil.getRelativePath( groupRoot, fo );
                
                ArrayList<String> path = new ArrayList<String>();
                StringTokenizer strtok = new StringTokenizer( relPath, "/" );
                while( strtok.hasMoreTokens() ) {
                   path.add( strtok.nextToken() );
                }
                
                if (path.size() > 0) {
                    path.remove(path.size() - 1);
                } else {
                    return null;
                }
                try {
                    //#75205
                    Node parent = NodeOp.findPath( root, Collections.enumeration( path ) );
                    if (parent != null) {
                        //not nice but there isn't a findNodes(name) method.
                        Node[] nds = parent.getChildren().getNodes(true);
                        for (int i = 0; i < nds.length; i++) {
                            FileObject dobj = nds[i].getLookup().lookup(FileObject.class);
                            if (dobj != null && fo.equals(dobj)) {
                                return nds[i];
                            }
                        }
                        String name = fo.getName();
                        try {
                            DataObject dobj = DataObject.find( fo );
                            name = dobj.getNodeDelegate().getName();
                        } catch (DataObjectNotFoundException ex) {
                        }
                        return parent.getChildren().findChild(name);
                    }
                }
                catch ( NodeNotFoundException e ) {
                    return null;
                }
            }   
            else if ( groupRoot.equals( fo ) ) {
                return root;
            }

            return null;
        }
                    
    }
    
}
