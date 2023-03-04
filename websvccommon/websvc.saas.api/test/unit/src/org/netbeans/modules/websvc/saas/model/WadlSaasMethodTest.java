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

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.websvc.saas.util.SetupUtil;

/**
 *
 * @author nam
 */
public class WadlSaasMethodTest extends NbTestCase {
    
    public WadlSaasMethodTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetResourcePath() throws Exception {
        SetupUtil.commonSetUp(super.getWorkDir());
        SaasServicesModel instance = SaasServicesModel.getInstance();
        SaasGroup group = instance.getRootGroup().getChildGroup("Delicious");
        WadlSaas saas = (WadlSaas) group.getServices().get(0);
        assertEquals("Bookmarking Service", saas.getDisplayName());
        WadlSaasMethod m = saas.getResources().get(0).getChildResources().get(0).getMethods().get(0);
        assertEquals("getPosts", m.getName());
        Resource[] path = m.getResourcePath();
        assertEquals("posts", path[0].getPath());
        assertEquals("get", path[1].getPath());

        SetupUtil.commonTearDown();
    }

    public void testGetResourcePathOfFilteredMethod() throws Exception {
        SetupUtil.commonSetUp(super.getWorkDir());
        
        SaasServicesModel instance = SaasServicesModel.getInstance();
        SaasGroup group = instance.getRootGroup().getChildGroup("Zillow");
        WadlSaas saas = (WadlSaas) group.getServices().get(0);
        assertEquals("Real Estate Service", saas.getDisplayName());
        /* These wadl methods are no longer filter.
        WadlSaasMethod m = (WadlSaasMethod) saas.getMethods().get(2);
        assertEquals("ListPopular", m.getName());
        Resource[] path = m.getResourcePath();
        assertEquals(1, path.length);
        assertEquals("api2_rest", path[0].getPath());
        */
        SetupUtil.commonTearDown();
    }
}
