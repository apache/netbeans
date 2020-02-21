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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.remote.support.RemoteProjectSupport;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.spi.remote.ServerListImplementation;
import org.netbeans.modules.cnd.spi.remote.setup.RemoteSyncFactoryDefaultProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * The cnd.remote implementation of ServerList.
 *
 */
@org.openide.util.lookup.ServiceProvider(service = ServerListImplementation.class)
public class RemoteServerList implements ServerListImplementation, ConnectionListener {

    public static final boolean TRACE_SETUP = Boolean.getBoolean("cnd.remote.trace.setup"); //NOI18N
    public static final String TRACE_SETUP_PREFIX = "#HostSetup"; //NOI18N

    private static final String CND_REMOTE = "cnd.remote"; // NOI18N
    private static final String REMOTE_SERVERS = CND_REMOTE + ".servers"; // NOI18N
    private static final String DEFAULT_RECORD = CND_REMOTE + ".defaultEnv"; // NOI18N
    private volatile RemoteServerRecord defaultRecord;
    private final PropertyChangeSupport pcs;
    private final ChangeSupport cs;
    private final CopyOnWriteArrayList<RemoteServerRecord> unlisted = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<RemoteServerRecord> items = new CopyOnWriteArrayList<>();
    private final Object lock = new Object();
    private static final RequestProcessor RP = new RequestProcessor("Remote setup", 1); // NOI18N

    public RemoteServerList() {
        pcs = new PropertyChangeSupport(this);
        cs = new ChangeSupport(this);

        // Creates the "localhost" record and any remote records cached in remote.preferences

        // "localhost" record
        RemoteServerRecord localRecord = new RemoteServerRecord(ExecutionEnvironmentFactory.getLocal(), null, RemoteSyncFactory.getDefault(), false);
        localRecord.setState(RemoteServerRecord.State.ONLINE);
        items.add(localRecord);

        // now remote records cached in remote.preferences
        String slist = getPreferences().get(REMOTE_SERVERS, null);
        if (slist != null) {
            List<RemoteServerRecord> toAdd = RemoteServerRecord.fromString(slist);
            for (RemoteServerRecord record : toAdd) {
                record.setState(RemoteServerRecord.State.OFFLINE);
                items.add(record);
            }
        }
        defaultRecord = localRecord;
        String defaultEnvId = getPreferences().get(DEFAULT_RECORD, null);
        if (defaultEnvId == null) {
            // Previously, we stored an index; trying to restore...
            int defaultIndex = getPreferences().getInt(".default", 0); //NOI18N
            defaultIndex = Math.min(defaultIndex, items.size() - 1);
            if (defaultIndex >= 0) {
                defaultRecord = items.get(defaultIndex);
            }
        } else {
            ExecutionEnvironment defEnv = ExecutionEnvironmentFactory.fromUniqueID(defaultEnvId);
            for (RemoteServerRecord r : items) {
                if (r.getExecutionEnvironment().equals(defEnv)) {
                    defaultRecord = r;
                    break;
                }
            }
        }

        refresh();
        ConnectionManager.getInstance().addConnectionListener(WeakListeners.create(ConnectionListener.class, this, ConnectionManager.getInstance()));
    }

