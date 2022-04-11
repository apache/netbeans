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

package org.netbeans.modules.cnd.debugger.common2.debugger.debugging;

import javax.swing.Action;
import org.netbeans.modules.cnd.debugger.common2.debugger.Frame;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.Thread;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 */
public abstract class DebuggingNodeActionsProvider implements NodeActionsProvider {
    private final NativeDebugger debugger;
    
    protected DebuggingNodeActionsProvider (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, NativeDebugger.class);
    }

    protected void makeCurrent(final Object node) {
        if (node == TreeModel.ROOT) {
            return;
        }
        if (node instanceof Thread || node instanceof Frame) {
            if (node instanceof Thread) {
                Thread t = (Thread) node;
                if (t.isSuspended()) {// not sure about it as Threads allows a user to select any thread
                    debugger.makeThreadCurrent(t);
                    if (t.getStack() != null && t.getStack().length > 0) {
                        debugger.makeFrameCurrent(t.getStack()[0]);
                    }
                }
            } else if (node instanceof Frame) {
                Frame f = (Frame) node;
                Thread t = f.getThread();
                if (t.isSuspended()) {// not sure about it as Threads allows a user to select any thread
                    if (!t.isCurrent()) {
                        debugger.makeThreadCurrent(t);
                    }
                    debugger.makeFrameCurrent(f);
                }
            }

            return;
        }
//        throw new UnknownTypeException(node);    // TODO
    }

    @Override
    public Action[] getActions(Object node) throws UnknownTypeException {
        return new Action[0];
    }
    
}
