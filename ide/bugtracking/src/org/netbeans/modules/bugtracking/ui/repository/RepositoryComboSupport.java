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

package org.netbeans.modules.bugtracking.ui.repository;

import org.netbeans.modules.bugtracking.BugtrackingOwnerSupport;
import org.openide.filesystems.FileObject;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.openide.util.RequestProcessor;
import static java.awt.event.HierarchyEvent.DISPLAYABILITY_CHANGED;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import static java.util.logging.Level.FINEST;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.api.IssueQuickSearch.RepositoryFilter;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.commons.NBBugzillaUtils;

/**
 * Loads the list of repositories and determines the default one off the AWT
 * thread. Once the results are ready, updates the UI (the combo-box).
 * It is activated by method {@code hierarchyChanged} when the component
 * containing the repository ComboBox is displayed. At this moment, a routine
 * for finding the known repositories  and for determination the default
 * repository is started in a separated thread. As soon as the list of known
 * repositories is ready, the repositories combo-box is updated. When the
 * default repository is determined, it is pre-selected in the combo-box,
 * unless the user had already selected some repository.
 *
 * @author  Marian Petras
 * @author  Tomas Stupka
 */
public final class RepositoryComboSupport implements ItemListener, Runnable {

    static final String LOADING_REPOSITORIES = "loading";               //NOI18N
    static final String NO_REPOSITORIES = "no repositories";            //NOI18N
    static final String SELECT_REPOSITORY = "select";                   //NOI18N
    private RepositoryFilter filter;
    
    private static final Logger LOG = Logger.getLogger(RepositoryComboSupport.class.getName());

    private final JComboBox comboBox;
    private final FileObject refFile;
    private final boolean preselectSingleRepo;
    private RepositoryComboModel comboBoxModel;
    private DisplayabilityListener displayabilityListener;
    private boolean shutdown;
    private boolean repositoriesDisplayed = false;
    private boolean defaultRepoSelected = false;
    private volatile FileObject[] selectedFiles;
    private volatile Repository[] repositories;
    private volatile boolean defaultRepoComputationPending;
    private volatile Repository defaultRepo;
    private boolean hideLocalRepository;

    /**
     * Setups the given repository with RepositoryComboRenderer. As soon as the component holding the combo
     * is displayed a list of all known repositories is retrieved and the first in the list is selected
     *
     * @param component component with the combobox
     * @param comboBox repository combobox
     * @param selectRepoIfSingle  if {@code true} and there is only one repository
     *                        known, then the (only) repository is preselected;
     *                        otherwise no repository is selected in the combo
     * @return
     */
    public static RepositoryComboSupport setup(JComponent component, JComboBox comboBox, boolean selectRepoIfSingle) {
        return setup(component, comboBox, RepositoryFilter.ALL, selectRepoIfSingle);
    }

    /**
     * Setups the given repository with RepositoryComboRenderer. As soon as the component holding the combo
     * is displayed a list of all known repositories is retrieved and either the default repository will be
     * selected or nothing if it's not between the known repositories
     *
     * @param component component with the combobox
     * @param comboBox repository combobox
     * @param defaultRepo the repository to be selected
     * @return
     */
    public static RepositoryComboSupport setup(JComponent component, JComboBox comboBox, Repository defaultRepo) {
        return setup(component, comboBox, RepositoryFilter.ALL, defaultRepo);
    }
    
/**
     * Setups the given repository with RepositoryComboRenderer. As soon as the component holding the combo
     * is displayed a list of all known repositories is retrieved and either the repository associated with
     * referenceFile will be selected or nothing if there is no such repository
     *
     * @param component component with the combobox
     * @param comboBox repository combobox
     * @param referenceFile file associated with a repository
     * @return
     */
    public static RepositoryComboSupport setup(JComponent component, JComboBox comboBox, FileObject referenceFile) {
        return setup(component, comboBox, RepositoryFilter.ALL, referenceFile);
    }
    
