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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.project.ui.groups.Group;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** TopComponment for viewing open projects. 
 * <P>
 * PENEDING : Fix persistence when new Winsys allows 
 *
 * @author Petr Hrebejk
 */
public class ProjectTab extends TopComponent 
                        implements ExplorerManager.Provider, PropertyChangeListener, UndoRedo.Provider {
                
    public static final String ID_LOGICAL = "projectTabLogical_tc"; // NOI18N                            
    public static final String ID_PHYSICAL = "projectTab_tc"; // NOI18N                        
    private static final @StaticResource String PROJECT_TAB = "org/netbeans/modules/project/ui/resources/projectTab.png";
    private static final @StaticResource String FILES_TAB = "org/netbeans/modules/project/ui/resources/filesTab.png";
    
    private static final Image ICON_LOGICAL = ImageUtilities.loadImage( PROJECT_TAB);
    private static final Image ICON_PHYSICAL = ImageUtilities.loadImage( FILES_TAB);

    private static final Logger LOG = Logger.getLogger(ProjectTab.class.getName());

    private static Map<String, ProjectTab> tabs = new HashMap<String, ProjectTab>();                            
                            
    private final transient ExplorerManager manager;
    private transient Node rootNode;
    
    private String id;
    private final transient ProjectTreeView btv;

    private final JLabel noProjectsLabel = new JLabel(NbBundle.getMessage(ProjectTab.class, "NO_PROJECT_OPEN"));

    private boolean synchronizeViews = false;

    private FileObject objectToSelect;
    private boolean prompt;
    private Task selectionTask;

    private static final int NODE_SELECTION_DELAY = 200;
    
    private final NodeSelectionProjectPanel nodeSelectionProjectPanel;
    
    public ProjectTab( String id ) {
        this();
        this.id = id;
        initValues();
    }
    
    public ProjectTab() {
        
        // See #36315        
        manager = new ExplorerManager();
        
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, true));
        
        initComponents();

        btv = new ProjectTreeView();    // Add the BeanTreeView
        
        btv.setDragSource (true);
        btv.setRootVisible(false);
        
        add( btv, BorderLayout.CENTER ); 

        OpenProjects.getDefault().addPropertyChangeListener(this);

        noProjectsLabel.addMouseListener(new LabelPopupDisplayer(noProjectsLabel));
        noProjectsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noProjectsLabel.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); // NOI18N
        if( null != usualWindowBkg ) {
            noProjectsLabel.setBackground(usualWindowBkg);
            noProjectsLabel.setOpaque(true);
        } else {
            noProjectsLabel.setOpaque(false);
        }

        associateLookup( ExplorerUtils.createLookup(manager, map) );

        selectionTask = createSelectionTask();

        Preferences nbPrefs = NbPreferences.forModule(SyncEditorWithViewsAction.class);
        synchronizeViews = nbPrefs.getBoolean(SyncEditorWithViewsAction.SYNC_ENABLED_PROP_NAME, false);
        nbPrefs.addPreferenceChangeListener(new NbPrefsListener());
        
        nodeSelectionProjectPanel = new NodeSelectionProjectPanel();
        ActualSelectionProject actualSelectionProject = new ActualSelectionProject(nodeSelectionProjectPanel);
        manager.addPropertyChangeListener(actualSelectionProject);
        btv.getViewport().addChangeListener(actualSelectionProject);
        add(nodeSelectionProjectPanel, BorderLayout.SOUTH);        
    }

    /**
     * Update display to reflect {@link Group#getActiveGroup}.
     * @param group current group, or null
     */
    public void setGroup(Group g) {
        if (id.equals(ID_LOGICAL)) {
            if (g != null) {
                setName(NbBundle.getMessage(ProjectTab.class, "LBL_projectTabLogical_tc_with_group", g.getName()));
            } else {
                setName(NbBundle.getMessage(ProjectTab.class, "LBL_projectTabLogical_tc"));
            }
        } else {
            setName(NbBundle.getMessage(ProjectTab.class, "LBL_projectTab_tc"));
        }
        // Seems to be useless: setToolTipText(getName());
    }

    private void initValues() {
        setGroup(Group.getActiveGroup());
        
        if (id.equals(ID_LOGICAL)) {
            setIcon( ICON_LOGICAL ); 
        }
        else {
            setIcon( ICON_PHYSICAL );
        }
            
        if ( rootNode == null ) {
            // Create the node which lists open projects      
            rootNode = new ProjectsRootNode(id.equals(ID_LOGICAL) ? ProjectsRootNode.LOGICAL_VIEW : ProjectsRootNode.PHYSICAL_VIEW);
        }
        manager.setRootContext( rootNode );
    }
            
    /** Explorer manager implementation 
     */
    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    @Override
    public UndoRedo getUndoRedo() {
        final UndoRedo undoRedo = Lookups.forPath("org/netbeans/modules/refactoring").lookup(UndoRedo.class);
        return undoRedo==null?UndoRedo.NONE:undoRedo;
    }    
    
    /* Singleton accessor. As ProjectTab is persistent singleton this
     * accessor makes sure that ProjectTab is deserialized by window system.
     * Uses known unique TopComponent ID TC_ID = "projectTab_tc" to get ProjectTab instance
     * from window system. "projectTab_tc" is name of settings file defined in module layer.
     * For example ProjectTabAction uses this method to create instance if necessary.
     */
    public static synchronized ProjectTab findDefault( String tcID ) {

        ProjectTab tab = tabs.get(tcID);
        
        if ( tab == null ) {
            //If settings file is correctly defined call of WindowManager.findTopComponent() will
            //call TestComponent00.getDefault() and it will set static field component.
            
            TopComponent tc = WindowManager.getDefault().findTopComponent( tcID ); 
            if (tc != null) {
                if (!(tc instanceof ProjectTab)) {
                    //This should not happen. Possible only if some other module
                    //defines different settings file with the same name but different class.
                    //Incorrect settings file?
                    IllegalStateException exc = new IllegalStateException
                    ("Incorrect settings file. Unexpected class returned." // NOI18N
                    + " Expected:" + ProjectTab.class.getName() // NOI18N
                    + " Returned:" + tc.getClass().getName()); // NOI18N
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                    //Fallback to accessor reserved for window system.
                    tab = ProjectTab.getDefault( tcID );
                }
                else {
                    tab = (ProjectTab)tc;
                }
            } 
            else {
                //This should not happen when settings file is correctly defined in module layer.
                //TestComponent00 cannot be deserialized
                //Fallback to accessor reserved for window system.
                tab = ProjectTab.getDefault( tcID );
            }
        }
        return tab;
    }
    
    /* Singleton accessor reserved for window system ONLY. Used by window system to create
     * ProjectTab instance from settings file when method is given. Use <code>findDefault</code>
     * to get correctly deserialized instance of ProjectTab */
    public static synchronized ProjectTab getDefault( String tcID ) {
        
        ProjectTab tab = tabs.get(tcID);
        
        if ( tab == null ) {
            tab = new ProjectTab( tcID );            
            tabs.put( tcID, tab );
        }
        
        return tab;        
    }
    
    public static TopComponent getLogical() {
        return getDefault( ID_LOGICAL );
    }
    
    public static TopComponent getPhysical() {
        return getDefault( ID_PHYSICAL );
    }
    
    @Override
    protected String preferredID () {
        return id;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return ExplorerUtils.getHelpCtx( 
            manager.getSelectedNodes(),
            ID_LOGICAL.equals( id ) ? new HelpCtx( "ProjectTab_Projects" ) : new HelpCtx( "ProjectTab_Files" ) );
    }

     
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    // APPEARANCE
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
        
    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return btv.requestFocusInWindow();
    }

    //#41258: In the SDI, requestFocus is called rather than requestFocusInWindow:
    @Override
    public void requestFocus() {
        super.requestFocus();
        btv.requestFocus();
    }
    
    // PERSISTENCE
    
    private static final long serialVersionUID = 9374872358L;
    
    @Override
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal( out );
        
        out.writeObject( id );
        out.writeObject( rootNode.getHandle() );                
        out.writeObject( btv.getExpandedPaths() );
        out.writeObject( getSelectedPaths() );
    }

    public @Override void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal( in );
        id = (String)in.readObject();
        rootNode = ((Node.Handle)in.readObject()).getNode();
        final List<String[]> exPaths = NbCollections.checkedListByCopy((List<?>) in.readObject(), String[].class, true);
        final List<String[]> selPaths = new ArrayList<String[]>();
        try {
            selPaths.addAll(NbCollections.checkedListByCopy((List<?>) in.readObject(), String[].class, true));
        }
        catch ( java.io.OptionalDataException e ) {
            // Sel paths missing
        }
        initValues();
        if (!"false".equals(System.getProperty("netbeans.keep.expansion"))) { // #55701
            KeepExpansion ke = new KeepExpansion(exPaths, selPaths);
            ke.task.schedule(0);
        }
    }

    private class KeepExpansion implements Runnable {
        final RequestProcessor.Task task;
        final List<String[]> exPaths;
        final List<String[]> selPaths;

        KeepExpansion(List<String[]> exPaths, List<String[]> selPaths) {
            this.exPaths = exPaths;
            this.selPaths = selPaths;
            this.task = RP.create(this);
        }

        @Override
        public void run() {
            try {
                LOG.log(Level.FINE, "{0}: waiting for projects being open", id);
                OpenProjects.getDefault().openProjects().get(10, TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                LOG.log(Level.FINE, "{0}: Timeout. Will retry in a second", id);
                task.schedule(1000);
                return;
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            LOG.log(Level.FINE, "{0}: Checking node state", id);
            for (Node n : rootNode.getChildren().getNodes()) {
                if (btv.isExpanded(n)) {
                    LOG.log(Level.FINE, "{0}: Node {1} has been expanded. Giving up.", new Object[] {id, n});
                    return;
                }
            }
            LOG.log(Level.FINE, "{0}: expanding paths", id);
            btv.expandNodes(exPaths);
            LOG.log(Level.FINE, "{0}: selecting paths", id);
            final List<Node> selectedNodes = new ArrayList<Node>();
            Node root = manager.getRootContext();
            for (String[] sp : selPaths) {
                LOG.log(Level.FINE, "{0}: selecting {1}", new Object[] {id, Arrays.asList(sp)});
                try {
                    Node n = NodeOp.findPath(root, sp);
                    if (n != null) {
                        selectedNodes.add(n);
                    }
                } catch (NodeNotFoundException x) {
                    LOG.log(Level.FINE, null, x);
                }
            }
            if (!selectedNodes.isEmpty()) {
                LOG.log(Level.FINE, "{0}: Switching to AWT", id);
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        try {
                            manager.setSelectedNodes(selectedNodes.toArray(new Node[0]));
                        } catch (PropertyVetoException x) {
                            LOG.log(Level.FINE, null, x);
                        }
                        LOG.log(Level.FINE, "{0}: done.", id);
                    }
                });
            }
        }

    }

    
    // MANAGING ACTIONS
    
    @Override
    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }
    
    @Override
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }

    // SEARCHING NODES

    private static final Lookup context = Utilities.actionsGlobalContext();

    private static final Lookup.Result<FileObject> foSelection = context.lookup(new Lookup.Template<FileObject>(FileObject.class));

    private static final Lookup.Result<DataObject> doSelection = context.lookup(new Lookup.Template<DataObject>(DataObject.class));

    private final LookupListener baseListener = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent ev) {
            if (TopComponent.getRegistry().getActivated() == ProjectTab.this) {
                // Do not want to go into a loop.
                return;
            }
            if (synchronizeViews) {
                Collection<? extends FileObject> fos = foSelection.allInstances();
                if (fos.size() == 1) {
                    selectNodeAsyncNoSelect(fos.iterator().next(), false);
                } else {
                    Collection<? extends DataObject> dos = doSelection.allInstances();
                    if (dos.size() == 1) {
                        selectNodeAsyncNoSelect((dos.iterator().next()).getPrimaryFile(), false);
                    }
                }
            }
        }
    };

    private final LookupListener weakListener = WeakListeners.create(LookupListener.class, baseListener, null);

    private void startListening() {
        foSelection.addLookupListener(weakListener);
        doSelection.addLookupListener(weakListener);
        baseListener.resultChanged(null);
    }

    private void stopListening() {
        foSelection.removeLookupListener(weakListener);
        doSelection.removeLookupListener(weakListener);
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        startListening();
    }

    @Override
    protected void componentHidden() {
        super.componentHidden();
        stopListening();
    }

    public static final RequestProcessor RP = new RequestProcessor(ProjectTab.class);
    
    public void selectNodeAsync(FileObject object) {
        setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) );
        open();
        requestActive();
        selectNodeAsyncNoSelect(object, true);
    }

    private Task createSelectionTask() {
        Task task = RP.create(new Runnable() {
            @Override
            public void run() {
                if (objectToSelect == null) {
                    return;
                }
                ProjectsRootNode root = (ProjectsRootNode) manager.getRootContext();
                 Node tempNode = root.findNode(objectToSelect);
                 if (tempNode == null) {
                     Project project = FileOwnerQuery.getOwner(objectToSelect);
                     Project found = null;
                     for (;;) {
                         if (project != null) {
                             for (Project p : OpenProjectList.getDefault().getOpenProjects()) {
                                 if (p.getProjectDirectory().equals(project.getProjectDirectory())) {
                                     found = p;
                                     break;
                                 }
                             }
                         }
                         if (found instanceof LazyProject) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                         } else {
                             tempNode = root.findNode(objectToSelect);
                             break;
                         }
                     }
                     if (prompt && project != null && found == null) {
                         String message = NbBundle.getMessage(ProjectTab.class, "MSG_openProject_confirm", //NOI18N
                                 ProjectUtils.getInformation(project).getDisplayName());
                         String title = NbBundle.getMessage(ProjectTab.class, "MSG_openProject_confirm_title");//NOI18N
                         NotifyDescriptor.Confirmation confirm =
                                 new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.OK_CANCEL_OPTION);
                         DialogDisplayer.getDefault().notify(confirm);
                         if (confirm.getValue() == NotifyDescriptor.OK_OPTION) {
                             if (!OpenProjectList.getDefault().isOpen(project)) {
                                 OpenProjects.getDefault().open(new Project[] { project }, false);
                                 ProjectsRootNode.ProjectChildren.RP.post(new Runnable() {@Override public void run() {}}).waitFinished(); // #199669
                             }
                             tempNode = root.findNode(objectToSelect);
                         }
                     }
                 }
                 final Node selectedNode = tempNode;
                // Back to AWT             // Back to AWT
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        if ( selectedNode != null ) {
                            try {
                                manager.setSelectedNodes( new Node[] { selectedNode } );
                                btv.scrollToNode(selectedNode);
                                StatusDisplayer.getDefault().setStatusText( "" ); // NOI18N
                            }
                            catch ( PropertyVetoException e ) {
                                // Bad day node found but can't be selected
                            }
                        } else if (prompt) {
                            try {
                                manager.setSelectedNodes( new Node[] {} );
                                StatusDisplayer.getDefault().setStatusText(
                                    NbBundle.getMessage( ProjectTab.class,
                                                         ID_LOGICAL.equals( id ) ? "MSG_NodeNotFound_ProjectsTab" : "MSG_NodeNotFound_FilesTab" ) ); // NOI18N
                            } catch (PropertyVetoException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        setCursor( null );
                    }
                } );
            }
        });
        return task;
    }

    private void selectNodeAsyncNoSelect(FileObject object, boolean prompt) {
        objectToSelect = object;
        this.prompt = prompt;
        selectionTask.schedule(NODE_SELECTION_DELAY);
    }

    Node findNode(FileObject object) {
        return ((ProjectsRootNode) manager.getRootContext()).findNode(object);
    }
    
    void selectNode(final Node node) {
        Mutex.EVENT.writeAccess(new Runnable() {
            public @Override void run() {
                try {
                    manager.setSelectedNodes(new Node[] {node});
                    btv.scrollToNode(node);
                } catch (PropertyVetoException e) {
                    // Bad day node found but can't be selected
                }
            }
        });
    }
    
    void expandNode(Node node) {
        btv.expandNode( node );
    }
    
    private List<String[]> getSelectedPaths() {
        List<String[]> result = new ArrayList<String[]>();
        Node root = manager.getRootContext();
        for (Node n : manager.getSelectedNodes()) {
            String[] path = NodeOp.createPath(n, root);
            LOG.log(Level.FINE, "path from {0} to {1}: {2}", new Object[] {root, n, Arrays.asList(path)});
            if (path != null) {
                result.add(path);
            }
        }
        return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
            final boolean someProjectsOpen = OpenProjects.getDefault().getOpenProjects().length > 0;
            Mutex.EVENT.readAccess(new Runnable() {
                public @Override void run() {
                    if (someProjectsOpen) {
                        restoreTreeView();
                    } else {
                        showNoProjectsLabel();
                    }
                }
            });
        }
    }

    private void showNoProjectsLabel() {
        if (noProjectsLabel.isShowing()) {
            return;
        }
        remove(btv);
        add(noProjectsLabel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void restoreTreeView() {
        if (btv.isShowing()) {
            return;
        }
        remove(noProjectsLabel);
        add(btv, BorderLayout.CENTER );
        revalidate();
        repaint();
    }

    // Private innerclasses ----------------------------------------------------
    
    /** Extending bean treeview. To be able to persist the selected paths
     */
    private class ProjectTreeView extends BeanTreeView {
        public void scrollToNode(final Node n) {
            // has to be delayed to be sure that events for Visualizers
            // were processed and TreeNodes are already in hierarchy
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    TreeNode tn = Visualizer.findVisualizer(n);
                    if (tn == null) {
                        return;
                    }
                    TreeModel model = tree.getModel();
                    if (!(model instanceof DefaultTreeModel)) {
                        return;
                    }
                    TreePath path = new TreePath(((DefaultTreeModel) model).getPathToRoot(tn));
                    Rectangle r = tree.getPathBounds(path);
                    if (r != null) {
                        tree.scrollRectToVisible(r);
                    }
                }
            });
    }
                        
        public List<String[]> getExpandedPaths() { 

            List<String[]> result = new ArrayList<String[]>();
            
            TreeNode rtn = Visualizer.findVisualizer( rootNode );
            TreePath tp = new TreePath( rtn ); // Get the root
            
            for( Enumeration exPaths = tree.getExpandedDescendants( tp ); exPaths != null && exPaths.hasMoreElements(); ) {
                TreePath ep = (TreePath)exPaths.nextElement();
                Node en = Visualizer.findNode( ep.getLastPathComponent() );                
                String[] path = NodeOp.createPath( en, rootNode );
                
                // System.out.print("EXP "); ProjectTab.print( path );
                
                result.add( path );
            }
            
            return result;
            
        }
        
        /** Expands all the paths, when exists
         */
        public void expandNodes(List<String[]> exPaths) {
            for (final String[] sp : exPaths) {
                LOG.log(Level.FINE, "{0}: expanding {1}", new Object[] {id, Arrays.asList(sp)});
                Node n;
                try {
                    n = NodeOp.findPath(rootNode, sp);
                } catch (NodeNotFoundException e) {
                    LOG.log(Level.FINE, "got {0}", e.toString());
                    n = e.getClosestNode();
                }
                if (n == null) { // #54832: it seems that sometimes we get unparented node
                    LOG.log(Level.FINE, "nothing from {0} via {1}", new Object[] {rootNode, Arrays.toString(sp)});
                    continue;
                }
                final Node leafNode = n;
                EventQueue.invokeLater(new Runnable() {
                    public @Override void run() {
                        TreeNode tns[] = new TreeNode[sp.length + 1];
                        Node n = leafNode;
                        for (int i = sp.length; i >= 0; i--) {
                            if (n == null) {
                                LOG.log(Level.FINE, "lost parent node at #{0} from {1}", new Object[] {i, leafNode});
                                return;
                            }
                            tns[i] = Visualizer.findVisualizer(n);
                            n = n.getParentNode();
                        }
                        showPath(new TreePath(tns));
                    }
                });
            }
        }
        
        public void showOrHideNodeSelectionProjectPanel(final Node n, final Node sn) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    TreeNode tn = Visualizer.findVisualizer(n);
                    TreeNode tsn = Visualizer.findVisualizer(sn);
                    if (tn == null || tsn == null) {
                        return;
                    }
                    TreeModel model = tree.getModel();
                    if (!(model instanceof DefaultTreeModel)) {
                        return;
                    }
                    TreePath path = new TreePath(((DefaultTreeModel) model).getPathToRoot(tn));
                    TreePath snPath = new TreePath(((DefaultTreeModel) model).getPathToRoot(tsn));
                    Rectangle projectNodeCoordinates = tree.getPathBounds(path);
                    Rectangle selectedNodeCoordinates = tree.getPathBounds(snPath);
                    Rectangle prjTabScrollCoordinates = tree.getVisibleRect();
                    
                    //Constant 0.5 was choosed, b/c sometimes is project node partially visible
                    Integer projectTabTopPos = prjTabScrollCoordinates.y;
                    Integer projectTabBottomPos = prjTabScrollCoordinates.y + prjTabScrollCoordinates.height + 
                            (nodeSelectionProjectPanel.isMinimized()?0:(NodeSelectionProjectPanel.COMPONENT_HEIGHT));
                    if (projectNodeCoordinates != null && selectedNodeCoordinates != null) {
                        Double projectNodePos = projectNodeCoordinates.y + (projectNodeCoordinates.height * 0.5);
                        Double selectedNodePos = selectedNodeCoordinates.y + (selectedNodeCoordinates.height * 0.5);
                        //Adding and subtacting 1 for project tab bottom y-index, b/c this index is slightly changing, when panel appears, then disappears and again appears
                        if ((projectTabTopPos < projectNodePos && projectTabBottomPos > projectNodePos)
                             || (projectTabTopPos > selectedNodePos 
                                || (projectTabBottomPos < selectedNodePos
                                || projectTabBottomPos + 1 < selectedNodePos 
                                || projectTabBottomPos - 1 < selectedNodePos))) {
                            nodeSelectionProjectPanel.minimize();
                        } else {
                            nodeSelectionProjectPanel.maximize();
                        }
                    } else {
                        nodeSelectionProjectPanel.minimize();
                    }
                }
            });
        }
    }
    

    // showing popup on right click in projects tab when label <No Project Open> is shown
    private class LabelPopupDisplayer extends MouseAdapter {

        private Component component;

        public LabelPopupDisplayer(Component comp) {
            component = comp;
        }

        private void showPopup(int x, int y) {
            Action actions[] = rootNode.getActions(false);
            JPopupMenu popup = Utilities.actionsToPopup(actions, component);
            popup.show(component, x, y);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger() && id.equals(ID_LOGICAL)) {
                showPopup(e.getX(), e.getY());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() && id.equals(ID_LOGICAL)) {
                showPopup(e.getX(), e.getY());
            }
        }

    }

    private class NbPrefsListener implements PreferenceChangeListener {

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (SyncEditorWithViewsAction.SYNC_ENABLED_PROP_NAME.equals(evt.getKey())) {
                synchronizeViews = Boolean.parseBoolean(evt.getNewValue());
            }
        }

    }

    @ActionID(category="Project", id="org.netbeans.modules.project.ui.collapseAllNodes")
    @ActionRegistration(displayName="#collapseAllNodes")
    @ActionReferences({
        @ActionReference(path=ProjectsRootNode.ACTIONS_FOLDER, position=1510),
        @ActionReference(path=ProjectsRootNode.ACTIONS_FOLDER_PHYSICAL, position=1000)
    })
    @Messages("collapseAllNodes=Collapse All")
    public static class CollapseAll implements ActionListener {

        private final String type;

        public CollapseAll(String type) {
            this.type = type;
        }

        @Override public void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final ProjectTab tab = findDefault(type);
                            final Children children = tab.manager.getRootContext().getChildren();
                            for (Node root : children.getNodes()) {
                                if( tab.btv.isExpanded(root) ) {
                                    collapseNodes(root, tab);
                                    tab.btv.collapseNode(root);
                                }
                            }
                            Mutex.EVENT.writeAccess(new Runnable() {
                                public @Override void run() {
                                    FileObject activeFile = null;
                                    Iterator<TopComponent> iterator = TopComponent.getRegistry().getOpened().iterator();
                                    while(iterator.hasNext()) {
                                        TopComponent componentIter = iterator.next();
                                        if(componentIter.isVisible() && componentIter.getLookup().lookup(FileObject.class) != null) {
                                            activeFile = componentIter.getLookup().lookup(FileObject.class);
                                            break;
                                        }
                                    }
                                    if ( activeFile != null ) {
                                        Project projectOwner = FileOwnerQuery.getOwner(activeFile);
                                        if (projectOwner != null) {
                                            Node projectNode = null;
                                            for (Node node : children.getNodes(true)) {
                                                if(projectOwner.equals(node.getLookup().lookup(Project.class))) {
                                                    projectNode = node;
                                                    break;
                                                }
                                            }
                                            if (projectNode != null) {
                                                try {
                                                    tab.manager.setSelectedNodes(new Node[] {projectNode});
                                                    tab.btv.scrollToNode(projectNode);
                                                } catch (PropertyVetoException pve) {
                                                    Logger.getLogger(ProjectTab.class.getName()).log(Level.WARNING, null, pve);
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            });
            
        }
        
        private void collapseNodes(Node node, ProjectTab tab) {
            if (  node.getChildren().getNodesCount() != 0 ) {
                for ( Node nodeIter : node.getChildren().getNodes() ) {
                    if( tab.btv.isExpanded(nodeIter) ) {
                        collapseNodes(nodeIter, tab);
                        tab.btv.collapseNode(nodeIter);
                    }
                }
            }
        }

    }
    
    @ActionID(category="Project", id="org.netbeans.modules.project.ui.NodeSelectionProjectAction")
    @ActionRegistration(displayName="#CTL_MenuItem_NodeSelectionProjectAction", lazy = false)
    @ActionReferences({
        @ActionReference(path=ProjectsRootNode.ACTIONS_FOLDER, position=1550),
        @ActionReference(path=ProjectsRootNode.ACTIONS_FOLDER_PHYSICAL, position=1100)
    })
    @Messages("CTL_MenuItem_NodeSelectionProjectAction=Show Selected Node(s) Project Owner")
    public static class NodeSelectionProjectAction extends BooleanStateAction {

        public NodeSelectionProjectAction() {
            super();
        }
        
        @Override
        public String getName() {
            return NbBundle.getMessage(NodeSelectionProjectAction.class, "CTL_MenuItem_NodeSelectionProjectAction");
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public boolean getBooleanState() {
            return NodeSelectionProjectPanel.prefs.getBoolean(NodeSelectionProjectPanel.KEY_ACTUALSELECTIONPROJECT, false);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(NodeSelectionProjectAction.class);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean show = NodeSelectionProjectPanel.prefs.getBoolean(NodeSelectionProjectPanel.KEY_ACTUALSELECTIONPROJECT, false);
            NodeSelectionProjectPanel.prefs.putBoolean(NodeSelectionProjectPanel.KEY_ACTUALSELECTIONPROJECT, !show);
        }
    }
    
    @Messages({"MSG_none_node_selected=None of the nodes selected",
        "MSG_nodes_from_more_projects=Selected nodes are from more than one project"})
    private class ActualSelectionProject implements PropertyChangeListener, ChangeListener {
        
        private final JPanel selectionsProjectPanel;
        
        private JLabel actualProjectLabel;
        
        private Node [] lastSelectedNodes;
        
        public ActualSelectionProject(JPanel selectionsProjectPanel) {
            this.selectionsProjectPanel = selectionsProjectPanel;
            this.actualProjectLabel = new JLabel(Bundle.MSG_none_node_selected());
            setSelectionLabelProperties(null);
            this.selectionsProjectPanel.add(actualProjectLabel);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ( evt.getPropertyName().equals("selectedNodes") 
                    &&  NodeSelectionProjectPanel.prefs.getBoolean(NodeSelectionProjectPanel.KEY_ACTUALSELECTIONPROJECT, false) ) {
                performChange(lastSelectedNodes = (Node [])evt.getNewValue());
            }
        }
        
        private void performChange(Node [] selectedNodes) {
            String text = "";
            Node projectNode = null;
            if( selectedNodes != null && selectedNodes.length > 0 ) {
                Node selectedNode = selectedNodes[0];
                Node originallySelectedNode = selectedNodes[0];
                Node rootNode = ProjectTab.this.manager.getRootContext();
                while ( selectedNode.getParentNode() != null && !selectedNode.getParentNode().equals(rootNode)) {
                    selectedNode = selectedNode.getParentNode();
                }
                projectNode = selectedNode;
                //Tests whether other selected items have same project owner
                if( selectedNodes.length > 1 ) {
                    for ( int i = 1; i < selectedNodes.length; i ++) {
                        selectedNode = selectedNodes[i];                        
                        while ( !selectedNode.getParentNode().equals(rootNode) ) {
                            selectedNode = selectedNode.getParentNode();
                        }
                        if ( !projectNode.equals(selectedNode) ) {
                            projectNode = null;
                            text = Bundle.MSG_nodes_from_more_projects();
                            break;
                        }
                    }
                }
                if ( projectNode != null ) {
                    ProjectTab.this.btv.showOrHideNodeSelectionProjectPanel(projectNode, originallySelectedNode);
                    text = projectNode.getDisplayName();
                }
            } else {
                text = Bundle.MSG_none_node_selected();
            }
            if ( this.actualProjectLabel != null ) {
                this.actualProjectLabel.setText(text);
                setSelectionLabelProperties(projectNode);
            } else {
                this.actualProjectLabel = new JLabel(text);
                setSelectionLabelProperties(projectNode);
                this.selectionsProjectPanel.add(actualProjectLabel);
            }
        }
        
        private void setSelectionLabelProperties( Node projectNode ) {
            if ( projectNode != null ) {
                this.actualProjectLabel.setIcon(ImageUtilities.image2Icon(projectNode.getIcon(BeanInfo.ICON_COLOR_16x16)));
            } else {
                this.actualProjectLabel.setIcon(null);
            }
            this.actualProjectLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
        }

        @Override
        public void stateChanged(ChangeEvent evt) {
            if ( NodeSelectionProjectPanel.prefs.getBoolean(NodeSelectionProjectPanel.KEY_ACTUALSELECTIONPROJECT, false) ) {
                performChange(lastSelectedNodes);
            }
        }
    }

}
