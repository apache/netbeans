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

import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.ComponentUpdater.Query;

/**
 *
 * @author Nam Nguyen
 */
public class SOAPComponentUpdater implements ComponentUpdater<SOAPComponent>, Query<SOAPComponent>, SOAPComponent.Visitor {
    private SOAPComponent parent;
    private Operation operation;
    private boolean canAdd;
    
    /** Creates a new instance of SOAPComponentUpdater */
    public SOAPComponentUpdater() {
    }
    
    public boolean canAdd(SOAPComponent target, Component child) {
        if (!(child instanceof SOAPComponent)) return false;
        update(target, (SOAPComponent) child, null);
        return canAdd;
    }

    public void update(SOAPComponent target, SOAPComponent child, Operation operation) {
        update(target, child, -1, operation);
    }

    
    public void update(SOAPComponent target, SOAPComponent child, int index, Operation operation) {
        parent = target;
        this.operation = operation;
        child.accept(this);
    }

    public void visit(SOAPOperation child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPBinding child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPHeader child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPBody child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPFault child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(SOAPHeaderFault child) {
        SOAPHeader target = (SOAPHeader) parent;
        if (operation == Operation.ADD) {
            target.addSOAPHeaderFault(child);
        } else if (operation == Operation.REMOVE) {
            target.removeSOAPHeaderFault(child);
        } else if (operation == null) {
            canAdd = true;
        }
    }

    public void visit(SOAPAddress child) {
        //not child of a SOAPComponent
        if (operation == null) {
            canAdd = false;
        }
    }
    
}
