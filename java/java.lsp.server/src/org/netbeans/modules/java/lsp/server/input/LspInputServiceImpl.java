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
package org.netbeans.modules.java.lsp.server.input;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

/**
 *
 * @author Dusan Balek
 */
@JsonSegment("input")
public class LspInputServiceImpl implements InputService {

    private final RegistryImpl registry = new RegistryImpl();

    @Override
    public CompletableFuture<Either<QuickPickStep, InputBoxStep>> step(InputCallbackParams params) {
        Callback callback = registry.callbacks.get(params.getInputId());
        if (callback == null) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Either<QuickPickStep, InputBoxStep>> future = new CompletableFuture<>();
        callback.step(params).handle((step, ex) -> {
            if (ex != null) {
                registry.callbacks.remove(params.getInputId());
                future.completeExceptionally(ex);
            } else {
                if (step == null) {
                    registry.callbacks.remove(params.getInputId());
                }
                future.complete(step);
            }
            return null;
        });
        return future;
    }

    @Override
    public CompletableFuture<String> validate(InputCallbackParams params) {
        Callback callback = registry.callbacks.get(params.getInputId());
        return callback != null ? callback.validate(params) : CompletableFuture.completedFuture(null);
    }

    public Registry getRegistry() {
        return registry;
    }

    private static class RegistryImpl implements Registry {

        private final Map<String, Callback> callbacks = new ConcurrentHashMap<>();
        private final AtomicInteger cnt = new AtomicInteger();

        @Override
        public String registerInput(Callback callback) {
            String id = "ID:" + cnt.incrementAndGet();
            callbacks.put(id, callback);
            return id;
        }
    }
}
