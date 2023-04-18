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
package org.netbeans.libs.graalsdk;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import junit.framework.TestSuite;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Assume;
import static org.junit.Assume.assumeFalse;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.netbeans.api.scripting.Scripting;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author sdedic
 */
public class JavaScriptEnginesTest2 extends NbTestCase {
    @Parameterized.Parameters(name = "{1}:{0}@{4}={2}")
    public static Object[][] engines() {
        List<Object[]> arr = new ArrayList<>();
        fillArray(Scripting.newBuilder().build(), false, arr);
        final ScriptEngineManager man = Scripting.newBuilder().allowAllAccess(true).build();
        fillArray(man, true, arr);
        return arr.toArray(new Object[0][]);
    }
    
    public static void createTests(TestSuite suite) {
        Object[][] data = engines();
        for (Object[] row : data) {
            for (Method m : JavaScriptEnginesTest2.class.getMethods())  {
                final org.junit.Test ann = m.getAnnotation(org.junit.Test.class);
                if (ann == null) {
                    continue;
                }
                junit.framework.Test t = new JavaScriptEnginesTest2(m.getName(), (String)row[0], 
                        row[1], (String)row[2], (ScriptEngine)row[3], (boolean)row[4]);
                suite.addTest(t);
            }
        }
    }

    private static void fillArray(final ScriptEngineManager man, boolean allowAllAccess, List<Object[]> arr) {
        for (ScriptEngineFactory f : man.getEngineFactories()) {
            final String name = f.getEngineName();
            if (
                    f.getMimeTypes().contains("text/javascript") ||
                    name.contains("Nashorn")
                    ) {
                final ScriptEngine eng = f.getScriptEngine();
                arr.add(new Object[] { name, "engineFactories", implName(eng), eng, allowAllAccess });
                for (String n : eng.getFactory().getNames()) {
                    ScriptEngine byName = n == null ? null : man.getEngineByName(n);
                    if (byName != null && eng.getClass() == byName.getClass()) {
                        arr.add(new Object[] { n, "name", implName(byName), byName, allowAllAccess });
                    }
                }
                for (String t : eng.getFactory().getMimeTypes()) {
                    ScriptEngine byType = t == null ? null : man.getEngineByMimeType(t);
                    if (byType != null && eng.getClass() == byType.getClass()) {
                        arr.add(new Object[] { t, "type", implName(byType), byType, allowAllAccess });
                    }
                }
                for (String e : eng.getFactory().getExtensions()) {
                    ScriptEngine byExt = e == null ? null : man.getEngineByExtension(e);
                    if (byExt != null && eng.getClass() == byExt.getClass()) {
                        arr.add(new Object[] { e, "ext", implName(byExt), byExt, allowAllAccess });
                    }
                }
            }
        }
    }

    private static String implName(Object obj) {
        return obj.getClass().getSimpleName();
    }

    private final String engineName;
    private final ScriptEngine engine;
    private final boolean allowAllAccess;


