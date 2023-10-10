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
package org.netbeans.modules.javascript2.editor.formatter;

import java.util.Collections;
import java.util.HashMap;
import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;

/**
 *
 * @author Petr Hejl
 */
public class JsFormatterTest extends JsFormatterTestBase {

    public JsFormatterTest(String testName) {
        super(testName);
    }

    public void testSimple() throws Exception {
        reformatFileContents("testfiles/simple.js",new IndentPrefs(4, 4));
    }

    public void testSimpleIndented() throws Exception {
        reindentFileContents("testfiles/simple.js", null);
    }

    public void testScriptInput() throws Exception {
        reformatFileContents("testfiles/scriptInput.js",new IndentPrefs(4, 4));
    }

    public void testScriptInputBroken() throws Exception {
        reformatFileContents("testfiles/scriptInputBroken.js",new IndentPrefs(4, 4));
    }

    public void testTrailingSpaces1() throws Exception {
        format("var a = 1;   \nvar b = 3;                   \n",
                "var a = 1;\nvar b = 3;\n", new IndentPrefs(4, 4));
    }

    public void testTrailingSpaces2() throws Exception {
        format("var a = 1;   \nvar b = 3;                   \n         \n",
                "var a = 1;\nvar b = 3;\n\n", new IndentPrefs(4, 4));
    }

    public void testIndentation1() throws Exception {
        format("\n var a = 1;   \n        var b = 3;                   \n",
                "\nvar a = 1;\nvar b = 3;\n", new IndentPrefs(4, 4));
    }

    public void testIndentation2() throws Exception {
        format(" var a = 1;   \n        var b = 3;                   \n",
                "var a = 1;\nvar b = 3;\n", new IndentPrefs(4, 4));
    }

    public void testFunctions1() throws Exception {
        reformatFileContents("testfiles/formatter/functions1.js",new IndentPrefs(4, 4));
    }

