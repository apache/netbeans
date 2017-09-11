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

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.Reference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class BindingOperationImpl extends WSDLComponentBase implements BindingOperation {
    
    /** Creates a new instance of BindingOperationImpl */
    public BindingOperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingOperationImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.OPERATION.getQName(), model));
    }
    
    public void addExtensibilityElement(ExtensibilityElement ee) {
        addAfter(EXTENSIBILITY_ELEMENT_PROPERTY, ee, TypeCollection.DOCUMENTATION.types());
    }
    
    public void setBindingInput(BindingInput bindingInput) {
        setChildAfter(BindingInput.class, BINDING_INPUT_PROPERTY, bindingInput, TypeCollection.DOCUMENTATION_EE.types());
    }
    
    public BindingInput getBindingInput() {
        return getChild(BindingInput.class);
    }
    
    public void setBindingOutput(BindingOutput bindingOutput) {
        setChildAfter(BindingOutput.class, BINDING_OUTPUT_PROPERTY, bindingOutput, 
                TypeCollection.DOCUMENTATION_EXTENSIBILITY_BINDINGINPUT.types());
    }
    
    public BindingOutput getBindingOutput() {
        return getChild(BindingOutput.class);
    }
    
    public void setOperation(Reference<Operation> operationRef) {
        setName(operationRef == null ? null : operationRef.get().getName());
    }
    
    public Reference<Operation> getOperation() {
        return getName() == null ? null : new OperationReference(this, getName());
    }
    
    public void addBindingFault(BindingFault bindingFault) {
        addAfter(BINDING_FAULT_PROPERTY, bindingFault, TypeCollection.DOCUMENTATION_EXTENSIBILITY_BINDINGOUTPUT.types());
    }
    
    public void removeBindingFault(BindingFault bindingFault) {
        removeChild(BINDING_FAULT_PROPERTY, bindingFault);
    }
    
    public Collection<BindingFault> getBindingFaults() {
        return getChildren(BindingFault.class);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
    public void setName(String name) {
        setAttribute(NAME_PROPERTY, WSDLAttribute.NAME, name);
    }

    public String getName() {
        return getAttribute(WSDLAttribute.NAME);
    }
}
