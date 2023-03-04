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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.node.TableNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


public class AddTableColumnDialog extends JPanel {
    static final Logger LOGGER = Logger.getLogger(AddTableColumnDialog.class.getName());
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport statusLine;
    Dialog dialog = null;
    JTextField colnamefield, colsizefield, colscalefield, defvalfield;
    JTextArea checkfield;
    JComboBox coltypecombo;
    JCheckBox pkcheckbox, ixcheckbox, checkcheckbox, nullcheckbox, uniquecheckbox;
    DataModel dmodel = new DataModel();
    private final Collection<String> sizelesstypes;
    private final Collection<String> charactertypes;

    @SuppressWarnings("unchecked")
    public AddTableColumnDialog(final Specification spe) {
        setBorder(new EmptyBorder(new Insets(12, 12, 5, 11)));
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints con;
        setLayout(layout);

        // Column name

        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumnName")); //NOI18N
        label.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnNameA11yDesc"));
        con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.anchor = GridBagConstraints.WEST;
        con.insets = new java.awt.Insets(0, 0, 0, 0);
        con.weightx = 0.0;
        con.weighty = 0.0;
        add(label, con);

        con = new GridBagConstraints();
        con.gridx = 1;
        con.gridy = 0;
        con.gridwidth = 3;
        con.gridheight = 1;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.insets = new java.awt.Insets(0, 12, 0, 0);
        con.weightx = 1.0;
        con.weighty = 0.0;
        colnamefield = new JTextField(35);
        colnamefield.setName(ColumnItem.NAME);
        colnamefield.setToolTipText(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnNameTextFieldA11yDesc"));
        colnamefield.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnNameTextFieldA11yName"));
        label.setLabelFor(colnamefield);
        add(colnamefield, con);
        DocumentListener docListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateState();
            }
        };
        colnamefield.getDocument().addDocumentListener(docListener);

        // Column type

        Collection<String> sizelesstypesSpec = (Collection<String>) spe.getProperties().get("SizelessTypes"); // NOI18N
        Collection<String> charactertypesSpec = (Collection<String>) spe.getProperties().get("CharacterTypes"); // NOI18N
        
        sizelesstypes = sizelesstypesSpec != null ? sizelesstypesSpec : Collections.<String>emptyList();
        charactertypes = charactertypesSpec != null ? charactertypesSpec : Collections.<String>emptyList();

        Map<String, String> tmap = spe.getTypeMap();
        List<TypeElement> ttab = new ArrayList<TypeElement>(tmap.size());
        for (String key : tmap.keySet()) {
            ttab.add(new TypeElement(key, tmap.get(key)));
        }

        ColumnItem item = new ColumnItem();
        item.setProperty(ColumnItem.TYPE, ttab.get(0));
        dmodel.addRow(item);

