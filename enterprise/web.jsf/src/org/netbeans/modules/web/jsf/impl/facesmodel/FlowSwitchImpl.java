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
import org.netbeans.modules.web.jsf.api.facesmodel.FlowSwitch;
import static org.netbeans.modules.web.jsf.api.facesmodel.FlowSwitch.NAVIGATION_CASE;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class FlowSwitchImpl extends IdentifiableComponentImpl implements FlowSwitch {

    public FlowSwitchImpl(JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.SWITCH));
    }

    FlowSwitchImpl(JSFConfigModelImpl myModel, Element element) {
        super(myModel, element);
    }

    @Override
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<NavigationCase> getNavigationCases() {
        return getChildren(NavigationCase.class);
    }

    @Override
    public void addNavigationCase(NavigationCase navigationCase) {
        appendChild(NAVIGATION_CASE, navigationCase);
    }

    @Override
    public void removeNavigationCase(NavigationCase navigationCase) {
        removeChild(NAVIGATION_CASE, navigationCase);
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
}
