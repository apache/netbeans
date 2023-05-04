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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Assume;
import static org.junit.Assume.assumeNotNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.scripting.Scripting;

public final class GraalEnginesTest {
    @BeforeClass
    public static void skipIfNoPolyglotFound() {
        try {
            Class.forName("org.graalvm.polyglot.Engine").getMethod("create").invoke(null);
        } catch (ClassNotFoundException ex) {
            Assume.assumeNoException("Skip if no Engine is found", ex);
        } catch (ReflectiveOperationException ex) {
            Assume.assumeNoException("Error when initializing Engine", ex);
        }
    }

    @Test
    public void invokeEngineViaGeneratedScriptEngine() {
        ScriptEngineManager man = Scripting.createManager();
        ScriptEngine llvm = man.getEngineByName("GraalVM:llvm");
        assumeNotNull("Need llvm. Found: " + man.getEngineFactories(), llvm);

        ScriptEngineFactory jsFactory = null;
        ScriptEngineFactory graalvmJsFactory = null;
        ScriptEngineFactory llvmFactory = null;

        StringBuilder log = new StringBuilder();
        for (ScriptEngineFactory factory : man.getEngineFactories()) {
            final List<String> engineNames = factory.getNames();
            final List<String> types = factory.getMimeTypes();

            log.append("\nclass: ").append(factory.getClass().getSimpleName())
                .append("\nnames: ").append(engineNames)
                .append("\ntypes: ").append(types);

            if (engineNames.contains("LLVM")) {
                llvmFactory = factory;
            }
            if (types.contains("text/javascript")) {
                if (factory.getEngineName().startsWith("GraalVM:")) {
                    assertNull("No previous generic GraalVM javascript factory: " + graalvmJsFactory, graalvmJsFactory);
                    graalvmJsFactory = factory;
                } else if (!factory.getEngineName().equalsIgnoreCase("Oracle Nashorn")) {
                    assertNull("No previous javascript factory: " + jsFactory, jsFactory);
                    jsFactory = factory;
                }
            }
        }

        assertNotNull("llvm factory found: " + log, llvmFactory);
        assertNotNull("js factory found: " + log, jsFactory);
        assertNotNull("Generic GraalVM js factory found: " + log, graalvmJsFactory);
    }

    private static final String MUL =
          "def mul(x, y):\n"
        + "  return x * y\n"
        + "mul\n"
        + "\n";

    @Test
    public void pythonDirect() throws Exception {
        assumeNotNull("Need python", Scripting.createManager().getEngineByMimeType("text/x-python"));
        final Context ctx = Context.newBuilder().allowAllAccess(true).build();
        Value mul = ctx.eval("python", MUL);
        Value fourtyTwo = mul.execute(6, 7);
        Assert.assertEquals("Fourty two", 42, fourtyTwo.asInt());
    }

    public static interface Mul {
        public int multiplyTwoNumbers(int x, int y);
    }

    @Test
    public void pythonFn() throws Exception {
        ScriptEngine python = Scripting.createManager().getEngineByMimeType("text/x-python");
        assumeNotNull("Need python", python);
        Object rawMul = python.eval(MUL);
        assertNotNull("mul created", rawMul);
        assertTrue("Engines are invocable: " + python, python instanceof Invocable);
        final Invocable inv = (Invocable)python;

        Object res = inv.invokeFunction("mul", 6, 7);
        assertNotNull("Expecting non-null", res);

        assertTrue("Expecting number: " + res + " type: " + res.getClass(), res instanceof Number);
        assertEquals(42, ((Number)res).intValue());

        Mul mul = inv.getInterface(rawMul, Mul.class);
        assertEquals(42, mul.multiplyTwoNumbers(7, 6));
    }

    @Test
    public void javaScriptFn() throws Exception {
        ScriptEngine js = Scripting.createManager().getEngineByName("GraalVM:js");
        Object rawFn = js.eval("(function (x, y) { return x * y; })");

        assertTrue("Engines are invocable: " + js, js instanceof Invocable);
        final Invocable inv = (Invocable)js;

        Mul mul = inv.getInterface(rawFn, Mul.class);
        assertEquals(42, mul.multiplyTwoNumbers(7, 6));
    }

    @Test
    public void pythonObjAccess() throws Exception {
        ScriptEngine python = Scripting.createManager().getEngineByMimeType("text/x-python");
        assumeNotNull("Need python", python);
        Object rawPoint = python.eval(
            "class O:\n" +
            "  x = 5\n" +
            "  y = -3\n" +
            "  def z(self, x):\n" +
            "    return x * x\n" +
            "O()\n"
        );
        assertNotNull("Object created", rawPoint);

        final Invocable inv = (Invocable)python;
        Point point = inv.getInterface(rawPoint, Point.class);
        assertNotNull("Object wrapped", point);

        assertEquals(5, point.x());
        assertEquals(-3, point.y(), 0.1);
        assertEquals("Power of sqrt(2)", 2, point.z(1.42), 0.1);
    }

