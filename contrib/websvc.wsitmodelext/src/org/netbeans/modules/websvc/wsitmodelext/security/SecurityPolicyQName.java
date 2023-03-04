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


package org.netbeans.modules.websvc.wsitmodelext.security;

import java.util.HashMap;
import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsitmodelext.versioning.SchemaLocationProvider;

/**
 *
 * @author Martin Grebac
 */
public enum SecurityPolicyQName {
    TRUST10(createSecurityPolicyQName("Trust10")),                        //NOI18N
    TRUST13(createSecurityPolicyQName("Trust13")),                        //NOI18N
    MUSTSUPPORTISSUEDTOKENS(createSecurityPolicyQName("MustSupportIssuedTokens")),  //NOI18N
    MUSTSUPPORTCLIENTCHALLENGE(createSecurityPolicyQName("MustSupportClientChallenge")),  //NOI18N
    MUSTSUPPORTSERVERCHALLENGE(createSecurityPolicyQName("MustSupportServerChallenge")),  //NOI18N
    REQUIRECLIENTENTROPY(createSecurityPolicyQName("RequireClientEntropy")),  //NOI18N
    REQUIRESERVERENTROPY(createSecurityPolicyQName("RequireServerEntropy")),  //NOI18N

    WSS11(createSecurityPolicyQName("Wss11")),                        //NOI18N
    WSS10(createSecurityPolicyQName("Wss10")),                        //NOI18N
    MUSTSUPPORTREFKEYIDENTIFIER(createSecurityPolicyQName("MustSupportRefKeyIdentifier")),  //NOI18N
    MUSTSUPPORTREFISSUERSERIAL(createSecurityPolicyQName("MustSupportRefIssuerSerial")),  //NOI18N
    MUSTSUPPORTREFTHUMBPRINT(createSecurityPolicyQName("MustSupportRefThumbprint")),  //NOI18N
    MUSTSUPPORTREFENCRYPTEDKEY(createSecurityPolicyQName("MustSupportRefEncryptedKey")),  //NOI18N
    MUSTSUPPORTREFEXTERNALURI(createSecurityPolicyQName("MustSupportRefExternalURI")),  //NOI18N
    MUSTSUPPORTREFEMBEDDEDTOKEN(createSecurityPolicyQName("MustSupportRefEmbeddedToken")),  //NOI18N
    REQUIRESIGNATURECONFIRMATION(createSecurityPolicyQName("RequireSignatureConfirmation")),  //NOI18N
    REQUESTSECURITYTOKENTEMPLATE(createSecurityPolicyQName("RequestSecurityTokenTemplate")),  //NOI18N

    SIGNEDPARTS(createSecurityPolicyQName("SignedParts")),  //NOI18N
    SIGNEDELEMENTS(createSecurityPolicyQName("SignedElements")),  //NOI18N
    ENCRYPTEDPARTS(createSecurityPolicyQName("EncryptedParts")),  //NOI18N
    ENCRYPTEDELEMENTS(createSecurityPolicyQName("EncryptedElements")),  //NOI18N
    REQUIREDELEMENTS(createSecurityPolicyQName("RequiredElements")),  //NOI18N
    XPATH(createSecurityPolicyQName("XPath")),  //NOI18N
    BODY(createSecurityPolicyQName("Body")),  //NOI18N
    ATTACHMENTS(createSecurityPolicyQName("Attachments")),  //NOI18N
    HEADER(createSecurityPolicyQName("Header")),  //NOI18N

    TRANSPORTBINDING(createSecurityPolicyQName("TransportBinding")),  //NOI18N
    SYMMETRICBINDING(createSecurityPolicyQName("SymmetricBinding")),  //NOI18N
    ASYMMETRICBINDING(createSecurityPolicyQName("AsymmetricBinding")),  //NOI18N
    BOOTSTRAPPOLICY(createSecurityPolicyQName("BootstrapPolicy")),  //NOI18N
    
    INCLUDETIMESTAMP(createSecurityPolicyQName("IncludeTimestamp")),  //NOI18N
    ENCRYPTBEFORESIGNING(createSecurityPolicyQName("EncryptBeforeSigning")),  //NOI18N
    ENCRYPTSIGNATURE(createSecurityPolicyQName("EncryptSignature")),  //NOI18N
    PROTECTTOKENS(createSecurityPolicyQName("ProtectTokens")),  //NOI18N
    ONLYSIGNENTIREHEADERSANDBODY(createSecurityPolicyQName("OnlySignEntireHeadersAndBody")),  //NOI18N

