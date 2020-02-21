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
package org.netbeans.modules.cnd.debugger.common2.ui.processlist;


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ui.ServerListUI;
import org.netbeans.modules.cnd.debugger.common2.debugger.DialogManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.AttachPanel;
import org.netbeans.modules.cnd.debugger.common2.ui.ExecutableProjectPanel;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.ProjectSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfoDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CndRemote;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CustomizableHostList;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.HostListEditor;
import org.netbeans.modules.cnd.debugger.common2.debugger.spi.UserAttachAction;
import org.netbeans.modules.cnd.debugger.common2.utils.FileMapper;
import org.netbeans.modules.cnd.debugger.common2.utils.ProcessListSupport;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.RecordListEvent;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.RecordListListener;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.spi.debugger.ui.Controller;
import org.netbeans.spi.debugger.ui.PersistentController;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * Top component which displays something.
 */

public final class AttachToProcessTopComponent extends AttachPanel{
    
//        implements ChangeListener
//        , ConnectionListener {
    
    /* Keep one instance so combobox choices are persistant */
    private static final  Map<EngineType, AttachToProcessTopComponent> cacheInstance = 
            new HashMap<>();
    private javax.swing.JPanel headingPanel;
    private javax.swing.JComboBox hostCombo;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JButton hostsButton;
    private static String lastHostChoice;
    private final AttachController controller = new AttachController();
    private static final RequestProcessor RP  = new RequestProcessor(AttachToProcessTopComponent.class.getName(), 1);
    
 // settings persistence
    // Saved in:
    // <userdir>/config/Preferences/
    //	org/netbeans/modules/cnd/debugger/common2/attach_filters.properties
    private static final Preferences prefs =
            NbPreferences.forModule(AttachToProcessTopComponent.class);
    private static final Preferences filterPrefs =
            prefs.node("attach_filters");		//NOI18N
    
// <From Process>
    private EngineDescriptor engine;

//    /** Don't allow filter unless filterReady is true */
//    private boolean filterReady = false;    
    
    private DialogManager dialogManager = null;
    private JButton okButton;
    private ExecutableProjectPanel executableProjectPanel;


    private static AttachToProcessTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "AttachToProcessTopComponent";//NOI18N
    private ExecutionEnvironment currentEnv = null;
    
    private ProcessListPanel processListPanel;

    private AttachToProcessTopComponent(EngineType debuggerType) {
        this.engine = new EngineDescriptor(debuggerType);
        initComponents(this.engine);
//        setName(NbBundle.getMessage(AttachToProcessTopComponent.class, "CTL_AttachToProcessTopComponent"));
//        setToolTipText(NbBundle.getMessage(AttachToProcessTopComponent.class, "HINT_AttachToProcessTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

    }
    
    public final Controller getController() {
        return controller;
    }
    
   /**
     * Return hostCombo's current selection.
     * May return null if the current selectionis not in the remote host DB.
     */
    private String getHostName() {
        return hostCombo.getSelectedItem().toString();
    }
    

    private void initComponents(EngineDescriptor debuggerType) {
        hostLabel = new javax.swing.JLabel();
        hostCombo = new javax.swing.JComboBox();
        hostsButton = new javax.swing.JButton();

       
        setLayout(new BorderLayout());
        hostLabel.setText(Catalog.get("AttachProcDiag_HostColon")); // NOI18N
        hostLabel.setDisplayedMnemonic(
                Catalog.getMnemonic("MNEM_AttachProcDiag_Host")); // NOI18N

        hostLabel.setLabelFor(hostCombo);
        hostCombo.setToolTipText(Catalog.get("HostName")); //NOI18N
        hostCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                String ac = evt.getActionCommand();
                if ((ac != null) && ac.equals("comboBoxChanged")) { // NOI18N
                    JComboBox cb = (JComboBox) evt.getSource();
                    if (cb != null && cb.getItemCount() > 0) {
                        final String hostName = getHostName();
                        if (hostName != null) {
                            //go out of UI thread here and then back to UI
                            processListPanel.setLoading();
                            RP.post(new Runnable() {
                                @Override
                                public void run() {
                                    final ExecutionEnvironment executionEnvironment = Host.byName(hostName).executionEnvironment();
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateContent(executionEnvironment);
                                        }
                                    });
                                }
                            });
                                                        
                        }
                    }
                }
            }
        });
        
