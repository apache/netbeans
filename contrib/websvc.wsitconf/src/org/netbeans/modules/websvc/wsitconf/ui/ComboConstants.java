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

package org.netbeans.modules.websvc.wsitconf.ui;

import org.openide.util.NbBundle;

/**
 *
 * @author MartinG
 */
public interface ComboConstants {

    //Transaction Constants
    String TX_REQUIRED = NbBundle.getMessage(ComboConstants.class, "COMBO_Required");    //NOI18N
    String TX_REQUIRESNEW = NbBundle.getMessage(ComboConstants.class, "COMBO_RequiresNew");    //NOI18N
    String TX_NOTSUPPORTED = NbBundle.getMessage(ComboConstants.class, "COMBO_NotSupported");    //NOI18N
    String TX_SUPPORTED = NbBundle.getMessage(ComboConstants.class, "COMBO_Supported");    //NOI18N
    String TX_MANDATORY = NbBundle.getMessage(ComboConstants.class, "COMBO_Mandatory");    //NOI18N
    String TX_NEVER = NbBundle.getMessage(ComboConstants.class, "COMBO_Never");    //NOI18N

    // Security Profiles
    String PROF_TRANSPORT = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_Transport");    //NOI18N
    String PROF_MSGAUTHSSL = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_MsgAuthSSL");    //NOI18N
    String PROF_SAMLSSL = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_SAMLSSL");    //NOI18N
    String PROF_USERNAME = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_Username");    //NOI18N
    String PROF_USERNAME_PASSWORDDERIVED = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_Username_PasswordDerived");    //NOI18N
    String PROF_MUTUALCERT = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_MutualCert");    //NOI18N
    String PROF_ENDORSCERT = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_EndorsCert");    //NOI18N
    String PROF_SAMLSENDER = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_SamlSender");    //NOI18N
    String PROF_SAMLHOLDER = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_SamlHolder");    //NOI18N
    String PROF_KERBEROS = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_Kerberos");    //NOI18N
    String PROF_STSISSUED = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_STSIssued");    //NOI18N
    String PROF_STSISSUEDCERT = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_STSIssuedCert");    //NOI18N
    String PROF_STSISSUEDENDORSE = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_STSIssuedEndorse");    //NOI18N
    String PROF_STSISSUEDSUPPORTING = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_STSIssuedSupporting");    //NOI18N
    String PROF_GENERIC = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_Generic");    //NOI18N
    String PROF_NOTRECOGNIZED = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_NotRecognized");    //NOI18N

    String PROF_TRANSPORT_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_Transport_Info");    //NOI18N
    String PROF_MSGAUTHSSL_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_MsgAuthSSL_Info");    //NOI18N
    String PROF_SAMLSSL_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_SAMLSSL_Info");    //NOI18N
    String PROF_USERNAME_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_Username_Info");    //NOI18N
    String PROF_USERNAME_PASSWORDDERIVED_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_Username_PasswordDerived_Info");    //NOI18N
    String PROF_MUTUALCERT_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_MutualCert_Info");    //NOI18N
    String PROF_ENDORSCERT_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_EndorsCert_Info");    //NOI18N
    String PROF_SAMLSENDER_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_SamlSender_Info");    //NOI18N
    String PROF_SAMLHOLDER_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_SamlHolder_Info");    //NOI18N
    String PROF_KERBEROS_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_Kerberos_Info");    //NOI18N
    String PROF_STSISSUED_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_STSIssued_Info");    //NOI18N
    String PROF_STSISSUEDCERT_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_STSIssuedCert_Info");    //NOI18N
    String PROF_STSISSUEDENDORSE_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_STSIssuedEndorse_Info");    //NOI18N
    String PROF_STSISSUEDSUPPORTING_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_STSIssuedSupporting_Info");    //NOI18N
    String PROF_GENERIC_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_Generic_Info");    //NOI18N
    String PROF_NOTRECOGNIZED_INFO = NbBundle.getMessage(ComboConstants.class, "COMBO_Profile_NotRecognized_Info");    //NOI18N

