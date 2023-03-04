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
import org.netbeans.modules.web.jsf.api.facesmodel.FlowFinalizer;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition;
import static org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition.INBOUND_PARAMETER;
import static org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition.METHOD_CALL;
import static org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition.START_NODE;
import static org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition.VIEW;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCall;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCallInboundParameter;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowReturn;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowSwitch;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowView;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowInitializer;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowStartNode;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class FlowDefinitionImpl extends IdentifiableDescriptionGroupImpl implements FlowDefinition {

    public FlowDefinitionImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }

    public FlowDefinitionImpl(JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.FLOW_DEFINITION));
    }

    @Override
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<FlowStartNode> getStartNodes() {
        return getChildren(FlowStartNode.class);
    }

    @Override
    public void addStartNode(FlowStartNode startNode) {
        appendChild(START_NODE, startNode);
    }

    @Override
    public void removeStartNode(FlowStartNode startNode) {
        removeChild(START_NODE, startNode);
    }

    @Override
    public List<FlowView> getViews() {
        return getChildren(FlowView.class);
    }

    @Override
    public void addView(FlowView definitionView) {
        appendChild(VIEW, definitionView);
    }

    @Override
    public void removeView(FlowView definitionView) {
        removeChild(VIEW, definitionView);
    }

    @Override
    public List<FlowSwitch> getSwitches() {
        return getChildren(FlowSwitch.class);
    }

    @Override
    public void addSwitch(FlowSwitch definitionSwitch) {
        appendChild(SWITCH, definitionSwitch);
    }

    @Override
    public void removeSwitch(FlowSwitch definitionSwitch) {
        removeChild(SWITCH, definitionSwitch);
    }

    @Override
    public List<FlowReturn> getFlowReturns() {
        return getChildren(FlowReturn.class);
    }

    @Override
    public void addFlowReturn(FlowReturn flowReturn) {
        appendChild(FLOW_RETURN, flowReturn);
    }

    @Override
    public void removeFlowReturn(FlowReturn flowReturn) {
        removeChild(FLOW_RETURN, flowReturn);
    }

    @Override
    public List<NavigationRule> getNavigationRules() {
        return getChildren(NavigationRule.class);
    }

    @Override
    public void addNavigationRule(NavigationRule navigationRule) {
        appendChild(NAVIGATION_RULE, navigationRule);
    }

    @Override
    public void removeNavigationRule(NavigationRule navigationRule) {
        removeChild(NAVIGATION_RULE, navigationRule);
    }

    @Override
    public List<FlowCall> getFlowCalls() {
        return getChildren(FlowCall.class);
    }

    @Override
    public void addFlowCall(FlowCall flowCall) {
        appendChild(FLOW_CALL, flowCall);
    }

    @Override
    public void removeFlowCall(FlowCall flowCall) {
        removeChild(FLOW_CALL, flowCall);
    }

    @Override
    public List<FlowMethodCall> getMethodCalls() {
        return getChildren(FlowMethodCall.class);
    }

    @Override
    public void addMethodCall(FlowMethodCall methodCall) {
        appendChild(METHOD_CALL, methodCall);
    }

    @Override
    public void removeMethodCall(FlowMethodCall methodCall) {
        removeChild(METHOD_CALL, methodCall);
    }

    @Override
    public List<FlowInitializer> getInitializers() {
        return getChildren(FlowInitializer.class);
    }

    @Override
    public void addInitializer(FlowInitializer initializer) {
        appendChild(START_NODE, initializer);
    }

    @Override
    public void removeInitializer(FlowInitializer initializer) {
        removeChild(START_NODE, initializer);
    }

    @Override
    public List<FlowFinalizer> getFinalizers() {
        return getChildren(FlowFinalizer.class);
    }

    @Override
    public void addFinalizer(FlowFinalizer finalizer) {
        appendChild(START_NODE, finalizer);
    }

    @Override
    public void removeFinalizer(FlowFinalizer finalizer) {
        removeChild(START_NODE, finalizer);
    }

    @Override
    public List<FlowCallInboundParameter> getInboundParameters() {
        return getChildren(FlowCallInboundParameter.class);
    }

    @Override
    public void addInboundParameter(FlowCallInboundParameter inboundParameter) {
        appendChild(INBOUND_PARAMETER, inboundParameter);
    }

    @Override
    public void removeInboundParameter(FlowCallInboundParameter inboundParameter) {
        removeChild(INBOUND_PARAMETER, inboundParameter);
    }
}
