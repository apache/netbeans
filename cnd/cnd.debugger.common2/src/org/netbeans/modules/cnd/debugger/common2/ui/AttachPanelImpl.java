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

package org.netbeans.modules.cnd.debugger.common2.ui;

import java.util.HashMap;
import java.util.Vector;
import java.util.Map;
import java.util.regex.*;
import java.util.prefs.Preferences;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.debugger.Properties;

import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.netbeans.spi.debugger.ui.Controller;

import org.netbeans.api.project.Project;

import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerUpdateCache;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.DialogManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.AttachPanel;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.ProjectSupport;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;

import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.RecordListListener;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.RecordListEvent;
import org.netbeans.modules.cnd.debugger.common2.utils.MRUComboBoxModel;
import org.netbeans.modules.cnd.debugger.common2.utils.PsProvider;

import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CustomizableHostList;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.HostListEditor;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CndRemote;


import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import org.netbeans.modules.cnd.debugger.common2.debugger.spi.UserAttachAction;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.spi.debugger.ui.PersistentController;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;

/**
 * Panel which presents a list of available processes and lets user
 * pick one.
 * <p>
 * There is no explicit attach Action. Instead this.DbxAttachType is
 * registered in META-INF/debugger/org.netbeans.spi.debugger.ui.AttachType.
 * <p>
 * <p>
 * Todo:
 * <ul>
 * <li> Make it optional whether we use /usr/ucb/ps -uwww or /usr/bin/ps -ef etc.
 * (tradeoffs)
 * <li> Use the regular expression match index to highlight the range
 * of the lines that matched
 * <li> Use the regular expression match index to scroll horizontally to
 * ensure that the match segment(s) are visible (if possible)
 * <li> Should I allow users to type in unix/globstyle regular expressions
 * instead of proper regular expression? If so, should it be user
 * explicit? The default? Or some heuristic? Here's an idea: if
 * there are no matches using real regular expressions, convert the
 * regular expression to glob style and try again. Is converting to
 * glob-style regular expression as simple as inserting a "." before
 * every "*", and changing all "." occurrences into "\." ??
 * </ul>
 */
public final class AttachPanelImpl extends AttachPanel {

    private JButton refreshButton;

    // settings persistence
    // Saved in:
    // <userdir>/config/Preferences/
    //	org/netbeans/modules/cnd/debugger/common2/attach_filters.properties
    private static final Preferences prefs =
            NbPreferences.forModule(AttachPanelImpl.class);
    private static final Preferences filterPrefs =
            prefs.node("attach_filters");			//NOI18N

    /* Keep one instance so combobox choices are persistant */
    private static Map<EngineType, AttachPanelImpl> cacheInstance = new HashMap<EngineType, AttachPanelImpl>();
    /** Filter combo box model */
    private MRUComboBoxModel filterModel;
    private DialogManager dialogManager = null;
    private JButton okButton;
    private ExecutableProjectPanel executableProjectPanel;

    // For persistance
//    private DefaultPicklistModel executablePickList = null;
    private String lastExecPath = null;

    // <From Process>
    private EngineDescriptor engine;

    /** Don't allow filter unless filterReady is true */
    private boolean filterReady = false;

    /** Creates new form AttachPanel */
    private AttachPanelImpl(EngineType debuggerType) {
        super();

        this.engine = new EngineDescriptor(debuggerType);
//        executablePickList = new DefaultPicklistModel(8);
//        String autoExe = "";
//        if (engine.hasCapability(EngineCapability.DERIVE_EXECUTABLE)) {
//            autoExe = Catalog.get("FromProcess"); // NOI18N
//            executablePickList.addElement(autoExe);
//        }
        initComponents(this.engine);
        Catalog.setAccessibleDescription(this, "ACSD_CTL_AttachToProcess"); // NOI18N

    }

