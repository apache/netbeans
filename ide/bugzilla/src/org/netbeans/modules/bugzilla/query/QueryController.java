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

package org.netbeans.modules.bugzilla.query;

import org.netbeans.modules.bugtracking.commons.SaveQueryPanel;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer;
import org.netbeans.modules.bugtracking.commons.SaveQueryPanel.QueryNameValidator;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.QueryParameter.AllWordsTextFieldParameter;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.bugzilla.query.QueryParameter.CheckBoxParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ComboParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.EmptyValuesListParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ListParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ParameterValue;
import org.netbeans.modules.bugzilla.query.QueryParameter.TextFieldParameter;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class QueryController implements org.netbeans.modules.bugtracking.spi.QueryController, ItemListener, ListSelectionListener, ActionListener, FocusListener, KeyListener, ChangeListener {

    protected QueryPanel panel;

    private static final String CHANGED_NOW = "Now";                            // NOI18N

    private final ComboParameter summaryParameter;
    private final ComboParameter commentsParameter;
    private final ComboParameter whiteboardParameter;
    private final ComboParameter keywordsParameter;
    private final ComboParameter peopleParameter;
    private final ListParameter productParameter;
    private final ListParameter componentParameter;
    private final ListParameter versionParameter;
    private final ListParameter statusParameter;
    private final ListParameter resolutionParameter;
    private final ListParameter priorityParameter;
    private final ListParameter changedFieldsParameter;
    private final ListParameter severityParameter;
    private final ListParameter issueTypeParameter;
    private ListParameter tmParameter;

    private final Map<String, QueryParameter> parameters;

    private final RequestProcessor rp = new RequestProcessor("Bugzilla query", 1, true);  // NOI18N

    private final BugzillaRepository repository;
    protected BugzillaQuery query;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOI18N
    private QueryTask refreshTask;
    private final IssueTable issueTable;
    private final boolean isNetbeans;

    private final Object REFRESH_LOCK = new Object();
    private final Semaphore querySemaphore = new Semaphore(1);
    private boolean populated = false;
    private boolean wasOpened;
    private boolean wasModeShow;
    private QueryProvider.IssueContainer<BugzillaIssue> delegatingIssueContainer;
        
    public QueryController(BugzillaRepository repository, BugzillaQuery query, String urlParameters, boolean urlDef) {
        this(repository, query, urlParameters, urlDef, true);
    }

    public QueryController(BugzillaRepository repository, BugzillaQuery query, String urlParameters, boolean urlDef, boolean populate) {
        this.repository = repository;
        this.query = query;
        
        issueTable = new IssueTable(repository.getID(), query.getDisplayName(), this, query.getColumnDescriptors(), query.isSaved());
        setupRenderer(issueTable);
        panel = new QueryPanel(issueTable.getComponent());

        isNetbeans = BugzillaUtil.isNbRepository(repository);
        panel.setNBFieldsVisible(isNetbeans);

        panel.productList.addListSelectionListener(this);
        panel.filterComboBox.addItemListener(this);
        panel.searchButton.addActionListener(this);
        panel.keywordsButton.addActionListener(this);
        panel.cancelChangesButton.addActionListener(this);
        panel.saveChangesButton.addActionListener(this);
        panel.gotoIssueButton.addActionListener(this);
        panel.webButton.addActionListener(this);
        panel.urlToggleButton.addActionListener(this);
        panel.refreshButton.addActionListener(this);
        panel.modifyButton.addActionListener(this);
        panel.seenButton.addActionListener(this);
        panel.removeButton.addActionListener(this);
        panel.refreshConfigurationButton.addActionListener(this);
        panel.cloneQueryButton.addActionListener(this);
        panel.changedFromTextField.addFocusListener(this);

        panel.idTextField.addActionListener(this);
        panel.productList.addKeyListener(this);
        panel.componentList.addKeyListener(this);
        panel.versionList.addKeyListener(this);
        panel.statusList.addKeyListener(this);
        panel.resolutionList.addKeyListener(this);
        panel.severityList.addKeyListener(this);
        panel.priorityList.addKeyListener(this);
        panel.changedList.addKeyListener(this);
        panel.tmList.addKeyListener(this);

        panel.summaryTextField.addActionListener(this);
        panel.commentTextField.addActionListener(this);
        panel.whiteboardTextField.addActionListener(this);
        panel.keywordsTextField.addActionListener(this);
        panel.peopleTextField.addActionListener(this);
        panel.changedFromTextField.addActionListener(this);
        panel.changedToTextField.addActionListener(this);
        panel.changedToTextField.addActionListener(this);

        // setup parameters
        parameters = new LinkedHashMap<>();
        summaryParameter = createQueryParameter(ComboParameter.class, panel.summaryComboBox, "short_desc_type");    // NOI18N
        commentsParameter = createQueryParameter(ComboParameter.class, panel.commentComboBox, "long_desc_type");    // NOI18N
        whiteboardParameter = createQueryParameter(ComboParameter.class, panel.whiteboardComboBox, "status_whiteboard_type"); // NOI18N
        keywordsParameter = createQueryParameter(ComboParameter.class, panel.keywordsComboBox, "keywords_type");    // NOI18N
        peopleParameter = createQueryParameter(ComboParameter.class, panel.peopleComboBox, "emailtype1");           // NOI18N
        productParameter = createQueryParameter(ListParameter.class, panel.productList, "product");                 // NOI18N
        componentParameter = createQueryParameter(ListParameter.class, panel.componentList, "component");           // NOI18N
        versionParameter = createQueryParameter(ListParameter.class, panel.versionList, "version");                 // NOI18N
        statusParameter = createQueryParameter(ListParameter.class, panel.statusList, "bug_status");                // NOI18N
        resolutionParameter = createQueryParameter(ListParameter.class, panel.resolutionList, "resolution");        // NOI18N
        priorityParameter = createQueryParameter(ListParameter.class, panel.priorityList, "priority");              // NOI18N
        changedFieldsParameter = createQueryParameter(EmptyValuesListParameter.class, panel.changedList, "chfield");           // NOI18N
        tmParameter = createQueryParameter(ListParameter.class, panel.tmList, "target_milestone");                  // NOI18N
        if(isNetbeans) {
            issueTypeParameter = createQueryParameter(ListParameter.class, panel.issueTypeList, "cf_bug_type");     // NOI18N            
            severityParameter = null;            
        } else {
            severityParameter = createQueryParameter(ListParameter.class, panel.severityList, "bug_severity");      // NOI18N
            issueTypeParameter = null;
        }

        createQueryParameter(AllWordsTextFieldParameter.class, panel.summaryTextField, "short_desc");               // NOI18N
        createQueryParameter(AllWordsTextFieldParameter.class, panel.commentTextField, "long_desc");                // NOI18N
        createQueryParameter(AllWordsTextFieldParameter.class, panel.whiteboardTextField, "status_whiteboard");     // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.keywordsTextField, "keywords");                        // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.peopleTextField, "email1");                            // NOI18N
        createQueryParameter(CheckBoxParameter.class, panel.bugAssigneeCheckBox, "emailassigned_to1");              // NOI18N
        createQueryParameter(CheckBoxParameter.class, panel.reporterCheckBox, "emailreporter1");                    // NOI18N
        createQueryParameter(CheckBoxParameter.class, panel.ccCheckBox, "emailcc1");                                // NOI18N
        createQueryParameter(CheckBoxParameter.class, panel.commenterCheckBox, "emaillongdesc1");                   // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.changedFromTextField, "chfieldfrom");                  // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.changedToTextField, "chfieldto");                      // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.newValueTextField, "chfieldvalue");                    // NOI18N

        for(QueryParameter p : parameters.values()) {
            p.addChangeListener(this);
        }
        
        panel.filterComboBox.setModel(new DefaultComboBoxModel(issueTable.getDefinedFilters()));

        if(query.isSaved()) {
            setAsSaved();
        } else {
            wasModeShow = true; // if not saved, means by default are issues shown
        }
        
        if(urlDef) {
            panel.switchQueryFields(false);
            panel.urlTextField.setText(urlParameters);
            populated = true;
            setChanged();
        } else {
            querySemaphore.acquireUninterruptibly();
            Bugzilla.LOG.log(Level.FINE, "lock aquired because populating {0}", query.getDisplayName()); // NOI18N
            postPopulate(urlParameters, false);
        }
    }

    private void setupRenderer(IssueTable issueTable) {
        BugzillaQueryCellRenderer renderer = new BugzillaQueryCellRenderer((QueryTableCellRenderer)issueTable.getRenderer());
        issueTable.setRenderer(renderer);
    }

    @Override
    public boolean providesMode(QueryMode mode) {
        return true;
    }
    
    @Override
    public void opened() {
        wasOpened = true;
        if(query.isSaved()) {
            setIssueCount(query.getSize());
            if(!query.wasRun()) {
                onRefresh();
            }
            if(refreshTask != null) {
                refreshTask.fillTableIfNeccessary();
            }
        }
    }

    @Override
    public void closed() {
        onCancelChanges();
        synchronized(REFRESH_LOCK) {
            if(refreshTask != null) {
                refreshTask.cancel();
            }
        }
        if(!query.isSaved()) {
            query.delete();
        }        
    }

    private <T extends QueryParameter> T createQueryParameter(Class<T> clazz, Component c, String parameter) {
        try {
            Constructor<T> constructor = clazz.getConstructor(c.getClass(), String.class, String.class);
            T t = constructor.newInstance(c, parameter, getRepository().getTaskRepository().getCharacterEncoding());
            parameters.put(parameter, t);
            return t;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Bugzilla.LOG.log(Level.SEVERE, parameter, ex);
        }
        return null;
    }

    @Override
    public JComponent getComponent(QueryMode mode) {
        setMode(mode);
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.bugzilla.query.BugzillaQuery"); // NOI18N
    }

    private void setMode(QueryMode mode) {
        switch(mode) {
            case EDIT:
                if(query.isSaved()) {
                    onModify();
                }
                break;
            case VIEW:
                wasModeShow = true;
                onCancelChanges();
                selectFilter(issueTable.getAllFilter());
                break;
            default: 
                throw new IllegalStateException("Unsupported mode " + mode);
        }
    }
        
    public String getUrlParameters(boolean encode) {
        if(panel.urlPanel.isVisible()) {
            return panel.urlTextField.getText();
        } else {
            StringBuilder sb = new StringBuilder();
            for (QueryParameter qp : parameters.values()) {
                sb.append(qp.get(encode));
            }
            return sb.toString();
        }
    }

    protected BugzillaRepository getRepository() {
        return repository;
    }

    protected void postPopulate(final String urlParameters, final boolean forceRefresh) {

        final Task[] t = new Task[1];
        Cancellable c = new Cancellable() {
            @Override
            public boolean cancel() {
                if(t[0] != null) {
                    return t[0].cancel();
                }
                return true;
            }
        };

        final String msgPopulating = NbBundle.getMessage(QueryController.class, "MSG_Populating", new Object[]{repository.getDisplayName()});    // NOI18N
        final ProgressHandle handle = ProgressHandleFactory.createHandle(msgPopulating, c);

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                enableFields(false);
                panel.showRetrievingProgress(true, msgPopulating, !query.isSaved());
                handle.start();
            }
        });   

        t[0] = rp.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if(forceRefresh) {
                        repository.refreshConfiguration();
                    }
                    populate(urlParameters);
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            enableFields(true);
                            setChanged();
                            handle.finish();
                            panel.showRetrievingProgress(false, null, !query.isSaved());
                        }
                    });
                }
            }
        });
    }

    private boolean ignoreChanges = false;
    protected void populate(final String urlParameters) {
        if(Bugzilla.LOG.isLoggable(Level.FINE)) {
            Bugzilla.LOG.log(Level.FINE, "Starting populate query controller{0}", (query.isSaved() ? " - " + query.getDisplayName() : "")); // NOI18N
        }
        final BugzillaConfiguration bc = repository.getConfiguration();
        if(bc == null || !bc.isValid()) {
            // XXX nice errro msg?
            querySemaphore.release();
            return;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                ignoreChanges = true;
                try {
                    productParameter.setParameterValues(toParameterValues(bc.getProducts()));
                    populateProductDetails();
                    if(isNetbeans) {
                        issueTypeParameter.setParameterValues(toParameterValues(bc.getIssueTypes()));
                    } else {
                        severityParameter.setParameterValues(toParameterValues(bc.getSeverities()));
                    }
                    statusParameter.setParameterValues(toParameterValues(bc.getStatusValues()));
                    resolutionParameter.setParameterValues(toParameterValues(getQueryResolutions(bc)));
                    priorityParameter.setParameterValues(toParameterValues(bc.getPriorities()));
                    changedFieldsParameter.setParameterValues(QueryParameter.PV_LAST_CHANGE);
                    summaryParameter.setParameterValues(QueryParameter.PV_TEXT_SEARCH_VALUES);
                    commentsParameter.setParameterValues(QueryParameter.PV_TEXT_SEARCH_VALUES);
                    whiteboardParameter.setParameterValues(QueryParameter.PV_TEXT_SEARCH_VALUES);
                    keywordsParameter.setParameterValues(QueryParameter.PV_KEYWORDS_VALUES);
                    peopleParameter.setParameterValues(QueryParameter.PV_PEOPLE_VALUES);
                    panel.changedToTextField.setText(CHANGED_NOW);

                    setParameters(urlParameters != null ? urlParameters : getDefaultParameters());

                    populated = true;
                    Bugzilla.LOG.log(Level.FINE, "populated query {0}", query.getDisplayName()); // NOI18N
                    
                } finally {
                    resetParameters();
                    ignoreChanges = false;
                    querySemaphore.release();
                    Bugzilla.LOG.log(Level.FINE, "released lock on query {0}", query.getDisplayName()); // NOI18N
                    
                    if(Bugzilla.LOG.isLoggable(Level.FINE)) {
                        Bugzilla.LOG.log(Level.FINE, "Finnished populate query controller {0}", (query.isSaved() ? " - " + query.getDisplayName() : "")); // NOI18N
                    }
                }
            }
        });
    }

    private String getDefaultParameters() {
        return BugzillaUtil.isNbRepository(repository) ? BugzillaConstants.DEFAULT_NB_STATUS_PARAMETERS : BugzillaConstants.DEFAULT_STATUS_PARAMETERS;
    }

    protected void enableFields(boolean bl) {
        // set all non parameter fields
        panel.enableFields(bl);
        // set the parameter fields
        for (Map.Entry<String, QueryParameter> e : parameters.entrySet()) {
            QueryParameter qp = parameters.get(e.getKey());
            qp.setEnabled(bl);
        }
    }

    protected void disableProduct() { // XXX whatever field
        productParameter.setAlwaysDisabled(true);
    }

    protected void selectFirstProduct() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if(panel.productList.getModel().getSize() > 0) {
                    panel.productList.setSelectedIndex(0);
                }
            }
        });
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if(e.getSource() == panel.filterComboBox) {
            onFilterChange((Filter)e.getItem());
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if(e.getSource() == panel.productList) {
            onProductChanged(e);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if(panel.changedFromTextField.getText().equals("")) {                   // NOI18N
            String lastChangeFrom = BugzillaConfig.getInstance().getLastChangeFrom();
            panel.changedFromTextField.setText(lastChangeFrom);
            panel.changedFromTextField.setSelectionStart(0);
            panel.changedFromTextField.setSelectionEnd(lastChangeFrom.length());
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        // do nothing
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.searchButton) {
            onRefresh();
        } else if (e.getSource() == panel.gotoIssueButton) {
            onGotoIssue();
        } else if (e.getSource() == panel.keywordsButton) {
            onKeywords();
        } else if (e.getSource() == panel.saveChangesButton) {
            onSave(); // refresh
        } else if (e.getSource() == panel.cancelChangesButton) {
            onCancelChanges();
        } else if (e.getSource() == panel.webButton) {
            onWeb();
        } else if (e.getSource() == panel.urlToggleButton) {
            onDefineAs();
        } else if (e.getSource() == panel.refreshButton) {
            onRefresh();
        } else if (e.getSource() == panel.modifyButton) {
            onModify();
        } else if (e.getSource() == panel.seenButton) {
            onMarkSeen();
        } else if (e.getSource() == panel.removeButton) {
            onRemove();
        } else if (e.getSource() == panel.refreshConfigurationButton) {
            onRefreshConfiguration();
        } else if (e.getSource() == panel.cloneQueryButton) {
            onCloneQuery();
        } else if (e.getSource() == panel.idTextField) {
            if(!panel.idTextField.getText().trim().equals("")) {                // NOI18N
                onGotoIssue();
            }
        } else if (e.getSource() == panel.summaryTextField ||
                   e.getSource() == panel.commentTextField ||
                   e.getSource() == panel.keywordsTextField ||
                   e.getSource() == panel.peopleTextField ||
                   e.getSource() == panel.changedFromTextField ||
                   e.getSource() == panel.newValueTextField ||
                   e.getSource() == panel.changedToTextField)
        {
            onRefresh();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // do nothing
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() != KeyEvent.VK_ENTER) {
            return;
        }
        if(e.getSource() == panel.productList ||
           e.getSource() == panel.componentList ||
           e.getSource() == panel.versionList ||
           e.getSource() == panel.statusList ||
           e.getSource() == panel.resolutionList ||
           e.getSource() == panel.priorityList ||
           e.getSource() == panel.changedList)
        {
            onRefresh();
        }
    }

    private void onFilterChange(Filter filter) {
        selectFilter(filter);
    }

    private void onSave() {
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                saveSynchronously(null);
            }
        });
    }

    boolean saveSynchronously(String name) {
        Bugzilla.LOG.fine("on save start");
        boolean firstTime = !query.isSaved();
        if (firstTime) {
            name = name == null ? getSaveName() : name;
            if (name == null) {
                return false;
            }
        }
        name = name == null ? query.getDisplayName() : name;
        assert name != null;
        Bugzilla.LOG.log(Level.FINE, "saving query ''{0}''", new Object[]{name});
        save(name);
        
        if (!firstTime) {
            Bugzilla.LOG.log(Level.FINE, "refreshing query ''{0}'' after save", new Object[]{name});
            onRefresh();
        }
        
        Bugzilla.LOG.log(Level.FINE, "query ''{0}'' saved", new Object[]{name});
        Bugzilla.LOG.fine("on save finnish");
        
        return true;
    }

    void save(String name) {
        query.setName(name);
        saveQuery();
        query.setSaved(true);
        setAsSaved();
        fireChanged();
    }

    private String getSaveName() {
        QueryNameValidator v = new QueryNameValidator() {
            @Override
            public String isValid(String name) {
                Collection<BugzillaQuery> queries = repository.getQueries ();
                for (BugzillaQuery q : queries) {
                    if(q.getDisplayName().equals(name)) {
                        return NbBundle.getMessage(QueryController.class, "MSG_SAME_NAME");
                    }
                }
                return null;
            }
        };
        return SaveQueryPanel.show(v, new HelpCtx("org.netbeans.modules.bugzilla.query.savePanel"));
    }

    private void onCancelChanges() {
        if(query.getDisplayName() != null) { // XXX need a better semantic - isSaved?
            String urlParameters = BugzillaConfig.getInstance().getUrlParams(repository, query.getDisplayName());
            if(urlParameters != null) {
                setParameters(urlParameters);
            }
        }
        setAsSaved();
    }

    public void selectFilter(final Filter filter) {
        if(filter != null) {
            // XXX this part should be handled in the issues table - move the filtercombo and the label over
            Collection<BugzillaIssue> issues = query.getIssues();
            int c = 0;
            if(issues != null) {
                for (BugzillaIssue issue : issues) {
                    if(filter.accept(issue.getNode())) c++;
                }
            }
            final int issueCount = c;

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    panel.filterComboBox.setSelectedItem(filter);
                    setIssueCount(issueCount);
                }
            };
            if(EventQueue.isDispatchThread()) {
                r.run();
            } else {
                EventQueue.invokeLater(r);
            }
        }
        issueTable.setFilter(filter);
    }

    private void setAsSaved() {
        panel.setSaved(query.getDisplayName(), getLastRefresh());
        panel.setModifyVisible(false);
        wasModeShow = true;
    }

    private String getLastRefresh() throws MissingResourceException {
        long l = query.getLastRefresh();
        return l > 0 ?
            dateFormat.format(new Date(l)) :
            NbBundle.getMessage(QueryController.class, "LBL_Never"); // NOI18N
    }

    private void onGotoIssue() {
        String idText = panel.idTextField.getText().trim();
        if(idText == null || idText.trim().equals("") ) {                       // NOI18N
            return;
        }

        final String id = idText.replaceAll("\\s", "");                         // NOI18N
        
        final Task[] t = new Task[1];
        Cancellable c = new Cancellable() {
            @Override
            public boolean cancel() {
                if(t[0] != null) {
                    return t[0].cancel();
                }
                return true;
            }
        };
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(QueryController.class, "MSG_Opening", new Object[] {id}), c); // NOI18N
        t[0] = Bugzilla.getInstance().getRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                handle.start();
                try {
                    openIssue(repository.getIssue(id));
                } finally {
                    handle.finish();
                }
            }
        });
        t[0].schedule(0);
    }

    protected void openIssue(BugzillaIssue issue) {
        if (issue != null) {
            BugzillaUtil.openIssue(issue);
        } else {
            // XXX nice message?
        }
    }

    private void onWeb() {
        String params = getUrlParameters(true);
        String repoURL = repository.getTaskRepository().getRepositoryUrl() + "/query.cgi?format=advanced"; // NOI18N //XXX need constants

        final String urlString = repoURL + (params != null && !params.equals("") ? params : ""); // NOI18N
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                URL url;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                    return;
                }
                HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
                if (displayer != null) {
                    displayer.showURL (url);
                } else {
                    // XXX nice error message?
                    Bugzilla.LOG.warning("No URLDisplayer found.");             // NOI18N
                }
            }
        });
    }

    private void onProductChanged(ListSelectionEvent e) {
        Object[] values =  panel.productList.getSelectedValues();
        String[] products = null;
        if(values != null) {
            products = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                products[i] = ((ParameterValue) values[i]).getValue();
            }
        }
        populateProductDetails(products);
    }

    private void onDefineAs() {
        panel.switchQueryFields(panel.urlPanel.isVisible());
    }

    private void onKeywords() {
        String keywords = BugzillaUtil.getKeywords(NbBundle.getMessage(QueryController.class, "LBL_SelectKeywords"), panel.keywordsTextField.getText(), repository); // NOI18N
        if(keywords != null) {
            panel.keywordsTextField.setText(keywords);
        }
    }

    public void autoRefresh() {
        refresh(true, false);
    }

    public void refresh(boolean synchronously) {
        refresh(false, synchronously);
    }
    
    @NbBundle.Messages({"MSG_Changed=The query was changed and has to be saved before refresh.",
                        "LBL_Save=Save",
                        "LBL_Discard=Discard"})
    public void onRefresh() {
        if(query.isSaved() && isChanged()) {
            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
                Bundle.MSG_Changed(), NotifyDescriptor.YES_NO_CANCEL_OPTION
            );
            Object[] choose = { Bundle.LBL_Save(), Bundle.LBL_Discard(), NotifyDescriptor.CANCEL_OPTION };
            desc.setOptions(choose);
            Object ret = DialogDisplayer.getDefault().notify(desc);
            if(ret == choose[0]) {
                saveQuery(); // persist the parameters
            } else if (ret == choose[1]) {
                onCancelChanges();
                return;
            } else {
                return;
            }
        }
        refresh(false, false);
    }

    private void refresh(final boolean auto, boolean synchronously) {
        Task t;
        synchronized(REFRESH_LOCK) {
            if(refreshTask == null) {
                refreshTask = new QueryTask();
            } else {
                refreshTask.cancel();
            }
            t = refreshTask.post(auto);
        }
        if(synchronously) {
            t.waitFinished();
        }
    }

    private void onModify() {
        panel.setModifyVisible(true);
    }

    private void onMarkSeen() {
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                Collection<BugzillaIssue> issues = query.getIssues();
                for (BugzillaIssue issue : issues) {
                    issue.setUpToDate(true);
                }
            }
        });
    }

    private void onRemove() {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
            NbBundle.getMessage(QueryController.class, "MSG_RemoveQuery", new Object[] { query.getDisplayName() }), // NOI18N
            NbBundle.getMessage(QueryController.class, "CTL_RemoveQuery"),      // NOI18N
            NotifyDescriptor.OK_CANCEL_OPTION);

        if(DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    remove();
                }
            });
        }
    }

    protected void onCloneQuery() {
        String p = getUrlParameters(false);
        BugzillaQuery q = new BugzillaQuery(null, getRepository(), p, false, isUrlDefined(), true);
        BugzillaUtil.openQuery(q);
    }

    private void onRefreshConfiguration() {
        postPopulate(getUrlParameters(false), true);
    }

    private void remove() {
        synchronized(REFRESH_LOCK) {
            if (refreshTask != null) {
                refreshTask.cancel();
            }
        }
        query.remove();
    }

    private void populateProductDetails(String... products) {
        BugzillaConfiguration bc = repository.getConfiguration();
        if(bc == null || !bc.isValid()) {
            // XXX nice errro msg?
            return;
        }
        
        // have to assure bc was loaded asyn, so do this here
        List<String> targetMilestones = bc.getTargetMilestones(null);
        final boolean usingTargetMilestones = !targetMilestones.isEmpty();
        
        UIUtils.runInAWT(new Runnable() {
            public void run() {
                panel.tmLabel.setVisible(usingTargetMilestones);
                panel.tmList.setVisible(usingTargetMilestones);
                panel.tmScrollPane.setVisible(usingTargetMilestones);
            }
        });
                
        if(products == null || products.length == 0) {
            products = new String[] {null};
        }

        List<String> newComponents = new ArrayList<>();
        List<String> newVersions = new ArrayList<>();
        List<String> newTargetMilestone = new ArrayList<>();
        for (String p : products) {
            List<String> productComponents = bc.getComponents(p);
            for (String c : productComponents) {
                if(!newComponents.contains(c)) {
                    newComponents.add(c);
                }
            }
            List<String> productVersions = bc.getVersions(p);
            for (String c : productVersions) {
                if(!newVersions.contains(c)) {
                    newVersions.add(c);
                }
            }            
            if(usingTargetMilestones) {
                List<String> targetMilestone = bc.getTargetMilestones(p);
                for (String c : targetMilestone) {
                    if(!newTargetMilestone.contains(c)) {
                        newTargetMilestone.add(c);
                    }
                }
            }
        }

        Collections.sort(newComponents);
        Collections.sort(newVersions);

        componentParameter.setParameterValues(toParameterValues(newComponents));
        versionParameter.setParameterValues(toParameterValues(newVersions));
        if(usingTargetMilestones) {
            tmParameter.setParameterValues(toParameterValues(newTargetMilestone));
        }
    }

    private List<ParameterValue> toParameterValues(List<String> values) {
        List<ParameterValue> ret = new ArrayList<>(values.size());
        for (String v : values) {
            ret.add(new ParameterValue(v, v));
        }
        return ret;
    }

    private void setParameters(String urlParameters) {
        if(urlParameters == null) {
            return;
        }
        String[] params = urlParameters.split("&"); // NOI18N
        if(params == null || params.length == 0) return;
        Map<String, List<ParameterValue>> normalizedParams = new HashMap<>();
        for (String p : params) {
            int idx = p.indexOf("="); // NOI18N
            if(idx > -1) {
                String parameter = p.substring(0, idx);
                String value = p.substring(idx + 1);

                ParameterValue pv = new ParameterValue(value, value);
                List<ParameterValue> values = normalizedParams.get(parameter);
                if(values == null) {
                    values = new ArrayList<>();
                    normalizedParams.put(parameter, values);
                }
                values.add(pv);
            } else {
                // XXX warning!!
            }
        }

        List<ParameterValue> componentPV = null;
        List<ParameterValue> versionPV = null;
        for (Map.Entry<String, List<ParameterValue>> e : normalizedParams.entrySet()) {
            QueryParameter qp = parameters.get(e.getKey());
            if(qp != null) {
                if(qp == componentParameter) {
                    componentPV = e.getValue();
                } else if(qp == versionParameter) {
                    versionPV = e.getValue();
                } else {
                    List<ParameterValue> pvs = e.getValue();
                    qp.setValues(pvs.toArray(new ParameterValue[0]));
                }
            }
        }
        setDependentParameter(componentParameter, componentPV);
        setDependentParameter(versionParameter, versionPV);
    }
                
    private void setDependentParameter(QueryParameter qp, List<ParameterValue> values) {
        if(values != null) {
            qp.setValues(values.toArray(new ParameterValue[0]));
        }
    }

    private void setIssueCount(final int count) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String msg =
                    count == 1 ?
                        NbBundle.getMessage(QueryController.class, "LBL_MatchingIssue", new Object[] {count}) : // NOI18N
                        NbBundle.getMessage(QueryController.class, "LBL_MatchingIssues", new Object[] {count}); // NOI18N
                panel.tableSummaryLabel.setText(msg);
            }
        });
    }

    boolean isUrlDefined() {
        return panel.urlPanel.isVisible();
    }

    void switchToDeterminateProgress(long issuesCount) {
        synchronized(REFRESH_LOCK) {
            if(refreshTask != null) {
                refreshTask.switchToDeterminateProgress(issuesCount);
            }
        }
    }

    void addProgressUnit(String issueDesc) {
        synchronized(REFRESH_LOCK) {
            if(refreshTask != null) {
                refreshTask.addProgressUnit(issueDesc);
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        setChanged();
    }

    public void setChanged() {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                panel.saveChangesButton.setEnabled((!ignoreChanges && isChanged()) || !query.isSaved());
                fireChanged();
            }
        });
    }

    @Override
    public boolean isChanged() {
        for(QueryParameter p : parameters.values()) {
            if(p.isChanged()) {
                return true;
            }
        }
        return false;
    }

    private List<String> getQueryResolutions(BugzillaConfiguration bc) {
        List<String> l = new ArrayList<>(bc.getResolutions());
        l.add(0, "---");
        return l;
    }
    
    private void resetParameters() {
        for(QueryParameter p : parameters.values()) {
            p.reset();
        }
    }

    private void saveQuery() {
        String name = query.getDisplayName();
        Bugzilla.LOG.log(Level.FINE, "saving query ''{0}''", new Object[]{name}); // NOI18N
        repository.saveQuery(query);
        resetParameters();
        Bugzilla.LOG.log(Level.FINE, "query ''{0}'' saved", new Object[]{name});  // NOI18N                 
    }

    @Override
    public boolean saveChanges(String name) {
        return saveSynchronously(name);
    }

    @Override
    public boolean discardUnsavedChanges() {
        onCancelChanges();
        return true;
    }

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    private void fireChanged() {
        support.firePropertyChange(QueryController.PROP_CHANGED, null, null);
    }

    /**
     * package private for testing purposes
     */
    IssueTable getIssueTable() {
        return issueTable;
    }

    public void setContainer(QueryProvider.IssueContainer<BugzillaIssue> c) {
        delegatingIssueContainer = c;
    }
    
    private class QueryTask implements Runnable, Cancellable, QueryNotifyListener {
        private ProgressHandle handle;
        private Task task;
        private int counter;
        private boolean autoRefresh;
        private long progressMaxWorkunits;
        private int progressWorkunits;
        private final LinkedList<BugzillaIssue> notifiedIssues = new LinkedList<>();

        public QueryTask() {
            query.addNotifyListener(this);
        }

        private void startQuery() {
            // NOI18N
            String displayName = query.getDisplayName() != null ? query.getDisplayName() + " (" + repository.getDisplayName() + ")" // NOI18N
                    : repository.getDisplayName();
            handle = ProgressHandleFactory.createHandle(
                    NbBundle.getMessage(
                            QueryController.class,
                            "MSG_SearchingQuery", // NOI18N
                            new Object[]{
                                displayName}),
                    this);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    enableFields(false);
                    panel.showSearchingProgress(true, NbBundle.getMessage(QueryController.class, "MSG_Searching")); // NOI18N
                }
            });
            if(delegatingIssueContainer != null) {
                delegatingIssueContainer.refreshingStarted();
            }
            handle.start();
        }

        private void finnishQuery() {
            task = null;
            if(delegatingIssueContainer != null) {
                delegatingIssueContainer.refreshingFinished();
            }
            if(handle != null) {
                handle.finish();
                handle = null;
            }
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panel.setQueryRunning(false);
                    panel.setLastRefresh(getLastRefresh());
                    panel.showNoContentPanel(false);
                    enableFields(true);
                }
            });
        }

        void switchToDeterminateProgress(long progressMaxWorkunits) {
            if(handle != null) {
                handle.switchToDeterminate((int) progressMaxWorkunits);
                this.progressMaxWorkunits = progressMaxWorkunits;
                this.progressWorkunits = 0;
            }
        }

        void addProgressUnit(String issueDesc) {
            if(handle != null && progressWorkunits < progressMaxWorkunits) {
                handle.progress(
                    NbBundle.getMessage(
                        QueryController.class, "LBL_RetrievingIssue", new Object[] {issueDesc}),
                    ++progressWorkunits);
            }
        }

        private void executeQuery() {
            setQueryRunning(true);
            // XXX isn't persistent and should be merged with refresh
            String lastChageFrom = panel.changedFromTextField.getText().trim();
            if(lastChageFrom != null && !lastChageFrom.equals("")) {    // NOI18N
                BugzillaConfig.getInstance().setLastChangeFrom(lastChageFrom);
            }
            try {
                if (panel.urlPanel.isVisible()) {
                    // XXX check url format etc...
                    // XXX what if there is a different host in queries repository as in the url?
                    query.refresh(panel.urlTextField.getText(), autoRefresh);
                } else {
                    query.refresh(getUrlParameters(true), autoRefresh);
                }
            } finally {
                setQueryRunning(false); // XXX do we need this? its called in finishQuery anyway
                task = null;
            }

        }

        private void setQueryRunning(final boolean running) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panel.setQueryRunning(running);
                }
            });
        }

        @Override
        public void run() {
            startQuery();
            try {
                Bugzilla.LOG.log(Level.FINE, "waiting until lock releases in query {0}", query.getDisplayName()); // NOI18N
                long t = System.currentTimeMillis();
                try {
                    querySemaphore.acquire();
                } catch (InterruptedException ex) {
                    Bugzilla.LOG.log(Level.INFO, "interuped while trying to lock query", ex); // NOI18N
                    return;
                } 
                querySemaphore.release();
                Bugzilla.LOG.log(Level.FINE, "lock aquired for query {0} after {1}", new Object[]{query.getDisplayName(), System.currentTimeMillis() - t}); // NOI18N
                if(!populated) {
                    Bugzilla.LOG.log(Level.WARNING, "Skipping refresh of query {0} because isn''t populated.", query.getDisplayName()); // NOI18N
                    // something went wrong during populate - skip execute
                    return;
                }
                executeQuery();
            } finally {
                finnishQuery();
            }
        }

        Task post(boolean autoRefresh) {
            Task t = task;
            if (t != null) {
                t.cancel();
            }
            task = t = rp.create(this);
            this.autoRefresh = autoRefresh;
            t.schedule(0);
            return t;
        }

        @Override
        public boolean cancel() {
            Task t = task;
            if (t != null) {
                t.cancel();
                finnishQuery();
            }
            return true;
        }

        @Override
        public void notifyDataAdded (final BugzillaIssue issue) {
            if(delegatingIssueContainer != null) {
                delegatingIssueContainer.add(issue);
            }
            if(wasOpened && wasModeShow) {
                issueTable.addNode(issue.getNode());
            } else {
                synchronized(notifiedIssues) {
                    notifiedIssues.add(issue);
                }
            }
            setIssueCount(++counter);
            if(counter == 1) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        panel.showNoContentPanel(false);
                    }
                });
            }
        }

        @Override
        public void notifyDataRemoved (final BugzillaIssue issue) {
            if(delegatingIssueContainer != null) {
                delegatingIssueContainer.remove(issue);
            }
            // issue table cannot remove data
        }

        @Override
        public void started() {
            issueTable.started();
            counter = 0;
            synchronized(notifiedIssues) {
                notifiedIssues.clear();
            }
            setIssueCount(counter);
        }

        @Override
        public void finished() { }

        void fillTableIfNeccessary() {
            synchronized(notifiedIssues) {
                for (BugzillaIssue issue : notifiedIssues) {
                    issueTable.addNode(issue.getNode());
                }
                notifiedIssues.clear();
            }
        }
        
    }

}
