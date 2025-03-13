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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.junit.Assume;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;
import org.netbeans.api.scripting.Scripting;


import org.netbeans.junit.NbTestCase;

public class ScriptingTutorial extends NbTestCase {

    private ScriptEngine engine;


    public ScriptingTutorial(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        engine = listAll();
        assertNotNull(engine);
    }

    public ScriptEngine listAll() {
        // @start region="listAll"
        ScriptEngine found = null;
        final ScriptEngineManager manager = Scripting.createManager();
        for (ScriptEngineFactory factory : manager.getEngineFactories()) {
            final String name = factory.getEngineName();
            System.err.println("Found " + name);
            if (factory.getMimeTypes().contains("text/javascript")) {
                if (name.equals("GraalVM:js")) {
                    found = factory.getScriptEngine();
                }
            }
        }
        // @end region="listAll"
        Assume.assumeTrue("Need Javascript", found != null);
        Assume.assumeTrue("Need Python", manager.getEngineByMimeType("text/x-python") != null);
        return found;
    }

    public void testHelloWorld() throws Exception {
        // @start region="testHelloWorld"
        ScriptEngine python = Scripting.createManager().getEngineByMimeType("text/x-python");
        assert python != null : "Install Graal Python via `gu install python`";

        String x = (String) python.eval("\n"
            + "x = 'Hello World!'\n"
            + "print(x)\n"
            + "x\n"
        );

        assert x.equals("Hello World!") : x;
        // @end region="testHelloWorld"
    }

    public void testCastPythonObj() throws Exception {
        // @start region="testCastPythonObj"
        ScriptEngine python = Scripting.createManager().getEngineByName("GraalVM:python");
        assert python != null : "Install Graal Python via `gu install python`";

        Object raw = python.eval("\n"
            + "class Point:\n"
            + "  x = 1\n"
            + "  y = 2\n"
            + "Point()\n"
        );

        Map<?,?> point = ((Invocable)python).getInterface(raw, Map.class);
        assert ((Number)point.get("x")).intValue() == 1;
        assert ((Number)point.get("y")).intValue() == 2;
        // @end region="testCastPythonObj"
    }

    public void testCastJsArray() throws Exception {
        // @start region="testCastJsArray"
        ScriptEngine js = Scripting.createManager().getEngineByName("GraalVM:js");
        assert js != null : "Run on GraalVM!";

        Object raw = js.eval("['Hello', 'World']\n");

        // convert the raw array into List:
        List<?> list = ((Invocable)js).getInterface(raw, List.class);

        assert list.size() == 2;
        assert list.get(0).equals("Hello");
        assert list.get(1).equals("World");
        // @end region="testCastJsArray"
    }

    public void testHelloWorldInPythonAndJavaScript() throws Exception {
        // only execute this test, if JS comes from GraalVM JDK, not from bundled impl:
        assumeTrue("Need GraalVM-provided JS to be interoperable with Python", 
                Scripting.createManager().getEngineByMimeType("text/javascript").getClass().getName().contains("graalsdk.system"));
        // @start region="testHelloWorldInPythonAndJavaScript"
        // creates a single shared manager for two languages
        final ScriptEngineManager manager = Scripting.createManager();

        // creates two engines connected to each other
        ScriptEngine js = manager.getEngineByMimeType("text/javascript");
        assert js != null : "Run on GraalVM!";
        ScriptEngine python = manager.getEngineByMimeType("text/x-python");
        assert python != null : "Install Graal Python via `gu install python`";

        // JavaScript function that takes another function as argument
        Object sayHelloRaw = js.eval("(function(subject) {\n"
                + "  return 'Hello ' + subject() + '!';\n"
                + "})\n");
        @SuppressWarnings("unchecked")
        Function<Object, ?> sayHelloFn = ((Invocable)js).getInterface(sayHelloRaw, Function.class);

        // Python function that returns a value
        Object worldFn = python.eval("\n"
            + "def world():\n"
            + "  return 'World'\n"
            + "\n"
            + "world\n"
            + ""
        );

        // pass Python function as an argument to JavaScript function
        String helloWorld = (String) sayHelloFn.apply(worldFn);

        assert "Hello World!".equals(helloWorld) : helloWorld;
        // @end region="testHelloWorldInPythonAndJavaScript"
    }

