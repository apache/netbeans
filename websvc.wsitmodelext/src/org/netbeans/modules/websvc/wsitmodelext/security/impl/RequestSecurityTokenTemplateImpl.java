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
