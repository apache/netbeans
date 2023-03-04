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

package org.netbeans.modules.java.hints.declarative;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.java.hints.declarative.Condition.MethodInvocation.ParameterKind;
import org.netbeans.modules.java.hints.declarative.conditionapi.Context;
import org.netbeans.modules.java.hints.declarative.conditionapi.Matcher;
import org.netbeans.modules.java.hints.declarative.conditionapi.Variable;

/**
 *
 * @author lahvac
 */
public class MethodInvocationContextTest {

    @Test
    public void testSetCode() {
        MethodInvocationContext mic = new MethodInvocationContext();

        mic.setCode("", Arrays.asList("public boolean test() {return false;}"));
        Method m =mic.linkMethod("test", Collections.<String, ParameterKind>emptyMap());
        assertFalse(mic.invokeMethod(null, m, Collections.<String, ParameterKind>emptyMap()));
    }

    @Test
    public void testPerformance() {
        MethodInvocationContext mic = new MethodInvocationContext();

        mic.setCode("", Collections.<String>emptyList());
        assertEquals(1, mic.ruleUtilities.size());
    }

    @Test
    public void testVarArg1() {
        MethodInvocationContext mic = new MethodInvocationContext();

        mic.ruleUtilities.add(TestConditionClass.class);

        Map<String, ParameterKind> params = new LinkedHashMap<>();

        params.put("$v", ParameterKind.VARIABLE);
        params.put("a", ParameterKind.STRING_LITERAL);
        params.put("b", ParameterKind.STRING_LITERAL);

        Method m = mic.linkMethod("test", params);

        assertNotNull(m);
        assertFalse(mic.invokeMethod(null, m, params));
    }

    @Test
    public void testPreferNonVarArg() {
        MethodInvocationContext mic = new MethodInvocationContext();

        mic.ruleUtilities.add(TestConditionClass.class);

        Map<String, ParameterKind> params = new LinkedHashMap<>();

        params.put("$v1", ParameterKind.VARIABLE);
        params.put("$v2", ParameterKind.VARIABLE);

        Method m = mic.linkMethod("preferNonVarArgs", params);

        assertNotNull(m);
        assertFalse(m.isVarArgs());
    }

    public static final class TestConditionClass {
        public TestConditionClass(Context ctx, Matcher m) {}
        public boolean test(Variable var, String... strings) {
            //TODO; verify variable name:
//            assertEquals("$v", var.variableName);
            assertEquals(2, strings.length);
            assertEquals("a", strings[0]);
            assertEquals("b", strings[1]);
            return false;
        }

        public boolean preferNonVarArgs(Variable v1, Variable... v2) {
            return false;
        }

        public boolean preferNonVarArgs(Variable v1, Variable v2) {
            return false;
        }
    }
}
