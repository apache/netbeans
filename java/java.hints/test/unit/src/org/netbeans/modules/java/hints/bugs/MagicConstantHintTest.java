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
package org.netbeans.modules.java.hints.bugs;

import java.io.IOException;
import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.netbeans.modules.java.source.annotations.AugmentedAnnotations;

public class MagicConstantHintTest {

    @Test
    public void testFlagsFromClass() throws Exception {
        writeAnnotations("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                         "<root>\n" +
                         "<item name=\"test.Test void test(int) 0\">" +
                         "<annotation name=\"org.intellij.lang.annotations.MagicConstant\">" +
                         "<val name=\"flagsFromClass\" val=\"java.lang.reflect.Modifier.class\" /> " +
                         "</annotation>\n" +
                         "</item>\n" +
                         "</root>\n");

        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int i) {\n" +
                       "        test(java.awt.event.InputEvent.META_MASK);\n" +
                       "    }\n" +
                       "}\n")
                .run(MagicConstantHint.class)
                .assertWarnings("3:13-3:48:verifier:" + Bundle.ERR_NotAValidValue());

        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int i) {\n" +
                       "        test(java.lang.reflect.Modifier.PUBLIC);\n" +
                       "    }\n" +
                       "}\n")
                .run(MagicConstantHint.class)
                .assertWarnings();
    }

    @Test
    public void testIntValues() throws Exception {
        writeAnnotations("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                         "<root>\n" +
                         "<item name=\"test.Test void test(int) 0\">" +
                         "<annotation name=\"org.intellij.lang.annotations.MagicConstant\">" +
                         "<val name=\"intValues\" val=\"{java.lang.reflect.Modifier.PUBLIC}\" /> " +
                         "</annotation>\n" +
                         "</item>\n" +
                         "</root>\n");

        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int i) {\n" +
                       "        test(java.lang.reflect.Modifier.PRIVATE);\n" +
                       "    }\n" +
                       "}\n")
                .run(MagicConstantHint.class)
                .assertWarnings("3:13-3:47:verifier:" + Bundle.ERR_NotAValidValue());

        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int i) {\n" +
                       "        test(java.lang.reflect.Modifier.PUBLIC);\n" +
                       "    }\n" +
                       "}\n")
                .run(MagicConstantHint.class)
                .assertWarnings();
    }

    @Test
    public void testConstructorAndValuesFromClass() throws Exception {
        writeAnnotations("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                         "<root>\n" +
                         "<item name=\"test.Test Test(int) 0\">" +
                         "<annotation name=\"org.intellij.lang.annotations.MagicConstant\">" +
                         "<val name=\"valuesFromClass\" val=\"test.Values.class\" /> " +
                         "</annotation>\n" +
                         "</item>\n" +
                         "</root>\n");

        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static final int V = 1;\n" +
                       "    public Test(int i) {\n" +
                       "        new Test(V);\n" +
                       "    }\n" +
                       "}\n" +
                       "class Values {\n" +
                       "    public static final int V = 0;\n" +
                       "}\n")
                .run(MagicConstantHint.class)
                .assertWarnings("4:17-4:18:verifier:" + Bundle.ERR_NotAValidValue());

        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test(int i) {\n" +
                       "        new Test(Values.V);\n" +
                       "    }\n" +
                       "}\n" +
                       "class Values {\n" +
                       "    public static final int V = 0;\n" +
                       "}\n")
                .run(MagicConstantHint.class)
                .assertWarnings();
    }

    private static void writeAnnotations(String content) throws IOException {
        AugmentedAnnotations.setAugmentedAnnotationsForTests(content);
    }

}
