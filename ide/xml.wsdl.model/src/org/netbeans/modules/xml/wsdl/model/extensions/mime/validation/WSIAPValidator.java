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
package org.netbeans.modules.xml.wsdl.model.extensions.mime.validation;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEContent;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEMimeXml;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEMultipartRelated;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEPart;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEQName;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Body;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Header;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12HeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;

/**
 * The WSIABPValidatorr will verify that the MIME bindings in a WSDL 
 * are in conformance with the WSI Attachment Profile 1.0.
 * @author jyang
 */
public class WSIAPValidator implements MIMEContent.Visitor {

    public static final String CONTENT_TYPE_TEXT_XML = "text/xml";
    public static final String NS_URI_SWA_REF = "http://ws-i.org/profiles/basic/1.1/xsd";
    public static final String SCHEMA_TYPE_SWA_REF = "swaRef";

    private final List<ResultItem> mResults;
    private final MIMEComponentValidator mValidator;
    private WSDLModel mWSDLModel;
    private boolean isSOAPBinding;
    private boolean isSOAP12Binding;

    public WSIAPValidator(MIMEComponentValidator validator, WSDLModel model) {
        mValidator = validator;
        mResults = new LinkedList<ResultItem>();
        mWSDLModel = model;
    }
    
    public void validate() {
        Definitions defs = mWSDLModel.getDefinitions();
        Iterator<Binding> bindings = defs.getBindings().iterator();
        while (bindings.hasNext()) {
            Binding binding = bindings.next();
            int numSoapBindings = binding.getExtensibilityElements(SOAPBinding.class).size();
            if (numSoapBindings == 1) {
                isSOAP12Binding = false;
                isSOAPBinding = true;
                visit(binding);
            }
            int numSoap12Bindings = binding.getExtensibilityElements(SOAP12Binding.class).size();
            if (numSoap12Bindings == 1) {
                isSOAP12Binding = true;
                isSOAPBinding = false;
                visit(binding);
            }
        }
    }

    public List<ResultItem> getResultItems() {
        return mResults;
    }  

   
    private void visit(Binding binding) {
        // Getting its wsdl:operation elements
        Collection<BindingOperation> ops = (Collection<BindingOperation>) binding.getBindingOperations();
        // Going through the operation elements
        for (BindingOperation bindingOperation : ops) {
            // Getting wsdl:input and wsdl:output elements of an operation
            BindingInput bindingInput = bindingOperation.getBindingInput();
            BindingOutput bindingOutput = bindingOperation.getBindingOutput();

            testAP2901(bindingInput, bindingOutput);

            // Collecting all the mime:content elements from wsdl:input and wsdl:output
            List inputMimeContents = getMimeContentElements(
                    bindingInput == null ? new ArrayList() : bindingInput.getExtensibilityElements());
            List outputMimeContents = getMimeContentElements(
                    bindingOutput == null ? new ArrayList() : bindingOutput.getExtensibilityElements());

            testAP2903_2910_2944(bindingOperation, bindingInput, bindingOutput, inputMimeContents, outputMimeContents);

            testAP2911_2906(bindingInput == null ? new ArrayList() : bindingInput.getExtensibilityElements((MIMEMultipartRelated.class)),
                    bindingOutput == null ? new ArrayList() : bindingOutput.getExtensibilityElements(MIMEMultipartRelated.class));


            List<MIMEPart> inputMimeParts = getAllMimeParts(
                    bindingInput == null ? new ArrayList() : bindingInput.getExtensibilityElements());
            List<MIMEPart> outputMimeParts = getAllMimeParts(
                    bindingOutput == null ? new ArrayList() : bindingOutput.getExtensibilityElements());

            testAP2909(inputMimeParts, outputMimeParts);
            testAP2930(bindingOperation);

        //rule 2940 and rule 2941 are pending
            /*Input portTypeInput = ((Operation) bindingOperation.getOperation().get()).getInput();
        Output portTypeOutput = ((Operation) bindingOperation.getOperation().get()).getOutput();
        testAP2940(portTypeInput, portTypeOutput);
        testAP2941(binding, bindingOperation, bindingInput, bindingOutput, portTypeInput, portTypeOutput);*/
        }
    }
    
