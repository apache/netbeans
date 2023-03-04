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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author lahvac
 */
public class NewImportClassTest extends ErrorHintsTestBase {

    public NewImportClassTest(String name) {
        super(name);
        doRunIndexing = true;
    }
    
    public void testImportHint200742a() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.Atomi|cBoolean;\n" +
                       "public class Test { AtomicBoolean b; }\n",
                       Bundle.Change_to_import_X(AtomicBoolean.class.getName(), ""),
                       ("package test;\n" +
                        "import java.util.concurrent.atomic.AtomicBoolean;\n" +
                        "public class Test { AtomicBoolean b; }\n").replaceAll("[ \t\n]+", " "));
    }

    public void testImportHint200742b() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.lang.Map;\n" +
                       "import java.lang.Ma|p.Entry;\n" +
                       "public class Test { Map b; }\n",
                       Bundle.Change_to_import_X(Map.class.getName() + ".Entry", ""),
                       ("package test;\n" +
                        "import java.lang.Map;\n" +
                        "import java.util.Map.Entry;\n" +
                        "public class Test { Map b; }\n").replaceAll("[ \t\n]+", " "));
    }
    
    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new ImportClass().run(info, null, pos, path, new Data<Void>());
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }
    
}
