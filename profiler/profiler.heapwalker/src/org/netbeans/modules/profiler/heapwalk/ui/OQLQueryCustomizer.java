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

package org.netbeans.modules.profiler.heapwalk.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.heapwalk.OQLSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "OQLQueryCustomizer_OkButtonText=OK",
    "OQLQueryCustomizer_CloseButtonText=Close",
    "OQLQueryCustomizer_SaveQueryCaption=Save Query",
    "OQLQueryCustomizer_QueryPropertiesCaption={0} Properties",
    "OQLQueryCustomizer_UpButtonToolTip=Move selected query up in the list",
    "OQLQueryCustomizer_DownButtonToolTip=Move selected query down in the list",
    "OQLQueryCustomizer_UpButtonAccessName=Up",
    "OQLQueryCustomizer_DownButtonAccessName=Down",
    "OQLQueryCustomizer_NewQueryRadioText=&Create New Query",
    "OQLQueryCustomizer_ExistingQueryRadioText=&Use Existing Query",
    "OQLQueryCustomizer_NameLabelText=&Name:",
    "OQLQueryCustomizer_DefaultQueryName=New OQL Query",
    "OQLQueryCustomizer_DescriptionLabelText=&Description (optional):",
    "OQLQueryCustomizer_UpdateQueryLabelText=&Query to update:"
})
public class OQLQueryCustomizer {
    private static HelpCtx HELP_CTX_SAVE_QUERY = new HelpCtx("OQLQueryCustomizer.SaveQuery.HelpCtx");  //NOI18N
    private static HelpCtx HELP_CTX_QUERY_PROPS = new HelpCtx("OQLQueryCustomizer.QueryProps.HelpCtx");//NOI18N
    private static Icon ICON_UP = Icons.getIcon(GeneralIcons.UP);
    private static Icon ICON_DOWN = Icons.getIcon(GeneralIcons.DOWN);


    public static boolean saveQuery(final String query,
                                    final OQLSupport.OQLTreeModel treeModel,
                                    final JTree tree) {
        JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, Bundle.OQLQueryCustomizer_OkButtonText());

        CustomizerPanel customizer = new CustomizerPanel(okButton,  treeModel);
        final DialogDescriptor dd = new DialogDescriptor(customizer,
                                            Bundle.OQLQueryCustomizer_SaveQueryCaption(), true,
                                            new Object[] { okButton,
                                            DialogDescriptor.CANCEL_OPTION },
                                            okButton, 0, HELP_CTX_SAVE_QUERY, null);
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.pack();
        d.setVisible(true);