    public void testFunctions1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions1.js");
    }

    public void testFunctions1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions1.js", null);
    }

    public void testFunctions2() throws Exception {
        reformatFileContents("testfiles/formatter/functions2.js",new IndentPrefs(4, 4));
    }

    public void testFunctions2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions2.js");
    }

    public void testFunctions2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions2.js", null);
    }

    public void testFunctions3() throws Exception {
        reformatFileContents("testfiles/formatter/functions3.js",new IndentPrefs(4, 4));
    }

    public void testFunctions3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions3.js");
    }

    public void testFunctions3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions3.js", null);
    }

    public void testFunctions4() throws Exception {
        reformatFileContents("testfiles/formatter/functions4.js",new IndentPrefs(4, 4));
    }

    public void testFunctions4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions4.js");
    }

    public void testFunctions4Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions4.js", null);
    }

    public void testFunctions5() throws Exception {
        reformatFileContents("testfiles/formatter/functions5.js",new IndentPrefs(4, 4));
    }

    public void testFunctions5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions5.js");
    }

    public void testFunctions5Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions5.js", null);
    }

    public void testFunctions6Default() throws Exception {
        reformatFileContents("testfiles/formatter/functions6.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFunctions6Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeMethodDeclParen, true);
        reformatFileContents("testfiles/formatter/functions6.js",
                options, ".inverted.formatted");
    }

    public void testFunctions6Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions6.js");
    }

    public void testFunctions6Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions6.js", null);
    }

    public void testFunctions7() throws Exception {
        reformatFileContents("testfiles/formatter/functions7.js",new IndentPrefs(4, 4));
    }

    public void testFunctions7Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions7.js");
    }

    public void testFunctions7Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions7.js", null);
    }

    public void testFunctionDeclaration1() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeMethodDeclParen, true);
        reformatFileContents("testfiles/formatter/functionDeclaration1.js", options);
    }

    public void testFunctionDeclaration1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionDeclaration1.js");
    }

    public void testFunctionDeclaration1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionDeclaration1.js", null);
    }

    public void testFunctionDeclaration2Default() throws Exception {
        reformatFileContents("testfiles/formatter/functionDeclaration2.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFunctionDeclaration2Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceWithinMethodDeclParens, true);
        reformatFileContents("testfiles/formatter/functionDeclaration2.js", options, ".inverted.formatted");
    }

    public void testFunctionDeclaration2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionDeclaration2.js");
    }

    public void testFunctionDeclaration2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionDeclaration2.js", null);
    }

    public void testFunctionDeclaration3Default() throws Exception {
        reformatFileContents("testfiles/formatter/functionDeclaration3.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFunctionDeclaration3Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, false);
        reformatFileContents("testfiles/formatter/functionDeclaration3.js", options, ".inverted.formatted");
    }

    public void testFunctionDeclaration3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionDeclaration3.js");
    }

    public void testFunctionDeclaration3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionDeclaration3.js", null);
    }

    public void testFunctionDeclaration4Default() throws Exception {
        reformatFileContents("testfiles/formatter/functionDeclaration4.js",new IndentPrefs(4, 4));
    }
    
    public void testFunctionDeclaration4Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeAnonMethodDeclParen, false);
        reformatFileContents("testfiles/formatter/functionDeclaration4.js", options, ".inverted.formatted");
    }

    public void testFunctionDeclaration4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionDeclaration4.js");
    }

    public void testFunctionDeclaration4Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionDeclaration4.js", null);
    }

    public void testFunctionDeclaration5Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/functionDeclaration5.js", options, ".wrapAlways.formatted");
    }

    public void testFunctionDeclaration5Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/functionDeclaration5.js", options, ".wrapNever.formatted");
    }

    public void testFunctionDeclaration5IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/functionDeclaration5.js", options, ".wrapIfLong.formatted");
    }

    public void testFunctionDeclaration5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionDeclaration5.js");
    }

    public void testFunctionDeclaration5Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionDeclaration5.js", null);
    }
    
    public void testFunctionDeclaration6Default() throws Exception {
        reformatFileContents("testfiles/formatter/functionDeclaration6.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }
    
    public void testFunctionDeclaration6Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionDeclaration6.js");
    }
    
    public void testFunctionDeclaration7Default() throws Exception {
        reformatFileContents("testfiles/formatter/functionDeclaration7.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }
    
    public void testFunctionDeclaration7Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/functionDeclaration7.js", options, ".wrapAlways.formatted");
    }
    
    public void testGenerator1Default() throws Exception {
        reformatFileContents("testfiles/formatter/generator1.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }
    

        
    public void testGenerator1Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/generator1.js", options, ".wrapAlways.formatted");
    }

    public void testGenerator1BinaryAfterAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapAfterBinaryOps, true);
        reformatFileContents("testfiles/formatter/generator1.js", options, ".binaryAfter.wrapAlways.formatted");
    }
    
    public void testGenerator1BinaryBeforeAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapAfterBinaryOps, false);
        reformatFileContents("testfiles/formatter/generator1.js", options, ".binaryBefore.wrapAlways.formatted");
    }
    
    public void testGenerator1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/generator1.js");
    }
    
    public void testGenerator2Default() throws Exception {
        reformatFileContents("testfiles/formatter/generator2.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }
    
    public void testGenerator2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/generator2.js");
    }

    public void testFunctionCall1Default() throws Exception {
        reformatFileContents("testfiles/formatter/functionCall1.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFunctionCall1Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeMethodCallParen, true);
        reformatFileContents("testfiles/formatter/functionCall1.js", options, ".inverted.formatted");
    }

    public void testFunctionCall1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionCall1.js");
    }

    public void testFunctionCall1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionCall1.js", null);
    }

    public void testFunctionCall2Default() throws Exception {
        reformatFileContents("testfiles/formatter/functionCall2.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFunctionCall2Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceWithinMethodCallParens, true);
        reformatFileContents("testfiles/formatter/functionCall2.js", options, ".inverted.formatted");
    }

    public void testFunctionCall2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionCall2.js");
    }

    public void testFunctionCall2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionCall2.js", null);
    }

    public void testFunctionCall3Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/functionCall3.js", options, ".wrapAlways.formatted");
    }

    public void testFunctionCall3Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/functionCall3.js", options, ".wrapNever.formatted");
    }

    public void testFunctionCall3IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/functionCall3.js", options, ".wrapIfLong.formatted");
    }

    public void testFunctionCall3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionCall3.js");
    }

    public void testFunctionCall3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionCall3.js", null);
    }

    public void testFunctionCall4Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/functionCall4.js", options, ".wrapAlways.formatted");
    }

    public void testFunctionCall4Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/functionCall4.js", options, ".wrapNever.formatted");
    }

    public void testFunctionCall4IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/functionCall4.js", options, ".wrapIfLong.formatted");
    }

    public void testFunctionCall4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionCall4.js");
    }

    public void testFunctionCall4Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionCall4.js", null);
    }

    public void testFunctionCall5WrapAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/functionCall5.js", options, ".wrapAlways.formatted");
    }

    public void testFunctionCall5WrapNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/functionCall5.js", options, ".wrapNever.formatted");
    }

    public void testFunctionCall5WrapIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/functionCall5.js", options, ".wrapIfLong.formatted");
    }

    public void testFunctionCall5WrapBeforeAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapAfterDotInChainedMethodCalls, false);
        reformatFileContents("testfiles/formatter/functionCall5.js", options, ".wrapBeforeAlways.formatted");
    }

    public void testFunctionCall5WrapBeforeNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapAfterDotInChainedMethodCalls, false);
        reformatFileContents("testfiles/formatter/functionCall5.js", options, ".wrapBeforeNever.formatted");
    }

    public void testFunctionCall5WrapBeforeIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterDotInChainedMethodCalls, false);
        reformatFileContents("testfiles/formatter/functionCall5.js", options, ".wrapBeforeIfLong.formatted");
    }

    public void testFunctionCall5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionCall5.js");
    }

    public void testFunctionCall5Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionCall5.js", null);
    }

    public void testComments1() throws Exception {
        reformatFileContents("testfiles/formatter/comments1.js",new IndentPrefs(4, 4));
    }

    public void testComments1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/comments1.js");
    }

    public void testComments1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/comments1.js", null);
    }

    public void testComments2() throws Exception {
        reformatFileContents("testfiles/formatter/comments2.js",new IndentPrefs(4, 4));
    }

    public void testComments3() throws Exception {
        reformatFileContents("testfiles/formatter/comments3.js",new IndentPrefs(4, 4));
    }
    
    public void testComments4() throws Exception {
        reformatFileContents("testfiles/formatter/comments4.js",new IndentPrefs(4, 4));
    }

    public void testComments4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/comments4.js");
    }

    public void testLet1ForAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/let1.js", options, ".forWrapAlways.formatted");
    }

    public void testLet1ForNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/let1.js", options, ".forWrapNever.formatted");
    }

    public void testLet1ForIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/let1.js", options, ".forWrapIfLong.formatted");
    }

    public void testLet1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/let1.js");
    }
    
    public void testLet2() throws Exception {
        reformatFileContents("testfiles/formatter/let2.js",new IndentPrefs(4, 4));
    }

    public void testLet2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/let2.js");
    }
    
    public void testNode1() throws Exception {
        reformatFileContents("testfiles/formatter/node1.js",new IndentPrefs(4, 4));
    }

    public void testNode1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/node1.js");
    }

    public void testObjects1() throws Exception {
        reformatFileContents("testfiles/formatter/objects1.js",new IndentPrefs(4, 4));
    }

    public void testObjects1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/objects1.js");
    }

    public void testObjects2() throws Exception {
        reformatFileContents("testfiles/formatter/objects2.js",new IndentPrefs(4, 4));
    }

    public void testObjects2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/objects2.js");
    }

    public void testObjects3() throws Exception {
        reformatFileContents("testfiles/formatter/objects3.js",new IndentPrefs(4, 4));
    }

    public void testObjects3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/objects3.js");
    }

    public void testObjects4Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/objects4.js", options, ".wrapAlways.formatted");
    }

    public void testObjects4Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects4.js", options, ".wrapNever.formatted");
    }

    public void testObjects4IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects4.js", options, ".wrapIfLong.formatted");
    }

    public void testObjects4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/objects4.js");
    }

    public void testObjects5() throws Exception {
        reformatFileContents("testfiles/formatter/objects5.js",new IndentPrefs(4, 4));
    }

    public void testObjects6() throws Exception {
        reformatFileContents("testfiles/formatter/objects6.js",new IndentPrefs(4, 4));
    }

    public void testObjects7Default() throws Exception {
        reformatFileContents("testfiles/formatter/objects7.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testObjects7Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeColon, true);
        options.put(FmtOptions.spaceAfterColon, false);
        reformatFileContents("testfiles/formatter/objects7.js", options, ".inverted.formatted");
    }

    public void testObjects8Spaces() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceWithinBraces, true);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".spaces.formatted");
    }

    public void testObjects8Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".wrapAlways.formatted");
    }

    public void testObjects8Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".wrapNever.formatted");
    }

    public void testObjects8IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".wrapIfLong.formatted");
    }

    public void testObjects8ObjectOnlyAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".objectOnlyWrapAlways.formatted");
    }

    public void testObjects8ObjectOnlyNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".objectOnlyWrapNever.formatted");
    }

    public void testObjects8ObjectOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".objectOnlyWrapIfLong.formatted");
    }

    public void testObjects8PropertiesOnlyAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".propertiesOnlyWrapAlways.formatted");
    }

    public void testObjects8PropertiesOnlyNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".propertiesOnlyWrapNever.formatted");
    }

    public void testObjects8PropertiesOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".propertiesOnlyWrapIfLong.formatted");
    }

    public void testObjects9Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/objects9.js", options, ".wrapAlways.formatted");
    }

    public void testObjects9Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects9.js", options, ".wrapNever.formatted");
    }

    public void testObjects9IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects9.js", options, ".wrapIfLong.formatted");
    }
    
    public void testObjects10ObjectOnlyAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/objects10.js", options, ".objectOnlyWrapAlways.formatted");
    }

    public void testObjects10ObjectOnlyNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects10.js", options, ".objectOnlyWrapNever.formatted");
    }

    public void testObjects10ObjectOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects10.js", options, ".objectOnlyWrapIfLong.formatted");
    }

    public void testObjects10PropertiesOnlyAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/objects10.js", options, ".propertiesOnlyWrapAlways.formatted");
    }
    
    public void testObjects10PropertiesOnlyNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects10.js", options, ".propertiesOnlyWrapNever.formatted");
    }
    
    public void testObjects10PropertiesOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects10.js", options, ".propertiesOnlyWrapIfLong.formatted");
    }
    
    public void testObjects10Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/objects10.js");
    }

    public void testObjects11ObjectOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects11.js", options, ".objectOnlyWrapIfLong.formatted");
    }
    
    public void testObjects12ObjectOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects12.js", options, ".objectOnlyWrapIfLong.formatted");
    }

    public void testObjects12Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/objects12.js");
    }
    
    public void testObjects13ObjectOnlyAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/objects13.js", options, ".objectOnlyWrapAlways.formatted");
    }

    public void testObjects13ObjectOnlyNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects13.js", options, ".objectOnlyWrapNever.formatted");
    }

    public void testObjects13ObjectOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects13.js", options, ".objectOnlyWrapIfLong.formatted");
    }

    public void testObjects13PropertiesOnlyAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/objects13.js", options, ".propertiesOnlyWrapAlways.formatted");
    }
    
    public void testObjects13PropertiesOnlyNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects13.js", options, ".propertiesOnlyWrapNever.formatted");
    }
    
    public void testObjects13PropertiesOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects13.js", options, ".propertiesOnlyWrapIfLong.formatted");
    }
    
    public void testObjects13Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/objects13.js");
    }
    
    public void testObjects14() throws Exception {
        reformatFileContents("testfiles/formatter/objects14.js",new IndentPrefs(4, 4));
    }
    
    public void testObjects14NoEmptyLinesRemoval() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.maxPreservedObjectLines, 5);
        reformatFileContents("testfiles/formatter/objects14.js", options, ".noEmptyLinesRemoval.formatted");
    }
    
    public void testObjects14AllEmptyLinesRemoval() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.maxPreservedObjectLines, 0);
        reformatFileContents("testfiles/formatter/objects14.js", options, ".allEmptyLinesRemoval.formatted");
    }

    public void testDecorators1() throws Exception {
        reformatFileContents("testfiles/formatter/decorators1.js", new IndentPrefs(4, 4));
    }

    public void testDecorators1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/decorators1.js");
    }
    
    public void testDecorators2() throws Exception {
        reformatFileContents("testfiles/formatter/decorators2.js", new IndentPrefs(4, 4));
    }

    public void testDecorators2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/decorators2.js");
    }

    public void testDecorators3Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapDecorators, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/decorators3.js", options, ".wrapAlways.formatted");
    }

    public void testDecorators3Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapDecorators, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/decorators3.js", options, ".wrapNever.formatted");
    }

    public void testDecorators3IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapDecorators, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/decorators3.js", options, ".wrapIfLong.formatted");
    }

    public void testDestructuringAssignment1() throws Exception {
        reformatFileContents("testfiles/formatter/destructuringAssignment1.js", new IndentPrefs(4, 4));
    }

    public void testDestructuringAssignment1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/destructuringAssignment1.js");
    }

    public void testDestructuringAssignment2() throws Exception {
        reformatFileContents("testfiles/formatter/destructuringAssignment2.js", new IndentPrefs(4, 4));
    }

    public void testDestructuringAssignment2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/destructuringAssignment2.js");
    }

    public void testObjects14Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/objects14.js");
    }
    
    public void testObjects15() throws Exception {
        reformatFileContents("testfiles/formatter/objects15.js",new IndentPrefs(4, 4));
    }
    
    public void testObjects15Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/objects15.js");
    }

    public void testSwitch1() throws Exception {
        reformatFileContents("testfiles/formatter/switch1.js",new IndentPrefs(4, 4));
    }

    public void testSwitch1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/switch1.js");
    }

    public void testSwitch1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/switch1.js", null);
    }

    public void testSwitch2() throws Exception {
        reformatFileContents("testfiles/formatter/switch2.js",new IndentPrefs(4, 4));
    }

    public void testSwitch2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/switch2.js");
    }

    public void testSwitch3Default() throws Exception {
        reformatFileContents("testfiles/formatter/switch3.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testSwitch3Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceWithinSwitchParens, true);
        reformatFileContents("testfiles/formatter/switch3.js",
                options, ".inverted.formatted");
    }

    public void testSwitch3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/switch3.js");
    }

    public void testSwitch3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/switch3.js", null);
    }

    public void testSwitch4Default() throws Exception {
        reformatFileContents("testfiles/formatter/switch4.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testSwitch4Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeSwitchLeftBrace, false);
        reformatFileContents("testfiles/formatter/switch4.js",
                options, ".inverted.formatted");
    }

    public void testSwitch4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/switch4.js");
    }

    public void testSwitch5() throws Exception {
        reformatFileContents("testfiles/formatter/switch5.js",new IndentPrefs(4, 4));
    }

    public void testSwitch5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/switch5.js");
    }

    public void testSwitch6() throws Exception {
        reformatFileContents("testfiles/formatter/switch6.js",new IndentPrefs(4, 4));
    }

    public void testSwitch6Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/switch6.js");
    }

    public void testIf1() throws Exception {
        reformatFileContents("testfiles/formatter/if1.js",new IndentPrefs(4, 4));
    }

    public void testIf1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if1.js");
    }

    public void testIf1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if1.js", null);
    }

    public void testIf2Default() throws Exception {
        reformatFileContents("testfiles/formatter/if2.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testIf2Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceWithinIfParens, true);
        reformatFileContents("testfiles/formatter/if2.js",
                options, ".inverted.formatted");
    }

    public void testIf2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if2.js");
    }

    public void testIf2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if2.js", null);
    }

    public void testIf3Default() throws Exception {
        reformatFileContents("testfiles/formatter/if3.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testIf3Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeIfLeftBrace, false);
        options.put(FmtOptions.spaceBeforeElseLeftBrace, false);
        reformatFileContents("testfiles/formatter/if3.js",
                options, ".inverted.formatted");
    }

    public void testIf3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if3.js");
    }

    public void testIf3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if3.js", null);
    }

    public void testIf4() throws Exception {
        reformatFileContents("testfiles/formatter/if4.js", new IndentPrefs(4, 4));
    }

    public void testIf4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if4.js");
    }

    public void testIf4Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if4.js", null);
    }

    public void testIf5() throws Exception {
        reformatFileContents("testfiles/formatter/if5.js",new IndentPrefs(4, 4));
    }

    public void testIf5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if5.js");
    }

    public void testIf5Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if5.js", null);
    }

    public void testIf6() throws Exception {
        reformatFileContents("testfiles/formatter/if6.js",new IndentPrefs(4, 4));
    }

    public void testIf6Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if6.js");
    }

    public void testIf6Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if6.js", null);
    }

    public void testIf7() throws Exception {
        reformatFileContents("testfiles/formatter/if7.js",new IndentPrefs(4, 4));
    }

    public void testIf7Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if7.js");
    }

    public void testIf7Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if7.js", null);
    }

    public void testIf8Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/if8.js", options, ".wrapAlways.formatted");
    }

    public void testIf8Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/if8.js", options, ".wrapNever.formatted");
    }

    public void testIf8IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/if8.js", options, ".wrapIfLong.formatted");
    }

    public void testIf8Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if8.js");
    }

    public void testIf8Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if8.js", null);
    }

    public void testIf9() throws Exception {
        reformatFileContents("testfiles/formatter/if9.js",new IndentPrefs(4, 4));
    }

    public void testIf9Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if9.js");
    }

    public void testIf9Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if9.js", null);
    }

    public void testImportExport1() throws Exception {
        reformatFileContents("testfiles/formatter/importExport1.js",new IndentPrefs(4, 4));
    }

    public void testImportExport1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/importExport1.js");
    }
    
    public void testImportExport2() throws Exception {
        reformatFileContents("testfiles/formatter/importExport2.js",new IndentPrefs(4, 4));
    }

    public void testImportExport2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/importExport2.js");
    }
    
    public void testImportExport3() throws Exception {
        reformatFileContents("testfiles/formatter/importExport3.js",new IndentPrefs(4, 4));
    }

    public void testImportExport3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/importExport3.js");
    }

    public void testImportExport4() throws Exception {
        reformatFileContents("testfiles/formatter/importExport4.js",new IndentPrefs(4, 4));
    }

    public void testImportExport4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/importExport4.js");
    }
    
    public void testImportExport5() throws Exception {
        reformatFileContents("testfiles/formatter/importExport5.js",new IndentPrefs(4, 4));
    }

    public void testImportExport5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/importExport5.js");
    }
    
    public void testImportExport6() throws Exception {
        reformatFileContents("testfiles/formatter/importExport6.js",new IndentPrefs(4, 4));
    }

    public void testImportExport6Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/importExport6.js");
    }
    
    public void testImportExport7() throws Exception {
        reformatFileContents("testfiles/formatter/importExport7.js",new IndentPrefs(4, 4));
    }

    public void testImportExport7Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/importExport7.js");
    }
    
    public void testImportExport8() throws Exception {
        reformatFileContents("testfiles/formatter/importExport8.js",new IndentPrefs(4, 4));
    }

    public void testImportExport8Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/importExport8.js");
    }
    
    public void testImportExport9() throws Exception {
        reformatFileContents("testfiles/formatter/importExport9.js",new IndentPrefs(4, 4));
    }

    public void testImportExport9Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/importExport9.js");
    }
    
    public void testImportExport10() throws Exception {
        reformatFileContents("testfiles/formatter/importExport10.js",new IndentPrefs(4, 4));
    }

    public void testImportExport10Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/importExport10.js");
    }

    public void testDoWhile1() throws Exception {
        reformatFileContents("testfiles/formatter/dowhile1.js",new IndentPrefs(4, 4));
    }

    public void testDoWhile1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/dowhile1.js");
    }

    public void testDoWhile2Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/dowhile2.js", options, ".wrapAlways.formatted");
    }

    public void testDoWhile2Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/dowhile2.js", options, ".wrapNever.formatted");
    }

    public void testDoWhile2IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/dowhile2.js", options, ".wrapIfLong.formatted");
    }

    public void testDoWhile2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/dowhile2.js");
    }

    public void testFor1() throws Exception {
        reformatFileContents("testfiles/formatter/for1.js",new IndentPrefs(4, 4));
    }

    public void testFor1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/for1.js");
    }

    public void testFor1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/for1.js", null);
    }

    public void testFor2() throws Exception {
        reformatFileContents("testfiles/formatter/for2.js",new IndentPrefs(4, 4));
    }

    public void testFor2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/for2.js");
    }

    public void testFor2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/for2.js", null);
    }

    public void testFor3Default() throws Exception {
        reformatFileContents("testfiles/formatter/for3.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFor3Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceWithinForParens, true);
        reformatFileContents("testfiles/formatter/for3.js",
                options, ".inverted.formatted");
    }

    public void testFor3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/for3.js");
    }

    public void testFor3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/for3.js", null);
    }

    public void testFor4Default() throws Exception {
        reformatFileContents("testfiles/formatter/for4.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFor4Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeForLeftBrace, false);
        reformatFileContents("testfiles/formatter/for4.js",
                options, ".inverted.formatted");
    }

    public void testFor4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/for4.js");
    }

    public void testFor4Indented() throws Exception {
        reindentFileContents("testfiles/formatter/for4.js", null);
    }

    public void testFor5Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/for5.js", options, ".wrapAlways.formatted");
    }

    public void testFor5Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/for5.js", options, ".wrapNever.formatted");
    }

    public void testFor5IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/for5.js", options, ".wrapIfLong.formatted");
    }

    public void testFor5IfLongNoSpace() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.spaceBeforeForLeftBrace, false);
        reformatFileContents("testfiles/formatter/for5.js", options, ".wrapIfLongNoSpace.formatted");
    }

    public void testFor5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/for5.js");
    }

    public void testFor5Indented() throws Exception {
        reindentFileContents("testfiles/formatter/for5.js", null);
    }

    public void testFor6Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/for6.js", options, ".wrapAlways.formatted");
    }

    public void testFor6Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/for6.js", options, ".wrapNever.formatted");
    }

    public void testFor6IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/for6.js", options, ".wrapIfLong.formatted");
    }

    public void testFor6Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/for6.js");
    }

    public void testFor6Indented() throws Exception {
        reindentFileContents("testfiles/formatter/for6.js", null);
    }

    public void testWhile1() throws Exception {
        reformatFileContents("testfiles/formatter/while1.js",new IndentPrefs(4, 4));
    }

    public void testWhile1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/while1.js");
    }

    public void testWhile2() throws Exception {
        reformatFileContents("testfiles/formatter/while2.js",new IndentPrefs(4, 4));
    }

    public void testWhile2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/while2.js");
    }

    public void testWhile3Default() throws Exception {
        reformatFileContents("testfiles/formatter/while3.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testWhile3Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceWithinWhileParens, true);
        reformatFileContents("testfiles/formatter/while3.js", options, ".inverted.formatted");
    }

    public void testWhile3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/while3.js");
    }

    public void testWhile4Default() throws Exception {
        reformatFileContents("testfiles/formatter/while4.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testWhile4Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeWhileLeftBrace, false);
        options.put(FmtOptions.spaceBeforeDoLeftBrace, false);
        reformatFileContents("testfiles/formatter/while4.js", options, ".inverted.formatted");
    }

    public void testWhile4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/while4.js");
    }

    public void testWhile5Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/while5.js", options, ".wrapAlways.formatted");
    }

    public void testWhile5Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/while5.js", options, ".wrapNever.formatted");
    }

    public void testWhile5IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/while5.js", options, ".wrapIfLong.formatted");
    }

    public void testWhile5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/while5.js");
    }

    public void testWith1() throws Exception {
        reformatFileContents("testfiles/formatter/with1.js",new IndentPrefs(4, 4));
    }

    public void testWith1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/with1.js");
    }

    public void testWith2Default() throws Exception {
        reformatFileContents("testfiles/formatter/with2.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testWith2Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceWithinWithParens, true);
        reformatFileContents("testfiles/formatter/with2.js", options, ".inverted.formatted");
    }

    public void testWith2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/with2.js");
    }

    public void testWith3Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapWithStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/with3.js", options, ".wrapAlways.formatted");
    }

    public void testWith3Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapWithStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/with3.js", options, ".wrapNever.formatted");
    }

    public void testWith3IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapWithStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/with3.js", options, ".wrapIfLong.formatted");
    }

    public void testWith3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/with3.js");
    }

    public void testFormatting1() throws Exception {
        reformatFileContents("testfiles/formatter/formatting1.js",new IndentPrefs(4, 4));
    }

    public void testFormatting1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/formatting1.js");
    }

    public void testFormatting1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/formatting1.js", null);
    }

    public void testFormatting2() throws Exception {
        reformatFileContents("testfiles/formatter/formatting2.js",new IndentPrefs(4, 4));
    }

    public void testFormatting2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/formatting2.js");
    }

    public void testFormatting2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/formatting2.js", null);
    }

    public void testCommas1() throws Exception {
        reformatFileContents("testfiles/formatter/commas1.js",new IndentPrefs(4, 4));
    }

    public void testCommas1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/commas1.js");
    }

    public void testCommas1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/commas1.js", null);
    }

    public void testCommas2() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceAfterComma, false);
        options.put(FmtOptions.spaceBeforeComma, false);
        reformatFileContents("testfiles/formatter/commas2.js", options);
    }

    public void testCommas2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/commas2.js");
    }

    public void testCommas2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/commas2.js", null);
    }

    public void testCommas3() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceAfterComma, false);
        options.put(FmtOptions.spaceBeforeComma, true);
        reformatFileContents("testfiles/formatter/commas3.js", options);
    }

    public void testCommas3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/commas3.js");
    }

    public void testCommas3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/commas3.js", null);
    }

    public void testDashboard() throws Exception {
        reformatFileContents("testfiles/formatter/dashboard.js",new IndentPrefs(4, 4));
    }

    public void testTabsIndents1Normal() throws Exception {
        reformatFileContents("testfiles/formatter/tabsIndents1.js",
                Collections.<String, Object>emptyMap(), ".normal.formatted");
    }

    public void testTabsIndents1Indented() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatter/tabsIndents1.js",
                options, ".indented.formatted");
    }

    public void testTabsIndents1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/tabsIndents1.js");
    }

    public void testSpaces1Enabled() throws Exception {
        reformatFileContents("testfiles/formatter/spaces1.js",
                Collections.<String, Object>emptyMap(), ".enabled.formatted");
    }

    public void testSpaces1Disabled() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeIfParen, false);
        options.put(FmtOptions.spaceBeforeWhileParen, false);
        options.put(FmtOptions.spaceBeforeForParen, false);
        options.put(FmtOptions.spaceBeforeWithParen, false);
        options.put(FmtOptions.spaceBeforeSwitchParen, false);
        options.put(FmtOptions.spaceBeforeCatchParen, false);
        options.put(FmtOptions.spaceBeforeWhile, false);
        options.put(FmtOptions.spaceBeforeElse, false);
        options.put(FmtOptions.spaceBeforeCatch, false);
        options.put(FmtOptions.spaceBeforeFinally, false);
        reformatFileContents("testfiles/formatter/spaces1.js", options, ".disabled.formatted");
    }

    public void testSpaces1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/spaces1.js");
    }

    public void testOperators1Default() throws Exception {
        reformatFileContents("testfiles/formatter/operators1.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testOperators1Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceAroundAssignOps, false);
        options.put(FmtOptions.spaceAroundBinaryOps, false);
        options.put(FmtOptions.spaceAroundUnaryOps, true);
        options.put(FmtOptions.spaceAroundTernaryOps, false);
        reformatFileContents("testfiles/formatter/operators1.js", options, ".inverted.formatted");
    }

    public void testOperators1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/operators1.js");
    }

    public void testOperators2BinaryWrapAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".binary.wrapAlways.formatted");
    }

    public void testOperators2BinaryWrapNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".binary.wrapNever.formatted");
    }

    public void testOperators2BinaryWrapIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".binary.wrapIfLong.formatted");
    }

    public void testOperators2AssignmentWrapAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapAssignOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".assignment.wrapAlways.formatted");
    }

    public void testOperators2AssignmentWrapNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapAssignOps, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".assignment.wrapNever.formatted");
    }

    public void testOperators2AssignmentWrapIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapAssignOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".assignment.wrapIfLong.formatted");
    }

    public void testOperators2TernaryWrapAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".ternary.wrapAlways.formatted");
    }

    public void testOperators2TernaryWrapNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".ternary.wrapNever.formatted");
    }

    public void testOperators2TernaryWrapIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".ternary.wrapIfLong.formatted");
    }

    public void testOperators2BinaryWrapAfterAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapAfterBinaryOps, true);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".binary.wrapAfterAlways.formatted");
    }

    public void testOperators2BinaryWrapAfterNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapAfterBinaryOps, true);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".binary.wrapAfterNever.formatted");
    }

    public void testOperators2BinaryWrapAfterIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterBinaryOps, true);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".binary.wrapAfterIfLong.formatted");
    }

    public void testOperators2TernaryWrapAfterAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapAfterTernaryOps, true);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".ternary.wrapAfterAlways.formatted");
    }

    public void testOperators2TernaryWrapAfterNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapAfterTernaryOps, true);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".ternary.wrapAfterNever.formatted");
    }

    public void testOperators2TernaryWrapAfterIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterTernaryOps, true);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".ternary.wrapAfterIfLong.formatted");
    }

    public void testOperators2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/operators2.js");
    }

    public void testSpacesSemicolons1Enabled() throws Exception {
        reformatFileContents("testfiles/formatter/spacesSemicolons1.js",
                Collections.<String, Object>emptyMap(), ".enabled.formatted");
    }

    public void testSpacesSemicolons1SemiDisabled() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceAfterSemi, false);
        reformatFileContents("testfiles/formatter/spacesSemicolons1.js",
                options, ".semiDisabled.formatted");
    }

    public void testSpacesSemicolons1WhileDisabled() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeWhile, false);
        reformatFileContents("testfiles/formatter/spacesSemicolons1.js",
                options, ".whileDisabled.formatted");
    }

    public void testSpacesSemicolons1Disabled() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceAfterSemi, false);
        options.put(FmtOptions.spaceBeforeWhile, false);
        reformatFileContents("testfiles/formatter/spacesSemicolons1.js",
                options, ".disabled.formatted");
    }

    public void testSpacesSemicolons1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/spacesSemicolons1.js");
    }

    public void testCatch1Default() throws Exception {
        reformatFileContents("testfiles/formatter/catch1.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testCatch1Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceWithinCatchParens, true);
        reformatFileContents("testfiles/formatter/catch1.js", options, ".inverted.formatted");
    }

    public void testCatch1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/catch1.js");
    }

    public void testCatch1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/catch1.js", null);
    }

    public void testCatch2Default() throws Exception {
        reformatFileContents("testfiles/formatter/catch2.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testCatch2Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeTryLeftBrace, false);
        options.put(FmtOptions.spaceBeforeCatchLeftBrace, false);
        options.put(FmtOptions.spaceBeforeFinallyLeftBrace, false);
        reformatFileContents("testfiles/formatter/catch2.js", options, ".inverted.formatted");
    }

    public void testCatch2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/catch2.js");
    }

    public void testCatch2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/catch2.js", null);
    }
    
    public void testClass1() throws Exception {
        reformatFileContents("testfiles/formatter/class1.js",new IndentPrefs(4, 4));
    }
    
    public void testClass1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/class1.js", null);
    }
    
    public void testClass1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/class1.js");
    }
    
    public void testClass2() throws Exception {
        reformatFileContents("testfiles/formatter/class2.js",new IndentPrefs(4, 4));
    }
    
    public void testClass2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/class2.js");
    }
    
    public void testClass3() throws Exception {
        reformatFileContents("testfiles/formatter/class3.js",new IndentPrefs(4, 4));
    }
    
    public void testClass3AllEmptyLinesRemoval() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.maxPreservedClassLines, 0);
        reformatFileContents("testfiles/formatter/class3.js", options, ".allEmptyLinesRemoval.formatted");
    }

    public void testClass3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/class3.js");
    }
    
    public void testClass4() throws Exception {
        reformatFileContents("testfiles/formatter/class4.js",new IndentPrefs(4, 4));
    }
    
    public void testClass4BraceIndented() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.classDeclBracePlacement, FmtOptions.OBRACE_NEWLINE_INDENTED);
        reformatFileContents("testfiles/formatter/class4.js", options, ".braceIndented.formatted");
    }
    
    public void testClass4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/class4.js");
    }
    
    public void testClass5Default() throws Exception {
        reformatFileContents("testfiles/formatter/class5.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testClass5Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapClassExtends, FmtOptions.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/class5.js", options, ".wrapAlways.formatted");
    }
    
    public void testClass5IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapClassExtends, FmtOptions.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/class5.js", options, ".wrapIfLong.formatted");
    }

    public void testClass5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/class5.js");
    }
    
    public void testClass6() throws Exception {
        reformatFileContents("testfiles/formatter/class6.js",new IndentPrefs(4, 4));
    }
    
    public void testClass6Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/class6.js");
    }
    
    public void testClassProperty1() throws Exception {
        reformatFileContents("testfiles/formatter/classProperty1.js",new IndentPrefs(4, 4));
    }
    
    public void testClassProperty1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/classProperty1.js");
    }
    
    public void testArrow1() throws Exception {
        reformatFileContents("testfiles/formatter/arrow1.js",new IndentPrefs(4, 4));
    }
    
    public void testArrow1Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceWithinMethodDeclParens, true);
        reformatFileContents("testfiles/formatter/arrow1.js", options, ".inverted.formatted");
    }

    public void testArrow1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/arrow1.js");
    }

    public void testArrow2Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/arrow2.js", options, ".wrapAlways.formatted");
    }
    
    public void testArrow2Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrow2.js", options, ".wrapNever.formatted");
    }
    
    public void testArrow2IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/arrow2.js", options, ".wrapIfLong.formatted");
    }

    public void testArrow2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/arrow2.js");
    }
    
    public void testArrow3Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrowOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/arrow3.js", options, ".wrapAlways.formatted");
    }
    
    public void testArrow3Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrowOps, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrow3.js", options, ".wrapNever.formatted");
    }
    
    public void testArrow3IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrowOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/arrow3.js", options, ".wrapIfLong.formatted");
    }

    public void testArrow3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/arrow3.js");
    }

    public void testArrow4() throws Exception {
        reformatFileContents("testfiles/formatter/arrow4.js",new IndentPrefs(4, 4));
    }
    
    public void testArrow4ObjectAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/arrow4.js", options, ".wrapObjectAlways.formatted");
    }

    public void testArrow4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/arrow4.js");
    }

    public void testParentheses1Default() throws Exception {
        reformatFileContents("testfiles/formatter/parentheses1.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testParentheses1Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceWithinParens, true);
        reformatFileContents("testfiles/formatter/parentheses1.js", options, ".inverted.formatted");
    }

    public void testParentheses1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/parentheses1.js");
    }

    public void testArrays1Default() throws Exception {
        reformatFileContents("testfiles/formatter/arrays1.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testArrays1Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceWithinArrayBrackets, true);
        reformatFileContents("testfiles/formatter/arrays1.js", options, ".inverted.formatted");
    }

    public void testArrays1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/arrays1.js");
    }

    public void testArrays1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/arrays1.js", null);
    }

    public void testArrays2Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/arrays2.js", options, ".wrapAlways.formatted");
    }

    public void testArrays2Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrays2.js", options, ".wrapNever.formatted");
    }

    public void testArrays2IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/arrays2.js", options, ".wrapIfLong.formatted");
    }

    public void testArrays2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/arrays2.js");
    }

    public void testArrays2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/arrays2.js", null);
    }

    public void testArrays3() throws Exception {
        reformatFileContents("testfiles/formatter/arrays3.js",new IndentPrefs(4, 4));
    }

    public void testArrays3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/arrays3.js");
    }

    public void testArrays4Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".wrapAlways.formatted");
    }

    public void testArrays4Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".wrapNever.formatted");
    }

    public void testArrays4IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".wrapIfLong.formatted");
    }

    public void testArrays4InitializerOnlyAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".initializerOnlyWrapAlways.formatted");
    }

    public void testArrays4InitializerOnlyNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".initializerOnlyWrapNever.formatted");
    }

    public void testArrays4InitializerOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".initializerOnlyWrapIfLong.formatted");
    }

    public void testArrays4ItemsOnlyAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".itemsOnlyWrapAlways.formatted");
    }

    public void testArrays4ItemsOnlyNever() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".itemsOnlyWrapNever.formatted");
    }

    public void testArrays4ItemsOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".itemsOnlyWrapIfLong.formatted");
    }

    public void testArrays5Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/arrays5.js", options, ".wrapAlways.formatted");
    }

    public void testArrays5Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrays5.js", options, ".wrapNever.formatted");
    }

    public void testArrays5IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/arrays5.js", options, ".wrapIfLong.formatted");
    }

    public void testArrays6() throws Exception {
        reformatFileContents("testfiles/formatter/arrays6.js",new IndentPrefs(4, 4));
    }
    
    public void testArrays6NoEmptyLinesRemoval() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.maxPreservedArrayLines, 5);
        reformatFileContents("testfiles/formatter/arrays6.js", options, ".noEmptyLinesRemoval.formatted");
    }
    
    public void testArrays6AllEmptyLinesRemoval() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.maxPreservedArrayLines, 0);
        reformatFileContents("testfiles/formatter/arrays6.js", options, ".allEmptyLinesRemoval.formatted");
    }
    
    public void testArrays6Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/arrays6.js");
    }

    public void testPartialFormat1() throws Exception {
        reformatFileContents("testfiles/formatter/partialFormat1.js", Collections.<String, Object>emptyMap());
    }

    public void testPartialFormat2() throws Exception {
        reformatFileContents("testfiles/formatter/partialFormat2.js", Collections.<String, Object>emptyMap());
    }

    public void testPartialFormat3() throws Exception {
        reformatFileContents("testfiles/formatter/partialFormat3.js", Collections.<String, Object>emptyMap());
    }

    public void testPartialFormat4() throws Exception {
        reformatFileContents("testfiles/formatter/partialFormat4.js", Collections.<String, Object>emptyMap());
    }

    public void testPartialFormat5() throws Exception {
        reformatFileContents("testfiles/formatter/partialFormat5.js", Collections.<String, Object>emptyMap());
    }

    public void testPartialFormat6() throws Exception {
        reformatFileContents("testfiles/formatter/partialFormat6.js", Collections.<String, Object>emptyMap());
    }

    public void testTemplates1() throws Exception {
        reformatFileContents("testfiles/formatter/templates1.js",new IndentPrefs(4, 4));
    }

    public void testTemplates1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/templates1.js");
    }
    
    public void testTemplates2() throws Exception {
        reformatFileContents("testfiles/formatter/templates2.js",new IndentPrefs(4, 4));
    }

    public void testTemplates2ObjectOnlyAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/templates2.js", options, ".objectOnlyWrapAlways.formatted");
    }
    
    public void testTemplates2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/templates2.js");
    }
    
    public void testTemplates3() throws Exception {
        reformatFileContents("testfiles/formatter/templates3.js",new IndentPrefs(4, 4));
    }

    public void testTemplates3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/templates3.js");
    }
    
    public void testTemplates4() throws Exception {
        reformatFileContents("testfiles/formatter/templates4.js",new IndentPrefs(4, 4));
    }

    public void testTemplates4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/templates4.js");
    }
    
    public void testTernary1() throws Exception {
        reformatFileContents("testfiles/formatter/ternary1.js",new IndentPrefs(4, 4));
    }

    public void testTernary1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/ternary1.js");
    }

    public void testTernary2() throws Exception {
        reformatFileContents("testfiles/formatter/ternary2.js",new IndentPrefs(4, 4));
    }

    public void testTernary2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/ternary2.js");
    }

    public void testVar1() throws Exception {
        reformatFileContents("testfiles/formatter/var1.js",new IndentPrefs(4, 4));
    }

    public void testVar1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/var1.js");
    }

    public void testSpread1Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/spread1.js", options, ".wrapAlways.formatted");
    }

    public void testSpread1Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/spread1.js", options, ".wrapNever.formatted");
    }

    public void testSpread1IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/spread1.js", options, ".wrapIfLong.formatted");
    }
    
    public void testStatements1Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/statements1.js", options, ".wrapAlways.formatted");
    }

    public void testStatements1Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/statements1.js", options, ".wrapNever.formatted");
    }

    public void testStatements1IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/statements1.js", options, ".wrapIfLong.formatted");
    }

    public void testVar2Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapVariables, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/var2.js", options, ".wrapAlways.formatted");
    }

    public void testVar2Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapVariables, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/var2.js", options, ".wrapNever.formatted");
    }

    public void testVar2IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapVariables, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/var2.js", options, ".wrapIfLong.formatted");
    }
    
    public void testVar2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/var2.js");
    }

    public void testVar3Always() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapVariables, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/var3.js", options, ".wrapAlways.formatted");
    }

    public void testVar3Never() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapVariables, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/var3.js", options, ".wrapNever.formatted");
    }

    public void testVar3IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapVariables, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/var3.js", options, ".wrapIfLong.formatted");
    }
    
    public void testVar3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/var3.js");
    }

    public void testVar4() throws Exception {
        reformatFileContents("testfiles/formatter/var4.js",new IndentPrefs(4, 4));
    }

    public void testVar4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/var4.js");
    }
    
    public void testImportsWithoutSemi() throws Exception {
        reformatFileContents("testfiles/formatter/importsWithoutSemi.js",new IndentPrefs(4, 4));
    }
    
    public void testImportsWithoutSemiTokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/importsWithoutSemi.js");
    }
    
    public void testCodeTemplate1() throws Exception {
        reformatFileContents("testfiles/formatter/codeTemplate1.js",
                Collections.<String, Object>emptyMap(), null, true);
    }

    public void testIssue189745() throws Exception {
        reformatFileContents("testfiles/formatter/issue189745.js",new IndentPrefs(4, 4));
    }

    public void testIssue189745Indented() throws Exception {
        reindentFileContents("testfiles/formatter/issue189745.js", null);
    }

    public void testIssue218090() throws Exception {
        reformatFileContents("testfiles/formatter/issue218090.js",new IndentPrefs(4, 4));
    }

    public void testIssue218328() throws Exception {
        reformatFileContents("testfiles/formatter/issue218328.js",new IndentPrefs(4, 4));
    }

    public void testIssue219046() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/issue219046.js", options);
    }

    public void testIssue220920() throws Exception {
        reformatFileContents("testfiles/formatter/issue220920.js",new IndentPrefs(4, 4));
    }

    public void testIssue220920Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/issue220920.js");
    }

    public void testIssue221293() throws Exception {
        reformatFileContents("testfiles/formatter/issue221293.js",new IndentPrefs(4, 4));
    }

    public void testIssue221495() throws Exception {
        reformatFileContents("testfiles/formatter/issue221495.js",new IndentPrefs(4, 4));
    }

    public void testIssue224246() throws Exception {
        reformatFileContents("testfiles/formatter/issue224246.js",new IndentPrefs(4, 4));
    }

    public void testIssue225654Partial() throws Exception {
        reformatFileContents("testfiles/formatter/issue225654_partial.js", Collections.<String, Object>emptyMap());
    }

    public void testIssue225654Full() throws Exception {
        reformatFileContents("testfiles/formatter/issue225654_full.js",new IndentPrefs(4, 4));
    }

    public void testIssue226282_1() throws Exception {
        reformatFileContents("testfiles/formatter/issue226282_1.js",new IndentPrefs(4, 4));
    }
    
    public void testIssue226282_2() throws Exception {
        reformatFileContents("testfiles/formatter/issue226282_2.js",new IndentPrefs(4, 4));
    }

    public void testIssue228919() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.spaceBeforeElseLeftBrace, false);
        reformatFileContents("testfiles/formatter/issue228919.js", options);
    }

    public void testIssue231163() throws Exception {
        reformatFileContents("testfiles/formatter/issue231163.js",new IndentPrefs(4, 4));
    }

    public void testIssue210134() throws Exception {
        reformatFileContents("testfiles/formatter/issue210134.js",new IndentPrefs(4, 4));
    }

    public void testIssue231918() throws Exception {
        reformatFileContents("testfiles/formatter/issue231918.js",new IndentPrefs(4, 4));
    }

    public void testIssue232374() throws Exception {
        reformatFileContents("testfiles/formatter/issue232374.js",new IndentPrefs(4, 4));
    }

    public void testIssue230007() throws Exception {
        reformatFileContents("testfiles/formatter/issue230007.js",new IndentPrefs(4, 4));
    }

    public void testIssue234244() throws Exception {
        reformatFileContents("testfiles/formatter/issue234244.js",new IndentPrefs(4, 4));
    }

    public void testIssue234385() throws Exception {
        format("this.", "this.", new IndentPrefs(4, 4));
    }

    public void testIssue228716() throws Exception {
        format("var o = {};", "var o = {};", new IndentPrefs(4, 4));
    }
    
    public void testIssue240402() throws Exception {
        reformatFileContents("testfiles/formatter/issue240402.js",new IndentPrefs(4, 4));
    }

    public void testIssue244983NoExpand() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.tabSize, 2);
        options.put(FmtOptions.indentSize, 2);
        options.put(FmtOptions.expandTabToSpaces, false);
        options.put(FmtOptions.continuationIndentSize, 0);
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterBinaryOps, true);
        options.put(FmtOptions.rightMargin, 100);
        reformatFileContents("testfiles/formatter/issue244983.js",
                options, ".noexpand.formatted");
    }

    public void testIssue244983Expand() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.tabSize, 2);
        options.put(FmtOptions.indentSize, 2);
        options.put(FmtOptions.expandTabToSpaces, true);
        options.put(FmtOptions.continuationIndentSize, 0);
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterBinaryOps, true);
        options.put(FmtOptions.rightMargin, 100);
        reformatFileContents("testfiles/formatter/issue244983.js",
                options, ".expand.formatted");
    }

    public void testIssue250557() throws Exception {
        reformatFileContents("testfiles/formatter/issue250557.js", new IndentPrefs(4, 4));
    }

    public void testIssue257144() throws Exception {
        reformatFileContents("testfiles/formatter/issue257144.js", new IndentPrefs(4, 4));
    }

    public void testIssue257144Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/issue257144.js");
    }
    
    public void testIssue258858() throws Exception {
        reformatFileContents("testfiles/formatter/issue258858.js", new IndentPrefs(4, 4));
    }
    
    public void testIssue258858Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/issue258858.js");
    }
    
    public void testIssue258858Stable() throws Exception {
        reformatFileContents("testfiles/formatter/issue258858_stable.js", new IndentPrefs(4, 4));
    }
    
    public void testIssue258858StableTokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/issue258858_stable.js");
    }
    
    public void testIssue258872() throws Exception {
        reformatFileContents("testfiles/formatter/issue258872.js", new IndentPrefs(4, 4));
    }
    
    public void testIssue258872Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/issue258872.js");
    }

    // braces formatting tests
    public void testBracesSameLine() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.functionDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.withBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatter/bracesFormat.js", options, ".sameLine.formatted");
    }

    public void testBracesNewLine() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.functionDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.withBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatter/bracesFormat.js", options, ".newLine.formatted");
    }

    public void testBracesNewLineIndented() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.functionDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.withBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatter/bracesFormat.js", options, ".newLineIndented.formatted");
    }

    public void testIssue227007WithContinuation() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.objectLiteralContinuation, true);
        reformatFileContents("testfiles/formatter/issue227007.js", options, ".continuation.formatted");
    }

    public void testIssue227007WithouContinuation() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.objectLiteralContinuation, false);
        reformatFileContents("testfiles/formatter/issue227007.js", options, ".noContinuation.formatted");
    }

    // alignment options tests
    public void testKeywordAlignmentSameLine() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.placeElseOnNewLine, false);
        options.put(FmtOptions.placeWhileOnNewLine, false);
        options.put(FmtOptions.placeCatchOnNewLine, false);
        options.put(FmtOptions.placeFinallyOnNewLine, false);
        reformatFileContents("testfiles/formatter/keywordPlacement.js", options, ".sameLine.formatted");
    }

    public void testKeywordAlignmentNewLine() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.placeElseOnNewLine, true);
        options.put(FmtOptions.placeWhileOnNewLine, true);
        options.put(FmtOptions.placeCatchOnNewLine, true);
        options.put(FmtOptions.placeFinallyOnNewLine, true);
        reformatFileContents("testfiles/formatter/keywordPlacement.js", options, ".newLine.formatted");
    }

    // test from original formatter

    public void testSemi01() throws Exception {
        format(
                "var p; p = 'hello';",
                "var p;\n" +
                "p = 'hello';", null
                );
    }

    public void testSemi02() throws Exception {
        format(
                "var p;                           p = 'hello';",
                "var p;\n" +
                "p = 'hello';", null
                );
    }

    public void testSemi03() throws Exception {
        format(
                "var p;p = 'hello';",
                "var p;\n" +
                "p = 'hello';", null
                );
    }

    public void testSemi04() throws Exception {
        format(
                "var p; p = getName(); p = stripName(p);",
                "var p;\n" +
                "p = getName();\n" +
                "p = stripName(p);", null
                );
    }

    public void testSemi05() throws Exception {
        format(
                "var p; for(var i = 0, l = o.length; i < l; i++) {             createDom(o[i], el);   p = true;} p = stripName(p);",
                "var p;\n" +
                "for (var i = 0, l = o.length; i < l; i++) {\n" +
                "    createDom(o[i], el);\n" +
                "    p = true;\n" +
                "}\n" +
                "p = stripName(p);", null
                );
    }

    public void testSemi06() throws Exception {
        format(
                "if (a == b) { a=c;\n" +
                "    } else if (c == b) { v=d;}",

                "if (a == b) {\n" +
                "    a = c;\n" +
                "} else if (c == b) {\n" +
                "    v = d;\n" +
                "}", null);
    }

    public void testSemi07() throws Exception {
        format(
                "var test = function() { a = b; };",

                "var test = function () {\n" +
                "    a = b;\n" +
                "};", null);
    }

    public void testSemi08() throws Exception {
        format(
                "Spry.forwards = 1; // const\n" +
                "Spry.backwards = 2; // const\n",

                "Spry.forwards = 1; // const\n" +
                "Spry.backwards = 2; // const\n", null);
    }

    public void testCommentAtTheEdnOfLine() throws Exception {
        format (
                "for(var i = 0, l = o.length; i < l; i++) { // some comment \ncreateDom(o[i], el);  p = true;       } //comment2\n p = stripName(p);",
                "for (var i = 0, l = o.length; i < l; i++) { // some comment \n" +
                "    createDom(o[i], el);\n" +
                "    p = true;\n" +
                "} //comment2\n" +
                "p = stripName(p);", null);
    }
}
