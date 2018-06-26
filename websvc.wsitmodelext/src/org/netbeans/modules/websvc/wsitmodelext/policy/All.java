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
