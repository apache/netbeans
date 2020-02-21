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

package org.netbeans.modules.cnd.remote.ui.wizard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.ui.impl.RemoteSyncNotifierImpl;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.openide.util.NbBundle;

@SuppressWarnings("rawtypes") // UI editor produces code with tons of rawtypes warnings
/*package*/ final class CreateHostVisualPanel3 extends CreateHostVisualPanelBase {

    public CreateHostVisualPanel3(CreateHostData data) {
        this.data = data;
        initComponents();
        adjustPreferredSize(); // otherwise GUI editor spoils the form
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CreateHostVisualPanel3.Title");//NOI18N
    }

    private final CreateHostData data;
    private CompilerSetManager compilerSetManager;

    @SuppressWarnings("unchecked")
    void init() {
        textHostDisplayName.setText(data.getExecutionEnvironment().getDisplayName());
        // here we know for sure that it is created and initialized
        compilerSetManager = data.getCacheManager().getCompilerSetManagerCopy(data.getExecutionEnvironment(), false);
        labelPlatformValue.setText(PlatformTypes.toString(compilerSetManager.getPlatform()));
        labelUsernameValue.setText(data.getExecutionEnvironment().getUser());
        labelHostnameValue.setText(data.getExecutionEnvironment().getHost());
        cbDefaultToolchain.setModel(new DefaultComboBoxModel(compilerSetManager.getCompilerSets().toArray()));
        cbDefaultToolchain.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel out = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    out.setText(""); //NOI18N
                } else if (value instanceof String) {
                    // BasicComboBoxUI replaces null with empty string
                    assert ((String) value).trim().isEmpty();
                    out.setText(""); //NOI18N
                } else {
                    CompilerSet cset = (CompilerSet) value;
                    out.setText(cset.getName());
                }
                return out;
            }
        });
        boolean selected = false;
        for(CompilerSet cs : compilerSetManager.getCompilerSets()) {
            if (compilerSetManager.isDefaultCompilerSet(cs)) {
                cbDefaultToolchain.setSelectedItem(cs);
                selected = true;
                break;
            }
        }
        if (!selected && compilerSetManager.getCompilerSets().size() > 0) {
            cbDefaultToolchain.setSelectedItem(0);
        }
        cbDefaultToolchain.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                compilerSetManager.setDefault((CompilerSet) cbDefaultToolchain.getSelectedItem());
            }
        });
        List<CompilerSet> sets2 = compilerSetManager.getCompilerSets();
        final String html = "<html>"; // NOI18N
        StringBuilder st = new StringBuilder(html);
        for (CompilerSet set : sets2) {
            if (st.length() > html.length()) {
                st.append("<br>\n"); //NOI18N
            }
            st.append(set.getName()).append(" (").append(set.getDirectory()).append(")");//NOI18N
        }
        RemoteServerRecord record = (RemoteServerRecord) ServerList.get(data.getExecutionEnvironment());
        if (record != null && record.hasProblems()) {
            st.append("<br><br>\n"); // NOI18N
            st.append("<font color=red>"); // NOI18N
            st.append(record.getProblems().replace("\n", "<br>\n")); // NOI18N
        }
        st.append("</html>"); // NOI18N
        jTextArea1.setEditorKit(new HTMLEditorKit());
        jTextArea1.setBackground(getBackground());
        jTextArea1.setForeground(getForeground());
        jTextArea1.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        jTextArea1.setText(st.toString());

        RemoteSyncNotifierImpl.arrangeComboBox(cbSyncMode, data.getExecutionEnvironment());
        cbSyncMode.setSelectedItem(record.getSyncFactory());
    }

    String getHostDisplayName() {
        return textHostDisplayName.getText();
    }

    RemoteSyncFactory getRemoteSyncFactory() {
        return (RemoteSyncFactory) cbSyncMode.getSelectedItem();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        syncButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        textHostDisplayName = new javax.swing.JTextField();
        labelPlatform = new javax.swing.JLabel();
        labelHostname = new javax.swing.JLabel();
        labelUsername = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cbDefaultToolchain = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JEditorPane();
        labelPlatformValue = new javax.swing.JLabel();
        labelHostnameValue = new javax.swing.JLabel();
        labelUsernameValue = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbSyncMode = new javax.swing.JComboBox();

        setPreferredSize(new java.awt.Dimension(534, 409));
        setRequestFocusEnabled(false);

        jLabel1.setLabelFor(textHostDisplayName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.jLabel1.text")); // NOI18N

        labelPlatform.setLabelFor(labelPlatformValue);
        labelPlatform.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.labelPlatform.text")); // NOI18N

        labelHostname.setLabelFor(labelHostnameValue);
        labelHostname.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.labelHostname.text")); // NOI18N

        labelUsername.setLabelFor(labelUsernameValue);
        labelUsername.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.labelUsername.text")); // NOI18N

        jLabel2.setLabelFor(jTextArea1);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.jLabel2.text")); // NOI18N

        jLabel3.setLabelFor(cbDefaultToolchain);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.jLabel3.text")); // NOI18N

        jTextArea1.setEditable(false);
        jTextArea1.setOpaque(false);
        jScrollPane1.setViewportView(jTextArea1);

        org.openide.awt.Mnemonics.setLocalizedText(labelPlatformValue, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.labelPlatformValue.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelHostnameValue, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.labelHostnameValue.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelUsernameValue, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.labelUsernameValue.text")); // NOI18N

        jLabel4.setLabelFor(cbSyncMode);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel3.class, "CreateHostVisualPanel3.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textHostDisplayName, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelPlatform)
                    .addComponent(labelHostname)
                    .addComponent(labelUsername))
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelUsernameValue)
                    .addComponent(labelHostnameValue)
                    .addComponent(labelPlatformValue))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbSyncMode, 0, 376, Short.MAX_VALUE)
                    .addComponent(cbDefaultToolchain, 0, 376, Short.MAX_VALUE)))
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(textHostDisplayName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPlatform)
                    .addComponent(labelPlatformValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelHostname)
                    .addComponent(labelHostnameValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelUsername)
                    .addComponent(labelUsernameValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbDefaultToolchain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbSyncMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(29, 29, 29))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbDefaultToolchain;
    private javax.swing.JComboBox cbSyncMode;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JEditorPane jTextArea1;
    private javax.swing.JLabel labelHostname;
    private javax.swing.JLabel labelHostnameValue;
    private javax.swing.JLabel labelPlatform;
    private javax.swing.JLabel labelPlatformValue;
    private javax.swing.JLabel labelUsername;
    private javax.swing.JLabel labelUsernameValue;
    private javax.swing.ButtonGroup syncButtonGroup;
    private javax.swing.JTextField textHostDisplayName;
    // End of variables declaration//GEN-END:variables
}

