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
package org.netbeans.modules.java.hints.spiimpl.processor;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
