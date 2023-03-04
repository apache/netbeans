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
package org.netbeans.modules.xml.wsdl.model.extensions.soap.validation;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderBase;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;

/**
 *
 * @author nang
 */
public class SOAPComponentVisitor implements SOAPComponent.Visitor {
    private static final String HTTP_DEFAULT_PORT_TOKEN = "${HttpDefaultPort}";
    private static final String HTTPS_DEFAULT_PORT_TOKEN = "${HttpsDefaultPort}";
    
    private final SOAPComponentValidator mValidator;
    private final Validation mValidation;
    private final List<ResultItem> results;

    SOAPComponentVisitor(SOAPComponentValidator validator, Validation validation) {
        mValidator = validator;
        mValidation = validation;
        results = new LinkedList<ResultItem>();
    }

    public List<ResultItem> getResultItems() {
        return results;
    }

    public void visit(WSDLModel wsdlModel) {
        Definitions defs = wsdlModel.getDefinitions();
        Iterator<Binding> bindings = defs.getBindings().iterator();
        while (bindings.hasNext()) {
            Binding binding = bindings.next();
            int numSoapBindings = binding.getExtensibilityElements(SOAPBinding.class).size();
            if (numSoapBindings > 0 && numSoapBindings != 1) {
                results.add(
                        new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        binding,
                        NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPBindingValidator.Only_one_binding_allowed")));
            }
            Iterator<SOAPBinding> soapBindings =
                    binding.getExtensibilityElements(SOAPBinding.class).iterator();
            while (soapBindings.hasNext()) {
                soapBindings.next().accept(this);
            }
            Iterator<BindingOperation> bindingOps =
                    binding.getBindingOperations().iterator();
            while (bindingOps.hasNext()) {
                BindingOperation bindingOp = bindingOps.next();
                List soapOpsList = bindingOp.getExtensibilityElements(SOAPOperation.class);
                Iterator<SOAPOperation> soapOps =
                        soapOpsList.iterator();
                while (soapOps.hasNext()) {
                    soapOps.next().accept(this);
                }

                if(soapOpsList.size() > 0) {
                    BindingInput bindingInput = bindingOp.getBindingInput();
                    if (bindingInput != null) {
                        visit(bindingInput);
                    }

                    BindingOutput bindingOutput = bindingOp.getBindingOutput();
                    if (bindingOutput != null) {
                        visit(bindingOutput);
                    }

                    Iterator<BindingFault> bindingFaults =
                            bindingOp.getBindingFaults().iterator();
                    while (bindingFaults.hasNext()) {
                        BindingFault bindingFault = bindingFaults.next();
                        int numSoapFaults = bindingFault.getExtensibilityElements(SOAPFault.class).size();
                        if (numSoapFaults == 0) {
                            results.add(
                                    new Validator.ResultItem(mValidator,
                                    Validator.ResultType.ERROR,
                                    bindingFault,
                                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPFaultValidator.No_soap_fault_defined")));
                        }
                        if (numSoapFaults > 0 && numSoapFaults != 1) {

                            results.add(
                                    new Validator.ResultItem(mValidator,
                                    Validator.ResultType.ERROR,
                                    bindingFault,
                                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPFaultValidator.Only_one_fault_allowed")));
                        }

                        Iterator<SOAPFault> soapFaults =
                                bindingFault.getExtensibilityElements(SOAPFault.class).iterator();
                        while(soapFaults.hasNext()) {
                            SOAPFault soapFault = (SOAPFault) soapFaults.next();   // should be only one defined
                            if (soapFault.getName() != null
                                    && !soapFault.getName().equals(bindingFault.getName()))  {
                                results.add(
                                    new Validator.ResultItem(mValidator,
                                    Validator.ResultType.ERROR,
                                    bindingFault,
                                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPFaultValidator.Fault_name_not_match")));
                            }
                            soapFault.accept(this);
                        }

                    }
                }
            }
        }
        Iterator<Service> services = defs.getServices().iterator();
        while (services.hasNext()) {
            Iterator<Port> ports = services.next().getPorts().iterator();
            while (ports.hasNext()) {
                Port port = ports.next();
                if(port.getBinding() != null) {
                    Binding binding = port.getBinding().get();
                    if(binding != null) {
                        int numRelatedSoapBindings = binding.getExtensibilityElements(SOAPBinding.class).size();
                        Iterator<SOAPAddress> soapAddresses = port.getExtensibilityElements(SOAPAddress.class).iterator();
                        if((numRelatedSoapBindings > 0) && (!soapAddresses.hasNext())){
                            results.add(
                                    new Validator.ResultItem(mValidator,
                                    Validator.ResultType.ERROR,
                                    port,
                                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPAddressValidator.Missing_SoapAddress")));
                        }

                        if(port.getExtensibilityElements(SOAPAddress.class).size() > 1){
                            results.add(
                                    new Validator.ResultItem(mValidator,
                                    Validator.ResultType.ERROR,
                                    port,
                                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPAddressValidator.Only_one_SoapAddress_allowed")));
                        }
                        while (soapAddresses.hasNext()) {
                            soapAddresses.next().accept(this);
                        }
                    }
                }
            }
        }
    }
    
    /////////////////////////////////////////////
    ////
    ////  SOAPComponent.Visitor interface
    ////
    /////////////////////////////////////////////
    
    public void visit(SOAPHeader header) {
        NamedComponentReference<Message> message = header.getMessage();
        if (message == null) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    header,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPHeaderValidator.Missing_message")));
        }
        
        String part = header.getPart();
        if (part == null) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    header,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPHeaderValidator.Missing_part")));
        }
        
        try {
            SOAPMessageBase.Use use = header.getUse();
            if (use == null) {
                results.add(
                        new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        header,
                        NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPHeaderValidator.Missing_use")));
            }
        } catch (Throwable th) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    header,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPHeaderValidator.Unsupported_header_use_attribute")));
        }
        
        Collection<String> encodingStyles = header.getEncodingStyles();
        if (encodingStyles != null) {
            // This is optional.  Don't verify contents at this point.
        }
        
        String namespace = header.getNamespace();
        if (namespace != null) {
            // This is optional.  We should verify that it is a valid URI, but
            // I don't want to be too restrictive at this point.
            
        }
    }
    
    public void visit(SOAPAddress address) {
        String location = address.getLocation();
        if (location == null) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    address,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPAddressValidator.Missing_location")));
            return;
        }
        
        ////////////////////////////////////////////////////////
        // GSR changed for Java EE Service Engine
        // As instructed by Jerry Waldorf.
        ////////////////////////////////////////////////////////
        if("REPLACE_WITH_ACTUAL_URL".equals(location)) {
            return;
        }
        
        ///////////////////////////////////////////////////////
        // Check for valid tokens for default HTTP and HTTPS port
        // Introduced to support clustering
        ////////////////////////////////////////////////////////
        
        if (location.indexOf(HTTP_DEFAULT_PORT_TOKEN, 6) > 0) {
            int colonIndex = -1;
            int contextStartIndex = -1;
            
            if (location.startsWith("http://")) {
                // look for ${HttpDefaultPort} token 
                colonIndex = location.indexOf(":", 7);
                contextStartIndex = location.indexOf("/", 7);
                
                if (HTTP_DEFAULT_PORT_TOKEN.equals(location.substring(colonIndex + 1, contextStartIndex))) {
                    return;
                } else {
                    results.add(
                            new Validator.ResultItem(mValidator,
                            Validator.ResultType.ERROR,
                            address,
                            NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPAddressValidator.Unsupported_location_attribute")));
                    return;
                }
            }
        }
        
        if (location.indexOf(HTTPS_DEFAULT_PORT_TOKEN, 7) > 0) {
            int colonIndex = -1;
            int contextStartIndex = -1;
            
            if (location.startsWith("https://")) {
                // look for ${HttpDefaultPort} token 
                colonIndex = location.indexOf(":", 8);
                contextStartIndex = location.indexOf("/", 8);
                
                if (HTTPS_DEFAULT_PORT_TOKEN.equals(location.substring(colonIndex + 1, contextStartIndex))) {
                    return;
                } else {
                    results.add(
                            new Validator.ResultItem(mValidator,
                            Validator.ResultType.ERROR,
                            address,
                            NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPAddressValidator.Unsupported_location_attribute")));
                    return;
                }
            }
        }
        
            if(containsToken(location)) {
                if(!isValidSoapAddressToken(location)) {
                        results.add(
                            new Validator.ResultItem(mValidator,
                            Validator.ResultType.ERROR,
                            address,
                            NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPAddressValidator.Unsupported_Token_Format")));
                    return;
                }
            } else {
                try {
                    URI uri = new URI(location);
                    String scheme = uri.getScheme();
                    if (!scheme.equalsIgnoreCase("http") &&
                        !scheme.equalsIgnoreCase("https")) {
                    return;
                    }
                    URL url = uri.toURL();
                } catch (Exception ex) {
                    results.add(
                        new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        address,
                        NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPAddressValidator.Unsupported_location_attribute")));
                }   
        }
    }
    
    public void visit(SOAPBinding binding) {
        String transportURI = binding.getTransportURI();
        if (transportURI == null) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    binding,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPBindingValidator.Transport_URI_required")));
        } else if (!transportURI.equals("http://schemas.xmlsoap.org/soap/http")) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    binding,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPBindingValidator.Unsupported_transport")));
        }
        
        try {
            SOAPBinding.Style style = binding.getStyle();
        } catch (Throwable th) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    binding,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPBindingValidator.Unsupported_style_attribute")));
        }
    }
    
    public void visit(SOAPBody body) {
        Collection<String> encodingStyles = body.getEncodingStyles();

        if (encodingStyles != null) {
            // This is optional.  Don't verify contents at this point.
        }
        String namespace = body.getNamespace();

        if (namespace != null) {
            // This is optional.  We should verify that it is a valid URI, but
            // I don't want to be too restrictive at this point.
        }
        try {
            SOAPMessageBase.Use use = body.getUse();

            // #184959
            if (use == SOAPMessageBase.Use.LITERAL) {
                if (namespace == null || namespace.length() == 0) {
                    results.add(
                        new Validator.ResultItem(mValidator,
                        Validator.ResultType.WARNING,
                        body,
                        NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPBodyValidator.R2717_Literal_Binding")));
                }
            }
        }
        catch (Throwable th) {
            results.add(
                new Validator.ResultItem(mValidator,
                Validator.ResultType.ERROR,
                body,
                NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPBodyValidator.Unsupported_use_attribute")));
        }
    }
    
    public void visit(SOAPFault fault) {
        String name = fault.getName();
        if (name == null) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    fault,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPFaultValidator.Missing_name")));
        }
        
        Collection<String> encodingStyles = fault.getEncodingStyles();
        if (encodingStyles != null) {
            // This is optional.  Don't verify contents at this point.
        }
        
        String namespace = fault.getNamespace();
        if (namespace != null) {
            // This is optional.  We should verify that it is a valid URI, but
            // I don't want to be too restrictive at this point.
        }
        
        try {
            SOAPMessageBase.Use use = fault.getUse();
        } catch (Throwable th) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    fault,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPFaultValidator.Unsupported_use_attribute")));
        }
    }
    
    public void visit(SOAPHeaderFault headerFault) {
        NamedComponentReference<Message> message = headerFault.getMessage();
        if (message == null) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    headerFault,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPHeaderFaultValidator.Missing_header_fault_message")));
        }
        
        String part = headerFault.getPart();
        if (part == null) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    headerFault,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPHeaderFaultValidator.Missing_header_fault_part")));
        }
        
        try {
            SOAPMessageBase.Use use = headerFault.getUse();
            if (use == null) {
                results.add(
                        new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        headerFault,
                        NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPHeaderFaultValidator.Missing_header_fault_use")));
            }
        } catch (Throwable th) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    headerFault,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPHeaderFaultValidator.Unsupported_header_fault_use_attribute")));
        }
        
        
        Collection<String> encodingStyles = headerFault.getEncodingStyles();
        if (encodingStyles != null) {
            // This is optional.  Don't verify contents at this point.
        }
        
        String namespace = headerFault.getNamespace();
        if (namespace != null) {
            // This is optional.  We should verify that it is a valid URI, but
            // I don't want to be too restrictive at this point.
        }
        
    }
    
    public void visit(SOAPOperation operation) {
        String soapActionURI = operation.getSoapAction();
        if (soapActionURI != null) {
            // This is fine.  The URI can be anything.  In reality,
            // we should verify that this is a valid URI, but I don't want
            // to be too restrictive.
        }
        
        try {
            SOAPBinding.Style style = operation.getStyle();
        } catch (Throwable th) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    operation,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPOperationValidator.Unsupported_style_attribute")));
        }
    }

    private void visit(BindingInput bindingInput) {
        Map<MessagePart, SOAPMessageBase> partMap =
                new HashMap<MessagePart, SOAPMessageBase>();
        
        List<SOAPHeader> soapHeaders=
                bindingInput.getExtensibilityElements(SOAPHeader.class);
        for (SOAPHeader header: soapHeaders) {
            header.accept(this);
            ensureUniqueParts(partMap, header);
            Collection<SOAPHeaderFault> soapHeaderFaults = header.getSOAPHeaderFaults();
            for (SOAPHeaderFault fault: soapHeaderFaults) {
                fault.accept(this);
                ensureUniqueParts(partMap, fault);
            }
        }
                            
        int numSoapBodies = bindingInput.getExtensibilityElements(SOAPBody.class).size();
        if(numSoapBodies == 0) {
            /*results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    bindingInput,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPBodyValidator.Atleast_one_body_Required")));*/

        } else if (numSoapBodies > 0 && numSoapBodies != 1) {
            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    bindingInput,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPBodyValidator.Only_one_body_allowed")));
        }
        List<SOAPBody> soapBodies = bindingInput.getExtensibilityElements(SOAPBody.class);
        for (SOAPBody body: soapBodies) {
            body.accept(this);
            ensureUniqueParts(partMap, body, bindingInput);
        }
    }
    
    private void visit(BindingOutput bindingOutput) {
        Map<MessagePart, SOAPMessageBase> partMap =
                new HashMap<MessagePart, SOAPMessageBase>();
        
        List<SOAPHeader> soapHeaders = bindingOutput.getExtensibilityElements(SOAPHeader.class);
        for (SOAPHeader soapHeader: soapHeaders) {
            soapHeader.accept(this);
            ensureUniqueParts(partMap, soapHeader);
            Collection<SOAPHeaderFault> soapHeaderFaults = soapHeader.getSOAPHeaderFaults();
            for (SOAPHeaderFault fault: soapHeaderFaults) {
                fault.accept(this);
                ensureUniqueParts(partMap, fault);
            }
        }

        int numSoapBodies = bindingOutput.getExtensibilityElements(SOAPBody.class).size();
        if(numSoapBodies == 0) {
           /* results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    bindingOutput,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPBodyValidator.Atleast_one_body_Required")));*/

        } else if (numSoapBodies > 0 && numSoapBodies != 1) {

            results.add(
                    new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    bindingOutput,
                    NbBundle.getMessage(SOAPComponentVisitor.class, "SOAPBodyValidator.Only_one_body_allowed")));
        }
        List<SOAPBody> soapBodies = bindingOutput.getExtensibilityElements(SOAPBody.class);
        for (SOAPBody body: soapBodies) {
            body.accept(this);
            ensureUniqueParts(partMap, body, bindingOutput);
        }
    }
    
    private void ensureUniqueParts(Map<MessagePart, SOAPMessageBase> partsMap,
                                   SOAPBody elem,
                                   WSDLComponent parent) {
        
        // Obtain the Message definition whose part this SOAPBody element extends
        OperationParameter param;
        if (parent instanceof BindingInput) {
            param = ((BindingInput) parent).getInput().get();
        } else if (parent instanceof BindingOutput) {
            param = ((BindingOutput) parent).getOutput().get();
        } else {
            throw new IllegalArgumentException("(Internal error) Unexpected WSDLComponent sub-type "
                    + parent.getClass().getName());
        }
        
        // Let wsdl validator catch undefined message for operation input or output
        if (param == null || param.getMessage() == null || param.getMessage().get() == null) {
            return;
        }
        
        Message msg = param.getMessage().get();
        
        List<String> partNames = elem.getParts();
        // If the SOAPBOdy does not explicitly specify a part, assume
        // it refers to the all the parts.
        if (partNames == null || partNames.isEmpty()) {
            Collection<Part> parts = msg.getParts();
            partNames = new ArrayList<String>(parts.size());
            for (Part part: parts) {
                partNames.add(part.getName());
            }
        }
        for (String name: partNames) {
            if (name != null && !"".equals(name)) {
                MessagePart msgpart = new MessagePart(msg, name);
                if (!partsMap.containsKey(msgpart)) {
                    partsMap.put(msgpart, elem);
                } else {
                    SOAPMessageBase conflictElem = partsMap.get(msgpart);
                    results.add(new Validator.ResultItem(
                        mValidator,
                        Validator.ResultType.ERROR,
                        elem,
                        NbBundle.getMessage(SOAPComponentVisitor.class,
                            "SOAPBodyValidator.Part_already_in_use_by_elem",
                            name,
                            msg.getName(),
                            conflictElem.getQName().toString())));
                }
            }
        }
    }
    
    private void ensureUniqueParts(Map<MessagePart, SOAPMessageBase> partsMap, SOAPHeaderBase elem) {
        NamedComponentReference<Message> comp = elem.getMessage();
        
        // Let wsdl validator catch undefined message for operation input or output
        if (elem == null || elem.getMessage() == null || elem.getMessage().get() == null) {
            return;
        }
        
        Message msg = comp.get();
        String part = elem.getPart();
        if (part != null && !"".equals(part)) {
            MessagePart msgpart = new MessagePart(msg, part);
            if (!partsMap.containsKey(msgpart)) {
                partsMap.put(msgpart, elem);
            } else {
                SOAPMessageBase conflictElem = partsMap.get(msgpart);
                results.add(new Validator.ResultItem(
                    mValidator,
                    Validator.ResultType.ERROR,
                    elem,
                    NbBundle.getMessage(SOAPComponentVisitor.class,
                        "SOAPHeaderValidator.Part_already_in_use_by_elem",
                        part,
                        msg.getName(),
                        conflictElem.getQName().toString())));
            }
        }
    }
    
    private List<String> allMessageParts(Message msg) {
        Collection<Part> parts = msg.getParts();
        List<String> partNames = new LinkedList<String>();
        for (Part part: parts) {
            partNames.add(part.getName());
        }
        return partNames;
    }
    
    private boolean containsToken(String val) {
        if(val.contains("${")) {                        
            return true;
        }
        return false;
    }

    /**
     * A token string can be of the following format: 
     * 1. http(s)://${host}:${port}/${context}
     * 2. ${URL}
     */
    private boolean isValidSoapAddressToken(String tokenString) {
        boolean containsProtocolInfo = false;
        boolean isValidToken = true;
        if(tokenString.startsWith("http://")) {
            //strip off the protocol stuff
            tokenString = tokenString.substring(7);
            containsProtocolInfo = true;
        }
        if(tokenString.startsWith("https://")) {
            //strip off the protocol stuff
            tokenString = tokenString.substring(8);
            containsProtocolInfo = true;
        }
        //No protocol info, it better be of the format ${URL}
        if(!containsProtocolInfo) {
            int indexOfTokenStart = tokenString.indexOf("${");
            int indexOfTokenEnd = tokenString.indexOf("}");
            if((indexOfTokenEnd == tokenString.length() - 1) && (indexOfTokenStart == 0)) {
                isValidToken = true;
            } else {
                return false;
            }
        }
        if(tokenString.contains("${") ) {
            int indexOfPortSeparator = tokenString.indexOf(":");
            int indexOfContextSeparator = tokenString.lastIndexOf("/");
            
            //Context separator / exists.
            if(indexOfContextSeparator != -1) {
                //The token is in the context.
                String context = tokenString.substring(indexOfContextSeparator+1);
                int indexOfContextTokenStart = context.indexOf("${");
                if(indexOfContextTokenStart == 0) {
                    int indexOfTokenEnd = context.indexOf("}");
                    if(indexOfTokenEnd < indexOfContextTokenStart) {
                        return false;
                    }
                } else if(context.indexOf("}") > 0) {
                    return false;
                }
                
                int indexOfTokenStart = tokenString.indexOf("${");
                if(indexOfTokenStart == 0) { //The token is in the host
                    String host = tokenString.substring(1, indexOfPortSeparator);
                    int indexOfTokenEnd = host.indexOf("}");
                    if(indexOfTokenEnd < 1) {
                        return false;
                    }
                } else if(tokenString.substring(1, indexOfPortSeparator).indexOf("}") > 0) {
                    return false;
                }
                String port = tokenString.substring(indexOfPortSeparator + 1, indexOfContextSeparator);
                if((port.indexOf("${") != -1) && (port.indexOf("}") > 0)) {
                    isValidToken = true;
                } else {
                    return false;
                }
            }
        }
        return isValidToken;
    }
}
