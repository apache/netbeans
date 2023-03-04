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
package org.netbeans.modules.debugger.jpda.impl;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.StepRequest;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;

import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;

public final class StepUtils {

    private static final String STEP_PROP_DEPTH = "originalThreadDepth";     // NOI18N

    private StepUtils() {}

    /**
     * Mark the frame depth of the thread when the step is created. It's to be retrieved
     * by {@link #getOriginalStepDepth(com.sun.jdi.request.StepRequest)}.
     */
    public static void markOriginalStepDepth(StepRequest stepRequest, ThreadReference threadReference) {
        try {
            EventRequestWrapper.putProperty(stepRequest, STEP_PROP_DEPTH, ThreadReferenceWrapper.frameCount(threadReference));
        } catch (IllegalThreadStateExceptionWrapper | IncompatibleThreadStateException | InternalExceptionWrapper |
                InvalidStackFrameExceptionWrapper | ObjectCollectedExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
            // Not successful, ignore.
        }
    }

    /**
     * Get the frame depth of the thread when the step was submitted, or <code>-1</code> when unknown.
     */
    public static int getOriginalStepDepth(StepRequest stepRequest) {
        Object depth;
        try {
            depth = EventRequestWrapper.getProperty(stepRequest, STEP_PROP_DEPTH);
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
            return -1;
        }
        if (depth instanceof Integer) {
            return (Integer) depth;
        }
        return -1;
    }
}
