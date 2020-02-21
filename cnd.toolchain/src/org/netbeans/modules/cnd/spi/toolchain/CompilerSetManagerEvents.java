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
package org.netbeans.modules.cnd.spi.toolchain;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.SPIAccessor;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * CompilerSetManagerEvents handles tasks which depends on CompilerSetManager activity and
 * have to survive CompilerSetManager.deepCopy()
 * 
 * There is only one (and I hope there would be only one) such event -- Code Model Readiness
 *
 */
public final class CompilerSetManagerEvents {

    static {
        SPIAccessor.register(new SPIAccessorImpl());
    }

    private static final Map<ExecutionEnvironment, CompilerSetManagerEvents> map =
            new HashMap<ExecutionEnvironment, CompilerSetManagerEvents>();

    private final ExecutionEnvironment executionEnvironment;
    private boolean isCodeModelInfoReady;
    private final CSMInitializationTaskRunner taskRunner;    
    

    public static synchronized CompilerSetManagerEvents get(ExecutionEnvironment env) {
        CompilerSetManagerEvents instance = map.get(env);
        if (instance == null) {
            instance = new CompilerSetManagerEvents(env);
            map.put(env, instance);
        }
        return instance;
    }

    public void runProjectReadiness(NamedRunnable task) {        
        taskRunner.runTask(executionEnvironment, isCodeModelInfoReady, task);
    }
    
    private CompilerSetManagerEvents(ExecutionEnvironment env) {
        this.executionEnvironment = env;
        this.isCodeModelInfoReady = CompilerSetManager.get(executionEnvironment).isComplete();
        taskRunner = CSMInitializationTaskRunner.getInstance();
    }

    private void runTasks() {
        isCodeModelInfoReady = true;
        taskRunner.runTasks();
    }

    private static final class SPIAccessorImpl extends SPIAccessor {

        @Override
        public void runTasks(CompilerSetManagerEvents event) {
            event.runTasks();
        }

        @Override
        public CompilerSetManagerEvents createEvent(ExecutionEnvironment env) {
            return new CompilerSetManagerEvents(env);
        }

        @Override
        public void add(ExecutionEnvironment env, CompilerSet cs) {
            ((CompilerSetManagerImpl)CompilerSetManagerImpl.get(env)).add(cs);
        }

        @Override
        public void remove(ExecutionEnvironment env, CompilerSet cs) {
            ((CompilerSetManagerImpl)CompilerSetManagerImpl.get(env)).remove(cs);
        }
    }
}
