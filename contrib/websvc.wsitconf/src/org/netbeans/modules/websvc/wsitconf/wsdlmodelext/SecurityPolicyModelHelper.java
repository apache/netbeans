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
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10QName;
import org.netbeans.modules.websvc.wsitmodelext.security.RequiredElements;
import org.netbeans.modules.websvc.wsitmodelext.security.TrustElement;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.security.listmodels.*;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.KeyStore;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.TrustStore;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RequireDerivedKeys;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RequireInternalReference;
import org.netbeans.modules.websvc.wsitmodelext.security.AsymmetricBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.Body;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.EncryptedElements;
import org.netbeans.modules.websvc.wsitmodelext.security.EncryptedParts;
import org.netbeans.modules.websvc.wsitmodelext.security.Header;
import org.netbeans.modules.websvc.wsitmodelext.security.Lax;
import org.netbeans.modules.websvc.wsitmodelext.security.LaxTsFirst;
import org.netbeans.modules.websvc.wsitmodelext.security.LaxTsLast;
import org.netbeans.modules.websvc.wsitmodelext.security.Layout;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.SignedElements;
import org.netbeans.modules.websvc.wsitmodelext.security.SignedParts;
import org.netbeans.modules.websvc.wsitmodelext.security.Strict;
import org.netbeans.modules.websvc.wsitmodelext.security.SymmetricBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.TransportBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.Trust10;
import org.netbeans.modules.websvc.wsitmodelext.security.Wss10;
import org.netbeans.modules.websvc.wsitmodelext.security.Wss11;
import org.netbeans.modules.websvc.wsitmodelext.security.WssElement;
import org.netbeans.modules.websvc.wsitmodelext.security.XPath;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.EncryptBeforeSigning;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.EncryptSignature;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.IncludeTimestamp;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportIssuedTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportRefEncryptedKey;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportRefIssuerSerial;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportRefKeyIdentifier;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportRefThumbprint;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.OnlySignEntireHeadersAndBody;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.RequireClientEntropy;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.RequireServerEntropy;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.RequireSignatureConfirmation;
import org.netbeans.modules.xml.wsdl.model.*;
import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMQName;
import org.netbeans.modules.websvc.wsitmodelext.security.Attachments;
import org.netbeans.modules.websvc.wsitmodelext.security.Trust13;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.ProtectTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RequireIssuerSerialReference;

/**
 *
 * @author Martin Grebac
 */
public class SecurityPolicyModelHelper {
    
    private static HashMap<ConfigVersion, SecurityPolicyModelHelper> instances = 
            new HashMap<ConfigVersion, SecurityPolicyModelHelper>();

    private ConfigVersion configVersion = ConfigVersion.getDefault();
    
    /**
     * Creates a new instance of SecurityPolicyModelHelper
     */
    private SecurityPolicyModelHelper(ConfigVersion configVersion) {
        this.configVersion = configVersion;
    }

    public static final synchronized SecurityPolicyModelHelper getInstance(ConfigVersion configVersion) {
        SecurityPolicyModelHelper instance = instances.get(configVersion);
        if (instance == null) {
            instance = new SecurityPolicyModelHelper(configVersion);
            instances.put(configVersion, instance);
        }
        return instance;
    }
    
    // checks if Security is enabled in the config wsdl on specified element (Binding/Operation/Message)
    public static boolean isSecurityEnabled(WSDLComponent c) {
        Policy p = PolicyModelHelper.getPolicyForElement(c);
        if (p != null) {
            ExtensibilityElement secElem = getSecurityBindingTypeElement(c);
            return (secElem != null);
        }
        return false;
    }

    public void disableSecurity(WSDLComponent c, boolean removeStoreConfig) {
        assert ((c instanceof Binding) || (c instanceof BindingOperation));
        setSecurityBindingType(c, null);
        SecurityTokensModelHelper.getInstance(configVersion).setSupportingTokens(c, null, SecurityTokensModelHelper.NONE);
        if (c instanceof Binding) {
            ProprietarySecurityPolicyModelHelper.setStreamingSecurity((Binding)c, true);
        }
        disableWss(c);
        disableTrust(c);
        removeTargets(c);
        Policy p = PolicyModelHelper.getPolicyForElement(c);
        if ((p != null) && (removeStoreConfig)) {
            KeyStore ks = PolicyModelHelper.getTopLevelElement(p, KeyStore.class,false);
            TrustStore ts = PolicyModelHelper.getTopLevelElement(p, TrustStore.class,false);
            if (ks != null) PolicyModelHelper.removeElement(ks);
            if (ts != null) PolicyModelHelper.removeElement(ts);
        }
        if (c instanceof Binding) {
            Binding b = (Binding)c;
            Collection<BindingOperation> ops = b.getBindingOperations();
            for (BindingOperation op : ops) {
                disableSecurity(op, removeStoreConfig);
            }
        } else {
            BindingOperation bop = (BindingOperation)c;
            BindingInput bi = bop.getBindingInput();
            BindingOutput bo = bop.getBindingOutput();
            if (bi != null) PolicyModelHelper.removePolicyForElement(bi);
            if (bo != null) PolicyModelHelper.removePolicyForElement(bo);
        }
        RMModelHelper rmh = RMModelHelper.getInstance(configVersion);
        if (rmh.isRMEnabled(c)) {
            rmh.setSequenceBinding((Binding) c, null);        
        }
        PolicyModelHelper.cleanPolicies(c);
    }
    
