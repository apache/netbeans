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
package org.netbeans.api.scripting;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JavaScriptEnginesTest {
    @Parameterized.Parameters(name = "{0}@{1}")
    public static Object[][] engines() {
        List<Object[]> arr = new ArrayList<>();
        for (ScriptEngineFactory f : Scripting.createManager().getEngineFactories()) {
            final String name = f.getEngineName();
            if (
                f.getMimeTypes().contains("text/javascript") ||
                name.contains("Nashorn")
            ) {
                arr.add(new Object[] { name, false, f.getScriptEngine() });
            }
        }
        for (ScriptEngineFactory f : Scripting.newBuilder().allowAllAccess(true).build().getEngineFactories()) {
            final String name = f.getEngineName();
            if (
                f.getMimeTypes().contains("text/javascript") ||
                name.contains("Nashorn")
            ) {
                arr.add(new Object[] { name, true, f.getScriptEngine() });
            }
        }
        return arr.toArray(new Object[0][]);
    }

    private final String engineName;
    private final ScriptEngine engine;
    private final boolean allowAllAccess;

    public JavaScriptEnginesTest(String engineName, boolean allowAllAccess, ScriptEngine engine) {
        this.engineName = engineName;
        this.allowAllAccess = allowAllAccess;
        this.engine = engine;
    }

    private Invocable inv() {
        assertTrue("Engines are invocable: " + engine, engine instanceof Invocable);
        return (Invocable) engine;
    }

    @Test
    public void fourtyTwo() throws Exception {
        Object fourtyTwo = engine.eval("6 * 7");
        assertTrue("Number: " + fourtyTwo, fourtyTwo instanceof Number);
        assertEquals("fourtyTwo", 42, ((Number)fourtyTwo).intValue());
    }

    public interface Call {
        public int call(int x, int y);
    }

    @Test
    public void mul() throws Exception {
        Object mul = engine.eval("(function(x, y) { return x * y; })");
        assertNotNull("creates function object", mul);

        Call call = inv().getInterface(mul, Call.class);
        assertNotNull("Converted obj to Call: " + mul, call);

        assertEquals("fourtyTwo", 42, call.call(7, 6));
    }

    public interface Mul {
        public int mul(double x, long y);
        public long mulExported(int x, float y);
    }

    @Test
    public void globalMul() throws Exception {
        Object none = engine.eval("\n"
                + "function mul(x, y) {\n"
                + "  return x * y;\n"
                + "}\n"
                + "this.mulExported = mul;\n"
                + "if (typeof Polyglot !== 'undefined') {\n"
                + "  Polyglot.export('mulExported', mul);\n"
                + "}"
                + "undefined\n"
                + ""
        );
        assertNull("creates nothing", none);

        Mul global = inv().getInterface(Mul.class);
        assertNotNull("mul function visible as Mul: " + none, global);

        try {
            assertEquals("seventy seven", 77, global.mul(11, 7));
        } catch (Exception ex) {
            assertTrue("GraalVM:js exposes only exported symbols: " + engine.getFactory().getNames(), engine.getFactory().getNames().contains("GraalVM:js"));
        }
        assertEquals("mulExported is accessible in all engines", 77, global.mulExported(11, 7));
    }

    @Test
    public void typeOfTrue() throws Exception {
        Object tot = engine.eval("typeof true");
        assertEquals("boolean", tot);
    }

    @Test
    public void undefinedIsNull() throws Exception {
        Object undef = engine.eval("undefined");
        assertNull(undef);
    }

    @Test
    public void exposeObject() throws Exception {
        Object rawPoint = engine.eval("({ x : 5, y : -3, z : function(a) { return Math.floor(a * a); } })");
        assertNotNull(rawPoint);

        Point point = inv().getInterface(rawPoint, Point.class);
        if (point == null) {
            assumeNotNashorn();
            assumeNotGraalJsFromJDK();
        }
        assertNotNull("Converted to typed interface", point);

        assertEquals(5, point.x(), 0.1);
        assertEquals(-3, point.y());

        assertEquals("Power of sqrt(2) rounded", 2, point.z(1.42));
    }

    @Test
    public void accessJavaObject() throws Exception {
        Object fn = engine.eval("(function(obj) {\n"
                + "  obj.sum += 5;\n"
                + "  obj.increment();\n"
                + "  try {\n"
                + "    obj.add(6);\n"
                + "  } catch (e) {\n"
                + "    obj.err = e\n"
                + "  }\n"
                + "  return obj.sum;\n"
                + "})\n");
        assertNotNull(fn);

        Sum sum = new Sum();
        sum.sum = -5;
        Object res = inv().invokeMethod(fn, "call", null, sum);

        assertEquals("Incremented to one", 1, sum.sum);
        assertTrue("Got a number: " + res, res instanceof Number);
        assertEquals(1, ((Number) res).intValue());
        assertNotNull("There was an error calling non-public add method: " + sum.err, sum.err);
    }

    @Test
    public void sumArrayOfInt() throws Exception {
        assertSumArray(new int[] { 1, 2, 3, 4, 5, 6 });
    }

    @Test
    public void sumArrayOfObject() throws Exception {
        assertSumArray(new Object[] { 1, 2, 3, 4, 5, 6 });
    }

    @Test
    public void sumArrayOfInteger() throws Exception {
        assertSumArray(new Integer[] { 1, 2, 3, 4, 5, 6 });
    }

    private void assertSumArray(Object arr) throws Exception {
        engine.eval("\n"
                + "function sum(arr) {\n"
                + "  var r = 0;\n"
                + "  for (var i = 0; i < arr.length; i++) {\n"
                + "    r += arr[i];\n"
                + "  }\n"
                + "  return r;\n"
                + "}\n");

        Object res = inv().invokeFunction("sum", arr);

        assertTrue("Is number: " + res, res instanceof Number);
        assertEquals("Twenty one", 21, ((Number)res).intValue());
    }


    @Test
    public void returnArrayInJS() throws Exception {
        Assume.assumeFalse("Broken in GraalVM 20.3.0 fixed in GraalVM 21.1.0", "25.272-b10-jvmci-20.3-b06".equals(System.getProperty("java.vm.version")));

        Object fn = engine.eval("(function(obj) {\n"
                + "  return [ 1, 2, 'a', Math.PI, obj ];\n"
                + "})\n");
        assertNotNull(fn);

        Sum sum = new Sum();
        Object raw = ((Invocable) engine).invokeMethod(fn, "call", null, sum);

        ArrayLike res = ((Invocable) engine).getInterface(raw, ArrayLike.class);
        if (res == null) {
            assumeNotNashorn();
            assumeNotGraalJsFromJDK();
        }
        assertNotNull("Result looks like array", res);

        List<?> list = ((Invocable) engine).getInterface(raw, List.class);
        assertEquals("Length of five", 5, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals("a", list.get(2));
        assertEquals(Math.PI, list.get(3));
        assertEquals(sum, list.get(4));
    }

    private void assumeNotNashorn() {
        Assume.assumeFalse(engine.getFactory().getNames().contains("Nashorn"));
    }

    private void assumeNotGraalJsFromJDK() {
        Assume.assumeFalse(engine.getFactory().getNames().contains("Graal.js"));
    }

    @Test
    public void allowLoadAClassInJS() throws Exception {
        Assume.assumeTrue("All access has to be allowed", allowAllAccess);
        // @start region="allowLoadAClassInJS"
        Object fn = engine.eval("(function(obj) {\n"
                + "  var Long = Java.type('java.lang.Long');\n"
                + "  return new Long(33);\n"
                + "})\n");
        // @end region="allowLoadAClassInJS"
        assertNotNull(fn);

        Object value = ((Invocable) engine).invokeMethod(fn, "call", null, null);
        assertTrue("Is number: " + value, value instanceof Number);
        assertEquals(33, ((Number) value).intValue());
    }

    @Test
    public void preventLoadAClassInJS() throws Exception {
        Assume.assumeFalse("All access has to be disabled", allowAllAccess);
        Object fn = engine.eval("(function(obj) {\n"
                + "  var Long = Java.type('java.lang.Long');\n"
                + "  return new Long(33);\n"
                + "})\n");
        assertNotNull(fn);

        Object value;
        try {
            value = ((Invocable) engine).invokeMethod(fn, "call", null, null);
        } catch (ScriptException | RuntimeException ex) {
            return;
        }
        fail("Access to Java.type classes shall be prevented: " + value);
    }

    public static interface ArrayLike {
        int length();
    }

    public static interface Point {
        public double x();
        public long y();
        public long z(double v);
    }

    @Test
    public void output() throws Exception {
        StringWriter w = new StringWriter();
        engine.getContext().setWriter(w);
        engine.eval("print('Ahoj');");
        assertEquals("Ahoj\n", w.toString());
    }

    @Test(expected = ScriptException.class)
    public void error() throws Exception {
        engine.eval("throw 'Hi'");
    }
}
