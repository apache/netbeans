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

package org.netbeans.modules.junit;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/** Thread-safe wrapper around JUnitProgress - panel showing progress info
 * and allowing the user to cancel running task. Used in actions creating
 * or executing tests.
 *
 * @author  Tomas Pavek
 * @author  Ondrej Rypacek
 * @author  Marian Petras
 */
final class ProgressIndicator implements Cancellable {

    /**
     * initial message to be used when GUI is created.
     * It is only used if setMessage(...) is called sooner than show().
     */
    private final ProgressHandle progressHandle;
    private volatile boolean cancelled = false;

    ProgressIndicator() {
        String msg = NbBundle.getMessage(ProgressIndicator.class,
                                        "LBL_generator_progress_title");//NOI18N
        progressHandle = ProgressHandleFactory.createHandle(msg);
    }

    public boolean cancel() {
        cancelled = true;
        return true;
    }

    synchronized boolean isCanceled() {
        return cancelled;
    }

    void displayStatusText(String statusText) {
        StatusDisplayer.getDefault().setStatusText(statusText);
    }

    /**
     * Sets a message to be displayed in the progress GUI.
     */
    synchronized void setMessage(final String msg) {
        progressHandle.progress(msg);
    }

    synchronized void show() {
        progressHandle.start();
    }

    synchronized void hide() {
        progressHandle.finish();
    }

}
