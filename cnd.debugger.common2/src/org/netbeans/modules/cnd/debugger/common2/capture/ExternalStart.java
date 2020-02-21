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

package org.netbeans.modules.cnd.debugger.common2.capture;

/**
 *
 */
public interface ExternalStart {

    /**
     * dbx has attached to some pid and has told the gui about it.
     * find out if it is one of the ones under ss_attach's control and if so,
     * a) tell ss_attach to proceed
     * b) return true so our caller can have dbx proceed
     * c) process the next request
     */
    boolean attached(int pid);

    void debuggerStarted();

    void fail();

    /**
     * Start the glue service, locally or remotely
     */
    boolean start();

    /**
     * Stop the glue service.
     */
    boolean stop();

    boolean isRunning();
}
