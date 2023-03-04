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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary;

import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author Martin Grebac
 */
public enum ProprietarySecurityPolicyAttribute implements Attribute {
        VISIBILITY("visibility"),                   //NOI18N
        ITERATIONSFORPDK("iterationsForPDK"),       //NOI18N
        WSTVERSION("wstVersion"),                   //NOI18N
        SHARETOKEN("shareToken"),                   //NOI18N
        TIMESTAMPTIMEOUT("timestampTimeout"),       //NOI18N
        MAXCLOCKSKEW("maxClockSkew"),               //NOI18N
        TIMESTAMPFRESHNESS("timestampFreshnessLimit"),  //NOI18N
//        MAXNONCEAGE("maxNonceAge"),                   //NOI18N
        REVOCATION("revocationEnabled"),                //NOI18N
        LOGINMODULE("loginModule"),                     //NOI18N
        SERVICEPRINCIPAL("servicePrincipal"),           //NOI18N
        CREDENTIALDELEGATION("credentialDelegation"),   //NOI18N
        DEFAULT("default"),                         //NOI18N
        ENCRYPTISSUEDKEY("encryptIssuedKey"),       //NOI18N
        ENCRYPTISSUEDTOKEN("encryptIssuedToken"),   //NOI18N
        ENDPOINT("endpoint"),                       //NOI18N
        METADATA("metadata"),                       //NOI18N
        WSDLLOCATION("wsdlLocation"),               //NOI18N
        SERVICENAME("serviceName"),                 //NOI18N
        PORTNAME("portName"),                       //NOI18N
        NAMESPACE("namespace"),                     //NOI18N
        TIMEOUT("timeout"),                         //NOI18N
        REQUIRECANCELSCT("requireCancelSCT"),       //NOI18N
        RENEWEXPIREDSCT("renewExpiredSCT"),         //NOI18N
        LOCATION("location"),                       //NOI18N
        ALIASSELECTOR("aliasSelector"),             //NOI18N
        CERTSELECTOR("certSelector"),               //NOI18N
        ALIAS("alias"),                     //NOI18N
        STSALIAS("stsalias"),               //NOI18N
        PEERALIAS("peeralias"),             //NOI18N
        TYPE("type"),                       //NOI18N
        KEYPASS("keypass"),                 //NOI18N
        STOREPASS("storepass"),             //NOI18N
        NAME("name"),                       //NOI18N
        CLASSNAME("classname");             //NOI18N
    
    private String name;
    private Class type;
    private Class subtype;
    
    /**
     * Creates a new instance of ProprietarySecurityPolicyAttribute
     */
    ProprietarySecurityPolicyAttribute(String name) {
        this(name, String.class);
    }
    ProprietarySecurityPolicyAttribute(String name, Class type) {
        this(name, type, null);
    }
    ProprietarySecurityPolicyAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    @Override
    public String toString() { return name; }

    public Class getType() {
        return type;
    }

    public String getName() { return name; }

    public Class getMemberType() { return subtype; }
}
