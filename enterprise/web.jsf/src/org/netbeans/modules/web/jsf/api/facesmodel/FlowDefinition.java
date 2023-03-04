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
package org.netbeans.modules.web.jsf.api.facesmodel;

import java.util.List;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public interface FlowDefinition extends ApplicationElement, IdentifiableElement, DescriptionGroup {

    /**
     * Property name of &lt;start-node&gt; element.
     * Name of the flow start node.
     */
    static final String START_NODE = JSFConfigQNames.START_NODE.getLocalName();

    /**
     * Property name of &lt;view&gt; element.
     */
    static final String VIEW = JSFConfigQNames.VIEW.getLocalName();

    /**
     * Property name of &lt;switch&gt; element.
     */
    static final String SWITCH = JSFConfigQNames.SWITCH.getLocalName();

    /**
     * Property name of &lt;flow-return&gt; element.
     */
    static final String FLOW_RETURN = JSFConfigQNames.FLOW_RETURN.getLocalName();

    /**
     * Property name of &lt;navigation-rule&gt; element.
     */
    static final String NAVIGATION_RULE = JSFConfigQNames.NAVIGATION_RULE.getLocalName();

    /**
     * Property name of &lt;flow-call&gt; element.
     */
    static final String FLOW_CALL = JSFConfigQNames.FLOW_CALL.getLocalName();

    /**
     * Property name of &lt;method-call&gt; element.
     */
    static final String METHOD_CALL = JSFConfigQNames.METHOD_CALL.getLocalName();

    /**
     * Property name of &lt;initializer&gt; element.
     */
    static final String INITIALIZER = JSFConfigQNames.INITIALIZER.getLocalName();

    /**
     * Property name of &lt;finalizer&gt; element.
     */
    static final String FINALIZER = JSFConfigQNames.FINALIZER.getLocalName();

    /**
     * Property name of &lt;inbound-parameter&gt; element.
     */
    static final String INBOUND_PARAMETER = JSFConfigQNames.INBOUND_PARAMETER.getLocalName();

    List<FlowStartNode> getStartNodes();
    void addStartNode(FlowStartNode startNode);
    void removeStartNode(FlowStartNode startNode);

    List<FlowView> getViews();
    void addView(FlowView definitionView);
    void removeView(FlowView definitionView);

    List<FlowSwitch> getSwitches();
    void addSwitch(FlowSwitch definitionSwitch);
    void removeSwitch(FlowSwitch definitionSwitch);

    List<FlowReturn> getFlowReturns();
    void addFlowReturn(FlowReturn flowReturn);
    void removeFlowReturn(FlowReturn flowReturn);

    List<NavigationRule> getNavigationRules();
    void addNavigationRule(NavigationRule navigationRule);
    void removeNavigationRule(NavigationRule navigationRule);

    List<FlowCall> getFlowCalls();
    void addFlowCall(FlowCall flowCall);
    void removeFlowCall(FlowCall flowCall);

    List<FlowMethodCall> getMethodCalls();
    void addMethodCall(FlowMethodCall methodCall);
    void removeMethodCall(FlowMethodCall methodCall);

    List<FlowInitializer> getInitializers();
    void addInitializer(FlowInitializer initializer);
    void removeInitializer(FlowInitializer initializer);

    List<FlowFinalizer> getFinalizers();
    void addFinalizer(FlowFinalizer finalizer);
    void removeFinalizer(FlowFinalizer finalizer);

    List<FlowCallInboundParameter> getInboundParameters();
    void addInboundParameter(FlowCallInboundParameter inboundParameter);
    void removeInboundParameter(FlowCallInboundParameter inboundParameter);
}
