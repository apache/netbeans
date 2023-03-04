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


package org.netbeans.modules.maven.repository.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.tree.TreeSelectionModel;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.indexer.api.ui.ArtifactViewer;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerFactory;
import org.netbeans.modules.maven.spi.IconResources;
import org.openide.awt.Actions;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author mkleint
 */
public class DependencyPanel extends TopComponent implements MultiViewElement, LookupListener{
    private static final Logger LOG = Logger.getLogger(DependencyPanel.class.getName());

    private Lookup.Result<DependencyNode> result;
    private JToolBar toolbar;
    private final ExplorerManager explorerManager;
    private final ExplorerManager treeExplorerManager;
    private final boolean includeToolbar;

    DependencyPanel(Lookup lookup, boolean includeToolbar) {
        super(lookup);
        this.includeToolbar = includeToolbar;
        explorerManager = new ExplorerManager();
        treeExplorerManager = new ExplorerManager();
        
        initComponents();

        ((BeanTreeView)tvTree).setBorder((Border)UIManager.get("ScrollPane.border"));
        ((BeanTreeView)tvDependencyList).setBorder((Border)UIManager.get("ScrollPane.border"));
        
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            jPanel1.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            jPanel2.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            jPanel3.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
        ((BeanTreeView)tvTree).setRootVisible(false);
        ((BeanTreeView)tvDependencyList).setRootVisible(false);
        ((BeanTreeView)tvTree).setDefaultActionAllowed(true);
        ((BeanTreeView)tvDependencyList).setDefaultActionAllowed(true);
        ((BeanTreeView)tvDependencyList).setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        explorerManager.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    for (Node nd : explorerManager.getSelectedNodes()) {
                        DependencyNode n = nd.getLookup().lookup(DependencyNode.class);
                        if (n != null) {
                            Artifact a = n.getArtifact();
                            recursCollapse(treeExplorerManager.getRootContext().getChildren().getNodes(), (BeanTreeView)tvTree);
                            Set<Node> selectedNodes = recurse(a, treeExplorerManager.getRootContext(), (BeanTreeView)tvTree);
                            try {
                                treeExplorerManager.setSelectedNodes(selectedNodes.toArray(new Node[0]));
                            } catch (PropertyVetoException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        
                    }
                }
            }

            private Set<Node> recurse(Artifact a, Node rootContext, BeanTreeView btv) {
                Set<Node> toRet = new HashSet<Node>();
                for (Node nd : rootContext.getChildren().getNodes(true)) {
                    DependencyNode n = nd.getLookup().lookup(DependencyNode.class);
                    if (n != null) {
                        if (n.getArtifact().equals(a) || (n.getRelatedArtifact() != null && n.getRelatedArtifact().equals(a))) {
                            btv.expandNode(rootContext);
                            toRet.add(nd);
                        }
                    }
                    toRet.addAll(recurse(a, nd, btv));
                }
                return toRet;
            }
            
            private void recursCollapse(Node[] nodes, BeanTreeView btv) {
                for (Node nn : nodes) {
                    recursCollapse(nn.getChildren().getNodes(true), btv);
                    ((BeanTreeView)tvTree).collapseNode(nn);
                }
            }
        });
    }
    
