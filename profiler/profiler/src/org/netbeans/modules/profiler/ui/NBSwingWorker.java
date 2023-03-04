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

package org.netbeans.modules.profiler.ui;

import org.netbeans.lib.profiler.ui.SwingWorker;
import org.openide.util.RequestProcessor;


/**
 * Overrides the default behaviour of the SwingWorker to use RequestProcessor
 * @author Jaroslav Bachorik
 */
public abstract class NBSwingWorker extends SwingWorker {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static RequestProcessor rp = null;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public NBSwingWorker(boolean forceEQ) {
        super(forceEQ);
    }

    public NBSwingWorker() {
        super();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    protected void postRunnable(Runnable runnable) {
        getRequestProcessor().post(runnable);
    }

    private static synchronized RequestProcessor getRequestProcessor() {
        if (rp == null) {
            rp = new RequestProcessor("NBSwingWorker - RequestProcessor", 1); // NOI18N
        }

        return rp;
    }
}
