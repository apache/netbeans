/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
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
        Map<String, String> outVariables2Names = new HashMap<String, String>();

        assertTrue(MatcherUtilities.matches(ctx, ctx.getPath().getParentPath(), "$1 $0 = $_;", outVariables, outMultiVariables, outVariables2Names));
        assertEquals("int", outVariables.get("$1").getLeaf().toString());
        assertEquals("i", outVariables2Names.get("$0"));
    }

}