        label = new JLabel();
        Mnemonics.setLocalizedText(label, NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumnType")); //NOI18N
        label.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnTypeA11yDesc"));
        con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 1;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.anchor = GridBagConstraints.WEST;
        con.insets = new java.awt.Insets(12, 0, 0, 0);
        con.weightx = 0.0;
        con.weighty = 0.0;
        add(label, con);

        con = new GridBagConstraints();
        con.gridx = 1;
        con.gridy = 1;
        con.gridwidth = 3;
        con.gridheight = 1;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.insets = new java.awt.Insets(12, 12, 0, 0);
        con.weightx = 1.0;
        con.weighty = 0.0;
        coltypecombo = new JComboBox(ttab.toArray());
        coltypecombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateState();
            }
        });
        coltypecombo.setName(ColumnItem.TYPE);
        coltypecombo.setToolTipText(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnTypeComboBoxA11yDesc"));
        coltypecombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnTypeComboBoxA11yName"));
        label.setLabelFor(coltypecombo);
        add(coltypecombo, con);

        // Column size

        label = new JLabel();
        Mnemonics.setLocalizedText(label, NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumnSize")); //NOI18N
        label.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnSizeA11yDesc"));
        con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 2;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.anchor = GridBagConstraints.WEST;
        con.insets = new java.awt.Insets(12, 0, 0, 0);
        con.weightx = 0.0;
        con.weighty = 0.0;
        add(label, con);

        con = new GridBagConstraints();
        con.gridx = 1;
        con.gridy = 2;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.insets = new java.awt.Insets(12, 12, 0, 0);
        con.weightx = 1.0;
        con.weighty = 0.0;
        colsizefield = new JTextField();
        colsizefield.setName(ColumnItem.SIZE);
        colsizefield.getDocument().addDocumentListener(docListener);
        colsizefield.setToolTipText(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnSizeTextFieldA11yDesc"));
        colsizefield.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnSizeTextFieldA11yName"));
        label.setLabelFor(colsizefield);
        add(colsizefield, con);

        // Column scale

        label = new JLabel();
        Mnemonics.setLocalizedText(label, NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumnScale")); //NOI18N
        label.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnScaleA11yDesc"));
        con = new GridBagConstraints();
        con.gridx = 2;
        con.gridy = 2;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.anchor = GridBagConstraints.WEST;
        con.insets = new java.awt.Insets(12, 12, 0, 0);
        con.weightx = 0.0;
        con.weighty = 0.0;
        add(label, con);

        con = new GridBagConstraints();
        con.gridx = 3;
        con.gridy = 2;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.insets = new java.awt.Insets(12, 12, 0, 0);
        con.weightx = 1.0;
        con.weighty = 0.0;
        colscalefield = new JTextField();
        colscalefield.setName(ColumnItem.SCALE);
        colscalefield.getDocument().addDocumentListener(docListener);
        colscalefield.setToolTipText(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnScaleTextFieldA11yDesc"));
        colscalefield.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnScaleTextFieldA11yName"));
        label.setLabelFor(colscalefield);
        add(colscalefield, con);

        // Column default value

        label = new JLabel();
        Mnemonics.setLocalizedText(label, NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumnDefault")); //NOI18N
        label.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnDefaultA11yDesc"));
        con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 3;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.anchor = GridBagConstraints.WEST;
        con.insets = new java.awt.Insets(12, 0, 0, 0);
        con.weightx = 0.0;
        con.weighty = 0.0;
        add(label, con);

        con = new GridBagConstraints();
        con.gridx = 1;
        con.gridy = 3;
        con.gridwidth = 3;
        con.gridheight = 1;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.insets = new java.awt.Insets(12, 12, 0, 0);
        con.weightx = 1.0;
        con.weighty = 0.0;
        defvalfield = new JTextField(35);
        defvalfield.setName(ColumnItem.DEFVAL);
        defvalfield.getDocument().addDocumentListener(docListener);
        defvalfield.setToolTipText(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnDefaultTextFieldA11yDesc"));
        defvalfield.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnDefaultTextFieldA11yName"));
        label.setLabelFor(defvalfield);
        layout.setConstraints(defvalfield, con);
        add(defvalfield);

        // Check subpane

        JPanel subpane = new JPanel();
        subpane.setBorder(new TitledBorder(NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumnConstraintsTitle"))); //NOI18N
        GridBagLayout sublayout = new GridBagLayout();
        subpane.setLayout(sublayout);

        ItemListener checkBoxListener = new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                JCheckBox cbx = (JCheckBox) e.getSource();
                // just set value. Validation is handled in model
                dmodel.setValue(Boolean.valueOf(cbx.isSelected()), cbx.getName(), 0);
            }
        };

        con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.anchor = GridBagConstraints.WEST;
        con.insets = new java.awt.Insets(0, 0, 0, 0);
        con.weightx = 0.0;
        con.weighty = 0.0;
        pkcheckbox = new JCheckBox();
        Mnemonics.setLocalizedText(pkcheckbox, NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumnConstraintPKTitle")); //NOI18N
        pkcheckbox.setName(ColumnItem.PRIMARY_KEY);
        pkcheckbox.addItemListener(checkBoxListener);
        pkcheckbox.setToolTipText(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnConstraintPKTitleA11yDesc"));
        subpane.add(pkcheckbox, con);

        con = new GridBagConstraints();
        con.gridx = 1;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.anchor = GridBagConstraints.WEST;
        con.insets = new java.awt.Insets(0, 12, 0, 0);
        con.weightx = 0.0;
        con.weighty = 0.0;
        uniquecheckbox = new JCheckBox();
        Mnemonics.setLocalizedText(uniquecheckbox, NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumnConstraintUniqueTitle")); //NOI18N
        uniquecheckbox.setName(ColumnItem.UNIQUE);
        uniquecheckbox.addItemListener(checkBoxListener);
        uniquecheckbox.setToolTipText(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnConstraintUniqueTitleA11yDesc"));
        subpane.add(uniquecheckbox, con);

        con = new GridBagConstraints();
        con.gridx = 2;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.anchor = GridBagConstraints.WEST;
        con.insets = new java.awt.Insets(0, 12, 0, 0);
        con.weightx = 0.0;
        con.weighty = 0.0;
        nullcheckbox = new JCheckBox();
        Mnemonics.setLocalizedText(nullcheckbox, NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumnConstraintNullTitle")); //NOI18N
        nullcheckbox.setName(ColumnItem.NULLABLE);
        nullcheckbox.addItemListener(checkBoxListener);
        nullcheckbox.setToolTipText(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnConstraintNullTitleA11yDesc"));
        subpane.add(nullcheckbox, con);

        con = new GridBagConstraints();
        con.gridx = 3;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.anchor = GridBagConstraints.WEST;
        con.insets = new java.awt.Insets(0, 12, 0, 0);
        con.weightx = 0.0;
        con.weighty = 0.0;
        ixcheckbox = new JCheckBox();
        Mnemonics.setLocalizedText(ixcheckbox, NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumnIndexName")); //NOI18N
        ixcheckbox.setName(ColumnItem.INDEX);
        ixcheckbox.addItemListener(checkBoxListener);
        ixcheckbox.setToolTipText(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnIndexNameA11yDesc"));
        subpane.add(ixcheckbox, con);

        // Insert subpane

        con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 4;
        con.gridwidth = 4;
        con.gridheight = 1;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.insets = new java.awt.Insets(12, 0, 0, 0);
        con.weightx = 1.0;
        con.weighty = 0.0;
        add(subpane, con);

        // Check title and textarea

        con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 5;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.anchor = GridBagConstraints.NORTHWEST;
        con.insets = new java.awt.Insets(12, 0, 0, 0);
        con.weightx = 0.0;
        con.weighty = 0.0;
        checkcheckbox = new JCheckBox();
        Mnemonics.setLocalizedText(checkcheckbox, NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumnConstraintCheckTitle")); //NOI18N
        checkcheckbox.setName(ColumnItem.CHECK);
        checkcheckbox.addItemListener(checkBoxListener);
        checkcheckbox.setToolTipText(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnCheckTitleA11yDesc"));
        add(checkcheckbox, con);

        con = new GridBagConstraints();
        con.gridx = 1;
        con.gridy = 5;
        con.gridwidth = 3;
        con.gridheight = 1;
        con.fill = GridBagConstraints.BOTH;
        con.insets = new java.awt.Insets(12, 12, 0, 0);
        con.weightx = 1.0;
        con.weighty = 1.0;
        checkfield = new JTextArea(3, 35);
        checkfield.setName(ColumnItem.CHECK_CODE);
        checkfield.getDocument().addDocumentListener(docListener);
        checkfield.setToolTipText(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnCheckTextAreaA11yDesc"));
        checkfield.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnCheckTextAreaA11yName"));
        checkfield.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnCheckTextAreaA11yDesc"));
        JScrollPane spane = new JScrollPane(checkfield);
        add(spane, con);

        checkcheckbox.setSelected(false);
        checkcheckbox.setSelected(false);
        nullcheckbox.setSelected(true);
        uniquecheckbox.setSelected(false);

        // update changes of model to check boxes
        item.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String pname = evt.getPropertyName();
                Object nval = evt.getNewValue();
                if (nval instanceof Boolean) {
                    boolean set = ((Boolean) nval).booleanValue();
                    if (pname.equals(ColumnItem.PRIMARY_KEY)) {
                        pkcheckbox.setSelected(set);
                    } else if (pname.equals(ColumnItem.INDEX)) {
                        ixcheckbox.setSelected(set);
                    } else if (pname.equals(ColumnItem.UNIQUE)) {
                        uniquecheckbox.setSelected(set);
                        ixcheckbox.setEnabled(!set);
                    } else if (pname.equals(ColumnItem.NULLABLE)) {
                        nullcheckbox.setSelected(set);
                    }
                }
            }
        });

        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddTableColumnDialog.class, "ACS_AddTableColumnDialogA11yDesc"));

        doLayout();

        Dimension referenceCurDimension = colsizefield.getSize();
        Dimension referencePrefDimension = colsizefield.getPreferredSize();
        Dimension referenceMinDimension = colsizefield.getMinimumSize();
        Dimension referenceMaxDimension = colsizefield.getMaximumSize();

        colsizefield.setSize(referenceCurDimension);
        colsizefield.setPreferredSize(referencePrefDimension);
        colsizefield.setMinimumSize(referenceMinDimension);
        colsizefield.setMaximumSize(referenceMaxDimension);
        colscalefield.setSize(referenceCurDimension);
        colscalefield.setPreferredSize(referencePrefDimension);
        colscalefield.setMinimumSize(referenceMinDimension);
        colscalefield.setMaximumSize(referenceMaxDimension);
    }

    /** Returns Integer instance from given text field or null if cannot be parsed.
     * If field is empty it returns zero. */
    private Integer getIntValue(JTextField textField) {
        String text = textField.getText();
        try {
            if (text == null || text.length() == 0) {
                text = "0";
            }
            return Integer.valueOf(text);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    /** Validate and update state of model and UI. */
    private void updateState() {
        assert statusLine != null : "Notification status line not available";  //NOI18N

        // enable/disable size/scale text field
        Object selectedItem = coltypecombo.getSelectedItem();
        String columnType = selectedItem == null ? null : selectedItem.toString();
        if (sizelesstypes.contains(columnType)) {
            if (colsizefield.isEditable()) {
                colsizefield.setText(null);
                colscalefield.setText(null);
            }
            colsizefield.setEditable(false);
            colscalefield.setEditable(false);
            colsizefield.setEnabled(false);
            colscalefield.setEnabled(false);
        } else {
            boolean disableScale = charactertypes.contains(columnType);
            colsizefield.setEditable(true);
            colscalefield.setEditable(! disableScale);
            colsizefield.setEnabled(true);
            colscalefield.setEnabled(! disableScale);
            if (disableScale) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        colscalefield.setText(null);
                    }
                });
            }
        }

        String columnName = colnamefield.getText();
        if (columnName == null || columnName.length() < 1) {
            statusLine.setInformationMessage(NbBundle.getMessage (AddTableColumnDialog.class, "AddTableColumn_EmptyColName"));
            updateOK(false);
            return;
        }

        Integer size = getIntValue(colsizefield);
        if (size == null) {
            statusLine.setInformationMessage(NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumn_SizeNotNumber"));
            updateOK(false);
            return;
        }
        Integer scale = getIntValue(colscalefield);
        if (scale == null) {
            statusLine.setInformationMessage(NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumn_ScaleNotNumber"));
            updateOK(false);
            return;
        }
        if (columnType.equals("VARCHAR")) {  //NOI18N
            if (size == 0) {
                statusLine.setInformationMessage(NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumn_NotVarcharSize"));
                updateOK(false);
                return;
            }
            // #155142 - check size of default value for VARCHAR
            if (defvalfield.getText().length() > size) {
                statusLine.setInformationMessage(NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumn_DefaultOverSize"));
                updateOK(false);
                return;
            }
        }
        if (size < scale) {
            statusLine.setInformationMessage(NbBundle.getMessage(AddTableColumnDialog.class, "AddTableColumn_ScaleOverSize"));
            updateOK(false);
            return;
        }
        // update model
        dmodel.setValue(columnName, colnamefield.getName(), 0);
        dmodel.setValue(scale, colscalefield.getName(), 0);
        dmodel.setValue(size, colsizefield.getName(), 0);
        dmodel.setValue(defvalfield.getText(), defvalfield.getName(), 0);
        dmodel.setValue(checkfield.getText(), checkfield.getName(), 0);
        dmodel.setValue(coltypecombo.getSelectedItem(), coltypecombo.getName(), 0);

        statusLine.clearMessages();
        updateOK(true);
    }

    /** Updates OK button. */
    private void updateOK(boolean valid) {
        if (descriptor != null) {
            descriptor.setValid(valid);
        }
    }

    private void setDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        this.statusLine = descriptor.getNotificationLineSupport();
        updateState();
    }

    private ColumnItem getColumnItem() {
        return (ColumnItem)dmodel.getData().get(0);
    }

    /** Sets UI controls according to given ColumnItem. */
    private void setValues(ColumnItem columnItem) {
        colnamefield.setText(columnItem.getName());
        coltypecombo.setSelectedItem(columnItem.getType());
        if (!sizelesstypes.contains(columnItem.getType().toString())) {
            colsizefield.setText(String.valueOf(columnItem.getSize()));
            colscalefield.setText(String.valueOf(columnItem.getScale()));
        }
        defvalfield.setText(columnItem.getDefaultValue());
        pkcheckbox.setSelected(columnItem.isPrimaryKey());
        uniquecheckbox.setSelected(columnItem.isUnique());
        nullcheckbox.setSelected(columnItem.allowsNull());
        ixcheckbox.setSelected(columnItem.isIndexed());
        checkcheckbox.setSelected(columnItem.hasCheckConstraint());
        checkfield.setText(columnItem.getCheckConstraint());
    }

    /**
     * Shows Add Column dialog and returns ColumnItem instance or null if
     * cancelled.
     * @param spec DB specification
     * @param columnItem column item to be edited or null
     * @return ColumnItem instance or null if cancelled
     */
    public static ColumnItem showDialog(Specification spec, ColumnItem columnItem) {
        AddTableColumnDialog panel = new AddTableColumnDialog(spec);
        DialogDescriptor descriptor = new DialogDescriptor(panel, NbBundle.getMessage(AddTableColumnDialog.class, "AddColumnDialogTitle")); //NOI18N
        descriptor.createNotificationLineSupport();
        panel.setDescriptor(descriptor);
        if (columnItem != null) {
            panel.setValues(columnItem);
        }
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            return panel.getColumnItem();
        }
        return null;
    }

    /**
     *  Shows Add Column dialog and creates a new column in specified table.
     * @param spec DB specification
     * @param node table node
     * @return true if new column successfully added, false if cancelled
     */
    public static boolean showDialogAndCreate(Specification spec, TableNode node) {
        final AddTableColumnDialog panel = new AddTableColumnDialog(spec);
        DialogDescriptor descriptor = new DialogDescriptor(panel, NbBundle.getMessage(AddTableColumnDialog.class, "AddColumnDialogTitle")); //NOI18N
        descriptor.createNotificationLineSupport();
        // inbuilt close of the dialog is only after CANCEL button click
        // after OK button is dialog closed by hand
        descriptor.setClosingOptions(new Object[]{DialogDescriptor.CANCEL_OPTION});
        panel.setDescriptor(descriptor);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);

        String tableName = node.getName();
        String schemaName = node.getSchemaName();
        String catName = node.getCatalogName();
        if (schemaName == null) {
            schemaName = catName;
        }
        final AddTableColumnDDL ddl = new AddTableColumnDDL(spec, schemaName, tableName);

        ActionListener listener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                if (event.getSource() != DialogDescriptor.OK_OPTION) {
                    return;
                }
                final ColumnItem columnItem = (ColumnItem) panel.dmodel.getData().get(0);
                boolean wasException;
                try {
                    wasException = DbUtilities.doWithProgress(null, new Callable<Boolean>() {

                        @Override
                        public Boolean call() throws Exception {
                            return ddl.execute(panel.colnamefield.getText(), columnItem);
                        }
                    });
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof DDLException) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                    } else {
                        LOGGER.log(Level.INFO, cause.getLocalizedMessage(), cause);
                        DbUtilities.reportError(NbBundle.getMessage(AddTableColumnDialog.class, "ERR_UnableToAddColumn"), e.getMessage());
                    }
                    return;
                }

                // was execution of commands with or without exception?
                if (wasException) {
                    return;
                }
                // dialog is closed after successfully add column
                dialog.setVisible(false);
                dialog.dispose();
            }
        };

        descriptor.setButtonListener(listener);
        dialog.setVisible(true);
        if (descriptor.getValue() == DialogDescriptor.CANCEL_OPTION) {
            return false;
        }
        return true;
    }
}
