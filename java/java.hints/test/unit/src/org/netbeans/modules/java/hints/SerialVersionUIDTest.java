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
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.modules.java.hints.spiimpl.TestCompilerSettings;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.openide.util.NbBundle;

/**
 * The following shell script was used to generate the code snippets
 * <code>cat test/unit/data/test/Test.java | tr '\n' ' ' | tr '\t' ' ' | sed -E 's| +| |g' | sed 's|"|\\"|g'</code>
 * @author Samuel Halliday
 */
public class SerialVersionUIDTest extends ErrorHintsTestBase {

    private String HINT_SUPPRESS;
    private String HINT_DEFAULT;
    private String HINT_GENERATED;

    public SerialVersionUIDTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        HINT_SUPPRESS = NbBundle.getMessage(ErrorDescriptionFactory.class, "LBL_FIX_Suppress_Waning", "serial");
        HINT_DEFAULT = NbBundle.getMessage(SerialVersionUID.class, "HINT_SerialVersionUID");
        HINT_GENERATED = NbBundle.getMessage(SerialVersionUID.class, "HINT_SerialVersionUID_Generated");
        TestCompilerSettings.commandLine = "-Xlint:serial";
    }

    @Override
    protected void tearDown() throws Exception {
        TestCompilerSettings.commandLine = null;
        super.tearDown();
    }

    public void testSerialVersionUID1() throws Exception {
        String test = "package test; import java.io.Serializable; public interface T|est extends Serializable { }";
        performAnalysisTest(test);
    }

    public void testSerialVersionUID2() throws Exception {
        String test = "package test; import java.io.Serializable; @SuppressWarnings(\"serial\") public class T|est implements Serializable { }";
        performAnalysisTest(test);
    }

    public void testSerialVersionUID3() throws Exception {
        String test = "package test; import java.io.Serializable; @SuppressWarnings(\"serial\") abstract public class T|est implements Serializable { }";
        performAnalysisTest(test);
    }

    public void testSerialVersionUID4() throws Exception {
        String test = "package test; import java.io.Serializable; public class Te|st implements Serializable { private static final long serialVersionUID = 1L; }";
        performAnalysisTest(test);
    }

    public void testSerialVersionUID5() throws Exception {
        String test = "package test; import java.io.Serializable; abstract public class Te|st implements Serializable { private static final long serialVersionUID = 1L; }";
        performAnalysisTest(test);
    }

    public void testSerialVersionUIDSuppress1() throws Exception {
        String test = "package test;\nimport java.io.Serializable;\npublic class Te|st implements Serializable { }";
        String golden = "package test; import java.io.Serializable; @SuppressWarnings(\"serial\") public class Test implements Serializable { }";
        performFixTest(test, golden, HINT_SUPPRESS);
    }

    public void testSerialVersionUIDSuppress2() throws Exception {
        String test = "package test;\nimport java.io.Serializable;\nabstract public class T|est implements Serializable { }";
        String golden = "package test; import java.io.Serializable; @SuppressWarnings(\"serial\") abstract public class Test implements Serializable { }";
        performFixTest(test, golden, HINT_SUPPRESS);
    }

    public void testSerialVersionUIDDefault1() throws Exception {
        String test = "package test; import java.io.Serializable; public class Te|st implements Serializable { }";
        String golden = "package test; import java.io.Serializable; public class Test implements Serializable { private static final long serialVersionUID = 1L; }";
        performFixTest(test, golden, HINT_DEFAULT);
    }

    public void testSerialVersionUIDDefault2() throws Exception {
        String test = "package test; import java.io.Serializable; abstract public class Te|st implements Serializable { }";
        String golden = "package test; import java.io.Serializable; abstract public class Test implements Serializable { private static final long serialVersionUID = 1L; }";
        performFixTest(test, golden, HINT_DEFAULT);
    }

    public void testSerialVersionNotOfferedForEnum() throws Exception {
        String test = "package test; public enum Te|st { B, C }";
        performAnalysisTest(test);
    }

    public void testAnonymous() throws Exception {
        String test = "package test; public class Test {private Serializable ser = new Serializable() {| public String toString() {return \"Hello from serializable\";}};}";
        String golden = "package test; public class Test {private Serializable ser = new Serializable() { private static final long serialVersionUID = 1L; public String toString() {return \"Hello from serializable\";}};}";
        performFixTest(test, golden, HINT_DEFAULT);
    }

    // test is single line source code for test.Test, | in the CLASS, space before, space after
    // golden is the output to test against
    private void performFixTest(String test, String golden, String hint) throws Exception {
        performFixTest("test/Test.java", test, hint, golden);
    }

    // test is single line source code for test.Test, | in the CLASS, space before, space after
    // completes successfully if there are no hints presented
    private void performAnalysisTest(String test) throws Exception {
        prepareTest("test/Test.java", test.replace("|", ""));

        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new SerialVersionUID().run(info, null, pos, path, null);
    }
}
