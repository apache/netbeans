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

package org.netbeans.modules.spring.webmvc.editor;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author alexeybutenko
 */
public class AddBeanPanelVisual extends javax.swing.JPanel {

    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport statusLine;
    private boolean dialogOK = false;
    private AddBeanPanel panel;
    private FileObject fileObject;
    private final AtomicBoolean classFound = new AtomicBoolean(false);
    private static final RequestProcessor RP = new RequestProcessor();

    /** Creates new form AddBeanPanelVisual */
    public AddBeanPanelVisual(AddBeanPanel panel) {
        this.panel = panel;
        fileObject = NbEditorUtilities.getFileObject(panel.getDocument());
        initComponents();
        scanningLabel.setVisible(false);
        idTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                validateInput();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                validateInput();
            }

        });

        classNameTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                validateInput();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                validateInput();
            }
        });
    }


    public boolean showDialog() {
        String id = panel.getId();
        String className = panel.getClassName();
        if (className !=null) {
            classNameTextField.setEditable(false);
        } else {
            className ="";  //NOI18N
        }
        idTextField.setText(id);
        classNameTextField.setText(className);
        String displayName = ""; // NOI18N
        try {
            displayName = NbBundle.getMessage(AddBeanPanelVisual.class, "TTL_Add_Bean_Panel");
        }
        catch (Exception e) {}
        descriptor = new DialogDescriptor
                (this, displayName, true,   // NOI18N
                 DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                 new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                            collectInput();
                            dialogOK = true;
                        }
                        dialog.dispose();
                     }

                  }
        );
        statusLine = descriptor.createNotificationLineSupport();
        validateInput();
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();

        return dialogOK;

    }

    private void collectInput() {
        panel.setClassName(classNameTextField.getText());
        panel.setId(idTextField.getText());
    }

    private void validateInput() {
        validateInput(true);
    }

    private void validateInput(boolean validateClass) {
        if (descriptor == null)
            return;
        if (idTextField.getText().length() < 1) {
            statusLine.setInformationMessage(NbBundle.getMessage(AddBeanPanelVisual.class,"Error_Empty_ID")); // NOI18N
            descriptor.setValid(false);
            return;
        }

        if (idExist()) {
            statusLine.setErrorMessage(NbBundle.getMessage(AddBeanPanelVisual.class, "Error_not_uniq_ID")); // NOI18N
            descriptor.setValid(false);
            return;
        }
        if (classNameTextField.getText().length() < 1) {
            statusLine.setInformationMessage(NbBundle.getMessage(AddBeanPanelVisual.class,"Error_Empty_Class")); // NOI18N
            descriptor.setValid(false);
            return;
        }
        if (beanExist()) {
            statusLine.setErrorMessage(NbBundle.getMessage(AddBeanPanelVisual.class, "Error_Bean_Already_exist")); // NOI18N
            descriptor.setValid(false);
            return;
        }
        if (validateClass) {
            scanningLabel.setVisible(SourceUtils.isScanInProgress());
            RP.submit(new Runnable() {
                @Override
                public void run() {
                    validClass();
                }
            });
        }
        if (!classFound.get()) {
            statusLine.setErrorMessage(NbBundle.getMessage(AddBeanPanelVisual.class, "Error_No_Such_class")); // NOI18N
            descriptor.setValid(false);
            return;
        }

        statusLine.clearMessages();
        descriptor.setValid(true);
    }

    private boolean idExist() {
        SpringScope scope = SpringScope.getSpringScope(fileObject);
        final String id = idTextField.getText();
        final boolean[] found = {false};
        for (SpringConfigModel model: scope.getAllConfigModels()) {
            try {
                model.runReadAction(new Action<SpringBeans>() {

                    public void run(SpringBeans beans) {
                        SpringBean bean = beans.findBean(id);
                        if (bean !=null)
                            found[0]=true;
                    }
                });
                if (found[0]) {
                    return true;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return found[0];
    }

    private boolean beanExist() {
        final boolean[] found = {false};
        SpringScope scope = SpringScope.getSpringScope(fileObject);
        final String className = classNameTextField.getText();
        if (className == null || "".equals(className)) {
            return false;
        }
        for (SpringConfigModel model: scope.getAllConfigModels()) {
            try {
                model.runReadAction(new Action<SpringBeans>() {

                    public void run(SpringBeans beans) {
                        for (SpringBean bean : beans.getBeans()){
                            if (className.equals(bean.getClassName())) {
                                found[0]=true;
                                break;
                            }
                        }
                    }
                });
                if (found[0]) {
                    return true;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return found[0];
    }

    private void validClass() {
        JavaSource js = JavaSource.create(ClasspathInfo.create(fileObject));
        if (js == null) {
            return;
        }
        try {
            ClassSeeker laterSeeker = new ClassSeeker();
            Future<Void> seekingTask = js.runWhenScanFinished(laterSeeker, true);
            if (seekingTask.isDone()) {
                classFound.set(laterSeeker.isClassFound());
                return;
            }
            ClassSeeker promptSeeker = new ClassSeeker();
            js.runUserActionTask(promptSeeker, true);
            classFound.set(promptSeeker.isClassFound());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private class ClassSeeker implements Task<CompilationController> {

        private final AtomicBoolean found = new AtomicBoolean(false);

        public boolean isClassFound() {
            return found.get();
        }

        @Override
        public void run(CompilationController parameter) throws Exception {
            found.set(parameter.getElements().getTypeElement(classNameTextField.getText()) != null);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    scanningLabel.setVisible(SourceUtils.isScanInProgress());
                    validateInput(false);
                }
            });
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

        jLabel1 = new javax.swing.JLabel();
        idTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        classNameTextField = new javax.swing.JTextField();
        scanningLabel = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(420, 120));
        setRequestFocusEnabled(false);

        jLabel1.setLabelFor(idTextField);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(AddBeanPanelVisual.class, "AddBeanPanelVisual.jLabel1.text")); // NOI18N

        idTextField.setText(org.openide.util.NbBundle.getMessage(AddBeanPanelVisual.class, "AddBeanPanelVisual.idTextField.text")); // NOI18N
        idTextField.setMinimumSize(new java.awt.Dimension(200, 27));
        idTextField.setPreferredSize(new java.awt.Dimension(450, 27));
        idTextField.setRequestFocusEnabled(false);

        jLabel2.setLabelFor(classNameTextField);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(AddBeanPanelVisual.class, "AddBeanPanelVisual.jLabel2.text")); // NOI18N

        classNameTextField.setText(org.openide.util.NbBundle.getMessage(AddBeanPanelVisual.class, "AddBeanPanelVisual.classNameTextField.text")); // NOI18N
        classNameTextField.setPreferredSize(new java.awt.Dimension(450, 27));

        scanningLabel.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        scanningLabel.setText(org.openide.util.NbBundle.getMessage(AddBeanPanelVisual.class, "AddBeanPanelVisual.scanningLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scanningLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(idTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                            .addComponent(classNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(idTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 18, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(classNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scanningLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField classNameTextField;
    private javax.swing.JTextField idTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel scanningLabel;
    // End of variables declaration//GEN-END:variables

}