    String ASYMMETRIC = NbBundle.getMessage(ComboConstants.class, "COMBO_Asymmetric");    //NOI18N
    String NOSECURITY = NbBundle.getMessage(ComboConstants.class, "COMBO_NoSecurity");  //NOI18N
    String SYMMETRIC = NbBundle.getMessage(ComboConstants.class, "COMBO_Symmetric");  //NOI18N
    String TRANSPORT = NbBundle.getMessage(ComboConstants.class, "COMBO_Transport");  //NOI18N
 
    String USERNAME = NbBundle.getMessage(ComboConstants.class, "COMBO_UsernameToken");
    String X509 = NbBundle.getMessage(ComboConstants.class, "COMBO_X509Token");
    String SAML = NbBundle.getMessage(ComboConstants.class, "COMBO_SamlToken");
    String REL = NbBundle.getMessage(ComboConstants.class, "COMBO_RelToken");
    String KERBEROS = NbBundle.getMessage(ComboConstants.class, "COMBO_KerberosToken");
    String SECURITYCONTEXT = NbBundle.getMessage(ComboConstants.class, "COMBO_SecurityContextToken");
    String SECURECONVERSATION = NbBundle.getMessage(ComboConstants.class, "COMBO_SecureConversationToken");
    String ISSUED = NbBundle.getMessage(ComboConstants.class, "COMBO_IssuedToken");
    String SPNEGOCONTEXT = NbBundle.getMessage(ComboConstants.class, "COMBO_SpNegoContextToken");
    String HTTPS = NbBundle.getMessage(ComboConstants.class, "COMBO_HttpsToken");

    String PROTECTION = "protection";     //NOI18N
    String SIGNATURE = "signature";     //NOI18N
    String ENCRYPTION = "encryption";     //NOI18N
    String INITIATOR = "initiator";     //NOI18N
    String RECIPIENT = "recipient";     //NOI18N
    
    String STRICT       = NbBundle.getMessage(ComboConstants.class, "COMBO_Strict");         //NOI18N
    String LAX          = NbBundle.getMessage(ComboConstants.class, "COMBO_Lax");            //NOI18N
    String LAXTSFIRST   = NbBundle.getMessage(ComboConstants.class, "COMBO_LaxTsFirst");     //NOI18N
    String LAXTSLAST    = NbBundle.getMessage(ComboConstants.class, "COMBO_LaxTsLast");      //NOI18N
    
    String BASIC256       = NbBundle.getMessage(ComboConstants.class, "COMBO_Basic256");     //NOI18N
    String BASIC192       = NbBundle.getMessage(ComboConstants.class, "COMBO_Basic192");     //NOI18N
    String BASIC128       = NbBundle.getMessage(ComboConstants.class, "COMBO_Basic128");     //NOI18N
    String TRIPLEDES      = NbBundle.getMessage(ComboConstants.class, "COMBO_TripleDes");    //NOI18N
    String BASIC256RSA15       = NbBundle.getMessage(ComboConstants.class, "COMBO_Basic256Rsa15");    //NOI18N
    String BASIC192RSA15       = NbBundle.getMessage(ComboConstants.class, "COMBO_Basic192Rsa15");    //NOI18N
    String BASIC128RSA15       = NbBundle.getMessage(ComboConstants.class, "COMBO_Basic128Rsa15");    //NOI18N
    String TRIPLEDESRSA15      = NbBundle.getMessage(ComboConstants.class, "COMBO_TripleDesRsa15");   //NOI18N
    String BASIC256SHA256       = NbBundle.getMessage(ComboConstants.class, "COMBO_Basic256Sha256");   //NOI18N
    String BASIC192SHA256       = NbBundle.getMessage(ComboConstants.class, "COMBO_Basic192Sha256");   //NOI18N
    String BASIC128SHA256       = NbBundle.getMessage(ComboConstants.class, "COMBO_Basic128Sha256");   //NOI18N
    String TRIPLEDESSHA256      = NbBundle.getMessage(ComboConstants.class, "COMBO_TripleDesSha256");  //NOI18N
    String BASIC256SHA256RSA15       = NbBundle.getMessage(ComboConstants.class, "COMBO_Basic256Sha256Rsa15");  //NOI18N
    String BASIC192SHA256RSA15       = NbBundle.getMessage(ComboConstants.class, "COMBO_Basic192Sha256Rsa15");  //NOI18N
    String BASIC128SHA256RSA15       = NbBundle.getMessage(ComboConstants.class, "COMBO_Basic128Sha256Rsa15");  //NOI18N
    String TRIPLEDESSHA256RSA15      = NbBundle.getMessage(ComboConstants.class, "COMBO_TripleDesSha256Rsa15"); //NOI18N

