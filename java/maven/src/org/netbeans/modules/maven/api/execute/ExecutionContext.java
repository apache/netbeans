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

package org.netbeans.modules.maven.api.execute;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.openide.windows.InputOutput;

/**
 *
 * @author mkleint
 */
public final class ExecutionContext {

    private int res;
    private InputOutput io;
    private ProgressHandle handle;

    public static final int EXECUTION_ABORTED = -10;

    static {
        AccessorImpl impl = new AccessorImpl();
        impl.assign();
    }
    
    static class AccessorImpl extends ActionToGoalUtils.ContextAccessor {
        
         public void assign() {
             if (ActionToGoalUtils.ACCESSOR == null) {
                 ActionToGoalUtils.ACCESSOR = this;
             }
         }
    
        @Override
        public ExecutionContext createContext(InputOutput inputoutput, ProgressHandle handle) {
            return new ExecutionContext(inputoutput, handle);
        }
    }

    private ExecutionContext(InputOutput inputoutput, ProgressHandle handle) {
        this.io = inputoutput;
        this.handle = handle;
    }

    public InputOutput getInputOutput() {
        return io;
    }

    public ProgressHandle getProgressHandle() {
        return handle;
    }

    
}
