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
package org.netbeans.modules.refactoring.api;

import java.util.EventListener;
import org.netbeans.api.annotations.common.NonNull;

/** Progress listener. Enables objects to listen to a progress of long operations.
 *
 * @author  Martin Matula
 */
public interface ProgressListener extends EventListener {
    /** Signals that an operation has started.
     * @param event Event object describing this event.
     */
    public void start(@NonNull ProgressEvent event);

    /** Signals that an operation has progressed.
     * @param event Event object describing this event.
     */
    public void step(@NonNull ProgressEvent event);

    /** Signals that an operation has finished.
     * @param event Event object describing this event.
     */
    public void stop(@NonNull ProgressEvent event);
}
