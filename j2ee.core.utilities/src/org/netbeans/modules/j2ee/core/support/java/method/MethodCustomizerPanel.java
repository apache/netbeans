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

package org.netbeans.modules.j2ee.core.support.java.method;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;

/**
 *
 * @author Martin Adamek
 * @author Petr Slechta
 */
public final class MethodCustomizerPanel extends javax.swing.JPanel {

    public static final String NAME = "name";  // NOI18N
    public static final String RETURN_TYPE = "returnType";  // NOI18N
    public static final String INTERFACES = "interfaces";  // NOI18N
    public static final String PARAMETERS = "parameters";  // NOI18N
    // immutable method prototype
    private final MethodModel methodModel;
    private final ParametersPanel parametersPanel;
    private final ExceptionsPanel exceptionsPanel;
    private final boolean hasInterfaces;
    private final ClasspathInfo cpInfo;
    private final boolean allowsNoInterface;

    private MethodCustomizerPanel(MethodModel methodModel, ClasspathInfo cpInfo, boolean hasLocal, boolean hasRemote, 
            boolean selectLocal, boolean selectRemote, boolean hasReturnType, String ejbql, 
            boolean hasFinderCardinality, boolean hasExceptions, boolean hasInterfaces, boolean allowsNoInterface) {
        initComponents();

        this.methodModel = methodModel;
        this.hasInterfaces = hasInterfaces;
        this.allowsNoInterface = allowsNoInterface;
        this.cpInfo = cpInfo;

        nameTextField.setText(methodModel.getName());
        returnTypeTextField.setText(methodModel.getReturnType());

        localRadio.setEnabled(hasLocal);
        remoteRadio.setEnabled(hasRemote);
        bothRadio.setEnabled(hasLocal && hasRemote);
        localRadio.setSelected(selectLocal);
        remoteRadio.setSelected(selectRemote && !selectLocal);

        if (!hasReturnType) {
            disableReturnType();
        }
        if (ejbql == null) {
            ejbqlPanel.setVisible(false);
        } else {
            ejbqlTextArea.setText(ejbql);
        }
        cardinalityPanel.setVisible(hasFinderCardinality);
        interfacesPanel.setVisible(hasInterfaces);

        parametersPanel = new ParametersPanel(cpInfo, methodModel.getParameters());
        parametersContainerPanel.add(parametersPanel);

        if (hasExceptions) {
            exceptionsPanel = new ExceptionsPanel(methodModel.getExceptions(), cpInfo);
            exceptionsContainerPanel.add(exceptionsPanel);
        }
        else {
            exceptionsPanel = null;
            exceptionsContainerPanel.setVisible(false);
        }

        // listeners
        nameTextField.getDocument().addDocumentListener(new SimpleListener(NAME));
        returnTypeTextField.getDocument().addDocumentListener(new SimpleListener(RETURN_TYPE));
        parametersPanel.addPropertyChangeListener(new SimpleListener(PARAMETERS));
        SimpleListener interfacesListener = new SimpleListener(INTERFACES);
        localRadio.addActionListener(interfacesListener);
        remoteRadio.addActionListener(interfacesListener);
        bothRadio.addActionListener(interfacesListener);
    }

