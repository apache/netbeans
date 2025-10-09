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

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;

/**
 *
 * @author lahvac
 */
public class NewImportClassTest extends ErrorHintsTestBase {

    public NewImportClassTest(String name) {
        super(name, ImportClass.class);
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
    
}
