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
package org.netbeans.modules.mylyn.util;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 *
 * @author Ondrej Vrabec
 */
public class CancelableProgressMonitor implements IProgressMonitor {

    private volatile boolean canceled;
    
    @Override
    public void beginTask (String string, int i) {
    }

    @Override
    public void done () {
    }

    @Override
    public void internalWorked (double d) {
    }

    @Override
    public synchronized boolean isCanceled () {
        return canceled;
    }

    @Override
    public synchronized void setCanceled (boolean canceled) {
        this.canceled = canceled;
    }

    @Override
    public void setTaskName (String string) {
    }

    @Override
    public void subTask (String string) {
    }

    @Override
    public void worked (int i) {
    }
    
}
