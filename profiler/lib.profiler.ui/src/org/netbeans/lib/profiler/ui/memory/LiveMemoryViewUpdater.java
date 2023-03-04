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

package org.netbeans.lib.profiler.ui.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.filters.GenericFilter;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import org.netbeans.lib.profiler.results.memory.MemoryCCTProvider;
import org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
public class LiveMemoryViewUpdater {
    
    private static final int MIN_UPDATE_DIFF = 900;
    private static final int MAX_UPDATE_DIFF = 1400;
    
    
    private CCTHandler handler;
    
    private final LiveMemoryView memoryView;
    private final ProfilerClient client;
    
    private volatile boolean paused;
    private volatile boolean forceRefresh;
    
    
    public LiveMemoryViewUpdater(LiveMemoryView memoryView, ProfilerClient client) {
        this.memoryView = memoryView;
        this.client = client;
        
        handler = CCTHandler.registerUpdater(this);
    }
    
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }
    
    public void update() throws ClientUtils.TargetAppOrVMTerminated {
        if (forceRefresh || (!paused && memoryView.getLastUpdate() + MAX_UPDATE_DIFF < System.currentTimeMillis()))
            switch (client.getCurrentInstrType()) {
                case CommonConstants.INSTR_NONE_MEMORY_SAMPLING:
                    updateData();
                    break;
                case CommonConstants.INSTR_OBJECT_LIVENESS:
                case CommonConstants.INSTR_OBJECT_ALLOCATIONS:
                    if (memoryView.getLastUpdate() + MAX_UPDATE_DIFF < System.currentTimeMillis()) {
                        client.forceObtainedResultsDump(true);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid profiling instr. type: " + client.getCurrentInstrType()); // NOI18N
            }
    }
    
    public void cleanup() {
        handler.unregisterUpdater(this);
        handler = null;
    }
    
    
    private void updateData() throws ClientUtils.TargetAppOrVMTerminated {
        if (!forceRefresh && (paused || memoryView.getLastUpdate() + MIN_UPDATE_DIFF > System.currentTimeMillis())) return;
        
        MemoryResultsSnapshot snapshot = client.getMemoryProfilingResultsSnapshot(false);

        // class names in VM format
        MemoryView.userFormClassNames(snapshot);

        // class names in VM format
        GenericFilter filter = client.getSettings().getInstrumentationFilter();
        
        memoryView.setData(snapshot, filter);
        
        forceRefresh = false;
    }
    
    private void resetData() {
        memoryView.resetData();
    }
    
    
    @ServiceProvider(service=MemoryCCTProvider.Listener.class)
    public static final class CCTHandler implements MemoryCCTProvider.Listener {

        private final List<LiveMemoryViewUpdater> updaters = new ArrayList<>();
        
        
        public static CCTHandler registerUpdater(LiveMemoryViewUpdater updater) {
            CCTHandler handler = Lookup.getDefault().lookup(CCTHandler.class);
            handler.updaters.add(updater);
            return handler;
        }
        
        public void unregisterUpdater(LiveMemoryViewUpdater updater) {
            updaters.remove(updater);
        }
        
        
        public void cctEstablished(RuntimeCCTNode appRootNode, boolean empty) {
            if (!empty) {
                for (LiveMemoryViewUpdater updater : updaters) try {
                    updater.updateData();
                } catch (ClientUtils.TargetAppOrVMTerminated ex) {
//                } catch (CPUResultsSnapshot.NoDataAvailableException ex) {
                    Logger.getLogger(LiveMemoryView.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }

        public void cctReset() {
            for (LiveMemoryViewUpdater updater : updaters) updater.resetData();
        }
    }
    
}
