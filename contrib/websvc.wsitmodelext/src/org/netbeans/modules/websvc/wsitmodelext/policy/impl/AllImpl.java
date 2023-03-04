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

import org.netbeans.modules.websvc.wsitmodelext.rm.FlowControl;
import org.netbeans.modules.websvc.wsitmodelext.rm.Ordered;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMAssertion;
import org.netbeans.modules.websvc.wsitmodelext.security.Wss10;
import org.netbeans.modules.websvc.wsitmodelext.security.Wss11;
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
public class AllImpl extends PolicyComponentImpl implements All {
    
    /**
     * Creates a new instance of AllImpl
     */
    public AllImpl(WSDLModel model, Element e) {
        super(model, e);
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

    public void setRMAssertion(RMAssertion rmAssertion) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(RMAssertion.class, RMAssertion.RMASSERTION_PROPERTY, rmAssertion, classes);
    }

    public RMAssertion getRMAssertion() {
        return getChild(RMAssertion.class);
    }

    public void removeRMAssertion(RMAssertion rmAssertion) {
        removeChild(RMAssertion.RMASSERTION_PROPERTY, rmAssertion);
    }

    public void setFlowControl(FlowControl flowControl) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(FlowControl.class, FlowControl.FLOWCONTROL_PROPERTY, flowControl, classes);
    }

    public FlowControl getFlowControl() {
        return getChild(FlowControl.class);
    }

    public void removeFlowControl(FlowControl flowControl) {
        removeChild(FlowControl.FLOWCONTROL_PROPERTY, flowControl);
    }

    public void setOrdered(Ordered ordered) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Ordered.class, Ordered.ORDERED_PROPERTY, ordered, classes);
    }

    public Ordered getOrdered() {
        return getChild(Ordered.class);
    }

    public void removeOrdered(Ordered ordered) {
        removeChild(Ordered.ORDERED_PROPERTY, ordered);
    }
    
    public void setWss11(Wss11 wss11) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Wss11.class, Wss11.WSS_PROPERTY, wss11, classes);
    }

    public Wss11 getWss11() {
        return getChild(Wss11.class);
    }

    public void removeWss11(Wss11 wss11) {
        removeChild(Wss11.WSS_PROPERTY, wss11);
    }

    public void setWss10(Wss10 wss10) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Wss10.class, Wss10.WSS_PROPERTY, wss10, classes);
    }

    public Wss10 getWss10() {
        return getChild(Wss10.class);
    }

    public void removeWss10(Wss10 wss10) {
        removeChild(Wss10.WSS_PROPERTY, wss10);
    }
}
