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

package org.netbeans.modules.cnd.remote.server;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import static org.netbeans.modules.cnd.remote.server.RemoteServerList.TRACE_SETUP;
import static org.netbeans.modules.cnd.remote.server.RemoteServerList.TRACE_SETUP_PREFIX;
import org.netbeans.modules.cnd.remote.support.ParallelWorker;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.spi.remote.setup.HostSetupProvider;
import org.netbeans.modules.cnd.spi.utils.CndNotifier;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.remote.api.ConnectionNotifier;
//import org.netbeans.modules.remote.api.ui.ConnectionNotifier;
//import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * The definition of a remote server and login. 
 * 
 */
public class RemoteServerRecord implements ServerRecord, ConnectionNotifier.ExplicitConnectionListener {

    public static enum State {
        UNINITIALIZED, INITIALIZING, ONLINE, OFFLINE, CANCELLED;
    }
    
    private final ExecutionEnvironment executionEnvironment;
    private final boolean editable;
    private boolean deleted;
    private State state;
    private final Object stateLock;
    private String reason;
    private String problems;
    private String displayName;
    private RemoteSyncFactory syncFactory;
    private boolean x11forwarding;
//    private boolean x11forwardingPossible;
    private final PropertyChangeSupport pcs;

    private HostInfo.OSFamily cachedOsFamily = null;
    private HostInfo.CpuFamily cachedCpuFamily = null;
    private String cachedOsVersion = null;
    private final RequestProcessor requestProcessor = new RequestProcessor(getClass().getSimpleName(), 10); //NOI18N
    private boolean needsValidationOnConnect = true;
    
    /**
     * Create a new ServerRecord. This is always called from RemoteServerList.get, but can be
     * in the AWT Event thread if called while adding a node from ToolsPanel, or in a different
     * thread if called during startup from cached information.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("Dm")
    /*package-local*/ RemoteServerRecord(final ExecutionEnvironment env, String displayName, RemoteSyncFactory syncFactory, boolean connect) {
        CndUtils.assertTrue(env != null);
        CndUtils.assertTrue(syncFactory != null);
        this.executionEnvironment = env;
        this.syncFactory = syncFactory;
        stateLock = new String("RemoteServerRecord state lock for " + toString()); // NOI18N
        reason = null;
        deleted = false;
        this.displayName = escape(displayName);
        this.pcs = new PropertyChangeSupport(this);
        if (env.isLocal()) {
            editable = false;
            state = State.ONLINE;            
        } else {
            editable = true;
            state = connect ? State.UNINITIALIZED : State.OFFLINE;
        }
        x11forwarding = Boolean.getBoolean("cnd.remote.X11"); //NOI18N;
//        x11forwardingPossible = true;
        
        checkHostInfo(); // is this a paranoya?
        if (env.isRemote()) {
            ConnectionNotifier.addExplicitConnectionListener(executionEnvironment, this);
        }
    }

    @Override
    public void connected() {
        RemoteUtil.checkSetupAfterConnection(this);
    }    

