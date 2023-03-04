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

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.tests.j2eeserver.devmodule.TestJ2eeModuleImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sherold
 */
public class ModuleConfigurationTest extends NbTestCase {

    private J2eeModule j2eeModule;
    private TestJ2eeModuleImpl j2eeModuleImpl;
    
    /** Creates a new instance of J2eeModuleTest */
    public ModuleConfigurationTest(String testName) {
        super(testName);
    }
        
    @Override
    protected void setUp() throws Exception {
        File dataDir = getDataDir();
        File rootFolder = new File(getDataDir(), "/sampleweb");
        FileObject samplewebRoot = FileUtil.toFileObject(rootFolder);
        j2eeModuleImpl = new TestJ2eeModuleImpl(samplewebRoot);
        j2eeModule = J2eeModuleFactory.createJ2eeModule(j2eeModuleImpl);
    }
    
    public void testCreateJ2eeModule() throws Exception {
        ModuleConfigurationImpl conf = ModuleConfigurationImpl.create(j2eeModule);
        ContextRootConfiguration contextRootConfiguration = conf.getLookup().lookup(ContextRootConfiguration.class);
        String contextRoot = "mycontext";
        contextRootConfiguration.setContextRoot(contextRoot);
        assertEquals(contextRoot, contextRootConfiguration.getContextRoot());
    }
    
    private static class ModuleConfigurationImpl implements ModuleConfiguration, ContextRootConfiguration, PropertyChangeListener {
        
        private final J2eeModule j2eeModule;
        private String context;
        
        private ModuleConfigurationImpl(J2eeModule j2eeModule) {
            this.j2eeModule = j2eeModule;
        }
        
        public static ModuleConfigurationImpl create(J2eeModule j2eeModule) {
            ModuleConfigurationImpl moduleConfigurationImpl = new ModuleConfigurationImpl(j2eeModule);
            j2eeModule.addPropertyChangeListener(moduleConfigurationImpl);
            return moduleConfigurationImpl;
        }
        
        public J2eeModule getJ2eeModule() {
            return j2eeModule;
        }

        public void dispose() {
            j2eeModule.removePropertyChangeListener(this);
        }

        public Lookup getLookup() {
            return Lookups.fixed(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
        }

        public String getContextRoot() throws ConfigurationException {
            return context;
        }

        public void setContextRoot(String contextRoot) throws ConfigurationException {
            context = contextRoot;
        }
    }
    
    
}
