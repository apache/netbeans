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
package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import java.io.File;
import java.util.Collection;
import junit.framework.*;
import org.netbeans.modules.websvc.wsitconf.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitconf.util.TestUtil;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyReference;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class PolicyTest extends TestCase {
    
    public PolicyTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testWithAddressing() throws Exception {
        File f = new File("PolicyTest-WithAddressing.wsdl");
        if (f.exists()) {
            f.delete();
        }

        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");
        
        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];
        
        for (ConfigVersion cfgV : ConfigVersion.values()) {
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(cfgV);
            pmh.createPolicy(b, true);
            ConfigVersion readCfgV = PolicyModelHelper.getConfigVersion(b);
            assertEquals("Set config version does not correspond to read config version", cfgV, readCfgV);
            
            Collection<BindingOperation> bindingops = b.getBindingOperations();
            for (BindingOperation bo : bindingops) {
                pmh.createPolicy(bo.getBindingInput(), false);
                pmh.createPolicy(bo.getBindingOutput(), false);
            }
            TestUtil.dumpToFile(model.getBaseDocument(), f);
            readAndCheck(model, true);
            PolicyModelHelper.cleanPolicies(b);
        }
    }

    private void readAndCheck(WSDLModel model, boolean addressing) {
        
        // the model operation is not enclosed in transaction inorder to catch 
        // whether the operations do not try to create non-existing elements
        
        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];
        
        ConfigVersion cfgVersion = PolicyModelHelper.getConfigVersion(b);
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(cfgVersion);

        All all = pmh.createPolicy(b, false);
        assertNotNull("Top Level Policy", all);
        assertEquals("Addressing does not correspond", addressing, AddressingModelHelper.isAddressingEnabled(b));
        Collection<PolicyReference> polRefs = b.getExtensibilityElements(PolicyReference.class);
        assertEquals("Top Level Policy Ref Size", 1, polRefs.size());
        assertEquals("Top Level Policy Ref URI", "#NewWebServicePortBindingPolicy", polRefs.iterator().next().getPolicyURI());
        
        Collection<BindingOperation> bindingops = b.getBindingOperations();
        for (BindingOperation bo : bindingops) {
            all = pmh.createPolicy(bo.getBindingInput(), false);
            assertNotNull("Binding Input Policy", all);
            
            all = pmh.createPolicy(bo.getBindingOutput(), false);
            assertNotNull("Binding Output Policy", all);
        }
        
    }
    
    public void testWithoutAddressing() throws Exception {
        File f = new File("PolicyTest-WithoutAddressing.wsdl");
        if (f.exists()) {
            f.delete();
        }
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");
        
        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];
        
        for (ConfigVersion cfgV : ConfigVersion.values()) {
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(cfgV);
        
            pmh.createPolicy(b, false);

            Collection<BindingOperation> bindingops = b.getBindingOperations();
            for (BindingOperation bo : bindingops) {
                pmh.createPolicy(bo.getBindingInput(), false);
                pmh.createPolicy(bo.getBindingOutput(), false);
            }
            TestUtil.dumpToFile(model.getBaseDocument(), f);
            readAndCheck(model, false);
            PolicyModelHelper.cleanPolicies(b);
        }
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}
