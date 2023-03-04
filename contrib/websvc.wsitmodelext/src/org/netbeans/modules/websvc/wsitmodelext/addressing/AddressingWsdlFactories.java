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

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Addressing10WsdlUsingAddressingImpl;
import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Addressing13WsdlAddressingImpl;
import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Addressing13WsdlAnonymousResponsesImpl;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;


public class AddressingWsdlFactories {

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class UsingAddressingFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(Addressing10WsdlQName.USINGADDRESSING.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Addressing10WsdlUsingAddressingImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class AddressingFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            set.add(Addressing13WsdlQName.ADDRESSING.getQName(ConfigVersion.CONFIG_1_3));
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Addressing13WsdlAddressingImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class AnonymousResponsesFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            set.add(Addressing13WsdlQName.ANONYMOUSRESPONSES.getQName(ConfigVersion.CONFIG_1_3));
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Addressing13WsdlAnonymousResponsesImpl(context.getModel(), element);
        }
    }
    
}
