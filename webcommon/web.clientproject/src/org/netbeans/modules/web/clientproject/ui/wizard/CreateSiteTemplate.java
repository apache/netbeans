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
package org.netbeans.modules.web.clientproject.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.sites.SiteZip;
import org.netbeans.modules.web.clientproject.ui.customizer.ClientSideProjectProperties;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.CheckableNode;
import org.openide.explorer.view.OutlineView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

@NbBundle.Messages({"CreateSiteTemplate_Name=Create site template",
    "CreateSiteTemplate_Title=Describe template",
    "CreateSiteTemplate_Label=Name template and select files",
    "CreateSiteTemplate_WizardTitle=Create Site Template from current project",
    "CreateSiteTemplate_Error1=Template name must be specified",
    "CreateSiteTemplate_Error1_extension=Template name must be a ZIP file (*.zip).",
    "CreateSiteTemplate_Error2=Destination name must be specified",
    "CreateSiteTemplate_Error3=Destination is not a valid folder",
    "# {0} - template file", "CreateSiteTemplate_Error4=Template file {0} already exists. Do you want to override it?",
    "CreateSiteTemplate_FileChooser=Select folder to store template in",
    "CreateSiteTemplate_FileChooserButton=Select"
})
public class CreateSiteTemplate extends javax.swing.JPanel implements ExplorerManager.Provider, DocumentListener {

    private FileObject root;
    private OutlineView tree;
    private ExplorerManager manager;
    private WizardPanel wp;

