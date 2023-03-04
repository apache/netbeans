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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.util.concurrent.Executor;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession",
                                 types=AsynchronousModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                                 types=AsynchronousModelFilter.class,
                                 position=13000)
})
public class JPDAAsynchronousModel implements AsynchronousModelFilter {
    
    private Executor rp;

    public JPDAAsynchronousModel(ContextProvider lookupProvider) {
        JPDADebuggerImpl debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
        rp = debugger.getRequestProcessor();
    }

    public Executor asynchronous(Executor exec, CALL asynchCall, Object object) {
        switch (asynchCall) {
            case VALUE:
            case CHILDREN:
            case SHORT_DESCRIPTION:
                return rp;
            case DISPLAY_NAME:
                return CURRENT_THREAD;
        }
        return null; // ??
    }

}
