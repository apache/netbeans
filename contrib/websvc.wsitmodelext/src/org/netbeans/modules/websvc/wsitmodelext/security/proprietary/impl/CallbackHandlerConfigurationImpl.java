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

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandlerConfiguration;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyAttribute;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class CallbackHandlerConfigurationImpl extends ProprietarySecurityPolicyComponentImpl implements CallbackHandlerConfiguration {
    
    /**
     * Creates a new instance of CallbackHandlerConfigurationImpl
     */
    public CallbackHandlerConfigurationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    @Override
    public void setVisibility(String vis) {
        setAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName(), vis);
    }

    @Override
    public String getVisibility() {
        return getAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName());
    }

    @Override
    public void setTimestampTimeout(String timeout) {
        setAttribute(TIMESTAMPTIMEOUT, ProprietarySecurityPolicyAttribute.TIMESTAMPTIMEOUT, timeout);        
    }

    @Override
    public String getTimestampTimeout() {
        return getAttribute(ProprietarySecurityPolicyAttribute.TIMESTAMPTIMEOUT);
    }

    @Override
    public void setIterationsForPDK(String iterations) {
        setAttribute(ITERATIONS, ProprietarySecurityPolicyAttribute.ITERATIONSFORPDK, iterations);
    }

    @Override
    public String getIterationsForPDK() {
        return getAttribute(ProprietarySecurityPolicyAttribute.ITERATIONSFORPDK);
    }
    
}