    LAYOUT(createSecurityPolicyQName("Layout")),  //NOI18N
    STRICT(createSecurityPolicyQName("Strict")),  //NOI18N
    LAX(createSecurityPolicyQName("Lax")),  //NOI18N
    LAXTSFIRST(createSecurityPolicyQName("LaxTsFirst")),  //NOI18N
    LAXTSLAST(createSecurityPolicyQName("LaxTsLast")),  //NOI18N

    WSSKERBEROSV5APREQTOKEN11(createSecurityPolicyQName("WssKerberosV5ApReqToken11")),  //NOI18N
    WSSGSSKERBEROSV5APREQTOKEN11(createSecurityPolicyQName("WssGssKerberosV5ApReqToken11")),  //NOI18N

    WSSX509V1TOKEN10(createSecurityPolicyQName("WssX509V1Token10")),  //NOI18N
    WSSX509V3TOKEN10(createSecurityPolicyQName("WssX509V3Token10")),  //NOI18N
    WSSX509PKCS7TOKEN10(createSecurityPolicyQName("WssX509Pkcs7Token10")),  //NOI18N
    WSSX509PKIPATHV1TOKEN10(createSecurityPolicyQName("WssX509PkiPathV1Token10")),  //NOI18N
    WSSX509V1TOKEN11(createSecurityPolicyQName("WssX509V1Token11")),  //NOI18N
    WSSX509V3TOKEN11(createSecurityPolicyQName("WssX509V3Token11")),  //NOI18N
    WSSX509PKCS7TOKEN11(createSecurityPolicyQName("WssX509Pkcs7Token11")),  //NOI18N
    WSSX509PKIPATHV1TOKEN11(createSecurityPolicyQName("WssX509PkiPathV1Token11")),  //NOI18N

    REQUIREKEYIDENTIFIERREFERENCE(createSecurityPolicyQName("RequireKeyIdentifierReference")),  //NOI18N
    REQUIREISSUERSERIALREFERENCE(createSecurityPolicyQName("RequireIssuerSerialReference")),  //NOI18N
    REQUIREEMBEDDEDTOKENREFERENCE(createSecurityPolicyQName("RequireEmbeddedTokenReference")),  //NOI18N
    REQUIRETHUMBPRINTREFERENCE(createSecurityPolicyQName("RequireThumbprintReference")),  //NOI18N
    REQUIREEXTERNALURIREFERENCE(createSecurityPolicyQName("RequireExternalUriReference")),  //NOI18N
    SC10SECURITYCONTEXTTOKEN(createSecurityPolicyQName("SC10SecurityContextToken")),  //NOI18N

    REQUIREINTERNALREFERENCE(createSecurityPolicyQName("RequireInternalReference")),  //NOI18N
    REQUIREEXTERNALREFERENCE(createSecurityPolicyQName("RequireExternalReference")),  //NOI18N
    REQUIREDERIVEDKEYS(createSecurityPolicyQName("RequireDerivedKeys")),  //NOI18N
    ISSUER(createSecurityPolicyQName("Issuer")),  //NOI18N
    
    WSSUSERNAMETOKEN10(createSecurityPolicyQName("WssUsernameToken10")),  //NOI18N
    WSSUSERNAMETOKEN11(createSecurityPolicyQName("WssUsernameToken11")),  //NOI18N

    WSSSAMLV10TOKEN10(createSecurityPolicyQName("WssSamlV10Token10")),  //NOI18N
    WSSSAMLV11TOKEN10(createSecurityPolicyQName("WssSamlV11Token10")),  //NOI18N
    WSSSAMLV10TOKEN11(createSecurityPolicyQName("WssSamlV10Token11")),  //NOI18N
    WSSSAMLV11TOKEN11(createSecurityPolicyQName("WssSamlV11Token11")),  //NOI18N
    WSSSAMLV20TOKEN11(createSecurityPolicyQName("WssSamlV20Token11")),  //NOI18N
    
    WSSRELV10TOKEN10(createSecurityPolicyQName("WssRelV10Token10")),  //NOI18N
    WSSRELV20TOKEN10(createSecurityPolicyQName("WssRelV20Token10")),  //NOI18N
    WSSRELV10TOKEN11(createSecurityPolicyQName("WssRelV10Token11")),  //NOI18N
    WSSRELV20TOKEN11(createSecurityPolicyQName("WssRelV20Token11")),  //NOI18N

