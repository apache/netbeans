/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.xml.wsdl.model.readwrite;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.Util;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.visitor.FindWSDLComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Nam Nguyen
 */
public class SimpleTest extends NbTestCase {
    
    public SimpleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public void testWrite() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/empty.wsdl");
        WSDLComponentFactory fact = model.getFactory();
        
        model.startTransaction();
        Definitions d = model.getDefinitions();
        d.setName("HelloService");
        d.setTargetNamespace("urn:HelloService/wsdl");
        Types types = fact.createTypes();
        d.setTypes(types);
        WSDLSchema wsdlSchema = fact.createWSDLSchema();
        types.addExtensibilityElement(wsdlSchema);
        wsdlSchema.getSchemaModel().getSchema().setTargetNamespace("urn:HelloService/wsdl");
        
        Message m1 = fact.createMessage();
        Message m2 = fact.createMessage();
        d.addMessage(m1); d.addMessage(m2);        
        m1.setName("HelloServiceSEI_sayHello");
        m2.setName("HelloServiceSEI_sayHelloResponse");
        Part p = fact.createPart();
        p.setName("String_1"); //TODO test setType
        p.setType(p.createSchemaReference(Util.getPrimitiveType("string"), GlobalType.class));
        m1.addPart(p);
        p = fact.createPart();
        p.setName("String_2"); 
        p.setType(p.createSchemaReference(Util.getPrimitiveType("string"), GlobalType.class));
        m1.addPart(p);
        p = fact.createPart();
        p.setName("result"); 
        p.setType(p.createSchemaReference(Util.getPrimitiveType("string"), GlobalType.class));
        m2.addPart(p);

        PortType pt = fact.createPortType();
        d.addPortType(pt);
        pt.setName("HelloServiceSEI");
        RequestResponseOperation op = fact.createRequestResponseOperation();
        pt.addOperation(op);
        op.setName("sayHello");
        op.setParameterOrder(Arrays.asList(new String[] {"String_1", "String_2"}));
        Input in = fact.createInput();
        op.setInput(in);
        in.setMessage(in.createReferenceTo(m1, Message.class));
        NamedComponentReference<Message> msgRef = in.getMessage();
              
        assertEquals("setTargetNamespace should declare prefix", "tns:HelloServiceSEI_sayHello", msgRef.getRefString());
        
        Output out = fact.createOutput();
        op.setOutput(out);
        out.setMessage(out.createReferenceTo(m2, Message.class));
        
        Binding b = fact.createBinding();
        d.addBinding(b);
        b.setName("HelloServiceSEIBinding");
        b.setType(b.createReferenceTo(pt, PortType.class));
        BindingOperation bo = fact.createBindingOperation();
        b.addBindingOperation(bo);
        bo.setName("sayHello");
        SOAPBinding sb = fact.createSOAPBinding();
        sb.setTransportURI("http://schemas.xmlsoap.org/soap/http");
        sb.setStyle(SOAPBinding.Style.RPC);
        b.addExtensibilityElement(sb);
        SOAPOperation soo = fact.createSOAPOperation();
        bo.addExtensibilityElement(soo);
        soo.setSoapAction("");
        
        assertTrue(b.getChildren().get(0) == sb);
        assertTrue(b.getChildren().get(1) == bo);
        
        BindingInput bin = fact.createBindingInput();
        bo.setBindingInput(bin);
        SOAPBody body = fact.createSOAPBody();
        bin.addExtensibilityElement(body);
        body.setUse(SOAPMessageBase.Use.LITERAL);
        body.setNamespace("urn:HelloService/wsdl");
        
        BindingOutput bout = fact.createBindingOutput();
        bo.setBindingOutput(bout);
        body = fact.createSOAPBody();
        bout.addExtensibilityElement(body);
        body.setUse(SOAPMessageBase.Use.LITERAL);
        body.setNamespace("urn:HelloService/wsdl");
        
        
        BindingFault bfault = fact.createBindingFault();
        bo.addBindingFault(bfault);
        SOAPFault sFault = fact.createSOAPFault();
        bfault.addExtensibilityElement(sFault);
        sFault.setUse(SOAPMessageBase.Use.LITERAL);
        sFault.setNamespace("urn:HelloService/wsdl");
        
        //test the order in which children were added in binding operation
        List children = bo.getChildren();
        Iterator it = children.iterator();
        assertEquals("binding.operation.soapOperation", soo, it.next());
        assertEquals("binding.operation.input", bin, it.next());
        assertEquals("binding.operation.output", bout, it.next());
        assertEquals("binding.operation.fault", bfault, it.next());
        
        Service service = fact.createService();
        d.addService(service);
        service.setName("HelloService");
        Port port = fact.createPort();
        service.addPort(port);
        port.setName("HelloServiceSEIPort");
        port.setBinding(port.createReferenceTo(b, Binding.class));
        SOAPAddress sad = fact.createSOAPAddress();
        port.addExtensibilityElement(sad);
        sad.setLocation("REPLACE_WITH_ACTUAL_URL");
        
        model.endTransaction();