    @Test
    public void returnArrayInPython() throws Exception {
        ScriptEngine python = Scripting.createManager().getEngineByMimeType("text/x-python");
        assumeNotNull("Need python", python);
        python.eval("\n"
                + "import math\n"
                + "\n"
                + "def arr(x):\n"
                + "  return [ 1, 2, 'a', math.pi, x ]\n"
                + "\n"
        );

        assertTrue("Engines are invocable: " + python, python instanceof Invocable);
        final Invocable inv = (Invocable)python;

        Sum sum = new Sum();
        Object raw = inv.invokeFunction("arr", sum);

        List<?> list = inv.getInterface(raw, List.class);
        assertNotNull("List " + list, list);
        assertEquals("Length of five", 5, list.size());

        List<?> list2 = inv.getInterface(list, List.class);
        assertNotNull("List 2 " + list2, list2);
        assertEquals("Length 2 of five", 5, list2.size());

        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals("a", list.get(2));
        assertEquals(Math.PI, list.get(3));
        assertEquals(sum, list.get(4));
    }


    @Test
    public void returnArrayInJS() throws Exception {
        Assume.assumeFalse("Broken in GraalVM 20.3.0 fixed in GraalVM 21.1.0", "25.272-b10-jvmci-20.3-b06".equals(System.getProperty("java.vm.version")));

        ScriptEngine js = Scripting.createManager().getEngineByName("GraalVM:js");
        Object fn = js.eval("(function(obj) {\n"
                + "  return [ 1, 2, 'a', Math.PI, obj ];\n"
                + "})\n");
        assertNotNull(fn);

        Sum sum = new Sum();
        Object raw = ((Invocable) js).invokeMethod(fn, "call", null, sum);

        List<?> list = ((Invocable) js).getInterface(raw, List.class);
        assertNotNull("List " + list, list);
        assertEquals("Length of five", 5, list.size());

        ArrLike like = ((Invocable) js).getInterface(raw, ArrLike.class);
        assertNotNull("Array like " + like, like);
        assertEquals("Length of five", 5, like.length());

        
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals("a", list.get(2));
        assertEquals(Math.PI, list.get(3));
        assertEquals(sum, list.get(4));
    }

    public interface ArrLike {
        public int length();
    }

    @Test
    public void returnMapInJS() throws Exception {
        ScriptEngine js = Scripting.createManager().getEngineByName("GraalVM:js");
        Object fn = js.eval("(function() {\n"
                + "  return {\n"
                + "    'x' : 1,\n"
                + "    'y' : 2,\n"
                + "  };\n"
                + "})\n");
        assertNotNull(fn);

        Object raw = ((Invocable) js).invokeMethod(fn, "call");

        Map<?,?> map = ((Invocable) js).getInterface(raw, Map.class);

        assertEquals(1, map.get("x"));
        assertEquals(2, map.get("y"));
        assertEquals("Expecting just two elements: " + map, 2, map.size());
    }

    public interface Point {
        public int x();
        public double y();
        public double z(double a);
    }

    @Test
    public void accessPolyglotBindings() throws Exception {
        ScriptEngineManager man = Scripting.createManager();
        ScriptEngine js = man.getEngineByName("GraalVM:js");
        ScriptEngine python = man.getEngineByName("GraalVM:python");
        assumeNotNull("Need python", python);
        
        List<Integer> scopes = js.getContext().getScopes();
        assertEquals(2, scopes.size());
        assertEquals(ScriptContext.GLOBAL_SCOPE, scopes.get(1).intValue());

        Bindings bindings = js.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put("x", 42);

        js.eval("\n"
            + "var x = Polyglot.import('x');\n"
            + "Polyglot.export('y', x);\n"
            + ""
        );

        Object y = python.eval("\n"
            + "import polyglot;\n"
            + "polyglot.import_value('y')"
        );

        assertTrue("Expecting number, but was: " + y, y instanceof Number);
        assertEquals(42, ((Number)y).intValue());
    }

    public interface TwoNumbers {
        public int fourtyTwo();
        public int eightyOne();
    }