    String SAML_V1011 = NbBundle.getMessage(ComboConstants.class, "COMBO_SAML1011");
    String SAML_V1010 = NbBundle.getMessage(ComboConstants.class, "COMBO_SAML1010");
    String SAML_V1110 = NbBundle.getMessage(ComboConstants.class, "COMBO_SAML1110");
    String SAML_V1111 = NbBundle.getMessage(ComboConstants.class, "COMBO_SAML1111");
    String SAML_V2011 = NbBundle.getMessage(ComboConstants.class, "COMBO_SAML2011");
    
    String X509_V110 = NbBundle.getMessage(ComboConstants.class, "COMBO_X509_V110");
    String X509_V310 = NbBundle.getMessage(ComboConstants.class, "COMBO_X509_V310");
    String X509_PKCS710 = NbBundle.getMessage(ComboConstants.class, "COMBO_X509_PKCS710");
    String X509_PKIPATHV110 = NbBundle.getMessage(ComboConstants.class, "COMBO_X509_PKIPATHV110");
    String X509_V111 = NbBundle.getMessage(ComboConstants.class, "COMBO_X509_V111");
    String X509_V311 = NbBundle.getMessage(ComboConstants.class, "COMBO_X509_V311");
    String X509_PKCS711 = NbBundle.getMessage(ComboConstants.class, "COMBO_X509_PKCS711");
    String X509_PKIPATHV111 = NbBundle.getMessage(ComboConstants.class, "COMBO_X509_PKIPATHV111");    
    
    String KERBEROS_KERBEROS = NbBundle.getMessage(ComboConstants.class, "COMBO_Kerberos");
    String KERBEROS_KERBEROSGSS = NbBundle.getMessage(ComboConstants.class, "COMBO_Kerberos_GSS");
    
    String NONE = "       ";         //NOI18N
    
    String NEVER = NbBundle.getMessage(ComboConstants.class, "COMBO_Never");         //NOI18N
    String ONCE = NbBundle.getMessage(ComboConstants.class, "COMBO_Once");           //NOI18N
    String ALWAYSRECIPIENT = NbBundle.getMessage(ComboConstants.class, "COMBO_AlwaysToRecipient");       //NOI18N
    String ALWAYS = NbBundle.getMessage(ComboConstants.class, "COMBO_Always");       //NOI18N

    String NEVER_POLICYSTR = "/IncludeToken/Never"; //NOI18N
    String ONCE_POLICYSTR = "/IncludeToken/Once"; //NOI18N
    String ALWAYSRECIPIENT_POLICYSTR = "/IncludeToken/AlwaysToRecipient"; //NOI18N
    String ALWAYS_POLICYSTR = "/IncludeToken/Always"; //NOI18N

    String WSS11 = NbBundle.getMessage(ComboConstants.class, "COMBO_WSS11");         //NOI18N
    String WSS10 = NbBundle.getMessage(ComboConstants.class, "COMBO_WSS10");         //NOI18N

