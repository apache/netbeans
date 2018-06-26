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
