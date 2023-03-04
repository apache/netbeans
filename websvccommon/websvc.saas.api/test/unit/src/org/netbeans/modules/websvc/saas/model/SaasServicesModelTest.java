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
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.util.SetupUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import java.io.OutputStream;
import org.netbeans.modules.websvc.saas.util.SaasUtil;

/**
 *
 * @author nam
 */
public class SaasServicesModelTest extends NbTestCase {
    
    public static void resetSaasServicesModel() {
        SaasServicesModel.getInstance().reset();
    }
    
    public static void setWsdlData(WsdlSaas saas, WsdlData data) {
        saas.setWsdlData(data);
    }
    
    public SaasServicesModelTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        SetupUtil.commonSetUp(super.getWorkDir());
    }

    @Override
    protected void tearDown() throws Exception {
        SetupUtil.commonTearDown();
    }

    public void testLoading() throws Exception {

        SaasServicesModel instance = SaasServicesModel.getInstance();
        assertEquals("Zillow", instance.getGroups().get(1).getName());
        //No Sub-group for now
        //SaasGroup group = instance.getGroups().get(0).getChildGroup("Videos");
        //assertNotNull(group);
        SaasGroup group = instance.getGroups().get(1);
        WadlSaas service = (WadlSaas) group.getServices().get(0);
        assertEquals("Real Estate Service", service.getDisplayName());
        assertNotNull(service.getWadlModel());

        SetupUtil.commonTearDown();
    }
    
    public void testAddGroup() throws Exception {

        SaasServicesModel instance = SaasServicesModel.getInstance();
        instance.createGroup(instance.getRootGroup(), "groupA");
        SaasGroup added = instance.getRootGroup().getChildGroup("groupA");
        assertEquals("groupA", added.getName());
        instance.createGroup(added, "child1");
        SaasGroup child2 = instance.createGroup(added, "child2");
        instance.createGroup(child2, "grandChild");
        
        instance.reset();
        instance.initRootGroup();
        
        SaasGroup reloaded = instance.getRootGroup().getChildGroup("groupA");
        assertEquals("groupA", reloaded.getName());
        assertEquals(2, reloaded.getChildrenGroups().size());
        assertEquals("child1", reloaded.getChildGroup("child1").getName());
        assertEquals("grandChild", reloaded.getChildGroup("child2").getChildGroup("grandChild").getName());

        SetupUtil.commonTearDown();
    }

    public void testAddToPrePackagedGroup() throws Exception {

        System.out.println(getWorkDirPath());
        SaasServicesModel instance = SaasServicesModel.getInstance();
        SaasGroup delicious = instance.getTopGroup("Delicious");
        assertFalse(delicious.isUserDefined());
        assertNotNull(delicious.getChildService("Bookmarking Service"));
        instance.createGroup(delicious, "myDelicious");
        assertTrue(delicious.getChildGroup("myDelicious").isUserDefined());
        assertNotNull(delicious.getChildService("Bookmarking Service"));
        
        instance.reset();
        instance.initRootGroup();
        
        delicious = instance.getTopGroup("Delicious");
        assertFalse(delicious.isUserDefined());
        assertNotNull(delicious.getChildGroup("myDelicious"));
        assertTrue(delicious.getChildGroup("myDelicious").isUserDefined());
        assertNotNull(delicious.getChildService("Bookmarking Service"));

    }

    private FileObject setupLocalWadl() throws Exception {
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject wadl = workDir.getFileObject("application.wadl.xml");
        if (wadl == null) {
            wadl = workDir.createData("application.wadl.xml");
        }
        OutputStream out = wadl.getOutputStream();
        try {
            FileUtil.copy(getClass().getResourceAsStream("application.wadl"), out);
        } finally {
            out.close();
        }
        return wadl;
    }

    public void testAddWadlService() throws Exception {
        FileObject wadl = setupLocalWadl();
        
        String url = wadl.getURL().toExternalForm();
        SaasServicesModel instance = SaasServicesModel.getInstance();
        SaasGroup delicious = instance.getTopGroup("Delicious");
        SaasGroup myDelicious = instance.createGroup(delicious, "myDelicious");
        WadlSaas saas = (WadlSaas) instance.createSaasService(myDelicious, url, null);
        assertEquals(1, saas.getResources().size());
    }

    public void testRemoveGroupWithWadlService() throws Exception {
        FileObject wadl = setupLocalWadl();
        
        String url = wadl.getURL().toExternalForm();
        SaasServicesModel instance = SaasServicesModel.getInstance();
        SaasGroup delicious = instance.getTopGroup("Delicious");
        SaasGroup myDelicious = instance.createGroup(delicious, "myDelicious");
        instance.createSaasService(myDelicious, url, null);
        
        instance.reset();
        
        SaasGroup g = instance.getTopGroup("Delicious").getChildGroup("myDelicious");
        WadlSaas saas = (WadlSaas)g.getServices().get(0);
        assertEquals(1, saas.getResources().get(0).getMethods().size());
        assertNotNull(instance.getWebServiceHome().getFileObject(SaasUtil.toValidJavaName(saas.getDisplayName())));
        
        instance.removeGroup(g);
        assertNull(instance.getWebServiceHome().getFileObject(SaasUtil.toValidJavaName(saas.getDisplayName())));
        assertNull(instance.getTopGroup("Delicious").getChildGroup("myDelicious"));
    }
}
