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
package org.netbeans.modules.web.wizards;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** 
 * Panel where the user enters the settings to create a new Object. 
 *
 * @author Ana von Klopp
 */
public class MappingEditor extends JPanel implements ActionListener {

    // UI Input Components
    private JRadioButton urlRadio;
    private JRadioButton servletRadio;
    private JTextField mappingField;
    private ToolTipCombo<String> servletCombo;
    private JCheckBox[] cb;
    private Dialog dialog;
    private DialogDescriptor editDialog;
    private static final String URL = "URL";
    private static final String SERVLET = "SERVLET";
    private static final String SELECT_SERVLET = "SELECT";
    private FilterMappingData fmd;
    private boolean haveNames = true;
    private boolean OK = false;
    private static final long serialVersionUID = 4947167720581796971L;

    /**  Creates new form MappingEditor */
    public MappingEditor(FilterMappingData fmd, String[] servletNames) {
        this.fmd = fmd;
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MappingEditor.class, "ACSD_filter_mappings_edit"));
        initComponents(servletNames);
    }

    boolean isOK() {
        return OK;
    }

    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.wizards.MappingEditor");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents(String[] names) {
        Insets insets = new Insets(4, 20, 4, 0);
        Insets endInsets = new Insets(4, 20, 4, 60);

        // Entity covers entire row
        GridBagConstraints fullRowC = new GridBagConstraints();
        fullRowC.gridx = 0;
        fullRowC.gridy = GridBagConstraints.RELATIVE;
        fullRowC.gridwidth = GridBagConstraints.REMAINDER;
        fullRowC.anchor = GridBagConstraints.WEST;
        fullRowC.fill = GridBagConstraints.HORIZONTAL;
        fullRowC.insets = endInsets;

        // Initial label
        GridBagConstraints firstC = new GridBagConstraints();
        firstC.gridx = 0;
        firstC.gridy = GridBagConstraints.RELATIVE;
        //firstC.weightx = 0.2; 
        firstC.anchor = GridBagConstraints.WEST;
        firstC.insets = insets;

        // Textfield covers end of row
        GridBagConstraints tflC = new GridBagConstraints();
        tflC.gridx = GridBagConstraints.RELATIVE;
        tflC.weightx = 0.8;
        tflC.gridwidth = GridBagConstraints.REMAINDER;
        tflC.fill = GridBagConstraints.HORIZONTAL;
        tflC.insets = endInsets;

        this.setLayout(new GridBagLayout());

        // Add the component rows
        // 1. Filter name
        JLabel jLname = new JLabel();
        jLname.setText(NbBundle.getMessage(MappingEditor.class,
                "LBL_name_filter")); //NOI18N

        JTextField jTFname = new JTextField(25);
        jTFname.setText(fmd.getName());
        jTFname.setEnabled(false);
        jTFname.setBackground(this.getBackground());
        jTFname.setDisabledTextColor(Color.black);


        jLname.setLabelFor(jTFname);
        jLname.setDisplayedMnemonic(NbBundle.getMessage(MappingEditor.class, "LBL_name_filter_mnem").charAt(0));
        jTFname.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MappingEditor.class, "ACSD_name_filter"));

        this.add(jLname, firstC);
        this.add(jTFname, tflC);

        // Create the radio buttons: web module button
        urlRadio = new JRadioButton(NbBundle.getMessage(MappingEditor.class, "LBL_url")); // NOI18N
        urlRadio.setMnemonic(NbBundle.getMessage(MappingEditor.class, "LBL_url_mnemonic").charAt(0)); // NOI18N
        urlRadio.addActionListener(this);
        urlRadio.setActionCommand(URL);
        urlRadio.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MappingEditor.class, "ACSD_pattern_mapping"));

        // Create the radio buttons: directory button
        servletRadio = new JRadioButton(NbBundle.getMessage(MappingEditor.class, "LBL_servlet")); // NOI18N
        servletRadio.setMnemonic(NbBundle.getMessage(MappingEditor.class, "LBL_servlet_mnemonic").charAt(0)); // NOI18N
        servletRadio.addActionListener(this);
        servletRadio.setActionCommand(SERVLET); // NOI18N
        servletRadio.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MappingEditor.class, "ACSD_servlet_mapping"));

        // Create the radio button group
        ButtonGroup group = new ButtonGroup();
        group.add(urlRadio);
        group.add(servletRadio);

        // 2. URL row
        this.add(urlRadio, firstC);
        mappingField = new JTextField();

        mappingField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MappingEditor.class, "LBL_url"));
        mappingField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MappingEditor.class, "ACSD_pattern_mapping_desc"));
        this.add(mappingField, tflC);

        // 3. Servlet row
        this.add(servletRadio, firstC);
        if (names == null || names.length == 0) {
            names = new String[1];
            names[0] = NbBundle.getMessage(MappingEditor.class,
                    "LBL_no_servlets"); //
            // NOI18N
            haveNames = false;
        }
        servletCombo = new ToolTipCombo(names);
        servletCombo.setBackground(this.getBackground());
        servletCombo.setActionCommand(SELECT_SERVLET);
        servletCombo.addActionListener(this);
        servletCombo.setEnabled(haveNames);
        servletCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MappingEditor.class, "ACSD_select_servlet"));
        servletCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MappingEditor.class, "ACSD_select_servlet_desc"));

        servletRadio.setEnabled(haveNames);
        this.add(servletCombo, tflC);


        if (fmd.getType() == FilterMappingData.Type.URL) {
            urlRadio.setSelected(true);
            mappingField.setText(fmd.getPattern());
        } else {
            servletRadio.setSelected(true);
            int size = servletCombo.getModel().getSize();
            for (int i = 0; i < size; ++i) {
                if (servletCombo.getModel().getElementAt(i).toString().equals(fmd.getPattern())) {
                    servletCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // 4. Conditions label
        JLabel conditions = new JLabel();
        conditions.setText(NbBundle.getMessage(MappingEditor.class,
                "LBL_conditions"));
        conditions.setDisplayedMnemonic(NbBundle.getMessage(MappingEditor.class, "LBL_conditions_mnemonic").charAt(0));
        this.add(conditions, fullRowC);

        // 5. Checkboxes for the conditions
        JPanel p0 = new JPanel();
        p0.setLayout(new FlowLayout(FlowLayout.LEFT));
        cb = new JCheckBox[4];
        String dispatcher;
        for (int i = 0; i < FilterMappingData.Dispatcher.getAll().length; ++i) {
            dispatcher = FilterMappingData.Dispatcher.getAll()[i].toString();
            cb[i] = new JCheckBox(dispatcher);
            cb[i].setMnemonic(dispatcher.charAt(0));
            cb[i].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MappingEditor.class, "ACSD_dispatcher_"+dispatcher));
            p0.add(cb[i]);
        }

        FilterMappingData.Dispatcher[] config = fmd.getDispatcher();
        for (int i = 0; i < config.length; ++i) {
            if (config[i] == FilterMappingData.Dispatcher.REQUEST) {
                cb[0].setSelected(true);
                continue;
            }
            if (config[i] == FilterMappingData.Dispatcher.FORWARD) {
                cb[1].setSelected(true);
                continue;
            }
            if (config[i] == FilterMappingData.Dispatcher.INCLUDE) {
                cb[2].setSelected(true);
                continue;
            }
            if (config[i] == FilterMappingData.Dispatcher.ERROR) {
                cb[3].setSelected(true);
                continue;
            }
        }
        this.add(p0, fullRowC);

        // 6. Add filler panel at the bottom
        JPanel p1 = new JPanel();
        GridBagConstraints filler = new java.awt.GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = GridBagConstraints.RELATIVE;
        filler.fill = java.awt.GridBagConstraints.HORIZONTAL;
        filler.weighty = 1.0;
        this.add(p1, filler);
    }

    public void showEditor() {
        String title = NbBundle.getMessage(MappingEditor.class, "TITLE_filter_mapping"); //NOI18N
        editDialog = new DialogDescriptor(this, title, true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.CANCEL_OPTION,
                DialogDescriptor.BOTTOM_ALIGN,
                null,
                this);

        dialog = DialogDisplayer.getDefault().createDialog(editDialog);
        dialog.pack();
        dialog.setVisible(true);
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand() == URL) {
            fmd.setType(FilterMappingData.Type.URL);
            fmd.setPattern(mappingField.getText().trim());
            mappingField.requestFocus();
            return;
        }

        if (evt.getActionCommand() == SERVLET) {
            if (!haveNames) {
                return;
            }
            fmd.setType(FilterMappingData.Type.SERVLET);
            fmd.setPattern(servletCombo.getSelectedItem().toString());
            servletCombo.requestFocus();
            return;
        }

        if (evt.getActionCommand() == SELECT_SERVLET) {
            if (!haveNames) {
                return;
            }
            fmd.setType(FilterMappingData.Type.SERVLET);
            servletRadio.setSelected(true);
            fmd.setPattern(servletCombo.getSelectedItem().toString());
            return;
        }

        Object retValue = editDialog.getValue();
        if (DialogDescriptor.CANCEL_OPTION.equals(retValue) || DialogDescriptor.CLOSED_OPTION.equals(retValue)) {
            OK = false;
            dialog.dispose();
            return;
        }

        if (fmd.getType() == FilterMappingData.Type.URL) {
            fmd.setPattern(mappingField.getText().trim());
            if (fmd.getPattern().length() == 0) {
                notifyBadInput(NbBundle.getMessage(TableRowDialog.class, "MSG_no_pattern"));
                return;
            }
        }

        int num = 0;
        for (int i = 0; i < 4; ++i) {
            if (cb[i].isSelected()) {
                num++;
            }
        }

        FilterMappingData.Dispatcher[] d = new FilterMappingData.Dispatcher[num];

        num = 0;
        if (cb[0].isSelected()) {
            d[num] = FilterMappingData.Dispatcher.REQUEST;
            ++num;
        }
        if (cb[1].isSelected()) {
            d[num] = FilterMappingData.Dispatcher.FORWARD;
            ++num;
        }
        if (cb[2].isSelected()) {
            d[num] = FilterMappingData.Dispatcher.INCLUDE;
            ++num;
        }
        if (cb[3].isSelected()) {
            d[num] = FilterMappingData.Dispatcher.ERROR;
        }

        fmd.setDispatcher(d);
        OK = true;
        dialog.dispose();
        return;
    }

    private void notifyBadInput(String msg) {
        Object[] options = {NotifyDescriptor.OK_OPTION};
        NotifyDescriptor badInputDialog =
                new NotifyDescriptor(msg, NbBundle.getMessage(TableRowDialog.class, "MSG_invalid_input"),
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.ERROR_MESSAGE,
                options,
                options[0]);
        DialogDisplayer.getDefault().notify(badInputDialog);
    }
}

