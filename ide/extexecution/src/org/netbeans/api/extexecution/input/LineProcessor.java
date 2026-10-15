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
import org.netbeans.api.annotations.common.NonNull;

/**
 * Processes the lines fetched by {@link InputReader} usually with help
 * of the {@link InputProcessors#bridge(LineProcessor)}.
 * <p>
 * When the implementation is used just by single bridge it
 * does not have to be thread safe.
 *
 * @author Petr Hejl
 * @see InputProcessors#bridge(LineProcessor)
 * @see InputReader
 * @deprecated use {@link org.netbeans.api.extexecution.base.input.LineProcessor}
 */
@Deprecated
public interface LineProcessor extends Closeable {

    /**
     * Processes the line.
     *
     * @param line the line to process
     */
    void processLine(@NonNull String line);

    /**
     * Notifies the processor that it should reset its state.
     * <p>
     * The circumstances when this method is called must be defined by
     * the code using this class.
     * <div class="nonnormative">
     * For example processor created with
     * {@link InputProcessors#bridge(LineProcessor)} delegates any call
     * to {@link InputProcessor#reset()} to this method.
     * </div>
     */
    void reset();

    /**
     * Closes the processor releasing the resources held by it.
     */
    void close();

}
