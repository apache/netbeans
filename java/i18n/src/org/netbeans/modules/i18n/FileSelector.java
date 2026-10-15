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

package org.netbeans.modules.i18n;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;

/**
 * Panel for selecting a properties file (by browsing a project tree). Also
 * allows to create a new file. Should be displayed as a dialog - using
 * getDialog(...) method. Substitutes use of NodeOperation.select which does
 * not provide a satisfactory UI.
 */
public class FileSelector extends JPanel implements PropertyChangeListener, ExplorerManager.Provider {

    // [this could be configurable to make the file selector more general]
    private static final String PROPERTIES_EXT = ".properties"; // NOI18N
    private static final String DEFAULT_BUNDLE_NAME = "Bundle"; // NOI18N

    private DataObject template;

    private ExplorerManager manager;

    private DataObject selectedDataObject;
    private DataFolder selectedFolder;
    private boolean confirmed;

    private JButton newButton;
    private JButton okButton;
    private JButton cancelButton;
    private JTextField fileNameTextField;

    public FileSelector(FileObject fileInProject, DataObject template) {
        this(fileInProject, template, null);
    }

    public FileSelector(FileObject fileInProject, 
                        DataObject template,
                        FileObject preselectedFile) {
        this(SelectorUtils.bundlesNode(null, fileInProject, template == null), template);
        if (preselectedFile != null) {
            preselectFile(preselectedFile);
        } else {
            preselectDefaultBundle(fileInProject);
        }
    }

