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

package org.netbeans.modules.websvc.wsitmodelext.addressing;

import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Address10Impl;
import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Addressing10EndpointReferenceImpl;
import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Addressing10MetadataImpl;
import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Addressing10ReferencePropertiesImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Addressing10AnonymousImpl;


public class Addressing10Factories {

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class EndpointReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(Addressing10QName.ENDPOINTREFERENCE.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Addressing10EndpointReferenceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class AnonymousFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(Addressing10QName.ANONYMOUS.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Addressing10AnonymousImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProviders({@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class), @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)})
    public static class Address10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(Addressing10QName.ADDRESS.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Address10Impl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class Addressing10MetadataFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(Addressing10QName.ADDRESSINGMETADATA.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Addressing10MetadataImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class Addressing10ReferencePropertiesFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(Addressing10QName.REFERENCEPROPERTIES.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Addressing10ReferencePropertiesImpl(context.getModel(), element);
        }
    }    
}
