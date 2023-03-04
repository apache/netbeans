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

package org.netbeans.spi.debugger;

import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.Session;


/**
 * Delegates {@link org.netbeans.api.debugger.DebuggerInfo}
 * support to some some existing
 * {@link org.netbeans.api.debugger.Session}.
 *
 * @author Jan Jancura
 * @deprecated This class is of no use. Nobody can create Session object, but debuggercore.
 */
// XXX: What this is for??
// XXX: Not usable anyway, Session is final with private constructor
// XXX: Should be deprecated? Or removed - can not be meaningfully implemented anyway...
@Deprecated
public abstract class DelegatingSessionProvider {

    /**
     * Returns a {@link org.netbeans.api.debugger.Session} to delegate
     * on.
     *
     * @return Session to delegate on
     */
    public abstract Session getSession (
        DebuggerInfo debuggerInfo
    );
}

