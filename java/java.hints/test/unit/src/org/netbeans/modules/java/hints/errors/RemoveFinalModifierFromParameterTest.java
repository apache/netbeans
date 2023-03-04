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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;

/**
 * Based on {@link AddCastTest}
 *
 * @author markiewb
 */
public class RemoveFinalModifierFromParameterTest extends ErrorHintsTestBase {

    public RemoveFinalModifierFromParameterTest(String testName) {
	super(testName);
    }

    public void testRemoveFinalFromParameter() throws Exception {
	performFixTest("test/Test.java",
		"package test;"
		+ "public class Test {"
		+ "    public static void main1(final int parameter) {"
		+ "	parameter = 2;"
		+ "    }"
		+ "}",
		-1,
		Bundle.FIX_RemoveFinalModifierFromParameter("parameter"),
		("package test;"
		+ "public class Test {"
		+ "	public static void main1(int parameter) {"
		+ "		parameter = 2;"
		+ "	}"
		+ "}").replaceAll("[\\s]+", " "));
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
	return AddOrRemoveFinalModifier.createRemoveFinalFromParameter().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
	return f.getText();
    }

    @Override
    protected Set<String> getSupportedErrorKeys() {
	return AddOrRemoveFinalModifier.createRemoveFinalFromParameter().getCodes();
    }
}
