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

package org.netbeans.modules.websvc.wsitmodelext.transport;

import org.netbeans.modules.websvc.wsitmodelext.transport.impl.OptimizedTCPTransportImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.transport.impl.AutomaticallySelectFastInfosetImpl;
import org.netbeans.modules.websvc.wsitmodelext.transport.impl.AutomaticallySelectOptimalTransportImpl;
import org.netbeans.modules.websvc.wsitmodelext.transport.impl.OptimizedFastInfosetSerializationImpl;

public class TransportFactories {

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class OptimizedFastInfosetSerialization extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(FIQName.OPTIMIZEDFASTINFOSETSERIALIZATION.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new OptimizedFastInfosetSerializationImpl(context.getModel(), element);
        }
    }   

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class AutomaticallySelectFastInfoset extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(FIQName.AUTOMATICALLYSELECTFASTINFOSET.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AutomaticallySelectFastInfosetImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class OptimizedTCPTransport extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TCPQName.OPTIMIZEDTCPTRANSPORT.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new OptimizedTCPTransportImpl(context.getModel(), element);
        }
    }   

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class AutomaticallySelectOptimalTransport extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TCPQName.AUTOMATICALLYSELECTOPTIMALTRANSPORT.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AutomaticallySelectOptimalTransportImpl(context.getModel(), element);
        }
    }
    
}
