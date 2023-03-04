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

package org.netbeans.spi.viewmodel;

import java.util.concurrent.Executor;

import org.openide.util.RequestProcessor;

/**
 * Change threading of implemented models.
 * Methods implemented in {@link TreeModel}, {@link NodeModel} ({@link ExtendedNodeModel})
 * and {@link TableModel} can be called synchronously in AWT thread as a direct
 * response to user action (this is the default behavior),
 * or asynchronously in a Request Processor or other thread.
 * Register an implementation of this along with other models,
 * if you need to change the original threading.
 *
 * @author Martin Entlicher
 * @since 1.20
 */
public interface AsynchronousModelFilter extends Model {

    /**
     * This enumeration identifies method(s) of view models for which
     * threading information is provided by
     * {@link #asynchronous(java.util.concurrent.Executor, org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL, java.lang.Object)} method.
     * <br>
     * <b>CHILDREN</b> for TreeModel.getChildrenCount() and TreeModel.getChildren()
     * <br>
     * <b>DISPLAY_NAME</b> is for NodeModel.getDisplayName() and ExtendedNodeModel.setName()
     * <br>
     * <b>SHORT_DESCRIPTION</b> for NodeModel.getShortDescription()
     * <br>
     * <b>VALUE</b> for TableModel.getValueAt() and TableModel.setValueAt()
     * <br>
     * The rest of the methods on models are called synchronously, or additional
     * enums can be added in the future.
     */
    static enum CALL { CHILDREN, DISPLAY_NAME, SHORT_DESCRIPTION, VALUE }

    /**
     * Executor for invocation of models method calls in the current thread.
     * This will make method invocation synchronous. It's important that the
     * methods execute fast so that they do not block AWT thread.
     * This is the default executor for {@link CALL#DISPLAY_NAME} and
     * {@link CALL#SHORT_DESCRIPTION}.
     */
    static final Executor CURRENT_THREAD = new Executor() {

        public void execute(Runnable command) {
            command.run();
        }

    };

    /**
     * Executor, which uses a shared {@link RequestProcessor} with
     * throughoutput = 1 for models method calls, making the method invocation
     * asynchronous. The UI gives a visual feedback to the user if models method
     * calls take a long time. Use this to keep the UI responsive.
     * This is the default executor for {@link CALL#CHILDREN} and
     * {@link CALL#VALUE}.
     */
    static final Executor DEFAULT = new RequestProcessor("Asynchronous view model", 1);   // NOI18N

    /**
     * Change the threading information for view models method calls.
     * The returned Executor is used to call methods identified by
     * {@link CALL} enum.
     *
     * @param original The original {@link Executor}
     * @param asynchCall Identification of the method call
     * @param node Object node
     * @return an instance of Executor
     */
    Executor asynchronous(Executor original, CALL asynchCall, Object node) throws UnknownTypeException;

}