    public static MethodCustomizerPanel create(MethodModel methodModel, ClasspathInfo cpInfo, boolean hasLocal, boolean hasRemote,
            boolean selectLocal, boolean selectRemote, boolean hasReturnType, String  ejbql, 
            boolean hasFinderCardinality, boolean hasExceptions, boolean hasInterfaces, boolean allowsNoInterface) {
        return new MethodCustomizerPanel(methodModel, cpInfo, hasLocal, hasRemote, selectLocal, selectRemote,
                hasReturnType, ejbql, hasFinderCardinality, hasExceptions, hasInterfaces, allowsNoInterface);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        // first validation before any real event is send
        firePropertyChange(NAME, null, null);
        firePropertyChange(RETURN_TYPE, null, null);
        firePropertyChange(INTERFACES, null, null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        finderCardinalityButtonGroup = new javax.swing.ButtonGroup();
        interfaceButtonGroup = new javax.swing.ButtonGroup();
        exceptionAndParameterPane = new javax.swing.JTabbedPane();
        parametersContainerPanel = new javax.swing.JPanel();
        exceptionsContainerPanel = new javax.swing.JPanel();
        returnTypeLabel = new javax.swing.JLabel();
        returnTypeTextField = new javax.swing.JTextField();
        nameTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        interfacesPanel = new javax.swing.JPanel();
        interfaceLabel = new javax.swing.JLabel();
        localRadio = new javax.swing.JRadioButton();
        remoteRadio = new javax.swing.JRadioButton();
        bothRadio = new javax.swing.JRadioButton();
        cardinalityPanel = new javax.swing.JPanel();
        cardinalityLabel = new javax.swing.JLabel();
        oneRadioButton = new javax.swing.JRadioButton();
        manyRadioButton = new javax.swing.JRadioButton();
        ejbqlPanel = new javax.swing.JPanel();
        ejbqlLabel = new javax.swing.JLabel();
        ejbqlScrollPane = new javax.swing.JScrollPane();
        ejbqlTextArea = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();

        parametersContainerPanel.setLayout(new java.awt.BorderLayout());
        exceptionAndParameterPane.addTab(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.parametersContainerPanel.TabConstraints.tabTitle"), parametersContainerPanel); // NOI18N
        parametersContainerPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_ParametersTab")); // NOI18N

        exceptionsContainerPanel.setLayout(new java.awt.BorderLayout());
        exceptionAndParameterPane.addTab(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.exceptionsPanel.TabConstraints.tabTitle"), exceptionsContainerPanel); // NOI18N
        exceptionsContainerPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_ExceptionsTab")); // NOI18N

        returnTypeLabel.setLabelFor(returnTypeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(returnTypeLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.returnTypeLabel.text")); // NOI18N

        returnTypeTextField.setText(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.returnTypeTextField.text")); // NOI18N
        returnTypeTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                returnTypeTextFieldFocusGained(evt);
            }
        });

        nameTextField.setText(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.nameTextField.text")); // NOI18N
        nameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameTextFieldFocusGained(evt);
            }
        });

        jLabel1.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(interfaceLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.interfaceLabel.text")); // NOI18N

        interfaceButtonGroup.add(localRadio);
        localRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(localRadio, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "Iface_type_Local")); // NOI18N
        localRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        interfaceButtonGroup.add(remoteRadio);
        org.openide.awt.Mnemonics.setLocalizedText(remoteRadio, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "Iface_type_Remote")); // NOI18N
        remoteRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        interfaceButtonGroup.add(bothRadio);
        org.openide.awt.Mnemonics.setLocalizedText(bothRadio, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "Iface_type_Both")); // NOI18N
        bothRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout interfacesPanelLayout = new javax.swing.GroupLayout(interfacesPanel);
        interfacesPanel.setLayout(interfacesPanelLayout);
        interfacesPanelLayout.setHorizontalGroup(
            interfacesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(interfacesPanelLayout.createSequentialGroup()
                .addComponent(interfaceLabel)
                .addGap(18, 18, 18)
                .addComponent(localRadio)
                .addGap(18, 18, 18)
                .addComponent(remoteRadio)
                .addGap(18, 18, 18)
                .addComponent(bothRadio)
                .addContainerGap(222, Short.MAX_VALUE))
        );
        interfacesPanelLayout.setVerticalGroup(
            interfacesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(interfacesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(interfaceLabel)
                .addComponent(localRadio)
                .addComponent(remoteRadio)
                .addComponent(bothRadio))
        );

        localRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_LocalRadioButton")); // NOI18N
        remoteRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_RemoteRadioButton")); // NOI18N
        bothRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_BothRadioButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cardinalityLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.cardinalityLabel.text")); // NOI18N

        finderCardinalityButtonGroup.add(oneRadioButton);
        oneRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(oneRadioButton, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.oneRadioButton.text")); // NOI18N
        oneRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        finderCardinalityButtonGroup.add(manyRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(manyRadioButton, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.manyRadioButton.text")); // NOI18N
        manyRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout cardinalityPanelLayout = new javax.swing.GroupLayout(cardinalityPanel);
        cardinalityPanel.setLayout(cardinalityPanelLayout);
        cardinalityPanelLayout.setHorizontalGroup(
            cardinalityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardinalityPanelLayout.createSequentialGroup()
                .addComponent(cardinalityLabel)
                .addGap(18, 18, 18)
                .addComponent(oneRadioButton)
                .addGap(18, 18, 18)
                .addComponent(manyRadioButton)
                .addContainerGap(305, Short.MAX_VALUE))
        );
        cardinalityPanelLayout.setVerticalGroup(
            cardinalityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardinalityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(cardinalityLabel)
                .addComponent(oneRadioButton)
                .addComponent(manyRadioButton))
        );

        oneRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_OneRadioButton")); // NOI18N
        manyRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_ManyRadioButton")); // NOI18N

        ejbqlLabel.setLabelFor(ejbqlTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(ejbqlLabel, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.ejbqlLabel.text")); // NOI18N

        ejbqlScrollPane.setBorder(null);

        ejbqlTextArea.setColumns(20);
        ejbqlTextArea.setRows(5);
        ejbqlTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ejbqlTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                ejbqlTextAreaFocusGained(evt);
            }
        });
        ejbqlScrollPane.setViewportView(ejbqlTextArea);
        ejbqlTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_EJB_QL")); // NOI18N

