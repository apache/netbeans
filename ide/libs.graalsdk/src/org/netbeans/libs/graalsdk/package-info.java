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

/** Polyglot scripting tutorial.
 *
 * <h2 id="tutorial">Polyglot Scripting Tutorial</h2>
 *
 * This tutorial shows how to embed the various languages in a
 * NetBeans or even plain Java application via {@link org.netbeans.api.scripting.Scripting} helper methods. This
 * environment lets Java interoperate with standard as well as
 * <a href="https://graalvm.org">GraalVM</a> based
 * <em>guest languages</em> via <em>foreign objects</em> and <em>foreign functions</em>.
 * For example Java code
 * can directly access guest language methods, objects, classes,
 * and some complex data structures
 * with Java-typed accessors. In the reverse direction, guest language code can access Java objects,
 * classes, and constructors.
 * <p>
 * This tutorial helps you get started, starting with setup instructions, followed by descriptions of
 * different interoperation scenarios with (working) code examples.
 *
 *
 * <h3>Contents</h3>
 *
 * <div id="toc"></div>
 * <div id="contents">
 *
 * <h3>Setup</h3>
 *
 * The most advanced features that this API provides work in cooperation with
 * <a href="https://graalvm.org">GraalVM</a>.
 * Downloading
 * <a href="https://graalvm.org">GraalVM</a> and running your (NetBeans) application on
 * <a href="https://graalvm.org">GraalVM</a> will give you access to the
 * polyglot features highlighted in this tutorial.
 * <p>
 * NetBeans modules are <a href="https://search.maven.org/search?q=org.netbeans.api">
 * uploaded to Maven central</a>. You can use them from your <em>pom.xml</em>
 * file as:
 * </p>
 * <pre>
&lt;dependency&gt;
    &lt;groupId&gt;<b>org.netbeans.api</b>&lt;/groupId&gt;
    &lt;artifactId&gt;<b>scripting</b>&lt;/artifactId&gt;
    &lt;version&gt;11&lt;/version&gt; <em>&lt;!-- or any later version --&gt;</em>
&lt;/dependency&gt;
 * </pre>
 *
 * <h3>Get started</h3>
 *
 * <h4>Guest language "Hello World!"</h4>
 *
 * Integrating scripting into your Java application starts with building
 * an instance of {@link javax.script.ScriptEngineManager} via
 * {@link org.netbeans.api.scripting.Scripting#createManager()} helper method.
 * You can then use the engine to
 * {@link javax.script.ScriptEngine#eval evaluate}
 * guest language source code.
 * <p>
 * The following example evaluates a Python source, let it print a message,
 * returns it, and then "casts" the result to a Java string.
 *
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="testHelloWorld"}
 *
 * <h4>It's a polyglot world</h4>
 *
 * How to list all available languages? To obtain all registered engine
 * factories in the system use:
 *
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="listAll"}
 * <p>
 * When the above code snippet is executed on <a href="https://graalvm.org">GraalVM</a> it may print:
 * <pre>
Found Oracle Nashorn
Found Graal.js
Found GraalVM:js
Found GraalVM:llvm
Found GraalVM:python
 * </pre>
 * e.g. a mixture of standard script engines in the JDK with additional ones
 * provided as <a href="https://graalvm.org">GraalVM</a> languages located
 * via dedicated implementation of {@link org.netbeans.spi.scripting.EngineProvider}
 * interface
 *
 * <h4>Add a language</h4>
 *
 * <a href="https://graalvm.org">GraalVM</a>
 * download comes with highly efficient implementations of <em>JavaScript</em>.
 * Additional languages like
 * <a href="https://github.com/graalvm/truffleruby">Ruby</a>,
 * the <a href="https://github.com/graalvm/fastr">R</a> and
 * <a href="https://github.com/graalvm/graalpython">Python</a>
 * can be installed via the <code>bin/gu</code> <em>Graal Updater</em> tool:
 *
 * <pre>
 * $ /graalvm/bin/gu available
 * Downloading: Component catalog
 * ComponentId              Version             Component name
 * ----------------------------------------------------------------
 * python                   1.0.0-rc9           Graal.Python
 * R                        1.0.0-rc9           FastR
 * ruby                     1.0.0-rc9           TruffleRuby
 * $ /graalvm/bin/gu install python
 * Downloading: Component catalog
 * Processing component archive: Component python
 * </pre>
 *
 * After invoking this command and downloading the bits, the JVM will be
 * ready to execute Python scripts.
 *
 * <h4>Hello World in Python and JavaScript</h4>
 *
 * The {@link org.netbeans.api.scripting.Scripting#createManager()} Scripting.createManager()} method
 * is your gateway to polyglot world! Just create a manager and it can serve
 * as a hub where various language engines connect together. Following example
 * shows JavaScript and Python interacting with each other:
 *
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="testHelloWorldInPythonAndJavaScript"}
 * <p>
 * Languages provided by <a href="https://graalvm.org">GraalVM</a> use the
 * {@link javax.script.ScriptEngineManager} created by
 * {@link org.netbeans.api.scripting.Scripting#createManager()} factory
 * method as a connection point to talk to each other and mutually share
 * and use its objects, functions and other services.
 *
 * <h4>Cast Array to List</h4>
 *
 * Dynamic languages may represent array in their own special ways.
 * However the
 * {@link org.netbeans.api.scripting.Scripting} interoperation let's you
 * view each array-like object as {@link java.util.List}. Just
 * "{@linkplain javax.script.Invocable#getInterface(java.lang.Object, java.lang.Class) cast it}"
 * like in following example:
 *
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="testCastJsArray"}
 *
 *
 * <h3>Call guest language functions from Java</h3>
 *
 * {@link org.netbeans.api.scripting.Scripting} interoperation lets Java call
 * <em>foreign functions</em> that guest language code <em>exports</em>
 * (details vary across languages).
 * This section presents a few examples.
 *
 * <h4>Define and call a JavaScript function</h4>
 *
 * A function exported from a dynamic language becomes a callable <em>foreign function</em>
 * by giving it a Java type, for example the Java interface {@code Multiplier} in the following code.
 * <p>
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="callJavaScriptFunctionFromJava"}
 *
 * Notes:
 * <ul>
 * <li>Evaluating the JS source returns an anonymous JS function of two arguments
 * represented as {@link java.lang.Object} that can be
 * {@linkplain javax.script.Invocable#getInterface(java.lang.Object, java.lang.Class) "cast"}
 * to a <em>foreign function</em> with a Java type.</li>
 * <li>Parentheses around the JS function definition keep it out of JavaScript's
 * global scope, so the Java object holds the only reference to it.</li>
 * </ul>
 *
 * <h4>Define and call a Python function</h4>
 *
 * The same example can be rewritten to Python:
 * <p>
 *
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="testPythonFunctionFromJava"}
 *
 * Notes:
 * <ul>
 * <li>Evaluating the Python source defines a {@code mul} function and also
 * returns it as a value to the Java code that can
 * {@linkplain javax.script.Invocable#getInterface(Class) "cast"}
 * it to a <em>foreign function</em> with a given Java type.</li>
 * </ul>
 *
 * <h4>Call an existing R function</h4>
 *
 * In this sample we use a reference to existing R function {@code qbinom} from the built-in <em>stats</em> package.
 * <p>
 *
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="callRFunctionFromJava"}
 *
 * Don't forget to install support for the R language into your <a href="https://graalvm.org">GraalVM</a>
 * instance:
 *
 * <pre>
 * $ /graalvm/bin/gu install
 * </pre>
 *
 * <h3>Call multiple guest language functions with shared state from Java</h3>
 *
 * Often it is necessary to export multiple dynamic language functions that work
 * together, for example by sharing variables.  This can be done by giving
 * an exported group of functions a Java type with more than a single method,
 * for example the Java interface {@code Counter} in the following code.
 * <p>
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="callJavaScriptFunctionsWithSharedStateFromJava"}
 *
 * Notes:
 * <ul>
 * <li>Evaluating the JS source returns an anonymous JS function of no arguments
 * assigned to {@code jsFunction}) that can be
 * {@linkplain javax.script.Invocable#invokeMethod executed directly},
 * without giving it a Java type.</li>
 * <li>Executing {@code jsFunction} returns a JS dynamic object (containing two methods
 * and a shared variable)
 * that can be {@linkplain javax.script.Invocable#getInterface "cast"}
 * to a <em>foreign object</em> with a Java type.</li>
 * <li>Parentheses around the JS function definition keep it out of JavaScript's
 * global scope, so the Java object holds the only reference to it.</li>
 * </ul>
 *
 * <h3>Access guest language classes from Java</h3>
 *
 * <h4>Access a JavaScript class</h4>
 *
 * The ECMAScript 6 specification adds the concept of typeless classes to JavaScript.
 * NetBeans {@link org.netbeans.api.scripting.Scripting}
 * interoperation allows Java to access fields and functions of a JavaScript class,
 * for example the <em>foreign function</em> factory and class given the Java type
 * {@code Incrementor} in the following code.
 * <p>
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="callJavaScriptClassFactoryFromJava"}
 *
 * Notes:
 * <ul>
 * <li>Evaluating the JS source returns an anonymous JS function of no arguments assigned
 * to {@code jsFunction} that can be
 * {@linkplain javax.script.Invocable#invokeMethod executed directly},
 * without giving it a Java type.</li>
 * <li>Executing {@code jsFunction} returns a JS factory method for class
 * {@code JSIncrementor} that can also be executed directly.</li>
 * <li>Executing the JS factory returns a JS object that can
 * be {@linkplain javax.script.Invocable#getInterface "cast"}
 * to a <em>foreign object</em> with the Java type {@code Incrementor}.</li>
 * <li>Parentheses around the JS function definition keep it out of JavaScript's
 * global scope, so the Java object holds the only reference to it.</li>
 * </ul>
 *
 * <h3>Access guest language data structures from Java</h3>
 *
 * The method {@link javax.script.Invocable#getInterface invocable.getInterface(Class)}
 * plays an essential role supporting interoperation between Java and guest language data
 * structures.
 * This section presents a few examples.
 *
 * <h4>Access a JavaScript Array</h4>
 *
 * The following example demonstrates type-safe Java foreign access
 * to members of a JavaScript array with members of a known type,
 * accessed as a Java {@link java.util.List} of objects with type given by interface {@code Point}.
 * <p>
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="accessJavaScriptArrayWithTypedElementsFromJava"}
 *
 * Notes:
 * <ul>
 * <li>Evaluating the JS source returns an anonymous JS function of no arguments
 * assigned to {@code jsFunction}) that can be
 * {@linkplain javax.script.Invocable#getInterface "cast"}
 * to a <em>foreign function</em> with Java type {@code PointProvider}.</li>
 * <li>Invoking the foreign function (assigned to {@code pointProvider}) creates
 * a JS array, which is returned as a <em>foreign object</em>
 * with Java type {@code List<Point>}.</li>
 * <li>Parentheses around the JS function definition keep it out of JavaScript's
 * global scope, so the Java object holds the only reference to it.</li>
 * </ul>
 *
 * <h4>Access a JavaScript JSON structure</h4>
 *
 * This example demonstrates type-safe Java foreign access to a JavaScript JSON-like
 * structure, based on JSON data returned by a GitHub API.
 * The GitHub response contains a list of repository objects. Each repository has an id,
 * name, list of URLs, and a nested structure describing its owner. Java interfaces
 * {@code Repository} and {@code Owner} define the structure as Java types.
 * <p>
 * The following Java code is able to inspect a JavaScript JSON data structure
 * generated by "mock parser" in a type-safe way.
 * <p>
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="accessJavaScriptJSONObjectFromJava"}
 *
 * Notes:
 * <ul>
 * <li>Evaluating the JS source returns an anonymous JS function of no arguments assigned
 * to {@code jsFunction} that can be
 * {@linkplain javax.script.Invocable#invokeMethod executed directly},
 * without giving it a Java type.</li>
 * <li>Executing {@code jsFunction} returns a JS mock JSON parser function
 * (assigned to {@code jsMockParser}), that can be
 * {@linkplain javax.script.Invocable#getInterface "cast"}
 * to a <em>foreign function</em> with Java type {@code ParseJSON}.</li>
 * <li>Calling the Java-typed mock parser creates a JS data structure, which is
 * returned as a <em>foreign object</em> with Java type {@code List<Repository>}.
 * <li>Parentheses around the JS function definition keep it out of JavaScript's
 * global scope, so the Java object holds the only reference to it.</li>
 * </ul>
 *
 * <h4>View any Object as Map</h4>
 *
 * Each dynamic object coming from a <a href="https://graalvm.org">GraalVM</a>
 * language can be "cast" to a {@link java.util.Map}. Here is an example:
 *
 * <p>
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="testCastPythonObj"}
 *
 * While not type-safe, it is a generic approach able to inspect objects
 * of unknown structure.
 *
 * <h3>Access Java from guest languages</h3>
 *
 * Just like Java can access guest language objects, the guest languages may
 * access Java objects, their fields and call their methods. Few examples
 * follow.
 *
 * <h4>Access Java fields and methods from JavaScript</h4>
 *
 * <em>Public</em> members of Java objects can be exposed to guest language code
 * as <em>foreign objects</em>, for example Java objects of type {@code Moment} in
 * the following example.
 * <p>
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="accessFieldsOfJavaObject"}
 *
 * Notes:
 * <ul>
 * <li>Evaluating the JS source returns an anonymous JS function of one argument
 * assigned to {@code jsFunction} that can be executed directly with one argument.</li>
 * <li>The Java argument {@code javaMoment} is seen by the JS function as a
 * <em>foreign object</em> whose public fields are visible.
 * <li>Executing {@code jsFunction} returns a JS number
 * that can be
 * {@linkplain javax.script.Invocable#getInterface "cast"}
 * to a Java {@link java.lang.Number} and then to a Java {@code int}.</li>
 * <li>Parentheses around the JS function definition keep it out of JavaScript's
 * global scope, so the Java object holds the only reference to it.</li>
 * </ul>
 * <p>
 * The multiple steps needed to convert the result in the above example
 * produces awkward code that can be simplified.
 * Instead of invoking the JS function directly, and "casting" the wrapped JS result,
 * we can instead
 * {@linkplain javax.script.Invocable#getInterface "cast"}
 * the JS function to a Java foreign function (of type {@code MomentConverter}) that
 * returns the desired Java type directly, as shown in the following variation.
 *
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="accessFieldsOfJavaObjectWithConverter"}
 *
 * <h4>Access Java constructors and static methods from JavaScript</h4>
 *
 * Dynamic languages can also access the <em>public constructors</em> and <em>public static methods</em>
 * of any Java class for which they are provided a reference.
 * The following example shows JavaScript access to the public constructor of a Java
 * class.
 * <p>
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="createJavaScriptFactoryForJavaClass"}
 *
 * Notes:
 * <ul>
 * <li>Evaluating the JS source returns an anonymous JS function of one argument
 * assigned to {@code jsFunction} that can be executed directly with one argument.</li>
 * <li>The Java class argument {@code Moment.class} is seen by the JS function as a
 * <em>foreign class</em> whose public constructor is visible.
 * <li>Executing {@code jsFunction} with the Java class argument returns
 * a JS "factory" function (for the Java class) that can be
 * {@linkplain javax.script.Invocable#getInterface "cast"}
 * to the desired Java function type ({@code MomentFactory}).</li>
 * <li>Parentheses around the JS function definition keep it out of JavaScript's
 * global scope, so the Java object holds the only reference to it.</li>
 * </ul>
 *
 * <h3>Exception handling</h3>
 * Exceptions are thrown in different way, depending on whether <b>the script</b>
 * (guest language) raised the error, or the error came from the host language, e.g.
 * a java object called from the script. 
 * <ul>
 * <li>Exceptions from the script code are always
 * reported as {@link javax.script.ScriptException} that provides the appropriate details.
 * <li>Checked exceptions from host (java) code, unhandled by the script are raised as
 * some {@link java.lang.RuntimeException} subclasses; the {@link java.lang.Throwable#getCause} then indicates
 * the original cause for the exception.
 * <li>Unchecked exceptions are directly propagated.
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="handleScriptExceptions"}
 * </ul>
 * 
 * </div>
 * <script src="doc-files/tutorial.js"></script>
 */
package org.netbeans.libs.graalsdk;
