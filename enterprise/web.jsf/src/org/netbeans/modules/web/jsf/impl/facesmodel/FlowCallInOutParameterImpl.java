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
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCallOutboundParameter;
import org.netbeans.modules.web.jsf.api.facesmodel.Name;
import org.netbeans.modules.web.jsf.api.facesmodel.Value;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public abstract class FlowCallInOutParameterImpl extends FlowCallParameterValueImpl implements FlowCallOutboundParameter {

    public FlowCallInOutParameterImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }

    @Override
    public List<Name> getNames() {
        return getChildren(Name.class);
    }

    @Override
    public void addName(Name name) {
        appendChild(NAME, name);
    }

    @Override
    public void removeName(Name name) {
        removeChild(NAME, name);
    }

    @Override
    public List<Value> getValues() {
        return getChildren(Value.class);
    }

    @Override
    public void addValue(Value value) {
        appendChild(VALUE, value);
    }

    @Override
    public void removeValue(Value value) {
        removeChild(VALUE, value);
    }

}