    private void visit(BindingFault target) {
        if (target.getExtensibilityElements(MIMEMultipartRelated.class).size() > 0) {
            mResults.add(new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    target,
                    NbBundle.getMessage(WSIAPValidator.class, "AP2930")));
        }

    }
    
    public void visit(MIMEContent target) {
        if (target.getPartRef() == null ||
                target.getPartRef().get() == null) {
            mResults.add(new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    target,
                    NbBundle.getMessage(WSIAPValidator.class, "AP2903_PART")));
        }

        if (target.getPartRef() != null &&
                target.getPartRef().get() != null) {
            Part part = (Part) target.getPartRef().get();
            if ((part.getType() == null && part.getElement() == null) 
                    || (part.getType() != null && part.getElement() != null)) {
                mResults.add(new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        target,
                        NbBundle.getMessage(WSIAPValidator.class, "AP2910")));
            }
        }

        if (target.getPartRef() != null &&
                target.getPartRef().get() != null) {
            Part part = (Part) target.getPartRef().get();
            if ((part.getElement() != null) && !(CONTENT_TYPE_TEXT_XML.equals(target.getType()))) {
                mResults.add(new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        target,
                        NbBundle.getMessage(WSIAPValidator.class, "AP2944")));
            }
        }
    }

    public void visit(MIMEMimeXml target) {
        
    }

    public void visit(MIMEMultipartRelated target) {
        List mimeParts = target.getMIMEParts();
        boolean soapBodyFound = false;
        // Going through all the mime:part elements
        for (int j = 0; j < mimeParts.size(); j++) {
            boolean hasSOAPBody = false;
            boolean hasSOAPHEADER = false;
            // Getting a list of extensibility elements of a mime:part
            List extElems =
                    ((MIMEPart) mimeParts.get(j)).getExtensibilityElements();
            // Going through the extensibility elements
            for (int k = 0; k < extElems.size(); k++) {
                // If an extensibility element is a soap:body
                if (((ExtensibilityElement) extElems.get(k)).getQName().equals(SOAPQName.BODY.getQName()) && isSOAPBinding 
                        || ((ExtensibilityElement) extElems.get(k)).getQName().equals(SOAP12QName.BODY.getQName()) && isSOAP12Binding) {
                    hasSOAPBody = true;
                    if (soapBodyFound ) {
                    	if (isSOAPBinding) {
                    		mResults.add(new Validator.ResultItem(mValidator,
                                Validator.ResultType.ERROR,
                                target,
                                NbBundle.getMessage(WSIAPValidator.class, "AP2911")));
                      } else if (isSOAP12Binding) {
                      		mResults.add(new Validator.ResultItem(mValidator,
                                Validator.ResultType.ERROR,
                                target,
                                NbBundle.getMessage(WSIAPValidator.class, "AP2911_SOAP12")));
                        }
                         
                    } // else set the variable to the true value
                    else {
                        soapBodyFound = true;
                    }
                }
                if (((ExtensibilityElement) extElems.get(k)).getQName().equals(SOAPQName.HEADER.getQName()) && isSOAPBinding 
                        || ((ExtensibilityElement) extElems.get(k)).getQName().equals(SOAP12QName.HEADER.getQName()) && isSOAP12Binding) {
                    hasSOAPHEADER = true;
                }
            }

            if (!hasSOAPBody && hasSOAPHEADER) {
            	if (isSOAPBinding) {
            		  mResults.add(new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        (MIMEPart) mimeParts.get(j),
                        NbBundle.getMessage(WSIAPValidator.class, "AP2906")));
            	} else if (isSOAP12Binding) {
            		 mResults.add(new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        (MIMEPart) mimeParts.get(j),
                        NbBundle.getMessage(WSIAPValidator.class, "AP2906_SOAP12")));
            	}
                
            }
        }
        if (!soapBodyFound) {
        	if (isSOAPBinding) {
        		mResults.add(new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    target,
                    NbBundle.getMessage(WSIAPValidator.class, "AP2911")));
        	} else if (isSOAP12Binding) {
        		mResults.add(new Validator.ResultItem(mValidator,
                    Validator.ResultType.ERROR,
                    target,
                    NbBundle.getMessage(WSIAPValidator.class, "AP2911_SOAP12")));
          }
            
        }
    }

    public void visit(MIMEPart target) {
        // Going through mime:part extensibility elements
        List contents = target.getExtensibilityElements(MIMEContent.class);
        if (contents != null && contents.size() > 1) {
            String partVal = ((MIMEContent) contents.get(0)).getPart();

            for (int j = 1; j < contents.size(); j++) {
                if (!partVal.equals(((MIMEContent) contents.get(j)).getPart())) {
                    mResults.add(new Validator.ResultItem(mValidator,
                            Validator.ResultType.ERROR,
                            target,
                            NbBundle.getMessage(WSIAPValidator.class, "AP2909")));
                }
                break;
            }
        }
    }
    
    /**
     * Collects all mime:content elements.
     * @param extElems a list of extensibility elements that can contain mime:contentS.
     * @return the list of mime:content elements found.
     */
    private List getMimeContentElements(List extElems) {
        List mimeContentElements = new ArrayList();
        if (extElems != null) {
            for (int i = 0; i < extElems.size(); i++) {
                ExtensibilityElement extElem = (ExtensibilityElement) extElems.get(i);
                // If the element is mime:multipartRelated
                if (extElem.getQName().equals(MIMEQName.MULTIPART_RELATED.getQName())) {
                    // Getting the mime:part elements of the mime:multipartRelated
                    List mimeParts = ((MIMEMultipartRelated) extElem).getMIMEParts();
                    // Going through all the mime:part elements
                    for (int j = 0; j < mimeParts.size(); j++) {
                        // Collecting all the mime:content elements of this mime:part
                        List elems = getMimeContentElements(
                                ((MIMEPart) mimeParts.get(j)).getExtensibilityElements());
                        // Adding the elements to the list being returned
                        mimeContentElements.addAll(elems);
                    }
                } // Else if the element is mime:content
                else if (extElem.getQName().equals(MIMEQName.CONTENT.getQName())) {
                    // Adding the element to the list being returned
                    mimeContentElements.add(extElem);
                }
            }
        }
        return mimeContentElements;
    }

    /**
     * Collects all mime:part elements.
     * @param extElems a list of extensibility elements that can contain mime:part elements.
     * @return the list of mime:part elements found.
     */
    private List <MIMEPart> getAllMimeParts(List extElems) {
        List<MIMEPart>  mimeParts = new ArrayList <MIMEPart> ();
        if (extElems != null) {
            for (int i = 0; i < extElems.size(); i++) {
                ExtensibilityElement extElem = (ExtensibilityElement) extElems.get(i);
                // If the element is mime:multipartRelated
                if (extElem.getQName().equals(MIMEQName.MULTIPART_RELATED.getQName())) {
                    // Getting the mime:part elements of the mime:multipartRelated
                    List mParts = ((MIMEMultipartRelated) extElem).getMIMEParts();
                    mimeParts.addAll(mParts);
                    // Going through all the mime:part elements
                    for (int j = 0; j < mParts.size(); j++) {
                        List elems = getAllMimeParts(
                                ((MIMEPart) mParts.get(j)).getExtensibilityElements());
                        // Adding the elements to the list being returned
                        mimeParts.addAll(elems);
                    }
                }
            }
        }
        return mimeParts;
    }

    /**
     * Collects all mime:multipartRelated elements.
     * @param extElems a list of extensibility elements that can contain mime:multipartRelated elements.
     * @return the list of mime:multipartRelated elements found.
     */
    private List getMimeMultipartElements(List extElems) {
        List mimeMultipartElements = new ArrayList();

        if (extElems != null) {
            // Going through all the extensibility elements
            for (int i = 0; i < extElems.size(); i++) {
                ExtensibilityElement extElem = (ExtensibilityElement) extElems.get(i);
                // If the element is mime:multipartRelated
                if (extElem.getQName().equals(MIMEQName.MULTIPART_RELATED.getQName())) {
                    // Adding the element to the list being returned
                    mimeMultipartElements.add(extElem);
                    // Getting the mime:part elements of the mime:multipartRelated
                    List mimeParts = ((MIMEMultipartRelated) extElem).getMIMEParts();
                    // Going through all the mime:part elements
                    for (int j = 0; j < mimeParts.size(); j++) {
                        // Collecting all the mime:multipartRelated elements of this mime:part
                        List elems = getMimeMultipartElements(
                                ((MIMEPart) mimeParts.get(j)).getExtensibilityElements());
                        // Adding the elements to the list being returned
                        mimeMultipartElements.addAll(elems);
                    }
                }
            }
        }
        return mimeMultipartElements;
    }

    /*
     * Assertion Description:
     *  A description uses either the WSDL MIME Binding as described in WSDL 1.1 Section 5 or WSDL SOAP binding as described 
     * in WSDL 1.1 Section 3 on each of the wsdl:input or wsdl:output elements of a wsdl:binding.
     */
    private void testAP2901(
            BindingInput bindingInput,
            BindingOutput bindingOutput) {
        if (bindingInput != null) {
            int soapNum = 0;
            if (isSOAPBinding) {
                soapNum = bindingInput.getExtensibilityElements(SOAPBody.class).size();
            } else if (isSOAP12Binding) {
                soapNum = bindingInput.getExtensibilityElements(SOAP12Body.class).size();
            }      
            int mimeConentNum = bindingInput.getExtensibilityElements(MIMEContent.class).size();
            int mimeMultipartNum = bindingInput.getExtensibilityElements(MIMEMultipartRelated.class).size();
            int mimeXMLNum = bindingInput.getExtensibilityElements(MIMEMimeXml.class).size();
            if (soapNum == 0 && mimeConentNum == 0 && mimeMultipartNum == 0 && mimeXMLNum == 0) {
                mResults.add(new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        bindingInput,
                        NbBundle.getMessage(WSIAPValidator.class, "AP2901_INPUT")));
            }

        }
        if (bindingOutput != null) {
            int soapNum = 0;
            if (isSOAPBinding) {                
                soapNum = bindingOutput.getExtensibilityElements(SOAPBody.class).size();
            } else if (isSOAP12Binding) {
                soapNum = bindingOutput.getExtensibilityElements(SOAP12Body.class).size();
            }   
            int mimeConentNum = bindingOutput.getExtensibilityElements(MIMEContent.class).size();
            int mimeMultipartNum = bindingOutput.getExtensibilityElements(MIMEMultipartRelated.class).size();
            int mimeXMLNum = bindingOutput.getExtensibilityElements(MIMEMimeXml.class).size();
            if (soapNum == 0 && mimeConentNum == 0 && mimeMultipartNum == 0 && mimeXMLNum == 0) {
                mResults.add(new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        bindingOutput,
                        NbBundle.getMessage(WSIAPValidator.class, "AP2901_OUTPUT")));
            }

        }
    }


    private void testAP2903_2910_2944(
            BindingOperation bindingOperation,
            BindingInput bindingInput,
            BindingOutput bindingOutput,
            List inputMimeContents,
            List outputMimeContents) {

        if (!inputMimeContents.isEmpty()) {
            Input portTypeInput = ((Operation) bindingOperation.getOperation().get()).getInput();
            if (portTypeInput == null) {
                mResults.add(new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        bindingInput,
                        NbBundle.getMessage(WSIAPValidator.class, "AP2903_PORTTYPE_NULL")));

            } else if (portTypeInput.getMessage() == null) {
                mResults.add(new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        bindingInput,
                        NbBundle.getMessage(WSIAPValidator.class, "AP2903_MESSAGE_NULL")));
            } else {
                for (int i = 0; i < inputMimeContents.size(); i++) {
                    MIMEContent mimeContent = (MIMEContent) inputMimeContents.get(i);
                    mimeContent.accept(this);
                }
            }
        }

        if (!outputMimeContents.isEmpty()) {
            Output portTypeOutput = ((Operation) bindingOperation.getOperation().get()).getOutput();
            if (portTypeOutput == null) {
                mResults.add(new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        bindingOutput,
                        NbBundle.getMessage(WSIAPValidator.class, "AP2903_PORTTYPE_NULL")));
            } else if (portTypeOutput.getMessage() == null) {
                mResults.add(new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        bindingOutput,
                        NbBundle.getMessage(WSIAPValidator.class, "AP2903_MESSAGE_NULL")));
            } else {
                for (int i = 0; i < outputMimeContents.size(); i++) {
                    // Getting the part name of a mime:element
                    MIMEContent mimeContent = (MIMEContent) outputMimeContents.get(i);
                    mimeContent.accept(this);
                }
            }
        }
    }

    private void testAP2911_2906(List inputMultiparts,  List outputMultiparts) {
        if (inputMultiparts != null) {
            for (int i = 0; i < inputMultiparts.size(); i++) {
                MIMEMultipartRelated multiPart = (MIMEMultipartRelated) inputMultiparts.get(i);
                multiPart.accept(this);
            }
        }
        if (outputMultiparts != null) {
            for (int i = 0; i < outputMultiparts.size(); i++) {
                MIMEMultipartRelated multiPart = (MIMEMultipartRelated) outputMultiparts.get(i);
                multiPart.accept(this);
            }
        }
    }

    private void testAP2930(BindingOperation bindingOperation) {
        Collection<BindingFault> faults = bindingOperation.getBindingFaults();
        if (!faults.isEmpty()) {
            for (BindingFault fault : faults) {
                visit(fault);
            }
        }
    }
    
    private void testAP2909(List<MIMEPart> inputMimeParts, List<MIMEPart> outputMimeParts) {
        for (MIMEPart part : inputMimeParts) {
            part.accept(this);
        }
        for (MIMEPart part : outputMimeParts) {
            part.accept(this);
        }

    }
    
    private void testAP2940(Input input, Output output) {
        if (input != null && input.getMessage() != null && input.getMessage().get() != null) {
            // Collecting all the message's parts defined with ref:swaRef
            List swaRefParts = getSwaRefParts((Message) input.getMessage().get());
            if (!swaRefParts.isEmpty()) {
                //TODO
                //testUnboundPart(swaRefParts,
                //portTypeInput.getMessage().getQName()          
            }
        }
    }

    private List <String> getSwaRefParts(Message message) {
            List swaRefParts = new ArrayList();

        // Going through message's parts
        Iterator it = message.getParts().iterator();
        while (it.hasNext()) {
            Part part = (Part) it.next();
            QName partRef;
            boolean isEle = true;
            // Getting either part's element reference or type reference
            if (part.getType() != null) {
                isEle = false;
                partRef = part.getType().getQName();
                GlobalType partType = part.getType().get();
                isSwarefType(partType);
                if (partType instanceof GlobalSimpleType) {
                    isSwarefType((GlobalSimpleType) partType);
                } else if (partType instanceof GlobalComplexType) {
                }
            } else if (part.getElement() != null) {
                partRef = part.getElement().getQName();
            } else {
                continue;
            }
        }

        // Return the list
        return swaRefParts;
    }
    
    private boolean isSwarefType(GlobalType globalType) {
        if (globalType instanceof GlobalSimpleType) {
            return isSwarefType((GlobalSimpleType)globalType);
        } else if (globalType instanceof GlobalComplexType) {
            
//          List list<SchemaComponent> = ((GlobalComplexType)globalType).getChildren();

//          ComplexTypeDefinition def = ((GlobalComplexType)globalType).getDefinition();

            return true;
            
        }
        return false;
    }
    
    private boolean isSwarefType(GlobalSimpleType simpleType) {
        return (NS_URI_SWA_REF.equals(simpleType.getModel().getSchema().getTargetNamespace()) 
                && (SCHEMA_TYPE_SWA_REF.equals(simpleType.getName())));       
    }

    private Collection<String> getPartNames(Collection<Part> parts) {
        List names = new ArrayList();
        for (Part part : parts) {
            names.add(part.getName());
        }
        return names;
    }

    private Collection<String> getFaultNames(Collection<Fault> faults) {
        List names = new ArrayList();
        for (Fault fault : faults) {
            names.add(fault.getName());
        }
        return names;
    }

    /* Failure Message:
     A wsdl:binding in a description does not bind every wsdl:part of a wsdl:message 
     in the wsdl:portType to which it refers to 
     one of soapbind:body, soapbind:header, soapbind:fault , soapbind:headerfault, or mime:content.
     * @see org.wsi.test.profile.validator.impl.BaseValidatorImpl.AssertionProcess#validate(org.wsi.test.profile.TestAssertion, org.wsi.test.profile.validator.EntryContext)
     */
    public void testAP2941(Binding binding,
            BindingOperation bindingOperation,
            BindingInput bindingInput,
            BindingOutput bindingOutput,
            Input portTypeInput,
            Output portTypeOutput) {


        if (portTypeInput != null && portTypeInput.getMessage() != null) {
            // Getting the list of all the parts bound by wsdl:input's child elements
            List inputParts = getBindingParts_AP2941(
                    bindingOperation.getBindingInput().getExtensibilityElements(),
                    (Message) portTypeInput.getMessage().get());
      
            if (!inputParts.containsAll(getPartNames(((Message) portTypeInput.getMessage().get()).getParts()))) {
                mResults.add(new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        bindingInput,
                        NbBundle.getMessage(WSIAPValidator.class, "AP2941")));
       

            }
        }

        // If the corresponding wsdl:output exists in wsdl:portType
        // and includes the message attribute
        if (portTypeOutput != null && portTypeOutput.getMessage() != null) {
            // Getting the list of all the parts bound by wsdl:output's child elements
            List outputParts = getBindingParts_AP2941(
                    bindingOperation.getBindingOutput().getExtensibilityElements(),
                    (Message) portTypeOutput.getMessage().get());

            if (!outputParts.containsAll(getPartNames(((Message) portTypeOutput.getMessage().get()).getParts()))) {
                mResults.add(new Validator.ResultItem(mValidator,
                        Validator.ResultType.ERROR,
                        bindingOutput,
                        NbBundle.getMessage(WSIAPValidator.class, "AP2941")));
                       
            }
        }

        //this logic doesn't seem right due to the fault names or part names
      
