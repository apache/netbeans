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
package org.netbeans.modules.editor.codegen;

import javax.swing.text.Document;
import org.netbeans.modules.editor.*;
import java.net.URL;
import javax.swing.text.DefaultStyledDocument;
import junit.framework.TestCase;

/**
 *
 * @author Dusan Balek
 */
public class CodeGenerationTest extends TestCase {

    public CodeGenerationTest(String testName) {
        super(testName);
    }

    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    protected void setUp() throws Exception {
        EditorTestLookup.setLookup(
                new URL[]{EditorTestConstants.EDITOR_LAYER_URL,
                    getClass().getClassLoader().getResource("org/netbeans/modules/editor/resources/codegen-test-layer.xml")
                },
                new Object[]{},
                getClass().getClassLoader());
    }

    public void testSimpleCodeGenerator() {
        Document doc = new DefaultStyledDocument();
        doc.putProperty(NbEditorDocument.MIME_TYPE_PROP, "text/x-simple-codegen-test");
        String[] generatorNames = NbGenerateCodeAction.test(doc, 0);
        assertEquals(generatorNames.length, 1);
        assertEquals(generatorNames[0], "SimpleCodeGenerator");
    }

    public void testCodeGenerator() {
        Document doc = new DefaultStyledDocument();
        doc.putProperty(NbEditorDocument.MIME_TYPE_PROP, "text/x-codegen-test");
        String[] generatorNames = NbGenerateCodeAction.test(doc, 0);
        assertEquals(generatorNames.length, 1);
        assertEquals(generatorNames[0], "CodeGenerator");
    }
}
