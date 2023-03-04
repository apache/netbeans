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

package org.netbeans.api.progress;

/**
 * Callable used by ProgressUtils.showProgressDialogAndRun to do background
 * work while a modal progress dialog is shown blocking all application windows.
 *
 * @since 1.19
 * @author Tim Boudreau
 */
public interface ProgressRunnable<T> {
    /**
     * Perform the background work
     * @param handle A progress handle to post background work progress from.
     * The handle, when passed in, has had start() and setToIndeterminate() called.
     * @return The result of the background computation
     */
    public T run(ProgressHandle handle);
}