    public JavaScriptEnginesTest2(String testName, String engineName, Object info, String implName, ScriptEngine engine, boolean allowAllAccess) {
        super(testName);
        this.engineName = engineName;
        this.engine = engine;
        this.allowAllAccess = allowAllAccess;
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
    public void classOfString() throws Exception {
        if (!allowAllAccess) {
            return;
        }
        Object clazz = engine.eval("\n"
            + "var s = '';\n"
            + "var n;\n"
            + "try {\n"
            + "  var c = s.getClass();\n"
            + "  n = c.getName();\n"
            + "} catch (e) {\n"
            + "  n = null;\n"
            + "}\n"
            + "n\n"
        );
        assertNull("No getClass attribute of string", clazz);
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
    public void classOfSum() throws Exception {
        if (!allowAllAccess) {
            return;
        }
        Assume.assumeFalse("GraalJSScriptEngine".equals(engine.getClass().getSimpleName()));

        Object fn = engine.eval("(function(obj) {\n"
                + "  try {\n"
                + "     return obj.getClass().getName();\n"
                + "  } catch (e) {\n"
                + "     return null;\n"
                + "  }\n"
                + "})\n");
        Object clazz = inv().invokeMethod(fn, "call", null, "Huuu");
        assertNull("No getClass attribute of string", clazz);
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
    public void nonInvocableInvoke() throws Exception {
        class ObscureObj {
        }
        ObscureObj obj = new ObscureObj();
        try {
            ((Invocable) engine).invokeMethod(obj, "unknown");
            fail("There is no such method unknown!");
        } catch (NoSuchMethodException | IllegalArgumentException ex) {
            // OK
        }
    }

    @Test
    public void nonFunctionInvoke() throws Exception {
        Object obj = engine.eval("\n"
                + "new Object()\n"
                + "\n");
        try {
            Object res = ((Invocable) engine).invokeMethod(obj, "unknown");
            fail("There is no such method unknown!" + res);
        } catch (NullPointerException | NoSuchMethodException ex) {
            // OK
        }
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
        try {
            engine.eval("throw 'Hi'");
            Assert.fail("Exception expected");
        } catch (ScriptException ex) {
            // ok
        }
    }

    /**
     * Checks that exception originating in the script/lang code will be reported
     * as ScriptException.
     * @throws Exception 
     */
    @Test
    public void guestExceptionReportedAsRuntime() throws Exception {
        try {
            engine.eval("var a = null; a.fn();");
            fail("Exception expected");
        } catch (ScriptException ex) {
            Throwable c = ex.getCause();
            Assert.assertThat(c, CoreMatchers.is(CoreMatchers.instanceOf(RuntimeException.class)));
        }
    }

    public class Callback {
        public void fn() throws Exception {
            throw new NoSuchElementException();
        }
        
        public void fn2() throws IOException {
            throw new IOException();
        }
    }
    
    /**
     * Checks that exception thrown in the callback Java code is reported 'as is'.
     * @throws Exception 
     */
    @Test
    public void hostCheckedExceptionAccessible() throws Exception {
        // Note: this seems to be broken on GraalVM's JDK js - runtime exceptions are wrapped into
        // polyglot wrapper and cannot be determined through the chain of getCauses().
        assumeFalse(engine.getFactory().getEngineName().toLowerCase().contains("graal.js"));
        
        try {
            engine.eval("var x; function setGlobalX(p) { x = p }");
            ((Invocable)engine).invokeFunction("setGlobalX", new Callback());
            engine.eval("x.fn2();");
            fail("Exception expected");
        } catch (RuntimeException ex) {
            Throwable c = ex.getCause();
            Assert.assertThat(c, CoreMatchers.is(CoreMatchers.instanceOf(IOException.class)));
        } catch (Exception ex) {
            fail("Runtime subclass is expected");
        }
    }

    /**
     * Checks that exception thrown in the callback Java code is reported 'as is'.
     * @throws Exception 
     */
    @Test
    public void hostRuntimeExceptionsAccessible() throws Exception {
        // Note: this seems to be broken on GraalVM's JDK js - runtime exceptions are wrapped into
        // polyglot wrapper and cannot be determined through the chain of getCauses().
        assumeFalse(engine.getFactory().getEngineName().toLowerCase().contains("graal.js"));
        
        try {
            engine.eval("var x; function setGlobalX(p) { x = p }");
            ((Invocable)engine).invokeFunction("setGlobalX", new Callback());
            engine.eval("x.fn();");
            fail("Exception expected");
        } catch (ScriptException ex) {
            Throwable c = ex.getCause();
            Assert.assertThat(c, CoreMatchers.is(CoreMatchers.instanceOf(NoSuchElementException.class)));
        } catch (NoSuchElementException ex) {
            // this is OK
        } catch (Exception ex) {
            
        }
    }
    
    /** 
     * Checks that values assigned by various mehtods are visible:
     * @throws Exception 
     */
    @Test
    public void testEngineGlobalVariablesVisible() throws Exception {
        Bindings b = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        b.put("a", 1111);
        Object o = engine.eval("var b = 3333; a");
        assertEquals(1111, o);
        assertEquals(3333, b.get("b"));
        
        engine.getContext().setAttribute("a", 2222, ScriptContext.ENGINE_SCOPE);
        o = engine.eval("var b = 4444; a");
        assertEquals(2222, o);
        assertEquals(4444, engine.getContext().getAttribute("b"));
        assertEquals(4444, engine.getContext().getAttribute("b", ScriptContext.ENGINE_SCOPE));
        assertNull(engine.getContext().getAttribute("b", ScriptContext.GLOBAL_SCOPE));
        
    }
}
