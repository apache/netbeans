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

package org.netbeans.libs.git.jgit;

import org.eclipse.jgit.lib.ProgressMonitor;

/**
 *
 * @author ondra
 */
public final class DelegatingProgressMonitor implements ProgressMonitor {
    private final org.netbeans.libs.git.progress.ProgressMonitor monitor;

    public DelegatingProgressMonitor (org.netbeans.libs.git.progress.ProgressMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void start (int totalTasks) {
    }

    @Override
    public void beginTask (String title, int totalWork) {
        monitor.beginTask(title, totalWork);
    }

    @Override
    public void update (int completed) {
        monitor.updateTaskState(completed);
    }

    @Override
    public void endTask () {
        monitor.endTask();
    }

    @Override
    public boolean isCancelled () {
        return monitor.isCanceled();
    }

    @Override
    public void showDuration(boolean bln) {
    }

}
