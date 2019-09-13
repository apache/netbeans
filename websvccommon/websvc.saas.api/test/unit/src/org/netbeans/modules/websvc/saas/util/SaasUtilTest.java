/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.websvc.saas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.model.wadl.Application;
import org.netbeans.modules.websvc.saas.model.wadl.Method;
import org.netbeans.modules.websvc.saas.model.wadl.Representation;
import org.netbeans.modules.websvc.saas.model.wadl.Response;

/**
 *
 * @author nam
 */
public class SaasUtilTest extends NbTestCase {
    
    public SaasUtilTest(String testName) {
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

    public void testLoadSaasGroup() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("rootGroup.xml");
        verifyRootGroup(in);
    }
    
    private void verifyRootGroup(InputStream in) throws Exception {
        SaasGroup result = SaasUtil.loadSaasGroup(in);
        assertEquals("Web Services", result.getName());
        SaasGroup test1 = result.getChildrenGroups().get(0);
        assertEquals("test1", test1.getName());
        assertSame(result, test1.getParent());
        assertEquals(0, test1.getChildrenGroups().size());
        
        SaasGroup test2 = result.getChildrenGroups().get(1);
        assertEquals("test2", test2.getName());
        assertEquals(2, test2.getChildrenGroups().size());
        assertEquals("test2.1", test2.getChildrenGroups().get(0).getName());
        assertEquals("test2.2", test2.getChildrenGroups().get(1).getName());
        assertSame(result, test2.getParent());
        assertEquals(2, test2.getChildrenGroups().size());
        assertEquals("test3", result.getChildrenGroups().get(2).getName());
    }

    public void testXpath() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("testwadl.xml");
        Application app = SaasUtil.loadJaxbObject(in, Application.class);
        assertNotNull(SaasUtil.wadlMethodFromXPath(app, "//resource[1]/method[1]"));
        assertNotNull(SaasUtil.wadlMethodFromXPath(app, "/application//resource[1]/method[2]"));
        assertNotNull(SaasUtil.wadlMethodFromXPath(app, "//resource[1]/method[3]"));
        assertNull(SaasUtil.wadlMethodFromXPath(app, "//resource[2]/method[1]"));
        assertNull(SaasUtil.wadlMethodFromXPath(app, "//resource[1]/method[1]/method[3]"));
    }
    
    public void testMediaTypes() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("testwadl.xml");
        Application app = SaasUtil.loadJaxbObject(in, Application.class);
        Method method = SaasUtil.wadlMethodFromXPath(app, "//resource[1]/method[1]");
        List<Response> resposes = method.getResponse();
        assertEquals(1, resposes.size());
        
        List<Representation> representation = resposes.get(0).getRepresentation();
        assertEquals(1, representation.size());

        assertEquals(1, SaasUtil.getMediaTypes(representation).size());
    }
    
    public void testSaveSaasGroup() throws Exception {
        File output = new File(getWorkDir(), "testSaveSaasGroup");
        InputStream in = this.getClass().getResourceAsStream("rootGroup.xml");
        SaasGroup rootGroup = SaasUtil.loadSaasGroup(in);
        SaasGroup test2Group = rootGroup.getChildrenGroups().get(1);
        
        SaasServices newSaasServices = new SaasServices();
        newSaasServices.setDisplayName("Test2Service1");
        Saas newSaas = new Saas(test2Group, newSaasServices);
        test2Group.addService(newSaas);
        
        SaasUtil.saveSaasGroup(rootGroup, output);
                
        verifyRootGroup(new FileInputStream(output));
        output.delete();
    }

    public void testSaasMetaData() throws Exception {
        SetupUtil.commonSetUp(super.getWorkDir());

        InputStream in = this.getClass().getResourceAsStream("FakeSaas.xml");
        SaasServices service = SaasUtil.loadJaxbObject(in, SaasServices.class);
        SaasMetadata metadata = service.getSaasMetadata();
        assertEquals("FakeSaas", metadata.getGroup().getName());
        assertEquals("org.netbeans.modules.websvc.saas.services.fakesaas.Bundle", metadata.getLocalizingBundle());
        assertEquals("zws-id", metadata.getAuthentication().getApiKey().getId());
        
        SetupUtil.commonTearDown();
    }

    public void testSaasServices() throws Exception {
        SetupUtil.commonSetUp(super.getWorkDir());

        InputStream in = this.getClass().getResourceAsStream("FakeSaas.xml");
        SaasServices ss = SaasUtil.loadSaasServices(in);
        assertEquals("Fake Saas Service", ss.getDisplayName());
        
        assertNotNull(ss.getSaasMetadata());
        assertEquals("Videos", ss.getSaasMetadata().getGroup().getGroup().get(0).getName());

        SetupUtil.commonTearDown();
    }
    
    public void testGetSaasDirName() throws Exception {
        SetupUtil.commonSetUp(super.getWorkDir());

        String [] urls = {
            "http://localhost:8080/WebApplication8/resources/application.wadl",
            "file://home/export/nam/mpProjectA/src/resources/BestApplication.wadl",
            "file://c:\\ProjectB\\WorstApplication.wadl",
        };
        
        assertEquals("WebApplication8", SaasUtil.getWadlServiceDirName(urls[0]));
        assertEquals("BestApplication", SaasUtil.getWadlServiceDirName(urls[1]));
        assertEquals("WorstApplication", SaasUtil.getWadlServiceDirName(urls[2]));
    
        SetupUtil.commonTearDown();
    }
    
    public void testEnsureUniqueServiceDirName() throws Exception {
        SetupUtil.commonSetUp(super.getWorkDir());

        assertEquals("application", SaasUtil.ensureUniqueServiceDirName("application"));
        assertNotNull(SaasServicesModel.getWebServiceHome().getFileObject("application"));
        assertEquals("application1", SaasUtil.ensureUniqueServiceDirName("application"));
        assertEquals("application2", SaasUtil.ensureUniqueServiceDirName("application"));
        assertEquals("application3", SaasUtil.ensureUniqueServiceDirName("application"));

        SetupUtil.commonTearDown();
    }
}
