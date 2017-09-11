/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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

        Map<String, ParameterKind> params = new LinkedHashMap<String, ParameterKind>();

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

        Map<String, ParameterKind> params = new LinkedHashMap<String, ParameterKind>();

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
