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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import org.netbeans.spi.scripting.EngineProvider;
import org.openide.util.Lookup;

/** NetBeans aware access to {@link ScriptEngineManager} manager.
 * Rather than using JDK's {@link ScriptEngineManager} manager directly,
 * instantiate it via {@link #createManager()} method. This method is aware
 * of NetBeans specific runtime configurations. It uses the right classloader
 * as well as specific discovery mechanisms to locate additional
 * implementations of {@link ScriptEngineFactory}. To execute a JavaScript
 * code use:
 * <p>
 * {@snippet file="org/netbeans/api/scripting/ScriptingTutorialTest.java" region="testFourtyTwo"}
 * <p>
 * Consult <a href="@org-netbeans-libs-graalsdk@/org/netbeans/libs/graalsdk/package-summary.html">scripting tutorial</a>
 * to learn more about advanced polyglot scripting topics.
 *
 * @since 1.0
 */
public final class Scripting {
    private boolean allowAllAccess;

    private Scripting() {
    }

    /** Create new {@link ScriptEngineManager} configured for the NetBeans
     * environment. The manager serves as an <em>isolated</em> environment -
     * engines created from the same manager are supposed to share the
     * same internals and be able to communicate with each other.
     *
     * @return new instance of the engine manager
     */
    public static ScriptEngineManager createManager() {
        return newBuilder().build();
    }

    /**
     * A builder to configure and create new instance of {@link ScriptEngineManager}.
     *
     * @return a builder object with {@link #build()} method
     * @since 1.2
     */
    public static Scripting newBuilder() {
        return new Scripting();
    }

    /** Allows the scripts to access JVM classes. By default the scripts
     * run in as restricted environment as possible. See
     * <a href="@org-netbeans-libs-graalsdk@/org/netbeans/libs/graalsdk/package-summary.html">scripting tutorial</a>
     * for details. That is the prefered mode of execution. However,
     * if your script is known and trusted, you may allow it to access
     * classes and features in the JVM. For example it is common in Nashorn scripts
     * to use:
     * 
     * {@snippet file="org/netbeans/api/scripting/JavaScriptEnginesTest.java" region="allowLoadAClassInJS"}
     * 
     * Such classloading is prevented by default. To allow it, specify {@code true}
     * in here.
     * <p>
     * {@link ScriptEngineManager} created with all access on, has a boolean property
     * in its {@link ScriptEngineManager#getBindings()}:
     * {@snippet file="org/netbeans/api/scripting/ScriptingTest.java" region="testBuilderAllowAccess"}
     *
     * @param allAccess allow access to JVM internals from the script
     * @return instance of {@code this} builder
     * @since 1.2
     */
    public Scripting allowAllAccess(boolean allAccess) {
        this.allowAllAccess = allAccess;
        return this;
    }

    /** Create new {@link ScriptEngineManager} configured for the NetBeans
     * environment. The manager serves as an <em>isolated</em> environment -
     * engines created from the same manager are supposed to share the
     * same internals and be able to communicate with each other.
     *
     * @return new instance of the engine manager
     * @since 1.2
     */
    public ScriptEngineManager build() {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        if (l == null) {
            l = Thread.currentThread().getContextClassLoader();
        }
        if (l == null) {
            l = Scripting.class.getClassLoader();
        }
        return new EngineManager(allowAllAccess, l);
    }

    private static final class EngineManager extends ScriptEngineManager {
        private final List<ScriptEngineFactory> extra;
        private final boolean allowAllAccess;

        EngineManager(boolean allowAllAccess, ClassLoader loader) {
            super(loader);
            this.allowAllAccess = allowAllAccess;
            if (allowAllAccess) {
                getBindings().put("allowAllAccess", true); // NOI18N
            }
            this.extra = populateExtras(this);
            for (ScriptEngineFactory f : extra) {
                registerEngineName(f.getEngineName(), f);
                for (String ext : f.getExtensions()) {
                    registerEngineExtension(ext, f);
                }
                for (String mime : f.getMimeTypes()) {
                    registerEngineMimeType(mime, f);
                }
            }
        }

        private static List<ScriptEngineFactory> populateExtras(EngineManager m) {
            List<ScriptEngineFactory> extra = new ArrayList<>();
            for (EngineProvider p : Lookup.getDefault().lookupAll(EngineProvider.class)) {
                extra.addAll(p.factories(m));
            }
            return Collections.unmodifiableList(extra);
        }

