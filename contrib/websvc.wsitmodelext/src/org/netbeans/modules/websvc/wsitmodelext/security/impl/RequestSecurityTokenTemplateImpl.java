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

import java.util.Collections;
import org.netbeans.modules.websvc.wsitmodelext.security.RequestSecurityTokenTemplate;
import org.netbeans.modules.websvc.wsitmodelext.trust.KeySize;
import org.netbeans.modules.websvc.wsitmodelext.trust.KeyType;
import org.netbeans.modules.websvc.wsitmodelext.trust.TokenType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class RequestSecurityTokenTemplateImpl extends SecurityPolicyComponentImpl implements RequestSecurityTokenTemplate {
    
    /**
     * Creates a new instance of RequestSecurityTokenTemplateImpl
     */
    public RequestSecurityTokenTemplateImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public KeyType getKeyType() {
        return getChild(KeyType.class);
    }

    public void setKeyType(KeyType keyType) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(KeyType.class, KEYTYPE_PROPERTY, keyType, classes);
    }

    public void removeKeyType(KeyType keyType) {
        removeChild(KEYTYPE_PROPERTY, keyType);
    }

    public KeySize getKeySize() {
        return getChild(KeySize.class);
    }

    public void setKeySize(KeySize keySize) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(KeySize.class, KEYSIZE_PROPERTY, keySize, classes);
    }

    public void removeKeySize(KeyType keySize) {
        removeChild(KEYSIZE_PROPERTY, keySize);
    }

    public TokenType getTokenType() {
        return getChild(TokenType.class);
    }

    public void setTokenType(TokenType tokenType) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(TokenType.class, TOKENTYPE_PROPERTY, tokenType, classes);
    }

    public void removeTokenType(TokenType tokenType) {
        removeChild(TOKENTYPE_PROPERTY, tokenType);
    }

}
