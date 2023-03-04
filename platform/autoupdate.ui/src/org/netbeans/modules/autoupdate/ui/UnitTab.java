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

package org.netbeans.modules.autoupdate.ui;

import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.modules.autoupdate.ui.wizards.UninstallUnitWizard;
import org.netbeans.modules.autoupdate.ui.wizards.InstallUnitWizard;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author  Jiri Rechtacek, Radek Matous
 */
public final class UnitTab extends javax.swing.JPanel {
    static final String PROP_LAST_CHECK = "lastCheckTime"; // NOI18N    
    private PreferenceChangeListener preferenceChangeListener;    
    private UnitTable table = null;
    private UnitDetails details = null;
    private UnitCategoryTableModel model = null;
    private DocumentListener dlForSearch;
    private FocusListener flForSearch;
    private String filter = "";
    private PluginManagerUI manager = null;
    private PopupActionSupport popupActionsSupport;
    private ButtonActionSupport buttonsActionsSupport;
    private TabAction reloadAction;
    private RowTabAction moreAction;
    private RowTabAction lessAction;
    private RowTabAction removeLocallyDownloaded;
    
    private static Boolean isWaitingForExternal = false;
    
    private static final RequestProcessor SEARCH_PROCESSOR = new RequestProcessor ("search-processor");
    private final RequestProcessor.Task searchTask = SEARCH_PROCESSOR.create (new Runnable (){
        @Override
        public void run () {
            if (filter != null) {
                int row = getSelectedRow();
                final Unit u = (row >= 0) ? getModel().getUnitAtRow(row) : null;
                final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
                Runnable runAftreWards = new Runnable (){
                    @Override
                    public void run () {
                        if (u != null) {
                            int row = findRow(u.updateUnit.getCodeName());
                            restoreSelectedRow(row);
                        }                        
                        UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                        refreshState ();
                    }
                };
                model.setFilter (filter, runAftreWards);
            }
        }
    });
    private static final RequestProcessor DOWNLOAD_SIZE_PROCESSOR = new RequestProcessor ("download-size-processor", 1, true);
    private Task getDownloadSizeTask = null;
    
