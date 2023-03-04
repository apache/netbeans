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

package org.netbeans.modules.maven.dependencies;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import static org.netbeans.modules.maven.dependencies.Bundle.*;
import org.netbeans.modules.maven.embedder.DependencyTreeFactory;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.spi.IconResources;
import org.openide.NotificationLineSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
public class ExcludeDependencyPanel extends javax.swing.JPanel {
    
    
    private MavenProject project;
    private DependencyNode rootnode;
    private Map<Artifact, TreeModel> modelCache;
    private Map<ChangeListener, CheckNode> change2Trans;
    private Map<ChangeListener, List<CheckNode>> change2Refs;
    private boolean isSingle = false;
    private NotificationLineSupport lineSupport;

    /** Creates new form ExcludeDependencyPanel */
    public ExcludeDependencyPanel(MavenProject prj, final Artifact single, final Set<DependencyNode> directs, final DependencyNode root) {
        project = prj;
        modelCache = new HashMap<Artifact, TreeModel>();
        change2Trans = new HashMap<ChangeListener, CheckNode>();
        change2Refs = new HashMap<ChangeListener, List<CheckNode>>();
        initComponents();
        isSingle = single != null;
        if (isSingle) {
            trTrans.setVisible(false);
            jScrollPane1.setVisible(false);
            jLabel1.setVisible(false);
        }
//        ToolTipManager.sharedInstance().registerComponent(trRef);
        trRef.setCellRenderer(new CheckRenderer(false));
        trTrans.setCellRenderer(new CheckRenderer(true));
        CheckNodeListener l = new CheckNodeListener(false);
        trRef.addMouseListener(l);
        trRef.addKeyListener(l);
        trRef.setToggleClickCount(0);
        trRef.setRootVisible(false);
        
        SwingUtilities.invokeLater(() -> trRef.setModel(new DefaultTreeModel(new DefaultMutableTreeNode())));

        new RequestProcessor(ExcludeDependencyPanel.class.getName()).post(() -> {
            if (!isSingle) {
                rootnode = DependencyTreeFactory.createDependencyTree(project, EmbedderFactory.getOnlineEmbedder(), Artifact.SCOPE_TEST);
            } else {
                rootnode = root;
            }
            SwingUtilities.invokeLater(() -> {
                if (!isSingle) {
                    trTrans.setModel(new DefaultTreeModel(createTransitiveDependenciesList()));
                } else {
                    CheckNode nd = new CheckNode(single, null, null);
                    DefaultTreeModel dtm = new DefaultTreeModel(createReferenceModel(directs, nd));
                    modelCache.put(single, dtm);
                    setReferenceTree(nd);
                    trTrans.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
                }
            });            
        });
        trTrans.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                printSpaceMessage();
            }

