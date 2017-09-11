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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.hudson.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonFolder;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.api.HudsonView;
import org.netbeans.modules.hudson.api.ui.OpenableInBrowser;
import org.netbeans.modules.hudson.constants.HudsonInstanceConstants;
import static org.netbeans.modules.hudson.constants.HudsonInstanceConstants.*;
import static org.netbeans.modules.hudson.constants.HudsonJobConstants.*;
import org.netbeans.modules.hudson.spi.BuilderConnector;
import org.netbeans.modules.hudson.spi.RemoteFileSystem;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Implementation of the HudsonInstacne
 *
 * @author Michal Mocnak
 */
public final class HudsonInstanceImpl implements HudsonInstance, OpenableInBrowser {

    private static final Logger LOG = Logger.getLogger(HudsonInstanceImpl.class.getName());

    private HudsonInstanceProperties properties;
    private BuilderConnector builderConnector;
    private Persistence persistence;
    
    private HudsonVersion version;
    private boolean connected;
    private boolean forbidden;
    private boolean terminated;
    
    private final RequestProcessor RP;
    private final Task synchronization;
    
    private Collection<HudsonJob> jobs = new ArrayList<HudsonJob>();
    private Collection<HudsonFolder> folders = new ArrayList<HudsonFolder>();
    private Collection<HudsonView> views = new ArrayList<HudsonView>();
    private HudsonView primaryView;
    private final Collection<HudsonChangeListener> listeners = new ArrayList<HudsonChangeListener>();
    /**
     * Must be kept here, not in {@link HudsonJobImpl}, because that is transient
     * and this should persist across refreshes.
     */
    private final Map<String,Reference<RemoteFileSystem>> workspaces = new HashMap<String,Reference<RemoteFileSystem>>();
    private final Map<String,Reference<RemoteFileSystem>> artifacts = new HashMap<String,Reference<RemoteFileSystem>>();
    
