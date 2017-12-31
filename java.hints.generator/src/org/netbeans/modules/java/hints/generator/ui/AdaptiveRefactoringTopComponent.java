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
package org.netbeans.modules.java.hints.generator.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import javax.swing.Action;
import javax.swing.JLabel;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.hints.generator.PatternGenerator;
import org.netbeans.modules.java.hints.generator.PatternGenerator.Result;
import org.netbeans.modules.java.hints.generator.PatternGenerator.Result.Item;
import org.netbeans.modules.java.hints.generator.PatternGenerator.Result.Kind;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author lahvac
 */
public class AdaptiveRefactoringTopComponent extends TopComponent implements ExplorerManager.Provider, PropertyChangeListener {

    private static final RequestProcessor WORKER = new RequestProcessor(AdaptiveRefactoringTopComponent.class.getName(), 1, false, false);
    private final ExplorerManager manager;
    private Result result;

    public AdaptiveRefactoringTopComponent(Result result) {
        initComponents();

        setDisplayName("Adaptive Refactoring");
        
        manager = new ExplorerManager();
        //XXX: should only allow single selection

        BeanTreeView btv = new BeanTreeView();
        mainPane.setLeftComponent(btv);
        btv.setRootVisible(false);
        mainPane.setRightComponent(new JLabel("No change selected."));
        manager.addPropertyChangeListener(this);

        propertyChange(null);
        
        setResult(result);
    }

    private void setResult(Result result) {
        this.result = result;

        Map<DecisionKey, Map<Project, Map<FileObject, List<Item>>>> categorize = new TreeMap<>((d1, d2) -> {
            return (int) (10 * (d1.certainty - d2.certainty) + (d1.kind != d2.kind ? d1.kind == Result.Kind.POSITIVE ? -1 : 1 : 0));
        });

        for (Item item : result.changes) {
            FileObject file = item.diffs.getModifiedFileObjects().iterator().next();
            Project prj = FileOwnerQuery.getOwner(file);
            categorize.computeIfAbsent(new DecisionKey(item.kind, item.certainty),
                                       unused -> new HashMap<>())
                      .computeIfAbsent(prj, unused -> new HashMap<>())
                      .computeIfAbsent(file, unused -> new ArrayList<>())
                      .add(item);
        }

        RootNode rootNode = new RootNode(categorize);

        manager.setRootContext(rootNode);
        WORKER.post(() -> selectFirst(rootNode));
    }

