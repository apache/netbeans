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
package org.netbeans;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ModuleManagerPersistanceTest extends NbTestCase {
    private ModuleManager mgr;
    private File sampleModule;
    
    public ModuleManagerPersistanceTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        File home = new File(getWorkDir(), "home");
        final File configModules = new File(new File(home, "config"), "Modules");
        configModules.mkdirs();
        new File(configModules, "a-b-c.xml").createNewFile();
        File moduleDir = new File(home, "modules");
        moduleDir.mkdirs();
        System.setProperty("netbeans.home", home.getPath());
        
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        Locale.setDefault(Locale.ENGLISH);
        NbBundle.setBranding("nb");
        
        Thread.sleep(100);
        
        
        Stamps.main("clear");
        sampleModule = new File(moduleDir, "m1.jar");
        mgr = createModuleManager();
        mgr.shutDown();
        Stamps.getModulesJARs().shutdown();
        assertTrue("Cache has been created", Stamps.getModulesJARs().exists("all-manifests.dat"));
        Stamps.main("init");
    }
    
    @RandomlyFails // NB-Core-Build #9913, 9915: Unstable
    public void testModuleManagerStoresIsOSGiInfo() throws Exception {
        ModuleManager snd = createModuleManager();
        assertSame("Is not OSGi, but is computed", Boolean.FALSE, snd.isOSGi(sampleModule));
    }

    private ModuleManager createModuleManager() throws Exception {
        MockModuleInstaller mi = new MockModuleInstaller();
        MockEvents me = new MockEvents();
        ModuleManager mm = new ModuleManager(mi, me);
        SetupHid.createJar(sampleModule, Collections.<String, String>emptyMap(), Collections.singletonMap("OpenIDE-Module", "m1/0"));
        Module m = mm.create(sampleModule, this, false, false, false);
        mm.enable(m);
        assertTrue("Successfully enabled", m.isEnabled());
        return mm;
    }
}
