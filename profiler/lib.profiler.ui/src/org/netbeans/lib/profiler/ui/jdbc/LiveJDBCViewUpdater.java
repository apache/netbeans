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

package org.netbeans.lib.profiler.ui.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider;
import org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri
 */
public class LiveJDBCViewUpdater {
    
    private static final int MIN_UPDATE_DIFF = 900;
    private static final int MAX_UPDATE_DIFF = 1400;
    
    
    private CCTHandler handler;
    
    private final LiveJDBCView jdbcView;
    private final ProfilerClient client;
    
    private volatile boolean paused;
    private volatile boolean forceRefresh;
    
    
    
    public LiveJDBCViewUpdater(LiveJDBCView jdbcView, ProfilerClient client) {
        this.jdbcView = jdbcView;
        this.client = client;
    }
    
    
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }
    
    public void update() throws ClientUtils.TargetAppOrVMTerminated {
        if (handler == null) handler = CCTHandler.registerUpdater(this);
        
        if (forceRefresh || (!paused && jdbcView.getLastUpdate() + MAX_UPDATE_DIFF < System.currentTimeMillis()))
            client.forceObtainedResultsDump(true);
    }
    
    public void cleanup() {
        if (handler != null) handler.unregisterUpdater(this);
        handler = null;
    }
    
    
    private void updateData() throws ClientUtils.TargetAppOrVMTerminated {
        if (!forceRefresh && (paused || jdbcView.getLastUpdate() + MIN_UPDATE_DIFF > System.currentTimeMillis())) return;
        
        JdbcResultsSnapshot data = client.getStatus().getInstrMethodClasses() == null ?
                            null : client.getJdbcProfilingResultsSnapshot(false);
        jdbcView.setData(data);
        
        forceRefresh = false;
    }
    
    private void resetData() {
        jdbcView.resetData();
    }
    
    
    @ServiceProvider(service=JdbcCCTProvider.Listener.class)
    public static class CCTHandler implements JdbcCCTProvider.Listener {
        
        private final List<LiveJDBCViewUpdater> updaters = new ArrayList<>();
        
        
        public static CCTHandler registerUpdater(LiveJDBCViewUpdater updater) {
            CCTHandler handler = Lookup.getDefault().lookup(CCTHandler.class);
            
            if (handler.updaters.isEmpty()) {
                Collection<? extends JdbcCCTProvider> jdbcCCTProviders = Lookup.getDefault().lookupAll(JdbcCCTProvider.class);
                assert !jdbcCCTProviders.isEmpty();
                for (JdbcCCTProvider provider : jdbcCCTProviders) provider.addListener(handler);
            }
            
            handler.updaters.add(updater);
            return handler;
        }
        
        public void unregisterUpdater(LiveJDBCViewUpdater updater) {
            updaters.remove(updater);
            
            if (updaters.isEmpty()) {
                Collection<? extends JdbcCCTProvider> jdbcCCTProviders = Lookup.getDefault().lookupAll(JdbcCCTProvider.class);
                assert !jdbcCCTProviders.isEmpty();
                for (JdbcCCTProvider provider : jdbcCCTProviders) provider.removeListener(this);
            }
        }
        

        public final void cctEstablished(RuntimeCCTNode appRootNode, boolean empty) {
           if (!empty) {
                for (LiveJDBCViewUpdater updater : updaters) try {
                    updater.updateData();
                } catch (ClientUtils.TargetAppOrVMTerminated ex) {
                    Logger.getLogger(LiveJDBCView.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }

        public final void cctReset() {
            for (LiveJDBCViewUpdater updater : updaters) updater.resetData();
        }

    }
    
}
