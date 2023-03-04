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

package org.netbeans.modules.apisupport.project.universe;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.Log;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author Richard Michalsky
 */
public class Issue167725DeadlockTest extends TestBase {

    public Issue167725DeadlockTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 20000;
    }
    
    // XXX cannot be run in binary dist, requires sources; test against fake platform
    public void testConcurrentScanDeadlock() throws Exception {
        final Logger LOG = Logger.getLogger("org.netbeans.modules.apisupport.project.universe.ModuleList");
        Logger observer = Logger.getLogger("observer");
        Log.enable("org.netbeans.modules.apisupport.project.universe.ModuleList", Level.ALL);
        
        String mt = "THREAD: Test Watch Dog: testConcurrentScanDeadlock MSG:";
        String wt = "THREAD: worker MSG:";
        String order = 
            mt + "beforeFindOrCreateML" +
            wt + "before PM.mutex" +
            wt + "beforeFindOrCreateML" +
            mt + "runProtected: sync 0";
        Log.controlFlow(LOG, observer, order, 0);
        Thread t = new Thread("worker") {

            @Override
            public void run() {
                try {
                    LOG.log(Level.FINE, "before PM.mutex");
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                        public Void run() throws Exception {
                            LOG.log(Level.FINE, "beforeFindOrCreateML");
                            ModuleList.findOrCreateModuleListFromNetBeansOrgSources(nbRootFile());
                            LOG.log(Level.FINE, "afterFindOrCreateML");
                            return null;
                        }
                    });
                } catch (MutexException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        t.start();
        LOG.log(Level.FINE, "beforeFindOrCreateML");
        ModuleList.findOrCreateModuleListFromNetBeansOrgSources(nbRootFile());
        LOG.log(Level.FINE, "afterFindOrCreateML");
        t.join();
    }

}