//        if (!NativeDebuggerManager.isStandalone()) {
//            hostsButton.setEnabled(false);	// IZ 147543
//        }
        hostsButton.setText(Catalog.get("TITLE_Hosts"));	// NOI18N
        hostsButton.setMnemonic(Catalog.getMnemonic("MNEM_Hosts"));// NOI18N
        Catalog.setAccessibleDescription(hostsButton,
                "ACSD_EditHosts");		// NOI18N
        hostsButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostsButtonActionPerformed(evt);
            }
        });

        headingPanel = new javax.swing.JPanel();
                
        headingPanel.setLayout(new java.awt.GridBagLayout());
        GridBagConstraints gridBagConstraints;
        int gridy = 0;

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        headingPanel.add(hostLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
        gridBagConstraints.weightx = 1.0;
        headingPanel.add(hostCombo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;//gridy++;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
        headingPanel.add(hostsButton, gridBagConstraints);

       
        add(headingPanel, BorderLayout.NORTH);
        processListPanel = new ProcessListPanel();
        processListPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                executableProjectPanel.setExecutablePath(getHostName(), 
                        processListPanel.getSelectedInfo()  == null ? null : 
                        processListPanel.getSelectedInfo().getExecutable());
            }
        });
        add(processListPanel, BorderLayout.CENTER);

        executableProjectPanel = new ExecutableProjectPanel(okButton, debuggerType, false);
        add(executableProjectPanel, BorderLayout.SOUTH);

    }
        
    public synchronized static AttachToProcessTopComponent getInstance(DialogManager dialogManager,
            JButton okButton, EngineType debuggerType) {
        AttachToProcessTopComponent panel = cacheInstance.get(debuggerType);
        if (panel == null) {
            panel = new AttachToProcessTopComponent(debuggerType);
            panel.initializeNew();
            cacheInstance.put(debuggerType, panel);
        } else {
            panel.initialize(dialogManager, okButton);
        }
        return panel;
    }

    private void initializeNew() {
        lastHostChoice = ServerList.getDefaultRecord().getDisplayName();
        initRemoteHost();
        //lastFilter = (String) filterCombo.getSelectedItem();
    }

    private void initialize(DialogManager dialogManager, JButton okButton) {
        this.dialogManager = dialogManager;
        this.okButton = okButton;

      //  filterCombo.setSelectedItem(lastFilter);
        executableProjectPanel.initGui();

        if (!NativeDebuggerManager.isStandalone()) {
            // ServerList has no change notifier so we resync everytime.
            updateRemoteHostList();
        }
        updateContent(Host.byName(hostCombo.getSelectedItem()  + "").executionEnvironment());
       //refreshProcesses(null, true);
        
    }

    private void initRemoteHost() {
        updateRemoteHostList();

        if (NativeDebuggerManager.isStandalone()) {
            CustomizableHostList hostlist = NativeDebuggerManager.get().getHostList();

            // listen to host list model
            if (hostlist != null) {
                hostlist.addRecordListListener(new RecordListListener() {

                    @Override
                    public void contentsChanged(RecordListEvent e) {
                        updateRemoteHostList();
                        String hostName = e.getHostName();
                        if (hostName != null)
                        {
                            hostCombo.setSelectedItem(hostName);
			    setHostChoice(hostName, hostCombo);
                        }
                        return;
                    // setDirty(true);
                    }
                });
            }
        } else {
        } 
    }

    /**
     * Refresh hostCombo with new remote host list.
     */
    private void updateRemoteHostList() {
        fillHostsCombo(hostCombo);
        // current value
        setHostChoice(lastHostChoice, hostCombo);
    }
        
      
    
    @Override
    protected void componentShowing() {
       // ConnectionManager.getInstance().addConnectionListener(this);
       // HostsRegistry.getInstance().addChangeListener(this);
       // updateContent(HostsRegistry.getInstance().getActiveEnvironment());
    }
    
    private synchronized void updateContent(final ExecutionEnvironment env) {
        if (env == currentEnv) {
            return;
        }
        this.currentEnv = env;

        if (env == null) {
            processListPanel.setListProvider(null);
        } else {
            if (SwingUtilities.isEventDispatchThread()) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        final ProcessListSupport.Provider providerFor = ProcessListSupport.getProviderFor(env);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                processListPanel.setListProvider(providerFor);
                            }
                        });
                    }
                });
            } else {
                final ProcessListSupport.Provider providerFor = ProcessListSupport.getProviderFor(env);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                processListPanel.setListProvider(providerFor);
                            }
                        });
            }
            
            
            
            //processListPanel.setActionsProvider(ProcessActionsSupport.getProviderFor(env));
        }        

    }    

    @Override
    public void removeNotify() {
        updateContent(null);
        super.removeNotify();
    }

    

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");//NOI18N
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
//        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

