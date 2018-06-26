/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.javascript.debugger.breakpoints.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.web.javascript.debugger.breakpoints.DOMBreakpoint;
import org.netbeans.modules.web.javascript.debugger.breakpoints.DOMNode;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.netbeans.spi.debugger.ui.Controller;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Martin
 */
public class DOMBreakpointCustomizer extends javax.swing.JPanel implements ControllerProvider, HelpCtx.Provider {

    private final DOMBreakpoint db;
    private final String origNodePathNames;
    private boolean createBreakpoint;
    private final CustomizerController controller;
    
    private static DOMBreakpoint createBreakpoint() {
        Node node = Utilities.actionsGlobalContext().lookup(Node.class);
        DOMNode dn;
        URL url;
        if (node != null) {
            dn = DOMNode.create(node);
            url = DOMNode.findURL(node);
        } else {
            dn = DOMNode.create("[\u0003-1,]"); // root
            FileObject fo = Utilities.actionsGlobalContext().lookup(FileObject.class);
            if (fo == null) {
                fo = EditorContextDispatcher.getDefault().getMostRecentFile();
            }
            if (fo != null) {
                url = fo.toURL();
            } else {
                
                url = null;
            }
        }
        DOMBreakpoint b = new DOMBreakpoint(url, dn);
        return b;
    }
    
    /**
     * Creates new form LineBreakpointCustomizer
     */
    public DOMBreakpointCustomizer() {
        this (createBreakpoint ());
        createBreakpoint = true;
    }
    
