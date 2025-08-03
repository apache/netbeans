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
package org.netbeans.modules.glassfish.tooling;

/**
 * GlassFish server administration command execution state report callback.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface TaskStateListener {

    // Interface Methods                                                      //
    /**
     * Callback to notify about GlassFish server administration command
     * execution state change.
     * <p/>
     * @param newState New command execution state.
     * @param event    Event related to execution state change.
     * @param args     Additional String arguments.
     */
    public void operationStateChanged(TaskState newState, TaskEvent event,
            String... args);

}
