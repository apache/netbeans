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
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class AccessErrorTest extends ErrorHintsTestBase  {
    
    public AccessErrorTest(String name) {
        super(name);
    }
    
    public void testSimple() throws Exception {
        doRunIndexing = true;
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        Acc.i = 0;\n" +
                       "    }\n" +
                       "}\n" +
                       "class Acc {\n" +
                       "    private static int i;\n" +
                       "}\n",
                       -1,
                       "FIX_AccessError_PACKAGE_PRIVATE:i",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    {\n" +
                        "        Acc.i = 0;\n" +
                        "    }\n" +
                        "}\n" +
                        "class Acc {\n" +
                        "    static int i;\n" +
                        "}\n").replaceAll("[\\s]+", " "));
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new AccessError().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

    @Override
    protected Set<String> getSupportedErrorKeys() {
        return new AccessError().getCodes();
    }

    static {
        NbBundle.setBranding("test");
    }
}