    private HudsonInstanceImpl(HudsonInstanceProperties properties, boolean interactive, BuilderConnector builderConnector, Persistence persistence) {
        this.builderConnector = builderConnector;
        this.properties = properties;
        this.persistence = persistence != null ? persistence : Persistence.persistent();

        RP = new RequestProcessor(getUrl(), 1, true);
        final AtomicBoolean firstSynch = new AtomicBoolean(interactive); // #200643
        synchronization = RP.create(new Runnable() {
            private boolean firstRun = true;

            @Override
            public void run() {
                String s = getProperties().get(INSTANCE_SYNC);
                int pause = Integer.parseInt(s) * 60 * 1000;
                if (pause > 0 || firstSynch.compareAndSet(true, false)) {
                    doSynchronize(false, firstRun);
                    firstRun = false;
                }
                if (pause > 0) {
                    synchronization.schedule(pause);
                }
            }
        });
        synchronization.schedule(0);
        this.properties.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(INSTANCE_SYNC)) {
                    synchronization.schedule(0);
                }
            }
        });
    }

    @Override public boolean isPersisted() {
        return properties.isPersisted();
    }

    public void makePersistent() {
        if (isPersisted()) {
            return;
        }
        properties.put(INSTANCE_PERSISTED, TRUE);
        fireContentChanges();
    }

    @Override public Preferences prefs() {
        return properties.getPreferences();
    }
    
    public static HudsonInstanceImpl createHudsonInstance(String name, String url, BuilderConnector client, int sync) {
        HudsonInstanceProperties hudsonInstanceProperties =
                new HudsonInstanceProperties(name, url,
                Integer.toBinaryString(sync));
        HudsonInstanceImpl instance = new HudsonInstanceImpl(
                hudsonInstanceProperties, true, client, null);
        if (null == HudsonManagerImpl.getDefault().addInstance(instance)) {
            return null;
        }
        return instance;
    }

    public static HudsonInstanceImpl createHudsonInstance(String name, String url, String sync) {
        return createHudsonInstance(new HudsonInstanceProperties(name, url, sync), true, null);
    }
    
    public static HudsonInstanceImpl createHudsonInstance(HudsonInstanceProperties properties, boolean interactive) {
        return createHudsonInstance(properties, interactive, Persistence.persistent());
    }

    public static HudsonInstanceImpl createHudsonInstance(HudsonInstanceProperties properties, boolean interactive, Persistence persistence) {
        HudsonConnector connector = new HudsonConnector(properties.get(HudsonInstanceConstants.INSTANCE_URL));
        HudsonInstanceImpl instance = new HudsonInstanceImpl(properties, interactive, connector, persistence);

        assert instance.getName() != null;
        assert instance.getUrl() != null;
        assert instance.getProperties().get(INSTANCE_SYNC) != null;
        
        if (null == HudsonManagerImpl.getDefault().addInstance(instance)) {
            return null;
        }
        
        return instance;
    }
    
    public void terminate() {
        // Clear all
        synchronized (this) {
            RP.stop();
            terminated = true;
            connected = false;
            forbidden = false;
            version = null;
            jobs.clear();
            folders.clear();
            views.clear();
            primaryView = null;
        }
        // Fire changes
        fireStateChanges();
        fireContentChanges();
    }
    
    public BuilderConnector getBuilderConnector() {
        return builderConnector;
    }
    
    public synchronized void changeBuilderConnector(BuilderConnector connector) {
        assert !(connector instanceof HudsonConnector);
        this.builderConnector = connector;
        this.jobs.clear();
        folders.clear();
        this.views.clear();
        synchronize(false);
    }

    @Override public HudsonVersion getVersion() {
        return version;
    }
    
    @Override public boolean isConnected() {
        return connected;
    }

    public boolean isForbidden() {
        return forbidden;
    }

    public HudsonInstanceProperties getProperties() {
        return properties;
    }
    
    @Override public String getName() {
        return getProperties().get(INSTANCE_NAME);
    }
    
    @Override public String getUrl() {
        String url = getProperties().get(INSTANCE_URL);
        assert url.endsWith("/") : url;
        return url;
    }
    
    @Override public synchronized Collection<HudsonJob> getJobs() {
        return new ArrayList<HudsonJob>(jobs);
    }

    @Override public synchronized Collection<HudsonFolder> getFolders() {
        return new ArrayList<HudsonFolder>(folders);
    }

    boolean isSalient(HudsonJobImpl job) {
        HudsonInstanceProperties props = getProperties();
        if (HudsonInstanceProperties.split(props.get(INSTANCE_SUPPRESSED_JOBS)).contains(job.getName())) {
            return false;
        }
        List<String> preferred = HudsonInstanceProperties.split(props.get(INSTANCE_PREF_JOBS));
        if (!preferred.isEmpty()) {
            return preferred.contains(job.getName());
        }
        return true;
    }
    void setSalient(HudsonJobImpl job, boolean salient) {
        HudsonInstanceProperties props = getProperties();
        List<String> preferred = new ArrayList<String>(HudsonInstanceProperties.split(props.get(INSTANCE_PREF_JOBS)));
        if (salient && !preferred.isEmpty() && !preferred.contains(job.getName())) {
            List<String> list = new ArrayList<String>(preferred);
            list.add(job.getName());
            props.put(INSTANCE_PREF_JOBS, HudsonInstanceProperties.join(list));
        }
        List<String> suppressed = new ArrayList<String>(HudsonInstanceProperties.split(props.get(INSTANCE_SUPPRESSED_JOBS)));
        if (salient) {
            suppressed.remove(job.getName());
        } else if (!suppressed.contains(job.getName())) {
            suppressed.add(job.getName());
        }
        props.put(INSTANCE_SUPPRESSED_JOBS, HudsonInstanceProperties.join(suppressed));
        fireContentChanges();
    }
    
    public @Override synchronized Collection<HudsonView> getViews() {
        return new ArrayList<HudsonView>(views);
    }
    
    public @Override synchronized HudsonView getPrimaryView() {
        if (primaryView == null) {
            primaryView = new HudsonViewImpl(this, "All", getUrl()); // NOI18N
        }
        return primaryView;
    }
    
    synchronized void setViews(Collection<HudsonView> views, HudsonView primaryView) {
        this.views = views;
        this.primaryView = primaryView;
    }

    /**
     * Initiate synchronization: fetching refreshed job data from the server.
     * Will run asynchronously.
     * @param authentication to prompt for login if the anonymous user cannot even see the job list; set to true for explicit user gesture, false otherwise
     */
    @Override
    public void synchronize(final boolean authentication) {
        if (terminated) {
            return;
        }
        RP.post(new Runnable() {
            @Override public void run() {
                doSynchronize(authentication, true);
            }
        });
    }

    @Messages({"# {0} - server label", "MSG_Synchronizing=Synchronizing {0}"})
    private void doSynchronize(final boolean authentication,
            final boolean showProgress) {
        final AtomicReference<Thread> synchThread = new AtomicReference<Thread>();
        final AtomicReference<ProgressHandle> handle = new AtomicReference<ProgressHandle>();
        ProgressHandle handleObject = ProgressHandleFactory.createHandle(
                Bundle.MSG_Synchronizing(getName()),
                new Cancellable() {
                    @Override
                    public boolean cancel() {
                        Thread t = synchThread.get();
                        if (t != null) {
                            LOG.log(Level.FINE,
                                    "Cancelling synchronization of {0}",//NOI18N
                                    getUrl());
                            if (!isPersisted()) {
                                properties.put(INSTANCE_SYNC, "0");     //NOI18N
                            }
                            t.interrupt();
                            handle.get().finish();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
        handleObject.setInitialDelay(showProgress ? 100 : 30000);
        handle.set(handleObject);
            
            handle.get().start();

            if (authentication) {
                ConnectionBuilder.clearRejectedAuthentication();
            }
            
                    synchThread.set(Thread.currentThread());
                    try {
                        // Get actual views
                        Collection<HudsonView> oldViews = getViews();
                        
                        // Retrieve jobs
                        BuilderConnector.InstanceData instanceData =
                                getBuilderConnector().getInstanceData(
                                authentication);
                        configureViews(instanceData.getViewsData());
                        Collection<HudsonJob> retrieved = createJobs(
                                instanceData.getJobsData());
                        Collection<HudsonFolder> retrievedFolders = createFolders(instanceData.getFoldersData());
                        
                        // Exit when instance is terminated
                        if (terminated) {
                            return;
                        }
                        
                        // Set connected and version
                        connected = getBuilderConnector().isConnected();
                        version = getBuilderConnector().getHudsonVersion(authentication);
                        forbidden = getBuilderConnector().isForbidden();
                        
                        // Update state
                        fireStateChanges();

                        synchronized (workspaces) {
                            Iterator<Map.Entry<String,Reference<RemoteFileSystem>>> it = workspaces.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry<String,Reference<RemoteFileSystem>> entry = it.next();
                                RemoteFileSystem fs = entry.getValue().get();
                                if (fs != null) {
                                    fs.refreshAll();
                                } else {
                                    it.remove();
                                }
                            }
                        }

                        synchronized (this) {
                            // When there are no changes return and do not fire changes
                            if (jobs.equals(retrieved)
                                    && folders.equals(retrievedFolders)
                                    && oldViews.equals(views)) {
                                return;
                            }

                            // Update jobs
                            jobs = retrieved;
                            folders = retrievedFolders;
                        }

                        // Fire all changes
                        fireContentChanges();
                    } finally {
                        handle.get().finish();
                    }
    }
    
    @Override public void addHudsonChangeListener(HudsonChangeListener l) {
        if (l != null) {
            synchronized (listeners) {
                listeners.add(l);
            }
        }
    }
    
    @Override public void removeHudsonChangeListener(HudsonChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireStateChanges() {
        ArrayList<HudsonChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<HudsonChangeListener>(listeners);
        }
        
        for (HudsonChangeListener l : tempList) {
            l.stateChanged();
        }
    }
    
    private void fireContentChanges() {
        ArrayList<HudsonChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<HudsonChangeListener>(listeners);
        }
        
        for (HudsonChangeListener l : tempList) {
            l.contentChanged();
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof HudsonInstance && getUrl().equals(((HudsonInstance) obj).getUrl());
    }

    @Override
    public int hashCode() {
        return getUrl().hashCode();
    }

    public @Override String toString() {
        return getUrl();
    }

    @Override public int compareTo(HudsonInstance o) {
        return getName().compareTo(o.getName());
    }

    /* access from HudsonJobImpl */ FileSystem getRemoteWorkspace(final HudsonJob job) {
        return getFileSystemFromCache(workspaces, job.getName(), new Callable<RemoteFileSystem>() {
            @Override public RemoteFileSystem call() throws Exception {
                return builderConnector.getWorkspace(job);
            }
        });
    }

    /* access from HudsonJobBuildImpl */ FileSystem getArtifacts(final HudsonJobBuild build) {
        return getFileSystemFromCache(artifacts, build.getJob().getName() + "/" + build.getNumber(), new Callable<RemoteFileSystem>() { // NOI18N
            @Override public RemoteFileSystem call() throws Exception {
                return builderConnector.getArtifacts(build);
            }
        });
    }

    /* access from HudsonJobBuildImpl */ FileSystem getArtifacts(final HudsonMavenModuleBuild module) {
        return getFileSystemFromCache(artifacts, module.getBuild().getJob().getName() + "/" + // NOI18N
                module.getBuild().getNumber() + "/" + module.getName(), // NOI18N
                new Callable<RemoteFileSystem>() {
            @Override public RemoteFileSystem call() throws Exception {
                return builderConnector.getArtifacts(module);
            }
        });
    }

    private static FileSystem getFileSystemFromCache(Map<String,Reference<RemoteFileSystem>> cache, String key, Callable<RemoteFileSystem> create) {
        synchronized (cache) {
            RemoteFileSystem fs = cache.containsKey(key) ? cache.get(key).get() : null;
            if (fs == null) {
                try {
                    fs = create.call();
                    if (fs == null) {
                        return null;
                    }
                    cache.put(key, new WeakReference<RemoteFileSystem>(fs));
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return FileUtil.createMemoryFileSystem();
                }
            }
            return fs;
        }
    }

    public Collection<HudsonJob> createJobs(
            Collection<BuilderConnector.JobData> data) {

        Collection<HudsonJob> jobList = new ArrayList<HudsonJob>();

        for (BuilderConnector.JobData jd : data) {

            HudsonJobImpl job = new HudsonJobImpl(this);
            if (jd.isSecured()) {
                job.putProperty(JOB_COLOR, HudsonJob.Color.secured);
            }
            job.putProperty(JOB_NAME, jd.getJobName());
            job.putProperty(JOB_URL, jd.getJobUrl());
            if (jd.getColor() != null) { // may be null, see #230406
                job.putProperty(JOB_COLOR, jd.getColor());
            }
            job.putProperty(JOB_DISPLAY_NAME, jd.getDisplayName() == null
                    ? jd.getJobName() : jd.getDisplayName());
            job.putProperty(JOB_BUILDABLE, jd.isBuildable());
            job.putProperty(JOB_IN_QUEUE, jd.isInQueue());
            job.putProperty(JOB_LAST_BUILD, jd.getLastBuild());
            job.putProperty(JOB_LAST_FAILED_BUILD, jd.getLastFailedBuild());
            job.putProperty(JOB_LAST_STABLE_BUILD, jd.getLastStableBuild());
            job.putProperty(JOB_LAST_SUCCESSFUL_BUILD, jd.getLastSuccessfulBuild());
            job.putProperty(JOB_LAST_COMPLETED_BUILD, jd.getLastCompletedBuild());

            for (BuilderConnector.ModuleData md : jd.getModules()) {
                job.addModule(md.getName(), md.getDisplayName(), md.getColor(), md.getUrl());
            }
            for (HudsonView v : this.getViews()) {
                /* https://github.com/hudson/hudson/commit/105f2b09cf1376f9fe4dbf80c5bdb7a0d30ba1c1#commitcomment-447142 */
                if (jd.isSecured() || jd.getViews().contains(v.getName())) {
                    job.addView(v);
                }
            }
            jobList.add(job);
        }
        return jobList;
    }

    public Collection<HudsonFolder> createFolders(Collection<BuilderConnector.FolderData> foldersData) {
        Collection<HudsonFolder> result = new ArrayList<HudsonFolder>();
        for (BuilderConnector.FolderData datum : foldersData) {
            result.add(new HudsonFolderImpl(this, datum.getName(), datum.getUrl()));
        }
        return result;
    }

    private void configureViews(Collection<BuilderConnector.ViewData> viewsData) {

        Collection<HudsonView> viewList = new ArrayList<HudsonView>();
        HudsonView foundPrimaryView = null;

        for (BuilderConnector.ViewData viewData: viewsData) {
            HudsonViewImpl view = new HudsonViewImpl(this, viewData.getName(),
                    viewData.getUrl());
            viewList.add(view);
            if (viewData.isPrimary()) {
                foundPrimaryView = view;
            }
        }
        this.setViews(viewList, foundPrimaryView);
    }

    public Persistence getPersistence() {
        return persistence;
    }

    @Override
    public List<String> getPreferredJobs() {
        String preferred = properties.get(INSTANCE_PREF_JOBS);
        if (preferred == null) {
            return null;
        } else {
            return HudsonInstanceProperties.split(preferred);
        }
    }

    @Override
    public void setPreferredJobs(List<String> preferredJobs) {
        if (preferredJobs == null) {
            properties.put(INSTANCE_PREF_JOBS, null);
        } else {
            properties.put(INSTANCE_PREF_JOBS,
                    HudsonInstanceProperties.join(preferredJobs));
        }
    }

    @Override
    public int getSyncInterval() {
        return Integer.parseInt(getProperties().get(
                HudsonInstanceConstants.INSTANCE_SYNC));
    }

    @Override
    public void setSyncInterval(int syncInterval) {
        getProperties().put(HudsonInstanceConstants.INSTANCE_SYNC,
                Integer.toString(syncInterval));
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getProperties().addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getProperties().removePropertyChangeListener(listener);
    }
}
