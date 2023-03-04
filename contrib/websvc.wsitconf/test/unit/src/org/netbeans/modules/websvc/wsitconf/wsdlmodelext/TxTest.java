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
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitconf.util.TestUtil;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class TxTest extends TestCase {
    
    public TxTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception { }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testTx() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");
        File f = new File("TxTest.wsdl");
        if (f.exists()) {
            f.delete();
        }        
        model.startTransaction();

        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];
        
        BindingOperation bop = (BindingOperation) b.getBindingOperations().toArray()[0];
        assertEquals("Tx enabled indicated on empty WSDL", ComboConstants.TX_NOTSUPPORTED, TxModelHelper.getTx(bop, null));

        TxModelHelper txHelper = TxModelHelper.getInstance(ConfigVersion.CONFIG_1_0);
        txHelper.setTx(bop, null, ComboConstants.TX_MANDATORY);
        assertEquals("TxValue", ComboConstants.TX_MANDATORY, TxModelHelper.getTx(bop, null));
        txHelper.setTx(bop, null, ComboConstants.TX_NEVER);
        assertEquals("TxValue", ComboConstants.TX_NOTSUPPORTED, TxModelHelper.getTx(bop, null));
        txHelper.setTx(bop, null, ComboConstants.TX_REQUIRED);
        assertEquals("TxValue", ComboConstants.TX_REQUIRED, TxModelHelper.getTx(bop, null));
        txHelper.setTx(bop, null, ComboConstants.TX_REQUIRESNEW);
        assertEquals("TxValue", ComboConstants.TX_REQUIRESNEW, TxModelHelper.getTx(bop, null));
        txHelper.setTx(bop, null, ComboConstants.TX_SUPPORTED);
        assertEquals("TxValue", ComboConstants.TX_SUPPORTED, TxModelHelper.getTx(bop, null));
        
        model.endTransaction();

        TestUtil.dumpToFile(model.getBaseDocument(), f);
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}
