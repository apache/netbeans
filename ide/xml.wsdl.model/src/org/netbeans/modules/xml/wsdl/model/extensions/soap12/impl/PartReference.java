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
package org.netbeans.modules.xml.wsdl.model.extensions.soap12.impl;

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Body;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12HeaderBase;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;


public class PartReference extends AbstractReference<Part> implements Reference<Part> {
    
    public PartReference(Part referenced, AbstractDocumentComponent parent) {
        super(referenced, Part.class, parent);
    }
    
    //used by resolve methods
    public PartReference(AbstractDocumentComponent parent, String ref){
        super(Part.class, parent, ref);
    }
    
    public String getRefString() {
        if (refString == null) {
            refString = getReferenced().getName();
        }
        return refString;
    }
    
    public SOAP12BodyImpl getBodyParent() {
        if (getParent() instanceof SOAP12BodyImpl) {
            return (SOAP12BodyImpl) getParent();
        } else {
            return null;
        }
    }
    
    public SOAP12HeaderBaseImpl getHeaderParent() {
        if (getParent() instanceof SOAP12HeaderBaseImpl) {
            return (SOAP12HeaderBaseImpl) getParent();
        } else {
            return null;
        }
    }
    
    public Part get() {
        if (getReferenced() == null) {
            Message m = null;
            if (getBodyParent() != null) {
                SOAP12Body p = getBodyParent();
                if (p.getParent() instanceof BindingInput) {
                    BindingInput bi = (BindingInput)p.getParent();
                    if (bi.getInput() != null) {
                        Input in = bi.getInput().get();
                        if (in != null) {
                            m = in.getMessage().get();
                        }
                    }
                } else if (p.getParent() instanceof BindingOutput) {
                    BindingOutput bo = (BindingOutput)p.getParent();
                    if (bo.getOutput() != null) {
                        Output out = bo.getOutput().get();
                        if (out != null) {
                            m = out.getMessage().get();
                        }
                    }
                }
                
            } else if (getHeaderParent() != null) {
                SOAP12HeaderBase header = getHeaderParent();
                if (header.getMessage() != null) {
                    m = header.getMessage().get();
                }
            }
            if (m != null) {
                for (Part part : m.getParts()) {
                    if (part.getName().equals(getRefString())) {
                        setReferenced(part);
                        break;
                    }
                }
            }
        }
        return getReferenced();
    }
}
