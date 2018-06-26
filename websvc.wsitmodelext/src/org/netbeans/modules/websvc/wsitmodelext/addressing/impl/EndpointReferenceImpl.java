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

package org.netbeans.modules.websvc.wsitmodelext.addressing.impl;

import org.netbeans.modules.websvc.wsitmodelext.addressing.EndpointReference;
import org.netbeans.modules.websvc.wsitmodelext.addressing.ReferenceParameters;
import org.netbeans.modules.websvc.wsitmodelext.addressing.ReferenceProperties;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;
import java.util.Collections;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Address;
import org.netbeans.modules.websvc.wsitmodelext.addressing.AddressingPortType;
import org.netbeans.modules.websvc.wsitmodelext.addressing.AddressingServiceName;

/**
 *
 * @author Martin Grebac
 */
public class EndpointReferenceImpl extends AddressingComponentImpl implements EndpointReference {
    
    /**
     * Creates a new instance of EndpointReferenceImpl
     */
    public EndpointReferenceImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public void setAddress(Address address) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Address.class, ADDRESS_PROPERTY, address, classes);
    }

    public Address getAddress() {
        return getChild(Address.class);
    }

    public void removeAddress(Address address) {
        removeChild(ADDRESS_PROPERTY, address);
    }

    public void setServiceName(AddressingServiceName serviceName) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(AddressingServiceName.class, SERVICENAME_PROPERTY, serviceName, classes);
    }

    public AddressingServiceName getServiceName() {
        return getChild(AddressingServiceName.class);
    }

    public void removeServiceName(AddressingServiceName serviceName) {
        removeChild(SERVICENAME_PROPERTY, serviceName);
    }
    
    public void setPortType(AddressingPortType addressingPortType) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(AddressingPortType.class, ADDRESSING_PORTTYPE_PROPERTY, addressingPortType, classes);
    }

    public AddressingPortType getPortType() {
        return getChild(AddressingPortType.class);
    }

    public void removePortType(AddressingPortType portType) {
        removeChild(ADDRESSING_PORTTYPE_PROPERTY, portType);
    }

    public void setReferenceProperties(ReferenceProperties referenceProperties) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(ReferenceProperties.class, REFERENCE_PROPERTIES_PROPERTY, referenceProperties, classes);
    }

    public ReferenceProperties getReferenceProperties() {
        return getChild(ReferenceProperties.class);
    }

    public void removeReferenceProperties(ReferenceProperties referenceProperties) {
        removeChild(REFERENCE_PROPERTIES_PROPERTY, referenceProperties);
    }

    public void setReferenceParameters(ReferenceParameters referenceParameters) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(ReferenceParameters.class, REFERENCE_PARAMETERS_PROPERTY, referenceParameters, classes);
    }

    public ReferenceParameters getReferenceParameters() {
        return getChild(ReferenceParameters.class);
    }

    public void removeReferenceParameters(ReferenceParameters referenceParameters) {
        removeChild(REFERENCE_PARAMETERS_PROPERTY, referenceParameters);
    }
    
}
