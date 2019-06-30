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
 * Events that caused state of Payara server administration command execution
 * change.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum TaskEvent {

    ////////////////////////////////////////////////////////////////////////////
    // Enum values                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /** Task queued for execution. */
    SUBMIT,
    /** Start of task. */
    START,
    /** Exception in task. */
    EXCEPTION,
    /** Illegal task state. */
    ILLEGAL_STATE,
    /** Missing server process. */
    PROCESS_NOT_EXISTS,
    /** Process not running. */
    PROCESS_NOT_RUNNING,
    /** Server command running. */
    CMD_RUNNING,
    /** Server command exception. */
    CMD_EXCEPTION,
    /** Server command completed. */
    CMD_COMPLETED,
    /** Server command failed. */
    CMD_FAILED,
    /** Authorization failed on HTTP protocol level (401 or 403 response). 
      * This is usually handled by java.net.Authenticator. */
    AUTH_FAILED_HTTP,
    /** Authorization failed on asadmin response level (response
     *  in Manifest). Here java.net.Authenticator does nothing. */
    AUTH_FAILED,
    /** Empty message received. */
    EMPTY_MESSAGE,
    /** Java VM not found. */
    NO_JAVA_VM,
    /** Java VM version is wrong. */
    WRONG_JAVA_VM,
    /** Java VM execution failed. */
    JAVA_VM_EXEC_FAILED,
    /** Signals wrong proxy settings. */
    BAD_GATEWAY;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**  A <code>String</code> representation of SUBMIT value. */
    private static final String SUBMIT_STR = "Submit";

    /**  A <code>String</code> representation of START value. */
    private static final String START_STR = "Start";

    /**  A <code>String</code> representation of EXCEPTION value. */
    private static final String EXCEPTION_STR = "Exception";

    /**  A <code>String</code> representation of ILLEGAL_STATE value. */
    private static final String ILLEGAL_STATE_STR = "IllegalState";

    /**  A <code>String</code> representation of PROCESS_NOT_EXISTS value. */
    private static final String PROCESS_NOT_EXISTS_STR = "ProcessNotExists";

    /**  A <code>String</code> representation of PROCESS_NOT_RUNNING value. */
    private static final String PROCESS_NOT_RUNNING_STR = "ProcessNotRunning";

    /**  A <code>String</code> representation of CMD_RUNNING value. */
    private static final String CMD_RUNNING_STR = "CmdRunning";

    /**  A <code>String</code> representation of CMD_EXCEPTION value. */
    private static final String CMD_EXCEPTION_STR = "CmdException";

    /**  A <code>String</code> representation of CMD_COMPLETED value. */
    private static final String CMD_COMPLETED_STR = "CmdCompleted";

    /**  A <code>String</code> representation of CMD_FAILED value. */
    private static final String CMD_FAILED_STR = "CmdFailed";

    /** A <code>String</code> representation of AUTH_FAILED_HTTP value. */
    private static final String AUTH_FAILED_HTTP_STR = "AuthFailedHttp";

    /** A <code>String</code> representation of AUTH_FAILED value. */
    private static final String AUTH_FAILED_STR = "AuthFailed";

    /** A <code>String</code> representation of EMPTY_MESSAGE value. */
    private static final String EMPTY_MESSAGE_STR = "EmptyMessage";

    /** A <code>String</code> representation of NO_JAVA_VM value. */
    private static final String NO_JAVA_VM_STR = "NoJavaVm";

    /** A <code>String</code> representation of NO_JAVA_VM value. */
    private static final String WRONG_JAVA_VM_STR = "WrongJavaVm";

    /** A <code>String</code> representation of JAVA_VM_EXEC_FAILED value. */
    private static final String JAVA_VM_EXEC_FAILED_STR = "JavaVmExecFailed";
    
    /** A <code>String</code> representation of BAD_GATEWAY value. */
    private static final String BAD_GATEWAY_STR = "BadGateway";

    /** 
     * Stored <code>String</code> values for backward <code>String</code>
     * conversion.
     */
    private static final Map<String, TaskEvent> stringValuesMap
            = new HashMap(values().length);

    // Initialize backward String conversion <code>Map</code>.
    static {
        for (TaskEvent state : TaskEvent.values()) {
            stringValuesMap.put(state.toString().toUpperCase(), state);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a <code>TaskEvent</code> with a value represented by the
     * specified <code>String</code>. The <code>TaskEvent</code> returned
     * represents existing value only if specified <code>String</code>
     * matches any <code>String</code> returned by <code>toString</code> method.
     * Otherwise <code>null</code> value is returned.
     * <p>
     * @param eventStr Value containing <code>TaskEvent</code> 
     *                 <code>toString</code> representation.
     * @return <code>TaskEvent</code> value represented by <code>String</code>
     *         or <code>null</code> if value was not recognized.
     */
    public static TaskEvent toValue(String eventStr) {
        if (eventStr != null) {
            return (stringValuesMap.get(eventStr.toUpperCase()));
        } else {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert <code>TaskEvent</code> value to <code>String</code>.
     * <p>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        switch (this) {
            case SUBMIT:              return SUBMIT_STR;
            case START:               return START_STR;
            case EXCEPTION:           return EXCEPTION_STR;
            case ILLEGAL_STATE:       return ILLEGAL_STATE_STR;
            case PROCESS_NOT_EXISTS:  return PROCESS_NOT_EXISTS_STR;
            case PROCESS_NOT_RUNNING: return PROCESS_NOT_RUNNING_STR;
            case CMD_RUNNING:         return CMD_RUNNING_STR;
            case CMD_EXCEPTION:       return CMD_EXCEPTION_STR;
            case CMD_COMPLETED:       return CMD_COMPLETED_STR;
            case CMD_FAILED:          return CMD_FAILED_STR;
            case AUTH_FAILED_HTTP:    return AUTH_FAILED_HTTP_STR;
            case AUTH_FAILED:         return AUTH_FAILED_STR;
            case EMPTY_MESSAGE:       return EMPTY_MESSAGE_STR;
            case NO_JAVA_VM:          return NO_JAVA_VM_STR;
            case WRONG_JAVA_VM:       return WRONG_JAVA_VM_STR;
            case JAVA_VM_EXEC_FAILED: return JAVA_VM_EXEC_FAILED_STR;
            case BAD_GATEWAY:         return BAD_GATEWAY_STR;
            // This is unrecheable. Returned null value means that some
            // enum value is not handled correctly.
            default:            return null;
        }
    }

}
