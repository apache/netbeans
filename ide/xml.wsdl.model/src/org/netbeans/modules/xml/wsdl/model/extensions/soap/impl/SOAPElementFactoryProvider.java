/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

/**
 * @author Nam Nguyen
 * @author rico
 */
public class SOAPElementFactoryProvider {
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class BindingFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.BINDING.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPBindingImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class AddressFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.ADDRESS.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPAddressImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class BodyFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.BODY.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPBodyImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class HeaderFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.HEADER.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPHeaderImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class HeaderFaultFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.HEADER_FAULT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPHeaderFaultImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class OperationFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.OPERATION.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPOperationImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class FaultFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.FAULT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPFaultImpl(context.getModel(), element);
        }
    }
}
