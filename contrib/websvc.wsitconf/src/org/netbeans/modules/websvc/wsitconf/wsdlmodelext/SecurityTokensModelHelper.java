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

import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Address10;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10Metadata;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10QName;
import org.netbeans.modules.websvc.wsitmodelext.mex.Metadata;
import org.netbeans.modules.websvc.wsitmodelext.mex.MetadataReference;
import org.netbeans.modules.websvc.wsitmodelext.mex.MetadataSection;
import org.netbeans.modules.websvc.wsitmodelext.mex.MexQName;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.RequestSecurityTokenTemplate;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.EncryptedSupportingTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.TransportToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssGssKerberosV5ApReqToken11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssKerberosV5ApReqToken11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssSamlV10Token10;
import org.netbeans.modules.websvc.wsitmodelext.trust.KeySize;
import org.netbeans.modules.websvc.wsitmodelext.trust.KeyType;
import org.netbeans.modules.websvc.wsitmodelext.trust.TokenType;
import org.netbeans.modules.websvc.wsitmodelext.trust.TrustQName;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.EncryptionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.EndorsingEncryptedSupportingTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.EndorsingSupportingTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.HashPassword;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.HttpsToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.InitiatorToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.IssuedToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.Issuer;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.KerberosToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RecipientToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RelToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SamlToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecurityContextToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SignatureToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SignedEncryptedSupportingTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SignedEndorsingEncryptedSupportingTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SignedEndorsingSupportingTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SignedSupportingTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SupportingTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.UsernameToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssSamlV10Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssSamlV11Token10;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssSamlV11Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssSamlV20Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssUsernameToken10;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssUsernameToken11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509Pkcs7Token10;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509Pkcs7Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509PkiPathV1Token10;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509PkiPathV1Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509V1Token10;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509V1Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509V3Token10;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509V3Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.X509Token;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class SecurityTokensModelHelper {

    public static final int SUPPORTING = 0;     
    public static final int SIGNED_SUPPORTING = 1;
    public static final int ENDORSING = 2;
    public static final int SIGNED_ENDORSING = 3;
    public static final int ENCRYPTED = 4;
    public static final int SIGNED_ENCRYPTED = 5;
    public static final int ENDORSING_ENCRYPTED = 6;
    public static final int SIGNED_ENDORSING_ENCRYPTED = 7;

    public static final int NONE = 100;
    
    public static Class[] SUPPORTING_TOKENS = { SupportingTokens.class, SignedSupportingTokens.class, 
                  EndorsingSupportingTokens.class, SignedEndorsingSupportingTokens.class,
                  EncryptedSupportingTokens.class, SignedEncryptedSupportingTokens.class, 
                  EndorsingEncryptedSupportingTokens.class, SignedEndorsingEncryptedSupportingTokens.class};    
    
    private static HashMap<ConfigVersion, SecurityTokensModelHelper> instances = 
            new HashMap<ConfigVersion, SecurityTokensModelHelper>();

    private ConfigVersion configVersion = ConfigVersion.getDefault();
    
    /**
     * Creates a new instance of SecurityTokensModelHelper
     */
    private SecurityTokensModelHelper(ConfigVersion configVersion) {
        this.configVersion = configVersion;
    }

    public static final synchronized SecurityTokensModelHelper getInstance(ConfigVersion configVersion) {
        SecurityTokensModelHelper instance = instances.get(configVersion);
        if (instance == null) {
            instance = new SecurityTokensModelHelper(configVersion);
            instances.put(configVersion, instance);
        }
        return instance;
    }

    public static boolean isRequireClientCertificate(HttpsToken token) {
        return token.isRequireClientCertificate();
    }

    public static void setRequireClientCertificate(HttpsToken token, boolean require) {
        WSDLModel model = token.getModel();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            token.setRequireClientCertificate(require);
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }

    public static String getTokenType(WSDLComponent tokenKind) {
        if (tokenKind != null) {
            WSDLComponent wc = null;
            wc = getTokenElement(tokenKind, UsernameToken.class);
            if (wc != null) return ComboConstants.USERNAME;
            wc = getTokenElement(tokenKind, X509Token.class);
            if (wc != null) return ComboConstants.X509;
            wc = getTokenElement(tokenKind, SamlToken.class);
            if (wc != null) return ComboConstants.SAML;
            wc = getTokenElement(tokenKind, RelToken.class);
            if (wc != null) return ComboConstants.REL;
            wc = getTokenElement(tokenKind, KerberosToken.class);
            if (wc != null) return ComboConstants.KERBEROS;
            wc = getTokenElement(tokenKind, SecurityContextToken.class);
            if (wc != null) return ComboConstants.SECURITYCONTEXT;
            wc = getTokenElement(tokenKind, SecureConversationToken.class);
            if (wc != null) return ComboConstants.SECURECONVERSATION;
            wc = getTokenElement(tokenKind, IssuedToken.class);
            if (wc != null) return ComboConstants.ISSUED;
        }
        return null;
    }

    public static WSDLComponent getTokenTypeElement(WSDLComponent tokenKind) {
        if (tokenKind == null) return null;
        WSDLComponent wc = null;
        wc = getTokenElement(tokenKind, HttpsToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, UsernameToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, X509Token.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, SamlToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, RelToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, KerberosToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, SecurityContextToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, SecureConversationToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, IssuedToken.class);
        return wc;
    }
    
    public static boolean isHashPassword(WSDLComponent tokenType) {
        if (tokenType instanceof UsernameToken) {
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, HashPassword.class)) {
                return true;
            }
        }
        return false;
    }
    
    public static String getTokenProfileVersion(WSDLComponent tokenType) {
        if (tokenType instanceof UsernameToken) {
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssUsernameToken10.class)) {
                return ComboConstants.WSS10;
            }
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssUsernameToken11.class)) {
                return ComboConstants.WSS11;
            }
        }
        if (tokenType instanceof SamlToken) {
            ExtensibilityElement e = (ExtensibilityElement) tokenType;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssSamlV10Token10.class)) return ComboConstants.SAML_V1010;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssSamlV10Token11.class)) return ComboConstants.SAML_V1011;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssSamlV11Token10.class)) return ComboConstants.SAML_V1110;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssSamlV11Token11.class)) return ComboConstants.SAML_V1111;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssSamlV20Token11.class)) return ComboConstants.SAML_V2011;
        }
        if (tokenType instanceof X509Token) {
            ExtensibilityElement e = (ExtensibilityElement) tokenType;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509V1Token10.class)) return ComboConstants.X509_V110;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509V1Token11.class)) return ComboConstants.X509_V111;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509V3Token10.class)) return ComboConstants.X509_V310;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509V3Token11.class)) return ComboConstants.X509_V311;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509Pkcs7Token10.class)) return ComboConstants.X509_PKCS710;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509Pkcs7Token11.class)) return ComboConstants.X509_PKCS711;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509PkiPathV1Token10.class)) return ComboConstants.X509_PKIPATHV110;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509PkiPathV1Token11.class)) return ComboConstants.X509_PKIPATHV111;
        }
        return ComboConstants.NONE;
    }

    public static WSDLComponent getTokenElement(WSDLComponent e, Class tokenClass) {
        if (e == null) return null;
        List<Policy> policies = e.getExtensibilityElements(Policy.class);
        if ((policies != null) && (!policies.isEmpty())) {
            Policy p = policies.get(0);
            return PolicyModelHelper.getTopLevelElement(p, tokenClass,false);
        }
        return null;
    }
    
    public WSDLComponent setTokenType(WSDLComponent secBinding, String tokenKindStr, String tokenTypeStr) {
        WSDLModel model = secBinding.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        WSDLComponent tokenType = null;
        WSDLComponent tokenKind = null;

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            Policy p = pmh.createElement(secBinding, PolicyQName.POLICY.getQName(configVersion), Policy.class, false);
            List<ExtensibilityElement> tokenKinds = p.getExtensibilityElements();
            if ((tokenKinds != null) && (!tokenKinds.isEmpty())) {
                for (ExtensibilityElement tkind : tokenKinds) {
                    if (ComboConstants.PROTECTION.equals(tokenKindStr) || 
                        ComboConstants.TRANSPORT.equals(tokenKindStr)) {
                        if (tkind instanceof SignatureToken ||
                            tkind instanceof TransportToken ||
                            tkind instanceof EncryptionToken ||
                            tkind instanceof InitiatorToken ||
                            tkind instanceof ProtectionToken ||
                            tkind instanceof RecipientToken) {
                                p.removeExtensibilityElement(tkind);
                        }
                    } else if (ComboConstants.ENCRYPTION.equals(tokenKindStr)) {
                        if (!(tkind instanceof SignatureToken)) {
                            p.removeExtensibilityElement(tkind);
                        }
                    } else if (ComboConstants.SIGNATURE.equals(tokenKindStr)) {
                        if (!(tkind instanceof EncryptionToken)) {
                            p.removeExtensibilityElement(tkind);
                        }
                    } else if (ComboConstants.INITIATOR.equals(tokenKindStr)) {
                        if (!(tkind instanceof RecipientToken)) {
                            p.removeExtensibilityElement(tkind);
                        }
                    } else if (ComboConstants.RECIPIENT.equals(tokenKindStr)) {
                        if (!(tkind instanceof InitiatorToken)) {
                            p.removeExtensibilityElement(tkind);
                        }
                    }
                }
            }
            
            if (ComboConstants.PROTECTION.equals(tokenKindStr)) {
                tokenKind = wcf.create(p, SecurityPolicyQName.PROTECTIONTOKEN.getQName(configVersion));
            }
            if (ComboConstants.SIGNATURE.equals(tokenKindStr)) {
                tokenKind = wcf.create(p, SecurityPolicyQName.SIGNATURETOKEN.getQName(configVersion));
            }
            if (ComboConstants.ENCRYPTION.equals(tokenKindStr)) {
                tokenKind = wcf.create(p, SecurityPolicyQName.ENCRYPTIONTOKEN.getQName(configVersion));
            }
            if (ComboConstants.INITIATOR.equals(tokenKindStr)) {
                tokenKind = wcf.create(p, SecurityPolicyQName.INITIATORTOKEN.getQName(configVersion));
            }
            if (ComboConstants.RECIPIENT.equals(tokenKindStr)) {
                tokenKind = wcf.create(p, SecurityPolicyQName.RECIPIENTTOKEN.getQName(configVersion));
            }
            if (ComboConstants.TRANSPORT.equals(tokenKindStr)) {
                tokenKind = wcf.create(p, SecurityPolicyQName.TRANSPORTTOKEN.getQName(configVersion));
            }

            p.addExtensibilityElement((ExtensibilityElement) tokenKind);

            Policy pinner = (Policy) wcf.create(tokenKind, PolicyQName.POLICY.getQName(configVersion));
            tokenKind.addExtensibilityElement(pinner);

            SecurityPolicyModelHelper spmh = SecurityPolicyModelHelper.getInstance(configVersion);
            if (ComboConstants.HTTPS.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, SecurityPolicyQName.HTTPSTOKEN.getQName(configVersion));
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                ((HttpsToken)tokenType).setRequireClientCertificate(false);
            }
            if (ComboConstants.X509.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, SecurityPolicyQName.X509TOKEN.getQName(configVersion));
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, ComboConstants.X509_V310);
                SecurityPolicyModelHelper.getInstance(configVersion).enableRequireIssuerSerialReference(tokenType, true);
            }
            if (ComboConstants.SAML.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, SecurityPolicyQName.SAMLTOKEN.getQName(configVersion));
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, ComboConstants.SAML_V1110);
            }
            if (ComboConstants.KERBEROS.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, SecurityPolicyQName.KERBEROSTOKEN.getQName(configVersion));
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, ComboConstants.KERBEROS_KERBEROSGSS);
            }
            if (ComboConstants.ISSUED.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, SecurityPolicyQName.ISSUEDTOKEN.getQName(configVersion));
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                RequestSecurityTokenTemplate template = 
                        (RequestSecurityTokenTemplate) wcf.create(tokenType, SecurityPolicyQName.REQUESTSECURITYTOKENTEMPLATE.getQName(configVersion));
                tokenType.addExtensibilityElement(template);

                TokenType trustTokenType = (TokenType) wcf.create(template, TrustQName.TOKENTYPE.getQName(configVersion));
                template.addExtensibilityElement(trustTokenType);
                trustTokenType.setContent(ComboConstants.ISSUED_TOKENTYPE_SAML11_POLICYSTR);

                KeyType trustKeyType = (KeyType) wcf.create(template, TrustQName.KEYTYPE.getQName(configVersion));
                template.addExtensibilityElement(trustKeyType);
                String nsStart = TrustQName.getNamespaceUri(configVersion);
                trustKeyType.setContent(nsStart + ComboConstants.ISSUED_KEYTYPE_SYMMETRIC_POLICYSTR);

                KeySize trustKeySize = (KeySize) wcf.create(template, TrustQName.KEYSIZE.getQName(configVersion));
                template.addExtensibilityElement(trustKeySize);
                trustKeySize.setContent(ComboConstants.ISSUED_KEYSIZE_256);
                
                spmh.enableRequireInternalReference(tokenType, true);
            }
            
            if (ComboConstants.USERNAME.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, SecurityPolicyQName.USERNAMETOKEN.getQName(configVersion));
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, ComboConstants.WSS10);
            }
            if (ComboConstants.REL.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, SecurityPolicyQName.RELTOKEN.getQName(configVersion));
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                spmh.enableRequireDerivedKeys(tokenType, true);
            }
            if (ComboConstants.SECURECONVERSATION.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, SecurityPolicyQName.SECURECONVERSATIONTOKEN.getQName(configVersion));
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                spmh.enableRequireDerivedKeys(tokenType, true);
//                setBootstrapPolicy(tokenType, 
//                        ComboConstants.SYMMETRIC, 
//                        ComboConstants.X509, 
//                        ComboConstants.X509,
//                        ComboConstants.WSS10);
            }
            if (ComboConstants.SECURITYCONTEXT.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, SecurityPolicyQName.SECURITYCONTEXTTOKEN.getQName(configVersion));
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                spmh.enableRequireDerivedKeys(tokenType, true);
            }
            if (ComboConstants.SPNEGOCONTEXT.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, SecurityPolicyQName.SPNEGOCONTEXTTOKEN.getQName(configVersion));
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                spmh.enableRequireDerivedKeys(tokenType, true);
            }

