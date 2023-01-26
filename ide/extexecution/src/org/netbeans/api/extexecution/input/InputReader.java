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

package org.netbeans.api.extexecution.input;

import java.io.Closeable;
import java.io.IOException;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * This interface represents abstraction for reading characters. It allows
 * custom processing of such characters through the given processor.
 * <p>
 * For safe usage in {@link InputReaderTask} implementation of this
 * interface has to be responsive to interruption.
 *
 * @author Petr Hejl
 * @deprecated use {@link org.netbeans.api.extexecution.base.input.InputReader}
 */
@Deprecated
public interface InputReader extends Closeable {

    /**
     * Reads some input and process it through the processor (if any).
     * <p>
     * Implementation of this method has to be non blocking
     * for safe usage in {@link InputReaderTask}.
     *
     * @param processor consumer of read characters, mey be <code>null</code>
     * @return number of characters read
     * @throws IOException if any read or process error occurs
     */
    int readInput(@NullAllowed InputProcessor processor) throws IOException;

    /**
     * Closes the reader releasing the resources held by it.
     */
    @Override
    void close() throws IOException;

}
