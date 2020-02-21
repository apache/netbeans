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
package org.netbeans.modules.cnd.toolchain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.remote.ServerUpdateCache;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.cnd.spi.utils.LongTaskRunner;
import org.netbeans.modules.cnd.spi.utils.LongTaskRunnerFactory;
import org.netbeans.modules.cnd.toolchain.support.ToolchainUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
/**
 *
 */
public final class ToolsCacheManagerImpl extends ToolsCacheManager {

    private static final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
    private ServerUpdateCache serverUpdateCache;
    private final HashMap<ExecutionEnvironment, CompilerSetManager> copiedManagers =
            new HashMap<ExecutionEnvironment, CompilerSetManager>();
    private final AtomicBoolean canceled = new AtomicBoolean(false);
    private Cancellable longTaskCancelable;

    public ToolsCacheManagerImpl(boolean initialize) {
        if (initialize) {
            for (ServerRecord record : ServerList.getRecords()) {
                CompilerSetManager csm = CompilerSetManager.get(record.getExecutionEnvironment());
                addCompilerSetManager(csm);
            }
        }
    }

    @Override
    public ServerUpdateCache getServerUpdateCache() {
        return serverUpdateCache;
    }

    @Override
    public void setHosts(Collection<? extends ServerRecord> list) {
        if (serverUpdateCache == null) {
            serverUpdateCache = new ServerUpdateCache();
        }
        serverUpdateCache.setHosts(list);
    }

    @Override
    public void setDefaultRecord(ServerRecord defaultRecord) {
        serverUpdateCache.setDefaultRecord(defaultRecord);
    }

    @Override
    public void applyChanges() {
        applyChanges(null);
    }

    @Override
    public void discardChanges() {
        clear();
    }

    @Override
    public CompilerSetManager getCompilerSetManagerCopy(ExecutionEnvironment env, boolean initialize) {
        CompilerSetManager out;
        synchronized(canceled) {
            out = (CompilerSetManager) copiedManagers.get(env);
        }
        if (out == null) {
            out = ToolchainUtilities.getDeepCopy(env, initialize);
            if (out.getCompilerSets().size() == 1 && out.getCompilerSets().get(0).getName().equals(CompilerSet.None)) {
                out.remove(out.getCompilerSets().get(0));
            }
            synchronized(canceled) {
                copiedManagers.put(env, out);
            }
        }
        return out;
    }

    @Override
    public void addCompilerSetManager(CompilerSetManager newCsm) {
        synchronized(canceled) {
            copiedManagers.put(newCsm.getExecutionEnvironment(), newCsm);
        }
    }

