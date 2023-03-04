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

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.IssuerImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.KeyTypeImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.CertAliasImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.ContractImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.LifeTimeSTSImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.STSConfigurationServiceImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.ServiceProviderImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.ServiceProvidersImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.TokenTypeImpl;

public class ProprietaryTrustServiceFactories {

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class CertAliasFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.CERTALIAS.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new CertAliasImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class ContractFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.CONTRACT.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ContractImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class STSIssuerFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.ISSUER.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new IssuerImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class STSConfigurationFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.STSCONFIGURATION.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new STSConfigurationServiceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class ServiceProviderFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.SERVICEPROVIDER.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ServiceProviderImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class ServiceProvidersFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.SERVICEPROVIDERS.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ServiceProvidersImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class TokenTypeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.TOKENTYPE.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TokenTypeImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class KeyTypeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.KEYTYPE.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new KeyTypeImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class LifeTimeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.LIFETIME.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LifeTimeSTSImpl(context.getModel(), element);
        }
    }
}
