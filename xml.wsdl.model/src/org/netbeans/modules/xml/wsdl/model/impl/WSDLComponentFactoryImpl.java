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

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPAddressImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPBindingImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPBodyImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPFaultImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPHeaderFaultImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPHeaderImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.impl.SOAPOperationImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.impl.WSDLSchemaImpl;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class WSDLComponentFactoryImpl implements WSDLComponentFactory {
    
    private WSDLModel model;
    /** Creates a new instance of WSDLComponentFactoryImpl */
    public WSDLComponentFactoryImpl(WSDLModel model) {
        this.model = model;
    }
    
    public WSDLComponent create(Element element, WSDLComponent context) {
        ElementFactory factory = ElementFactoryRegistry.getDefault().get(
                Util.getQName(element, (WSDLComponentBase)context));
        return create(factory, element, context);
    }
    
    private WSDLComponent create(ElementFactory factory, Element element, WSDLComponent context) {
        if(factory != null ){
            return factory.create(context, element);
        } else {
            return new GenericExtensibilityElement(model, element);
        }
    }
    
    public WSDLComponent create(WSDLComponent parent, QName qName) {
       String q = qName.getPrefix();
       if (q == null || q.length() == 0) {
           q = qName.getLocalPart();
       } else {
           q = q + ":" + qName.getLocalPart();
       }

       ElementFactory factory = ElementFactoryRegistry.getDefault().get(qName);
       Element element = model.getDocument().createElementNS(qName.getNamespaceURI(), q);
       return create(factory, element, parent);
    }
    
    public Port createPort() {	 
         return new PortImpl(model);
     }	 
     	 
     public Part createPart() {	 
         return new PartImpl(model);
     }	 
     	 
     public Output createOutput() {	 
         return new OutputImpl(model);
     }	 
     	 
     public Binding createBinding() {	 
         return new BindingImpl(model);
     }	 
     	 
     public BindingFault createBindingFault() {	 
         return new BindingFaultImpl(model);
     }	 
     	 
     public BindingInput createBindingInput() {	 
         return new BindingInputImpl(model);
     }	 
     	 
     public BindingOperation createBindingOperation() {	 
         return new BindingOperationImpl(model);
     }	 
     	 
     public BindingOutput createBindingOutput() {	 
         return new BindingOutputImpl(model);
     }	 
     	 
     public Documentation createDocumentation() {	 
         return new DocumentationImpl(model);
     }	 
     	 
     public Fault createFault() {	 
         return new FaultImpl(model);
     }	 
     	 
     public Import createImport() {	 
         return new ImportImpl(model);
     }	 
     	 
     public Input createInput() {	 
         return new InputImpl(model);
     }	 
     	 
     public Message createMessage() {	 
         return new MessageImpl(model);
     }	 
     	 
     public OneWayOperation createOneWayOperation() {	 
         return new OneWayOperationImpl(model);
     }

     public NotificationOperation createNotificationOperation() {	 
         return new NotificationOperationImpl(model);
     }
     public RequestResponseOperation createRequestResponseOperation() {	 
         return new RequestResponseOperationImpl(model);
     }

     public SolicitResponseOperation createSolicitResponseOperation() {	 
         return new SolicitResponseOperationImpl(model);
     }
    public Types createTypes() {
        return new TypesImpl(model);
    }

    public Service createService() {
        return new ServiceImpl(model);
    }

    public PortType createPortType() {
        return new PortTypeImpl(model);
    }
    
    // SOAP
    
    public SOAPAddress createSOAPAddress() {
        return new SOAPAddressImpl(model);
    }

    public SOAPBinding createSOAPBinding() {
        return new SOAPBindingImpl(model);
    }

    public SOAPBody createSOAPBody() {
        return new SOAPBodyImpl(model);
    }

    public SOAPFault createSOAPFault() {
        return new SOAPFaultImpl(model);
    }

    public SOAPHeader createSOAPHeader() {
        return new SOAPHeaderImpl(model);
    }

    public SOAPHeaderFault createSOAPHeaderFault() {
        return new SOAPHeaderFaultImpl(model);
    }

    public SOAPOperation createSOAPOperation() {
        return new SOAPOperationImpl(model);
    }
    
    // XSD
    public WSDLSchema createWSDLSchema() {
        return new WSDLSchemaImpl(model);
    }
}