        //Util.dumpToFile(model.getBaseDocument(), new File("C:\\temp\\HelloService.wsdl"));
        readAndCheck(model);
    }

    public void testRead() throws Exception {
        WSDLModel model = Util.loadWSDLModel(getTestResourcePath());
        readAndCheck(model);
    }
    
    private void readAndCheck(WSDLModel model) {
        Definitions d = model.getDefinitions();
        Collection<Message> messages = d.getMessages();
        assertEquals("read.message.count", 2, messages.size());
        Iterator<Message> it = messages.iterator(); it.next();
        Message m = it.next();
        AbstractDocumentComponent acm = (AbstractDocumentComponent) m;
        String prefix = acm.getPeer().getPrefix();
        assertTrue("wsdl prefix is not null or empty: "+prefix, prefix == null || prefix.equals(""));
        assertEquals("read.message.name", "HelloServiceSEI_sayHelloResponse", m.getName());
        List<Part> parts = new ArrayList(m.getParts());
        assertEquals("read.message.part.name", "result", parts.iterator().next().getName());
        assertEquals("string", parts.get(0).getType().get().getName());
        
        Collection<PortType> porttypes = d.getPortTypes();
        assertEquals("read.portType", 1, porttypes.size());
        PortType pt = porttypes.iterator().next();
        assertEquals("read.portType.name", "HelloServiceSEI", pt.getName());
        Operation op = pt.getOperations().iterator().next();
        assertTrue("read.portType.operation", op instanceof RequestResponseOperation);
        assertEquals("read.portType.operation.parameterOrder", "[String_1, String_2]", op.getParameterOrder().toString());
        
        Message m1 = d.getMessages().iterator().next();
        assertEquals("message[1].name", "HelloServiceSEI_sayHello", m1.getName());
        assertEquals("message[1].parts.count=2", 2, m1.getParts().size());

        String xpath0 = "/definitions/message[1]";
        WSDLComponent found = (WSDLComponent) new FindWSDLComponent().findComponent(model.getRootComponent(), xpath0);
        assertTrue(xpath0, found instanceof Message);
        Message m1x = (Message) found;
        assertEquals("write.xpath", m1, m1x);

        RequestResponseOperation rro = (RequestResponseOperation) op;
        assertEquals("operation.name", "sayHello", rro.getName());
        Input in = rro.getInput();
        assertEquals("portType.operation.input.message", messages.iterator().next(), in.getMessage().get());

        Output out = rro.getOutput();
        assertEquals("portType.operation.output.message", m.getName(), out.getMessage().get().getName());
        
        Binding b = d.getBindings().iterator().next();
        Collection<SOAPBinding> soapB = b.getExtensibilityElements(SOAPBinding.class);
        SOAPBinding sb = soapB.iterator().next();
        assertEquals("binding.soap.style", SOAPBinding.Style.RPC, sb.getStyle());
        assertEquals("binding.soap.uri", "http://schemas.xmlsoap.org/soap/http", sb.getTransportURI());

        BindingOperation bo = b.getBindingOperations().iterator().next();
        Collection<SOAPOperation> soapOps = bo.getExtensibilityElements(SOAPOperation.class);
        assertEquals("binding.soap.style", SOAPBinding.Style.RPC, soapOps.iterator().next().getStyle());
        assertEquals("binding.soap.uri", "", soapOps.iterator().next().getSoapAction());

        assertEquals("binding.type", pt, b.getType().get());
        assertEquals("binding.name", "HelloServiceSEIBinding", b.getName());
        assertEquals("sayHelloRequest", bo.getBindingInput().getInput().get().getName());
        SOAPBody body = (SOAPBody) bo.getBindingInput().getExtensibilityElements().iterator().next();
        assertEquals("binding.operation.input", SOAPBody.Use.LITERAL, body.getUse());
        SOAPBody body2 = (SOAPBody) bo.getBindingOutput().getExtensibilityElements().iterator().next();
        assertEquals("binding.operation.output", "urn:HelloService/wsdl", body2.getNamespace());
        
        Collection<BindingFault> bFaults =  bo.getBindingFaults();
        assertEquals("binding.operation.faults", 1, bFaults.size());
        BindingFault fault = bFaults.iterator().next();
        SOAPFault sfault = (SOAPFault)fault.getExtensibilityElements().iterator().next();
        assertEquals("binding.operation.fault", "urn:HelloService/wsdl", sfault.getNamespace());
        
        Service s = d.getServices().iterator().next();
        assertEquals("serice.name", "HelloService", s.getName());
        Port p = s.getPorts().iterator().next();
        assertEquals("service.port.name", "HelloServiceSEIPort", p.getName());
        Binding binding = p.getBinding().get();
        assertEquals("service.port.binding", b, binding);
        SOAPAddress soapAddress = p.getExtensibilityElements(SOAPAddress.class).iterator().next();
        assertEquals("service.port.soapAddress", "REPLACE_WITH_ACTUAL_URL", soapAddress.getLocation());
        
    }

    public String getTestResourcePath() {
        return "resources/HelloService.wsdl";
    }
    
    public void testPrefixPreference() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/empty.wsdl");
        model.startTransaction();
        model.getDefinitions().setTypes(model.getFactory().createTypes());
        model.endTransaction();
        assertNull(model.getDefinitions().getTypes().getPeer().getPrefix());
        assertNull(model.getDefinitions().getTypes().getPeer().getAttributeNode("xmlns:wsdl"));
        assertNull(model.getDefinitions().getTypes().getPeer().getAttributeNode("xmlns"));
        //Util.dumpToFile(model.getBaseDocument(), new File(getWorkDir(), "test.wsdl"));
    }
}
