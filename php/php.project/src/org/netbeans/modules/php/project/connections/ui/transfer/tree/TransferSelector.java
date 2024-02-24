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

package org.netbeans.modules.php.project.connections.ui.transfer.tree;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooser.TransferType;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooserPanel;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

@org.netbeans.api.annotations.common.SuppressWarnings({"SE_BAD_FIELD_STORE", "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE"})
public final class TransferSelector extends TransferFilesChooserPanel implements ExplorerManager.Provider {

    private static final long serialVersionUID = 875487456455313L;

    static final Comparator<TransferFile> TRANSFER_FILE_COMPARATOR = new TransferFileComparator();

    final TransferSelectorModel model;
    private final TransferType transferType;
    private final ExplorerManager explorerManager;
    private final ItemListener checkAllItemListener;


    public TransferSelector(Set<TransferFile> transferFiles, TransferType transferType, long timestamp) {
        assert EventQueue.isDispatchThread();
        this.transferType = transferType;

        model = new TransferSelectorModel(transferType, transferFiles, timestamp);
        explorerManager = new ExplorerManager();

        RootChildren rootChildren = new RootChildren(transferFiles);
        explorerManager.setRootContext(new RootNode(rootChildren));

        initComponents();

        CheckTreeView treeView = new CheckTreeView(model);
        treeView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TransferSelector.class, "ACSN_TransferFilesTree"));
        treeView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TransferSelector.class, "ACSD_TransferFilesTree"));
        treePanel.add(treeView, BorderLayout.CENTER);

        checkAllCheckBox.setSelected(model.isAllSelected());
        checkAllItemListener = new CheckAllItemListener(model, treeView);
        checkAllCheckBox.addItemListener(checkAllItemListener);

        model.addChangeListener(new TransferFilesChangeListener() {
            @Override
            public void selectedFilesChanged() {
                checkAllCheckBox.removeItemListener(checkAllItemListener);
                checkAllCheckBox.setSelected(model.isAllSelected());
                checkAllCheckBox.addItemListener(checkAllItemListener);
            }

            @Override
            public void filterChanged() {
            }
        });
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public void addChangeListener(TransferFilesChangeListener listener) {
        model.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(TransferFilesChangeListener listener) {
        model.removeChangeListener(listener);
    }

    @Override
    public Set<TransferFile> getSelectedFiles() {
        return model.getSelected();
    }

    @Override
    public TransferFilesChooserPanel getEmbeddablePanel() {
        return this;
    }

    @Override
    public boolean hasAnyTransferableFiles() {
        return !model.getData().isEmpty();
    }

    Node create(TransferFile transferFile) {
        if (transferFile.isDirectory()) {
            return new FolderNode(transferFile);
        }
        return new FileNode(transferFile);
    }

    boolean hasChildrenFetched(TransferFile transferFile) {
        switch (transferType) {
            case DOWNLOAD:
                return transferFile.hasRemoteChildrenFetched();
            case UPLOAD:
                return transferFile.hasLocalChildrenFetched();
            default:
                throw new IllegalStateException("Unknown transfer type: " + transferType);
        }
    }

    List<TransferFile> getChildren(TransferFile transferFile) {
        switch (transferType) {
            case DOWNLOAD:
                return transferFile.getRemoteChildren();
            case UPLOAD:
                return transferFile.getLocalChildren();
            default:
                throw new IllegalStateException("Unknown transfer type: " + transferType);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {


        treePanel = new JPanel();
        checkAllCheckBox = new JCheckBox();

        setBorder(BorderFactory.createEtchedBorder());

        treePanel.setLayout(new BorderLayout());
        Mnemonics.setLocalizedText(checkAllCheckBox, NbBundle.getMessage(TransferSelector.class, "TransferSelector.checkAllCheckBox.text"));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(checkAllCheckBox)
                .addContainerGap(212, Short.MAX_VALUE))
            .addComponent(treePanel, GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(treePanel, GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(checkAllCheckBox))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox checkAllCheckBox;
    private JPanel treePanel;
    // End of variables declaration//GEN-END:variables

    private static class CheckTreeView extends BeanTreeView {
        private static final long serialVersionUID = 9856432132154L;

        public CheckTreeView(TransferSelectorModel model) {
            CheckListener listener = new CheckListener(model);
            tree.addMouseListener(listener);
            tree.addKeyListener(listener);

            CheckRenderer renderer = new CheckRenderer(model);
            tree.setCellRenderer(renderer);

            tree.setEditable(false);
        }

        public void repaintTree() {
            tree.repaint();
        }
    }

    private class FileNode extends AbstractNode {
        @StaticResource
        private static final String RESOURCE_ICON_FILE_DOWNLOAD = "org/netbeans/modules/php/project/ui/resources/fileDownload.gif"; // NOI18N
        @StaticResource
        private static final String RESOURCE_ICON_FILE_UPLOAD = "org/netbeans/modules/php/project/ui/resources/fileUpload.gif"; // NOI18N

        protected FileNode(TransferFile transferFile, Children children, Lookup lookup) {
            super(children, lookup);
            setDisplayName(transferFile.getName());
        }

        protected FileNode(TransferFile transferFile) {
            this(transferFile, Children.LEAF, Lookups.singleton(transferFile));
        }

        protected FileNode(Children children) {
            super(children);
        }

        @Override
        public boolean canCopy() {
            return false;
        }

        @Override
        public Image getIcon(int type) {
            return getIcon();
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon();
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }

        @Override
        public Action getPreferredAction() {
            return null;
        }

        private Image getIcon() {
            if (transferType.equals(TransferType.UPLOAD)) {
                return ImageUtilities.loadImage(RESOURCE_ICON_FILE_UPLOAD, false);
            }
            return ImageUtilities.loadImage(RESOURCE_ICON_FILE_DOWNLOAD, false);
        }
    }

    private class FolderNode extends FileNode {
        // see org.netbeans.swing.plaf.LFCustoms
        private static final String EXPLORER_FOLDER_ICON = "Nb.Explorer.Folder.icon"; // NOI18N
        private static final String EXPLORER_FOLDER_OPENED_ICON = "Nb.Explorer.Folder.openedIcon"; // NOI18N

        @StaticResource
        private static final String RESOURCE_ICON_FOLDER = "org/netbeans/modules/php/project/ui/resources/folder.gif"; // NOI18N
        @StaticResource
        private static final String RESOURCE_ICON_FOLDER_OPENED = "org/netbeans/modules/php/project/ui/resources/folderOpen.gif"; // NOI18N

        protected FolderNode(TransferFile transferFile) {
            super(transferFile, Children.create(new FileChildFactory(transferFile), !hasChildrenFetched(transferFile)), Lookups.singleton(transferFile));
        }

        protected FolderNode(Children children) {
            super(children);
        }

        @Override
        public Image getIcon(int type) {
            Object icon = UIManager.get(EXPLORER_FOLDER_ICON);
            if (icon instanceof Image) {
                return (Image) icon;
            }
            return ImageUtilities.loadImage(RESOURCE_ICON_FOLDER, false);
        }

        @Override
        public Image getOpenedIcon(int type) {
            Object icon = UIManager.get(EXPLORER_FOLDER_OPENED_ICON);
            if (icon instanceof Image) {
                return (Image) icon;
            }
            return ImageUtilities.loadImage(RESOURCE_ICON_FOLDER_OPENED, false);
        }
    }

    private class RootNode extends FolderNode {

        public RootNode(RootChildren children) {
            super(children);

            String nameKey = null;
            if (children.hasProjectRoot()) {
                nameKey = "LBL_SourceFiles"; // NOI18N
            } else {
                nameKey = "LBL_SelectFilesForTransfer"; // NOI18N
            }
            setDisplayName(NbBundle.getMessage(TransferSelector.class, nameKey));
        }
    }

    private class RootChildren extends Children.Keys<TransferFile> {
        private final boolean projectRoot;

        public RootChildren(Set<TransferFile> transferFiles) {
            // do not allow select source-dir
            boolean projRoot = false;
            List<TransferFile> roots = new LinkedList<>();
            for (TransferFile file : transferFiles) {
                if (file.isProjectRoot()) {
                    roots.clear();
                    roots.addAll(getChildren(file));
                    projRoot = true;
                    break;
                }
                if (file.isRoot()) {
                    roots.add(file);
                }
            }
            projectRoot = projRoot;

            roots.sort(TRANSFER_FILE_COMPARATOR);
            setKeys(roots);
        }

        public boolean hasProjectRoot() {
            return projectRoot;
        }

        @Override
        protected Node[] createNodes(TransferFile file) {
            return new Node[] {TransferSelector.this.create(file)};
        }
    }

    private class FileChildFactory extends ChildFactory<TransferFile> {

        private final TransferFile transferFile;


        public FileChildFactory(TransferFile transferFile) {
            this.transferFile = transferFile;
        }

        @Override
        protected boolean createKeys(List<TransferFile> transferFiles) {
            transferFiles.addAll(getChildren(transferFile));
            transferFiles.sort(TRANSFER_FILE_COMPARATOR);
            return true;
        }

        @Override
        protected Node createNodeForKey(TransferFile file) {
            Node node = TransferSelector.this.create(file);
            model.addNode(node);
            return node;
        }

    }

    private static class TransferFileComparator implements Comparator<TransferFile> {

        @Override
        public int compare(TransferFile o1, TransferFile o2) {
            boolean isDir1 = o1.isDirectory();
            boolean isDir2 = o2.isDirectory();
            if ((isDir1 && isDir2)
                    || (!isDir1 && !isDir2)) {
                return o1.getName().compareTo(o2.getName());
            }
            if (isDir1) {
                return -1;
            }
            return 1;
        }
    }

    private static final class CheckAllItemListener implements ItemListener {

        private final TransferSelectorModel model;
        private final CheckTreeView treeView;


        public CheckAllItemListener(TransferSelectorModel model, CheckTreeView treeView) {
            this.model = model;
            this.treeView = treeView;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                model.selectAll();
                treeView.repaintTree();
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                model.unselectAll();
                treeView.repaintTree();
            }
        }

    }

}
