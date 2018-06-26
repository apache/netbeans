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
