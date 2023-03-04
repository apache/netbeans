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

package org.netbeans.modules.websvc.wsitmodelext.rm;

import org.netbeans.modules.websvc.wsitmodelext.rm.impl.*;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;

public class RMFactories {

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RMAssertionFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            HashSet<QName> set = new HashSet<QName>();
            for (ConfigVersion cfgVersion : ConfigVersion.values()) {
                set.add(RMQName.RMASSERTION.getQName(cfgVersion));
            }
            return Collections.unmodifiableSet(set);
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RMAssertionImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class AcknowledgementIntervalFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.ACKNOWLEDGEMENTINTERVAL.getQName(ConfigVersion.CONFIG_1_0));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AcknowledgementIntervalImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class DeliveryAssuranceFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.DELIVERYASSURANCE.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new DeliveryAssuranceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class ExactlyOnceFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.EXACTLYONCE.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ExactlyOnceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class AtMostOnceFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.ATMOSTONCE.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AtMostOnceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class AtLeastOnceFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.ATLEASTONCE.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AtLeastOnceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class InOrderFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.INORDER.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new InOrderImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SequenceSTRFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.SEQUENCESTR.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SequenceSTRImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class SequenceTransportSecurityFactory extends ElementFactory {

        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMQName.SEQUENCETRANSPORTSECURITY.getQName(ConfigVersion.CONFIG_1_3));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SequenceTransportSecurityImpl(context.getModel(), element);
        }
    }
    
}