    public void testCallJavaScriptFunctionFromJava() throws Exception {
        callJavaScriptFunctionFromJava();
    }

    // @start region="callJavaScriptFunctionFromJava"
    @FunctionalInterface
    interface Multiplier {
        int multiply(int a, int b);
    }

    public void callJavaScriptFunctionFromJava() throws Exception {
        Invocable invocable = (Invocable) engine;

        String src = "(" +
            "function (a, b) {\n" +
            "  return a * b;\n" +
            "})";

        // Evaluate JavaScript function definition
        Object jsFunction = engine.eval(src);

        // Create Java access to JavaScript function
        Multiplier mul = invocable.getInterface(jsFunction, Multiplier.class);

        assertEquals(42, mul.multiply(6, 7));
        assertEquals(144, mul.multiply(12, 12));
        assertEquals(256, mul.multiply(32, 8));
    }
    // @end region="callJavaScriptFunctionFromJava"

    public void testPythonFunctionFromJava() throws Exception {
        ScriptEngineManager manager = Scripting.createManager();

        // @start region="testPythonFunctionFromJava"
        ScriptEngine python = manager.getEngineByName("GraalVM:python");
        Invocable invocable = (Invocable) python;
        String src = "" +
            "def mul(a, b):\n" +
            "  return a * b\n" +
            "\n" +
            "mul\n";

        // Evaluate Python function definition
        Object pythonFunction = python.eval(src);

        // Create Java access to Python function
        Multiplier mul = invocable.getInterface(pythonFunction, Multiplier.class);

        assertEquals(42, mul.multiply(6, 7));
        assertEquals(144, mul.multiply(12, 12));
        assertEquals(256, mul.multiply(32, 8));
        // @end region="testPythonFunctionFromJava"
    }

    public void testCallRFunctionFromJava() throws Exception {
        ScriptEngine rEngine = Scripting.createManager().getEngineByMimeType("application/x-r");
        if (rEngine != null) {
            callRFunctionFromJava();
        }
    }

    // @start region="callRFunctionFromJava"
    @FunctionalInterface
    interface BinomQuantile {
        int qbinom(double q, int count, double prob);
    }

    public void callRFunctionFromJava() throws Exception {
        // @start region="allowAllAccess"
        // FastR currently needs access to native libraries:
        final ScriptEngineManager manager = Scripting.newBuilder().allowAllAccess(true).build();
        ScriptEngine rEngine = manager.getEngineByMimeType("application/x-r");
        // @end region="allowAllAccess"

        assumeNotNull(rEngine);
        final Object funcRaw = rEngine.eval("qbinom");
        BinomQuantile func = ((Invocable) rEngine).getInterface(funcRaw, BinomQuantile.class);
        assertEquals(4, func.qbinom(0.37, 10, 0.5));
    }
    // @end region="callRFunctionFromJava"

    public void testCallRFunctionFromJavaTheOldWay() throws Exception {
        ScriptEngine rEngine = Scripting.createManager().getEngineByMimeType("application/x-r");
        assumeNotNull(rEngine);
        // FastR currently needs access to native libraries:
        rEngine.getContext().setAttribute("allowAllAccess", true, ScriptContext.GLOBAL_SCOPE);

        final Object funcRaw = rEngine.eval("qbinom");
        BinomQuantile func = ((Invocable) rEngine).getInterface(funcRaw, BinomQuantile.class);
        assertEquals(4, func.qbinom(0.37, 10, 0.5));
    }

    public void testCallJavaScriptFunctionsWithSharedStateFromJava() throws Exception {
        callJavaScriptFunctionsWithSharedStateFromJava();
    }

    // @start region="callJavaScriptFunctionsWithSharedStateFromJava"
    interface Counter {
        void addTime(int hours, int minutes, int seconds);
        int timeInSeconds();
    }

