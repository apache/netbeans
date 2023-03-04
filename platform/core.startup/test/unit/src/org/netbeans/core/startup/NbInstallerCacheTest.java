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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.Events;
import org.netbeans.MockEvents;
import org.netbeans.MockModuleInstaller;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.Stamps;
import org.netbeans.StampsTest;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NbInstallerCacheTest extends NbTestCase {

    public NbInstallerCacheTest(String name) {
        super(name);
    }
    
    public void testValuesCachedAndEmpty() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
        File conf = new File(new File(new File(getWorkDir(), "config"), "Modules"), "some-mock.xml");
        conf.getParentFile().mkdirs();
        assertTrue("File OK", conf.createNewFile());
        
        MockModuleInstaller mmi = new MockModuleInstaller();
        MockEvents me = new MockEvents();
        ModuleManager mm = new ModuleManager(mmi, me);
        MockModule m = new MockModule(mm, me);
    
        NbInstaller.Cache c = new NbInstaller.Cache();
        assertEquals("1", c.findProperty(m, "one", true));
        assertEquals("One call to module", 1, m.cnt);
        assertNull(c.findProperty(m, "null", true));
        assertEquals("Another call to module", 2, m.cnt);
        
        Stamps.getModulesJARs().flush(0);
        Stamps.getModulesJARs().shutdown();
        m.cnt = 0;
        StampsTest.reset();
        
        NbInstaller.Cache loaded = new NbInstaller.Cache();
        assertEquals("1", loaded.findProperty(m, "one", true));
        assertEquals("No call to module", 0, m.cnt);
        assertNull(loaded.findProperty(m, "null", true));
        assertEquals("Even null is cached", 0, m.cnt);
     
        assertEquals("2", loaded.findProperty(m, "two", true));
        assertEquals("Has to call to module", 1, m.cnt);
    }
    
    private static class MockModule extends Module {

        public MockModule(ModuleManager mgr, Events ev) throws IOException {
            super(mgr, ev, null, false, false, false);
        }
        @Override
        public List<File> getAllJars() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setReloadable(boolean r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void reload() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void classLoaderUp(Set<Module> parents) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void classLoaderDown() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void cleanup() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void destroy() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isFixed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private Manifest mf = new Manifest();
        {
            mf.getMainAttributes().putValue("OpenIDE-Module", "test.mock");
        }
        @Override
        public Manifest getManifest() {
            return mf;
        }

        @Override
        public Object getLocalizedAttribute(String attr) {
            cnt++;
            if ("one".equals(attr)) {
                return "1";
            }
            if ("two".equals(attr)) {
                return "2";
            }
            return null;
        }
        int cnt;
        
    }
}
