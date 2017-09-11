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
package org.netbeans.modules.xml.wsdl.model.extensions.soap12.impl;

import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Fault;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.AbstractReference;
import org.netbeans.modules.xml.xam.Reference;


public class FaultReference extends AbstractReference<Fault> implements Reference<Fault> {
    
    public FaultReference(Fault referenced, AbstractDocumentComponent parent) {
        super(referenced, Fault.class, parent);
    }
    
    //used by resolve methods
    public FaultReference(AbstractDocumentComponent parent, String ref){
        super(Fault.class, parent, ref);
    }
    
    public String getRefString() {
        if (refString == null) {
            refString = getReferenced().getName();
        }
        return refString;
    }

    private SOAP12Fault getSOAPFault() {
        return (SOAP12Fault) getParent();
    }
    
    public Fault get() {
        if (getReferenced() == null) {
            if (getSOAPFault().getParent() == null || 
                getSOAPFault().getParent().getParent() == null) {
                return null;
            }
            BindingOperation bindingOp = (BindingOperation) getSOAPFault().getParent().getParent();
            Reference<Operation> ref = bindingOp.getOperation();
            Operation op = (ref == null) ? null : ref.get();
            if (op != null) {
                for (Fault f : op.getFaults()) {
                    if (refString.equals(f.getName())) {
                        setReferenced(f);
                        break;
                    }
                }
            }
        }
        return getReferenced();
    }
}
