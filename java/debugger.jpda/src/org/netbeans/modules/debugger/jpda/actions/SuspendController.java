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

package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

/**
 * Implementation of this interface can be used to control debugger suspension.
 * In some situation it can happen that it's better not to allow debugger to
 * suspend the program in the current state. These are situations like when
 * the debuggee is holding native locks that affect other applications on the
 * system. For instance under X-Windows when debuggee is holding an AWT lock,
 * no other applications (including the debugger application) can gain focus,
 * thus it's not desired to suspend debuggee in such state, or the implementation
 * code can perform actions leading to the releasing of such a lock.
 * 
 * @author Martin Entlicher
 */
public interface SuspendController {
    
    /**
     * Decide whether the thread can be suspended, or perform any steps
     * that are necessary.
     * @param tRef An already suspended thread
     * @return <code>true</code> when the thread can remain suspended, or
     *         <code>false</code> when the thread needs to be resumed.
     */
    boolean suspend(ThreadReference tRef);
    
    /**
     * Decide whether the whole virtual machine can be suspended,
     * or perform any steps that are necessary.
     * @param vm An already suspended virtual machine
     * @return <code>true</code> when the virtual machine can remain suspended, or
     *         <code>false</code> when it needs to be resumed.
     */
    boolean suspend(VirtualMachine vm);
}
