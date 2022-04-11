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

package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import org.netbeans.modules.cnd.debugger.common2.ui.ExecutableProjectPanel;
import org.netbeans.modules.cnd.debugger.common2.ui.AttachPanelImpl;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.Project;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.RecordListListener;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.RecordListEvent;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineCapability;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;

import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CustomizableHostList;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.HostListEditor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.picklist.DefaultPicklistModel;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.debugger.common2.APIAccessor;
import org.netbeans.modules.cnd.debugger.common2.utils.ProjectComboBoxSupport;
import org.netbeans.modules.cnd.debugger.common2.utils.ProjectComboBoxSupport.ProjectCBItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder;
import org.openide.util.RequestProcessor;

/**
 * Chooser for corefile, executable and project.
 *
 * SHOULD factor with very identical code in ExecutableProjectPanel!
 */

final class DebugCorePanel extends javax.swing.JPanel {
    private JButton actionButton = null;
    private String autoString = null;
    private boolean readonly;
    private boolean noproject;

    private static Project lastSelectedProject = null;
    private static EngineType lastSelectedEngine = null;
    private static String lastSelectedCorefile = null;
    
    private static final DefaultPicklistModel executablePickList = new DefaultPicklistModel(6);
    private static final DefaultPicklistModel corefilePickList = new DefaultPicklistModel(6);
    
    private final RequestProcessor RP = new RequestProcessor();
    private volatile ValidationWorkerCheckState currentState = new ValidationWorkerCheckState(Boolean.TRUE, 
            new ValidationResult(Boolean.FALSE, "DebugCorePanel.Validating", false));//NOI18N    
    private final ValidationWorker validationWorker = new ValidationWorker();    
    static final int VALIDATION_DELAY = 300;    
    private final AtomicBoolean coreFileBrowseDialogInvoked = new AtomicBoolean(false);
    private final AtomicBoolean execFileBrowseDialogInvoked = new AtomicBoolean(false);
    
    public DebugCorePanel(String corePath, JButton actionButton, boolean readonly, String host) {
	this.actionButton = actionButton;
	this.readonly = readonly;
	initialize(corePath, host);
    }
    
