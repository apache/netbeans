/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
