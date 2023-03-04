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

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author jyang
 */
public class PartReference extends AbstractReference<Part> implements Reference<Part> {

    public PartReference(Part referenced, AbstractDocumentComponent parent) {
        super(referenced, Part.class, parent);
    }
    //used by resolve methods
    public PartReference(AbstractDocumentComponent parent, String ref) {
        super(Part.class, parent, ref);
    }

    public String getRefString() {
        if (refString == null) {
            refString = getReferenced().getName();
        }
        return refString;
    }

    public Part get() {
        if (getReferenced() == null) {
            Message m = null;

            Object parent = getParent();



            while (parent != null && !(parent instanceof BindingInput) && !(parent instanceof BindingOutput)) {
                parent = ((WSDLComponent) parent).getParent();
            }

            if (parent instanceof BindingInput) {
                BindingInput bi = (BindingInput) parent;
                if (bi.getInput() != null) {
                    Input in = bi.getInput().get();
                    if (in != null) {
                        m = in.getMessage().get();
                    }
                }
            } else if (parent instanceof BindingOutput) {
                BindingOutput bo = (BindingOutput) parent;
                if (bo.getOutput() != null) {
                    Output out = bo.getOutput().get();
                    if (out != null) {
                        m = out.getMessage().get();
                    }
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
