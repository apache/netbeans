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

package org.netbeans.modules.profiler.api;

import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Cancellable;

/**
 *
 * @author Jaroslav Bachorik
 */
public interface ProgressDisplayer {
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    public static interface ProgressController extends Cancellable {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void close();

    public ProgressDisplayer showProgress(String message);

    public ProgressDisplayer showProgress(String message, ProgressController controller);

    public ProgressDisplayer showProgress(String caption, String message, ProgressController controller);
    
    public static final ProgressDisplayer DEFAULT = new ProgressDisplayer() {
        ProgressHandle ph = null;

        public synchronized ProgressDisplayer showProgress(String message) {
            ph = ProgressHandle.createHandle(message);
            ph.start();
            return DEFAULT;
        }

        public synchronized ProgressDisplayer showProgress(String message, ProgressController controller) {
            ph = ProgressHandle.createHandle(message, controller);
            ph.start();
            return DEFAULT;
        }

        public synchronized ProgressDisplayer showProgress(String caption, String message, ProgressController controller) {
            ph = ProgressHandle.createHandle(message, controller);
            ph.start();
            return DEFAULT;
        }

        public synchronized void close() {
            if (ph != null) {
                ph.finish();
                ph = null;
            }
        }
    };

}
