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

package org.netbeans.modules.java.hints.declarative.test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.SourceVersion;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.java.hints.declarative.test.TestParser.TestCase;

/**
 *
 * @author lahvac
 */
public class TestParserTest {

    public TestParserTest() {}

    @Test
    public void testParse1() {
        String code = "%%TestCase name\ncode\n%%=>\nfixed1\n%%=>\nfixed2\n";

        code += code;
        
        List<String> golden = Arrays.asList("name:code\n:[fixed1\n, fixed2\n]:0:16:[26, 38]",
                                            "name:code\n:[fixed1\n, fixed2\n]:45:61:[71, 83]");
        List<String> testCases = new LinkedList<String>();

        for (TestCase ts : TestParser.parse(code)) {
            testCases.add(ts.toString());
        }

        assertEquals(golden, testCases);
    }

    @Test
    public void testNoResults() {
        String code = "%%TestCase name\ncode\n";

        code += code;

        List<String> golden = Arrays.asList("name:code\n:[]:0:16:[]",
                                            "name:code\n:[]:21:37:[]");
        List<String> testCases = new LinkedList<String>();

        for (TestCase ts : TestParser.parse(code)) {
            testCases.add(ts.toString());
        }

        assertEquals(golden, testCases);
    }

    @Test
    public void testSourceLevelOption() {
        String code = "%%TestCase name source-level=1.4\ncode\n%%=>\nfixed1\n%%=>\nfixed2\n";
        TestCase[] tests = TestParser.parse(code);
        
        assertEquals(1, tests.length);

        assertEquals(SourceVersion.RELEASE_4, tests[0].getSourceLevel());
        assertEquals("name:code\n:[fixed1\n, fixed2\n]:0:33:[43, 55]", tests[0].toString());
    }
}