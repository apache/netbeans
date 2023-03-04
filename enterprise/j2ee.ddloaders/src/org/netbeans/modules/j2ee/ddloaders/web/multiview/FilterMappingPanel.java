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
package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.openide.util.NbBundle;

/**
 * FilterMappingPanel.java
 * Panel for adding/editing filter mapping data
 *
 * Created on January 24, 2005, 4:22 PM
 * @author  mkuchtiak
 */
public class FilterMappingPanel extends javax.swing.JPanel {

    private FilterMapping fm;
    private boolean hasFilterNames = true;
    private boolean hasServletNames = true;
    private static final String[] dispatcherTypes = {
        "REQUEST", "FORWARD", "INCLUDE", "ERROR" //NOI18N
    };
    
    private boolean isMultipleUrlServlet;

    /** Creates new form FilterMappingPanel */
    public FilterMappingPanel(FilterMapping fm, String[] filterNames, 
            String[] servletNames, boolean supportsMultiple)
    {
        this.fm = fm;
        isMultipleUrlServlet = supportsMultiple;
        initComponents();
        jCheckBox1.setText(dispatcherTypes[0]);
        jCheckBox2.setText(dispatcherTypes[1]);
        jCheckBox3.setText(dispatcherTypes[2]);
        jCheckBox4.setText(dispatcherTypes[3]);
        jCheckBox1.setMnemonic(dispatcherTypes[0].charAt(0));
        jCheckBox2.setMnemonic(dispatcherTypes[1].charAt(0));
        jCheckBox3.setMnemonic(dispatcherTypes[2].charAt(0));
        jCheckBox4.setMnemonic(dispatcherTypes[3].charAt(0));

        // fill CB1 with filter names
        if (filterNames == null || filterNames.length == 0) {
            filterNames = new String[1];
            filterNames[0] = NbBundle.getMessage(FilterMappingPanel.class, "LBL_no_filters");
            hasFilterNames = false;
        }
        for (int i = 0; i < filterNames.length; i++) {
            filterNameCB.addItem(filterNames[i]);
        }

        String filterName = fm.getFilterName();
        if (filterName != null) {
            filterNameCB.setSelectedItem(filterName);
        }

        // fill CB2 with servlet names
        if (servletNames == null || servletNames.length == 0) {
            servletNames = new String[1];
            servletNames[0] = NbBundle.getMessage(FilterMappingPanel.class, "LBL_no_servlets");
            hasServletNames = false;
        }

        myServletNameList.setModel( new DefaultComboBoxModel(servletNames));
        if (!hasServletNames) {
            jRadioButton2.setEnabled(false);
        }

        if ( !isMultipleUrlServlet){
            myServletNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            jPanel2.remove( myComment );
        }
        String[] selectedServletNames = null;
        String[] urlPatterns = null;
        try {
            selectedServletNames= fm.getServletNames();
        }
        catch (VersionNotSupportedException ex) {
            if ( fm.getServletName() != null ){
                selectedServletNames = new String[]{ fm.getServletName()};
            }
        }
        try {
            urlPatterns = fm.getUrlPatterns();
        }
        catch (VersionNotSupportedException ex) {
            if ( fm.getUrlPattern() != null ){
                urlPatterns = new String[]{ fm.getUrlPattern()};
            }
        }
        
        if (selectedServletNames != null && selectedServletNames.length >0 ) {
            jRadioButton2.setSelected(true);
            urlTF.setEnabled(false);
            List<Integer> indeces = new LinkedList<Integer>( );
            Set<String> servletSet = new HashSet<String>( Arrays.asList( 
                    selectedServletNames));
            for( int i=0; i<servletNames.length ;i++ ){
                if ( servletSet.contains( servletNames[i])){
                    myServletNameList.addSelectionInterval( i, i);
                }
            }
        } else {
            if ( urlPatterns!= null){
                StringBuilder builder = new StringBuilder();
                for( String url : urlPatterns ){
                    builder.append(url);
                    builder.append(", ");            // NOI18N
                }
                if ( builder.length() >0 ){
                    urlTF.setText( builder.substring( 0, builder.length()-2));
                }
            }
            jRadioButton1.setSelected(true);
            myServletNameList.setEnabled(false);
        }

        try {
            String[] dispTypes = fm.getDispatcher();
            for (int i = 0; i < dispTypes.length; i++) {
                if (dispatcherTypes[0].equals(dispTypes[i])) {
                    jCheckBox1.setSelected(true);
                } else if (dispatcherTypes[1].equals(dispTypes[i])) {
                    jCheckBox2.setSelected(true);
                } else if (dispatcherTypes[2].equals(dispTypes[i])) {
                    jCheckBox3.setSelected(true);
                } else if (dispatcherTypes[3].equals(dispTypes[i])) {
                    jCheckBox4.setSelected(true);
                }
            }
        } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {
        }
        
    }

