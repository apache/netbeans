/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.saas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.model.wadl.Application;
import org.netbeans.modules.websvc.saas.model.wadl.Method;

/**
 *
 * @author nam
 */
public class SaasUtilTest extends NbTestCase {

    File output;
    
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
        if (output != null && output.isFile()) {
            output.delete();
        }
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

    /*public void testXpath() throws Exception {
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
        List<JAXBElement<RepresentationType>> elements = method.getResponse().getRepresentationOrFault();
        //TODO none of the wadl's has response representation
        //assertEquals(1, SaasUtil.getMediaTypesFromJAXBElement(elements).size());
    }*/
    
    public void testSaveSaasGroup() throws Exception {
        output = new File(getWorkDir(), "testSaveSaasGroup");
        InputStream in = this.getClass().getResourceAsStream("rootGroup.xml");
        SaasGroup rootGroup = SaasUtil.loadSaasGroup(in);
        SaasGroup test2 = rootGroup.getChildrenGroups().get(1);
        test2.addService(new Saas(test2, new SaasServices()));
        test2.getServices().get(0).getDelegate().setDisplayName("Test2Service1");
        SaasUtil.saveSaasGroup(rootGroup, output);
        
        verifyRootGroup(new FileInputStream(output));
    }

    public void testSaasMetaData() throws Exception {
        SetupUtil.commonSetUp(super.getWorkDir());

        InputStream in = this.getClass().getResourceAsStream("/org/netbeans/modules/websvc/saas/services/zillow/resources/Zillow.xml");
        SaasServices service = SaasUtil.loadJaxbObject(in, SaasServices.class);
        SaasMetadata metadata = service.getSaasMetadata();
        assertEquals("Zillow", metadata.getGroup().getName());
        assertEquals("Zillow", metadata.getGroup().getName());
        assertEquals("org.netbeans.modules.websvc.saas.services.zillow.Bundle", metadata.getLocalizingBundle());
        //assertEquals("SaaSServices/Zillow/profile.properties", metadata.getAuthentication().getProfile());
        assertEquals("zws-id", metadata.getAuthentication().getApiKey().getId());

        SetupUtil.commonTearDown();
    }

    public void testSaasServices() throws Exception {
        SetupUtil.commonSetUp(super.getWorkDir());

        InputStream in = this.getClass().getResourceAsStream("/org/netbeans/modules/websvc/saas/services/zillow/resources/Zillow.xml");
        SaasServices ss = SaasUtil.loadSaasServices(in);
        assertEquals("Real Estate Service", ss.getDisplayName());
        
        //TODO fixme this only works if we have absolute include/href=<absolute-URI>
        assertNotNull(ss.getSaasMetadata());
        //No Sub-group for now
        //assertEquals("Videos", ss.getSaasMetadata().getGroup().getGroup().get(0).getName());

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
