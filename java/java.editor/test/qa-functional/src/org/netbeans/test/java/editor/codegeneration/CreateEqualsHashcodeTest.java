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
package org.netbeans.test.java.editor.codegeneration;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.editor.jelly.GenerateEqualsAndHashCodeOperator;
import org.netbeans.test.java.editor.jelly.GenerateEqualsOperator;

/**
 *
 * @author Jiri Prox
 */
public class CreateEqualsHashcodeTest extends GenerateCodeTestCase {

    public CreateEqualsHashcodeTest(String testMethodName) {
        super(testMethodName);
    }

    public void testEqualsOnly() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "testEqualsHashcode");
        editor = new EditorOperator("testEqualsHashcode");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(14, 5);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_EQUALS, editor);
            GenerateEqualsOperator geo = new GenerateEqualsOperator();
            JTreeOperator jto = geo.equalsTreeOperator();
            jto.selectRow(0);
            jto.selectRow(2);
            geo.generate();
            String expected = "" +
                    "    @Override\n" +
                    "    public boolean equals(Object obj) {\n" +
                    "        if (obj == null) {\n" +
                    "            return false;\n" +
                    "        }\n" +
                    "        if (getClass() != obj.getClass()) {\n" +
                    "            return false;\n" +
                    "        }\n" +
                    "        final testEqualsHashcode other = (testEqualsHashcode) obj;\n" +
                    "        if (!Objects.equals(this.a, other.a)) {\n"+
                    "            return false;\n"+
                    "        }\n"+
                    "        if (!Objects.equals(this.c, other.c)) {\n"+
                    "            return false;\n"+
                    "        }\n"+                    
                    "        return true;\n" +
                    "    }\n" +
                    "\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }
    }

    public static void main(String[] args) {
        TestRunner.run(CreateEqualsHashcodeTest.class);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CreateEqualsHashcodeTest.class).enableModules(".*").clusters(".*"));
    }
}
