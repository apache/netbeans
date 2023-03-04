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

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.PreconfiguredSTS;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyAttribute;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryTrustClientQName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class PreconfiguredSTSImpl extends ProprietaryTrustComponentClientImpl implements PreconfiguredSTS {
    
    /**
     * Creates a new instance of PreconfiguredSTSImpl
     */
    public PreconfiguredSTSImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public PreconfiguredSTSImpl(WSDLModel model){
        this(model, createPrefixedElement(ProprietaryTrustClientQName.PRECONFIGUREDSTS.getQName(), model));
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
    
    public void setEndpoint(String url) {
        setAttribute(ENDPOINT, ProprietarySecurityPolicyAttribute.ENDPOINT, url);
    }

    public String getEndpoint() {
        return getAttribute(ProprietarySecurityPolicyAttribute.ENDPOINT);
    }

    public void setMetadata(String url) {
        setAttribute(METADATA, ProprietarySecurityPolicyAttribute.METADATA, url);
    }

    public String getMetadata() {
        return getAttribute(ProprietarySecurityPolicyAttribute.METADATA);
    }
    
    public void setWsdlLocation(String url) {
        setAttribute(WSDLLOCATION, ProprietarySecurityPolicyAttribute.WSDLLOCATION, url);
    }

    public String getWsdlLocation() {
        return getAttribute(ProprietarySecurityPolicyAttribute.WSDLLOCATION);
    }

    public void setServiceName(String sname) {
        setAttribute(SERVICENAME, ProprietarySecurityPolicyAttribute.SERVICENAME, sname);
    }

    public String getServiceName() {
        return getAttribute(ProprietarySecurityPolicyAttribute.SERVICENAME);
    }

    public void setPortName(String pname) {
        setAttribute(PORTNAME, ProprietarySecurityPolicyAttribute.PORTNAME, pname);
    }

    public String getPortName() {
        return getAttribute(ProprietarySecurityPolicyAttribute.PORTNAME);
    }

    public void setNamespace(String ns) {
        setAttribute(NAMESPACE, ProprietarySecurityPolicyAttribute.NAMESPACE, ns);
    }

    public String getNamespace() {
        return getAttribute(ProprietarySecurityPolicyAttribute.NAMESPACE);
    }

    public void setTrustVersion(String trustVersion) {
        setAttribute(WSTVERSION, ProprietarySecurityPolicyAttribute.WSTVERSION, trustVersion);
    }

    public String getTrustVersion() {
        return getAttribute(ProprietarySecurityPolicyAttribute.WSTVERSION);
    }

    public void setShareToken(boolean shareToken) {
        setAttribute(SHARE_TOKEN, ProprietarySecurityPolicyAttribute.SHARETOKEN, Boolean.toString(shareToken));
    }

    public boolean isShareToken() {
        return Boolean.parseBoolean(getAttribute(ProprietarySecurityPolicyAttribute.SHARETOKEN));
    }

}
