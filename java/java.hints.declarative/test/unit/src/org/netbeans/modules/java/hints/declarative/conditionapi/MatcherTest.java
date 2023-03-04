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

package org.netbeans.modules.java.hints.declarative.conditionapi;

import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import org.netbeans.modules.java.hints.spiimpl.SPIAccessor;
import org.netbeans.modules.java.hints.spiimpl.TestBase;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;

/**
 *
 * @author lahvac
 */
public class MatcherTest extends TestBase {

    public MatcherTest(String name) {
        super(name);
    }

    public void testReferencedInNoNPEForMissingTrees() throws Exception {
        String code = "package test; public class Test { private void test() { | if (true) System.err.println(); } private int a^aa;}";
        int pos = code.indexOf("|");
        
        code = code.replaceAll(Pattern.quote("|"), "");

        int varpos = code.indexOf("^");
        
        code = code.replaceAll(Pattern.quote("^"), "");

        prepareTest("test/Test.java", code);

        TreePath tp = info.getTreeUtilities().pathFor(pos);
        TreePath var = info.getTreeUtilities().pathFor(varpos);
        Map<String, TreePath> variables = Collections.singletonMap("$1", var);
        Map<String, Collection<? extends TreePath>> multiVariables = Collections.<String, Collection<? extends TreePath>>singletonMap("$2$", Arrays.asList(tp));
        Map<String, String> variables2Names = Collections.emptyMap();
        Context ctx = new Context(SPIAccessor.getINSTANCE().createHintContext(info, HintsSettings.getGlobalSettings(), null, null, variables, multiVariables, variables2Names));

        new Matcher(ctx).referencedIn(new Variable("$1"), new Variable("$2$"));
    }

}
