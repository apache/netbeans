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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.modules.java.source.transform.Transformer;

/**
 * Test name change.
 * 
 * @author Pavel Flaska
 */
public class Field5Test extends GeneratorTestBase {
    
    /**
     * Creates a new instance of Field5Test
     */
    public Field5Test(String testName) {
        super(testName);
    }
    
    public void testChangeParName() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package yerba.mate;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void hierbasDelLitoral(Test[] arrFile) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package yerba.mate;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test2 {\n" +
            "    public void hierbasDelLitoral(Test2[] arrFile) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        System.err.println("visitClass");
                        copy.rewrite(node, make.setLabel(node, "Test2"));
                    }
                    return null;
                }
                
                public Void visitIdentifier(IdentifierTree node, Object p) {
                    super.visitIdentifier(node, p);
                    if ("Test".contentEquals(node.getName())) {
                        System.err.println("visitIdentifier");
                        copy.rewrite(node, make.setLabel(node, "Test2"));
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "Test.java");
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
    
}