    @Test
    public void accessPolyglotBindings2() throws Exception {
        ScriptEngineManager man = Scripting.createManager();
        ScriptEngine python = man.getEngineByName("GraalVM:python");
        ScriptEngine js = man.getEngineByName("GraalVM:js");
        assumeNotNull("Need python", python);
        
        python.eval("\n"
                + "import polyglot;\n"
                + "@polyglot.export_value\n"
                + "def fourtyTwo():\n"
                + "  return 42\n"
                + "\n"
        );

        js.eval("\n"
                + "Polyglot.export('eightyOne', function() {\n"
                + "  return 81;\n"
                + "});\n"
        );

        TwoNumbers numbers1 = ((Invocable)python).getInterface(TwoNumbers.class);

        assertEquals("Fourty two from python", 42, numbers1.fourtyTwo());
        assertEquals("Eighty one from python", 81, numbers1.eightyOne());

        TwoNumbers numbers2 = ((Invocable)js).getInterface(TwoNumbers.class);

        assertEquals("Fourty two from JS", 42, numbers2.fourtyTwo());
        assertEquals("Eighty one from JS", 81, numbers2.eightyOne());
    }
    
    /**
     * Attributes that have been set up as global scope should be accessible as polyglot 
     * bindings. Polyglot bindings should be visible as global scope attributes.
     * @throws Exception 
     */
    @Test
    public void polyglotBindingsAsAttributes() throws Exception {
        ScriptEngineManager man = Scripting.newBuilder().build();

        ScriptEngine snake = man.getEngineByName("GraalVM:python");
        ScriptEngine js = man.getEngineByName("GraalVM:js");
        
        snake.getContext().setAttribute("preSnake", 1111, ScriptContext.GLOBAL_SCOPE);
        js.getContext().setAttribute("preJs", 2222, ScriptContext.GLOBAL_SCOPE);
        
        Bindings pythonBindings = snake.getBindings(ScriptContext.GLOBAL_SCOPE);
        Bindings jsBindings = js.getBindings(ScriptContext.GLOBAL_SCOPE);

        pythonBindings.put("ctxSnake", 3333);
        jsBindings.put("ctxJs", 4444);
        
        Object s = js.eval("var s = '' + Polyglot.import('preSnake') + Polyglot.import('preJs') + Polyglot.import('ctxSnake') + Polyglot.import('ctxJs');" 
                + "Polyglot.export('s', s); s;");
        assertEquals("1111222233334444", s);

        assertEquals(s, js.getContext().getAttribute("s"));
        assertEquals(s, js.getContext().getAttribute("s", ScriptContext.GLOBAL_SCOPE));
        
        assertEquals(s, snake.getContext().getAttribute("s"));
        assertEquals(s, snake.getContext().getAttribute("s", ScriptContext.GLOBAL_SCOPE));

    }

    @Test
    public void hostAccessGlobalAttributeWorks() throws Exception {
        ScriptEngineManager man = Scripting.createManager();
        ScriptEngine js = man.getEngineByName("GraalVM:js");
        
        js.getContext().setAttribute("allowAllAccess", true, ScriptContext.GLOBAL_SCOPE);
        Object o = js.eval("var a = new java.util.ArrayList(); Polyglot.export('a', a); a;");
        
        assertTrue(o instanceof ArrayList);
    }

    @Test
    public void allAccessEnabledBuilder() throws Exception {
        ScriptEngineManager man = Scripting.newBuilder().allowAllAccess(true).build();
        ScriptEngine js = man.getEngineByName("GraalVM:js");
        
        // consistency of builder / attribute
        assertEquals(Boolean.TRUE, js.getContext().getAttribute("allowAllAccess"));
        
        Object o = js.eval("new java.util.ArrayList();");
        assertTrue(o instanceof ArrayList);
        
        // consistency after Polyglot init:
        assertEquals(Boolean.TRUE, js.getContext().getAttribute("allowAllAccess"));
        // should not be exported into the language or polyglot
        assertNull(js.get("allowAllAccess"));
        // or its bindings
        assertNull(js.getBindings(ScriptContext.GLOBAL_SCOPE).get("allowAllAccess"));
    }

    @Test
    public void allAccessEnabledAttribute() throws Exception {
        ScriptEngineManager man = Scripting.newBuilder().build();
        ScriptEngine js = man.getEngineByName("GraalVM:js");

        js.getContext().setAttribute("allowAllAccess", true, ScriptContext.GLOBAL_SCOPE);

        Object o = js.eval("new java.util.ArrayList();");
        assertTrue(o instanceof ArrayList);

        // consistency after Polyglot init:
        assertEquals(Boolean.TRUE, js.getContext().getAttribute("allowAllAccess"));
        // should not be in engine scope
        assertNull(js.get("allowAllAccess"));
        // or its bindings
        assertNull(js.getBindings(ScriptContext.GLOBAL_SCOPE).get("allowAllAccess"));
    }
    
}