//    @MultiViewElement.Registration(
//        displayName="#TAB_Tree",
//        iconBase=IconResources.ICON_DEPENDENCY_JAR,
//        persistenceType=TopComponent.PERSISTENCE_NEVER,
//        preferredID=ArtifactViewer.HINT_DEPENDENCIES,
//        mimeType=Constants.POM_MIME_TYPE,
//        position=101
//    )
    //we want to include in editable editors once we have modification actions included.
    @NbBundle.Messages("TAB_Tree=Tree")
    public static MultiViewElement forPOM(final Lookup editor) {
        class L extends ProxyLookup implements PropertyChangeListener {
            Project p;
            L() {
                FileObject pom = editor.lookup(FileObject.class);
                if (pom != null) {
                    p = FileOwnerQuery.getOwner(pom);
                    if (p != null) {
                        NbMavenProject nbmp = p.getLookup().lookup(NbMavenProject.class);
                        if (nbmp != null) {
                            nbmp.addPropertyChangeListener(WeakListeners.propertyChange(this, nbmp));
                            reset();
                        } else {
                            LOG.log(Level.WARNING, "not a Maven project: {0}", p);
                        }
                    } else {
                        LOG.log(Level.WARNING, "no owner of {0}", pom);
                    }
                } else {
                    LOG.log(Level.WARNING, "no FileObject in {0}", editor);
                }
            }
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    reset();
                }
            }
            private void reset() {
                ArtifactViewerFactory avf = Lookup.getDefault().lookup(ArtifactViewerFactory.class);
                if (avf != null) {
                    Lookup l = avf.createLookup(p);
                    if (l != null) {
                        setLookups(l);
                    } else {
                        LOG.log(Level.WARNING, "no artifact lookup for {0}", p);
                    }
                } else {
                    LOG.warning("no ArtifactViewerFactory found");
                }
            }
        }
        return new DependencyPanel(new L(), false);
    }    
    

    public @Override int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblList = new javax.swing.JLabel();
        lblTree = new javax.swing.JLabel();
        lblHint = new javax.swing.JLabel();
        jPanel2 = new ExplorerPanel(treeExplorerManager)
        ;
        tvTree = new BeanTreeView();
        ;
        jPanel3 = new ExplorerPanel(explorerManager);
        tvDependencyList = new BeanTreeView();

        setFocusable(true);
        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(lblList, org.openide.util.NbBundle.getMessage(DependencyPanel.class, "DependencyPanel.lblList.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblTree, org.openide.util.NbBundle.getMessage(DependencyPanel.class, "DependencyPanel.lblTree.text")); // NOI18N

        lblHint.setText(org.openide.util.NbBundle.getMessage(DependencyPanel.class, "DependencyPanel.lblHint.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tvTree)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tvTree, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tvDependencyList)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tvDependencyList)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblList)
                        .addGap(0, 43, Short.MAX_VALUE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblTree)
                        .addGap(0, 134, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblHint, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTree)
                    .addComponent(lblList))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(34, 34, 34))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap(278, Short.MAX_VALUE)
                    .addComponent(lblHint)
                    .addContainerGap()))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DependencyPanel.class, "DependencyPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DependencyPanel.class, "DependencyPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblList;
    private javax.swing.JLabel lblTree;
    private javax.swing.JScrollPane tvDependencyList;
    private javax.swing.JScrollPane tvTree;
    // End of variables declaration//GEN-END:variables

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            toolbar = new JToolBar();
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                toolbar.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            }
            
            toolbar.setFloatable(false);
            if (includeToolbar) {
                Action[] a = new Action[1];
                Action[] actions = getLookup().lookup(a.getClass());
                Dimension space = new Dimension(3, 0);
                toolbar.addSeparator(space);
                for (Action act : actions) {
                    JButton btn = new JButton();
                    Actions.connect(btn, act);
                    toolbar.add(btn);
                    toolbar.addSeparator(space);
                }
            }
        }
        return toolbar;
    }


    @Override
    public void componentOpened() {
        super.componentOpened();
        result = getLookup().lookupResult(DependencyNode.class);
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                populateFields();
            }
        });
        result.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        result.removeLookupListener(this);
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }

    public @Override void setMultiViewCallback(MultiViewElementCallback callback) {}

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    private void populateFields() {
        Iterator<? extends DependencyNode> iter = result.allInstances().iterator();
        if (iter.hasNext()) {
            final DependencyNode root = iter.next();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    NodeVisitor vis = new NodeVisitor(Arrays.asList(new String[]{ Artifact.SCOPE_COMPILE, Artifact.SCOPE_PROVIDED, Artifact.SCOPE_RUNTIME, Artifact.SCOPE_TEST}));
                    root.accept(vis);
                    vis.getListOfDependencies();
                    explorerManager.setRootContext(new AbstractNode(createListChildren(vis.getListOfDependencies(), getLookup())));
                    treeExplorerManager.setRootContext(new AbstractNode(createTreeChildren(root, getLookup())));
                    ((BeanTreeView)tvTree).expandAll();

                }
            });
        } else {

        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        populateFields();
    }
    @NbBundle.Messages({
        "TIP_Included=Is included",
        "TIP_Conflict=Is omitted for conflict, version used is {0}",
        "TIP_Duplicate=Is omitted for duplicate with the same version",
        "TIP_Cycle=Is omitted for cycle"
    })
    private static String calculateStateTipPart(DependencyNode node) {
            int s = node.getState();
            if (s == DependencyNode.INCLUDED) {
                return Bundle.TIP_Included();
            } else if (s == DependencyNode.OMITTED_FOR_CONFLICT) {
                return Bundle.TIP_Conflict(node.getRelatedArtifact().getVersion());
            } else if (s == DependencyNode.OMITTED_FOR_DUPLICATE) {
                return Bundle.TIP_Duplicate();
            } else if (s == DependencyNode.OMITTED_FOR_CYCLE) {
                return Bundle.TIP_Cycle();
            }
            throw new IllegalStateException("illegal state:" + s);
        }    

    private static class NodeVisitor implements DependencyNodeVisitor {
        private List<DependencyNode> lst;
        private final List<String> scopes;
        private DependencyNode root;

        private NodeVisitor(List<String> scopes) {
            this.scopes = scopes;
        }

        @Override
    public boolean visit(DependencyNode node) {
        if (root == null) {
            root = node;
            lst = new ArrayList<DependencyNode>();
        }
        for (DependencyNode ch : node.getChildren()) {
            if (ch.getState() == DependencyNode.INCLUDED &&
                    scopes.contains(ch.getArtifact().getScope())) {
                lst.add(ch);
            }
        }
        return true;
    }

        @Override
    public boolean endVisit(DependencyNode node) {
        if (root == node) {
            root = null;
            return true;
        }
        return true;
    }

        private Collection<DependencyNode> getListOfDependencies() {
            return lst;
        }
    }
    
    private static class TreeNode extends AbstractNode {
        private final DependencyNode node;
        private final Lookup tcLookup;

        public TreeNode(DependencyNode node, final Lookup tcLookup) {
            super(createTreeChildren(node, tcLookup), Lookups.fixed(node));
            this.tcLookup = tcLookup;
            final Artifact artifact = node.getArtifact();
            setName(artifact.getId());
            this.node = node;
            setDisplayName(artifact.getArtifactId() + "-" + artifact.getVersion() + "." + artifact.getArtifactHandler().getExtension());
            if (node.getDepth() > 1) {
                setIconBaseWithExtension(IconResources.TRANSITIVE_DEPENDENCY_ICON);
            } else {
                setIconBaseWithExtension(IconResources.ICON_DEPENDENCY_JAR);
            }
            setShortDescription(Bundle.TIP_listNode(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), calculateStateTipPart(node)));
        }

        @Override
        public String getHtmlDisplayName() {
            if (node.getState() == DependencyNode.OMITTED_FOR_DUPLICATE) {
                return "<html><s>" + getDisplayName() + "</s></html>";
            }
            if (node.getState() == DependencyNode.OMITTED_FOR_CONFLICT) {
                return "<html><font color=\"!nb.errorForeground\"><s>" + getDisplayName() + "</s></font></html>";
            }
            return super.getHtmlDisplayName(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Action getPreferredAction() {
            return new OpenAction(node, tcLookup);
        }
        
        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {new OpenAction(node, tcLookup)};
        }
    }
    
    private static class ListNode extends AbstractNode {
        private final DependencyNode node;
        private final Lookup tcLookup;

        @NbBundle.Messages({"TIP_listNode=<html><i>GroupId:</i> <b>{0}</b><br/><i>ArtifactId:</i> <b>{1}</b><br/><i>Version:</i> <b>{2}</b><br/><i>State:</i> <b>{3}</b><br/></html>"})
        public ListNode(DependencyNode node, final Lookup tcLookup) {
            super(Children.LEAF, Lookups.fixed(node));
            this.tcLookup = tcLookup;
            this.node = node;
            final Artifact artifact = node.getArtifact();
            setName(artifact.getId());
            setDisplayName(artifact.getArtifactId() + "-" + artifact.getVersion() + "." + artifact.getArtifactHandler().getExtension());
            if (node.getDepth() > 1) {
                setIconBaseWithExtension(IconResources.TRANSITIVE_DEPENDENCY_ICON);
            } else {
                setIconBaseWithExtension(IconResources.ICON_DEPENDENCY_JAR);
            }
            setShortDescription(Bundle.TIP_listNode(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), calculateStateTipPart(node)));
        }
        
        @Override
        public Action getPreferredAction() {
            return new OpenAction(node, tcLookup);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {new OpenAction(node, tcLookup)};
        }
    }
    
    
    
    private static Children createTreeChildren(final DependencyNode dn, final Lookup tcLookup) {
        if (!dn.hasChildren()) {
            return Children.LEAF;
        }
        return Children.create(new ChildFactory<DependencyNode>() {

            @Override
            protected Node createNodeForKey(DependencyNode key) {
                return new TreeNode(key, tcLookup);
            }

            @Override
            protected boolean createKeys(List<DependencyNode> toPopulate) {
                toPopulate.addAll(dn.getChildren());
                return true;
            }
        }, false);
    }
    
    private static Children createListChildren(final Collection<DependencyNode> dns, final Lookup tcLookup) {
        return Children.create(new ChildFactory<DependencyNode>() {

            @Override
            protected Node createNodeForKey(DependencyNode key) {
                return new ListNode(key, tcLookup);
            }

            @Override
            protected boolean createKeys(List<DependencyNode> toPopulate) {
                toPopulate.addAll(dns);
                return true;
            }
        }, false);
    }

    
    private class ExplorerPanel extends JPanel implements ExplorerManager.Provider {
        private final ExplorerManager manager;

        public ExplorerPanel(ExplorerManager manager) {
            this.manager = manager;
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
    }
    
    private static class OpenAction extends AbstractAction {
        private final DependencyNode dependencyNode;
        private final Lookup lkp;

        @NbBundle.Messages({"ACT_Open=View Artifact Details"})
        public OpenAction(DependencyNode dn, Lookup lkp) {
            this.dependencyNode = dn;
            this.lkp = lkp;
            putValue(NAME, Bundle.ACT_Open());
        }
        
        

        @Override
        public void actionPerformed(ActionEvent e) {
            if (dependencyNode != null) {
                MavenProject prj = lkp.lookup(MavenProject.class);
                if (prj != null) {
                    ArtifactViewer.showArtifactViewer(dependencyNode.getArtifact(), prj.getRemoteArtifactRepositories(), ArtifactViewer.HINT_DEPENDENCIES);
                }
            }
        }
        
    }
}
