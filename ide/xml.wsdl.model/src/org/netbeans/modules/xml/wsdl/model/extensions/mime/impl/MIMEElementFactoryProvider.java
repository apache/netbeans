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

package org.netbeans.modules.xml.wsdl.model.extensions.mime.impl;

import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEQName;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

/**
 *
 * @author jyang
 */
public class MIMEElementFactoryProvider {

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class ContentFactory extends ElementFactory {

        public Set<QName> getElementQNames() {
            return Collections.singleton(MIMEQName.CONTENT.getQName());
        }

        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MIMEContentImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MultipartRelatedFactory extends ElementFactory {

        public Set<QName> getElementQNames() {
            return Collections.singleton(MIMEQName.MULTIPART_RELATED.getQName());
        }

        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MIMEMultipartRelatedImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MimeXmlFactory extends ElementFactory {

        public Set<QName> getElementQNames() {
            return Collections.singleton(MIMEQName.MIME_XML.getQName());
        }

        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MIMEMimeXmlImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class Part extends ElementFactory {

        public Set<QName> getElementQNames() {
            return Collections.singleton(MIMEQName.PART.getQName());
        }

        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MIMEPartImpl(context.getModel(), element);
        }
    }
}
