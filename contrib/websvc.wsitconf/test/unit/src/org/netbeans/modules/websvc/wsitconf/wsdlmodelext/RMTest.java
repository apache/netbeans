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
import junit.framework.*;
import org.netbeans.modules.websvc.wsitconf.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitconf.util.TestUtil;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class RMTest extends TestCase {
    
    public RMTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception { }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testRM() throws Exception {
        
        File f = new File("RMTest.wsdl");
        if (f.exists()) {
            f.delete();
        }

        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");

        for (ConfigVersion cfgV : ConfigVersion.values()) {

            model.startTransaction();

            Definitions d = model.getDefinitions();
            Binding b = (Binding) d.getBindings().toArray()[0];

            assertFalse("RM enabled indicated on empty WSDL", RMModelHelper.getInstance(cfgV).isRMEnabled(b));

            RMModelHelper.getInstance(cfgV).enableRM(b, true);
            assertTrue("RM not enabled correctly", RMModelHelper.getInstance(cfgV).isRMEnabled(b));
            assertTrue("Addressing not enabled for RM", AddressingModelHelper.isAddressingEnabled(b));
            
            assertNull("Inactivity timeout set even when not specified", RMModelHelper.getInstance(cfgV).getInactivityTimeout(b));
            RMModelHelper.getInstance(cfgV).setInactivityTimeout(b, "112233");
            assertEquals("Inactivity Timeout Value Not Saved/Read Correctly", "112233", RMModelHelper.getInstance(cfgV).getInactivityTimeout(b));

            assertFalse("Flow Control enabled indicated", RMModelHelper.isFlowControl(b));
            RMModelHelper.getInstance(cfgV).enableFlowControl(b, true);
            RMModelHelper.getInstance(cfgV).enableFlowControl(b, false);
            RMModelHelper.getInstance(cfgV).enableFlowControl(b, true);
            assertTrue("Flow Control disabled indicated", RMModelHelper.isFlowControl(b));

            assertNull("Max Receive Buffer Size set even when not specified", RMModelHelper.getMaxReceiveBufferSize(b));
            RMModelHelper.setMaxReceiveBufferSize(b, "2233");
            assertEquals("Max Receive Buffer Size Value Not Saved/Read Correctly", "2233", RMModelHelper.getMaxReceiveBufferSize(b));

            assertFalse("Ordered enabled indicated", RMModelHelper.getInstance(cfgV).isOrderedEnabled(b));
            RMModelHelper.getInstance(cfgV).enableOrdered(b, true);
            assertTrue("Ordered disabled indicated", RMModelHelper.getInstance(cfgV).isOrderedEnabled(b));
            RMModelHelper.getInstance(cfgV).enableOrdered(b, false);
            assertFalse("Ordered enabled indicated", RMModelHelper.getInstance(cfgV).isOrderedEnabled(b));

            RMModelHelper.getInstance(cfgV).enableRM(b, false);
            assertFalse("RM not disabled correctly", RMModelHelper.getInstance(cfgV).isRMEnabled(b));
            assertNull("RM not disabled correctly", RMModelHelper.getInstance(cfgV).getInactivityTimeout(b));

            model.endTransaction();
        }

        TestUtil.dumpToFile(model.getBaseDocument(), f);
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}
