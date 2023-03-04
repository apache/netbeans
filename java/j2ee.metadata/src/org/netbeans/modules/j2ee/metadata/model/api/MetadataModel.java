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

package org.netbeans.modules.j2ee.metadata.model.api;

import java.io.IOException;
import java.util.concurrent.Future;
import org.netbeans.modules.j2ee.metadata.model.MetadataModelAccessor;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.openide.util.Parameters;

/**
 * Encapsulates a generic metadata model. The kind of metadata and the
 * operation allowed on them is given by the <code>T</code> type parameter,
 * and must be described to the client by the provider of the model.
 *
 * @author Andrei Badea
 * @since 1.2
 */
public final class MetadataModel<T> {

    static {
        MetadataModelAccessor.DEFAULT = new MetadataModelAccessor() {
            public <E> MetadataModel<E> createMetadataModel(MetadataModelImplementation<E> impl) {
                return new MetadataModel<E>(impl);
            }
        };
    }

    private final MetadataModelImplementation<T> impl;

    private MetadataModel(MetadataModelImplementation<T> impl) {
        assert impl != null;
        this.impl = impl;
    }

    /**
     * Executes an action in the context of this model and in the calling thread.
     * This method is used to provide
     * the model client with access to the metadata contained in the model.
     *
     * <p>This method provides safe access to the model in the presence of concurrency.
     * It ensures that when the action's {@link MetadataModelAction#run} method
     * is running, no other thread  can be running another action's <code>run()</code> method on the same
     * <code>MetadataModel</code> instance. It also guarantees that the
     * metadata does not change until the action's <code>run()</code> method
     * returns.</p>
     *
     * <p><strong>This method does not, however, guarantee, that any piece of
     * metadata obtained from the model as a result of invoking <code>runReadAction()</code>
     * will still be present in the model when a subsequent invocation of
     * <code>runReadAction()</code> takes place. As a result, clients are forbidden
     * to call any methods on any piece of metadata obtained from the model outside
     * the <code>run()</code> method of an action being executed as a result of an
     * invocation of <code>runReadAction()</code>. In other words, pieces of metadata
     * that are not explicitly documented as immutable are not allowed to escape
     * the action's <code>run()</code> method.</strong></p>
     *
     * <p><strong>This method may take a long time to execute. It is
     * recommended that the method not be called from the AWT event thread. In some
     * situations, though, the call needs to be made from the event thread, such
     * as when computing the enabled state of an action. In this case it is
     * recommended that the call not take place if the {@link #isReady} method
     * returns false (and a default value be returned as the result of
     * the computation).</strong></p>
     *
     * @param  action the action to be executed.
     * @return the value returned by the action's <code>run()</code> method.
     * @throws MetadataModelException if a checked exception was thrown by
     *         the action's <code>run()</code> method. That checked exception
     *         will be available as the return value of the {@link MetadataModelException#getCause getCause()}
     *         method. This only applies to checked exceptions; unchecked exceptions
     *         are propagated from the <code>run()</code> method unwrapped.
     * @throws IOException if there was a problem reading the model from its storage (for
     *         example an exception occured while reading the disk files
     *         which constitute the source for the model's metadata).
     * @throws NullPointerException if the <code>action</code> parameter was null.
     */
    public <R> R runReadAction(MetadataModelAction<T, R> action) throws MetadataModelException, IOException {
        Parameters.notNull("action", action); // NOI18N
        return impl.runReadAction(action);
    }

    /**
     * Returns true if the metadata contained in the model correspond exactly
     * to their source. For example, for a model containing metadata expressed
     * in annotations in Java files, the model could be considered ready if
     * no classpath scanning is taking place.
     *
     * <p><strong>It is not guaranteed that if this method returns true, a
     * subsequent invocation of {@link #runReadAction runReadAction()} will see the model in a
     * ready state.</strong> Therefore this method is intended just as a hint useful
     * in best-effort scenarios. For example the method might be used by a client
     * which needs immediate access to the model to make its best effort to
     * ensure that the model will at least not be accessed when not ready.</p>
     *
     * @return true if the model is ready, false otherwise.
     *
     * @since 1.3
     */
    public boolean isReady() {
        return impl.isReady();
    }

    /**
     * Executes an action in the context of this model either immediately
     * if the model is ready, or at a later point in time when the model becomes
     * ready. The action is executed in the calling thread if executed immediately,
     * otherwise it is executed in another, unspecified thread.
     *
     * <p>The same guarantees with respect to concurrency and constraints
     * with respect to re-readability of metadata that apply to
     * {@link #runReadAction runReadAction()} apply to this method too.
     * Furthermore, it is guaranteed that the action will see the model
     * in a ready state, that is, when invoked by the action, the
     * {@link #isReady} method will return <code>true</code>.</p>
     *
     * <p><strong>This method may take a long time to execute (in the case
     * the action is executed immediately). It is recommended that
     * the method not be called from the AWT event thread.</strong></p>
     *
     * @param  action the action to be executed.
     * @return a {@link Future} encapsulating the result of the action's
     *         <code>run()</code> method. If the action was not run
     *         immediately and it threw an exception (checked or unchecked),
     *         the future's <code>get()</code> methods will throw an
     *         {@link java.util.concurrent.ExecutionException} encapsulating
     *         that exception.
     * @throws MetadataModelException if the action was run immediately
     *         and a checked exception was thrown by
     *         the action's <code>run()</code> method. That checked exception
     *         will be available as the return value of the {@link MetadataModelException#getCause getCause()}
     *         method. This only applies to checked exceptions; unchecked exceptions
     *         are propagated from the <code>run()</code> method unwrapped.
     * @throws IOException if there was a problem reading the model from its storage (for
     *         example an exception occured while reading the disk files
     *         which constitute the source for the model's metadata).
     * @throws NullPointerException if the <code>action</code> parameter was null.
     *
     * @since 1.3
     */
    public <R> Future<R> runReadActionWhenReady(MetadataModelAction<T, R> action) throws MetadataModelException, IOException {
        Parameters.notNull("action", action); // NOI18N
        return impl.runReadActionWhenReady(action);
    }
}
