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

package org.netbeans.api.extexecution.input;

import java.io.Closeable;
import java.io.IOException;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Processes chars read by {@link InputReader}.
 * <p>
 * When the implementation is used just by single InputReader it
 * does not have to be thread safe.
 *
 * @author Petr Hejl
 * @see InputReader
 * @deprecated use {@link org.netbeans.api.extexecution.base.input.InputProcessor}
 */
@Deprecated
public interface InputProcessor extends Closeable {

    /**
     * Processes the characters.
     *
     * @param chars characters to process
     * @throws IOException if any processing error occurs
     */
    void processInput(@NonNull char[] chars) throws IOException;

    /**
     * Notifies the processor that it should reset its state.
     * <p>
     * The circumstances when this method is called must be defined
     * by the particular {@link InputReader}.
     * <div class="nonnormative">
     * For example reset is called by reader returned from
     * {@link InputReaders#forFileInputProvider(org.netbeans.api.extexecution.input.InputReaders.FileInput.Provider)}
     * when the provided file is changed.
     * </div>
     *
     * @throws IOException if error occurs while reseting
     */
    void reset() throws IOException;

    /**
     * Closes the processor releasing the resources held by it.
     */
    void close() throws IOException;

}
