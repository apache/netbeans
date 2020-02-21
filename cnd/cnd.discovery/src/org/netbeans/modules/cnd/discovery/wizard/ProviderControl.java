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

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.ProviderPropertyType.PropertyKind;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 */
public class ProviderControl<T> {
    private final ProviderProperty property;
    private final String propertyKey;
    private final DiscoveryDescriptor wizardDescriptor;
    private final JPanel panel;
    private final ChangeListener listener;
    private FileSystem projectFileSystem;
    private JLabel label;
    private ExpandableEditableComboBox field;
    private JButton button;
    private int chooserMode = 0;
    private static final String LIST_LIST_DELIMITER = ";"; // NOI18N
    
    public ProviderControl(String key, ProviderProperty<T> property, DiscoveryDescriptor wizardDescriptor,
            JPanel panel, ChangeListener listener){
        this.propertyKey = key;
        this.property = property;
        this.panel = panel;
        this.listener = listener;
        this.wizardDescriptor = wizardDescriptor;
        try {
            projectFileSystem = wizardDescriptor.getProject().getProjectDirectory().getFileSystem();
        } catch (FileStateInvalidException ex) {
            projectFileSystem = CndFileSystemProvider.getLocalFileSystem();
        }
        label = new JLabel();
        Mnemonics.setLocalizedText(label, property.getName());
        switch(property.getPropertyType().kind()) {
            case MakeLogFile:
                field = new ExpandableEditableComboBox();
                field.setEditable(true);
                chooserMode = JFileChooser.FILES_ONLY;
                initBuildOrRoot(wizardDescriptor);
                button = new JButton();
                Mnemonics.setLocalizedText(button, getString("ROOT_DIR_BROWSE_BUTTON_TXT"));
                layout(panel);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        rootFolderButtonActionPerformed(evt, ProviderControl.this.property.getPropertyType().kind()==PropertyKind.BinaryFile,
                                                        getString("LOG_FILE_CHOOSER_TITLE_TXT"));
                    }
                });
                addListeners();
                break;
            case BinaryFile:
                field = new ExpandableEditableComboBox();
                field.setEditable(true);
                chooserMode = JFileChooser.FILES_ONLY;
                initBuildOrRoot(wizardDescriptor);
                button = new JButton();
                Mnemonics.setLocalizedText(button, getString("ROOT_DIR_BROWSE_BUTTON_TXT"));
                layout(panel);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        rootFolderButtonActionPerformed(evt, ProviderControl.this.property.getPropertyType().kind()==PropertyKind.BinaryFile,
                                                        getString("BINARY_FILE_CHOOSER_TITLE_TXT"));
                    }
                });
                addListeners();
                break;
            case Folder:
                field = new ExpandableEditableComboBox();
                field.setEditable(true);
                chooserMode = JFileChooser.DIRECTORIES_ONLY;
                initRoot(wizardDescriptor);
                button = new JButton();
                Mnemonics.setLocalizedText(button, getString("ROOT_DIR_BROWSE_BUTTON_TXT"));
                layout(panel);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        rootFolderButtonActionPerformed(evt, true, getString("ROOT_DIR_CHOOSER_TITLE_TXT"));
                    }
                });
                addListeners();
                break;
            case BinaryFiles:
                field = new ExpandableEditableComboBox();
                field.setEditable(true);
                chooserMode = JFileChooser.FILES_ONLY;
                initArray();
                button = new JButton();
                Mnemonics.setLocalizedText(button, getString("ROOT_DIR_EDIT_BUTTON_TXT"));
                layout(panel);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        additionalLibrariesButtonActionPerformed(evt);
                    }
                });
                addListeners();
                break;
            default:
                // unsuported UI
                break;
        }
    }
    
    private void initBuildOrRoot(DiscoveryDescriptor wizardDescriptor){
        Object val = property.getValue();
        String output = null;
        if (val instanceof String){
            output = (String)val;
        }
        if (output != null && output.length() > 0){
            initFields(output);
            return;
        }
        output = wizardDescriptor.getBuildResult();
        if (output != null && output.length() > 0){
            initFields(output);
            return;
        }
        initFields(wizardDescriptor.getRootFolder());
    }
    
    private void initRoot(DiscoveryDescriptor wizardDescriptor){
        Object val = property.getValue();
        String output = null;
        if (val instanceof String){
            output = (String)val;
        }
        if (output != null && output.length() > 0){
            initFields(output);
            return;
        }
        initFields(wizardDescriptor.getRootFolder());
    }
    
    private void initArray(){
        Object val = property.getValue();
        if (val instanceof String[]){
            StringBuilder buf = new StringBuilder();
            for(String s : (String[])val){
                if (buf.length()>0){
                    buf.append(LIST_LIST_DELIMITER);
                }
                buf.append(s);
            }
            initFields(buf.toString());
        } else {
            initFields(""); // NOI18N
        }
    }

    private void initComboBox(String root){
        Preferences preferences;
        if (SelectProviderPanel.USE_PROJECT_PROPERTIES) {
            preferences = ProjectUtils.getPreferences(wizardDescriptor.getProject(), ProviderControl.class, false);
        } else {
            preferences = NbPreferences.forModule(ProviderControl.class);
        }
        field.setStorage(propertyKey, preferences);
        field.setEnv(FileSystemProvider.getExecutionEnvironment(projectFileSystem));
        field.read(root);
    }
    
    private void addListeners(){
        field.addChangeListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
    }
    
    private void update() {
        listener.stateChanged(null);
    }
    
    public void store(){
        switch(property.getPropertyType().kind()) {
            case MakeLogFile:
            case Folder:
            case BinaryFile:
                property.setValue(getComboBoxText());
                storeHistory();
                break;
            case BinaryFiles:
                String text = getComboBoxText();
                StringTokenizer st = new StringTokenizer(text,LIST_LIST_DELIMITER); // NOI18N
                List<String> list = new ArrayList<>();
                while(st.hasMoreTokens()){
                    list.add(st.nextToken());
                }
                property.setValue(list.toArray(new String[list.size()]));
                storeHistory();
                break;
            default:
                break;
        }

    }
    
    private void storeHistory() {
        Preferences preferences;
        if (SelectProviderPanel.USE_PROJECT_PROPERTIES) {
            preferences = ProjectUtils.getPreferences(wizardDescriptor.getProject(), ProviderControl.class, false);
        } else {
            preferences = NbPreferences.forModule(ProviderControl.class);
            field.setStorage(propertyKey, NbPreferences.forModule(ProviderControl.class));
        }
        field.setStorage(propertyKey, preferences);
        field.store();
    }

    public boolean valid() {
        String path = getComboBoxText();
        //ProviderControlFolderError="{0}" is not a folder
        //ProviderControlFileError=File "{0}" not found
        switch(property.getPropertyType().kind()) {
            case Folder: {
                if (path.length() == 0) {
                    wizardDescriptor.setMessage(getString("ProviderControlFolderError", path)); // NOI18N
                    return false;
                }
                FSPath file = new FSPath(projectFileSystem, path);
                FileObject fo = file.getFileObject();
                if (fo == null || !fo.isValid() || !fo.isFolder()) {
                    wizardDescriptor.setMessage(getString("ProviderControlFolderError", path)); // NOI18N
                    return false;
                }
                return true;
            }
            case MakeLogFile:
            case BinaryFile: {
                if (path.length() == 0) {
                    wizardDescriptor.setMessage(getString("ProviderControlFileError", path)); // NOI18N
                    return false;
                }
                FSPath file = new FSPath(projectFileSystem, path);
                FileObject fo = file.getFileObject();
                if (fo == null || !fo.isValid() || !fo.isData()) {
                    wizardDescriptor.setMessage(getString("ProviderControlFileError", path)); // NOI18N
                    return false;
                }
                return true;
            }
            case BinaryFiles:
                String text = getComboBoxText();
                StringTokenizer st = new StringTokenizer(text,LIST_LIST_DELIMITER); // NOI18N
                while(st.hasMoreTokens()){
                    path = st.nextToken();
                    if (path.length() == 0) {
                        wizardDescriptor.setMessage(getString("ProviderControlFileError", path)); // NOI18N
                        return false;
                    }
                    FSPath file = new FSPath(projectFileSystem, path);
                    FileObject fo = file.getFileObject();
                    if (fo == null || !fo.isValid() || !fo.isData()) {
                        wizardDescriptor.setMessage(getString("ProviderControlFileError", path)); // NOI18N
                        return false;
                    }
                }
                return true;
            default:
                break;
        }
        return false;
    }
    
    private void layout(JPanel panel){
        GridBagConstraints gridBagConstraints;
        label.setLabelFor(field);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        panel.add(label, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 4, 0, 0);
        panel.add(field, gridBagConstraints);
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < 35; i++) {
            buf.append("w"); // NOI18N
        }
        field.setPrototypeDisplayValue(buf.toString());
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = GridBagConstraints.SOUTH;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        panel.add(button, gridBagConstraints);
    }
    
    private void additionalLibrariesButtonActionPerformed(ActionEvent evt) {
        StringTokenizer tokenizer = new StringTokenizer(getComboBoxText(), LIST_LIST_DELIMITER); // NOI18N
        List<String> list = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        AdditionalLibrariesListPanel libPanel = new AdditionalLibrariesListPanel(list);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(AdditionalLibrariesListPanel.wrapPanel(libPanel),
                getString("ADDITIONAL_LIBRARIES_TXT"));
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue()  == DialogDescriptor.OK_OPTION) {
            List<String> newList = libPanel.getListData();
            StringBuilder includes = new StringBuilder();
            for (int i = 0; i < newList.size(); i++) {
                if (i > 0) {
                    includes.append(LIST_LIST_DELIMITER); // NOI18N
                }
                includes.append(newList.get(i));
            }
            field.setSelectedItem(includes.toString());
        }
    }
    
    private void rootFolderButtonActionPerformed(ActionEvent evt, boolean isBinary, String title) {
        FileFilter[] filters = null;
        if (chooserMode == JFileChooser.FILES_ONLY){
            if (isBinary) {
                filters = FileFilterFactory.getBinaryFilters();
            } else {
                filters = new FileFilter[]{new LogFileFilter()};
            }
        }
        ExecutionEnvironment execEnv = FileSystemProvider.getExecutionEnvironment(projectFileSystem);
        JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(execEnv,
                title,
                getString("ROOT_DIR_BUTTON_TXT"), // NOI18N
                chooserMode,
                filters,
                getComboBoxText(),
                false
                );
        int ret = fileChooser.showOpenDialog(panel);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        field.setSelectedItem(path);
    }

    private String getComboBoxText() {
        return field.getText();
    }
    
    private void initFields(String path) {
        // Set default values
        if (path == null) {
            initComboBox(""); // NOI18N
        } else {
            if (CndFileUtils.isLocalFileSystem(projectFileSystem) && Utilities.isWindows()) {
                path = path.replace('/', CndFileUtils.getFileSeparatorChar(projectFileSystem));
            }
            initComboBox(path);
        }
    }
    
    private String getString(String key, String ... params) {
        return NbBundle.getMessage(ProviderControl.class, key, params);
    }

    private class LogFileFilter extends javax.swing.filechooser.FileFilter {
        public LogFileFilter() {
        }
        @Override
        public String getDescription() {
            return(getString("FILECHOOSER_MAK_LOG_FILEFILTER")); // NOI18N
        }
        @Override
        public boolean accept(File f) {
            if (f != null) {
                if (f.isDirectory()) {
                    return true;
                }
                String name = f.getName();
                return name.endsWith(".log") || name.endsWith(".json"); // NOI18N
            }
            return false;
        }
    }
}
