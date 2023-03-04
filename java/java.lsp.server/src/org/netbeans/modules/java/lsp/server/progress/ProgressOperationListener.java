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
package org.netbeans.modules.java.lsp.server.progress;

import java.util.EventListener;

/**
 * The Listener allows to intercept creation and finish of an operation that
 * uses Progress API to report the progress.
 * In JLS server, this can be used to control that operation on behalf of JLS client,
 * or report to the client that the operation has finished. Useful where the original
 * NB APIs do not provide feedback about start/stop (progress).
 * 
 * @author sdedic
 */
public interface ProgressOperationListener extends EventListener {
    /**
     * Called when the handle was created. The action may not be started yet at the
     * time, just the operation handle was initialized. 
     * @param e event instance
     */
    public default void progressHandleCreated(ProgressOperationEvent e) {}
    
    /**
     * Called when the handle is called to report operation finish.
     * @param e event instance.
     */
    public default void progressHandleFinished(ProgressOperationEvent e) {}
}
