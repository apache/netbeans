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

package org.netbeans.modules.xml.wsdl.model.visitor;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author Nam Nguyen
 */
public class FindReferencedVisitor<T extends ReferenceableWSDLComponent> extends DefaultVisitor {
    private Class<T> type;
    private String localName;
    private T referenced = null;
    private Definitions root;
    
    /** Creates a new instance of FindReferencedVisitor */
    public FindReferencedVisitor(Definitions root) {
        this.root = root;
    }
    
    public T find(String localName, Class<T> type) {
        this.type = type;
        this.localName = localName;
        visitChildren(root);
        return referenced;
    }

    public void visit(Binding c) {
        checkReference(c, true); //extensible
    }

    public void visit(Message c) { //extensible
        checkReference(c, true);
    }

    public void visit(PortType c) { //extensible
        checkReference(c, true);
    }
    
    public void visit(RequestResponseOperation op){
        checkReference(op, true);
    }    
    
    public void visit(OneWayOperation op){
        checkReference(op, true);
    }
    
    
    public void visit(NotificationOperation op){
        checkReference(op, true);
    }
    
    public void visit(SolicitResponseOperation op){
        checkReference(op, true);
    }

    public void visit(Service s){
        checkReference(s, true);
    }
    
    public void visit(Port p){
        checkReference(p, true);
    }
    
    public void visit(Part part){
        checkReference(part, true);
    }
    
    public void visit(ExtensibilityElement c) {
        if (c instanceof ReferenceableWSDLComponent) {
            checkReference(ReferenceableWSDLComponent.class.cast(c), true);
        }
        visitChildren(c);
    }

    private void checkReference(ReferenceableWSDLComponent c, boolean checkChildren) {
         if (type.isAssignableFrom(c.getClass()) && c.getName() != null &&
                 c.getName().equals(localName)) {
             referenced = type.cast(c);
             return;
         } else if (checkChildren) {
             visitChildren(c);
         }
    }
    
    private void visitChildren(WSDLComponent c) {
        for (WSDLComponent child : c.getChildren()) {
           if (referenced != null) { // before start each visit
                return;
           }
            child.accept(this);
        }
    }
    
}
