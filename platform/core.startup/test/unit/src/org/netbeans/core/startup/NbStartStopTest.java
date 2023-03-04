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
package org.netbeans.core.startup;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Task;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NbStartStopTest extends NbTestCase{
    private InstanceContent stop;
    private InstanceContent start;
    private NbStartStop onStartStop;
    
    public NbStartStopTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        start = new InstanceContent();
        stop = new InstanceContent();
        onStartStop = new NbStartStop(
            new AbstractLookup(start),
            new AbstractLookup(stop)
        );
    }
    
    public void testStartIsInvokedDuringInit() {
        final boolean[] ok = { false };
        start.add(new Runnable() {
            @Override public void run() {
                ok[0] = true;
            }
        });
        
        onStartStop.initialize();
        onStartStop.waitOnStart();
        
        assertTrue("Initialized", ok[0]);
    }

    public void testStartIsInvokedWhenModuleIsAdded() {
        final boolean[] ok = { false };
        
        onStartStop.initialize();
        onStartStop.waitOnStart();
        start.add(new Runnable() {
            @Override public void run() {
                ok[0] = true;
            }
        });
        onStartStop.waitOnStart();
        
        assertTrue("Initialized", ok[0]);
    }

    public void testStopping() {
        final boolean[] ok = { false };
        
        onStartStop.initialize();
        onStartStop.waitOnStart();
        final Callable<Boolean> run = new Callable<Boolean>() {
            @Override public Boolean call() {
                return ok[0] = true;
            }
        };
        stop.add(run);
        List<ModuleInfo> modules = Collections.singletonList(Modules.getDefault().ownerOf(run.getClass()));
        assertTrue("Close approved", onStartStop.closing(modules));
        
        assertTrue("Initialized", ok[0]);
    }

    public void testStoppingFalse() {
        final boolean[] ok = { false };
        
        onStartStop.initialize();
        onStartStop.waitOnStart();
        final Callable<Boolean> run = new Callable<Boolean>() {
            @Override public Boolean call() {
                ok[0] = true;
                return false;
            }
        };
        stop.add(run);
        List<ModuleInfo> modules = Collections.singletonList(Modules.getDefault().ownerOf(run.getClass()));
        assertFalse("Close rejected", onStartStop.closing(modules));
        
        assertTrue("Initialized", ok[0]);
    }

    public void testStop() {
        final boolean[] ok = { false };
        
        onStartStop.initialize();
        onStartStop.waitOnStart();
        final Runnable run = new Runnable() {
            @Override public void run() {
                ok[0] = true;
            }
        };
        stop.add(run);
        List<ModuleInfo> modules = Collections.singletonList(Modules.getDefault().ownerOf(run.getClass()));
        for (Task t : onStartStop.startClose(modules)) {
            t.waitFinished();
        }
        
        assertTrue("Initialized", ok[0]);
    }
}