    HASHPASSWORD(createSecurityPolicyQName("HashPassword")),  //NOI18N
    
    INCLUDETOKENATTRIBUTE(createSecurityPolicyQName("IncludeToken")),  //NOI18N
    
    USERNAMETOKEN(createSecurityPolicyQName("UsernameToken")),  //NOI18N
    X509TOKEN(createSecurityPolicyQName("X509Token")),  //NOI18N
    KERBEROSTOKEN(createSecurityPolicyQName("KerberosToken")),  //NOI18N
    SPNEGOCONTEXTTOKEN(createSecurityPolicyQName("SpnegoContextToken")),  //NOI18N
    SECURITYCONTEXTTOKEN(createSecurityPolicyQName("SecurityContextToken")),  //NOI18N
    SECURECONVERSATIONTOKEN(createSecurityPolicyQName("SecureConversationToken")),  //NOI18N
    PROTECTIONTOKEN(createSecurityPolicyQName("ProtectionToken")),  //NOI18N
    TRANSPORTTOKEN(createSecurityPolicyQName("TransportToken")),  //NOI18N
    SUPPORTINGTOKENS(createSecurityPolicyQName("SupportingTokens")),  //NOI18N
    SIGNEDSUPPORTINGTOKENS(createSecurityPolicyQName("SignedSupportingTokens")),  //NOI18N
    ENDORSINGSUPPORTINGTOKENS(createSecurityPolicyQName("EndorsingSupportingTokens")),  //NOI18N
    SIGNEDENDORSINGSUPPORTINGTOKENS(createSecurityPolicyQName("SignedEndorsingSupportingTokens")),  //NOI18N
    ENCRYPTEDSUPPORTINGTOKENS(createSecurityPolicyQName("EncryptedSupportingTokens")),  //NOI18N
    ENDORSINGENCRYPTEDSUPPORTINGTOKENS(createSecurityPolicyQName("EndorsingEncryptedSupportingTokens")),  //NOI18N
    SIGNEDENCRYPTEDSUPPORTINGTOKENS(createSecurityPolicyQName("SignedEncryptedSupportingTokens")),  //NOI18N
    SIGNEDENDORSINGENCRYPTEDSUPPORTINGTOKENS(createSecurityPolicyQName("SignedEndorsingEncryptedSupportingTokens")),  //NOI18N
    SIGNATURETOKEN(createSecurityPolicyQName("SignatureToken")),  //NOI18N
    ENCRYPTIONTOKEN(createSecurityPolicyQName("EncryptionToken")),  //NOI18N
    INITIATORTOKEN(createSecurityPolicyQName("InitiatorToken")),  //NOI18N
    RECIPIENTTOKEN(createSecurityPolicyQName("RecipientToken")),  //NOI18N
    SAMLTOKEN(createSecurityPolicyQName("SamlToken")),  //NOI18N
    RELTOKEN(createSecurityPolicyQName("RelToken")),  //NOI18N
    HTTPSTOKEN(createSecurityPolicyQName("HttpsToken")),  //NOI18N
    ISSUEDTOKEN(createSecurityPolicyQName("IssuedToken")),  //NOI18N
    
