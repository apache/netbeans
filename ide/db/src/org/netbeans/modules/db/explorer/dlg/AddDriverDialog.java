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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.sql.Driver;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.db.explorer.node.DriverNode;
import org.netbeans.modules.db.util.DriverListUtil;
import org.netbeans.modules.db.util.JdbcUrl;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class AddDriverDialog extends javax.swing.JPanel {
    private static HelpCtx ADD_DRIVER_DIALOG_HELPCTX = new HelpCtx(AddDriverDialog.class);

    public static HelpCtx getHelpCtx() {
        return ADD_DRIVER_DIALOG_HELPCTX;
    }
    
    private DefaultListModel<String> dlm;
    private List<URL> drvs = new LinkedList<URL>();
    private boolean customizer = false;
    private ProgressHandle progressHandle;
    private JComponent progressComponent;
    private DialogDescriptor descriptor;
    private final ChoosingDriverUI wp;
    
    private static final Logger LOGGER = Logger.getLogger(AddDriverDialog.class.getName());
    private JDBCDriver drv;
    private final AddConnectionWizard wd;
    private volatile URLClassLoader jarClassLoader = null;

    /** Creates new AddDriverDialog.
     * @param driverNode driver node to be customized or null to create a new one
     */
    public AddDriverDialog(JDBCDriver driver, ChoosingDriverUI panel, AddConnectionWizard wd) {
        this.drv = driver;
        this.wp = panel;
        this.wd = wd;
        initComponents();
        
        // hide abundant fields in New Connection wizard
        if (wd != null) {
            drvClassLabel.setVisible(false);
            drvClassComboBox.setVisible(false);
            findButton.setVisible(false);
            nameLabel.setVisible(false);
            nameTextField.setVisible(false);
            progressMessageLabel.setVisible(false);
            progressContainerPanel.setVisible(false);
        }

        // hack to force the progressContainerPanel honor its preferred height
        // without it, the preferred height is sometimes ignored during resize
        // progressContainerPanel.add(Box.createVerticalStrut(progressContainerPanel.getPreferredSize().height), BorderLayout.EAST);
        initAccessibility();
        dlm = (DefaultListModel<String>) drvList.getModel();

        if (driver != null) {
            setDriver(driver);
        }

        DocumentListener documentListener = new DocumentListener() {

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
        nameTextField.getDocument().addDocumentListener(documentListener);
        dlm.addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(ListDataEvent evt) {
                updateState();
            }

            @Override
            public void intervalRemoved(ListDataEvent evt) {
                updateState();
            }

            @Override
            public void contentsChanged(ListDataEvent evt) {
                updateState();
            }
        });
        Component editorComponent = drvClassComboBox.getEditor().getEditorComponent();
        if (editorComponent instanceof JTextComponent) {
            ((JTextComponent) editorComponent).getDocument().addDocumentListener(documentListener);
        }
    }
    
    /** Fills this dialog by parameters of given driver. */
    public void setDriver(JDBCDriver drv) {
        this.drv = drv;
        customizer = true;
        
        String fileName = null;
        dlm.clear();
        drvs.clear();
        jarClassLoader = null;
        URL[] urls = drv == null ? new URL[0] : drv.getURLs();
        for (int i = 0; i < urls.length; i++) {
            URL url = urls[i];
            if ("nbinst".equals(url.getProtocol())) { // NOI18N
                // try to get a file: URL for the nbinst: URL
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    URL localURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                    if (localURL != null) {
                        url = localURL;
                    }
                }
            }
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                File diskFile = FileUtil.toFile(fo);
                if (diskFile != null) {
                    fileName = diskFile.getAbsolutePath();
                }
            } else {
                if (url.getProtocol().equals("file")) {  //NOI18N
                    try {
                        fileName = new File(new URI(url.toExternalForm())).getAbsolutePath();
                    } catch (URISyntaxException e) {
                        Exceptions.printStackTrace(e);
                        fileName = null;
                    }
                }
            }
            if (fileName != null) {
                dlm.addElement(fileName);
                // use urls[i], not url, because we want to add the original URL
                drvs.add(urls[i]);
            }
        }
        if (urls.length == 0) {
            dlm.addElement(NbBundle.getMessage(AddDriverDialog.class, "AddDriverMissingDriverFiles")); // NOI18N
        }
        drvClassComboBox.addItem(drv == null ? "" : drv.getClassName());
        drvClassComboBox.setSelectedItem(drv == null ? "" : drv.getClassName());
        nameTextField.setText(drv == null ? "" : drv.getDisplayName());
    }

    public JDBCDriver getDriver() {
        return drv;
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDialogA11yDesc")); //NOI18N
        drvListLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDriverFileA11yDesc")); //NOI18N
        drvList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDriverFileListA11yName")); //NOI18N
        drvClassLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDriverDriverClassA11yDesc")); //NOI18N
        drvClassComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDriverDriverClassComboBoxA11yName")); //NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDriverNameA11yDesc")); //NOI18N
        nameTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverDriverNameTextFieldA11yName")); //NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverAddButtonA11yDesc")); //NOI18N
        findButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverRemoveButtonA11yDesc")); //NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverFindButtonA11yDesc")); //NOI18N
        progressContainerPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverProgressBarA11yName")); //NOI18N
        progressContainerPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddDriverDialog.class, "ACS_AddDriverProgressBarA11yDesc")); //NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        drvListLabel = new javax.swing.JLabel();
        drvListScrollPane = new javax.swing.JScrollPane();
        drvList = new javax.swing.JList();
        browseButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        drvClassLabel = new javax.swing.JLabel();
        drvClassComboBox = new javax.swing.JComboBox();
        findButton = new javax.swing.JButton();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        progressMessageLabel = new javax.swing.JLabel();
        progressContainerPanel = new javax.swing.JPanel();

        FormListener formListener = new FormListener();

        drvListLabel.setLabelFor(drvList);
        org.openide.awt.Mnemonics.setLocalizedText(drvListLabel, org.openide.util.NbBundle.getMessage(AddDriverDialog.class, "AddDriverDriverFile")); // NOI18N

        drvList.setModel(new DefaultListModel());
        drvList.addListSelectionListener(formListener);
        drvListScrollPane.setViewportView(drvList);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(AddDriverDialog.class, "AddDriverDriverAdd")); // NOI18N
        browseButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(AddDriverDialog.class, "AddDriverDriverRemove")); // NOI18N
        removeButton.addActionListener(formListener);

        drvClassLabel.setLabelFor(drvClassComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(drvClassLabel, org.openide.util.NbBundle.getMessage(AddDriverDialog.class, "AddDriverDriverClass")); // NOI18N

        drvClassComboBox.setEditable(true);
        drvClassComboBox.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(findButton, org.openide.util.NbBundle.getMessage(AddDriverDialog.class, "AddDriverDriverFind")); // NOI18N
        findButton.addActionListener(formListener);

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(AddDriverDialog.class, "AddDriverDriverName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(progressMessageLabel, " ");

        progressContainerPanel.setMinimumSize(new java.awt.Dimension(20, 20));
        progressContainerPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        progressContainerPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(drvListLabel)
                            .addComponent(drvClassLabel)
                            .addComponent(nameLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                            .addComponent(drvClassComboBox, 0, 258, Short.MAX_VALUE)
                            .addComponent(drvListScrollPane))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(removeButton)
                            .addComponent(browseButton)
                            .addComponent(findButton)))
                    .addComponent(progressMessageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
                    .addComponent(progressContainerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {browseButton, findButton, removeButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(drvListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                    .addComponent(drvListLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(browseButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(drvClassComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(drvClassLabel)
                    .addComponent(findButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressContainerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, javax.swing.event.ListSelectionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == browseButton) {
                AddDriverDialog.this.browseButtonActionPerformed(evt);
            }
            else if (evt.getSource() == removeButton) {
                AddDriverDialog.this.removeButtonActionPerformed(evt);
            }
            else if (evt.getSource() == drvClassComboBox) {
                AddDriverDialog.this.drvClassComboBoxActionPerformed(evt);
            }
            else if (evt.getSource() == findButton) {
                AddDriverDialog.this.findButtonActionPerformed(evt);
            }
        }

        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
            if (evt.getSource() == drvList) {
                AddDriverDialog.this.drvListValueChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void drvClassComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drvClassComboBoxActionPerformed
        if (!customizer) {
            nameTextField.setText(DriverListUtil.findFreeName(DriverListUtil.getName((String) drvClassComboBox.getSelectedItem())));
        }
    }//GEN-LAST:event_drvClassComboBoxActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        stopProgress();
        
        ListSelectionModel lsm = drvList.getSelectionModel();
        int count = dlm.getSize();
        int i = 0;
        
        if (count < 1) {
            return;
        }
        do {
            if (lsm.isSelectedIndex(i)) {
                dlm.remove(i);
                drvs.remove(i);
                jarClassLoader = null;
                count--;
                continue;
            }
            i++;
        } while (count != i);
        
        findDriverClass();
        if (wp != null) {
            wp.fireChangeEvent();
        }
        updateState();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void findButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findButtonActionPerformed
        findDriverClassByInspection();
    }//GEN-LAST:event_findButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        stopProgress();

        FileChooserBuilder fileChooserBuilder = new FileChooserBuilder(AddDriverDialog.class);
        fileChooserBuilder.setTitle(NbBundle.getMessage(AddDriverDialog.class, "AddDriver_Chooser_Title")); //NOI18N
        //.jar and .zip file filter
        fileChooserBuilder.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return (f.isDirectory() || f.getName().endsWith(".jar") || f.getName().endsWith(".zip")); //NOI18N
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(AddDriverDialog.class, "AddDriver_Chooser_Filter"); //NOI18N
            }
        });

        File[] selectedFiles = fileChooserBuilder.showMultiOpenDialog();
        if (selectedFiles != null) {
            for (final File file : selectedFiles) {
                if (file.isFile()) {
                    if (dlm.contains(file.toString())) {
                        // file already added
                        NotifyDescriptor msgDesc = new NotifyDescriptor.Message(NbBundle.getMessage(AddDriverDialog.class, "AddDriverDuplicateFile", file.toString()));
                        DialogDisplayer.getDefault().notify(msgDesc);
                        continue;
                    }
                    if (drvs.isEmpty()) {
                        dlm.clear();
                    }
                    dlm.addElement(file.toString());
                    try {
                        drvs.add(file.toURI().toURL());
                        jarClassLoader = null;
                    } catch (MalformedURLException exc) {
                        LOGGER.log(Level.WARNING,
                                "Unable to add driver jar file " +
                                file.getAbsolutePath() +
                                ": can not convert to URL", exc);
                    }
                    if (wd != null) {
                        boolean privileged = wd.getAllPrivilegedNames().isEmpty();
                        for (String name : wd.getAllPrivilegedNames()) {
                            if (file.getName().startsWith(name)) {
                                privileged = true;
                                break;
                            }
                        }
                        if (privileged) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    notifyUser(null, false);
                                }
                            });
                        } else {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    notifyUser(NbBundle.getMessage(AddDriverDialog.class, "AddDriverDialog_NotPrivilegedDriver", // NOI18N
                                            file.getName(), wd.getPrivilegedName()), true);
                                }
                            });
                        }
                    }
                }
            }
            findDriverClass();
            if (wp != null) {
                wp.fireChangeEvent();
            }
        }
        updateState();
    }//GEN-LAST:event_browseButtonActionPerformed

    private void drvListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_drvListValueChanged
        updateState();
    }//GEN-LAST:event_drvListValueChanged
            
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox drvClassComboBox;
    private javax.swing.JLabel drvClassLabel;
    private javax.swing.JList drvList;
    private javax.swing.JLabel drvListLabel;
    private javax.swing.JScrollPane drvListScrollPane;
    private javax.swing.JButton findButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JPanel progressContainerPanel;
    private javax.swing.JLabel progressMessageLabel;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
    
    private boolean isDriverClass(URLClassLoader jarloader, String className) {
        Class<?> clazz;

        try {
            clazz = jarloader.loadClass(className);
        } catch ( Throwable t ) {
            LOGGER.log(Level.FINE, null, t);
            
            LOGGER.log(Level.INFO, 
                 "Got an exception trying to load class " +
                 className + " during search for JDBC drivers in " +
                 " driver jar(s): " + t.getClass().getName() + ": "
                 + t.getMessage() + ".  Skipping this class..."); // NOI18N

            return false;         
        }

        if ( Driver.class.isAssignableFrom(clazz) ) {
            return true;
        }
        
        return false;
    }
        
    public URL[] getDriverURLs() {
        return drvs.toArray(new URL[0]);
    }
    
    private void findDriverClass() {
        JarFile jf;
        String[] drivers = DriverListUtil.getDrivers ().toArray (new String[DriverListUtil.getDrivers ().size ()]);
        
        drvClassComboBox.removeAllItems();
        for (int i = 0; i < drvs.size(); i++) {
            try {
                URL url = drvs.get (i);

                if ("nbinst".equals(url.getProtocol())) { // NOI18N
                    // try to get a file: URL for the nbinst: URL
                    FileObject fo = URLMapper.findFileObject(url);
                    if (fo != null) {
                        URL localURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                        if (localURL != null) {
                            url = localURL;
                        }
                    }
                }

                File file = new File(new URI(url.toExternalForm()));
                jf = new JarFile(file);
                for (int j = 0; j < drivers.length; j++) {
                    if (jf.getEntry(drivers[j].replace('.', '/') + ".class") != null) {  //NOI18N
                        addDriverClass(drivers[j]);
                    }
                }
                jf.close();
            } catch (IOException exc) {
                //PENDING
            } catch (URISyntaxException e) {
                //PENDING
            }
        }
        getJarClassLoader(); // init class loader while the JAR file is cached
    }
    
    private void findDriverClassByInspection() {
        drvClassComboBox.removeAllItems();
        findButton.setEnabled(false);
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                startProgress();
                URLClassLoader jarloader = getJarClassLoader();

                for (int i = 0; i < dlm.size(); i++) {
                    try {
                        String file  = dlm.get(i);
                        JarFile jf = new JarFile(file);
                        try {
                            Enumeration<JarEntry> entries = jf.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry entry = (JarEntry)entries.nextElement();
                                String className = entry.getName();
                                if (className.endsWith(".class")) { // NOI18N
                                    className = className.replace('/', '.');
                                    className = className.substring(0, className.length() - 6);
                                    if ( isDriverClass(jarloader, className) ) {
                                        if (progressHandle != null) {
                                            addDriverClass(className);
                                        } else {
                                            // already stopped
                                            updateState();
                                            return;
                                        }
                                    }
                                }
                            }
                        } finally {
                            jf.close();
                        }
                    } catch (IOException exc) {
                        //PENDING
                    }
                }
                stopProgress();
                updateState();
            }
        }, 0);
    }
    
    private URLClassLoader getJarClassLoader() {
        /* This classloader is used to load classes from the jar files for the driver.  We can then
        introspection to see if a class in one of these jar files implements java.sql.Driver. (We
        clear the jarClassLoader whenever drvs is modified, to avoid the AddDriverNotJavaSqlDriver
        message popping up if a different driver is picked from the dropdown after an unrelated JAR
        file is added.) */
        jarClassLoader =
                new URLClassLoader(drvs.toArray(new URL[0]),
                this.getClass().getClassLoader());
        return jarClassLoader;
    }
    private void addDriverClass(String drv) {
        if (((DefaultComboBoxModel) drvClassComboBox.getModel()).getIndexOf(drv) < 0) {
            drvClassComboBox.addItem(drv);
        }
    }
    
    private void startProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressHandle = ProgressHandleFactory.createHandle(null);
                progressComponent = ProgressHandleFactory.createProgressComponent(progressHandle);
                progressContainerPanel.add(progressComponent, BorderLayout.CENTER);
                progressHandle.start();
                progressMessageLabel.setText(NbBundle.getMessage (AddDriverDialog.class, "AddDriverProgressStart"));
            }
        });
    }

    private void stopProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (progressHandle != null) {
                    progressHandle.finish();
                    progressHandle = null;
                    progressMessageLabel.setText(" "); // NOI18N
                    progressContainerPanel.remove(progressComponent);
                    // without this, the removed progress component remains painted on its parent... why?
                    repaint();
                }
            }
        });
    }

    private void setDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        updateState();
    }

    /** Updates state of UI controls. */
    private void updateState() {
        boolean enable = getDriverURLs().length > 0;
        // update Browse button state
        browseButton.setEnabled(drv != null || ! (wp != null));
        // update Remove button state
        removeButton.setEnabled(enable && drvList.getSelectedIndices().length > 0);
        // update Find button state
        findButton.setEnabled(enable && progressHandle == null && drvList.getModel().getSize() > 0);
        // drvList
        drvList.setEnabled(enable);
        // update status line and OK button
        String message = null;
        if (drvs.isEmpty()) {
            if (wd != null && wd.getDownloadFrom() != null) {
                message = NbBundle.getMessage(AddDriverDialog.class, "AddDriverDownloadMissingFile", wd.getDownloadFrom(), wd.getPrivilegedName());
            } else {
                message = NbBundle.getMessage(AddDriverDialog.class, "AddDriverMissingFile");
            }
        } else if (drvClassComboBox.getEditor().getItem().toString().length() == 0) {
            message = NbBundle.getMessage(AddDriverDialog.class, "AddDriverMissingClass");
        } else if (jarClassLoader != null && !isDriverClass(jarClassLoader,
                drvClassComboBox.getEditor().getItem().toString())) {
            message = NbBundle.getMessage(AddDriverDialog.class, "AddDriverNotJavaSqlDriver"); //NOI18N
        } else if (nameTextField.getText().length() == 0) {
            message = NbBundle.getMessage(AddDriverDialog.class, "AddDriverMissingName");
        } else if (!customizer && nameTextField.getText().length() > 0) {
            String newDisplayName = nameTextField.getText();
            for (JDBCDriver driver : JDBCDriverManager.getDefault().getDrivers()) {
                if (driver.getDisplayName().equalsIgnoreCase(newDisplayName)) {
                    message = NbBundle.getMessage(AddDriverDialog.class, "AddDriverDuplicateName");
                    break;
                }
            }
        }
        notifyUser(message, false);
    }

    private void notifyUser(String message, boolean isWarning) {
        if (descriptor != null) {
            if (message != null) {
                if (isWarning) {
                    descriptor.getNotificationLineSupport().setWarningMessage(message);
                } else {
                    descriptor.getNotificationLineSupport().setInformationMessage(message);
                }
                descriptor.setValid(false);
            } else {
                descriptor.getNotificationLineSupport().clearMessages();
                descriptor.setValid(true);
            }
        } else if (wd != null) {
            if (message != null) {
                if (isWarning) {
                    wd.getNotificationLineSupport().setWarningMessage(message);
                } else {
                    wd.getNotificationLineSupport().setInformationMessage(message);
                }
            } else {
                wd.getNotificationLineSupport().clearMessages();
            }
        } else {
            Logger.getLogger(AddDriverDialog.class.getName()).log(Level.INFO, "DialogDescriptor or wizard is not set, cannot display message: " + message);
        }
    }

    /** Shows New JDBC Driver dialog and returns driver instance if user
     * clicks OK. Otherwise it returns null.
     * @param driverNode existing driver node to be customized or null to create new one
     * @return driver instance if user clicks OK, null otherwise
     */
    public static JDBCDriver showDialog(DriverNode driverNode) {
        AddDriverDialog dlgPanel = new AddDriverDialog(driverNode == null ? null : driverNode.getDatabaseDriver().getJDBCDriver(), null, null);

        DialogDescriptor descriptor = new DialogDescriptor(dlgPanel, NbBundle.getMessage(AddDriverDialog.class,
                driverNode == null ? "AddDriverDialogTitle" : "CustomizeDriverDialogTitle")); //NOI18N
        descriptor.setHelpCtx(AddDriverDialog.getHelpCtx());
        descriptor.createNotificationLineSupport();
        dlgPanel.setDescriptor(descriptor);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);

        JDBCDriver driver = null;
        if (DialogDescriptor.OK_OPTION == descriptor.getValue()) {
            JDBCDriver current = dlgPanel.getDriver();
            if (driverNode != null) {
                driverNode.destroy();
            }
            String drvClass = dlgPanel.getDriverClass();
            String displayName = dlgPanel.getDisplayName();
            // any change?
            if (current != null && ! Arrays.equals(current.getURLs(), dlgPanel.getDriverURLs())) {
                JDBCDriver modified = JDBCDriver.create(current.getName(), displayName, drvClass, dlgPanel.getDriverURLs());
                try {
                    JDBCDriverManager.getDefault().removeDriver(current);
                    JDBCDriverManager.getDefault().addDriver(modified);
                } catch (DatabaseException ex) {
                    Logger.getLogger(AddDriverDialog.class.getName()).log(Level.WARNING,
                            "Unable to modify driver " + current.getName() + " and add driver jar files " +
                            Arrays.asList(dlgPanel.getDriverURLs()), ex);
                }
                driver = modified;
            } else {
                URL[] urls = dlgPanel.getDriverURLs();
                driver = JDBCDriver.create(displayName, displayName, drvClass, urls);
                try {
                    JDBCDriverManager.getDefault().addDriver(driver);
                } catch (DatabaseException ex) {
                    Logger.getLogger(AddDriverDialog.class.getName()).log(Level.WARNING,
                            "Unable to add driver " + driver.getName() + " and add driver jar files " +
                            Arrays.asList(dlgPanel.getDriverURLs()), ex);
                }
            }
        }
        return driver;
    }

    /** Shows New JDBC Driver dialog and returns driver instance if user
     * clicks OK. Otherwise it returns null.
     * @return driver instance if user clicks OK, null otherwise
     */
    public static JDBCDriver showDialog() {
        return showDialog(null);
    }

    private String getDriverClass() {
        return (String) drvClassComboBox.getSelectedItem();
    }

    private String getDisplayName() {
        return nameTextField.getText();
    }
}
