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

import java.util.Collection;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CSMInitializationTaskRunner {
     private static final CSMInitializationTaskRunner DEFAULT = new CSMInitializationTaskRunnerDefault();
    
    
    public static CSMInitializationTaskRunner getInstance() {
        Collection<? extends CSMInitializationTaskRunner> notifiers = Lookup.getDefault().lookupAll(CSMInitializationTaskRunner.class);
        if (notifiers.isEmpty()) {
            return DEFAULT;
        }
       return notifiers.iterator().next();
    }
    
    abstract public void runTask(ExecutionEnvironment execEnv, boolean isComplete, NamedRunnable task);
    
    abstract public void runTasks();

    private static class CSMInitializationTaskRunnerDefault extends CSMInitializationTaskRunner {

        public CSMInitializationTaskRunnerDefault() {
        }

        @Override
        public void runTask(ExecutionEnvironment execEnv, boolean isComplete, NamedRunnable task) {
            task.run();
        }

        @Override
        public void runTasks() {
            
        }
    }
    
    
}
