/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.spi.java.hints;

import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.netbeans.modules.java.hints.spiimpl.SPIAccessor;
import org.netbeans.modules.java.hints.spiimpl.TestBase;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;

/**
 *
 * @author lahvac
 */
public class MatcherUtilitiesTest extends TestBase {

    public MatcherUtilitiesTest(String name) {
        super(name);
    }

    public void testParentMatches1() throws Exception {
        String code = "package test; public class Test { private int test() { int i = 0; i = test(|); } }";
        int pos = code.indexOf("|");

        code = code.replaceAll(Pattern.quote("|"), "");

        prepareTest("test/Test.java", code);

        TreePath tp = info.getTreeUtilities().pathFor(pos);
        HintContext ctx = SPIAccessor.getINSTANCE().createHintContext(info, HintsSettings.getGlobalSettings(), null, tp, Collections.<String, TreePath>emptyMap(), Collections.<String, Collection<? extends TreePath>>emptyMap(), Collections.<String, String>emptyMap());

        assertTrue(MatcherUtilities.matches(ctx, ctx.getPath().getParentPath(), "$0 = $_"));
    }

    public void testParentMatches2() throws Exception {
        String code = "package test; public class Test { private int test() { int i = test(|); } }";
        int pos = code.indexOf("|");

        code = code.replaceAll(Pattern.quote("|"), "");

        prepareTest("test/Test.java", code);

        TreePath tp = info.getTreeUtilities().pathFor(pos);
        HintContext ctx = SPIAccessor.getINSTANCE().createHintContext(info, HintsSettings.getGlobalSettings(), null, tp, Collections.<String, TreePath>emptyMap(), Collections.<String, Collection<? extends TreePath>>emptyMap(), Collections.<String, String>emptyMap());

        assertTrue(MatcherUtilities.matches(ctx, ctx.getPath().getParentPath(), "$1 $0 = $_;"));
    }

    public void testOutVariables1() throws Exception {
        String code = "package test; public class Test { private int test() { int i = test(|); } }";
        int pos = code.indexOf("|");

        code = code.replaceAll(Pattern.quote("|"), "");

        prepareTest("test/Test.java", code);

        TreePath tp = info.getTreeUtilities().pathFor(pos);
        HintContext ctx = SPIAccessor.getINSTANCE().createHintContext(info, HintsSettings.getGlobalSettings(), null, tp, Collections.<String, TreePath>emptyMap(), Collections.<String, Collection<? extends TreePath>>emptyMap(), Collections.<String, String>emptyMap());

        Map<String, TreePath> outVariables = new HashMap<String, TreePath>();
        Map<String, Collection<? extends TreePath>> outMultiVariables = new HashMap<String, Collection<? extends TreePath>>();
        Map<String, String> outVariables2Names = new HashMap<>();

        assertTrue(MatcherUtilities.matches(ctx, ctx.getPath().getParentPath(), "$1 $0 = $_;", outVariables, outMultiVariables, outVariables2Names));
        assertEquals("int", outVariables.get("$1").getLeaf().toString());
        assertEquals("i", outVariables2Names.get("$0"));
    }

}