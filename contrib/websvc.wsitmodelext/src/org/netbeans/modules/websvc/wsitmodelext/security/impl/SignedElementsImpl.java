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

package org.netbeans.modules.websvc.wsitmodelext.security.impl;

import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyAttribute;
import org.netbeans.modules.websvc.wsitmodelext.security.SignedElements;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class SignedElementsImpl extends SecurityPolicyComponentImpl implements SignedElements {
    
    /**
     * Creates a new instance of SignedElementsImpl
     */
    public SignedElementsImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public void setXpathVersion(String xPathVersion) {
        setAttribute(XPATHVERSION_PROPERTY, SecurityPolicyAttribute.XPATHVERSION, xPathVersion);        
    }

    public String getXpathVersion() {
        return getAttribute(SecurityPolicyAttribute.XPATHVERSION);
    }

}
