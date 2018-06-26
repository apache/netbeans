/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.websvc.wsitmodelext.security;

import org.netbeans.modules.websvc.wsitmodelext.security.impl.*;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.impl.*;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;

public class SecurityPolicyFactories {

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class Wss11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSS11.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Wss11Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class Wss10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.WSS10.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Wss10Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MustSupportRefEmbeddedTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.MUSTSUPPORTREFEMBEDDEDTOKEN.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportRefEmbeddedTokenImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MustSupportRefEncryptedKeyFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.MUSTSUPPORTREFENCRYPTEDKEY.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportRefEncryptedKeyImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MustSupportRefExternalURIFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.MUSTSUPPORTREFEXTERNALURI.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportRefExternalURIImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MustSupportRefIssuerSerialFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.MUSTSUPPORTREFISSUERSERIAL.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportRefIssuerSerialImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MustSupportRefKeyIdentifierFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.MUSTSUPPORTREFKEYIDENTIFIER.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportRefKeyIdentifierImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MustSupportRefThumbprintFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.MUSTSUPPORTREFTHUMBPRINT.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportRefThumbprintImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RequireSignatureConfirmationFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.REQUIRESIGNATURECONFIRMATION.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireSignatureConfirmationImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RequestSecurityTokenTemplateFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.REQUESTSECURITYTOKENTEMPLATE.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequestSecurityTokenTemplateImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class Trust10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.TRUST10.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Trust10Impl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class Trust13Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.TRUST13.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Trust13Impl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RequireServerEntropyFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.REQUIRESERVERENTROPY.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireServerEntropyImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RequireClientEntropyFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.REQUIRECLIENTENTROPY.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireClientEntropyImpl(context.getModel(), element);
        }
    }
 
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MustSupportIssuedTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.MUSTSUPPORTISSUEDTOKENS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportIssuedTokensImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MustSupportClientChallengeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.MUSTSUPPORTCLIENTCHALLENGE.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportClientChallengeImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MustSupportServerChallengeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.MUSTSUPPORTSERVERCHALLENGE.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportServerChallengeImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class IncludeTimestampFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.INCLUDETIMESTAMP.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new IncludeTimestampImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class OnlySignEntireHeadersAndBodyFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.ONLYSIGNENTIREHEADERSANDBODY.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new OnlySignEntireHeadersAndBodyImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class ProtectTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.PROTECTTOKENS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ProtectTokensImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class EncryptSignatureFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.ENCRYPTSIGNATURE.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EncryptSignatureImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class EncryptBeforeSigningFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.ENCRYPTBEFORESIGNING.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EncryptBeforeSigningImpl(context.getModel(), element);
        }
    }    
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SignedElementsFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SIGNEDELEMENTS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SignedElementsImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SignedPartsFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SIGNEDPARTS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SignedPartsImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class EncryptedElementsFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.ENCRYPTEDELEMENTS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EncryptedElementsImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class EncryptedPartsFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.ENCRYPTEDPARTS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EncryptedPartsImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class HeaderFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.HEADER.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new HeaderImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class BodyFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.BODY.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new BodyImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class AttachmentsFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.ATTACHMENTS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AttachmentsImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class XPathFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.XPATH.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new XPathImpl(context.getModel(), element);
        }
    }
    
   @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
   public static class TransportBindingFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.TRANSPORTBINDING.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TransportBindingImpl(context.getModel(), element);
        }
    }

   @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
   public static class SymmetricBindingFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.SYMMETRICBINDING.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SymmetricBindingImpl(context.getModel(), element);
        }
    }    

   @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
   public static class AsymmetricBindingFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.ASYMMETRICBINDING.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AsymmetricBindingImpl(context.getModel(), element);
        }
    }

   @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
   public static class LayoutFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.LAYOUT.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LayoutImpl(context.getModel(), element);
        }
    }
   
   @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
   public static class StrictFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.STRICT.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new StrictImpl(context.getModel(), element);
        }
    }

   @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
   public static class LaxFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.LAX.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LaxImpl(context.getModel(), element);
        }
    }

   @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
   public static class LaxTsFirstFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.LAXTSFIRST.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LaxTsFirstImpl(context.getModel(), element);
        }
    }

   @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
   public static class LaxTsLastFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.LAXTSLAST.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LaxTsLastImpl(context.getModel(), element);
        }
    }

   @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
   public static class RequiredElementsFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.REQUIREDELEMENTS.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequiredElementsImpl(context.getModel(), element);
        }
    }

   @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
   public static class BootstrapPolicyFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(SecurityPolicyQName.BOOTSTRAPPOLICY.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new BootstrapPolicyImpl(context.getModel(), element);
        }
    }
}
