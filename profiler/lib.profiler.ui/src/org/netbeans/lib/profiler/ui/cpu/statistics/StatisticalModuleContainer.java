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

package org.netbeans.lib.profiler.ui.cpu.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import org.netbeans.lib.profiler.results.cpu.CPUCCTProvider;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Jaroslav Bachorik
 */
//@ServiceProvider(service=CPUCCTProvider.Listener.class)
public class StatisticalModuleContainer implements CPUCCTProvider.Listener {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final Set<StatisticalModule> modules = new HashSet<>();

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of StatisticalModuleContainer */
    public StatisticalModuleContainer() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Collection getAllModules() {
        return new ArrayList(modules);
    }

    public void addModule(StatisticalModule module) {
        modules.add(module);
    }

    public void cctEstablished(RuntimeCCTNode appNode, boolean empty) {
        if (empty) {
            return;
        }

        if (!(appNode instanceof RuntimeCPUCCTNode)) {
            return;
        }

        Set tmpModules;

        synchronized (modules) {
            if (modules.isEmpty()) {
                return;
            }

            tmpModules = new HashSet(modules);
        }

        for (Iterator iter = tmpModules.iterator(); iter.hasNext();) {
            ((StatisticalModule) iter.next()).refresh((RuntimeCPUCCTNode) appNode);
        }
    }

    public void cctReset() {
        cctEstablished(null, false);
    }

    public void removeAllModules() {
        modules.clear();
    }

    public void removeModule(StatisticalModule module) {
        modules.remove(module);
    }
}
