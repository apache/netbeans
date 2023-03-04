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

package org.netbeans.lib.profiler.results.cpu;

import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.CCTFlattener;

//import org.netbeans.lib.profiler.results.cpu.cct.NodeMarker;
import java.util.logging.Logger;
import org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor;
import org.netbeans.lib.profiler.results.cpu.cct.CCTResultsFilter;
import org.netbeans.lib.profiler.results.cpu.cct.TimeCollector;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.SimpleCPUCCTNode;


/**
 *
 * @author Jaroslav Bachorik
 */
public class FlatProfileBuilder implements FlatProfileProvider, CPUCCTProvider.Listener {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final Logger LOGGER = Logger.getLogger(FlatProfileBuilder.class.getName());

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private CCTFlattener flattener;
    private FlatProfileContainer lastFlatProfile = null;
    private ProfilerClient client;
    private SimpleCPUCCTNode appNode;

    private TimeCollector collector = null;
    private CCTResultsFilter filter = null;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setContext(ProfilerClient client, TimeCollector collector, CCTResultsFilter filter) {
        if (this.client != null) {
            this.collector = null;
            this.filter = null;
            this.client.registerFlatProfileProvider(null);
        }

        if (client != null) {
            this.collector = collector;
            this.filter = filter;
            flattener = new CCTFlattener(client, filter);
            client.registerFlatProfileProvider(this);
        } else {
            flattener = null;
        }

        this.client = client;
        appNode = null;
    }

    public synchronized void cctEstablished(RuntimeCCTNode appRootNode, boolean empty) {
        if (empty) return;
        
        if (appRootNode instanceof SimpleCPUCCTNode) {
            appNode = (SimpleCPUCCTNode) appRootNode;
        } else {
            appNode = null;
        }
    }

    public synchronized void cctReset() {
        appNode = null;
    }

    public synchronized FlatProfileContainer createFlatProfile() {
        if (appNode == null) {
            return null;
        }

        client.getStatus().beginTrans(false);

        try {
            RuntimeCCTNodeProcessor.process(
                appNode, 
                filter,
                flattener,
                collector
            );
            
            lastFlatProfile = flattener.getFlatProfile();

        } finally {
            client.getStatus().endTrans();
        }

        return lastFlatProfile;
    }
}
