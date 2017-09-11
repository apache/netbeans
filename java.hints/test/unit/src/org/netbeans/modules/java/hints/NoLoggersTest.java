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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author vita
 */
public class NoLoggersTest extends NbTestCase {

    public NoLoggersTest(String name) {
        super(name);
    }

    public void testSimple() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {}")
                .run(NoLoggers.class)
                .assertWarnings("1:13-1:17:verifier:No logger declared for test.Test class");
    }

    public void testSimpleFix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "}")
                .run(NoLoggers.class)
                .findWarning("1:13-1:17:verifier:No logger declared for test.Test class")
                .applyFix("MSG_NoLoggers_checkNoLoggers_Fix:test.Test")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test {\n" +
                              "    private static final Logger LOG = Logger.getLogger(Test.class.getName());\n" +
                              "}");
    }

    public void testLoggerName1Fix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private String LOG;" +
                       "}")
                .run(NoLoggers.class)
                .findWarning("1:13-1:17:verifier:No logger declared for test.Test class")
                .applyFix("MSG_NoLoggers_checkNoLoggers_Fix:test.Test")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test {\n" +
                              "    private String LOG;" +
                              "    private static final Logger LOGGER = Logger.getLogger(Test.class.getName());\n" +
                              "}");
    }

    public void testLoggerName2Fix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private String LOG;" +
                       "    private String LOGGER;" +
                       "}")
                .run(NoLoggers.class)
                .findWarning("1:13-1:17:verifier:No logger declared for test.Test class")
                .applyFix("MSG_NoLoggers_checkNoLoggers_Fix:test.Test")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test {\n" +
                              "    private String LOG;" +
                              "    private String LOGGER;" +
                              "    private static final Logger LOG1 = Logger.getLogger(Test.class.getName());\n" +
                              "}");
    }

    public void testNoWarningsForAbstractClass() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public abstract class Test {}")
                .run(NoLoggers.class)
                .assertWarnings();
    }

    public void testNoWarningsForInterface() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public interface Test {}")
                .run(NoLoggers.class)
                .assertWarnings();
    }

    public void testNoWarningsForEnum() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public enum Test {}")
                .run(NoLoggers.class)
                .assertWarnings();
    }

    public void testNoWarningsForInnerClasses() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public abstract class Test {\n" +
                       "    public static class Inner {\n" +
                       "    }\n" +
                       "}")
                .run(NoLoggers.class)
                .assertWarnings();
    }

    public void testCustomLogger() throws Exception {
        HintTest
                .create()
                .preference(LoggerHintsCustomizer.CUSTOM_LOGGERS_ENABLED, true)
                .preference(LoggerHintsCustomizer.CUSTOM_LOGGERS, "java.lang.String")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final String LOG = null;\n" +
                       "}")
                .run(NoLoggers.class)
                .assertWarnings();
    }
}
