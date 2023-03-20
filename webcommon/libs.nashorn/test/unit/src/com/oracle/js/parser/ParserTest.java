/*
 * Copyright (c) 2022, Matthias Bläsing. All rights reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oracle.js.parser;

import com.oracle.js.parser.ir.FunctionNode;
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

    private Predicate<Node> functionNodeWithName(String name) {
        return n -> n instanceof FunctionNode && name.equals(((FunctionNode) n).getName());
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
            dumpTree(fn);
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
