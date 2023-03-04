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
package org.netbeans.modules.websvc.owsm.generators;

import java.util.ArrayList;
import java.util.Collection;

import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.owsm.OWSMPolicyCodeGenerator;
import org.openide.util.lookup.ServiceProvider;


/**
 * Client code generator for 
 * 'wss11_saml_or_username_token_with_message_protection_service_policy' service policy 
 * @author ads
 *
 */
@ServiceProvider( service = OWSMPolicyCodeGenerator.class )
public class Wss11SamlOrUsernameGenerator implements OWSMPolicyCodeGenerator {

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.owsm.OWSMPolicyCodeGenerator#getId()
     */
    @Override
    public String getId() {
        // this is service policy id
        return "wss11_saml_or_username_token_with_message_protection_service_policy";   // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.owsm.OWSMPolicyCodeGenerator#generatePolicyAccessCode(java.lang.StringBuilder)
     */
    @Override
    public void generatePolicyAccessCode( StringBuilder code , Client client) {
        code.append( "WSBindingProvider wsbp = (WSBindingProvider)port;\n" );   // NOI18N
        code.append("Map<String, Object> requestContext = wsbp.getRequestContext();\n");// NOI18N
        code.append("//comment out for B16\n");                                 // NOI18N
        code.append("//requestContext.put(ClientConstants.WSSEC_KEYSTORE_TYPE, \"JKS\");\n");// NOI18N
        code.append("//requestContext.put(ClientConstants.WSSEC_KEYSTORE_LOCATION, // need location here);\n");// NOI18N
        code.append("//requestContext.put(ClientConstants.WSSEC_KEYSTORE_PASSWORD, // need keystore password here);\n");// NOI18N
        code.append("//comment out for B16\n");                                 // NOI18N
        code.append("\n//FOR SAML\n");
        code.append("//requestContext.put(ClientConstants.WSSEC_SIG_KEY_ALIAS, // need key alias here);  " );// NOI18N
        code.append("// public key from server for signing\n");                 // NOI18N
        code.append("//requestContext.put(ClientConstants.WSSEC_SIG_KEY_PASSWORD, // need key password here);\n");// NOI18N
        code.append("//FOR SAML\n");                                            // NOI18N
        code.append("// Override the endpoint - useful when switching target environments without regenerating the jax-ws client\n");// NOI18N
        code.append("requestContext.put(WSBindingProvider.ENDPOINT_ADDRESS_PROPERTY, \"");// NOI18N
        code.append(client.getWsdlUrl());
        code.append("\");\n");                                      // NOI18N
        code.append("\n//B16\n");                                   // NOI18N
        code.append("// requestContext.put(ClientConstants.WSSEC_RECIPIENT_KEY_ALIAS, // need key alias here)\n");// NOI18N
        code.append("//B16  \n");                                   // NOI18N
        code.append("// Specify username\n");                       // NOI18N
        code.append("requestContext.put(WSBindingProvider.USERNAME_PROPERTY, null );\n");// NOI18N
        code.append("// For username token, specify password.  Not used for SAML security policy\n");// NOI18N
        code.append("requestContext.put(WSBindingProvider.PASSWORD_PROPERTY, null );\n");// NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.owsm.OWSMPolicyCodeGenerator#addRequiredClassesFqns(java.util.Collection)
     */
    @Override
    public void addRequiredClassesFqns( Collection<String> fqns ) {
        fqns.add("oracle.webservices.ClientConstants");              // NOI18N
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.owsm.OWSMPolicyCodeGenerator#getRelatedPolicyIds()
     */
    @Override
    public Collection<String> getRelatedPolicyIds() {
        Collection<String> result =  new ArrayList<String>(2);
        result.add("oracle/wss11_username_token_with_message_protection_client_policy");//NOI18N
        result.add("oracle/wss11_saml_token_with_message_protection_client_policy");    //NOI18N
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.owsm.OWSMPolicyCodeGenerator#getClientId()
     */
    @Override
    public String getClientId() {
        return "oracle/wss_username_token_client_policy";
    }

}