    @Override
    public ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }

    @Override
    public String toString() {
        return executionEnvironment.toString();
    }

    @Override
    public synchronized void validate(final boolean force) {
        if (isOnline()) {
            return;
        }
        RemoteUtil.LOGGER.log(Level.FINE, "RSR.validate2: Validating {0}", toString());
        if (force) {
            ProgressHandle ph = ProgressHandle.createHandle(NbBundle.getMessage(RemoteServerRecord.class, "PBAR_ConnectingTo", getDisplayName())); // NOI18N
            ph.start();
            init(null);
            ph.finish();
        }
        String msg;
        if (isOnline()) {
            msg = NbBundle.getMessage(RemoteServerRecord.class, "Validation_OK", getDisplayName());// NOI18N
        } else {
            msg = NbBundle.getMessage(RemoteServerRecord.class, "Validation_ERR", getDisplayName(), getStateAsText(), getReason());// NOI18N
        }
        CndNotifier.getDefault().notifyStatus(msg);
    }

    @Override
    public void checkSetupAfterConnection(Runnable task) {
        if (!isOnline()) {
            resetOfflineState();
            init(null);
            if (isOnline()) {
                if (task != null) {
                    task.run();
                }
            }
        }
    }

    
    /**
     * Start the initialization process. This should <b>never</b> be done from the AWT Event
     * thread. Parts of the initialization use this thread and will block.
     */
    public synchronized void init(PropertyChangeSupport pcs) {
        assert !SwingUtilities.isEventDispatchThread() : "RemoteServer initialization must be done out of EDT"; // NOI18N
        try {
            ConnectionManager.getInstance().connectTo(executionEnvironment);
        } catch (IOException ex) {
            CndNotifier.getDefault().notifyStatus(NbBundle.getMessage(RemoteServerRecord.class, "ERR_ConnectingToHost", executionEnvironment, ex.getLocalizedMessage()));
            reason = ex.getMessage();            
            setState(State.OFFLINE);
            return;
        } catch (CancellationException ex) {
            setState(State.CANCELLED);
            return;
        }
        Object ostate = state;
        setState(State.INITIALIZING);        
        final RemoteServerSetup rss = new RemoteServerSetup(getExecutionEnvironment());
        CountDownLatch latch = new CountDownLatch(1);
        requestProcessor.post(new ParallelWorker("Updating remote binaries at " + getExecutionEnvironment(), latch) { //NOI18N
            @Override
            protected void runImpl() {
                // We could do fast checks without posting a task to RP;
                // but in fact it's always true; cheking outside => calculating the latch count
                if (!Boolean.getBoolean("cnd.remote.skip.setup")) {                    
                    final boolean needsSetupOrUpdate = rss.needsSetupOrUpdate();
                    if (needsSetupOrUpdate) {
                        StopWatch sw = StopWatch.createAndStart(TRACE_SETUP, TRACE_SETUP_PREFIX, executionEnvironment, "rss.setup"); //NOI18N
                        rss.setup();
                        sw.stop();
                    }
                }
//        if (ostate == State.UNINITIALIZED) {
//            checkX11Forwarding();
//        }
            }
        });

        requestProcessor.post(new ParallelWorker("Initializing path map at " + getExecutionEnvironment(), null) { //NOI18N
            @Override
            protected void runImpl() {
                StopWatch sw;
                sw = StopWatch.createAndStart(TRACE_SETUP, TRACE_SETUP_PREFIX, executionEnvironment, "init pathmap"); //NOI18N
                RemotePathMap.getPathMap(getExecutionEnvironment()).initIfNeeded();
                sw.stop();
            }
        });
        
        try {
            latch.await(10, TimeUnit.MINUTES);
            synchronized (stateLock) {
                setState(State.ONLINE);
                if (rss.hasProblems()) {
                    problems = rss.getReason();
                }
            }
        } catch (InterruptedException ex) {
            synchronized (stateLock) {
                setState(State.CANCELLED);
            }            
        }

        if (pcs != null) {
            pcs.firePropertyChange(RemoteServerRecord.PROP_STATE_CHANGED, ostate, state);
        }        
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    

//    private void checkX11Forwarding() {
//        X11ForwardingChecker x11checker = new X11ForwardingChecker(executionEnvironment);
//        try {
//            x11forwardingPossible = x11checker.check();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        } catch (CancellationException ex) {
//            ex.printStackTrace();
//        }
//    }
    
    public String getProblems() {
        return problems;
    }
    
    public boolean hasProblems() {
        return problems != null;
    }
    
    public boolean resetOfflineState() {
        synchronized (stateLock) {
            if (this.state != State.INITIALIZING && state != State.ONLINE) {
                setState(State.UNINITIALIZED);
                return true;
            }
        }
        return false;
    }
    
    public String getStateAsText() {
        // TODO: not good to use object's toString as resource key
        return NbBundle.getMessage(RemoteServerRecord.class, state.toString());
    }

    public boolean needsValidationOnConnect() {
        synchronized (stateLock) {
            return needsValidationOnConnect;
        }
    }

    public void setNeedsValidationOnConnect(boolean needsValidationOnConnect) {
        synchronized (stateLock) {
            this.needsValidationOnConnect = needsValidationOnConnect;
        }
    }

    @Override
    public boolean isOnline() {
        if (state == State.ONLINE && !ConnectionManager.getInstance().isConnectedTo(executionEnvironment)) {
            setState(State.OFFLINE);
        }
        return state == State.ONLINE;
    }

    @Override
    public boolean isOffline() {
        if (state == State.ONLINE && !ConnectionManager.getInstance().isConnectedTo(executionEnvironment)) {
            setState(State.OFFLINE);
        }
        return state == State.OFFLINE;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }
    
    public boolean isEditable() {
        return editable;
    }

    @Override
    public boolean isRemote() {
        return executionEnvironment.isRemote();
    }

    @Override
    public String getDisplayName() {
        return (displayName != null && displayName.length() > 0) ? displayName : executionEnvironment.getDisplayName();
    }

    /* package-local */ String getRawDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteServerRecord other = (RemoteServerRecord) obj;
        if (this.executionEnvironment != other.executionEnvironment && (this.executionEnvironment == null || !this.executionEnvironment.equals(other.executionEnvironment))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.executionEnvironment != null ? this.executionEnvironment.hashCode() : 0);
        return hash;
    }

    @Override
    public String getServerDisplayName() {
        if (displayName == null || displayName.length() == 0) {
            // TODO: should we add ExecutionEnvironment.getHostDisplayName() ?
            if (executionEnvironment.isLocal()) {
                return "localhost"; //NOI18N
            } else {
                return executionEnvironment.getHost();
            }
        } else {
            return displayName;
        }
    }

    public void setDisplayName(String displayName) {
        String oldName = this.displayName;
        String newName = escape(displayName);
        this.displayName = newName;
        this.pcs.firePropertyChange(DISPLAY_NAME_CHANGED, oldName, newName);
    }

    // #164242 Remote gets in trouble in the case user uses "," in host display name
    private String escape(String text) {
        if (text != null) {
            text = text.replace('|', '_'); //NOI18N
            text = text.replace(',', '_'); //NOI18N
        }
        return text;
    }

    @Override
    public String getServerName() {
        return executionEnvironment.getHost();
    }

    @Override
    public String getUserName() {
        return executionEnvironment.getUser();
    }

    @Override
    public boolean isRememberPassword() {
        return PasswordManager.getInstance().isRememberPassword(executionEnvironment);
    }

    public void setRememberPassword(boolean rememberPassword) {
        PasswordManager.getInstance().setRememberPassword(executionEnvironment, rememberPassword);
    }
    
    public String getReason() {
        return reason == null ? "" : reason;
    }

    @Override
    public RemoteSyncFactory getSyncFactory() {
        return this.syncFactory;
    }

    public void setSyncFactory(RemoteSyncFactory factory) {
        this.syncFactory = factory;
    }

    /*package*/void setState(State newState) {
        State oldState = this.state;
        this.state = newState;
        this.pcs.firePropertyChange(RemoteServerRecord.PROP_STATE_CHANGED, oldState, newState);
    }

    @Override
    public boolean isSetUp() {
        final Collection<? extends HostSetupProvider> providers = Lookup.getDefault().lookupAll(HostSetupProvider.class);
        for (HostSetupProvider provider : providers) {
            if (provider.canCheckSetup(executionEnvironment)) {
                return provider.isSetUp(executionEnvironment);
            }
        }
        return true;
    }

    @Override
    public boolean setUp() {
        final Collection<? extends HostSetupProvider> providers = Lookup.getDefault().lookupAll(HostSetupProvider.class);
        for (HostSetupProvider provider : providers) {
            if (provider.canCheckSetup(executionEnvironment)) {
                return provider.setUp(executionEnvironment);
            }
        }
        return true;
    }

    public boolean getX11Forwarding() {
        return x11forwarding;
    }

    public void setX11Forwarding(boolean x11forwarding) {
        this.x11forwarding = x11forwarding;
    }