    public CreateSiteTemplate(FileObject root, FileObject externalSiteRoot, WizardPanel wp) {
        this.root = root;
        this.manager = new ExplorerManager();
        this.wp = wp;
        try {
            if (externalSiteRoot != null) {
                ExternalSiteRootNode externalSiteRootNode = new ExternalSiteRootNode(DataObject.find(externalSiteRoot).getNodeDelegate(), externalSiteRoot.isFolder());
                manager.setRootContext(new OurFilteredNode(DataObject.find(root).getNodeDelegate(), externalSiteRootNode));
            } else {
                manager.setRootContext(new OurFilteredNode(DataObject.find(root).getNodeDelegate(), root.isFolder()));
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        initComponents();
        tree = new OutlineView();
        tree.setTreeSortable(false);
        placeholder.setLayout(new BorderLayout());
        placeholder.add(tree, BorderLayout.CENTER);
        nameTextField.getDocument().addDocumentListener(this);
        fileTextField.getDocument().addDocumentListener(this);
        fileTextField.setText(new File(System.getProperty("user.home")).getAbsolutePath()); // NOI18N
    }

    @Override
    public String getName() {
        return Bundle.CreateSiteTemplate_Label();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        placeholder = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(CreateSiteTemplate.class, "CreateSiteTemplate.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout placeholderLayout = new javax.swing.GroupLayout(placeholder);
        placeholder.setLayout(placeholderLayout);
        placeholderLayout.setHorizontalGroup(
            placeholderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 393, Short.MAX_VALUE)
        );
        placeholderLayout.setVerticalGroup(
            placeholderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 185, Short.MAX_VALUE)
        );

        jLabel2.setText(org.openide.util.NbBundle.getMessage(CreateSiteTemplate.class, "CreateSiteTemplate.jLabel2.text")); // NOI18N

        fileTextField.setText(org.openide.util.NbBundle.getMessage(CreateSiteTemplate.class, "CreateSiteTemplate.fileTextField.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(CreateSiteTemplate.class, "CreateSiteTemplate.jLabel3.text")); // NOI18N

        nameTextField.setText(org.openide.util.NbBundle.getMessage(CreateSiteTemplate.class, "CreateSiteTemplate.nameTextField.text")); // NOI18N

        browseButton.setText(org.openide.util.NbBundle.getMessage(CreateSiteTemplate.class, "CreateSiteTemplate.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileTextField)
                    .addComponent(nameTextField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseButton))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 177, Short.MAX_VALUE))
            .addComponent(placeholder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(placeholder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Bundle.CreateSiteTemplate_FileChooser());
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonText(Bundle.CreateSiteTemplate_FileChooserButton());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showDialog(this, null) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f.isFile()) {
                f = f.getParentFile();
            }
            fileTextField.setText(f.getAbsolutePath());
        }

    }//GEN-LAST:event_browseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JPanel placeholder;
    // End of variables declaration//GEN-END:variables

    private String getErrorMessage() {
        String tplName = getTemplateName().trim();
        if (tplName.length() == 0) {
            return Bundle.CreateSiteTemplate_Error1();
        }
        if (tplName.indexOf('.') != -1 // NOI18N
                && !tplName.endsWith(".zip")) { // NOI18N
            return Bundle.CreateSiteTemplate_Error1_extension();
        }
        if (getTemplateFolder().trim().length() == 0) {
            return Bundle.CreateSiteTemplate_Error2();
        }
        if (!new File(getTemplateFolder()).exists()) {
            return Bundle.CreateSiteTemplate_Error3();
        }
        return ""; //NOI18N
    }

    public String getTemplateName() {
        return nameTextField.getText();
    }

    public String getTemplateFolder() {
        return fileTextField.getText();
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        wp.fireChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        wp.fireChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        wp.fireChange();
    }

    private static class WizardPanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor> {

        private final CreateSiteTemplate comp;
        private final ChangeSupport sup = new ChangeSupport(this);
        private WizardDescriptor wd;

        public WizardPanel(ClientSideProject p) {
            FileObject siteRoot = p.getSiteRootFolder();
            comp = new CreateSiteTemplate(p.getProjectDirectory(),
                    siteRoot != null && !FileUtil.isParentOf(p.getProjectDirectory(), siteRoot) ? siteRoot : null, this);
            comp.putClientProperty("WizardPanel_contentSelectedIndex", 0); //NOI18N
            // Sets steps names for a panel
            comp.putClientProperty("WizardPanel_contentData", new String[]{Bundle.CreateSiteTemplate_Title()}); //NOI18N
            // Turn on subtitle creation on each step
            comp.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); //NOI18N
            // Show steps on the left side with the image on the background
            comp.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); //NOI18N
            // Turn on numbering of all steps
            comp.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); //NOI18N
        }

        @Override
        public Component getComponent() {
            return comp;
        }

        @Override
        public HelpCtx getHelp() {
            return new HelpCtx("org.netbeans.modules.web.clientproject.ui.wizard.CreateSiteTemplate"); // NOI18N
        }

        @Override
        public boolean isValid() {
            String error = comp.getErrorMessage();
            setErrorMessage(error);
            return error.length() == 0;
        }

        public void setErrorMessage(String message) {
            if (wd != null) {
                wd.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            }
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            sup.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            sup.removeChangeListener(l);
        }

        void fireChange() {
            sup.fireChange();
        }

        @Override
        public boolean isFinishPanel() {
            return true;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            this.wd = settings;
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
        }
    }

    private static class WizardIterator implements WizardDescriptor.BackgroundInstantiatingIterator<WizardDescriptor> {

        private final WizardPanel panel;
        private final ClientSideProject p;
        private final ChangeSupport sup = new ChangeSupport(this);

        public WizardIterator(ClientSideProject p) {
            this.p = p;
            panel = new WizardPanel(p);
            panel.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    sup.fireChange();
                }
            });
        }

        @NbBundle.Messages({
            "# {0} - template name",
            "CreateSiteTemplate.info.templateCreating=Creating template {0}...",
            "# {0} - template name",
            "CreateSiteTemplate.info.templateCreated=Template {0} successfully created.",
        })
        @Override
        public Set<FileObject> instantiate() throws IOException {
            assert !EventQueue.isDispatchThread();
            // threading model of WizardPanel is broken and cannot be properly used
            String name = panel.comp.getTemplateName();
            if (!name.endsWith(".zip")) { //NOI18N
                name += ".zip"; //NOI18N
            }
            ProgressHandle progressHandle = ProgressHandle.createHandle(Bundle.CreateSiteTemplate_info_templateCreating(name));
            try {
                progressHandle.start();
                File f = new File(panel.comp.getTemplateFolder(), name);
                if (f.exists()) {
                    if (DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Confirmation(Bundle.CreateSiteTemplate_Error4(f.getAbsolutePath()))) != NotifyDescriptor.YES_OPTION) {
                        return null;
                    }
                }
                createZipFile(f, p, panel.comp.manager.getRootContext());
                StatusDisplayer.getDefault().setStatusText(Bundle.CreateSiteTemplate_info_templateCreated(name));
            } finally {
                progressHandle.finish();
            }
            ClientSideProjectUtilities.logUsage(CreateSiteTemplate.class, "USG_PROJECT_HTML5_SAVE_AS_TEMPLATE", null); // NOI18N
            return null;
        }

        @Override
        public void initialize(WizardDescriptor wizard) {
        }

        @Override
        public void uninitialize(WizardDescriptor wizard) {
        }

        @Override
        public Panel<WizardDescriptor> current() {
            return panel;
        }

        @Override
        public String name() {
            return Bundle.CreateSiteTemplate_Name();
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public void nextPanel() {
        }

        @Override
        public void previousPanel() {
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            sup.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            sup.removeChangeListener(l);
        }

    }

    public static void showWizard(ClientSideProject p) {
        WizardDescriptor wd = new WizardDescriptor(new WizardIterator(p));
        wd.setTitleFormat(new MessageFormat("{0}")); //NOI18N
        wd.setTitle(Bundle.CreateSiteTemplate_WizardTitle());
        DialogDisplayer.getDefault().notify(wd);
    }

    /**
     * Filter node which shows filtered list of children and has ability to be
     * "selected" via Checkable instance in its lookup.
     */
    private class OurFilteredNode extends FilterNode {

        public OurFilteredNode(Node projectNode, ExternalSiteRootNode externalSiteRoot) {
            super(projectNode, new ProjectChildren(projectNode, externalSiteRoot),
                    Lookups.fixed(new Checkable(), projectNode.getLookup().lookup(FileObject.class)));
            Checkable ch = getLookup().lookup(Checkable.class);
            ch.setOwner(this);
            ch.setComponent(tree);
        }

        public OurFilteredNode(Node original, boolean hasChildren) {
            super(original, hasChildren ? new FilteredChildren(original) : Children.LEAF,
                    Lookups.fixed(new Checkable(), original.getLookup().lookup(FileObject.class)));
            Checkable ch = getLookup().lookup(Checkable.class);
            ch.setOwner(this);
            ch.setComponent(tree);
        }

        public void refresh() {
            fireIconChange();
        }


    }

    /**
     * Special children list which merges 'external site root node' and
     * project's folder natural children.
     */
    private class ProjectChildren extends Children.Keys<Node> {

        private Node projectNode;
        private Node externalSiteRoot;

        public ProjectChildren(Node projectNode, Node externalSiteRoot) {
            this.projectNode = projectNode;
            this.externalSiteRoot = externalSiteRoot;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            List<Node> res = new ArrayList<Node>();
            res.add(externalSiteRoot);
            res.addAll(Arrays.asList(projectNode.getChildren().getNodes(true)));
            setKeys(res);
        }

        @Override
        protected Node[] createNodes(Node key) {
            if (!isValidChild(key)) {
                return new Node[0];
            }
            FileObject fo = key.getLookup().lookup(FileObject.class);
            assert fo != null;
            return new Node[] {new OurFilteredNode(key, fo.isFolder())};
        }

    }

    /**
     * Filtered node's children, see {@link #isValidChild(org.openide.nodes.Node)}
     * for applied filter.
     */
    private class FilteredChildren extends FilterNode.Children {

        public FilteredChildren(Node owner) {
            super(owner);
        }

        @Override
        protected Node copyNode(Node node) {
            FileObject fo = node.getLookup().lookup(FileObject.class);
            assert fo != null;
            return new OurFilteredNode(node, fo.isFolder());
        }

        @Override
        protected Node[] createNodes(Node key) {
            if (!isValidChild(key)) {
                return new Node[0];
            }
            return super.createNodes(key);
        }
    }

    /**
     * Checks whether given node represents a folder/file suitable to be
     * exported into site template.
     */
    private static boolean isValidChild(Node n) {
        FileObject fo = n.getLookup().lookup(FileObject.class);
        if (fo == null) {
            return false;
        }
        if ("nbproject".equals(fo.getName())) { //NOI18N
            return false;
        }
        if (!VisibilityQuery.getDefault().isVisible(fo)) {
            return false;
        }
        return true;
    }

    /**
     * This node wraps 'external site root folder node' and gives it
     * display name of {@value ClientSideProjectConstants#DEFAULT_SITE_ROOT_FOLDER}.
     */
    private class ExternalSiteRootNode extends OurFilteredNode {

        public ExternalSiteRootNode(Node original, boolean hasChildren) {
            super(original, hasChildren);
        }

        @Override
        public String getDisplayName() {
            return ClientSideProjectConstants.DEFAULT_SITE_ROOT_FOLDER;
        }

        @Override
        public String getShortDescription() {
            return getDisplayName();
        }

    }

    private static class Checkable implements CheckableNode {

        private static boolean internalUpdate = false;

        private Boolean checked = Boolean.TRUE;
        private OurFilteredNode node;
        private JComponent comp;

        @Override
        public boolean isCheckable() {
            return true;
        }

        @Override
        public boolean isCheckEnabled() {
            return true;
        }

        @Override
        public Boolean isSelected() {
            return checked;
        }

        @Override
        public void setSelected(Boolean selected) {
            checked = selected;
            if (internalUpdate) {
                return;
            } else {
                try {
                    internalUpdate = true;
                    if (checked != null) {
                        propagateChanges(node, checked);
                    }
                } finally {
                    internalUpdate = false;
                }
            }
        }

        private static void propagateChanges(OurFilteredNode node, boolean checked) {
            if (checked) {
                tick(node.getChildren(), true);
                OurFilteredNode n = node;
                while (n.getParentNode() != null) {
                    n = (OurFilteredNode)n.getParentNode();
                    n.getLookup().lookup(Checkable.class).setSelected(Boolean.TRUE);
                    n.refresh();
                }
            } else {
                tick(node.getChildren(), false);
            }
        }

        private static void tick(Children ch, boolean tick) {
            if (ch == null) {
                return;
            }
            for (Node n : ch.getNodes(true)) {
                n.getLookup().lookup(Checkable.class).setSelected(tick ? Boolean.TRUE : Boolean.FALSE);
                ((OurFilteredNode)n).refresh();
                tick(n.getChildren(), tick);
            }
        }

        private void setOwner(OurFilteredNode aThis) {
            node = aThis;
        }

        public void setComponent(JComponent comp) {
            this.comp = comp;
        }

    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value="RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification="checking return value of createNewFile() is pointless")
    private static void createZipFile(File templateFile, ClientSideProject project, Node rootNode) throws IOException {
        if (!templateFile.exists()) {
            templateFile.createNewFile();
        }
        try (ZipOutputStream str = new ZipOutputStream(new FileOutputStream(templateFile))) {
            writeProjectMetadata(str, project);
            writeChildren(str, project.getProjectDirectory(), project.getSiteRootFolder(), rootNode.getChildren());
        }
        SiteZip.registerTemplate(templateFile);
    }

    private static void writeProjectMetadata(ZipOutputStream str, ClientSideProject project) throws IOException {
        ZipEntry ze = new ZipEntry(ClientSideProjectConstants.TEMPLATE_DESCRIPTOR);
        str.putNextEntry(ze);
        EditableProperties ep = new EditableProperties(false);
        ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(project);
        FileObject projectDirectory = project.getProjectDirectory();
        FileObject siteRootFolder = project.getSiteRootFolder();
        String siteRoot;
        if (!ClientSideProjectUtilities.isParentOrItself(projectDirectory, siteRootFolder)) {
            siteRoot = ClientSideProjectConstants.DEFAULT_SITE_ROOT_FOLDER;
        } else {
            siteRoot = projectProperties.getSiteRootFolder().get();
        }
        if (siteRoot != null) {
            ep.setProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER, siteRoot);
        }
        FileObject sourceFolder = project.getSourcesFolder();
        String sources;
        if (!ClientSideProjectUtilities.isParentOrItself(projectDirectory, sourceFolder)) {
            sources = ClientSideProjectConstants.DEFAULT_SOURCE_FOLDER;
        } else {
            sources = projectProperties.getSourceFolder().get();
        }
        if (sources != null) {
            ep.setProperty(ClientSideProjectConstants.PROJECT_SOURCE_FOLDER, sources);
        }
        String testFolder = projectProperties.getTestFolder().get();
        if (testFolder != null) {
            ep.setProperty(ClientSideProjectConstants.PROJECT_TEST_FOLDER, testFolder);
        }
        ep.store(str);
    }

    private static void writeChildren(ZipOutputStream str, FileObject projectDirectory, FileObject siteRoot, Children children) throws IOException {
        for (Node node : children.getNodes(true)) {
            FileObject fo = node.getLookup().lookup(FileObject.class);
            InputStream is = null;
            if (!fo.isFolder()) {
                is = fo.getInputStream();
            }
            try {
                Checkable ch = node.getLookup().lookup(Checkable.class);
                if (!Boolean.TRUE.equals(ch.isSelected())) {
                    continue;
                }
                String relPath = getRelativePath(projectDirectory, siteRoot, fo);
                if (fo.isFolder()) {
                    relPath += "/"; //NOI18N
                }
                ZipEntry ze = new ZipEntry(relPath);
                str.putNextEntry(ze);
                if (is != null) {
                    FileUtil.copy(fo.getInputStream(), str);
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            if (!node.isLeaf()) {
                writeChildren(str, projectDirectory, siteRoot, node.getChildren());
            }
        }
    }

    private static String getRelativePath(FileObject projectDirectory, FileObject siteRoot, FileObject fo) {
        String relativePath = FileUtil.getRelativePath(projectDirectory, fo);
        if (relativePath != null) {
            return relativePath;
        }
        if (fo.equals(siteRoot)) {
            // site root itself
            return ClientSideProjectConstants.DEFAULT_SITE_ROOT_FOLDER;
        }
        relativePath = FileUtil.getRelativePath(siteRoot, fo);
        assert relativePath != null : "File '" + fo + "' not underneath site root '" + siteRoot + "'";
        assert !relativePath.isEmpty() : "Some relative path expected for '" + fo + "' and site root '" + siteRoot + "'";
        return ClientSideProjectConstants.DEFAULT_SITE_ROOT_FOLDER + "/" + relativePath; // NOI18N
    }

}
