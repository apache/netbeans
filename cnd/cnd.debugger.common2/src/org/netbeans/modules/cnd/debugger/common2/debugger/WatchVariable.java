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


/*
 * WatchVariable.java"
 * Allows uniform treatment of DbxWatch and GdbWatch.
 */

package org.netbeans.modules.cnd.debugger.common2.debugger;

import org.netbeans.modules.cnd.debugger.common2.utils.ListMapItem;

public interface WatchVariable extends ListMapItem {
    // Shared with Variable:
    public NativeDebugger getDebugger();
    public void removeAllDescendantFromOpenList(boolean isLocal);
    public boolean isPtr();
    public String getVariableName();

    public NativeWatch getNativeWatch();
    public void setNativeWatch(NativeWatch parent);
        
    public int getRoutingToken();
}
