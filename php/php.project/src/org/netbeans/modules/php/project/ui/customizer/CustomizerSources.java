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

package org.netbeans.modules.php.project.ui.customizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.netbeans.modules.php.project.environment.PhpEnvironment.DocumentRoot;
import org.netbeans.modules.php.project.ui.CopyFilesVisual;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.LocalServerController;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.Utils.PhpVersionComboBoxModel;
import org.netbeans.modules.php.project.ui.SourcesFolderProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class CustomizerSources extends JPanel implements SourcesFolderProvider, HelpCtx.Provider {
    private static final long serialVersionUID = -5884875643137545L;

    private static final String DEFAULT_WEB_ROOT = NbBundle.getMessage(CustomizerSources.class, "LBL_DefaultWebRoot");
    private final CopyFilesVisual copyFilesVisual;
    private final boolean originalCopySrcFiles;
    private final String originalCopySrcTarget;
    final Category category;
    final PhpProjectProperties properties;
    String originalEncoding;
    boolean notified;
    boolean visible;

    public CustomizerSources(final Category category, final PhpProjectProperties properties) {
        initComponents();

        this.category = category;
        this.properties = properties;

        initEncoding();
        initProjectAndSources();
        webRootTextField.setText(getWebRoot());
        originalCopySrcFiles = initCopyFiles();
        initPhpVersion();
        initTags();

        final LocalServer copyTarget = initCopyTarget();
        originalCopySrcTarget = copyTarget.getSrcRoot();

        copyFilesVisual = new CopyFilesVisual(this, LocalServer.PENDING_LOCAL_SERVER);
        copyFilesVisual.setCopyFiles(originalCopySrcFiles);
        copyFilesVisual.setCopyOnOpen(properties.getCopySrcOnOpen());
        copyFilesVisual.setState(false);
        copyFilesPanel.add(BorderLayout.CENTER, copyFilesVisual);

        PhpEnvironment.get().readDocumentRoots(new PhpEnvironment.ReadDocumentRootsNotifier() {
            @Override
            public void finished(final List<DocumentRoot> documentRoots) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        initCopyTargets(documentRoots, copyTarget);
                    }
                });
            }
        }, getSourcesFolderName());

        encodingComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Charset enc = (Charset) encodingComboBox.getSelectedItem();
                String encName;
                if (enc != null) {
                    encName = enc.name();
                } else {
                    encName = originalEncoding;
                }
                if (!notified && encName != null && !encName.equals(originalEncoding)) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(CustomizerSources.class, "MSG_EncodingWarning"), NotifyDescriptor.WARNING_MESSAGE));
                    notified = true;
                }
                properties.setEncoding(encName);
            }
        });
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        ChangeListener defaultChangeListener = new DefaultChangeListener();
        copyFilesVisual.addChangeListener(defaultChangeListener);
        sourceFolderTextField.getDocument().addDocumentListener(defaultDocumentListener);
        webRootTextField.getDocument().addDocumentListener(defaultDocumentListener);
        phpVersionComboBox.addItemListener(new DefaultComboBoxItemListener());
        ItemListener defaultCheckBoxItemListener = new DefaultCheckBoxItemListener();
        shortTagsCheckBox.addItemListener(defaultCheckBoxItemListener);
        aspTagsCheckBox.addItemListener(defaultCheckBoxItemListener);
    }

    @Override
    public void addNotify() {
        visible = true;
        // validate data on focus
        validateFields();
        super.addNotify();
    }

    @Override
    public void removeNotify() {
        visible = false;
        super.removeNotify();
    }

    // XXX remove
    @SuppressWarnings("unchecked")
    private void initEncoding() {
        originalEncoding = ProjectPropertiesSupport.getEncoding(properties.getProject());
        if (originalEncoding == null) {
            originalEncoding = Charset.defaultCharset().name();
        }
        encodingComboBox.setRenderer(ProjectCustomizer.encodingRenderer());
        encodingComboBox.setModel(ProjectCustomizer.encodingModel(originalEncoding));
        final String lafid = UIManager.getLookAndFeel().getID();
        if (!"Aqua".equals(lafid)) { // NOI18N
             encodingComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE); // NOI18N
             encodingComboBox.addItemListener(new ItemListener() {
                @Override
                 public void itemStateChanged(ItemEvent e) {
                     JComboBox combo = (JComboBox) e.getSource();
                     combo.setPopupVisible(false);
                 }
             });
        }
    }

    private void initProjectAndSources() {
        PhpProject project = properties.getProject();

        // load project path
        FileObject projectFolder = project.getProjectDirectory();
        String projectPath = FileUtil.getFileDisplayName(projectFolder);
        projectFolderTextField.setText(projectPath);

        // sources
        sourceFolderTextField.setText(FileUtil.getFileDisplayName(ProjectPropertiesSupport.getSourcesDirectory(properties.getProject())));
    }

    private void initPhpVersion() {
        PhpVersion phpVersion = ProjectPropertiesSupport.getPhpVersion(properties.getProject());
        assert phpVersion != null;
        phpVersionComboBox.setModel(new PhpVersionComboBoxModel(phpVersion));
    }

    private void initTags() {
        shortTagsCheckBox.setSelected(ProjectPropertiesSupport.areShortTagsEnabled(properties.getProject()));
        aspTagsCheckBox.setSelected(ProjectPropertiesSupport.areAspTagsEnabled(properties.getProject()));
    }

    private boolean initCopyFiles() {
        return ProjectPropertiesSupport.isCopySourcesEnabled(properties.getProject());
    }

    private LocalServer initCopyTarget() {
        // copy target, if any
        File copyTarget = ProjectPropertiesSupport.getCopySourcesTarget(properties.getProject());
        if (copyTarget == null) {
            return LocalServer.getEmpty();
        }
        FileObject resolvedFO = FileUtil.toFileObject(copyTarget);
        if (resolvedFO == null) {
            // target directory doesn't exist?!
            return new LocalServer(copyTarget.getAbsolutePath());
        }
        return new LocalServer(FileUtil.getFileDisplayName(resolvedFO));
    }

    void initCopyTargets(final List<DocumentRoot> roots, LocalServer initialLocalServer) {
        assert initialLocalServer != null;
        int size = roots.size() + 1;
        List<LocalServer> localServers = new ArrayList<>(size);
        localServers.add(initialLocalServer);
        for (DocumentRoot root : roots) {
            LocalServer ls = new LocalServer(root.getDocumentRoot());
            localServers.add(ls);
        }
        copyFilesVisual.setLocalServerModel(new LocalServer.ComboBoxModel(localServers.toArray(new LocalServer[0])));
        copyFilesVisual.selectLocalServer(initialLocalServer);
        copyFilesVisual.setState(true);
        validateFields();
    }

    @Override
    public String getSourcesFolderName() {
        return getSourcesFolder().getName();
    }

    @Override
    public File getSourcesFolder() {
        return FileUtil.normalizeFile(new File(projectFolderTextField.getText()));
    }


    void validateFields() {
        if (!visible) {
            // #160249
            category.setValid(true);
            return;
        }
        if (!copyFilesVisual.getState()) {
            // document roots not read yet
            category.setValid(false);
            return;
        }
        category.setErrorMessage(null);
        category.setValid(true);

        String err = null;

        // sources
        File srcDir = getSrcDir();
        if (!srcDir.isDirectory()) {
            category.setErrorMessage(NbBundle.getMessage(CustomizerSources.class, "MSG_IllegalSources"));
            category.setValid(false);
            return;
        }

        File webRootDir = getWebRootDir();
        if (!webRootDir.exists()) {
            category.setErrorMessage(NbBundle.getMessage(CustomizerSources.class, "MSG_IllegalWebRoot"));
            category.setValid(false);
            return;
        }
        // copy files
        File copyTargetDir = getCopyTargetDir();
        boolean isCopyFiles = copyFilesVisual.isCopyFiles();
        if (isCopyFiles) {
            if (copyTargetDir == null) {
                // nothing selected
                category.setErrorMessage(NbBundle.getMessage(CustomizerSources.class, "MSG_IllegalFolderName"));
                category.setValid(false);
                return;
            }
            err = LocalServerController.validateLocalServer(copyFilesVisual.getLocalServer(), "Folder", true, true); // NOI18N
            if (err != null) {
                category.setErrorMessage(err);
                category.setValid(false);
                return;
            }
            // #131023
            err = Utils.validateSourcesAndCopyTarget(srcDir.getAbsolutePath(), copyTargetDir.getAbsolutePath());
            if (err != null) {
                category.setErrorMessage(err);
                category.setValid(false);
                return;
            }
            // #214888
            if (copyTargetDir.isDirectory()) {
                // target folder already exists - changed or copying not checked before?
                if (targetFolderChanged(copyTargetDir.getAbsolutePath())
                        || !originalCopySrcFiles) {
                    // just warning
                    category.setErrorMessage(NbBundle.getMessage(CustomizerSources.class, "MSG_TargetFolderNotEmpty"));
                }
            }
        }

        // everything ok
        File projectDirectory = FileUtil.toFile(properties.getProject().getProjectDirectory());
        String srcPath = PropertyUtils.relativizeFile(projectDirectory, srcDir);
        if (srcPath == null) {
            // path cannot be relativized => use absolute path (any VCS can be hardly use, of course)
            srcPath = srcDir.getAbsolutePath();
        }
        properties.setSrcDir(srcPath);
        properties.setCopySrcFiles(String.valueOf(isCopyFiles));
        properties.setCopySrcTarget(copyTargetDir == null ? "" : copyTargetDir.getAbsolutePath()); // NOI18N
        properties.setCopySrcOnOpen(copyFilesVisual.isCopyOnOpen());
        String webRoot = PropertyUtils.relativizeFile(srcDir, webRootDir);
        assert webRoot != null && !webRoot.startsWith("../") : "WebRoot must be underneath Sources";
        properties.setWebRoot(webRoot);
        properties.setShortTags(String.valueOf(shortTagsCheckBox.isSelected()));
        properties.setAspTags(String.valueOf(aspTagsCheckBox.isSelected()));
        properties.setPhpVersion(((PhpVersion) phpVersionComboBox.getSelectedItem()).name());
    }

    private File getSrcDir() {
        return new File(sourceFolderTextField.getText()); // file already normalized
    }

    private File getWebRootDir() {
        String webRoot = webRootTextField.getText();
        if (isDefaultWebRoot(webRoot)) {
            return getSrcDir();
        }
        return FileUtil.normalizeFile(new File(getSrcDir(), webRoot));
    }

    private String getWebRoot() {
        String webRoot = properties.getWebRoot();
        if (isDefaultWebRoot(webRoot)) {
            return DEFAULT_WEB_ROOT;
        }
        return webRoot;
    }

    private static boolean isDefaultWebRoot(String webRoot) {
        return webRoot == null || webRoot.trim().length() == 0 || webRoot.equals(".") || DEFAULT_WEB_ROOT.equals(webRoot); // NOI18N
    }

    private File getCopyTargetDir() {
        LocalServer localServer = copyFilesVisual.getLocalServer();
        // #132864
        String srcRoot = localServer.getSrcRoot();
        if (srcRoot == null || srcRoot.length() == 0) {
            return null;
        }
        return FileUtil.normalizeFile(new File(srcRoot));
    }

    private boolean targetFolderChanged(String copyTargetDir) {
        return !originalCopySrcTarget.equals(copyTargetDir); // #133109
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectFolderLabel = new JLabel();
        projectFolderTextField = new JTextField();
        sourceFolderLabel = new JLabel();
        sourceFolderTextField = new JTextField();
        sourceFolderButton = new JButton();
        webRootLabel = new JLabel();
        webRootTextField = new JTextField();
        webRootButton = new JButton();
        copyFilesPanel = new JPanel();
        encodingLabel = new JLabel();
        encodingComboBox = new JComboBox<Charset>();
        phpVersionLabel = new JLabel();
        phpVersionComboBox = new JComboBox<PhpVersion>();
        phpVersionInfoLabel = new JLabel();
        shortTagsCheckBox = new JCheckBox();
        aspTagsCheckBox = new JCheckBox();

        projectFolderLabel.setLabelFor(projectFolderTextField);
        ResourceBundle bundle = ResourceBundle.getBundle("org/netbeans/modules/php/project/ui/customizer/Bundle"); // NOI18N
        Mnemonics.setLocalizedText(projectFolderLabel, bundle.getString("LBL_ProjectFolder")); // NOI18N

        projectFolderTextField.setEditable(false);

        sourceFolderLabel.setLabelFor(sourceFolderTextField);
        Mnemonics.setLocalizedText(sourceFolderLabel, bundle.getString("LBL_SourceFolder")); // NOI18N

        sourceFolderTextField.setEditable(false);

        Mnemonics.setLocalizedText(sourceFolderButton, NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.sourceFolderButton.text")); // NOI18N
        sourceFolderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sourceFolderButtonActionPerformed(evt);
            }
        });

        webRootLabel.setLabelFor(webRootTextField);
        Mnemonics.setLocalizedText(webRootLabel, bundle.getString("LBL_WebRoot")); // NOI18N

        webRootTextField.setEditable(false);

        Mnemonics.setLocalizedText(webRootButton, NbBundle.getMessage(CustomizerSources.class, "LBL_BrowseWebRoot")); // NOI18N
        webRootButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                webRootButtonActionPerformed(evt);
            }
        });

        copyFilesPanel.setLayout(new BorderLayout());

        encodingLabel.setLabelFor(encodingComboBox);
        Mnemonics.setLocalizedText(encodingLabel, NbBundle.getMessage(CustomizerSources.class, "LBL_Encoding")); // NOI18N

        phpVersionLabel.setLabelFor(phpVersionComboBox);
        Mnemonics.setLocalizedText(phpVersionLabel, NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.phpVersionLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(phpVersionInfoLabel, NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.phpVersionInfoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(shortTagsCheckBox, NbBundle.getMessage(CustomizerSources.class, "LBL_ShortTagsEnabled")); // NOI18N

        Mnemonics.setLocalizedText(aspTagsCheckBox, NbBundle.getMessage(CustomizerSources.class, "LBL_AspTagsEnabled")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(projectFolderLabel)
                        .addComponent(sourceFolderLabel)
                        .addComponent(webRootLabel)
                        .addComponent(encodingLabel)
                        .addComponent(phpVersionLabel))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(projectFolderTextField)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(sourceFolderTextField)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(sourceFolderButton))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(webRootTextField)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(webRootButton))
                        .addComponent(encodingComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(phpVersionComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(phpVersionInfoLabel)
                            .addGap(0, 0, Short.MAX_VALUE))))
                .addComponent(copyFilesPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(aspTagsCheckBox)
                    .addComponent(shortTagsCheckBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(projectFolderLabel)
                    .addComponent(projectFolderTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceFolderLabel)
                    .addComponent(sourceFolderTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourceFolderButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(webRootLabel)
                    .addComponent(webRootTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(webRootButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(copyFilesPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(encodingComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(encodingLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(phpVersionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(phpVersionLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(phpVersionInfoLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(shortTagsCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aspTagsCheckBox))
        );

        projectFolderLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.projectFolderLabel.AccessibleContext.accessibleName")); // NOI18N
        projectFolderLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.projectFolderLabel.AccessibleContext.accessibleDescription")); // NOI18N
        projectFolderTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "ACSN_ProjectFolder")); // NOI18N
        projectFolderTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "ACSD_ProjectFolder")); // NOI18N
        sourceFolderLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.sourceFolderLabel.AccessibleContext.accessibleName")); // NOI18N
        sourceFolderLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.sourceFolderLabel.AccessibleContext.accessibleDescription")); // NOI18N
        sourceFolderTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.sourceFolderTextField.AccessibleContext.accessibleName")); // NOI18N
        sourceFolderTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.sourceFolderTextField.AccessibleContext.accessibleDescription")); // NOI18N
        webRootLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.webRootLabel.AccessibleContext.accessibleName")); // NOI18N
        webRootLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.webRootLabel.AccessibleContext.accessibleDescription")); // NOI18N
        webRootTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.webRootTextField.AccessibleContext.accessibleName")); // NOI18N
        webRootTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.webRootTextField.AccessibleContext.accessibleDescription")); // NOI18N
        webRootButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "ACSN_Browse")); // NOI18N
        webRootButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "ACSD_BrowseWebRoot")); // NOI18N
        copyFilesPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.copyFilesPanel.AccessibleContext.accessibleName")); // NOI18N
        copyFilesPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.copyFilesPanel.AccessibleContext.accessibleDescription")); // NOI18N
        encodingLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.encodingLabel.AccessibleContext.accessibleName")); // NOI18N
        encodingLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.encodingLabel.AccessibleContext.accessibleDescription")); // NOI18N
        encodingComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "ACSN_Encoding")); // NOI18N
        encodingComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "ACSD_Encoding")); // NOI18N
        phpVersionLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.phpVersionLabel.AccessibleContext.accessibleName")); // NOI18N
        phpVersionLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.phpVersionLabel.AccessibleContext.accessibleDescription")); // NOI18N
        phpVersionComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.phpVersionComboBox.AccessibleContext.accessibleName")); // NOI18N
        phpVersionComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.phpVersionComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        phpVersionInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.phpVersionInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        phpVersionInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.phpVersionInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        shortTagsCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.shortTagsCheckBox.AccessibleContext.accessibleName")); // NOI18N
        shortTagsCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.shortTagsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        aspTagsCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.aspTagsCheckBox.AccessibleContext.accessibleName")); // NOI18N
        aspTagsCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.aspTagsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void webRootButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_webRootButtonActionPerformed
        FileObject fo = FileUtil.toFileObject(getSrcDir());
        if (fo == null) {
            return;
        }
        String selected = Utils.browseFolder(properties.getProject(), fo, webRootTextField.getText());
        if (isDefaultWebRoot(selected)) {
            selected = DEFAULT_WEB_ROOT;
        }
        webRootTextField.setText(selected);
    }//GEN-LAST:event_webRootButtonActionPerformed

    @NbBundle.Messages("CustomizerSources.src.browse.title=Select Source Files")
    private void sourceFolderButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sourceFolderButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File sources = new FileChooserBuilder(CustomizerSources.class)
                .setDirectoriesOnly(true)
                .setTitle(Bundle.CustomizerSources_src_browse_title())
                .setDefaultWorkingDirectory(FileUtil.toFile(properties.getProject().getProjectDirectory()))
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (sources != null) {
            sourceFolderTextField.setText(FileUtil.normalizeFile(sources).getAbsolutePath());
        }
    }//GEN-LAST:event_sourceFolderButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox aspTagsCheckBox;
    private JPanel copyFilesPanel;
    private JComboBox<Charset> encodingComboBox;
    private JLabel encodingLabel;
    private JComboBox<PhpVersion> phpVersionComboBox;
    private JLabel phpVersionInfoLabel;
    private JLabel phpVersionLabel;
    private JLabel projectFolderLabel;
    private JTextField projectFolderTextField;
    private JCheckBox shortTagsCheckBox;
    private JButton sourceFolderButton;
    private JLabel sourceFolderLabel;
    private JTextField sourceFolderTextField;
    private JButton webRootButton;
    private JLabel webRootLabel;
    private JTextField webRootTextField;
    // End of variables declaration//GEN-END:variables

    private class DefaultChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            validateFields();
        }
    }

    private class DefaultDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            validateFields();
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            validateFields();
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            validateFields();
        }
    }

    private class DefaultCheckBoxItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            validateFields();
        }
    }

    private class DefaultComboBoxItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                validateFields();
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.project.ui.customizer.CustomizerSources"); // NOI18N
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        throw new IllegalStateException();
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        throw new IllegalStateException();
    }
}
