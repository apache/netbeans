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

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * First panel of the librarywrapper module wizard.
 *
 * @author Milos Kleint
 */
final class LibraryStartVisualPanel extends NewTemplateVisualPanel {
    
    static final String PROP_LIBRARY_PATH = "LIBRARY_PATH_VALUE"; //NOI18N
    static final String PROP_LICENSE_PATH = "LICENSE_PATH_VALUE"; //NOI18N
    
    private boolean listenersAttached;
    private final DocumentListener libraryDL;
    private final DocumentListener licenseDL;
    
    /** Creates new form BasicConfVisualPanel */
    public LibraryStartVisualPanel(final NewModuleProjectData data) {
        super(data);
        initComponents();
        initAccessibility();
        libraryDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                checkLibraryAndLicense();
            }
        };
        licenseDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                checkLibraryAndLicense();
            }
        };
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LibraryStartVisualPanel"));
        browseLibraryButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_BrowseLibraries"));
        browseLicenceButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_BrowseLicense"));
        txtLibrary.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Library"));
        txtLicense.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_License"));
    }
    
    private void checkLibraryAndLicense() {
        String text = txtLibrary.getText().trim();
        if (text.length() > 0) {
            StringTokenizer tokens = new StringTokenizer(text, File.pathSeparator);
            while (tokens.hasMoreTokens()) {
                String one = tokens.nextToken();
                File fil = new File(one);
                if (!fil.exists()) {
                    setError(getMessage("MSG_Invalid_Library_Path"));
                    return;
                }
                try {
                    new JarFile(fil); // just checking whether the jar is valid
                } catch (IOException exc) {
                    setError(getMessage("MSG_Selected_File_Is_Not_A_JAR"));
                    return;
                }
                String badOnes = populateProjectData(getData(), text, false);
                if (badOnes != null) {
                    setWarning(NbBundle.getMessage(LibraryStartVisualPanel.class, "MSG_ClassInDefaultPackage", badOnes));
                    return;
                }
            }
        } else  {
            setWarning(getMessage("MSG_Library_Path_Not_Defined"), false);
            return;
        }
        text = txtLicense.getText().trim();
        if (text.length() > 0) {
            File fil = new File(text);
            if (!fil.exists()) {
                setError(getMessage("MSG_Invalid_License_Path"));
                return;
            }
        }
        markValid();
    }
    
    void refreshData() {
        // XXX should be cleaned out if it is not needed
//        String license = (String)getSettings().getProperty(PROP_LICENSE_PATH);
//        String jars = (String)getSettings().getProperty(PROP_LIBRARY_PATH);
        
//        String cnb = data.getCodeNameBase();
//        codeNameBaseValue.setText(cnb);
//        if (cnb.startsWith(EXAMPLE_BASE_NAME)) {
//            codeNameBaseValue.select(0, EXAMPLE_BASE_NAME.length() - 1);
//        }
//        String dn = data.getProjectDisplayName();
//        displayNameValue.setText(dn);
//        checkCodeNameBase();
    }
    
    /** Stores collected data into model. */
    void storeData() {
        String jars = txtLibrary.getText().trim();
        getSettings().putProperty(PROP_LIBRARY_PATH, jars);
        getSettings().putProperty(PROP_LICENSE_PATH, txtLicense.getText().trim());
        populateProjectData(getData(), jars, true);
//        // change will be fired -> update data
//        data.setCodeNameBase(getCodeNameBaseValue());
//        data.setProjectDisplayName(displayNameValue.getText());
//        data.setBundle(getBundleValue());
//        if (!libraryModule) {
//            data.setLayer(getLayerValue());
//        }
    }
    
    static String populateProjectData(NewModuleProjectData data, String paths, boolean assignValues) {
        if (data.getProjectName() != null && data.getCodeNameBase() != null && assignValues) {
            return null;
        }
        String wrongOnes = null;
        StringTokenizer tokens = new StringTokenizer(paths, File.pathSeparator);
        boolean cutShortestPath = false;
        boolean fileAlreadyMarked = false;
        if (tokens.hasMoreTokens()) {
            fileAlreadyMarked = false;
            File fil = new File(tokens.nextToken());
            if (!fil.exists()) {
                // #63438 hmm. happens when cancelling the panel? why?
                return wrongOnes;
            }
            String name = fil.getName();
            int inddd = name.lastIndexOf('.');
            if (inddd > -1) {
                name = name.substring(0, inddd);
            }
            name = name.replaceAll("[0-9._-]+$", ""); // NOI18N
            if (assignValues) {
                data.setProjectName(name);
            }
            JarFile jf = null;
            String shortestPath = null;
            try {
                jf = new JarFile(fil);
                if(assignValues) {
                    Manifest manifest = jf.getManifest();
                    if (manifest != null) {
                        Attributes attributes = manifest.getMainAttributes();
                        if(attributes.getValue("Specification-Title") != null) {
                            data.setProjectName(attributes.getValue("Specification-Title").replaceAll("[0-9._-]+$", "").replace(" ", "-"));
                        } else {
                            if (manifest.getEntries().size() == 1) {
                                attributes = manifest.getEntries().values().iterator().next();
                                if(attributes.getValue("Specification-Title") != null) {
                                    data.setProjectName(attributes.getValue("Specification-Title").replaceAll("[0-9._-]+$", "").replace(" ", "-"));
                                }
                            }
                        }
                    }
                }
                Enumeration en = jf.entries();
                while (en.hasMoreElements()) {
                    JarEntry entry = (JarEntry)en.nextElement();
                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) { // NOI18N
                        String nm = entry.getName();
                        if (!ApisupportAntUtils.isValidJavaFQN(nm.substring(0, nm.length() - 6).replace('/', '.'))) {
                            continue; // #72669
                        }
                        int index = nm.lastIndexOf('/');
                        if (index > -1) {
                            String path = nm.substring(0, index);
                            if (shortestPath != null && path.length() == shortestPath.length() && !path.equals(shortestPath)) {
                                cutShortestPath = true;
                            }
                            if (shortestPath == null || path.length() < shortestPath.length()) {
                                shortestPath = path;
                                cutShortestPath = false;
                            }
                        } else {
                            // a bad, bad jar having class files in default package.
                            if (!fileAlreadyMarked) {
                                wrongOnes = wrongOnes == null ? fil.getName() : wrongOnes + "," + fil.getName(); // NOI18N
                                fileAlreadyMarked = true;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            } finally {
                if (jf != null) {
                    try {
                        jf.close();
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
            if (shortestPath != null && assignValues) {
                shortestPath = shortestPath.replace('/', '.');
                if (cutShortestPath && shortestPath.indexOf('.') != shortestPath.lastIndexOf('.')) {
                    // if there's more than one dot (meanign we don't want to cut too much to present just
                    // org or com. org.apache is probably already good enough
                    int ind = shortestPath.lastIndexOf('.');
                    shortestPath = shortestPath.substring(0, ind);
                }
                data.setCodeNameBase(shortestPath);
            }
        }
        return wrongOnes;
    }
    
    public @Override void addNotify() {
        super.addNotify();
        attachDocumentListeners();
        checkLibraryAndLicense();
    }
    
    public @Override void removeNotify() {
        // prevent checking when the panel is not "active"
        removeDocumentListeners();
        super.removeNotify();
    }
    
    private void attachDocumentListeners() {
        if (!listenersAttached) {
            txtLibrary.getDocument().addDocumentListener(libraryDL);
            txtLicense.getDocument().addDocumentListener(licenseDL);
            listenersAttached = true;
        }
    }
    
    private void removeDocumentListeners() {
        if (listenersAttached) {
            txtLibrary.getDocument().removeDocumentListener(libraryDL);
            txtLicense.getDocument().removeDocumentListener(licenseDL);
            listenersAttached = false;
        }
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(LibraryStartVisualPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        confPanel = new javax.swing.JPanel();
        lblLibrary = new javax.swing.JLabel();
        txtLibrary = new javax.swing.JTextField();
        lblLicense = new javax.swing.JLabel();
        txtLicense = new javax.swing.JTextField();
        browseLibraryButton = new javax.swing.JButton();
        browseLicenceButton = new javax.swing.JButton();
        filler = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        confPanel.setLayout(new java.awt.GridBagLayout());

        lblLibrary.setLabelFor(txtLibrary);
        org.openide.awt.Mnemonics.setLocalizedText(lblLibrary, org.openide.util.NbBundle.getMessage(LibraryStartVisualPanel.class, "LBL_Library_path"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 12);
        confPanel.add(lblLibrary, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 0);
        confPanel.add(txtLibrary, gridBagConstraints);

        lblLicense.setLabelFor(txtLicense);
        org.openide.awt.Mnemonics.setLocalizedText(lblLicense, org.openide.util.NbBundle.getMessage(LibraryStartVisualPanel.class, "LBL_License_Path"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        confPanel.add(lblLicense, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        confPanel.add(txtLicense, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseLibraryButton, org.openide.util.NbBundle.getMessage(LibraryStartVisualPanel.class, "CTL_BrowseButton_o"));
        browseLibraryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLibraryButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        confPanel.add(browseLibraryButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseLicenceButton, org.openide.util.NbBundle.getMessage(LibraryStartVisualPanel.class, "CTL_BrowseButton_w"));
        browseLicenceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLicenceButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        confPanel.add(browseLicenceButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        confPanel.add(filler, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(confPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void browseLicenceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLicenceButtonActionPerformed
        JFileChooser chooser = new JFileChooser(ModuleUISettings.getDefault().getLastChosenLibraryLocation());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        if (txtLicense.getText().trim().length() > 0) {
            chooser.setSelectedFile(new File(txtLicense.getText().trim()));
        }
        int ret = chooser.showDialog(this, getMessage("LBL_Select"));
        if (ret == JFileChooser.APPROVE_OPTION) {
            txtLicense.setText(chooser.getSelectedFile().getAbsolutePath());
            ModuleUISettings.getDefault().setLastChosenLibraryLocation(txtLicense.getText());
        }
    }//GEN-LAST:event_browseLicenceButtonActionPerformed
    
    private void browseLibraryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLibraryButtonActionPerformed
        JFileChooser chooser = new JFileChooser(ModuleUISettings.getDefault().getLastChosenLibraryLocation());
        File[] olds = convertStringToFiles(txtLibrary.getText().trim());
        chooser.setSelectedFiles(olds);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        chooser.addChoosableFileFilter(new JarZipFilter());
        int ret = chooser.showDialog(this, getMessage("LBL_Select"));
        if (ret == JFileChooser.APPROVE_OPTION) {
            File[] files =  chooser.getSelectedFiles();
            if (files.length == 0) {
                return;
            }
            String path = "";
            for (int i = 0; i < files.length; i++) {
                path = path + files[i] + ( i == files.length - 1 ? "" : File.pathSeparator);
            }
            txtLibrary.setText(path);
            ModuleUISettings.getDefault().setLastChosenLibraryLocation(files[0].getParentFile().getAbsolutePath());
        }
    }//GEN-LAST:event_browseLibraryButtonActionPerformed
    
    static File[] convertStringToFiles(String path) {
        StringTokenizer tok = new StringTokenizer(path, File.pathSeparator);
        File[] olds = new File[tok.countTokens()];
        for (int i = 0; i < olds.length; i++) {
            olds[i] = new File(tok.nextToken());
        }
        return olds;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseLibraryButton;
    private javax.swing.JButton browseLicenceButton;
    private javax.swing.JPanel confPanel;
    private javax.swing.JPanel filler;
    private javax.swing.JLabel lblLibrary;
    private javax.swing.JLabel lblLicense;
    private javax.swing.JTextField txtLibrary;
    private javax.swing.JTextField txtLicense;
    // End of variables declaration//GEN-END:variables
    
    private static final class JarZipFilter extends FileFilter {
        public boolean accept(File pathname) {
            return  pathname.isDirectory() || pathname.getName().endsWith("zip") || pathname.getName().endsWith("jar"); // NOI18N
        }
        public String getDescription() {
            return getMessage("LibraryStartVisualPanel_jar_zip_filter");
        }
    }
    
}
