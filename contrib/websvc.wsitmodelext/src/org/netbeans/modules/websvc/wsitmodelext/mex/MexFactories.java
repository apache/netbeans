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

package org.netbeans.modules.websvc.wsitmodelext.mex;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.mex.impl.DialectImpl;
import org.netbeans.modules.websvc.wsitmodelext.mex.impl.IdentifierImpl;
import org.netbeans.modules.websvc.wsitmodelext.mex.impl.LocationImpl;
import org.netbeans.modules.websvc.wsitmodelext.mex.impl.MetadataImpl;
import org.netbeans.modules.websvc.wsitmodelext.mex.impl.MetadataReferenceImpl;
import org.netbeans.modules.websvc.wsitmodelext.mex.impl.MetadataSectionImpl;

public class MexFactories {

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class Metadata extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(MexQName.METADATA.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MetadataImpl(context.getModel(), element);
        }
    }   

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MetadataReference extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(MexQName.METADATAREFERENCE.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MetadataReferenceImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MetadataSection extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(MexQName.METADATASECTION.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MetadataSectionImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class Dialect extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(MexQName.DIALECT.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new DialectImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class Identifier extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(MexQName.IDENTIFIER.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new IdentifierImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class Location extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(MexQName.LOCATION.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LocationImpl(context.getModel(), element);
        }
    }
}
