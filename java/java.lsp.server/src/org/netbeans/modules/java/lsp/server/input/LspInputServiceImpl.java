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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

/**
 *
 * @author Dusan Balek
 */
@JsonSegment("input")
public class LspInputServiceImpl implements InputService {

    private final Map<String, Function<InputCallbackParams, CompletableFuture<Either<QuickPickStep, InputBoxStep>>>> stepCallbacks = new HashMap<>();
    private final Map<String, Function<InputCallbackParams, CompletableFuture<String>>> validateCallbacks = new HashMap<>();

    public String registerInput(Function<InputCallbackParams, CompletableFuture<Either<QuickPickStep, InputBoxStep>>> stepCallback, Function<InputCallbackParams, CompletableFuture<String>> validateCallback) {
        String id = "ID:" + System.identityHashCode(stepCallback);
        stepCallbacks.put(id, stepCallback);
        if (validateCallback != null) {
            validateCallbacks.put(id, validateCallback);
        }
        return id;
    }

    public void unregisterInput(String inputId) {
        stepCallbacks.remove(inputId);
        validateCallbacks.remove(inputId);
    }

    @Override
    public CompletableFuture<Either<QuickPickStep, InputBoxStep>> step(InputCallbackParams params) {
        Function<InputCallbackParams, CompletableFuture<Either<QuickPickStep, InputBoxStep>>> callback = stepCallbacks.get(params.getInputId());
        return callback != null ? callback.apply(params) : CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<String> validate(InputCallbackParams params) {
        Function<InputCallbackParams, CompletableFuture<String>> callback = validateCallbacks.get(params.getInputId());
        return callback != null ? callback.apply(params) : CompletableFuture.completedFuture(null);
    }
}
