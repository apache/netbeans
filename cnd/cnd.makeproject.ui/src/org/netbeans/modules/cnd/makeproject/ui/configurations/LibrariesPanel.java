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
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Platforms;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.utils.PathPanel;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class LibrariesPanel extends javax.swing.JPanel implements HelpCtx.Provider, PropertyChangeListener {

    private final Project project;
    private final MakeConfiguration conf;
    private final MyListEditorPanel myListEditorPanel;
    private final FSPath baseDir;
    private final PropertyEditorSupport editor;
    private final JButton addProjectButton;
    private final JButton addStandardLibraryButton;
    private final JButton addPkgConfigLibraryButton;
    private final JButton addLibraryButton;
    private final JButton addLibraryFileButton;
    private final JButton addLibraryOption;

    public LibrariesPanel(Project project, MakeConfiguration conf, FSPath baseDir, List<LibraryItem> data, PropertyEditorSupport editor, PropertyEnv env) {
        this.project = project;
        this.conf = conf;
        this.baseDir = baseDir;
        this.editor = editor;
        initComponents();
        //
        addProjectButton = new JButton(getString("ADD_PROJECT_BUTTON_TXT")); // NOI18N
        addProjectButton.setToolTipText(getString("ADD_PROJECT_BUTTON_TT")); // NOI18N
        addProjectButton.setMnemonic(getString("ADD_PROJECT_BUTTON_MN").charAt(0)); // NOI18N

        addStandardLibraryButton = new JButton(getString("ADD_STANDARD_LIBRARY_BUTTON_TXT")); // NOI18N
        addStandardLibraryButton.setToolTipText(getString("ADD_STANDARD_LIBRARY_BUTTON_TT")); // NOI18N
        addStandardLibraryButton.setMnemonic(getString("ADD_STANDARD_LIBRARY_BUTTON_MN").charAt(0)); // NOI18N

        addPkgConfigLibraryButton = new JButton(getString("ADD_PKG_CONFIG_LIBRARY_BUTTON_TXT")); // NOI18N
        addPkgConfigLibraryButton.setToolTipText(getString("ADD_PKG_CONFIG_LIBRARY_BUTTON_TT")); // NOI18N
        addPkgConfigLibraryButton.setMnemonic(getString("ADD_PKG_CONFIG_LIBRARY_BUTTON_MN").charAt(0)); // NOI18N

        addLibraryButton = new JButton(getString("ADD_LIBRARY_BUTTON_TXT")); // NOI18N
        addLibraryButton.setToolTipText(getString("ADD_LIBRARY_BUTTON_TT")); // NOI18N
        addLibraryButton.setMnemonic(getString("ADD_LIBRARY_BUTTON_MN").charAt(0)); // NOI18N

        addLibraryFileButton = new JButton(getString("ADD_LIBRARY_FILE_BUTTON_TXT")); // NOI18N
        addLibraryFileButton.setToolTipText(getString("ADD_LIBRARY_FILE_BUTTON_TT")); // NOI18N
        addLibraryFileButton.setMnemonic(getString("ADD_LIBRARY_FILE_BUTTON_MN").charAt(0)); // NOI18N

        addLibraryOption = new JButton(getString("ADD_OPTION_BUTTON_TXT")); // NOI18N
        addLibraryOption.setToolTipText(getString("ADD_OPTION_BUTTON_TT")); // NOI18N
        addLibraryOption.setMnemonic(getString("ADD_OPTION_BUTTON_MN").charAt(0)); // NOI18N

        JButton[] extraButtons = new JButton[]{addProjectButton, addStandardLibraryButton, addPkgConfigLibraryButton,
                                               addLibraryButton, addLibraryFileButton, addLibraryOption};
        myListEditorPanel = new MyListEditorPanel(conf, data, extraButtons);
        addProjectButton.addActionListener(new AddProjectButtonAction());
        addStandardLibraryButton.addActionListener(new AddStandardLibraryButtonAction());
        addPkgConfigLibraryButton.addActionListener(new AddPkgCongigLibraryButtonAction());
        addLibraryButton.addActionListener(new AddLibraryButtonAction());
        addLibraryOption.addActionListener(new AddLinkerOptionButtonAction());
        addLibraryFileButton.addActionListener(new AddLibraryFileButtonAction());
        //
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        outerPanel.add(myListEditorPanel, gridBagConstraints);
        instructionsTextArea.setBackground(instructionPanel.getBackground());
        setPreferredSize(new java.awt.Dimension(700, 450));

        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);
    }

    public void setInstructionsText(String txt) {
        instructionsTextArea.setText(txt);
    }

    private List<LibraryItem> getListData() {
        return myListEditorPanel.getListData();
    }

    private ArrayList<LibraryItem> getPropertyValue() throws IllegalStateException {
        return new ArrayList<>(getListData());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("Libraries"); // NOI18N
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        outerPanel = new javax.swing.JPanel();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(323, 223));
        outerPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(outerPanel, gridBagConstraints);

        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 12, 0, 12);
        add(instructionPanel, gridBagConstraints);
    }

    private class MyListEditorPanel extends TableEditorPanel {

        public MyListEditorPanel(MakeConfiguration conf, List<LibraryItem> objects, JButton[] extraButtons) {
            super(conf, objects, extraButtons, baseDir);
            getAddButton().setVisible(false);
            //getCopyButton().setVisible(false);
            getEditButton().setVisible(false);
            getDefaultButton().setVisible(false);
        }

        @Override
        public String getListLabelText() {
            return getString("LIBRARIES_AND_OPTIONS_TXT");
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("LIBRARIES_AND_OPTIONS_MN").charAt(0);
        }

        @Override
        public LibraryItem copyAction(LibraryItem o) {
            LibraryItem libraryItem = o;
            return libraryItem.clone();
        }
    }

    private final class AddProjectButtonAction implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            MakeArtifact[] artifacts = MakeArtifactChooser.showDialog(MakeArtifactChooser.ArtifactType.LIBRARY, project, baseDir, myListEditorPanel);
            if (artifacts != null) {
                for (int i = 0; i < artifacts.length; i++) {
                    String location = ProjectSupport.toProperPath(baseDir.getFileObject(), artifacts[i].getProjectLocation(), project);
                    String workingdir = ProjectSupport.toProperPath(baseDir.getFileObject(), artifacts[i].getWorkingDirectory(), project);
                    location = CndPathUtilities.normalizeSlashes(location);
                    workingdir = CndPathUtilities.normalizeSlashes(workingdir);
                    artifacts[i].setProjectLocation(location);
                    artifacts[i].setWorkingDirectory(workingdir);
                    myListEditorPanel.addObjectAction(new LibraryItem.ProjectItem(artifacts[i]));
                }
            }
        }
    }

    private final class AddStandardLibraryButtonAction implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            StdLibPanel stdLibPanel = new StdLibPanel(Platforms.getPlatform(conf.getDevelopmentHost().getBuildPlatform()).getStandardLibraries());
            DialogDescriptor dialogDescriptor = new DialogDescriptor(stdLibPanel, getString("SELECT_STATNDARD_LIBRARY_DIALOG_TITLE"));
            DialogDisplayer.getDefault().notify(dialogDescriptor);
            if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
                return;
            }
            LibraryItem.StdLibItem[] libs = stdLibPanel.getSelectedStdLibs();
            for (int i = 0; i < libs.length; i++) {
                myListEditorPanel.addObjectAction(libs[i]);
            }
        }
    }

    private final class AddPkgCongigLibraryButtonAction implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            JButton okButton = new JButton(getString("PkgConfigLibrary_CTL_OK")); //NOI18N
            JButton cancel = new JButton(getString("PkgConfigLibrary_CTL_Cancel")); //NOI18N
            PkgConfigLibrary stdLibPanel = new PkgConfigLibrary(conf.getDevelopmentHost().getExecutionEnvironment(), conf, okButton);
            DialogDescriptor dialogDescriptor = new DialogDescriptor(stdLibPanel, getString("SELECT_STATNDARD_LIBRARY_DIALOG_TITLE")); //NOI18N
            Object[] options = new Object[] { okButton , cancel };
            dialogDescriptor.setOptions(options);
            dialogDescriptor.setClosingOptions(options);
            DialogDisplayer.getDefault().notify(dialogDescriptor);
            if (dialogDescriptor.getValue() != okButton) {
                return;
            }
            PackageConfiguration[] libs = stdLibPanel.getPkgConfigLibs();
            for (int i = 0; i < libs.length; i++) {
                // This string is parsed in class org.netbeans.modules.cnd.makeproject.configurations.QmakeProjectWriter                
                myListEditorPanel.addObjectAction(new LibraryItem.OptionItem("`pkg-config --libs "+libs[i].getName()+"`")); //NOI18N
            }
        }
    }

    private final class AddLibraryButtonAction implements java.awt.event.ActionListener {

        private FileFilter lastSelectedFilter = null;

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final String chooser_key = "AddLibrary"; //NOI18N
            final ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(baseDir.getFileSystem());
            String seed = RemoteFileChooserUtil.getCurrentChooserFile(chooser_key, env);
            if (seed == null) {
                seed = baseDir.getPath();
            }
            FileFilter[] filters = FileFilterFactory.getLibraryFilters(baseDir.getFileSystem());
            if (lastSelectedFilter == null) {
                lastSelectedFilter = filters[0];
            }
            JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(env, getString("SELECT_LIBRARY_CHOOSER_TITLE"), getString("SELECT_CHOOSER_BUTTON"),
                                       JFileChooser.FILES_ONLY, filters, seed, true);
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setFileFilter(lastSelectedFilter);
            int ret = fileChooser.showOpenDialog(myListEditorPanel);
            if (ret == JFileChooser.CANCEL_OPTION) {
                return;
            }
            lastSelectedFilter = fileChooser.getFileFilter();
            final File[] files = fileChooser.getSelectedFiles();
            if (files == null || files.length == 0) {
                return;
            }            
            File selectedFolder = files[0].getParentFile();
            RemoteFileChooserUtil.setCurrentChooserFile(chooser_key, selectedFolder.getPath(), env);
            for(File libFile: files) {
                String libName = libFile.getName();
                if (libName.startsWith("lib")) // NOI18N
                {
                    libName = libName.substring(3);
                }
                if (libName.endsWith(".so") || // NOI18N
                        libName.endsWith(".dll") || // NOI18N
                        libName.endsWith(".dylib") || // NOI18N
                        libName.endsWith(".lib") || // NOI18N
                        libName.endsWith(".a")) { // NOI18N
                    int i = libName.lastIndexOf('.');
                    libName = libName.substring(0, i);
                }
                myListEditorPanel.addObjectAction(new LibraryItem.LibItem(libName));
            }
        }
    }

    private final class AddLibraryFileButtonAction implements java.awt.event.ActionListener {

        private FileFilter lastSelectedFilter = null;

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final String chooser_key = "AddLibraryFile"; //NOI18N
            final ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(baseDir.getFileSystem());
            String seed = RemoteFileChooserUtil.getCurrentChooserFile(chooser_key, env);
            if (seed == null) {
                seed = baseDir.getPath();
            }
            FileFilter[] filters = FileFilterFactory.getLibraryFilters(baseDir.getFileSystem());
            if (lastSelectedFilter == null) {
                lastSelectedFilter = filters[0];
            }
            JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(env, getString("SELECT_LIBRARY_FILE_CHOOSER_TITLE"), getString("SELECT_CHOOSER_BUTTON"), JFileChooser.FILES_ONLY, filters, seed, true);
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setFileFilter(lastSelectedFilter);
            PathPanel pathPanel = new PathPanel();
            fileChooser.setAccessory(pathPanel);
            int ret = fileChooser.showOpenDialog(null);
            if (ret == JFileChooser.CANCEL_OPTION) {
                return;
            }
            lastSelectedFilter = fileChooser.getFileFilter();
            final File[] files = fileChooser.getSelectedFiles();
            if (files == null || files.length == 0) {
                return;
            }                        
            File selectedFolder = files[0].getParentFile();
            RemoteFileChooserUtil.setCurrentChooserFile(chooser_key, selectedFolder.getPath(), env);            
            for(File libFile: files) {
                // FIXUP: why are baseDir UNIX path when remote?
                String path = ProjectSupport.toProperPath(baseDir.getFileObject(), libFile.getPath(), project);
                path = CndPathUtilities.normalizeSlashes(path);
                myListEditorPanel.addObjectAction(new LibraryItem.LibFileItem(path));
            }
        }
    }

    private final class AddLinkerOptionButtonAction implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            LibraryOptionPanel libraryOptionPanel = new LibraryOptionPanel();
            DialogDescriptor dialogDescriptor = new DialogDescriptor(libraryOptionPanel, getString("SELECT_OPTION_DIALOG_TITLE"));
            DialogDisplayer.getDefault().notify(dialogDescriptor);
            if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
                return;
            }
            if (libraryOptionPanel.getOption(conf).trim().length() == 0) {
                return;
            }
            myListEditorPanel.addObjectAction(new LibraryItem.OptionItem(libraryOptionPanel.getOption(conf)));
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JPanel outerPanel;
    // End of variables declaration//GEN-END:variables

    private static String getString(String s) {
        return NbBundle.getBundle(LibrariesPanel.class).getString(s);
    }
}
