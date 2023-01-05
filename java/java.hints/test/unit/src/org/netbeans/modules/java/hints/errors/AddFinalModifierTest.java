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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;

/**
 * Testdata taken from {@link MakeVariableFinalTest}
 *
 * @author markiewb
 */
public class AddFinalModifierTest extends ErrorHintsTestBase {

    public AddFinalModifierTest(String testName) {
	super(testName);
    }

    public void testAddFinalToVariable() throws Exception {
	performFixTest("test/Test.java",
		"package org.netbeans.test.java.hints;"
		+ "public class MakeVariableFinal {"
		+ "    public static void main() {"
		+ "        byte g = 9;"
		+ "        new Runnable() {"
		+ "            public void run() {"
		+ "                System.err.println(g);"
		+ "            }"
		+ "        };"
		+ "    }"
		+ "}",
		-1,
		Bundle.FIX_MakeVariableFinal("g"),
		("package org.netbeans.test.java.hints;"
		+ "public class MakeVariableFinal {"
		+ "    public static void main() {"
		+ "        final byte g = 9;"
		+ "        new Runnable() {"
		+ "            public void run() {"
		+ "                System.err.println(g);"
		+ "            }"
		+ "        };"
		+ "    }"
		+ "}").replaceAll("[\\s]+", " "));
    }
    public void testAddFinalToParameter() throws Exception {
	performFixTest("test/Test.java",
		"package org.netbeans.test.java.hints;"
		+ "public class MakeVariableFinal {"
		+ "    public static void main(byte f) {"
		+ "        new Runnable() {"
		+ "            public void run() {"
		+ "                System.err.println(f);"
		+ "            }"
		+ "        };"
		+ "    }"
		+ "}",
		-1,
		Bundle.FIX_MakeVariableFinal("f"),
		("package org.netbeans.test.java.hints;"
		+ ""
		+ "public class MakeVariableFinal {"
		+ "    public static void main(final byte f) {"
		+ "        new Runnable() {"
		+ "            public void run() {"
		+ "                System.err.println(f);"
		+ "            }"
		+ "        };"
		+ "    }"
		+ "}").replaceAll("[\\s]+", " "));
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
	return AddOrRemoveFinalModifier.createAddFinalModifier().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
	return f.getText();
    }

    @Override
    protected Set<String> getSupportedErrorKeys() {
	return AddOrRemoveFinalModifier.createAddFinalModifier().getCodes();
    }
    public static Test suite() {
        //javac no longer supports any source level that would produce this error
        return new TestCase("noop") {
            @Override
            public void runBare() throws Throwable {
            }
        };
    }
}
