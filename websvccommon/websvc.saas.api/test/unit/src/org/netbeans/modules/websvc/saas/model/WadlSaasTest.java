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

package org.netbeans.modules.websvc.saas.model;

import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.saas.util.SetupUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author nam
 */
public class WadlSaasTest extends NbTestCase {
    SaasServicesModel model;
    
    public WadlSaasTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SetupUtil.commonSetUp(super.getWorkDir());
        model = SaasServicesModel.getInstance();
    }

    @Override
    protected void tearDown() throws Exception {
        SetupUtil.commonTearDown();
        super.tearDown();
    }

    public void testGetLocalSchemaFiles() throws Exception {
        SaasGroup zillow = model.getTopGroup("Zillow");
        
        //Could not do this due to ant.bridge requires SystemFileSystem which 
        // is not available in unit test environment
        //model.toStateReady(true);
        
        WadlSaas res = (WadlSaas) zillow.getChildService("Real Estate Service");
        res.getWadlModel();
        List<FileObject> schemas = res.getLocalSchemaFiles();
        assertEquals(8, schemas.size());
        assertEquals("Chart.xsd", schemas.get(0).getNameExt());
    }

}
