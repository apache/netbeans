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

package org.netbeans.lib.profiler.ui.cpu;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import org.netbeans.lib.profiler.results.cpu.CPUCCTProvider;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
public class LiveCPUViewUpdater {
    
    private static final int MIN_UPDATE_DIFF = 900;
    private static final int MAX_UPDATE_DIFF = 1400;
    
    
    private CCTHandler handler;
    
    private final LiveCPUView cpuView;
    private final ProfilerClient client;
    
    private volatile boolean paused;
    private volatile boolean forceRefresh;
    
    
    
    public LiveCPUViewUpdater(LiveCPUView cpuView, ProfilerClient client) {
        this.cpuView = cpuView;
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
        if (forceRefresh || (!paused && cpuView.getLastUpdate() + MAX_UPDATE_DIFF < System.currentTimeMillis()))
            client.forceObtainedResultsDump(true);
    }
    
    public void cleanup() {
        handler.unregisterUpdater(this);
        handler = null;
    }
    
    
    private void updateData() throws ClientUtils.TargetAppOrVMTerminated, CPUResultsSnapshot.NoDataAvailableException {
        if (!forceRefresh && (paused || cpuView.getLastUpdate() + MIN_UPDATE_DIFF > System.currentTimeMillis())) return;
        
        boolean sampling = client.getCurrentInstrType() == ProfilerClient.INSTR_NONE_SAMPLING;
        CPUResultsSnapshot data = client.getStatus().getInstrMethodClasses() == null ?
                           null : client.getCPUProfilingResultsSnapshot(false);
        cpuView.setData(data, sampling);
        
        forceRefresh = false;
    }
    
    private void resetData() {
        cpuView.resetData();
    }
    
    
    @ServiceProvider(service=CPUCCTProvider.Listener.class)
    public static class CCTHandler implements CPUCCTProvider.Listener {
        
        private final List<LiveCPUViewUpdater> updaters = new ArrayList<>();
        
        
        public static CCTHandler registerUpdater(LiveCPUViewUpdater updater) {
            CCTHandler handler = Lookup.getDefault().lookup(CCTHandler.class);
            handler.updaters.add(updater);
            return handler;
        }
        
        public void unregisterUpdater(LiveCPUViewUpdater updater) {
            updaters.remove(updater);
        }
        

        public final void cctEstablished(RuntimeCCTNode appRootNode, boolean empty) {
            if (!empty) {
                for (LiveCPUViewUpdater updater : updaters) try {
                    updater.updateData();
                } catch (ClientUtils.TargetAppOrVMTerminated ex) {
                } catch (CPUResultsSnapshot.NoDataAvailableException ex) {
                    Logger.getLogger(LiveCPUView.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }

        public final void cctReset() {
            for (LiveCPUViewUpdater updater : updaters) updater.resetData();
        }

    }
    
}
