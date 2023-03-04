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

package org.netbeans.modules.form.editors;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * A file chooser allowing to choose a file or folder from the classpath of a
 * user project. Can be used as a standalone panel, or as a dialog.
 * 
 * @author Tomas Pavek
 */
public class ClassPathFileChooser extends JPanel implements ExplorerManager.Provider {

    private boolean choosingFolder;

    private ExplorerManager explorerManager;

    private List<FileObject> packageRoots;
    private FileObject selectedFile;
    private FileObject selectedFolder;
    private boolean confirmed;

    private BeanTreeView treeView;
    private JButton newButton;
    private JButton okButton;
    private JButton cancelButton;
    private JTextField fileNameTextField;

    public static final String PROP_SELECTED_FILE = "selectedFile"; // NOI18N

    public interface Filter {
        boolean accept(FileObject file);
    }

// [TODO: multiselection, separate type of classpath (all vs. project's sources only)
//  - not needed for now]

    /**
     * Creates a new ClassPathFileChooser. Can be used directly as a panel,
     * or getDialog can be called to get it wrapped in a Dialog.
     * @param fileInProject a source file from project sources (determines the
     *        project's classpath)
     * @param filter a filter for files to be displayed
     * @param choosingFolder if true, the chooser only allows to select a folder,
     *        and only source classpath is shown (i.e. not JARs on execution CP)
     * @param okCancelButtons defines whether the controls buttons should be shown
     *        (typically true if using as a dialog and false if using as a panel)
     */
    public ClassPathFileChooser(FileObject fileInProject, Filter filter, boolean choosingFolder, boolean okCancelButtons) {
        this.choosingFolder = choosingFolder;

        Listener listener = new Listener();

        Node root = getRootNode(fileInProject, filter);
        explorerManager = new ExplorerManager();
        explorerManager.setRootContext(root);
        try {
            explorerManager.setSelectedNodes (new Node[] { root });
        }
        catch(PropertyVetoException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        explorerManager.addPropertyChangeListener(listener);

        if (choosingFolder) { // add a button allowing to create a new folder
            newButton = new JButton();
            Mnemonics.setLocalizedText(newButton, NbBundle.getMessage(ClassPathFileChooser.class, "CTL_CreateNewButton")); // NOI18N
            newButton.addActionListener(listener);
            newButton.setEnabled(false);
            newButton.setToolTipText(NbBundle.getMessage(ClassPathFileChooser.class, "CTL_CreateNewButtonHint")); // NOI18N
        }
        if (okCancelButtons) {
            okButton = new JButton(NbBundle.getMessage(ClassPathFileChooser.class, "CTL_OKButton")); // NOI18N
            okButton.addActionListener(listener);
            okButton.setEnabled(false);
            cancelButton = new JButton(NbBundle.getMessage(ClassPathFileChooser.class, "CTL_CancelButton")); // NOI18N
        }

        treeView = new BeanTreeView();
        treeView.setPopupAllowed(false);
        treeView.setDefaultActionAllowed(true);
        treeView.setBorder((Border)UIManager.get("Nb.ScrollPane.border")); // NOI18N
        treeView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ClassPathFileChooser.class, "ACSN_FileSelectorTreeView")); // NOI18N
        treeView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ClassPathFileChooser.class, "ACSD_FileSelectorTreeView")); // NOI18N
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ClassPathFileChooser.class, "ACSD_FileSelectorPanel")); // NOI18N

        // label and text field with mnemonic
        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, NbBundle.getMessage(ClassPathFileChooser.class,
                choosingFolder ? "LBL_FolderName": "LBL_FileName")); // NOI18N
        fileNameTextField = new JTextField();
        fileNameTextField.getDocument().addDocumentListener(listener);
        fileNameTextField.addActionListener(listener);
        label.setLabelFor(fileNameTextField);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateGaps(true);

        GroupLayout.SequentialGroup sq = layout.createSequentialGroup()
                                            .addComponent(label).addComponent(fileNameTextField);
        if (!okCancelButtons && newButton != null) // add newButton next to the text field
            sq.addComponent(newButton);
        layout.setHorizontalGroup(layout.createParallelGroup()
            .addComponent(treeView, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(sq));

        GroupLayout.ParallelGroup pq = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label).addComponent(fileNameTextField);
        if (!okCancelButtons && newButton != null) // add newButton next to the text field
            pq.addComponent(newButton);
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(treeView, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(pq));
    }

    /**
     * Creates a modal dialog containing the file chooser with given title.
     * Use ActionListener to be informed about pressing OK button. Otherwise
     * call isConfirmed which returns true if OK button was pressed.
     * @param title the title of the dialog
     * @param listener ActionListener attached to the OK button (if not null)
     */
    public Dialog getDialog(String title, ActionListener listener) {
        if (okButton == null)
            throw new IllegalStateException("Can't create dialog for a chooser without OK and Cancel buttons."); // NOI18N

        ((GroupLayout)getLayout()).setAutoCreateContainerGaps(true);

        DialogDescriptor dd = new DialogDescriptor(
            this, title,  true,
            newButton != null ?
                new JButton[] { newButton, okButton, cancelButton } :
                new JButton[] { okButton, cancelButton },
            okButton,
            DialogDescriptor.DEFAULT_ALIGN, HelpCtx.DEFAULT_HELP,
            null
        );
        dd.setClosingOptions(new JButton[] { okButton, cancelButton });
        if (listener != null)
            okButton.addActionListener(listener);
        return DialogDisplayer.getDefault().createDialog(dd);
    }

    @Override
    public void addNotify() {
        confirmed = false;
        super.addNotify();
        treeView.requestFocusInWindow();
    }

    /**
     * Returns if the user selected some file and confirmed by OK button.
     * @return true if OK button has been pressed by the user since last call of
     *         getDialog
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Returns the file selected by the user (or set via setSelectedFile method).
     * @return the FileObject selected in the chooser
     */
    public FileObject getSelectedFile() {
        return selectedFile;
    }

    /**
     * Sets the selected file in the chooser. The tree view is expanded as
     * needed and the corresponding node selected.
     * @param file the FileObject to be selected in the chooser
     */
    public void setSelectedFile(FileObject file) {
        if (file != null) {
            if (getRoot(file) == null) {
                return;
            }
            selectFileNode(file);
        }
        selectedFile = file;
    }

    /**
     * Returns the classpath root of the selected file.
     * @return the package root of the selected file
     */
    public FileObject getSelectedPackageRoot() {
        return getRoot(getSelectedFile());
    }

    private class Listener implements PropertyChangeListener, ActionListener, DocumentListener {
        // called when Create New or OK button pressed
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == okButton) {
                confirmed = true;
            }
            else if (e.getSource() == newButton) {
                if (selectedFolder == null || selectedFile != null)
                    return;

                String fileName = fileNameTextField.getText();
                try { // create a new dir
                    selectedFile = selectedFolder.createFolder(fileName);
                    selectFileNode(selectedFile);
                }
                catch (Exception ex) { // report failure (name should be OK from checkFileName)
                    ErrorManager.getDefault().notify(ex);
                }
                if (choosingFolder && selectedFile != null)
                    firePropertyChange(PROP_SELECTED_FILE, null, selectedFile);
            }
            else if (e.getSource() == fileNameTextField) { // enter pressed in the text field
                if (selectedFile == null) { // nothing set from checkNameField
                    String fileName = fileNameTextField.getText();
                    if (fileName.startsWith("/")) // NOI18N
                        fileName = fileName.substring(1);
                    // try to find from root
                    for (FileObject root : packageRoots) {
                        FileObject fo = root.getFileObject(fileName);
                        if (fo != null) {
                            selectedFile = fo;
                            selectedFolder = fo.getParent();
                            break;
                        }
                    }
                    
                }
                if (selectedFile != null) {
                    Node[] nodes = explorerManager.getSelectedNodes();
                    if (nodes.length != 1 || fileFromNode(nodes[0]) != selectedFile) {
                        selectFileNode(selectedFile);
                        treeView.requestFocus();
                    }
                    else if (okButton != null) {
                        okButton.doClick();
                        return;
                    }
                    if (okButton != null)
                        okButton.setEnabled(selectedFile != null && (!selectedFile.isFolder() || choosingFolder));
                    if (newButton != null)
                        newButton.setEnabled(false);
                }
            }
        }

        // called from ExplorerManager when node selection changes
        @Override
        public void propertyChange (PropertyChangeEvent ev) {
            if (ev.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                Node[] nodes = explorerManager.getSelectedNodes();
                FileObject oldSelected = selectedFile;
                selectedFile = null;
                selectedFolder = null;
                if (nodes.length == 1) {
                    FileObject fo = fileFromNode(nodes[0]);
                    if (fo != null) {
                        fileNameTextField.setText(!fo.isFolder() || choosingFolder ?
                                                  fo.getNameExt() : ""); // NOI18N
                        selectedFile = fo;
                        selectedFolder = fo.getParent();
                    }
                }
                if (okButton != null)
                    okButton.setEnabled(selectedFile != null && (!selectedFile.isFolder() || choosingFolder));
                if (newButton != null)
                    newButton.setEnabled(false);

                firePropertyChange(PROP_SELECTED_FILE, oldSelected, selectedFile);
            }
        }

        // called when a the user types in the text field (DocumentListener)
        @Override
        public void changedUpdate(DocumentEvent e) {
        }

        // called when a the user types in the text field (DocumentListener)
        @Override
        public void insertUpdate(DocumentEvent e) {
            checkNameField();
        }

        // called when a the user types in the text field (DocumentListener)
        @Override
        public void removeUpdate(DocumentEvent e) {
            checkNameField();
        }
    }

    private void checkNameField() {
        if (selectedFolder != null) {
            selectedFile = null;
            String fileName = fileNameTextField.getText();
            Node[] nodes = explorerManager.getSelectedNodes();
            if (nodes.length == 1) {
                FileObject fo = fileFromNode(nodes[0]);
                if (fo != null) {
                    if (!fo.isFolder())
                        fo = fo.getParent();
                    selectedFile = fo.getFileObject(fileName);
                    selectedFolder = fo;
                }
            }
            if (okButton != null)
                okButton.setEnabled(selectedFile != null && (!selectedFile.isFolder() || choosingFolder));
            if (newButton != null) {
                newButton.setEnabled(selectedFile == null && choosingFolder
                                     && Utilities.isJavaIdentifier(fileName));
            }
        }
    }

    /**
     * Implementation of ExplorerManager.Provider. Needed for the tree view to work.
     */
    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    // ------

    private static FileObject fileFromNode(Node n) {
        DataObject dobj = n.getCookie(DataObject.class);
        return dobj != null ? dobj.getPrimaryFile() : null;
    }

    private void selectFileNode(FileObject fo) {
        selectNode(explorerManager.getRootContext(), fo);
    }

    private void selectNode(Node parent, FileObject fo) {
        for (Node n : parent.getChildren().getNodes(true)) {
            FileObject nodeFO = fileFromNode(n);
            if (nodeFO == fo) {
                try {
                    if (fo.isFolder()) {
                        explorerManager.setExploredContext(n); // to expand the folder
                    }
                    explorerManager.setSelectedNodes(new Node[] { n });
                }
                catch (PropertyVetoException ex) { // should not happen
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
                break;
            }
            else if (FileUtil.isParentOf(nodeFO, fo)) {
                selectNode(n, fo);
                break;
            }
        }
    }

    private FileObject getRoot(FileObject fo) {
        if (fo != null) {
            for (FileObject root : packageRoots) {
                if (root == fo || FileUtil.isParentOf(root, fo))
                    return root;
            }
        }
        return null;
    }

    private Node getRootNode(FileObject fileInProject, Filter filter) {
        Children children = new Children.Array();
        children.add(createPackageRootNodes(fileInProject, choosingFolder, filter));
        AbstractNode root = new AbstractNode(children);
        root.setIconBaseWithExtension("org/netbeans/modules/form/editors2/iconResourceRoot.gif"); // NOI18N
        root.setDisplayName(NbBundle.getMessage(ClassPathFileChooser.class, "CTL_ClassPathName")); // NOI18N
        // ProjectUtils.getInformation(prj).getDisplayName()
        return root;
    }

    private Node[] createPackageRootNodes(FileObject fileInProject, boolean onlySources, Filter filter) {
        ClassPath cp = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
        Project project = FileOwnerQuery.getOwner(fileInProject);
        if (cp == null || project == null)
            return new Node[] {};

        packageRoots = new LinkedList<FileObject>();
        List<Node> nodeList = new LinkedList<Node>();
        for (Object e : cp.entries()) {
            ClassPath.Entry cpEntry = (ClassPath.Entry) e;
            // try to map it to sources
            URL url = cpEntry.getURL();
            SourceForBinaryQuery.Result r= SourceForBinaryQuery.findSourceRoots(url);
            FileObject[] roots = r.getRoots();
            if (roots.length > 0) {
                for (FileObject fo : roots) {
                    if (!onlySources || fo.canWrite()) { // if sources then writable sources
                        Node n = createPackageRootNode(fo, project, filter);
                        if (n != null) {
                            packageRoots.add(fo);
                            nodeList.add(n);
                        }
                    }
                }
            }
            else if (!onlySources)  { // add the class-path location directly
                FileObject fo = cpEntry.getRoot();
                Node n = fo != null ? createPackageRootNode(fo, project, filter) : null;
                if (n != null) {
                    packageRoots.add(fo);
                    nodeList.add(n);
                }
            }
        }

        Node[] nodes = new Node[nodeList.size()];
        nodeList.toArray(nodes);
        return nodes;
    }

    private Node createPackageRootNode(FileObject rootFO, Project project, Filter filter) {
        Node origNode;
        try {
            origNode = DataObject.find(rootFO).getNodeDelegate();
        }
        catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }

        String displayName;
        Project owner = FileOwnerQuery.getOwner(rootFO);
        if (owner != null) {
            SourceGroup g = getSourceGroup(rootFO, owner);
            displayName = g != null ? g.getDisplayName() : FileUtil.getFileDisplayName(rootFO);
            if (project != owner) {
                ProjectInformation pi = ProjectUtils.getInformation(owner);
                displayName += " [" + pi.getDisplayName() + "]"; // NOI18N
            }
        }
        else displayName = FileUtil.getFileDisplayName(rootFO);

        return new FilteredNode(origNode, displayName, filter);

    }

    private static SourceGroup getSourceGroup(FileObject file, Project prj) {
        Sources src = ProjectUtils.getSources(prj);
        for (SourceGroup g : src.getSourceGroups("java")) { // NOI18N
            if (file == g.getRootFolder()) {
                return g;
            }
        }
        for (SourceGroup g : src.getSourceGroups("Resources")) { // NOI18N
            if (file == g.getRootFolder()) {
                return g;
            }
        }
        return null;
    }

    private class FilteredNode extends FilterNode {
        FilteredNode(Node original, String displayName, Filter filter) {
            super(original, original.isLeaf() ? Children.LEAF : new FilteredChildren(original, filter));
            if (displayName != null) {
                disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME);
                setDisplayName(displayName);
            }
        }

        @Override
        public Action getPreferredAction() {
            if (isLeaf()) { // double click on file will invoke OK button
                return new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (okButton.isEnabled()) {
                            okButton.doClick();
                        }
                    }
                };
            }
            return null;
        }
    }

    /**
     * A mutually recursive children that ensure propagation of the
     * filter to deeper levels of hierarchy. That is, it creates
     * FilteredNodes filtered by the same filter.
     */
    public class FilteredChildren extends FilterNode.Children {
        private Filter filter;

        public FilteredChildren(Node original, Filter filter) {
            super(original);
            this.filter = filter;
        }

        @Override
        protected Node copyNode(Node node) {
            return filter != null ? new FilteredNode(node, null, filter) :
                                    super.copyNode(node);
        }

        @Override
        protected Node[] createNodes(Node key) {
            if (filter != null) {
                FileObject fo = fileFromNode(key);
                if (fo == null || !filter.accept(fo))
                    return new Node[0];
            }
            return super.createNodes(key);
        }
    }
}
