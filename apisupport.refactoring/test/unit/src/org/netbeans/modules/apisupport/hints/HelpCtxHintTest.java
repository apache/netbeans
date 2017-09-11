/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.hints;

import java.net.URL;
import org.junit.Ignore;
import org.junit.Test;
import static org.netbeans.modules.apisupport.hints.Bundle.*;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

public class HelpCtxHintTest {

    @Test public void literalString() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    Object _ = new org.openide.util.HelpCtx(\"some.id\");\n" +
                       "}\n").
                run(HelpCtxHint.class).
                assertWarnings();
    }

    @Test public void constantString() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    Object _ = new org.openide.util.HelpCtx(\"some.\" + \"id\");\n" +
                       "}\n").
                run(HelpCtxHint.class).
                assertWarnings();
    }

    @Ignore // XXX need #209759 to implement check for constants
    @Test public void computedString() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    Object _ = new org.openide.util.HelpCtx(toString());\n" +
                       "}\n").
                run(HelpCtxHint.class).
                assertWarnings("2:15-2:55:verifier:nonconstant help ID");
    }

    @Test public void simpleClass() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    Object _ = new org.openide.util.HelpCtx(Test.class);\n" +
                       "}\n").
                run(HelpCtxHint.class).
                findWarning("2:15-2:55:verifier:" + HelpCtx_onClass_warning()).
                applyFix(HelpCtx_onClass_fix()).
                assertCompilable().
                assertOutput("package test;\n" +
                       "class Test {\n" +
                       "    Object _ = new org.openide.util.HelpCtx(\"test.Test\");\n" +
                       "}\n");
    }

    @Test public void className() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    Object _ = new org.openide.util.HelpCtx(Test.class.getName());\n" +
                       "}\n").
                run(HelpCtxHint.class).
                findWarning("2:15-2:65:verifier:" + HelpCtx_onClassName_warning()).
                applyFix(HelpCtx_onClass_fix()).
                assertCompilable().
                assertOutput("package test;\n" +
                       "class Test {\n" +
                       "    Object _ = new org.openide.util.HelpCtx(\"test.Test\");\n" +
                       "}\n");
    }

    @Test public void nestedClass() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    class Nested {\n" +
                       "        Object _ = new org.openide.util.HelpCtx(Nested.class);\n" +
                       "    }\n" +
                       "}\n").
                run(HelpCtxHint.class).
                findWarning("3:19-3:61:verifier:" + HelpCtx_onClass_warning()).
                applyFix(HelpCtx_onClass_fix()).
                assertCompilable().
                assertOutput("package test;\n" +
                       "class Test {\n" +
                       "    class Nested {\n" +
                       "        Object _ = new org.openide.util.HelpCtx(\"test.Test$Nested\");\n" +
                       "    }\n" +
                       "}\n");
    }

    @Test public void nestedClassName() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    class Nested {\n" +
                       "        Object _ = new org.openide.util.HelpCtx(Nested.class.getName());\n" +
                       "    }\n" +
                       "}\n").
                run(HelpCtxHint.class).
                findWarning("3:19-3:71:verifier:" + HelpCtx_onClassName_warning()).
                applyFix(HelpCtx_onClass_fix()).
                assertCompilable().
                assertOutput("package test;\n" +
                       "class Test {\n" +
                       "    class Nested {\n" +
                       "        Object _ = new org.openide.util.HelpCtx(\"test.Test$Nested\");\n" +
                       "    }\n" +
                       "}\n");
    }

    private URL cp() {
        URL cp = HelpCtx.class.getProtectionDomain().getCodeSource().getLocation();
        return cp.toString().endsWith("/") ? cp : FileUtil.getArchiveRoot(cp);
    }
}
