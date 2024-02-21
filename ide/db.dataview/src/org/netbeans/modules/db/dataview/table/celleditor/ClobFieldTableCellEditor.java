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
package org.netbeans.modules.db.dataview.table.celleditor;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.db.dataview.util.FileBackedClob;
import org.netbeans.modules.db.dataview.util.LobHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

public class ClobFieldTableCellEditor extends AbstractCellEditor
        implements TableCellEditor,
        ActionListener, AlwaysEnable {
    
    private class CharsetSelector extends JPanel {
        private JComboBox charsetSelect;
        
        CharsetSelector() {
            List<Charset> charset = new ArrayList<Charset>(Charset.availableCharsets().values());
            charset.sort(new Comparator<Charset>() {
                @Override
                public int compare(Charset o1, Charset o2) {
                    return o1.displayName().compareTo(o2.displayName());
                }
            });
            charsetSelect = new JComboBox();
            charsetSelect.setModel(new DefaultComboBoxModel(
                    charset.toArray(new Charset[0])));
            charsetSelect.setSelectedItem(Charset.defaultCharset());
            this.add(charsetSelect);
        }
        
        public Charset getSelectedCharset() {
            return (Charset) charsetSelect.getSelectedItem();
        }
        
        public void setSelectedCharset(Charset selectedCharset) {
            charsetSelect.setSelectedItem(selectedCharset);
        }
    }
    private static final Logger LOG = Logger.getLogger(
            ClobFieldTableCellEditor.class.getName());
    private static final String EDIT = "edit";
    
    private static File lastFile;
    
    private Clob currentValue;
    private JButton button;
    private JPopupMenu popup;
    private JTable table;
    private int currentRow;
    private int currentColumn;
    private int currentModelRow;
    private int currentModelColumn;
    private JMenuItem saveContentMenuItem;
    private JMenuItem editContentMenuItem;
    private JMenuItem loadContentMenuItem;
    private JMenuItem nullContentMenuItem;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public ClobFieldTableCellEditor() {
        button = new JButton();
        button.setActionCommand(EDIT);
        button.addActionListener(this);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setRolloverEnabled(false);
        button.setAlignmentX(0);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font(button.getFont().getFamily(), Font.ITALIC, 9));
        
        popup = new JPopupMenu();
        saveContentMenuItem = new JMenuItem(NbBundle.getMessage(ClobFieldTableCellEditor.class, "saveLob.title"));
        saveContentMenuItem.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                saveLobToFile(currentValue);
                fireEditingCanceled();
            }
        });
        popup.add(saveContentMenuItem);
        editContentMenuItem = new JMenuItem(NbBundle.getMessage(ClobFieldTableCellEditor.class, "editClob.title"));
        editContentMenuItem.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
                editCell();
            }
        });
        popup.add(editContentMenuItem);
        loadContentMenuItem = new JMenuItem(NbBundle.getMessage(ClobFieldTableCellEditor.class, "loadLob.title"));
        loadContentMenuItem.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                Object newValue = loadLobFromFile();
                if (newValue != null) {
                    currentValue = (Clob) newValue;
                }
                fireEditingStopped();
            }
        });
        popup.add(loadContentMenuItem);
        nullContentMenuItem = new JMenuItem(NbBundle.getMessage(ClobFieldTableCellEditor.class, "nullLob.title"));
        nullContentMenuItem.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                currentValue = null;
                fireEditingStopped();
            }
        });
        popup.add(nullContentMenuItem);
        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            popup.show(button, 0, button.getHeight());
        }
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentValue = (java.sql.Clob) value;
        this.currentColumn = column;
        this.currentRow = row;
        this.table = table;
        this.currentModelColumn = table.convertColumnIndexToModel(column);
        this.currentModelRow = table.convertRowIndexToModel(row);
        boolean editable = table.getModel().isCellEditable(currentModelRow, currentModelColumn);
        if (currentValue != null) {
            saveContentMenuItem.setEnabled(true);
            button.setText(LobHelper.clobToDescription(currentValue));
        } else {
            saveContentMenuItem.setEnabled(false);
            button.setText("<NULL>");
        }
        loadContentMenuItem.setEnabled(editable);
        nullContentMenuItem.setEnabled(editable);
        if (editable) {
            editContentMenuItem.setEnabled(true);
            editContentMenuItem.setText(NbBundle.getMessage(ClobFieldTableCellEditor.class, "editClob.title"));
        } else {
            editContentMenuItem.setEnabled(currentValue != null);
            editContentMenuItem.setText(NbBundle.getMessage(ClobFieldTableCellEditor.class, "editClobReadOnly.title"));
        }
        return button;
    }
    
    @Override
    public Object getCellEditorValue() {
        return currentValue;
    }
    
    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= 2;
        }
        return super.isCellEditable(anEvent);
    }
    
    private void saveLobToFile(Clob b) {
        if (b == null) {
            return;
        }
        CharsetSelector charset = new CharsetSelector();
        JFileChooser c = new JFileChooser();
        c.setCurrentDirectory(lastFile);
        c.setAccessory(charset);
        int fileDialogState = c.showSaveDialog(table);
        if (fileDialogState == JFileChooser.APPROVE_OPTION) {
            File f = c.getSelectedFile();
            lastFile = f;
            Reader r;
            Writer w;
            try {
                r = b.getCharacterStream();
                w = new OutputStreamWriter(new FileOutputStream(f), charset.getSelectedCharset());
                if(! doTransfer(r, w, (int) b.length(), "Save to file: " + f.toString(), false)) {
                    f.delete();
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, "IOException while saving CLOB to file", ex);
                displayError(f, ex, false);
            } catch (SQLException ex) {
                LOG.log(Level.INFO, "SQLException while saving CLOB to file", ex);
                displayError(f, ex, false);
            }
        }
    }
    
    private Clob loadLobFromFile() {
        CharsetSelector charset = new CharsetSelector();
        JFileChooser c = new JFileChooser();
        c.setCurrentDirectory(lastFile);
        c.setAccessory(charset);
        Clob result = null;
        int fileDialogState = c.showOpenDialog(table);
        if (fileDialogState == JFileChooser.APPROVE_OPTION) {
            File f = c.getSelectedFile();
            lastFile = f;
            Reader r;
            try {
                result = new FileBackedClob();
                r = new InputStreamReader(new FileInputStream(f), charset.getSelectedCharset());
                if(! doTransfer(r, result.setCharacterStream(1), (int) f.length() / 2, "Load from file: " + f.toString(), true)) {
                    result = null;
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, "IOException while loading CLOB from file", ex);
                displayError(f, ex, true);
                result = null;
            } catch (SQLException ex) {
                LOG.log(Level.INFO, "SQLException while loading CLOB from file", ex);
                displayError(f, ex, true);
                result = null;
            }
        }
        return result;
    }

    /**
     * Note: The character streams will be closed after this method was invoked
     * 
     * @return true if transfer is complete and not interrupted 
     */
    private boolean doTransfer(Reader in, Writer out, Integer size, String title, boolean sizeEstimated) throws IOException {
        // Only pass size if it is _not_ estimated
        MonitorableCharacterStreamTransfer ft = new MonitorableCharacterStreamTransfer(in, out, sizeEstimated ? null : size);
        Throwable t;
        // Only show dialog, if the filesize is large enougth and has a use for the user
        if (size == null || size > (1024 * 1024)) {
            t = ProgressUtils.showProgressDialogAndRun(ft, title, false);
        } else {
            t = ft.run(null);
        }
        in.close();
        out.close();
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else if (t instanceof IOException) {
            throw (IOException) t;
        } else if (t != null) {
            throw new RuntimeException(t);
        }
        return !ft.isCancel();
    }
    
    private void displayError(File f, Exception ex, boolean read) {
        DialogDisplayer dd = DialogDisplayer.getDefault();

        String errorObjectMsg;
        String messageMsg;
        String titleMsg;

        if (ex instanceof SQLException) {
            errorObjectMsg = NbBundle.getMessage(ClobFieldTableCellEditor.class,
                    "lobErrorObject.database");
        } else {
            errorObjectMsg = NbBundle.getMessage(ClobFieldTableCellEditor.class,
                    "lobErrorObject.file");
        }

        if (!read) {
            titleMsg = NbBundle.getMessage(ClobFieldTableCellEditor.class,
                    "clobSaveToFileError.title");
            messageMsg = NbBundle.getMessage(ClobFieldTableCellEditor.class,
                    "clobSaveToFileError.message",
                    errorObjectMsg,
                    f.getAbsolutePath(),
                    ex.getLocalizedMessage());
        } else {
            titleMsg = NbBundle.getMessage(ClobFieldTableCellEditor.class,
                    "clobReadFromFileError.title");
            messageMsg = NbBundle.getMessage(ClobFieldTableCellEditor.class,
                    "clobReadFromFileError.message",
                    errorObjectMsg,
                    f.getAbsolutePath(),
                    ex.getLocalizedMessage());
        }

        NotifyDescriptor nd = new NotifyDescriptor(
                messageMsg,
                titleMsg,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object[]{NotifyDescriptor.CANCEL_OPTION},
                NotifyDescriptor.CANCEL_OPTION);

        dd.notifyLater(nd);
    }

    protected void editCell() {
        String stringVal = "";
        if (currentValue != null) {
            try {
                stringVal = currentValue.getSubString(1, (int) currentValue.length());
            } catch (SQLException ex) {
            }
            
        }
        
        JTextArea textArea = new JTextArea(20, 80);
        // Work around: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7100524
        textArea.setDropTarget(null);
        textArea.setText(stringVal);
        textArea.setCaretPosition(0);
        textArea.setEditable(table.getModel().isCellEditable(currentModelRow, currentModelColumn));
        
        JScrollPane pane = new JScrollPane(textArea);
        pane.addHierarchyListener(
                new StringTableCellEditor.MakeResizableListener(pane));
        Component parent = WindowManager.getDefault().getMainWindow();
        
        if (table.isCellEditable(currentRow, currentColumn)) {
            int result = JOptionPane.showOptionDialog(parent, pane, table.getColumnName(currentColumn), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    table.setValueAt(new FileBackedClob(textArea.getText()), currentRow, currentColumn);
                } catch (SQLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            JOptionPane.showMessageDialog(parent, pane, table.getColumnName(currentColumn), JOptionPane.PLAIN_MESSAGE, null);
        }
    }
}