    public WssElement enableWss(WSDLComponent c, boolean wss11) {
    
        if (c == null) return null;
        
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
                
        if ((c instanceof Binding) || 
            (c instanceof BindingOperation) || 
            (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
            c = pmh.createPolicy(c, true);
        }
        
        if (wss11) {
            if (isWss10(c)) {
                disableWss(c);
            }
            if (!isWss11(c)) {
                return pmh.createElement(c, SecurityPolicyQName.WSS11.getQName(configVersion), Wss11.class, false);
            } else {
                return getWss11(c);
            }
        } else {
            if (isWss11(c)) {
                disableWss(c);
            }
            if (!isWss10(c)) {
                return pmh.createElement(c, SecurityPolicyQName.WSS10.getQName(configVersion), Wss10.class, false);
            } else {
                return getWss10(c);
            }
        }
    }
    
    public TrustElement enableTrust(WSDLComponent c, ConfigVersion cfgVersion) {
        if (c == null) return null;        
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
        
        if ((c instanceof Binding) || 
            (c instanceof BindingOperation) || 
            (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
            c = pmh.createPolicy(c, true);
        }
        if (!isTrust(c, cfgVersion)) {
            if (cfgVersion == ConfigVersion.CONFIG_1_0) {
                return pmh.createElement(c, SecurityPolicyQName.TRUST10.getQName(configVersion), Trust10.class, false);
            } else {
                return pmh.createElement(c, SecurityPolicyQName.TRUST13.getQName(configVersion), Trust13.class, false);
            }
        } else {
            return getTrust(c, cfgVersion);
        }
    }

    // disables Wss in the config wsdl on specified binding
    public void disableWss(WSDLComponent c) {
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
        WSDLModel model = c.getModel();
        if ((c instanceof Binding) || 
            (c instanceof BindingOperation) || 
            (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
            c = pmh.createPolicy(c, true);
        }
        WssElement wss10 = getWss10(c);
        WssElement wss11 = getWss11(c);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (wss10 != null) {
                wss10.getParent().removeExtensibilityElement(wss10);
            }
            if (wss11 != null) {
                wss11.getParent().removeExtensibilityElement(wss11);
            }
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }
    
    /* Disables Trust in the config wsdl on specified component
     */
    public void disableTrust(WSDLComponent c) {
        WSDLModel model = c.getModel();
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
        if ((c instanceof Binding) || 
            (c instanceof BindingOperation) || 
            (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
            c = pmh.createPolicy(c, true);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        TrustElement trust = getTrust(c, configVersion);
        try {
            if (trust != null) {
                trust.getParent().removeExtensibilityElement(trust);
            }
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }

    static boolean isWss10(WSDLComponent c) {
        return getWss10(c) != null;
    }

    public static boolean isWss11(WSDLComponent c) {
        return getWss11(c) != null;
    }

    static boolean isTrust(WSDLComponent c, ConfigVersion cfgVersion) {
        return getTrust(c, cfgVersion) != null;
    }

    static Wss10 getWss10(WSDLComponent c) {
        if ((c instanceof Binding) || (c instanceof BindingOperation)) {
            c = PolicyModelHelper.getPolicyForElement(c);
        }
        return PolicyModelHelper.getTopLevelElement(c, Wss10.class,false);
    }

    public static Wss11 getWss11(WSDLComponent c) {
        if ((c instanceof Binding) || (c instanceof BindingOperation)) {
            c = PolicyModelHelper.getPolicyForElement(c);
        }
        return PolicyModelHelper.getTopLevelElement(c, Wss11.class,false);
    }
    
    static TrustElement getTrust(WSDLComponent c, ConfigVersion cfgVersion) {
        if ((c instanceof Binding) || (c instanceof BindingOperation)) {
            c = PolicyModelHelper.getPolicyForElement(c);
        }
        if (cfgVersion.equals(ConfigVersion.CONFIG_1_0)) {
            return PolicyModelHelper.getTopLevelElement(c, Trust10.class,false);
        } else {
            return PolicyModelHelper.getTopLevelElement(c, Trust13.class,false);
        }
    }

    public static boolean isRequireSignatureConfirmation(WSDLComponent comp) {
        Wss11 wss11 = getWss11(comp);
        return isAttributeEnabled(wss11, RequireSignatureConfirmation.class);
    }

    /* Used to get values of attributes defined in WSS10/WSS11/TRUST10 assertions, for tokens, ...
     * first retrieves the Policy element and then element of class a underneath
     */
    static boolean isAttributeEnabled(ExtensibilityElement element, Class a) {
        if (element != null) {
            Policy p = PolicyModelHelper.getTopLevelElement(element, Policy.class,false);
            return (PolicyModelHelper.getTopLevelElement(p, a,false) != null);
        }
        return false;
    }

    public void enableIncludeTimestamp(WSDLComponent secBinding, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(secBinding, SecurityPolicyQName.INCLUDETIMESTAMP.getQName(configVersion), IncludeTimestamp.class, true);
        } else {
            PolicyModelHelper.removeElement(secBinding, IncludeTimestamp.class, true);
        }
    }
    
    public void enableEncryptSignature(WSDLComponent secBinding, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(secBinding, SecurityPolicyQName.ENCRYPTSIGNATURE.getQName(configVersion), EncryptSignature.class, true);
        } else {
            PolicyModelHelper.removeElement(secBinding, EncryptSignature.class, true);
        }
    }

    void enableSignEntireHeadersAndBody(WSDLComponent secBinding, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(secBinding, SecurityPolicyQName.ONLYSIGNENTIREHEADERSANDBODY.getQName(configVersion), OnlySignEntireHeadersAndBody.class, true);
        } else {
            PolicyModelHelper.removeElement(secBinding, OnlySignEntireHeadersAndBody.class, true);
        }
    }

    public void enableProtectTokens(WSDLComponent secBinding, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(secBinding, SecurityPolicyQName.PROTECTTOKENS.getQName(configVersion), ProtectTokens.class, true);
        } else {
            PolicyModelHelper.removeElement(secBinding, ProtectTokens.class, true);
        }
    }

    public void enableEncryptBeforeSigning(WSDLComponent secBinding, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(secBinding, SecurityPolicyQName.ENCRYPTBEFORESIGNING.getQName(configVersion), EncryptBeforeSigning.class, true);
        } else {
            PolicyModelHelper.removeElement(secBinding, EncryptBeforeSigning.class, true);
        }
    }

    public void enableMustSupportRefIssuerSerial(WssElement wss, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(wss, SecurityPolicyQName.MUSTSUPPORTREFISSUERSERIAL.getQName(configVersion), MustSupportRefIssuerSerial.class, true);
        } else {
            PolicyModelHelper.removeElement(wss, MustSupportRefIssuerSerial.class, true);
        }
    }    
    
