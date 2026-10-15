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
package org.netbeans.modules.java.hints.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.lang.model.element.TypeElement;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.api.java.source.ui.TypeElementFinder.Customizer;
import org.netbeans.modules.java.source.parsing.ClasspathInfoTask;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;

/**
 * Provides reusable JPanel to enter a collection of types.
 * The list may be ordered or unordered. If ordered, Move Up/Down button appear.
 * The caller may attach a {@link ChangeListener} to be informed about changes in the list.
 * 
 * @author sdedic
 */
public class ClassNameList extends javax.swing.JPanel implements Runnable {
    private ClasspathInfo       classpathInfo;
    private boolean             ordered;
    private DefaultTableModel   model;
    private List<ChangeListener>    listeners = new ArrayList<>();
    private boolean suppressEvents;
    
    private TypeAcceptor<ElementHandle<TypeElement>, CompilationController>    typeAcceptor = new TypeAcceptor<ElementHandle<TypeElement>, CompilationController>() {
        @Override
        public boolean accept(ElementHandle<TypeElement> item, CompilationController context) {
            return true;
        }
    };
    
    /**
     * Configures classpath to search types. By default classpaths of all opened projects are considered; this can
     * be restricted by providing a specific ClasspathInfo (i.e. from a specific project).
     * 
     * @param cpInfo valid ClasspathInfo instance
     * @return this
     */
    public ClassNameList onClassPath(ClasspathInfo cpInfo) {
        this.classpathInfo = cpInfo;
        return this;
    }
    
    /**
     * Types entered into the list may be restricted. The restriction only applies on 
     * newly entered types picked up from the search list. Restricting hand-edited qualified
     * names is not (yet) supported - the user should be allowed to enter any weird value into configuration.
     * 
     * @param acceptor filer for type
     * @return this
     */
    public ClassNameList restrictTypes(TypeAcceptor<ElementHandle<TypeElement>, CompilationController> acceptor) {
        this.typeAcceptor = acceptor;
        return this;
    }
    
    /**
     * Configures ordering. An entry is ordered when it is inserted or reordered  when it is changed.
     * If the list is ordered, no automatic ordering is done; the item is kept at the position, and new items
     * are inserted either at the end of the list, or before the selected item.
     * @param ordered
     * @return 
     */
    public ClassNameList setOrdered(boolean ordered) {
        this.ordered = ordered;
        updateButtons();
        return this;
    }
    
    private static class EnableAwareRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            TableCellRenderer def = table.getDefaultRenderer(table.getColumnClass(column));
            if (!table.isEnabled()) {
                isSelected = hasFocus = false;
            }
            JComponent c = (JComponent)def.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setEnabled(table.isEnabled());
            