//    @Override
//    public void connected(ExecutionEnvironment env) {
//        if (env == currentEnv) {
//            updateContent(env);
//        }
//    }
//
//    @Override
//    public void disconnected(ExecutionEnvironment env) {
//        if (env == currentEnv) {
//            updateContent(env);
//        }
//    }

//    private synchronized void updateContent(final ExecutionEnvironment env) {
//        if (env == currentEnv) {
//            return;
//        }
//
//        if (env == null) {
//            processListPanel.setListProvider(null);
//        } else {
//            processListPanel.setListProvider(ProcessListSupport.getProviderFor(env));
//            processListPanel.setActionsProvider(ProcessActionsSupport.getProviderFor(env));
//        }
//
//        currentEnv = env;
//    }
//
//    @Override
//    public void stateChanged(ChangeEvent e) {
//        if (HostsRegistry.getInstance() == e.getSource()) {
//            updateContent(HostsRegistry.getInstance().getActiveEnvironment());
//        }
//    }
    
/**
     * Ensure that the override engine matches the debugger selected
     * for the attach panel.
     * Ideally we need to use overrideEngineType() to control which
     * engines are available in the debugger menu but that is controlled
     * "statically" by registering various AttachTypes.
     * @return
     */
    private boolean ckMatch() {
	EngineType override = EngineTypeManager.getOverrideEngineType();
	if (override == null)
	    return true;
	else
	    return override.equals(engine.getType());
    }    
    
 // This class is made public, to support attach history
    // see org.netbeans.modules.debugger.ui.actions.ConnectorPanel.ok() method implementation
    public class AttachController implements PersistentController {

        private final PropertyChangeSupport pcs =
                new PropertyChangeSupport(this);
        private ProcessInfo processInfo = null;

        // interface Controller
        @Override
        final public boolean isValid() {
	    if (!ckMatch())
		return false;

//            
//            if (loadedPID == null) {
//                int selectedRow = procTable.getSelectedRow();
//                if (selectedRow == -1) {
//                    return false;
//                }
//                if (processModel.getRowCount() <= selectedRow) {
//                    return false;
//                }
//                Object pidobj = processModel.getValueAt(selectedRow, 1);
//                if (!(pidobj instanceof String)) {
//                    return false;
//                }
//            }
            processInfo =  processListPanel.getSelectedInfo();// == null ? null : processListPanel.getSelectedInfo().getPID() + "";
            return true;
        }

        // interface Controller
        @Override
        final public boolean ok() {
            //System.out.println("AttachPanel.ok");
            
            if ( executableProjectPanel.getNoProject() ) {
                UserAttachAction action = Lookup.getDefault().lookup(UserAttachAction.class);
                if (action != null) {
                    ProcessInfo processInfo = processListPanel.getSelectedInfo();
                            //procTable.getSelectedRow();
                    if (processInfo == null) {
                        return false;
                    }

//                    String pid = processModel.getValueAt(selectedRow, getPsData().pidColumnIdx()).toString();
                    String hostName = getHostName();
                    int index = hostName.indexOf(":");      // NOI18N
                    if (index != -1) {
                        hostName = hostName.substring(0, index);
                    }
                    int pid = processInfo.getPID();
                    action.attach(hostName, pid + "", engine.getType(), processListPanel.getFilter());
                    return true;
                }
            }
            
            if (isValid()) {
                saveState();
                //target descriptor should be prepared before removeNotify
                final TargetDescriptor targetDescriptor = new TargetDescriptor(processInfo, 
                        executableProjectPanel.getSelectedProject(), executableProjectPanel.getNoProject(), engine);
                
                // doAttach() should not be called in EDT (i.e. see #212908).
                // The idea is to start it outside EDT, but 'block' UI.
                // ProgressUtils.showProgressDialogAndRun() is a good candidate
                // for this, but debugger also displays it's progress dialog on
                // warm-up, which makes this look ugly and, most important, 
                // it's 'Cancel' button is disabled in this case.
                // So we can show 'blocking dialog' for the time of target 
                // preparation and close it once we are close to debuger 
                // invocation.
                // see #212908

                // Workaround for IZ 134708                
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        String title = NbBundle.getMessage(AttachController.class, "PROGRESS_PREPARE_TARGET"); // NOI18N
                        final TargetPreparator tp = new TargetPreparator(targetDescriptor);

                        Runnable cont = new Runnable() {

                            @Override
                            public void run() {
                                if (tp.cancelled.get()) {
                                    return;
                                }
                                final DebugTarget target = tp.targetRef.get();
                                if (target != null) {
                                    if (targetDescriptor.project == null) {
                                        NativeDebuggerManager.get().attach(target);                                    
                                    } else {
                                        ProjectActionEvent projectActionEvent = new ProjectActionEvent(targetDescriptor.project, 
                                                ProjectActionEvent.PredefinedType.ATTACH, 
                                                target.getExecutable(), target.getConfig(), 
                                                target.getRunProfile(), false, 
                                                Lookups.fixed(target)
                                        );
                                        ProjectActionSupport.getInstance().fireActionPerformed(new ProjectActionEvent[] {projectActionEvent});                                        
                                    }
                                }
                            }
                        };
                        ModalMessageDlg.runLongTask(WindowManager.getDefault().getMainWindow(), tp, cont, null, title, title);
                    }
                });

                return true;
            } else {
                return false;
            }
        }

        // interface Controller
        @Override
        final public boolean cancel() {
            return true;
        }

        // interface Controller
        @Override
        final public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        // interface Controller
        @Override
        final public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

        private void validChanged() {
            pcs.firePropertyChange(Controller.PROP_VALID, null, null);
        }

        private static final String COMMAND_PROP = "command"; //NOI18N
        private static final String EXECUTABLE_PATH_PROP = "executable_path"; //NOI18N
        private static final String SELECTED_PROJECT_PROP = "selected_project"; //NOI18N
        private static final String ENGINE_PROP = "engine"; //NOI18N
        private static final String HOST_NAME_PROP = "host_name"; //NOI18N
        private static final String NO_EXISTING_PROCESS = "qwdq123svdfv"; //NOI18N

        @Override
        public boolean load(Properties props) {
//            if (processListPanel.get == null) {
//                NativeDebuggerManager.warning(Catalog.get("MSG_Uninitialized_Process_Table"));
//                return false;
//            }
//            Vector<Vector<String>> processes = psData.processes(Pattern.compile(props.getString(COMMAND_PROP, NO_EXISTING_PROCESS), Pattern.LITERAL));
//            if (processes.isEmpty()) {
//                return false;
//            }
//            EngineType et = EngineTypeManager.getEngineTypeByID(props.getString(ENGINE_PROP, "")); //NOI18N
//            if (et == null) {
//                return false;
//            }
//            String hostName = props.getString(HOST_NAME_PROP, ""); //NOI18N
//            hostCombo.setSelectedItem(hostName);
//            if (!hostCombo.getSelectedItem().equals(hostName)) {
//                return false;
//            }
//            String selectedProject = props.getString(SELECTED_PROJECT_PROP, "");  //NOI18N
//            if (!executableProjectPanel.containsProjectWithPath(getHostName(), selectedProject)) {
//                return false;
//            }
//            executableProjectPanel.setSelectedProjectByPath(getHostName(), selectedProject);
//            loadedPID = processes.get(0).get(psData.pidColumnIdx()) + "";//NOI18N
//            executableProjectPanel.setExecutablePath(getHostName(), props.getString(EXECUTABLE_PATH_PROP, "")); //NOI18N
//            engine = new EngineDescriptor(et);
            return true;
        }

        @Override
        public void save(Properties props) {
            String selectedCommand = getSelectedProcessCommand();
            if (selectedCommand != null) {
                props.setString(COMMAND_PROP, selectedCommand);
                props.setString(EXECUTABLE_PATH_PROP, executableProjectPanel.getExecutablePath());
 //              props.setString(SELECTED_PROJECT_PROP, executableProjectPanel.getSelectedProjectPath());
                props.setString(ENGINE_PROP, engine.getType().getDebuggerID());
                props.setString(HOST_NAME_PROP, (String) hostCombo.getSelectedItem());
            }
        }

        @Override
        public String getDisplayName() {
            String selectedCommand = getSelectedProcessCommand();
            if (selectedCommand != null) {
                return getString("ATTACH_HISTORY_MESSAGE", (new File(selectedCommand)).getName(), (String) hostCombo.getSelectedItem()); //NOI18N
            }
            return ""; //NOI18N
        }

        private String getSelectedProcessCommand() {
            ProcessInfo selectedInfo = processListPanel.getSelectedInfo();
            if (selectedInfo == null) {
                return null;
            }
            return selectedInfo.get(ProcessInfoDescriptor.COMMAND_COLUMN_ID, String.class);
//            int selectedRow = procTable.getSelectedRow();
//            if (selectedRow == -1) {
//                return null;
//            }
//            return getProcessCommand(selectedRow);
        }

