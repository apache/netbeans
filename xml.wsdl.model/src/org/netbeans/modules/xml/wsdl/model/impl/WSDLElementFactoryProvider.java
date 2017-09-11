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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Nam Nguyen
 * @author rico
 */
public class WSDLElementFactoryProvider {
   
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class DefinitionsFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.DEFINITIONS.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            throw new UnsupportedOperationException("Root 'definitions' should be bootstrapped when WSDL model is created"); //NOI18N
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class BindingFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.BINDING.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new BindingImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class DocumentationFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.DOCUMENTATION.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new DocumentationImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class FaultFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.FAULT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            if (context instanceof BindingOperation) {
                return new BindingFaultImpl(context.getModel(), el);
            } else if (context instanceof Operation) {
                return new FaultImpl(context.getModel(), el);
            } else {
                throw new IllegalArgumentException("Wrong parent for 'fault'"); //NOI18N
            }
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class OperationFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.OPERATION.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getImpliedQName(el));
            if (context instanceof Binding) {
                return new BindingOperationImpl(context.getModel(), el);
            } else if (! (context instanceof PortType)) {
                throw new IllegalArgumentException("Wrong parent for 'operation'"); //NOI18N
            }
            
            //portType/operation
            NodeList list = el.getChildNodes();
            int in = 0, out = 0;
            for (int i=0; i<list.getLength(); i++) {
                if (in > 0 && out > 0) {
                    break;
                }
                Node n = list.item(i);
                if (!(n instanceof Element)) {
                    continue;
                }
                if (n.getLocalName().equals(WSDLQNames.INPUT.getQName().getLocalPart())) {
                    in = out == 0 ? 1 : 2;
                } else if (n.getLocalName().equals(WSDLQNames.OUTPUT.getQName().getLocalPart())) {
                    out = in == 0 ? 1 : 2;
                }
            }
            
            WSDLComponent ret = null;
            if (in == 0 && out > 0) {
                ret = new NotificationOperationImpl(context.getModel(), el);
            } else if (in > 0 && out == 0) {
                ret = new OneWayOperationImpl(context.getModel(), el);
            } else if (in > out) {
                ret = new SolicitResponseOperationImpl(context.getModel(), el);
            } else if (in < out) {
                ret = new RequestResponseOperationImpl(context.getModel(), el);
            } 
            return ret;
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class InputFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.INPUT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            if (context instanceof BindingOperation) {
                return new BindingInputImpl(context.getModel(), el);
            } else if (context instanceof Operation) {
                return new InputImpl(context.getModel(), el);
            } else {
                throw new IllegalArgumentException("Wrong parent for 'input'"); //NOI18N
            }
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class ImportFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.IMPORT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new ImportImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MessageFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.MESSAGE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new MessageImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class OutputFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.OUTPUT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            if (context instanceof BindingOperation) {
                return new BindingOutputImpl(context.getModel(), el);
            } else if (context instanceof Operation) {
                return new OutputImpl(context.getModel(), el);
            } else {
                throw new IllegalArgumentException("Wrong parent for 'output'"); //NOI18N
            }
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class PartFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.PART.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new PartImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class PortFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.PORT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new PortImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class PortTypeFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.PORTTYPE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new PortTypeImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class ServiceFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.SERVICE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new ServiceImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class TypesFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.TYPES.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new TypesImpl(context.getModel(), el);
        }
    }

    private static QName getImpliedQName(Element el) {
        String ns = el.getNamespaceURI();
        if (ns == null) { // this can happen if new element has not added to xdm tree
            ns = WSDLQNames.WSDL_NS_URI;
        }
        return new QName(ns, el.getLocalName());
    }
    
    private static void checkArgument(Set<QName> wqnames, QName qname) {
        checkArgument(wqnames.iterator().next(), qname);
    }

    private static void checkArgument(QName wqname, QName qname) {
        if (! wqname.equals(qname)) {
            throw new IllegalArgumentException("Invalid element "+qname.getLocalPart()); //NOI18N
        }
    }
}