    public void callJavaScriptFunctionsWithSharedStateFromJava() throws Exception {
        String src = "\n"
            + "(function() {\n"
            + "  var seconds = 0;\n"
            + "  function addTime(h, m, s) {\n"
            + "    seconds += 3600 * h;\n"
            + "    seconds += 60 * m;\n"
            + "    seconds += s;\n"
            + "  }\n"
            + "  function time() {\n"
            + "    return seconds;\n"
            + "  }\n"
            + "  return {\n"
            + "    'addTime': addTime,\n"
            + "    'timeInSeconds': time\n"
            + "  }\n"
            + "})\n";

        // Evaluate JavaScript function definition
        Object jsFunction = engine.eval(src);

        // Execute the JavaScript function via its call method
        Object jsObject = ((Invocable)engine).invokeMethod(jsFunction, "call");

        // Create Java access to the JavaScript object
        Counter counter = ((Invocable)engine).getInterface(jsObject, Counter.class);

        counter.addTime(6, 30, 0);
        counter.addTime(9, 0, 0);
        counter.addTime(12, 5, 30);

        assertEquals(99330, counter.timeInSeconds());
    }
    // @end region="callJavaScriptFunctionsWithSharedStateFromJava"

    public void testAccessFieldsOfJavaObject() throws Exception {
        accessFieldsOfJavaObject();
    }

    public void testAccessFieldsOfJavaObjectWithConverter() throws Exception {
        accessFieldsOfJavaObjectWithConverter();
    }

    // @start region="accessFieldsOfJavaObject"

    public static final class Moment {
        public final int hours;
        public final int minutes;
        public final int seconds;

        public Moment(int hours, int minutes, int seconds) {
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
        }
    }

    public void accessFieldsOfJavaObject() throws Exception {
        String src = "\n"
            + "(function(t) {\n"
            + "  return 3600 * t.hours + 60 * t.minutes + t.seconds;\n"
            + "})\n";

        final Moment javaMoment = new Moment(6, 30, 10);

        // Evaluate the JavaScript function definition
        Object jsFunction = engine.eval(src);

        // Execute the JavaScript function, passing a Java object argument
        Object jsSeconds = ((Invocable)engine).invokeMethod(
            jsFunction, "call", null, javaMoment
        );

        // Convert foreign object result to desired Java type
        int seconds = ((Number) jsSeconds).intValue();

        assertEquals(3600 * 6 + 30 * 60 + 10, seconds);
    }
    // @end region="accessFieldsOfJavaObject"

    // @start region="accessFieldsOfJavaObjectWithConverter"

    @FunctionalInterface
    interface MomentConverter {
        int toSeconds(Moment moment);
    }

    public void accessFieldsOfJavaObjectWithConverter() throws Exception {
        String src = "\n"
            + "(function(t) {\n"
            + "  return 3600 * t.hours + 60 * t.minutes + t.seconds;\n"
            + "})\n";

        final Moment javaMoment = new Moment(6, 30, 10);

        // Evaluate the JavaScript function definition
        final Object jsFunction = engine.eval(src);

        // Convert the function to desired Java type
        MomentConverter converter = ((Invocable) engine).getInterface(
            jsFunction, MomentConverter.class
        );

        // Execute the JavaScript function as a Java foreign function
        int seconds = converter.toSeconds(javaMoment);

        assertEquals(3600 * 6 + 30 * 60 + 10, seconds);
    }
    // @end region="accessFieldsOfJavaObjectWithConverter"

    public void testCreateJavaScriptFactoryForJavaClass() throws Exception {
        createJavaScriptFactoryForJavaClass();
    }

    // @start region="createJavaScriptFactoryForJavaClass"

    interface MomentFactory {
        Moment create(int h, int m, int s);
    }

    public void createJavaScriptFactoryForJavaClass() throws Exception {
        String src = "\n"
            + "(function(Moment) {\n"
            + "  return function(h, m, s) {\n"
            + "     return new Moment(h, m, s);\n"
            + "  };\n"
            + "})\n";

        // Evaluate the JavaScript function definition
        final Object jsFunction = engine.eval(src);

        // Create a JavaScript factory for the provided Java class
        final Object jsFactory = ((Invocable) engine).invokeMethod(jsFunction, "call", null, Moment.class);

        // Convert the JavaScript factory to a Java foreign function
        MomentFactory momentFactory = ((Invocable) engine).getInterface(jsFactory, MomentFactory.class);

        final Moment javaMoment = momentFactory.create(6, 30, 10);
        assertEquals("Hours", 6, javaMoment.hours);
        assertEquals("Minutes", 30, javaMoment.minutes);
        assertEquals("Seconds", 10, javaMoment.seconds);
    }
    // @end region="createJavaScriptFactoryForJavaClass"

