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
package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowReturn;
import org.netbeans.modules.web.jsf.api.facesmodel.FromOutcome;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class FlowReturnImpl extends IdentifiableComponentImpl implements FlowReturn {

    public FlowReturnImpl(JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.FLOW_RETURN));
    }

    FlowReturnImpl(JSFConfigModelImpl myModel, Element element) {
        super(myModel, element);
    }

    @Override
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<FromOutcome> getFromOutcomes() {
        return getChildren(FromOutcome.class);
    }

    @Override
    public void addFromOutcome(FromOutcome fromOutcome) {
        appendChild(FROM_OUTCOME, fromOutcome);
    }

    @Override
    public void removeFromOutcome(FromOutcome fromOutcome) {
        removeChild(FROM_OUTCOME, fromOutcome);
    }
}
