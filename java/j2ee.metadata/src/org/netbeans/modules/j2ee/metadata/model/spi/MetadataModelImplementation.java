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

package org.netbeans.modules.j2ee.metadata.model.spi;

import java.io.IOException;
import java.util.concurrent.Future;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;

/**
 * The SPI for <code>MetadataModel</code>.
 *
 * @see MetadataModelFactory
 *
 * @author Andrei Badea
 * @since 1.2
 */
public interface MetadataModelImplementation<T> {

    /**
     * Corresponds to {@link org.netbeans.modules.j2ee.metadata.model.api.MetadataModel#runReadAction}.
     *
     * @param  action the action to be executed; never null.
     * @return the value returned by the action's {@link MetadataModelAction#run} method.
     * @throws MetadataModelException if the action's <code>run()</code> method
     *         threw a checked exception.
     * @throws IOException if an error occured while reading the model from its storage.
     */
    <R> R runReadAction(MetadataModelAction<T, R> action) throws MetadataModelException, IOException;

    /**
     * Corresponds to {@link org.netbeans.modules.j2ee.metadata.model.api.MetadataModel#isReady}.
     *
     * @return true if the model is ready, false otherwise.
     */
    boolean isReady();

    /**
     * Corresponds to {@link org.netbeans.modules.j2ee.metadata.model.api.MetadataModel#runReadActionWhenReady}.
     *
     * @param  action the action to be executed; never null.
     * @return a {@link Future} encapsulating the value returned by the action's {@link MetadataModelAction#run} method.
     * @throws MetadataModelException if the action's <code>run()</code> method
     *         threw a checked exception.
     * @throws IOException if an error occured while reading the model from its storage.
     */
    <R> Future<R> runReadActionWhenReady(MetadataModelAction<T, R> action) throws MetadataModelException, IOException;
}