            return c;
        }
        
    }

    @Override
    public void addNotify() {
        super.addNotify(); 
        updateButtons();
    }
    
    private class ValidatingCellEditor extends DefaultCellEditor implements DocumentListener {
        private JTextComponent editor;
        private Color defColor;
        private boolean contentsOK;
        
        public ValidatingCellEditor() {
            super(new JTextField());
            editor = (JTextField)editorComponent;
            editor.getDocument().addDocumentListener(this);
            defColor = editor.getForeground();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            textChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            textChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
        
        private void textChanged() {
            String ident = ((JTextComponent)editorComponent).getText().trim();
            StringTokenizer tukac = new StringTokenizer(ident, ".", false);
            boolean ok = true;
            while (ok && tukac.hasMoreTokens()) {
                String t = tukac.nextToken();
                if (t.length() == 0 && tukac.hasMoreTokens()) {
                    ok = false;
                } else {
                    ok &= BaseUtilities.isJavaIdentifier(t);
                }
            }
            Color c = ok ? defColor : UIManager.getColor("nb.errorForeground");
            editor.setForeground(c);
            contentsOK = ok;
        }

        @Override
        public boolean stopCellEditing() {
            boolean editing = listClasses.isEditing();
            boolean x = super.stopCellEditing();
            if (!editing) {
                return x;
            }
            if (ordered) {
                fireStateChange();
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final int currentIndex = listClasses.getEditingRow();
                        if (currentIndex != -1) {
                            reinsertRow(currentIndex);
                        }
                    }
                });
            }
            return x;
        }
    }
    
    /**
     * Creates new form ClassNameList
     */
    public ClassNameList() {
        initComponents();
        listClasses.setTableHeader(null);
        listClasses.getColumnModel().getColumn(0).setCellRenderer(new EnableAwareRenderer());
        listClasses.getColumnModel().getColumn(0).setCellEditor(new ValidatingCellEditor());
        listClasses.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateButtons();
            }
        });
        InnerPanelSupport.displayExtendedCells(listClasses);
    }
    
    public void run() {
        updateButtons();
    }
    
    private void updateButtons() {
        boolean selected = !listClasses.getSelectionModel().isSelectionEmpty();
        btnRemove.setEnabled(selected);
        
        if (ordered) {
            btnUp.setEnabled(selected);
            btnDown.setEnabled(selected);
        } else {
            btnUp.setVisible(false);
            btnDown.setVisible(false);
        }
    }
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireStateChange() {
        if (listeners.isEmpty()) {
            return;
        }
        ChangeEvent e = new ChangeEvent(this);
        List<ChangeListener> toCall = new ArrayList<>(listeners);
        for (ChangeListener cl : toCall) {
            cl.stateChanged(e);
        }
    }
    
    public List<String> getClassNames() {
        int s = model.getRowCount();
        List<String> res = new ArrayList<>(s);
        for (int i = 0; i < s; i++) {
            res.add((String)model.getValueAt(i, 0));
        }
        
        return res;
    }
    
    public String getClassNameList() {
        List<String> types = getClassNames();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        for (String s : types) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(s);
        }
        return sb.toString();
    }
    
    public void setClassNames(String classNameList) {
        StringTokenizer tukac = new StringTokenizer(classNameList, ", ", false);
        List<String> res = new ArrayList<>(3);
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if (token.isEmpty()) {
                continue;
            }
            res.add(token);
        }
        setClassNames(res);
    }
    
    public void setClassNames(List<String> names) {
        DefaultTableModel mdl = new DefaultTableModel(0, 1);
        for (String s : names) {
            mdl.addRow(new Object[] { s });
        }
        listClasses.setModel(mdl);
        listClasses.getColumnModel().getColumn(0).setCellRenderer(new EnableAwareRenderer());
        listClasses.getColumnModel().getColumn(0).setCellEditor(new ValidatingCellEditor());
        model = mdl;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnUp = new javax.swing.JButton();
        btnDown = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        listClasses = new ExTable();

        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, org.openide.util.NbBundle.getMessage(ClassNameList.class, "ClassNameList.btnAdd.text")); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, org.openide.util.NbBundle.getMessage(ClassNameList.class, "ClassNameList.btnRemove.text")); // NOI18N
        btnRemove.setEnabled(false);
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnUp, org.openide.util.NbBundle.getMessage(ClassNameList.class, "ClassNameList.btnUp.text")); // NOI18N
        btnUp.setEnabled(false);
        btnUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnDown, org.openide.util.NbBundle.getMessage(ClassNameList.class, "ClassNameList.btnDown.text")); // NOI18N
        btnDown.setEnabled(false);
        btnDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownActionPerformed(evt);
            }
        });

        listClasses.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1"
            }
        ));
        listClasses.setFillsViewportHeight(true);
        listClasses.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listClasses.setShowVerticalLines(false);
        jScrollPane2.setViewportView(listClasses);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnDown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnUp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRemove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDown)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private int[] getSelectedIndices() {
        ListSelectionModel mdl = listClasses.getSelectionModel();
        int min = mdl.getMinSelectionIndex();
        int max = mdl.getMaxSelectionIndex();
        int[] indices = new int[max - min + 1];
        int ix = 0;
        for (int i = mdl.getMinSelectionIndex(); i <= max; i++) {
            if (mdl.isSelectedIndex(i)) {
                indices[ix++] = i;
            }
        }
        int[] result = new int[ix];
        System.arraycopy(indices, 0, result, 0, ix);
        return result;
    }
    
    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        int[] selected = getSelectedIndices();
        if (selected.length == 0) {
            return;
        }
        suppressEvents = true;
        for (int ind : selected) {
            model.removeRow(ind);
        }
        suppressEvents = false;
        fireStateChange();
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpActionPerformed
        int[] selected = getSelectedIndices();
        if (selected.length == 0) {
            return;
        }
        suppressEvents = true;
        for (int ind : selected) {
            if (ind == 0) {
                continue;
            }
            String item = (String)model.getValueAt(ind, 0);
            model.removeRow(ind);
            model.insertRow(ind - 1, new Object[] { item });
            listClasses.getSelectionModel().addSelectionInterval(ind - 1, ind - 1);
        }
        suppressEvents = false;
        fireStateChange();
    }//GEN-LAST:event_btnUpActionPerformed

    private void btnDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownActionPerformed
        int[] selected = getSelectedIndices();
        if (selected.length == 0) {
            return;
        }
        suppressEvents = true;
        for (int i = selected.length - 1; i >= 0; i--) {
            int ind = selected[i];
            if (ind >= model.getRowCount()-1) {
                continue;
            }
            String item = (String)model.getValueAt(ind, 0);
            model.removeRow(ind);
            model.insertRow(ind + 1, new Object[] { item });
            listClasses.getSelectionModel().addSelectionInterval(ind + 1, ind + 1);
        }
        suppressEvents = false;
        fireStateChange();
    }//GEN-LAST:event_btnDownActionPerformed

    private CompilationController controller;
    
    private Customizer customizer = new Customizer() {
        @Override
        public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, ClassIndex.NameKind nameKind, Set<ClassIndex.SearchScope> searchScopes) {
            return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
        }

        @Override
        public boolean accept(ElementHandle<TypeElement> typeHandle) {
            return typeAcceptor.accept(typeHandle, controller);
        }
        
    };
    
    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if (classpathInfo == null) {
            Set<ClassPath> cpPaths = new HashSet<>(GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE));
            Set<ClassPath> bootPaths = new HashSet<>(GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT));
            Set<ClassPath> sourcePaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);

            JavaPlatform p = JavaPlatform.getDefault();
            if (p != null) {
                bootPaths.add(p.getBootstrapLibraries());
                cpPaths.add(p.getStandardLibraries());
            }

            ClassPath compPath = ClassPathSupport.createProxyClassPath(cpPaths.toArray(new ClassPath[0]));
            ClassPath bootPath = ClassPathSupport.createProxyClassPath(bootPaths.toArray(new ClassPath[0]));
            ClassPath sourcePath = ClassPathSupport.createProxyClassPath(sourcePaths.toArray(new ClassPath[0]));
            
            classpathInfo = ClasspathInfo.create(bootPath, compPath, sourcePath);
        }
        ElementHandle<TypeElement> handle;
        
        try {
            try {
                ParserManager.parse("text/x-java", new ClasspathInfoTask(classpathInfo) { // NOI18N
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        if (controller != null) {
                            return;
                        }
                        Parser.Result res = resultIterator.getParserResult();
                        CompilationController ctrl = CompilationController.get(res);
                        if (ctrl != null) {
                            controller = ctrl;
                            return;
                        }
                        for (Embedding e : resultIterator.getEmbeddings()) {
                            run(resultIterator.getResultIterator(e));
                            if (controller != null) {
                                return;
                            }
                        }
                    }
                });
                if (controller == null) {
                    // eeee ?
                    return;
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            handle = TypeElementFinder.find(classpathInfo, customizer);
        } finally {
            controller = null;
        }
        if (handle == null) {
            return;
        }
        insertRow(handle.getQualifiedName(), true);
    }//GEN-LAST:event_btnAddActionPerformed

    private void reinsertRow(int index) {
        String qn = (String)model.getValueAt(index, 0);
        boolean sel = listClasses.isRowSelected(index);
        model.removeRow(index);
        insertRow(qn, sel);
    }
    
    private void insertRow(String qn, boolean setSelection) {
        String[] all = new String[model.getRowCount()];
        for (int i = 0; i < all.length; i++) {
            all[i] = (String)model.getValueAt(i, 0);
        }
        int newIndex;
        if (!ordered) {
            // insert into proper order. Effectivity ... ouch.
            int where = Arrays.binarySearch(all, qn);
            if (where >= 0) {
                // already exists, just select it.
                newIndex = where;
            } else {
                newIndex = -(where + 1);
                model.insertRow(newIndex, new Object[] { qn });
            }
        } else {
            int where = Arrays.asList(all).indexOf(qn);
            if (where != -1) {
                listClasses.getSelectionModel().setSelectionInterval(where, where);
                return;
            }
            if (listClasses.getSelectionModel().isSelectionEmpty()) {
                model.addRow(new Object[] { qn } );
                where =  model.getRowCount()- 1;
            } else {
                int anchor = listClasses.getSelectionModel().getAnchorSelectionIndex();
                model.insertRow(anchor, new Object[] { qn });
                where = anchor;
            }
            newIndex = where;
        }
        listClasses.getSelectionModel().setSelectionInterval(newIndex, newIndex);
        Rectangle cell = listClasses.getCellRect(newIndex, 0, true);
        listClasses.scrollRectToVisible(cell);
        fireStateChange();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDown;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnUp;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable listClasses;
    // End of variables declaration//GEN-END:variables


    public static class ExTable extends JTable {
        public String getToolTipText(MouseEvent ev) {
            return super.getToolTipText(ev);
        }
    }
}
