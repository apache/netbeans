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

package org.netbeans.modules.profiler.snaptracer.impl.export;

import org.netbeans.modules.profiler.snaptracer.TracerProgressObject;
import java.io.IOException;

/**
 *
 * @author Jiri Sedlacek
 */
final class ExportBatch {
    
    private final TracerProgressObject progress;
    private final BatchRunnable worker;

    protected ExportBatch(TracerProgressObject progress, BatchRunnable worker) {
        this.progress = progress;
        this.worker = worker;
    }

    TracerProgressObject getProgress() { return progress; }
    BatchRunnable getWorker() { return worker; }

    static interface BatchRunnable {
        public void run() throws IOException;
    }

}
