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
package org.netbeans.modules.css.prep.ui.customizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.util.CssPreprocessorUtils;
import org.netbeans.modules.web.common.api.CssPreprocessor;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorsUI;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public class OptionsPanel extends JPanel {

    private static final long serialVersionUID = 16987546576769L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final CssPreprocessorType type;
    private final Project project;
    // @GuardedBy("EDT")
    private final MappingsTableModel mappingsTableModel;
    // we must be thread safe
    private final List<Pair<String, String>> mappings = new CopyOnWriteArrayList<>();

    // we must be thread safe
    volatile boolean configured = false;
    volatile boolean enabled;
    volatile String compilerOptions;
    // #253814
    // @GuardedBy("EDT")
    private boolean recompileButtonDisabled = false;


    public OptionsPanel(CssPreprocessorType type, Project project, boolean initialEnabled, List<Pair<String, String>> initialMappings, String initialCompilerOptions) {
        assert EventQueue.isDispatchThread();
        assert type != null;
        assert project != null;

        this.type = type;
        this.project = project;
        mappingsTableModel = new MappingsTableModel(mappings);
        enabled = initialEnabled;
        compilerOptions = initialCompilerOptions;

        initComponents();
        init(initialEnabled, initialMappings, initialCompilerOptions);
    }

    @NbBundle.Messages({
        "# {0} - preprocessor name",
        "OptionsPanel.compilationEnabled.label=Co&mpile {0} Files on Save",
    })
    private void init(boolean initialEnabled, List<Pair<String, String>> initialMappings, String initialCompilerOptions) {
        assert EventQueue.isDispatchThread();
        configureExecutablesButton.setVisible(false);
        Mnemonics.setLocalizedText(enabledCheckBox, Bundle.OptionsPanel_compilationEnabled_label(type.getDisplayName()));
        // values
        mappingsTable.setModel(mappingsTableModel);
        setCompilationEnabled(initialEnabled);
        setMappings(initialMappings);
        setCompilerOptions(initialCompilerOptions);
        // ui
        enablePanel(initialEnabled);
        enableRemoveButton();
        enableMoveButtons();
        if ("Mac OS X".equals(UIManager.getLookAndFeel().getName())) { //NOI18N
            mappingsTable.setShowGrid(true);
            mappingsTable.setGridColor(Color.GRAY);
        }
        // listeners
        enabledCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enabled = e.getStateChange() == ItemEvent.SELECTED;
                configured = true;
                enablePanel(enabled);
                fireChange(false);
            }
        });
        mappingsTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                fireChange(true);
            }
        });
        mappingsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                enableRemoveButton();
                enableMoveButtons();
            }
        });
        compilerOptionsTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                processChange();
            }
            private void processChange() {
                compilerOptions = compilerOptionsTextField.getText();
                fireChange(true);
            }
        });
    }

    public void showConfigureExecutableButton() {
        configureExecutablesButton.setVisible(true);
    }

    public boolean isConfigured() {
        return configured;
    }

    public boolean isCompilationEnabled() {
        return enabled;
    }

    public void setCompilationEnabled(boolean enabled) {
        assert EventQueue.isDispatchThread();
        enabledCheckBox.setSelected(enabled);
    }

    public List<Pair<String, String>> getMappings() {
        return Collections.unmodifiableList(mappings);
    }

    public void setMappings(List<Pair<String, String>> mappings) {
        assert EventQueue.isDispatchThread();
        this.mappings.clear();
        this.mappings.addAll(mappings);
        mappingsTableModel.fireMappingsChange();
    }

    public String getCompilerOptions() {
        assert compilerOptions != null;
        return compilerOptions;
    }

    public void setCompilerOptions(String compilerOptions) {
        assert EventQueue.isDispatchThread();
        compilerOptionsTextField.setText(compilerOptions);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void fireChange(boolean disableRecompile) {
        assert EventQueue.isDispatchThread();
        changeSupport.fireChange();
        if (disableRecompile) {
            boolean refresh = !recompileButtonDisabled;
            recompileButtonDisabled = true;
            if (refresh) {
                enableRecompileButton();
            }
        }
    }

    void enablePanel(boolean enabled) {
        if (enabled) {
            enableRecompileButton();
        } else {
            recompileButton.setEnabled(false);
        }
        watchLabel.setEnabled(enabled);
        mappingsTable.setEnabled(enabled);
        mappingsInfoLabel.setEnabled(enabled);
        addButton.setEnabled(enabled);
        configureExecutablesButton.setEnabled(enabled);
        if (enabled) {
            enableRemoveButton();
            enableMoveButtons();
        } else {
            removeButton.setEnabled(false);
            moveDownButton.setEnabled(false);
            moveUpButton.setEnabled(false);
        }
        compilerOptionsLabel.setEnabled(enabled);
        compilerOptionsTextField.setEnabled(enabled);
        compilerOptionsInfoLabel.setEnabled(enabled);
    }

    @NbBundle.Messages({
        "OptionsPanel.recompile.error.noWebRoot=No web root is set.",
        "OptionsPanel.recompile.error.changeDetected=Save and reopen this dialog first.",
    })
    void enableRecompileButton() {
        assert EventQueue.isDispatchThread();
        FileObject webRoot = CssPreprocessorUtils.getWebRoot(project);
        if (webRoot == null) {
            recompileButton.setEnabled(false);
            recompileButton.setToolTipText(Bundle.OptionsPanel_recompile_error_noWebRoot());
            return;
        }
        if (recompileButtonDisabled) {
            recompileButton.setEnabled(false);
            recompileButton.setToolTipText(Bundle.OptionsPanel_recompile_error_changeDetected());
            return;
        }
        ValidationResult result = type.getPreferencesValidator()
                .validateMappings(webRoot, true, mappings)
                .getResult();
        recompileButton.setEnabled(result.isFaultless());
        recompileButton.setToolTipText(null);
    }

    void enableRemoveButton() {
        removeButton.setEnabled(mappingsTable.getSelectedRowCount() > 0);
    }

    void enableMoveButtons() {
        int[] selectedRows = mappingsTable.getSelectedRows();
        if (selectedRows.length == 0) {
            moveDownButton.setEnabled(false);
            moveUpButton.setEnabled(false);
            return;
        }
        moveDownButton.setEnabled(Arrays.binarySearch(selectedRows, mappingsTable.getRowCount() - 1) < 0);
        moveUpButton.setEnabled(Arrays.binarySearch(selectedRows, 0) < 0);
    }

    private void selectRows(int[] selectedRows, int delta) {
        ListSelectionModel listSelectionModel = mappingsTable.getSelectionModel();
        listSelectionModel.clearSelection();
        for (int selectedRow : selectedRows) {
            listSelectionModel.addSelectionInterval(selectedRow + delta, selectedRow + delta);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        enabledCheckBox = new JCheckBox();
        configureExecutablesButton = new JButton();
        watchLabel = new JLabel();
        recompileButton = new JButton();
        mappingsScrollPane = new JScrollPane();
        mappingsTable = new JTable();
        addButton = new JButton();
        removeButton = new JButton();
        moveUpButton = new JButton();
        moveDownButton = new JButton();
        mappingsInfoLabel = new JLabel();
        compilerOptionsLabel = new JLabel();
        compilerOptionsTextField = new JTextField();
        compilerOptionsInfoLabel = new JLabel();

        Mnemonics.setLocalizedText(enabledCheckBox, "COMPILATION_ON_SAVE"); // NOI18N

        Mnemonics.setLocalizedText(configureExecutablesButton, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.configureExecutablesButton.text")); // NOI18N
        configureExecutablesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configureExecutablesButtonActionPerformed(evt);
            }
        });

        watchLabel.setLabelFor(mappingsTable);
        Mnemonics.setLocalizedText(watchLabel, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.watchLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(recompileButton, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.recompileButton.text")); // NOI18N
        recompileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                recompileButtonActionPerformed(evt);
            }
        });

        mappingsScrollPane.setViewportView(mappingsTable);

        Mnemonics.setLocalizedText(addButton, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(moveUpButton, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.moveUpButton.text")); // NOI18N
        moveUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(moveDownButton, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.moveDownButton.text")); // NOI18N
        moveDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(mappingsInfoLabel, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.mappingsInfoLabel.text")); // NOI18N

        compilerOptionsLabel.setLabelFor(compilerOptionsTextField);
        Mnemonics.setLocalizedText(compilerOptionsLabel, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.compilerOptionsLabel.text")); // NOI18N

        compilerOptionsTextField.setColumns(20);

        Mnemonics.setLocalizedText(compilerOptionsInfoLabel, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.compilerOptionsInfoLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(configureExecutablesButton))
            .addGroup(layout.createSequentialGroup()
                .addComponent(mappingsScrollPane, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(addButton)
                    .addComponent(removeButton)
                    .addComponent(moveDownButton, GroupLayout.Alignment.LEADING)
                    .addComponent(moveUpButton)))
            .addGroup(layout.createSequentialGroup()
                .addComponent(compilerOptionsLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(compilerOptionsInfoLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(compilerOptionsTextField)))
            .addGroup(layout.createSequentialGroup()
                .addComponent(watchLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(recompileButton))
            .addGroup(layout.createSequentialGroup()
                .addComponent(mappingsInfoLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {addButton, moveDownButton, moveUpButton, removeButton});

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(enabledCheckBox)
                    .addComponent(configureExecutablesButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(watchLabel)
                    .addComponent(recompileButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(mappingsScrollPane, GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveUpButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveDownButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mappingsInfoLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(compilerOptionsLabel)
                    .addComponent(compilerOptionsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compilerOptionsInfoLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        assert EventQueue.isDispatchThread();
        mappings.add(Pair.of("", "")); // NOI18N
        mappingsTableModel.fireMappingsChange();
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        assert EventQueue.isDispatchThread();
        int[] selectedRows = mappingsTable.getSelectedRows();
        assert selectedRows.length > 0;
        for (int i = selectedRows.length - 1; i >= 0; --i) {
            mappings.remove(selectedRows[i]);
        }
        mappingsTableModel.fireMappingsChange();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void configureExecutablesButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configureExecutablesButtonActionPerformed
        OptionsDisplayer.getDefault().open(CssPreprocessorsUI.OPTIONS_PATH);
    }//GEN-LAST:event_configureExecutablesButtonActionPerformed

    private void recompileButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_recompileButtonActionPerformed
        assert EventQueue.isDispatchThread();
        CssPreprocessor cssPreprocessor = CssPreprocessors.getDefault().getCssPreprocessor(type.getIdentifier());
        assert cssPreprocessor != null : "CSS preprocessor must be found for identifier: " + type.getIdentifier();
        FileObject webRoot = CssPreprocessorUtils.getWebRoot(project);
        assert webRoot != null : "No web root found for project: " + project.getProjectDirectory();
        for (Pair<String, String> mapping : mappings) {
            FileObject input = FileUtil.toFileObject(CssPreprocessorUtils.resolveInput(webRoot, mapping));
            if (input == null) {
                // non-existing file
                continue;
            }
            CssPreprocessors.getDefault().process(cssPreprocessor, project, input);
        }
    }//GEN-LAST:event_recompileButtonActionPerformed

    private void moveUpButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        assert EventQueue.isDispatchThread();
        int[] selectedRows = mappingsTable.getSelectedRows();
        for (int selectedRow : selectedRows) {
            Pair<String, String> up = mappings.get(selectedRow);
            Pair<String, String> down = mappings.get(selectedRow - 1);
            mappings.set(selectedRow - 1, up);
            mappings.set(selectedRow, down);
        }
        mappingsTableModel.fireMappingsChange();
        selectRows(selectedRows, -1);
    }//GEN-LAST:event_moveUpButtonActionPerformed

    private void moveDownButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        assert EventQueue.isDispatchThread();
        int[] selectedRows = mappingsTable.getSelectedRows();
        for (int i = selectedRows.length - 1; i >= 0; --i) {
            int selectedRow = selectedRows[i];
            Pair<String, String> down = mappings.get(selectedRow);
            Pair<String, String> up = mappings.get(selectedRow + 1);
            mappings.set(selectedRow + 1, down);
            mappings.set(selectedRow, up);
        }
        mappingsTableModel.fireMappingsChange();
        selectRows(selectedRows, 1);
    }//GEN-LAST:event_moveDownButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton addButton;
    private JLabel compilerOptionsInfoLabel;
    private JLabel compilerOptionsLabel;
    private JTextField compilerOptionsTextField;
    private JButton configureExecutablesButton;
    private JCheckBox enabledCheckBox;
    private JLabel mappingsInfoLabel;
    private JScrollPane mappingsScrollPane;
    private JTable mappingsTable;
    private JButton moveDownButton;
    private JButton moveUpButton;
    private JButton recompileButton;
    private JButton removeButton;
    private JLabel watchLabel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class MappingsTableModel extends AbstractTableModel {

        private static final long serialVersionUID = -65325657686411L;

        private final List<Pair<String, String>> mappings;


        public MappingsTableModel(List<Pair<String, String>> mappings) {
            assert mappings != null;
            this.mappings = mappings;
        }

        @Override
        public int getRowCount() {
            return mappings.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Pair<String, String> pair = mappings.get(rowIndex);
            if (columnIndex == 0) {
                return pair.first();
            }
            if (columnIndex == 1) {
                return pair.second();
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            String path = (String) aValue;
            Pair<String, String> pair = mappings.get(rowIndex);
            if (columnIndex == 0) {
                mappings.set(rowIndex, Pair.of(path, pair.second()));
            } else if (columnIndex == 1) {
                mappings.set(rowIndex, Pair.of(pair.first(), path));
            } else {
                throw new IllegalStateException("Unknown column index: " + columnIndex);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @NbBundle.Messages({
            "MappingsTableModel.column.input.title=Input",
            "MappingsTableModel.column.output.title=Output",
        })
        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return Bundle.MappingsTableModel_column_input_title();
            }
            if (columnIndex == 1) {
                return Bundle.MappingsTableModel_column_output_title();
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0
                    || columnIndex == 1) {
                return String.class;
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0
                    || columnIndex == 1) {
                return true;
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        public void fireMappingsChange() {
            assert EventQueue.isDispatchThread();
            fireTableDataChanged();
        }

    }

}
