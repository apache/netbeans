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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.EventObject;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.db.dataview.util.CharsetSelector;
import org.netbeans.modules.db.dataview.util.EncodingHelper;
import org.netbeans.modules.db.dataview.util.FileBackedBlob;
import org.netbeans.modules.db.dataview.util.LobHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

public class BlobFieldTableCellEditor extends AbstractCellEditor
        implements TableCellEditor,
        ActionListener, AlwaysEnable {
    private static final Logger LOG = Logger.getLogger(
            BlobFieldTableCellEditor.class.getName());
    private static final String EDIT = "edit";

    private static File lastFile;
    private static Charset lastSelectedCharset = Charset.defaultCharset();

    private Blob currentValue;
    private JButton button;
    private JPopupMenu popup;
    private JTable table;
    private int currentRow;
    private int currentColumn;
    private int currentModelColumn;
    private int currentModelRow;
    private JMenuItem saveContentMenuItem;
    private JMenuItem miOpenImageMenuItem;
    private JMenuItem miOpenAsTextMenuItem;
    private JMenuItem miLobLoadAction;
    private JMenuItem miLobNullAction;

    @SuppressWarnings("LeakingThisInConstructor")
    public BlobFieldTableCellEditor() {
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

        miOpenImageMenuItem = new JMenuItem("Open as Image");
        miOpenImageMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openAsImage(currentValue);
                fireEditingCanceled();
            }
        });
        popup.add(miOpenImageMenuItem);

        miOpenAsTextMenuItem = new JMenuItem("Open as Text");
        miOpenAsTextMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openAsText();
                fireEditingCanceled();
            }
        });
        popup.add(miOpenAsTextMenuItem);

        popup.addSeparator();
        
        saveContentMenuItem = new JMenuItem(NbBundle.getMessage(BlobFieldTableCellEditor.class, "saveLob.title"));
        saveContentMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveLobToFile(currentValue);
                fireEditingCanceled();
            }
        });
        popup.add(saveContentMenuItem);

        miLobLoadAction = new JMenuItem(NbBundle.getMessage(BlobFieldTableCellEditor.class, "loadLob.title"));
        miLobLoadAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object newValue = loadLobFromFile();
                if (newValue != null) {
                    currentValue = (Blob) newValue;
                }
                fireEditingStopped();
            }
        });
        popup.add(miLobLoadAction);
        miLobNullAction = new JMenuItem(NbBundle.getMessage(BlobFieldTableCellEditor.class, "nullLob.title"));
        miLobNullAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                currentValue = null;
                fireEditingStopped();
            }
        });
        popup.add(miLobNullAction);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            popup.show(button, 0, button.getHeight());
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentValue = (java.sql.Blob) value;
        int modelRow = table.convertRowIndexToModel(row);
        int modelColumn = table.convertColumnIndexToModel(column);
        boolean editable = table.getModel().isCellEditable(modelRow, modelColumn);
        if (currentValue != null) {
            saveContentMenuItem.setEnabled(true);
            miOpenImageMenuItem.setEnabled(true);
            button.setText(LobHelper.blobToString(currentValue));
        } else {
            saveContentMenuItem.setEnabled(false);
            miOpenImageMenuItem.setEnabled(false);
            button.setText("<NULL>");
        }
        miLobLoadAction.setEnabled(editable);
        miLobNullAction.setEnabled(editable);
        this.table = table;
        this.currentColumn = column;
        this.currentRow = row;
        this.currentModelColumn = table.convertColumnIndexToModel(column);
        this.currentModelRow = table.convertRowIndexToModel(row);
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

    private void saveLobToFile(Blob b) {
        if(b == null) {
            return;
        }
        JFileChooser c = new JFileChooser();
        c.setCurrentDirectory(lastFile);
        int fileDialogState = c.showSaveDialog(table);
        if (fileDialogState == JFileChooser.APPROVE_OPTION) {
            File f = c.getSelectedFile();
            lastFile = f;
            InputStream is;
            FileOutputStream fos;
            try {
                is = b.getBinaryStream();
                fos = new FileOutputStream(f);
                if(! doTransfer(is, fos, (int) b.length(), "Saving to file: " + f.toString())) {
                    f.delete();
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, "IOError while saving BLOB to file", ex);
                displayError(f, ex, false);
            } catch (SQLException ex) {
                LOG.log(Level.INFO, "SQLException while saving BLOB to file", ex);
                displayError(f, ex, false);
            }
        }
    }

    private Blob loadLobFromFile() {
        JFileChooser c = new JFileChooser();
        c.setCurrentDirectory(lastFile);
        Blob result = null;
        int fileDialogState = c.showOpenDialog(table);
        if (fileDialogState == JFileChooser.APPROVE_OPTION) {
            File f = c.getSelectedFile();
            lastFile = f;
            FileInputStream fis;
            try {
                fis = new FileInputStream(f);
                result = new FileBackedBlob();
                if(! doTransfer(fis, result.setBinaryStream(1), (int) f.length(), "Loading file: " + f.toString())) {
                    result = null;
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, "IOError while loading BLOB from file", ex);
                displayError(f, ex, true);
                result = null;
            } catch (SQLException ex) {
                LOG.log(Level.INFO, "SQLException while loading BLOB from file", ex);
                displayError(f, ex, true);
                result = null;
            }
        }
        return result;
    }

    /**
     * Note: The streams will be closed after this method was invoked
     * 
     * @return true if transfer is complete and not interrupted 
     */
    private boolean doTransfer(InputStream is, OutputStream os, Integer size, String title) throws IOException {
        MonitorableStreamTransfer ft = new MonitorableStreamTransfer(is, os, size);
        Throwable t;
        // Only show dialog, if the filesize is large enougth and has a use for the user
        if (size == null || size > (1024 * 1024)) {
            t = ProgressUtils.showProgressDialogAndRun(ft, title, false);
        } else {
            t = ft.run(null);
        }
        is.close();
        os.close();
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
            errorObjectMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                    "lobErrorObject.database");
        } else {
            errorObjectMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                    "lobErrorObject.file");
        }

        if (!read) {
            titleMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                    "blobSaveToFileError.title");
            messageMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                    "blobSaveToFileError.message",
                    errorObjectMsg,
                    f.getAbsolutePath(),
                    ex.getLocalizedMessage());
        } else {
            titleMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                    "blobReadFromFileError.title");
            messageMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                    "blobReadFromFileError.message",
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

    private void openAsImage(Blob b) {
        if (b == null) {
            return;
        }
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(
                    b.getBinaryStream());
            Iterator<ImageReader> irs = ImageIO.getImageReaders(iis);
            if (irs.hasNext()) {
                FileSystem fs = FileUtil.createMemoryFileSystem();
                FileObject fob = fs.getRoot().createData(
                        Long.toString(System.currentTimeMillis()),
                        irs.next().getFormatName());
                OutputStream os = fob.getOutputStream();
                os.write(b.getBytes(1, (int) b.length()));
                os.close();
                DataObject data = DataObject.find(fob);
                OpenCookie cookie = data.getLookup().lookup(OpenCookie.class);
                if (cookie != null) {
                    cookie.open();
                    return;
                }
            }
            displayErrorOpenImage("openImageErrorNotImage.message");    //NOI18N
        } catch (SQLException ex) {
            LOG.log(Level.INFO,
                    "SQLException while opening BLOB as file", ex);     //NOI18N
            displayErrorOpenImage("openImageErrorDB.message");          //NOI18N
        } catch (IOException ex) {
            LOG.log(Level.INFO, "IOError while opening BLOB as file", //NOI18N
                    ex);
        }

    }

    private void displayErrorOpenImage(String messageProperty) {
        DialogDisplayer dd = DialogDisplayer.getDefault();

        String messageMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                messageProperty);
        String titleMsg = NbBundle.getMessage(BlobFieldTableCellEditor.class,
                "openImageError.title");                                //NOI18N

        NotifyDescriptor nd = new NotifyDescriptor(
                messageMsg,
                titleMsg,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object[]{NotifyDescriptor.CANCEL_OPTION},
                NotifyDescriptor.CANCEL_OPTION);

        dd.notifyLater(nd);
    }

    protected void openAsText() {
        GridBagConstraints gbc;
        
        Charset detectedCharset = detectEncoding();
        
        final CharsetSelector charsetSelector = new CharsetSelector();
        
        charsetSelector.setSelectedItem(detectedCharset == null ? lastSelectedCharset : detectedCharset);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        final JTextArea textArea = new JTextArea(20, 80);
        // Work around: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7100524
        textArea.setDropTarget(null);
        textArea.setText(getTextFromCurrentCell(charsetSelector.getSelectedItem()));
        textArea.setCaretPosition(0);
        textArea.setEditable(table.getModel().isCellEditable(currentModelRow, currentModelColumn));

        JScrollPane pane = new JScrollPane(textArea);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.BASELINE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 0d;
        gbc.weighty = 0d;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("Charset-Encoding: "), gbc);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.BASELINE;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 1d;
        gbc.weighty = 0d;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(charsetSelector, gbc);
        
        JButton reloadButton = new JButton("Reload");
        reloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText(getTextFromCurrentCell(charsetSelector.getSelectedItem()));
                lastSelectedCharset = charsetSelector.getSelectedItem();
}
        });
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 0d;
        gbc.weighty = 0d;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(reloadButton, gbc);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 1d;
        gbc.weighty = 1d;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(pane, gbc);
        
        pane.addHierarchyListener(
                new StringTableCellEditor.MakeResizableListener(panel));
        Component parent = WindowManager.getDefault().getMainWindow();

        if (table.isCellEditable(currentRow, currentColumn)) {
            int result = JOptionPane.showOptionDialog(parent, panel, table.getColumnName(currentColumn), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    table.setValueAt(new FileBackedBlob(
                            new ByteArrayInputStream(
                            textArea.getText().getBytes(charsetSelector.getSelectedItem()))),
                            currentRow, currentColumn);
                    lastSelectedCharset = charsetSelector.getSelectedItem();
                } catch (SQLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            JOptionPane.showMessageDialog(parent, panel, table.getColumnName(currentColumn), JOptionPane.PLAIN_MESSAGE, null);
        }
    }
    
    private Charset detectEncoding() {
        if(currentValue == null) {
            return null;
        }
        InputStream blobStream = null;
        try {
            blobStream = currentValue.getBinaryStream();
            BufferedInputStream inputStream = new BufferedInputStream(blobStream);
            String charsetName = EncodingHelper.detectEncoding(inputStream);
            Charset result = null;
            if(charsetName != null) {
                result = Charset.forName(charsetName);
            }
            return result;
        } catch (SQLException | IOException ex) {
            LOG.log(Level.FINE, "Failed to read BLOB contents.", ex);
        } finally {
            if (blobStream != null) {
                try {
                    blobStream.close();
                } catch (IOException ex) {
                }
            }
        }
        return null;
    }
    
    private String getTextFromCurrentCell(Charset charset) {
        if(currentValue == null) {
            return "";
        }
        InputStream blobStream = null;
        try {
            blobStream = currentValue.getBinaryStream();
            Reader reader = new BufferedReader(
                    new InputStreamReader(blobStream, charset)
            );
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while((read = reader.read(buffer)) > 0) {
                sb.append(buffer, 0, read);
            }
            return sb.toString();
        } catch (SQLException | IOException ex) {
            LOG.log(Level.FINE, "Failed to read BLOB contents.", ex);
        } finally {
            if(blobStream != null) {
                try {
                    blobStream.close();
                } catch (IOException ex) {
                }
            }
        }
        return "";
    }
}
