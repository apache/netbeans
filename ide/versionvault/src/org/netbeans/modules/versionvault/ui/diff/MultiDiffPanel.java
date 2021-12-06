/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.ui.diff;

import org.netbeans.modules.versioning.util.PlaceholderPanel;
import org.netbeans.modules.versioning.util.DelegatingUndoRedo;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versionvault.FileInformation;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.FileStatusCache;
import org.netbeans.modules.versionvault.ClearcaseModuleConfig;
import org.netbeans.modules.versionvault.ui.checkin.CheckinAction;
import org.netbeans.modules.versionvault.ui.update.UpdateAction;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.awt.UndoRedo;
import org.openide.windows.WindowManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import org.netbeans.modules.versionvault.util.ProgressSupport;
import org.netbeans.modules.versioning.diff.DiffLookup;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.diff.EditorSaveCookie;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.diff.SaveBeforeCommitConfirmation;
import org.netbeans.modules.versioning.util.CollectionUtils;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.util.Lookup;
import static org.netbeans.modules.versioning.util.CollectionUtils.copyArray;

/**
 *
 * @author Maros Sandor
 */
class MultiDiffPanel extends javax.swing.JPanel implements ActionListener, VersioningListener, DiffSetupSource, PropertyChangeListener {
    
    /**
     * Array of DIFF setups that we show in the DIFF view. Contents of this array is changed if
     * the user switches DIFF types.
     */
    private Setup[] setups;
    /**
     * editor cookies belonging to the files being diffed.
     * The array may contain {@code null}s if {@code EditorCookie}s
     * for the corresponding files were not found.
     *
     * @see  #nodes
     */
    private EditorCookie[] editorCookies;
    
    private final DelegatingUndoRedo delegatingUndoRedo = new DelegatingUndoRedo(); 
    private final DiffLookup lookup = new DiffLookup();

    /**
     * Context in which to DIFF.
     */
    private final VCSContext context;

    private int displayStatuses;

    /**
     * Display name of the context of this diff.
     */ 
    private final String contextName;
    
    private int currentType;
    private int currentIndex = -1;
    private int currentModelIndex = -1;
    
    private RequestProcessor.Task prepareTask;
    private DiffPrepareTask dpt;

    private AbstractAction nextAction;
    private AbstractAction          prevAction;
    
    /**
     * null for view that are not
     */
    private RequestProcessor.Task   refreshTask;
    private ProgressSupport         refreshSupport;
    
    private JComponent              diffView;
    private DiffFileTable           fileTable;
    private boolean                 dividerSet;

    private RequestProcessor        rp;
    
    /**
     * panel that is used for displaying the diff if {@code JSplitPane}
     * is not used
     */
    private final PlaceholderPanel diffViewPanel;
    private JComponent infoPanelLoadingFromRepo;

