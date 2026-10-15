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

package org.netbeans.modules.refactoring.java.ui;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.progress.BaseProgressUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class ComputeOffAWT {

    public static <T> T computeOffAWT(Worker<T> w, String featureName, final JavaSource source, Phase phase) {
        AtomicBoolean cancel = new AtomicBoolean();
        Compute<T> c = new Compute(cancel, source, phase, w);

        BaseProgressUtils.runOffEventDispatchThread(c, featureName, cancel, false);

        return c.result;
    }

    private static final class Compute<T> implements Runnable, Task<CompilationController> {

        private final AtomicBoolean cancel;
        private final JavaSource source;
        private final Phase phase;
        private final Worker<T> worker;
        private       T result;

        public Compute(AtomicBoolean cancel, JavaSource source, Phase phase, Worker<T> worker) {
            this.cancel = cancel;
            this.source = source;
            this.phase = phase;
            this.worker = worker;
        }

        public void run() {
            try {
                source.runUserActionTask(this, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                result = null;
            }
        }

        public void run(CompilationController parameter) throws Exception {
            if (cancel.get()) return ;

            parameter.toPhase(phase);

            if (cancel.get()) return ;

            T t = worker.process(parameter);

            if (cancel.get()) return ;

            result = t;
        }
        
    }
    
    public static interface Worker<T> {
        T process(CompilationInfo info);
    }

    private ComputeOffAWT() {
    }
}
