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

import java.util.ArrayList;
import java.util.List;
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
            return all;
        }
    }
}
