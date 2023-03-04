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

package org.netbeans.modules.websvc.editor.hints;

import java.util.Collection;
import java.util.LinkedList;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.common.RulesEngine;
import org.netbeans.modules.websvc.editor.hints.rules.*;

/**
 *
 * @author Ajit.Bhate@Sun.COM
 */
public class WebServicesRulesEngine extends RulesEngine {
    private static final LinkedList<Rule<TypeElement>> classRules = new LinkedList<Rule<TypeElement>>();
    private static final LinkedList<Rule<ExecutableElement>> operationRules = new LinkedList<Rule<ExecutableElement>>();
    private static final LinkedList<Rule<VariableElement>> paramRules = new LinkedList<Rule<VariableElement>>();
    
    static{
        //class rules
        classRules.add(new NoOperations());
        classRules.add(new InvalidJSRAnnotations());
        classRules.add(new InvalidNameAttribute());
        classRules.add(new DefaultPackage());
        classRules.add(new InterfaceServiceName());
        classRules.add(new InterfaceEndpointInterface());
        classRules.add(new HandlerChainAndSoapMessageHandlers());
        classRules.add(new RPCStyleWrappedParameterStyle());
        //operation rules
        operationRules.add(new InvalidWebMethodAnnotation());
        operationRules.add(new OnewayOperationReturnType());
        operationRules.add(new OnewayOperationParameterMode());
        operationRules.add(new OnewayOperationExceptions());
        operationRules.add(new InvalidExcludeAttribute());
        //parameters rules
        paramRules.add(new WebParamHolder());
        paramRules.add(new WebParamDuplicity());
    }
    
    protected Collection<Rule<TypeElement>> getClassRules() {
        return classRules;
    }

    protected Collection<Rule<ExecutableElement>> getOperationRules() {
        return operationRules;
    }

    protected Collection<Rule<VariableElement>> getParameterRules() {
        return paramRules;
    }

}