        @Override
        public List<ScriptEngineFactory> getEngineFactories() {
            List<ScriptEngineFactory> all = new ArrayList<>();
            all.addAll(super.getEngineFactories());
            all.addAll(extra);
            ListIterator<ScriptEngineFactory> it = all.listIterator();
            while (it.hasNext()) {
                ScriptEngineFactory f = it.next();
                if (f.getNames().contains("Graal.js") || isNashornFactory(f)) { // NOI18N
                    it.set(new GraalJSWrapperFactory(f));
                }
            }
            return all;
        }

        @Override
        public ScriptEngine getEngineByExtension(String extension) {
            return postConfigure(super.getEngineByExtension(extension));
        }

        @Override
        public ScriptEngine getEngineByMimeType(String mimeType) {
            return postConfigure(super.getEngineByMimeType(mimeType));
        }

        @Override
        public ScriptEngine getEngineByName(String shortName) {
            return postConfigure(super.getEngineByName(shortName));
        }

        private ScriptEngine postConfigure(ScriptEngine eng) {
            if (eng == null) {
                return null;
            }
            if (eng.getFactory().getNames().contains("Graal.js")) { // NOI18N
                final Bindings b = eng.getBindings(ScriptContext.ENGINE_SCOPE);
                if (allowAllAccess) {
                    b.put("polyglot.js.nashorn-compat", true); // NOI18N
                }
                b.put("polyglot.js.allowHostAccess", true); // NOI18N
                b.put("polyglot.js.allowHostClassLookup", (Predicate<String>) (s) -> { // NOI18N
                    return allowHostClassLookup(eng, s);
                });
            }
            if (isNashornFactory(eng.getFactory())) {
                return secureEngineEngine(eng);
            }
            return eng;
        }


        private static final Class<?> nashornScriptEngineFactory;
        static {
            Class<?> klass;
            try {
                klass = Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory"); // NOI18N
            } catch (ClassNotFoundException ex) {
                klass = String.class;
            }
            nashornScriptEngineFactory = klass;
        }
        private boolean isNashornFactory(ScriptEngineFactory f) {
            return nashornScriptEngineFactory.isInstance(f);
        }

        private ScriptEngine secureEngineEngine(ScriptEngine prototypeEngine) {
            final ScriptEngine[] engine = { prototypeEngine };
            try {
                ScriptEngineFactory f = engine[0].getFactory();
                final Class<? extends ScriptEngineFactory> factoryClass = f.getClass();
                final ClassLoader factoryClassLoader = factoryClass.getClassLoader();
                Class<?> filterClass = Class.forName("jdk.nashorn.api.scripting.ClassFilter", true, factoryClassLoader); // NOI18N
                Method createMethod = factoryClass.getMethod("getScriptEngine", filterClass); // NOI18N
                Object filter = java.lang.reflect.Proxy.newProxyInstance(factoryClassLoader, new Class[]{filterClass}, (Object proxy, Method method, Object[] args) -> {
                    return allowHostClassLookup(engine[0], (String) args[0]);
                });
                engine[0] = (ScriptEngine) createMethod.invoke(f, filter);
                return engine[0];
            } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                return engine[0];
            }
        }

        private boolean allowHostClassLookup(final ScriptEngine engine, String className) {
            return allowAllAccess;
        }

        private final class GraalJSWrapperFactory implements ScriptEngineFactory {
            private final ScriptEngineFactory original;

            GraalJSWrapperFactory(ScriptEngineFactory original) {
                this.original = original;
            }

            @Override
            public String getEngineName() {
                return original.getEngineName();
            }

            @Override
            public String getEngineVersion() {
                return original.getEngineVersion();
            }

            @Override
            public List<String> getExtensions() {
                return original.getExtensions();
            }

            @Override
            public List<String> getMimeTypes() {
                return original.getMimeTypes();
            }

            @Override
            public List<String> getNames() {
                return original.getNames();
            }

            @Override
            public String getLanguageName() {
                return original.getLanguageName();
            }

            @Override
            public String getLanguageVersion() {
                return original.getLanguageVersion();
            }

            @Override
            public Object getParameter(String key) {
                return original.getParameter(key);
            }

            @Override
            public String getMethodCallSyntax(String obj, String m, String... args) {
                return original.getMethodCallSyntax(obj, m, args);
            }

            @Override
            public String getOutputStatement(String toDisplay) {
                return original.getOutputStatement(toDisplay);
            }

            @Override
            public String getProgram(String... statements) {
                return original.getProgram(statements);
            }

            @Override
            public ScriptEngine getScriptEngine() {
                return postConfigure(original.getScriptEngine());
            }
        }
    }
}
