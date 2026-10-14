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
package org.netbeans.modules.debugger.jpda.heapwalk.models;

import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.jpda.ObjectInfoProvider;
import org.openide.util.RequestProcessor;

@DebuggerServiceRegistration(path="netbeans-JPDASession", types=ObjectInfoProvider.class)
public final class ShowObjectVariableDetails implements ObjectInfoProvider {
    private static final RequestProcessor RP = new RequestProcessor("Variable details");
    private final JPDADebugger debugger;

    public ShowObjectVariableDetails(ContextProvider provider) {
        debugger = provider.lookupFirst(null, JPDADebugger.class);
        assert debugger != null;
    }

    @Override
    public boolean display(Session session, ObjectVariable v) {
        ShowObjectVariableInHeap show = new ShowObjectVariableInHeap(debugger, RP);
        show.showObjectVariable(v);
        return true;
    }
}
