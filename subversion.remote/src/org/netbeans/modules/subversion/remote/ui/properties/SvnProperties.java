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
package org.netbeans.modules.subversion.remote.ui.properties;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.ISVNProperty;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * 
 * 
 */
public final class SvnProperties implements ActionListener {

    /** Subversion properties that may be set only on directories */
    private static final HashSet<String> DIR_ONLY_PROPERTIES = new HashSet<>(Arrays.asList(new String[] {
                                                            "svn:ignore", //NOI18N
                                                            "svn:externals"})); //NOI18N
 
    /** Subversion properties that may be set only on files (not directories) */
    private static final HashSet<String> FILE_ONLY_PROPERTIES = new HashSet<>(Arrays.asList(new String[] {
                                                            "svn:eol-style", //NOI18N
                                                            "svn:executable", //NOI18N
                                                            "svn:keywords", //NOI18N
                                                            "svn:needs-lock", //NOI18N
                                                            "svn:mime-type"})); //NOI18N

    private static final HashSet<String> MIXED_PROPERTIES = new HashSet<>(DIR_ONLY_PROPERTIES.size() + FILE_ONLY_PROPERTIES.size());
    static {
        MIXED_PROPERTIES.addAll(DIR_ONLY_PROPERTIES);
        MIXED_PROPERTIES.addAll(FILE_ONLY_PROPERTIES);
    }

    private PropertiesPanel panel;
    private final VCSFileProxy[] roots;
    private final PropertiesTable propTable;
    private SvnProgressSupport support;
    private boolean loadedFromFile;
    private VCSFileProxy loadedValueFile;
    private final Set<VCSFileProxy> folders = new HashSet<>();
    private final Set<VCSFileProxy> files = new HashSet<>();
    private final Map<String, Set<VCSFileProxy>> filesPerProperty = new HashMap<>();

    /** Creates a ew instance of SvnProperties */
    public SvnProperties(PropertiesPanel panel, PropertiesTable propTable, VCSFileProxy[] files) {
        this.panel = panel;
        this.propTable = propTable;
        this.roots = files;
        propTable.getTable().addMouseListener(new TableMouseListener());
        panel.btnRefresh.addActionListener(this);
        panel.btnAdd.addActionListener(this);
        panel.btnRemove.addActionListener(this);
        panel.btnBrowse.addActionListener(this);
        panel.comboName.setEditable(true);
        for (VCSFileProxy f : files) {
            if (f.isDirectory()) {
                folders.add(f);
            } else {
                this.files.add(f);
            }
        }
        panel.setForDirectory(!folders.isEmpty());
        if (folders.isEmpty()) {
            panel.setIllegalPropertyNames(
                    DIR_ONLY_PROPERTIES.toArray(new String[DIR_ONLY_PROPERTIES.size()]),
                    "PropertiesPanel.errInvalidPropertyForFile");       //NOI18N
        } else {
            panel.setRecursiveProperties(FILE_ONLY_PROPERTIES);
        }
        setLoadedValueFile(null);
        initPropertyNameCbx();
        setLoadedFromFile(false);
        refreshProperties();
        panel.initInteraction();
    }

    public PropertiesPanel getPropertiesPanel() {
        return panel;
    }

    public void setPropertiesPanel(PropertiesPanel panel) {
        this.panel = panel;
    }

    public VCSFileProxy[] getFiles () {
        return roots;
    }

    private void setLoadedValueFile(VCSFileProxy file) {
        this.loadedValueFile = file;
    }

    private VCSFileProxy getLoadedValueFile() {
        return loadedValueFile;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source.equals(panel.btnRefresh)) {
            refreshProperties();
        }

        if (source.equals(panel.btnAdd)) {
            setProperties();
        }

        if (source.equals(panel.btnRemove)) {
            removeProperties();
        }

