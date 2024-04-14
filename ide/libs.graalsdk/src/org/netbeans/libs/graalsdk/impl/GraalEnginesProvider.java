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
package org.netbeans.libs.graalsdk.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.script.Bindings;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Language;
import org.netbeans.spi.scripting.EngineProvider;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

// The module libs.graalsdk.system defines another provider that assumes the last
// position and is able to override languages provided by this module by the system-provided 
// libraries.
@ServiceProvider(service = EngineProvider.class, position = 100000)
public final class GraalEnginesProvider implements EngineProvider {

    private static final Logger LOG = Logger.getLogger(GraalEnginesProvider.class.getName());

    private Throwable disable;

    public GraalEnginesProvider() {
    }

    @Override
    public List<ScriptEngineFactory> factories() {
        return factories(null);
    }

    @Override
    public List<ScriptEngineFactory> factories(ScriptEngineManager m) {
        List<ScriptEngineFactory> arr = new ArrayList<>();
        try {
            if (disable == null) {
                enumerateLanguages(arr, m == null ? null : m.getBindings());
            }
        } catch (IllegalStateException | LinkageError err) {
            disable = err;
        }
        return arr;
    }

    // @GuardedBy(this)
    private ClassLoader currentAllLoader;

    // @GuardedBy(this)
    private ClassLoader languagesLoader;

    /**
     * Build a classloader to load GraalVM languages. The classloader is build
     * by finding all modules that directly or transitively depend on the
     * "GraalVM SDK API" module and use that classloader to load the languages.
     *
     * @return
     */
    private ClassLoader createGraalDependentClassLoader() {
        ClassLoader allLoader = Lookup.getDefault().lookup(ClassLoader.class);
        synchronized (this) {
            if (languagesLoader != null && currentAllLoader == allLoader) {
                return languagesLoader;
            }
            languagesLoader = null;
        }
        Collection<? extends ModuleInfo> modules = new ArrayList<>(Lookup.getDefault().lookupAll(ModuleInfo.class));
        ModuleInfo thisModule = Modules.getDefault().ownerOf(getClass());
        Map<String, ModuleInfo> dependentsOfSDK = new LinkedHashMap<>();
        if (thisModule == null) {
            // this happens only when mod system is not active, i.e. in tests.
            return allLoader;
        }
        dependentsOfSDK.put(thisModule.getCodeName(), thisModule);

        boolean added;
        do {
            added = false;
            for (Iterator<? extends ModuleInfo> it = modules.iterator(); it.hasNext();) {
                ModuleInfo m = it.next();
                if (!m.isEnabled()) {
                    continue;
                }
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
        } while (added); // Loop if a new dependent module was found

        ClassLoader created;
        try {
            BiFunction<String, ClassLoader, Boolean> decideDelegation = (n, c) -> c == null ? false : true;

            // this is a hack that allows to use good implementation of a classloader ...
            // there should have to be an API for this in the module system.
            Class pcl = Class.forName("org.netbeans.ProxyClassLoader", true, allLoader);
            Constructor ctor = pcl.getConstructor(ClassLoader[].class, Boolean.TYPE, BiFunction.class);
            ClassLoader[] delegates = new ClassLoader[dependentsOfSDK.size()];
            int index = delegates.length - 1;
            // reverse the order: in the LinkedHashMap, the 1st entry is GraalSDK, following by direct dependents
            // if some of module deeper in the hierarchy masks JDK packages, it should be consulted first, so the
            for (ModuleInfo mi : dependentsOfSDK.values()) {
                delegates[index--] = mi.getClassLoader();
            }
            created = (ClassLoader) ctor.newInstance(delegates, true, decideDelegation);
        } catch (ReflectiveOperationException ex) {
            created = allLoader;
        }
        synchronized (this) {
            if ((currentAllLoader == null || currentAllLoader == allLoader) && languagesLoader == null) {
                languagesLoader = created;
            }
        }
        return created;
    }

    private void enumerateLanguages(List<ScriptEngineFactory> arr, Bindings globals) {
        ClassLoader langLoader = createGraalDependentClassLoader();
        final GraalContext ctx = new GraalContext(globals, langLoader);
        String specVersion = System.getProperty("java.specification.version"); //NOI18N
        String vmVersion = System.getProperty("java.vm.version"); //NOI18N
        if ("1.8".equals(specVersion) && vmVersion.contains("jvmci-")) { //NOI18N
            // this is GraalVM 8, whose JVMCI support returns a PolyglotImpl from the system classloader
            // incompatible with PolyglotImpl bundled in this module's libraries.
            // GraalVM 8 always contains (mandatory) JS implementation, so the JS is loaded by libs.graalsdk.system module.
            // No need to offer a bundled GraalVM implementation and in fact, it is not even possible.
            return;
        }
        GraalContext.executeWithClassLoader(() -> {
            try {
                try (Engine engine = Engine.newBuilder().build()) {
                    for (Map.Entry<String, Language> entry : engine.getLanguages().entrySet()) {
                        arr.add(new GraalEngineFactory(ctx, entry.getKey(), entry.getValue()));
                    }
                }
                return null;
            } catch (Throwable t) {
                LOG.log(Level.INFO, "Failed to initialize GraalEngines", t);
                return null;
            }
        }, langLoader);
    }
}
