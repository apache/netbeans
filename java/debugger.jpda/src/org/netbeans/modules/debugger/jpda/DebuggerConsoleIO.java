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

package org.netbeans.modules.debugger.jpda;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.io.InputOutput;
import org.netbeans.modules.debugger.jpda.console.DebuggerOutput;
import org.netbeans.spi.debugger.ContextProvider;

/**
 *
 * @author Martin Entlicher
 */
public final class DebuggerConsoleIO {
    
    private final JPDADebuggerImpl debugger;
    private final DebuggerOutput output;
    
    DebuggerConsoleIO(JPDADebuggerImpl debugger, ContextProvider lookupProvider) {
        this.debugger = debugger;
        this.output = new DebuggerOutput(debugger, lookupProvider);
    }
    
    @CheckForNull
    public InputOutput getIO() {
        return output.getIOManager().getIO();
    }
    
    public void println(String text, Line line) {
        output.getIOManager().println(text, line);
    }
    
    public void println(String text, Line line, boolean important) {
        output.getIOManager().println(text, line, important);
    }
    
    public static final class Line {
        
        private final String url;
        private final int lineNumber;
        private final Reference<JPDADebugger> debuggerRef;
        
        public Line (String url, int lineNumber, JPDADebugger debuggerTimeStamp) {
            this.url = url;
            this.lineNumber = lineNumber;
            this.debuggerRef = new WeakReference<>(debuggerTimeStamp);
        }
        
        public void show () {
            EditorContextBridge.getContext().showSource (url, lineNumber, debuggerRef.get());
        }
    }
}