    public void testCallJavaScriptClassFactoryFromJava() throws Exception {
        callJavaScriptClassFactoryFromJava();
    }

    // @start region="callJavaScriptClassFactoryFromJava"

    interface Incrementor {
        int inc();
        int dec();
        int value();
    }

    public void callJavaScriptClassFactoryFromJava() throws Exception {
        String src = "\n"
            + "(function() {\n"
            + "  class JSIncrementor {\n"
            + "     constructor(init) {\n"
            + "       this.value = init;\n"
            + "     }\n"
            + "     inc() {\n"
            + "       return ++this.value;\n"
            + "     }\n"
            + "     dec() {\n"
            + "       return --this.value;\n"
            + "     }\n"
            + "  }\n"
            + "  return function(init) {\n"
            + "    return new JSIncrementor(init);\n"
            + "  }\n"
            + "})\n";

        // Evaluate JavaScript function definition
        Object jsFunction = engine.eval(src);

        final Invocable inv = (Invocable) engine;

        // Execute the JavaScript function
        Object jsFactory = inv.invokeMethod(jsFunction, "call");

        // Execute the JavaScript factory to create Java objects
        Incrementor initFive = inv.getInterface(
            inv.invokeMethod(jsFactory, "call", null, 5),
            Incrementor.class
        );
        Incrementor initTen = inv.getInterface(
            inv.invokeMethod(jsFactory, "call", null, 10),
            Incrementor.class
        );

        initFive.inc();
        assertEquals("Now at seven", 7, initFive.inc());

        initTen.dec();
        assertEquals("Now at eight", 8, initTen.dec());
        initTen.dec();

        assertEquals("Values are the same", initFive.value(), initTen.value());
    }
    // @end region="callJavaScriptClassFactoryFromJava"


    public void testAccessJavaScriptArrayWithTypedElementsFromJava() throws Exception {
        accessJavaScriptArrayWithTypedElementsFromJava();
    }

    // @start region="accessJavaScriptArrayWithTypedElementsFromJava"

    interface Point {
        int x();
        int y();
    }

    @FunctionalInterface
    interface PointProvider {
        List<Point> createPoints();
    }

    public void accessJavaScriptArrayWithTypedElementsFromJava() throws Exception {
        String src = "\n"
            + "(function() {\n"
            + "  class Point {\n"
            + "     constructor(x, y) {\n"
            + "       this.x = x;\n"
            + "       this.y = y;\n"
            + "     }\n"
            + "  }\n"
            + "  return [ new Point(30, 15), new Point(5, 7) ];\n"
            + "})\n";

        // Evaluate the JavaScript function definition
        Object jsFunction = engine.eval(src);

        // Create Java-typed access to the JavaScript function
        PointProvider pointProvider = ((Invocable) engine).getInterface(jsFunction, PointProvider.class);

        // Invoke the JavaScript function to generate points
        List<Point> points = pointProvider.createPoints();

        assertEquals("Two points", 2, points.size());

        Point first = points.get(0);
        assertEquals(30, first.x());
        assertEquals(15, first.y());

        Point second = points.get(1);
        assertEquals(5, second.x());
        assertEquals(7, second.y());
    }
    // @end region="accessJavaScriptArrayWithTypedElementsFromJava"

    public void tetsAccessJSONObjectProperties() throws Exception {
        accessJavaScriptJSONObjectFromJava();
    }


    // Checkstyle: stop
    // @start region="accessJavaScriptJSONObjectFromJava"

    interface Repository {
        int id();
        String name();
        Owner owner();
        boolean has_wiki();
        List<String> urls();
    }

    interface Owner {
        int id();
        String login();
        boolean site_admin();
    }

