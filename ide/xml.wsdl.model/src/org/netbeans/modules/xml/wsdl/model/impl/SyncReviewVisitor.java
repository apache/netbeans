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
