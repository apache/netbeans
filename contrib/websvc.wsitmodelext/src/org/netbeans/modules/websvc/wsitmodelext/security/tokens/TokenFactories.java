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

package org.netbeans.modules.websvc.wsitmodelext.security.tokens;

import org.netbeans.modules.websvc.wsitmodelext.security.tokens.impl.*;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;
import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;

public class TokenFactories {

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SupportingTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SUPPORTINGTOKENS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SupportingTokensImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SignedSupportingTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SIGNEDSUPPORTINGTOKENS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SignedSupportingTokensImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class EndorsingSupportingTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.ENDORSINGSUPPORTINGTOKENS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EndorsingSupportingTokensImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class EncryptedSupportingTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.ENCRYPTEDSUPPORTINGTOKENS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EncryptedSupportingTokensImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class EndorsingEncryptedSupportingTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.ENDORSINGENCRYPTEDSUPPORTINGTOKENS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EndorsingEncryptedSupportingTokensImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SignedEncryptedSupportingTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SIGNEDENCRYPTEDSUPPORTINGTOKENS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SignedEncryptedSupportingTokensImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SignedEndorsingEncryptedSupportingTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SIGNEDENDORSINGENCRYPTEDSUPPORTINGTOKENS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SignedEndorsingEncryptedSupportingTokensImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SignedEndorsingSupportingTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SIGNEDENDORSINGSUPPORTINGTOKENS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SignedEndorsingSupportingTokensImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class HttpsTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.HTTPSTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new HttpsTokenImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class InitiatorTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.INITIATORTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new InitiatorTokenImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SignatureTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SIGNATURETOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SignatureTokenImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class EncryptionTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.ENCRYPTIONTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EncryptionTokenImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class IssuedTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.ISSUEDTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new IssuedTokenImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class KerberosTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.KERBEROSTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new KerberosTokenImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class ProtectionTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.PROTECTIONTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ProtectionTokenImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class TransportTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.TRANSPORTTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TransportTokenImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RecipientTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.RECIPIENTTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RecipientTokenImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RelTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.RELTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RelTokenImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SamlTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SAMLTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SamlTokenImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SecureConversationTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SECURECONVERSATIONTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SecureConversationTokenImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SecurityContextTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SECURITYCONTEXTTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SecurityContextTokenImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SpnegoContextTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SPNEGOCONTEXTTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SpnegoContextTokenImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class UsernameTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.USERNAMETOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new UsernameTokenImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class X509TokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.X509TOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new X509TokenImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssUsernameToken10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSUSERNAMETOKEN10.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssUsernameToken10Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssUsernameToken11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSUSERNAMETOKEN11.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssUsernameToken11Impl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class IssuerFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.ISSUER.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new IssuerImpl(context.getModel(), element);
        }
    }    

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RequireDerivedKeysFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.REQUIREDERIVEDKEYS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireDerivedKeysImpl(context.getModel(), element);
        }
    }    

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RequireExternalReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.REQUIREEXTERNALREFERENCE.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireExternalReferenceImpl(context.getModel(), element);
        }
    }    

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RequireInternalReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.REQUIREINTERNALREFERENCE.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireInternalReferenceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RequireKeyIdentifierReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.REQUIREKEYIDENTIFIERREFERENCE.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireKeyIdentifierReferenceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RequireIssuerSerialReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.REQUIREISSUERSERIALREFERENCE.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireIssuerSerialReferenceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RequireThumbprintReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.REQUIRETHUMBPRINTREFERENCE.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireThumbprintReferenceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RequireEmbeddedTokenReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.REQUIREEMBEDDEDTOKENREFERENCE.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireEmbeddedTokenReferenceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RequireExternalUriReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.REQUIREEXTERNALURIREFERENCE.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireExternalUriReferenceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SC10SecurityContextTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SC10SECURITYCONTEXTTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SC10SecurityContextTokenImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssX509V1Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSX509V1TOKEN10.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509V1Token10Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssX509V3Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSX509V3TOKEN10.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509V3Token10Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssX509Pkcs7Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSX509PKCS7TOKEN10.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509Pkcs7Token10Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssX509PkiPathV1Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSX509PKIPATHV1TOKEN10.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509PkiPathV1Token10Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssX509V1Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSX509V1TOKEN11.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509V1Token11Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssX509V3Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSX509V3TOKEN11.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509V3Token11Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssX509Pkcs7Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSX509PKCS7TOKEN11.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509Pkcs7Token11Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssX509PkiPathV1Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSX509PKIPATHV1TOKEN11.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509PkiPathV1Token11Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssSamlV10Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSSAMLV10TOKEN10.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssSamlV10Token10Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssSamlV11Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSSAMLV11TOKEN10.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssSamlV11Token10Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssSamlV10Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSSAMLV10TOKEN11.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssSamlV10Token11Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssSamlV11Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSSAMLV11TOKEN11.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssSamlV11Token11Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssSamlV20Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSSAMLV20TOKEN11.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssSamlV20Token11Impl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssRelV10Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSRELV10TOKEN10.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssRelV10Token10Impl(context.getModel(), element);
        }
    }    

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssRelV20Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSRELV20TOKEN10.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssRelV20Token10Impl(context.getModel(), element);
        }
    }    

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssRelV10Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSRELV10TOKEN11.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssRelV10Token11Impl(context.getModel(), element);
        }
    }    

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssRelV20Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSRELV20TOKEN11.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssRelV20Token11Impl(context.getModel(), element);
        }
    }    

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssKerberosV5ApReqToken11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSKERBEROSV5APREQTOKEN11.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssKerberosV5ApReqToken11Impl(context.getModel(), element);
        }
    }    

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class WssGssKerberosV5ApReqToken11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSSGSSKERBEROSV5APREQTOKEN11.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssGssKerberosV5ApReqToken11Impl(context.getModel(), element);
        }
    }    

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class HashPasswordFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
           HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.HASHPASSWORD.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new HashPasswordImpl(context.getModel(), element);
        }
    }    
    
}
