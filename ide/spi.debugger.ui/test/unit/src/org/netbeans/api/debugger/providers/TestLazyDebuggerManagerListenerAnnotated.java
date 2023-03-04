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

package org.netbeans.api.debugger.providers;

import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="unittest/annotated", types=LazyDebuggerManagerListener.class)
public class TestLazyDebuggerManagerListenerAnnotated implements LazyDebuggerManagerListener {

    public static Set<TestLazyDebuggerManagerListenerAnnotated> INSTANCES = new HashSet<TestLazyDebuggerManagerListenerAnnotated>();
    
    public ContextProvider context;

    public TestLazyDebuggerManagerListenerAnnotated() {
        INSTANCES.add(this);
    }

    public TestLazyDebuggerManagerListenerAnnotated(ContextProvider context) {
        INSTANCES.add(this);
        this.context = context;
    }

    public String[] getProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Breakpoint[] initBreakpoints() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void breakpointAdded(Breakpoint breakpoint) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void breakpointRemoved(Breakpoint breakpoint) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void initWatches() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void watchAdded(Watch watch) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void watchRemoved(Watch watch) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sessionAdded(Session session) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sessionRemoved(Session session) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void engineAdded(DebuggerEngine engine) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void engineRemoved(DebuggerEngine engine) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