//        private String getProcessCommand(int row) {
//            Object commandobj = processModel.getValueAt(row, getPsData().commandColumnIdx());
//            if (commandobj instanceof String) {
//                return (String) commandobj;
//            }
//            return null;
//        }

        private String getString(String key, String... a1) {
            return NbBundle.getMessage(AttachController.class, key, a1);
        }



    }
    
private void saveState() {
        if (hostCombo != null && hostCombo.getItemCount() > 0) {
            String hostName = getHostName();
            if (hostName != null) {
                lastHostChoice = hostName;
            }
        }
        
//        if (filterCombo != null) {
//            String filter = (String) filterCombo.getSelectedItem();
//            if (filter != null) {
//                lastFilter = filter;
//            }
//        }
    }
    
    
    private class TargetPreparator implements Runnable, Cancellable {

        private final TargetDescriptor targetDesctiptor;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final AtomicReference<DebugTarget> targetRef = new AtomicReference<DebugTarget>(null);

        public TargetPreparator(TargetDescriptor targetDesctiptor) {
            this.targetDesctiptor = targetDesctiptor;
        }

        @Override
        public void run() {
            String pidobj = targetDesctiptor.processInfo == null ? null : targetDesctiptor.processInfo.getPID() + "";
            if (pidobj == null) {                  
                return;
            }

            ProjectSupport.ProjectSeed seed;

            String pidstring = (String) pidobj;
            long pid = Long.parseLong(pidstring);

            assert pid != 0;

            String executable = null;
            Object path = targetDesctiptor.processInfo.getExecutable();
                    //executableProjectPanel.getExecutablePath();
            Project project = targetDesctiptor.project;
            boolean noproject = targetDesctiptor.noProject;

            if (project != null) {
                MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project);
		if (conf != null) {
                    path = conf.getAbsoluteOutputValue();
                }
            }
            
            if (path != null) {
                executable = path.toString();
                // convert to world
                //TODO:FIX IT BEFORE PUSH
                executable = FileMapper.getDefault().engineToWorld(executable);
//                executablePickList.addElement(executable);
//                executableProjectPanel.setExecutablePaths(
//                        executablePickList.getElementsDisplayName());
            }

            seed = new ProjectSupport.ProjectSeed(
                    project, targetDesctiptor.engine.getType(), noproject,
                    executable,
                    ProjectSupport.Model.DONTCARE,
                        /*corefile*/ null,
                    pid,
                        /*workingdir*/ null,
                        /*args*/ null,
                        /*envs*/ null,
                    getHostName());

            ProjectSupport.getProject(seed);

            if (cancelled.get()) {
                return;
            }

            // For persistance
            executableProjectPanel.setLastSelectedProject(seed.project());
//            lastExecPath = seed.executable();

            // Do it
            final DebugTarget dt = new DebugTarget(seed.conf());
            dt.setExecutable(seed.executableNoSentinel());
            dt.setPid(seed.pid());
            dt.setHostName(seed.getHostName());
            dt.setEngine(targetDesctiptor.engine.getType());

            if (project == null) {
                if (noproject) { // < no project>
                    dt.setProjectMode(DebugTarget.ProjectMode.NO_PROJECT);
                } else { // <new project>
                    dt.setBuildFirst(false);
                    dt.setProjectMode(DebugTarget.ProjectMode.NEW_PROJECT);
                }
            }
            targetRef.set(dt);
        }

        @Override
        public boolean cancel() {
            cancelled.set(true);
            return true;
        }
    }
    
    private class TargetDescriptor {
        final ProcessInfo processInfo;
        final Project project;
        final boolean noProject;
        final EngineDescriptor engine;

        public TargetDescriptor(ProcessInfo processInfo, Project project, boolean noProject,  EngineDescriptor engine) {
            this.processInfo = processInfo;
            this.project = project;
            this.noProject = noProject;
            this.engine = engine;
        }
        
    }

    private void hostsButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (NativeDebuggerManager.isStandalone()) {
            // It's effect will come back to us via
            // contentsChanged(RecordListEvent)
            HostListEditor editor = new HostListEditor();
            editor.showDialog(this);

        } else {
            ServerListUI.showServerListDialog();            
            //and update combox box
            updateRemoteHostList();
//            // This doesn't really work. See IZ 147543.
//            // The Hosts button is disabled under the IDE.
//
//            // Make copy of ServerList
//            ServerUpdateCache suc = new ServerUpdateCache();
//            suc.setHosts(ServerList.getRecords());
//            suc.setDefaultRecord(ServerList.getDefaultRecord());
//
//            // Show editor
//	    /*
//            ToolsCacheManager cacheManager = ToolsCacheManager.get();
//            cacheManager.setHosts(ServerList.getRecords());
//            cacheManager.setDefaultRecord(ServerList.getDefaultRecord());
//
//            if (ServerListUIEx.showServerListDialog(cacheManager)) {
//                // assign back to main ServerList
//                cacheManager.applyChanges();
//            }
//	    */
        }
    }
    
    public static void fillHostsCombo(JComboBox combo) {
        String[] hostChoices = null;
        if (NativeDebuggerManager.isStandalone()) {
            CustomizableHostList hostlist = NativeDebuggerManager.get().getHostList();
            if (hostlist != null) {
                hostChoices = hostlist.getRecordsDisplayName();
            }
        } else {
            hostChoices = CndRemote.getServerListIDs();
        }

        combo.removeAllItems();
        if (hostChoices != null) {
            for (String item : hostChoices) {
                combo.addItem(item);
            }
        }
    }

    public static void setHostChoice(String hostname, JComboBox combo) {
        if (hostname == null) {
            combo.setSelectedIndex(0);
            return;
        }
        for(int i=0; i < combo.getModel().getSize(); i++) {
            Object item = combo.getModel().getElementAt(i);
            if (item.toString().startsWith(hostname)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(0);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("Attaching");
    }    
}