        if (dd.getValue() == okButton) {
            OQLSupport.OQLQueryNode node;
            if (customizer.isNewQuery()) {
                OQLSupport.Query q = new OQLSupport.Query(query,
                                        customizer.getQueryName(),
                                        customizer.getQueryDescription());
                node = new OQLSupport.OQLQueryNode(q);
                treeModel.customCategory().add(node);
                treeModel.nodeStructureChanged(treeModel.customCategory());
            } else {
                node = (OQLSupport.OQLQueryNode)customizer.getSelectedValue();
                node.getUserObject().setScript(query);
                treeModel.nodeChanged(node);
            }
            tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(node)));
            return true;
        } else {
            return false;
        }
    }

    public static boolean editNode(final OQLSupport.OQLNode node,
                                   final OQLSupport.OQLTreeModel treeModel,
                                   final JTree tree) {

        boolean readOnly = node.isReadOnly();
        final OQLSupport.OQLNode parent = (OQLSupport.OQLNode)node.getParent();
        int originalIndex = parent.getIndex(node);

        JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, Bundle.OQLQueryCustomizer_OkButtonText());

        final JButton[] upDownButtons = new JButton[2];
        upDownButtons[0] = new JButton(ICON_UP) {
            protected void fireActionPerformed(ActionEvent e) {
                int index = parent.getIndex(node) - 1;
                treeModel.removeNodeFromParent(node);
                treeModel.insertNodeInto(node, parent, index);
                tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(node)));
                updateButtons(upDownButtons, node);
            }
        };
        upDownButtons[0].setToolTipText(Bundle.OQLQueryCustomizer_UpButtonToolTip());
        upDownButtons[0].getAccessibleContext().
                                        setAccessibleName(Bundle.OQLQueryCustomizer_UpButtonAccessName());
        upDownButtons[1] = new JButton(ICON_DOWN) {
            protected void fireActionPerformed(ActionEvent e) {
                int index = parent.getIndex(node) + 1;
                treeModel.removeNodeFromParent(node);
                treeModel.insertNodeInto(node, parent, index);
                tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(node)));
                updateButtons(upDownButtons, node);
            }
        };
        upDownButtons[1].setToolTipText(Bundle.OQLQueryCustomizer_DownButtonToolTip());
        upDownButtons[1].getAccessibleContext().
                                      setAccessibleName(Bundle.OQLQueryCustomizer_DownButtonAccessName());

        final CustomizerPanel customizer = new CustomizerPanel(okButton,
                                                node.toString(),
                                                node.getDescription(),
                                                readOnly);

        customizer.getInputMap(CustomizerPanel.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_MASK), "MOVE_UP"); // NOI18N
        customizer.getActionMap().put("MOVE_UP", new AbstractAction() {// NOI18N
            public void actionPerformed(ActionEvent e) {
                if (upDownButtons[0].isEnabled()) upDownButtons[0].doClick();
            }
        });
        customizer.getInputMap(CustomizerPanel.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_MASK), "MOVE_DOWN"); // NOI18N
        customizer.getActionMap().put("MOVE_DOWN", new AbstractAction() {// NOI18N
            public void actionPerformed(ActionEvent e) {
                if (upDownButtons[1].isEnabled()) upDownButtons[1].doClick();
            }
        });

        JButton closeButton = readOnly ? new JButton() : null;
        if (closeButton != null) Mnemonics.setLocalizedText(closeButton, Bundle.OQLQueryCustomizer_CloseButtonText());
        Object[] options = readOnly ? new Object[] { closeButton } :
                                      new Object[] { okButton,
                                            DialogDescriptor.CANCEL_OPTION };
        JButton defaultButton = readOnly ? closeButton : okButton;

        final DialogDescriptor dd = new DialogDescriptor(customizer,
                                            Bundle.OQLQueryCustomizer_QueryPropertiesCaption(node.getCaption()),
                                            true, options, defaultButton, 0, HELP_CTX_QUERY_PROPS, null);
        dd.setAdditionalOptions(new Object[] { upDownButtons[0],
                                               upDownButtons[1] });
        updateButtons(upDownButtons, node);

        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);

        d.pack();
        d.setVisible(true);

        if (dd.getValue() == okButton) {
            OQLSupport.Query query = (OQLSupport.Query)node.getUserObject();
            query.setName(customizer.getQueryName());
            query.setDescription(customizer.getQueryDescription());
            treeModel.nodeChanged(node); // Updates UI
            return true;
        } else {
            int index = parent.getIndex(node);
            if (index != originalIndex) {
                treeModel.removeNodeFromParent(node);
                treeModel.insertNodeInto(node, parent, originalIndex);
                tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(node)));
            }
            return false;
        }
    }

    private static void updateButtons(JButton[] upDownButtons,
                                      OQLSupport.OQLNode node) {
        if (node.isReadOnly()) {
            upDownButtons[0].setEnabled(false);
            upDownButtons[1].setEnabled(false);
        } else {
            upDownButtons[0].setEnabled(node.getPreviousSibling() != null);
            upDownButtons[1].setEnabled(node.getNextSibling() != null);
        }
    }

    private static class CustomizerPanel extends JPanel {

        private JComponent submitComponent;
        private Object lastSelectedValue;


        public CustomizerPanel(JComponent submitComponent,
                               OQLSupport.OQLTreeModel treeModel) {
            this.submitComponent = submitComponent;

            initComponents(treeModel, false);
            updateComponents();
        }

        public CustomizerPanel(JComponent submitComponent, String name,
                               String description, boolean readOnly) {
            this.submitComponent = submitComponent;

            initComponents(null, readOnly);

            nameField.setText(name);
            descriptionArea.setText(description == null ? "" : description); // NOI18N
            try { descriptionArea.setCaretPosition(0); } catch (Exception e) {}

            updateComponents();
        }


        public boolean isNewQuery() {
            return newRadio == null || newRadio.isSelected();
        }

        public String getQueryName() {
            return nameField.getText().trim();
        }

        public String getQueryDescription() {
            String description = descriptionArea.getText().trim();
            return description.length() > 0 ? description : null;
        }

        public Object getSelectedValue() {
            return existingList.getSelectedValue();
        }


        private void updateComponents() {
            if (newRadio != null) {
                boolean createNew = newRadio.isSelected();

                nameLabel.setEnabled(createNew);
                nameField.setEnabled(createNew);
                descriptionLabel.setEnabled(createNew);
                descriptionArea.setEnabled(createNew);

                existingLabel.setEnabled(!createNew);
                if (createNew && existingList.isEnabled()) {
                    lastSelectedValue = existingList.getSelectedValue();
                    existingList.setEnabled(false);
                    existingList.clearSelection();
                } else if (!createNew && !existingList.isEnabled()) {
                    existingList.setEnabled(true);
                    if (lastSelectedValue == null)
                        lastSelectedValue = existingList.getModel().getElementAt(0);
                    existingList.setSelectedValue(lastSelectedValue, false);
                }
            }

            if (existingRadio != null && existingRadio.isSelected()) {
                submitComponent.setEnabled(existingList.getSelectedValue() != null);
            } else {
                submitComponent.setEnabled(nameField.getText().trim().length() > 0);
            }
        }


        private void initComponents(OQLSupport.OQLTreeModel treeModel, boolean readOnly) {
            final boolean allowExisting = treeModel != null && treeModel.hasCustomQueries();

            setLayout(new GridBagLayout());
            GridBagConstraints c;


            if (allowExisting) {
                JPanel headerContainer1 = new JPanel(new GridBagLayout());

                newRadio = new JRadioButton() {
                    protected void fireActionPerformed(ActionEvent e) { updateComponents(); }
                };
                Mnemonics.setLocalizedText(newRadio, Bundle.OQLQueryCustomizer_NewQueryRadioText());
                newRadio.setSelected(true);
                c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.NONE;
                c.insets = new Insets(0, 0, 0, 0);
                headerContainer1.add(newRadio, c);

                newSeparator = new JSeparator(JSeparator.HORIZONTAL) {
                    public Dimension getMinimumSize() {
                        return getPreferredSize();
                    }
                };
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 0;
                c.weightx = 1;
                c.weighty = 1;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.insets = new Insets(0, 0, 0, 0);
                headerContainer1.add(newSeparator, c);

                c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.insets = new Insets(8, 8, 0, 8);
                add(headerContainer1, c);
            }

            nameLabel = new JLabel();
            Mnemonics.setLocalizedText(nameLabel, Bundle.OQLQueryCustomizer_NameLabelText());
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(allowExisting ? 8 : 16, allowExisting ? 40 : 16, 8, 8);
            add(nameLabel, c);

            nameField = new JTextField();
            nameLabel.setLabelFor(nameField);
            nameField.setText(Bundle.OQLQueryCustomizer_DefaultQueryName());
            nameField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {  updateComponents(); }
                public void removeUpdate(DocumentEvent e) {  updateComponents(); }
                public void changedUpdate(DocumentEvent e) {  updateComponents(); }
            });
            nameField.setEditable(!readOnly);
            c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 1;
            c.weightx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(allowExisting ? 8 : 16, 0, 8, 16);
            add(nameField, c);

            descriptionLabel = new JLabel();
            Mnemonics.setLocalizedText(descriptionLabel, Bundle.OQLQueryCustomizer_DescriptionLabelText());
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 2;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(8, allowExisting ? 40 : 16, 8, 8);
            add(descriptionLabel, c);

            descriptionArea = new JTextArea();
            descriptionLabel.setLabelFor(descriptionArea);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setFont(descriptionLabel.getFont());
            descriptionArea.setRows(3);
            JScrollPane descriptionAreaScroll = new JScrollPane(descriptionArea,
                                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
                public Dimension getPreferredSize() {
                    return new Dimension(250, super.getPreferredSize().height);
                }
                public Dimension getMinimumSize() {
                    return allowExisting ? getPreferredSize() : super.getMinimumSize();
                }
            };
            descriptionArea.setEditable(!readOnly);
            if (readOnly) descriptionArea.setBackground(nameField.getBackground());
            c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 3;
            if (!allowExisting) c.weighty = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(0, 0, allowExisting ? 8 : 16, 16);
            add(descriptionAreaScroll, c);

            if (allowExisting) {
                JPanel headerContainer2 = new JPanel(new GridBagLayout());

                existingRadio = new JRadioButton() {
                    protected void fireActionPerformed(ActionEvent e) { updateComponents(); }
                };
                Mnemonics.setLocalizedText(existingRadio, Bundle.OQLQueryCustomizer_ExistingQueryRadioText());
                c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 4;
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.NONE;
                c.insets = new Insets(0, 0, 0, 0);
                headerContainer2.add(existingRadio, c);

                existingSeparator = new JSeparator(JSeparator.HORIZONTAL) {
                    public Dimension getMinimumSize() {
                        return getPreferredSize();
                    }
                };
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 4;
                c.weightx = 1;
                c.weighty = 1;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.insets = new Insets(0, 0, 0, 0);
                headerContainer2.add(existingSeparator, c);

                c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 4;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.insets = new Insets(8, 8, 0, 8);
                add(headerContainer2, c);

                existingLabel = new JLabel();
                Mnemonics.setLocalizedText(existingLabel, Bundle.OQLQueryCustomizer_UpdateQueryLabelText());
                c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 5;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.NONE;
                c.insets = new Insets(8, 40, 8, 8);
                add(existingLabel, c);

                Vector v = new Vector();
                Enumeration e = treeModel.customCategory().children();
                while (e.hasMoreElements()) v.add(e.nextElement());
                existingList = new JList(v);
                existingLabel.setLabelFor(existingList);
                existingList.setVisibleRowCount(3);
                existingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                existingList.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        updateComponents();
                    }
                });
                JScrollPane existingListScroll = new JScrollPane(existingList,
                                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = 6;
                c.weighty = 1;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.fill = GridBagConstraints.BOTH;
                c.insets = new Insets(0, 0, 16, 16);
                add(existingListScroll, c);

                ButtonGroup radios = new ButtonGroup();
                radios.add(newRadio);
                radios.add(existingRadio);
            }

            addHierarchyListener(new HierarchyListener() {
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                        if (isShowing()) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    nameField.requestFocus();
                                    nameField.selectAll();
                                }
                            });
                        }
                    }
                }
            });

        }


        private JRadioButton newRadio;
        private JSeparator newSeparator;
        private JLabel nameLabel;
        private JTextField nameField;
        private JLabel descriptionLabel;
        private JTextArea descriptionArea;
        private JRadioButton existingRadio;
        private JSeparator existingSeparator;
        private JLabel existingLabel;
        private JList existingList;

    }

}
