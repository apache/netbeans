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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfileRegistry;
import org.netbeans.modules.websvc.wsitconf.spi.features.AdvancedSecurityFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ClientDefaultsFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ServiceDefaultsFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.TrustStoreFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ValidatorsFeature;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.TrustElement;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Address;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Address10;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.AsymmetricBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.SymmetricBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.TransportBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.WssElement;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.InitiatorToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RecipientToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;

/**
 *
 * @author Martin Grebac
 */
public class ProfilesModelHelper {

    public static final String XWS_SECURITY_SERVER = "xws-security-server";
    public static final String XWS_SECURITY_CLIENT = "xws-security-client";
    public static final String WSSIP = "wssip";    
    public static final String DEFAULT_PASSWORD = "wsit";
    public static final String DEFAULT_USERNAME = "wsit";
    public static final String DEFAULT_KERBEROS_LMODULE_SERVICE = "KerberosService";
    public static final String DEFAULT_KERBEROS_LMODULE_CLIENT = "KerberosClient";
    public static final String DEFAULT_KERBEROS_PRINCIPAL = "websvc/service@SUN.COM";

    private static final Logger logger = Logger.getLogger(ProfilesModelHelper.class.getName());
    
    private static HashMap<ConfigVersion, ProfilesModelHelper> instances =
            new HashMap<ConfigVersion, ProfilesModelHelper>();
    private ConfigVersion configVersion = ConfigVersion.getDefault();

    /**
     * Creates a new instance of ProfilesModelHelper
     */
    private ProfilesModelHelper(ConfigVersion configVersion) {
        this.configVersion = configVersion;
    }

    public static final ProfilesModelHelper getInstance(ConfigVersion configVersion) {
        ProfilesModelHelper instance = instances.get(configVersion);
        if (instance == null) {
            instance = new ProfilesModelHelper(configVersion);
            instances.put(configVersion, instance);
        }
        return instance;
    }

    public static boolean isSSLProfile(String s) {
        if (ComboConstants.PROF_MSGAUTHSSL.equals(s) || 
            ComboConstants.PROF_SAMLSSL.equals(s) ||
            ComboConstants.PROF_TRANSPORT.equals(s)) {
            return true;
        }
        return false;
    }
    
    /** 
     * Returns security profile for Binding or BindingOperation
     */
    public static String getSecurityProfile(WSDLComponent c) {
        assert ((c instanceof BindingOperation) || (c instanceof Binding));

        Set<SecurityProfile> profiles = SecurityProfileRegistry.getDefault().getSecurityProfiles();
        for (SecurityProfile profile : profiles) {
            if (profile.isCurrentProfile(c)) {
                return profile.getDisplayName();
            }
        }
        
        return ComboConstants.PROF_NOTRECOGNIZED;
    }

    /** 
     * Checks whether Secure Conversation is enabled
     */
    public static boolean isSCEnabled(WSDLComponent c) {
        assert ((c instanceof BindingOperation) || (c instanceof Binding));
        Policy p = PolicyModelHelper.getPolicyForElement(c);
        SymmetricBinding sb = (SymmetricBinding)PolicyModelHelper.getTopLevelElement(p, SymmetricBinding.class,false);
        if (sb == null) return false;
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(sb, ProtectionToken.class);
        if (protTokenKind == null) return false;
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
        if (protToken == null) return false;
        boolean secConv = (protToken instanceof SecureConversationToken);
        return secConv;        
    }