        javax.swing.GroupLayout ejbqlPanelLayout = new javax.swing.GroupLayout(ejbqlPanel);
        ejbqlPanel.setLayout(ejbqlPanelLayout);
        ejbqlPanelLayout.setHorizontalGroup(
            ejbqlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ejbqlPanelLayout.createSequentialGroup()
                .addComponent(ejbqlLabel)
                .addContainerGap(538, Short.MAX_VALUE))
            .addComponent(ejbqlScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
        );
        ejbqlPanelLayout.setVerticalGroup(
            ejbqlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ejbqlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ejbqlLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ejbqlScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "MethodCustomizerPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(exceptionAndParameterPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 586, Short.MAX_VALUE)
                    .addComponent(ejbqlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cardinalityPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(interfacesPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(returnTypeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(returnTypeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1))
                            .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(returnTypeLabel)
                    .addComponent(jButton1)
                    .addComponent(returnTypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(exceptionAndParameterPane, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(interfacesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cardinalityPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ejbqlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        exceptionAndParameterPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_TabPane")); // NOI18N
        returnTypeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_ReturnType")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_Name")); // NOI18N
        jButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_MethodCustomizerPanel_Browse")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void nameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameTextFieldFocusGained
        nameTextField.selectAll();
    }//GEN-LAST:event_nameTextFieldFocusGained

    private void returnTypeTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_returnTypeTextFieldFocusGained
        returnTypeTextField.selectAll();
    }//GEN-LAST:event_returnTypeTextFieldFocusGained

    private void ejbqlTextAreaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ejbqlTextAreaFocusGained
        ejbqlTextArea.selectAll();
    }//GEN-LAST:event_ejbqlTextAreaFocusGained

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        final ElementHandle<TypeElement> handle = TypeElementFinder.find(cpInfo, new TypeElementFinder.Customizer() {
            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {                                            
                return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
            }

            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                return true;
            }
        });
        if (handle != null) {
            returnTypeTextField.setText(handle.getQualifiedName());
        }
    }                                        

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton bothRadio;
    private javax.swing.JLabel cardinalityLabel;
    private javax.swing.JPanel cardinalityPanel;
    private javax.swing.JLabel ejbqlLabel;
    private javax.swing.JPanel ejbqlPanel;
    private javax.swing.JScrollPane ejbqlScrollPane;
    private javax.swing.JTextArea ejbqlTextArea;
    private javax.swing.JTabbedPane exceptionAndParameterPane;
    private javax.swing.JPanel exceptionsContainerPanel;
    private javax.swing.ButtonGroup finderCardinalityButtonGroup;
    private javax.swing.ButtonGroup interfaceButtonGroup;
    private javax.swing.JLabel interfaceLabel;
    private javax.swing.JPanel interfacesPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton localRadio;
    private javax.swing.JRadioButton manyRadioButton;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JRadioButton oneRadioButton;
    private javax.swing.JPanel parametersContainerPanel;
    private javax.swing.JRadioButton remoteRadio;
    private javax.swing.JLabel returnTypeLabel;
    private javax.swing.JTextField returnTypeTextField;
    // End of variables declaration//GEN-END:variables

    public String getMethodName() {
        return nameTextField.getText().trim();
    }

    public String getReturnType() {
        return returnTypeTextField.getText().trim();
    }

    public List<MethodModel.Variable> getParameters() {
        return parametersPanel.getParameters();
    }

    public List<String> getExceptions() {
        List<String> result = new ArrayList<String>();
        if (exceptionsPanel != null) {
            for (String exception : exceptionsPanel.getExceptions()) {
                if (!"".equals(exception.trim())) {  // NOI18N
                    result.add(exception);
                }
            }
        }
        return result;
    }

    public Set<Modifier> getModifiers() {
        // not changing?
        return methodModel.getModifiers();
    }

    public String getMethodBody() {
        // not changing?
        return methodModel.getBody();
    }

    public boolean supportsInterfacesChecking() {
        return hasInterfaces;
    }

    public boolean hasLocal() {
        return (localRadio.isEnabled() && localRadio.isSelected()) || hasBothInterfaces();
    }

    public boolean hasRemote() {
        return (remoteRadio.isEnabled() && remoteRadio.isSelected()) || hasBothInterfaces();
    }

    public boolean allowsNoInterface(){
        return allowsNoInterface;
    }

    public String getEjbql() {
        if (ejbqlTextArea != null) {
            return ejbqlTextArea.getText().trim();
        }
        return null;
    }

    public boolean finderReturnIsSingle() {
        return oneRadioButton != null ? oneRadioButton.isSelected() : false;
    }

    private boolean hasBothInterfaces() {
        return localRadio.isEnabled() && remoteRadio.isEnabled() && bothRadio.isSelected();
    }

    private void disableReturnType() {
        returnTypeLabel.setVisible(false);
        returnTypeTextField.setVisible(false);
    }

    /**
     * Listener on text fields.
     * Fires change event for specified property of this JPanel,
     * old and new value of event is null.
     * After receiving event, client can get property value by
     * calling {@link #getProperty(String)}
     */
    private class SimpleListener implements DocumentListener, ActionListener, PropertyChangeListener {

        private final String propertyName;

        public SimpleListener(String propertyName) {
            this.propertyName = propertyName;
        }

        public void insertUpdate(DocumentEvent documentEvent) {
            fire();
        }

        public void removeUpdate(DocumentEvent documentEvent) {
            fire();
        }

        public void changedUpdate(DocumentEvent documentEvent) {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            fire();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            fire();
        }
        
        private void fire() {
            firePropertyChange(propertyName, null, null);
        }

    }
    
}