    ALGORITHMSUITE(createSecurityPolicyQName("AlgorithmSuite")),  //NOI18N
    BASIC256(createSecurityPolicyQName("Basic256")),  //NOI18N
    BASIC192(createSecurityPolicyQName("Basic192")),  //NOI18N
    BASIC128(createSecurityPolicyQName("Basic128")),  //NOI18N
    TRIPLEDES(createSecurityPolicyQName("TripleDes")),  //NOI18N
    BASIC256RSA15(createSecurityPolicyQName("Basic256Rsa15")),  //NOI18N
    BASIC192RSA15(createSecurityPolicyQName("Basic192Rsa15")),  //NOI18N
    BASIC128RSA15(createSecurityPolicyQName("Basic128Rsa15")),  //NOI18N
    TRIPLEDESRSA15(createSecurityPolicyQName("TripleDesRsa15")),  //NOI18N
    BASIC256SHA256(createSecurityPolicyQName("Basic256Sha256")),  //NOI18N
    BASIC192SHA256(createSecurityPolicyQName("Basic192Sha256")),  //NOI18N
    BASIC128SHA256(createSecurityPolicyQName("Basic128Sha256")),  //NOI18N
    TRIPLEDESSHA256(createSecurityPolicyQName("TripleDesSha256")),  //NOI18N
    BASIC256SHA256RSA15(createSecurityPolicyQName("Basic256Sha256Rsa15")),  //NOI18N
    BASIC192SHA256RSA15(createSecurityPolicyQName("Basic192Sha256Rsa15")),  //NOI18N
    BASIC128SHA256RSA15(createSecurityPolicyQName("Basic128Sha256Rsa15")),  //NOI18N
    TRIPLEDESSHA256RSA15(createSecurityPolicyQName("TripleDesSha256Rsa15")),  //NOI18N
    INCLUSIVEC14N(createSecurityPolicyQName("InclusiveC14N")),  //NOI18N
    SOAPNORMALIZATION10(createSecurityPolicyQName("SOAPNormalization10")),  //NOI18N
    STRTRANSFORM10(createSecurityPolicyQName("STRTransform10")),  //NOI18N
    XPATH10(createSecurityPolicyQName("XPath10")),  //NOI18N
    XPATHFILTER20(createSecurityPolicyQName("XPathFilter20"));  //NOI18N
    
    public static final String SECPOLICY_NS_PREFIX = "sp";         //NOI18N

    public static final String SECPOLICY_NS = 
            "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy"; //NOI18N
    public static final String SECPOLICY_NS_EXT =
            "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/ws-securitypolicy.xsd"; //NOI18N
    public static final String SECPOLICY_NS_LOCAL =
            "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/ws-securitypolicy.xsd"; //NOI18N

    public static final String SECPOLICY_13_NS =
            "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702"; //NOI18N
    public static final String SECPOLICY_13_NS_EXT =
            "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702/ws-securitypolicy-1.2.xsd"; //NOI18N
    public static final String SECPOLICY_13_NS_LOCAL =
            "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/ws-securitypolicy-1.2.xsd"; //NOI18N
            
    public static QName createSecurityPolicyQName(String localName){
        return new QName(SECPOLICY_NS, localName, SECPOLICY_NS_PREFIX);
    }
    
    SecurityPolicyQName(QName name) {
        qName = name;
    }
    
    public QName getQName(ConfigVersion cfgVersion) {
        return new QName(getNamespaceUri(cfgVersion), qName.getLocalPart(), qName.getPrefix());
    }

    public static String getNamespaceUri(ConfigVersion cfgVersion) {
        switch (cfgVersion) {
            case CONFIG_1_0 : return SECPOLICY_NS;
            case CONFIG_1_3 :
            case CONFIG_2_0 : return SECPOLICY_13_NS;
        }
        return null;
    }

    public static ConfigVersion getConfigVersion(QName q) {
        for (ConfigVersion cfgVersion : ConfigVersion.values()) {
            if (getQNames(cfgVersion).contains(q)) {
                return cfgVersion;
            }
        }
        return null;
    }
    
    public static Set<QName> getQNames(ConfigVersion cfgVersion) {
        Set<QName> qnames = new HashSet<QName>();
        for (SecurityPolicyQName wq : values()) {
            qnames.add(wq.getQName(cfgVersion));
        }
        
        // some assertions are not present in new namespaces ?
        
        return qnames;
    }    
    private final QName qName;

    public Map<String, String> getSchemaLocations(boolean local) {
        HashMap<String, String> hmap = new HashMap<String, String>();
        for (ConfigVersion cfg : ConfigVersion.values()) {
            try {
                String nsUri = getNamespaceUri(cfg);
                if (nsUri != null) {
                    hmap.put(nsUri, getSchemaLocation(nsUri, local));
                }
            } catch (IllegalArgumentException iae) {
                // ignore - just skip this
            }
        }
        return hmap;
    }

    public String getSchemaLocation(String namespace, boolean local) {
        if (SECPOLICY_NS.equals(namespace)) {
            return local ? SECPOLICY_NS_LOCAL : SECPOLICY_NS_EXT;
        }
        if (SECPOLICY_13_NS.equals(namespace)) {
            return local ? SECPOLICY_13_NS_LOCAL : SECPOLICY_13_NS_EXT;
        }
        return null;
    }

}
