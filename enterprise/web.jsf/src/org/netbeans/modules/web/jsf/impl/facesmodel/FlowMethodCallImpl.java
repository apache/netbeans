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
import org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall;
import static org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall.DEFAULT_OUTCOME;
import static org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall.METHOD;
import static org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall.PARAMETER;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCallParameter;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.Method;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class FlowMethodCallImpl extends JSFConfigComponentImpl implements FlowMethodCall {

    public FlowMethodCallImpl(JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.METHOD_CALL));
    }

    FlowMethodCallImpl(JSFConfigModelImpl myModel, Element element) {
        super(myModel, element);
    }

    @Override
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<Method> getMethods() {
        return getChildren(Method.class);
    }

    @Override
    public void addMethod(Method method) {
        appendChild(METHOD, method);
    }

    @Override
    public void removeMethod(Method method) {
        removeChild(METHOD, method);
    }

    @Override
    public List<FlowDefaultOutcome> getDefaultOutcomes() {
        return getChildren(FlowDefaultOutcome.class);
    }

    @Override
    public void addDefaultOutcome(FlowDefaultOutcome defaultOutcome) {
        appendChild(DEFAULT_OUTCOME, defaultOutcome);
    }

    @Override
    public void removeDefaultOutcome(FlowDefaultOutcome defaultOutcome) {
        removeChild(DEFAULT_OUTCOME, defaultOutcome);
    }

    @Override
    public List<FlowCallParameter> getParameters() {
        return getChildren(FlowCallParameter.class);
    }

    @Override
    public void addParameter(FlowCallParameter parameter) {
        appendChild(PARAMETER, parameter);
    }

    @Override
    public void removeParameter(FlowCallParameter parameter) {
        removeChild(PARAMETER, parameter);
    }


}
