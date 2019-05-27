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
package org.netbeans.api.scripting;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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
 * {@codesnippet org.netbeans.api.scripting.ScriptingTutorialTest#testFourtyTwo}
 * <p>
 * Consult <a href="@org-netbeans-libs-graalsdk@/org/netbeans/libs/graalsdk/package-summary.html">scripting tutorial</a>
 * to learn more about advanced polyglot scripting topics.
 *
 * @since 1.0
 */
public final class Scripting {
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
        List<ScriptEngineFactory> extra = new ArrayList<>();
        for (EngineProvider p : Lookup.getDefault().lookupAll(EngineProvider.class)) {
            extra.addAll(p.factories());
        }
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        if (l == null) {
            l = Thread.currentThread().getContextClassLoader();
        }
        if (l == null) {
            l = Scripting.class.getClassLoader();
        }
        return new EngineManager(extra, l);
    }

    private static final class EngineManager extends ScriptEngineManager {
        private final List<ScriptEngineFactory> extra;

        EngineManager(List<ScriptEngineFactory> extra, ClassLoader loader) {
            super(loader);
            this.extra = extra;
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
                b.put("polyglot.js.allowHostAccess", true); // NOI18N
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
                klass = Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory");
            } catch (ClassNotFoundException ex) {
                klass = String.class;
            }
            nashornScriptEngineFactory = klass;
        }
        private boolean isNashornFactory(ScriptEngineFactory f) {
            return nashornScriptEngineFactory.isInstance(f);
        }

        private ScriptEngine secureEngineEngine(ScriptEngine e) {
            try {
                ScriptEngineFactory f = e.getFactory();
                final Class<? extends ScriptEngineFactory> factoryClass = f.getClass();
                final ClassLoader factoryClassLoader = factoryClass.getClassLoader();
                Class<?> filterClass = Class.forName("jdk.nashorn.api.scripting.ClassFilter", true, factoryClassLoader);
                Method createMethod = factoryClass.getMethod("getScriptEngine", filterClass);
                Object filter = java.lang.reflect.Proxy.newProxyInstance(factoryClassLoader, new Class[]{filterClass}, (Object proxy, Method method, Object[] args) -> {
                    return false;
                });
                return (ScriptEngine) createMethod.invoke(f, filter);
            } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                return e;
            }
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
