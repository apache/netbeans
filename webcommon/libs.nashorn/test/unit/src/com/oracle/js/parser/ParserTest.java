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

import com.oracle.js.parser.ir.ClassNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.LexicalContext;
import com.oracle.js.parser.ir.Node;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.function.Predicate;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParserTest {

    @Test
    public void testParseImportFunction() {
        assertParses("var a = import('test');");
        assertParses("(async () => {\n"
                + "  if (somethingIsTrue) {\n"
                + "    const { default: myDefault, foo, bar } = await import('/modules/my-module.js');\n"
                + "  }\n"
                + "})();");
        assertParsesNot(10, "var a = import('test');");
    }

    @Test
    public void testBasicClass() {
        assertParses("class Rectangle {\n"
                + "  constructor(height, width) {\n"
                + "    this.name = 'Rectangle';\n"
                + "    this.height = height;   \n"
                + "    this.width = width; \n"
                + "  }\n"
                + "}\n"
                + "\n"
                + "class FilledRectangle extends Rectangle {\n"
                + "  constructor(height, width, color) {\n"
                + "    super(height, width);\n"
                + "    this.name = 'Filled rectangle';\n"
                + "    this.color = color;  \n"
                + "  }\n"
                + "}");
    }

    @Test
    public void testGetterInClass() {
        assertParses("const Rectangle = class {\n"
                + "  constructor(height, width) {\n"
                + "    this.height = height;\n"
                + "    this.width = width;\n"
                + "  }\n"
                + "\n"
                + "  get area() {\n"
                + "    return this.calcArea();"
                + "  }\n"
                + "\n"
                + "  calcArea() {\n"
                + "    return this.height * this.width;\n"
                + "  }\n"
                + "}");
    }

    @Test
    public void testStaticMembers() {
        assertParses("class Point {\n"
                + "  constructor(x, y) {\n"
                + "    this.x = x;\n"
                + "    this.y = y;\n"
                + "  }\n"
                + "\n"
                + "  static displayName = \"Point\";\n"
                + "  static distance(a, b) {\n"
                + "    const dx = a.x - b.x;\n"
                + "    const dy = a.y - b.y;\n"
                + "\n"
                + "    return Math.hypot(dx, dy);\n"
                + "  }\n"
                + "}");
    }

    @Test
    public void testPrivateMember() {
        assertParses("class T {"
                + "#X;"
                + "#Y() {}"
                + "get #Z() { return 'Z';}"
                + "}");
        assertParses("class Rectangle {\n"
                + "  #height = 0;\n"
                + "  #width;\n"
                + "  constructor(height, width) {\n"
                + "    this.#height = height;\n"
                + "    this.#width = width;\n"
                + "  }\n"
                + "\n"
                + "  #implCalcArea() {\n"
                + "    return this.#height * this.#width;"
                + "  }\n"
                + "}");
    }

    @Test
    public void testConstructor() {
        assertParses("""
            class T {
                constructor() {}
            }
        """);
        // Parser should reject multiple constructors
        assertParsesNot(Integer.MAX_VALUE, """
            class T {
                constructor() {}
                constructor() {}
            }
        """);
        // Parser should reject private constructors
        assertParsesNot(Integer.MAX_VALUE, """
            class T {
                #constructor() {}
            }
        """);
        // Parser should reject fields with name constructor
        assertParsesNot(Integer.MAX_VALUE, """
            class T {
                constructor = X
            }
        """);
        // Parser should reject generator function as constructor
        assertParsesNot(Integer.MAX_VALUE, """
            class T {
                *constructor() {}
            }
        """);
        // Parser should reject getter function as constructor
        assertParsesNot(Integer.MAX_VALUE, """
            class T {
                get constructor() {}
            }
        """);
        // Parser should reject getter function as constructor
        assertParsesNot(Integer.MAX_VALUE, """
            class T {
                set constructor(value) {}
            }
        """);
    }

    @Test
    public void testRejectDuplicatePrivateMembers() {
        assertParses(Integer.MAX_VALUE, """
            class T {
                get #X() {};
                set #X(value) {};
            }
        """);
        // Plain field and getter should be rejected
        assertParsesNot(Integer.MAX_VALUE, """
            class T {
                #X;
                get #X() {};
            }
        """);
        assertParsesNot(Integer.MAX_VALUE, """
            class T {
                #X = 1;
                get #X() {};
            }
        """);
        // Plain field and setter should be rejected
        assertParsesNot(Integer.MAX_VALUE, """
            class T {
                #X;
                set #X() {};
            }
        """);
        assertParsesNot(Integer.MAX_VALUE, """
            class T {
                #X = 1;
                set #X() {};
            }
        """);
        // Plain field and method should be rejected
        assertParsesNot(Integer.MAX_VALUE, """
            class T {
                #X;
                #X() {};
            }
        """);
        // Duplicate method should be rejected
        assertParsesNot(Integer.MAX_VALUE, """
            class T {
                #X() {};
                #X() {};
            }
        """);
        // Duplicate fields should be rejected
        assertParsesNot(Integer.MAX_VALUE, """
            class T {
                #X;
                #X;
            }
        """);
        // Mixed static declarations should be rejected
       assertParsesNot(Integer.MAX_VALUE, """
            class T {
                get #X() {};
                static set #X(value) {};
            }
        """);
    }

    @Test
    public void testNullishCoalesce() {
        assertParses("let a = b ?? 3");
        assertParsesNot( 10, "let a = b ?? 3");
    }

    @Test
    public void testShortcircuritAssignment() {
        assertParses("let a = 1; a &&= b");
        assertParses("let a = 1; a ||= b");
        assertParses("let a = 1; a ??= b");
        assertParsesNot( 11, "let a = 1; a &&= b");
        assertParsesNot( 11, "let a = 1; a ||= b");
        assertParsesNot( 11, "let a = 1; a ??= b");
    }

    @Test
    public void testNumericLiteralSeparator() {
        assertParses("let a = 1000000");
        assertParses(11, "let a = 1000000");
        assertParses("let a = 1_000_000");
        assertParsesNot(11, "let a = 1_000_000");
        assertParses("let a = 1__000__000");
        assertParsesNot(11, "let a = 1__000__000");
    }

    @Test
    public void testBigInt() {
        assertParses("let a = 1000000n");
        assertParsesNot(10, "let a = 1000000n");
    }

    @Test
    public void testOptionalChaining() {
        assertParses("let a = b.c.d");
        assertParses("let a = b?.c?.d");
        assertParsesNot(10, "let a = b?.c?.d");
    }

    @Test
    public void testOptionalIndexing() {
        assertParses("let a = b[1][2]");
        assertParses("let a = b['c']['d']");
        assertParses("let a = b[c][d]");
        assertParses("let a = b?.[1]?.[2]");
        assertParses("let a = b?.['c']?.['d']");
        assertParses("let a = b?.[c]?.[d]");
        assertParsesNot(10, "let a = b?.[1]?.[2]");
        assertParsesNot(10, "let a = b?.['c']?.['d']");
        assertParsesNot(10, "let a = b?.[c]?.[d]");
    }

    @Test
    public void testOptionalCalling() {
        assertParses("b()");
        assertParses("b?.()");
        assertParsesNot(10, "b?.()");
    }

    @Test
    public void testImportExport() {
        assertParses(Integer.MAX_VALUE, true, false, "import v from 'mod';");
        assertParses(Integer.MAX_VALUE, true, false, "import * as ns from 'mod';");
        assertParses(Integer.MAX_VALUE, true, false, "import {x} from 'mod';");
        assertParses(Integer.MAX_VALUE, true, false, "import {x as v} from 'mod';");
        assertParses(Integer.MAX_VALUE, true, false, "import 'mod';");
        assertParses(Integer.MAX_VALUE, true, false, "export var v;");
        assertParses(Integer.MAX_VALUE, true, false, "export default function f(){};");
        assertParses(Integer.MAX_VALUE, true, false, "export default function(){};");
        assertParses(Integer.MAX_VALUE, true, false, "export default 42;");
        assertParses(Integer.MAX_VALUE, true, false, "export {x};");
        assertParses(Integer.MAX_VALUE, true, false, "export {x as v};");
        assertParses(Integer.MAX_VALUE, true, false, "export {x} from 'mod';");
        assertParses(Integer.MAX_VALUE, true, false, "export {x as v} from 'mod';");
        assertParses(Integer.MAX_VALUE, true, false, "export * from 'mod';");
        assertParses(Integer.MAX_VALUE, true, false, "export * as ns from 'mod';");
        assertParses(10, true, true, "export * as ns from 'mod';");
    }

    @Test
    public void testOptionalCatchBinding() {
        assertParses("try {} catch (e) {}");
        assertParses("try {} catch {}");
        assertParsesNot(9, "try {} catch {}");
    }

    @Test
    public void testFailParsingRedeclaration() {
        assertParses(11, "var a = 1; var a = 1;");
        assertParses(11, "var a = 1; function b() { let a = 1; }");
        assertParses(11, "let a = 1; function b() { let a = 1; }");
        assertParses(11, "const a = 1; function b() { let a = 1; }");
        assertParsesNot(11, "var a = 1; let a = 1;");
        assertParsesNot(11, "var a = 1; const a = 1;");
        assertParsesNot(11, "let a = 1; let a = 1;");
        assertParsesNot(11, "const a = 1; const a = 1;");
        assertParsesNot(11, "class a {}; let a = 'test'");
        assertParsesNot(11, "class a {}; const a = 'test'");
        assertParsesNot(11, "function a() {}; let a = 'test'");
        assertParsesNot(11, "function a() {}; const a = 'test'");
    }

    @Test
    public void testTryCatchWithoutVariable() {
        assertParses("try {} catch {}");
        assertParsesNot(9, "try {} catch {}");
    }

    @Test
    public void testParseEvalInMethodInModule() {
        assertParses(Integer.MAX_VALUE, true, false, "import $ from 'jquery'; class e { dummy(x) {eval(x);} }");
    }

    @Test
    public void testAsyncMethod() {
        FunctionNode programm = parse(13, false, "function a() {}; async function b() {}; class x { y(){} async z(){} }");

        FunctionNode a = findNode(programm, functionNodeWithName("a"), FunctionNode.class);
        FunctionNode b = findNode(programm, functionNodeWithName("b"), FunctionNode.class);
        FunctionNode y = findNode(programm, functionNodeWithName("y"), FunctionNode.class);
        FunctionNode z = findNode(programm, functionNodeWithName("z"), FunctionNode.class);
        assertFalse(a.isAsync());
        assertTrue(b.isAsync());
        assertFalse(y.isAsync());
        assertTrue(z.isAsync());
        assertFalse(a.isMethod());
        assertFalse(b.isMethod());
        assertTrue(y.isMethod());
        assertTrue(z.isMethod());
    }

    @Test
    public void testClassElements() {
        assertParses(11, "class demoClass { static staticMethod(){} instanceMethod(){}}");
        assertParses(13, "class demoClass { a = 1; #b = 2; static c = 3; static #d = 4}");
        assertParses(13, "class demoClass { static a = 1; static { a  = 2; } }");
    }

    @Test
    public void testFor() {
        assertParses(13, "async function dummy() { for await (const num of foo()) {}}");
    }

    @Test
    public void testJsx() {
        // JSX Fragments are parsed as JSX Elements without name and attributes
        assertParses(13, "const a = <></>;");
        assertParses(13, "const a = <table style={{ border: '2px solid black', borderRadius: '.5em'}}></table>");
        assertParses(13, "const a = <table>{/* Test */ /* Test */ /* Test */}{ a = 3 }</table>");
    }

    @Test
    public void testBindingPattern() {
        assertParses(13, "const buildTerserOptions = ({\n"
                + "  ecma\n"
                + "} = {}) => ({\n"
                + "  ecma\n"
                + "});");
    }

    @Test
    public void testGeneratedConstructorMethod() {
        FunctionNode programm = parse(13, false, "class Demo {}");

        FunctionNode generatedConstructor = findNode(programm, functionNodeWithName("Demo"), FunctionNode.class);
        assertNotNull(generatedConstructor);
        assertTrue(generatedConstructor.isGenerated());

        programm = parse(13, false, "class Demo {constructor() {}}");

        FunctionNode definedConstructor = findNode(programm, functionNodeWithName("constructor"), FunctionNode.class);
        assertNotNull(definedConstructor);
        assertFalse(definedConstructor.isGenerated());

        programm = parse(13, false, "class Demo {constructor() {}} class Demo2 extends Demo {}");

        ClassNode demoClassDemo = findNode(programm, classNodeWithName("Demo"), ClassNode.class);
        ClassNode demoClass2Demo = findNode(programm, classNodeWithName("Demo2"), ClassNode.class);

        definedConstructor = findNode(demoClassDemo, functionNodeWithName("constructor"), FunctionNode.class);
        assertNotNull(definedConstructor);
        assertFalse(definedConstructor.isGenerated());
        definedConstructor = findNode(demoClass2Demo, functionNodeWithName("Demo2"), FunctionNode.class);
        assertNotNull(definedConstructor);
        assertTrue(definedConstructor.isGenerated());
    }

    @Test
    public void testMetaProperties() {
        // import.meta and new.target are declared meta properties provided by
        // the runtime. The parser must be able to report them, even if they are
        // based on keywords
        assertParses("import.meta");
        assertParses("function() { new.target }");

        // Other variations should be rejected by the parser
        assertParsesNot(Integer.MAX_VALUE, "import.dummy");
        assertParsesNot(Integer.MAX_VALUE, "function() { new.dummy }");

        FunctionNode programm1 = parse(Integer.MAX_VALUE, false, "import.meta");
        FunctionNode programm2 = parse(Integer.MAX_VALUE, false, "function() { new.target }");
        // The two special properties are reported as identifiers with an
        // embedded period
        assertNotNull(findNode(programm1, n -> n instanceof IdentNode && "import".equals(((IdentNode) n).getName()), IdentNode.class));
        assertNotNull(findNode(programm2, n -> n instanceof IdentNode && "new".equals(((IdentNode) n).getName()), IdentNode.class));
    }

    @Test
    public void testTopLevelAwait() {
        // Validate top-level await is support for ES13 modules
        assertParses(13, true, false, "await Promise.resolve(1);");
        // Validate top-level await is not support for ES12 modules
        assertParses(12, true, true, "await Promise.resolve(1);");
        // Validate top-level await is not supported for ES13 non-modules
        assertParses(13, false, true, "await Promise.resolve(1);");
        // Validate await in async function can still be parsed
        assertParses(12, false, false, "async function dummy() {await Promise.resolve(1);}");
        // Validate await in synchronous function fails to parse
        assertParses(12, false, true, "function dummy() {await Promise.resolve(1);}");
    }

    @Test
    public void testAsyncGenerator() {
        assertParsesNot(8, "class Demo {\nasync * generator() {\nyield i;\nyield i + 10;\n}\n}");
        assertParses("class Demo {\nasync * generator() {\nyield 1;\nyield 1 + 10;\n}\n}");
        assertParses("class Demo {\nasync * generator(i) {\nyield i;\nyield i + 10;\n}\n}");
        assertParsesNot(8, "class Demo {\nasync * [Symbol.asyncIterator]() {\nyield i;\nyield i + 10;\n}\n}");
        assertParses("class Demo {\nasync * [Symbol.asyncIterator]() {\nyield 1;\nyield 1 + 10;\n}\n}");
        assertParses("class Demo {\nasync * [Symbol.asyncIterator](i) {\nyield i;\nyield i + 10;\n}\n}");
        assertParses("class Demo {\nasync * [Symbol.asyncIterator]() {\nyield await this.next();\n}\n}");
    }

    private Predicate<Node> functionNodeWithName(String name) {
        return n -> n instanceof FunctionNode && name.equals(((FunctionNode) n).getName());
    }

    private Predicate<Node> classNodeWithName(String name) {
        return n -> n instanceof ClassNode 
                && ((ClassNode) n).getIdent() != null
                && name.equals(((ClassNode) n).getIdent().getName());
    }

    public void assertParses(String script) {
        assertParses(Integer.MAX_VALUE, false, false, script);
    }

    public void assertParses(int ecmascriptVersion, String script) {
        assertParses(ecmascriptVersion, false, false, script);
    }

    public void assertParsesNot(int ecmascriptVersion, String script) {
        assertParses(ecmascriptVersion, false, true, script);
    }

    public void assertParses(int emcascriptVersion, boolean module, boolean invert, String script) {
        FunctionNode fn = null;

        try {
            fn = parse(emcascriptVersion, module, script);
        } catch (ParsingFailedException ex) {}

        if (invert) {
            assertNull("Parsing should have failed", fn);
        } else {
            assertNotNull("Parser did not yield result", fn);
        }
    }

    private FunctionNode parse(int emcascriptVersion, boolean module, String script) {
        Source source = Source.sourceFor("dummy.js", script);
        ScriptEnvironment.Builder builder = ScriptEnvironment.builder();
        ErrorManager em = new ErrorManager.PrintWriterErrorManager() {
            @Override
            public void error(ParserException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                this.error(e.getMessage() + "\n\n" + sw.toString());
            }
        };
        Parser parser = new Parser(
                builder.emptyStatements(true)
                        .ecmacriptEdition(emcascriptVersion)
                        .jsx(true)
                        .build(),
                source,
                em
        );

        FunctionNode fn;
        if (module) {
            fn = parser.parseModule("dummy.js");
        } else {
            fn = parser.parse();
        }

        if(em.hasErrors()) {
            throw new ParsingFailedException();
        }
        return fn;
    }

    private void dumpTree(Node fn) {
        DumpingVisitor dv = new DumpingVisitor(new LexicalContext());
        fn.accept(dv);
    }

    private <T extends Node> T findNode(Node base, Predicate<Node> predicate, Class<T> resultType) {
        T[] result = (T[]) Array.newInstance(resultType, 1);
        LexicalContext lc = new LexicalContext();
        NodeVisitor nv = new NodeVisitor(lc) {
            @Override
            protected boolean enterDefault(Node node) {
                if(predicate.test(node)) {
                    result[0] = resultType.cast(node);
                    return false;
                }
                return true;
            }
        };
        base.accept(nv);
        return result[0];
    }

    private static class ParsingFailedException extends IllegalStateException {}
}