//        if (!((Operation) bindingOperation.getOperation().get()).getFaults().isEmpty()) {
//            // Collecting all the soap:fault names
//            List faultNames = new ArrayList();
//            Collection faults = bindingOperation.getBindingFaults();
//            // Going through all the wsdl:faultS
//            Iterator it = faults.iterator();
//            while (it.hasNext()) {
//                // Getting wsdl:fault's extensibility elements
//                List extElems = ((BindingFault) it.next()).getExtensibilityElements();
//                for (int j = 0; j < extElems.size(); j++) {
//                    if (extElems.get(j) instanceof SOAPFault) {
//                        faultNames.add(((SOAPFault) extElems.get(j)).getName());
//                    }
//                }
//            }
//            //soap:fault + soap:headerfault
//            Collection <ExtensibilityElement> extEles = new ArrayList<ExtensibilityElement>();
//            extEles.addAll(bindingOperation.getBindingInput().getExtensibilityElements());
//            extEles.addAll(bindingOperation.getBindingOutput().getExtensibilityElements());
//            faultNames.addAll(findAllHeaderFaults(extEles));
//
//            // If not true that all the wsdl:faultS are bound,
//            // the assertion failed
//            if (!faultNames.containsAll(getFaultNames(((Operation) bindingOperation.getOperation().get()).getFaults()))) {
//                mResults.add(new Validator.ResultItem(mValidator,
//                        Validator.ResultType.ERROR,
//                        bindingOperation,
//                        //NbBundle.getMessage(WSIAPValidator.class, "AP2901")));
//                        "AP2941"));

