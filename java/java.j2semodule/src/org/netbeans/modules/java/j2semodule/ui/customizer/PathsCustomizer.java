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
package org.netbeans.modules.java.j2semodule.ui.customizer;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public final class PathsCustomizer extends javax.swing.JPanel {

    private static final String MODULEPATH = NbBundle.getMessage(PathsCustomizer.class, "LBL_CustomizeLibraries_Modulepath_Tab");
    private static final String CLASSPATH = NbBundle.getMessage(PathsCustomizer.class, "LBL_CustomizeLibraries_Classpath_Tab");
    
    /**
     * Creates new form PathsCustomizer
     */
    public PathsCustomizer() {
        initComponents();

        // Disable tree line painting if enabled in current look and feel.
        // Do not use BasicTreeUI for all look and feels because this would
        // break selection painting in FlatLaf and Nimbus.
        // https://issues.apache.org/jira/browse/NETBEANS-4030
        if (UIManager.getBoolean("Tree.paintLines")) {
            mpTree.setUI(new BasicTreeUI() {
                @Override
                protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right) {
                }
                @Override
                protected void paintVerticalLine(Graphics g, JComponent c, int x, int top, int bottom) {
                }
            });
            cpTree.setUI(new javax.swing.plaf.basic.BasicTreeUI() {
                @Override
                protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right) {
                }
                @Override
                protected void paintVerticalLine(Graphics g, JComponent c, int x, int top, int bottom) {
                }
            });
        }

        setBackground(mpTree.getBackground());
        Mnemonics.setLocalizedText(addProject, NbBundle.getMessage(PathsCustomizer.class, "LBL_CustomizeLibraries_AddProject_JButton"));
        Mnemonics.setLocalizedText(addLibrary, NbBundle.getMessage(PathsCustomizer.class, "LBL_CustomizeLibraries_AddLibary_JButton"));
        Mnemonics.setLocalizedText(addFile, NbBundle.getMessage(PathsCustomizer.class, "LBL_CustomizeLibraries_AddJar_JButton"));
        menu.add(addProject);
        menu.add(addLibrary);
        menu.add(addFile);
    }

    public void setModels(final DefaultListModel mpModel, final DefaultListModel cpModel) {
        this.model = new JoinModel(mpModel, cpModel);
        this.list.setModel(this.model);
        DefaultTreeModel mpTreeModel = toTreeModel(mpModel, MODULEPATH);
        DefaultTreeModel cpTreeModel = toTreeModel(cpModel, CLASSPATH);
        mpModel.addListDataListener(new ListModelListener(mpModel, mpTreeModel, mpTree));
        cpModel.addListDataListener(new ListModelListener(cpModel, cpTreeModel, cpTree));
        mpTree.setModel(mpTreeModel);
        cpTree.setModel(cpTreeModel);
        SelectionModel mpTreeSelectionModel = new SelectionModel();
        SelectionModel cpTreeSelectionModel = new SelectionModel();
        mpTreeSelectionModel.addTreeSelectionListener(new SelectionListener(mpTreeModel, this.list, null, cpTreeSelectionModel));
        cpTreeSelectionModel.addTreeSelectionListener(new SelectionListener(cpTreeModel, this.list, mpModel, mpTreeSelectionModel));
        mpTree.setSelectionModel(mpTreeSelectionModel);
        cpTree.setSelectionModel(cpTreeSelectionModel);
    }

    public void setTreeCellRenderer(TreeCellRenderer renderer) {
        mpTree.setCellRenderer(new Renderer(renderer));
        cpTree.setCellRenderer(new Renderer(renderer));
    }
    
    public ButtonModel getAddProjectModel() {
        return addProject.getModel();
    }

    public ButtonModel getAddLibraryModel() {
        return addLibrary.getModel();
    }

    public ButtonModel getAddFileModel() {
        return addFile.getModel();
    }

    public JList getList() {
        return list;
    }
    
    public void setModulesEnabled(boolean enabled) {
        
    }

    private static DefaultTreeModel toTreeModel(final DefaultListModel lm, final String rootName) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootName);
        for (int i = 0; i < lm.getSize(); i++) {
            Object obj = lm.getElementAt(i);
            if (obj instanceof ClassPathSupport.Item) {
                root.add(toTreeNode(obj));
            }
        }
        return new DefaultTreeModel(root);
    }    

    private static DefaultMutableTreeNode toTreeNode(Object obj) {
        DefaultMutableTreeNode node = null;
        ClassPathSupport.Item item = (ClassPathSupport.Item) obj;
        if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
            File file = item.getResolvedFile();
            if (file != null && file.isDirectory()) {
                node = new DefaultMutableTreeNode(obj);
                for (File f : file.listFiles()) {
                    URL url = FileUtil.urlForArchiveOrDir(f);
                    if (url != null && SourceUtils.getModuleName(url) != null) {
                        ClassPathSupport.Item child = ClassPathSupport.Item.create(f.getName(), file, null, null);
                        node.add(new DefaultMutableTreeNode(child, false));
                    }
                }
            }
        }
        if (node == null) {
            node = new DefaultMutableTreeNode(obj, false);
        }
        return node;
    }    

    private static final class Renderer implements TreeCellRenderer {

        private final TreeCellRenderer delegate;
        private Font normal = null;
        private Font bold = null;

        public Renderer(TreeCellRenderer renderer) {
            this.delegate = renderer;
        }
                
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            if (normal == null) {
                normal = label.getFont().deriveFont(Font.PLAIN);
                bold = normal.deriveFont(Font.BOLD);
            }
            label.setFont(normal);
            if (value instanceof DefaultMutableTreeNode) {
                Object obj = ((DefaultMutableTreeNode) value).getUserObject();
                if (obj == MODULEPATH || obj == CLASSPATH) {
                    label.setText((String)obj);
                    label.setFont(bold);
                }
            }
            return label;
        }        
    }
    
    private static final class SelectionModel extends DefaultTreeSelectionModel {

        public SelectionModel() {
            setSelectionMode(SINGLE_TREE_SELECTION);
        }

        @Override
        public void setSelectionPaths(TreePath[] treePaths) {
            ArrayList<TreePath> paths = new ArrayList<>(treePaths.length);
            for (TreePath treePath : treePaths) {
                Object lastPathComponent = treePath.getLastPathComponent();
                if (lastPathComponent instanceof DefaultMutableTreeNode) {
                    Object obj = ((DefaultMutableTreeNode) lastPathComponent).getUserObject();
                    if (obj != MODULEPATH && obj != CLASSPATH) {
                        paths.add(treePath);
                    }
                }
            }
            if (treePaths.length > 0 && paths.isEmpty()) {
                paths.addAll(Arrays.asList(getSelectionPaths()));
            }
            super.setSelectionPaths(paths.toArray(new TreePath[0]));
        }        
    }
    
    private static final class SelectionListener implements TreeSelectionListener {

        final DefaultTreeModel treeModel;
        final JList list;
        final DefaultListModel otherListModel;
        final DefaultTreeSelectionModel otherTreeSelectionModel;

        public SelectionListener(DefaultTreeModel treeModel, JList list, DefaultListModel otherListModel, DefaultTreeSelectionModel otherTreeModel) {
            this.treeModel = treeModel;
            this.list = list;
            this.otherListModel = otherListModel;
            this.otherTreeSelectionModel = otherTreeModel;
        }

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            if (e.isAddedPath()) {
                otherTreeSelectionModel.clearSelection();
            }
            int idx = treeModel.getIndexOfChild(treeModel.getRoot(), e.getPath().getLastPathComponent());
            if (idx >= 0) {
                if (otherListModel != null) {
                    idx += otherListModel.getSize() + 1;
                }
                if (e.isAddedPath()) {
                    list.setSelectionInterval(idx, idx);
                } else {
                    list.removeSelectionInterval(idx, idx);
                }
            } else {
                list.clearSelection();
            }
        }        
    }

    private static final class ListModelListener implements ListDataListener {

        final DefaultListModel listModel;
        final DefaultTreeModel treeModel;
        final JTree tree;

        public ListModelListener(DefaultListModel listModel, DefaultTreeModel treeModel, JTree tree) {
            this.listModel = listModel;
            this.treeModel = treeModel;
            this.tree = tree;
        }
        
        @Override
        public void intervalAdded(ListDataEvent e) {
            for (int i = e.getIndex1(); i >= e.getIndex0(); i--) {
                Object obj = listModel.getElementAt(i);
                if (obj instanceof ClassPathSupport.Item) {
                    DefaultMutableTreeNode node = toTreeNode(obj);
                    treeModel.insertNodeInto(node, (MutableTreeNode)treeModel.getRoot(), e.getIndex0());
                    TreePath path = new TreePath(node.getPath());
                    tree.setSelectionPath(path);
                    tree.makeVisible(path);
                }
            }
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            for (int i = e.getIndex1(); i >= e.getIndex0(); i--) {
                treeModel.removeNodeFromParent((MutableTreeNode)treeModel.getChild(treeModel.getRoot(), i));
            }
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }        
    }
    
    private static class JoinModel extends DefaultListModel {

        private static final byte MP_ACTIVE = 1;
        private static final byte CP_ACTIVE = 2;
        
        private final DefaultListModel mpModel;
        private final DefaultListModel cpModel;
        private byte active = 0;
        private byte previousActive = 0;

        public JoinModel(DefaultListModel mpModel, DefaultListModel cpModel) {
            this.mpModel = mpModel;
            this.cpModel = cpModel;
        }

        @Override
        public int getSize() {
            return mpModel.getSize() + cpModel.getSize() + 1;
        }
        
        @Override
        public Object getElementAt(int index) {
            return index < mpModel.getSize()
                    ? mpModel.getElementAt(index)
                    : index == mpModel.getSize() ? null : cpModel.getElementAt(index - mpModel.getSize() - 1);
        }

        @Override
        public void copyInto(Object[] anArray) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trimToSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void ensureCapacity(int minCapacity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSize(int newSize) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int capacity() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return mpModel.size() + cpModel.size() + 1;
        }

        @Override
        public boolean isEmpty() {
            return mpModel.isEmpty() && cpModel.isEmpty();
        }

        @Override
        public Enumeration elements() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object elem) {
            return mpModel.contains(elem) || cpModel.contains(elem);
        }

        @Override
        public int indexOf(Object elem) {
            int idx = mpModel.indexOf(elem);
            return idx < 0 ? cpModel.indexOf(elem) + mpModel.getSize() + 1 : idx;
        }

        @Override
        public int indexOf(Object elem, int index) {
            int idx = mpModel.indexOf(elem, index);
            return idx < 0 ? cpModel.indexOf(elem, index - mpModel.getSize()) + mpModel.getSize() + 1 : idx;
        }

        @Override
        public int lastIndexOf(Object elem) {
            int idx = cpModel.lastIndexOf(elem);
            return idx < 0 ? mpModel.lastIndexOf(elem) : idx + mpModel.getSize() + 1;
        }

        @Override
        public int lastIndexOf(Object elem, int index) {
            int idx = cpModel.lastIndexOf(elem, index - mpModel.getSize());
            return idx < 0 ? mpModel.lastIndexOf(elem, index) : idx + mpModel.getSize() + 1;
        }

        @Override
        public Object elementAt(int index) {
            return index < mpModel.getSize()
                    ? mpModel.elementAt(index)
                    : index == mpModel.getSize() ? null : cpModel.elementAt(index - mpModel.getSize());
        }

        @Override
        public Object firstElement() {
            return mpModel.isEmpty() ? cpModel.firstElement() : mpModel.firstElement();
        }

        @Override
        public Object lastElement() {
            return cpModel.isEmpty() ? mpModel.lastElement() : cpModel.lastElement();
        }

        @Override
        public void setElementAt(Object element, int index) {
            if (index <= mpModel.getSize()) {
                mpModel.setElementAt(element, index);
            } else {
                cpModel.setElementAt(element, index - mpModel.getSize() - 1);
            }
            active = 0;
            previousActive = 0;
        }

        @Override
        public void removeElementAt(int index) {
            if (index < mpModel.getSize()) {
                mpModel.removeElementAt(index);                
            } else if (index > mpModel.getSize()) {
                cpModel.removeElementAt(index - mpModel.getSize() - 1);
            }
            active = 0;
            previousActive = 0;
        }

        @Override
        public void insertElementAt(Object element, int index) {
            if (index <= mpModel.getSize()) {
                mpModel.insertElementAt(element, index);
            } else {
                cpModel.insertElementAt(element, index - mpModel.getSize());
            }
            active = 0;
            previousActive = 0;
        }

        @Override
        public void addElement(Object element) {
            switch(active) {
                case CP_ACTIVE:
                    cpModel.addElement(element);
                    previousActive = active;
                    break;
                case MP_ACTIVE:
                    mpModel.addElement(element);
                    previousActive = active;
                    break;
                default:
                    if (previousActive == MP_ACTIVE) {
                        mpModel.addElement(element);
                    } else {
                        cpModel.addElement(element);
                    }
            }       
            active = 0;
        }

        @Override
        public boolean removeElement(Object obj) {
            active = 0;
            previousActive = 0;
            return mpModel.removeElement(obj) || cpModel.removeElement(obj);
        }

        @Override
        public void removeAllElements() {
            mpModel.removeAllElements();
            cpModel.removeAllElements();
            active = 0;
            previousActive = 0;
        }

        @Override
        public String toString() {
            return mpModel.toString() + cpModel.toString();
        }

        @Override
        public Object[] toArray() {
            Object[] arr = new Object[mpModel.getSize() + cpModel.getSize()];
            System.arraycopy(mpModel.toArray(), 0, arr, 0, mpModel.getSize());
            System.arraycopy(cpModel.toArray(), 0, arr, mpModel.getSize(), cpModel.getSize());
            return arr;
        }

        @Override
        public Object get(int index) {
            return index < mpModel.getSize()
                    ? mpModel.get(index)
                    : index == mpModel.getSize() ? null : cpModel.get(index - mpModel.getSize() - 1);
        }

        @Override
        public Object set(int index, Object element) {
            active = 0;
            previousActive = 0;
            return index <= mpModel.getSize() ? mpModel.set(index, element) : cpModel.set(index - mpModel.getSize() - 1, element);
        }

        @Override
        public void add(int index, Object element) {
            switch(active) {
                case CP_ACTIVE:
                    cpModel.add(Math.max(index - mpModel.getSize() - 1, 0), element);
                    previousActive = active;
                    break;
                case MP_ACTIVE:
                    mpModel.add(Math.min(index, mpModel.getSize()), element);
                    previousActive = active;
                    break;
                default:
                    switch(previousActive) {
                        case CP_ACTIVE:
                            cpModel.add(Math.max(index - mpModel.getSize() - 1, 0), element);
                            break;
                        case MP_ACTIVE:
                            mpModel.add(Math.min(index, mpModel.getSize()), element);
                            break;
                        default:
                            if (index <= mpModel.getSize()) {
                                mpModel.add(index, element);
                            } else {
                                cpModel.add(index - mpModel.getSize() - 1, element);
                            }
                    }                    
            }
            active = 0;
        }

        @Override
        public Object remove(int index) {
            active = 0;
            previousActive = 0;
            return index < mpModel.getSize()
                    ? mpModel.remove(index)
                    : index == mpModel.getSize() ? null : cpModel.remove(index - mpModel.getSize() - 1);
        }

        @Override
        public void clear() {
            active = 0;
            previousActive = 0;
            mpModel.clear();
            cpModel.clear();
        }

        @Override
        public void removeRange(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
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

        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        mpTree = new javax.swing.JTree();
        mpAddButton = new javax.swing.JButton();
        cpTree = new javax.swing.JTree();
        cpAddButton = new javax.swing.JButton();

        list.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(list);

        mpAddButton.setText("+");
        mpAddButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        mpAddButton.setPreferredSize(new java.awt.Dimension(20, 20));
        mpAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mpAddButtonActionPerformed(evt);
            }
        });

        cpAddButton.setText("+");
        cpAddButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cpAddButton.setPreferredSize(new java.awt.Dimension(20, 20));
        cpAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cpAddButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cpTree, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mpTree, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cpAddButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mpAddButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mpTree, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mpAddButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cpTree, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cpAddButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void mpAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mpAddButtonActionPerformed
        model.active = JoinModel.MP_ACTIVE;
        menu.show(mpAddButton, 0, mpAddButton.getHeight());        
    }//GEN-LAST:event_mpAddButtonActionPerformed

    private void cpAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cpAddButtonActionPerformed
        model.active = JoinModel.CP_ACTIVE;
        menu.show(cpAddButton, 0, cpAddButton.getHeight());
    }//GEN-LAST:event_cpAddButtonActionPerformed

    private final JPopupMenu menu = new JPopupMenu();
    private final JMenuItem addProject = new JMenuItem();
    private final JMenuItem addLibrary = new JMenuItem();
    private final JMenuItem addFile = new JMenuItem();
    
    private JoinModel model;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cpAddButton;
    private javax.swing.JTree cpTree;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> list;
    private javax.swing.JButton mpAddButton;
    private javax.swing.JTree mpTree;
    // End of variables declaration//GEN-END:variables
}
