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
package org.netbeans.modules.bugtracking.ui.query;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.LayoutStyle;
import static javax.swing.SwingConstants.WEST;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.team.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryController.QueryMode;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.commons.LinkButton;
import org.netbeans.modules.bugtracking.commons.NBBugzillaUtils;
import org.netbeans.modules.bugtracking.commons.NoContentPanel;
import org.netbeans.modules.bugtracking.commons.SaveQueryPanel;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.team.TeamRepositories;
import org.netbeans.modules.bugtracking.ui.repository.RepositoryComboSupport;
import org.netbeans.modules.team.spi.TeamAccessorUtils;
import org.netbeans.modules.team.spi.TeamProject;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class QueryTopComponent extends TopComponent
                                     implements PropertyChangeListener, FocusListener {

    private static QueryTopComponent instance;

    /** Set of opened {@code QueryTopComponent}s. */
    private static final Set<QueryTopComponent> openQueries = new HashSet<QueryTopComponent>();

    private final RepoSelectorPanel repoPanel;
    private final LinkButton newButton;
    private final JComboBox repositoryComboBox;

    private static final String PREFERRED_ID = "QueryTopComponent"; // NOI18N
    private QueryImpl query; // XXX synchronized

    private final RequestProcessor rp = new RequestProcessor("Bugtracking query", 1, true); // NOI18N
    private RequestProcessor.Task prepareTask;
    private RepositoryComboSupport rs;
    private File context;
    private QueryController.QueryMode mode;

    private final InstanceContent instanceContent = new InstanceContent();
    
    QueryTopComponent() {
        initComponents();
        instanceContent.add(getActionMap());
        associateLookup(new AbstractLookup(instanceContent));
        RepositoryRegistry.getInstance().addPropertyChangeListener(this);
        repositoryComboBox = new javax.swing.JComboBox();
        newButton = new LinkButton();

        /* layout */
        Font titleFont = title.getFont();
        title.setFont(titleFont.deriveFont(1.7f * titleFont.getSize()));
        title.setBorder(BorderFactory.createEmptyBorder(
                0, getLeftContainerGap(title), 0, 0));

        leftRepoPanel.setVisible(false);
        repoPanel = new RepoSelectorPanel(repositoryComboBox, newButton);
        
        GroupLayout layout = (GroupLayout) headerPanel.getLayout();
        leftRepoPanel.setVisible(true);
        layout.replace(leftRepoPanel, repoPanel);
        
        /* texts */
        Mnemonics.setLocalizedText(
                title,
                getBundleText("QueryTopComponent.findIssuesLabel.text"));//NOI18N
        Mnemonics.setLocalizedText(newButton,
                getBundleText("QueryTopComponent.newButton.text_1"));   //NOI18N

        /* accessibility texts */
        repositoryComboBox.getAccessibleContext().setAccessibleDescription(
                getBundleText("QueryTopComponent.repositoryComboBox.AccessibleContext.accessibleDescription")); //NOI18N
        newButton.getAccessibleContext().setAccessibleDescription(
                getBundleText("QueryTopComponent.newButton.AccessibleContext.accessibleDescription")); //NOI18N

        /* background colors */
        Color editorBgColor = UIManager.getDefaults()
                              .getColor("EditorPane.background");       //NOI18N
        repoPanel.setBackground(editorBgColor);
        headerPanel.setBackground(editorBgColor);
        queryPanel.setBackground(editorBgColor);
        mainPanel.setBackground(editorBgColor);

        /* focus */
        repoPanel.setNextFocusableComponent(newButton);

        /* scrolling */
        int unitIncrement = (int) (1.5f * titleFont.getSize() + 0.5f);
        jScrollPane1.getHorizontalScrollBar().setUnitIncrement(unitIncrement);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(unitIncrement);
    }

    private static int getLeftContainerGap(JComponent comp) {
        LayoutStyle layoutStyle = LayoutStyle.getInstance();
        return layoutStyle.getContainerGap(comp, WEST, null);
    }
    
    public QueryImpl getQuery() {
        return query;
    }

    void setMode(QueryMode mode) {
        this.mode = mode;
        QueryController c = getController(query);
        addQueryComponent(c);
    }
    
    @NbBundle.Messages({"LBL_RepositoryInit=<Initializing...>",
                        "LBL_NoRepositorySelected=<no repository selected>"})
    void init(QueryImpl query, RepositoryImpl defaultRepository, File context, boolean suggestedSelectionOnly, QueryController.QueryMode mode, boolean isNew) {
        this.query = query;
        this.context = context;
        this.mode = mode;
        
        setNameAndTooltip();

        if(suggestedSelectionOnly) {
            repositoryComboBox.setEnabled(false);
            newButton.setVisible(false);
        }
        
        if (query != null) {
            if(!isNew) {
                RepositoryImpl repoImpl = query.getRepositoryImpl();
                if(repoImpl.isTeamRepository()) {
                    TeamProject teamProject = TeamRepositories.getInstance().getTeamProject(defaultRepository);
                    if(teamProject != null) {
                        instanceContent.add(query.getQuery());
                        instanceContent.add(teamProject);
                    }
                }
                setSaved();
            } else {
                if(!suggestedSelectionOnly) {
                    rs = RepositoryComboSupport.setup(this, repositoryComboBox, defaultRepository.getRepository());
                }
            }
            QueryController c = getController(query);
            addQueryComponent(c);
            registerListeners();
        } else {
            newButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onNewClick();
                }
            });
            repositoryComboBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        Object item = e.getItem();
                        if (item instanceof Repository) {
                            onRepoSelected();
                        }
                    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                        Object item = e.getItem();
                        if (item instanceof Repository) {
                            ((Repository) item).removePropertyChangeListener(QueryTopComponent.this);
                        }
                    }
                }
            });
            
            NoContentPanel ncp = new NoContentPanel();
            if(defaultRepository == null) {
                rs = RepositoryComboSupport.setup(this, repositoryComboBox, true);
                ncp.setText(Bundle.LBL_NoRepositorySelected());
            } else {
                rs = RepositoryComboSupport.setup(this, repositoryComboBox, defaultRepository.getRepository());
                ncp.setText(Bundle.LBL_RepositoryInit());
            }
            queryPanel.add(ncp);
            rs.setLocalRepositoryHidden(true);
            newButton.addFocusListener(this);
            repositoryComboBox.addFocusListener(this);
        }                
    }

    private void registerListeners() {
        unregisterListeners(); // avoid duplicates
        query.addPropertyChangeListener(this);
        query.getController().addPropertyChangeListener(this);
        query.getRepositoryImpl().addPropertyChangeListener(this);
    }
    
    private void unregisterListeners() {
        query.removePropertyChangeListener(this);
        query.getController().removePropertyChangeListener(this);
        query.getRepositoryImpl().removePropertyChangeListener(this);
    }

    private QueryController getController(QueryImpl query) {
        return query.getController();
    }

    private static String getBundleText(String key) {
        return NbBundle.getMessage(QueryTopComponent.class, key);
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized QueryTopComponent getDefault() {
        if (instance == null) {
            instance = new QueryTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the QueryTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized QueryTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(QueryTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof QueryTopComponent) {
            return (QueryTopComponent) win;
        }
        Logger.getLogger(QueryTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }

    /**
     * Returns top-component that should display the given query.
     *
     * @param query query for which the top-component should be found.
     * @return top-component that should display the given query.
     */
    public static synchronized QueryTopComponent find(QueryImpl query) {
        QueryTopComponent[] tcs = getOpenQueries();
        for (QueryTopComponent tc : tcs) {
            if (query.equals(tc.getQuery())) {
                return tc;
            }
        }
        return null;
    }

    public static void closeFor(RepositoryImpl repo) {
        QueryTopComponent[] tcs = getOpenQueries();
        for (QueryTopComponent itc : tcs) {
            QueryImpl tcQuery = itc.getQuery(); 
            if(tcQuery == null) {
                continue;
            }
            RepositoryImpl tcRepo = tcQuery.getRepositoryImpl(); 
            if(tcRepo.getId().equals(repo.getId()) && 
               tcRepo.getConnectorId().equals(repo.getConnectorId()) ) 
            {
                itc.closeInAwt();
            }
        }
    }

    private static QueryTopComponent[] getOpenQueries() {
        QueryTopComponent[] tcs;
        synchronized(openQueries) {
            tcs = openQueries.toArray(new QueryTopComponent[0]);
        }
        return tcs;
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("o.n.m.bugtracking.ui.query.QueryTopComponent"); // NOI18N
    }

    @Override
    public void componentOpened() {
        synchronized(openQueries) {
            openQueries.add(this);
        }
        if(query != null) {
            getController(query).opened();
        }
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                repositoryComboBox.requestFocusInWindow();
//            }
//        });
        BugtrackingManager.LOG.log(Level.FINE, "{0} - {1} opened", new Object[] {this.getClass().getName(), query != null ? query.getDisplayName() : null}); // NOI18N
    }

    @Override
    public void componentClosed() {
        synchronized(openQueries) {
            openQueries.remove(this);
        }
        RepositoryRegistry.getInstance().removePropertyChangeListener(this);

        if(query != null) {
            releaseQuery(!isSaved());
        }
        
        if(prepareTask != null) {
            prepareTask.cancel();
        }
        BugtrackingManager.LOG.log(Level.FINE, "{0} - {1} closed", new Object[] {this.getClass().getName(), query != null ? query.getDisplayName() : null});  // NOI18N
    }

    private void releaseQuery(boolean remove) {
        if(query != null) {
            unregisterListeners();
            getController(query).closed();
            if(remove) {
                query = null;
            }
        }
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return query != null && query.getDisplayName() != null ? query.getDisplayName() : PREFERRED_ID;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(RepositoryImpl.EVENT_QUERY_LIST_CHANGED)) {
            // only saved queries can be removed
            if(query != null && isSaved()) {
                Collection<QueryImpl> queries = query.getRepositoryImpl().getQueries();
                boolean stillExists = false;
                for (QueryImpl q : queries) {
                    if(q.getDisplayName().equals(query.getDisplayName())) {
                        stillExists = true;
                        break;
                    }    
                }
                if(!stillExists) {
                    queryRemoved();
                }
            }
        } else if(evt.getPropertyName().equals(RepositoryRegistry.EVENT_REPOSITORIES_CHANGED)) {
            if(query != null) {
                Object cOld = evt.getOldValue();
                if(cOld instanceof Collection)
                {
                    RepositoryImpl thisRepo = query.getRepositoryImpl();
                    if(contains((Collection) cOld, thisRepo)) {
                        queryRemoved();
                    }
                } else if(cOld == null) {
                    RepositoryImpl thisRepo = query.getRepositoryImpl();
                    Collection<RepositoryImpl> knownRepos = RepositoryRegistry.getInstance().getKnownRepositories(true);
                    if(!contains((Collection) knownRepos, thisRepo)) {
                        queryRemoved();
                    }
                }
            }
            if(!repositoryComboBox.isEnabled()) {
                // well, looks like there should be only one repository available
                return;
            }
            UIUtils.runInAWT(new Runnable() {
                @Override
                public void run() {
                    if(rs != null) {
                        rs.refreshRepositoryModel();
                    }
                }
            });
        } else if(evt.getPropertyName().equals(QueryController.PROP_CHANGED)) {
            Object o = evt.getNewValue();
            boolean changed;
            if(o instanceof Boolean) {
                changed = (Boolean) o;
            } else {
                changed = getController(query).isChanged();
            }
            if(changed) {
                if (getLookup().lookup(QuerySavable.class) == null) {
                    instanceContent.add(new QuerySavable(this));
                    setNameAndTooltip();
                }
            } else {
                String qn = query.getDisplayName();
                if(qn != null && !"".equals(qn.trim())) {
                    // was saved
                    if(!isSaved()) {
                        openDashboard();
                        
                        setSaved();
                    }
                }
                QuerySavable savable = getSavable();
                if(savable != null) {
                    savable.destroy();
                    setNameAndTooltip();
                }                
            }
            
        } 
    }

    private void queryRemoved() {
        closeInAwt();
        releaseQuery(true);
    }

    private void openDashboard() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                TopComponent tc = WindowManager.getDefault().findTopComponent("DashboardTopComponent"); // NOI18N
                if (tc == null) {
                    BugtrackingManager.LOG.fine("No Tasks Dashboard found"); // NOI18N
                    return;
                }
                tc.open();
                tc.requestActive();
            }
        });
    }

    private QuerySavable getSavable() {
        return getLookup().lookup(QuerySavable.class);
    }
    
    private boolean contains(Collection c, RepositoryImpl r) {
        for (Object o : c) {
            assert o instanceof RepositoryImpl;
            if(((RepositoryImpl)o).getId().equals(r.getId())) {
                return true;
            }
        }
        return false;
    }

    @NbBundle.Messages({
        "CTL_Save=Save",
        "CTL_Discard=Discard",
        "# {0} - the name of a not saved bugtracking query", "MSG_HasChanges={0}\nhas changes. Save?",
        "#Question is simply i dialog title", "LBL_Question=Question"
    })
    @Override
    public boolean canClose() {
        if(query != null) {
            QuerySavable savable = getSavable();
            if(savable != null) {
                JButton save = new JButton(Bundle.CTL_Save());
                JButton discard = new JButton(Bundle.CTL_Discard());
                NotifyDescriptor nd = 
                    new NotifyDescriptor(
                        Bundle.MSG_HasChanges(getFQQueryName(query)), 
                        Bundle.LBL_Question(), 
                        NotifyDescriptor.YES_NO_CANCEL_OPTION, 
                        NotifyDescriptor.INFORMATION_MESSAGE, 
                        new Object[] {save, discard, NotifyDescriptor.CANCEL_OPTION}, null);
                Object ret = DialogDisplayer.getDefault().notify(nd);
                boolean canClose = false;
                if(ret == save) {
                    canClose = save();
                } else if(ret == discard) {
                    canClose = query.getController().discardUnsavedChanges();
                } 
                if(canClose) {
                    savable.destroy();
                }
                return canClose;
            }
        }
        return super.canClose(); 
    }
    
    private boolean save() {
        String newName = null;
        if(query.getDisplayName() == null) {
            newName = SaveQueryPanel.show(new SaveQueryPanel.QueryNameValidator() {
                @Override
                public String isValid(String name) {
                    Collection<QueryImpl> queries = query.getRepositoryImpl().getQueries();
                    for (QueryImpl q : queries) {
                        if(name.equals(q.getDisplayName())) {
                            return NbBundle.getMessage(QueryTopComponent.class, "MSG_SAME_NAME"); // NOI18N
                        }
                    }
                    return null;
                }
            }, null);
            if(newName == null) {
                return false;
            }
        }
        return query.getController().saveChanges(newName);
    }
    
    @Override
    public void focusGained(FocusEvent e) {
        Component c = e.getComponent();
        if(c instanceof JComponent) {
            Point p = SwingUtilities.convertPoint(c.getParent(), c.getLocation(), repoPanel);
            final Rectangle r = new Rectangle(p, c.getSize());
            UIUtils.runInAWT(new Runnable() {
                @Override
                public void run() {
                    repoPanel.scrollRectToVisible(r);
                }
            });
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        // do nothing
    }

    private void closeInAwt() {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                close();
            }
        });
    }

    static final class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return QueryTopComponent.getDefault();
        }
    }

    /***********
     * PRIVATE *
     ***********/

    private void onNewClick() {
        RepositoryImpl repoImpl = BugtrackingUtil.createRepository();
        if(repoImpl != null) {
            Repository repo = repoImpl.getRepository();
            repositoryComboBox.addItem(repo);
            repositoryComboBox.setSelectedItem(repo);
        }
    }

    private void onRepoSelected() {
        if(prepareTask != null) {
            prepareTask.cancel();
        }
        Cancellable c = new Cancellable() {
            @Override
            public boolean cancel() {
                if(prepareTask != null) {
                    prepareTask.cancel();
                }
                return true;
            }
        };
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(QueryTopComponent.class, "CTL_PreparingQuery"), c); // NOI18N
        prepareTask = rp.post(new Runnable() {
            @Override
            public void run() {
                try {
                    handle.start();
                    RepositoryImpl repo = getRepository();
                    if(repo == null) {
                        return;
                    }
                    repo.removePropertyChangeListener(QueryTopComponent.this);
                    repo.addPropertyChangeListener(QueryTopComponent.this);

                    if(query != null) {
                        unregisterListeners();
                    }

                    query = repo.createNewQuery();
                    if (query == null) {
                        return;
                    }

                    if(context != null && NBBugzillaUtils.isNbRepository(repo.getUrl())) {
                        OwnerInfo ownerInfo = TeamAccessorUtils.getOwnerInfo(context);
                        if(ownerInfo != null) {
                            query.setContext(ownerInfo);
                        }
                    }
                    registerListeners();

                    final QueryController addController = getController(query);
                    UIUtils.runInAWT(new Runnable() {
                        @Override
                        public void run() {
                            addQueryComponent(addController);
                            focusFirstEnabledComponent();
                        }
                    });
                } finally {
                    handle.finish();
                    prepareTask = null;
                }
            }

        });
    }

    private void addQueryComponent(QueryController controller) {
        JComponent cmp = controller.getComponent(mode != null ? mode : QueryMode.EDIT);
        queryPanel.removeAll();
        queryPanel.add(cmp);
        controller.opened();
    }

    private RepositoryImpl getRepository() {
        Object item = repositoryComboBox.getSelectedItem();
        if (!(item instanceof Repository)) {
            return null;
        }
        return APIAccessor.IMPL.getImpl((Repository)item);
    }

    private void focusFirstEnabledComponent() {
        repositoryComboBox.requestFocusInWindow();
        if(!repositoryComboBox.isEnabled()) {
            newButton.requestFocusInWindow();
            if(!newButton.isEnabled()) {
                newButton.transferFocus();
            }
        }
    }

    private void setNameAndTooltip() throws MissingResourceException {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                if(query != null && query.getDisplayName() != null) {
                    String name = getFQQueryName(query);
                    if(getSavable() != null) {
                        name = "<html><b>" + name + "</b></html>"; // NOI18N
                    }
                    setName(name); // NOI18N
                    setToolTipText(NbBundle.getMessage(QueryTopComponent.class, "LBL_QueryName", new Object[]{query.getRepositoryImpl().getDisplayName(), query.getTooltip()})); // NOI18N
                } else {
                    setName(NbBundle.getMessage(QueryTopComponent.class, "CTL_QueryTopComponent")); // NOI18N
                    setToolTipText(NbBundle.getMessage(QueryTopComponent.class, "HINT_QueryTopComponent")); // NOI18N
                }
            }
        });
    }

    private static String getFQQueryName(QueryImpl query) throws MissingResourceException {
        String repoName = query.getRepositoryImpl().getDisplayName();
        final String queryName = query.getDisplayName();
        if(queryName != null) {
            return NbBundle.getMessage(QueryTopComponent.class, "LBL_QueryName", new Object[]{repoName, queryName});
        } else {
            return NbBundle.getMessage(QueryTopComponent.class, "LBL_UnsavedQuery", new Object[]{repoName});
        }
    }

    private void setSaved() {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                headerPanel.setVisible(false);
                mainPanel.revalidate();
                mainPanel.repaint();
                setNameAndTooltip();
            }
        });
    }
    
    private boolean isSaved() {
        return !headerPanel.isVisible();
    }

    @Override
    public boolean requestFocusInWindow() {
        return mainPanel.requestFocusInWindow();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        headerPanel = new javax.swing.JPanel();
        title = new javax.swing.JLabel();
        leftRepoPanel = new javax.swing.JPanel();
        queryPanel = new javax.swing.JPanel();

        org.openide.awt.Mnemonics.setLocalizedText(title, org.openide.util.NbBundle.getMessage(QueryTopComponent.class, "QueryTopComponent.findIssuesLabel.text")); // NOI18N

        leftRepoPanel.setOpaque(false);

        javax.swing.GroupLayout leftRepoPanelLayout = new javax.swing.GroupLayout(leftRepoPanel);
        leftRepoPanel.setLayout(leftRepoPanelLayout);
        leftRepoPanelLayout.setHorizontalGroup(
            leftRepoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 66, Short.MAX_VALUE)
        );
        leftRepoPanelLayout.setVerticalGroup(
            leftRepoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addComponent(title)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 203, Short.MAX_VALUE)
                .addComponent(leftRepoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(leftRepoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(title))
                .addContainerGap())
        );

        queryPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(queryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(queryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(mainPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel headerPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel leftRepoPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel queryPanel;
    private javax.swing.JLabel title;
    // End of variables declaration//GEN-END:variables

    private static class QuerySavable extends AbstractSavable {
        private final QueryTopComponent tc;
        
        QuerySavable(QueryTopComponent tc) {
            this.tc = tc;
            register();
        }

        @Override
        protected String findDisplayName() {
            if(tc.query != null) {
                return getFQQueryName(tc.query);
            }
            return tc.getName();
        }

        @Override
        protected void handleSave() throws IOException {
            if(tc.query != null) {
                tc.save();
            }
        }

        void destroy() {
            tc.instanceContent.remove(this);
            unregister();
        }
        
        @Override
        public boolean equals(Object obj) {
             if (obj instanceof QuerySavable) {
                return tc == ((QuerySavable)obj).tc;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return tc.hashCode();
        }
        
    }    
}
