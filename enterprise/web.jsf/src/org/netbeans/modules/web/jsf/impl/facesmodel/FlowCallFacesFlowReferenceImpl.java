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
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCallFacesFlowReference;
import static org.netbeans.modules.web.jsf.api.facesmodel.FlowCallFacesFlowReference.FLOW_DOCUMENT_ID;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowDocumentId;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowId;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import static org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigComponentImpl.createElementNS;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class FlowCallFacesFlowReferenceImpl extends JSFConfigComponentImpl implements FlowCallFacesFlowReference {

    public FlowCallFacesFlowReferenceImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }

    public FlowCallFacesFlowReferenceImpl(JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.FLOW_REFERENCE));
    }

    @Override
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<FlowId> getFlowIds() {
        return getChildren(FlowId.class);
    }

    @Override
    public void addFlowId(FlowId flowId) {
        appendChild(FLOW_ID, flowId);
    }

    @Override
    public void removeFlowId(FlowId flowId) {
        removeChild(FLOW_ID, flowId);
    }

    @Override
    public List<FlowDocumentId> getFlowDocumentIds() {
        return getChildren(FlowDocumentId.class);
    }

    @Override
    public void addFlowDocumentId(FlowDocumentId flowDocumentId) {
        appendChild(FLOW_DOCUMENT_ID, flowDocumentId);
    }

    @Override
    public void removeFlowDocumentId(FlowDocumentId flowDocumentId) {
        removeChild(FLOW_DOCUMENT_ID, flowDocumentId);
    }

}
