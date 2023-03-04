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