    @Override
    public CompilerSetManager restoreCompilerSets(CompilerSetManager oldCsm) {

        ExecutionEnvironment execEnv = oldCsm.getExecutionEnvironment();

        ServerRecord record = ServerList.get(execEnv);
        if (record.isOffline()) {
            record.validate(true);
            if (record.isOffline()) {
                return null;
            }
        }

        CompilerSetManager newCsm = ToolchainUtilities.create(execEnv);
        String progressMessage = NbBundle.getMessage(CompilerSetManager.class, "PROGRESS_TEXT", execEnv.getDisplayName()); // NOI18N
        final AtomicBoolean cancel = new AtomicBoolean(false);
        longTaskCancelable = new Cancellable() {
            @Override
            public boolean cancel() {
                cancel.set(true);
                return true;
            }
        };
        ProgressHandle progressHandle = ProgressHandle.createHandle(progressMessage, longTaskCancelable);
        progressHandle.start();
        try {
            newCsm.initialize(false, true, null);
            while (newCsm.isPending()) {
                if (cancel.get()) {
                    if (newCsm.cancel()) {
                        return null;
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    return null;
                }
            }
        } finally {
            progressHandle.finish();
            longTaskCancelable = null;
        }

        if (cancel.get()) {
            return null;
        }

        List<CompilerSet> list = oldCsm.getCompilerSets();
        for (CompilerSet cs : list) {
            if (!cs.isAutoGenerated()) {
                String name = cs.getName();
                String newName = ToolchainUtilities.getUniqueCompilerSetName(newCsm, name);
                if (!name.equals(newName)) {
                    // FIXUP: show a dialog with renamed custom sets. Can't do now because of UI freeze.
                    ToolchainUtilities.setCSName(cs, newName);
                }
                newCsm.add(cs);
            }
        }

        String oldDefaultName;
        CompilerSet oldDefault = oldCsm.getDefaultCompilerSet();
        if (oldDefault != null) {
            oldDefaultName = oldDefault.getName();
        } else {
            oldDefaultName = null;
        }

        CompilerSet newDefault = newCsm.getCompilerSet(oldDefaultName);
        if (newDefault != null) {
            newCsm.setDefault(newDefault);
        }

        if (canceled.get()) {
            return null;
        }
        addCompilerSetManager(newCsm);

        return newCsm;
    }

    public void applyChanges(final ServerRecord selectedRecord) {
        Cancellable aLongTaskCancelable = longTaskCancelable;
        if (aLongTaskCancelable != null) {
            aLongTaskCancelable.cancel();
        }
        Runnable offEDTTask = new Runnable() {
            @Override
            public void run() {
                 List<ExecutionEnvironment> liveServers = null;
                if (serverUpdateCache != null) {
                    liveServers = new ArrayList<>();
                    ServerList.set(serverUpdateCache.getHosts(), serverUpdateCache.getDefaultRecord());
                    for (ServerRecord rec : serverUpdateCache.getHosts()) {
                        liveServers.add(rec.getExecutionEnvironment());
                    }
                    serverUpdateCache = null;
                } else {
                    if (selectedRecord == null) {
                        ServerRecord defaultRecord = ServerList.getDefaultRecord();
                        if (defaultRecord == null) { // or is it a paranoya?
                            ServerList.setDefaultRecord(ServerList.get(ExecutionEnvironmentFactory.getLocal()));
                        }
                    } else {
                        ServerList.setDefaultRecord(selectedRecord);
                    }
                }

                ServerList.save();
                saveCompileSetManagers(liveServers);
            }
        };
        Runnable postEDTTask = new Runnable() {
            @Override
            public void run() {
                fireChange(ToolsCacheManagerImpl.this);
            }
        }; 
        LongTaskRunner runner = LongTaskRunnerFactory.getInstance(offEDTTask, postEDTTask);
        String title = NbBundle.getMessage(ToolsCacheManagerImpl.class, "DLG_TITLE_ApplyChanges"); // NOI18N
        String msg = NbBundle.getMessage(ToolsCacheManagerImpl.class, "MSG_TITLE_ApplyChanges"); // NOI18N

        runner.runLongTask(title, msg, null);
//        final ModalMessageDlg.LongWorker runner = new ModalMessageDlg.LongWorker() {
//            @Override
//            public void doWork() {
//                List<ExecutionEnvironment> liveServers = null;
//                if (serverUpdateCache != null) {
//                    liveServers = new ArrayList<ExecutionEnvironment>();
//                    ServerList.set(serverUpdateCache.getHosts(), serverUpdateCache.getDefaultRecord());
//                    for (ServerRecord rec : serverUpdateCache.getHosts()) {
//                        liveServers.add(rec.getExecutionEnvironment());
//                    }
//                    serverUpdateCache = null;
//                } else {
//                    if (selectedRecord == null) {
//                        ServerRecord defaultRecord = ServerList.getDefaultRecord();
//                        if (defaultRecord == null) { // or is it a paranoya?
//                            ServerList.setDefaultRecord(ServerList.get(ExecutionEnvironmentFactory.getLocal()));
//                        }
//                    } else {
//                        ServerList.setDefaultRecord(selectedRecord);
////                    }
////                }
////
////                ServerList.save();
////                saveCompileSetManagers(liveServers);
////            }
////            @Override
////            public void doPostRunInEDT() {
////                fireChange(ToolsCacheManagerImpl.this);
////            }
////        };
//        if (SwingUtilities.isEventDispatchThread()) {            
////            Frame mainWindow = WindowManager.getDefault().getMainWindow();
////            String title = NbBundle.getMessage(ToolsCacheManagerImpl.class, "DLG_TITLE_ApplyChanges"); // NOI18N
////            String msg = NbBundle.getMessage(ToolsCacheManagerImpl.class, "MSG_TITLE_ApplyChanges"); // NOI18N
////            ModalMessageDlg.runLongTask(mainWindow, title, msg, runner, null);
//        } else {
//            runner.doWork();
//            runner.doPostRunInEDT();
//        }
    }

    @Override
    public Collection<? extends ServerRecord> getHosts() {
        if (serverUpdateCache != null) {
            return serverUpdateCache.getHosts();
        }
        return ServerList.getRecords();
    }

    @Override
    public ServerRecord getDefaultHostRecord() {
        if (serverUpdateCache != null) {
            return serverUpdateCache.getDefaultRecord();
        } else {
            return ServerList.getDefaultRecord();
        }
    }

    @Override
    public boolean hasCache() {
        return serverUpdateCache != null;
    }

    @Override
    public void clear() {
        synchronized(canceled) {
            canceled.set(false);
            serverUpdateCache = null;
            copiedManagers.clear();
        }
    }

    @Override
    public void cancel() {
        synchronized(canceled) {
            canceled.set(true);
            Cancellable aLongTaskCancelable = longTaskCancelable;
            if (aLongTaskCancelable != null) {
                aLongTaskCancelable.cancel();
            }
            serverUpdateCache = null;
            copiedManagers.clear();
        }
    }

    private synchronized void saveCompileSetManagers(List<ExecutionEnvironment> liveServers) {
        Collection<CompilerSetManager> allCSMs = new ArrayList<CompilerSetManager>();
        synchronized(canceled) {
            for (ExecutionEnvironment copiedServer : copiedManagers.keySet()) {
                if (liveServers == null || liveServers.contains(copiedServer)) {
                    allCSMs.add(copiedManagers.get(copiedServer));
                }
            }
            copiedManagers.clear();
        }
        ToolchainUtilities.saveCompileSetManagers(allCSMs, liveServers);
    }

    public static void addChangeListener(ChangeListener l) {
        synchronized (changeListeners) {
            changeListeners.add(l);
        }
    }

    public static void removeChangeListener(ChangeListener l) {
        synchronized (changeListeners) {
            changeListeners.remove(l);
        }
    }

    private static void fireChange(ToolsCacheManagerImpl manager) {
        ChangeListener[] listenersCopy;
        synchronized (changeListeners) {
            listenersCopy = new ChangeListener[changeListeners.size()];
            changeListeners.toArray(listenersCopy);
        }
        ChangeEvent ev = new ChangeEvent(manager);
        for (ChangeListener l : listenersCopy) {
            l.stateChanged(ev);
        }
    }
}
