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

import java.io.IOException;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.netbeans.modules.xml.wsdl.model.visitor.DefaultVisitor;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.SyncUnit;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class SyncReviewVisitor extends DefaultVisitor {
    
    private SyncUnit unit;
    
    /** Creates a new instance of SyncUnitReviewVisistor */
    public SyncReviewVisitor() {
    }
 
    SyncUnit review(SyncUnit toReview) {
        this.unit = toReview;
        if (unit.getTarget() instanceof WSDLComponent) {
            ((WSDLComponent)unit.getTarget()).accept(this);
        }
        return unit;
    }

    private void reviewOperation(Operation target) {
        if (unit.getToAddList().size() > 0 || unit.getToRemoveList().size() > 0) {
            SyncUnit reviewed = new SyncUnit(target.getParent());
            ChangeInfo change = unit.getLastChange();
            Element peer = change.getParent();
            change.markParentAsChanged();
            change.setParentComponent(target.getParent());
            reviewed.addChange(change);
            reviewed.addToRemoveList(target);
            reviewed.addToAddList(createOperation(target.getParent(), peer));
            unit = reviewed;
        }
    }
    
    private Operation createOperation(WSDLComponent parent, Element e) {
        ElementFactory factory = ElementFactoryRegistry.getDefault().get(WSDLQNames.OPERATION.getQName());
        WSDLComponent component = factory.create(parent, e);
        if (component == null) {
            throw new IllegalArgumentException(new IOException("Cannot create operation."));
        }
        return (Operation) component;
    }
    
    public void visit(OneWayOperation target) {
        reviewOperation(target);
    }

    public void visit(SolicitResponseOperation target) {
        reviewOperation(target);
    }

    public void visit(RequestResponseOperation target) {
        reviewOperation(target);
    }

    public void visit(NotificationOperation target) {
        reviewOperation(target);
    }
}
