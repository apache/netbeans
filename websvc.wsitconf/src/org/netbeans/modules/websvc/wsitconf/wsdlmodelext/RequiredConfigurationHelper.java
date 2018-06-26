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

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.xml.wsdl.model.*;

/**
 *
 * @author Martin Grebac
 */
public class RequiredConfigurationHelper {
    
    /**
     * @param c One of Binding, Operation
     * @param glassfish
     * @param jsr109
     * @param cbHandlerType One of usernameHandler, kerberosHandler, samlHandler, passwordHandler
     * @return Returns true if configuration of callbackhandler cbHandlerType is required for component
     */
    public static boolean isCallbackHandlerRequired(
            WSDLComponent c, boolean glassfish, boolean jsr109, String cbHandlerType) {
        return true;
    }

    /**
     * @param c One of Binding, Operation
     * @param glassfish
     * @param jsr109
     * @param validatorType One of usernameValidator, timestampValidator, certificateValidator, samlValidator
     * @return Returns true if configuration of validator validatorType is required for component
     */
    public static boolean isValidatorRequired(
            WSDLComponent c, boolean glassfish, boolean jsr109, String validatorType) {
        return true;
    }
    
    public static boolean isKeystoreRequired(
            WSDLComponent c, boolean client, boolean glassfish, boolean jsr109) {
        return true; 
    }

    public static boolean isTruststoreRequired(
            WSDLComponent c, boolean client, boolean glassfish, boolean jsr109) {
        return true;
    }
    
    public static boolean isSecureConversationParamRequired(
            WSDLComponent c) {

        boolean secConvEnabled = false;
        
        if (SecurityPolicyModelHelper.isSecurityEnabled(c)) {
            WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(c);
            WSDLComponent tokenKind = SecurityTokensModelHelper.getTokenElement(secBinding, ProtectionToken.class);
            WSDLComponent tokenType = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
            secConvEnabled = (tokenType instanceof SecureConversationToken);
        }
        
        return secConvEnabled; 
    }

}
