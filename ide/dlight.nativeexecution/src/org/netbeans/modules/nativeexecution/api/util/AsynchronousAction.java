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

package org.netbeans.modules.nativeexecution.api.util;

import javax.swing.Action;

/**
 * This intrface is pretty much the same as {@link Action javax.swing.Action}
 * but it assures that action is performed asynchronously
 * ({@link #actionPerformed(java.awt.event.ActionEvent)} must be implemented so
 * that it does not block the current thread).
 *
 * Also method {@link #invoke()} is provided that assures synchronous execution
 * of the action. I.e. it must be implemented so, that it does block the current
 * thread until the action is performed.
 */
public interface AsynchronousAction extends Action {
    /**
     * Synchronous action invocation.
     */
    public void invoke() throws Exception;
}
