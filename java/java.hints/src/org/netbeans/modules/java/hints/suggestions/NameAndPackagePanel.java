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
package org.netbeans.modules.java.hints.suggestions;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

import org.openide.util.Utilities;

/**
 *
 * @author balek
 */
class NameAndPackagePanel extends javax.swing.JPanel {

    private static RequestProcessor WORKER = new RequestProcessor(NameAndPackagePanel.class.getName(), 1, false, false);
    
    static final String IS_VALID = "NameAndPackagePanel.isValidData"; //NOI18N

    private volatile ErrorListener errorListener;
    private final JavaSource testingJavaSource;
    private final ElementHandle<TypeElement> baseclass;
    
    public NameAndPackagePanel(FileObject targetSourceRoot, ElementHandle<TypeElement> baseclass, String className, String packageName) {
        initComponents();
        classNameTextField.setText(className);
        packageNameTextField.setText(packageName);
        this.testingJavaSource = JavaSource.create(ClasspathInfo.create(targetSourceRoot));
        this.baseclass = baseclass;
        DocumentListener l = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                checkValid();
            }
            @Override public void removeUpdate(DocumentEvent e) {
                checkValid();
            }
            @Override public void changedUpdate(DocumentEvent e) {}
        };
        this.classNameTextField.getDocument().addDocumentListener(l);
        this.packageNameTextField.getDocument().addDocumentListener(l);
    }
    
    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public String getClassName() {
        return classNameTextField.getText();
    }

    public String getPackageName() {
        return packageNameTextField.getText();
    }

    private static final AtomicLong documentVersion = new AtomicLong();
    
    /**
     * Should be called after errorListener is set up.
     */
    @Messages({
        "ERR_TypeNameNotValid=Type name is not valid",
        "ERR_PackageNameNotValid=Package name is not valid",
        "ERR_SameClass=The proposed type is the same as the type to be subtyped",
        "ERR_AlreadyExtends=The proposed type already subclasses the original class",
        "ERR_ExtendsOther=The proposed type already subclasses a different class",
        "ERR_AlreadyImplements=The proposed type already implements the original interface",
    })
    void checkValid() {
        assert errorListener != null;

        final long currentVersion = documentVersion.incrementAndGet();
        final String className = getClassName();
        final String packageName = getPackageName();
        
        if (!isValidTypeIdentifier(className)) {
            errorListener.setErrorMessage(Bundle.ERR_TypeNameNotValid());
        } else if (!isValidPackageName(packageName)) {
            errorListener.setErrorMessage(Bundle.ERR_PackageNameNotValid());
        } else {
            WORKER.post(new Runnable() {
                @Override public void run() {
                    try {
                        testingJavaSource.runUserActionTask(new Task<CompilationController>() {
                            @Override public void run(CompilationController parameter) throws Exception {
                                if (documentVersion.get() > currentVersion) return ;
                                
                                TypeElement element = parameter.getElements().getTypeElement(packageName + "." + className);
                                final String[] error = new String[] {null};
                                
                                if (element != null) {
                                    TypeElement baseclass = NameAndPackagePanel.this.baseclass.resolve(parameter);
                                    Types types = parameter.getTypes();
                                    TypeMirror baseclassType = types.erasure(baseclass.asType());
                                    
                                    if (element.equals(baseclass)) {
                                        error[0] = Bundle.ERR_SameClass();
                                    } else if (baseclass.getKind() == ElementKind.CLASS) {
                                        TypeElement jlObject = parameter.getElements().getTypeElement("java.lang.Object"); //NOI18N
                                        TypeMirror superClass = types.erasure(element.getSuperclass());
                                        if (types.isSameType(superClass, baseclassType)) {
                                            error[0] = Bundle.ERR_AlreadyExtends();
                                        } else if (jlObject != null && !types.isSameType(superClass, jlObject.asType())) {
                                            error[0] = Bundle.ERR_ExtendsOther();
                                        }
                                    } else {
                                        for (TypeMirror superInterface : element.getInterfaces()) {
                                            superInterface = types.erasure(superInterface);
                                            
                                            if (types.isSameType(superInterface, baseclassType)) {
                                                error[0] = Bundle.ERR_AlreadyImplements();
                                            }
                                        }
                                    }
                                }
                                
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override public void run() {
                                        if (documentVersion.get() > currentVersion) return ;
                                        errorListener.setErrorMessage(error[0]);
                                    }
                                });
                                
                            }
                        }, true);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }
    
    private boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(str, ".");
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token)) {
                return false;
            }
            if (!Utilities.isJavaIdentifier(token)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidTypeIdentifier(String ident) {
        if (ident == null || "".equals(ident) || !Utilities.isJavaIdentifier(ident)) {
            return false;
        } else {
            return true;
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

        classNameLabel = new javax.swing.JLabel();
        classNameTextField = new javax.swing.JTextField();
        packageNameLabel = new javax.swing.JLabel();
        packageNameTextField = new javax.swing.JTextField();

        classNameLabel.setLabelFor(classNameTextField);
        classNameLabel.setText(org.openide.util.NbBundle.getMessage(NameAndPackagePanel.class, "LBL_ClassName")); // NOI18N

        packageNameLabel.setLabelFor(packageNameTextField);
        packageNameLabel.setText(org.openide.util.NbBundle.getMessage(NameAndPackagePanel.class, "LBL_PackageName")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(packageNameLabel)
                    .addComponent(classNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(classNameTextField)
                    .addComponent(packageNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(classNameLabel)
                    .addComponent(classNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(packageNameLabel)
                    .addComponent(packageNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel classNameLabel;
    private javax.swing.JTextField classNameTextField;
    private javax.swing.JLabel packageNameLabel;
    private javax.swing.JTextField packageNameTextField;
    // End of variables declaration//GEN-END:variables

    public interface ErrorListener {
        public void setErrorMessage(String errorMessage);
    }
}
