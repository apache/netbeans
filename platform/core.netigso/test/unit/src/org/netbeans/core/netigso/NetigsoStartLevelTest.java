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

package org.netbeans.core.netigso;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.ModuleSystem;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.service.startlevel.StartLevel;

/**
 * Do we correctly use start levels?
 *
 * @author Jaroslav Tulach
 */
public class NetigsoStartLevelTest extends SetupHid 
implements FrameworkListener {
    private static Module m1;
    private static ModuleManager mgr;
    private int cnt;
    private File simpleModule;
    private boolean levelChanged;

    public NetigsoStartLevelTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        simpleModule = createTestJAR("activate", null);
    }

    public void testActivation() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            m1 = mgr.createBundle(simpleModule, null, false, false, false, 10);
            mgr.enable(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }

        Class<?> main = m1.getClassLoader().loadClass("org.activate.Main");
        Object s = main.getField("start").get(null);
        assertNull("Not started yet", s);

        Framework f = NetigsoServicesTest.findFramework();
        final BundleContext fc = f.getBundleContext();
        fc.addFrameworkListener(this);
        ServiceReference sr = fc.getServiceReference(StartLevel.class.getName());
        assertNotNull("Start level service found", sr);
        StartLevel level = (StartLevel) fc.getService(sr);
        assertNotNull("Start level found", level);
        level.setStartLevel(10);
        waitLevelChanged();
            
        s = main.getField("start").get(null);
        assertNotNull("Bundle started, its context provided", s);

        mgr.mutexPrivileged().enterWriteAccess();
        try {
            mgr.disable(m1);

            Object e = main.getField("stop").get(null);
            assertNotNull("Bundle stopped, its context provided", e);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }

    @Override
    public synchronized void frameworkEvent(FrameworkEvent fe) {
        if (fe.getType() == FrameworkEvent.STARTLEVEL_CHANGED) {
            levelChanged = true;
            notifyAll();
        }
    }
    
    private synchronized void waitLevelChanged() throws InterruptedException {
        while (!levelChanged) {
            wait();
        }
    }
}