//            }
//        }

    }
    
//    /**
//     * find all header faults declared in a binding operation input and output
//     * @param bindingOp
//     * @return
//     */
//    private List findAllHeaderFaults(Collection<ExtensibilityElement> ioElements) {
//        List headerFaults = new ArrayList();
//        for (ExtensibilityElement extElem : ioElements) {
//
//            if (extElem instanceof SOAPHeader) {
//                Collection<SOAPHeaderFault> shfList = ((SOAPHeader) extElem).getSOAPHeaderFaults();
//                for (SOAPHeaderFault shf : shfList) {
//                    headerFaults.add(((Message) shf.getMessage().get()).getName());
//                }
//                if (extElem instanceof MIMEMultipartRelated) {
//                    List mimeParts = ((MIMEMultipartRelated) extElem).getMIMEParts();
//                    // Going through all the mime:part elements
//                    for (int j = 0; j < mimeParts.size(); j++) {
//                        // Collecting all the values of part attributes
//                        // of mime:part's extensibility elements
//                        headerFaults.addAll(findAllHeaderFaults(
//                                ((MIMEPart) mimeParts.get(j)).getExtensibilityElements()));
//                    }
//                }
//            }
//
//        }
//        return headerFaults;
//    }
      

    /**
     * Collects all the parts bound by extensibility elements.
     * @param extElems a lit of extensibility elements.
     * @param message the wsdl:message element corresponging
     * to the extensibility elements.
     * @return a list of wsdl:part names bound.
     */
    private List getBindingParts_AP2941(List extElems, Message message) {
        List parts = new ArrayList();
        if (isSOAPBinding) {
                   if (extElems != null) {
            // Going through the extensibility elements
            for (int i = 0; i < extElems.size(); i++) {
                ExtensibilityElement extElem = (ExtensibilityElement) extElems.get(i);
                // If that is a soap:body
                if (extElem instanceof SOAPBody) {
                    // Adding all the parts bound to the list
                    List pts = ((SOAPBody) extElem).getParts();
                    if (pts != null) {
                        parts.addAll(pts);
                    } else {
                        //TODO
                        //parts.addAll(message.getParts().);
                    }
                } else if (extElem instanceof SOAPHeader) {
                    Collection<SOAPHeaderFault> headerFaults = null;
                    if (extElem instanceof SOAPHeader) {
                        SOAPHeader header = (SOAPHeader) extElem;
                        // If a header references the corresponding message,
                        // adding part name to the list 
                        //TODO

                        if (message.equals(header.getMessage().get())) {
                            parts.add(header.getPart());
                        }
                        headerFaults = header.getSOAPHeaderFaults();
                        for (SOAPHeaderFault hFault : headerFaults) {

                            if (message.equals(hFault.getMessage().get())) {
                                parts.add(hFault.getPart());
                            }
                        }
                    }

                } // else if that is a mime:content
                else if (extElem instanceof MIMEContent) {
                    // adding part name to the list
                    parts.add(((MIMEContent) extElem).getPart());
                } // else if that is a mime:multipartRelated
                else if (extElem instanceof MIMEMultipartRelated) {
                    // Getting the mime:part elements of the mime:multipartRelated
                    List mimeParts = ((MIMEMultipartRelated) extElem).getMIMEParts();
                    // Going through all the mime:part elements
                    for (int j = 0; j < mimeParts.size(); j++) {
                        // Collecting all the values of part attributes
                        // of mime:part's extensibility elements
                        parts.addAll(getBindingParts_AP2941(
                                ((MIMEPart) mimeParts.get(j)).getExtensibilityElements(),
                                message));
                    }
                }
            }
          }
        } else if (isSOAP12Binding) {
                   if (extElems != null) {
            // Going through the extensibility elements
            for (int i = 0; i < extElems.size(); i++) {
                ExtensibilityElement extElem = (ExtensibilityElement) extElems.get(i);
                // If that is a soap:body
                if (extElem instanceof SOAP12Body) {
                    // Adding all the parts bound to the list
                    List pts = ((SOAP12Body) extElem).getParts();
                    if (pts != null) {
                        parts.addAll(pts);
                    } else {
                        //TODO
                        //parts.addAll(message.getParts().);
                    }
                } else if (extElem instanceof SOAP12Header) {
                    Collection<SOAP12HeaderFault> headerFaults = null;
                    if (extElem instanceof SOAP12Header) {
                        SOAP12Header header = (SOAP12Header) extElem;
                        // If a header references the corresponding message,
                        // adding part name to the list 
                        //TODO

                        if (message.equals(header.getMessage().get())) {
                            parts.add(header.getPart());
                        }
                        headerFaults = header.getSOAPHeaderFaults();
                        for (SOAP12HeaderFault hFault : headerFaults) {

                            if (message.equals(hFault.getMessage().get())) {
                                parts.add(hFault.getPart());
                            }
                        }
                    }

                } // else if that is a mime:content
                else if (extElem instanceof MIMEContent) {
                    // adding part name to the list
                    parts.add(((MIMEContent) extElem).getPart());
                } // else if that is a mime:multipartRelated
                else if (extElem instanceof MIMEMultipartRelated) {
                    // Getting the mime:part elements of the mime:multipartRelated
                    List mimeParts = ((MIMEMultipartRelated) extElem).getMIMEParts();
                    // Going through all the mime:part elements
                    for (int j = 0; j < mimeParts.size(); j++) {
                        // Collecting all the values of part attributes
                        // of mime:part's extensibility elements
                        parts.addAll(getBindingParts_AP2941(
                                ((MIMEPart) mimeParts.get(j)).getExtensibilityElements(),
                                message));
                    }
                }
            }
        }
        }
 
        return parts;
    }
 

}
