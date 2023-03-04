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

import java.io.IOException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.netbeans.junit.NbTestSuite;

import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.rules.*;

/**
 *
 * @author Ajit
 */
public class WSHintsTest extends WSHintsTestBase {
    
    public WSHintsTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(WSHintsTest.class);
        return suite;
    }
    
    // class level hints
    public final void testDefaultPackage() throws IOException {
        Rule<TypeElement> instance = new DefaultPackage();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testHandlerChainAndSoapMessageHandlers() throws IOException {
        Rule<TypeElement> instance = new HandlerChainAndSoapMessageHandlers();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInterfaceEndpointInterface() throws IOException {
        Rule<TypeElement> instance = new InterfaceEndpointInterface();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInterfaceServiceName() throws IOException {
        Rule<TypeElement> instance = new InterfaceServiceName();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInvalidJSRAnnotations() throws IOException {
        Rule<TypeElement> instance = new InvalidJSRAnnotations();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInvalidNameAttribute() throws IOException {
        Rule<TypeElement> instance = new InvalidNameAttribute();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testRPCStyleWrappedParameterStyle() throws IOException {
        Rule<TypeElement> instance = new RPCStyleWrappedParameterStyle();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    // operation level hints
    public final void testOnewayOperationReturnType() throws IOException {
        Rule<ExecutableElement> instance = new OnewayOperationReturnType();
        getRulesEngine().getOperationRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testOnewayOperationExceptions() throws IOException {
        Rule<ExecutableElement> instance = new OnewayOperationExceptions();
        getRulesEngine().getOperationRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testOnewayOperationParameterMode() throws IOException {
        Rule<ExecutableElement> instance = new OnewayOperationParameterMode();
        getRulesEngine().getOperationRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInvalidExcludeAttribute() throws IOException {
        Rule<ExecutableElement> instance = new InvalidExcludeAttribute();
        getRulesEngine().getOperationRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    // parameter level hints
    public final void testWebParamHolder() throws IOException {
        Rule<VariableElement> instance = new WebParamHolder();
        getRulesEngine().getParameterRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testUnknownElement() throws IOException {
        Rule<TypeElement> instance = new DefaultPackage();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,"UnknownElementTest.java");
    }
}