    @Override
    public void connected(final ExecutionEnvironment env) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                checkSetup(env);
            }
        });
    }

    private void checkSetup(ExecutionEnvironment env) {
        Collection<RemoteServerRecord> recordsToNotify = new ArrayList<>();
        for (RemoteServerRecord rec : items) {
            if (rec.getExecutionEnvironment().equals(env)) {
                if (rec.needsValidationOnConnect()) {
                    recordsToNotify.add(rec);
                }
            }
        }
        // previously, it was done by RemoteFileSupport, but it is moved to dlight.remote
        if (recordsToNotify.isEmpty()) {
            // inlined RemoteServerListUI.revalidate
            RemoteServerRecord record = get(env);
            if (record.needsValidationOnConnect()) {
                if (record.isDeleted()) {
                    addServer(record.getExecutionEnvironment(), record.getDisplayName(), record.getSyncFactory(), false, true);
                } else if (!record.isOnline()) {
                    record.validate(true);
                }
            }
        } else {
            for (RemoteServerRecord rec : recordsToNotify) {
                rec.checkHostInfo();
            }
        }
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
    }

    /**
     * Get a ServerRecord pertaining to env. If needed, create the record.
     *
     * @param env specifies the host
     * @return A RemoteServerRecord for env
     */
    @Override
    public RemoteServerRecord get(ExecutionEnvironment env) {
        return get(env, true);
    }

    public RemoteServerRecord get(ExecutionEnvironment env, boolean create) {
        // Search the active server list
        for (RemoteServerRecord record : items) {
            if (env.equals(record.getExecutionEnvironment())) {
                return record;
            }
        }
        // Search the unlisted servers list. These are records created by Tools->Options
        // which haven't been added yet (and won't until/unless OK is pressed in T->O).
        for (RemoteServerRecord record : unlisted) {
            if (env.equals(record.getExecutionEnvironment())) {
                return record;
            }
        }
        if (create) {
            synchronized (lock) {
                // double check in items and unlisted (now synchronized)
                for (RemoteServerRecord record : items) {
                    if (env.equals(record.getExecutionEnvironment())) {
                        return record;
                    }
                }
                for (RemoteServerRecord record : unlisted) {
                    if (env.equals(record.getExecutionEnvironment())) {
                        return record;
                    }
                }
                // Create a new unlisted record and return it
                RemoteServerRecord record = new RemoteServerRecord(env, null, RemoteServerList.getDefaultFactory(env), false);
                unlisted.add(record);
                return record;
            }
        } else {
            return null;
        }
    }


    @org.netbeans.api.annotations.common.SuppressWarnings("UG") // since get(ExecutionEnvironment) is synchronized
    @Override
    public ServerRecord get(Project project) {
        ExecutionEnvironment execEnv = RemoteProjectSupport.getExecutionEnvironment(project);
        if( execEnv != null) {
            return get(execEnv);
        }
        return null;
    }

    @Override
    public ServerRecord getDefaultRecord() {
        return defaultRecord;
    }

    @Override
    public void setDefaultRecord(ServerRecord record) {
        assert record != null;
        CndUtils.assertNonUiThread();
        synchronized (lock) {
            for (RemoteServerRecord r : items) {
                if (r.equals(record)) {
                    RemoteServerRecord old = defaultRecord;
                    defaultRecord = r;
                    getPreferences().put(DEFAULT_RECORD, ExecutionEnvironmentFactory.toUniqueID(defaultRecord.getExecutionEnvironment()));
                    firePropertyChange(ServerList.PROP_DEFAULT_RECORD, old, r);
                    return;
                }
            }
        }
        CndUtils.assertTrue(false, "Can not set nonexistent record as default");
    }

    @Override
    public List<ExecutionEnvironment> getEnvironments() {
        List<ExecutionEnvironment> result = new ArrayList<>(items.size());
        for (RemoteServerRecord item : items) {
            result.add(item.getExecutionEnvironment());
        }
        return result;
    }

    @Override
    public ServerRecord addServer(final ExecutionEnvironment execEnv, String displayName,
            RemoteSyncFactory syncFactory, boolean asDefault, boolean connect) {
        CndUtils.assertNonUiThread();
        return addServerImpl(execEnv, displayName, syncFactory, asDefault, connect, true);
    }

    private ServerRecord addServerImpl(final ExecutionEnvironment execEnv, String displayName,
            RemoteSyncFactory syncFactory, boolean asDefault, boolean connect, boolean fireChanges) {
        synchronized (lock) {
            RemoteServerRecord record = null;
            if (syncFactory == null) {
                syncFactory = RemoteServerList.getDefaultFactory(execEnv);
            }

            // First off, check if we already have this record
            for (RemoteServerRecord r : items) {
                if (r.getExecutionEnvironment().equals(execEnv)) {
                    if (asDefault) {
                        defaultRecord = r;
                        getPreferences().put(DEFAULT_RECORD, ExecutionEnvironmentFactory.toUniqueID(defaultRecord.getExecutionEnvironment()));
                    }
                    return r;
                }
            }

            // Now see if its unlisted (created in Tools->Options but cancelled with no OK)
            for (RemoteServerRecord r : unlisted) {
                if (r.getExecutionEnvironment().equals(execEnv)) {
                    record = r;
                    break;
                }
            }

            if (record == null) {
                record = new RemoteServerRecord(execEnv, displayName, syncFactory, connect);
            } else {
                record.setDeleted(false);
                record.setDisplayName(displayName);
                record.setSyncFactory(syncFactory);
                unlisted.remove(record);
            }
            ArrayList<RemoteServerRecord> oldItems = new ArrayList<>(items);
            insert(items, record, RECORDS_COMPARATOR);
            if (asDefault) {
                defaultRecord = record;
            }
            if (fireChanges) {
                refresh();
                storePreferences();
                getPreferences().put(DEFAULT_RECORD, ExecutionEnvironmentFactory.toUniqueID(defaultRecord.getExecutionEnvironment()));
                firePropertyChange(ServerList.PROP_RECORD_LIST, oldItems, new ArrayList<>(items));
            }
            return record;
        }
    }

    private static <T> void insert(List<T> list, T value, Comparator<T> comparator) {
        for (int i = 0; i < list.size(); i++) {
            T curr = list.get(i);
            int comparison = comparator.compare(curr, value);
            if (comparison >= 0) {
                int sz = list.size();
                for (int j = sz - 1; j >=  i; j--) {
                    if (j == sz - 1) {
                        list.add(list.get(j));
                    } else {
                        list.set(j + 1, list.get(j));
                    }
                }
                list.set(i, value);
                return;
            }
        }
        list.add(value);
    }

    public static RemoteServerList getInstance() {
        RemoteServerList instance = null;
        for (ServerListImplementation inst : Lookup.getDefault().lookupAll(ServerListImplementation.class)) {
            if (inst instanceof RemoteServerList) {
                instance = (RemoteServerList) inst;
                break;
            }
        }
        return instance;
    }

    public static RemoteSyncFactory getDefaultFactory(ExecutionEnvironment env) {
        RemoteSyncFactoryDefaultProvider rsfdp = Lookup.getDefault().lookup(RemoteSyncFactoryDefaultProvider.class);
        if (rsfdp != null) {
            return rsfdp.getDefaultFactory(env);
        } else {
            return RemoteSyncFactory.getDefault();
        }
    }

    public static void storePreferences() {
        RemoteServerList instance = getInstance();
        if (instance == null)  {
            RemoteUtil.LOGGER.warning("Can not find RemoteServerList instance");
            return;
        }
        List<RemoteServerRecord> records = new ArrayList<>();
        for (RemoteServerRecord record : instance.items) {
            if (record.isRemote()) {
                records.add(record);
            }
        }
        getPreferences().put(REMOTE_SERVERS, RemoteServerRecord.toString(records));
    }

    @Override
    public void set(List<ServerRecord> records, ServerRecord defaultRecord) {
        CndUtils.assertNonUiThread();
        synchronized (lock) {
            ArrayList<RemoteServerRecord> oldItems = new ArrayList<>(items);
            RemoteUtil.LOGGER.log(Level.FINEST, "ServerList: set {0}", records);
            Collection<ExecutionEnvironment> removed = clear();
            List<ExecutionEnvironment> allEnv = new ArrayList<>();
            for (ServerRecord rec : records) {
                addServerImpl(rec.getExecutionEnvironment(), rec.getDisplayName(), rec.getSyncFactory(), false, false, false);
                removed.remove(rec.getExecutionEnvironment());
                allEnv.add(rec.getExecutionEnvironment());
            }
            setDefaultRecord(defaultRecord);
            refresh();
            storePreferences();
            PasswordManager.getInstance().setServerList(allEnv);
            firePropertyChange(ServerList.PROP_RECORD_LIST, oldItems, new ArrayList<>(items));
        }
    }

    @Override
    public void save() {
        unlisted.clear();
    }

    private Collection<ExecutionEnvironment> clear() {
        Collection<ExecutionEnvironment> removed = new ArrayList<>();
        CndUtils.assertNonUiThread();
        synchronized (lock) {
            for (RemoteServerRecord record : items) {
                record.setDeleted(true);
                removed.add(record.getExecutionEnvironment());
            }
            getPreferences().remove(REMOTE_SERVERS);
            unlisted.addAll(items);
            items.clear();
        }
        return removed;
    }

    private void refresh() {
        cs.fireChange();
    }

    public RemoteServerRecord getLocalhostRecord() {
        return items.get(0);
    }

    //TODO: why this is here?
    //TODO: deprecate and remove
    @Override
    public boolean isValidExecutable(ExecutionEnvironment env, String path) {
        if (path == null || path.length() == 0) {
            return false;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            RemoteUtil.LOGGER.warning("RemoteServerList.isValidExecutable from EDT"); // NOI18N
        }
        if (!CndPathUtilities.isPathAbsolute(path)) {
            if (RemoteUtil.isWindows(env) ?
                    (path.contains("\\") || path.contains("/")) : //NOI18N
                    path.contains("/")) { //NOI18N
                // path contains slashes - don't call 'which'
                return false;
            }
            ProcessUtils.ExitStatus res = ProcessUtils.execute(env, "/usr/bin/which", path); // NOI18N
            if (res.isOK()) {
                path = res.getOutputString();
            } else {
                return false;
            }
        }
        try {
            FileInfoProvider.StatInfo info = FileInfoProvider.stat(env, path).get();
            return info.canExecute(env);
        } catch (InterruptedException ex) {
            return false;
        } catch (ExecutionException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public Collection<? extends ServerRecord> getRecords() {
        return new ArrayList<>(items);
    }

    // TODO: Are these still needed?
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    private void firePropertyChange(String property, Object oldValue, Object newValue) {
        pcs.firePropertyChange(property, oldValue, newValue);
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(RemoteServerList.class);
    }

    private static final Comparator<RemoteServerRecord> RECORDS_COMPARATOR = new Comparator<RemoteServerRecord> () {
        @Override
        public int compare(RemoteServerRecord o1, RemoteServerRecord o2) {
            if (o1 == o2) {
                return 0;
            }

            // make localhosts first in the list
            boolean o1local = o1.getExecutionEnvironment().isLocal();
            boolean o2local = o2.getExecutionEnvironment().isLocal();
            if (o1local != o2local) {
                if (o1local) {
                    return -1;
                } else if (o2local) {
                    return 1;
                }
            }

            // others sort in alphabetical order
            return o1.getServerName().compareTo(o2.getServerName());
        }
    };

    @Override
    public ServerRecord createServerRecord(ExecutionEnvironment env, String displayName, RemoteSyncFactory syncFactory) {
        return new RemoteServerRecord(env, displayName, syncFactory, false);
    }
}
