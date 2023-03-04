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
package org.netbeans.libs.graalsdk.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Language;
import org.netbeans.spi.scripting.EngineProvider;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = EngineProvider.class)
public final class GraalEnginesProvider implements EngineProvider {
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

    private void enumerateLanguages(List<ScriptEngineFactory> arr, Bindings globals) {
        final GraalContext ctx = new GraalContext(globals);
        try (Engine engine = Engine.newBuilder().build()) {
            for (Map.Entry<String, Language> entry : engine.getLanguages().entrySet()) {
                arr.add(new GraalEngineFactory(ctx, entry.getKey(), entry.getValue()));
            }
        }
    }
}