            @Override
            public void focusLost(FocusEvent e) {
                printSpaceMessage();
            }

        });
        trTrans.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                printSpaceMessage();
            }
        });
        trTrans.addKeyListener(new KeyListener() {
            @Override
           public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (KeyEvent.VK_SPACE == e.getKeyCode()) {
                    TreePath path = trTrans.getSelectionPath();
                    CheckNode nd = (CheckNode) path.getLastPathComponent();
                    TreeModel mdl = trRef.getModel();
                    int childs = mdl.getChildCount(mdl.getRoot());
                    for (int i = 0; i < childs; i++) {
                        CheckNode refNode = (CheckNode) mdl.getChild(mdl.getRoot(), i);
                        refNode.setSelected(true);
                    }
                    trRef.repaint();

                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }

    public ExcludeDependencyPanel(MavenProject prj) {
        this(prj, null, null, null);
    }

    public Map<Artifact, List<DependencyNode>> getDependencyExcludes() {
        Map<Artifact, List<DependencyNode>> toRet = new HashMap<Artifact, List<DependencyNode>>();
        for (Map.Entry<ChangeListener, CheckNode> entry : change2Trans.entrySet()) {
            ChangeListener list = entry.getKey();
            CheckNode trans = entry.getValue();
            List<CheckNode> refs = change2Refs.get(list);
            List<DependencyNode> nds = new ArrayList<DependencyNode>();
            for (CheckNode ref : refs) {
                if (ref.isSelected()) {
                    nds.add((DependencyNode)ref.getUserObject());
                }
            }
            toRet.put((Artifact)trans.getUserObject(), nds);
        }
        return toRet;
    }

    public void setStatusDisplayer(NotificationLineSupport createNotificationLineSupport) {
        lineSupport = createNotificationLineSupport;
    }

    @Messages("TXT_Exclude_all=Exclude from all by pressing 'SPACE' key.")
    private void printSpaceMessage() {
        if (lineSupport == null) {
            return;
        }
        if (trTrans.isFocusOwner() && trTrans.getSelectionPath() != null) {
            lineSupport.setInformationMessage(TXT_Exclude_all());
        } else {
            lineSupport.clearMessages();
        }
    }

    private TreeNode createReferenceModel(Set<DependencyNode> nds, CheckNode trans) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(null, true);
        ChangeListener list = new Listener();
        List<CheckNode> s = new ArrayList<CheckNode>();
        Icon icn = ImageUtilities.image2Icon(ImageUtilities.loadImage(IconResources.DEPENDENCY_ICON, true)); //NOI18N
        change2Trans.put(list, trans);
        change2Refs.put(list, s);
        for (DependencyNode nd : nds) {
            String label = nd.getArtifact().getGroupId() + ":" + nd.getArtifact().getArtifactId();
            CheckNode child = new CheckNode(nd, label, icn);
            child.setSelected(isSingle);
            child.addChangeListener(list);
            s.add(child);
            root.add(child);
        }
        return root;
    }

    private TreeNode createTransitiveDependenciesList() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(null, true);
        Set<Artifact> artifacts = project.getArtifacts();
        Icon icn = ImageUtilities.image2Icon(ImageUtilities.loadImage(IconResources.TRANSITIVE_DEPENDENCY_ICON, true)); //NOI18N
        for (Artifact a : artifacts) {
            if (a.getDependencyTrail().size() > 2) {
                String label = a.getGroupId() + ":" + a.getArtifactId();
                root.add(new CheckNode(a, label, icn));
            }
        }
        return root;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        trTrans = new javax.swing.JTree();
        jScrollPane2 = new javax.swing.JScrollPane();
        trRef = new javax.swing.JTree();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        trTrans.setRootVisible(false);
        trTrans.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                trTransValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(trTrans);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        trRef.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        trRef.setRootVisible(false);
        jScrollPane2.setViewportView(trRef);

        jLabel1.setText(org.openide.util.NbBundle.getBundle(ExcludeDependencyPanel.class).getString("ExcludeDependencyPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getBundle(ExcludeDependencyPanel.class).getString("ExcludeDependencyPanel.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void setReferenceTree(CheckNode mtb) {
        Artifact art = (Artifact) mtb.getUserObject();
        if (modelCache.containsKey(art)) {
            trRef.setModel(modelCache.get(art));
        } else {
            if (rootnode == null) {
                trRef.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
            } else {
                DependencyExcludeNodeVisitor nv = new DependencyExcludeNodeVisitor(art.getGroupId(), art.getArtifactId(), art.getType());
                rootnode.accept(nv);
                Set<DependencyNode> nds = nv.getDirectDependencies();
                DefaultTreeModel dtm = new DefaultTreeModel(createReferenceModel(nds, mtb));
                trRef.setModel(dtm);
                modelCache.put(art, dtm);
            }
        }
    }

    private void trTransValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_trTransValueChanged
        TreeNode tn = (TreeNode) evt.getPath().getLastPathComponent();
        if (tn instanceof CheckNode) {
            setReferenceTree((CheckNode)tn);
        } else {
            trRef.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
        }

    }//GEN-LAST:event_trTransValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTree trRef;
    private javax.swing.JTree trTrans;
    // End of variables declaration//GEN-END:variables


    private class Listener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            CheckNode trans = change2Trans.get(this);
            List<CheckNode> refs = change2Refs.get(this);
            boolean all = true;
            boolean some = false;
            for (CheckNode ref : refs) {
                if (!ref.isSelected()) {
                    all = false;
                }
                if (ref.isSelected()) {
                    some = true;
                }
            }
            if (all) {
                //competely gone.. -> strikethrough
                trans.strike();
            } else {
                trans.unstrike();
            }
            if (some) {
                trans.italic();
            } else {
                trans.unitalic();
            }
            if (trTrans.isVisible()) {
                trTrans.repaint();
            }
        }

    }

}
