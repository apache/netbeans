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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.tests.j2eeserver.devmodule.TestJ2eeModuleImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sherold
 */
public class J2eeModuleTest extends NbTestCase {

    private J2eeModule j2eeModule;
    private TestJ2eeModuleImpl j2eeModuleImpl;
    
    /** Creates a new instance of J2eeModuleTest */
    public J2eeModuleTest(String testName) {
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
    
    public void testCreateJ2eeModule() {
        assertNotNull(j2eeModule);
        assertNotNull(j2eeModuleImpl);
    }
    
    public void testPropertyChangeListener() {
        final Set propChanged = new HashSet();
        PropertyChangeListener p = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                propChanged.add(evt.getPropertyName());
            }
        };
        // check that event comes
        j2eeModule.addPropertyChangeListener(p);
        j2eeModuleImpl.firePropertyChange(J2eeModule.PROP_MODULE_VERSION, null, null);
        assertTrue(propChanged.contains(J2eeModule.PROP_MODULE_VERSION));
        // check that event does not come
        j2eeModule.removePropertyChangeListener(p);
        j2eeModuleImpl.firePropertyChange(J2eeModule.PROP_RESOURCE_DIRECTORY, null, null);
        assertFalse(propChanged.contains(J2eeModule.PROP_RESOURCE_DIRECTORY));
    }
    
    public void testGetDeploymentDescriptor() throws Exception {
        // check non-existing DDs
        assertNull(j2eeModule.getMetadataModel(EjbJarMetadata.class));
        
        // check existing DDs
        assertNotNull(j2eeModule.getMetadataModel(WebAppMetadata.class));
    }
}