    /**
     * Setups the given repository with RepositoryComboRenderer. As soon as the component holding the combo
     * is displayed a list of all known repositories is retrieved and the first in the list is selected
     *
     * @param component component with the combobox
     * @param comboBox repository combobo
     * @param mode for what kind of functionality the presented list of repositories should be filtered
     * @param selectRepoIfSingle  if {@code true} and there is only one repository
     *                        known, then the (only) repository is preselected;
     *                        otherwise no repository is selected in the combo
     * @return
     */
    public static RepositoryComboSupport setup(JComponent component, JComboBox comboBox, RepositoryFilter mode, boolean selectRepoIfSingle) {
        RepositoryComboSupport repositoryComboSupport
                = new RepositoryComboSupport(comboBox, (Repository) null,
                                                       (FileObject) null,
                                                       selectRepoIfSingle);
        if (component != null) {
            repositoryComboSupport.setupDisplayabilityTrigger(component);
        }
        repositoryComboSupport.filter = mode;
        return repositoryComboSupport;
    }

    /**
     * Setups the given repository with RepositoryComboRenderer. As soon as the component holding the combo
     * is displayed a list of all known repositories is retrieved and either the default repository will be
     * selected or nothing if it's not between the known repositories
     *
     * @param component component with the combobox
     * @param comboBox repository combobox
     * @param mode for what kind of functionality the presented list of repositories should be filtered
     * @param defaultRepo the repository to be selected
     * @return
     */
    public static RepositoryComboSupport setup(JComponent component, JComboBox comboBox, RepositoryFilter mode, Repository defaultRepo) {
        if (defaultRepo == null) {
            throw new IllegalArgumentException("default repository must be specified"); //NOI18N
        }
        RepositoryComboSupport repositoryComboSupport
                = new RepositoryComboSupport(comboBox, defaultRepo,
                                                       (FileObject) null,
                                                       false);
        if (component != null) {
            repositoryComboSupport.setupDisplayabilityTrigger(component);
        }
        repositoryComboSupport.filter = mode;
        return repositoryComboSupport;
    }

    /**
     * Setups the given repository with RepositoryComboRenderer. As soon as the component holding the combo
     * is displayed a list of all known repositories is retrieved and either the repository associated with
     * referenceFile will be selected or nothing if there is no such repository
     *
     * @param component component with the combobox
     * @param comboBox repository combobox
     * @param filter for what kind of functionality the presented list of repositories should be filtered
     * @param referenceFile file associated with a repository
     * @return
     */
    public static RepositoryComboSupport setup(JComponent component, JComboBox comboBox, RepositoryFilter filter, FileObject referenceFile) {
        if (referenceFile == null) {
            throw new IllegalArgumentException("reference file must be specified"); //NOI18N
        }

        RepositoryComboSupport repositoryComboSupport
                = new RepositoryComboSupport(comboBox, (Repository) null,
                                                       referenceFile,
                                                       false);
        if (component != null) {
            repositoryComboSupport.setupDisplayabilityTrigger(component);
        }
        repositoryComboSupport.filter = filter;
        return repositoryComboSupport;
    }
    private final RequestProcessor rp;

    private RepositoryComboSupport(JComboBox comboBox, Repository defaultRepo,
                                                       FileObject refFile,
                                                       boolean preselectSingleRepo) {
        if (comboBox == null) {
            throw new IllegalArgumentException("combo-box must be specified"); //NOI18N
        }

        checkAtMostOneSpecified(defaultRepo, refFile);

        this.comboBox = comboBox;
        this.defaultRepo = defaultRepo;
        this.refFile = refFile;
        this.preselectSingleRepo = preselectSingleRepo;

        this.comboBox.setModel(comboBoxModel = new RepositoryComboModel());
        this.comboBox.setRenderer(new RepositoryComboRenderer());
        
        defaultRepoComputationPending = (defaultRepo == null);

        progress.set(Progress.INITIALIZED);
        rp = BugtrackingManager.getInstance().getRequestProcessor();
    }

    private void checkOldComboBoxModel(JComboBox comboBox) {
        ComboBoxModel oldModel = comboBox.getModel();
        if ((oldModel != null) && (oldModel.getSize() != 0)) {
            throw new IllegalStateException("The combo-box must be empty."); //NOI18N
        }
    }

    private void setComboBoxData(Object[] data) {
        comboBox.setPopupVisible(false);
        comboBoxModel.setData(data);
    }