    public synchronized static AttachPanelImpl getInstance(DialogManager dialogManager,
            JButton okButton, EngineType debuggerType) {
        AttachPanelImpl panel = cacheInstance.get(debuggerType);
        if (panel == null) {
            panel = new AttachPanelImpl(debuggerType);
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
        lastFilter = (String) filterCombo.getSelectedItem();
    }

    private void initialize(DialogManager dialogManager, JButton okButton) {
        this.dialogManager = dialogManager;
        this.okButton = okButton;

        filterCombo.setSelectedItem(lastFilter);
        executableProjectPanel.initGui();

        if (!NativeDebuggerManager.isStandalone()) {
            // ServerList has no change notifier so we resync everytime.
            updateRemoteHostList();
        }
        refreshProcesses(null, true);
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
    
    /**
     * Override the regular cell renderer so we can add margins to the
     * text in the cells.
     */
    static class MyTableCellRenderer implements TableCellRenderer {

        TableCellRenderer original;

        MyTableCellRenderer(TableCellRenderer original) {
            this.original = original;
        }

        // interface TableCellRenderer
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row, int column) {

            Component renderer = original.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);
            // TEST renderer.setBackground(Color.red);
            JComponent c = (JComponent) renderer;
            Insets insets = new Insets(0, 5, 0, 0);
            Border border = new EmptyBorder(insets);
            c.setBorder(border);
            return c;
        }
    };
    private static final String REFRESH_ICON =
            "org/netbeans/modules/cnd/debugger/common2/icons/refresh.png";	// NOI18N

