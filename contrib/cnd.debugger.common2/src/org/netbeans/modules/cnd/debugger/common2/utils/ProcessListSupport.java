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

package org.netbeans.modules.cnd.debugger.common2.utils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.debugger.common2.ProcessListAccessor;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfoDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessList;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 */
public final class ProcessListSupport {

    private static final RequestProcessor RP = new RequestProcessor("ProcessListSupport refresh", 3); // NOI18N
    private static final HashMap<ExecutionEnvironment, Provider> cache =
            new HashMap<ExecutionEnvironment, Provider>();    

    private ProcessListSupport() {
    }

    public static Provider getProviderFor(final ExecutionEnvironment env) {
        synchronized (cache) {
            if (env == null || !ConnectionManager.getInstance().isConnectedTo(env)) {
                cache.remove(env);
                return null;
            }

            // TODO: a workaround Host info should be already available at this
            // point ...
            if (!HostInfoUtils.isHostInfoAvailable(env)) {
                RequestProcessor.getDefault().post(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            HostInfoUtils.getHostInfo(env);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (CancellationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }).waitFinished();

                if (!HostInfoUtils.isHostInfoAvailable(env)) {
                    cache.remove(env);
                    return null;
                }
            }

            Provider known = cache.get(env);

            if (known != null) {
                return known;
            }

            Provider result = new Provider(env, PsProvider.getDefault(env));
            cache.put(env, result);
//            Collection<? extends ProcessInfoProvider> allInfoProviders =
//                    Lookup.getDefault().lookupAll(ProcessInfoProvider.class);
//
//            for (ProcessInfoProvider provider : allInfoProviders) {
//                if (provider.supports(env)) {
//                    Provider result = new Provider(env, provider);
//                    cache.put(env, result);
//                    return result;
//                }
//            }

            return result;
        }
    }

    public final static class Provider {

        private final PsProvider infoProvider;
        private final ChangeSupport changeSupport;
        private final ExecutionEnvironment env;
        private final Task updateTask;
        private final AtomicReference<ProcessList> plistRef = new AtomicReference<ProcessList>(null);
        private final AtomicBoolean showAllProcesses = new AtomicBoolean(false);

        private Provider(ExecutionEnvironment env, PsProvider infoProvider) {
            this.infoProvider = infoProvider;
            this.env = env;
            changeSupport = new ChangeSupport(this);
            updateTask = RP.create(new ProcessListUpdater(), true);
        }

        public List<ProcessInfoDescriptor> getDescriptors() {
            List<ProcessInfoDescriptor> result = new ArrayList<>();
            List<ProcessInfoDescriptor> descriptors = infoProvider.getDescriptors();
            for (ProcessInfoDescriptor descriptor : descriptors) {
                if (!ProcessInfoDescriptor.PID_COLUMN_ID.equals(descriptor.id)) {
                   result.add(descriptor); 
                }
            }
            return result;
        }

        /**
         * Returns a list of processes that was retrieved while last successful
         * refresh() request processing.
         *
         * @return
         */
        public ProcessList getProcessList() {
            return plistRef.get();
        }

        /**
         * An asynchronous request to refresh the list of processes. Once it is
         * processed, listeners will be notified. Note: if there is already an
         * active request in processing, the new one will be ignored.
         */
        public void refresh(boolean asynchronious, boolean isAllProcesses) {
            synchronized (updateTask) {
                showAllProcesses.set(isAllProcesses);
                if (asynchronious && !updateTask.isFinished()) {
                    return;
                }

                updateTask.schedule(0);

                if (!asynchronious) {
                    updateTask.waitFinished();
                }
            }
        }

        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        @Override
        public String toString() {
            return "ProcessListProvider for " + env.getDisplayName(); // NOI18N
        }

        private final class ProcessListUpdater implements Runnable {

            @Override
            public void run() {
                Collection<ProcessInfo> info = infoProvider.getData(showAllProcesses.get()).getProcessesInfo();
                if (info != null) {
                    ProcessList newList = ProcessListAccessor.getDefault().create(info, env);
                    plistRef.set(newList);
                    changeSupport.fireChange();
                }
            }
        }
    }
}
