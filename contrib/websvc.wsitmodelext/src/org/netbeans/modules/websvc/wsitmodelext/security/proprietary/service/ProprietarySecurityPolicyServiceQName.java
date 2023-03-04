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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Martin Grebac
 */
public enum ProprietarySecurityPolicyServiceQName {
    KEYSTORE(createSecurityPolicyQName("KeyStore")),  //NOI18N
    TRUSTSTORE(createSecurityPolicyQName("TrustStore")),  //NOI18N
    KERBEROSCONFIG(createSecurityPolicyQName("KerberosConfig")),  //NOI18N
    CALLBACKHANDLERCONFIGURATION(createSecurityPolicyQName("CallbackHandlerConfiguration")),  //NOI18N
    CALLBACKHANDLER(createSecurityPolicyQName("CallbackHandler")), //NOI18N
    DISABLESTREAMINGSECURITY(createSecurityPolicyQName("DisableStreamingSecurity")), //NOI18N
    VALIDATORCONFIGURATION(createSecurityPolicyQName("ValidatorConfiguration")),  //NOI18N
    VALIDATOR(createSecurityPolicyQName("Validator")), //NOI18N
    TIMESTAMP(createSecurityPolicyQName("Timestamp")); //NOI18N

    public static final String PROPRIETARY_SERVICE_SECPOLICY_UTILITY = 
            "http://schemas.sun.com/2006/03/wss/server"; //NOI18N
    public static final String PROPRIETARY_SECPOLICY_UTILITY_NS_PREFIX = "sc"; //NOI18N
            
    public static QName createSecurityPolicyQName(String localName){
        return new QName(PROPRIETARY_SERVICE_SECPOLICY_UTILITY, localName, PROPRIETARY_SECPOLICY_UTILITY_NS_PREFIX);
    }
    
    ProprietarySecurityPolicyServiceQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (ProprietarySecurityPolicyServiceQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

}
