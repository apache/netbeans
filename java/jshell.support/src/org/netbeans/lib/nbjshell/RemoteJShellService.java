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
package org.netbeans.lib.nbjshell;

import jdk.jshell.spi.ExecutionControl;

/**
 * Carries out non-agent tasks for the JShell. Its actual implementation
 * may vary depending on the launch mode of the JShell
 *
 * @author sdedic
 */
public interface RemoteJShellService extends ExecutionControl {
    /**
     * Requests shutdown of the target process. The implementation may ignore
     * the request, but JShell should terminate at the local side anyway.
     * @return true, if the request was accepted
     */
    public boolean requestShutdown();

    /**
     * Closes the supplied I/O streams. If the streams are not yet opened or created
     * the method does not even attempt to initiate the target VM. Further requests
     * to get streams will result in an IOException.
     */
    public void closeStreams();

    public String getTargetSpec();

    public void suppressClasspathChanges(boolean b);
    
    public ExecutionControlException getBrokenException();
}
