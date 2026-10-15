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

package org.netbeans.modules.java.completion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.Callable;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.swing.text.Document;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.EntityCatalog;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Dusan Balek, Jan Lahoda
 */
public class JavaDocumentationTaskTest extends CompletionTestBaseBase {

    public JavaDocumentationTaskTest(String testName) {
        super(testName, "org/netbeans/modules/java/completion/JavaDocumentationTaskTest");
    }

    public void testConstructor() throws Exception {
        performTest("import java.util.*;\n" +
                    "public class Test {\n" +
                    "    List<String> l = new Array|List();\n" +
                    "}\n",
                    "",
                    "11",
                    "[java.util.ArrayList, <init>, ()V]");
    }

    public void testConstructorDiamond() throws Exception {
        performTest("import java.util.*;\n" +
                    "public class Test {\n" +
                    "    List<String> l = new Array|List<>();\n" +
                    "}\n",
                    "",
                    "11",
                    "[java.util.ArrayList, <init>, ()V]");
    }

    public void testConstructorTypeParams1() throws Exception {
        performTest("import java.util.*;\n"+
                    "public class Test {\n"+
                    "   List<String> l = new Array|List<String>();\n"+
                    "}\n",
                    "",
                    "11",
                    "[java.util.ArrayList, <init>, ()V]");
    }

    public void testConstructorTypeParams2() throws Exception {
        performTest("import java.util.*;\n"+
                    "public class Test {\n"+
                    "    List<String> l = new ArrayList<Str|ing>();\n"+
                    "}\n",
                    "",
                    "11",
                    "[java.lang.String]");
    }

    public void testConstructorAnnotation1() throws Exception {
        performTest("import java.lang.annotation.ElementType;\n"+
                    "import java.lang.annotation.Target;\n"+
                    "import java.util.*;\n"+
                    "public class Test {\n"+
                    "    List<String> l = new @Ann Array|List();\n"+
                    "}\n"+
                    "@Target(ElementType.TYPE_USE)\n"+
                    "@interface Ann {}\n",
                    "",
                    "11",
                    "[java.util.ArrayList, <init>, ()V]");
    }

    public void testConstructorAnnotation2() throws Exception {
        performTest("import java.lang.annotation.ElementType;\n"+
                    "import java.lang.annotation.Target;\n"+
                    "import java.util.*;\n"+
                    "public class Test {\n"+
                    "    List<String> l = new @An|n ArrayList();\n"+
                    "}\n"+
                    "@Target(ElementType.TYPE_USE)\n"+
                    "@interface Ann {}\n",
                    "",
                    "11",
                    "[Ann]");
    }

    public void testConstructorAnnotationTypeParams1() throws Exception {
        performTest("import java.lang.annotation.ElementType;\n"+
                    "import java.lang.annotation.Target;\n"+
                    "import java.util.*;\n"+
                    "public class Test {\n"+
                    "    List<String> l = new @Ann Array|List<String>();\n"+
                    "}\n"+
                    "@Target(ElementType.TYPE_USE)\n"+
                    "@interface Ann {}\n",
                    "",
                    "11",
                    "[java.util.ArrayList, <init>, ()V]");
    }

    public void testConstructorAnnotationTypeParams2() throws Exception {
        performTest("import java.lang.annotation.ElementType;\n"+
                    "import java.lang.annotation.Target;\n"+
                    "import java.util.*;\n"+
                    "public class Test {\n"+
                    "    List<String> l = new @An|n ArrayList<String>();\n"+
                    "}\n"+
                    "@Target(ElementType.TYPE_USE)\n"+
                    "@interface Ann {}\n",
                    "",
                    "11",
                    "[Ann]");
    }

    public void testConstructorAnnotationTypeParams3() throws Exception {
        performTest("import java.lang.annotation.ElementType;\n"+
                    "import java.lang.annotation.Target;\n"+
                    "import java.util.*;\n"+
                    "public class Test {\n"+
                    "    List<String> l = new @Ann ArrayList<Str|ing>();\n"+
                    "}\n"+
                    "@Target(ElementType.TYPE_USE)\n"+
                    "@interface Ann {}\n",
                    "",
                    "11",
                    "[java.lang.String]");
    }
    
    public void testConstructorIntegerArgument() throws Exception {
        performTest("public class Test {\n" +
                      "/**\n"+
                      "This is constructor level Javadoc\n"+
                      "**/\n"+
                      "Test(int i){}\n"+ 
                      "    public static void main(String[] args) {\n"+
                      "        Test t = new Test(|10000);\n" +
                      "    }\n" +
                      "}\n",
                    "",
                    "11",
                    null);
    }
    
    public void testConstructorCharacterArgument() throws Exception {
        performTest("public class Test {\n" +
                      "/**\n"+
                      "This is constructor level Javadoc\n"+
                      "**/\n"+
                      "Test(char c){}\n"+ 
                      "    public static void main(String[] args) {\n"+
                      "        Test t = new Test(|'x');\n" +
                      "    }\n" +
                      "}\n",
                    "",
                    "11",
                    null);
    }

    protected void performTest(String source, String textToInsert, String sourceLevel, String expected) throws Exception {
        this.sourceLevel.set(sourceLevel);
        int caretPos = source.indexOf("|");
        assertTrue(caretPos != (-1));
        String code = source.substring(0, caretPos) + source.substring(caretPos + 1);
        File testSource = new File(getWorkDir(), "test/Test.java");
        testSource.getParentFile().mkdirs();
        try (Writer w = new FileWriter(testSource)) {
            w.write(code);
        }
        FileObject testSourceFO = FileUtil.toFileObject(testSource);
        assertNotNull(testSourceFO);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        assertNotNull(testSourceDO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        assertNotNull(ec);
        final Document doc = ec.openDocument();
        assertNotNull(doc);
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");
        int textToInsertLength = textToInsert != null ? textToInsert.length() : 0;
        if (textToInsertLength > 0) {
            doc.insertString(caretPos, textToInsert, null);
        }
        Source s = Source.create(doc);
        JavaDocumentationTask<String> task = JavaDocumentationTask.create(caretPos + textToInsertLength, null, new StringFactory(), null);
        ParserManager.parse(Collections.singletonList(s), task);
        String documentation = task.getDocumentation();

        assertEquals(expected, documentation);

        LifecycleManager.getDefault().saveAll();
    }

    private static class StringFactory implements JavaDocumentationTask.DocumentationFactory<String> {

        @Override
        public String create(CompilationInfo compilationInfo, Element element, Callable<Boolean> cancel) {
            return Arrays.toString(SourceUtils.getJVMSignature(ElementHandle.create(element)));
        }

    }

}
