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
package org.netbeans.modules.java.source.save;

import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.indent.api.Indent;

/**
 * @author Dusan Balek, Jan Lahoda
 */
public class ReindenterTest extends NbTestCase {

    public ReindenterTest(String name) {
        super(name);
    }
    private Document doc;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        doc = new PlainDocument();
        doc.putProperty("mimeType", "text/x-java");
        doc.putProperty(Language.class, JavaTokenId.language());
    }

    public void testNewLineIndentationAtFileBeginning() throws Exception {
        performNewLineIndentationTest("|package t;\npublic class T {\n}\n",
                "\npackage t;\npublic class T {\n}\n");
    }

    public void testLineIndentationAtFileBeginning() throws Exception {
        performLineIndentationTest("| package t;\npublic class T {\n}\n",
                "package t;\npublic class T {\n}\n");
    }

    public void testNewLineIndentationBeforeClass() throws Exception {
        performNewLineIndentationTest("package t;\n|public class T {\n}\n",
                "package t;\n\npublic class T {\n}\n");
    }

    public void testLineIndentationBeforeClass() throws Exception {
        performLineIndentationTest("package t;\n| public class T {\n}\n",
                "package t;\npublic class T {\n}\n");
    }

    public void testNewLineIndentationBeforeClassBody() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T|{\n}\n",
                "package t;\npublic class T\n{\n}\n");
    }

    public void testLineIndentationBeforeClassBody() throws Exception {
        performLineIndentationTest("package t;\npublic class T\n| {\n}\n",
                "package t;\npublic class T\n{\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedClassBody() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T|{\n}\n",
                    "package t;\npublic class T\n  {\n}\n");
        } finally {
            preferences.remove("classDeclBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedClassBody() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T\n|{\n}\n",
                    "package t;\npublic class T\n  {\n}\n");
        } finally {
            preferences.remove("classDeclBracePlacement");
        }
    }

    public void testNewLineIndentationInsideClass() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {|\n}\n",
                "package t;\npublic class T {\n    \n}\n");
    }

    public void testLineIndentationInsideClass() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n|\n}\n",
                "package t;\npublic class T {\n    \n}\n");
    }

    public void testNewLineIndentationBeforeEmptyClassEnd() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {|}\n",
                "package t;\npublic class T {\n}\n");
    }

    public void testLineIndentationBeforeEmptyClassEnd() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n|}\n",
                "package t;\npublic class T {\n}\n");
    }

    public void testNewLineIndentationBeforeEmptyHalfIndentedClassEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T\n  {|}\n",
                    "package t;\npublic class T\n  {\n  }\n");
        } finally {
            preferences.remove("classDeclBracePlacement");
        }
    }

    public void testLineIndentationBeforeEmptyHalfIndentedClassEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T\n  {\n|    }\n",
                    "package t;\npublic class T\n  {\n  }\n");
        } finally {
            preferences.remove("classDeclBracePlacement");
        }
    }

    public void testNewLineIndentationInsideClassImplementsList() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T implements|{\n}\n",
                "package t;\npublic class T implements\n        {\n}\n");
    }

    public void testLineIndentationInsideClassImplementsList() throws Exception {
        performLineIndentationTest("package t;\npublic class T implements\n|{\n}\n",
                "package t;\npublic class T implements\n        {\n}\n");
    }

    public void testNewLineIndentationAfterAlignedClassImplementsList() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T implements Cloneable,\n                         Runnable,|{\n}\n",
                "package t;\npublic class T implements Cloneable,\n                         Runnable,\n                         {\n}\n");
    }

    public void testLineIndentationAfterAlignedClassImplementsList() throws Exception {
        performLineIndentationTest("package t;\npublic class T implements Cloneable,\n                         Runnable,\n|{\n}\n",
                "package t;\npublic class T implements Cloneable,\n                         Runnable,\n                         {\n}\n");
    }

    public void testNewLineIndentationInsideEnum() throws Exception {
        performNewLineIndentationTest("package t;\npublic enum T {\n    ONE,|\n}\n",
                "package t;\npublic enum T {\n    ONE,\n    \n}\n");
    }

    public void testLineIndentationInsideEnum() throws Exception {
        performLineIndentationTest("package t;\npublic enum T {\n    ONE,\n|\n}\n",
                "package t;\npublic enum T {\n    ONE,\n    \n}\n");
    }

    public void testNewLineIndentationBeforeEnumEnd() throws Exception {
        performNewLineIndentationTest("package t;\npublic enum T {\n    ONE, TWO|}\n",
                "package t;\npublic enum T {\n    ONE, TWO\n}\n");
    }

    public void testLineIndentationBeforeEnumEnd() throws Exception {
        performLineIndentationTest("package t;\npublic enum T {\n    ONE, TWO\n|}\n",
                "package t;\npublic enum T {\n    ONE, TWO\n}\n");
    }

    public void testNewLineIndentationInsideAnnotation() throws Exception {
        performNewLineIndentationTest("package t;\n@SuppressWarning(\"unchecked\"|)\npublic class T {\n}\n",
                "package t;\n@SuppressWarning(\"unchecked\"\n)\npublic class T {\n}\n");
    }

    public void testLineIndentationInsideAnnotation() throws Exception {
        performLineIndentationTest("package t;\n@SuppressWarning(\"unchecked\"\n|)\npublic class T {\n}\n",
                "package t;\n@SuppressWarning(\"unchecked\"\n)\npublic class T {\n}\n");
    }

    public void testNewLineIndentationAfterAlignedAnnotationArg() throws Exception {
        performNewLineIndentationTest("package t;\n@SuppressWarning(\"unchecked\",\n                 \"deprecation\",|)\npublic class T {\n}\n",
                "package t;\n@SuppressWarning(\"unchecked\",\n                 \"deprecation\",\n                 )\npublic class T {\n}\n");
    }

    public void testLineIndentationAfterAlignedAnnotationArg() throws Exception {
        performLineIndentationTest("package t;\n@SuppressWarning(\"unchecked\",\n                 \"deprecation\",\n|)\npublic class T {\n}\n",
                "package t;\n@SuppressWarning(\"unchecked\",\n                 \"deprecation\",\n                 )\npublic class T {\n}\n");
    }

    public void testNewLineIndentationAfterAnnotation() throws Exception {
        performNewLineIndentationTest("package t;\n@SuppressWarning(\"unchecked\")|public class T {\n}\n",
                "package t;\n@SuppressWarning(\"unchecked\")\npublic class T {\n}\n");
    }

    public void testLineIndentationAfterAnnotation() throws Exception {
        performLineIndentationTest("package t;\n@SuppressWarning(\"unchecked\")\n|public class T {\n}\n",
                "package t;\n@SuppressWarning(\"unchecked\")\npublic class T {\n}\n");
    }

    public void testNewLineIndentationAfterSimpleAnnotation() throws Exception {
        performNewLineIndentationTest("package t;\n@Deprecated|public class T {\n}\n",
                "package t;\n@Deprecated\npublic class T {\n}\n");
    }

    public void testLineIndentationAfterSimpleAnnotation() throws Exception {
        performLineIndentationTest("package t;\n@Deprecated\n|public class T {\n}\n",
                "package t;\n@Deprecated\npublic class T {\n}\n");
    }

    public void testNewLineIndentationAfterSimpleAnnotationInsideEmptyClass() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    @Deprecated|\n}\n",
                "package t;\npublic class T {\n    @Deprecated\n    \n}\n");
    }

    public void testLineIndentationAfterSimpleAnnotationInsideEmptyClass() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    @Deprecated\n|\n}\n",
                "package t;\npublic class T {\n    @Deprecated\n    \n}\n");
    }

    public void testNewLineIndentationAfterField() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public int i;|\n}\n",
                "package t;\npublic class T {\n    public int i;\n    \n}\n");
    }

    public void testLineIndentationAfterField() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public int i;\n|\n}\n",
                "package t;\npublic class T {\n    public int i;\n    \n}\n");
    }

    public void testNewLineIndentationAfterMethod() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n    }|\n}\n",
                "package t;\npublic class T {\n    public void op() {\n    }\n    \n}\n");
    }

    public void testLineIndentationAfterMethod() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n    }\n|\n}\n",
                "package t;\npublic class T {\n    public void op() {\n    }\n    \n}\n");
    }

    public void testNewLineIndentationAfterMethodArg() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op(int i,|) {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op(int i,\n            ) {\n    }\n}\n");
    }

    public void testLineIndentationAfterMethodArg() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op(int i,\n|) {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op(int i,\n            ) {\n    }\n}\n");
    }

    public void testNewLineIndentationAfterAlignedMethodArg() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op(int i,\n                   int j,|) {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op(int i,\n                   int j,\n                   ) {\n    }\n}\n");
    }

    public void testLineIndentationAfterAlignedMethodArg() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op(int i,\n                   int j,\n|) {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op(int i,\n                   int j,\n                   ) {\n    }\n}\n");
    }

    public void testNewLineIndentationAfterMethodThows() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() throws| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() throws\n            {\n    }\n}\n");
    }

    public void testLineIndentationAfterMethodThrows() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() throws\n| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() throws\n            {\n    }\n}\n");
    }

    public void testNewLineIndentationAfterAlignedMethodThrows() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() throws IllegalStateException,\n                            IllegalArgumentException,| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() throws IllegalStateException,\n                            IllegalArgumentException,\n                            {\n    }\n}\n");
    }

    public void testLineIndentationAfterAlignedMethodThrows() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() throws IllegalStateException,\n                            IllegalArgumentException,\n| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() throws IllegalStateException,\n                            IllegalArgumentException,\n                            {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeMethodBody() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op()| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op()\n    {\n    }\n}\n");
    }

    public void testLineIndentationBeforeMethodBody() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op()\n| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op()\n    {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedMethodBody() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op()| {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op()\n      {\n    }\n}\n");
        } finally {
            preferences.remove("methodDeclBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedMethodBody() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op()\n|{\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op()\n      {\n    }\n}\n");
        } finally {
            preferences.remove("methodDeclBracePlacement");
        }
    }

    public void testNewLineIndentationInsideEmptyMethodBody() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        \n    }\n}\n");
    }

    public void testLineIndentationIndideEmptyMethodBody() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        \n    }\n}\n");
    }

    public void testNewLineIndentationInsideEmptyMethodBodyBeforeEnd() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {|}\n}\n",
                "package t;\npublic class T {\n    public void op() {\n    }\n}\n");
    }

    public void testLineIndentationIndideEmptyMethodBodyBeforeEnd() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n|        }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n    }\n}\n");
    }

    public void testNewLineIndentationInsideEmptyMethodBodyWithMultilineMethodHeader() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op(\n            int i) {|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op(\n            int i) {\n        \n    }\n}\n");
    }

    public void testLineIndentationIndideEmptyMethodBodyWithMultilineMethodHeader() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op(\n            int i) {\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op(\n            int i) {\n        \n    }\n}\n");
    }

    public void testNewLineIndentationInsideEmptyMethodBodyWithMultilineMethodHeaderBeforeEnd() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op(\n            int i) {|}\n}\n",
                "package t;\npublic class T {\n    public void op(\n            int i) {\n    }\n}\n");
    }

    public void testLineIndentationIndideEmptyMethodBodyWithMultilineMethodHeaderBeforeEnd() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op(\n            int i) {\n|        }\n}\n",
                "package t;\npublic class T {\n    public void op(\n            int i) {\n    }\n}\n");
    }

    public void testNewLineIndentationInsideEmptyHalfIndentedMethodBody() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op()\n      {|\n      }\n}\n",
                    "package t;\npublic class T {\n    public void op()\n      {\n        \n      }\n}\n");
        } finally {
            preferences.remove("methodDeclBracePlacement");
        }
    }

    public void testLineIndentationInsideEmptyHalfIndentedMethodBody() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op()\n      {\n|\n      }\n}\n",
                    "package t;\npublic class T {\n    public void op()\n      {\n        \n      }\n}\n");
        } finally {
            preferences.remove("methodDeclBracePlacement");
        }
    }

    public void testNewLineIndentationInsideMethodBody() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int i;|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int i;\n        \n    }\n}\n");
    }

    public void testLineIndentationIndideMethodBody() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int i;\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int i;\n        \n    }\n}\n");
    }

    public void testNewLineIndentationInsideMethodBodyBeforeStmt() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int i;| System.out.println();\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int i;\n        System.out.println();\n    }\n}\n");
    }

    public void testLineIndentationIndideMethodBodyBeforeStmt() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int i;\n|System.out.println();\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int i;\n        System.out.println();\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeEmptyMethodBodyEnd() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {|}\n}\n",
                "package t;\npublic class T {\n    public void op() {\n    }\n}\n");
    }

    public void testLineIndentationBeforeEmptyMethodBodyEnd() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n|}\n}\n",
                "package t;\npublic class T {\n    public void op() {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeEmptyHalfIndentedMethodBodyEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op()\n      {|}\n}\n",
                    "package t;\npublic class T {\n    public void op()\n      {\n      }\n}\n");
        } finally {
            preferences.remove("methodDeclBracePlacement");
        }
    }

    public void testLineIndentationBeforeEmptyHalfIndentedMethodBodyEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op()\n      {\n|        }\n}\n",
                    "package t;\npublic class T {\n    public void op()\n      {\n      }\n}\n");
        } finally {
            preferences.remove("methodDeclBracePlacement");
        }
    }

    public void testNewLineIndentationBeforeMethodBodyEnd() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        System.out.println();|}\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        System.out.println();\n    }\n}\n");
    }

    public void testLineIndentationBeforeMethodBodyEnd() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        System.out.println();\n|}\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        System.out.println();\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeStaticBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    static| {\n    }\n}\n",
                "package t;\npublic class T {\n    static\n    {\n    }\n}\n");
    }

    public void testLineIndentationBeforeStaticBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    static\n| {\n    }\n}\n",
                "package t;\npublic class T {\n    static\n    {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedStaticBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    static| {\n    }\n}\n",
                    "package t;\npublic class T {\n    static\n      {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedStaticBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    static\n|{\n    }\n}\n",
                    "package t;\npublic class T {\n    static\n      {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationInsideEmptyStaticBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    static {|\n    }\n}\n",
                "package t;\npublic class T {\n    static {\n        \n    }\n}\n");
    }

    public void testLineIndentationIndideEmptyStaticBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    static {\n|\n    }\n}\n",
                "package t;\npublic class T {\n    static {\n        \n    }\n}\n");
    }

    public void testNewLineIndentationInsideEmptyHalfIndentedStaticBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    static\n      {|\n      }\n}\n",
                    "package t;\npublic class T {\n    static\n      {\n        \n      }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationInsideEmptyHalfIndentedStaticBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    static\n      {\n|\n      }\n}\n",
                    "package t;\npublic class T {\n    static\n      {\n        \n      }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationInsideEmptyStaticBlockBeforeEnd() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    static {|}\n}\n",
                "package t;\npublic class T {\n    static {\n    }\n}\n");
    }

    public void testLineIndentationIndideEmptyStaticBlockBeforeEnd() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    static {\n|        }\n}\n",
                "package t;\npublic class T {\n    static {\n    }\n}\n");
    }

    public void testNewLineIndentationInsideEmptyHalfIndentedStaticBlockBeforeEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    static\n      {|}\n}\n",
                    "package t;\npublic class T {\n    static\n      {\n      }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationIndideEmptyHalfIndentedStaticBlockBeforeEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    static\n      {\n|}\n}\n",
                    "package t;\npublic class T {\n    static\n      {\n      }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationInsideBlockStatement() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        System.out.|println();\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        System.out.\n                println();\n    }\n}\n");
    }

    public void testLineIndentationInsideBlockStatement() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        System.out.\n|println();\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        System.out.\n                println();\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeEmptyBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {|{\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        {\n    }\n}\n");
    }

    public void testLineIndentationBeforeEmptyBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n|{\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        {\n    }\n}\n");
    }

    public void testNewLineIndentationInsideEmptyBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        {|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        {\n            \n    }\n}\n");
    }

    public void testLineIndentationInsideEmptyBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        {\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        {\n            \n    }\n}\n");
    }

    public void testNewLineIndentationBeforeEmptyBlockEnd() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        {|}\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        {\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeEmptyBlockEnd() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        {\n|            }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeEmptyHalfIndentedBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {|{\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeEmptyHalfIndentedBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n|{\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationBeforeEmptyHalfIndentedBlockEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n          {|}\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n          {\n          }\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeEmptyHalfIndentedBlockEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n          {\n|            }\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n          {\n          }\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationBeforeBlockEnd() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        {\n            System.out.println();|}\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        {\n            System.out.println();\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeBlockEnd() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        {\n            System.out.println();\n|            }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        {\n            System.out.println();\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeDoWhileStmt() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        do|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        do\n            \n    }\n}\n");
    }

    public void testLineIndentationBeforeDoWhileStmt() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        do\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        do\n            \n    }\n}\n");
    }

    public void testNewLineIndentationBeforeDoWhileBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        do| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        do\n        {\n    }\n}\n");
    }

    public void testLineIndentationBeforeDoWhileBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        do\n|            {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        do\n        {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedDoWhileBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        do| {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        do\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedDoWhileBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        do\n|            {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        do\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationBeforeWhileInDoWhile() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        do {\n        }| while (check());\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        do {\n        }\n        while (check());\n    }\n}\n");
    }

    public void testLineIndentationBeforeWhileInDoWhile() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        do {\n        }\n| while (check());\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        do {\n        }\n        while (check());\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeWhileOnNewLineInDoWhile() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        do {\n        }|\n        while (check());\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        do {\n        }\n        \n        while (check());\n    }\n}\n");
    }

    public void testLineIndentationBeforeWhileOnNewLineInDoWhile() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        do {\n        }\n|\n        while (check());\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        do {\n        }\n        \n        while (check());\n    }\n}\n");
    }

    public void testNewLineIndentationInsideDoWhileCond() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        do {\n        } while (check()|);\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        do {\n        } while (check()\n                );\n    }\n}\n");
    }

    public void testLineIndentationInsideDoWhileCond() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        do {\n        } while (check()\n|);\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        do {\n        } while (check()\n                );\n    }\n}\n");
    }

    public void testNewLineIndentationAfterForVar() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (int i = 0;| i < 10; update(i)) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (int i = 0;\n                i < 10; update(i)) {\n        }\n    }\n}\n");
    }

    public void testLineIndentationAfterForVar() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (int i = 0;\n|i < 10; update(i)) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (int i = 0;\n                i < 10; update(i)) {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationAfterAlignedForCond() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (int i = 0;\n             i < 10;| update(i)) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (int i = 0;\n             i < 10;\n             update(i)) {\n        }\n    }\n}\n");
    }

    public void testLineIndentationAfterAlignedForCond() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (int i = 0;\n             i < 10;\n| update(i)) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (int i = 0;\n             i < 10;\n             update(i)) {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationInsideForUpdate() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (int i = 0; i < 10; update(i)|) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (int i = 0; i < 10; update(i)\n                ) {\n        }\n    }\n}\n");
    }

    public void testLineIndentationInsideForUpdate() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (int i = 0; i < 10; update(i)\n|) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (int i = 0; i < 10; update(i)\n                ) {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeForStmt() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (;;)|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (;;)\n            \n    }\n}\n");
    }

    public void testLineIndentationBeforeForStmt() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (;;)\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (;;)\n            \n    }\n}\n");
    }

    public void testNewLineIndentationBeforeForBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (;;)| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (;;)\n        {\n    }\n}\n");
    }

    public void testLineIndentationBeforeForBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (;;)\n|            {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (;;)\n        {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedForBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (;;)| {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        for (;;)\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedForBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (;;)\n|            {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        for (;;)\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationInsideForEachExpr() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings()|) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings()\n                ) {\n        }\n    }\n}\n");
    }

    public void testLineIndentationInsideForEachExpr() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings()\n|) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings()\n                ) {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeForEachStmt() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings())|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings())\n            \n    }\n}\n");
    }

    public void testLineIndentationBeforeForEachStmt() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings())\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings())\n            \n    }\n}\n");
    }

    public void testNewLineIndentationBeforeForEachBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings())| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings())\n        {\n    }\n}\n");
    }

    public void testLineIndentationBeforeForEachBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings())\n|            {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings())\n        {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedForEachBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings())| {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings())\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedForEachBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings())\n|            {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        for (String s : getStrings())\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationInsideIfCond() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (check()|)\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (check()\n                )\n    }\n}\n");
    }

    public void testLineIndentationInsideIfCond() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (check()\n|)\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (check()\n                )\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeIfStmt() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true)|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true)\n            \n    }\n}\n");
    }

    public void testLineIndentationBeforeIfStmt() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true)\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true)\n            \n    }\n}\n");
    }

    public void testNewLineIndentationBeforeIfBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true)| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true)\n        {\n    }\n}\n");
    }

    public void testLineIndentationBeforeIfBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true)\n|            {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true)\n        {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedIfBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true)| {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        if (true)\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedIfBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true)\n|            {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        if (true)\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationBeforeElse() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true) {\n        }| else {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true) {\n        }\n        else {\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeElse() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true) {\n        }\n|else {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true) {\n        }\n        else {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeElseOnNewLine() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true) {\n        }|\n        else {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true) {\n        }\n        \n        else {\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeElseOnNewLine() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true) {\n        }\n|\n        else {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true) {\n        }\n        \n        else {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationAfterStatementBeforeElseOnNewLine() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true)\n            System.out.println();|\n        else {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true)\n            System.out.println();\n        \n        else {\n        }\n    }\n}\n");
    }

    public void testLineIndentationAfterStatementBeforeElseOnNewLine() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true)\n            System.out.println();\n|\n        else {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true)\n            System.out.println();\n        \n        else {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeElseStmt() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true) {\n        } else|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true) {\n        } else\n            \n    }\n}\n");
    }

    public void testLineIndentationBeforeElseStmt() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true) {\n        } else\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true) {\n        } else\n            \n    }\n}\n");
    }

    public void testNewLineIndentationBeforeElseBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true) {\n        } else| {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true) {\n        } else\n        {\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeElseBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true) {\n        } else\n|            {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        if (true) {\n        } else\n        {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedElseBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true) {\n        } else| {\n        }\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        if (true) {\n        } else\n          {\n        }\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedElseBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        if (true) {\n        } else\n|            {\n        }\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        if (true) {\n        } else\n          {\n        }\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationInsideSynchronizedExpr() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        synchronized (check()|)\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        synchronized (check()\n                )\n    }\n}\n");
    }

    public void testLineIndentationInsideSynchronizedExpr() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        synchronized (check()\n|)\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        synchronized (check()\n                )\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeSynchronizedStmt() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        synchronized (check())|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        synchronized (check())\n            \n    }\n}\n");
    }

    public void testLineIndentationBeforeSynchronizedStmt() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        synchronized (check())\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        synchronized (check())\n            \n    }\n}\n");
    }

    public void testNewLineIndentationBeforeSynchronizedBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        synchronized (check())| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        synchronized (check())\n        {\n    }\n}\n");
    }

    public void testLineIndentationBeforeSynchronizedBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        synchronized (check())\n|            {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        synchronized (check())\n        {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedSynchronizedBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        synchronized (check())| {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        synchronized (check())\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedSynchronizedBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        synchronized (check())\n|            {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        synchronized (check())\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationBeforeTryStmt() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try\n            \n    }\n}\n");
    }

    public void testLineIndentationBeforeTryStmt() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try\n            \n    }\n}\n");
    }

    public void testNewLineIndentationBeforeTryBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try\n        {\n    }\n}\n");
    }

    public void testLineIndentationBeforeTryBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try\n|            {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try\n        {\n    }\n}\n");
    }

    public void testNewLineIndentationInsideTryResources() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try (lock()|) {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try (lock()\n                ) {\n    }\n}\n");
    }

    public void testLineIndentationInsideTryResources() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try (lock()\n|) {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try (lock()\n                ) {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeTryWithResourceBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try (lock())| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try (lock())\n        {\n    }\n}\n");
    }

    public void testLineIndentationBeforeTryWithResourceBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try (lock())\n|            {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try (lock())\n        {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedTryBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try| {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        try\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedTryBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try\n|            {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        try\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationBeforeCatch() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try {\n        }| catch (Exception e) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try {\n        }\n        catch (Exception e) {\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeCatch() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try {\n        }\n|catch (Exception e) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try {\n        }\n        catch (Exception e) {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeCatchOnNewLine() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try {\n        }|\n        catch (Exception e) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try {\n        }\n        \n        catch (Exception e) {\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeCatchOnNewLine() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try {\n        }\n|\n        catch (Exception e) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try {\n        }\n        \n        catch (Exception e) {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeCatchBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try {\n        } catch (Exception e)| {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try {\n        } catch (Exception e)\n        {\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeCatchBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try {\n        } catch (Exception e)\n|{\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try {\n        } catch (Exception e)\n        {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedCatchBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try\n          {\n          }\n        catch (Exception e)| {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        try\n          {\n          }\n        catch (Exception e)\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedCatchBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try\n          {\n          }\n        catch (Exception e)\n|{\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        try\n          {\n          }\n        catch (Exception e)\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationBeforeFinally() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try {\n        }| finally {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try {\n        }\n        finally {\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeFinally() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try {\n        }\n|finally {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try {\n        }\n        finally {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeFinallyOnNewLine() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try {\n        }|\n        finally {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try {\n        }\n        \n        finally {\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeFinallyOnNewLine() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try {\n        }\n|\n        finally {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try {\n        }\n        \n        finally {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeFinallyBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try {\n        } finally| {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try {\n        } finally\n        {\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeFinallyBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try {\n        } finally\n|{\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        try {\n        } finally\n        {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedFinallyBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try\n          {\n          }\n        finally| {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        try\n          {\n          }\n        finally\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedFinallyBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        try\n          {\n          }\n        finally\n|{\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        try\n          {\n          }\n        finally\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationInsideWhileCond() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        while (check()|)\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        while (check()\n                )\n    }\n}\n");
    }

    public void testLineIndentationInsideWhileCond() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        while (check()\n|)\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        while (check()\n                )\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeWhileStmt() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        while (true)|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        while (true)\n            \n    }\n}\n");
    }

    public void testLineIndentationBeforeWhilefStmt() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        while (true)\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        while (true)\n            \n    }\n}\n");
    }

    public void testNewLineIndentationBeforeWhileBlock() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        while (true)| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        while (true)\n        {\n    }\n}\n");
    }

    public void testLineIndentationBeforeWhileBlock() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        while (true)\n|            {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        while (true)\n        {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedWhileBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        while (true)| {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        while (true)\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedWhileBlock() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        while (true)\n|            {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        while (true)\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationInsideSwitchExpr() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()|) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()\n                ) {\n        }\n    }\n}\n");
    }

    public void testLineIndentationInsideSwitchExpr() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()\n|) {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()\n                ) {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationAfterSwitchExpr() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get())|{\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get())\n        {\n        }\n    }\n}\n");
    }

    public void testLineIndentationAfterSwitchExpr() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get())\n| {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get())\n        {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationAfterHalfIndentedSwitchExpr() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get())|{\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        switch(get())\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationAfterHalfIndentedSwitchExpr() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get())\n|            {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        switch(get())\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationInsideEmptySwitch() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {|\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            \n        }\n    }\n}\n");
    }

    public void testLineIndentationInsideEmptySwitch() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n|\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            \n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeSwitchCases() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {|case 1:\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeSwitchCases() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n|case 1:\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationAfterSwitchCase() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:|\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                \n        }\n    }\n}\n");
    }

    public void testLineIndentationAfterSwitchCase() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n|\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                \n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeEmptyBlockAfterSwitchCase() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:|{\n            }\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n            {\n            }\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeEmptyBlockAfterSwitchCase() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n|{\n            }\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n            {\n            }\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationInsideCase() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:|\n                break;\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                \n                break;\n        }\n    }\n}\n");
    }

    public void testLineIndentationInsideCase() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n|\n                break;\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                \n                break;\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationAfterCaseStmt() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                break;|\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                break;\n                \n        }\n    }\n}\n");
    }

    public void testLineIndentationAfterCaseStmt() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                break;\n|\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                break;\n                \n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeSwitchDefault() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                break;|default:\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                break;\n            default:\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeSwitchDefault() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                break;\n                |default:\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                break;\n            default:\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeSwitchEnd() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                break;|}\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                break;\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeSwitchEnd() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                break;\n                |}\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1:\n                break;\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedSwitchEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get())\n          {\n            case 1:\n                break;|}\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        switch(get())\n          {\n            case 1:\n                break;\n          }\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedSwitchEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get())\n          {\n            case 1:\n                break;\n                |}\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        switch(get())\n          {\n            case 1:\n                break;\n          }\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationAfterEqInArrayInit() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr =|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr =\n                \n    }\n}\n");
    }

    public void testLineIndentationAfterEqInArrayInit() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr =\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr =\n                \n    }\n}\n");
    }

    public void testNewLineIndentationBeforeArrayInit() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr =| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr =\n        {\n    }\n}\n");
    }

    public void testLineIndentationBeforeArrayInit() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr =\n|                {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr =\n        {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedArrayInit() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr =| {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        int[] arr =\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedArrayInit() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr =\n|                {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        int[] arr =\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationBeforeNewArrayInit() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = new int[]| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr = new int[]\n        {\n    }\n}\n");
    }

    public void testLineIndentationBeforeNewArrayInit() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = new int[]\n|                {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr = new int[]\n        {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedNewArrayInit() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = new int[]| {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        int[] arr = new int[]\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedNewArrayInit() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = new int[]\n|                {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        int[] arr = new int[]\n          {\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationIndideEmptyArrayInit() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = {|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n            \n    }\n}\n");
    }

    public void testLineIndentationIndideEmptyArrayInit() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n            \n    }\n}\n");
    }

    public void testNewLineIndentationIndideArrayInit() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n            1,|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n            1,\n            \n    }\n}\n");
    }

    public void testLineIndentationIndideArrayInit() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n            1,\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n            1,\n            \n    }\n}\n");
    }

    public void testNewLineIndentationIndideAlignedArrayInit() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = {1,\n                     2,|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr = {1,\n                     2,\n                     \n    }\n}\n");
    }

    public void testLineIndentationIndideAlignedArrayInit() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = {1,\n                     2,\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr = {1,\n                     2,\n                     \n    }\n}\n");
    }

    public void testNewLineIndentationBeforeArrayInitEnd() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n            1, 2|}\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n            1, 2\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforArrayInitEnd() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n            1, 2\n|            }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n            1, 2\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeEmptyArrayInitEnd() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = {|}\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeEmptyArrayInitEnd() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n|            }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int[] arr = {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeEmptyHalfIndentedArrayInitEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr =\n          {|}\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        int[] arr =\n          {\n          }\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeEmptyHalfIndentedArrayInitEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int[] arr =\n          {\n|            }\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        int[] arr =\n          {\n          }\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentationBeforeNewClassTreeBody() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        Runnable r = new Runnable()| {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        Runnable r = new Runnable()\n        {\n    }\n}\n");
    }

    public void testLineIndentationBeforeNewClassTreeBody() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        Runnable r = new Runnable()\n|                {\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        Runnable r = new Runnable()\n        {\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedNewClassTreeBody() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        Runnable r = new Runnable()| {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        Runnable r = new Runnable()\n          {\n    }\n}\n");
        } finally {
            preferences.remove("classDeclBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedNewClassTreeBody() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        Runnable r = new Runnable()\n|                {\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        Runnable r = new Runnable()\n          {\n    }\n}\n");
        } finally {
            preferences.remove("classDeclBracePlacement");
        }
    }

    public void testNewLineIndentationAfterLineComment() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        System.out.println(); // Comment|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        System.out.println(); // Comment\n        \n    }\n}\n");
    }

    public void testLineIndentationAfterLineComment() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        System.out.println(); // Comment\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        System.out.println(); // Comment\n        \n    }\n}\n");
    }

    public void testNewLineIndentationAtMultilineCommentStart() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("enableBlockCommentFormatting", true);
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    /*|\n    public void op() {\n    }\n}\n",
                    "package t;\npublic class T {\n    /*\n     * \n    public void op() {\n    }\n}\n");
        } finally {
            preferences.remove("enableBlockCommentFormatting");
        }
    }

    public void testLineIndentationAtMultilineCommentStart() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("enableBlockCommentFormatting", true);
        try {
            performLineIndentationTest("package t;\npublic class T {\n    /*\n|\n    public void op() {\n    }\n}\n",
                    "package t;\npublic class T {\n    /*\n     * \n    public void op() {\n    }\n}\n");
        } finally {
            preferences.remove("enableBlockCommentFormatting");
        }
    }

    public void testNewLineIndentationInsideMultilineComment() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("enableBlockCommentFormatting", true);
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    /*|\n     */\n    public void op() {\n    }\n}\n",
                    "package t;\npublic class T {\n    /*\n     * \n     */\n    public void op() {\n    }\n}\n");
        } finally {
            preferences.remove("enableBlockCommentFormatting");
        }
    }

    public void testLineIndentationInsideMultilineComment() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("enableBlockCommentFormatting", true);
        try {
            performLineIndentationTest("package t;\npublic class T {\n    /*\n|\n     */\n    public void op() {\n    }\n}\n",
                    "package t;\npublic class T {\n    /*\n     * \n     */\n    public void op() {\n    }\n}\n");
        } finally {
            preferences.remove("enableBlockCommentFormatting");
        }
    }

    public void testNewLineIndentationAtMultilineCommentStartWithTabIndents() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("expand-tabs", false);
        preferences.putInt("tab-size", 4);
        preferences.putBoolean("enableBlockCommentFormatting", true);
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n\t/*|\n\tpublic void op() {\n\t}\n}\n",
                    "package t;\npublic class T {\n\t/*\n\t * \n\tpublic void op() {\n\t}\n}\n");
        } finally {
            preferences.remove("enableBlockCommentFormatting");
            preferences.remove("tab-size");
            preferences.remove("expand-tabs");
        }
    }

    public void testLineIndentationAtMultilineCommentStartWithTabIndents() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("expand-tabs", false);
        preferences.putInt("tab-size", 4);
        preferences.putBoolean("enableBlockCommentFormatting", true);
        try {
            performLineIndentationTest("package t;\npublic class T {\n\t/*\n|\n\tpublic void op() {\n\t}\n}\n",
                    "package t;\npublic class T {\n\t/*\n\t * \n\tpublic void op() {\n\t}\n}\n");
        } finally {
            preferences.remove("enableBlockCommentFormatting");
            preferences.remove("tab-size");
            preferences.remove("expand-tabs");
        }
    }

    public void testNewLineIndentationInsideMultilineCommentWithTabIndents() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("expand-tabs", false);
        preferences.putInt("tab-size", 4);
        preferences.putBoolean("enableBlockCommentFormatting", true);
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n\t/*|\n\t */\n\tpublic void op() {\n\t}\n}\n",
                    "package t;\npublic class T {\n\t/*\n\t * \n\t */\n\tpublic void op() {\n\t}\n}\n");
        } finally {
            preferences.remove("enableBlockCommentFormatting");
            preferences.remove("tab-size");
            preferences.remove("expand-tabs");
        }
    }

    public void testLineIndentationInsideMultilineCommentWithTabIndents() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("expand-tabs", false);
        preferences.putInt("tab-size", 4);
        preferences.putBoolean("enableBlockCommentFormatting", true);
        try {
            performLineIndentationTest("package t;\npublic class T {\n\t/*\n|\n\t */\n\tpublic void op() {\n\t}\n}\n",
                    "package t;\npublic class T {\n\t/*\n\t * \n\t */\n\tpublic void op() {\n\t}\n}\n");
        } finally {
            preferences.remove("enableBlockCommentFormatting");
            preferences.remove("tab-size");
            preferences.remove("expand-tabs");
        }
    }

    public void testNewLineIndentationAtWrongIndentedMultilineCommentStart() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("enableBlockCommentFormatting", true);
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n   /*|\n    public void op() {\n    }\n}\n",
                    "package t;\npublic class T {\n   /*\n    * \n    public void op() {\n    }\n}\n");
        } finally {
            preferences.remove("enableBlockCommentFormatting");
        }
    }

    public void testLineIndentationAtWrongIndentedMultilineCommentStart() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("enableBlockCommentFormatting", true);
        try {
            performLineIndentationTest("package t;\npublic class T {\n   /*\n|\n    public void op() {\n    }\n}\n",
                    "package t;\npublic class T {\n   /*\n    * \n    public void op() {\n    }\n}\n");
        } finally {
            preferences.remove("enableBlockCommentFormatting");
        }
    }

    public void testNewLineIndentationInsideWrongIndentedMultilineComment() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("enableBlockCommentFormatting", true);
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n   /*\n    *|\n    */\n    public void op() {\n    }\n}\n",
                    "package t;\npublic class T {\n   /*\n    *\n    * \n    */\n    public void op() {\n    }\n}\n");
        } finally {
            preferences.remove("enableBlockCommentFormatting");
        }
    }

    public void testLineIndentationInsideWrongIndentedMultilineComment() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("enableBlockCommentFormatting", true);
        try {
            performLineIndentationTest("package t;\npublic class T {\n   /*\n    *\n|\n    */\n    public void op() {\n    }\n}\n",
                    "package t;\npublic class T {\n   /*\n    *\n    * \n    */\n    public void op() {\n    }\n}\n");
        } finally {
            preferences.remove("enableBlockCommentFormatting");
        }
    }

    public void testNewLineIndentationInsideMethodInvocation() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        System.arraycopy(arr,|);\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        System.arraycopy(arr,\n                );\n    }\n}\n");
    }

    public void testLineIndentationInsideMethodInvocation() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        System.arraycopy(arr,\n|);\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        System.arraycopy(arr,\n                );\n    }\n}\n");
    }

    public void testNewLineIndentationAfterAlignedMethodInvocationArg() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        System.arraycopy(arr,\n                         1,|);\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        System.arraycopy(arr,\n                         1,\n                         );\n    }\n}\n");
    }

    public void testLineIndentationAfterAlignedMethodInvocationArg() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        System.arraycopy(arr,\n                         1,\n|);\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        System.arraycopy(arr,\n                         1,\n                         );\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeLabeledStatement() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {|mylabel:\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        mylabel:\n    }\n}\n");
    }

    public void testLineIndentationBeforeLabeledStatement() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n|mylabel:\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        mylabel:\n    }\n}\n");
    }

    public void testNewLineIndentationInsideLabeledStatement() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        mylabel:|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        mylabel:\n        \n    }\n}\n");
    }

    public void testLineIndentationInsideLabeledStatement() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        mylabel:\n|\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        mylabel:\n        \n    }\n}\n");
    }

    public void testNewLineIndentationBeforeAbsolutLabeledStatement() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("absoluteLabelIndent", true);
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {|mylabel:\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\nmylabel:\n    }\n}\n");
        } finally {
            preferences.remove("absoluteLabelIndent");
        }
    }

    public void testLineIndentationBeforeAbsolutLabeledStatement() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("absoluteLabelIndent", true);
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n|        mylabel:\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\nmylabel:\n    }\n}\n");
        } finally {
            preferences.remove("absoluteLabelIndent");
        }
    }

    public void testNewLineIndentationInsideAbsolutLabeledStatement() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("absoluteLabelIndent", true);
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\nmylabel:|\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\nmylabel:\n        \n    }\n}\n");
        } finally {
            preferences.remove("absoluteLabelIndent");
        }
    }

    public void testLineIndentationInsideAbsolutLabeledStatement() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("absoluteLabelIndent", true);
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\nmylabel:\n|\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\nmylabel:\n        \n    }\n}\n");
        } finally {
            preferences.remove("absoluteLabelIndent");
        }
    }

    public void testSpanIndentation() throws Exception {
        performSpanIndentationTest("package t;\npublic class T {\n|private void t() {\nSystem.err.println(1);\n}\n |}\n",
                "package t;\npublic class T {\n    private void t() {\n        System.err.println(1);\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeSwitchRuleCases() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {|case 1->\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeSwitchRuleCases() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n|case 1->\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationAfterSwitchRuleCase() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->|\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                \n        }\n    }\n}\n");
    }

    public void testLineIndentationAfterSwitchRuleCase() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n|\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                \n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeEmptyBlockAfterSwitchRuleCase() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->|{\n            }\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n            {\n            }\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeEmptyBlockAfterSwitchRuleCase() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n|{\n            }\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n            {\n            }\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationInsideRuleCase() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->|\n                break;\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                \n                break;\n        }\n    }\n}\n");
    }

    public void testLineIndentationInsideRuleCase() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n|\n                break;\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                \n                break;\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationAfterRuleCaseStmt() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                break;|\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                break;\n                \n        }\n    }\n}\n");
    }

    public void testLineIndentationAfterRuleCaseStmt() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                break;\n|\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                break;\n                \n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeSwitchRuleDefault() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                break;|default->\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                break;\n            default->\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeSwitchRuleDefault() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                break;\n                |default->\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                break;\n            default->\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeSwitchRuleEnd() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                break;|}\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                break;\n        }\n    }\n}\n");
    }

    public void testLineIndentationBeforeSwitchRuleEnd() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                break;\n                |}\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        switch(get()) {\n            case 1->\n                break;\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationBeforeHalfIndentedSwitchRuleEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get())\n          {\n            case 1->\n                break;|}\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        switch(get())\n          {\n            case 1->\n                break;\n          }\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testLineIndentationBeforeHalfIndentedSwitchRuleEnd() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        switch(get())\n          {\n            case 1->\n                break;\n                |}\n    }\n}\n",
                    "package t;\npublic class T {\n    public void op() {\n        switch(get())\n          {\n            case 1->\n                break;\n          }\n    }\n}\n");
        } finally {
            preferences.remove("otherBracePlacement");
        }
    }

    public void testNewLineIndentBeforeSwitchExprCases() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {|case 1->\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->\n        }\n    }\n}\n");
    }

    public void testLineIndentBeforeSwitchExprCases() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n|case 1->\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->\n        }\n    }\n}\n");
    }

    public void testNewLineIndentAfterSwitchExprCase() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->|\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->\n                \n        }\n    }\n}\n");
    }

    public void testNewLineIndentationInsideSwExpCase() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->|\n                5;\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->\n                \n                5;\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationInsideSwExpCase2() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1:|\n                yield 5;\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1:\n                \n                yield 5;\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationInsideSwExpCase3() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->|\n                {yield 5;}\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->\n                \n                {yield 5;}\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationInsideSwExpIllegalCase() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->|\n                yield 5;\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->\n                \n                yield 5;\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationInsideSwExpIllegalCase2() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->|\n                System.out.println(\"aa\");\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->\n                \n                System.out.println(\"aa\");\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationAfterSwExprCase() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1:\n                yield 5;|\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1:\n                yield 5;\n                \n        }\n    }\n}\n");
    }

    public void testNewLineIndentationAfterSwExprRule() throws Exception {
        performNewLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->\n                5;|\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int a = switch(get()) {\n            case 1->\n                5;\n                \n        }\n    }\n}\n");
    }

    public void testLineIndentationAfterSwitchExprSwitch() throws Exception {
        performLineIndentationTest("package t;\npublic class T {\n    public void op() {\n        int a = switch(get())\n| {\n        }\n    }\n}\n",
                "package t;\npublic class T {\n    public void op() {\n        int a = switch(get())\n        {\n        }\n    }\n}\n");
    }

    public void testNewLineIndentationTextBlock1() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_13");
        } catch (IllegalArgumentException ex) {
            //OK, skip test:
            return ;
        }
        performNewLineIndentationTest("package t;\npublic class T {\n    private final String s = \"\"\"|\n}\n",
                "package t;\npublic class T {\n    private final String s = \"\"\"\n                             \n}\n");
    }

    public void testSpanIndentationTextBlock1() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_13");
        } catch (IllegalArgumentException ex) {
            //OK, skip test:
            return ;
        }
        performSpanIndentationTest("package t;\npublic class T {\n|private final String s = \"\"\"\n\"\"\";|\n}\n",
                "package t;\npublic class T {\n    private final String s = \"\"\"\n                             \"\"\";\n}\n");
    }

    public void testSpanIndentationTextBlock2() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_13");
        } catch (IllegalArgumentException ex) {
            //OK, skip test:
            return ;
        }
        performSpanIndentationTest("package t;\npublic class T {\n|private final String s = \"\"\"\n1\n  2\n 3\n\"\"\";|\n}\n",
                "package t;\npublic class T {\n    private final String s = \"\"\"\n                             1\n                               2\n                              3\n                             \"\"\";\n}\n");
    }
    
    public void testLineIndentationBeforeRecord() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_14");
        } catch (IllegalArgumentException ex) {
            //OK, skip test:
            return;
        }
        performLineIndentationTest("package t;\n| public record T() {\n}\n",
                "package t;\npublic record T() {\n}\n");
    }

    public void testNewLineIndentationBeforeRecodBody() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_14");
        } catch (IllegalArgumentException ex) {
            //OK, skip test:
            return;
        }
        performNewLineIndentationTest("package t;\npublic record T()|{\n}\n",
                "package t;\npublic record T()\n{\n}\n");
    }

    public void testLineIndentationBeforeHalfIndentedRecordBody() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_14");
        } catch (IllegalArgumentException ex) {
            //OK, skip test:
            return;
        }
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic record T()\n|{\n}\n",
                    "package t;\npublic record T()\n  {\n}\n");
        } finally {
            preferences.remove("classDeclBracePlacement");
        }
    }

    public void testNewLineIndentationInsideRecord() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_14");
        } catch (IllegalArgumentException ex) {
            //OK, skip test:
            return;
        }
        performNewLineIndentationTest("package t;\npublic record T() {|\n}\n",
                "package t;\npublic record T() {\n    \n}\n");
    }

    public void testNewLineIndentationBeforeEmptyRecordEnd() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_14");
        } catch (IllegalArgumentException ex) {
            //OK, skip test:
            return;
        }
        performNewLineIndentationTest("package t;\npublic record T() {|}\n",
                "package t;\npublic record T() {\n}\n");
    }

    public void testLineIndentationBeforeEmptyRecordEnd() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_14");
        } catch (IllegalArgumentException ex) {
            //OK, skip test:
            return;
        }
        performLineIndentationTest("package t;\npublic record T() {\n|}\n",
                "package t;\npublic record T() {\n}\n");
    }

    public void testLineIndentationBeforeEmptyHalfIndentedRecordEnd() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_14");
        } catch (IllegalArgumentException ex) {
            //OK, skip test:
            return;
        }
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        try {
            performLineIndentationTest("package t;\npublic record T()\n  {\n|    }\n",
                    "package t;\npublic record T()\n  {\n  }\n");
        } finally {
            preferences.remove("classDeclBracePlacement");
        }
    }

    public void testCompactSourceFilesNewLineIndent() throws Exception {
        performNewLineIndentationTest(
                """
                void main() {|
                """,
                """
                void main() {
                \s\s\s\s
                """);
    }

    public void testCompactSourceFilesReindent() throws Exception {
        performSpanIndentationTest(
                """
                |void main() {
                switch (0) {
                case 0 -> {
                if (true) {
                System.err.println(0);
                }
                }
                }
                }|
                """,
                """
                void main() {
                    switch (0) {
                        case 0 -> {
                            if (true) {
                                System.err.println(0);
                            }
                        }
                    }
                }
                """);
    }

    private void performNewLineIndentationTest(String code, String golden) throws Exception {
        int pos = code.indexOf('|');

        assertNotSame(-1, pos);

        code = code.replaceAll(Pattern.quote("|"), "");

        doc.insertString(0, code, null);
        Indent indent = Indent.get(doc);
        indent.lock();
        indent.indentNewLine(pos);
        indent.unlock();
        assertEquals(golden, doc.getText(0, doc.getLength()));
    }

    private void performLineIndentationTest(String code, String golden) throws Exception {
        int pos = code.indexOf('|');

        assertNotSame(-1, pos);

        code = code.replaceAll(Pattern.quote("|"), "");

        doc.insertString(0, code, null);
        Indent indent = Indent.get(doc);
        indent.lock();
        indent.reindent(pos);
        indent.unlock();
        assertEquals(golden, doc.getText(0, doc.getLength()));
        switch (pos) {
            case 1:

        }
    }

    private void performSpanIndentationTest(String code, String golden) throws Exception {
        String[] parts = code.split(Pattern.quote("|"));

        assertEquals(3, parts.length);

        int start = parts[0].length();
        int end = start + parts[1].length();

        code = parts[0] + parts[1] + parts[2];

        doc.insertString(0, code, null);
        Indent indent = Indent.get(doc);
        indent.lock();
        indent.reindent(start, end);
        indent.unlock();
        assertEquals(golden, doc.getText(0, doc.getLength()));
    }
}