//    public boolean isX11forwardingPossible() {
//        return x11forwardingPossible;
//    }
//
//    public void setX11forwardingPossible(boolean x11forwardingPossible) {
//        this.x11forwardingPossible = x11forwardingPossible;
//    }

    private static final char SERVER_RECORD_SEPARATOR = '|'; //NOI18N
    private static final String SERVER_LIST_SEPARATOR = ","; //NOI18N

    /*package-local*/ static List<RemoteServerRecord> fromString(String slist) {
        List<RemoteServerRecord> result = new ArrayList<>();

        for (String serverString : slist.split(SERVER_LIST_SEPARATOR)) { // NOI18N
            // there moght be to forms:
            // 1) user@host:port
            // 2) user@host:port|DisplayName
            // 3) user@host:port|DisplayName|syncID
            // 4) user@host:port|DisplayName|syncID|x11possible|x11
            if (serverString.trim().isEmpty()) {
                continue;
            }
            String displayName = null;
            RemoteSyncFactory syncFactory = RemoteSyncFactory.getDefault();
            final String[] arr = serverString.split("\\" + SERVER_RECORD_SEPARATOR); // NOI18N
            CndUtils.assertTrue(arr.length > 0);
            String hostKey = arr[0];
            if (arr.length > 1) {
                displayName = arr[1];
            }
            ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(hostKey);
            if (arr.length > 2) {
                final String syncId = arr[2];
                syncFactory = RemoteSyncFactory.fromID(syncId);
                if (syncFactory == null) {
                    syncFactory = RemoteSyncFactory.getDefault();
                    RemoteUtil.LOGGER.log(Level.WARNING, "Unsupported synchronization mode \"{0}\" for {1}. Switching to default one.", new Object[]{syncId, env.toString()}); //NOI18N
                }
            }
            if (env.isRemote()) {
                RemoteServerRecord record = new RemoteServerRecord(env, displayName, syncFactory, false);
                record.setState(RemoteServerRecord.State.OFFLINE);
                result.add(record);
                if (arr.length > 3) {
                    record.setX11Forwarding(Boolean.parseBoolean(arr[3]));
                }
                if (arr.length > 4) {
                    if (arr[4].length() > 0) {
                        try {
                            record.cachedOsFamily = HostInfo.OSFamily.valueOf(arr[4]);
                        } catch (IllegalArgumentException ex) {
                            RemoteUtil.LOGGER.log(Level.WARNING, "Error restoring OS family", ex);
                        }
                    }
                }
                if (arr.length > 5) {
                    if (arr[5].length() > 0) {
                        try {
                            record.cachedCpuFamily = HostInfo.CpuFamily.valueOf(arr[5]);
                        } catch (IllegalArgumentException ex) {
                            RemoteUtil.LOGGER.log(Level.WARNING, "Error restoring CPU family", ex);
                        }
                    }
                }
                if (arr.length > 6) {
                    if (arr[5].length() > 0) {
                        record.cachedOsVersion = arr[6];
                    }
                }
            }
        }

        return result;
    }

    /*package-local*/ static String toString(List<RemoteServerRecord> records) {
        StringBuilder result = new StringBuilder();
        for (RemoteServerRecord record : records) {
            if (result.length() > 0) {
                result.append(SERVER_LIST_SEPARATOR);
            }
            String displayName = record.getRawDisplayName();
            String hostKey = ExecutionEnvironmentFactory.toUniqueID(record.getExecutionEnvironment());

            HostInfo.CpuFamily cpuFamily = record.getCpuFamily();
            HostInfo.OSFamily osFamily = record.getOsFamily();
            String osVersion = record.getOsVersion();

            String preferencesKey = hostKey + SERVER_RECORD_SEPARATOR +
                    ((displayName == null) ? "" : displayName) + SERVER_RECORD_SEPARATOR +
                    record.getSyncFactory().getID()  + SERVER_RECORD_SEPARATOR +
                    record.getX11Forwarding() + SERVER_RECORD_SEPARATOR +
                    ((osFamily == null) ? "" : osFamily.name()) + SERVER_RECORD_SEPARATOR +
                    ((cpuFamily == null) ? "" : cpuFamily.name()) + SERVER_RECORD_SEPARATOR +
                    ((osVersion == null) ? "" : osVersion);

            result.append(preferencesKey);

        }
        return result.toString();
    }

    public String getOsVersion() {
        return cachedOsVersion;
    }
    
    public HostInfo.CpuFamily getCpuFamily() {
        return cachedCpuFamily;
    }

    public HostInfo.OSFamily getOsFamily() {
        return cachedOsFamily;
    }

    /*package-local*/ final void checkHostInfo() {
        if (HostInfoUtils.isHostInfoAvailable(executionEnvironment)) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(executionEnvironment);
                HostInfo.OSFamily osFamily = hostInfo.getOSFamily();
                HostInfo.CpuFamily cpuFamily = hostInfo.getCpuFamily();
                String osVersion = hostInfo.getOS().getVersion();
                if (!osFamily.equals(cachedOsFamily) || !cpuFamily.equals(cachedCpuFamily) || !osVersion.equals(cachedOsVersion)) {
                    cachedOsFamily = osFamily;
                    cachedCpuFamily = cpuFamily;
                    cachedOsVersion = osVersion;
                    if (executionEnvironment.isRemote() && !syncFactory.isApplicable(executionEnvironment)) {
                        for (RemoteSyncFactory newFactory : RemoteSyncFactory.getFactories()) {
                            if (newFactory.isApplicable(executionEnvironment)) {
                                RemoteUtil.LOGGER.log(Level.WARNING, "Inapplicable factory for {0} : {1}; changing to {2}",
                                        new Object[] { executionEnvironment.getDisplayName(), syncFactory.getDisplayName(), newFactory.getDisplayName() });
                                syncFactory = newFactory;
                                break;
                            }
                        }
                    }
                }
            } catch (IOException | CancellationException ex) {
                // don't report
            }
        }

    }
}