    String ASSYMETRIC_KEYS = NbBundle.getMessage(ComboConstants.class, "COMBO_AssymetricKeys");         //NOI18N
    String RANDOMSYMMETRIC_KEYS = NbBundle.getMessage(ComboConstants.class, "COMBO_RandomSymmetricKeys");         //NOI18N
    String ISSUEDSYMMETRIC_KEYS = NbBundle.getMessage(ComboConstants.class, "COMBO_IssuedSymmetricKeys");         //NOI18N
    String TRANSPORTPROTECTION = NbBundle.getMessage(ComboConstants.class, "COMBO_TransportProtection");         //NOI18N

    String ISSUED_TOKENTYPE_SAML10 = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_TokenType_SAML10");         //NOI18N
    String ISSUED_TOKENTYPE_SAML10_POLICYSTR = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_TokenType_SAML10_PolicyStr");         //NOI18N
    String ISSUED_TOKENTYPE_SAML11 = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_TokenType_SAML11");         //NOI18N
    String ISSUED_TOKENTYPE_SAML11_POLICYSTR = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_TokenType_SAML11_PolicyStr");         //NOI18N
    String ISSUED_TOKENTYPE_SAML20 = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_TokenType_SAML20");         //NOI18N
    String ISSUED_TOKENTYPE_SAML20_POLICYSTR = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_TokenType_SAML20_PolicyStr");         //NOI18N

    String ISSUED_KEYTYPE_SYMMETRIC = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_KeyType_Symmetric");         //NOI18N
    String ISSUED_KEYTYPE_SYMMETRIC_POLICYSTR = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_KeyType_Symmetric_PolicyStr");         //NOI18N
    String ISSUED_KEYTYPE_PUBLIC = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_KeyType_Public");         //NOI18N
    String ISSUED_KEYTYPE_PUBLIC_POLICYSTR = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_KeyType_Public_PolicyStr");         //NOI18N
    String ISSUED_KEYTYPE_NOPROOF = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_KeyType_NoProof");         //NOI18N
    String ISSUED_KEYTYPE_NOPROOF_POLICYSTR = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_KeyType_NoProof_PolicyStr");         //NOI18N
    String ISSUED_KEYTYPE_NOPROOF13_POLICYSTR = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_KeyType_NoProof13_PolicyStr");         //NOI18N

    String ISSUED_KEYSIZE_128 = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_KeySize_128");         //NOI18N
    String ISSUED_KEYSIZE_192 = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_KeySize_192");         //NOI18N
    String ISSUED_KEYSIZE_256 = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_KeySize_256");         //NOI18N

    String ISSUED_KEYSIZE_1024 = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_KeySize_1024");         //NOI18N
    String ISSUED_KEYSIZE_2048 = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_KeySize_2048");         //NOI18N
    String ISSUED_KEYSIZE_3072 = NbBundle.getMessage(ComboConstants.class, "COMBO_Issued_KeySize_3072");         //NOI18N

    String STATIC = NbBundle.getMessage(ComboConstants.class, "COMBO_Static");         //NOI18N
    String DYNAMIC = NbBundle.getMessage(ComboConstants.class, "COMBO_Dynamic");         //NOI18N
    
    String MEX_NONE = NbBundle.getMessage(ComboConstants.class, "COMBO_MEX_NONE");      //NOI18N
    String MEX_HTTP = NbBundle.getMessage(ComboConstants.class, "COMBO_MEX_HTTP");      //NOI18N
    String MEX_HTTPS = NbBundle.getMessage(ComboConstants.class, "COMBO_MEX_HTTPS");    //NOI18N

    String TRUST_10 = NbBundle.getMessage(ComboConstants.class, "COMBO_TRUST_10");      //NOI18N
    String TRUST_10_POLICYSTR = NbBundle.getMessage(ComboConstants.class, "COMBO_TRUST_10_POLICY");      //NOI18N
    String TRUST_13 = NbBundle.getMessage(ComboConstants.class, "COMBO_TRUST_13");      //NOI18N
    String TRUST_13_POLICYSTR = NbBundle.getMessage(ComboConstants.class, "COMBO_TRUST_13_POLICY");      //NOI18N
}
