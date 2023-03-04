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
package org.netbeans.modules.netbinox;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.MockEvents;
import org.netbeans.MockModuleInstaller;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/** Simulates deadlock #218050 when 
 * osgi.classloader.singleThreadLoads=true
 * is used.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class SingleThreadLoadsDeadlockTest extends NetigsoHid {
    private File l2;
    private File bottom;
    private File middle;
    private File top;
    private File l3;

    public SingleThreadLoadsDeadlockTest(String name) {
        super(name);
    }
    
    protected Boolean singleThreadLoads() {
        return true;
    }
    
    public static Test suite() {
        return new NbTestSuite();
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 10000;
    }
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("osgi.classloader.singleThreadLoads", singleThreadLoads().toString());
        
        
        super.setUp();
        l2 = createTestJAR("loading2", null, simpleModule);
        l3 = createTestJAR("loading3", null, simpleModule, l2);
        
        String mfBottom = "Bundle-SymbolicName: org.bottom\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Export-Package: org.foo\n" +
                "\n\n";
        
        bottom = changeManifest(getWorkDir(), "bottom.jar", simpleModule, mfBottom);

        String mfMiddle = 
            "OpenIDE-Module: org.middle\n"
           + "OpenIDE-Module-Public-Packages: org.load2.*\n"
           + "OpenIDE-Module-Module-Dependencies: org.bottom\n"
           + "\n"
           + "\n";
        middle = changeManifest(getWorkDir(), "middle.jar", l2, mfMiddle);
        
        String mfTop = "Bundle-SymbolicName: org.top\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Require-Bundle: org.middle\n" + 
                "Export-Package: org.load3\n" +
                "\n\n";
        
        top = changeManifest(getWorkDir(), "top.jar", l3, mfTop);
    }
    
    

    public void testOSGiCanRequireBundleOnNetBeans() throws Throwable {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        
        
        mgr.mutexPrivileged().enterWriteAccess();
        HashSet<Module> all = null;
        try {
            Module mBottom = mgr.create(bottom, null, false, false, false);
            Module mMiddle = mgr.create(middle, null, false, false, false);
            final Module mTop = mgr.create(top, null, false, false, false);
            
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(mBottom, mMiddle, mTop));
            mgr.enable(b);
            all = b;
            
            String order = 
                "THREAD: Load3 MSG: Loaded.*org/load3/Load.class\n"
              + "THREAD: Test Watch Dog: testOSGiCanRequireBundleOnNetBeans MSG: Loading 2\n";
            
            Logger messages = Logger.getLogger("org.netbeans.modules.netbinox");
            Log.controlFlow(messages, LOG, order, 500);
            
            final Throwable[] exArr = { null };
            final Class[] classTop = { null };
            RequestProcessor RP = new RequestProcessor("Load3");
            
            Task task = RP.post(new Runnable() {
                     @Override
                     public void run() {
                         LOG.info("Loading 3");
                         try {
                             classTop[0] = Class.forName("org.load3.Load", true, mTop.getClassLoader());
                         } catch (Throwable ex) {
                             exArr[0] = ex;
                             LOG.info("ClassNotFoundException while loading 3");
                         }
                         LOG.info("Loading 3 done");
                     }
                 });

            Thread.yield();
            
            LOG.info("Loading 2");
            Class<?> classMiddle = Class.forName("org.load2.Load", true, mMiddle.getClassLoader());
            LOG.info("Loading 2 is done");
            
            task.waitFinished();
            if (exArr[0] != null) {
                throw exArr[0];
            }
            assertNotNull("All classes loaded", classTop[0]);
        } finally {
            if (all != null) {
                mgr.disable(all);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    private static final Logger LOG = Logger.getLogger("test." + SingleThreadLoadsDeadlockTest.class.getName());
}
