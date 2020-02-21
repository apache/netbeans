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

package org.netbeans.modules.cnd.debugger.common2.debugger.assembly;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;

public interface Controller {
    public void setBreakpoint(String address, boolean set);
    public void toggleBreakpoint(String address);
    public void updateBreakpoint(long address, NativeBreakpoint bpt, boolean set);
    public void enableBreakpoint(long address, NativeBreakpoint bpt, boolean enable);
    public void addStateListenerInst(StateListener sl);
    public void removeStateListenerInst(StateListener sl);

    // ask for disassembly for current visiting location
    public void requestDis(boolean withSource);

    // ask for disassembly for the specified range (raw ... no srclines)
    public void requestDis(String start, int count, boolean withSource);

}
