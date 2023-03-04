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
import javax.xml.namespace.QName;
import java.util.List;
import java.util.HashMap;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.AlgorithmSuite;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic128;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic192;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic256;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic256Sha256;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic256Sha256Rsa15;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.TripleDes;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.TripleDesRsa15;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.TripleDesSha256;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.TripleDesSha256Rsa15;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic128Rsa15;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic128Sha256;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic128Sha256Rsa15;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic192Rsa15;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic192Sha256;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic192Sha256Rsa15;
import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.Basic256Rsa15;

/**
 *
 * @author Martin Grebac
 */
public class AlgoSuiteModelHelper {

    private static HashMap<ConfigVersion, AlgoSuiteModelHelper> instances =
            new HashMap<ConfigVersion, AlgoSuiteModelHelper>();
    private ConfigVersion configVersion = ConfigVersion.getDefault();

    /**
     * Creates a new instance of AlgoSuiteModelHelper
     */
    private AlgoSuiteModelHelper(ConfigVersion configVersion) {
        this.configVersion = configVersion;
    }

    public static final AlgoSuiteModelHelper getInstance(ConfigVersion configVersion) {
        AlgoSuiteModelHelper instance = instances.get(configVersion);
        if (instance == null) {
            instance = new AlgoSuiteModelHelper(configVersion);
            instances.put(configVersion, instance);
        }
        return instance;
    }

    public static String getAlgorithmSuite(WSDLComponent comp) {
        WSDLComponent layout = getAlgorithmSuiteElement(comp);
        if (layout != null) {
            if (layout instanceof Basic128) return ComboConstants.BASIC128;
            if (layout instanceof Basic192) return ComboConstants.BASIC192;
            if (layout instanceof Basic256) return ComboConstants.BASIC256;
            if (layout instanceof TripleDes) return ComboConstants.TRIPLEDES;
            if (layout instanceof Basic256Rsa15) return ComboConstants.BASIC256RSA15;
            if (layout instanceof Basic192Rsa15) return ComboConstants.BASIC192RSA15;
            if (layout instanceof Basic128Rsa15) return ComboConstants.BASIC128RSA15;
            if (layout instanceof TripleDesRsa15) return ComboConstants.TRIPLEDESRSA15;
            if (layout instanceof Basic256Sha256) return ComboConstants.BASIC256SHA256;
            if (layout instanceof Basic192Sha256) return ComboConstants.BASIC192SHA256;
            if (layout instanceof Basic128Sha256) return ComboConstants.BASIC128SHA256;
            if (layout instanceof TripleDesSha256) return ComboConstants.TRIPLEDESSHA256;
            if (layout instanceof Basic256Sha256Rsa15) return ComboConstants.BASIC256SHA256RSA15;
            if (layout instanceof Basic192Sha256Rsa15) return ComboConstants.BASIC192SHA256RSA15;
            if (layout instanceof Basic128Sha256Rsa15) return ComboConstants.BASIC128SHA256RSA15;
            if (layout instanceof TripleDesSha256Rsa15) return ComboConstants.TRIPLEDESSHA256RSA15;
        }
        return null;
    }

    private static WSDLComponent getAlgorithmSuiteElement(WSDLComponent comp) {
        if ((comp instanceof Binding) || (comp instanceof BindingOperation)) {
            comp = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
        }
        if (comp == null) return null;
        Policy p = PolicyModelHelper.getTopLevelElement(comp, Policy.class,false);
        AlgorithmSuite as = PolicyModelHelper.getTopLevelElement(p, AlgorithmSuite.class,false);
        p = PolicyModelHelper.getTopLevelElement(as, Policy.class,false);
        if (p != null) {
            List<ExtensibilityElement> elements = p.getExtensibilityElements();
            if ((elements != null) && !(elements.isEmpty())) {
                ExtensibilityElement e = elements.get(0);
                return e;
            }
        }
        return null;
    }

    public void setAlgorithmSuite(WSDLComponent c, String algoSuite) {
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();

        WSDLComponent topElem = c;

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            QName qnameToCreate = null;

            if (ComboConstants.BASIC128.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.BASIC128.getQName(configVersion);
            } else if (ComboConstants.BASIC192.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.BASIC192.getQName(configVersion);
            } else if (ComboConstants.BASIC256.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.BASIC256.getQName(configVersion);
            } else if (ComboConstants.TRIPLEDES.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.TRIPLEDES.getQName(configVersion);
            } else if (ComboConstants.BASIC128RSA15.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.BASIC128RSA15.getQName(configVersion);
            } else if (ComboConstants.BASIC192RSA15.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.BASIC192RSA15.getQName(configVersion);
            } else if (ComboConstants.BASIC256RSA15.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.BASIC256RSA15.getQName(configVersion);
            } else if (ComboConstants.TRIPLEDESRSA15.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.TRIPLEDESRSA15.getQName(configVersion);
            } else if (ComboConstants.BASIC128SHA256.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.BASIC128SHA256.getQName(configVersion);
            } else if (ComboConstants.BASIC192SHA256.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.BASIC192SHA256.getQName(configVersion);
            } else if (ComboConstants.BASIC256SHA256.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.BASIC256SHA256.getQName(configVersion);
            } else if (ComboConstants.TRIPLEDESSHA256.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.TRIPLEDESSHA256.getQName(configVersion);
            } else if (ComboConstants.BASIC128SHA256RSA15.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.BASIC128SHA256RSA15.getQName(configVersion);
            } else if (ComboConstants.BASIC192SHA256RSA15.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.BASIC192SHA256RSA15.getQName(configVersion);
            } else if (ComboConstants.BASIC256SHA256RSA15.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.BASIC256SHA256RSA15.getQName(configVersion);
            } else if (ComboConstants.TRIPLEDESSHA256RSA15.equals(algoSuite)) {
                qnameToCreate = SecurityPolicyQName.TRIPLEDESSHA256RSA15.getQName(configVersion);
            }

            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            AlgorithmSuite suite = pmh.createElement(topElem,
                    SecurityPolicyQName.ALGORITHMSUITE.getQName(configVersion),
                    AlgorithmSuite.class,
                    !(topElem instanceof Policy));

            List<Policy> policies = suite.getExtensibilityElements(Policy.class);
            if ((policies != null) && (!policies.isEmpty())) {
                for (Policy pol : policies) {
                    suite.removeExtensibilityElement(pol);
                }
            }

            Policy p = pmh.createElement(suite, PolicyQName.POLICY.getQName(configVersion), Policy.class, false);
            ExtensibilityElement e = (ExtensibilityElement) wcf.create(p, qnameToCreate);
            p.addExtensibilityElement(e);
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }
    
}