    /**
     * Creates new form LineBreakpointCustomizer
     */
    public DOMBreakpointCustomizer(DOMBreakpoint db) {
        this.db = db;
        initComponents();
        nodeTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                controller.checkValid();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                controller.checkValid();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                controller.checkValid();
            }
        });
        controller = new CustomizerController();
        DOMNode node = db.getNode();
        origNodePathNames = (node != null) ? node.getNodePathNames() : "";
        nodeTextField.setText(origNodePathNames);
        URL url = db.getURL();
        String urlStr;
        if (url != null) {
            urlStr = url.toExternalForm();
            if (urlStr.startsWith("file:")) {
                try {
                    urlStr = Utilities.toFile(url.toURI()).getAbsolutePath();
                } catch (URISyntaxException ex) {}
            }
        } else {
            urlStr = "";
        }
        fileTextField.setText(urlStr);
        onSubtreeModifCheckBox.setSelected(db.isOnSubtreeModification());
        onAttrModifCheckBox.setSelected(db.isOnAttributeModification());
        onNodeRemoveCheckBox.setSelected(db.isOnNodeRemoval());
    }
    
    @Override
    public Controller getController() {
        return controller;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        nodeTextField = new javax.swing.JTextField();
        onSubtreeModifCheckBox = new javax.swing.JCheckBox();
        onAttrModifCheckBox = new javax.swing.JCheckBox();
        onNodeRemoveCheckBox = new javax.swing.JCheckBox();
        fileLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();

        jLabel2.setText(org.openide.util.NbBundle.getMessage(DOMBreakpointCustomizer.class, "DOMBreakpointCustomizer.jLabel2.text")); // NOI18N

        nodeTextField.setText(org.openide.util.NbBundle.getMessage(DOMBreakpointCustomizer.class, "DOMBreakpointCustomizer.nodeTextField.text")); // NOI18N
        nodeTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DOMBreakpointCustomizer.class, "DOMBreakpointCustomizer.nodeTextField.toolTipText")); // NOI18N

        onSubtreeModifCheckBox.setText(org.openide.util.NbBundle.getMessage(DOMBreakpointCustomizer.class, "DOMBreakpointCustomizer.onSubtreeModifCheckBox.text")); // NOI18N
        onSubtreeModifCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onSubtreeModifCheckBoxActionPerformed(evt);
            }
        });

        onAttrModifCheckBox.setText(org.openide.util.NbBundle.getMessage(DOMBreakpointCustomizer.class, "DOMBreakpointCustomizer.onAttrModifCheckBox.text")); // NOI18N
        onAttrModifCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onAttrModifCheckBoxActionPerformed(evt);
            }
        });

        onNodeRemoveCheckBox.setText(org.openide.util.NbBundle.getMessage(DOMBreakpointCustomizer.class, "DOMBreakpointCustomizer.onNodeRemoveCheckBox.text")); // NOI18N
        onNodeRemoveCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onNodeRemoveCheckBoxActionPerformed(evt);
            }
        });

        fileLabel.setText(org.openide.util.NbBundle.getMessage(DOMBreakpointCustomizer.class, "DOMBreakpointCustomizer.fileLabel.text")); // NOI18N

        fileTextField.setText(org.openide.util.NbBundle.getMessage(DOMBreakpointCustomizer.class, "DOMBreakpointCustomizer.fileTextField.text")); // NOI18N
        fileTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DOMBreakpointCustomizer.class, "DOMBreakpointCustomizer.fileTextField.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(onSubtreeModifCheckBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(onAttrModifCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                    .addComponent(onNodeRemoveCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(fileLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nodeTextField)
                            .addComponent(fileTextField))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(nodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileLabel)
                    .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(onSubtreeModifCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(onAttrModifCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(onNodeRemoveCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void onSubtreeModifCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onSubtreeModifCheckBoxActionPerformed
        controller.checkValid();
    }//GEN-LAST:event_onSubtreeModifCheckBoxActionPerformed

    private void onAttrModifCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onAttrModifCheckBoxActionPerformed
        // TODO add your handling code here:
        controller.checkValid();
    }//GEN-LAST:event_onAttrModifCheckBoxActionPerformed

    private void onNodeRemoveCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onNodeRemoveCheckBoxActionPerformed
        // TODO add your handling code here:
        controller.checkValid();
    }//GEN-LAST:event_onNodeRemoveCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField nodeTextField;
    private javax.swing.JCheckBox onAttrModifCheckBox;
    private javax.swing.JCheckBox onNodeRemoveCheckBox;
    private javax.swing.JCheckBox onSubtreeModifCheckBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("NetbeansDebuggerDOMBreakpointJavaScript"); // NOI18N
    }
    
    private class CustomizerController implements Controller {
        
        PropertyChangeSupport pchs = new PropertyChangeSupport(this);

        @Override
        public boolean ok() {
            String nodePathNames = nodeTextField.getText().trim();
            if (!origNodePathNames.equals(nodePathNames)) {
                DOMNode node = DOMNode.create(nodePathNames);
                db.setNode(node);
            }
            db.setOnSubtreeModification(onSubtreeModifCheckBox.isSelected());
            db.setOnAttributeModification(onAttrModifCheckBox.isSelected());
            db.setOnNodeRemoval(onNodeRemoveCheckBox.isSelected());
            if (createBreakpoint) {
                DebuggerManager.getDebuggerManager().addBreakpoint(db);
            }
            return true;
        }

        @Override
        public boolean cancel() {
            return true;
        }

        @Override
        public boolean isValid() {
            if (db.getNode() == null || nodeTextField.getText().trim().isEmpty()) {
                return false;
            }
            if (onAttrModifCheckBox.isSelected() ||
                onSubtreeModifCheckBox.isSelected() ||
                onNodeRemoveCheckBox.isSelected()) {
                
                return true;
            } else {
                return false;
            }
        }
        
        @NbBundle.Messages({
            "Warning_NoDOMNode=No DOM node is selected",
            "Warning_NoDOMModification=At least one modification type needs to be selected"
        })
        private void checkValid() {
            if (db.getNode() == null) {
                firePropertyChange(NotifyDescriptor.PROP_ERROR_NOTIFICATION, null, Bundle.Warning_NoDOMNode());
                firePropertyChange(Controller.PROP_VALID, null, Boolean.FALSE);
                return;
            }
            if (nodeTextField.getText().trim().isEmpty()) {
                firePropertyChange(NotifyDescriptor.PROP_ERROR_NOTIFICATION, null, Bundle.Warning_NoDOMNode());
                firePropertyChange(Controller.PROP_VALID, null, Boolean.FALSE);
                return ;
            }
            if (onAttrModifCheckBox.isSelected() ||
                onSubtreeModifCheckBox.isSelected() ||
                onNodeRemoveCheckBox.isSelected()) {
                
                firePropertyChange(NotifyDescriptor.PROP_ERROR_NOTIFICATION, null, null);
                firePropertyChange(Controller.PROP_VALID, null, Boolean.TRUE);
            } else {
                firePropertyChange(NotifyDescriptor.PROP_ERROR_NOTIFICATION, null, Bundle.Warning_NoDOMModification());
                firePropertyChange(Controller.PROP_VALID, null, Boolean.FALSE);
            }
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pchs.addPropertyChangeListener(l);
            checkValid();
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            pchs.removePropertyChangeListener(l);
        }
        
        private void firePropertyChange(String propName, Object oldValue, Object newValue) {
            pchs.firePropertyChange(propName, oldValue, newValue);
        }
        
    }
}