    javax.swing.JTextField getUrlTF() {
        return urlTF;
    }
    
    JList  getServletNamesList(){
        return myServletNameList;
    }

    javax.swing.JRadioButton getUrlRB() {
        return jRadioButton1;
    }

    javax.swing.JRadioButton getServletNameRB() {
        return jRadioButton2;
    }

    String getUrlPattern() {
        return (jRadioButton1.isSelected() ? urlTF.getText().trim() : null);
    }

    String[] getServletNames() {
        if ( jRadioButton2.isSelected()){
            Object[] selected = myServletNameList.getSelectedValues();
            return Arrays.asList(selected).toArray( new String[ selected.length] );
        }
        return null;
    }

    String getFilterName() {
        return (hasFilterNames ? (String) filterNameCB.getSelectedItem() : null);
    }

    String[] getDispatcherTypes() {
        List<String> list = new ArrayList<String>(4);
        if (jCheckBox1.isSelected()) {
            list.add(dispatcherTypes[0]);
        }
        if (jCheckBox2.isSelected()) {
            list.add(dispatcherTypes[1]);
        }
        if (jCheckBox3.isSelected()) {
            list.add(dispatcherTypes[2]);
        }
        if (jCheckBox4.isSelected()) {
            list.add(dispatcherTypes[3]);
        }
        String[] ret = new String[list.size()];
        list.toArray(ret);
        return ret;
    }

    boolean hasFilterNames() {
        return hasFilterNames;
    }

    boolean hasServletNames() {
        return hasServletNames;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        filterNameCB = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        urlTF = new javax.swing.JTextField();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        myComment = new javax.swing.JLabel();
        myScrollPane = new javax.swing.JScrollPane();
        myServletNameList = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(filterNameCB);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "LBL_filterName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(filterNameCB, gridBagConstraints);
        filterNameCB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_filter_name")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "LBL_dispatcherTypes")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel4, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.add(jCheckBox1);
        jCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_req_box")); // NOI18N

        jPanel1.add(jCheckBox2);
        jCheckBox2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_fw_box")); // NOI18N

        jPanel1.add(jCheckBox3);
        jCheckBox3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_inc_box")); // NOI18N

        jPanel1.add(jCheckBox4);
        jCheckBox4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_err_box")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(jPanel1, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        urlTF.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanel2.add(urlTF, gridBagConstraints);
        urlTF.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_filter_mapping_url_text_field")); // NOI18N
        urlTF.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_filter_mapping_url_text_field")); // NOI18N

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton1, org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "LBL_urlPattern")); // NOI18N
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 0);
        jPanel2.add(jRadioButton1, gridBagConstraints);
        jRadioButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_url_pattern")); // NOI18N

        buttonGroup1.add(jRadioButton2);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton2, org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "LBL_servletName")); // NOI18N
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 5, 0);
        jPanel2.add(jRadioButton2, gridBagConstraints);
        jRadioButton2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "ACSD_servlet_name")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myComment, org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "HINT_urlPatterns")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel2.add(myComment, gridBagConstraints);

        myServletNameList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        myServletNameList.setVisibleRowCount(2);
        myScrollPane.setViewportView(myServletNameList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(myScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(jPanel2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(FilterMappingPanel.class, "LBL_applyTo")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        urlTF.setEnabled(false);
        myServletNameList.setEnabled(true);
        jRadioButton2.requestFocus();
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        urlTF.setEnabled(true);
        myServletNameList.setEnabled(false);
        urlTF.requestFocus();
    }//GEN-LAST:event_jRadioButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox filterNameCB;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JLabel myComment;
    private javax.swing.JScrollPane myScrollPane;
    private javax.swing.JList myServletNameList;
    private javax.swing.JTextField urlTF;
    // End of variables declaration//GEN-END:variables
}