    private void initComponents(EngineDescriptor debuggerType) {
        headingPanel = new javax.swing.JPanel();
        refreshButton = new JButton();
        filterLabel = new javax.swing.JLabel();
        filterCombo = new javax.swing.JComboBox();
        hostLabel = new javax.swing.JLabel();
        hostCombo = new javax.swing.JComboBox();
        hostsButton = new javax.swing.JButton();

        tableLabel = new javax.swing.JLabel();
        allProcessesCheckBox = new javax.swing.JCheckBox(Catalog.get("All_Processes_Lab")); // NOI18N
	allProcessesCheckBox.setEnabled(true);
	allProcessesCheckBox.setSelected(prefs.getBoolean("showAllUserProcesses", true));//NOI18N
	allProcessesCheckBox.setFocusable(false);
	allProcessesCheckBox.setToolTipText(Catalog.get("All_Processes")); // NOI18N
	allProcessesCheckBox.setMnemonic(Catalog.getMnemonic("MNEM_All_Processes")); // NOI18N
	Catalog.setAccessibleDescription(allProcessesCheckBox, "ACSD_All_Processes"); // NOI18N

        allProcessesCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                refreshProcesses(null, true);
                prefs.putBoolean("showAllUserProcesses", allProcessesCheckBox.isSelected());//NOI18N
            }
        });

        setLayout(new BorderLayout());

        refreshButton.setIcon(new ImageIcon(ImageUtilities.loadImage(REFRESH_ICON)));
        // need to add text
        refreshButton.setText(Catalog.get("Refresh")); // NOI18N
        refreshButton.setToolTipText(Catalog.get("Refresh")); // NOI18N
        refreshButton.setMnemonic(Catalog.getMnemonic("MNEM_Refresh")); // NOI18N
        Catalog.setAccessibleDescription(refreshButton, "ACSD_Refresh"); // NOI18N
        refreshButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                refreshProcesses(null, true);
            }
        });

        filterLabel.setText(Catalog.get("AttachProcDiag_FilterColon")); // NOI18N
        filterLabel.setDisplayedMnemonic(
                Catalog.getMnemonic("MNEM_AttachProcDiag_Filter")); // NOI18N

        filterLabel.setLabelFor(filterCombo);
        filterCombo.setToolTipText(Catalog.get("RegExp")); //NOI18N
        filterCombo.setEditable(true);
        
        final JTextComponent cbEditor = (JTextComponent) filterCombo.getEditor().getEditorComponent();
        cbEditor.getDocument().addDocumentListener(new AnyChangeDocumentListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                refreshProcesses(null, false);
            }
        });

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
                        String hostName = getHostName();
                        if (hostName != null) {
                            refreshProcesses(hostName, true);
                        }
                    }
                }
            }
        });
        
        if (!NativeDebuggerManager.isStandalone()) {
            hostsButton.setEnabled(false);	// IZ 147543
        }
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
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
        headingPanel.add(hostsButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        headingPanel.add(filterLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
        gridBagConstraints.weightx = 1.0;
        headingPanel.add(filterCombo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
        headingPanel.add(refreshButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
        gridBagConstraints.weightx = 1.0;
	//attach dialog should show ALL processes for selection optionally
        headingPanel.add(allProcessesCheckBox, gridBagConstraints);

        // Hack to make the panel appear wider:
        // Add an empty label with 800 pixels insert.
        JLabel fillLabel = new JLabel();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 800);
        headingPanel.add(fillLabel, gridBagConstraints);

        add(headingPanel, BorderLayout.NORTH);


        executableProjectPanel = new ExecutableProjectPanel(okButton, debuggerType, true);
        add(executableProjectPanel, BorderLayout.SOUTH);



        // Setting selected index in the model will force an update
        // (which fires the action event) and we're not ready for
        // that yet
        filterReady = false;

        // Fix up the combo box models
        Vector<String> filters2 = restoreFilterPrefs();
        filterModel = new MRUComboBoxModel(filters2);
        filterCombo.setModel(filterModel);

        procTable = new JTable(processModel);
        procTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        procTable.setShowVerticalLines(false);
        procTable.setShowHorizontalLines(true);
        procTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        procTable.setAutoCreateRowSorter(true);

        // No white clipping lines on selected table rows: reduce separator
        // to 0. That means text may touch but HIE prefers this.
        procTable.setIntercellSpacing(new Dimension(0, procTable.getRowMargin()));


        /* OLD

        This is now done in doFilter

        TableCellRenderer tcr = procTable.getDefaultRenderer(String.class);
        TableCellRenderer my_tcr = new MyTableCellRenderer(tcr);
        procTable.setDefaultRenderer(String.class, my_tcr);
         */

        // Grid color: HIE's asked for (230,230,230) but that seems troublesome
        // since we'd have to make a GUI for customizing it. Instead, go
        // with Metal's secondary2, since for alternative UIs this will continue
        // to look good (and it's customizable by the user). And secondary2
        // is close to the request valued - it's (204,204,204).
        procTable.setGridColor((Color) javax.swing.UIManager.getDefaults().get(
                "Label.background")); // NOI18N

        // Add mouse listener to listen for double-clicks
        procTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                if (procTable.isEnabled() && !isTableInfoShown()) {
                    procTableClicked(evt);
                }
            }
        });

        // Also listen to plain selections
        ListSelectionModel sm = procTable.getSelectionModel();
        sm.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() || isTableInfoShown()) {
                    return;
                }
                checkValid();
                chosenProcess();
            }
        });

        JScrollPane jsp = new JScrollPane(procTable);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setBorder(new javax.swing.border.EmptyBorder(new Insets(6, 0, 12, 0)));

        tableLabel.setText(Catalog.get("AttachProcDiag_Table"));// NOI18N
        tableLabel.setDisplayedMnemonic(Catalog.getMnemonic("MNEM_AttachProcDiag_Table")); // NOI18N

        tableLabel.setLabelFor(procTable);
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(tableLabel, BorderLayout.NORTH);
        tablePanel.add(jsp, BorderLayout.CENTER);
        Catalog.setAccessibleDescription(procTable,
                "ACSD_AttachProcDiag_Table");//NOI18N

        add(tablePanel, BorderLayout.CENTER);


        // Select text in regexp filter textfield:
        ComboBoxEditor editor = filterCombo.getEditor();
        editor.selectAll();
        // filterCombo.requestDefaultFocus();

        filterReady = true;
    }

    // Process double-click in process table
    private void procTableClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            if (dialogManager != null) {
                dialogManager.accept(true);
            } else {
                Component c = this;
                // First dismiss the attach dialog. We have no 
                // direct access to it, but can find it by walking
                // through component parents until found.
                while (c != null) {
                    if (c instanceof JDialog) {
                        JDialog d = (JDialog) c;
                        d.setVisible(false);
                        d.dispose();
                        break;
                    }
                    c = c.getParent();
                }
                controller.ok();
            }
        }
    }

    /*
     * automatically fill "Executable" field base on chosen process
     */
    private void chosenProcess() {
        int selectedRow = procTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

	int cmdIndex = getPsData().commandColumnIdx();

        Object cmdobj = processModel.getValueAt(selectedRow, cmdIndex);

        if (cmdobj instanceof String) {
            executableProjectPanel.setExecutablePath(getHostName(), (String) cmdobj);
	}
    }

    /**
     * Return hostCombo's current selection.
     * May return null if the current selectionis not in the remote host DB.
     */
    private String getHostName() {
        return hostCombo.getSelectedItem().toString();
    }

    //
    // To support 6646693
    //
    private static final String PREF_COUNT = "count"; // NOI18N
    private static final String PREF_ITEM = "item_"; // NOI18N

    private void saveFilterPrefs() {
        try {
            filterPrefs.clear();

            filterPrefs.putInt(PREF_COUNT, filterCombo.getItemCount());
            for (int ix = 0; ix < filterCombo.getItemCount(); ix++) {
                String item = (String) filterCombo.getItemAt(ix);
                filterPrefs.put(PREF_ITEM + ix, item);
            }

            // Make changes appear on disc now
            prefs.flush();

        } catch (java.util.prefs.BackingStoreException x) {
            return;
        }
    }

    private Vector<String> restoreFilterPrefs() {
        Vector<String> items = new Vector<String>();
        int count = filterPrefs.getInt(PREF_COUNT, 0);

        if (count == 0) {
            // first time ever ... add the default match-all pattern
            items.add("");
            return items;
        }

        for (int ix = 0; ix < count; ix++) {
            String item = filterPrefs.get(PREF_ITEM + ix, null);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }
    
    private void tableInfo(final String infoKey, final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                procTable.clearSelection();
                setUIEnabled(enabled);
                processModel.setDataVector(new Object[][]{{Catalog.get(infoKey)}}, new Object[]{" "}); //NOI18N
            }
        });
    }
    
    // See bugs #244267, #193443
    // There is one column in the table if "ps" task failed and returned null.
    // In this case "tableInfo(String)" sets an error message as a table Vector
    // instead of a list of processes. After that the table has 1 row and 1 column,
    // which leads to the exception (in #244267).
    private boolean isTableInfoShown() {
        return procTable.getSelectedRow() == 0 && processModel.getColumnCount() <= 1;
    }

    private void setUIEnabled(boolean st) {
        filterCombo.setEnabled(st);
        refreshButton.setEnabled(st);
        hostCombo.setEnabled(st);
        procTable.setEnabled(st);
    }

    /**
     * Run ps, filter the output and update the table model.
     */
    private void refreshProcesses(String hostName, boolean request) {

        if (!filterReady) {
            return;
        }
        
        JTextComponent cbEditor = (JTextComponent) filterCombo.getEditor().getEditorComponent();
        Object selected = request ? filterCombo.getSelectedItem() : cbEditor.getText();

        // Get ready to filter based on a regular expression
        String regexp = "";
        if (selected != null) {
            regexp = selected.toString();
        }

        // compile and validate 'regexp' into 're'
        Pattern re = null;
        try {
            re = Pattern.compile(regexp, Pattern.LITERAL);
        } catch (PatternSyntaxException e) {
            if (e.getLocalizedMessage() != null) {
                StatusDisplayer.getDefault().setStatusText(e.getLocalizedMessage());
            }
            return;
        }

        /*
        if (selected != null) {
        // Select the text in the filter textbox
        ComboBoxEditor editor = filterCombo.getEditor();
        editor.setItem(selected);
        filterCombo.requestDefaultFocus();
        editor.selectAll();
        }
         */

        final boolean getAllProcesses = !allProcessesCheckBox.isSelected();
        if (request) {
            // accept and commit to the new filter
            filterModel.add(regexp);
            filterCombo.setSelectedItem(selected);

            saveFilterPrefs();

            // DEBUG hostList().dumpRecords();

            if (hostName == null) {
                hostName = (String) hostCombo.getSelectedItem();
            }

            final Pattern fre = re;
            final String hostname = hostName;
            
            //final boolean getAllProcesses = false;

            tableInfo("MSG_Gathering_Data", false); //NOI18N

            CndRemote.validate(hostName, new Runnable() {

                @Override
                public void run() {
                    requestProcesses(fre, hostname, getAllProcesses);
                }
            }, new Runnable() {

                @Override
                public void run() {
                    tableInfo("MSG_PS_Failed", true); //NOI18N
                }
            });
        } else {
            filterProcesses(re);
        }
    }

    private void requestProcesses(final Pattern re, final String hostname, final boolean getAll) {
        Runnable asycData = new Runnable() {
            @Override
	    public void run() {
                final Host selectedHost = Host.byName(hostname);
                PsProvider psProvider = PsProvider.getDefault(selectedHost);
                if (psProvider == null) {
                    // "clear" the table
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            processModel.setRowCount(0);
                        }
                    });
                    return;
                }
		PsProvider.PsData data = 
			psProvider.getData(getAll); // may take a while
		setPsData(data);
		try {
		    javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            filterProcesses(re);
                        }
                    });
		} catch (Exception x) {
		    x.printStackTrace();
		}
	    }
	};

	RequestProcessor.Task task = getPcRP.post(asycData);
    }

    private static class AttachTableModel extends DefaultTableModel {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    private final static RequestProcessor getPcRP =  
				new RequestProcessor("processes"); // throughput 1 // NOI18N

    private volatile PsProvider.PsData psData = null;

    private void setPsData (PsProvider.PsData data) {
	psData = data;
    }

    private PsProvider.PsData getPsData() {
	return psData;
    }
    
    private void filterProcesses(final Pattern re) {
        //final PsProvider.PsData psData = psProvider.getData(selectedHost, getAll);
        final PsProvider.PsData psData = getPsData();

        if (psData == null) {
            tableInfo("MSG_PS_Failed", true); //NOI18N
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                processModel.setDataVector(psData.processes(re), psData.header());
                setUIEnabled(true);

                // It seems we need to reassign the renderer whenever we
                // setDataVector ...
                TableCellRenderer tcr = procTable.getDefaultRenderer(Object.class);
                TableCellRenderer my_tcr = new MyTableCellRenderer(tcr);
                procTable.setDefaultRenderer(Object.class, my_tcr);


                // adjust columnMargin so the text is a bit more readable
                // Same as interCellSpacing and no good
                // TableColumnModel tcm = procTable.getColumnModel();
                // tcm.setColumnMargin(5);

                // Try to make the command column wider than the others
                try {
                    TableColumn tbc =
                            procTable.getColumnModel().getColumn(
                            psData.commandColumnIdx());
                    tbc.setPreferredWidth(300);
                    tbc.setMinWidth(75);
                } catch (IllegalArgumentException e) {
                    // Not a critical error
                }

                checkValid();
            }
        });
    }

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

    private void checkValid() {
        controller.validChanged();
	if (!ckMatch()) {
            executableProjectPanel.setEnabled(false);
	    executableProjectPanel.setError("ERROR_DONTAPPLY", false); // NOI18N
	    return;
	}

        if (procTable.getSelectedRow() == -1 ) {
            executableProjectPanel.setEnabled(false);
        } else {
            executableProjectPanel.setEnabled(true);
        }
    }
    private final DefaultTableModel processModel = new AttachTableModel();
    private javax.swing.JTable procTable;
    private javax.swing.JPanel headingPanel;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JComboBox pathComboBox;
    private javax.swing.JComboBox filterCombo;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JLabel tableLabel;
    private javax.swing.JCheckBox allProcessesCheckBox;
    private javax.swing.JPanel buttonRowPanel;
    private static String lastHostChoice;
    private static String lastFilter;
    private javax.swing.JComboBox hostCombo;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JButton hostsButton;
    private final AttachController controller = new AttachController();
    
    private void saveState() {
        if (hostCombo != null && hostCombo.getItemCount() > 0) {
            String hostName = getHostName();
            if (hostName != null) {
                lastHostChoice = hostName;
            }
        }
        
        if (filterCombo != null) {
            String filter = (String) filterCombo.getSelectedItem();
            if (filter != null) {
                lastFilter = filter;
            }
        }
    }

    public static abstract class AnyChangeDocumentListener implements DocumentListener {
        protected abstract void documentChanged(DocumentEvent e);

        @Override
        public void changedUpdate(DocumentEvent e) {
            documentChanged(e);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            documentChanged(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            documentChanged(e);
        }
    }

    public Controller getController() {
        return controller;
    }

    // This class is made public, to support attach history
    // see org.netbeans.modules.debugger.ui.actions.ConnectorPanel.ok() method implementation
    public class AttachController implements PersistentController {

        private final PropertyChangeSupport pcs =
                new PropertyChangeSupport(this);
        private String loadedPID = null;

        // interface Controller
        @Override
        final public boolean isValid() {
	    if (!ckMatch())
		return false;
            
            if (isTableInfoShown()) {
                return false;
            }
            
            if (loadedPID == null) {
                int selectedRow = procTable.getSelectedRow();
                if (selectedRow == -1) {
                    return false;
                }
                if (processModel.getRowCount() <= selectedRow) {
                    return false;
                }
                Object pidobj = processModel.getValueAt(selectedRow, 1);
                if (!(pidobj instanceof String)) {
                    return false;
                }
            }
            return true;
        }

        // interface Controller
        @Override
        final public boolean ok() {
            //System.out.println("AttachPanel.ok");
            
            if ( executableProjectPanel.getNoProject() ) {
                UserAttachAction action = Lookup.getDefault().lookup(UserAttachAction.class);
                if (action != null) {
                    int selectedRow = procTable.getSelectedRow();
                    if (selectedRow == -1) {
                        return false;
                    }

                    String pid = processModel.getValueAt(selectedRow, getPsData().pidColumnIdx()).toString();
                    String hostName = getHostName();
                    int index = hostName.indexOf(":");      // NOI18N
                    if (index != -1) {
                        hostName = hostName.substring(0, index);
                    }
                    action.attach(hostName, pid, engine.getType(), filterCombo.getSelectedItem() + "");
                    return true;
                }
            }
            
            if (isValid()) {
                saveState();
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
                        final TargetPreparator tp = new TargetPreparator(loadedPID);

                        Runnable cont = new Runnable() {

                            @Override
                            public void run() {
                                if (tp.cancelled.get()) {
                                    return;
                                }
                                final DebugTarget target = tp.targetRef.get();
                                if (target != null) {
                                    if (executableProjectPanel.getSelectedProject() == null) {
                                        NativeDebuggerManager.get().attach(target);                                    
                                    } else {
                                        ProjectActionEvent projectActionEvent = new ProjectActionEvent(executableProjectPanel.getSelectedProject(), 
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
            if (psData == null) {
                NativeDebuggerManager.warning(Catalog.get("MSG_Uninitialized_Process_Table"));
                return false;
            }
            Vector<Vector<String>> processes = psData.processes(Pattern.compile(props.getString(COMMAND_PROP, NO_EXISTING_PROCESS), Pattern.LITERAL));
            if (processes.isEmpty()) {
                return false;
            }
            EngineType et = EngineTypeManager.getEngineTypeByID(props.getString(ENGINE_PROP, "")); //NOI18N
            if (et == null) {
                return false;
            }
            String hostName = props.getString(HOST_NAME_PROP, ""); //NOI18N
            hostCombo.setSelectedItem(hostName);
            if (!hostCombo.getSelectedItem().equals(hostName)) {
                return false;
            }
            String selectedProject = props.getString(SELECTED_PROJECT_PROP, "");  //NOI18N
            if (!executableProjectPanel.containsProjectWithPath(getHostName(), selectedProject)) {
                return false;
            }
            executableProjectPanel.setSelectedProjectByPath(getHostName(), selectedProject);
            loadedPID = processes.get(0).get(psData.pidColumnIdx()) + "";//NOI18N
            executableProjectPanel.setExecutablePath(getHostName(), props.getString(EXECUTABLE_PATH_PROP, "")); //NOI18N
            engine = new EngineDescriptor(et);
            return true;
        }

        @Override
        public void save(Properties props) {
            String selectedCommand = getSelectedProcessCommand();
            if (selectedCommand != null) {
                props.setString(COMMAND_PROP, selectedCommand);
                props.setString(EXECUTABLE_PATH_PROP, executableProjectPanel.getExecutablePath());
                props.setString(SELECTED_PROJECT_PROP, executableProjectPanel.getSelectedProjectPath());
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
            int selectedRow = procTable.getSelectedRow();
            if (selectedRow == -1) {
                return null;
            }
            return getProcessCommand(selectedRow);
        }

        private String getProcessCommand(int row) {
            Object commandobj = processModel.getValueAt(row, getPsData().commandColumnIdx());
            if (commandobj instanceof String) {
                return (String) commandobj;
            }
            return null;
        }

        private String getString(String key, String... a1) {
            return NbBundle.getMessage(AttachController.class, key, a1);
        }

    }
    
    private class TargetPreparator implements Runnable, Cancellable {

        private final String pidString;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final AtomicReference<DebugTarget> targetRef = new AtomicReference<DebugTarget>(null);

        public TargetPreparator(String process) {
            this.pidString = process;
        }

        @Override
        public void run() {
            Object pidobj = pidString;

            if (pidobj == null) {
                int selectedRow = procTable.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }

                pidobj = processModel.getValueAt(selectedRow, getPsData().pidColumnIdx());
            }

            if (!(pidobj instanceof String)) {
                return;
            }

            ProjectSupport.ProjectSeed seed;

            String pidstring = (String) pidobj;
            long pid = Long.parseLong(pidstring);

            assert pid != 0;

            String executable = null;
            Object path = executableProjectPanel.getExecutablePath();
            Project project = executableProjectPanel.getSelectedProject();
            boolean noproject = executableProjectPanel.getNoProject();

            if (project != null) {
                MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project);
		if (conf != null) {
                    path = conf.getAbsoluteOutputValue();
                }
            }
            
            if (path != null) {
                executable = path.toString();
                // convert to world
                executable = psData.getFileMapper().engineToWorld(executable);
//                executablePickList.addElement(executable);
//                executableProjectPanel.setExecutablePaths(
//                        executablePickList.getElementsDisplayName());
            }

            seed = new ProjectSupport.ProjectSeed(
                    project, engine.getType(), noproject,
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
            lastExecPath = seed.executable();

            // Do it
            final DebugTarget dt = new DebugTarget(seed.conf());
            dt.setExecutable(seed.executableNoSentinel());
            dt.setPid(seed.pid());
            dt.setHostName(seed.getHostName());
            dt.setEngine(engine.getType());

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

    private void hostsButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (NativeDebuggerManager.isStandalone()) {
            // It's effect will come back to us via
            // contentsChanged(RecordListEvent)
            HostListEditor editor = new HostListEditor();
            editor.showDialog(this);

        } else {
            // This doesn't really work. See IZ 147543.
            // The Hosts button is disabled under the IDE.

            // Make copy of ServerList
            ServerUpdateCache suc = new ServerUpdateCache();
            suc.setHosts(ServerList.getRecords());
            suc.setDefaultRecord(ServerList.getDefaultRecord());

            // Show editor
	    /*
            ToolsCacheManager cacheManager = ToolsCacheManager.get();
            cacheManager.setHosts(ServerList.getRecords());
            cacheManager.setDefaultRecord(ServerList.getDefaultRecord());

            if (ServerListUIEx.showServerListDialog(cacheManager)) {
                // assign back to main ServerList
                cacheManager.applyChanges();
            }
	    */
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