    public void enableMustSupportRefKeyIdentifier(WssElement wss, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(wss, SecurityPolicyQName.MUSTSUPPORTREFKEYIDENTIFIER.getQName(configVersion), MustSupportRefKeyIdentifier.class, true);
        } else {
            PolicyModelHelper.removeElement(wss, MustSupportRefKeyIdentifier.class, true);
        }
    }

    public static boolean isRequireDerivedKeys(WSDLComponent token) {
        return isAttributeEnabled((ExtensibilityElement) token, RequireDerivedKeys.class);
    }

    public void enableRequireDerivedKeys(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(tokenType, SecurityPolicyQName.REQUIREDERIVEDKEYS.getQName(configVersion), RequireDerivedKeys.class, true);
        } else {
            PolicyModelHelper.removeElement(tokenType, RequireDerivedKeys.class, true);
        }
    }

    public void enableRequireIssuerSerialReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(tokenType, SecurityPolicyQName.REQUIREISSUERSERIALREFERENCE.getQName(configVersion), RequireIssuerSerialReference.class, true);
        } else {
            PolicyModelHelper.removeElement(tokenType, RequireIssuerSerialReference.class, true);
        }
    }

    public void enableRequireInternalReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(tokenType, SecurityPolicyQName.REQUIREINTERNALREFERENCE.getQName(configVersion), RequireInternalReference.class, true);
        } else {
            PolicyModelHelper.removeElement(tokenType, RequireInternalReference.class, true);
        }
    }

    public void enableMustSupportRefEncryptedKey(WssElement wss, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(wss, SecurityPolicyQName.MUSTSUPPORTREFENCRYPTEDKEY.getQName(configVersion), MustSupportRefEncryptedKey.class, true);
        } else {
            PolicyModelHelper.removeElement(wss, MustSupportRefEncryptedKey.class, true);
        }
    }

    public void enableMustSupportRefThumbprint(WssElement wss, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(wss, SecurityPolicyQName.MUSTSUPPORTREFTHUMBPRINT.getQName(configVersion), MustSupportRefThumbprint.class, true);
        } else {
            PolicyModelHelper.removeElement(wss, MustSupportRefThumbprint.class, true);
        }
    }
    
    public void enableRequireSignatureConfirmation(WssElement wss, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(wss, SecurityPolicyQName.REQUIRESIGNATURECONFIRMATION.getQName(configVersion), RequireSignatureConfirmation.class, true);
        } else {
            PolicyModelHelper.removeElement(wss, RequireSignatureConfirmation.class, true);
        }
    }

    // ----------- TRUST -------------------
    public void enableRequireClientEntropy(TrustElement trust, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(trust, SecurityPolicyQName.REQUIRECLIENTENTROPY.getQName(configVersion), RequireClientEntropy.class, true);
        } else {
            PolicyModelHelper.removeElement(trust, RequireClientEntropy.class, true);
        }
    }
    
    public void enableRequireServerEntropy(TrustElement trust, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(trust, SecurityPolicyQName.REQUIRESERVERENTROPY.getQName(configVersion), RequireServerEntropy.class, true);
        } else {
            PolicyModelHelper.removeElement(trust, RequireServerEntropy.class, true);
        }
    }

    public void enableMustSupportIssuedTokens(TrustElement trust, boolean enable) {
        if (enable) {
            PolicyModelHelper.getInstance(configVersion).createElement(trust, SecurityPolicyQName.MUSTSUPPORTISSUEDTOKENS.getQName(configVersion), MustSupportIssuedTokens.class, true);
        } else {
            PolicyModelHelper.removeElement(trust, MustSupportIssuedTokens.class, true);
        }
    }

    /*************** SIGN ENCRYPT TARGETS PARTS *******************/
    
    public static Vector<Vector> getTargets(WSDLComponent comp) {
        
        Vector<Vector> rows = new Vector<Vector>();
        
        Policy p = null;
        p = PolicyModelHelper.getPolicyForElement(comp);
        if (p == null) {
            return rows;
        }

        // ENCRYPTED PARTS FIRST
        List<Body> bodies = Collections.emptyList();
        List<Attachments> attchs = Collections.emptyList();
        List<Header> headers = Collections.emptyList();
        List<XPath> xpaths = Collections.emptyList();
        EncryptedParts encryptedParts = (EncryptedParts)PolicyModelHelper.getTopLevelElement(p, EncryptedParts.class,false);
        EncryptedElements encryptedElements = (EncryptedElements)PolicyModelHelper.getTopLevelElement(p, EncryptedElements.class,false);
        if (encryptedParts != null) {
            bodies = encryptedParts.getExtensibilityElements(Body.class);
            attchs = encryptedParts.getExtensibilityElements(Attachments.class);
            headers = encryptedParts.getExtensibilityElements(Header.class);
        }
        if (encryptedElements != null) {
            xpaths = encryptedElements.getExtensibilityElements(XPath.class);
        }
        // BODY
        if ((bodies != null) && (!bodies.isEmpty())) {
            Vector<Object> columns = new Vector<Object>();
            columns.add(TargetElement.DATA, new MessageBody());
            columns.add(TargetElement.SIGN, Boolean.FALSE);
            columns.add(TargetElement.ENCRYPT, Boolean.TRUE);
            columns.add(TargetElement.REQUIRE, Boolean.FALSE);
            rows.add(columns);
        }
        // ATTACHMENTS
        if ((attchs != null) && (!attchs.isEmpty())) {
            Vector<Object> columns = new Vector<Object>();
            columns.add(TargetElement.DATA, new MessageAttachments());
            columns.add(TargetElement.SIGN, Boolean.FALSE);
            columns.add(TargetElement.ENCRYPT, Boolean.TRUE);
            columns.add(TargetElement.REQUIRE, Boolean.FALSE);
            rows.add(columns);
        }
        // HEADERS
        for (Header h : headers) {
            MessageHeader header = getListModelForHeader(h);
            if (header != null) {
                Vector<Object> columns = new Vector<Object>();
                columns.add(TargetElement.DATA, header);
                columns.add(TargetElement.SIGN, Boolean.FALSE);
                columns.add(TargetElement.ENCRYPT, Boolean.TRUE);
                columns.add(TargetElement.REQUIRE, Boolean.FALSE);
                rows.add(columns);
            }
        }
        // XPATH ELEMENTS
        for (XPath x : xpaths) {
            MessageElement e = getListModelForXPath(x);
            if (e != null) {
                Vector<Object> columns = new Vector<Object>();
                columns.add(TargetElement.DATA, e);
                columns.add(TargetElement.SIGN, Boolean.FALSE);
                columns.add(TargetElement.ENCRYPT, Boolean.TRUE);
                columns.add(TargetElement.REQUIRE, Boolean.FALSE);
                rows.add(columns);
            }
        }
        
        SignedParts signedParts = (SignedParts)PolicyModelHelper.getTopLevelElement(p, SignedParts.class,false);
        SignedElements signedElements = (SignedElements)PolicyModelHelper.getTopLevelElement(p, SignedElements.class,false);
        if (signedParts != null) {
            bodies = signedParts.getExtensibilityElements(Body.class);
            attchs = signedParts.getExtensibilityElements(Attachments.class);
            headers = signedParts.getExtensibilityElements(Header.class);
        }
        if (signedElements != null) {
            xpaths = signedElements.getExtensibilityElements(XPath.class);
        }

        if ((bodies != null) && (!bodies.isEmpty())) {
            MessageBody body = new MessageBody();
            Vector existing = targetExists(rows, body);
            if (existing != null) {
                existing.set(TargetElement.SIGN, Boolean.TRUE);
            } else {
                Vector<Object> columns = new Vector<Object>();
                columns.add(TargetElement.DATA, body);
                columns.add(TargetElement.SIGN, Boolean.TRUE);
                columns.add(TargetElement.ENCRYPT, Boolean.FALSE);
                columns.add(TargetElement.REQUIRE, Boolean.FALSE);
                rows.add(columns);
            }
        }
        if ((attchs != null) && (!attchs.isEmpty())) {
            MessageAttachments att = new MessageAttachments();
            Vector existing = targetExists(rows, att);
            if (existing != null) {
                existing.set(TargetElement.SIGN, Boolean.TRUE);
            } else {
                Vector<Object> columns = new Vector<Object>();
                columns.add(TargetElement.DATA, att);
                columns.add(TargetElement.SIGN, Boolean.TRUE);
                columns.add(TargetElement.ENCRYPT, Boolean.FALSE);
                columns.add(TargetElement.REQUIRE, Boolean.FALSE);
                rows.add(columns);
            }
        }
        for (Header h : headers) {
            MessageHeader header = getListModelForHeader(h);
            if (header != null) {
                Vector existing = targetExists(rows, header);
                if (existing != null) {
                    existing.set(TargetElement.SIGN, Boolean.TRUE);
                } else {
                    Vector<Object> columns = new Vector<Object>();
                    columns.add(TargetElement.DATA, header);
                    columns.add(TargetElement.SIGN, Boolean.TRUE);
                    columns.add(TargetElement.ENCRYPT, Boolean.FALSE);
                    columns.add(TargetElement.REQUIRE, Boolean.FALSE);
                    rows.add(columns);
                }
            }
        }
        for (XPath x : xpaths) {
            MessageElement e = getListModelForXPath(x);
            if (e != null) {
                Vector existing = targetExists(rows, e);
                if (existing != null) {
                    existing.set(TargetElement.SIGN, Boolean.TRUE);
                } else {
                    Vector<Object> columns = new Vector<Object>();
                    columns.add(TargetElement.DATA, e);
                    columns.add(TargetElement.SIGN, Boolean.TRUE);
                    columns.add(TargetElement.ENCRYPT, Boolean.FALSE);
                    columns.add(TargetElement.REQUIRE, Boolean.FALSE);
                    rows.add(columns);
                }
            }
        }

        RequiredElements requiredElements = (RequiredElements)PolicyModelHelper.getTopLevelElement(p, RequiredElements.class,false);
        if (requiredElements != null) {
            xpaths = requiredElements.getExtensibilityElements(XPath.class);
        }
        for (XPath x : xpaths) {
            MessageElement e = getListModelForXPath(x);
            if (e != null) {
                Vector existing = targetExists(rows, e);
                if (existing != null) {
                    existing.set(TargetElement.REQUIRE, Boolean.TRUE);
                } else {
                    Vector<Object> columns = new Vector<Object>();
                    columns.add(TargetElement.DATA, e);
                    columns.add(TargetElement.SIGN, Boolean.FALSE);
                    columns.add(TargetElement.ENCRYPT, Boolean.FALSE);
                    columns.add(TargetElement.REQUIRE, Boolean.TRUE);
                    rows.add(columns);
                }
            }
        }

        return rows;
    }

    public static Vector targetExists(Vector<Vector> rows, TargetElement e) {
        for (Vector row : rows) {
            TargetElement te = (TargetElement) row.get(TargetElement.DATA);
            if (te.equals(e)) {
                return row;
            }
        }
        return null;
    }

    public void setTargets(WSDLComponent comp, Vector<Vector> targetModel) {

        if (comp == null) return;
        
        WSDLModel model = comp.getModel();

        Policy p = null;
        if (comp instanceof Policy) {
            p = (Policy) comp;
        } else {
            p = PolicyModelHelper.getPolicyForElement(comp);
        }
        EncryptedParts encryptedParts = (EncryptedParts) PolicyModelHelper.getTopLevelElement(p, EncryptedParts.class,false);
        SignedParts signedParts = (SignedParts) PolicyModelHelper.getTopLevelElement(p, SignedParts.class,false);
        EncryptedElements encryptedElements = (EncryptedElements) PolicyModelHelper.getTopLevelElement(p, EncryptedElements.class,false);
        SignedElements signedElements = (SignedElements) PolicyModelHelper.getTopLevelElement(p, SignedElements.class,false);
        RequiredElements requiredElements = (RequiredElements) PolicyModelHelper.getTopLevelElement(p, RequiredElements.class,false);
        WSDLComponentFactory wcf = model.getFactory();

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            WSDLComponent topLevel = null;
            if (encryptedParts != null) {
                topLevel = encryptedParts.getParent();
                topLevel.removeExtensibilityElement(encryptedParts);
                encryptedParts = null;
            }
            if (signedParts != null) {
                topLevel = signedParts.getParent();
                topLevel.removeExtensibilityElement(signedParts);
                signedParts = null;
            }
            if (encryptedElements != null) {
                topLevel = encryptedElements.getParent();
                topLevel.removeExtensibilityElement(encryptedElements);
                encryptedElements = null;
            }
            if (signedElements != null) {
                topLevel = signedElements.getParent();
                topLevel.removeExtensibilityElement(signedElements);
                signedElements = null;
            }
            if (requiredElements != null) {
                topLevel = requiredElements.getParent();
                topLevel.removeExtensibilityElement(requiredElements);
                requiredElements = null;
            }

            if (targetModel == null) {
                return;
            }
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            if (p == null) {
                topLevel = pmh.createPolicy(comp, true);
            } else if (!(comp instanceof Policy)) {
                topLevel = pmh.createTopExactlyOneAll(p);
            } else {
                topLevel = p;
            }
           
            boolean streamingSecurity = true;
            for (Vector v : targetModel) {
                TargetElement te = (TargetElement) v.get(TargetElement.DATA);
                boolean encrypt = ((Boolean)v.get(TargetElement.ENCRYPT)).booleanValue();
                boolean sign = ((Boolean)v.get(TargetElement.SIGN)).booleanValue();
                boolean require = ((Boolean)v.get(TargetElement.REQUIRE)).booleanValue();
                if (te instanceof MessageHeader) {    
                    if (encrypt) {
                        if (encryptedParts == null) {
                            encryptedParts = pmh.createElement(topLevel, SecurityPolicyQName.ENCRYPTEDPARTS.getQName(configVersion), EncryptedParts.class, false);
                        }
                        addHeaderElementForListItem(te.toString(), encryptedParts, wcf);
                    }
                    if (sign) {
                        if (signedParts == null) {
                            signedParts = pmh.createElement(topLevel, SecurityPolicyQName.SIGNEDPARTS.getQName(configVersion), SignedParts.class, false);
                        }
                        addHeaderElementForListItem(te.toString(), signedParts, wcf);                        
                    }
                } else if (te instanceof MessageElement) {
                    streamingSecurity = false;
                    if (encrypt) {
                        if (encryptedElements == null) {
                            encryptedElements = pmh.createElement(topLevel, SecurityPolicyQName.ENCRYPTEDELEMENTS.getQName(configVersion), EncryptedElements.class, false);
                        } 
                        addElementForListItem(te.toString(), encryptedElements, wcf);
                    }
                    if (sign) {
                        if (signedElements == null) {
                            signedElements = pmh.createElement(topLevel, SecurityPolicyQName.SIGNEDELEMENTS.getQName(configVersion), SignedElements.class, false);
                        }
                        addElementForListItem(te.toString(), signedElements, wcf);
                    }
                    if (require) {
                        if (requiredElements == null) {
                            requiredElements = pmh.createElement(topLevel, SecurityPolicyQName.REQUIREDELEMENTS.getQName(configVersion), RequiredElements.class, false);            
                        }
                        addElementForListItem(te.toString(), requiredElements, wcf);
                    }
                } else if (te instanceof MessageBody) {
                    if (encrypt) {
                        if (encryptedParts == null) {
                            encryptedParts = pmh.createElement(topLevel, SecurityPolicyQName.ENCRYPTEDPARTS.getQName(configVersion), EncryptedParts.class, false);
                        }
                        addBody(encryptedParts, wcf);
                    }
                    if (sign) {
                        if (signedParts == null) {
                            signedParts = pmh.createElement(topLevel, SecurityPolicyQName.SIGNEDPARTS.getQName(configVersion), SignedParts.class, false);
                        }
                        addBody(signedParts, wcf);
                    }
                } else if (te instanceof MessageAttachments) {
                    if (encrypt) {
                        if (encryptedParts == null) {
                            encryptedParts = pmh.createElement(topLevel, SecurityPolicyQName.ENCRYPTEDPARTS.getQName(configVersion), EncryptedParts.class, false);
                        }
                        addAttachments(encryptedParts, wcf);
                    }
                    if (sign) {
                        if (signedParts == null) {
                            signedParts = pmh.createElement(topLevel, SecurityPolicyQName.SIGNEDPARTS.getQName(configVersion), SignedParts.class, false);
                        }
                        addAttachments(signedParts, wcf);
                    }
                }
            }
            if ((comp instanceof BindingInput) || (comp instanceof BindingOutput) || (comp instanceof BindingFault)) {
                Binding b = (Binding) comp.getParent().getParent();
                ProprietarySecurityPolicyModelHelper.setStreamingSecurity(b, streamingSecurity);
            }
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }

    private static MessageHeader getListModelForHeader(Header h) {
        String name = h.getName();
        if ("To".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_TO);               //NOI18N
        if ("From".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_FROM);           //NOI18N
        if ("FaultTo".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_FAULTTO);     //NOI18N
        if ("ReplyTo".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_REPLYTO);     //NOI18N
        if ("MessageID".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_MESSAGEID); //NOI18N
        if ("RelatesTo".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_RELATESTO); //NOI18N
        if ("Action".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_ACTION);       //NOI18N
        if ("AckRequested".equals(name)) return new MessageHeader(MessageHeader.RM_ACKREQUESTED);   //NOI18N
        if ("SequenceAcknowledgement".equals(name)) return new MessageHeader(MessageHeader.RM_SEQUENCEACK);   //NOI18N
        if ("Sequence".equals(name)) return new MessageHeader(MessageHeader.RM_SEQUENCE);           //NOI18N
        if ("CreateSequence".equals(name)) return new MessageHeader(MessageHeader.RM_CREATESEQUENCE);           //NOI18N
        return null;
    }

    private static MessageElement getListModelForXPath(XPath x) {
        String xpath = x.getXPath();
        return new MessageElement(xpath);
    }
    
    private ExtensibilityElement addHeaderElementForListItem(String item, WSDLComponent c, WSDLComponentFactory wcf) {
        
        Header h = (Header)wcf.create(c, SecurityPolicyQName.HEADER.getQName(configVersion));
        if (MessageHeader.ADDRESSING_TO.equals(item)) {
            h.setName("To");        //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
        }
        if (MessageHeader.ADDRESSING_FROM.equals(item)) {
            h.setName("From");      //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
        }
        if (MessageHeader.ADDRESSING_FAULTTO.equals(item)) {
            h.setName("FaultTo");      //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
        }
        if (MessageHeader.ADDRESSING_REPLYTO.equals(item)) {
            h.setName("ReplyTo");   //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
        }
        if (MessageHeader.ADDRESSING_MESSAGEID.equals(item)) {
            h.setName("MessageID"); //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
        }
        if (MessageHeader.ADDRESSING_RELATESTO.equals(item)) {
            h.setName("RelatesTo"); //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
        }
        if (MessageHeader.ADDRESSING_ACTION.equals(item)) {
            h.setName("Action");    //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
        }
        String rmNspace = RMQName.RMASSERTION.getHeaderNamespaceUri(configVersion);
        if (MessageHeader.RM_ACKREQUESTED.equals(item)) {
            h.setName("AckRequested");  //NOI18N
            h.setNamespace(rmNspace);
        }
        if (MessageHeader.RM_SEQUENCEACK.equals(item)) {
            h.setName("SequenceAcknowledgement");   //NOI18N
            h.setNamespace(rmNspace);
        }
        if (MessageHeader.RM_SEQUENCE.equals(item)) {
            h.setName("Sequence");  //NOI18N
            h.setNamespace(rmNspace);
        }
        if (MessageHeader.RM_CREATESEQUENCE.equals(item)) {
            h.setName("CreateSequence");  //NOI18N
            h.setNamespace(rmNspace);
        }
        if (h != null) {
            c.addExtensibilityElement(h);
        }
        return h;
    }
    
    private ExtensibilityElement addElementForListItem(String item, WSDLComponent c, WSDLComponentFactory wcf) {
        XPath x = null;
        x = (XPath)wcf.create(c, SecurityPolicyQName.XPATH.getQName(configVersion));
        if (x != null) {
            c.addExtensibilityElement(x);
            x.setXPath(item);
        }
        return x;
    }

    private ExtensibilityElement addBody(WSDLComponent c, WSDLComponentFactory wcf) {
        Body b = null;
        b = (Body)wcf.create(c, SecurityPolicyQName.BODY.getQName(configVersion));
        c.addExtensibilityElement(b);
        return b;
    }

    private ExtensibilityElement addAttachments(WSDLComponent c, WSDLComponentFactory wcf) {
        Attachments a = null;
        a = (Attachments)wcf.create(c, SecurityPolicyQName.ATTACHMENTS.getQName(configVersion));
        c.addExtensibilityElement(a);
        return a;
    }
    
    /**************************** SECURITY BINDING TYPE *********************/

    /**
     * Returns security binding type element for specified element which can be either top level Binding, BindingOperation, ...
     * or sub-level like SecureConversationToken
     */ 
    public static ExtensibilityElement getSecurityBindingTypeElement(WSDLComponent c) {
        assert c != null;
        WSDLComponent p = c;
        
        if ((c instanceof Binding) || (c instanceof BindingOperation) || 
            (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
            p = PolicyModelHelper.getPolicyForElement(c);
        } else if (c instanceof BootstrapPolicy) {
            p = PolicyModelHelper.getTopLevelElement(c, Policy.class,false);
        }
        
        ExtensibilityElement ee = PolicyModelHelper.getTopLevelElement(p, SymmetricBinding.class,false);
        if (ee != null) return ee;
        ee = (AsymmetricBinding)PolicyModelHelper.getTopLevelElement(p, AsymmetricBinding.class,false);
        if (ee != null) return ee;
        ee = (TransportBinding)PolicyModelHelper.getTopLevelElement(p, TransportBinding.class,false);
        if (ee != null) return ee;
        
        return null;
    }

    WSDLComponent setSecurityBindingType(WSDLComponent c, String bindingType) {
        assert (c!=null);
        WSDLModel model = c.getModel();
        WSDLComponent secBindingType = null;
               
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
        All a = pmh.createPolicy(c, true);
        
        try {
            SymmetricBinding sb = (SymmetricBinding)PolicyModelHelper.getTopLevelElement(a, SymmetricBinding.class,false);
            AsymmetricBinding ab = (AsymmetricBinding)PolicyModelHelper.getTopLevelElement(a, AsymmetricBinding.class,false);
            TransportBinding tb = (TransportBinding)PolicyModelHelper.getTopLevelElement(a, TransportBinding.class,false);

            if (sb != null) sb.getParent().removeExtensibilityElement(sb);
            if (ab != null) ab.getParent().removeExtensibilityElement(ab);
            if (tb != null) tb.getParent().removeExtensibilityElement(tb);

            if (ComboConstants.SYMMETRIC.equals(bindingType)) {
                sb = pmh.createElement(a, SecurityPolicyQName.SYMMETRICBINDING.getQName(configVersion), SymmetricBinding.class, false);
                secBindingType = sb;
            }
            if (ComboConstants.ASYMMETRIC.equals(bindingType)) {
                ab = pmh.createElement(a, SecurityPolicyQName.ASYMMETRICBINDING.getQName(configVersion), AsymmetricBinding.class, false);
                secBindingType = ab;
            }
            if (ComboConstants.TRANSPORT.equals(bindingType)) {
                tb = pmh.createElement(a, SecurityPolicyQName.TRANSPORTBINDING.getQName(configVersion), TransportBinding.class, false);
                secBindingType = tb;
            }

        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
        
        return secBindingType;
    }

    /**
     * @param c
     * @param headers
     * @param rm
     * @return Policy Name created for these elements
     */
    public void setDefaultTargets(WSDLComponent c, boolean headers, boolean rm) {
        Vector<Vector> targets = new Vector<Vector>();

        Vector<Object> row = new Vector<Object>();
        MessageBody body = new MessageBody();
        row.add(TargetElement.DATA, body);
        row.add(TargetElement.SIGN, Boolean.TRUE);
        row.add(TargetElement.ENCRYPT, Boolean.TRUE);
        row.add(TargetElement.REQUIRE, Boolean.FALSE);
        targets.add(row);

        if (headers) {
            for (String s : MessageHeader.ADDRESSING_HEADERS) {
                row = new Vector<Object>();
                MessageHeader h = new MessageHeader(s);
                row.add(TargetElement.DATA, h);
                row.add(TargetElement.SIGN, Boolean.TRUE);
                row.add(TargetElement.ENCRYPT, Boolean.FALSE);
                row.add(TargetElement.REQUIRE, Boolean.FALSE);
                targets.add(row);
            }
//            if (rm) {
                for (String s : MessageHeader.RM_HEADERS) {
                    row = new Vector<Object>();
                    MessageHeader h = new MessageHeader(s);
                    row.add(TargetElement.DATA, h);
                    row.add(TargetElement.SIGN, Boolean.TRUE);
                    row.add(TargetElement.ENCRYPT, Boolean.FALSE);
                    row.add(TargetElement.REQUIRE, Boolean.FALSE);
                    targets.add(row);
  //              }
            }
        }

        setTargets(c, targets);
    }

    private void removeTargets(WSDLComponent c) {
        setTargets(c, null);
    }
    
    /********** Other binding attributes ****************/

    public static String getMessageLayout(WSDLComponent comp) {
        WSDLComponent layout = getMessageLayoutElement(comp);
        if (layout != null) {
            if (layout instanceof Strict) return ComboConstants.STRICT;
            if (layout instanceof Lax) return ComboConstants.LAX;
            if (layout instanceof LaxTsFirst) return ComboConstants.LAXTSFIRST;
            if (layout instanceof LaxTsLast) return ComboConstants.LAXTSLAST;            
        }
        return null;
    }
    
    private static WSDLComponent getMessageLayoutElement(WSDLComponent comp) {
        if ((comp instanceof Binding) || (comp instanceof BindingOperation)) {
            comp = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
        }
        if (comp == null) return null;
        Policy p = PolicyModelHelper.getTopLevelElement(comp, Policy.class,false);
        Layout l = PolicyModelHelper.getTopLevelElement(p, Layout.class,false);
        p = PolicyModelHelper.getTopLevelElement(l, Policy.class,false);
        if (p != null) {
            List<ExtensibilityElement> elements = p.getExtensibilityElements();
            if ((elements != null) && !(elements.isEmpty())) {
                ExtensibilityElement e = elements.get(0);
                return e;
            }
        }
        return null;
    }
    
    public static boolean isEncryptBeforeSigning(WSDLComponent c) {
        ExtensibilityElement e = getSecurityBindingTypeElement(c);
        if (e != null) {
            return isAttributeEnabled(e, EncryptBeforeSigning.class);
        }
        return false;
    }

    public static boolean isEncryptSignature(WSDLComponent c) {
        ExtensibilityElement e = getSecurityBindingTypeElement(c);
        if (e != null) {
            return isAttributeEnabled(e, EncryptSignature.class);
        }
        return false;
    }
    
    static boolean isSignEntireHeadersAndBody(WSDLComponent c) {
        ExtensibilityElement e = getSecurityBindingTypeElement(c);
        if (e != null) {
            return isAttributeEnabled(e, OnlySignEntireHeadersAndBody.class);
        }
        return false;
    }

    public static boolean isProtectTokens(WSDLComponent c) {
        ExtensibilityElement e = getSecurityBindingTypeElement(c);
        if (e != null) {
            return isAttributeEnabled(e, ProtectTokens.class);
        }
        return false;
   }

   public void setLayout(WSDLComponent c, String msgLayout) {
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            QName qnameToCreate = null;
            if (ComboConstants.STRICT.equals(msgLayout)) {
                qnameToCreate = SecurityPolicyQName.STRICT.getQName(configVersion);
            } else if (ComboConstants.LAX.equals(msgLayout)) {
                qnameToCreate = SecurityPolicyQName.LAX.getQName(configVersion);
            } else if (ComboConstants.LAXTSFIRST.equals(msgLayout)) {
                qnameToCreate = SecurityPolicyQName.LAXTSFIRST.getQName(configVersion);
            } else if (ComboConstants.LAXTSLAST.equals(msgLayout)) {
                qnameToCreate = SecurityPolicyQName.LAXTSLAST.getQName(configVersion);
            }

            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            Layout layout = pmh.createElement(c, SecurityPolicyQName.LAYOUT.getQName(configVersion), Layout.class, true);

            List<Policy> policies = layout.getExtensibilityElements(Policy.class);
            if ((policies != null) && (!policies.isEmpty())) {
                for (Policy pol : policies) {
                    layout.removeExtensibilityElement(pol);
                }
            }        
            Policy p = pmh.createElement(layout, PolicyQName.POLICY.getQName(configVersion), Policy.class, false);
            ExtensibilityElement e = (ExtensibilityElement) wcf.create(p, qnameToCreate);
            p.addExtensibilityElement(e);
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }    
    
}