    /** Creates new form UnitTab */
    public UnitTab (UnitTable table, UnitDetails details, PluginManagerUI manager) {
        this.table = table;
        this.details = details;
        this.manager = manager;
        TableModel m = table.getModel ();
        assert m instanceof UnitCategoryTableModel : m + " instanceof UnitCategoryTableModel.";
        this.model = (UnitCategoryTableModel) m;
        table.getSelectionModel ().setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        initComponents ();
        lWarning.setVisible(false);//#164953
        spTab.setLeftComponent (new JScrollPane (table));
        spTab.setRightComponent (new JScrollPane (details,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        initTab ();
        listenOnSelection ();
        addComponentListener (new ComponentAdapter (){
            @Override
            public void componentShown (ComponentEvent e) {
                super.componentShown (e);
                focusTable ();
                
            }
        });
        addUpdateUnitListener(new UpdateUnitListener() {
            @Override
            public void updateUnitsChanged() {
                UnitTab.this.manager.updateUnitsChanged();
            }

            @Override
            public void buttonsChanged() {
                UnitTab.this.manager.buttonsChanged();
            }

            @Override
            public void filterChanged() {
                model.fireTableDataChanged();
                UnitTab.this.manager.decorateTabTitle(UnitTab.this.table);                
                refreshState();
            }
        });
        table.getInputMap ().put (
                KeyStroke.getKeyStroke (KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK),
                "org.netbeans.modules.autoupdate.ui.UnitTab.PopupActionSupport"); // NOI18N
        table.getActionMap ().put (
                "org.netbeans.modules.autoupdate.ui.UnitTab.PopupActionSupport", // NOI18N
                popupActionsSupport.popupOnF10);

    }
    
    void focusTable () {
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                table.requestFocusInWindow ();
            }
        });
    }
    
    UnitCategoryTableModel getModel () {
        return model;
    }
    
    UnitTable getTable () {
        return table;
    }
    
    public String getHelpId () {
        return UnitTab.class.getName () + '.' + model.getType (); // NOI18N
    }
    
    void setWaitingState (boolean waitingState) {
        boolean enabled = !waitingState;
        Component[] all = getComponents ();
        for (Component component : all) {
            if (component == bTabAction || component == bDeactivate || component == bUninstall) {
                if (enabled) {
                    TabAction a = (TabAction) ((AbstractButton)component).getAction();
                    component.setEnabled (a == null ? false : a.isEnabled());
                } else {
                    component.setEnabled (enabled);
                }
            } else {
                if (component == spTab) {
                    spTab.getLeftComponent ().setEnabled (enabled);
                    spTab.getRightComponent ().setEnabled (enabled);
                    details.setEnabled (enabled);
                    table.setEnabled (enabled);
                } else {
                    component.setEnabled (enabled);
                }
            }
        }
        if (reloadAction != null) {
            reloadAction.setEnabled (enabled);
        }
        Component parent = getParent ();
        Component rootPane = getRootPane ();
        if (parent != null) {
            parent.setEnabled (enabled);
        }
        if (rootPane != null) {
            if (enabled) {
                rootPane.setCursor (null);
            } else {
                rootPane.setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
            }
        }
        focusTable ();
    }
    
    private void prepareTopButton (Action action) {
        if(action!=null) {
            topButton.setToolTipText ((String)action.getValue (JComponent.TOOL_TIP_TEXT_KEY));
            topButton.setAction (action);
            topButton.setVisible(true);            
        } else {
            topButton.setVisible(false);
        }
    }
    
    @Override
    public void addNotify () {
        super.addNotify ();
        if (dlForSearch == null) {
            tfSearch.getDocument ().addDocumentListener (getDocumentListener ());            
        }
        
        if (flForSearch == null) {
            flForSearch = new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    tfSearch.selectAll();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    tfSearch.select(0, 0);
                }
            };
            tfSearch.addFocusListener(flForSearch);
        }
        RequestProcessor.Task runningTask = PluginManagerUI.getRunningTask (new Runnable() {

            @Override
            public void run() {
                if (isWaitingForExternal) {
                    reloadTask (false).schedule (10);
                    isWaitingForExternal = false;
                }
            }
        });
        synchronized (this) {
            if (runningTask != null && ! runningTask.isFinished () && ! isWaitingForExternal) {
                isWaitingForExternal = true;
                runningTask.addTaskListener (new TaskListener () {
                    @Override
                     public void taskFinished (org.openide.util.Task task) {
                        if (isWaitingForExternal) {
                            reloadTask (false).schedule (10);
                        }
                        isWaitingForExternal = false;
                     }
                });
            }
        }
    }
    
    @Override
    public void removeNotify () {
        super.removeNotify ();
        if (dlForSearch != null) {
            tfSearch.getDocument ().removeDocumentListener (getDocumentListener ());
        }
        dlForSearch = null;
        if (flForSearch != null) {
            tfSearch.removeFocusListener(flForSearch);
        }
        flForSearch = null;
        Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate");//NOI18N
        if (preferenceChangeListener != null) {
            p.removePreferenceChangeListener (preferenceChangeListener);
            preferenceChangeListener = null;
        }
    }
    
    private Collection<Unit> oldUnits = Collections.emptySet ();
    
    public void refreshState () {
        detailView.setVisible(this.model.supportsTwoViews());
        final Collection<Unit> units = model.getMarkedUnits ();
        if (oldUnits.equals (units)) {
            return ;
        }
        oldUnits = units;
        popupActionsSupport.tableDataChanged ();
        buttonsActionsSupport.tableDataChanged();
        
        if (units.isEmpty()) {
            cleanSelectionInfo ();
        } else {
            setSelectionInfo (null, units.size ());
        }
        getDefaultAction ().tableDataChanged(units);
        boolean alreadyScheduled = false;
        if (getDownloadSizeTask != null) {
            if (getDownloadSizeTask.getDelay () > 0) {
                getDownloadSizeTask.schedule (1000);
                alreadyScheduled = true;
            } else if (! getDownloadSizeTask.isFinished ()) {
                getDownloadSizeTask.cancel ();
            }
        }
        if (units.size () > 0 && ! alreadyScheduled) {
            getDownloadSizeTask = DOWNLOAD_SIZE_PROCESSOR.post (new Runnable () {
                @Override
                public void run () {
                    int downloadSize = model.getDownloadSize ();
                    if (Thread.interrupted ()) {
                        return ;
                    }
                    if (model.getMarkedUnits ().isEmpty()) {
                        cleanSelectionInfo ();
                    } else {
                        setSelectionInfo (Utilities.getDownloadSizeAsString (downloadSize), model.getMarkedUnits ().size ());
                    }
                }
            }, 150);
        }
    }

    final void updateTab(final Map<String, Boolean> state) {
        final Runnable addUpdates = new Runnable() {
            @Override
            public void run() {
                final LocallyDownloadedTableModel downloadedTableModel = ((LocallyDownloadedTableModel) model);
                List<UpdateUnit> empty = Collections.emptyList();
                downloadedTableModel.setUnits(empty);
                SwingUtilities.invokeLater(new Runnable()  {

                    @Override
                    public void run() {
                        fireUpdataUnitChange();
                        UnitCategoryTableModel.restoreState(model.getUnits(), state, model.isMarkedAsDefault());
                        refreshState();
                        setWaitingState(false);
                    }
                });
            }
        };
        setWaitingState(true);
        Utilities.startAsWorkerThread(addUpdates, 250);
    }
    
    private TabAction getDefaultAction () {
        return (TabAction)bTabAction.getAction ();
    }
    
    private void initTab () {
        RowTabAction[] forPopup = null;
        TabAction[] forButtons = null;
        switch (model.getType ()) {
        case INSTALLED :
        {
            RowTabAction checkCategoryAction = new CheckCategoryAction ();
            RowTabAction uncheckCategoryAction = new UncheckCategoryAction ();
            RowTabAction checkAllAction = new CheckAllAction ();
            RowTabAction uncheckAllAction = new UncheckAllAction ();
            RowTabAction activateCategoryAction = new ActivateCategoryAction ();
            RowTabAction deactivateCategoryAction = new DeactivateCategoryAction ();
            
            ActivateAction activateAction = new ActivateAction ();
            DeactivateAction deactivateAction = new DeactivateAction ();
            UninstallAction uninstallAction = new UninstallAction();
            
            ActivateRowAction activateRowAction = new ActivateRowAction();
            DeactivateRowAction deactivateRowAction = new DeactivateRowAction();
            UninstallRowAction uninstallRowAction = new UninstallRowAction();
            
            forPopup = new RowTabAction[] {
                activateRowAction, deactivateRowAction,
                activateCategoryAction, deactivateCategoryAction,
                checkCategoryAction, uncheckCategoryAction,
                checkAllAction, uncheckAllAction, new CheckAction(),
                uninstallRowAction
            };
            
            forButtons = new TabAction[] {
                activateAction, deactivateAction, uninstallAction
            };
            
            bDeactivate.setVisible(true);
            bUninstall.setVisible(true);
            bTabAction.setAction(activateAction);
            bDeactivate.setAction(deactivateAction);
            bUninstall.setAction (uninstallAction);
            prepareTopButton (null/*reloadAction = new ReloadAction ()*/);
            table.setEnableRenderer (new EnableRenderer ());
            initReloadTooltip();
            break;
        }
        
        case UPDATE :
        {
            RowTabAction selectCategoryAction = new CheckCategoryAction ();
            RowTabAction deselectCategoryAction = new UncheckCategoryAction ();
            RowTabAction selectAllAction = new CheckAllAction ();
            RowTabAction deselectAllAction = new UncheckAllAction ();
            moreAction = new MoreAction();
            lessAction = new LessAction();
            
            forPopup = new RowTabAction[] {
                new UpdateRowAction(),
                selectCategoryAction, deselectCategoryAction,
                selectAllAction, deselectAllAction, new CheckAction (),
                moreAction, lessAction
            };
        }
        bTabAction.setAction (new UpdateAction ());
        bDeactivate.setVisible(false);
        bUninstall.setVisible(false);
        prepareTopButton (reloadAction = new ReloadAction ("UnitTab_ReloadActionUpdates"));
        initReloadTooltip();
        break;
            
        case AVAILABLE :
        {
            RowTabAction selectCategoryAction = new CheckCategoryAction ();
            RowTabAction deselectCategoryAction = new UncheckCategoryAction ();
            RowTabAction selectAllAction = new CheckAllAction ();
            RowTabAction deselectAllAction = new UncheckAllAction ();
            moreAction = new MoreAction();
            lessAction = new LessAction();
            
            forPopup = new RowTabAction[] {
                new AvailableRowAction(),
                selectCategoryAction, deselectCategoryAction,
                selectAllAction, deselectAllAction, new CheckAction (),
                moreAction, lessAction                
            };
        }
        bTabAction.setAction (new AvailableAction ());
        bDeactivate.setVisible(false);
        bUninstall.setVisible(false);
        prepareTopButton (reloadAction = new ReloadAction ("UnitTab_ReloadAction"));
        table.setEnableRenderer (new SourceCategoryRenderer ());
        initReloadTooltip();
        break;
            
        case LOCAL :
            removeLocallyDownloaded = new RemoveLocallyDownloadedAction ();
            forPopup = new RowTabAction[]{
                new LocalUpdateRowAction(),
                removeLocallyDownloaded, new CheckAction()
            };
            bTabAction.setAction (new LocalUpdateAction ());
            bDeactivate.setVisible(false);
            bUninstall.setVisible(false);
            prepareTopButton (new AddLocallyDownloadedAction ());
            break;
        }
        model.addTableModelListener (new TableModelListener () {
            @Override
            public void tableChanged (TableModelEvent e) {
                refreshState ();
            }
        });
        table.addMouseListener (popupActionsSupport = new PopupActionSupport (forPopup));
        buttonsActionsSupport = new ButtonActionSupport(forButtons);
        getDefaultAction ().setEnabled (model.getMarkedUnits ().size () > 0);
    }
    
    
    private void cleanSelectionInfo () {
        lSelectionInfo.setText ("");
        lWarning.setText ("");
        lWarning.setIcon (null);
    }
    
    private void setSelectionInfo (String downloadSize, int count) {
        String operationNameKey = null;
        switch (model.getType ()) {
        case INSTALLED :
            operationNameKey = "UnitTab_OperationName_Text_INSTALLED";
            break;
        case UPDATE :
            operationNameKey = "UnitTab_OperationName_Text_UPDATE";
            break;
        case AVAILABLE :
            operationNameKey = "UnitTab_OperationName_Text_AVAILABLE";
            break;
        case LOCAL :
            operationNameKey = "UnitTab_OperationName_Text_LOCAL";
            break;
        }
        String key = count == 1 ? "UnitTab_lHowManySelected_Single_Text" : "UnitTab_lHowManySelected_Many_Text";
        if (UnitCategoryTableModel.Type.INSTALLED == model.getType () || UnitCategoryTableModel.Type.LOCAL == model.getType ()) {
            lSelectionInfo.setText ((NbBundle.getMessage (UnitTab.class, key, count)));
        } else {
            if (downloadSize == null) {
                lSelectionInfo.setText ((NbBundle.getMessage (UnitTab.class, key, count)));
            } else {
                lSelectionInfo.setText (NbBundle.getMessage (UnitTab.class, "UnitTab_lHowManySelected_TextFormatWithSize",
                        NbBundle.getMessage (UnitTab.class, key, count), downloadSize));
            }
        }
        if (model.needsRestart ()) {
            Icon warningIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/warning.gif", false); // NOI18N
            lWarning.setIcon (warningIcon);
            lWarning.setText (NbBundle.getMessage (UnitTab.class, "UnitTab_lWarning_Text", NbBundle.getMessage (UnitTab.class, operationNameKey))); // NOI18N
        }
    }
    
    private void showDetailsAtRow (int row) {
        showDetailsAtRow (row, null);
    }
    
    private void showDetailsAtRow (int row, Action action) {
        if (row == -1) {
            details.setUnit (null);
        } else {
            Unit u = model.isExpansionControlAtRow(row) ? null : model.getUnitAtRow (row);
            if (u == null) {
                //TODO: add details about more ... or les ...
            } else {
                details.setUnit (u, action);
            }
        }
    }
    
    private void listenOnSelection () {
        table.getSelectionModel ().addListSelectionListener (new ListSelectionListener () {
            @Override
            public void valueChanged (final ListSelectionEvent e) {
                if (! SwingUtilities.isEventDispatchThread()) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            valueChanged(e);
                        }
                    });
                    return ;
                }
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }
                ListSelectionModel lsm =
                        (ListSelectionModel)e.getSource ();
                if (lsm.isSelectionEmpty ()) {
                    //no rows are selected
                    showDetailsAtRow (-1);
                    popupActionsSupport.rowChanged (-1);
                } else {
                    int selectedRow = table.convertRowIndexToModel(lsm.getMinSelectionIndex());                    
                    popupActionsSupport.rowChanged (selectedRow);
                    //selectedRow is selected
                    Action action = null;
                    if (removeLocallyDownloaded != null && removeLocallyDownloaded.isEnabled()) {
                        action = removeLocallyDownloaded;
                    }
                    showDetailsAtRow (selectedRow, action);
                }
            }
        });
    }
    
    public void addUpdateUnitListener (UpdateUnitListener l) {
        model.addUpdateUnitListener (l);
    }
    
    public void removeUpdateUnitListener (UpdateUnitListener l) {
        model.removeUpdateUnitListener (l);
    }
    
    void fireUpdataUnitChange () {
        model.fireUpdataUnitChange ();
    }
    
    DocumentListener getDocumentListener () {
        if (dlForSearch == null) {
            dlForSearch = new DocumentListener () {
                @Override
                public void insertUpdate (DocumentEvent arg0) {
                    filter = tfSearch.getText ().trim ();
                    searchTask.schedule (350);
                }
                
                @Override
                public void removeUpdate (DocumentEvent arg0) {
                    insertUpdate (arg0);
                }
                
                @Override
                public void changedUpdate (DocumentEvent arg0) {
                    insertUpdate (arg0);
                }
                
            };
        }
        return dlForSearch;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lSelectionInfo = new javax.swing.JLabel();
        bTabAction = new javax.swing.JButton();
        lSearch = new javax.swing.JLabel();
        tfSearch = new javax.swing.JTextField();
        spTab = new javax.swing.JSplitPane();
        topButton = new javax.swing.JButton();
        lWarning = new javax.swing.JLabel();
        detailView = new javax.swing.JCheckBox();
        bDeactivate = new javax.swing.JButton();
        bUninstall = new javax.swing.JButton();

        lSearch.setLabelFor(tfSearch);
        org.openide.awt.Mnemonics.setLocalizedText(lSearch, org.openide.util.NbBundle.getMessage(UnitTab.class, "lSearch1.text")); // NOI18N

        spTab.setBorder(null);
        spTab.setDividerLocation(Integer.parseInt(NbBundle.getMessage (UnitTab.class, "UnitTab_Splitter_DefaultDividerLocation")));
        spTab.setResizeWeight(0.5);
        spTab.setOneTouchExpandable(true);

        org.openide.awt.Mnemonics.setLocalizedText(topButton, "jButton1");

        detailViewInit();
        org.openide.awt.Mnemonics.setLocalizedText(detailView, org.openide.util.NbBundle.getMessage(UnitTab.class, "UnitTab.detailView.text")); // NOI18N
        detailView.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                detailViewItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(topButton)
                        .addGap(18, 18, 18)
                        .addComponent(detailView)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 443, Short.MAX_VALUE)
                        .addComponent(lSearch)
                        .addGap(4, 4, 4)
                        .addComponent(tfSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(spTab, javax.swing.GroupLayout.DEFAULT_SIZE, 776, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bTabAction)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bDeactivate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bUninstall)
                        .addGap(8, 8, 8)
                        .addComponent(lSelectionInfo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lWarning, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
                        .addGap(99, 99, 99)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tfSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lSearch)
                    .addComponent(topButton)
                    .addComponent(detailView))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spTab, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER, false)
                    .addComponent(bTabAction)
                    .addComponent(lSelectionInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lWarning)
                    .addComponent(bDeactivate)
                    .addComponent(bUninstall))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {lSelectionInfo, lWarning});

        lSearch.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UnitTab.class, "ACD_Search")); // NOI18N
        topButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UnitTab.class, "ACN_Reload")); // NOI18N
        topButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UnitTab.class, "ACD_Reload")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void detailViewItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_detailViewItemStateChanged
        if (this.model.supportsTwoViews()) {
            manager.setDetailView(detailView.isSelected());
            uncheckAll();
            manager.updateUnitsChanged();
            System.setProperty(PluginManagerUI.DETAIL_VIEW_SELECTED_PROP, "" + detailView.isSelected());
        }
}//GEN-LAST:event_detailViewItemStateChanged

    private void detailViewInit() {
        detailView.setVisible(this.model.supportsTwoViews());
        if (this.model.supportsTwoViews()) {            
            detailView.setSelected(Boolean.getBoolean(PluginManagerUI.DETAIL_VIEW_SELECTED_PROP));
        }
    }
    
    private LocalDownloadSupport getLocalDownloadSupport () {
        return (model instanceof LocallyDownloadedTableModel) ? ((LocallyDownloadedTableModel)model).getLocalDownloadSupport () : null;
    }
    
    private Task reloadTask (final boolean force) {
        final Runnable checkUpdates = new Runnable (){
            @Override
            public void run () {
                ProgressHandle handle = ProgressHandleFactory.createHandle (NbBundle.getMessage (UnitTab.class,  ("UnitTab_ReloadAction")));
                JComponent progressComp = ProgressHandleFactory.createProgressComponent (handle);
                JLabel detailLabel = new JLabel (NbBundle.getMessage (UnitTab.class, "UnitTab_PrepareReloadAction"));
                manager.setProgressComponent (detailLabel, progressComp);
                handle.setInitialDelay (0);
                handle.start ();
                manager.initTask.waitFinished ();
                setWaitingState (true);
                if (getDownloadSizeTask != null && ! getDownloadSizeTask.isFinished ()) {
                    if (getDownloadSizeTask.getDelay () > 0) {
                        getDownloadSizeTask.cancel ();
                    } else {
                        getDownloadSizeTask.waitFinished ();
                    }
                }
                final int row = getSelectedRow ();
                final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
                if (model instanceof LocallyDownloadedTableModel) {
                    ((LocallyDownloadedTableModel) model).removeInstalledUnits ();
                    ((LocallyDownloadedTableModel) model).setUnits (null);
                }
                manager.unsetProgressComponent (detailLabel, progressComp);
                Utilities.presentRefreshProviders (manager, force);
                SwingUtilities.invokeLater (new Runnable () {
                    @Override
                    public void run () {
                        fireUpdataUnitChange ();
                        UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                        restoreSelectedRow (row);
                        refreshState ();                        
                        setWaitingState (false);
                    }
                });
            }
        };
        return Utilities.startAsWorkerThread (checkUpdates);
    }
    
    class ButtonActionSupport {

        private final TabAction[] actions;

        ButtonActionSupport(TabAction[] actions) {
            this.actions = actions;
        }

        void tableDataChanged() {
            if (actions != null) {
                Collection<Unit> units = model.getMarkedUnits();
                for (TabAction ta : actions) {
                    ta.tableDataChanged(units);
                }
            }
        }
    }

    class PopupActionSupport extends MouseAdapter implements Runnable {
        private final RowTabAction[] actions;
        
        PopupActionSupport (RowTabAction[] actions) {
            this.actions = actions;
        }
        
        void rowChanged (int row) {
            Unit u = null;
            if (row > -1) {
                u = model.getUnitAtRow (row);
            }
            
            for (TabAction action : actions) {
                if (action instanceof RowTabAction) {
                    RowTabAction rowAction = (RowTabAction)action;
                    rowAction.unitChanged (row, u);
                }
            }
        }
        
        void tableDataChanged () {
            Collection<Unit> units = model.getMarkedUnits ();
            for (RowTabAction action : actions) {
                action.tableDataChanged (units);
            }
        }
        
        private JPopupMenu createPopup () {
            JPopupMenu popup = new JPopupMenu ();
            for (RowTabAction action : actions) {
                if (action.isVisible ()) {
                    popup.add (new JMenuItem (action));
                }
            }
            return popup;
        }
        
        @Override
        public void mousePressed (MouseEvent e) {
            maybeShowPopup (e);
        }
        
        @Override
        public void mouseReleased (MouseEvent e) {
            maybeShowPopup (e);
        }
        
        @Override
        public void mouseClicked (MouseEvent e) {
            if (!maybeShowPopup (e)) {
                int row = UnitTab.this.table.rowAtPoint(e.getPoint());
                if (model.isExpansionControlAtRow(row)) {
                    moreAction.unitChanged(row, null);
                    lessAction.unitChanged(row, null);
                    if (moreAction.isEnabled()) {
                        moreAction.performAction();
                    } else if (lessAction.isEnabled()) {
                        lessAction.performAction();
                    }                    
                }
            }
        }
        
        private boolean maybeShowPopup (MouseEvent e) {
            if (e.isPopupTrigger ()) {  
                focusTable ();
                showPopup (e.getPoint (), e.getComponent ());
                return true;
            }
            return false;
        }

        @Override
        public void run () {
            Point p = getPositionForPopup();

            if (p != null) {
                showPopup (p, table);
            }
        }
        
        private Point getPositionForPopup () {
            int r = table.getSelectedRow ();
            int c = table.getSelectedColumn ();

            if (r < 0 || c < 0) {
                return null;
            }

            Rectangle rect = table.getCellRect (r, c, false);

            if (rect == null) {
                return null;
            }

            return SwingUtilities.convertPoint (table, rect.x, rect.y, table);
        }

        public final Action popupOnF10 = new AbstractAction () {

            @Override
            public void actionPerformed (ActionEvent evt) {
                popupActionsSupport.run ();
            }

            @Override
            public boolean isEnabled () {
                return table.isFocusOwner ();
            }
        };

    }
    
    private void showPopup (Point e, Component invoker) {
        int row = UnitTab.this.table.rowAtPoint (e);
        if (row >= 0) {
            table.getSelectionModel ().setSelectionInterval (row, row);
            final JPopupMenu finalPopup = popupActionsSupport.createPopup ();
            if (finalPopup != null && finalPopup.getComponentCount () > 0) {
                finalPopup.show (invoker,e.x, e.y);
                
            }
        }
    }
    
    private int getSelectedRow () {
        return table.getSelectedRow ();
    }
    private void restoreSelectedRow (int row) {
        if (row < 0) {
            row = 0;
        }
        for(int temp = row; temp >= 0; temp--) {
            if (temp < table.getRowCount () && temp > -1) {
                table.getSelectionModel ().setSelectionInterval (temp, temp);
                break;
            }
        }
    }    
    int findRow(String codeName) {
        for (int i = 0; i < model.getRowCount();i++) {
            Unit u = model.getUnitAtRow(i);
            if (u != null && codeName.equals(u.updateUnit.getCodeName())) {
                return i;
            }
        }
        return -1;
    } 
    
    
    private void initReloadTooltip() {
        Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate");//NOI18N
        long lastTime = p.getLong(PROP_LAST_CHECK, 0);
        if (lastTime > 0) {
            topButton.setToolTipText("<html>"+NbBundle.getMessage(UnitTab.class, "UnitTab_ReloadTime", //NOI18N
                    "<b>"+new SimpleDateFormat().format(new Date(lastTime)) + "</b>")+"</html>");
        } else {
            String never = NbBundle.getMessage(UnitTab.class, "UnitTab_ReloadTime_Never");//NOI18N
            topButton.setToolTipText("<html>"+NbBundle.getMessage(UnitTab.class, "UnitTab_ReloadTime", "<b>"+never+"</b>") + "/<html>");//NOI18N
        }
        if (preferenceChangeListener == null) {
            preferenceChangeListener = new PreferenceChangeListener() {

                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                             initReloadTooltip();
                        }
                    });
                }
            };
            p.addPreferenceChangeListener(preferenceChangeListener);
        }
    }
    
    
    static String textForKey (String key) {
        JButton jb = new JButton ();
        Mnemonics.setLocalizedText (jb, NbBundle.getMessage (UnitTab.class, key));
        return jb.getText ();
    }
    
    static int mnemonicForKey(String key) {
        JButton jb = new JButton();
        Mnemonics.setLocalizedText(jb, NbBundle.getMessage(UnitTab.class, key));
        return jb.getMnemonic();
    }
    
    private  abstract class TabAction extends AbstractAction {
        private String name;
        private String actionCategory;
        public TabAction (String nameKey, KeyStroke accelerator, String actionCategoryKey) {
            super (textForKey (nameKey));
            this.actionCategory = actionCategoryKey;//(actionCategoryKey != null) ? NbBundle.getMessage(UnitTab.class, actionCategoryKey) : null;
            if(accelerator!=null) {
                putValue (ACCELERATOR_KEY, accelerator);
            }

            putValue (MNEMONIC_KEY, mnemonicForKey (nameKey));
            name = (String)getValue (NAME);
            putIntoActionMap (table);
        }
        
        public TabAction (String key, String actionCategoryKey) {
            this (key, null, actionCategoryKey);
        }
        
        protected String getActionName () {
            return name;
        }
        
        public String getActionCategory () {
            return getActionCategoryImpl ();//NOI18N
        }
        
        protected String getActionCategoryImpl () {
            return actionCategory;
        }
        
        protected void setContextName (String name) {
            putValue (NAME, name);
        }
        
        @Override
        public void setEnabled (boolean enabled) {
            if (isEnabled () != enabled) {
                if (enabled) {
                    RequestProcessor.Task t = PluginManagerUI.getRunningTask (new Runnable () {

                        @Override
                        public void run() {
                            setEnabled (true);
                        }
                    });
                    if (t != null && ! t.isFinished ()) {
                        t.addTaskListener (new TaskListener () {
                            @Override
                            public void taskFinished (org.openide.util.Task task) {
                                setEnabled (true);
                            }
                        });
                    } else {
                        super.setEnabled (true);
                    }
                } else {
                    super.setEnabled (false);
                }
            }
        }
        
        private void putIntoActionMap (JComponent component) {
            KeyStroke ks = (KeyStroke)getValue (ACCELERATOR_KEY);
            Object key = getValue (NAME);
            if (ks == null) {
                ks = KeyStroke.getKeyStroke ((Integer)getValue (MNEMONIC_KEY), KeyEvent.ALT_DOWN_MASK);
            }
            if (ks != null && key != null) {
                component.getInputMap (JComponent.WHEN_FOCUSED).put (ks, key);
                component.getActionMap ().put (key,this);
            }
        }
        
        public final void performAction () {
            if (isEnabled ()) {
                actionPerformed (null);
            }
        }
        @Override
        public final void actionPerformed (ActionEvent e) {
            try {
                performerImpl ();
            } finally {
            }
        }
        
        
        public void tableDataChanged () {
            tableDataChanged (model.getMarkedUnits ());
        }
        
        public void tableDataChanged (Collection<Unit> units) {
            setEnabled (units.size () > 0);
        }
        
        
        public abstract void performerImpl ();
    }
    
    private abstract class RowTabAction extends TabAction {
        private Unit u;
        private int row;
        public RowTabAction (String nameKey, String actionCategoryKey) {
            super (nameKey, actionCategoryKey);
        }
        
        public RowTabAction (String nameKey, KeyStroke accelerator, String actionCategoryKey) {
            super (nameKey, accelerator, actionCategoryKey);
        }
        public void unitChanged (int row, Unit u) {
            this.u = u;
            this.row = row;
            unitChanged();
            }
        public final boolean isVisible (){
            return (u != null) ? isVisible (u) : isVisible(row);
        }
        private void unitChanged () {
            if (u != null) {
                setEnabled (isEnabled (u));
                setContextName (getContextName (u));
            } else {
                setEnabled (isEnabled(row));
                setContextName (getContextName(row));
            }
        }
        
        @Override
        public void tableDataChanged () {
            unitChanged ();
        }
        
        @Override
        public void tableDataChanged (Collection<Unit> units) {
            unitChanged ();
        }
        
        @Override
        public final  void performerImpl () {
            performerImpl (u);
        }
        protected boolean isVisible (Unit u) {
            return u != null;
        }
        protected boolean isVisible (int row) {
            return false;
        }        
        public abstract void performerImpl (Unit u);
        protected abstract boolean isEnabled (Unit u);
        protected boolean isEnabled (int row) {
            return false;
        }
        protected abstract String getContextName (Unit u);
        protected String getContextName (int row) {
            return getActionName();
        }
    }
    
    private class CheckAction extends RowTabAction {
        public CheckAction () {
            super ("UnitTab_CheckAction", KeyStroke.getKeyStroke (KeyEvent.VK_SPACE, 0), null);
        }
        
        @Override
        public void performerImpl (Unit u) {
            final int row = getSelectedRow();
            if (model.isExpansionControlAtRow(row)) {
                if (moreAction != null && moreAction.isEnabled()) {
                    moreAction.performAction();
                } else if (lessAction != null && lessAction.isEnabled()) {
                    lessAction.performAction();
                }
            } else if (u != null && u.canBeMarked ()) {
                u.setMarked (!u.isMarked ());
            } 
            model.fireTableDataChanged ();
            restoreSelectedRow(row);
        }

        
        @Override
        protected boolean isEnabled (Unit u) {
            return u != null && u.canBeMarked ();
        }
        
        @Override
        protected boolean isEnabled (int row) {
            return model.isExpansionControlAtRow(row);
        }
        
        
        @Override
        protected String getContextName (Unit u) {
            return getActionName ();
        }
        
        @Override
        protected boolean isVisible (Unit u) {
            return false;
        }        
        @Override
        protected boolean isVisible (int row) {
            return false;
        }        
        
    }
    
    private class UninstallAction extends TabAction {
        public UninstallAction () {
            super ("UnitTab_bTabAction_Name_INSTALLED", null);
        }
        
        @Override
        public void performerImpl () {
            boolean wizardFinished = false;
            final int row = getSelectedRow ();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            UninstallUnitWizard wizard = new UninstallUnitWizard ();
            try {
                wizardFinished = wizard.invokeWizard ();
            } finally {
                Containers.forUninstall ().removeAll ();
                Containers.forDisable().removeAll ();
                Containers.forEnable().removeAll ();
                fireUpdataUnitChange ();
                if (!wizardFinished) {
                    UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                }
                restoreSelectedRow(row);
                refreshState ();
                focusTable ();
            }
        }

        @Override
        public void tableDataChanged (Collection<Unit> units) {
            if (units.isEmpty()) {
                setEnabled(false);
                return;
            }
            for (Unit u : units) {
                if (u instanceof Unit.Installed) {
                    Unit.Installed inst = (Unit.Installed)u;
                    if (inst.isUninstallAllowed()) {
                        setEnabled(true);
                        return;
                    }
                }
            }
            setEnabled (false);
        }

    }
    
    private class UpdateAction extends TabAction {
        public UpdateAction () {
            super ("UnitTab_bTabAction_Name_UPDATE", null);
        }
        
        @Override
        public void performerImpl () {
            boolean wizardFinished = false;
            final int row = getSelectedRow();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits());
            try {
                wizardFinished = new InstallUnitWizard ().invokeWizard (OperationType.UPDATE, manager);
            } finally {
                if (manager == null || ! manager.isClosing()) {
                    //must be called before restoreState
                    fireUpdataUnitChange ();
                    if (!wizardFinished) {
                        UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                    }
                    restoreSelectedRow(row);                
                    refreshState ();
                    focusTable ();
                }
            }
        }
    }
    
    private class UpdateRowAction extends RowTabAction {
        public UpdateRowAction() {
            super("UnitTab_UpdateRowAction", null);
        }

        @Override
        protected boolean isEnabled(Unit u) {
            boolean retval = false;
            if (u instanceof Unit.Update) {
                retval = u.canBeMarked();
            }
            return retval;
        }

        @Override
        public void performerImpl(Unit u) {
            final int row = getSelectedRow ();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            Containers.forUpdate().removeAll();
            if (u instanceof Unit.CompoundUpdate) {
                Unit.CompoundUpdate uc = (Unit.CompoundUpdate) u;
                OperationContainer container = Containers.forUpdate();
                for (UpdateUnit invisible : uc.getUpdateUnits()) {
                    if (container.canBeAdded(invisible, invisible.getAvailableUpdates().get(0))) {
                        container.add(invisible, invisible.getAvailableUpdates().get(0));
                    }
                }
            } else {
                Containers.forUpdate().add(u.updateUnit, u.getRelevantElement());
            }
            boolean finished = false;
            try {
                if (Containers.forUpdate().listAll().size() > 0) {
                    finished = new InstallUnitWizard ().invokeWizard (OperationType.UPDATE, manager);
                }
            } finally {
                Containers.forUpdate().removeAll();
                if (manager == null || ! manager.isClosing()) {
                    Containers.forUpdate().removeAll();
                    fireUpdataUnitChange();
                    if (!finished) {
                        UnitCategoryTableModel.restoreState(model.getUnits(), state, model.isMarkedAsDefault());
                    }
                    restoreSelectedRow(row);
                    refreshState();
                    focusTable();
                }
            }

        }

        @Override
        protected String getContextName(Unit u) {
            if (u != null) {
                return getActionName() + " \"" + u.getDisplayName() + "\""; //NOI18N
            }
            return getActionName();
        }
        
    }
    
    private class AvailableAction extends TabAction {
        public AvailableAction () {
            super ("UnitTab_bTabAction_Name_AVAILABLE", null);
        }
        
        @Override
        public void performerImpl () {
            boolean wizardFinished = false;
            final int row = getSelectedRow();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits());
            try {
                wizardFinished = new InstallUnitWizard ().invokeWizard (OperationType.INSTALL, manager);
            } finally {
                if (manager != null) {
                    fireUpdataUnitChange ();
                }
                if (!wizardFinished) {
                    UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                }
                restoreSelectedRow(row);
                refreshState ();
                focusTable ();
            }
        }
    }
    
    private class AvailableRowAction extends RowTabAction {
        public AvailableRowAction() {
            super("UnitTab_InstallRowAction", null);
        }

        @Override
        protected boolean isEnabled(Unit u) {
            boolean retval = false;
            if (u instanceof Unit.Available) {
                retval = u.canBeMarked();
            }
            return retval;
        }

        @Override
        public void performerImpl(Unit u) {
            final int row = getSelectedRow ();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            Containers.forAvailable().removeAll();
            if (u instanceof Unit.Available) {
                Containers.forAvailable().add(u.updateUnit, u.getRelevantElement());
            }
            boolean finished = false;
            try {
                if (Containers.forAvailable().listAll().size() > 0) {
                    finished = new InstallUnitWizard ().invokeWizard (OperationType.INSTALL, manager);
                }
            } finally {
                Containers.forAvailable().removeAll();
                if (manager == null || ! manager.isClosing()) {
                    fireUpdataUnitChange();
                    if (!finished) {
                        UnitCategoryTableModel.restoreState(model.getUnits(), state, model.isMarkedAsDefault());
                    }
                    restoreSelectedRow(row);
                    refreshState();
                    focusTable();
                }
            }

        }

        @Override
        protected String getContextName(Unit u) {
            if (u != null) {
                return getActionName() + " \"" + u.getDisplayName() + "\""; //NOI18N
            }
            return getActionName();
        }
        
    }
    
    private class LocalUpdateAction extends TabAction {
        public LocalUpdateAction () {
            super ("UnitTab_bTabAction_Name_LOCAL", null);
        }
        @Override
        public void performerImpl () {
            boolean wizardFinished = false;
            final int row = getSelectedRow();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            
            try {
                wizardFinished = new InstallUnitWizard ().invokeWizard (OperationType.LOCAL_DOWNLOAD, manager);
            } finally {
                // fireUpdataUnitChange ();
                if (wizardFinished) {
                    reloadTask (false).schedule (10);
                } else {
                    fireUpdataUnitChange ();
                    if (!wizardFinished) {
                        UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                    }
                    restoreSelectedRow(row);                
                    refreshState ();
                }
                focusTable ();
            }
        }
    }
    
    private class LocalUpdateRowAction extends RowTabAction {
        public LocalUpdateRowAction() {
            super("UnitTab_LocalUpdateRowAction", null);
        }

        @Override
        protected boolean isEnabled(Unit u) {
            boolean retval = false;
            if (u != null) {
                retval = u.canBeMarked();
            }
            return retval;
        }

        @Override
        public void performerImpl(Unit u) {
            final int row = getSelectedRow ();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            Containers.forAvailableNbms().removeAll();
            Containers.forUpdateNbms().removeAll();
            if (u instanceof Unit.Available) {
                Containers.forAvailableNbms().add(u.updateUnit, u.getRelevantElement());
            } else if (u instanceof Unit.Update) {
                Containers.forUpdateNbms().add(u.updateUnit, u.getRelevantElement());
            }
            boolean finished = false;
            try {
                if (Containers.forAvailableNbms().listAll().size() > 0 || Containers.forUpdateNbms().listAll().size() > 0) {
                    finished = new InstallUnitWizard ().invokeWizard (OperationType.LOCAL_DOWNLOAD, manager);
                }
            } finally {
                Containers.forAvailableNbms().removeAll();
                Containers.forUpdateNbms().removeAll();
                if (manager == null || ! manager.isClosing()) {
                    fireUpdataUnitChange();
                    if (!finished) {
                        UnitCategoryTableModel.restoreState(model.getUnits(), state, model.isMarkedAsDefault());
                    }
                    restoreSelectedRow(row);
                    refreshState();
                    focusTable();
                }
            }

        }

        @Override
        protected String getContextName(Unit u) {
            if (u != null) {
                return getActionName() + " \"" + u.getDisplayName() + "\""; //NOI18N
            }
            return getActionName();
        }
        
    }
    
    private class CheckCategoryAction extends RowTabAction {
        protected CheckCategoryAction (String nameKey,KeyStroke stroke, String actionCategoryKey) {
            super (nameKey, stroke, actionCategoryKey);
        }
        public CheckCategoryAction () {
            super ("UnitTab_CheckCategoryAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),*/ "Check");
        }
        @Override
        protected boolean isEnabled (Unit u) {
            boolean retval = false;
            String category = u.getCategoryName();
            List<Unit> units = model.getUnits();
            for (Unit unit : units) {
                if (unit != null && category.equals(unit.getCategoryName()) && !unit.isMarked() && unit.canBeMarked()) {
                    retval = true;
                    break;
                }
            }
            return retval;
        }
        @Override
        protected String getContextName (Unit u) {
            return getActionName () + " \"" + u.getCategoryName ()+"\"";//NOI18N
        }
        @Override
        public void performerImpl (Unit u) {
            String category = u.getCategoryName ();
            int count = model.getRowCount ();
            final int row = getSelectedRow();        
            for (int i = 0; i < count; i++) {
                u = model.getUnitAtRow (i);
                if (u != null && category.equals (u.getCategoryName ()) && !u.isMarked () && u.canBeMarked ()) {
                    u.setMarked (true);
                }
            }
            model.fireTableDataChanged ();
            restoreSelectedRow(row);
        }
                
        @Override
        protected boolean isVisible (Unit u) {
            if (u.getRelevantElement().getUpdateUnit().getType() == UpdateManager.TYPE.FEATURE) {
                return false;
            }
            return super.isVisible (u);
        }
    }
    
    private class ActivateAction extends TabAction {
        public ActivateAction () {
            super ("UnitTab_ActivateAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK),*/ "EnableDisable");
        }

        @Override
        public void tableDataChanged(Collection<Unit> units) {
            if (units.isEmpty()) {
                setEnabled(false);
                return;
            }
            for (Unit u : units) {
                if (isEnabled(u)) {
                    setEnabled(true);
                    return ;
                }
            }
            setEnabled(false);
        }
        
        boolean finished = false;
        Map<String, Boolean> state;
        
        void clearContainers() {
            Containers.forEnable().removeAll();
            Containers.forDisable().removeAll();
            Containers.forAvailable().removeAll();
            Containers.forUninstall ().removeAll();
        }
        
        /**
         * Forces refresh of UI data. Replans work to EDT, but waits for the result.
         * First waits until the download/install task completes, so that UI models
         * for checking items are not suspended by the pending operation.
         * <p/>
         * Will <b>clean all containers</b> and populate them again from the UI.
         * @return newly populated Container
         */
        OperationContainer refreshData() {
            if (!SwingUtilities.isEventDispatchThread()) {
                final OperationContainer[] c = new OperationContainer[1];
                RequestProcessor.Task runningTask = PluginManagerUI.getRunningTask();
                if (runningTask != null) {
                    runningTask.waitFinished();
                }
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        c[0] = refreshData();
                    });
                } catch (InterruptedException | InvocationTargetException ex) {
                    OperationContainer x = Containers.forEnable();
                    x.removeAll();
                }
                return c[0];
            }
            refreshState ();
            try {
                clearContainers();
                fireUpdataUnitChange();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            if(!finished) {
                UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
            }
            return prepareContainer();
        }
        
        OperationContainer prepareContainer() {
            OperationContainer<OperationSupport> c = Containers.forEnable();
            Collection<Unit> units = new ArrayList<>();
            for (Unit u : model.getUnits()) {
                if (u.isMarked() && isEnabled(u)) {
                    units.add(u);
                }
            }
            c.removeAll();
            for (Unit u : units) {
                    c.add(u.updateUnit, u.getRelevantElement());
            }
            return c;
        }

        @Override
        public void performerImpl() {
            final int row = getSelectedRow ();
            state = UnitCategoryTableModel.captureState (model.getUnits ());
            UninstallUnitWizard wizard = new UninstallUnitWizard ();
            finished = false;
            // unused, but hold the reference on stack.
            OperationContainer c = prepareContainer();
            try {
                finished = wizard.invokeWizard(true, this::refreshData);
            } finally {
                clearContainers();
                if (finished) {
                    for (Unit u : model.getMarkedUnits()) {
                        u.setMarked(false);
                    }
                }
                fireUpdataUnitChange();
                if(!finished) {
                    UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                }
                restoreSelectedRow(row);
                refreshState ();
                focusTable ();
            }
        }

        protected boolean isEnabled (Unit u) {
            boolean retval = false;
            if (u instanceof Unit.Installed) {
                Unit.Installed i = (Unit.Installed)u;
                if (!i.getRelevantElement ().isEnabled ()) {
                     retval = Unit.Installed.isOperationAllowed (u.updateUnit, u.getRelevantElement (), Containers.forEnable ());
                }
            }
            return  retval;
        }
        protected String getContextName (Unit u) {
            return getActionName ();
        }
    }
    
    private class ActivateCategoryAction extends RowTabAction {
        public ActivateCategoryAction () {
            super ("UnitTab_ActivateCategoryAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK),*/ "EnableDisable");
        }
        
        @Override
        protected boolean isEnabled (Unit uu) {
            boolean retval = false;
            
            String category = uu.getCategoryName();
            List<Unit> units = model.getUnits();
            for (Unit u : units) {
                if ((u instanceof Unit.Installed) && category.equals(u.getCategoryName())) {
                    Unit.Installed installed = (Unit.Installed) u;
                    if (!installed.getRelevantElement().isEnabled()) {
                        retval = Unit.Installed.isOperationAllowed(installed.updateUnit, installed.getRelevantElement(), Containers.forEnable());
                    }
                }
            }
            
            return  retval;
        }
        @Override
        protected String getContextName (Unit u) {
            if (u instanceof Unit.Installed) {
                return getActionName ()+ " \"" + u.getCategoryName () + "\"";
            }
            return getActionName ();
        }
        @Override
        public void performerImpl (Unit uu) {
            Unit.Installed unit = (Unit.Installed)uu;
            final int row = getSelectedRow();
            
            String category = unit.getCategoryName ();
            int count = model.getRowCount ();
            for (int i = 0; i < count; i++) {
                Unit u = model.getUnitAtRow (i);
                if ((u instanceof Unit.Installed) && category.equals(u.getCategoryName())) {
                    Unit.Installed installed = (Unit.Installed)u;
                    if (!installed.getRelevantElement().isEnabled() && !installed.updateUnit.isPending()) {
                        OperationInfo info = Containers.forEnable ().add (installed.updateUnit, installed.getRelevantElement ());
                        if (info ==  null) {
                            Logger.getLogger(UnitTab.class.getName()).log(Level.WARNING, "Null OperationInfo for {0}", installed.getRelevantElement());
                        }
                    }
                }
            }
            
            if (Containers.forEnable ().listAll ().size () > 0) {
                UninstallUnitWizard wizard = new UninstallUnitWizard ();
                wizard.invokeWizard (true);
                Containers.forEnable ().removeAll ();
            }
            fireUpdataUnitChange ();
            restoreSelectedRow(row);
            focusTable();
        }
                
        @Override
        protected boolean isVisible (Unit u) {
            if (u.getRelevantElement().getUpdateUnit().getType() == UpdateManager.TYPE.FEATURE) {
                return false;
            }
            return isEnabled();
        }
    }
    
    private class DeactivateAction extends TabAction {
        public DeactivateAction () {
            super ("UnitTab_DeactivateAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK),*/ "EnableDisable");
        }
        
        @Override
        public void tableDataChanged(Collection<Unit> units) {
            if (units.isEmpty()) {
                setEnabled(false);
                return;
            }

            for (Unit u : units) {
                if (isEnabled(u)) {
                    setEnabled(true);
                    return;
                }
            }
            setEnabled(false);
        }

        @Override
        public void performerImpl() {
            final int row = getSelectedRow ();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            OperationContainer<OperationSupport> c = Containers.forDisable();
            for (Unit u : model.getUnits()) {
                if (u.isMarked() && isEnabled(u)) {
                    c.add(u.updateUnit, u.getRelevantElement());
                }
            }
            UninstallUnitWizard wizard = new UninstallUnitWizard ();
            boolean finished = false;
            try {
                finished = wizard.invokeWizard(false);
            } finally {
                Containers.forUninstall().removeAll();
                Containers.forDisable().removeAll();
                Containers.forEnable().removeAll();
                fireUpdataUnitChange();
                if (!finished) {
                    UnitCategoryTableModel.restoreState(model.getUnits(), state, model.isMarkedAsDefault());
                }
                restoreSelectedRow(row);
                refreshState();
                focusTable();
            }
        }

        protected boolean isEnabled (Unit u) {
            boolean retval = false;
            if (u instanceof Unit.Installed) {
                Unit.Installed i = (Unit.Installed)u;
                if (i.getRelevantElement ().isEnabled ()) {
                    retval = Unit.Installed.isOperationAllowed (u.updateUnit, u.getRelevantElement (), Containers.forDisable ());
                }
            }
            return  retval;
        }
        protected String getContextName (Unit u) {
            return getActionName ();
        }
    }
    
    private class ActivateRowAction extends RowTabAction {
        public ActivateRowAction() {
            super("UnitTab_ActivateRowAction", "EnableDisable");
        }

        @Override
        protected boolean isEnabled(Unit u) {
            boolean retval = false;
            if (u instanceof Unit.Installed) {
                Unit.Installed i = (Unit.Installed) u;
                if (! i.getRelevantElement().isEnabled()) {
                    retval = Unit.Installed.isOperationAllowed(u.updateUnit, u.getRelevantElement(), Containers.forEnable());
                }
            }
            return retval;
        }

        @Override
        public void performerImpl(Unit u) {
            final int row = getSelectedRow ();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            Containers.forEnable().removeAll();
            Containers.forEnable().add(u.updateUnit, u.getRelevantElement());
            UninstallUnitWizard wizard = new UninstallUnitWizard ();
            boolean finished = false;
            try {
                if (Containers.forEnable().listAll().size() > 0) {
                    finished = wizard.invokeWizard(true);
                }
            } finally {
                Containers.forUninstall().removeAll();
                Containers.forDisable().removeAll();
                Containers.forEnable().removeAll();
                fireUpdataUnitChange();
                if (!finished) {
                    UnitCategoryTableModel.restoreState(model.getUnits(), state, model.isMarkedAsDefault());
                }
                restoreSelectedRow(row);
                refreshState();
                focusTable();
            }
        }

        @Override
        protected String getContextName(Unit u) {
            if (u instanceof Unit.Installed) {
                return getActionName() + " \"" + u.getDisplayName() + "\""; //NOI18N
            }
            return getActionName();
        }
        
    }
    
    private class DeactivateRowAction extends RowTabAction {
        public DeactivateRowAction() {
            super("UnitTab_DeactivateRowAction", "EnableDisable");
        }

        @Override
        protected boolean isEnabled(Unit u) {
            boolean retval = false;
            if (u instanceof Unit.Installed) {
                Unit.Installed i = (Unit.Installed) u;
                if (i.getRelevantElement().isEnabled()) {
                    retval = Unit.Installed.isOperationAllowed(u.updateUnit, u.getRelevantElement(), Containers.forDisable());
                }
            }
            return retval;
        }

        @Override
        public void performerImpl(Unit u) {
            final int row = getSelectedRow ();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            Containers.forDisable().removeAll();
            Containers.forDisable().add(u.updateUnit, u.getRelevantElement());
            UninstallUnitWizard wizard = new UninstallUnitWizard ();
            boolean finished = false;
            try {
                if (Containers.forDisable().listAll().size() > 0) {
                    finished = wizard.invokeWizard(false);
                }
            } finally {
                Containers.forUninstall().removeAll();
                Containers.forDisable().removeAll();
                Containers.forEnable().removeAll();
                fireUpdataUnitChange();
                if (!finished) {
                    UnitCategoryTableModel.restoreState(model.getUnits(), state, model.isMarkedAsDefault());
                }
                restoreSelectedRow(row);
                refreshState();
                focusTable();
            }
        }

        @Override
        protected String getContextName(Unit u) {
            if (u instanceof Unit.Installed) {
                return getActionName() + " \"" + u.getDisplayName() + "\""; //NOI18N
            }
            return getActionName();
        }
        
    }
    
    private class UninstallRowAction extends RowTabAction {
        public UninstallRowAction() {
            super("UnitTab_UninstallRowAction", "EnableDisable");
        }

        @Override
        protected boolean isEnabled(Unit u) {
            boolean retval = false;
            if (u instanceof Unit.Installed) {
                retval = ((Unit.Installed)u).isUninstallAllowed();
            }
            return retval;
        }

        @Override
        public void performerImpl(Unit u) {
            final int row = getSelectedRow ();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            Containers.forUninstall().removeAll();
            Containers.forUninstall().add(u.updateUnit, u.getRelevantElement());
            UninstallUnitWizard wizard = new UninstallUnitWizard ();
            boolean finished = false;
            try {
                if (Containers.forUninstall().listAll().size() > 0) {
                    finished = wizard.invokeWizard();
                }
            } finally {
                Containers.forUninstall().removeAll();
                Containers.forDisable().removeAll();
                Containers.forEnable().removeAll();
                fireUpdataUnitChange();
                if (!finished) {
                    UnitCategoryTableModel.restoreState(model.getUnits(), state, model.isMarkedAsDefault());
                }
                restoreSelectedRow(row);
                refreshState();
                focusTable();
            }
        }

        @Override
        protected String getContextName(Unit u) {
            if (u instanceof Unit.Installed) {
                return getActionName() + " \"" + u.getDisplayName() + "\""; //NOI18N
            }
            return getActionName();
        }
        
    }
    
    private class DeactivateCategoryAction extends RowTabAction {
        public DeactivateCategoryAction () {
            super ("UnitTab_DeactivateCategoryAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK),*/ "EnableDisable");
        }
        
        @Override
        protected boolean isEnabled (Unit uu) {
            boolean retval = false;
            
            String category = uu.getCategoryName();
            List<Unit> units = model.getUnits();
            for (Unit u : units) {
                if ((u instanceof Unit.Installed) && category.equals(u.getCategoryName())) {
                    Unit.Installed installed = (Unit.Installed) u;
                    if (installed.getRelevantElement().isEnabled()) {
                        retval = Unit.Installed.isOperationAllowed(installed.updateUnit, installed.getRelevantElement(), Containers.forDisable());
                    }
                }
            }
            return  retval;
        }
        
        @Override
        protected String getContextName (Unit u) {
            if (u instanceof Unit.Installed) {
                return getActionName ()+ " \"" + u.getCategoryName () + "\"";//NOI18N
            }
            return getActionName ();
        }
        @Override
        public void performerImpl (Unit uu) {
            Unit.Installed unit = (Unit.Installed)uu;
            final int row = getSelectedRow();
            
            String category = unit.getCategoryName ();
            int count = model.getRowCount ();
            for (int i = 0; i < count; i++) {
                Unit u = model.getUnitAtRow (i);
                if ((u instanceof Unit.Installed) && category.equals(u.getCategoryName())) {
                    Unit.Installed installed = (Unit.Installed)u;
                    if (installed.getRelevantElement().isEnabled() && !installed.updateUnit.isPending()) {
                        OperationInfo info = Containers.forDisable ().add (installed.updateUnit, installed.getRelevantElement ());
                        // Issue #169640
                        // The relevant element can actually be present in forDisable container
                        // due to current Unit.Installed.setMarked() implementation as
                        // it operates on the the same ("global") Containers.forDisable() container.
                        // That is the reason for the commenting the following assertion originally presented
                        
                        //assert info != null;
                    }
                }
            }
            
            if (Containers.forDisable ().listAll ().size () > 0) {
                UninstallUnitWizard wizard = new UninstallUnitWizard ();
                wizard.invokeWizard (false);
                Containers.forDisable ().removeAll ();
            }
            fireUpdataUnitChange ();
            restoreSelectedRow(row);
            focusTable ();            
        }
        @Override
        protected boolean isVisible (Unit u) {
            if (u.getRelevantElement().getUpdateUnit().getType() == UpdateManager.TYPE.FEATURE) {
                return false;
            }
            return isEnabled();
        }
    }
    
    private class UncheckCategoryAction extends RowTabAction {
        public UncheckCategoryAction () {
            super ("UnitTab_UncheckCategoryAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),*/ "Uncheck");
        }
        @Override
        protected boolean isEnabled (Unit u) {
            boolean retval = false;
            
            String category = u.getCategoryName();
            List<Unit> units = model.getUnits();
            for (Unit uu : units) {
                if (uu != null && category.equals(uu.getCategoryName()) && uu.isMarked()) {
                    retval = true;
                    break;
                }
            }
            return retval;
        }
        
        @Override
        public void performerImpl (Unit u) {
            String category = u.getCategoryName ();
            final int row = getSelectedRow();
            
            int count = model.getRowCount ();
            for (int i = 0; i < count; i++) {
                u = model.getUnitAtRow (i);
                if (u != null && category.equals (u.getCategoryName ()) && u.isMarked () && u.canBeMarked ()) {
                    u.setMarked (false);
                }
            }
            model.fireTableDataChanged ();
            restoreSelectedRow(row);
            focusTable();
        }
        
        @Override
        protected boolean isVisible (Unit u) {
            if (u.getRelevantElement().getUpdateUnit().getType() == UpdateManager.TYPE.FEATURE) {
                return false;
            }
            return super.isVisible(u);
        }
        
        @Override
        protected String getContextName (Unit u) {
            return getActionName () + " \"" + u.getCategoryName ()+"\""; //NOI18N
        }
    }
    
    private void uncheckAll() {
        Collection<Unit> markedUnits = model.getMarkedUnits();
        for (Unit u : markedUnits) {
            if (u != null && u.isMarked() && u.canBeMarked()) {
                u.setMarked(false);
            }
        }
        model.fireTableDataChanged();
    }
    
    private class CheckAllAction extends RowTabAction {
        public CheckAllAction () {
            super ("UnitTab_CheckAllAction", KeyStroke.getKeyStroke (KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK), "Check");
        }
        
        @Override
        public void performerImpl (Unit uu) {
            final int row = getSelectedRow();
            Collection<Unit> allUnits = model.getUnits();
            for (Unit u : allUnits) {
                if (u != null && !u.isMarked () &&  u.canBeMarked ()) {
                    u.setMarked (true);
                }                
            }
            model.fireTableDataChanged ();
            restoreSelectedRow(row);
        }
        
        @Override
        protected boolean isEnabled (Unit uu) {
            return true;
        }
        @Override
        protected String getContextName (Unit u) {
            return getActionName ();
        }
    }
    
    private class UncheckAllAction extends RowTabAction {
        public UncheckAllAction () {
            super ("UnitTab_UncheckAllAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), */"Uncheck");
        }
        @Override
        public void performerImpl (Unit uu) {
            final int row = getSelectedRow();
            uncheckAll();
            model.fireTableDataChanged ();            
            restoreSelectedRow(row);
        }
        
        @Override
        protected boolean isEnabled (Unit uu) {
            return true;
        }
        
        @Override
        protected String getContextName (Unit u) {
            return getActionName ();
        }
    }
    
    private class MoreAction extends RowTabAction {
        public MoreAction () {
            super ("UnitTab_MoreAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), */"Expand");//NOI18N
        }
        @Override
        public void performerImpl (Unit uu) {
            try {
                setWaitingState(true);
                final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());            
                model.setExpanded(true);
                fireUpdataUnitChange ();
                UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                focusTable();
            } finally {
                setWaitingState(false);
            }
        }

        @Override
        protected boolean isVisible(Unit u) {
            return isEnabled(u);
        }

        @Override
        protected boolean isVisible(int row) {
            return !model.isExpansionControlAtRow(row) && isEnabled(row);
        }
                
        @Override
        protected boolean isEnabled (Unit uu) {
            return uu != null && model.isExpansionControlPresent() && model.isCollapsed();
        }

        @Override
        protected boolean isEnabled (int row) {
            return model.isExpansionControlPresent() && model.isCollapsed();
        }
        
        @Override
        protected String getContextName (Unit u) {
            return getActionName();
        }
    }    
    
    private class LessAction extends RowTabAction {
        public LessAction () {
            super ("UnitTab_LessAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), */"Expand");//NOI18N
        }
        @Override
        public void performerImpl (Unit uu) {
            try {
                setWaitingState(true);
                final Map<String, Boolean> state = UnitCategoryTableModel.captureState(model.getUnits());
                model.setExpanded(false);
                fireUpdataUnitChange();
                UnitCategoryTableModel.restoreState(model.getUnits(), state, model.isMarkedAsDefault());
                focusTable();
            } finally {
                setWaitingState(false);
            }            
        }

        @Override
        protected boolean isVisible(Unit u) {
            return isEnabled(u);
        }

        @Override
        protected boolean isVisible(int row) {
            return !model.isExpansionControlAtRow(row) && isEnabled(row);
        }
                
        @Override
        protected boolean isEnabled (Unit uu) {
            return uu != null && model.isExpansionControlPresent() && model.isExpanded();
        }

        @Override
        protected boolean isEnabled (int row) {
            return model.isExpansionControlPresent() && model.isExpanded();
        }
                
        @Override
        protected String getContextName (Unit u) {
            return getActionName();
        }
    }    
    
    
    private class ReloadAction extends TabAction {
        Task reloadTask = null;
        @SuppressWarnings("OverridableMethodCallInConstructor")
        public ReloadAction (String nameKey) {            
            super (nameKey, KeyStroke.getKeyStroke (KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), null);
            String tooltip = NbBundle.getMessage (UnitTab.class, "UnitTab_Tooltip_RefreshAction");//NOI18N
            putValue (TOOL_TIP_TEXT_KEY, tooltip);
            setEnabled (false);
        }

        @Override
        public void performerImpl () {
            setEnabled (false);
            reloadTask = reloadTask (true);
        }

        @Override
        public void setEnabled (boolean enabled) {
            super.setEnabled (enabled);
            super.firePropertyChange ("enabled", !isEnabled (), isEnabled ());
        }
    }
    
    private class AddLocallyDownloadedAction extends TabAction {
        public AddLocallyDownloadedAction () {
            super ("UnitTab_bAddLocallyDownloads_Name", null);
            topButton.getAccessibleContext ().setAccessibleName (NbBundle.getMessage(UnitTab.class, "UnitTab_bAddLocallyDownloads_ACN")); // NOI18N
            topButton.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(UnitTab.class, "UnitTab_bAddLocallyDownloads_ACD")); // NOI18N
            String tooltip = NbBundle.getMessage (UnitTab.class, "UnitTab_Tooltip_AddAction_LOCAL");//NOI18N
            putValue (TOOL_TIP_TEXT_KEY, tooltip);
        }
        
        @Override
        public void performerImpl () {
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            if (getLocalDownloadSupport ().chooseNbmFiles ()) {
                updateTab (state);
            }
        }

        @Override
        public void setEnabled (boolean enabled) {
            super.setEnabled (enabled);
            super.firePropertyChange ("enabled", !isEnabled (), isEnabled ());
        }
    }
    
    private class RemoveLocallyDownloadedAction extends RowTabAction {
        public RemoveLocallyDownloadedAction () {
            super ("UnitTab_RemoveLocallyDownloadedAction",  null);
            String tooltip = NbBundle.getMessage (UnitTab.class, "UnitTab_Tooltip_RemoveAction_LOCAL");//NOI18N
            putValue (TOOL_TIP_TEXT_KEY, tooltip);
        }
        
        @Override
        protected boolean isEnabled (Unit uu) {
            return uu != null && (model.getType ().equals (UnitCategoryTableModel.Type.LOCAL));
        }
        
        @Override
        public boolean isEnabled () {
            if (super.isEnabled ()) {
                return table.getSelectedRow () > -1;
            } else {
                return false;
            }
        }
        
        @Override
        public void setEnabled (boolean enabled) {
            super.setEnabled (enabled);
            super.firePropertyChange ("enabled", !isEnabled (), isEnabled ());
        }
        @Override
        public void performerImpl (final Unit unit) {
            final Runnable removeUpdates = new Runnable (){
                @Override
                public void run () {
                    final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
                    try {
                        if (unit.isMarked ()) {
                            //this removes it from container
                            unit.setMarked (false);
                        }
                        getLocalDownloadSupport ().remove (unit.updateUnit);
                        getLocalDownloadSupport ().getUpdateUnits ();
                    } finally {
                        SwingUtilities.invokeLater (new Runnable () {
                            @Override
                            public void run () {
                                fireUpdataUnitChange();
                                UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                                refreshState ();
                                setWaitingState (false);
                            }
                        });
                    }
                }
            };
            setWaitingState (true);
            Utilities.startAsWorkerThread (removeUpdates, 250);
        }
        
        @Override
        protected String getContextName (Unit u) {
            return getActionName ();//NOI18N
        }
        
        @Override
        protected boolean isVisible (Unit u) {
            return false;
        }        
    }
    private class EnableRenderer extends  DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent (
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderComponent = (JLabel)super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Boolean) {
                Unit u = model.getUnitAtRow(table.convertRowIndexToModel(row));
                if (u != null && u.getRelevantElement ().getUpdateUnit ().isPending ()) {
                    renderComponent.setIcon (ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/restart.png", false)); // NOI18N
                } else {
                    Boolean state = (Boolean)value;
                    if (state.booleanValue()) {
                        renderComponent.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/active.png", false)); // NOI18N
                    } else {
                        renderComponent.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/inactive.png", false)); // NOI18N
                    }
                }
                renderComponent.setText ("");
                renderComponent.setHorizontalAlignment (SwingConstants.CENTER);
                
            }
            Component retval = renderComponent;
            return retval;
        }
    }
     
    class SourceCategoryRenderer extends  DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent (
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderComponent = (JLabel)super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Image) {
                Unit u = model.getUnitAtRow(table.convertRowIndexToModel(row));
                if (u instanceof Unit.Available) {
                    Unit.Available a = (Unit.Available)u;
                    renderComponent.setIcon(ImageUtilities.image2Icon(a.getSourceIcon()));
                    renderComponent.setText ("");
                    renderComponent.setHorizontalAlignment (SwingConstants.CENTER);
                }
                
            }
            Component retval = renderComponent;
            return retval;
        }
    }    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bDeactivate;
    private javax.swing.JButton bTabAction;
    private javax.swing.JButton bUninstall;
    private javax.swing.JCheckBox detailView;
    private javax.swing.JLabel lSearch;
    private javax.swing.JLabel lSelectionInfo;
    private javax.swing.JLabel lWarning;
    private javax.swing.JSplitPane spTab;
    private javax.swing.JTextField tfSearch;
    private javax.swing.JButton topButton;
    // End of variables declaration//GEN-END:variables
    
}
