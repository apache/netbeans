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

package org.netbeans.modules.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectIconAnnotator;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUIUtils;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import static org.openide.nodes.Node.PROP_DISPLAY_NAME;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Union2;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;

/** Root node for list of open projects
 */
public class ProjectsRootNode extends AbstractNode {

    private static final Logger LOG = Logger.getLogger(ProjectsRootNode.class.getName());
    private static final Set<ProjectsRootNode> all = new WeakSet<ProjectsRootNode>();
    private static final RequestProcessor RP = new RequestProcessor(ProjectsRootNode.class);

    static final int PHYSICAL_VIEW = 0;
    static final int LOGICAL_VIEW = 1;
        
    private static final @StaticResource String ICON_BASE = "org/netbeans/modules/project/ui/resources/projectsRootNode.gif"; //NOI18N
    public static final String ACTIONS_FOLDER = "ProjectsTabActions"; // NOI18N
    public static final String ACTIONS_FOLDER_PHYSICAL = "FilesTabActions";

    private ResourceBundle bundle;
    private final int type;
    
    public ProjectsRootNode( int type ) {
        super(new ProjectChildren(type), /* for CollapseAll */Lookups.singleton(type == LOGICAL_VIEW ? ProjectTab.ID_LOGICAL : ProjectTab.ID_PHYSICAL));
        setIconBaseWithExtension( ICON_BASE );
        this.type = type;
        synchronized(all){
            all.add(this);
        }
    }
        
    @Override
    public String getName() {
        return ( "OpenProjects" ); // NOI18N
    }
    
    @Override
    public String getDisplayName() {
        if ( this.bundle == null ) {
            this.bundle = NbBundle.getBundle( ProjectsRootNode.class );
        }
        return bundle.getString( "LBL_OpenProjectsNode_Name" ); // NOI18N
    }
    
    @Override
    public boolean canRename() {
        return false;
    }
        
    @Override
    public Node.Handle getHandle() {        
        return new Handle(type);
    }
    
    @Override
    public Action[] getActions( boolean context ) {
        if (context) { // XXX why?
            return new Action[0];
        } else {
            List<? extends Action> actions = Utilities.actionsForPath(type == PHYSICAL_VIEW ? ACTIONS_FOLDER_PHYSICAL : ACTIONS_FOLDER);
            return actions.toArray(new Action[0]);
        }
    }
    
    /** Finds node for given object in the view
     * @return the node or null if the node was not found
     */
    Node findNode(FileObject target) {        

        ProjectChildren ch = (ProjectChildren)getChildren();

        assert ((ch.type == LOGICAL_VIEW) || (ch.type == PHYSICAL_VIEW));
        // Speed up search in case we have an owner project - look in its node first.
        Project ownerProject = findProject(target);
        final SelectInProjectFileOwnerQueryImpl foq = SelectInProjectFileOwnerQueryImpl.getInstance();
        if (foq != null) {
            foq.setCurrentProject(target, ownerProject);
        }
        try {
            for (int lookOnlyInOwnerProject = (ownerProject != null) ? 0 : 1; lookOnlyInOwnerProject < 2; lookOnlyInOwnerProject++) {
                for (Node node : ch.getNodes(true)) {
                    Project p = node.getLookup().lookup(Project.class);
                    assert p != null : "Should have had a Project in lookup of " + node;
                    if (lookOnlyInOwnerProject == 0 && p != ownerProject) {
                        continue; // but try again (in next outer loop) as a fallback
                    }
                    Node n = null;
                    LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
                    if (lvp != null) {
                        // XXX (cf. #63554): really should be calling this on DataObject usually, since
                        // DataNode does *not* currently have a FileObject in its lookup (should it?)
                        // ...but it is not clear who has implemented findPath to assume FileObject!
                        n = lvp.findPath(node, target);
                    }
                    if (n == null && ch.type == PHYSICAL_VIEW) {
                        PhysicalView.PathFinder pf = node.getLookup().lookup(PhysicalView.PathFinder.class);
                        if ( pf != null ) {
                            n = pf.findPath(node, target);
                        }
                    }
                    if ( n != null ) {
                        return n;
                    }
                }
            }
        } finally {
            if (foq != null) {
                foq.clearCurrentProject();
            }
        }
        return null;
    }

