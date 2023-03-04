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
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ValidatorConfiguration;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class ValidatorConfigurationImpl extends ProprietarySecurityPolicyComponentImpl implements ValidatorConfiguration {
    
    /**
     * Creates a new instance of ValidatorConfigurationImpl
     */
    public ValidatorConfigurationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public void setVisibility(String vis) {
        setAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName(), vis);
    }

    public String getVisibility() {
        return getAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName());
    }

    public void setMaxClockSkew(String maxClockSkew) {
        setAttribute(MAXCLOCKSKEW, ProprietarySecurityPolicyAttribute.MAXCLOCKSKEW, maxClockSkew);        
    }

    public String getMaxClockSkew() {
        return getAttribute(ProprietarySecurityPolicyAttribute.MAXCLOCKSKEW);
    }

    public void setTimestampFreshnessLimit(String limit) {
        setAttribute(TIMESTAMPFRESHNESS, ProprietarySecurityPolicyAttribute.TIMESTAMPFRESHNESS, limit);
    }

    public String getTimestampFreshnessLimit() {
        return getAttribute(ProprietarySecurityPolicyAttribute.TIMESTAMPFRESHNESS);
    }

    public void setRevocationEnabled(boolean revocation) {
        setAttribute(REVOCATION, ProprietarySecurityPolicyAttribute.REVOCATION, Boolean.toString(revocation));
    }

    public boolean isRevocationEnabled() {
        return Boolean.parseBoolean(getAttribute(ProprietarySecurityPolicyAttribute.REVOCATION));
    }
    
//    public void setMaxNonceAge(String maxNonceAge) {
//        setAttribute(MAXNONCEAGE, ProprietarySecurityPolicyAttribute.MAXNONCEAGE, maxNonceAge);
//    }
//
//    public String getMaxNonceAge() {
//        return getAttribute(ProprietarySecurityPolicyAttribute.MAXNONCEAGE);
//    }
    
}
