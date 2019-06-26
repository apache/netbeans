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
package org.netbeans.modules.payara.tooling;

import java.util.HashMap;
import java.util.Map;

/**
 * Current state of Payara server administration command execution
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum TaskState {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /** Value representing task waiting in executor queue. */
    READY,

    /** Value representing running task. */
    RUNNING,

    /** Value representing successfully completed task (with no errors). */
    COMPLETED,

    /** Value representing failed task. */
    FAILED;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**  A <code>String</code> representation of READY value. */
    private static final String READY_STR = "READY";

    /**  A <code>String</code> representation of RUNNING value. */
    private static final String RUNNING_STR = "RUNNING";

    /**  A <code>String</code> representation of COMPLETED value. */
    private static final String COMPLETED_STR = "COMPLETED";

    /**  A <code>String</code> representation of FAILED value. */
    private static final String FAILED_STR = "FAILED";

    /** 
     * Stored <code>String</code> values for backward <code>String</code>
     * conversion.
     */
    private static final Map<String, TaskState> stringValuesMap
            = new HashMap(values().length);

    // Initialize backward String conversion <code>Map</code>.
    static {
        for (TaskState state : TaskState.values()) {
            stringValuesMap.put(state.toString().toUpperCase(), state);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a <code>TaskState</code> with a value represented by the
     * specified <code>String</code>. The <code>TaskState</code> returned
     * represents existing value only if specified <code>String</code>
     * matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     * @param stateStr Value containing <code>TaskState</code> 
     *                 <code>toString</code> representation.
     * @return <code>TaskState</code> value represented by <code>String</code>
     *         or <code>null</code> if value was not recognized.
     */
    public static TaskState toValue(final String stateStr) {
        if (stateStr != null) {
            return (stringValuesMap.get(stateStr.toUpperCase()));
        } else {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert <code>TaskState</code> value to <code>String</code>.
     * <p>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        switch (this) {
            case READY:     return READY_STR;
            case RUNNING:   return RUNNING_STR;
            case COMPLETED: return COMPLETED_STR;
            case FAILED:    return FAILED_STR;
            // This is unrecheable. Returned null value means that some
            // enum value is not handled correctly.
            default:        return null;
        }
    }

}
