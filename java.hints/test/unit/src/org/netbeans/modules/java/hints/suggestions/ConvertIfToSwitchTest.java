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
package org.netbeans.modules.java.hints.suggestions;

import org.netbeans.modules.java.hints.jdk.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 * Note: many of the cases are already tested in StringToSwitchTest. String
 * conversion works for equals only, so null-ity may not be tested fully.
 *
 * @author Svata
 */
public class ConvertIfToSwitchTest extends NbTestCase {

    public ConvertIfToSwitchTest(String name) {
        super(name);
    }

    public void testPrimitiveWithDefault() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                        + "\n"
                        + "public abstract class Test {\n"
                        + "    public String toString(int t) {\n"
                        + "	String s;\n"
                        + "	if (t == 1) {\n"
                        + "		s = \"1\";\n"
                        + "	} else if (t == 2) {\n"
                        + "		s = \"2\";\n"
                        + "	} else {\n"
                        + "		s = \"3\";\n"
                        + "	}\n"
                        + "	return \"number is \" + s;\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.7")
                .run(ConvertIfToSwitch.class)
                .findWarning("5:1-5:14:verifier:HINT_ConvertIfToSwitch")
                .applyFix("FIX_ConvertIfsToSwitch")
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "\n"
                        + "public abstract class Test {\n"
                        + "    public String toString(int t) {\n"
                        + "	String s;\n"
                        + "        switch (t) {\n"
                        + "            case 1:\n"
                        + "                s = \"1\";\n"
                        + "                break;\n"
                        + "            case 2:\n"
                        + "                s = \"2\";\n"
                        + "                break;\n"
                        + "            default:\n"
                        + "                s = \"3\";\n"
                        + "                break;\n"
                        + "        }\n"
                        + "	return \"number is \" + s;\n"
                        + "    }\n"
                        + "}\n"
                        + "");
    }

    public void testUsedValueFromElseBranch259071() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                        + "\n"
                        + "public abstract class Test {\n"
                        + "    public enum testEnum{\n"
                        + "            ONE,\n"
                        + "            TWO,\n"
                        + "            THREE;\n"
                        + "    }\n"
                        + "    \n"
                        + "    public String toString(testEnum t) {\n"
                        + "	String s;\n"
                        + "	if (t == testEnum.ONE) {\n"
                        + "		s = \"1\";\n"
                        + "	} else if (t == testEnum.TWO) {\n"
                        + "		s = \"2\";\n"
                        + "	} else {\n"
                        + "		s = \"3\";\n"
                        + "	}\n"
                        + "	return \"number is \" + s;\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.7")
                .run(ConvertIfToSwitch.class)
                .findWarning("11:1-11:25:verifier:HINT_ConvertIfToSwitch")
                .applyFix("FIX_ConvertIfsToSwitch")
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "\n"
                        + "public abstract class Test {\n"
                        + "    public enum testEnum{\n"
                        + "            ONE,\n"
                        + "            TWO,\n"
                        + "            THREE;\n"
                        + "    }\n"
                        + "    \n"
                        + "    public String toString(testEnum t) {\n"
                        + "	String s;\n"
                        + "	if (null == t) {\n"
                        + "            s = \"3\";\n"
                        + "        } else switch (t) {\n"
                        + "            case ONE:\n"
                        + "                s = \"1\";\n"
                        + "                break;\n"
                        + "            case TWO:\n"
                        + "                s = \"2\";\n"
                        + "                break;\n"
                        + "            default:\n"
                        + "                s = \"3\";\n"
                        + "                break;\n"
                        + "        }\n"
                        + "	return \"number is \" + s;\n"
                        + "    }\n"
                        + "}");
    }
}