    @FunctionalInterface
    interface ParseJSON {
        List<Repository> parse();
    }

    public void accessJavaScriptJSONObjectFromJava() throws Exception {
        String src =
            "(function () { \n" +
            "  return function() {\n" +
            "    return [\n" +
            "      {\n" +
            "        \"id\": 6109440,\n" +
            "        \"name\": \"holssewebsocket\",\n" +
            "        \"owner\": {\n" +
            "          \"login\": \"jersey\",\n" +
            "          \"id\": 399710,\n" +
            "          \"site_admin\": false\n" +
            "        },\n" +
            "        \"urls\": [\n" +
            "          \"https://api.github.com/repos/jersey/hol\",\n" +
            "          \"https://api.github.com/repos/jersey/hol/forks\",\n" +
            "          \"https://api.github.com/repos/jersey/hol/teams\",\n" +
            "        ],\n" +
            "        \"has_wiki\": true\n" +
            "      }\n" +
            "    ]\n" +
            "  };\n" +
            "})\n";

        // Evaluate the JavaScript function definition
        Object jsFunction = engine.eval(src);

        // Execute the JavaScript function to create the "mock parser"
        Object jsMockParser = ((Invocable) engine).invokeMethod(jsFunction, "call");

        // Create Java-typed access to the "mock parser"
        ParseJSON mockParser = ((Invocable) engine).getInterface(jsMockParser, ParseJSON.class);

        List<Repository> repos = mockParser.parse();
        assertEquals("One repo", 1, repos.size());
        assertEquals("holssewebsocket", repos.get(0).name());
        assertTrue("wiki", repos.get(0).has_wiki());
        assertEquals("3 urls", 3, repos.get(0).urls().size());
        final String url1 = repos.get(0).urls().get(0);
        assertEquals("1st", "https://api.github.com/repos/jersey/hol", url1);

        Owner owner = repos.get(0).owner();
        assertNotNull("Owner exists", owner);

        assertEquals("login", "jersey", owner.login());
        assertEquals("id", 399710, owner.id());
        assertFalse(owner.site_admin());
    }
    // @end region="accessJavaScriptJSONObjectFromJava"

    public void testHandleScriptException() throws Exception {
        handleScriptExceptions();
    }
    
    public static class Callback {
        public String next(List<String> l) {
            return l.iterator().next();
        }
        
        public void io() throws IOException {
            throw new IOException("");
        }
    }
    
    // Checkstyle: stop
    public void handleScriptExceptions() throws Exception {
        // @start region="handleScriptExceptions"
        // this is error in Javascript (null dereference), so ScriptException will be thrown,
        // with scripting engine's implementation exception inside.
        try {
            engine.eval(
                 "var a = null;\n"
                + "a.call(null)");
        } catch (ScriptException ex) {
        }
        
        // The callback will throw a checked exception - something that happens
        // "outside" the script in the runtime: will throw RuntimeException subclass
        // with the real exception set as cause.
        Callback cb = new Callback();
        try {
            Object jsFunction = engine.eval(
                  "(function(cb) {"
                + " cb.io();"
                + "})"
            );
            ((Invocable) engine).invokeMethod(jsFunction, "call", null, cb);
        } catch (RuntimeException ex) {
            // this is a checked java exception; it's just wrapped into a
            // RuntimeException:
            assertTrue(ex.getCause() instanceof IOException);
        } catch (Exception ex) {
            fail("Runtime expected");
        }
        
        // the last exception is a runtime exception originating from java.
        // it will be reported 'as is' or wrapped, depending on the engine
        try {
            Object jsFunction = engine.eval(
                  "(function(cb, l) {"
                + " cb.next(l);"
                + "})"
            );
            ((Invocable) engine).invokeMethod(jsFunction, "call", null, cb, new LinkedList());
        } catch (NoSuchElementException ex) {
            // this is a checked java exception; it's thrown unchanged.
        } catch (RuntimeException ex) {
            // ... or wrapped in a Runtime:
            assertTrue(ex.getCause() instanceof NoSuchElementException);
        } catch (Exception ex) {
            fail("NoSuchElement expected");
        }
        // @end region="handleScriptExceptions"
    }
}