    /**
     * Creates diff panel and immediatelly starts loading...
     */
    public MultiDiffPanel(VCSContext context, int initialType, String contextName) {
        this.context = context;
        this.contextName = contextName;
        currentType = initialType;
        initComponents();
        diffViewPanel = null;
        initFileTable();
        initToolbarButtons();
        initNextPrevActions();
        refreshSetups();
        refreshComponents();
        refreshTask = org.netbeans.modules.versioning.util.Utils.createTask(new RefreshViewTask());        
        
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            Color color = UIManager.getColor("NbExplorerView.background");      // NOI18N 
            setBackground(color); 
            controlsToolBar.setBackground(color); 
        }     
    }

    /**
     * Construct diff component showing just one file.
     * It hides All, Local, Remote toggles and file chooser combo.
     */
    public MultiDiffPanel(File file, String rev1, String rev2) {
        context = null;
        contextName = file.getName();
        initComponents();
        initToolbarButtons();
        initNextPrevActions();

        diffViewPanel = new PlaceholderPanel();
        diffViewPanel.setComponent(getInfoPanelLoading());
        replaceVerticalSplitPane(diffViewPanel);


        // mimics refreshSetups()
        setSetups(new Setup(file, rev1, rev2));
        setDiffIndex(0, 0);
        dpt = new DiffPrepareTask(setups);
        prepareTask = RequestProcessor.getDefault().post(dpt);
    }

    private void replaceVerticalSplitPane(JComponent replacement) {
        removeAll();
        splitPane = null;
        setLayout(new BorderLayout());
        controlsToolBar.setPreferredSize(new Dimension(Short.MAX_VALUE, 25));
        add(controlsToolBar, BorderLayout.NORTH);
        add(replacement, BorderLayout.CENTER);
    }

    private void setSetups(Setup... setups) {
        this.setups = setups;
        this.editorCookies = (setups != null)
                             ? DiffUtils.setupsToEditorCookies(setups)
                             : null;
    }

    private boolean fileTableSetSelectedIndexContext;

    public void tableRowSelected(int viewIndex) {
        if (fileTableSetSelectedIndexContext) return;
        setDiffIndex(viewIndex, 0);
    }
    
    UndoRedo getUndoRedo() {
        return delegatingUndoRedo;
    }

    private void cancelBackgroundTasks() {
        if (prepareTask != null) {
            prepareTask.cancel();
        }
/*
        if(executeStatusSupport!=null) {
            executeStatusSupport.cancel();
        }
*/
    }

    public Lookup getLookup() {
        return lookup;
    }

    boolean canClose() {
        if (setups == null) {
            return true;
        }

        EditorCookie[] editorCookiesCopy = copyArray(editorCookies);
        DiffUtils.cleanThoseUnmodified(editorCookiesCopy);
        DiffUtils.cleanThoseWithEditorPaneOpen(editorCookiesCopy);
        SaveCookie[] saveCookies = getSaveCookies(setups, editorCookiesCopy);

        return (saveCookies.length == 0)
               || SaveBeforeClosingDiffConfirmation.allSaved(saveCookies);
    }

    private static SaveCookie[] getSaveCookies(Setup[] setups,
                                               EditorCookie[] editorCookies) {
        assert setups.length == editorCookies.length;

        final int length = setups.length;
        SaveCookie[] proResult = new SaveCookie[length];

        int count = 0;
        for (int i = 0; i < length; i++) {
            EditorCookie editorCookie = editorCookies[i];
            if (editorCookie == null) {
                continue;
            }

            File baseFile = setups[i].getBaseFile();
            if (baseFile == null) {
                continue;
            }

            FileObject fileObj = FileUtil.toFileObject(baseFile);
            if (fileObj == null) {
                continue;
            }

            proResult[count++] = new EditorSaveCookie(editorCookie,
                                                      fileObj.getNameExt());
        }

        return CollectionUtils.shortenArray(proResult, count);
    }

    /**
     * Called by the enclosing TopComponent to interrupt the fetching task.
     */
    void componentClosed() {
        setSetups((Setup[]) null);
        /**
         * must disable these actions, otherwise key shortcuts would trigger them even after tab closure
         * see #159266
         */
        prevAction.setEnabled(false);
        nextAction.setEnabled(false);
        cancelBackgroundTasks(); 
    }

    void requestActive() {
        if (diffView != null) {
            diffView.requestFocusInWindow();
        }
    }

    private void initFileTable() {
        fileTable = new DiffFileTable(this);
        splitPane.setTopComponent(fileTable.getComponent());
        splitPane.setBottomComponent(getInfoPanelLoading());
    }

    private void initToolbarButtons() {
        if (context != null) {
            commitButton.addActionListener(this);
            localToggle.addActionListener(this);
            remoteToggle.addActionListener(this);
            allToggle.addActionListener(this);

            commitButton.setToolTipText(NbBundle.getMessage(MultiDiffPanel.class, "MSG_CommitDiff_Tooltip", contextName));
            updateButton.setToolTipText(NbBundle.getMessage(MultiDiffPanel.class, "MSG_UpdateDiff_Tooltip", contextName));
            ButtonGroup grp = new ButtonGroup();
            grp.add(localToggle);
            grp.add(remoteToggle);
            grp.add(allToggle);
            if (currentType == Setup.DIFFTYPE_LOCAL) localToggle.setSelected(true);
            else if (currentType == Setup.DIFFTYPE_REMOTE) remoteToggle.setSelected(true);
            else if (currentType == Setup.DIFFTYPE_ALL) allToggle.setSelected(true);

            commitButton.setEnabled(false);
        } else {
            localToggle.setVisible(false);
            remoteToggle.setVisible(false);
            allToggle.setVisible(false);
            commitButton.setVisible(false);
        }

        allToggle.setVisible(false);
        localToggle.setVisible(false);
        remoteToggle.setVisible(false);
    }
        
    private void initNextPrevActions() {
        nextAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/diff-next.png"))) {  // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/versionvault/ui/diff/Bundle").
                                                   getString("CTL_DiffPanel_Next_Tooltip"));                
            }
            public void actionPerformed(ActionEvent e) {
                onNextButton();
            }
        };
        prevAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/diff-prev.png"))) { // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/versionvault/ui/diff/Bundle").
                                                   getString("CTL_DiffPanel_Prev_Tooltip"));                
            }
            public void actionPerformed(ActionEvent e) {
                onPrevButton();
            }
        };
        nextButton.setAction(nextAction);
        prevButton.setAction(prevAction);
        if(context != null) {
            updateButton.setEnabled(new UpdateAction("", context).isEnabled());
        } else {
            updateButton.setEnabled(false);
        }
    }
    
    private JComponent getInfoPanelLoading() {
        if (infoPanelLoadingFromRepo == null) {
            infoPanelLoadingFromRepo = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoContent"));
        }
        return infoPanelLoadingFromRepo;
    }

    private void refreshComponents() {
        DiffController view = setups != null && currentModelIndex != -1 ? setups[currentModelIndex].getView() : null;
        int currentDifferenceIndex = view != null ? view.getDifferenceIndex() : -1;
        if (view != null) {
            nextAction.setEnabled(currentIndex < setups.length - 1 || currentDifferenceIndex < view.getDifferenceCount() - 1);
        } else {
            nextAction.setEnabled(false);
        }
        prevAction.setEnabled(currentIndex > 0 || currentDifferenceIndex > 0);
        dividerSet = false;
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        if (refreshTask != null) {
            Clearcase.getInstance().getFileStatusCache().addVersioningListener(this);
        }
        JComponent parent = (JComponent) getParent();
        parent.getActionMap().put("jumpNext", nextAction);  // NOI18N
        parent.getActionMap().put("jumpPrev", prevAction); // NOI18N
    }

    private void updateSplitLocation() {
        if (dividerSet) return;
        JComponent parent = (JComponent) getParent();
        Dimension dim = parent == null ? new Dimension() : parent.getSize();
        if (dim.width <=0 || dim.height <=0) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateSplitLocation();
                }
            });
            return;
        }
        dividerSet = true;
        JTable jt = fileTable.getTable();
        int optimalLocation = jt.getPreferredSize().height + jt.getTableHeader().getPreferredSize().height;
        if (optimalLocation > dim.height / 3) {
            optimalLocation = dim.height / 3;
        }
        if (optimalLocation <= jt.getTableHeader().getPreferredSize().height) {
            optimalLocation = jt.getTableHeader().getPreferredSize().height * 3;
        }
        splitPane.setDividerLocation(optimalLocation);
    }
    
    @Override
    public void removeNotify() {
        Clearcase.getInstance().getFileStatusCache().removeVersioningListener(this);
        super.removeNotify();
    }
    
    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            if (!affectsView(event)) {
                return;
            }
            refreshTask.schedule(200);
        }
    }
    
    private boolean affectsView(VersioningEvent event) {
        File file = (File) event.getParams()[0];
        FileInformation oldInfo = (FileInformation) event.getParams()[1];
        FileInformation newInfo = (FileInformation) event.getParams()[2];
        if (oldInfo == null && newInfo == null) {
            return false;
        } else if (oldInfo == null) {
            if ((newInfo.getStatus() & displayStatuses) == 0) return false;
        } else if (newInfo == null) {
            if ((oldInfo.getStatus() & displayStatuses) == 0) return false;
        } else {
            if ((oldInfo.getStatus() & displayStatuses) + (newInfo.getStatus() & displayStatuses) == 0) return false;
        }
        return context.contains(file);
    }
    
    private void setDiffIndex(int idx, int location) {
        currentIndex = idx;
        DiffController view = null;
        
        if (currentIndex != -1) {
            currentModelIndex = showingFileTable() ? fileTable.getModelIndex(currentIndex) : 0;
            view = setups[currentModelIndex].getView();

            // enable Select in .. action
            FileObject fileObj = null;
            EditorCookie.Observable observableEditorCookie = null;
            File baseFile = setups[currentModelIndex].getBaseFile();
            if (baseFile != null) {
                fileObj = FileUtil.toFileObject(baseFile);
            }
            EditorCookie editorCookie = editorCookies[currentModelIndex];
            if (editorCookie instanceof EditorCookie.Observable) {
                observableEditorCookie = (EditorCookie.Observable) editorCookie;
            }
            
            diffView = null;
            boolean focus = false;
            if (view != null) {
                if (showingFileTable()) {
                    fileTableSetSelectedIndexContext = true;
                    fileTable.setSelectedIndex(currentIndex);
                    fileTableSetSelectedIndexContext = false;
                }
                diffView = view.getJComponent();
                diffView.getActionMap().put("jumpNext", nextAction);  // NOI18N
                diffView.getActionMap().put("jumpPrev", prevAction);  // NOI18N
                displayDiffView();
                if (location == -1) {
                    location = view.getDifferenceCount() - 1;
                }
                if (location >=0 && location < view.getDifferenceCount()) {
                    view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, location);
                }
                Component toc = WindowManager.getDefault().getRegistry().getActivated();
                if (SwingUtilities.isDescendingFrom(this, toc)) {
                //                focus = true;
                }
            } else {
                diffView = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoContent"));
            }            
            lookup.setData(fileObj, observableEditorCookie, diffView.getActionMap());
        } else {
            currentModelIndex = -1;
            lookup.setData();
            diffView = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoFileSelected"));
            lookup.setData(diffView.getActionMap());
            displayDiffView();
        }

        delegatingUndoRedo.setDiffView(diffView);

        refreshComponents();
    }

    private boolean showingFileTable() {
        return fileTable != null;
    }

    private void displayDiffView() {
        if (splitPane != null) {
            int gg = splitPane.getDividerLocation();
            splitPane.setBottomComponent(diffView);
            splitPane.setDividerLocation(gg);
        } else {
            diffViewPanel.setComponent(diffView);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == commitButton) onCommitButton();
        else if (source == localToggle || source == remoteToggle || source == allToggle) onDiffTypeChanged();
    }

    private void onRefreshButton() {
        getProgressSupport().start();
    }                    

    private void onUpdateButton() {
        UpdateAction.update(context);
    }
    
    private void onCommitButton() {
        EditorCookie[] editorCookiesCopy = copyArray(editorCookies);
        DiffUtils.cleanThoseUnmodified(editorCookiesCopy);
        SaveCookie[] saveCookies = getSaveCookies(setups, editorCookiesCopy);

        if ((saveCookies.length == 0)
                || SaveBeforeCommitConfirmation.allSaved(saveCookies)) {
            CheckinAction.checkin(context);
        }
    }

    /** Next that is driven by visibility. It continues to next not yet visible difference. */
    private void onNextButton() {
        if (showingFileTable()) {
            currentIndex = fileTable.getSelectedIndex();
            currentModelIndex = fileTable.getSelectedModelIndex();
        }

        DiffController view = setups[currentModelIndex].getView();
        if (view != null) {
            int currentDifferenceIndex = view.getDifferenceIndex();
            if (++currentDifferenceIndex >= view.getDifferenceCount()) {
                if (++currentIndex >= setups.length) {
                    currentIndex--;
                } else {
                    setDiffIndex(currentIndex, 0);
                }
            } else {
                view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            if (++currentIndex >= setups.length) currentIndex = 0;
            setDiffIndex(currentIndex, 0);
        }
        refreshComponents();
    }

    private void onPrevButton() {
        DiffController view = setups[currentModelIndex].getView();
        if (view != null) {
            int currentDifferenceIndex = view.getDifferenceIndex();
            if (--currentDifferenceIndex < 0) {
                if (--currentIndex < 0) {
                    currentIndex++;
                } else {
                    setDiffIndex(currentIndex, -1);
                }
            } else {
                view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            if (--currentIndex < 0) currentIndex = setups.length - 1;
            setDiffIndex(currentIndex, -1);
        }
        refreshComponents();
    }

    /**
     * @return setups, takes into account Local, Remote, All switch
     */
    public Collection<Setup> getSetups() {
        if (setups == null) {
            return Collections.emptySet();
        } else {
            return Arrays.asList(setups);
        }
    }

    public String getSetupDisplayName() {
        return contextName;
    }


    private void refreshSetups() {
        if (dpt != null) {
            prepareTask.cancel();
        }

        File [] files;
        switch (currentType) {
        case Setup.DIFFTYPE_LOCAL:
            displayStatuses = FileInformation.STATUS_LOCAL_CHANGE;
            break;
        case Setup.DIFFTYPE_REMOTE:
            displayStatuses = FileInformation.STATUS_LOCAL_CHANGE;
            // TODO displayStatuses = FileInformation.STATUS_REMOTE_CHANGE; 
            break;
        case Setup.DIFFTYPE_ALL:
            displayStatuses = FileInformation.STATUS_LOCAL_CHANGE /* TODO | FileInformation.STATUS_REMOTE_CHANGE */; 
            break;
        default:
            throw new IllegalStateException("Unknown DIFF type:" + currentType); // NOI18N
        }
        files = computeFilesToDiff();
        
        setSetups(computeSetups(files));

        fileTable.setColumns(new String[] { DiffNode.COLUMN_NAME_NAME, DiffNode.COLUMN_NAME_STATUS, DiffNode.COLUMN_NAME_LOCATION });
        fileTable.setTableModel(setups, editorCookies);

        if (setups.length == 0) {
            String noContentLabel;
            switch (currentType) {
            case Setup.DIFFTYPE_LOCAL:
                noContentLabel = NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoLocalChanges");
                break;
            case Setup.DIFFTYPE_REMOTE:
                noContentLabel = NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoRemoteChanges");
                break;
            case Setup.DIFFTYPE_ALL:
                noContentLabel = NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoAllChanges");
                break;
            default:
                throw new IllegalStateException("Unknown DIFF type:" + currentType); // NOI18N
            }
            setSetups((Setup[]) null);
            fileTable.getComponent().setEnabled(false);
            fileTable.getComponent().setPreferredSize(null);
            Dimension dim = fileTable.getComponent().getPreferredSize();
            fileTable.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
            diffView = null;
            diffView = new NoContentPanel(noContentLabel);
            displayDiffView();
            nextAction.setEnabled(false);
            prevAction.setEnabled(false);
            commitButton.setEnabled(false);
            revalidate();
            repaint();
        } else {
            fileTable.getComponent().setEnabled(true);
            fileTable.getComponent().setPreferredSize(null);
            Dimension dim = fileTable.getComponent().getPreferredSize();
            fileTable.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
            setDiffIndex(0, 0);
            commitButton.setEnabled(true);
            dpt = new DiffPrepareTask(setups);
            prepareTask = getRequestProcessor().post(dpt);
        }
    }

    private Setup[] computeSetups(File[] files) {
        List<Setup> newSetups = new ArrayList<Setup>(files.length);
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (!file.isDirectory()) {
                Setup setup = new Setup(file, currentType);
                setup.setNode(new DiffNode(setup, displayStatuses));
                newSetups.add(setup);
            }
        }
        Collections.sort(newSetups, new SetupsComparator());
        return newSetups.toArray(new Setup[newSetups.size()]);
    }

    private void onDiffTypeChanged() {
        if (localToggle.isSelected()) {
            if (currentType == Setup.DIFFTYPE_LOCAL) return;
            currentType = Setup.DIFFTYPE_LOCAL;
        } else if (remoteToggle.isSelected()) {
            if (currentType == Setup.DIFFTYPE_REMOTE) return;
            currentType = Setup.DIFFTYPE_REMOTE;
        } else if (allToggle.isSelected()) {
            if (currentType == Setup.DIFFTYPE_ALL) return;
            currentType = Setup.DIFFTYPE_ALL;
        }
        refreshSetups();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
            refreshComponents();
        }
    }

    public File [] computeFilesToDiff() {
        File [] all = Clearcase.getInstance().getFileStatusCache().listFiles(context, displayStatuses);
        List<File> files = new ArrayList<File>();
        for (int i = 0; i < all.length; i++) {
            File file = all[i];
            String path = file.getAbsolutePath();
            if (!ClearcaseModuleConfig.isExcludedFromCommit(path)) {
                files.add(file);
            }
        }
        // ensure that command roots (files that were explicitly selected by user) are included in Diff
        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
        for (File file : context.getFiles()) {
            if (file.isFile() && (cache.getInfo(file).getStatus() & displayStatuses) != 0 && !files.contains(file)) {
                files.add(file);
            }
        }
        return files.toArray(new File[files.size()]);
    }

    private RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("ClearCase Diff", 50);                    // NOI18N
        }
        return rp;
    }

    private class DiffPrepareTask implements Runnable {
        
        private final Setup[] prepareSetups;

        public DiffPrepareTask(Setup [] prepareSetups) {
            this.prepareSetups = prepareSetups;
        }

        public void run() {
            for (int i = 0; i < prepareSetups.length; i++) {
                if (prepareSetups != setups) return;
                try {
                    prepareSetups[i].initSources();  // slow network I/O
                    final int fi = i;
                    StreamSource ss1 = prepareSetups[fi].getFirstSource();
                    StreamSource ss2 = prepareSetups[fi].getSecondSource();
                    final DiffController view = DiffController.create(ss1, ss2);  // possibly executing slow external diff
                    view.addPropertyChangeListener(MultiDiffPanel.this);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            prepareSetups[fi].setView(view);
                            if (prepareSetups != setups) {
                                return;
                            }
                            if (currentModelIndex == fi) {
                                setDiffIndex(currentIndex, 0);
                            }
                            if (splitPane != null) {
                                updateSplitLocation();
                            }
                        }
                    });
                } catch (IOException e) {
                    Clearcase.LOG.log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private static class SetupsComparator implements Comparator<Setup> {

        private ClearcaseUtils.ByImportanceComparator delegate = new ClearcaseUtils.ByImportanceComparator();
        private FileStatusCache cache;

        public SetupsComparator() {
            cache = Clearcase.getInstance().getFileStatusCache();
        }

        public int compare(Setup setup1, Setup setup2) {
            int cmp = delegate.compare(cache.getInfo(setup1.getBaseFile()), cache.getInfo(setup2.getBaseFile()));
            if (cmp == 0) {
                return setup1.getBaseFile().getName().compareToIgnoreCase(setup2.getBaseFile().getName());
            }
            return cmp;
        }
    }

    private class RefreshViewTask implements Runnable {
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    refreshSetups();
                }
            });
        }
    }
    
    private ProgressSupport getProgressSupport() {
        if(refreshSupport == null) {
            refreshSupport = new FileStatusCache.RefreshSupport(new RequestProcessor("Clearcase-diff-refresh", 1), context, contextName) {
                @Override
                protected void perform() {
                    refresh();
                }
            };
        }
        return refreshSupport;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        controlsToolBar = new javax.swing.JToolBar();
        allToggle = new javax.swing.JToggleButton();
        jPanel3 = new javax.swing.JPanel();
        localToggle = new javax.swing.JToggleButton();
        jPanel4 = new javax.swing.JPanel();
        remoteToggle = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        nextButton = new javax.swing.JButton();
        prevButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        refreshButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        commitButton = new javax.swing.JButton();
        splitPane = new javax.swing.JSplitPane();

        controlsToolBar.setFloatable(false);
        controlsToolBar.setRollover(true);

        allToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/remote_vs_local.png"))); // NOI18N
        allToggle.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_All_Tooltip")); // NOI18N
        allToggle.setFocusable(false);
        allToggle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        allToggle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(allToggle);

        jPanel3.setMaximumSize(new java.awt.Dimension(12, 32767));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel3);

        localToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/local_vs_local.png"))); // NOI18N
        localToggle.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Local_Tooltip")); // NOI18N
        localToggle.setFocusable(false);
        localToggle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        localToggle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(localToggle);

        jPanel4.setMaximumSize(new java.awt.Dimension(12, 32767));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel4);

        remoteToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/remote_vs_remote.png"))); // NOI18N
        remoteToggle.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Remote_Tooltip")); // NOI18N
        remoteToggle.setFocusable(false);
        remoteToggle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        remoteToggle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(remoteToggle);

        jPanel1.setMaximumSize(new java.awt.Dimension(80, 32767));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel1);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/diff-next.png"))); // NOI18N
        nextButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Next_Tooltip")); // NOI18N
        nextButton.setFocusable(false);
        nextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(nextButton);

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/diff-prev.png"))); // NOI18N
        prevButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Prev_Tooltip")); // NOI18N
        prevButton.setFocusable(false);
        prevButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        prevButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(prevButton);

        jPanel2.setMaximumSize(new java.awt.Dimension(30, 32767));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel2);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/refresh.png"))); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "refreshButton.toolTipText")); // NOI18N
        refreshButton.setFocusable(false);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        controlsToolBar.add(refreshButton);

        updateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/update.png"))); // NOI18N
        updateButton.setFocusable(false);
        updateButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        updateButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });
        controlsToolBar.add(updateButton);

        jPanel5.setMaximumSize(new java.awt.Dimension(20, 32767));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel5);

        commitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/commit.png"))); // NOI18N
        commitButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MSG_CommitDiff_Tooltip")); // NOI18N
        commitButton.setFocusable(false);
        commitButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        commitButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(commitButton);

        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(controlsToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(controlsToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        onUpdateButton();
    }//GEN-LAST:event_updateButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        onRefreshButton();
    }//GEN-LAST:event_refreshButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton allToggle;
    private javax.swing.JButton commitButton;
    private javax.swing.JToolBar controlsToolBar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JToggleButton localToggle;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton prevButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JToggleButton remoteToggle;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
    
    /** Interprets property blob. */
    static final class Property {
        final byte[] value;

        Property(Object value) {
            this.value = (byte[]) value;
        }

        String getMIME() {            
            return "text/plain"; // NOI18N
        }

        Reader toReader() {
            if (ClearcaseUtils.isBinary(value)) {
                return new StringReader(NbBundle.getMessage(MultiDiffPanel.class, "LBL_Diff_NoBinaryDiff"));  // hexa-flexa txt? // NOI18N
            } else {
                try {
                    return new InputStreamReader(new ByteArrayInputStream(value), "utf8");  // NOI18N
                } catch (UnsupportedEncodingException ex) {
                    Clearcase.LOG.log(Level.SEVERE, null, ex);
                    return new StringReader("[ERROR: " + ex.getLocalizedMessage() + "]"); // NOI18N
                }
            }
        }
    }
}