    private void initialize(String corePath, String host) {
        initComponents();	
	if (readonly) {
	    corefileComboBox.setEditable(false);
	    corefileBrowseButton.setEnabled(false);
	    guidanceTextArea.setText(Catalog.get("LOADCORE_GUIDANCETEXT2")); // NOI18N
	    Catalog.setAccessibleDescription(guidanceTextArea, 
		"LOADCORE_GUIDANCETEXT2"); // NOI18N
	}
	errorLabel.setForeground(javax.swing.UIManager.getColor("nb.errorForeground")); // NOI18N
	if (corePath == null) {
            corePath = lastSelectedCorefile;
        } else {
            corefilePickList.addElement(corePath);
        }
        
        corefileComboBox.getEditor().setItem(corePath);
	initGui();
        guidanceTextArea.setBackground(getBackground());
	setPreferredSize(new java.awt.Dimension(700, (int)getPreferredSize().getHeight()));

        corefileComboBox.setModel(new DefaultComboBoxModel(corefilePickList.getElementsDisplayName()));
	((JTextField)corefileComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(validationWorker);

	executableComboBox.setModel(new DefaultComboBoxModel(executablePickList.getElementsDisplayName()));
	((JTextField)executableComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(validationWorker);

        if (lastHostChoice == null) {
            ServerRecord defaultHost = ServerList.getDefaultRecord();
            if (defaultHost != null) {
                lastHostChoice = ExecutionEnvironmentFactory.toUniqueID(defaultHost.getExecutionEnvironment());
            }
        }
	initRemoteHost();
	initEngine();
	adjustAutoCore();

        projectComboBox.addItemListener(validationWorker);
        validationWorker.actionPerformed(null);
        
        actionButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                lastSelectedProject = getSelectedProject();
                lastSelectedEngine = getEngine();
                lastSelectedCorefile = getCorefilePath();
                lastHostChoice = getHostName();
                
                executablePickList.addElement(getExecutablePath());
                corefilePickList.addElement(getCorefilePath());
            }
        });    
    }

    @Override
    public void removeNotify() {
        super.removeNotify(); //To change body of generated methods, choose Tools | Templates.
        validationWorker.shutdownExecutor();
    }
    
    

    public EngineType getEngine() {
        Object selected = engineComboBox.getSelectedItem();
        Collection<EngineType> engineTypes = EngineTypeManager.getEngineTypes(false);
        for (EngineType engineType : engineTypes) {
            if (engineType.getDisplayName().equals(selected)) {
                return engineType;
            }
        }
        assert false : "selected object doesn't have associated engine type " + selected;
        return null;
    }

    private int lookupAutoEntry() {
	int count = executableComboBox.getItemCount();
	for (int i = 0; i < count; i++) {
	    if (((String)executableComboBox.getItemAt(i)).equals(Catalog.get("AutoCoreExe"))) // NOI18N
		return i;
	}
	return -1;
    }

    /**
     *
     * Add or remove a "Choose from corefile" item to the executableComboBox.
     */
    private void adjustAutoCore() {
	String exec = getExecutablePath();
        EngineDescriptor descriptor = new EngineDescriptor(getEngine());
	if (!descriptor.hasCapability(EngineCapability.DERIVE_EXECUTABLE)) {
            autoString = null;
	    int i = lookupAutoEntry(); // look up <from core> item
	    if (executableComboBox.getItemCount() > 0 && i != -1) {
		executableComboBox.removeItemAt(i); // remove <from core> item
//		executableComboBox.insertItemAt(" ", i);
	    }

	    if (exec.equals(Catalog.get("AutoCoreExe"))) { // NOI18N
                exec = "";
            }
	} else {
            autoString = Catalog.get("AutoCoreExe"); // NOI18N
	    int i = lookupAutoEntry(); // look up <from core> item
	    if (i == -1) {
		executableComboBox.addItem(Catalog.get("AutoCoreExe")); // NOI18N
	    }
	    if (exec.isEmpty()) {
                exec = autoString;
            }
        }
        setExecutable(exec);
    }

    private void setExecutable(String exec) {
        ((JTextField)executableComboBox.getEditor().getEditorComponent()).setText(exec);
    }
    
    private void initEngine() {
        ActionListener engineComboBoxActionListener = engineComboBox.getActionListeners()[0];
        engineComboBox.removeActionListener(engineComboBoxActionListener);
        engineComboBox.removeAllItems();
        Collection<EngineType> engineTypes = EngineTypeManager.getEngineTypes(false);
        for (EngineType engineType : engineTypes) {
            engineComboBox.addItem(engineType.getDisplayName());
            if (engineType.equals(lastSelectedEngine)) {
                engineComboBox.setSelectedItem(engineType.getDisplayName());
            }
        }
        engineComboBox.addActionListener(engineComboBoxActionListener);
    }

    private void initRemoteHost() {
        updateRemoteHostList();

	if (NativeDebuggerManager.isStandalone()) {
	    CustomizableHostList hostlist = NativeDebuggerManager.get().getHostList();

	    // listen to host host list model
	    if (hostlist != null) {
		hostlist.addRecordListListener(new RecordListListener() {
                    @Override
		    public void contentsChanged(RecordListEvent e) {
			if (e.getHostName() != null)
			    lastHostChoice = e.getHostName();
			updateRemoteHostList();
			// setDirty(true);
		    }
		} );
	    }
	} else {
	}

        // Listen to hostComboBox events
        // but only after we've initialized all the above.
        hostComboBox.addActionListener(validationWorker);
    }    

    

    /**
     * Refresh hostComboBox with new remote host list.
     */
    private void updateRemoteHostList() {
        AttachPanelImpl.fillHostsCombo(hostComboBox);
        // current value
        AttachPanelImpl.setHostChoice(lastHostChoice, hostComboBox);
    }

    public String getCorefilePath() {
	return ((JTextField)corefileComboBox.getEditor().getEditorComponent()).getText();
    }

    /**
     * Return hostComboBox's current selection.
     * May return null if the current selection is not in the remote host DB.
     */
    public String getHostName() {
        return hostComboBox.getSelectedItem().toString();
    }

    public String getExecutablePath() {
	return ((JTextField)executableComboBox.getEditor().getEditorComponent()).getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

	Catalog.setAccessibleDescription(this, "ACSD_DebugCoreFile"); // NOI18N

        java.awt.GridBagConstraints gridBagConstraints;

        guidanceTextArea = new javax.swing.JTextArea();
        corefileLabel = new javax.swing.JLabel();
        corefileComboBox = new javax.swing.JComboBox();
        corefileBrowseButton = new javax.swing.JButton();

        executableLabel = new javax.swing.JLabel();
        executableComboBox = new javax.swing.JComboBox();
        executableBrowseButton = new javax.swing.JButton();

        projectLabel = new javax.swing.JLabel();
        projectComboBox = new javax.swing.JComboBox();

        engineLabel = new javax.swing.JLabel();
        engineComboBox = new javax.swing.JComboBox();

        hostComboBox = new javax.swing.JComboBox();
        hostLabel = new javax.swing.JLabel();
        hostsButton = new javax.swing.JButton();


        fill = new javax.swing.JPanel();
        errorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

	int gridy = 0;

	Catalog.setAccessibleName(guidanceTextArea, "ACSN_Guidance"); // NOI18N
        guidanceTextArea.setEditable(false);
        guidanceTextArea.setLineWrap(true);
        guidanceTextArea.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("LOADCORE_GUIDANCETEXT1")); // NOI18N
	Catalog.setAccessibleDescription(guidanceTextArea, "LOADCORE_GUIDANCETEXT1"); // NOI18N
        guidanceTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy++;
        //gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(guidanceTextArea, gridBagConstraints);

        hostLabel.setText(Catalog.get("HOST_LBL")); //NOI18N
        hostLabel.setLabelFor(hostComboBox);
	hostLabel.setDisplayedMnemonic(
	           Catalog.getMnemonic("MNEM_Host")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 13, 8, 0);
	add(hostLabel, gridBagConstraints);

        hostComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 0);
	add(hostComboBox, gridBagConstraints);

	if (!NativeDebuggerManager.isStandalone())
	    hostsButton.setEnabled(false);      // IZ 147543

        hostsButton.setText(Catalog.get("TITLE_Hosts")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 12);
	add(hostsButton, gridBagConstraints);

        hostsButton.setMnemonic(Catalog.
                getMnemonic("MNEM_Hosts")); // NOI18N
        Catalog.setAccessibleDescription(hostsButton, 
                "ACSD_EditHosts"); // NOI18N

        hostsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostsButtonActionPerformed(evt);
            }
        });

        engineLabel.setLabelFor(engineComboBox);
        engineLabel.setDisplayedMnemonic(Catalog.
            getMnemonic("MNEM_Engine")); // NOI18N
        Catalog.setAccessibleDescription(engineComboBox,
            "ACSD_Engine"); // NOI18N
        engineLabel.setText(Catalog.get("ASSOCIATED_ENGINE_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 8, 0);
	/* CR 7013811
        String engine = System.getProperty("debug.engine");
        if (engine != null && engine.equals("on")) // NOI18N
	 *
	 */
	add(engineLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 6, 12);
        // CR 7013811 if (engine != null && engine.equals("on")) // NOI18N
	add(engineComboBox, gridBagConstraints);

        engineComboBox.addActionListener(validationWorker);

        corefileLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("LOADCORE_COREFILE_MN").charAt(0));
        corefileLabel.setLabelFor(corefileComboBox);
        corefileLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("LOADCORE_COREFILE_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        //gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 8, 0);
        add(corefileLabel, gridBagConstraints);

	Catalog.setAccessibleDescription(corefileComboBox,
	    "ACSD_Corefile"); // NOI18N
        corefileComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy;
        //gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 0);
        add(corefileComboBox, gridBagConstraints);

	Catalog.setAccessibleDescription(corefileBrowseButton, 
	    "ACSD_CorefileBrowse"); // NOI18N
        corefileBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("LOADCORE_COREFILEBROWSE_BUTTON_MN").charAt(0));
        corefileBrowseButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("BROWSE_BUTTON_TXT"));
        corefileBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                corefileBrowseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy++;
        //gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 12);
        add(corefileBrowseButton, gridBagConstraints);

        executableLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("EXECUTABLE_MN").charAt(0));
        executableLabel.setLabelFor(executableComboBox);
        executableLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("EXECUTABLE_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        //gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 13, 8, 0);
        add(executableLabel, gridBagConstraints);

        executableComboBox.setEditable(true);
        executableComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("ProgramPathname"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy;
        //gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 0);
        add(executableComboBox, gridBagConstraints);

	Catalog.setAccessibleDescription(executableBrowseButton, 
	    "ACSD_ExecutableBrowse"); // NOI18N
        executableBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("EXECUTABLEBROWSE_BUTTON_MN").charAt(0));
        executableBrowseButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("BROWSE_BUTTON_TXT"));
        executableBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executableBrowseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy++;
        //gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 12);
        add(executableBrowseButton, gridBagConstraints);

        projectLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("ASSOCIATED_PROJECT_MN").charAt(0));
        projectLabel.setLabelFor(projectComboBox);
	Catalog.setAccessibleDescription(projectComboBox,
	    "ACSD_Project"); // NOI18N
        projectLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("ASSOCIATED_PROJECT_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        //gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
	if (!NativeDebuggerManager.isStandalone() && !NativeDebuggerManager.isPL())
	    add(projectLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy++;
        //gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 6, 12);
	if (!NativeDebuggerManager.isStandalone() && !NativeDebuggerManager.isPL())
	    add(projectComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy++;
        //gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(fill, gridBagConstraints);

        errorLabel.setText(" "); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy++;
        //gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(errorLabel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void executableBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executableBrowseButtonActionPerformed
        if (execFileBrowseDialogInvoked.getAndSet(true)) {
            //we already proceed
            return;
        }

        //first cancel validation
        validationWorker.cancel();
        final String hostname = (String)hostComboBox.getSelectedItem();
        String startFolder = getExecutablePath();
        if (startFolder.isEmpty()) {
            startFolder = getCorefilePath();
            if (startFolder.isEmpty()) {
                startFolder = System.getProperty("user.home");
            }
        }
        if (startFolder.equals(autoString)) {
	    startFolder = getCorefilePath();
        }
        final String startF = startFolder;
        RP.post(new Runnable() {
            @Override
            public void run() {
                Host host = Host.byName(hostname);
                final ExecutionEnvironment exEnv = host.executionEnvironment();
                if (!ConnectionManager.getInstance().connect(exEnv)) {
                    return;
                }

                FileChooserBuilder fcb = new FileChooserBuilder(exEnv);
                final JFileChooser fileChooser = fcb.createFileChooser(startF);
                fileChooser.setDialogTitle(getString("SelectExecutable"));
                fileChooser.setApproveButtonText(getString("CHOOSER_BUTTON"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.addChoosableFileFilter(FileFilterFactory.getElfExecutableFileFilter());
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        int ret = fileChooser.showOpenDialog(DebugCorePanel.this);
                        execFileBrowseDialogInvoked.set(false);
                        if (ret == JFileChooser.CANCEL_OPTION) {
                            return;
                        }
                        ((JTextField) executableComboBox.getEditor().getEditorComponent()).setText(fileChooser.getSelectedFile().getPath());
                    }
                });
            }
        });
    }//GEN-LAST:event_executableBrowseButtonActionPerformed

    private void hostsButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // It's effect will come back to us via
        // contentsChanged(RecordListEvent)
        HostListEditor editor = new HostListEditor();
        editor.showDialog(this);
    }

    private void corefileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_corefileBrowseButtonActionPerformed
        if (coreFileBrowseDialogInvoked.getAndSet(true)) {
            //we already proceed
            return;
        }
        validationWorker.cancel();
        final String hostname = (String)hostComboBox.getSelectedItem();
        String startFolder = getCorefilePath();
        if (startFolder.isEmpty()) {
            startFolder = System.getProperty("user.home");
        }
        final String startF = startFolder;
        RP.post(new Runnable() {
            @Override
            public void run() {
                Host host = Host.byName(hostname);
                final ExecutionEnvironment exEnv = host.executionEnvironment();
                if (!ConnectionManager.getInstance().connect(exEnv)) {
                    return;
                }

                FileChooserBuilder fcb = new FileChooserBuilder(exEnv);
                final JFileChooser fileChooser = fcb.createFileChooser(startF);
                fileChooser.setDialogTitle(getString("CorefileChooser"));
                fileChooser.setApproveButtonText(getString("CHOOSER_BUTTON"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.addChoosableFileFilter(FileFilterFactory.getCoreFileFilter());
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        int ret = fileChooser.showOpenDialog(DebugCorePanel.this);
                        coreFileBrowseDialogInvoked.set(false);
                        if (ret == JFileChooser.CANCEL_OPTION) {
                            return;
                        }
                        corefileComboBox.getEditor().setItem(fileChooser.getSelectedFile().getPath());
                    }
                });
            }
        });
    }//GEN-LAST:event_corefileBrowseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton corefileBrowseButton;
    private javax.swing.JLabel corefileLabel;
    private javax.swing.JComboBox corefileComboBox;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton executableBrowseButton;
    private javax.swing.JComboBox executableComboBox;
    private javax.swing.JLabel executableLabel;
    private javax.swing.JPanel fill;
    private javax.swing.JTextArea guidanceTextArea;
    private javax.swing.JComboBox projectComboBox;
    private javax.swing.JLabel projectLabel;

    private javax.swing.JComboBox engineComboBox;
    private javax.swing.JLabel engineLabel;

    private static String lastHostChoice;
    private javax.swing.JComboBox hostComboBox;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JButton hostsButton;


    // End of variables declaration//GEN-END:variables

    private void initGui() {
	projectComboBox.removeAllItems();
        // fake items
	projectComboBox.addItem(getString("NO_PROJECT")); // always first
	projectComboBox.addItem(getString("NEW_PROJECT")); // always first
        
        ProjectComboBoxSupport.fillProjectsCombo(projectComboBox, lastSelectedProject);
    }

    private ValidationResult validateCorefilePath(String corePath) {
	corePath = corePath.trim();
	if (corePath.length() == 0) {
	    return new ValidationResult(Boolean.FALSE, "ERROR_CORE_NOT_SPECIFIED", true); //NOI18N
	}

        Host host = Host.byName(getHostName());
        final ExecutionEnvironment exEnv = host.executionEnvironment();

        try {
            if (!HostInfoUtils.fileExists(exEnv, corePath)) {
                return new ValidationResult(Boolean.FALSE, "ERROR_CORE_DONTEXIST", true); //NOI18N
            }
        } catch (Exception e) {
            return new ValidationResult(Boolean.FALSE, "ERROR_CORE_DONTEXIST", true); //NOI18N;
        }

        // Some more validation locally
        if (exEnv.isLocal()) {
            File coreFile = new File(corePath);
            if (coreFile.isDirectory()) {
                return new ValidationResult(Boolean.FALSE, "ERROR_NOTACOREFILE", true); //NOI18N
            }
            /* fromFile Deprecated replaced by toFileObject
            FileObject fo[] = FileUtil.fromFile(coreFile);
            if (fo == null || fo.length == 0) {
                setError("ERROR_CORE_DONTEXIST", true);
                return false;
            }
            DataObject dataObject = null;
            try {
                dataObject = DataObject.find(fo[0]);
            }
            catch (Exception e) {
                setError("ERROR_CORE_DONTEXIST", true);
                return false;
            }
            */

            FileObject fo = FileUtil.toFileObject(coreFile);
            if (fo == null) {
                return new ValidationResult(Boolean.FALSE, "ERROR_CORE_DONTEXIST", true); //NOI18N
            }
            DataObject dataObject = null;
            try {
                dataObject = DataObject.find(fo);
            }
            catch (Exception e) {
                return new ValidationResult(Boolean.FALSE, "ERROR_CORE_DONTEXIST", true); //NOI18N                
            }
            if (!MIMENames.ELF_CORE_MIME_TYPE.equals(IpeUtils.getMime(dataObject))) {
                return new ValidationResult(Boolean.FALSE, "ERROR_NOTACOREFILE", true); //NOI18N                
            }
        }

	return new ValidationResult(Boolean.TRUE, null, false);
    }

    private ValidationResult validateExecutablePath(String exePath) {
	exePath = exePath.trim();
	if (exePath.equals(autoString)) {
            /* 6966340
	    if (!matchProject(pName)) 
	        setProject();
             *
             */
	    return new ValidationResult(Boolean.TRUE, null, false);
	}

        Host host = Host.byName(getHostName());
        final ExecutionEnvironment exEnv = host.executionEnvironment();

        try {
            if (!HostInfoUtils.fileExists(exEnv, exePath)) {
                EngineDescriptor descriptor = new EngineDescriptor(getEngine());
                if (descriptor.hasCapability(EngineCapability.DERIVE_EXECUTABLE)) {
                    return new ValidationResult(Boolean.FALSE, "ERROR_DONTEXIST", true); //NOI18N                
                } else {
                    return new ValidationResult(Boolean.FALSE, "DBG_ERROR_DONTEXIST", false); //NOI18N                
                }
            }
        } catch (Exception e) {
            return new ValidationResult(Boolean.FALSE, null, false); //NOI18N                
        }

        // more validation locally
        if (exEnv.isLocal()) {
            File exeFile = new File(exePath);
            if (exeFile.isDirectory()) {
                return new ValidationResult(Boolean.FALSE, "ERROR_NOTAEXEFILE", true); //NOI18N                
            }

            FileObject fo = FileUtil.toFileObject(exeFile);
            if (fo == null) {
                return new ValidationResult(Boolean.FALSE, "ERROR_NOTAEXEFILE", true); //NOI18N                
            }
            DataObject dataObject = null;
            try {
                dataObject = DataObject.find(fo);
            }
            catch (Exception e) {
                return new ValidationResult(Boolean.FALSE, "ERROR_DONTEXIST", true); //NOI18N                
            }
            if (!MIMENames.isBinary(IpeUtils.getMime(dataObject))) {
                return new ValidationResult(Boolean.FALSE, "ERROR_NOTAEXEFILE", true); //NOI18N                
            }
        }
	
	return new ValidationResult(Boolean.TRUE, null, false);
    }

    private boolean matchProject(String executable) {
	// <no project> is the default for  <from core>
	if (executable.equals(autoString)) {
 //           projectComboBox.setEnabled(false);
	    projectComboBox.setSelectedIndex(0);
	    return true;
	}
        projectComboBox.setEnabled(true);
	// match opened Project first
	for (int i = 0; i < projectComboBox.getItemCount(); i++) {
	    if (executable.equalsIgnoreCase(
                projectComboBox.getItemAt(i).toString())) {
	        projectComboBox.setSelectedIndex(i);
		return true;
	    }
	}
	return false;
    }
    
    private boolean matchExecutable(Project p) {
        if (p == null) {
            adjustAutoCore();
            return true;
        }
        ProjectInformation pi = ProjectUtils.getInformation(p);
        String displayName = pi.getDisplayName();
        if (displayName != null && !displayName.trim().isEmpty()) {
            for (int i = 0; i < executableComboBox.getItemCount(); i++) {
                if (displayName.equalsIgnoreCase(
                        CndPathUtilities.getBaseName(executableComboBox.getItemAt(i).toString()))) {
                    executableComboBox.setSelectedIndex(i);
                    return true;
                }
            }
        }
        return false;
    }
    
    private void setError() {
        ValidationResult validationResult = this.currentState.validationResult;
        if (validationResult.isValid) {
            clearError();
            return;
        }
        setError(validationResult.msgError, validationResult.disable);
    }
    

    private void setError(final String errorMsg, final boolean disable) {        
        errorLabel.setText(getString(errorMsg));
        if (disable) {
            projectComboBox.setEnabled(false);
        }
        actionButton.setEnabled(false);
    }

    private void clearError() {
        errorLabel.setText(" "); // NOI18N
        projectComboBox.setEnabled(true);
        actionButton.setEnabled(true);
    }

    
    public Project getSelectedProject() {
        Object selectedItem = projectComboBox.getSelectedItem();
        if (selectedItem instanceof ProjectCBItem) {
            noproject = false;
            return ((ProjectCBItem)selectedItem).getProject();
        }
        // set noproject if NO_PROJECT is selected
        noproject = (projectComboBox.getSelectedIndex() == 0);
        return null;
    }

    /** Look up i18n strings here */
    private ResourceBundle bundle;
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(DebugCorePanel.class);
	}
        try{
            return bundle.getString(s);
        }catch (Exception e) {
            return s;
        }
    }

    public boolean asynchronous() {
	return false;
    }

    public boolean getNoProject() {
	return noproject;
    }
    
    private static final class ValidationParams {

        private String hostName;
        private final Project project;
        private final String corePath;
        private final String execPath;
        private long eventID;

        ValidationParams(String hostName, Project project, String execPath, String corePath) {
            this.hostName = hostName;
            this.project = project;
            this.execPath = execPath;
            this.corePath = corePath;
        }

        void setRequestID(long eventID) {
            this.eventID = eventID;
        }
    }

    private static final class ValidationWorkerCheckState {
        // null - all is fine
        // TRUE - check in progress
        // FALSE - check failed

        private final Boolean checking;
        private final ValidationResult validationResult;

        private ValidationWorkerCheckState(Boolean checking, ValidationResult validationResult) {
            this.checking = checking;
            this.validationResult = validationResult;
        }
    }

    private static class ValidationResult {

        private Boolean isValid;
        private String msgError;
        private boolean disable;

        ValidationResult(Boolean isValid, String msgError, boolean disable) {
            this.isValid = isValid;
            this.msgError = msgError;
            this.disable = disable;
        }
    }

    private final class ValidationWorker extends AttachPanelImpl.AnyChangeDocumentListener implements Runnable, ActionListener, ItemListener {

        private final Object validationExecutorLock = new Object();
        private final ScheduledExecutorService validationExecutor;
        private ScheduledFuture<?> validationTask;
        private long lastEventID = 0;
        private ValidationWorkerCheckState lastCheck = null;
        private ValidationParams validationParams;

        ValidationWorker() {
            validationExecutor = Executors.newScheduledThreadPool(1);
        }

        @Override
        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                ValidationWorkerCheckState curStatus = lastCheck;
                currentState = curStatus;

                ValidationResult validationResult = curStatus == null ? null : curStatus.validationResult;
                if (curStatus == null || curStatus.checking == null) {
                    if (validationResult == null) {
                        validationResult = new ValidationResult(Boolean.TRUE, "DebugCorePanel.Validating", true);//NOI18N
                    } else {
                        validationResult = new ValidationResult(Boolean.TRUE, validationResult.msgError, validationResult.disable);
                    }
                    currentState = new ValidationWorkerCheckState(null, validationResult);
                }
                //TODO: show error
                setError();
            } else {
                recalculateValidationParams();
                //check if we are not cancelled already
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    //log.log(Level.FINEST, "Interrupted (1) check for {0}", path);
                }
                ValidationResult result = validate();
                if (Thread.interrupted()) {
                    return;
                }
                lastCheck = new ValidationWorkerCheckState(result.isValid ? null : Boolean.FALSE, result);
                SwingUtilities.invokeLater(this);

            }
        }

        private void recalculateValidationParams() {
        }

        public ValidationResult validate() {
            if (validationParams.eventID < lastEventID) {
                return new ValidationResult(Boolean.FALSE, null, false);
            }
            final String hostName = validationParams.hostName;
            final String corePath = validationParams.corePath;
            final String execPath = validationParams.execPath;
            final Project project = validationParams.project;

            if (Thread.interrupted()) {
                return new ValidationResult(Boolean.FALSE, null, false);
            }
            //check hostname    
            if (!APIAccessor.get().syncValidate(hostName)) {
                return new ValidationResult(Boolean.FALSE, "DebugCorePanel.HOST_IS_NOT_VALID", true);//NOI18N
            }
            
            if (Thread.interrupted()) {
                return new ValidationResult(Boolean.FALSE, null, false);
            }
            final ValidationResult coreFileValidationResult = validateCorefilePath(corePath);
            if (!coreFileValidationResult.isValid) {
                return coreFileValidationResult;
            }
            if (Thread.interrupted()) {
                return new ValidationResult(Boolean.FALSE, null, false);
            }
            final ValidationResult execPathValidationResult = validateExecutablePath(execPath);
            if (!execPathValidationResult.isValid) {
                return execPathValidationResult;
            }

            if (Thread.interrupted()) {
                return new ValidationResult(Boolean.FALSE, null, false);
            }
            // Validate that project toolchain family is the same as debugger type

            if (project != null) {
                final MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project);
                if (conf != null) {
                    EngineType projectDebuggerType = NativeDebuggerManager.debuggerType(conf);
                    if (getEngine() != projectDebuggerType) {
                        return new ValidationResult(Boolean.FALSE, "ERROR_WRONG_FAMILY", false);//NOI18N
                    }
                }
            }
            return new ValidationResult(Boolean.TRUE, null, false);
        }


        private void handleProjectParamsChanges() {
            //will handle next event
            if (validationParams != null) {
                validationParams.setRequestID(++lastEventID);
            }
            ValidationResult validationResult = new ValidationResult(Boolean.FALSE, 
                    "DebugCorePanel.Validating", false);//NOI18N
            currentState = new ValidationWorkerCheckState(Boolean.TRUE, validationResult);
            setError();
            synchronized (validationExecutorLock) {
                if (validationTask != null) {
                    validationTask.cancel(true);
                }
                validationTask = validationExecutor.schedule(this,
                        VALIDATION_DELAY, TimeUnit.MILLISECONDS);
            }
        }

        void cancel() {
            synchronized (validationExecutorLock) {
                if (validationTask != null) {
                    validationTask.cancel(true);
                }
            }
        }
        
        void shutdownExecutor() {
            synchronized (validationExecutorLock) {
                if (validationTask != null) {
                    validationTask.cancel(true);
                }                
                validationExecutor.shutdown();
            }
        }

        private void updateValidationParams() {
            validationParams = new ValidationParams(getHostName(), getSelectedProject(), getExecutablePath(), getCorefilePath());
            handleProjectParamsChanges();
        }

        @Override
        public void itemStateChanged(ItemEvent evt) {   // project changed
            if (evt.getStateChange() == ItemEvent.SELECTED) {
                matchExecutable(getSelectedProject());
                updateValidationParams();
            }
        }

        @Override
        protected void documentChanged(DocumentEvent e) {// exec changed
            String pName = CndPathUtilities.getBaseName(getExecutablePath());
            matchProject(pName);
            updateValidationParams();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e != null && e.getSource() == engineComboBox) {
                adjustAutoCore();
            }
            updateValidationParams();
        }
    }
}
