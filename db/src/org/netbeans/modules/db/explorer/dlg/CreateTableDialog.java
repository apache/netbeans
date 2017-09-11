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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.lib.ddl.DDLException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.util.PListReader;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.node.TableNode;
import org.openide.NotificationLineSupport;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

public class CreateTableDialog {
    Dialog dialog = null;
    JTextField dbnamefield, dbownerfield;
    JTable table;
    JButton addbtn, delbtn, editBtn, upBtn, downBtn;
    Specification spec;
    private final BaseNode tablesNode;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport statusLine;


    private static Map dlgtab = null;
    private static final String filename = "org/netbeans/modules/db/resources/CreateTableDialog.plist"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(
            CreateTableDialog.class.getName());

    public static Map getProperties() {
        if (dlgtab == null) try {
            ClassLoader cl = CreateTableDialog.class.getClassLoader();
            InputStream stream = cl.getResourceAsStream(filename);
            if (stream == null) {
                String message = NbBundle.getMessage (CreateTableDialog.class, "EXC_UnableToOpenStream", filename); // NOI18N
                throw new Exception(message);
            }
            PListReader reader = new PListReader(stream);
            dlgtab = reader.getData();
            stream.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            dlgtab = null;
        }

        return dlgtab;
    }

    public CreateTableDialog(final BaseNode node, final String schema) {
        DatabaseConnection connection = node.getLookup().lookup(DatabaseConnection.class);
                
        spec = connection.getConnector().getDatabaseSpecification();
        tablesNode = node;
        
        try {
            JLabel label;
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints constr = new GridBagConstraints();
            pane.setLayout(layout);
            pane.setMinimumSize(new Dimension(200,100));
     
            // Table name field

            label = new JLabel();
            Mnemonics.setLocalizedText(label, NbBundle.getMessage (CreateTableDialog.class, "CreateTableName")); // NOI18N
            label.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableNameA11yDesc"));
            constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.fill = GridBagConstraints.NONE;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 0;
            layout.setConstraints(label, constr);
            pane.add(label);

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.weighty = 0.0;
            constr.gridx = 1;
            constr.gridy = 0;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            dbnamefield = new JTextField(getTableUntitledName(), 10); 
            dbnamefield.setToolTipText(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableNameTextFieldA11yDesc"));
            dbnamefield.getAccessibleContext().setAccessibleName(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableNameTextFieldA11yName"));
            label.setLabelFor(dbnamefield);
            layout.setConstraints(dbnamefield, constr);
            pane.add(dbnamefield);
            dbnamefield.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void insertUpdate(DocumentEvent e) {
                    validate();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    validate();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    validate();
                }
            });

            // Table columns in scrollpane

            constr.fill = GridBagConstraints.BOTH;
            constr.weightx = 1.0;
            constr.weighty = 1.0;
            constr.gridx = 0;
            constr.gridy = 1;
            constr.gridwidth = 4;
            constr.gridheight = 3;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            table = new DataTable(new DataModel());
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setToolTipText(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableColumnTableA11yDesc"));
            table.getAccessibleContext().setAccessibleName(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableColumnTableA11yName"));
            table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableColumnTableA11yDesc"));
            JScrollPane scrollpane = new JScrollPane(table);
            scrollpane.setBorder(new BevelBorder(BevelBorder.LOWERED));
            layout.setConstraints(scrollpane, constr);
            pane.add(scrollpane);

            table.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent event) {
                    if (event.getClickCount() == 2) {
                        editBtn.doClick();
                    }
                }
            });
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    // update Edit and Remove buttons
                    validate();
                }
            });
            table.getModel().addTableModelListener(new TableModelListener() {

                @Override
                public void tableChanged(TableModelEvent e) {
                    validate();
                }
            });

            // Button pane

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.anchor = GridBagConstraints.NORTH;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.gridx = 4;
            constr.gridy = 1;
            constr.insets = new java.awt.Insets (2, 8, 2, 2);
            JPanel btnpane = new JPanel();
            GridLayout btnlay = new GridLayout(5,1,0,5);
            btnpane.setLayout(btnlay);
            layout.setConstraints(btnpane, constr);
            pane.add(btnpane);

            // Button add column

            addbtn = new JButton();
            Mnemonics.setLocalizedText(addbtn, NbBundle.getMessage (CreateTableDialog.class, "CreateTableAddButtonTitle")); // NOI18N
            addbtn.setToolTipText(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableAddButtonTitleA11yDesc"));
            btnpane.add(addbtn);
            addbtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {

                    ColumnItem columnItem = AddTableColumnDialog.showDialog(spec, null);
                    if (columnItem != null) {
                        DataModel model = (DataModel) table.getModel();
                        model.addRow(columnItem);
                    }
                }
            });

            // Button edit column

            editBtn = new JButton();
            Mnemonics.setLocalizedText(editBtn, NbBundle.getMessage(CreateTableDialog.class, "CreateTableEditButtonTitle")); // NOI18N
            editBtn.setToolTipText(NbBundle.getMessage(CreateTableDialog.class, "ACS_CreateTableEditButtonTitleA11yDesc")); // NOI18N
            btnpane.add(editBtn);
            editBtn.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {
                    int selectedIndex = table.getSelectedRow();
                    if (selectedIndex != -1) {
                        ColumnItem selectedColumnItem = ((DataModel) table.getModel()).getRow(selectedIndex);
                        ColumnItem columnItemModified = AddTableColumnDialog.showDialog(spec, selectedColumnItem);
                        if (columnItemModified != null) {
                            DataModel model = (DataModel) table.getModel();
                            model.removeRow(selectedIndex);
                            model.insertRow(selectedIndex, columnItemModified);
                        }
                    }
                }
            });

            // Button delete column

            delbtn = new JButton();
            Mnemonics.setLocalizedText(delbtn, NbBundle.getMessage (CreateTableDialog.class, "CreateTableRemoveButtonTitle")); // NOI18N
            delbtn.setToolTipText(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableRemoveButtonTitleA11yDesc")); // NOI18N
            btnpane.add(delbtn);
            delbtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    int idx = table.getSelectedRow();
                    if (idx != -1) {
                        ((DataModel) table.getModel()).removeRow(idx);
                    }
                }
            });

            // Button to move row up
            upBtn = new JButton();
            Mnemonics.setLocalizedText(upBtn, NbBundle.getMessage(CreateTableDialog.class, "CreateTableUpButtonTitle")); // NOI18N
            upBtn.setToolTipText(NbBundle.getMessage(CreateTableDialog.class, "ACS_CreateTableUpButtonTitleA11yDesc")); // NOI18N
            btnpane.add(upBtn);
            upBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    int idx = table.getSelectedRow();
                    if (idx > 0) {
                        DataModel dm = (DataModel) table.getModel();
                        ColumnItem ci = dm.getRow(idx);
                        dm.removeRow(idx);
                        dm.insertRow(idx - 1, ci);
                        table.getSelectionModel().setSelectionInterval(
                                idx - 1, idx - 1);
                    }
                }
            });

            // Button to move row down
            downBtn = new JButton();
            Mnemonics.setLocalizedText(downBtn, NbBundle.getMessage(CreateTableDialog.class, "CreateTableDownButtonTitle")); // NOI18N
            downBtn.setToolTipText(NbBundle.getMessage(CreateTableDialog.class, "ACS_CreateTableDownButtonTitleA11yDesc")); // NOI18N
            btnpane.add(downBtn);
            downBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    int idx = table.getSelectedRow();
                    if (idx < table.getRowCount() - 1) {
                        DataModel dm = (DataModel) table.getModel();
                        ColumnItem ci = dm.getRow(idx);
                        dm.removeRow(idx);
                        dm.insertRow(idx + 1, ci);
                        table.getSelectionModel().setSelectionInterval(
                                idx + 1, idx + 1);
                    }
                }
            });

            ActionListener listener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    final ActionEvent evt = event;
                    if (evt.getSource() == DialogDescriptor.OK_OPTION) {
                        try {
                            final String tablename = getTableName();
                            final DataModel dataModel = (DataModel)table.getModel();
                            final List<ColumnItem> data = dataModel.getData();

                            boolean wasException = DbUtilities.doWithProgress(null, new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    CreateTableDDL ddl = new CreateTableDDL(
                                            spec, schema, tablename);

                                    return ddl.execute(data, dataModel.getTablePrimaryKeys());
                                }
                            });

                            // was execution of commands with or without exception?
                            if(!wasException) {
                                // dialog is closed after successfully create table
                                dialog.setVisible(false);
                                dialog.dispose();
                            }
                            //dialog is not closed after unsuccessfully create table

                        } catch (InvocationTargetException e) {
                            Throwable cause = e.getCause();
                            if (cause instanceof DDLException) {
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                            } else {
                                LOGGER.log(Level.INFO, cause.getLocalizedMessage(), cause);
                                DbUtilities.reportError(NbBundle.getMessage (CreateTableDialog.class, "ERR_UnableToCreateTable"), e.getMessage());
                            }
                        }
                    }
                }
            };

            pane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableDialogA11yDesc")); // NOI18N

            descriptor = new DialogDescriptor(pane, NbBundle.getMessage (CreateTableDialog.class, "CreateTableDialogTitle"), true, listener); // NOI18N
            descriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.db.explorer.actions.CreateTableAction")); // NOI18N
            statusLine = descriptor.createNotificationLineSupport();
            // inbuilt close of the dialog is only after CANCEL button click
            // after OK button is dialog closed by hand
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
            validate();
        } catch (MissingResourceException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     *  Shows Create Table dialog and creates a new table in specified schema.
     * @param spec DB specification
     * @param schema DB schema to create table in
     * @return true if new table successfully created, false if cancelled
     */
    public static boolean showDialogAndCreate(final BaseNode node, final String schema) {                
        final CreateTableDialog dlg = new CreateTableDialog(node, schema);
        dlg.dialog.setVisible(true);
        if (dlg.descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            return true;
        }
        return false;
    }
    
    private String getTableUntitledName() {
        final String nameBase = NbBundle.getMessage (CreateTableDialog.class, "CreateTableUntitledName"); // NOI18N
        String name = nameBase;
        int counter = 1;        
        boolean existsSameTableName;
        
        do {
            existsSameTableName = false;
            for(Node node : tablesNode.getChildNodes()) {
                if (node instanceof TableNode && node.getName().equalsIgnoreCase(name)) {
                    counter++;
                    name = nameBase + counter;
                    existsSameTableName = true;
                }
            }
        } while(existsSameTableName);       
        
        return name;
    }

    private String getTableName() {
        return dbnamefield.getText();
    }

    /** Validate and update state of UI. */
    private void validate() {
        assert statusLine != null : "Notification status line not available";  //NOI18N

        int selectedIndex = table.getSelectedRow();
        boolean oneRowSelected = table.getSelectedRowCount() == 1;
        editBtn.setEnabled(oneRowSelected);
        delbtn.setEnabled(oneRowSelected);
        upBtn.setEnabled(oneRowSelected && selectedIndex > 0);
        downBtn.setEnabled(oneRowSelected
                && selectedIndex < table.getRowCount() - 1);

        String tname = getTableName();
        if (tname == null || tname.length() < 1) {
            statusLine.setInformationMessage(NbBundle.getMessage(CreateTableDialog.class, "CreateTableMissingTableName"));
            updateOK(false);
            return;
        }
        if (table.getModel().getRowCount() == 0) {
            statusLine.setInformationMessage(NbBundle.getMessage(CreateTableDialog.class, "CreateTableNoColumns"));
            updateOK(false);
            return;
        }
        statusLine.clearMessages();
        updateOK(true);
    }

    /** Updates OK button. */
    private void updateOK(boolean valid) {
        if (descriptor != null) {
            descriptor.setValid(valid);
        }
    }

    class DataTable extends JTable {
        static final long serialVersionUID =1222037401669064863L;
        public DataTable(TableModel model) {
            super(model);
            setSurrendersFocusOnKeystroke(true);
            TableColumnModel cmodel = getColumnModel();
            int i;
            int ccount = model.getColumnCount();
            int columnWidth;
            int preferredWidth;
            String columnName;
            int width = 0;
            for (i = 0; i < ccount; i++) {
                TableColumn col = cmodel.getColumn(i);
                Map cmap = ColumnItem.getColumnProperty(i);
                col.setIdentifier(cmap.get("name")); //NOI18N
                columnName = NbBundle.getMessage (CreateTableDialog.class, "CreateTable_" + i); //NOI18N
                columnWidth = (new Double(getFontMetrics(getFont()).getStringBounds(columnName, getGraphics()).getWidth())).intValue() + 20;
                if (cmap.containsKey("width")) { // NOI18N
                    if (((Integer)cmap.get("width")).intValue() < columnWidth)
                        col.setPreferredWidth(columnWidth);
                    else
                        col.setPreferredWidth(((Integer)cmap.get("width")).intValue()); // NOI18N
                    preferredWidth = col.getPreferredWidth();
                }
                if (cmap.containsKey("minwidth")) // NOI18N
                    if (((Integer)cmap.get("minwidth")).intValue() < columnWidth)
                        col.setMinWidth(columnWidth);
                    else
                        col.setMinWidth(((Integer)cmap.get("minwidth")).intValue()); // NOI18N
                //				if (cmap.containsKey("alignment")) {}
                //				if (cmap.containsKey("tip")) ((JComponent)col.getCellRenderer()).setToolTipText((String)cmap.get("tip"));
                if (i < 7) { // the first 7 columns should be visible
                    width += col.getPreferredWidth();
                }
            }
            width = Math.min(Math.max(width, 380), Toolkit.getDefaultToolkit().getScreenSize().width - 100);
            setPreferredScrollableViewportSize(new Dimension(width, 150));
        }
    }
}
