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

import java.util.EventObject;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/** Progress event object.
 *
 * @author Martin Matula
 */
public final class ProgressEvent extends EventObject {
    /** Start event id */
    public static final int START = 1;
    /** Step event id */
    public static final int STEP = 2;
    /** Stop event id */
    public static final int STOP = 4;

    // event id
    private final int eventId;
    // type of opreation that is being processed (source-specific number)
    private final int operationType;
    // number of steps of the operation being processed
    private final int count;

    /** Creates ProgressEvent instance.
     * @param source Source of the event.
     * @param eventId ID of the event.
     */
    public ProgressEvent(@NonNull Object source, int eventId) {
        this(source, eventId, 0, 0);
    }

    /** Creates ProgressEvent instance.
     * @param source Source of the event.
     * @param eventId ID of the event.
     * @param operationType Source-specific number identifying source operation that
     * is being processed.
     * @param count Number of steps that the processed opration consists of.
     */
    public ProgressEvent(@NonNull Object source, int eventId, int operationType, int count) {
        super(source);
        Parameters.notNull("source", source); // NOI18N
        this.eventId = eventId;
        this.operationType = operationType;
        this.count = count;
    }

    /** Returns ID of the event.
     * @return ID of the event.
     */
    public int getEventId() {
        return eventId;
    }

    /** Returns operation type.
     * @return Source-specific number identifying operation being processed. Needs to
     * be valid for START events, can be 0 for STEP and STOP events.
     */
    public int getOperationType() {
        return operationType;
    }

    /** Returns step count.
     * @return Number of step that the operation being processed consists of. Needs to
     * be valid for START events, can be 0 for STEP and STOP events. If it is not 0
     * for STEP events, it is a new progress.
     */
    public int getCount() {
        return count;
    }
}
