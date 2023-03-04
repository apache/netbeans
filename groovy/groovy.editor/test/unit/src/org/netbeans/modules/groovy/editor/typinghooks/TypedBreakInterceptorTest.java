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

package org.netbeans.modules.groovy.editor.typinghooks;

import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 *
 * @author Martin Janicek
 */
public class TypedBreakInterceptorTest extends GroovyTestBase {

    public TypedBreakInterceptorTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testInsertBrace3() throws Exception {
        insertBreak("x = {^}", "x = {\n    ^\n}");
    }

    public void testInsertEnd1() throws Exception {
        insertBreak("x^", "x\n^");
    }

    public void testInsertBlockComment5() throws Exception {
        insertBreak("^/*\n*/\n", "\n^/*\n*/\n");
    }

    public void testInsertEnd2() throws Exception {
        insertBreak("function foo() {^", "function foo() {\n    ^\n}");
    }

    public void testInsertEnd3() throws Exception {
        insertBreak("function foo() {^\n}", "function foo() {\n    ^\n}");
    }

    public void testInsertIf1() throws Exception {
        insertBreak("    if (true) {^", "    if (true) {\n        ^\n    }");
    }
    
    public void testContComment() throws Exception {
        if (GroovyTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("// ^", "// \n// ^");
        } else {
            insertBreak("// ^", "// \n^");
        }
    }

    public void testContComment2() throws Exception {
        // No auto-# on new lines
        if (GroovyTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("   //  ^", "   //  \n   //  ^");
        } else {
            insertBreak("   //  ^", "   //  \n   ^");
        }
    }

    public void testContComment3() throws Exception {
        // No auto-# on new lines
        if (GroovyTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("   //\t^", "   //\t\n   //\t^");
        } else {
            insertBreak("   //\t^", "   //\t\n   ^");
        }
    }

    public void testContComment4() throws Exception {
        insertBreak("// foo\n^", "// foo\n\n^");
    }

    public void testContComment5() throws Exception {
        // No auto-# on new lines
        if (GroovyTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("      // ^", "      // \n      // ^");
        } else {
            insertBreak("      // ^", "      // \n      ^");
        }
    }

    public void testContComment6() throws Exception {
        insertBreak("   // foo^bar", "   // foo\n   // ^bar");
    }

    public void testContComment8() throws Exception {
        insertBreak("   // foo^bar", "   // foo\n   // ^bar");
    }


    public void testContComment9() throws Exception {
        insertBreak("^// foobar", "\n^// foobar");
    }

    public void testContComment10() throws Exception {
        insertBreak("//foo\n^// foobar", "//foo\n// ^\n// foobar");
    }

    public void testContComment11() throws Exception {
        // This behavior is debatable -- to be consistent with testContComment10 I
        // should arguably continue comments here as well
        insertBreak("code //foo\n^// foobar", "code //foo\n\n^// foobar");
    }

    public void testContComment12() throws Exception {
        insertBreak("  code\n^// foobar", "  code\n\n  ^// foobar");
    }

    public void testContComment14() throws Exception {
        insertBreak("function foo() {\n    code\n^// foobar\n}\n", "function foo() {\n    code\n\n    ^// foobar\n}\n");
    }

    public void testContComment15() throws Exception {
        insertBreak("\n\n^// foobar", "\n\n\n^// foobar");
    }

    public void testContComment16() throws Exception {
        insertBreak("\n  \n^// foobar", "\n  \n\n^// foobar");
    }

    public void testContComment17() throws Exception {
        insertBreak("function foo() {\n  // cmnt1\n^  // cmnt2\n}\n", "function foo() {\n  // cmnt1\n  // ^\n  // cmnt2\n}\n");
    }

    public void testNoContComment() throws Exception {
        // No auto-// on new lines
        insertBreak("foo // ^", "foo // \n^");
    }
    
    public void testStringLiteral01() throws Exception {
        insertBreak("String a = \"\"\" ahoj^how are you? \"\"\"", "String a = \"\"\" ahoj\n^how are you? \"\"\"");            
    }
    
    public void testStringLiteral02() throws Exception {
        insertBreak("\"\"\" ahoj^how are you?", "\"\"\" ahoj\n^how are you?");
    }

    public void testStringLiteral03() throws Exception {
        insertBreak("\"\"\" ahoj^", "\"\"\" ahoj\n^");
    }
    
    public void testStringLiteral04() throws Exception {
        insertBreak("String a = \" ahoj^how are you? \"", "String a = \" ahoj\\n\\\n^how are you? \"");            
    }

    public void testStringLiteral05() throws Exception {
        insertBreak("String a = ''' ahoj^how are you? '''", "String a = ''' ahoj\n^how are you? '''");            
    }
    
    public void testStringLiteral06() throws Exception {
        insertBreak("''' ahoj^how are you?", "''' ahoj\n^how are you?");
    }

    public void testStringLiteral07() throws Exception {
        insertBreak("''' ahoj^", "''' ahoj\n^");
    }
        
}