    private void selectFirst(Node currentNode) {
        Node[] nodes = currentNode.getChildren().getNodes(true);
        if (nodes.length > 0) {
            selectFirst(nodes[0]);
        } else {
            try {
                manager.setSelectedNodes(new Node[] {currentNode});
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Node[] selected = manager.getSelectedNodes();

        if (selected.length >= 1) {
            Item item = selected[0].getLookup().lookup(Item.class);
            if (item != null) {
                FileObject modifiedFile = item.diffs.getModifiedFileObjects().iterator().next();

                Reader originalIn = null;
                Reader modifiedIn = null;

                try {
                    originalIn = new InputStreamReader(modifiedFile.getInputStream(), FileEncodingQuery.getEncoding(modifiedFile));
                    modifiedIn = new StringReader(item.diffs.getResultingSource(modifiedFile));
                    String name = modifiedFile.getNameExt();
                    DiffController diff = DiffController.create(StreamSource.createSource(name, name, "text/x-java", originalIn),
                                                                StreamSource.createSource(name, name, "text/x-java", modifiedIn));

                    mainPane.setRightComponent(diff.getJComponent());
                    acceptButton.setEnabled(true);
                    rejectButton.setEnabled(true);
                } catch (IOException ex) {
                    //XXX:
                    Exceptions.printStackTrace(ex);
                    if (originalIn != null) {
                        try {
                            originalIn.close();
                        } catch (IOException ex1) {
                            Exceptions.printStackTrace(ex1);
                        }
                    }
                    if (modifiedIn != null) {
                        try {
                            modifiedIn.close();
                        } catch (IOException ex1) {
                            Exceptions.printStackTrace(ex1);
                        }
                    }
                } //the DiffController should close the readers when it finishes reading them
                  //(it reads them asynchronously, so they cannot be closed here).

                return;
            }
        }

        acceptButton.setEnabled(false);
        rejectButton.setEnabled(false);
    }

    private static final class DecisionKey {
        public final Kind kind;
        public final long certainty;

        public DecisionKey(Kind kind, long certainty) {
            this.kind = kind;
            this.certainty = certainty;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + Objects.hashCode(this.kind);
            hash = 59 * hash + (int) (this.certainty ^ (this.certainty >>> 32));
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
            final DecisionKey other = (DecisionKey) obj;
            if (this.kind != other.kind) {
                return false;
            }
            if (this.certainty != other.certainty) {
                return false;
            }
            return true;
        }

    }

    private final class RootNode extends AbstractNode {
        public RootNode(Map<DecisionKey, Map<Project, Map<FileObject, List<Item>>>> decision2Project2File2Items) {
            super(Children.create(new ChildFactory<Entry<DecisionKey, Map<Project, Map<FileObject, List<Item>>>>>() {
                @Override
                protected boolean createKeys(List<Entry<DecisionKey, Map<Project, Map<FileObject, List<Item>>>>> toPopulate) {
                    toPopulate.addAll(decision2Project2File2Items.entrySet());
                    return true;
                }
                @Override
                protected Node createNodeForKey(Entry<DecisionKey, Map<Project, Map<FileObject, List<Item>>>> key) {
                    return new DecisionNode(key.getKey(), key.getValue());
                }
            }, true));
        }
    }

    private final class DecisionNode extends AbstractNode {
        public DecisionNode(DecisionKey decision, Map<Project, Map<FileObject, List<Item>>> project2File2Items) {
            super(Children.create(new ChildFactory<Entry<Project, Map<FileObject, List<Item>>>>() {
                @Override
                protected boolean createKeys(List<Entry<Project, Map<FileObject, List<Item>>>> toPopulate) {
                    toPopulate.addAll(project2File2Items.entrySet());
                    return true;
                }
                @Override
                protected Node createNodeForKey(Entry<Project, Map<FileObject, List<Item>>> key) {
                    return new ProjectNode(key.getKey(), key.getValue());
                }
            }, true));

            int changes = 0;

            for (Map<FileObject, List<Item>> v : project2File2Items.values()) {
                for (List<Item> items : v.values()) {
                    changes += items.size();
                }
            }

            setDisplayName(String.format("%s; Certainty: %.2f%% (%d changes)", decision.kind.name().toLowerCase(), decision.certainty / 10.0, changes));
            setIconBaseWithExtension(decision.kind == Result.Kind.POSITIVE ? "org/netbeans/modules/java/hints/generator/resources/accept.png"
                                                                           : "org/netbeans/modules/java/hints/generator/resources/reject.png");
        }
    }

    private final class ProjectNode extends AbstractNode {
        private final ProjectInformation information;
        public ProjectNode(Project project, Map<FileObject, List<Item>> file2Items) {
            super(Children.create(new ChildFactory<Entry<FileObject, List<Item>>>() {
                @Override
                protected boolean createKeys(List<Entry<FileObject, List<Item>>> toPopulate) {
                    toPopulate.addAll(file2Items.entrySet());
                    return true;
                }
                @Override
                protected Node[] createNodesForKey(Entry<FileObject, List<Item>> key) {
                    try {
                        return new Node[] {
                            new FileNode(key.getKey(), key.getValue())
                        };
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                        return new Node[0];
                    }
                }
            }, true));

            assert project != null; //TODO: not really an internal constraint

            information = ProjectUtils.getInformation(project);
            setDisplayName(information.getDisplayName());
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.icon2Image(information.getIcon());
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }

    private final class FileNode extends FilterNode {

        public FileNode(FileObject file, List<Item> items) throws DataObjectNotFoundException {
            super(DataObject.find(file).getNodeDelegate(),
                  Children.create(new ChildFactory<Item>() {
                @Override
                protected boolean createKeys(List<Item> toPopulate) {
                    toPopulate.addAll(items);
                    return true;
                }
                @Override
                protected Node createNodeForKey(Item key) {
                    return new ItemNode(key);
                }
            }, true));

            setDisplayName(file.getName());
        }

    }

    private final class ItemNode extends AbstractNode {

        public ItemNode(Item item) {
            super(Children.LEAF, Lookups.singleton(item));

            setDisplayName("Change");
            setIconBaseWithExtension("org/netbeans/modules/java/hints/generator/resources/change_icon.png");
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPane = new javax.swing.JSplitPane();
        refactorButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        rejectButton = new javax.swing.JButton();
        acceptButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(refactorButton, org.openide.util.NbBundle.getMessage(AdaptiveRefactoringTopComponent.class, "AdaptiveRefactoringTopComponent.refactorButton.text", new Object[] {})); // NOI18N
        refactorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refactorButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(AdaptiveRefactoringTopComponent.class, "AdaptiveRefactoringTopComponent.cancelButton.text", new Object[] {})); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(rejectButton, org.openide.util.NbBundle.getMessage(AdaptiveRefactoringTopComponent.class, "AdaptiveRefactoringTopComponent.rejectButton.text", new Object[] {})); // NOI18N
        rejectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rejectButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(acceptButton, org.openide.util.NbBundle.getMessage(AdaptiveRefactoringTopComponent.class, "AdaptiveRefactoringTopComponent.acceptButton.text", new Object[] {})); // NOI18N
        acceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(refactorButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(acceptButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rejectButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPane, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refactorButton)
                    .addComponent(cancelButton)
                    .addComponent(rejectButton)
                    .addComponent(acceptButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptButtonActionPerformed
        Item item = manager.getSelectedNodes()[0].getLookup().lookup(Item.class);
        setResult(PatternGenerator.updatePositive(result, item));
    }//GEN-LAST:event_acceptButtonActionPerformed

    private void rejectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rejectButtonActionPerformed
        Item item = manager.getSelectedNodes()[0].getLookup().lookup(Item.class);
        setResult(PatternGenerator.updateNegative(result, item));
    }//GEN-LAST:event_rejectButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        close();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void refactorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refactorButtonActionPerformed
        try {
            PatternGenerator.doRefactoring(result);
            close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_refactorButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acceptButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JSplitPane mainPane;
    private javax.swing.JButton refactorButton;
    private javax.swing.JButton rejectButton;
    // End of variables declaration//GEN-END:variables
}
