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
package org.netbeans.modules.java.lsp.server.input;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

/**
 *
 * @author Dusan Balek
 */
@JsonSegment("input")
public interface InputService {

    @JsonRequest("step")
    public CompletableFuture<Either<QuickPickStep, InputBoxStep>> step(InputCallbackParams params);

    @JsonRequest("validate")
    public CompletableFuture<String> validate(InputCallbackParams params);

    public static interface Registry {

        public String registerInput(Callback callback);
    }

    public static interface Callback {

        CompletableFuture<Either<QuickPickStep, InputBoxStep>> step(InputCallbackParams params);

        default CompletableFuture<String> validate(InputCallbackParams params) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
