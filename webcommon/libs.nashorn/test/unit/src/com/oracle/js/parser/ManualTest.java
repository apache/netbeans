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

package com.oracle.js.parser;

import com.oracle.js.parser.ErrorManager.PrintWriterErrorManager;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.LexicalContext;

public class ManualTest {

    public static void main(String[] args) {
//        Source source = Source.sourceFor("dummy.js", "function hallo() {return 'Welt';}");
//        Source source = Source.sourceFor("dummy.js", "class hallo { constructor() {} dummy() {return 'Welt'} async dummy2() {} #height = 0; #internal() {} async #internal2() {} }");
//        Source source = Source.sourceFor("dummy.js", "async function hallo() {}; async function hallo2() {let a = await hallo(); return a;}");
//        Source source = Source.sourceFor("dummy.js", "var a = {'b': 1, d, ...c}");
//        Source source = Source.sourceFor("dummy.js", "var a = [1, 2, ...b]");
//        Source source = Source.sourceFor("dummy.js", "var a = 1 ** 2");
//        Source source = Source.sourceFor("dummy.js", "for await(const line of readLines(filePath)) { console.log(line); }");
//        Source source = Source.sourceFor("dummy.js", "async function hallo() { return 'Welt';}");
//        Source source = Source.sourceFor("dummy.js", "async (a,b) => { return 'Welt';}");
//        Source source = Source.sourceFor("dummy.js", "var a = import('test');");
//        Source source = Source.sourceFor("dummy.js", "try {} catch (e) {}");
//        Source source = Source.sourceFor("dummy.js", "function a() {}; async function b() {}; class x { y(){} async z(){} }");
        Source source = Source.sourceFor("dummy.js", "class Polygon {\n" +
"  constructor(height, width) {\n" +
"    this.height = height;\n" +
"    this.width = width;\n" +
"  }\n" +
"} ");
        ScriptEnvironment.Builder builder = ScriptEnvironment.builder();
        Parser parser = new Parser(
                builder.emptyStatements(true).ecmacriptEdition(13).jsx(true).build(),
                source,
                new PrintWriterErrorManager());
        FunctionNode fn = parser.parse();
        DumpingVisitor dv = new DumpingVisitor(new LexicalContext());
        fn.accept(dv);
    }

}
