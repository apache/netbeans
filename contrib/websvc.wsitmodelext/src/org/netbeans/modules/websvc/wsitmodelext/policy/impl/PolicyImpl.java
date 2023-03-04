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

package org.netbeans.modules.websvc.wsitmodelext.policy.impl;

import org.netbeans.modules.websvc.wsitmodelext.security.SecurityQName;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.ExactlyOne;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyReference;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

import java.util.Collections;

/**
 *
 * @author Martin Grebac
 */
public class PolicyImpl extends PolicyComponentImpl implements Policy {
    
    /**
     * Creates a new instance of PolicyImpl
     */
    public PolicyImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public void setID(String id) {
        setAnyAttribute(SecurityQName.SECPOLID.getQName(), id);  // TODO -> update to get correct version 
    }

    public String getID() {
        return getAnyAttribute(SecurityQName.SECPOLID.getQName()); // TODO -> update to get correct version 
    }

    public void setAll(All all) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(All.class, ALL_PROPERTY, all, classes);
    }

    public All getAll() {
        return getChild(All.class);
    }

    public void removeAll(All all) {
        removeChild(ALL_PROPERTY, all);
    }

    public void setExactlyOne(ExactlyOne exactlyOne) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(ExactlyOne.class, EXACTLYONE_PROPERTY, exactlyOne, classes);
    }

    public ExactlyOne getExactlyOne() {
        return getChild(ExactlyOne.class);
    }

    public void removeExactlyOne(ExactlyOne exactlyOne) {
        removeChild(EXACTLYONE_PROPERTY, exactlyOne);
    }

    public void setPolicy(Policy policy) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Policy.class, POLICY_PROPERTY, policy, classes);
    }

    public Policy getPolicy() {
        return getChild(Policy.class);
    }

    public void removePolicy(Policy policy) {
        removeChild(POLICY_PROPERTY, policy);
    }

    public void setPolicyReference(PolicyReference policyReference) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(PolicyReference.class, POLICYREFERENCE_PROPERTY, policyReference, classes);
    }

    public PolicyReference getPolicyReference() {
        return getChild(PolicyReference.class);
    }

    public void removePolicyReference(PolicyReference policyReference) {
        removeChild(POLICYREFERENCE_PROPERTY, policyReference);
    }    
}
