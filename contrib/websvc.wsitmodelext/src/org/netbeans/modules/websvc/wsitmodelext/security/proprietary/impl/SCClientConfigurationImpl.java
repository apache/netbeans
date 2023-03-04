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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary.impl;

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySCClientQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyAttribute;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.SCClientConfiguration;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class SCClientConfigurationImpl extends ProprietarySCComponentClientImpl implements SCClientConfiguration {
    
    /**
     * Creates a new instance of SCClientConfigurationImpl
     */
    public SCClientConfigurationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SCClientConfigurationImpl(WSDLModel model){
        this(model, createPrefixedElement(ProprietarySCClientQName.SCCLIENTCONFIGURATION.getQName(), model));
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
    
    public void setRequireCancelSCT(boolean requireCancel) {
        setAttribute(REQUIRECANCELSCT, ProprietarySecurityPolicyAttribute.REQUIRECANCELSCT, Boolean.toString(requireCancel));
    }

    public boolean isRequireCancelSCT() {
        return Boolean.parseBoolean(getAttribute(ProprietarySecurityPolicyAttribute.REQUIRECANCELSCT));
    }

    public void setRenewExpiredSCT(boolean renewExpired) {
        setAttribute(RENEWEXPIREDSCT, ProprietarySecurityPolicyAttribute.RENEWEXPIREDSCT, Boolean.toString(renewExpired));
    }

    public boolean isRenewExpiredSCT() {
        return Boolean.parseBoolean(getAttribute(ProprietarySecurityPolicyAttribute.RENEWEXPIREDSCT));
    }
    
}
