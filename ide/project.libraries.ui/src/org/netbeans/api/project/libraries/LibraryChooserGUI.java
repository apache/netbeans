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
package org.netbeans.api.project.libraries;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.BaseProgressUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

class LibraryChooserGUI extends JPanel implements ExplorerManager.Provider, HelpCtx.Provider, LibraryChooser.Panel {

    private final LibraryManager manager;
    private final LibraryChooser.Filter filter;
    private final ExplorerManager explorer;
    private LibraryChooser.LibraryImportHandler importHandler;
    private DialogDescriptor dialogDescriptor;

    private LibraryChooserGUI(LibraryManager manager, LibraryChooser.Filter filter, 
            LibraryChooser.LibraryImportHandler importHandler) {
        if (manager == null) {
            manager = LibraryManager.getDefault();
        }
        this.manager = manager;
        this.filter = filter;
        this.importHandler = importHandler;
        explorer = new ExplorerManager();
        initComponents();
        tree.setDefaultActionAllowed(true);
    }
    
    public static LibraryChooser.Panel createPanel(LibraryManager manager, 
            LibraryChooser.Filter filter) {
        LibraryChooserGUI l = new LibraryChooserGUI(manager, filter, null);
        l.configureForEmbedded();
        return l;
    }

    public static Set<Library> showChooser(LibraryManager manager, LibraryChooser.Filter filter, 
            LibraryChooser.LibraryImportHandler handler, boolean addOperation) {
        LibraryChooserGUI l = new LibraryChooserGUI(manager, filter, handler);
        return l.showDialog(addOperation);
    }

    private Set<Library> showDialog(boolean addOperatation) {
        // show manage button only in embedded panel:
        manageLibrariesButton.setVisible(false);
        // import enabled only for non-global library manager
        importButton.setVisible(manager.getLocation() != null && importHandler != null);
        JPanel inset = new JPanel(new BorderLayout());
        inset.setBorder(new EmptyBorder(12,12,0,12));
        inset.add(this);
        String title;
        String buttonLabel;
        String buttonA11YName;
        String buttonA11YDesc;
        if (addOperatation) {
            title = NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.add.title");
            buttonLabel = NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.add.button");
            buttonA11YName = NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.add.button");
            buttonA11YDesc = NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.add.button.a11y.desc");
        } else {
            title = NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.import.title");
            buttonLabel = NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.import.button");
            buttonA11YName = NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.import.button");
            buttonA11YDesc = NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.import.button.a11y.desc");
            createButton.setVisible(false);
        }
        inset.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.AccessibleContext.accessibleName")); // NOI18N
        inset.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.accessibleDescription")); // NOI18N
        dialogDescriptor = new DialogDescriptor(inset, title);
        dialogDescriptor.setModal(true);
        final JButton add = new JButton(buttonLabel);
        add.setEnabled(false);
        add.setDefaultCapable(true);
        add.getAccessibleContext().setAccessibleName(buttonA11YName);
        add.getAccessibleContext().setAccessibleDescription(buttonA11YDesc);
        explorer.addPropertyChangeListener(new PropertyChangeListener() {
           public void propertyChange(PropertyChangeEvent evt) {
               add.setEnabled(!getSelectedLibraries().isEmpty());
           }
        });
        dialogDescriptor.setOptions(new Object[] {add, NotifyDescriptor.CANCEL_OPTION});
        dialogDescriptor.setClosingOptions(new Object[] {add, NotifyDescriptor.CANCEL_OPTION});
        if (DialogDisplayer.getDefault().notify(dialogDescriptor) == add) {
            Set<Library> selection = getSelectedLibraries();
            assert !selection.isEmpty();
            return selection;
        } else {
            return null;
        }
    }

    private void configureForEmbedded() {
        explorer.addPropertyChangeListener(new PropertyChangeListener() {
           public void propertyChange(PropertyChangeEvent evt) {
               firePropertyChange(PROP_SELECTED_LIBRARIES, null, null);
           }
        });
        createButton.setVisible(false);
        importButton.setVisible(false);
    }

    public Set<Library> getSelectedLibraries() {
        Set<Library> s = new HashSet<Library>();
        for (Node n : explorer.getSelectedNodes()) {
            Library l = n.getLookup().lookup(Library.class);
            if (l != null) {
                s.add(l);
            } else {
                return Collections.emptySet();
            }
        }
        return s;
    }

    public Component getVisualComponent() {
        return this;
    }

    public ExplorerManager getExplorerManager() {
        return explorer;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(LibraryChooserGUI.class);
    }

    private void setRootNode() {
        explorer.setRootContext(new AbstractNode(new LibraryManagerChildren()));
        tree.expandAll();
        try {
            if (explorer.getRootContext().getChildren().getNodes(true).length > 0) {
                explorer.setSelectedNodes(new Node[] {explorer.getRootContext().getChildren().getNodes(true)[0]});
            }
        } catch (PropertyVetoException x) {
            Exceptions.printStackTrace(x);
        }
        /* XXX Nothing seems to work to scroll to top; how is it done?
        tree.getViewport().setViewPosition(new Point());
        tree.getViewport().scrollRectToVisible(new Rectangle(0, 0, 1, 1));
         */
        tree.requestFocus();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        setRootNode();
    }

    private class LibraryManagerChildren extends Children.Keys<LibraryManager> {

        @Override
        protected void addNotify() {
            super.addNotify();
            if (manager != null) {
                setKeys(Collections.singleton(manager));
            }
        }

