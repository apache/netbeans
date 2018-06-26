/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.TypeElement;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Chris Webster
 * @author  Martin Fousek
 */
public class ServiceLocatorStrategyPanel extends javax.swing.JPanel implements ChangeListener {

    public static final String IS_VALID = "ServiceLocatorStrategyPanel_isValid"; //NOI18N
    private static final RequestProcessor RP = new RequestProcessor();
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final ClasspathInfo cpInfo;
    private final String serviceLocatorName;
    private String errorMsg = null;

    /** Creates new form ServiceLocatorStrategyPanel */
    public ServiceLocatorStrategyPanel(String serviceLocatorName, ClasspathInfo cpInfo) {
        initComponents();
        this.cpInfo = cpInfo;
        this.serviceLocatorName = serviceLocatorName;

        changeSupport.addChangeListener(this);
        
        className.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                fireChange();
            }
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                fireChange();
            }
            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                fireChange();
            }
            private void fireChange() {
                changeSupport.fireChange();
            }
        });

        if (serviceLocatorName != null) {
            existingServiceLocator.setSelected(true);
            className.setText(serviceLocatorName);
        } else {
            noServiceLocator.setSelected(true);
            setEnabledClassNameTextField(false);
        }
    }
    
    public String classSelected() {
        return existingServiceLocator.isSelected() ? className.getText() : null;
    }

    public JRadioButton getUnreferencedServiceLocator() {
        return existingServiceLocator;
    }
    
    public JTextField getClassName() {
        return className;
    }
    
    private boolean validClass() {
        if (existingServiceLocator.isSelected()) {
            final AtomicBoolean classExists = new AtomicBoolean(false);
            final JavaSource javaSource = JavaSource.create(cpInfo);
            if (javaSource != null) {
                try {
                    ValidClassChecker classChecker = new ValidClassChecker(classExists);
                    Future<Void> future = javaSource.runWhenScanFinished(classChecker, true);
                    if (future.isDone()) {
                        return classExists.get();
                    } else {
                        javaSource.runUserActionTask(classChecker, true);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return classExists.get();
        }
        return true;
    }

    private class ValidClassChecker implements Task<CompilationController> {

        private final AtomicBoolean wasScanning = new AtomicBoolean(false);
        private final AtomicBoolean classExists;

        public ValidClassChecker(AtomicBoolean classExists) {
            this.classExists = classExists;
        }

        @Override
        public void run(CompilationController parameter) throws Exception {
            parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            classExists.set(validClassExists(parameter, className.getText()));
            if (wasScanning.get()) {
                changeSupport.fireChange();
            }
            // after first call is set to true - when the scan runs, it comes here once
            // more and the fireChange() is called then to update validation
            wasScanning.set(true);
        }
        
    }

    private boolean validClassExists(CompilationController controller, String className) {
        TypeElement typeElement = controller.getElements().getTypeElement(className);
        if (typeElement != null) {
            return typeElement.getKind().isClass();
        }
        return false;
    }

    @NbBundle.Messages("serviceLocatorStrategyPanel.error.class.not.valid=Service Locator Class is not valid")
    public boolean verifyComponents() {
        if (validClass()) {
            errorMsg = null;
        } else {
            errorMsg = Bundle.serviceLocatorStrategyPanel_error_class_not_valid();
        }
        return errorMsg == null;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (verifyComponents()) {
            firePropertyChange(IS_VALID, false, true);
        } else {
            firePropertyChange(IS_VALID, true, false);
        }
    }

    public String getErrorMessage() {
        return errorMsg;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        noServiceLocator = new javax.swing.JRadioButton();
        existingServiceLocator = new javax.swing.JRadioButton();
        className = new javax.swing.JTextField();
        showFindTypeButton = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entries/Bundle"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("LBL_ServiceLocatorStrategy"))); // NOI18N

        buttonGroup1.add(noServiceLocator);
        noServiceLocator.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(noServiceLocator, bundle.getString("LBL_NoServiceLocator")); // NOI18N
        noServiceLocator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noLocatorActionPerformed(evt);
            }
        });

        buttonGroup1.add(existingServiceLocator);
        org.openide.awt.Mnemonics.setLocalizedText(existingServiceLocator, bundle.getString("LBL_UseServiceLocatorClass")); // NOI18N
        existingServiceLocator.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                existingServiceLocatorItemStateChanged(evt);
            }
        });
        existingServiceLocator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existingClassActionPerformed(evt);
            }
        });

        showFindTypeButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entries/Bundle").getString("MN_Browse").charAt(0));
        showFindTypeButton.setText("...");
        showFindTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showFindTypeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(noServiceLocator)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(existingServiceLocator)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(className, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(showFindTypeButton)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(noServiceLocator)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(existingServiceLocator)
                    .addComponent(className, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showFindTypeButton)))
        );

        noServiceLocator.getAccessibleContext().setAccessibleName("&Generator inline lookup code");
        noServiceLocator.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceLocatorStrategyPanel.class, "ACSD_NoServiceLocator")); // NOI18N
        existingServiceLocator.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceLocatorStrategyPanel.class, "ACSD_UseServiceLocatorClass")); // NOI18N
        className.getAccessibleContext().setAccessibleName(bundle.getString("ACS_ExistingClassName")); // NOI18N
        className.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_ExistingClassName")); // NOI18N
        showFindTypeButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_Browse")); // NOI18N
        showFindTypeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_Browse")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void showFindTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showFindTypeButtonActionPerformed
        existingServiceLocator.setSelected(true);
        setEnabledClassNameTextField(true);
        String simpleName = "";
        if (serviceLocatorName != null) {
            int dotPosition = serviceLocatorName.lastIndexOf(".");
            simpleName = serviceLocatorName.substring(dotPosition + 1);
        }
        final ElementHandle<TypeElement> handle = TypeElementFinder.find(cpInfo, simpleName, new TypeElementFinder.Customizer() {
            
            @Override
            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery,
                    NameKind nameKind, Set<SearchScope> searchScopes) {
                return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
            }

            @Override
            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                return true;
            }
        });
        if (handle != null) {
            className.setText(handle.getQualifiedName());
        }
    }//GEN-LAST:event_showFindTypeButtonActionPerformed

    private void existingServiceLocatorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_existingServiceLocatorItemStateChanged
        changeSupport.fireChange();
    }//GEN-LAST:event_existingServiceLocatorItemStateChanged

    private void noLocatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noLocatorActionPerformed
        setEnabledClassNameTextField(false);
        changeSupport.fireChange();
    }//GEN-LAST:event_noLocatorActionPerformed

    private void existingClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingClassActionPerformed
        setEnabledClassNameTextField(true);
        changeSupport.fireChange();
    }//GEN-LAST:event_existingClassActionPerformed
    
    private void setEnabledClassNameTextField(boolean enabled) {
        className.setEnabled(enabled);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField className;
    private javax.swing.JRadioButton existingServiceLocator;
    private javax.swing.JRadioButton noServiceLocator;
    private javax.swing.JButton showFindTypeButton;
    // End of variables declaration//GEN-END:variables

}
