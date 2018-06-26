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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