        protected Node[] createNodes(LibraryManager mgr) {
            List<Library> libs = new ArrayList<Library>();
            for (Library lib : mgr.getLibraries()) {
                if (filter == null || filter.accept(lib)) {
                    libs.add(lib);
                }
            }
            if (libs.isEmpty()) {
                return new Node[0];
            } else {
                libs.sort(new Comparator<Library>() {
                    Collator COLL = Collator.getInstance();
                    public int compare(Library lib1, Library lib2) {
                        return COLL.compare(lib1.getDisplayName(), lib2.getDisplayName());
                    }
                });
                Node n = new AbstractNode(new LibraryChildren(libs)) {
                    Node iconDelegate = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
                    public Image getIcon(int type) {
                        return iconDelegate.getIcon(type);
                    }
                    public Image getOpenedIcon(int type) {
                        return iconDelegate.getOpenedIcon(type);
                    }
                };
                n.setName(mgr.getDisplayName());
                n.setDisplayName(mgr.getDisplayName());
                return new Node[] {n};
            }
        }

    }

    private class LibraryChildren extends Children.Keys<Library> {

        LibraryChildren(List<Library> libs) {
            setKeys(libs);
        }

        @Override
        protected Node[] createNodes(Library lib) {
            return new Node[] {
                new LibraryNode(lib)
            };
        }

    }

    private class LibraryNode extends AbstractNode {

        private final Action[] actions = new Action[0];
        private final Action addAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((JButton)dialogDescriptor.getOptions()[0]).doClick();
            }
        };

        LibraryNode (@NonNull final Library lib) {
            super(Children.LEAF, Lookups.singleton(lib));
            setName(lib.getName());
            setDisplayName(lib.getDisplayName());
            setShortDescription(lib.getDescription());
            setIconBaseWithExtension("org/netbeans/modules/project/libraries/resources/libraries.gif"); // NOI18N
        }

        @Override
        public Action[] getActions(boolean context) {
            return actions;
        }

        @Override
        public Action getPreferredAction() {
            return dialogDescriptor == null ?
                null :
                addAction;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        librariesLabel = new javax.swing.JLabel();
        tree = new org.openide.explorer.view.BeanTreeView();
        createButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();
        manageLibrariesButton = new javax.swing.JButton();

        librariesLabel.setLabelFor(tree);
        org.openide.awt.Mnemonics.setLocalizedText(librariesLabel, org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.librariesLabel")); // NOI18N

        tree.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tree.setRootVisible(false);

        org.openide.awt.Mnemonics.setLocalizedText(createButton, org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.createButton.text")); // NOI18N
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(importButton, org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.importButton.text")); // NOI18N
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(manageLibrariesButton, org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.manageLibrariesButton.text")); // NOI18N
        manageLibrariesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageLibrariesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(librariesLabel)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(tree, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(createButton)
                    .addComponent(importButton)))
            .addGroup(layout.createSequentialGroup()
                .addComponent(manageLibrariesButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(librariesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(createButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(importButton))
                    .addComponent(tree, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(manageLibrariesButton))
        );

        librariesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "ACSD_AvailableLibraries")); // NOI18N
        tree.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "ACSD_AvailableLibrariesTree")); // NOI18N
        createButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.create.a11y.name")); // NOI18N
        createButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.create.a11y.desc")); // NOI18N
        importButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.import.a11y.name")); // NOI18N
        importButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.import.a11y.desc")); // NOI18N
        manageLibrariesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "ACSD_ManageLibraries")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibraryChooserGUI.class, "LibraryChooserGUI.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void manageLibrariesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageLibrariesButtonActionPerformed
        if (LibrariesCustomizer.showCustomizer(null, manager)) {
            setRootNode();
        }
    }//GEN-LAST:event_manageLibrariesButtonActionPerformed

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        Library l = LibrariesCustomizer.showCreateNewLibraryCustomizer(manager);
        if (l != null) {
            setRootNode();
            selectLibrary(Collections.singleton(l));
        }
    }//GEN-LAST:event_createButtonActionPerformed

    private void selectLibrary(Collection<Library> libraries) {
        Node root = explorer.getRootContext();
        List<Node> selection = new ArrayList<Node>();
        for (Library lib : libraries) {
            String[] path = {lib.getManager().getDisplayName(), lib.getName()};
            try {
                Node node = NodeOp.findPath(root, path);
                if (node != null) {
                    selection.add(node);
                }
            } catch (NodeNotFoundException e) {
                //Ignore it
            }
        }
        try {
            explorer.setSelectedNodes(selection.toArray(new Node[0]));
        } catch (PropertyVetoException e) {
            //Ignore it
        }
    }
    
    @NbBundle.Messages({"LBL_Importing=Importing..."})
    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        final Set<Library> libs = showChooser(LibraryManager.getDefault(), 
                new IgnoreAlreadyImportedLibrariesFilter(), null, false);
        if (libs != null) {            
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        final Set<Library> importedLibs = new HashSet<Library>();
                        for (Library lib : libs) {
                            importedLibs.add(importHandler.importLibrary(lib));
                        }
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                setRootNode();                                
                                selectLibrary(importedLibs);
                            }
                        });
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        enableButtons(true);
                    }
                }
            };
            enableButtons(false);
            BaseProgressUtils.showProgressDialogAndRun(r, Bundle.LBL_Importing());
        }
    }//GEN-LAST:event_importButtonActionPerformed

    private void enableButtons(boolean enable) {
        importButton.setEnabled(enable);
        createButton.setEnabled(enable);
        manageLibrariesButton.setEnabled(enable);
    }
    
    private class IgnoreAlreadyImportedLibrariesFilter implements LibraryChooser.Filter {
        public boolean accept(Library library) {
            return manager.getLibrary(library.getName()) == null;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createButton;
    private javax.swing.JButton importButton;
    private javax.swing.JLabel librariesLabel;
    private javax.swing.JButton manageLibrariesButton;
    private org.openide.explorer.view.BeanTreeView tree;
    // End of variables declaration//GEN-END:variables

}
