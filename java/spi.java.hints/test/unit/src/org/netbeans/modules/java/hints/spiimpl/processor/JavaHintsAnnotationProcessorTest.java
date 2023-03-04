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
package org.netbeans.modules.java.hints.spiimpl.processor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Locale;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.AnnotationProcessorTestUtils;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author lahvac
 */
public class JavaHintsAnnotationProcessorTest extends NbTestCase {

    public JavaHintsAnnotationProcessorTest(String name) {
        super(name);
    }

    private Locale originalLocale;

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Locale.setDefault(originalLocale);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    public void testErrors1() throws Exception {
        File src = new File(getWorkDir(), "src");
        File dest = new File(getWorkDir(), "classes");
        AnnotationProcessorTestUtils.makeSource(src, "p.H",
                "import org.netbeans.spi.java.hints.*;\n",
                "import org.netbeans.spi.editor.hints.*;\n",
                "@Hint(category=\"general\", displayName=\"aa\", description=\"aab\")\n",
                "public class H {\n",
                "    @TriggerPattern(\"$1.$2\")\n",
                "    public static String h1(HintContext ctx) { return null;}\n",
                "    @TriggerPattern(\"$1.$2.$3\")\n",
                "    public static ErrorDescription h2() { return null;}\n",
                "    @TriggerPatterns({@TriggerPattern(\"$1.$2.$3.$4\")})\n",
                "    private ErrorDescription h3(HintContext ctx) { return null;}\n",
                "    @TriggerPattern(value=\"$1.isEmpty()\", constraints=@ConstraintVariableType(variable=\"$unknown\", type=\"java.lang.String\"))\n",
                "    public static ErrorDescription h4(HintContext ctx) { return null;}\n",
                "}\n");
        TestFileUtils.writeFile(new File(src, "p/Bundle.properties"), "");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        String errors = err.toString();
        assertTrue(errors.contains("error: The return type must be either org.netbeans.spi.editor.hints.ErrorDescription or java.util.List<org.netbeans.spi.editor.hints.ErrorDescription>"));
        assertTrue(errors.contains("error: The method must have exactly one parameter of type org.netbeans.spi.java.hints.HintContext"));
        assertTrue(errors.contains("error: The method must be static"));
        assertTrue(errors.contains("warning: Variable $unknown not used in the pattern"));
    }

    public void testErrors2() throws Exception {
        File src = new File(getWorkDir(), "src");
        File dest = new File(getWorkDir(), "classes");
        AnnotationProcessorTestUtils.makeSource(src, "p.H",
                "import org.netbeans.spi.java.hints.*;\n",
                "import org.netbeans.spi.editor.hints.*;\n",
                "import java.util.*;\n",
                "@Hint(displayName=\"#DN_p.H\", description=\"#DESC_p.H\", category=\"general\")\n",
                "public class H {\n",
                "    @TriggerPattern(\"$1.$2.$3\")\n",
                "    public static List<ErrorDescription> hint(HintContext ctx) { return null;}\n",
                "}\n");
        TestFileUtils.writeFile(new File(src, "p/Bundle.properties"), "DN_p.H=DN_p.H\nDESC_p.H=DESC_p.H\n");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
    }

    public void testCustomizerProvider() throws Exception {
        File src = new File(getWorkDir(), "src");
        File dest = new File(getWorkDir(), "classes");
        AnnotationProcessorTestUtils.makeSource(src, "p.H",
                "import org.netbeans.spi.java.hints.*;\n",
                "import org.netbeans.spi.editor.hints.*;\n",
                "import java.util.*;\n",
                "import java.util.prefs.*;\n",
                "import javax.swing.*;\n",
                "@Hint(displayName=\"dn\", description=\"desc\", category=\"general\", customizerProvider=H.Customizer.class)\n",
                "public class H {\n",
                "    @TriggerPattern(\"$1.$2.$3\")\n",
                "    public static List<ErrorDescription> hint(HintContext ctx) { return null;}\n",
                "    static final class Customizer implements CustomizerProvider {\n",
                "        @Override public JComponent getCustomizer(Preferences prefs) {\n",
                "            return new JPanel();\n",
                "        }\n",
                "    }\n",
                "}\n");
        TestFileUtils.writeFile(new File(src, "p/Bundle.properties"), "DN_p.H=DN_p.H\nDESC_p.H=DESC_p.H\n");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        String errors = err.toString();
        assertTrue(errors.contains("error: Customizer provider must be public"));
        assertTrue(errors.contains("error: Customizer provider must provide a public default constructor"));
    }

    public void testErrorsMessagesOnMethod() throws Exception {
        File src = new File(getWorkDir(), "src");
        File dest = new File(getWorkDir(), "classes");
        AnnotationProcessorTestUtils.makeSource(src, "p.H",
                "import org.netbeans.spi.java.hints.*;\n",
                "import org.netbeans.spi.editor.hints.*;\n",
                "import org.openide.util.NbBundle.Messages;\n",
                "import java.util.*;\n",
                "public class H {\n",
                "    @Hint(displayName=\"#DN_p.H\", description=\"#DESC_p.H\", category=\"general\")\n",
                "    @TriggerPattern(\"$1.$2.$3\")\n",
                "    @Messages({\"DN_p.H=1\", \"DESC_p.H=2\"})\n",
                "    public static List<ErrorDescription> hint(HintContext ctx) { return null;}\n",
                "}\n");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        boolean result = AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err);
        assertTrue(err.toString(), result);
    }
}