    static void checkNoLazyNode() {
        synchronized(all){
            for (ProjectsRootNode root : all) {
                checkNoLazyNode(root.getChildren());
            }
        }
    }
    static void checkNoLazyNode(Children children) {
        for (Node n : children.getNodes()) {
            if (n instanceof BadgingNode) {
                ((BadgingNode)n).replaceProject(null);
            }

            if (n.getLookup().lookup(LazyProject.class) != null) {
                OpenProjectList.LOGGER.warning("LazyProjects remain visible");
            }
        }
    }

    @CheckForNull
    private static Project findProject(@NonNull final FileObject target) {
        Project owner = FileOwnerQuery.getOwner(target);
        if (owner != null && ProjectConvertors.isConvertorProject(owner)) {
            FileObject dir = owner.getProjectDirectory().getParent();
            while (dir != null) {
                Project p = FileOwnerQuery.getOwner(dir);
                if (p != null && !ProjectConvertors.isConvertorProject(p)) {
                    owner = p;
                    break;
                }
                dir = dir.getParent();
            }
        }
        return owner;
    }

    private static class Handle implements Node.Handle {

        private static final long serialVersionUID = 78374332058L;
        
        private final int viewType;
        
        public Handle( int viewType ) {
            this.viewType = viewType;
        }
        
        @Override
        public Node getNode() {
            return new ProjectsRootNode( viewType );
        }
        
    }
       
    
    // However project rename is currently disabled so it is not a big deal
    static class ProjectChildren extends Children.Keys<ProjectChildren.Pair> implements ChangeListener, PropertyChangeListener, NodeListener {

        static final RequestProcessor RP = new RequestProcessor(ProjectChildren.class);

        private final java.util.Map <Sources,Reference<Project>> sources2projects = new WeakHashMap<Sources,Reference<Project>>();
        //@GuardedBy("projects2Pairs")
        private final java.util.Map <Project,Reference<Pair>> projects2Pairs = Collections.synchronizedMap(new WeakHashMap<>());
        
        final int type;
        
        public ProjectChildren( int type ) {
            this.type = type;            
        }
        
        // Children.Keys impl --------------------------------------------------
        
