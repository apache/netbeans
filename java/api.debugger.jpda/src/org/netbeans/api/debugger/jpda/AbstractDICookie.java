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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import java.io.IOException;

/**
 * Abstract ancestor of all {@link org.netbeans.api.debugger.DebuggerInfo}
 * Cookies. DebuggerInfo Cookie is responsible for creating of new JPDA
 * VirtualMachine.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerInfo di = DebuggerInfo.create (
 *        "My First Debugger Info", 
 *        new Object [] {
 *            abstractDICookieInstance
 *        }
 *    );
 *    DebuggerManager.getDebuggerManager ().startDebugging (di);</pre>
 *
 * @see AttachingDICookie
 * @see LaunchingDICookie
 * @see ListeningDICookie
 *
 * @author Jan Jancura
 */
public abstract class AbstractDICookie {
    
    /**
     * Creates a new instance of VirtualMachine for this DebuggerInfo Cookie.
     *
     * @return a new instance of VirtualMachine for this DebuggerInfo Cookie
     * @throws java.net.ConnectException When a connection is refused
     */
    public abstract VirtualMachine getVirtualMachine () throws IOException,
    IllegalConnectorArgumentsException, VMStartException;
}