    public static String getWSITSecurityProfile(WSDLComponent c) {
        if ((c instanceof Binding) || (c instanceof BindingOperation)) {
            Policy p = PolicyModelHelper.getPolicyForElement(c);

            SymmetricBinding sb = (SymmetricBinding)PolicyModelHelper.getTopLevelElement(p, SymmetricBinding.class,false);
            WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(sb, ProtectionToken.class);
            WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
            WSDLComponent secConvSecBinding = null;
            boolean secConv = (protToken instanceof SecureConversationToken);

            WSDLComponent bootPolicy = null;
            
            if (secConv) {
                bootPolicy = SecurityTokensModelHelper.getTokenElement(protToken, BootstrapPolicy.class);
                secConvSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
            }
            
            TransportBinding tb = null;
            if (secConv && (secConvSecBinding instanceof TransportBinding)) {
                tb = (TransportBinding) secConvSecBinding;
            } else {
                tb = (TransportBinding)PolicyModelHelper.getTopLevelElement(p, TransportBinding.class,false);
            }
            if (tb != null) { // profiles 1,2,3
                // depends on message level policy
                if (c instanceof BindingOperation) {
                    BindingInput input = ((BindingOperation)c).getBindingInput();
                    WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(input, SecurityTokensModelHelper.ENDORSING);
                    if (tokenKind != null) {
                        return ComboConstants.PROF_MSGAUTHSSL; // profile 2 with secure conversation
                    }
                    tokenKind = SecurityTokensModelHelper.getSupportingToken(input, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                    String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
                    if (ComboConstants.SAML.equals(tokenType)) { // profile3
                        return ComboConstants.PROF_SAMLSSL;
                    } else if ((ComboConstants.USERNAME.equals(tokenType)) || (ComboConstants.X509.equals(tokenType))) {  // profile2
                        return ComboConstants.PROF_MSGAUTHSSL;
                    }
                    tokenKind = SecurityTokensModelHelper.getSupportingToken(input, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                    if (tokenKind != null) {
                        return ComboConstants.PROF_MSGAUTHSSL;
                    }
                    return ComboConstants.PROF_TRANSPORT;
                } else {
                    WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(c, SecurityTokensModelHelper.ENDORSING);
                    if (tokenKind != null) {
                        return ComboConstants.PROF_MSGAUTHSSL; // profile 2 with secure conversation
                    }
                    Policy pp = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class,false);
                    if (secConv) {
                        tokenKind = SecurityTokensModelHelper.getSupportingToken(pp, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                    } else {
                        tokenKind = SecurityTokensModelHelper.getSupportingToken(c, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                    }
                    String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
                    if (ComboConstants.SAML.equals(tokenType)) { // profile3
                        return ComboConstants.PROF_SAMLSSL;
                    } else if ((ComboConstants.USERNAME.equals(tokenType)) || (ComboConstants.X509.equals(tokenType))) {  // profile2
                        return ComboConstants.PROF_MSGAUTHSSL;
                    }

                    if (secConv) {
                        tokenKind = SecurityTokensModelHelper.getSupportingToken(pp, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                    } else {
                        tokenKind = SecurityTokensModelHelper.getSupportingToken(c, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                    }
                    if (tokenKind != null) {
                        return ComboConstants.PROF_MSGAUTHSSL;
                    }
                    return ComboConstants.PROF_TRANSPORT;
                }
            }

            if (secConv && (secConvSecBinding instanceof SymmetricBinding)) {
                sb = (SymmetricBinding) secConvSecBinding;
            } else {
                sb = (SymmetricBinding)PolicyModelHelper.getTopLevelElement(p, SymmetricBinding.class,false);
            }
            if (sb != null) { // profiles 4,6,9,10,12 + PasswdDerived
                protToken = (ProtectionToken) SecurityTokensModelHelper.getTokenElement(sb, ProtectionToken.class);
                if (protToken != null) {
                    String tokenType = SecurityTokensModelHelper.getTokenType(protToken);
                    if (ComboConstants.ISSUED.equals(tokenType)) {  // STS Issued Token
                        return ComboConstants.PROF_STSISSUED;
                    }
                    if (ComboConstants.KERBEROS.equals(tokenType)) {  // Kerberos Profile
                        return ComboConstants.PROF_KERBEROS;
                    }
                    if (ComboConstants.USERNAME.equals(tokenType)) {  // Username Authentication with Password Derived keys Profile
                        return ComboConstants.PROF_USERNAME_PASSWORDDERIVED;
                    }
                    if (ComboConstants.X509.equals(tokenType)) { // profile 12, 6, 4
                        WSDLComponent tokenKind = null;
                        if (secConv) {
                            Policy pp = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class,false);
                            tokenKind = SecurityTokensModelHelper.getSupportingToken(pp, SecurityTokensModelHelper.ENDORSING);
                        } else {
                            tokenKind = SecurityTokensModelHelper.getSupportingToken(c, SecurityTokensModelHelper.ENDORSING);
                        }
                        
                        tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
                        if (ComboConstants.ISSUED.equals(tokenType)) { // profile 12
                            return ComboConstants.PROF_STSISSUEDENDORSE;
                        }
                        if (ComboConstants.X509.equals(tokenType)) { // profile 6
                            return ComboConstants.PROF_ENDORSCERT;
                        }
                        if (tokenType == null) {    // profile 4
                            WSDLComponent encTokenKind = null;
                            if (secConv) {
                                Policy pp = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class, false);
                                tokenKind = SecurityTokensModelHelper.getSupportingToken(pp, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                                encTokenKind = SecurityTokensModelHelper.getSupportingToken(pp, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                            } else {
                                tokenKind = SecurityTokensModelHelper.getSupportingToken(c, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                                encTokenKind = SecurityTokensModelHelper.getSupportingToken(c, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                            }
                            if (encTokenKind != null) {
                                tokenType = SecurityTokensModelHelper.getTokenType(encTokenKind);
                                if (ComboConstants.USERNAME.equals(tokenType)) {
                                    return ComboConstants.PROF_USERNAME;
                                }
                                return ComboConstants.PROF_STSISSUEDSUPPORTING;
                            }
                            tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
                            if (ComboConstants.ISSUED.equals(tokenType)) { // profile 13
                                return ComboConstants.PROF_STSISSUEDSUPPORTING;
                            }
                            return ComboConstants.PROF_USERNAME;
                        }
                    }
                }
            }

            AsymmetricBinding ab = null;
            if (secConv && (secConvSecBinding instanceof AsymmetricBinding)) {
                ab = (AsymmetricBinding) secConvSecBinding;
            } else {
                ab = (AsymmetricBinding)PolicyModelHelper.getTopLevelElement(p, AsymmetricBinding.class,false);
            }
            if (ab != null) { // profiles 5,7,8,11
                InitiatorToken initToken = (InitiatorToken) SecurityTokensModelHelper.getTokenElement(ab, InitiatorToken.class);
                RecipientToken recipToken = (RecipientToken) SecurityTokensModelHelper.getTokenElement(ab, RecipientToken.class);
                if ((initToken != null) && (recipToken!= null)) {
                    String initTokenType = SecurityTokensModelHelper.getTokenType(initToken);
                    String recipTokenType = SecurityTokensModelHelper.getTokenType(recipToken);
                    if ((ComboConstants.X509.equals(initTokenType)) && (ComboConstants.X509.equals(recipTokenType))) {  // profile 5, 7                       
                        if (c instanceof BindingOperation) {
                            BindingInput input = ((BindingOperation)c).getBindingInput();
                            WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(input, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                            if (tokenKind == null) tokenKind = SecurityTokensModelHelper.getSupportingToken(input, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                            String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
                            if (ComboConstants.SAML.equals(tokenType)) { // profile7
                                return ComboConstants.PROF_SAMLSENDER;
                            } else if (tokenType == null) {  // profile5
                                return ComboConstants.PROF_MUTUALCERT;
                            }
                        } else {
                            WSDLComponent tokenKind = null;
                            if (secConv) {
                                Policy pp = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class,false);
                                tokenKind = SecurityTokensModelHelper.getSupportingToken(pp, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                                if (tokenKind == null) tokenKind = SecurityTokensModelHelper.getSupportingToken(pp, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                            } else {
                                tokenKind = SecurityTokensModelHelper.getSupportingToken(c, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                                if (tokenKind == null) tokenKind = SecurityTokensModelHelper.getSupportingToken(c, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                            }
                            String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
                            if (ComboConstants.SAML.equals(tokenType)) { // profile7
                                return ComboConstants.PROF_SAMLSENDER;
                            } else if (tokenType == null) {  // profile5
                                return ComboConstants.PROF_MUTUALCERT;
                            }
                        }
                    }
                    if ((ComboConstants.SAML.equals(initTokenType)) && (ComboConstants.X509.equals(recipTokenType))) {  // profile 8,
                        return ComboConstants.PROF_SAMLHOLDER;
                    }
                    if ((ComboConstants.ISSUED.equals(initTokenType)) && (ComboConstants.X509.equals(recipTokenType))) {  // profile 11
                        return ComboConstants.PROF_STSISSUEDCERT;
                    }
                }
            }
        }
        
        return ComboConstants.PROF_NOTRECOGNIZED;
    }

    private static void updateServiceUrl(WSDLComponent c, boolean toHttps) {
        
        String from, to;
        String portFrom, portTo;
        if (toHttps) {
            from = "http:";     //NOI18N
            to = "https:";      //NOI18N
            portFrom	= "\\$\\{HttpDefaultPort}";
            portTo		= "\\${HttpsDefaultPort}";
        } else {
            from = "https:";    //NOI18N
            to = "http:";       //NOI18N
            portFrom	= "\\$\\{HttpsDefaultPort}";
            portTo		= "\\${HttpDefaultPort}";
        }
        if (c instanceof Binding) {
            Collection<Service> services = c.getModel().getDefinitions().getServices();
            for (Service s : services) {
                Collection<Port> ports = s.getPorts();                
                for (Port p : ports) {
                   if (p.getBinding().references((Binding)c)) {
                       List<Address> addresses = p.getExtensibilityElements(Address.class);
                       if ((addresses != null) && (!addresses.isEmpty())) {
                           for (Address a : addresses) {
                               String addr = a.getAddress();
                               if (addr != null) {
                            	   addr = addr.replaceFirst(portFrom, portTo);
                                   a.setAddress(addr.replaceFirst(from, to));
                               }
                           }
                       }
                       List<Address10> addresses10 = p.getExtensibilityElements(Address10.class);
                       if ((addresses10 != null) && (!addresses10.isEmpty())) {
                           for (Address10 a : addresses10) {
                               String addr = a.getAddress();
                               if (addr != null) {
                            	   addr = addr.replaceFirst(portFrom, portTo); 
                                   a.setAddress(addr.replaceFirst(from, to));
                               }
                           }
                       }
                       List<SOAPAddress> soapAddresses = p.getExtensibilityElements(SOAPAddress.class);
                       if ((soapAddresses != null) && (!soapAddresses.isEmpty())) {
                           for (SOAPAddress a : soapAddresses) {
                               String addr = a.getLocation();
                               if (addr != null) {
                            	   addr = addr.replaceFirst(portFrom, portTo); 
                                   a.setLocation(addr.replaceFirst(from, to));
                               }
                           }
                       }
                   }
                }
            }
        }
    }

    public static boolean isServiceUrlHttps(Binding binding) {     
        Collection<Service> services = binding.getModel().getDefinitions().getServices();
        for (Service s : services) {
            Collection<Port> ports = s.getPorts();                
            for (Port p : ports) {
               if (p.getBinding().references(binding)) {
                   List<Address> addresses = p.getExtensibilityElements(Address.class);
                   if ((addresses != null) && (!addresses.isEmpty())) {
                       for (Address a : addresses) {
                           String addr = a.getAddress();
                           if ((addr != null) && (addr.contains("https:"))) {
                               return true;
                           }
                       }
                   }
                   List<Address10> addresses10 = p.getExtensibilityElements(Address10.class);
                   if ((addresses10 != null) && (!addresses10.isEmpty())) {
                       for (Address10 a : addresses10) {
                           String addr = a.getAddress();
                           if ((addr != null) && (addr.contains("https:"))) {
                               return true;
                           }
                       }
                   }
                   List<SOAPAddress> soapAddresses = p.getExtensibilityElements(SOAPAddress.class);
                   if ((soapAddresses != null) && (!soapAddresses.isEmpty())) {
                       for (SOAPAddress a : soapAddresses) {
                           String addr = a.getLocation();
                           if ((addr != null) && (addr.contains("https:"))) {
                               return true;
                           }
                       }
                   }
               }
            }
        }
        return false;
    }
    
    /** Sets security profile on Binding or BindingOperation
     */
    public void setSecurityProfile(WSDLComponent c, String profile, String oldProfile, boolean updateServiceUrl) {
        assert (c != null);
        assert (profile != null);
        assert ((c instanceof BindingOperation) || (c instanceof Binding));

        SecurityProfile newP = SecurityProfileRegistry.getDefault().getProfile(profile);
        SecurityProfile oldP = SecurityProfileRegistry.getDefault().getProfile(oldProfile);
        
        if (oldP != null) {
            oldP.profileDeselected(c, configVersion);
        }
        newP.profileSelected(c, updateServiceUrl, configVersion);
    }
    
    public static boolean isServiceDefaultSetupSupported(String profile) {
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(profile);
        return (p instanceof ServiceDefaultsFeature);
    }

    public static boolean isTruststoreRequired(String profile, WSDLComponent c, boolean client) {
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(profile);
        if (p instanceof TrustStoreFeature) {
            return ((TrustStoreFeature)p).isTrustStoreRequired(c, client);
        } else {
            return !isSSLProfile(profile);
        }
    }
    
    public static boolean isClientDefaultSetupSupported(String profile) {
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(profile);
        return (p instanceof ClientDefaultsFeature);
    }
    
    public static boolean isServiceDefaultSetupUsed(String profile, Binding binding, Project project) {
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(profile);
        if (p instanceof ServiceDefaultsFeature) {
            return ((ServiceDefaultsFeature)p).isServiceDefaultSetupUsed(binding, project);
        }
        return false;
    }

    public static boolean isClientDefaultSetupUsed(String profile, Binding binding, WSDLComponent serviceBinding, Project project) {
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(profile);
        if (p instanceof ClientDefaultsFeature) {
            return ((ClientDefaultsFeature)p).isClientDefaultSetupUsed(binding, (Binding)serviceBinding, project);
        }
        return false;
    }

    public static boolean isValidatorsSupported(String profile) {
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(profile);
        return (p instanceof ValidatorsFeature);
    }

    public static boolean isAdvancedSecuritySupported(String profile) {
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(profile);
        return (p instanceof AdvancedSecurityFeature);
    }
    
    public static void setClientDefaults(String profile, Binding binding, WSDLComponent serviceBinding, Project project) {
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(profile);
        if (p instanceof ClientDefaultsFeature) {
            ((ClientDefaultsFeature)p).setClientDefaults(binding, serviceBinding, project);
        }
    }

    public static void setServiceDefaults(String profile, Binding binding, Project project) {
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(profile);
        if (p instanceof ServiceDefaultsFeature) {
            ((ServiceDefaultsFeature)p).setServiceDefaults(binding, project);
        }
    }
    
//    private void updateServiceUrl(WSDLComponent c) {
//        if (c instanceof Binding) {
//            Collection<Service> services = c.getModel().getDefinitions().getServices();
//            for (Service s : services) {
//                Collection<Port> ports = s.getPorts();                
//                for (Port p : ports) {
//                   if (p.getBinding().references((Binding)c)) {
//                       List<Address> addresses = p.getExtensibilityElements(Address.class);
//                       if ((addresses != null) && (!addresses.isEmpty())) {
//                           for (Address a : addresses) {
//                               String addr = a.getAddress();
//                               if (addr != null) {
//                                   a.setAddress(addr.replaceFirst("http:", "https:")); //NOI18N
//                               }
//                           }
//                       }
//                       List<Address10> addresses10 = p.getExtensibilityElements(Address10.class);
//                       if ((addresses10 != null) && (!addresses10.isEmpty())) {
//                           for (Address10 a : addresses10) {
//                               String addr = a.getAddress();
//                               if (addr != null) {
//                                   a.setAddress(addr.replaceFirst("http:", "https:")); //NOI18N
//                               }
//                           }
//                       }
//                       List<SOAPAddress> soapAddresses = p.getExtensibilityElements(SOAPAddress.class);
//                       if ((soapAddresses != null) && (!soapAddresses.isEmpty())) {
//                           for (SOAPAddress a : soapAddresses) {
//                               String addr = a.getLocation();
//                               if (addr != null) {
//                                   a.setLocation(addr.replaceFirst("http:", "https:")); //NOI18N
//                               }
//                           }
//                       }
//                   }
//                }
//            }
//        }
//    }
    
    /** Sets security profile on Binding or BindingOperation
     */
    public void setSecurityProfile(WSDLComponent c, String profile, boolean updateServiceUrl) {
        WSDLModel model = c.getModel();
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
        SecurityTokensModelHelper stmh = SecurityTokensModelHelper.getInstance(configVersion);
        SecurityPolicyModelHelper spmh = SecurityPolicyModelHelper.getInstance(configVersion);
        AlgoSuiteModelHelper asmh = AlgoSuiteModelHelper.getInstance(configVersion);
        pmh.createPolicy(c, true);
        try {
            // Profile #1
            if (ComboConstants.PROF_TRANSPORT.equals(profile)) {
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.TRANSPORT);
                stmh.setTokenType(bt, ComboConstants.TRANSPORT, ComboConstants.HTTPS);
                spmh.setLayout(bt, ComboConstants.LAX);
                spmh.enableIncludeTimestamp(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                spmh.enableWss(c, false);
                spmh.disableTrust(c);
                SecurityTokensModelHelper.removeSupportingTokens(c);
            } else if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) { // Profile #2
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.TRANSPORT);
                stmh.setTokenType(bt, ComboConstants.TRANSPORT, ComboConstants.HTTPS);
                spmh.setLayout(bt, ComboConstants.LAX);
                spmh.enableIncludeTimestamp(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                WssElement wss = spmh.enableWss(c, false);
                spmh.disableTrust(c);
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                if (ConfigVersion.CONFIG_1_0.equals(configVersion)) {
                    stmh.setSupportingTokens(c, ComboConstants.USERNAME, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                } else {
                    stmh.setSupportingTokens(c, ComboConstants.USERNAME, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                }
            } else if (ComboConstants.PROF_SAMLSSL.equals(profile)) {   // Profile #3
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.TRANSPORT);
                stmh.setTokenType(bt, ComboConstants.TRANSPORT, ComboConstants.HTTPS);
                spmh.setLayout(bt, ComboConstants.LAX);
                spmh.enableIncludeTimestamp(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                WssElement wss = spmh.enableWss(c, false);
                spmh.disableTrust(c);
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                stmh.setSupportingTokens(c, ComboConstants.SAML, SecurityTokensModelHelper.SIGNED_SUPPORTING);
            } else if (ComboConstants.PROF_USERNAME.equals(profile)) {   // Profile #4
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                WSDLComponent tokenType = stmh.setTokenType(bt, ComboConstants.PROTECTION, ComboConstants.X509);
//                spmh.enableRequireThumbprintReference(tokenType, true);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.NEVER);
                spmh.setLayout(bt, ComboConstants.STRICT);
                spmh.enableIncludeTimestamp(bt, true);
                spmh.enableSignEntireHeadersAndBody(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                WssElement wss = spmh.enableWss(c, true);
                spmh.disableTrust(c);
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
                spmh.enableMustSupportRefIssuerSerial(wss, true);
                spmh.enableMustSupportRefThumbprint(wss, true);
                spmh.enableMustSupportRefEncryptedKey(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                int suppTokenType = (ConfigVersion.CONFIG_1_0.equals(configVersion)) ? 
                    SecurityTokensModelHelper.SIGNED_SUPPORTING : SecurityTokensModelHelper.SIGNED_ENCRYPTED;                
                stmh.setSupportingTokens(c, ComboConstants.USERNAME, suppTokenType);
            } else if (ComboConstants.PROF_USERNAME_PASSWORDDERIVED.equals(profile)) {   // Profile #5
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                WSDLComponent tokenType = stmh.setTokenType(bt, ComboConstants.PROTECTION, ComboConstants.USERNAME);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
                spmh.setLayout(bt, ComboConstants.STRICT);
                spmh.enableIncludeTimestamp(bt, true);
                spmh.enableSignEntireHeadersAndBody(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                WssElement wss = spmh.enableWss(c, true);
                spmh.disableTrust(c);
                spmh.enableMustSupportRefIssuerSerial(wss, true);
                spmh.enableMustSupportRefThumbprint(wss, true);
                spmh.enableMustSupportRefEncryptedKey(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
            } else if (ComboConstants.PROF_MUTUALCERT.equals(profile)) {         // #5
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.ASYMMETRIC);
                WSDLComponent tokenType = stmh.setTokenType(bt, ComboConstants.INITIATOR, ComboConstants.X509);
                SecurityPolicyModelHelper.getInstance(configVersion).enableRequireIssuerSerialReference(tokenType, false);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
                tokenType = stmh.setTokenType(bt, ComboConstants.RECIPIENT, ComboConstants.X509);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.NEVER);
                spmh.setLayout(bt, ComboConstants.STRICT);
                spmh.enableIncludeTimestamp(bt, true);
                spmh.enableSignEntireHeadersAndBody(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                WssElement wss = spmh.enableWss(c, false);
                spmh.disableTrust(c);
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
                spmh.enableMustSupportRefIssuerSerial(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
            } else if (ComboConstants.PROF_ENDORSCERT.equals(profile)) {               //#6
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                WSDLComponent tokenType = stmh.setTokenType(bt, ComboConstants.PROTECTION, ComboConstants.X509);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.NEVER);
//                spmh.enableRequireThumbprintReference(tokenType, true);
                spmh.setLayout(bt, ComboConstants.LAX);
                spmh.enableIncludeTimestamp(bt, true);
                spmh.enableSignEntireHeadersAndBody(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = spmh.enableWss(c, true);
                spmh.disableTrust(c);
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
                spmh.enableMustSupportRefIssuerSerial(wss, true);
                spmh.enableMustSupportRefThumbprint(wss, true);
                spmh.enableMustSupportRefEncryptedKey(wss, true);
                //endorsing supporting token
                SecurityTokensModelHelper.removeSupportingTokens(c);
                tokenType = stmh.setSupportingTokens(c, ComboConstants.X509, SecurityTokensModelHelper.ENDORSING);
            } else if (ComboConstants.PROF_SAMLSENDER.equals(profile)) {        //#7
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.ASYMMETRIC);
                WSDLComponent tokenType = stmh.setTokenType(bt, ComboConstants.INITIATOR, ComboConstants.X509);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
                tokenType = stmh.setTokenType(bt, ComboConstants.RECIPIENT, ComboConstants.X509);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.NEVER);
                spmh.setLayout(bt, ComboConstants.STRICT);
                spmh.enableIncludeTimestamp(bt, true);
                spmh.enableSignEntireHeadersAndBody(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = spmh.enableWss(c, false);
                spmh.disableTrust(c);
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
                spmh.enableMustSupportRefIssuerSerial(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                if (configVersion.equals(ConfigVersion.CONFIG_1_0)) {
                    tokenType = stmh.setSupportingTokens(c, ComboConstants.SAML, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                } else {
                    tokenType = stmh.setSupportingTokens(c, ComboConstants.SAML, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                }
            } else if (ComboConstants.PROF_SAMLHOLDER.equals(profile)) {        // #8
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.ASYMMETRIC);
                WSDLComponent tokenType = stmh.setTokenType(bt, ComboConstants.INITIATOR, ComboConstants.SAML);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
                tokenType = stmh.setTokenType(bt, ComboConstants.RECIPIENT, ComboConstants.X509);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.NEVER);
                spmh.setLayout(bt, ComboConstants.STRICT);
                spmh.enableIncludeTimestamp(bt, true);
                spmh.enableSignEntireHeadersAndBody(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = spmh.enableWss(c, false);
                spmh.disableTrust(c);
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
                spmh.enableMustSupportRefIssuerSerial(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
            } else if (ComboConstants.PROF_KERBEROS.equals(profile)) {          //#9
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                WSDLComponent tokenType = stmh.setTokenType(bt, ComboConstants.PROTECTION, ComboConstants.KERBEROS);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.ONCE);
                spmh.setLayout(bt, ComboConstants.STRICT);
                spmh.enableIncludeTimestamp(bt, true);
                spmh.enableSignEntireHeadersAndBody(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = spmh.enableWss(c, true);
                spmh.disableTrust(c);
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
                spmh.enableMustSupportRefIssuerSerial(wss, true);
                spmh.enableMustSupportRefThumbprint(wss, true);
                spmh.enableMustSupportRefEncryptedKey(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
            } else if (ComboConstants.PROF_STSISSUED.equals(profile)) {         //#10
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                WSDLComponent tokenType = stmh.setTokenType(bt, ComboConstants.PROTECTION, ComboConstants.ISSUED);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
                spmh.setLayout(bt, ComboConstants.LAX);
                spmh.enableIncludeTimestamp(bt, true);
                spmh.enableSignEntireHeadersAndBody(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = spmh.enableWss(c, true);
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
                spmh.enableMustSupportRefIssuerSerial(wss, true);
                spmh.enableMustSupportRefThumbprint(wss, true);
                spmh.enableMustSupportRefEncryptedKey(wss, true);
                //trust10
                TrustElement trust = spmh.enableTrust(c, configVersion);
                spmh.enableMustSupportIssuedTokens(trust, true);
                spmh.enableRequireClientEntropy(trust, true);
                spmh.enableRequireServerEntropy(trust, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
            } else if (ComboConstants.PROF_STSISSUEDCERT.equals(profile)) {     //#11
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.ASYMMETRIC);
                WSDLComponent tokenType = stmh.setTokenType(bt, ComboConstants.INITIATOR, ComboConstants.ISSUED);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
                tokenType = stmh.setTokenType(bt, ComboConstants.RECIPIENT, ComboConstants.X509);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.NEVER);
                spmh.setLayout(bt, ComboConstants.LAX);
                spmh.enableIncludeTimestamp(bt, true);
                spmh.enableSignEntireHeadersAndBody(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = spmh.enableWss(c, true);
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
                spmh.enableMustSupportRefIssuerSerial(wss, true);
                spmh.enableMustSupportRefThumbprint(wss, true);
                spmh.enableMustSupportRefEncryptedKey(wss, true);
                //trust10
                TrustElement trust = spmh.enableTrust(c, configVersion);
                spmh.enableMustSupportIssuedTokens(trust, true);
                spmh.enableRequireClientEntropy(trust, true);
                spmh.enableRequireServerEntropy(trust, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
            } else if (ComboConstants.PROF_STSISSUEDENDORSE.equals(profile)) {  //#12
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                WSDLComponent tokenType = stmh.setTokenType(bt, ComboConstants.PROTECTION, ComboConstants.X509);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYS);
//                SecurityPolicyModelHelper.enableRequireThumbprintReference(tokenType, true);
                spmh.setLayout(bt, ComboConstants.LAX);
                spmh.enableIncludeTimestamp(bt, true);
                spmh.enableSignEntireHeadersAndBody(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = spmh.enableWss(c, true);
                spmh.enableMustSupportRefKeyIdentifier(wss, true);
                spmh.enableMustSupportRefIssuerSerial(wss, true);
                spmh.enableMustSupportRefThumbprint(wss, true);
                spmh.enableMustSupportRefEncryptedKey(wss, true);
                //trust10
                TrustElement trust = spmh.enableTrust(c, configVersion);
                spmh.enableMustSupportIssuedTokens(trust, true);
                spmh.enableRequireClientEntropy(trust, true);
                spmh.enableRequireServerEntropy(trust, true);
                //endorsing supporting token
                SecurityTokensModelHelper.removeSupportingTokens(c);
                tokenType = stmh.setSupportingTokens(c, ComboConstants.ISSUED, SecurityTokensModelHelper.ENDORSING);
            } else if (ComboConstants.PROF_STSISSUEDSUPPORTING.equals(profile)) {  //#13
                WSDLComponent bt = spmh.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                WSDLComponent tokenType = stmh.setTokenType(bt, ComboConstants.PROTECTION, ComboConstants.X509);
                stmh.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYS);
//                SecurityPolicyModelHelper.enableRequireThumbprintReference(tokenType, true);
                spmh.setLayout(bt, ComboConstants.LAX);
                spmh.enableIncludeTimestamp(bt, true);
                spmh.enableSignEntireHeadersAndBody(bt, true);
                asmh.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = spmh.enableWss(c, true);
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
                spmh.enableMustSupportRefIssuerSerial(wss, true);
                spmh.enableMustSupportRefThumbprint(wss, true);
                spmh.enableMustSupportRefEncryptedKey(wss, true);
                //trust10
                TrustElement trust = spmh.enableTrust(c, configVersion);
                spmh.enableMustSupportIssuedTokens(trust, true);
                spmh.enableRequireClientEntropy(trust, true);
                spmh.enableRequireServerEntropy(trust, true);
                //endorsing supporting token
                SecurityTokensModelHelper.removeSupportingTokens(c);
                if (configVersion.equals(ConfigVersion.CONFIG_1_0)) {
                    tokenType = stmh.setSupportingTokens(c, ComboConstants.ISSUED, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                } else {
                    tokenType = stmh.setSupportingTokens(c, ComboConstants.ISSUED, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                }
            }
            setMessageLevelSecurityProfilePolicies(c, profile);
            if (updateServiceUrl) {
                updateServiceUrl(c, isSSLProfile(profile));
            }
            
            if (!ConfigVersion.CONFIG_1_0.equals(configVersion)) {
                boolean rm = RMModelHelper.getInstance(configVersion).isRMEnabled(c);
                if (rm) {
                    if (isSSLProfile(profile)) {
                        if (ProfilesModelHelper.isSSLProfile(profile)) {
                            RMSequenceBinding.SECURED_TRANSPORT.set(configVersion,(Binding) c);
                        } else {
                            RMSequenceBinding.SECURED_TOKEN.set(configVersion,(Binding) c);
                        }
                    }
                }
            }
            
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }

    public void setMessageLevelSecurityProfilePolicies(WSDLComponent c, String profile) {
        assert ((c instanceof BindingOperation) || (c instanceof Binding));
        
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
        if (c instanceof Binding) {
            Collection<BindingOperation> ops = ((Binding)c).getBindingOperations();
            Iterator<BindingOperation> i = null;
            if ((ops != null) && (ops.size() > 0)) {
                i = ops.iterator();
                BindingOperation bOp = i.next();
                setMessageLevelSecurityProfilePolicies(bOp, profile);
                BindingInput inputB = bOp.getBindingInput();
                BindingOutput outputB = bOp.getBindingOutput();
//                BindingFault faultPolicy = bOp.getBindingFaults().;
                String inputPolicyUri = null;
                if (inputB != null) {
                    inputPolicyUri = PolicyModelHelper.getPolicyUriForElement(inputB);
                }
                String outputPolicyUri = null;
                if (outputB != null) {
                    outputPolicyUri = PolicyModelHelper.getPolicyUriForElement(outputB);
                }
                while (i.hasNext()) {
                    BindingOperation op = i.next();
                    if (inputB != null) {
                        inputB = op.getBindingInput();
                        pmh.attachPolicyToElement(inputPolicyUri, inputB);
                    }
                    if (outputB != null) {
                        outputB = op.getBindingOutput();
                        pmh.attachPolicyToElement(outputPolicyUri, outputB);
                    }
                }
            }            
        } else {
            setMessageLevelSecurityProfilePolicies((BindingOperation)c, profile);
        }
    }
    
    private void setMessageLevelSecurityProfilePolicies(BindingOperation o, String profile) {
        assert (o != null);
        
        WSDLModel model = o.getModel();
        
        BindingInput input = o.getBindingInput();
        BindingOutput output = o.getBindingOutput();

        Binding b = (Binding) o.getParent();
        
        boolean wss11 = SecurityPolicyModelHelper.isWss11(b);                
        boolean rm = RMModelHelper.getInstance(configVersion).isRMEnabled(b);
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            //if (input != null) return;//PolicyModelHelper.removePolicyForElement(input);
            //if (output != null) return;//PolicyModelHelper.removePolicyForElement(output);

            SecurityPolicyModelHelper spmh = SecurityPolicyModelHelper.getInstance(configVersion);
            
            if (ComboConstants.PROF_TRANSPORT.equals(profile)) {
                // do nothing, there are no msg level policies
                return;
            }
            if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) {
                return;
            }
            if (ComboConstants.PROF_SAMLSSL.equals(profile)) {
                return;
            }
            if (ComboConstants.PROF_MUTUALCERT.equals(profile)) {
                spmh.setDefaultTargets(input, true, rm);
                spmh.setDefaultTargets(output, true, rm);
                return;
            }
            //default for all other profiles
            spmh.setDefaultTargets(input, wss11, rm);
            spmh.setDefaultTargets(output, wss11, rm);
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }

    public void setSecureConversation(WSDLComponent c, boolean enable) {
        assert (c != null);
        assert ((c instanceof BindingOperation) || (c instanceof Binding));
        
        Binding b = null;
        if (c instanceof BindingOperation) {
            b = (Binding) c.getParent();
        } else {
            b = (Binding) c;
        }

        WSDLModel model = c.getModel();        
        WSDLComponentFactory wcf = model.getFactory();
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            SecurityPolicyModelHelper spmh = SecurityPolicyModelHelper.getInstance(configVersion);
            SecurityTokensModelHelper stmh = SecurityTokensModelHelper.getInstance(configVersion);

            if (enable) {
                WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(c);
                WSDLComponent par = secBinding.getParent();
                
                boolean onlySign = SecurityPolicyModelHelper.isSignEntireHeadersAndBody(c);
                boolean includeTimestamp = SecurityPolicyModelHelper.isSignEntireHeadersAndBody(c);
                String algoSuite = AlgoSuiteModelHelper.getAlgorithmSuite(c);
                        
                BootstrapPolicy bp = (BootstrapPolicy) wcf.create(par, SecurityPolicyQName.BOOTSTRAPPOLICY.getQName(configVersion));
                par.addExtensibilityElement(bp);
                Policy p = pmh.createElement(bp, PolicyQName.POLICY.getQName(configVersion), Policy.class, false);
                ExtensibilityElement ec = (ExtensibilityElement) secBinding.copy(p);
                p.addExtensibilityElement(ec);

                for (int suppTokenType=0; suppTokenType < 3; suppTokenType++) {
                    ExtensibilityElement suppToken = 
                            (ExtensibilityElement) SecurityTokensModelHelper.getSupportingToken(c, suppTokenType);
                    if (suppToken == null) continue;
                    p.addExtensibilityElement((ExtensibilityElement) suppToken.copy(p));
                    suppToken.getParent().removeExtensibilityElement(suppToken);
                }

                WSDLComponent bType = spmh.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                SecureConversationToken tType = (SecureConversationToken) SecurityTokensModelHelper.getInstance(configVersion).setTokenType(
                        bType, ComboConstants.PROTECTION, ComboConstants.SECURECONVERSATION);                    
                stmh.setTokenInclusionLevel(tType, ComboConstants.ALWAYSRECIPIENT);
                p = pmh.createElement(tType, PolicyQName.POLICY.getQName(configVersion), Policy.class, false);
                ExtensibilityElement bpcopy = (ExtensibilityElement) bp.copy(p);
                p.addExtensibilityElement(bpcopy);
                par.removeExtensibilityElement(bp);
                p = PolicyModelHelper.getTopLevelElement(bpcopy, Policy.class,false);
                WSDLComponent wss10 = SecurityPolicyModelHelper.getWss10(par);
                if (wss10 != null) {
                    p.addExtensibilityElement((ExtensibilityElement) wss10.copy(p));
                }
                WssElement wss11 = SecurityPolicyModelHelper.getWss11(par);
                if (wss11 != null) {
                    p.addExtensibilityElement((ExtensibilityElement) wss11.copy(p));
                }
                TrustElement trust = SecurityPolicyModelHelper.getTrust(par, configVersion);
                if (trust != null) {
                    p.addExtensibilityElement((ExtensibilityElement) trust.copy(p));
                }

                // set top level secure conversation policy
                spmh.setLayout(bType, ComboConstants.STRICT);
                if (algoSuite != null) {
                    AlgoSuiteModelHelper.getInstance(configVersion).setAlgorithmSuite(bType, algoSuite);
                } else {
                    AlgoSuiteModelHelper.getInstance(configVersion).setAlgorithmSuite(bType, ComboConstants.BASIC128);
                }
                if (includeTimestamp) {
                    spmh.enableIncludeTimestamp(bType, true);
                }
                if (onlySign) {
                    spmh.enableSignEntireHeadersAndBody(bType, true);
                }
                
                boolean rm = RMModelHelper.getInstance(configVersion).isRMEnabled(b);
                spmh.setDefaultTargets(p, true, rm);
                
                spmh.disableWss(par);
                WssElement wss = spmh.enableWss(par, true);
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
                spmh.enableMustSupportRefIssuerSerial(wss, true);
                spmh.enableMustSupportRefThumbprint(wss, true);
                spmh.enableMustSupportRefEncryptedKey(wss, true);

                spmh.disableTrust(par);
                trust = spmh.enableTrust(par,configVersion);
                spmh.enableRequireClientEntropy(trust, true);
                spmh.enableRequireServerEntropy(trust, true);
                spmh.enableMustSupportIssuedTokens(trust, true);

            } else {
                WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(c);
                WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
                WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
                WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(protToken, BootstrapPolicy.class);
                WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);

                WSDLComponent par = topSecBinding.getParent().getParent();

                par.addExtensibilityElement((ExtensibilityElement) secBinding.copy(par));

                for (int suppTokenType=0; suppTokenType < 3; suppTokenType++) {
                    ExtensibilityElement suppToken = 
                            (ExtensibilityElement) SecurityTokensModelHelper.getSupportingToken(secBinding.getParent(), suppTokenType);
                    if (suppToken == null) continue;
                    par.addExtensibilityElement((ExtensibilityElement) suppToken.copy(par));
                    suppToken.getParent().removeExtensibilityElement(suppToken);
                }
                
                WssElement wss10 = SecurityPolicyModelHelper.getWss10(secBinding.getParent());
                if (wss10 != null) {
                    par.addExtensibilityElement((ExtensibilityElement) wss10.copy(par));
                }
                WssElement wss11 = SecurityPolicyModelHelper.getWss11(secBinding.getParent());
                if (wss11 != null) {
                    par.addExtensibilityElement((ExtensibilityElement) wss11.copy(par));
                }
                TrustElement trust = SecurityPolicyModelHelper.getTrust(secBinding.getParent(), configVersion);
                if (trust != null) {
                    par.addExtensibilityElement((ExtensibilityElement) trust.copy(par));
                }
                
                spmh.setSecurityBindingType(c, null);
                spmh.disableWss(c);
                spmh.disableTrust(c);
                
                WSDLComponent copyto = PolicyModelHelper.getTopLevelElement(par, All.class,false);
                WSDLComponent bType = SecurityPolicyModelHelper.getSecurityBindingTypeElement(par);
                copyto.addExtensibilityElement((ExtensibilityElement) bType.copy(copyto));
                bType.getParent().removeExtensibilityElement((ExtensibilityElement) bType);
                wss10 = SecurityPolicyModelHelper.getWss10(par);
                if (wss10 != null) {
                    copyto.addExtensibilityElement((ExtensibilityElement) wss10.copy(copyto));
                    wss10.getParent().removeExtensibilityElement(wss10);
                }
                wss11 = SecurityPolicyModelHelper.getWss11(par);
                if (wss11 != null) {
                    copyto.addExtensibilityElement((ExtensibilityElement) wss11.copy(copyto));
                    wss11.getParent().removeExtensibilityElement(wss11);
                }
                trust = SecurityPolicyModelHelper.getTrust(par, configVersion);
                if (trust != null) {
                    copyto.addExtensibilityElement((ExtensibilityElement) trust.copy(copyto));
                    trust.getParent().removeExtensibilityElement(trust);
                }                
                for (int suppTokenType=0; suppTokenType < 3; suppTokenType++) {
                    ExtensibilityElement suppToken = 
                            (ExtensibilityElement) SecurityTokensModelHelper.getSupportingToken(par, suppTokenType);
                    if (suppToken == null) continue;
                    copyto.addExtensibilityElement((ExtensibilityElement) suppToken.copy(copyto));
                    suppToken.getParent().removeExtensibilityElement(suppToken);
                }
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
        
    }    
}
