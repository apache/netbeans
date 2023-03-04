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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl;

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyAttribute;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ProprietaryTrustServiceQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.STSConfiguration;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class STSConfigurationServiceImpl extends ProprietaryTrustComponentServiceImpl implements STSConfiguration {
    
    /**
     * Creates a new instance of SCConfigurationServiceImpl
     */
    public STSConfigurationServiceImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public STSConfigurationServiceImpl(WSDLModel model){
        this(model, createPrefixedElement(ProprietaryTrustServiceQName.STSCONFIGURATION.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setVisibility(String vis) {
        setAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName(), vis);
    }

    public String getVisibility() {
        return getAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName());
    }
    
    public void setEncryptIssuedKey(boolean encrypt) {
        setAttribute(ENCRYPTISSUEDKEY, ProprietarySecurityPolicyAttribute.ENCRYPTISSUEDKEY, Boolean.toString(encrypt));
    }

    public boolean isEncryptIssuedKey() {
        return Boolean.parseBoolean(getAttribute(ProprietarySecurityPolicyAttribute.ENCRYPTISSUEDKEY));
    }

    public void setEncryptIssuedToken(boolean encrypt) {
        setAttribute(ENCRYPTISSUEDTOKEN, ProprietarySecurityPolicyAttribute.ENCRYPTISSUEDTOKEN, Boolean.toString(encrypt));        
    }

    public boolean isEncryptIssuedToken() {
        return Boolean.parseBoolean(getAttribute(ProprietarySecurityPolicyAttribute.ENCRYPTISSUEDTOKEN));
    }    
}