    public void preselectFile(FileObject fo) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp == null) {
            return;
        }
        String packagePath = cp.getResourceName(fo.getParent()) 
                                + "/" + fo.getName(); //NOI18N
        String[] packagePathArr = packagePath.split("/"); //NOI18N
        Node[] roots = manager.getRootContext().getChildren().getNodes();

        // try search inside every "src" subnode in tree ...
        for (Node possibleRoot : roots) {
            try {
                Node foundNode = NodeOp.findPath(possibleRoot, packagePathArr);

                if (foundNode != null) {
                    manager.setSelectedNodes(new Node[]{foundNode});
                    break;
                }
            } catch (NodeNotFoundException ex) {
                //Exceptions.printStackTrace(ex);
            } catch (PropertyVetoException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }
    }

    public void preselectDefaultBundle(FileObject fo) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp == null) {
            return;
        }
        String packageName = cp.getResourceName(fo.getParent());
        Node root = manager.getRootContext();
        List<String> path = new ArrayList<>();
        for (FileObject fo2 : cp.getRoots()) {
            if (FileUtil.isParentOf(fo2, fo)) {
                path.add(fo2.getName());
                break;
            }
        }
        assert path.size() == 1;
        path.addAll(Arrays.<String>asList(packageName.split("/"))); //NOI18N
        path.add("Bundle"); // NOI18N
        try {
            manager.setSelectedNodes(new Node[] {NodeOp.findPath(root, path.toArray(new String[0]))});
            return;
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NodeNotFoundException e) {
            //Ignore it
        }
        // removes Bundle and selects package:
        path = path.subList(0, path.size()-1);
        try {
            Node found = NodeOp.findPath(root,path.toArray(new String[0]));
            manager.setExploredContext(found);
            manager.setSelectedNodes(new Node[] {found});
            return;
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NodeNotFoundException ex) {
            // Ignore it
        }
        try {
            manager.setSelectedNodes(new Node[] {root});
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private FileSelector(Node root, DataObject template) {
        this.template = template;

        manager = new ExplorerManager();
        manager.setRootContext(root);
        manager.addPropertyChangeListener(this);

        if (template != null) {
            newButton = new JButton();
            Mnemonics.setLocalizedText(
                    newButton,
                    getLocMessage("CTL_CreateNewButton"));              //NOI18N
            newButton.getAccessibleContext().setAccessibleName(
                    getLocMessage("ACSN_CreateNewBundle"));             //NOI18N
            newButton.getAccessibleContext().setAccessibleDescription(
                    getLocMessage("ACSD_CreateNewBundle"));             //NOI18N
            newButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (selectedFolder == null)
                        return;

                    String fileName = fileNameTextField.getText();
                    try {
                        if (fileName.equals(""))
                            fileName = DEFAULT_BUNDLE_NAME; // NOI18N
                        else if (fileName.toLowerCase().endsWith(PROPERTIES_EXT))
                            fileName = fileName.substring(0, fileName.length()-PROPERTIES_EXT.length());

                        selectedDataObject = FileSelector.this.template.createFromTemplate(selectedFolder, fileName);
                        // select created
                        Node[] selected = manager.getSelectedNodes();
                        //case when folder was selected
                        if (selected != null && selected.length == 1
                                && selected[0].getCookie(DataObject.class) == selectedFolder) {
                            Node[] sub = selected[0].getChildren().getNodes(true);
                            for (int i=0; i < sub.length; i++) {
                                if (sub[i].getCookie(DataObject.class) == selectedDataObject) {
                                    manager.setSelectedNodes(new Node[] { sub[i] });
                                    break;
                                }
                            }
                        //case when another properties file was selected
                        } else if (selected != null && selected.length == 1
                                && selected[0].getCookie(DataObject.class).getFolder() == selectedFolder) {
                            Node[] sub = selected[0].getParentNode().getChildren().getNodes(true);
                            for (int i=0; i < sub.length; i++) {
                                if (sub[i].getCookie(DataObject.class) == selectedDataObject) {
                                    manager.setSelectedNodes(new Node[] { sub[i] });
                                    break;
                                }
                            }
                        }
                    }
                    catch (Exception ex) { // TODO report failure
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            });
            newButton.setEnabled(false);
        }
        okButton = new JButton(NbBundle.getMessage(FileSelector.class, "CTL_OKButton")); // NOI18N
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                confirmed = true;
            }
        });
        okButton.setEnabled(false);
        cancelButton = new JButton(getLocMessage("CTL_CancelButton")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleName(
                getLocMessage("ACSN_CancelSelection"));                 //NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(
                getLocMessage("ACSD_CancelSelection"));                 //NOI18N

        BeanTreeView treeView = new BeanTreeView ();
        treeView.setPopupAllowed(false);
        treeView.setDefaultActionAllowed(false);
        treeView.setBorder((Border)UIManager.get("Nb.ScrollPane.border")); // NOI18N
        treeView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FileSelector.class, "ACSN_FileSelectorTreeView")); // NOI18N
        treeView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSelector.class, "ACSD_FileSelectorTreeView")); // NOI18N
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSelector.class, "ACSD_FileSelectorPanel")); // NOI18N

        JLabel treeViewLabel = new JLabel();
        Mnemonics.setLocalizedText(
                treeViewLabel,
                getLocMessage("LBL_ExistingBundles"));                  //NOI18N
        treeViewLabel.setLabelFor(treeView.getViewport());

        // label and text field with mnemonic
        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label,
                getLocMessage("LBL_FileName"));                         //NOI18N
        label.getAccessibleContext().setAccessibleName(
                getLocMessage("ACSN_FileName"));                        //NOI18N
        label.getAccessibleContext().setAccessibleDescription(
                getLocMessage("ACSD_FileName"));                        //NOI18N
        fileNameTextField = new JTextField();
        fileNameTextField.getDocument().addDocumentListener(new DocumentListener() { // NOI18N
            public void changedUpdate(DocumentEvent e) {
            }
            public void insertUpdate(DocumentEvent e) {
                checkFileName();
            }
            public void removeUpdate(DocumentEvent e) {
                checkFileName();
            }
        });
        label.setLabelFor(fileNameTextField);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup()
            .addComponent(treeViewLabel)
            .addComponent(treeView, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(label)
                .addComponent(fileNameTextField)));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(treeViewLabel)
            .addComponent(treeView, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(label)
                .addComponent(fileNameTextField)));
    }

    private static String getLocMessage(String bundleKey) {
        return NbBundle.getMessage(FileSelector.class, bundleKey);
    }

    /**
     * Creates a modal dialog containing the file selector with given title.
     * Use ActionListener to be informed about pressing OK button.
     * @param title
     * @param listener ActionListener attached to the OK button (if not null)
     */
    public Dialog getDialog(String title, ActionListener listener) {
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
    }

    boolean isConfirmed() {
        return confirmed;
    }

    public DataObject getSelectedDataObject() {
        return selectedDataObject;
    }

    public void propertyChange (PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
            Node[] nodes = manager.getSelectedNodes();
            selectedDataObject = null;
            selectedFolder = null;
            if (nodes != null && nodes.length == 1) {
                DataObject dobj = nodes[0].getCookie(DataObject.class);
                if (dobj != null) {
                    if (dobj instanceof PropertiesDataObject) {
                        fileNameTextField.setText(dobj.getName());
                        selectedDataObject = dobj;
                        selectedFolder = dobj.getFolder();
                    }
                    else if (dobj instanceof DataFolder) {
                        fileNameTextField.setText(""); // NOI18N
                        selectedFolder = (DataFolder) dobj;
                    }
                    else selectedFolder = dobj.getFolder();
                }
            }
            okButton.setEnabled(selectedDataObject != null);
            if (newButton != null)
                newButton.setEnabled(selectedFolder != null
                                     && selectedDataObject == null
                                     && !checkForDefaultBundle());
        }
    }

    private boolean checkForDefaultBundle() {
        if (selectedFolder != null) {
            return selectedFolder.getPrimaryFile().getFileObject(DEFAULT_BUNDLE_NAME + PROPERTIES_EXT) != null;
        }
        return false;
    }

    private void checkFileName() {
        if (selectedFolder == null)
            return;

        selectedDataObject = null;
        String fileName = fileNameTextField.getText();
        if ("".equals(fileName)) { // NOI18N
            okButton.setEnabled(false);
            if (newButton != null)
                newButton.setEnabled(!checkForDefaultBundle());
        }
        else {
            if (!fileName.toLowerCase().endsWith(PROPERTIES_EXT))
                fileName = fileName + PROPERTIES_EXT;

            FileObject fo = selectedFolder.getPrimaryFile().getFileObject(fileName);
            if (fo != null) {
                try {
                    selectedDataObject = DataObject.find(fo);
                }
                catch (DataObjectNotFoundException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }

            okButton.setEnabled(selectedDataObject != null);
            if (newButton != null)
                newButton.setEnabled(selectedDataObject == null);
        }
    }

    /**
     * Implementation of ExplorerManager.Provider. Needed for the tree view to work.
     */
    public ExplorerManager getExplorerManager() {
        return manager;
    }
}