    private void checkAtMostOneSpecified(Object... items) {
        boolean oneSpecifed = false;
        for (Object item : items) {
            if (item == null) {
                continue;
            }
            if (oneSpecifed) {
                throw new IllegalArgumentException("At most one item may be specified."); //NOI18N
            }
            oneSpecifed = true;
        }
    }

    void start() {
        assert EventQueue.isDispatchThread();
        LOG.finer("start()");                                           //NOI18N

        /* This is the right time to get information about selected nodes: */
        if ((defaultRepo == null) && (refFile == null)) {
            FileObject[] currFiles = BugtrackingUtil.getCurrentSelection();
            if (currFiles == null) {
                currFiles = new FileObject[0];
            }
            this.selectedFiles = currFiles;
        }

        /*
         * As this method should be running in the event-dispatching thread,
         * this is also the right time for manipulation with GUI:
         */
        checkOldComboBoxModel(comboBox);
        this.comboBox.setModel(comboBoxModel = new RepositoryComboModel());        
        setComboBoxData(new Object[] {LOADING_REPOSITORIES});

        updateProgress(Progress.STARTED);

        rp.post(this);
    }

    private void shutdown() {
        assert EventQueue.isDispatchThread();
        LOG.finer("shutdown()");                                        //NOI18N

        shutdownDisplayabilityTrigger();
        comboBox.removeItemListener(this);

        shutdown = true;
    }

    /**
     * Activates the mechanism of the bugtracking repository combo-box.
     * If a non-null component is passed as an argument, a trigger is set up
     * such that loading of bugtracking repositories is started as soon as
     * the given component becomes displayable.
     *
     * @param  triggerComponent  component whose displayability should activate
     *                           the combo-box
     * @exception  java.lang.IllegalStateException
     *             if the given component is already displayable
     */
    private void setupDisplayabilityTrigger(final Component triggerComponent) {
        assert EventQueue.isDispatchThread();
        LOG.finer("setupDisplayabilityTrigger(Component)");             //NOI18N

        if (triggerComponent.isDisplayable()) {
            throw new IllegalStateException(
                    "The trigger component must not be initially displayable.");//NOI18N
        }

        displayabilityListener = new DisplayabilityListener(triggerComponent);
        triggerComponent.addHierarchyListener(displayabilityListener);
    }

    private void shutdownDisplayabilityTrigger() {
        assert EventQueue.isDispatchThread();
        LOG.finer("shutdownDisplayabilityTrigger()");                   //NOI18N

        if (displayabilityListener != null) {
            displayabilityListener.dispose();
            displayabilityListener = null;
        }
    }

    public void setLocalRepositoryHidden (boolean hidden) {
        this.hideLocalRepository = hidden;
    }