//            setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);

        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
        return tokenType;
    }

    void setTokenInclusionLevel(WSDLComponent tokenType, String incLevel) {
        WSDLModel model = tokenType.getModel();
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            String nsStart = SecurityPolicyQName.getNamespaceUri(configVersion);
            String levelStr = nsStart;
            if (ComboConstants.NEVER.equals(incLevel)) {
                levelStr += ComboConstants.NEVER_POLICYSTR;
            } else if (ComboConstants.ALWAYS.equals(incLevel)) {
                levelStr += ComboConstants.ALWAYS_POLICYSTR;
            } else if (ComboConstants.ALWAYSRECIPIENT.equals(incLevel)) {
                levelStr += ComboConstants.ALWAYSRECIPIENT_POLICYSTR;
            } else if (ComboConstants.ONCE.equals(incLevel)) {
                levelStr += ComboConstants.ONCE_POLICYSTR;
            }
            ((ExtensibilityElement)tokenType).setAnyAttribute(SecurityPolicyQName.INCLUDETOKENATTRIBUTE.getQName(configVersion), levelStr);
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
   }

    public void setHashPassword(WSDLComponent tokenType, boolean enable) {
        
        WSDLModel model = tokenType.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {            
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            Policy p = pmh.createElement(tokenType, PolicyQName.POLICY.getQName(configVersion), Policy.class, false);
            List<ExtensibilityElement> tokenAssertions = p.getExtensibilityElements();
            
            if (tokenType instanceof UsernameToken) {
                if ((tokenAssertions != null) && (!tokenAssertions.isEmpty())) {
                    for (ExtensibilityElement e : tokenAssertions) {
                        if (e instanceof HashPassword) {                     
                             p.removeExtensibilityElement(e);
                        }
                    }
                }
                
                if (enable) {
                    WSDLComponent wc = wcf.create(p, SecurityPolicyQName.HASHPASSWORD.getQName(configVersion));
                    p.addExtensibilityElement((ExtensibilityElement) wc);
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }
    
    public void setTokenProfileVersion(WSDLComponent tokenType, String profileVersion) {
        WSDLModel model = tokenType.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            Policy p = pmh.createElement(tokenType, PolicyQName.POLICY.getQName(configVersion), Policy.class, false);
            WSDLComponent profileVersionAssertion = null;
            List<ExtensibilityElement> tokenAssertions = p.getExtensibilityElements();
            
            if (tokenType instanceof UsernameToken) {
                if ((tokenAssertions != null) && (!tokenAssertions.isEmpty())) {
                    for (ExtensibilityElement e : tokenAssertions) {
                        if ((e instanceof WssUsernameToken10) ||
                            (e instanceof WssUsernameToken11)) {                     
                                p.removeExtensibilityElement(e);
                        }
                    }
                }
                if (ComboConstants.WSS10.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSUSERNAMETOKEN10.getQName(configVersion));
                if (ComboConstants.WSS11.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSUSERNAMETOKEN11.getQName(configVersion));
            }
            if (tokenType instanceof SamlToken) {
                if ((tokenAssertions != null) && (!tokenAssertions.isEmpty())) {
                    for (ExtensibilityElement e : tokenAssertions) {
                        if ((e instanceof WssSamlV10Token11) ||
                            (e instanceof WssSamlV10Token10) || 
                            (e instanceof WssSamlV11Token10) || 
                            (e instanceof WssSamlV11Token11) || 
                            (e instanceof WssSamlV20Token11)) {                     
                                p.removeExtensibilityElement(e);
                        }
                    }
                }
                if (ComboConstants.SAML_V1010.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSSAMLV10TOKEN10.getQName(configVersion));
                if (ComboConstants.SAML_V1011.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSSAMLV10TOKEN11.getQName(configVersion));
                if (ComboConstants.SAML_V1110.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSSAMLV11TOKEN10.getQName(configVersion));
                if (ComboConstants.SAML_V1111.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSSAMLV11TOKEN11.getQName(configVersion));
                if (ComboConstants.SAML_V2011.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSSAMLV20TOKEN11.getQName(configVersion));
            }

            if (tokenType instanceof X509Token) {
                if ((tokenAssertions != null) && (!tokenAssertions.isEmpty())) {
                    for (ExtensibilityElement e : tokenAssertions) {
                        if ((e instanceof WssX509V1Token10) ||
                            (e instanceof WssX509V3Token10) || 
                            (e instanceof WssX509V1Token11) || 
                            (e instanceof WssX509V3Token11) || 
                            (e instanceof WssX509Pkcs7Token10) || 
                            (e instanceof WssX509Pkcs7Token11) || 
                            (e instanceof WssX509PkiPathV1Token10) || 
                            (e instanceof WssX509PkiPathV1Token11)) {                     
                                p.removeExtensibilityElement(e);
                        }
                    }
                }

                if (ComboConstants.X509_V110.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSX509V1TOKEN10.getQName(configVersion));
                if (ComboConstants.X509_V310.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSX509V3TOKEN10.getQName(configVersion));
                if (ComboConstants.X509_V111.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSX509V1TOKEN11.getQName(configVersion));
                if (ComboConstants.X509_V311.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSX509V3TOKEN11.getQName(configVersion));
                if (ComboConstants.X509_PKCS710.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSX509PKCS7TOKEN10.getQName(configVersion));
                if (ComboConstants.X509_PKCS711.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSX509PKCS7TOKEN11.getQName(configVersion));
                if (ComboConstants.X509_PKIPATHV110.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSX509PKIPATHV1TOKEN10.getQName(configVersion));
                if (ComboConstants.X509_PKIPATHV111.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSX509PKIPATHV1TOKEN11.getQName(configVersion));
            }
            
            if (tokenType instanceof KerberosToken) {
                if ((tokenAssertions != null) && (!tokenAssertions.isEmpty())) {
                    for (ExtensibilityElement e : tokenAssertions) {
                        if ((e instanceof WssGssKerberosV5ApReqToken11) ||
                            (e instanceof WssKerberosV5ApReqToken11)) {                     
                                p.removeExtensibilityElement(e);
                        }
                    }
                }
                if (ComboConstants.KERBEROS_KERBEROS.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSKERBEROSV5APREQTOKEN11.getQName(configVersion));
                if (ComboConstants.KERBEROS_KERBEROSGSS.equals(profileVersion)) profileVersionAssertion = wcf.create(p, SecurityPolicyQName.WSSGSSKERBEROSV5APREQTOKEN11.getQName(configVersion));
            }

            if (profileVersionAssertion != null) p.addExtensibilityElement((ExtensibilityElement) profileVersionAssertion);
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }

    public static List<WSDLComponent> getSupportingTokens(WSDLComponent c) {
        ArrayList<WSDLComponent> result = new ArrayList<WSDLComponent>(1);
        if (c == null) return null;
        WSDLComponent p = c;
        if ((c instanceof Binding) || (c instanceof BindingOperation) || 
           (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
             p = PolicyModelHelper.getPolicyForElement(c);
        }
        if (p == null) return null;
        for (Class cl : SUPPORTING_TOKENS) {
            WSDLComponent token = PolicyModelHelper.getTopLevelElement(p, cl,false);
            if (token != null) {
                result.add(token);
            }
        }
        return result;
    }
    
    public static WSDLComponent getSupportingToken(WSDLComponent c, int supportingType) {
        if (c == null) return null;
        WSDLComponent p = c;
        if ((c instanceof Binding) || (c instanceof BindingOperation) || 
           (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
             p = PolicyModelHelper.getPolicyForElement(c);
        }
        if (p == null) return null;
        
        return PolicyModelHelper.getTopLevelElement(p, SUPPORTING_TOKENS[supportingType], false);
    }

    public static void removeSupportingTokens(WSDLComponent c) {
        if (c == null) return;
        WSDLModel model = c.getModel();
        WSDLComponent p = c;
        if ((c instanceof Binding) || (c instanceof BindingOperation) || 
           (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
             p = PolicyModelHelper.getPolicyForElement(c);
        }

        if (p == null) return;
        
        ExtensibilityElement rem = null;

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            for (Class cl : SUPPORTING_TOKENS) {
                rem = PolicyModelHelper.getTopLevelElement(p, cl,false);
                if (rem != null) {
                    rem.getParent().removeExtensibilityElement(rem);
                }
            }
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }
    
    public WSDLComponent setSupportingTokens(WSDLComponent c, String authToken, int supportingType) {
        if (c == null) return null;
        
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        WSDLComponent tokenType = null;
        WSDLComponent tokenKind = null;

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            for (int i=0; i < SecurityTokensModelHelper.SUPPORTING_TOKENS.length; i++) {
                tokenKind = getSupportingToken(c, i);
                if (tokenKind != null) {
                    if (ComboConstants.NONE.equals(authToken) || (authToken == null)) { 
                        if ((i == supportingType) || (supportingType == NONE)) {
                            tokenKind.getParent().removeExtensibilityElement((ExtensibilityElement) tokenKind);
                        }
                        if (supportingType != NONE) return null;
                    } else {
                        if (i == supportingType) {
                            tokenKind.getParent().removeExtensibilityElement((ExtensibilityElement) tokenKind);
                        }
                    }
                }
            }
            
            if (supportingType == NONE) return null;
            
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            WSDLComponent topLevel = null;
            if (c instanceof Policy) {
                topLevel = c;
            } else {
                topLevel = pmh.createPolicy(c, true);
            }
        
            if (SUPPORTING == supportingType) {
                tokenKind = wcf.create(topLevel, SecurityPolicyQName.SUPPORTINGTOKENS.getQName(configVersion));
            }
            if (SIGNED_SUPPORTING == supportingType) {
                tokenKind = wcf.create(topLevel, SecurityPolicyQName.SIGNEDSUPPORTINGTOKENS.getQName(configVersion));
            }
            if (ENDORSING == supportingType) {
                tokenKind = wcf.create(topLevel, SecurityPolicyQName.ENDORSINGSUPPORTINGTOKENS.getQName(configVersion));
            }
            if (SIGNED_ENDORSING == supportingType) {
                tokenKind = wcf.create(topLevel, SecurityPolicyQName.SIGNEDENDORSINGSUPPORTINGTOKENS.getQName(configVersion));
            }
            if (ENCRYPTED == supportingType) {
                tokenKind = wcf.create(topLevel, SecurityPolicyQName.ENCRYPTEDSUPPORTINGTOKENS.getQName(configVersion));
            }
            if (SIGNED_ENCRYPTED == supportingType) {
                tokenKind = wcf.create(topLevel, SecurityPolicyQName.SIGNEDENCRYPTEDSUPPORTINGTOKENS.getQName(configVersion));
            }
            if (SIGNED_ENDORSING_ENCRYPTED == supportingType) {
                tokenKind = wcf.create(topLevel, SecurityPolicyQName.SIGNEDENDORSINGENCRYPTEDSUPPORTINGTOKENS.getQName(configVersion));
            }
            if (ENDORSING_ENCRYPTED == supportingType) {
                tokenKind = wcf.create(topLevel, SecurityPolicyQName.ENDORSINGENCRYPTEDSUPPORTINGTOKENS.getQName(configVersion));
            }
            topLevel.addExtensibilityElement((ExtensibilityElement) tokenKind);

            if (ComboConstants.USERNAME.equals(authToken)) {
                tokenType = pmh.createElement(tokenKind, SecurityPolicyQName.USERNAMETOKEN.getQName(configVersion), UsernameToken.class, true);
                setTokenProfileVersion(tokenType, ComboConstants.WSS10);
                setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
            }
            if (ComboConstants.X509.equals(authToken)) {
                tokenType = pmh.createElement(tokenKind, SecurityPolicyQName.X509TOKEN.getQName(configVersion), X509Token.class, true);
                setTokenProfileVersion(tokenType, ComboConstants.X509_V310);
//                SecurityPolicyModelHelper.enableRequireThumbprintReference(tokenType, true);
                setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
            }
            if (ComboConstants.SAML.equals(authToken)) {
                tokenType = pmh.createElement(tokenKind, SecurityPolicyQName.SAMLTOKEN.getQName(configVersion), SamlToken.class, true);
                setTokenProfileVersion(tokenType, ComboConstants.SAML_V1110);
                setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
            }
            if (ComboConstants.SECURECONVERSATION.equals(authToken)) {
                tokenType = pmh.createElement(tokenKind, SecurityPolicyQName.SECURECONVERSATIONTOKEN.getQName(configVersion), SecureConversationToken.class, true);
                setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
            }
            if (ComboConstants.ISSUED.equals(authToken)) {
                tokenType = pmh.createElement(tokenKind, SecurityPolicyQName.ISSUEDTOKEN.getQName(configVersion), IssuedToken.class, true);
                setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);

                RequestSecurityTokenTemplate template = 
                        (RequestSecurityTokenTemplate) wcf.create(tokenType, SecurityPolicyQName.REQUESTSECURITYTOKENTEMPLATE.getQName(configVersion));
                tokenType.addExtensibilityElement(template);
                
                TokenType trustTokenType = pmh.createElement(template, TrustQName.TOKENTYPE.getQName(configVersion), TokenType.class, false);
                trustTokenType.setContent(ComboConstants.ISSUED_TOKENTYPE_SAML11_POLICYSTR);
                
                KeyType trustKeyType = pmh.createElement(template, TrustQName.KEYTYPE.getQName(configVersion), KeyType.class, false);
                String nsStart = TrustQName.getNamespaceUri(configVersion);
                trustKeyType.setContent(nsStart + ComboConstants.ISSUED_KEYTYPE_SYMMETRIC_POLICYSTR);

                KeySize trustKeySize = pmh.createElement(template, TrustQName.KEYSIZE.getQName(configVersion), KeySize.class, false);
                trustKeySize.setContent(ComboConstants.ISSUED_KEYSIZE_256);

                SecurityPolicyModelHelper.getInstance(configVersion).enableRequireInternalReference(tokenType, true);            
            }
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
        return tokenType;
    }    
    
    public static String getIssuedIssuerAddress(WSDLComponent tokenType) {
        if (tokenType != null) {
            List<Issuer> issuerList = tokenType.getExtensibilityElements(Issuer.class);
            if ((issuerList != null) && (!issuerList.isEmpty())) {
                Issuer issuer = issuerList.get(0);
                List<Address10> addrs = issuer.getExtensibilityElements(Address10.class);
                if ((addrs != null) && (!addrs.isEmpty())) {
                    Address10 a = addrs.get(0);
                    if (a != null) {
                        return a.getAddress();
                    }
                }
            }
        }
        return null;
    }

    public static String getIssuedTokenType(WSDLComponent tokenType) {
        if (tokenType != null) {
            List<RequestSecurityTokenTemplate> rstList = tokenType.getExtensibilityElements(RequestSecurityTokenTemplate.class);
            if ((rstList != null) && (!rstList.isEmpty())) {
                RequestSecurityTokenTemplate rst = rstList.get(0);
                TokenType tType = rst.getTokenType();
                if (tType != null) {
                    String type = tType.getContent();
                    if (ComboConstants.ISSUED_TOKENTYPE_SAML10_POLICYSTR.equals(type)) {
                        return ComboConstants.ISSUED_TOKENTYPE_SAML10;
                    }
                    if (ComboConstants.ISSUED_TOKENTYPE_SAML11_POLICYSTR.equals(type)) {
                        return ComboConstants.ISSUED_TOKENTYPE_SAML11;
                    }
                    if (ComboConstants.ISSUED_TOKENTYPE_SAML20_POLICYSTR.equals(type)) {
                        return ComboConstants.ISSUED_TOKENTYPE_SAML20;
                    }   
                }
            }
        }
        return null;
    }

    public static String getIssuedKeyType(WSDLComponent tokenType) {
        if (tokenType != null) {
            List<RequestSecurityTokenTemplate> rstList = tokenType.getExtensibilityElements(RequestSecurityTokenTemplate.class);
            if ((rstList != null) && (!rstList.isEmpty())) {
                RequestSecurityTokenTemplate rst = rstList.get(0);
                KeyType kType = rst.getKeyType();
                if (kType != null) {
                    String type = kType.getContent();
                    if (type!= null) {
                        if (type.endsWith(ComboConstants.ISSUED_KEYTYPE_PUBLIC_POLICYSTR)) {
                            return ComboConstants.ISSUED_KEYTYPE_PUBLIC;
                        }
                        if (type.endsWith(ComboConstants.ISSUED_KEYTYPE_SYMMETRIC_POLICYSTR)) {
                            return ComboConstants.ISSUED_KEYTYPE_SYMMETRIC;
                        }
                        if (type.endsWith(ComboConstants.ISSUED_KEYTYPE_NOPROOF_POLICYSTR) ||
                            type.endsWith(ComboConstants.ISSUED_KEYTYPE_NOPROOF13_POLICYSTR)) {
                            return ComboConstants.ISSUED_KEYTYPE_NOPROOF;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String getIssuedKeySize(WSDLComponent tokenType) {
        if (tokenType != null) {
            List<RequestSecurityTokenTemplate> rstList = tokenType.getExtensibilityElements(RequestSecurityTokenTemplate.class);
            if ((rstList != null) && (!rstList.isEmpty())) {
                RequestSecurityTokenTemplate rst = rstList.get(0);
                KeySize kSize = rst.getKeySize();
                if (kSize != null) {
                    return kSize.getContent();
                }
            }
        }
        return null;
    }
    
    public static String getIssuedIssuerMetadataAddress(WSDLComponent tokenType) {
        if (tokenType != null) {
            List<Issuer> issuerList = tokenType.getExtensibilityElements(Issuer.class);
            if ((issuerList != null) && (!issuerList.isEmpty())) {
                Issuer issuer = issuerList.get(0);
                List<Addressing10Metadata> amdatas = issuer.getExtensibilityElements(Addressing10Metadata.class);
                if ((amdatas != null) && (!amdatas.isEmpty())) {
                    Addressing10Metadata amdata = amdatas.get(0);
                    List<Metadata> mdata = amdata.getExtensibilityElements(Metadata.class);
                    if ((mdata != null) && (!mdata.isEmpty())) {
                        Metadata m = mdata.get(0);
                        if (m != null) {
                            MetadataSection ms = m.getMetadataSection();
                            if (ms != null) {
                                MetadataReference mr = ms.getMetadataReference();
                                if (mr != null) {
                                    Address10 a = mr.getAddress();
                                    if (a != null) {
                                        return a.getAddress();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public void setIssuedTokenAddressAttributes(WSDLComponent token, String address, String metaAddress) {
        WSDLModel model = token.getModel();

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);            
            Issuer i = pmh.createElement(token, SecurityPolicyQName.ISSUER.getQName(configVersion), Issuer.class, false);
            Address10 a = pmh.createElement(i, Addressing10QName.ADDRESS.getQName(), Address10.class, false);
            a.setAddress(address);

            Addressing10Metadata am = pmh.createElement(i, 
                    Addressing10QName.ADDRESSINGMETADATA.getQName(), Addressing10Metadata.class, false);
            Metadata m = pmh.createElement(am, MexQName.METADATA.getQName(), Metadata.class, false);
            MetadataSection ms = pmh.createElement(m, MexQName.METADATASECTION.getQName(), MetadataSection.class, false);
            MetadataReference mr = pmh.createElement(ms, MexQName.METADATAREFERENCE.getQName(), MetadataReference.class, false);
            Address10 ma = pmh.createElement(mr, Addressing10QName.ADDRESS.getQName(), Address10.class, false);
            ma.setAddress(metaAddress);
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }

    public void setIssuedTokenRSTAttributes(WSDLComponent token, String tokenType, String keyType, String keySize) {
        WSDLModel model = token.getModel();

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            RequestSecurityTokenTemplate rst = pmh.createElement(token, 
                    SecurityPolicyQName.REQUESTSECURITYTOKENTEMPLATE.getQName(configVersion), 
                    RequestSecurityTokenTemplate.class, false);
            
            TokenType t = pmh.createElement(rst, TrustQName.TOKENTYPE.getQName(configVersion), TokenType.class, false);
            if (tokenType.equals(ComboConstants.ISSUED_TOKENTYPE_SAML20)) {
                t.setContent(ComboConstants.ISSUED_TOKENTYPE_SAML20_POLICYSTR);
            }
            if (tokenType.equals(ComboConstants.ISSUED_TOKENTYPE_SAML11)) {
                t.setContent(ComboConstants.ISSUED_TOKENTYPE_SAML11_POLICYSTR);
            }
            if (tokenType.equals(ComboConstants.ISSUED_TOKENTYPE_SAML10)) {
                t.setContent(ComboConstants.ISSUED_TOKENTYPE_SAML10_POLICYSTR);
            }

            KeyType k = pmh.createElement(rst, TrustQName.KEYTYPE.getQName(configVersion), KeyType.class, false);
            String nsStart = TrustQName.getNamespaceUri(configVersion);
            if (keyType.equals(ComboConstants.ISSUED_KEYTYPE_PUBLIC)) {
                k.setContent(nsStart + ComboConstants.ISSUED_KEYTYPE_PUBLIC_POLICYSTR);
            }
            if (keyType.equals(ComboConstants.ISSUED_KEYTYPE_SYMMETRIC)) {
                k.setContent(nsStart + ComboConstants.ISSUED_KEYTYPE_SYMMETRIC_POLICYSTR);
            }
            if (keyType.equals(ComboConstants.ISSUED_KEYTYPE_NOPROOF)) {
                if (ConfigVersion.CONFIG_1_0 != configVersion) {
                    k.setContent(nsStart + ComboConstants.ISSUED_KEYTYPE_NOPROOF13_POLICYSTR);
                } else {
                    k.setContent(ComboConstants.ISSUED_KEYTYPE_NOPROOF_POLICYSTR);
                }
            }

            if (keySize != null) {
                KeySize s = pmh.createElement(rst, TrustQName.KEYSIZE.getQName(configVersion), KeySize.class, false);
                s.setContent(keySize);
            } else {
                List<KeySize> s = rst.getExtensibilityElements(KeySize.class);
                if ((s != null) && (s.size() > 0)) {
                    KeySize ks = s.get(0);
                    ks.getParent().removeExtensibilityElement(ks);
                }
            }
            
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }
}
