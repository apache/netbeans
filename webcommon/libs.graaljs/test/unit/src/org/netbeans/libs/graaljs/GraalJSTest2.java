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
package org.netbeans.libs.graaljs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import org.graalvm.polyglot.Context;
import static org.junit.Assume.assumeFalse;
import org.junit.AssumptionViolatedException;
import org.netbeans.api.scripting.Scripting;
import org.netbeans.junit.NbTestCase;
import org.netbeans.libs.graalsdk.GraalSDK;
import org.netbeans.libs.graalsdk.JavaScriptEnginesTest;
import org.netbeans.libs.graalsdk.JavaScriptEnginesTest2;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Lookup;

public final class GraalJSTest2 extends NbTestCase {
    public GraalJSTest2(String name) {
        super(name);
    }

    private ClassLoader createGraalDependentClassLoader() {
        ClassLoader allLoader = Lookup.getDefault().lookup(ClassLoader.class);
        Collection<? extends ModuleInfo> modules = new ArrayList<>(Lookup.getDefault().lookupAll(ModuleInfo.class));
        ModuleInfo thisModule = Modules.getDefault().ownerOf(GraalSDK.class);
        Map<String, ModuleInfo> dependentsOfSDK = new LinkedHashMap<>();
        if (thisModule == null) {
            // this happens only when mod system is not active, i.e. in tests.
            return allLoader;
        }
        dependentsOfSDK.put(thisModule.getCodeName(), thisModule);
        
        boolean added;
        do {
            added = false;
            for (Iterator<? extends ModuleInfo> it = modules.iterator(); it.hasNext(); ) {
                ModuleInfo m = it.next();
                for (Dependency d : m.getDependencies()) {
                    if (d.getType() == Dependency.TYPE_MODULE) {
                        if (dependentsOfSDK.keySet().contains(d.getName())) {
                            dependentsOfSDK.put(m.getCodeName(), m);
                            it.remove();
                            added = true;
                            break;
                        }
                    }
                }
            }
        } while (!added);
        
        ClassLoader created;
        try {
            BiFunction<String, ClassLoader, Boolean> decideDelegation = (n, c) -> {
                System.err.println(n);
                if (n.startsWith("java/") || n.startsWith("junit/")) {
                    return true;
                }
                return c != null;
            };
            // this is a hack that allows to use good implementation of a classloader ...
            // there should have to be an API for this in the module system.
            Class pcl = Class.forName("org.netbeans.ProxyClassLoader", true, allLoader);
            Constructor ctor = pcl.getConstructor(ClassLoader[].class, Boolean.TYPE, BiFunction.class);
            ClassLoader[] delegates = new ClassLoader[dependentsOfSDK.size()];
            int index = delegates.length -1;
            // reverse the order: in the LinkedHashMap, the 1st entry is GraalSDK, following by direct dependents
            // if some of module deeper in the hierarchy masks JDK packages, it should be consulted first, so the
            for (ModuleInfo mi : dependentsOfSDK.values()) {
                delegates[index--] = mi.getClassLoader();
            }
            created = (ClassLoader)ctor.newInstance(delegates, true, decideDelegation);
        } catch (ReflectiveOperationException ex) {
            created = allLoader;
        }
        return created;
    }
    
    /**
     * Checks direct invocation of JS using Polyglot API from within module system. Works only on GraalVM 11+
     * @throws Exception 
     */
    public void testDirectEvaluationOfGraalJS() throws Exception {
        // GraalVM 8 JVMCI creates directly Polyglot Impl from the JDK, for GraalVM 8, must be tested elsewhere:
        String specVersion = System.getProperty("java.specification.version"); //NOI18N
        String vmVersion = System.getProperty("java.vm.version"); //NOI18N
        assumeFalse("GraalVM 8 requires direct testing from app classloader", "1.8".equals(specVersion) && vmVersion.contains("jvmci-"));

        ClassLoader ldr = createGraalDependentClassLoader();
        Thread.currentThread().setContextClassLoader(ldr);
        
        // the test code itself HAS to use the module system to load appropriate Engine.
        URL u = getClass().getProtectionDomain().getCodeSource().getLocation();
        ClassLoader ldr2 = new URLClassLoader(new URL[] { u }, ldr);
        Callable c = (Callable)ldr2.loadClass(getClass().getName() + "$T").getDeclaredConstructor().newInstance();
        c.call();
    }
    
    public static class T implements Callable {
        @Override
        public Object call() throws Exception {
            Context ctx = Context.newBuilder("js").build();
            int fourtyTwo = ctx.eval("js", "6 * 7").asInt();
            assertEquals(42, fourtyTwo);
            return null;
        }
    }
    
    public void testJavaScriptEngineIsGraalJS() {
        ScriptEngineManager m = Scripting.createManager();
        StringBuilder sb = new StringBuilder();
        for (ScriptEngineFactory f : m.getEngineFactories()) {
            sb.append("\nf: ").append(f.getEngineName()).append(" ext: ").append(f.getMimeTypes());
        }
        ScriptEngine text = m.getEngineByMimeType("text/javascript");
        assertEquals(sb.toString(), "GraalVM:js", text.getFactory().getEngineName());

        ScriptEngine app = m.getEngineByMimeType("application/javascript");
        assertEquals(sb.toString(), "GraalVM:js", app.getFactory().getEngineName());
    }

    public void testDeleteASymbol() throws Exception {
        ScriptEngine eng = Scripting.createManager().getEngineByName("GraalVM:js");
        Object function = eng.eval("typeof isFinite");
        eng.eval("delete isFinite");
        Object undefined = eng.eval("typeof isFinite");

        assertEquals("Defined at first", "function", function);
        assertEquals("Deleted later", "undefined", undefined);
    }

    public void testAllJavaScriptEnginesTest() throws Throwable {
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        boolean err = false;
        Method[] testMethods = JavaScriptEnginesTest.class.getMethods();
        for (Method m : testMethods) {
            final org.junit.Test ann = m.getAnnotation(org.junit.Test.class);
            if (ann == null) {
                continue;
            }
            ScriptEngine eng = Scripting.createManager().getEngineByName("GraalVM:js");
            err |= invokeTestMethod(eng, false, pw, m, ann);
            ScriptEngine engAllow = Scripting.newBuilder().allowAllAccess(true).build().getEngineByName("GraalVM:js");
            err |= invokeTestMethod(engAllow, true, pw, m, ann);
        }
        pw.flush();
        if (err) {
            fail(w.toString());
        }
    }

    private static boolean invokeTestMethod(ScriptEngine eng, final boolean allowAllAccess, PrintWriter pw, Method m, final org.junit.Test ann) throws IllegalAccessException, IllegalArgumentException {
        JavaScriptEnginesTest2 instance = new JavaScriptEnginesTest2(m.getName(), "GraalVM:js", null, null, eng, allowAllAccess);
        try {
            pw.println("Invoking " + m.getName() + " allowAllAccess: " + allowAllAccess);
            m.invoke(instance);
        } catch (InvocationTargetException invEx) {
            if (invEx.getCause() instanceof AssumptionViolatedException) {
                return false;
            }
            if (ann.expected().equals(invEx.getCause().getClass())) {
                pw.println("Expected exception received " + ann.expected().getName());
            } else {
                invEx.getCause().printStackTrace(pw);
                return true;
            }
        }
        return false;
    }
}
