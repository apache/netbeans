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
package org.netbeans.modules.base64;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Base64;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays the Base64 Tool.
 */
@TopComponent.Description(
        preferredID = "Base64TopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "org.netbeans.modules.base64.Base64TopComponent")
@ActionReference(path = "Menu/Window/Tools", position = 605)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_Base64Action",
        preferredID = "Base64TopComponent"
)
@Messages({
    "CTL_Base64Action=Base64 Tool",
    "CTL_Base64TopComponent=Base64 Tool",
    "HINT_Base64TopComponent=This is a Base64 Tool window"
})
public final class Base64TopComponent extends TopComponent {

    private final SpinnerNumberModel marginModel = new SpinnerNumberModel(76, 4, 256, 4);

    public Base64TopComponent() {
        initComponents();
        setName(Bundle.CTL_Base64TopComponent());
        setToolTipText(Bundle.HINT_Base64TopComponent());
        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, taText.getFont().getSize());

        taText.addFocusListener(new ListenerAttacher(taText, encoder));
        taText.setFont(mono);

        taBase64.addFocusListener(new ListenerAttacher(taBase64, decoder));
        taBase64.setFont(mono);

        marginModel.addChangeListener((e) -> encode());
        spMargin.setModel(marginModel);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        lbText = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taText = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        lbBase64 = new javax.swing.JLabel();
        cbWrap = new javax.swing.JCheckBox();
        spMargin = new javax.swing.JSpinner();
        jScrollPane2 = new javax.swing.JScrollPane();
        taBase64 = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerSize(7);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.5);

        lbText.setLabelFor(taText);
        org.openide.awt.Mnemonics.setLocalizedText(lbText, org.openide.util.NbBundle.getMessage(Base64TopComponent.class, "Base64TopComponent.lbText.text")); // NOI18N

        taText.setColumns(20);
        taText.setRows(5);
        jScrollPane1.setViewportView(taText);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lbText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        jSplitPane1.setTopComponent(jPanel1);

        lbBase64.setLabelFor(taBase64);
        org.openide.awt.Mnemonics.setLocalizedText(lbBase64, org.openide.util.NbBundle.getMessage(Base64TopComponent.class, "Base64TopComponent.lbBase64.text")); // NOI18N

        cbWrap.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbWrap, org.openide.util.NbBundle.getMessage(Base64TopComponent.class, "Base64TopComponent.cbWrap.text")); // NOI18N
        cbWrap.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cbWrapStateChanged(evt);
            }
        });

        taBase64.setColumns(20);
        taBase64.setRows(5);
        jScrollPane2.setViewportView(taBase64);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(lbBase64, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cbWrap)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spMargin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbBase64, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbWrap)
                    .addComponent(spMargin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cbWrapStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cbWrapStateChanged
        spMargin.setEnabled(cbWrap.isSelected());
        encode();
    }//GEN-LAST:event_cbWrapStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbWrap;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel lbBase64;
    private javax.swing.JLabel lbText;
    private javax.swing.JSpinner spMargin;
    private javax.swing.JTextArea taBase64;
    private javax.swing.JTextArea taText;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        taText.requestFocus();
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    private static class ListenerAttacher implements FocusListener {

        private final JTextArea ta;
        private final DocumentListener l;

        public ListenerAttacher(JTextArea ta, DocumentListener l) {
            this.ta = ta;
            this.l = l;
        }

        @Override
        public void focusGained(FocusEvent fe) {
            ta.getDocument().addDocumentListener(l);
            ta.selectAll();
        }

        @Override
        public void focusLost(FocusEvent fe) {
            ta.getDocument().removeDocumentListener(l);
        }

    }

    private static final byte[] CRLF = new byte[] {'\r', '\n'};
    private void encode() {
        int wrap = cbWrap.isSelected() ? marginModel.getNumber().intValue() : 0;
        Base64.Encoder encoder = Base64.getMimeEncoder(wrap, CRLF);
        String text = taText.getText();
        String base64 = encoder.encodeToString(text.getBytes());
        taBase64.setText(base64);
    }

    private final DocumentListener encoder = new DocumentListener() {

        @Override
        public void insertUpdate(DocumentEvent de) {
            encode();
        }

        @Override
        public void removeUpdate(DocumentEvent de) {
            encode();
        }

        @Override
        public void changedUpdate(DocumentEvent de) {
            encode();
        }
    };

    private final DocumentListener decoder = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent de) {
            decode();
        }

        @Override
        public void removeUpdate(DocumentEvent de) {
            decode();
        }

        @Override
        public void changedUpdate(DocumentEvent de) {
            decode();
        }

        private void decode() {
            String base64 = taBase64.getText();
            String text = taText.getText();
            boolean isError = false;
            try {
                text = new String(Base64.getMimeDecoder().decode(base64));
            } catch (IllegalArgumentException ex) {
                isError = true;
            }
            taText.setText(text);
            Color fg = UIManager.getColor(isError ? "nb.errorForeground" : "TextArea.foreground"); //NOI18N
            taBase64.setForeground(fg);
        }
    };
}