        if (source.equals(panel.btnBrowse)) {
            loadFromFile();
        }
    }

    protected void initPropertyNameCbx() {
        if (panel.comboName.isEditable()) {
            panel.setPredefinedPropertyNames(folders.isEmpty()
                                             ? FILE_ONLY_PROPERTIES.toArray(new String[FILE_ONLY_PROPERTIES.size()])
                                             : MIXED_PROPERTIES.toArray(new String[MIXED_PROPERTIES.size()]));
        }
    }

    protected String getPropertyValue() {
        return SvnUtils.fixLineEndings(panel.getPropertyValue());
    }

    protected String getPropertyName() {
        return panel.getPropertyName();
    }

    public boolean isLoadedFromFile() {
        return loadedFromFile;
    }

    public void setLoadedFromFile(boolean value) {
        loadedFromFile = value;
        if (loadedFromFile) {
            panel.setPropertyValueChangeListener(this);
        }
    }

    public void handleBinaryFile(VCSFileProxy source) {
        setLoadedValueFile(source);
        StringBuilder txtValue = new StringBuilder();
        txtValue.append(NbBundle.getMessage(SvnProperties.class, "Binary_Content"));
        txtValue.append("\n"); //NOI18N
        try {
            txtValue.append(VCSFileProxySupport.getCanonicalPath(source));
        } catch (IOException ex) {
            Subversion.LOG.log(Level.SEVERE, null, ex);
        }
        panel.txtAreaValue.setText(txtValue.toString());
        setLoadedFromFile(true);
    }

    public void loadFromFile() {
        final JFileChooser chooser = VCSFileProxySupport.createFileChooser(roots[0].getParentFile());
        //final JFileChooser chooser = new AccessibleJFileChooser(NbBundle.getMessage(SvnProperties.class, "ACSD_Properties"));
        chooser.setDialogTitle(NbBundle.getMessage(SvnProperties.class, "CTL_Load_Value_Title"));
        chooser.setMultiSelectionEnabled(false);
        javax.swing.filechooser.FileFilter[] fileFilters = chooser.getChoosableFileFilters();
        for (int i = 0; i < fileFilters.length; i++) {
            javax.swing.filechooser.FileFilter fileFilter = fileFilters[i];
            chooser.removeChoosableFileFilter(fileFilter);
        }
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(SvnProperties.class, "MNE_LoadValue").charAt(0));
        chooser.setApproveButtonText(NbBundle.getMessage(SvnProperties.class, "CTL_LoadValue"));
        DialogDescriptor dd = new DialogDescriptor(chooser, NbBundle.getMessage(SvnProperties.class, "CTL_Load_Value_Title"));
        dd.setOptions(new Object[0]);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        chooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String state = e.getActionCommand();
                if (state.equals(JFileChooser.APPROVE_SELECTION)) {
                    VCSFileProxy source = VCSFileProxySupport.getSelectedFile(chooser);
                    if (VCSFileProxySupport.isFileContentText(source)) {
                        if (VCSFileProxySupport.canRead(source)) {
                            StringWriter sw = new StringWriter();
                            try {
                                org.netbeans.modules.versioning.util.Utils.copyStreamsCloseAll(sw, new InputStreamReader(source.getInputStream(false), "UTF-8")); //NOI18N
                                panel.txtAreaValue.setText(sw.toString());
                            } catch (IOException ex) {
                                Subversion.LOG.log(Level.SEVERE, null, ex);
                            }
                        }
                    } else {
                        handleBinaryFile(source);
                    }
                }
                dialog.dispose();
            }
        });
        dialog.setVisible(true);

    }

    protected void refreshProperties() {
        final SVNUrl repositoryUrl;
        final Context context = new Context(roots);
        try {
            repositoryUrl = ContextAction.getSvnUrl(context);
            // NB: repository can be null here
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(context, ex, true, true);
            return;
        }

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        try {
            support = new SvnProgressSupport(context.getFileSystem()) {
                HashMap<String, String> properties;
                @Override
                protected void perform() {
                    final Context context = new Context(roots);
                    try {
                        SvnClient client = Subversion.getInstance().getClient(false, context);
                        properties = new HashMap<>();
                        for (VCSFileProxy f : roots) {
                            ISVNStatus status = SvnUtils.getSingleStatus(client, f);
                            if (!status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
                                addProperties(f, client.getProperties(f));
                            }
                        }
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(context, ex, true, true);
                        return;
                    }
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            String[] propNames = new String[properties.size()];
                            SvnPropertiesNode[] svnProps = new SvnPropertiesNode[properties.size()];
                            int i = 0;
                            for (Map.Entry<String, String> e : properties.entrySet()) {
                                String name = e.getKey();
                                propNames[i] = name;
                                String value = e.getValue();
                                svnProps[i] = new SvnPropertiesNode(name, value);
                                ++i;
                            }
                            propTable.setNodes(svnProps);
                            panel.setExistingPropertyNames(propNames);
                        }
                    });
                }

                private void addProperties (VCSFileProxy file, ISVNProperty[] toAddProps) {
                    for (ISVNProperty prop : toAddProps) {
                        String propName = prop.getName();
                        String propValue;
                        if (SvnUtils.isBinary(prop.getData())) {
                            propValue = org.openide.util.NbBundle.getMessage(SvnProperties.class, "Binary_Content"); //NOI18N
                        } else {
                            String tmp = prop.getValue();
                            propValue = tmp != null ? tmp : ""; //NOI18N
                        }
                        String existingValue = properties.get(propName);
                        if (existingValue != null && !existingValue.equals(propValue)) {
                            propValue = org.openide.util.NbBundle.getMessage(SvnProperties.class, "SvnProperties.VariousValues"); //NOI18N"
                        }
                        properties.put(propName, propValue);
                        Set<VCSFileProxy> filesPerProp = filesPerProperty.get(propName);
                        if (filesPerProp == null) {
                            filesPerProp = new HashSet<>();
                            filesPerProperty.put(propName, filesPerProp);
                        }
                        filesPerProp.add(file);
                    }
                }
            };
            support.start(rp, repositoryUrl, org.openide.util.NbBundle.getMessage(SvnProperties.class, "LBL_Properties_Progress"));
        } finally {
            support = null;
        }
    }

    private void setProperties() {
        final SVNUrl repositoryUrl;
        final Context context = new Context(roots);
        try {
            repositoryUrl = ContextAction.getSvnUrl(context);
            // NB: repository can be null here
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(context, ex, true, true);
            return;
        }

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        try {
            support = new SvnProgressSupport(context.getFileSystem()) {
                ISVNProperty[] isvnProps;
                @Override
                protected void perform() {
                    SvnClient client;
                    final Context context = new Context(roots);
                    try {
                        client = Subversion.getInstance().getClient(false, context);
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(context, ex, true, true);
                        return;
                    }
                    boolean recursively = panel.cbxRecursively.isSelected();
                    Set<VCSFileProxy> toRefresh = new HashSet<>();
                    try {
                        String propName = getPropertyName();
                        for (VCSFileProxy root : getAllowedFiles(propName, recursively)) {
                            addFile(client, root, recursively);
                            if (isLoadedFromFile()) {
                                try {
                                    client.propertySet(root, propName, getLoadedValueFile(), recursively);
                                    toRefresh.add(root);
                                } catch (IOException ex) {
                                    Subversion.LOG.log(Level.SEVERE, null, ex);
                                    return;
                                }
                            } else {
                                client.propertySet(root, propName, getPropertyValue(), recursively);
                                toRefresh.add(root);
                            }
                        }
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(context, ex, true, true);
                        return;
                    } finally {
                        Subversion.getInstance().getStatusCache().refreshAsync(recursively, toRefresh.toArray(new VCSFileProxy[toRefresh.size()]));
                    }
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            panel.comboName.getEditor().setItem("");
                            panel.txtAreaValue.setText("");
                        }
                    });
                }
            };
            support.start(rp, repositoryUrl, org.openide.util.NbBundle.getMessage(SvnProperties.class, "LBL_Properties_Progress"));
        } finally {
            support = null;
        }
        refreshProperties();
    }

    private VCSFileProxy[] getAllowedFiles (String propertyName, boolean recursively) {
        List<VCSFileProxy> fileList = new LinkedList<>();
        for (VCSFileProxy root : roots) {
            boolean isFile = files.contains(root);
            if (!(isFile && DIR_ONLY_PROPERTIES.contains(propertyName) // do not set folder properties on files
                    || !isFile && !recursively && FILE_ONLY_PROPERTIES.contains(propertyName))) { // do not set file properties on folders
                fileList.add(root);
            }
        }
        return fileList.toArray(new VCSFileProxy[fileList.size()]);
    }

    private VCSFileProxy[] getFilesWithProperty (String propertyName) {
        Set<VCSFileProxy> filesWithProperty = filesPerProperty.get(propertyName);
        Set<VCSFileProxy> fileList = new HashSet<>();
        if (filesWithProperty != null) {
            fileList.addAll(filesWithProperty);
        }
        return fileList.toArray(new VCSFileProxy[fileList.size()]);
    }

    private void addFile(SvnClient client, VCSFileProxy file, boolean recursively) throws SVNClientException {
        if(SvnUtils.isPartOfSubversionMetadata(file)) {
            return;
        }
        ISVNStatus status = SvnUtils.getSingleStatus(client, file);
        if(status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
            boolean isDir = file.isDirectory();
            if (isDir) {
                client.addDirectory(file, false);
            } else {
                client.addFile(file);
            }
            if(recursively && isDir) {
                VCSFileProxy[] files = file.listFiles();
                if(files == null) {
                    return;
                }
                for (VCSFileProxy f : files) {
                    addFile(client, f, recursively);
                }
            }
        }
    }

    @Messages({
        "LBL_SvnProperties.RecursiveDelete.title=Recursively Delete Property",
        "# {0} - svn property name",
        "MSG_SvnProperties.RecursiveDelete.question=Do you want to recursively delete property {0}?"
    })
    private void removeProperties() {
        final SVNUrl repositoryUrl;
        final Context context = new Context(roots);
        try {
            repositoryUrl = ContextAction.getSvnUrl(context);
            // NB: repository can be null here
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(context, ex, true, true);
            return;
        }
        final int[] rows = propTable.getSelectedItems();
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        try {
            support = new SvnProgressSupport(context.getFileSystem()) {
                @Override
                protected void perform() {
                    SvnClient client;
                    try {
                        client = Subversion.getInstance().getClient(false, context);
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(context, ex, true, true);
                        return;
                    }

                    boolean recursively = panel.cbxRecursively.isSelected();
                    Set<VCSFileProxy> toRefresh = new HashSet<>();
                    try {
                        SvnPropertiesNode[] svnPropertiesNodes = propTable.getNodes();
                        List<SvnPropertiesNode> lstSvnPropertiesNodes = Arrays.asList(svnPropertiesNodes);
                        if (recursively && rows.length == 0) {
                            removePropertyRecursively(client, toRefresh);
                        }
                        for (int i = rows.length - 1; i >= 0; i--) {
                            String svnPropertyName = svnPropertiesNodes[propTable.getModelIndex(rows[i])].getName();
                            for (VCSFileProxy root : getFilesWithProperty(svnPropertyName)) {
                                client.propertyDel(root, svnPropertyName, recursively);
                                toRefresh.add(root);
                            }
                            try {
                                lstSvnPropertiesNodes.remove(svnPropertiesNodes[propTable.getModelIndex(rows[i])]);
                            } catch (UnsupportedOperationException e) {
                            }
                        }
                        final SvnPropertiesNode[] remainingNodes
                                = (SvnPropertiesNode[]) lstSvnPropertiesNodes.toArray();
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                propTable.setNodes(remainingNodes);

                                if (remainingNodes.length == 0) {
                                    panel.setExistingPropertyNames(new String[0]);
                                } else {
                                    String[] propNames = new String[remainingNodes.length];
                                    for (int i = 0; i < propNames.length; i++) {
                                        propNames[i] = remainingNodes[i].getName();
                                    }
                                    panel.setExistingPropertyNames(propNames);
                                }
                            }
                        });
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(context, ex, true, true);
                    } finally {
                        Subversion.getInstance().getStatusCache().refreshAsync(recursively, toRefresh.toArray(new VCSFileProxy[toRefresh.size()]));
                    }
                }

                private void removePropertyRecursively (SvnClient client, Set<VCSFileProxy> toRefresh) throws SVNClientException {
                    String propName = getPropertyName();
                    if (!propName.trim().isEmpty()) {
                        if (JOptionPane.showConfirmDialog(panel, 
                                Bundle.MSG_SvnProperties_RecursiveDelete_question(propName),
                                Bundle.LBL_SvnProperties_RecursiveDelete_title(),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                            for (VCSFileProxy root : roots) {
                                client.propertyDel(root, propName, true);
                                toRefresh.add(root);
                            }
                        }
                    }
                }
            };
            support.start(rp, repositoryUrl, org.openide.util.NbBundle.getMessage(SvnProperties.class, "LBL_Properties_Progress"));
        } finally {
            support = null;
        }
        refreshProperties();
    }

    public void propertyValueChanged() {
        assert isLoadedFromFile();
        panel.removePropertyValueChangeListener();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                panel.txtAreaValue.setText("");
            }
        });
        setLoadedFromFile(false);
    }

    public class TableMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent event) {
            //super.mouseClicked(arg0);
            if (event.getClickCount() == 2) {
                int[] rows = propTable.getSelectedItems();
                SvnPropertiesNode[] svnPropertiesNodes = propTable.getNodes();
                if (svnPropertiesNodes == null) {
                    return;
                }
                final String svnPropertyName = svnPropertiesNodes[propTable.getModelIndex(rows[0])].getName();
                final String svnPropertyValue = svnPropertiesNodes[propTable.getModelIndex(rows[0])].getValue();
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        panel.comboName.getEditor().setItem(svnPropertyName);
                        panel.txtAreaValue.setText(svnPropertyValue);
                    }
                });
            }
        }
}
}