    private final class DisplayabilityListener implements HierarchyListener {
        private final Component triggerComponent;
        private DisplayabilityListener(Component triggerComponent) {
            this.triggerComponent = triggerComponent;
        }
        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getChangeFlags() & DISPLAYABILITY_CHANGED) == 0) {
                return;
            }

            assert e.getChanged() == triggerComponent;

            if (triggerComponent.isDisplayable()) {
                LOG.finer("trigger component became displayable");      //NOI18N
                RepositoryComboSupport.this.start();
            } else {
                LOG.finer("trigger component became non-displayable");  //NOI18N
                RepositoryComboSupport.this.shutdown();
            }
        }
        private void dispose() {
            triggerComponent.removeHierarchyListener(this);
        }
    }

    /**
     * This is called when an issue tracker is selected/deselected in the
     * Issue Tracker combo-box (at the top of the Issue Window).
     * This method causes that if item "Select Issue Tracker" is selected
     * in the combo-box and the user selects a real issue tracker (repository),
     * the item "Select Issue Tracker" is removed.
     * 
     * @param  e  {@code ItemEvent} that holds information about the
     *            (de)selection event
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        if ((e.getStateChange() == ItemEvent.DESELECTED)
                && (e.getItem() == SELECT_REPOSITORY)
                && (comboBox.getItemAt(0) == SELECT_REPOSITORY)) {
            if (comboBox.isPopupVisible()) {

                /*
                 * We do not want to remove item "Select repository..."
                 * while the popup is visible. Only remove it once the user
                 * closes the popup with another item selected.
                 */

                /*
                 * Do not respond to item selection changes while the popup
                 * is visible:
                 */
                comboBox.removeItemListener(this);

                comboBox.addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                        comboBox.removePopupMenuListener(this);

                        /* Restore the item selection listener: */
                        comboBox.addItemListener(RepositoryComboSupport.this);
                    }
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                        comboBox.removePopupMenuListener(this);

                        /* Restore the item selection listener: */
                        comboBox.addItemListener(RepositoryComboSupport.this);
                    }
                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        comboBox.removePopupMenuListener(this);
                        if (comboBox.getSelectedItem() != SELECT_REPOSITORY) {
                            comboBoxModel.removeElementAt(0);
                        } else {
                            /* Restore the item selection listener: */
                            comboBox.addItemListener(RepositoryComboSupport.this);
                        }
                    }
                });
            } else {
                comboBoxModel.removeElementAt(0);
                comboBox.removeItemListener(this);
            }
        }
    }

    @Override
    public void run() {
        if (rp.isRequestProcessorThread()) {
            loadRepositories();

            if (defaultRepoComputationPending) {
                if (repositories.length == 0) {
                    defaultRepoComputationPending = false;
                } else if ((repositories.length == 1) && preselectSingleRepo) {
                    defaultRepo = repositories[0];
                    defaultRepoComputationPending = false;
                }
            }

            /* schedule display of list of repositories */
            updateProgress(Progress.WILL_SCHEDULE_DISPLAY_OF_REPOS);
            EventQueue.invokeLater(this);
            updateProgress(Progress.SCHEDULED_DISPLAY_OF_REPOS);

            if (defaultRepoComputationPending) {
                try {
                    findDefaultRepository();
                } finally {
                    defaultRepoComputationPending = false;
                }
                if (defaultRepo != null) {
                    /* schedule selection of default repository (if any) */
                    updateProgress(Progress.WILL_SCHEDULE_SELECTION_OF_DEFAULT_REPO);
                    EventQueue.invokeLater(this);
                    updateProgress(Progress.SCHEDULED_SELECTION_OF_DEFAULT_REPO);
                }
            }

            notifyProgressDone();
        } else {
            doGuiJob();
        }
    }

    private void doGuiJob() {
        assert EventQueue.isDispatchThread();

        if (repositoriesDisplayed && defaultRepoSelected) {
            /*
             * The default repository selection was performed during the
             * previous invocation of this method (in one shot with displaying
             * the list of available repositories).
             */
            LOG.finest("run() called from AWT - nothing to do - all work already done"); //NOI18N
            return;
        }

        if (shutdown) {
            LOG.finest(" - too late - the component has been already closed"); //NOI18N
            return;
        }

        shutdownDisplayabilityTrigger();

        if (!repositoriesDisplayed) {
            displayRepositories();
        } else {
            selectDefaultRepository();
        }
    }

    private void displayRepositories() {
        updateProgress(Progress.WILL_DISPLAY_REPOS);

        boolean computationPending = defaultRepoComputationPending;
        Repository knownDefaultRepo = computationPending ? null : defaultRepo;

        LOG.finest("going to display the list of repositories");        //NOI18N
        if ((knownDefaultRepo != null) && (LOG.isLoggable(FINEST))) {
            LOG.log(FINEST, "  - default repository: {0}", knownDefaultRepo.getDisplayName());
        }
        try {
            setRepositories(repositories, knownDefaultRepo);
        } finally {
            repositoriesDisplayed = true;
            defaultRepoSelected = !computationPending;
            updateProgress(Progress.DISPLAYED_REPOS);
        }
    }

    private void selectDefaultRepository() {
        assert (!defaultRepoComputationPending) && (defaultRepo != null);
        /*
         * We are going to preselect the default repository.
         */
        LOG.finest("going to select the default repository");           //NOI18N
        updateProgress(Progress.WILL_SELECT_DEFAULT_REPO);
        try {
            if ((comboBox.getSelectedItem() instanceof Repository)
                    && !comboBox.isPopupVisible()) {
                /*
                 * the user has already selected some item - do not override it
                 */
            } else {
                preselectRepository(defaultRepo);
            }
        } finally {
            defaultRepoSelected = true;
            updateProgress(Progress.SELECTED_DEFAULT_REPO);
        }
    }

    /**
     * Selects the given repository in the combo-box if no repository has been
     * selected yet by the user.
     * If the user had already selected some repository before this method
     * was called, this method does nothing. If this method is called at
     * the moment the popup of the combo-box is opened, the operation of
     * pre-selecting the repository is deferred until the popup is closed. If
     * the popup had been displayed at the moment this method was called
     * and the user selects some repository during the period since the
     * call of this method until the deferred selection takes place, the
     * deferred selection operation is cancelled.
     *
     * @param  repoToPreselect  repository to preselect
     */
    private void preselectRepository(final Repository repoToPreselect) {
        assert EventQueue.isDispatchThread();

        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "preselectRepository({0})", repoToPreselect.getDisplayName()); //NOI18N
        }

        if (comboBox.isPopupVisible()) {
            LOG.finest(" - the popup is visible - deferred");           //NOI18N
            comboBox.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    LOG.finer("popupMenuWillBecomeInvisible()");        //NOI18N
                    comboBox.removePopupMenuListener(this);
                }
                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    LOG.finer("popupMenuCanceled()");                   //NOI18N
                    comboBox.removePopupMenuListener(this);
                    LOG.finest(" - processing deferred selection");     //NOI18N
                    preselectItemUnconditionally(repoToPreselect);
                }
            });
        } else {
            preselectItemUnconditionally(repoToPreselect);
        }
    }

    private void preselectItemUnconditionally(Object item) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "preselectItemUnconditionally({0})", getItemName(item)); //NOI18N
        }

        comboBox.setSelectedItem(item);
    }

    private static String getItemName(Object item) {
        if (item instanceof Repository) {
            return ((Repository) item).getDisplayName();
        } else{
            return item.toString();
        }
    }

    private void setRepositories(Repository[] repos,
                                 Repository knownDefaultRepository) {
        assert EventQueue.isDispatchThread();

        int reposCount = (repos != null) ? repos.length : 0;
        Object[] comboData;
        if(repos != null) {
            Arrays.sort(repos, new RepositoryComparator());
        }
        int startIndex = 0;
        if (reposCount == 0) {
            comboData = new Object[] {NO_REPOSITORIES};
        } else if (knownDefaultRepository == null) {
            comboData = new Object[reposCount + 1];
            comboData[startIndex++] = SELECT_REPOSITORY;
        } else {
            comboData = new Object[reposCount];
        }
        if (reposCount != 0) {
            System.arraycopy(repos, 0, comboData, startIndex, reposCount);
        }

        setComboBoxData(comboData);
        if (knownDefaultRepository != null) {
            comboBox.setSelectedItem(knownDefaultRepository);
        } else if (reposCount != 0) {
            assert (comboBox.getSelectedItem() == SELECT_REPOSITORY);
            comboBox.addItemListener(this);
        }
    }

    private void refreshComboBoxData(Repository[] repos) {
        setComboBoxData((repos.length == 0) ? new Object[] {NO_REPOSITORIES}
                                            : repos);
    }

    public void refreshRepositoryModel() {
        LOG.finer("refreshRepositoryModel()");                          //NOI18N
        rp.post(new Runnable() {
            @Override
            public void run() {
                if (rp.isRequestProcessorThread()) {
                    loadRepositories();
                    EventQueue.invokeLater(this);
                } else {
                    assert EventQueue.isDispatchThread();
                    refreshComboBoxData(repositories);
                }
            }
        });
    }

    private void loadRepositories() {
        assert rp.isRequestProcessorThread();
        LOG.finer("loadRepositories()");                                //NOI18N
        updateProgress(Progress.WILL_LOAD_REPOS);

        long startTimeMillis = System.currentTimeMillis();

        if(refFile != null) {
            pingNBRepository(refFile);
        }
        Collection<RepositoryImpl> repoImpls = RepositoryRegistry.getInstance().getKnownRepositories(true);
        List<Repository> repos = new ArrayList<>(repoImpls.size());
        for (RepositoryImpl impl : repoImpls) {
            boolean hidden = hideLocalRepository && BugtrackingManager.isLocalConnectorID(impl.getConnectorId());
            if ( !hidden && 
                 ( filter == RepositoryFilter.ALL || 
                 ( filter == RepositoryFilter.ATTACH_FILE && impl.canAttachFiles()))) 
            {
                repos.add(impl.getRepository());
            }
        }
        repositories = repos.toArray(new Repository[0]);

        long endTimeMillis = System.currentTimeMillis();
        if (LOG.isLoggable(FINEST)) {
            LOG.log(FINEST, "BugtrackingUtil.getKnownRepositories() took {0} ms.", (endTimeMillis - startTimeMillis));   //NOI18N
        }
        updateProgress(Progress.LOADED_REPOS);
    }

    private void findDefaultRepository() {
        assert rp.isRequestProcessorThread();
        LOG.finer("findDefaultRepository()");                           //NOI18N
        updateProgress(Progress.WILL_DETERMINE_DEFAULT_REPO);

        long startTimeMillis, endTimeMillis;
        RepositoryImpl result;

        startTimeMillis = System.currentTimeMillis();

        assert (refFile == null) ^ (selectedFiles == null);

        if (refFile != null) {
            result = BugtrackingOwnerSupport.getInstance().getRepository(refFile,
                                                                         false);
        } else {
            assert (selectedFiles != null);
            result = BugtrackingOwnerSupport.getInstance().getRepository(selectedFiles);
        }

        endTimeMillis = System.currentTimeMillis();

        if (LOG.isLoggable(FINEST)) {
            LOG.log(FINEST, "BugtrackingOwnerSupport.getRepository(...) took {0} ms.", (endTimeMillis - startTimeMillis));   //NOI18N
        }

        if (result != null) {
            if (LOG.isLoggable(FINEST)) {
                LOG.log(FINEST, " - default repository: {0}", result.getDisplayName()); //NOI18N
            }
            defaultRepo = result.getRepository();
        } else {
            LOG.finest(" - default repository: <null>");                //NOI18N
        }

        updateProgress(Progress.DETERMINED_DEFAULT_REPO);
    }

    private void pingNBRepository(FileObject referenceFile) {
        String url = VersioningQuery.getRemoteLocation(referenceFile.toURI());
        if (url != null) {
            if(NBBugzillaUtils.isNbRepository(url)) {
                BugtrackingUtil.findNBRepository(); // ensure repository exists 
            }
        }
    }
        
    //--------------- infrastructure for unit tests -----------------

    enum Progress {
        INITIALIZED,
        STARTED,
        WILL_LOAD_REPOS,
        LOADED_REPOS,
        WILL_SCHEDULE_DISPLAY_OF_REPOS,
        SCHEDULED_DISPLAY_OF_REPOS,
        WILL_DISPLAY_REPOS,
        DISPLAYED_REPOS,
        WILL_DETERMINE_DEFAULT_REPO,
        DETERMINED_DEFAULT_REPO,
        WILL_SCHEDULE_SELECTION_OF_DEFAULT_REPO,
        SCHEDULED_SELECTION_OF_DEFAULT_REPO,
        WILL_SELECT_DEFAULT_REPO,
        SELECTED_DEFAULT_REPO,
        WILL_DISPLAY_REPOS_AND_SELECT_DEFAULT,
        DISPLAYED_REPOS_AND_SELECTED_DEFAULT,
        THREAD_PROGRESS_DONE;
    }

    private final ThreadLocal<Progress> progress = new ThreadLocal<Progress>();
    private volatile ChangeListener progressListener;

    private void notifyProgressDone() {
        if (progressListener != null) {
            assert !EventQueue.isDispatchThread();

            Runnable doneNotifier = new Runnable() {
                @Override
                public void run() {
                    updateProgress(Progress.THREAD_PROGRESS_DONE);
                }
            };
            doneNotifier.run();
            EventQueue.invokeLater(doneNotifier);
        }
    }

    private void updateProgress(Progress progress) {
        this.progress.set(progress);
        fireProgressChange();
    }

    private void fireProgressChange() {
        if (progressListener != null) {
            progressListener.stateChanged(new ChangeEvent(this));
        }
    }

    void setProgressListener(ChangeListener l) {
        progressListener = l;
    }

    Progress getProgress() {
        return progress.get();
    }

}
