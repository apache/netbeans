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
package org.netbeans.modules.csl.api;

import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 * Specifies additional method to {@link CodeCompletionHandler} providing
 * complete documentation for elements.
 *
 * @author Petr Hejl
 * @since 2.43
 */
public interface CodeCompletionHandler2 extends CodeCompletionHandler {

    /**
     * Provides a full documentation for the element. The infrastructure will
     * always prefer this method to the {@link CodeCompletionHandler#document(org.netbeans.modules.csl.spi.ParserResult, org.netbeans.modules.csl.api.ElementHandle)}.
     * The old method will be called as callback only if this one returns {@code null}.
     *
     * @param info the parsing information
     * @param element the element for which the documentation is requested
     * @param cancel a {@link Callable} to signal the cancel request, can be used by clients to check ({@code cancel.call()})
     *               whether the parent request for documentation is still relevant, not necessary to use in case of not
     *		     time-consuming job
     * @return the documentation for the element
     * @since 2.46
     */
    @CheckForNull
    Documentation documentElement(@NonNull ParserResult info, @NonNull ElementHandle element, @NonNull Callable<Boolean> cancel);

}
