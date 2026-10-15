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
package org.netbeans.modules.maven.execute;

import java.util.concurrent.CountDownLatch;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.openide.execution.ExecutorTask;
import org.openide.windows.InputOutput;

/**
 * Block the actual maven execution, but pretend that the task has been completed
 */
public class MockMavenExec extends MavenCommandLineExecutor.ExecuteMaven {

    public static class Reporter {
        public volatile boolean executed;
        public volatile RunConfig executedConfig;
        public CountDownLatch executedLatch = new CountDownLatch(1);

        public void executed(RunConfig config) {
            executed = true;
            executedConfig = config;
        }
    }

    @Override
    public ExecutorTask execute(RunConfig config, InputOutput io, AbstractMavenExecutor.TabContext tc) {
        Reporter r = config.getActionContext().lookup(Reporter.class);
        if (r != null) {
            r.executed(config);
        }
        ExecutorTask t = new ExecutorTask(() -> {
        }) {
            @Override
            public void stop() {
            }

            @Override
            public int result() {
                return 0;
            }

            @Override
            public InputOutput getInputOutput() {
                return null;
            }
        };
        t.run();
        if (r != null) {
            r.executedLatch.countDown();
        }
        return t;
    }
    
}
