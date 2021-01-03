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

package org.netbeans.modules.python.project.ui.customizer;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.netbeans.modules.python.project.ui.Utils;
import org.netbeans.modules.python.project.util.Pair;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Customizer panel "Sources": source roots, encoding.
 */
public class CustomizerSources extends javax.swing.JPanel implements HelpCtx.Provider {
    
    
    private String originalEncoding;
    private boolean notified;
    private final Utils.SourceRootsMediator emSR;
    private final Utils.SourceRootsMediator emTSR;
    private final MediatorListener mListener;
    private final PythonProjectProperties uiProperties;

    public CustomizerSources(PythonProjectProperties uiProperties) {
        assert uiProperties != null;
        this.uiProperties = uiProperties;
        initComponents();
        jScrollPane1.getViewport().setBackground( sourceRoots.getBackground() );
        jScrollPane2.getViewport().setBackground( testRoots.getBackground() );
        
        sourceRoots.setModel(Utils.createSourceRootsModel(uiProperties.getSourceRoots()));
        testRoots.setModel( Utils.createSourceRootsModel(uiProperties.getTestRoots()));
        sourceRoots.getTableHeader().setReorderingAllowed(false);
        testRoots.getTableHeader().setReorderingAllowed(false);                        
        
        FileObject projectFolder = uiProperties.getProjectDirectory();
        File pf = FileUtil.toFile( projectFolder );
        this.projectLocation.setText( pf == null ? "" : pf.getPath() ); // NOI18N
        this.mListener = new MediatorListener();        
        
        emSR = Utils.registerEditMediator(
            uiProperties.getProject(),
            sourceRoots,
            addSourceRoot,
            removeSourceRoot, 
            upSourceRoot, 
            downSourceRoot,
            new LabelCellEditor(sourceRoots, testRoots),
            mListener,
            false);
        
        emTSR = Utils.registerEditMediator(
            uiProperties.getProject(),
            testRoots,
            addTestRoot,
            removeTestRoot, 
            upTestRoot, 
            downTestRoot,
            new LabelCellEditor(sourceRoots, testRoots),
            mListener,
            true);
        
        emSR.setRelatedEditMediator( emTSR );
        emTSR.setRelatedEditMediator( emSR );

        this.originalEncoding = this.uiProperties.getEncoding();
        if (this.originalEncoding == null) {
            this.originalEncoding = Charset.defaultCharset().name();
        }
        
        this.encoding.setModel(ProjectCustomizer.encodingModel(originalEncoding));
        this.encoding.setRenderer(ProjectCustomizer.encodingRenderer());
        final String lafid = UIManager.getLookAndFeel().getID();
        if (!"Aqua".equals(lafid)) { //NOI18N
            this.encoding.putClientProperty ("JComboBox.isTableCellEditor", Boolean.TRUE);    //NOI18N
            this.encoding.addItemListener(new java.awt.event.ItemListener(){ 
                @Override
                public void itemStateChanged(java.awt.event.ItemEvent e){ 
                    javax.swing.JComboBox combo = (javax.swing.JComboBox)e.getSource(); 
                    combo.setPopupVisible(false); 
                } 
            });
        }
        this.encoding.addActionListener(new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                handleEncodingChange();
            }            
        });
        initTableVisualProperties(sourceRoots);
        initTableVisualProperties(testRoots);
    }
    
    private class TableColumnSizeComponentAdapter extends ComponentAdapter {
        private JTable table = null;
        
        public TableColumnSizeComponentAdapter(JTable table){
            this.table = table;
        }
        
        @Override
        public void componentResized(ComponentEvent evt){
            double pw = table.getParent().getParent().getSize().getWidth();
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            TableColumn column = table.getColumnModel().getColumn(0);
            column.setWidth( ((int)pw/2) - 1 );
            column.setPreferredWidth( ((int)pw/2) - 1 );
            column = table.getColumnModel().getColumn(1);
            column.setWidth( ((int)pw/2) - 1 );
            column.setPreferredWidth( ((int)pw/2) - 1 );
        }
    }
    
    private void initTableVisualProperties(JTable table) {

        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        // set the color of the table's JViewport
        table.getParent().setBackground(table.getBackground());
        
        //we'll get the parents width so we can use that to set the column sizes.
        double pw = table.getParent().getParent().getPreferredSize().getWidth();
        
        //#88174 - Need horizontal scrollbar for library names
        //ugly but I didn't find a better way how to do it
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMinWidth(226);
        column.setWidth( ((int)pw/2) - 1 );
        column.setPreferredWidth( ((int)pw/2) - 1 );
        column.setMinWidth(75);
        column = table.getColumnModel().getColumn(1);
        column.setMinWidth(226);
        column.setWidth( ((int)pw/2) - 1 );
        column.setPreferredWidth( ((int)pw/2) - 1 );
        column.setMinWidth(75);
        this.addComponentListener(new TableColumnSizeComponentAdapter(table));
    }
    
    private void handleEncodingChange () {
            Charset enc = (Charset) encoding.getSelectedItem();
            String encName;
            if (enc != null) {
                encName = enc.name();
            }
            else {
                encName = originalEncoding;
            }
            if (!notified && encName!=null && !encName.equals(originalEncoding)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(CustomizerSources.class,"MSG_EncodingWarning"), NotifyDescriptor.WARNING_MESSAGE));
                notified=true;
            }
            this.uiProperties.setEncoding(encName);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx (CustomizerSources.class);
    }            
    
    private class MediatorListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == emSR) {                
                uiProperties.setSourceRoots(tableToData(sourceRoots));
            }
            else if (e.getSource() == emTSR) {
                uiProperties.setTestRoots(tableToData(testRoots));
            }
        }
        
        private List<Pair<File,String>> tableToData (JTable table) {
            final TableModel model = sourceRoots.getModel();
            List<Pair<File,String>> data = new LinkedList<>();
            for (int i=0; i< model.getRowCount(); i++) {
                File f = (File) model.getValueAt(i, 0);
                String s = (String) model.getValueAt(i, 1);
                data.add(Pair.of(f, s));
            }
            return data;
        }
        
    }
    
    private static class ResizableRowHeightTable extends JTable {

        private boolean needResize = true;
        
        @Override
        public void setFont(Font font) {
            needResize = true;
            super.setFont(font);
        }

        @Override
        public void paint(Graphics g) {
            if(needResize) {
                this.setRowHeight(g.getFontMetrics(this.getFont()).getHeight());
                needResize = false;
            }
            super.paint(g);
        }
        
    }
    
    private static class LabelCellEditor extends DefaultCellEditor {
        
        private JTable sourceRoots;
        private JTable testRoots;
        
        public LabelCellEditor(JTable sourceRoots, JTable testRoots) {
            super(new JTextField());
            this.sourceRoots = sourceRoots;
            this.testRoots = testRoots;
        }
        
        @Override
        public boolean stopCellEditing() {
            JTextField field = (JTextField) getComponent();
            String text = field.getText();
            boolean validCell = true;
            TableModel model = sourceRoots.getModel();
            int rowCount = model.getRowCount();
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                String value = (String) model.getValueAt(rowIndex, 1);
                if (text.equals(value)) {
                    validCell = false;
                }
            }
            model = testRoots.getModel();
            rowCount = model.getRowCount();
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                String value = (String) model.getValueAt(rowIndex, 1);
                if (text.equals(value)) {
                    validCell = false;
                }
            }
            
            return validCell == false ? validCell : super.stopCellEditing();
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
        sourceRootsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sourceRoots = new ResizableRowHeightTable();
        addSourceRoot = new javax.swing.JButton();
        removeSourceRoot = new javax.swing.JButton();
        upSourceRoot = new javax.swing.JButton();
        downSourceRoot = new javax.swing.JButton();
        testRootsPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        testRoots = new ResizableRowHeightTable();
        addTestRoot = new javax.swing.JButton();
        removeTestRoot = new javax.swing.JButton();
        upTestRoot = new javax.swing.JButton();
        downTestRoot = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        encoding = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/python/project/ui/customizer/Bundle").getString("MNE_ProjectFolder").charAt(0));
        jLabel1.setLabelFor(projectLocation);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/python/project/ui/customizer/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("CTL_ProjectFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabel1, gridBagConstraints);

        projectLocation.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(projectLocation, gridBagConstraints);
        projectLocation.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_projectLocation")); // NOI18N

        sourceRootsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/python/project/ui/customizer/Bundle").getString("MNE_SourceRoots").charAt(0));
        jLabel2.setLabelFor(sourceRoots);
        jLabel2.setText(bundle.getString("CTL_SourceRoots")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        sourceRootsPanel.add(jLabel2, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(450, 150));

        sourceRoots.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Package Folder", "Label"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(sourceRoots);
        sourceRoots.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_sourceRoots")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        sourceRootsPanel.add(jScrollPane1, gridBagConstraints);

        addSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/python/project/ui/customizer/Bundle").getString("MNE_AddSourceRoot").charAt(0));
        addSourceRoot.setText(bundle.getString("CTL_AddSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        sourceRootsPanel.add(addSourceRoot, gridBagConstraints);
        addSourceRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_addSourceRoot")); // NOI18N

        removeSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/python/project/ui/customizer/Bundle").getString("MNE_RemoveSourceRoot").charAt(0));
        removeSourceRoot.setText(bundle.getString("CTL_RemoveSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        sourceRootsPanel.add(removeSourceRoot, gridBagConstraints);
        removeSourceRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_removeSourceRoot")); // NOI18N

        upSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/python/project/ui/customizer/Bundle").getString("MNE_UpSourceRoot").charAt(0));
        upSourceRoot.setText(bundle.getString("CTL_UpSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        sourceRootsPanel.add(upSourceRoot, gridBagConstraints);
        upSourceRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_upSourceRoot")); // NOI18N

        downSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/python/project/ui/customizer/Bundle").getString("MNE_DownSourceRoot").charAt(0));
        downSourceRoot.setText(bundle.getString("CTL_DownSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        sourceRootsPanel.add(downSourceRoot, gridBagConstraints);
        downSourceRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_downSourceRoot")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(sourceRootsPanel, gridBagConstraints);

        testRootsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel3.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/python/project/ui/customizer/Bundle").getString("MNE_TestRoots").charAt(0));
        jLabel3.setLabelFor(testRoots);
        jLabel3.setText(bundle.getString("CTL_TestRoots")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        testRootsPanel.add(jLabel3, gridBagConstraints);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(450, 150));

        testRoots.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Package Folder", "Label"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(testRoots);
        testRoots.getAccessibleContext().setAccessibleDescription("null");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        testRootsPanel.add(jScrollPane2, gridBagConstraints);
        jScrollPane2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_testRoots")); // NOI18N

        addTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/python/project/ui/customizer/Bundle").getString("MNE_AddTestRoot").charAt(0));
        addTestRoot.setText(bundle.getString("CTL_AddTestRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        testRootsPanel.add(addTestRoot, gridBagConstraints);
        addTestRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_addTestRoot")); // NOI18N

        removeTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/python/project/ui/customizer/Bundle").getString("MNE_RemoveTestRoot").charAt(0));
        removeTestRoot.setText(bundle.getString("CTL_RemoveTestRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        testRootsPanel.add(removeTestRoot, gridBagConstraints);
        removeTestRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_removeTestRoot")); // NOI18N

        upTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/python/project/ui/customizer/Bundle").getString("MNE_UpTestRoot").charAt(0));
        upTestRoot.setText(bundle.getString("CTL_UpTestRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        testRootsPanel.add(upTestRoot, gridBagConstraints);
        upTestRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_upTestRoot")); // NOI18N

        downTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/python/project/ui/customizer/Bundle").getString("MNE_DownTestRoot").charAt(0));
        downTestRoot.setText(bundle.getString("CTL_DownTestRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        testRootsPanel.add(downTestRoot, gridBagConstraints);
        downTestRoot.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizerSources_downTestRoot")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(testRootsPanel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel5.setLabelFor(encoding);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "TXT_Encoding")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 12);
        jPanel1.add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleDescription("null");

        encoding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        jPanel1.add(encoding, gridBagConstraints);
        encoding.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_Encoding")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceRoot;
    private javax.swing.JButton addTestRoot;
    private javax.swing.JButton downSourceRoot;
    private javax.swing.JButton downTestRoot;
    private javax.swing.JComboBox encoding;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JButton removeSourceRoot;
    private javax.swing.JButton removeTestRoot;
    private javax.swing.JTable sourceRoots;
    private javax.swing.JPanel sourceRootsPanel;
    private javax.swing.JTable testRoots;
    private javax.swing.JPanel testRootsPanel;
    private javax.swing.JButton upSourceRoot;
    private javax.swing.JButton upTestRoot;
    // End of variables declaration//GEN-END:variables
    
}
