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

import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import java.util.HashMap;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10WsdlQName;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10WsdlUsingAddressing;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing13WsdlAddressing;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing13WsdlQName;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class AddressingModelHelper {
    
    private static HashMap<ConfigVersion, AddressingModelHelper> instances =
            new HashMap<ConfigVersion, AddressingModelHelper>();
    private ConfigVersion configVersion = ConfigVersion.getDefault();

    /**
     * Creates a new instance of AddressingModelHelper
     */
    private AddressingModelHelper(ConfigVersion configVersion) {
        this.configVersion = configVersion;
    }

    public static final AddressingModelHelper getInstance(ConfigVersion configVersion) {
        AddressingModelHelper instance = instances.get(configVersion);
        if (instance == null) {
            instance = new AddressingModelHelper(configVersion);
            instances.put(configVersion, instance);
        }
        return instance;
    }
    
    private static Addressing10WsdlUsingAddressing getUsingAddressing(Policy p) {
        return (Addressing10WsdlUsingAddressing) PolicyModelHelper.getTopLevelElement(p, Addressing10WsdlUsingAddressing.class,false);        
    }

    private static Addressing13WsdlAddressing getAddressing(Policy p) {
        return (Addressing13WsdlAddressing) PolicyModelHelper.getTopLevelElement(p, Addressing13WsdlAddressing.class,false);        
    }
    
    // checks if Addressing is enabled in the config wsdl on specified binding
    public static boolean isAddressingEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            Addressing10WsdlUsingAddressing addrAssertion = getUsingAddressing(p);
            if (addrAssertion != null) return true;
            Addressing13WsdlAddressing addr13Assertion = getAddressing(p);
            if (addr13Assertion != null) return true;
        }
        return false;
    }
    
    // enables Addressing in the config wsdl on specified binding
    public void enableAddressing(WSDLComponent c, boolean optional) {
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
        if (c instanceof Binding) {
            c = pmh.createPolicy(c, false);
        }
        WSDLModel model = c.getModel();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (configVersion == ConfigVersion.CONFIG_1_0) {
                Addressing10WsdlUsingAddressing addrAssertion =
                        pmh.createElement(c, Addressing10WsdlQName.USINGADDRESSING.getQName(),
                        Addressing10WsdlUsingAddressing.class, false);
                addrAssertion.setOptional(optional);
            } else {
                Addressing13WsdlAddressing addrAssertion =
                        pmh.createElement(c, Addressing13WsdlQName.ADDRESSING.getQName(configVersion),
                        Addressing13WsdlAddressing.class, false);
                addrAssertion.setOptional(optional);
            }
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }

    // disables Addressing in the config wsdl on specified binding
    public static void disableAddressing(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        Addressing10WsdlUsingAddressing a10 = getUsingAddressing(p);
        Addressing13WsdlAddressing a13 = getAddressing(p);
        if (a10 != null) {
            PolicyModelHelper.removeElement(a10.getParent(), Addressing10WsdlUsingAddressing.class, false);
        }
        if (a13 != null) {
            PolicyModelHelper.removeElement(a13.getParent(), Addressing13WsdlAddressing.class, false);
        }
        PolicyModelHelper.cleanPolicies(b);
    }
}