        @Override
        public void addNotify() {   
            OpenProjectList.getDefault().addPropertyChangeListener(this);              
            if (Boolean.getBoolean("test.projectnode.sync")) {
                setKeys( getKeys());
            } else {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        setKeys( getKeys() );
                    }
                });
            }
        }

        @Override
        public void removeNotify() {
            OpenProjectList.getDefault().removePropertyChangeListener(this);
            for (Sources sources : sources2projects.keySet()) {
                sources.removeChangeListener( this );                
            }
            sources2projects.clear();
            projects2Pairs.clear();
            setKeys(Collections.<Pair>emptySet());
        }

        @Override
        public int getNodesCount(boolean optimalResult) {
            if (optimalResult) {
                setKeys(getKeys());
            }
            return super.getNodesCount(optimalResult);
        }


        
        @Override
        protected Node[] createNodes(Pair p) {
            Project project = p.project;
            
            Node origNodes[] = null;
            boolean[] projectInLookup = new boolean[1];
            projectInLookup[0] = true;
                        
            if (type == PHYSICAL_VIEW) {
                final Sources sources = p.data.second().first();
                final SourceGroup[] groups = p.data.second().second();
                sources.removeChangeListener( this );
                sources.addChangeListener( this );
                sources2projects.put( sources, new WeakReference<Project>( project ) );
                final List<Node> nodes = new ArrayList<>(groups.length);
                for (SourceGroup group : groups) {
                    final Node n = PhysicalView.createNodeForSourceGroup(group, project);
                    if (n != null) {
                        nodes.add(n);
                    }
                }
                origNodes = nodes.toArray(new Node[0]);
            } else {
                assert type == LOGICAL_VIEW;
                origNodes = new Node[] {
                    logicalViewForProject(
                            project,
                            p.data,
                            projectInLookup)
                };
            }

            Node[] badgedNodes = new Node[ origNodes.length ];
            for( int i = 0; i < origNodes.length; i++ ) {
                if ( type == PHYSICAL_VIEW && !PhysicalView.isProjectDirNode( origNodes[i] ) ) {
                    // Don't badge external sources
                    badgedNodes[i] = origNodes[i];
                }
                else {
                    badgedNodes[i] = new BadgingNode(
                        this,
                        p,
                        origNodes[i],
                        type == LOGICAL_VIEW
                    );
                }
            }
                        
            return badgedNodes;
        }        
        
        @NonNull
        final Node logicalViewForProject(
                @NonNull final Project project,
                final Union2<LogicalViewProvider,org.openide.util.Pair<Sources,SourceGroup[]>> data,
                final boolean[] projectInLookup) {
            Node node;            
            if (!data.hasFirst()) {
                LOG.log(
                        Level.WARNING,
                        "Warning - project of {0} in {1} doesn't supply a LogicalViewProvider in its lookup",  // NOI18N
                        new Object[]{
                            project.getClass(),
                            FileUtil.getFileDisplayName(project.getProjectDirectory())
                        });
                final Sources sources = data.second().first();
                final SourceGroup[] groups = data.second().second();
                sources.removeChangeListener(this);
                sources.addChangeListener(this);
                if (groups.length > 0) {
                    node = PhysicalView.createNodeForSourceGroup(groups[0], project);
                } else {
                    node = Node.EMPTY;
                }
            } else {
                final LogicalViewProvider lvp = data.first();
                node = lvp.createLogicalView();
                if (!project.equals(node.getLookup().lookup(Project.class))) {
                    // Various actions, badging, etc. are not going to work.
                    LOG.log(
                            Level.WARNING,
                            "Warning - project {0} failed to supply itself in the lookup of the root node of its own logical view",  // NOI18N
                            ProjectUtils.getInformation(project).getName());
                    //#114664
                    if (projectInLookup != null) {
                        projectInLookup[0] = false;
                    }
                }
            }                        
            node.addNodeListener(WeakListeners.create(NodeListener.class, this, node));
            return node;
        }
        
        // NodeListener impl -----------------------------------------
        
        @Override public void childrenAdded(NodeMemberEvent ev) { }
        @Override public void childrenRemoved(NodeMemberEvent ev) { }
        @Override public void childrenReordered(NodeReorderEvent ev) { }
        @Override public void nodeDestroyed(NodeEvent ev) { }        
        
        // PropertyChangeListener & NodeListener impl -----------------------------------------
        
        @Override
        public void propertyChange( PropertyChangeEvent e ) {
            if ( OpenProjectList.PROPERTY_OPEN_PROJECTS.equals( e.getPropertyName() ) ) {
                RP.post(new Runnable() {
                    public @Override void run() {
                        setKeys(getKeys());
                    }
                });
            } else if( PROP_DISPLAY_NAME.equals(e.getPropertyName()) ) {
                RP.schedule(new Runnable() {
                    public @Override void run() {
                        setKeys( getKeys() );
                    }
                }, 500, TimeUnit.MILLISECONDS);
            }
        }
        
        // Change listener impl ------------------------------------------------
        
        @Override
        public void stateChanged( ChangeEvent e ) {
            
            Reference<Project> projectRef = sources2projects.get(e.getSource());
            if ( projectRef == null ) {
                return;
            }
            
            final Project project = projectRef.get();
            
            if ( project == null ) {
                return;
            }
            
            // Fix for 50259, callers sometimes hold locks
            RP.post(new Runnable() {
                public @Override void run() {
                    Optional.ofNullable(projects2Pairs.get(project))
                            .map((ref) -> ref.get())
                            .ifPresent((p) -> p.update(project));
                    refresh(project);
                }
            } );
        }
        
        final void refresh(Project p) {
            refreshKey(new Pair(p, type));
        }
                                
        // Own methods ---------------------------------------------------------
        
        public Collection<Pair> getKeys() {
            List<Project> projects = Arrays.asList( OpenProjectList.getDefault().getOpenProjects() );
            projects.sort(OpenProjectList.projectByDisplayName());
            
            final List<Pair> dirs = new ArrayList<>(projects.size());
            final java.util.Map<Project,Pair> snapshot = new HashMap<>();
            for (Project project : projects) {
                final Pair p = new Pair(project, type);
                dirs.add(p);
                snapshot.put(project, p);
            }
            synchronized (projects2Pairs) {
                projects2Pairs.clear();
                snapshot.entrySet()
                        .forEach((e) -> projects2Pairs.put(
                                e.getKey(),
                                new WeakReference<>(e.getValue())));
                
            }
            return dirs;
        }
        
        /** Object that comparers two projects just by their directory.
         * This allows to replace a LazyProject with real one without discarding
         * the nodes.
         */
        static final class Pair extends Object {
            Project project;
            final FileObject fo;
            private final int type;
            private Union2<LogicalViewProvider,org.openide.util.Pair<Sources,SourceGroup[]>> data;

            public Pair(
                    final Project project,
                    final int type) {
                this.project = project;
                this.fo = project.getProjectDirectory();
                this.type = type;
                this.data = createData(project, type);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final Pair other = (Pair) obj;
                if (this.fo != other.fo && (this.fo == null || !this.fo.equals(other.fo))) {
                    return false;
                }
                return true;
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 53 * hash + (this.fo != null ? this.fo.hashCode() : 0);
                return hash;
            }

            private void update(@NonNull final Project project) {
                assert project != null;
                this.project = project;
                this.data = createData(project, type);
            }

            private static Union2<LogicalViewProvider,org.openide.util.Pair<Sources,SourceGroup[]>> createData(
                    final Project p,
                    final int type) {
                switch (type) {
                    case LOGICAL_VIEW:
                        final LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
                        if (lvp != null) {
                            return Union2.createFirst(lvp);
                        }
                    case PHYSICAL_VIEW:
                        final Sources s = ProjectUtils.getSources(p);
                        final SourceGroup[] groups = s.getSourceGroups(Sources.TYPE_GENERIC);                
                        return Union2.createSecond(org.openide.util.Pair.of(s, groups));
                    default:
                        throw new IllegalArgumentException(Integer.toString(type));
                }
            }
        }
                                                
    }

    static final class BadgingNode extends FilterNode implements ChangeListener, PropertyChangeListener, Runnable, FileStatusListener {
        private static final String MAGIC = "BadgingNode.μαγικ"; // #199591
        private final Object privateLock = new Object();
        private Set<FileObject> files;
        private Map<FileSystem,FileStatusListener> fileSystemListeners;
        private ChangeListener sourcesListener;
        private Map<SourceGroup,PropertyChangeListener> groupsListeners;
        RequestProcessor.Task task;
        private boolean nameChange;
        private boolean iconChange;
        private volatile Boolean mainCache;
        private final ProjectChildren ch;
        private final boolean logicalView;
        private final ProjectChildren.Pair pair;
        private final Set<FileObject> projectDirsListenedTo = new WeakSet<FileObject>();
        private static final int DELAY = 50;
        private final FileChangeListener newSubDirListener = new FileChangeAdapter() {
            public @Override void fileDataCreated(FileEvent fe) {
                setProjectFilesAsynch();
            }
            public @Override void fileFolderCreated(FileEvent fe) {
                setProjectFilesAsynch();
            }
        };
        private void setProjectFilesAsynch() {
            if (Boolean.getBoolean("test.nodelay")) { //for tests only
                setProjectFiles();
                return;
            }
            fsRefreshTask.schedule(DELAY);
        }
        private final RequestProcessor.Task fsRefreshTask = Hacks.RP.create(new Runnable() {
            @Override
            public void run() {
                setProjectFiles();
            }
        });
        private final Lookup.Result<ProjectIconAnnotator> result = Lookup.getDefault().lookupResult(ProjectIconAnnotator.class);
        
        static class AnnotationListener implements LookupListener, ChangeListener {
            private final Set<ProjectIconAnnotator> annotators = new WeakSet<ProjectIconAnnotator>();
            private final Reference<BadgingNode> node;
            
            public AnnotationListener(BadgingNode node) {
                this.node = new WeakReference<BadgingNode>(node);
            }
            void init() {
                BadgingNode n = node.get();
                if (n == null) {
                    return;
                }
                for (ProjectIconAnnotator annotator : n.result.allInstances()) {
                    if (annotators.add(annotator)) {
                        annotator.addChangeListener(WeakListeners.change(this, annotator));
                    }
                }
            }
            public @Override void resultChanged(LookupEvent ev) {
                init();
                stateChanged(null);
            }
            public @Override void stateChanged(ChangeEvent e) {
                BadgingNode n = node.get();
                if (n == null) {
                    return;
                }
                n.fireIconChange();
                n.fireOpenedIconChange();
            }
        }

        public BadgingNode(ProjectChildren ch, ProjectChildren.Pair p, Node n, boolean logicalView) {
            super(n, null, badgingLookup(n));
            this.ch = ch;
            this.pair = p;
            this.logicalView = logicalView;
            OpenProjectList.log(Level.FINER, "BadgingNode init {0}", toStringForLog()); // NOI18N
            OpenProjectList.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this, OpenProjectList.getDefault()));
            setProjectFilesAsynch();
            OpenProjectList.log(Level.FINER, "BadgingNode finished {0}", toStringForLog()); // NOI18N
            AnnotationListener annotationListener = new AnnotationListener(this);
            annotationListener.init();
            result.addLookupListener(annotationListener);
        }
        
        private static Lookup badgingLookup(Node n) {
            return new BadgingLookup(n.getLookup());
        }
        
        protected final void setProjectFiles() {
            Project prj = getLookup().lookup(Project.class);

            if (prj != null && /* #145682 */ !(prj instanceof LazyProject)) {
                setProjectFiles(prj);
            }
        }

        private void replaceProject(Project newProj) {
            if (newProj == null) {
                try {
                    newProj = ProjectManager.getDefault().findProject(pair.fo);
                    if (newProj == pair.project) {
                        return;
                    }
                } catch (IOException ex) {
                    OpenProjectList.log(Level.INFO, "No project for " + pair.fo, ex); // NOI18N
                } catch (IllegalArgumentException ex) {
                    OpenProjectList.log(Level.INFO, "No project for " + pair.fo, ex); // NOI18N
                }

            }
            
            OpenProjectList.log(Level.FINER, "replacing for {0}", toStringForLog());
            Project p = getLookup().lookup(Project.class);
            if (p == null) {
                OpenProjectList.log(Level.FINE, "no project in lookup {0}", toStringForLog());
                return;
            }
            FileObject fo = p.getProjectDirectory();
            if (newProj != null && newProj.getProjectDirectory().equals(fo)) {
                Node n = null;
                if (logicalView) {
                    n = ch.logicalViewForProject(
                            newProj,
                            ProjectChildren.Pair.createData(
                                    newProj,
                                    logicalView ? LOGICAL_VIEW : PHYSICAL_VIEW),
                            null);
                    OpenProjectList.log(Level.FINER, "logical view {0}", n);
                } else {
                    Node[] arr = PhysicalView.createNodesForProject(newProj);
                    OpenProjectList.log(Level.FINER, "physical view {0}", Arrays.asList(arr));
                    if (arr.length > 1) {
                        pair.update(newProj);
                        OpenProjectList.log(Level.FINER, "refreshing for {0}", newProj);
                        ch.refresh(newProj);
                        OpenProjectList.log(Level.FINER, "refreshed for {0}", newProj);
                        return;
                    } else if (arr.length == 1) {
                        n = arr[0];
                    } else {
                        OpenProjectList.log(Level.WARNING, "newProject yields null node: " + newProj);
                        n = Node.EMPTY;
                    }
                }
                OpenProjectList.log(Level.FINER, "change original: {0}", n);
                OpenProjectList.log(Level.FINER, "children before change original: {0}", getChildren());
                OpenProjectList.log(Level.FINER, "delegate children before change original: {0}", getOriginal().getChildren());
                changeOriginal(n, true);
                OpenProjectList.log(Level.FINER, "delegate after change original: {0}", getOriginal());
                OpenProjectList.log(Level.FINER, "name after change original: {0}", getName());
                OpenProjectList.log(Level.FINER, "children after change original: {0}", getChildren());
                OpenProjectList.log(Level.FINER, "delegate children after change original: {0}", getOriginal().getChildren());
                BadgingLookup bl = (BadgingLookup) getLookup();
                bl.setMyLookups(n.getLookup());
                OpenProjectList.log(Level.FINER, "done {0}", toStringForLog());
                setProjectFilesAsynch();
            } else {
                FileObject newDir;
                if (newProj != null) {
                    newDir = newProj.getProjectDirectory();
                } else {
                    newDir = null;
                    //#228790 use RP instead of EventQueue.invokeLater, job can block on project write mutex
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            OpenProjectList.getDefault().close(new Project[] { pair.project }, false);
                        }
                    });
                }
                OpenProjectList.log(Level.FINER, "wrong directories. current: " + fo + " new " + newDir);
            }
        }

        private void setProjectFiles(Project project) {
            Sources sources = ProjectUtils.getSources(project);  // returns singleton
            if (sourcesListener == null) {
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);
            }
            setGroups(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)), project.getProjectDirectory());
        }

        private void setGroups(Collection<SourceGroup> groups, FileObject projectDirectory) {
            if (groupsListeners != null) {
                for (Map.Entry<SourceGroup, PropertyChangeListener> entry : groupsListeners.entrySet()) {
                    entry.getKey().removePropertyChangeListener(entry.getValue());
                }
            }
            Map<SourceGroup,PropertyChangeListener> _groupsListeners = new HashMap<SourceGroup, PropertyChangeListener>();
            Set<FileObject> roots = new HashSet<FileObject>();
            for (SourceGroup group : groups) {
                PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
                _groupsListeners.put(group, pcl);
                group.addPropertyChangeListener(pcl);
                FileObject fo = group.getRootFolder();
                if (fo.equals(projectDirectory)) {
                    // #78994: do not listen to project root folder since changes in a nested project will mark it as modified.
                    // Instead, listen to direct subdirs which are owned by this project. Not very precise but the best we can do.
                    // (Would ideally obtain a complete but minimal list of dirs which cover this project but no subprojects.
                    // Unfortunately the current APIs provide no efficient way of doing this in general.)
                    for (FileObject kid : fo.getChildren()) {
                        Project owner = FileOwnerQuery.getOwner(kid);
                        // Not sufficient to check owner == project, because at startup owner will be a LazyProject.
                        if (owner != null && owner.getProjectDirectory() == projectDirectory) {
                            roots.add(kid);
                        }
                    }
                    if (projectDirsListenedTo.add(fo)) {
                        fo.addFileChangeListener(FileUtil.weakFileChangeListener(newSubDirListener, fo));
                    }
                } else {
                    roots.add(fo);
                }
            }
            groupsListeners = _groupsListeners;
            setFiles(roots);
        }

        protected final void setFiles(Set<FileObject> files) {
            if (fileSystemListeners != null) {
                for (Map.Entry<FileSystem, FileStatusListener> entry : fileSystemListeners.entrySet()) {
                    entry.getKey().removeFileStatusListener(entry.getValue());
                }
            }

            fileSystemListeners = new HashMap<FileSystem, FileStatusListener>();
            this.files = files;

            Set<FileSystem> hookedFileSystems = new HashSet<FileSystem>();
            for (FileObject fo : files) {
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (hookedFileSystems.contains(fs)) {
                        continue;
                    }
                    hookedFileSystems.add(fs);
                    FileStatusListener fsl = FileUtil.weakFileStatusListener(this, fs);
                    fs.addFileStatusListener(fsl);
                    fileSystemListeners.put(fs, fsl);
                } catch (FileStateInvalidException e) {
                    LOG.log(Level.INFO, "Cannot get " + fo + " filesystem, ignoring...", e); // NOI18N
                }
            }
        }

        @Override
        public void run() {
            boolean fireIcon;
            boolean fireName;
            synchronized (privateLock) {
                fireIcon = iconChange;
                fireName = nameChange;
                iconChange = false;
                nameChange = false;
            }
            if (fireIcon) {
                fireIconChange();
                fireOpenedIconChange();
            }
            if (fireName) {
                fireDisplayNameChange(null, null);
            }
        }

        @Override
        public void annotationChanged(FileStatusEvent event) {
            if (task == null) {
                task = Hacks.RP.create(this);
            }

            synchronized (privateLock) {
                if ((iconChange == false && event.isIconChange())  || (nameChange == false && event.isNameChange())) {
                    for (FileObject fo : files) {
                        if (event.hasChanged(fo)) {
                            iconChange |= event.isIconChange();
                            nameChange |= event.isNameChange();
                        }
                    }
                }
            }

            task.schedule(50);  // batch by 50 ms
        }
    
        public @Override String getDisplayName() {
            String original = super.getDisplayName();
            if (files != null && files.iterator().hasNext()) {
                try {
                    original = files.iterator().next().getFileSystem().getDecorator().annotateName(original, files);
                } catch (FileStateInvalidException e) {
                    LOG.log(Level.INFO, null, e);
                }
            }
            return original;
        }

        /** Get display name used for logging as original display name can cause deadlock issue #160512 */
        private String getLogName() {
            String original = super.getDisplayName();
            if (files != null && files.iterator().hasNext()) {
                try {
                    original = files.iterator().next().getFileSystem().getDecorator().annotateName(original, files);
                } catch (FileStateInvalidException e) {
                    LOG.log(Level.INFO, null, e);
                }
            }
            return original;
        }
        
        /** Special version of to Strign used for logging as original toString uses display name
         * => can cause deadlock issue #160512 */
        private String toStringForLog() {
            return getClass().getName() + "@" + Integer.toHexString(hashCode()) //NOI18N
                   + "[Name=" + getName() + ", displayName=" + getLogName() + "]"; //NOI18N
        }

        public @Override String getHtmlDisplayName() {
            String htmlName = getOriginal().getHtmlDisplayName();
            if (htmlName == null) {
                try {
                    htmlName = XMLUtil.toElementContent(getOriginal().getDisplayName());
                } catch (CharConversionException ex) {
                    // ignore
                }
            }
            if (htmlName == null) {
                return null;
            }
            if (files != null && files.iterator().hasNext()) {
                try {
                    String annotatedMagic = files.iterator().next().getFileSystem().
                            getDecorator().annotateNameHtml(MAGIC, files);
                    if (annotatedMagic != null) {
                        htmlName = annotatedMagic.replace(MAGIC, htmlName);
                    }
                } catch (FileStateInvalidException e) {
                    LOG.log(Level.INFO, null, e);
                }
            }      
            return isMainAsync()? "<b>" + htmlName + "</b>" : htmlName;
        }

        public @Override Image getIcon(int type) {
            return getIcon(type, false);
        }
        public @Override Image getOpenedIcon(int type) {
            return getIcon(type, true);
        }
        private Image getIcon(int type, boolean opened) {
            Image img = opened ? super.getOpenedIcon(type) : super.getIcon(type);

            if (logicalView) {
                if (files != null && files.iterator().hasNext()) {
                    try {
                        FileObject fo = files.iterator().next();
                        img = FileUIUtils.getImageDecorator(fo.getFileSystem()).annotateIcon(img, type, files);
                    } catch (FileStateInvalidException e) {
                        LOG.log(Level.INFO, null, e);
                    }
                }
                Project p = getLookup().lookup(Project.class);
                if (p != null) {
                    for (ProjectIconAnnotator pa : result.allInstances()) {
                        img = pa.annotateIcon(p, img, opened);
                    }
                }
            }

            return img;
        }

        @Override
        public void propertyChange( PropertyChangeEvent e ) {
            if ( OpenProjectList.PROPERTY_MAIN_PROJECT.equals( e.getPropertyName() ) ) {
                mainCache = null;
                fireDisplayNameChange( null, null );
            }
            if ( OpenProjectList.PROPERTY_REPLACE.equals(e.getPropertyName())) {
                replaceProject((Project)e.getNewValue());
            }
            if (SourceGroup.PROP_CONTAINERSHIP.equals(e.getPropertyName())) {
                setProjectFilesAsynch();
            }
        }

        private boolean isMainAsync() {
            final Boolean res = mainCache;
            if (res != null) {
                return res;
            }
            RP.execute(new Runnable() {
                @Override
                public void run() {                    
                    mainCache = isMain();
                    fireDisplayNameChange( null, null );
                }
            });
            return false;
        }

        private boolean isMain() {
            Project p = getLookup().lookup(Project.class);
            return p != null && OpenProjectList.getDefault().isMainProject( p );
        }
        
        // sources change
        @Override
        public void stateChanged(ChangeEvent e) {
            fsRefreshTask.schedule(DELAY);
        }

        @Override
        public Object getValue(String attributeName) {
            if ("customDelete".equals(attributeName)) {
                return true;
            }
            return super.getValue(attributeName);
        }

        @Override
        public boolean canDestroy() {
            Project p = getLookup().lookup(Project.class);
            if (p == null) {
                return false;
            }
            ActionProvider ap = p.getLookup().lookup(ActionProvider.class);

            String[] sa = ap != null ? ap.getSupportedActions() : new String[0];
            int k = sa.length;

            for (int i = 0; i < k; i++) {
                if (ActionProvider.COMMAND_DELETE.equals(sa[i])) {
                    return ap.isActionEnabled(ActionProvider.COMMAND_DELETE, getLookup());
                }
            }
            return false;
        }

        @Override
        public void destroy() throws IOException {
            Project p = getLookup().lookup(Project.class);
            if (p == null) {
                return;
            }
            final ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
            Mutex.EVENT.writeAccess(new Runnable() {
                @Override
                public void run() {
                    ap.invokeAction(ActionProvider.COMMAND_DELETE, getLookup());
                }
            });
        }
    } // end of BadgingNode
    private static final class BadgingLookup extends ProxyLookup {
        public BadgingLookup(Lookup... lkps) {
            super(lkps);
        }
        public void setMyLookups(Lookup... lkps) {
            setLookups(lkps);
        }
        public boolean isSearchInfo() {
            return getLookups().length > 1;
        }
    } // end of BadgingLookup

    /**
     * The {@link FileOwnerQueryImplementation} returning the first non artificial project owner
     * for {@link LogicalViewProvider#findPath}.
     * This {@link FileOwnerQueryImplementation} removes a need for {@link Project}'s {@link LogicalViewProvider}s
     * to ignore the artificial projects as done in {@link ProjectsRootNode#findProject}.
     * Needs to have the higher priority than the SimpleFileOwnerQueryImplementation.
     */
    @ServiceProvider(service = FileOwnerQueryImplementation.class, position = 10)
    public static final class SelectInProjectFileOwnerQueryImpl implements FileOwnerQueryImplementation {

        private final ThreadLocal<Object[]> current = new ThreadLocal<Object[]>();

        @Override
        @CheckForNull
        public Project getOwner(final URI file) {
            final Object[] currentTuple = current.get();
            if (currentTuple != null) {
                Object currentUri = currentTuple[1];
                if (currentUri == null) {
                     currentTuple[1] = currentUri = ((FileObject)currentTuple[0]).toURI();
                }
                if (currentUri.equals(file)) {
                    return (Project) currentTuple[2];
                }
            }
            return null;
        }

        @Override
        public Project getOwner(final FileObject file) {
            final Object[] currentTuple = current.get();
            if (currentTuple != null && currentTuple[0].equals(file)) {
                return (Project) currentTuple[2];
            }
            return null;
        }

        private void setCurrentProject(
                @NonNull final FileObject fo,
                @NullAllowed final Project prj) {
            assert fo != null;
            current.set(new Object[]{fo, null, prj});
        }

        private void clearCurrentProject() {
            current.remove();
        }

        @CheckForNull
        private static SelectInProjectFileOwnerQueryImpl getInstance() {
            for (FileOwnerQueryImplementation impl : Lookup.getDefault().lookupAll(FileOwnerQueryImplementation.class)) {
                if (SelectInProjectFileOwnerQueryImpl.class == impl.getClass()) {
                    return SelectInProjectFileOwnerQueryImpl.class.cast(impl);
                }
            }
            return null;
        }
    }
}
