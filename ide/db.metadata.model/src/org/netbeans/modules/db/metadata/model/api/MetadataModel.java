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

package org.netbeans.modules.db.metadata.model.api;

import javax.swing.SwingUtilities;
import org.netbeans.modules.db.metadata.model.MetadataModelImplementation;
import org.openide.util.Parameters;

/**
 * This class encapsulates a metadata model and provides a way to access
 * the metadata in the model.
 *
 * @author Andrei Badea
 */
public class MetadataModel {

    final MetadataModelImplementation impl;

    MetadataModel(MetadataModelImplementation impl) {
        this.impl = impl;
    }

    /**
     * Provides access to the metadata in the model.
     *
     * <p>To access the model, an implementation of {@link Action} is
     * passed to this method. The {@link Action#run} method will be called,
     * and the root {@link Metadata} instance will be passed as the parameter
     * of this method.</p>
     *
     * <p>Any instance reachable from the {@link Metadata} instance
     * is only meaningful inside the action's {@code run()} method. It is not guaranteed
     * that a in subsequent access to the model the same instances will be available.</p>
     *
     * <p><b>No instance reachable from the {@code Metadata} instance, including
     * the {code Metadata} instance itself, is allowed to escape the action's {@code run()}
     * method!</p>
     *
     * @param  action the action to be run.
     * @throws MetadataModelException if an exception occurs during the read access.
     *         For example, an error could occur while retrieving the metadata.
     *         Also, runtime exception thrown by the action's {@code run()} are
     *         rethrown as {@code MetadataModelException}.
     * @throws IllegalStateException if this method is called on the AWT event
     *         dispatching thread.
     */
    public void runReadAction(Action<Metadata> action) throws MetadataModelException {
        Parameters.notNull("action", action);
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException();
        }
        impl.runReadAction(action);
    }
}
