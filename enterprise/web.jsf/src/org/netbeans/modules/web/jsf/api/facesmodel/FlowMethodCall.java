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
public interface FlowMethodCall extends JSFConfigComponent {

    /**
     * Property name of &lt;method&gt; element.
     */
    static final String METHOD = JSFConfigQNames.METHOD.getLocalName();

    /**
     * Property name of &lt;default-outcome&gt; element.
     */
    static final String DEFAULT_OUTCOME = JSFConfigQNames.DEFAULT_OUTCOME.getLocalName();

    /**
     * Property name of &lt;parameter&gt; element.
     */
    static final String PARAMETER = JSFConfigQNames.PARAMETER.getLocalName();

    List<Method> getMethods();
    void addMethod(Method method);
    void removeMethod(Method method);

    List<FlowDefaultOutcome> getDefaultOutcomes();
    void addDefaultOutcome(FlowDefaultOutcome defaultOutcome);
    void removeDefaultOutcome(FlowDefaultOutcome defaultOutcome);

    List<FlowCallParameter> getParameters();
    void addParameter(FlowCallParameter parameter);
    void removeParameter(FlowCallParameter parameter);

}
