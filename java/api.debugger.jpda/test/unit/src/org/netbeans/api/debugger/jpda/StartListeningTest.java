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
package org.netbeans.api.debugger.jpda;

import java.util.concurrent.Executor;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;
import org.openide.util.RequestProcessor;

public class StartListeningTest extends NbTestCase {

    public StartListeningTest(String name) {
        super(name);
    }

    public void testStartListeningOnRandomPort() throws Exception {
        startListening();
    }

    // BEGIN: org.netbeans.api.debugger.jpda.StartListeningTest
    private static final Executor LISTENING = new RequestProcessor("Listening");

    int startListening() throws Exception {
        ListeningDICookie config = ListeningDICookie.create(-1);
        DebuggerInfo info = DebuggerInfo.create(ListeningDICookie.ID, config);
        LISTENING.execute(() -> {
            DebuggerManager.getDebuggerManager().startDebugging(info);
        });
        int port = config.getPortNumber();
        assertNotSame("Listening on a real port", -1, port);
        return port;
    }
    // END: org.netbeans.api.debugger.jpda.StartListeningTest
}
