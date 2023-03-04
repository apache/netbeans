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
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.TrustStore;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class TrustStoreImpl extends ProprietarySecurityPolicyComponentImpl implements TrustStore {
    
    /**
     * Creates a new instance of TrustStoreImpl
     */
    public TrustStoreImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public void setVisibility(String vis) {
        setAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName(), vis);
    }

    public String getVisibility() {
        return getAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName());
    }

    public void setLocation(String location) {
        setAttribute(LOCATION, ProprietarySecurityPolicyAttribute.LOCATION, location);        
    }

    public String getLocation() {
        return getAttribute(ProprietarySecurityPolicyAttribute.LOCATION);
    }
    
    public void setType(String type) {
        setAttribute(TYPE, ProprietarySecurityPolicyAttribute.TYPE, type);        
    }

    public String getType() {
        return getAttribute(ProprietarySecurityPolicyAttribute.TYPE);
    }

    public void setStorePassword(String storepass) {
        setAttribute(PASSWORD, ProprietarySecurityPolicyAttribute.STOREPASS, storepass);        
    }

    public String getStorePassword() {
        return getAttribute(ProprietarySecurityPolicyAttribute.STOREPASS);
    }

    public void setSTSAlias(String alias) {
        setAttribute(STSALIAS, ProprietarySecurityPolicyAttribute.STSALIAS, alias);        
    }

    public String getSTSAlias() {
        return getAttribute(ProprietarySecurityPolicyAttribute.STSALIAS);
    }

    public void setPeerAlias(String alias) {
        setAttribute(PEERALIAS, ProprietarySecurityPolicyAttribute.PEERALIAS, alias);        
    }

    public String getPeerAlias() {
        return getAttribute(ProprietarySecurityPolicyAttribute.PEERALIAS);
    }

    public void setCertSelector(String selector) {
        setAttribute(CERTSELECTOR, ProprietarySecurityPolicyAttribute.CERTSELECTOR, selector);
    }

    public String getCertSelector() {
        return getAttribute(ProprietarySecurityPolicyAttribute.CERTSELECTOR);
    }
    
}
