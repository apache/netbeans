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
package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderBase;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;

/**
 *
 * @author Nam Nguyen
 */
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
    
    public SOAPBodyImpl getBodyParent() {
        if (getParent() instanceof SOAPBodyImpl) {
            return (SOAPBodyImpl) getParent();
        } else {
            return null;
        }
    }
    
    public SOAPHeaderBaseImpl getHeaderParent() {
        if (getParent() instanceof SOAPHeaderBaseImpl) {
            return (SOAPHeaderBaseImpl) getParent();
        } else {
            return null;
        }
    }
    
    public Part get() {
        if (getReferenced() == null) {
            Message m = null;
            if (getBodyParent() != null) {
                SOAPBody p = getBodyParent();
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
                SOAPHeaderBase header = getHeaderParent();
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
