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

package org.netbeans.modules.websvc.wsitmodelext.policy;

import org.netbeans.modules.websvc.wsitmodelext.rm.FlowControl;
import org.netbeans.modules.websvc.wsitmodelext.rm.Ordered;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMAssertion;
import org.netbeans.modules.websvc.wsitmodelext.security.Wss10;
import org.netbeans.modules.websvc.wsitmodelext.security.Wss11;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 *
 * @author Martin Grebac
 */
public interface All extends ExtensibilityElement {
    public static final String ALL_PROPERTY = "ALL";     //NOI18N
    public static final String EXACTLYONE_PROPERTY = "EXACTLYONE";     //NOI18N
    public static final String POLICY_PROPERTY = "POLICY";     //NOI18N
    public static final String POLICYREFERENCE_PROPERTY = "POLICYREFERENCE";     //NOI18N
    
    All getAll();
    void setAll(All all);
    void removeAll(All all);

    ExactlyOne getExactlyOne();
    void setExactlyOne(ExactlyOne exactlyOne);
    void removeExactlyOne(ExactlyOne exactlyOne);
    
    PolicyReference getPolicyReference();
    void setPolicyReference(PolicyReference policyReference);
    void removePolicyReference(PolicyReference policyReference);

    RMAssertion getRMAssertion();
    void setRMAssertion(RMAssertion rmAssertion);
    void removeRMAssertion(RMAssertion rmAssertion);

    Wss11 getWss11();
    void setWss11(Wss11 wss11);
    void removeWss11(Wss11 wss11);

    Wss10 getWss10();
    void setWss10(Wss10 wss10);
    void removeWss10(Wss10 wss10);

    FlowControl getFlowControl();
    void setFlowControl(FlowControl flowControl);
    void removeFlowControl(FlowControl flowControl);

    Ordered getOrdered();
    void setOrdered(Ordered ordered);
    void removeOrdered(Ordered ordered);
}
