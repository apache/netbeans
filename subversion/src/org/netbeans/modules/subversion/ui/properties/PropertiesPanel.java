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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.subversion.ui.properties;

import javax.swing.GroupLayout;
import java.util.Set;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;

/**
 *
 * @author  Peter Pis
 * @author  Marian Petras
 */
public class PropertiesPanel extends JPanel implements DocumentListener,
                                                       ItemListener,
                                                       PreferenceChangeListener,
                                                       TableModelListener {
    private enum AddButtonState {
        ADD       ("PropertiesPanel.btnAdd.text"),                      //NOI18N
        ADD_UPDATE("PropertiesPanel.btnAddUpdate.text"),                //NOI18N
        UPDATE    ("PropertiesPanel.btnUpdate.text");                   //NOI18N

        private final String btnLabel;
        AddButtonState(String msgKey) {
            btnLabel = NbBundle.getMessage(PropertiesPanel.class, msgKey);
        }
        String getLabel() {
            return btnLabel;
        }
    }

    final JButton btnAdd = new JButton() {
        private Dimension prefSize;
        @Override
        public Dimension getPreferredSize() {
            if (prefSize == null) {
                int maxWidth;
                int maxHeight;
                Dimension d;
                Mnemonics.setLocalizedText(this, AddButtonState.ADD.getLabel());
                d = super.getPreferredSize();
                maxWidth = d.width;
                maxHeight = d.height;
                Mnemonics.setLocalizedText(this, AddButtonState.ADD_UPDATE.getLabel());
                d = super.getPreferredSize();
                maxWidth = Math.max(maxWidth, d.width);
                maxHeight = Math.max(maxHeight, d.height);
                Mnemonics.setLocalizedText(this, AddButtonState.UPDATE.getLabel());
                d = super.getPreferredSize();
                maxWidth = Math.max(maxWidth, d.width);
                maxHeight = Math.max(maxHeight, d.height);
                prefSize = new Dimension(maxWidth, maxHeight);
            }
            return prefSize;
        }
    };
    final JButton btnBrowse = new JButton();
    final JButton btnRefresh = new JButton();
    final JButton btnRemove = new JButton();
    final JCheckBox cbxRecursively = new JCheckBox();
    final JComboBox comboName = new JComboBox();
    final JTextArea txtAreaValue = new JTextArea();
    final JPanel propsPanel = new DerivedHeightPanel(txtAreaValue, 1.0f);
    final JLabel labelForTable = new JLabel();

    private static final Object EVENT_SETTINGS_CHANGED = new Object();
    private PropertiesTable propertiesTable;
    private ListenersSupport listenerSupport = new ListenersSupport(this);
    private final javax.swing.ImageIcon warningIcon = new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/warning.gif")); //NOI18N
    private final javax.swing.ImageIcon errorIcon = new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/error.gif")); //NOI18N
    private final JLabel lblErrMessage = new JLabel(errorIcon, SwingConstants.LEADING);
    private final Document propNameDocument;
    private final Document propValueDocument;
    private String propertyName;
    private String propertyValue;
    private String illegalPropErrMsgKey;
    private boolean recursive;
    private String[] existingProperties;
    private String[] illegalProperties = new String[0];
    private Set<String> recursiveProperties = Collections.emptySet();
    private SvnProperties propValueChangeListener;
    private boolean interactionInitialized;

    /** Creates new form PropertiesPanel */
    public PropertiesPanel() {
        propNameDocument = ((JTextField) comboName.getEditor().getEditorComponent())
                           .getDocument();
        propValueDocument = txtAreaValue.getDocument();
        initComponents();
    }

    String getPropertyName() {
        return propertyName;
    }

    String getPropertyValue() {
        return propertyValue;
    }
    
    void setPropertiesTable(PropertiesTable propertiesTable){
        this.propertiesTable = propertiesTable;
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        SvnModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);        
        propertiesTable.getTableModel().addTableModelListener(this);
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
        txtAreaValue.selectAll();
    }

    @Override
    public void removeNotify() {
        propertiesTable.getTableModel().removeTableModelListener(this);
        SvnModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
        super.removeNotify();
    }
    
    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(SvnModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            propertiesTable.dataChanged();
            listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
        }
    }

    /**
     * Adds the given list of property names to the combo-box's pop-up
     * and clears the current combo-box value.
     * If there were other property names present in the pop-up, they are
     * removed first.
     */
    void setPredefinedPropertyNames(String[] propertyNames) {
        comboName.setModel(new DefaultComboBoxModel(propertyNames));
        comboName.getEditor().setItem("");                              //NOI18N
    }

    void setExistingPropertyNames(String[] propNames) {
        existingProperties = propNames;
        if (interactionInitialized) {
            existingPropertiesChanged();
        }
    }

    void setIllegalPropertyNames(String[] propNames, String errMsgKey) {
        illegalProperties = propNames;
        illegalPropErrMsgKey = errMsgKey;
        if (interactionInitialized) {
            illegalPropertiesChanged();
        }
    }

    void setRecursiveProperties (Collection<String> propNames) {
        recursiveProperties = new HashSet<String>(propNames);
    }

    void setForDirectory(boolean forDirectory) {
        if (forDirectory) {
            cbxRecursively.setEnabled(true);
        } else {
            cbxRecursively.setEnabled(false);
            cbxRecursively.setSelected(false);
        }
    }

    void initInteraction() {
        updatePropertyName();
        updatePropertyValue();
        updateIsRecursive();
        propNameDocument.addDocumentListener(this);
        propValueDocument.addDocumentListener(this);
        cbxRecursively.addItemListener(this);

        refreshAddButtonText();
        refreshAddButtonState();

        interactionInitialized = true;
    }

    void setPropertyValueChangeListener(SvnProperties l) {
        propValueChangeListener = l;
    }

    void removePropertyValueChangeListener() {
        propValueChangeListener = null;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
    }

    // <editor-fold defaultstate="collapsed" desc="UI Definition Code">
    private void initComponents() {
        JLabel lblPropertyName = new JLabel();
        lblPropertyName.setLabelFor(comboName);
        Mnemonics.setLocalizedText(lblPropertyName, getString("PropertiesPanel.jLabel2.text")); // NOI18N

        JLabel lblPropertyValue = new JLabel();
        lblPropertyValue.setLabelFor(txtAreaValue);
        Mnemonics.setLocalizedText(lblPropertyValue, getString("PropertiesPanel.jLabel1.text")); // NOI18N

        txtAreaValue.setColumns(20);
        txtAreaValue.setRows(5);
        JScrollPane jScrollPane1 = new JScrollPane();
        jScrollPane1.setViewportView(txtAreaValue);

        lblErrMessage.setForeground(Color.RED);
        lblErrMessage.setVisible(false);
        lblErrMessage.setText(" ");  //to get non-zero preferred height //NOI18N

        Mnemonics.setLocalizedText(btnBrowse, getString("PropertiesPanel.btnBrowse.text")); // NOI18N
        btnBrowse.setActionCommand(getString("btnBrowse.actionCommand")); // NOI18N

        JSeparator jSeparator1 = new JSeparator();

        Mnemonics.setLocalizedText(btnAdd, getString("PropertiesPanel.btnAdd.text")); // NOI18N

        Mnemonics.setLocalizedText(cbxRecursively, getString("PropertiesPanel.cbxRecursively.text")); // NOI18N
        cbxRecursively.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        Mnemonics.setLocalizedText(btnRemove, getString("PropertiesPanel.btnRemove.text")); // NOI18N
        btnRemove.setActionCommand(getString("btnRemove.actionCommand")); // NOI18N

        Mnemonics.setLocalizedText(btnRefresh, getString("PropertiesPanel.btnRefresh.text")); // NOI18N
        btnRefresh.setActionCommand(getString("btnRefresh.actionCommand")); // NOI18N

        Mnemonics.setLocalizedText(labelForTable, getString("jLabel3.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup()
                                                .addComponent(lblPropertyName)
                                                .addComponent(lblPropertyValue))
                                        .addPreferredGap(RELATED)
                                        .addGroup(layout.createParallelGroup()
                                                .addComponent(comboName, 0, DEFAULT_SIZE, DEFAULT_SIZE)
                                                .addComponent(jScrollPane1)))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblErrMessage, 0, DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(RELATED)
                                        .addComponent(btnBrowse))
                                .addComponent(jSeparator1)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(btnAdd, PREFERRED_SIZE, DEFAULT_SIZE, DEFAULT_SIZE)
                                        .addPreferredGap(RELATED)
                                        .addComponent(cbxRecursively)
                                        .addPreferredGap(cbxRecursively, btnRemove, RELATED)
                                        .addComponent(btnRemove)
                                        .addPreferredGap(RELATED)
                                        .addComponent(btnRefresh))
                                .addComponent(labelForTable)
                                .addComponent(propsPanel))
                        .addContainerGap()
        );
        layout.linkSize(SwingConstants.HORIZONTAL, btnBrowse, btnRefresh, btnRemove);
        layout.setHonorsVisibility(false);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(lblPropertyName)
                                .addComponent(comboName))
                        .addPreferredGap(RELATED)
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(lblPropertyValue)
                                .addComponent(jScrollPane1, PREFERRED_SIZE, DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(RELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(lblErrMessage)
                                .addComponent(btnBrowse))
                        .addPreferredGap(RELATED)
                        .addComponent(jSeparator1, PREFERRED_SIZE, 10, PREFERRED_SIZE)
                        .addPreferredGap(RELATED)
                        .addGroup(layout.createParallelGroup(BASELINE)
                                .addComponent(btnAdd)
                                .addComponent(cbxRecursively)
                                .addComponent(btnRemove)
                                .addComponent(btnRefresh))
                        .addGap(18)
                        .addComponent(labelForTable)
                        .addPreferredGap(RELATED)
                        .addComponent(propsPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap()
        );

        lblPropertyName.getAccessibleContext().setAccessibleDescription(getString("jLabel2.AccessibleContext.accessibleDescription")); // NOI18N
        lblPropertyValue.getAccessibleContext().setAccessibleDescription(getString("jLabel1.AccessibleContext.accessibleDescription")); // NOI18N

        comboName.getAccessibleContext().setAccessibleName(getString("comboName.AccessibleContext.accessibleName")); // NOI18N
        comboName.getAccessibleContext().setAccessibleDescription(getString("comboName.AccessibleContext.accessibleDescription")); // NOI18N

        txtAreaValue.getAccessibleContext().setAccessibleName(getString("txtAreaValue.AccessibleContext.accessibleName")); // NOI18N
        txtAreaValue.getAccessibleContext().setAccessibleDescription(getString("txtAreaValue.AccessibleContext.accessibleDescription")); // NOI18N

        btnBrowse.getAccessibleContext().setAccessibleDescription(getString("btnBrowse.AccessibleContext.accessibleDescription")); // NOI18N

        btnAdd.getAccessibleContext().setAccessibleDescription(getString("btnAdd.AccessibleContext.accessibleDescription")); // NOI18N
        cbxRecursively.getAccessibleContext().setAccessibleDescription(getString("cbxRecursively.AccessibleContext.accessibleDescription")); // NOI18N
        btnRemove.getAccessibleContext().setAccessibleDescription(getString("btnRemove.AccessibleContext.accessibleDescription")); // NOI18N
        btnRefresh.getAccessibleContext().setAccessibleDescription(getString("btnRefresh.AccessibleContext.accessibleDescription")); // NOI18N

        labelForTable.getAccessibleContext().setAccessibleDescription(getString("labelForTable.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>

    private void setErrMessage(String message) {
        setMessage(message, errorIcon, Color.RED);
    }

    private void setWarningMessage (String message) {
        setMessage(message, warningIcon, Color.BLACK);
    }

    private void setMessage (String message, javax.swing.ImageIcon icon, Color foregroundColor) {
        if (message == null) {
            lblErrMessage.setText(" ");                                 //NOI18N
            lblErrMessage.setVisible(false);
        } else {
            lblErrMessage.setText(message);
            lblErrMessage.setVisible(true);
            lblErrMessage.setIcon(icon);
            lblErrMessage.setForeground(foregroundColor);
            int widthReserve = lblErrMessage.getSize().width - lblErrMessage.getPreferredSize().width;
            if (widthReserve < 0) {
                makeDialogWider(-widthReserve);
            }
        }
    }

    private void makeDialogWider(int delta) {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) {
            Dimension size = w.getSize();
            size.width += delta;
            w.setSize(size);
        }
    }

    private static String getString(String msgKey) {
        return NbBundle.getMessage(PropertiesPanel.class, msgKey);
    }

    private void refreshAddButtonText() {
        AddButtonState buttonState;
        if (recursive) {
            buttonState = AddButtonState.ADD_UPDATE;
        } else {
            boolean isExistingProperty = false;
            if (existingProperties != null) {
                for (int i = 0; i < existingProperties.length; i++) {
                    if (existingProperties[i].equals(propertyName)) {
                        isExistingProperty = true;
                        break;
                    }
                }
            }
            buttonState = isExistingProperty ? AddButtonState.UPDATE
                                             : AddButtonState.ADD;
        }
        Mnemonics.setLocalizedText(btnAdd, buttonState.getLabel());
    }

    private void refreshAddButtonState() {
        boolean enabled;
        String errMsg, warningMsg = null;

        if (propertyName.length() == 0) {
            enabled = false;
            errMsg = null;
        } else if (!isPropertyNameLegal()) {
            enabled = false;
            errMsg = NbBundle.getMessage(PropertiesPanel.class,
                                         illegalPropErrMsgKey,
                                         propertyName);
        } else if (propertyName.indexOf(' ') != -1) {
            enabled = false;
            errMsg = NbBundle.getMessage(PropertiesPanel.class,
                                         "PropertiesPanel.errPropNameInvalid"); //NOI18N
        } else {
            enabled = propertyValue.length() != 0;
            errMsg = null;
        }

        if (errMsg == null && recursiveProperties.contains(propertyName)) {
            cbxRecursively.setSelected(true);
            cbxRecursively.setEnabled(false);
            warningMsg = NbBundle.getMessage(PropertiesPanel.class, "PropertiesPanel.needsRecursive", propertyName); //NOI18N
        } else {
            cbxRecursively.setEnabled(true);
        }

        btnAdd.setEnabled(enabled);
        if (errMsg != null) {
            setErrMessage(errMsg);
        } else {
            setWarningMessage(warningMsg);
        }
    }

    private boolean isPropertyNameLegal() {
        for (int i = 0; i < illegalProperties.length; i++) {
            if (propertyName.equals(illegalProperties[i])) {
                return false;
            }
        }
        return true;
    }

    private void propNameChanged() {
        /* update button's text: */
        if (!recursive) {              //if recursive, it is always "Add/Update"
            refreshAddButtonText();
        }

        /* update button's state (enabled/disabled): */
        refreshAddButtonState();
    }

    private void propValueChanged() {
        refreshAddButtonState();

        if (propValueChangeListener != null) {
            propValueChangeListener.propertyValueChanged();
        }
    }

    private void recursiveToggled() {
        refreshAddButtonText();
    }

    private void existingPropertiesChanged() {
        refreshAddButtonText();
    }

    private void illegalPropertiesChanged() {
        refreshAddButtonState();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        textChanged(e.getDocument());
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        textChanged(e.getDocument());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        //document attribute changes - not interesting
    }

    /** Called when the Recursive check-box is toggled. */
    @Override
    public void itemStateChanged(ItemEvent e) {
        assert e.getSource() == cbxRecursively;
        updateIsRecursive();
        recursiveToggled();
    }

    private void textChanged(Document document) {
        if (document == propNameDocument) {
            updatePropertyName();
            propNameChanged();
        } else {
            assert document == propValueDocument;
            updatePropertyValue();
            propValueChanged();
        }
    }

    private void updatePropertyName() {
        try {
            propertyName = propNameDocument.getText(0, propNameDocument.getLength()).trim();
        } catch (BadLocationException ex) {
            assert false;
            propertyName = "";                                          //NOI18N
        }
    }

    private void updatePropertyValue() {
        propertyValue = txtAreaValue.getText().trim();
    }

    private void updateIsRecursive() {
        recursive = cbxRecursively.isSelected();
    }

    /**
     * Panel whose preferred height is derived from preferred height
     * of another component.
     */
    private final class DerivedHeightPanel extends JPanel {
        private final Component deriveFrom;
        private final float ratio;
        private Dimension prefSize = null;
        private DerivedHeightPanel(Component deriveFrom, float ratio) {
            this.deriveFrom = deriveFrom;
            this.ratio = ratio;
        }
        @Override
        public Dimension getPreferredSize() {
            if (prefSize == null) {
                prefSize = computePrefSize();
            }
            return prefSize;
        }
        private Dimension computePrefSize() {
            int prefHeight = Math.round(
                                 ratio * deriveFrom.getPreferredSize().height);
            return new Dimension(0, prefHeight);
        }
    }

}
