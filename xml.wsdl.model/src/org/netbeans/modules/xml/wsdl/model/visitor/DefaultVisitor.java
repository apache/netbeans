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

/*
 * DefaultVisitor.java
 *
 * Created on November 17, 2005, 9:59 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.visitor;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author nn136682
 */
public class DefaultVisitor implements WSDLVisitor {
    
    /** Creates a new instance of DefaultVisitor */
    public DefaultVisitor() {
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Types types) {
        visitComponent(types);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Port port) {
        visitComponent(port);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Definitions definition) {
        visitComponent(definition);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.BindingInput bi) {
        visitComponent(bi);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.BindingOutput bo) {
        visitComponent(bo);
    }

    public void visit(OneWayOperation op) {
        visitComponent(op);
    }

    public void visit(RequestResponseOperation op) {
        visitComponent(op);
    }
    
    public void visit(NotificationOperation op) {
        visitComponent(op);
    }

    public void visit(SolicitResponseOperation op) {
        visitComponent(op);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Part part) {
        visitComponent(part);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Documentation doc) {
        visitComponent(doc);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.BindingOperation bop) {
        visitComponent(bop);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Binding binding) {
        visitComponent(binding);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Message message) {
        visitComponent(message);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Service service) {
        visitComponent(service);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.BindingFault bf) {
        visitComponent(bf);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Import importDef) {
        visitComponent(importDef);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Output out) {
        visitComponent(out);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.PortType portType) {
        visitComponent(portType);
    }

    public void visit(org.netbeans.modules.xml.wsdl.model.Input in) {
        visitComponent(in);
    }
    
    public void visit(org.netbeans.modules.xml.wsdl.model.Fault fault) {
        visitComponent(fault);
    }
    
    public void visit(ExtensibilityElement ee) {
        visitComponent(ee);
    }
    
    protected void visitComponent(WSDLComponent component) {
    }
}
