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

import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.scripting.EngineProvider;
import org.openide.util.lookup.ServiceProvider;

public class ScriptingTest {

    public ScriptingTest() {
    }

    @Test
    public void testCreateManager() {
        MockServices.setServices(MyProvider.class);

        ScriptEngineManager man = Scripting.createManager();
        assertMyFactory(man.getEngineFactories());

        assertNotNull(man.getEngineByName("testEngine"), "Test engine found");

        assertNotNull(man.getEngineByMimeType("application/x-ng"), "Ng engine found");
        assertNotNull(man.getEngineByMimeType("application/x-junit"), "JUnit engine found");

        assertNotNull(man.getEngineByExtension("ng"), "Ng engine found by ext");
        assertNotNull(man.getEngineByExtension("junit"), "JUnit engine found by ext");

    }

    @Test
    public void testBuilderAllowAccess() {
        // @start region="testBuilderAllowAccess"
        ScriptEngineManager manager = Scripting.newBuilder().
            allowAllAccess(true).build();
        assertEquals(Boolean.TRUE, manager.getBindings().get("allowAllAccess"));
        // @end region="testBuilderAllowAccess"
    }

    private static void assertMyFactory(List<ScriptEngineFactory> factories) {
        for (ScriptEngineFactory f : factories) {
            if (f instanceof MyFactory) {
                return;
            }
        }
        org.junit.Assert.fail("Cannot find MyFactory in " + factories);
    }

    private static void assertNotNull(Object obj, String msg) {
        org.junit.Assert.assertNotNull(msg, obj);
    }

    @ServiceProvider(service = EngineProvider.class)
    public static final class MyProvider implements EngineProvider {
        @Override
        public List<ScriptEngineFactory> factories() {
            final ScriptEngineFactory factory = new MyFactory();
            return Arrays.asList(factory);
        }
    }

    private static final class MyFactory implements ScriptEngineFactory {
        @Override
        public String getEngineName() {
            return "testEngine";
        }

        @Override
        public String getEngineVersion() {
            return "0.0.1";
        }

        @Override
        public List<String> getExtensions() {
            return Arrays.asList("ng", "junit");
        }

        @Override
        public List<String> getMimeTypes() {
            return Arrays.asList("application/x-ng", "application/x-junit");
        }

        @Override
        public List<String> getNames() {
            return Arrays.asList("NG", "JUnit");
        }

        @Override
        public String getLanguageName() {
            return "testing";
        }

        @Override
        public String getLanguageVersion() {
            return "0.1";
        }

        @Override
        public Object getParameter(String key) {
            return null;
        }

        @Override
        public String getMethodCallSyntax(String obj, String m, String... args) {
            return null;
        }

        @Override
        public String getOutputStatement(String toDisplay) {
            return null;
        }

        @Override
        public String getProgram(String... statements) {
            return null;
        }

        @Override
        public ScriptEngine getScriptEngine() {
            return new MyEngine(this);
        }
    }

    private static class MyEngine implements ScriptEngine {
        private final ScriptEngineFactory factory;

        MyEngine(ScriptEngineFactory factory) {
            this.factory = factory;
        }

        @Override
        public Object eval(String script, ScriptContext context) throws ScriptException {
            throw new ScriptException(script);
        }

        @Override
        public Object eval(Reader reader, ScriptContext context) throws ScriptException {
            throw new ScriptException(reader.toString());
        }

        @Override
        public Object eval(String script) throws ScriptException {
            throw new ScriptException(script);
        }

        @Override
        public Object eval(Reader reader) throws ScriptException {
            throw new ScriptException(reader.toString());
        }

        @Override
        public Object eval(String script, Bindings n) throws ScriptException {
            throw new ScriptException(script);
        }

        @Override
        public Object eval(Reader reader, Bindings n) throws ScriptException {
            throw new ScriptException(reader.toString());
        }

        @Override
        public void put(String key, Object value) {
        }

        @Override
        public Object get(String key) {
            return null;
        }

        @Override
        public Bindings getBindings(int scope) {
            return null;
        }

        @Override
        public void setBindings(Bindings bindings, int scope) {
        }

        @Override
        public Bindings createBindings() {
            return null;
        }

        @Override
        public ScriptContext getContext() {
            return null;
        }

        @Override
        public void setContext(ScriptContext context) {
        }

        @Override
        public ScriptEngineFactory getFactory() {
            return factory;
        }
    }
}
