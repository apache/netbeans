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

package org.netbeans.modules.cnd.debugger.common2.debugger;

public interface Constants extends org.netbeans.spi.debugger.ui.Constants {

    // Used for ColumnModel ID's.
    //
    // Note how some of "our" PROPs are aliased to debuggercore
    // COLUMN_IDs.

    // Thread view
    public static final String PROP_THREAD_STATE =
	THREAD_STATE_COLUMN_ID;
    public static final String PROP_THREAD_SUSPENDED =
	THREAD_SUSPENDED_COLUMN_ID;

    public static final String PROP_THREAD_PRIORITY =
	"PROP_THREAD_PRIORITY"; // NOI18N
    public static final String PROP_THREAD_LWP =
	"PROP_THREAD_LWP"; // NOI18N
    public static final String PROP_THREAD_STARTUP_FLAGS =
	"PROP_THREAD_STARTUP_FLAGS"; // NOI18N
    public static final String PROP_THREAD_EXECUTING_FUNCTION =
	"PROP_THREAD_EXECUTING_FUNCTION"; // NOI18N
    public static final String PROP_THREAD_START_FUNCTION =
	"PROP_THREAD_START_FUNCTION"; // NOI18N
    public static final String PROP_THREAD_ADDRESS =
	"PROP_THREAD_ADDRESS"; // NOI18N
    public static final String PROP_THREAD_SIZE =
	"PROP_THREAD_SIZE"; // NOI18N
    public static final String PROP_THREAD_ID =
	"PROP_THREAD_ID"; // NOI18N
    public static final String PROP_THREAD_FILE =
	"PROP_THREAD_FILE"; // NOI18N
    public static final String PROP_THREAD_LINE =
	"PROP_THREAD_LINE"; // NOI18N

    // Stack view
    public static final String PROP_FRAME_LOCATION =
	CALL_STACK_FRAME_LOCATION_COLUMN_ID;

    public static final String PROP_FRAME_NUMBER =
	"PROP_FRAME_NUMBER"; // NOI18N
    public static final String PROP_FRAME_OPTIMIZED =
	"PROP_FRAME_OPTIMIZED"; // NOI18N
    public static final String PROP_FRAME_CURRENT_PC =
	"PROP_FRAME_CURRENT_PC"; // NOI18N
    public static final String PROP_FRAME_LOADOBJ =
	"PROP_FRAME_LOADOBJ"; // NOI18N

    public static final String PROP_BREAKPOINT_ENABLE  =
	"PROP_BREAKPOINT_ENABLE"; // NOI18N
    public static final String PROP_BREAKPOINT_COUNTLIMIT  =
	"PROP_BREAKPOINT_COUNTLIMIT"; // NOI18N
    public static final String PROP_BREAKPOINT_COUNT  =
	"PROP_BREAKPOINT_COUNT"; // NOI18N
    public static final String PROP_BREAKPOINT_LWP =
	"PROP_BREAKPOINT_LWP"; // NOI18N
    public static final String PROP_BREAKPOINT_ID =
	"PROP_BREAKPOINT_ID"; // NOI18N
    public static final String PROP_BREAKPOINT_CONTEXT =
	"PROP_BREAKPOINT_CONTEXT"; // NOI18N
    public static final String PROP_BREAKPOINT_WHILEIN =
	"PROP_BREAKPOINT_WHILEIN"; // NOI18N
    public static final String PROP_BREAKPOINT_QWHILEIN =
	"PROP_BREAKPOINT_QWHILEIN"; // NOI18N
    public static final String PROP_BREAKPOINT_CONDITION =
	"PROP_BREAKPOINT_CONDITION"; // NOI18N
    public static final String PROP_BREAKPOINT_QCONDITION =
	"PROP_BREAKPOINT_QCONDITION"; // NOI18N
    public static final String PROP_BREAKPOINT_THREAD =
	"PROP_BREAKPOINT_THREAD"; // NOI18N
    public static final String PROP_BREAKPOINT_TEMP =
	"PROP_BREAKPOINT_TEMP"; // NOI18N
    public static final String PROP_BREAKPOINT_JAVA =
	"PROP_BREAKPOINT_JAVA"; // NOI18N
    public static final String PROP_BREAKPOINT_TIMESTAMP =
	"PROP_BREAKPOINT_TIMESTAMP"; // NOI18N


    // Session view
    public static final String PROP_SESSION_PID =
	"PROP_SESSION_PID"; // NOI18N
    public static final String PROP_SESSION_CURRENT_LANGUAGE =
	SESSION_LANGUAGE_COLUMN_ID;
    public static final String PROP_SESSION_LOCATION =
	"PROP_SESSION_LOCATION"; // NOI18N
    public static final String SESSION_DEBUGGER_COLUMN_ID =
	"SESSION_DEBUGGER_COLUMN_ID"; // NOI18N
    public static final String PROP_SESSION_MODE =
	"PROP_SESSION_MODE"; // NOI18N
    public static final String PROP_SESSION_ARGS =
	"PROP_SESSION_ARGS"; // NOI18N
    public static final String PROP_SESSION_CORE =
	"PROP_SESSION_CORE"; // NOI18N
    public static final String PROP_SESSION_HOST =
	"PROP_SESSION_HOST"; // NOI18N

    // Local view
    public static final String PROP_LOCAL_TYPE =
	LOCALS_TYPE_COLUMN_ID;
    public static final String PROP_LOCAL_VALUE =
	LOCALS_VALUE_COLUMN_ID;
    public static final String PROP_LOCAL_TO_STRING =
	LOCALS_TO_STRING_COLUMN_ID;
    public static final String PROP_LOCAL_ADDRESS =
	"PROP_ADDRESS_COLUMN_ID"; // NOI18N
    public static final String PROP_LOCAL_DTYPE =
	"PROP_DTYPE_COLUMN_ID"; // NOI18N

    // Watch view
    public static final String PROP_WATCH_TYPE =
	WATCH_TYPE_COLUMN_ID;
    public static final String PROP_WATCH_VALUE =
	WATCH_VALUE_COLUMN_ID;
    public static final String PROP_WATCH_TO_STRING =
	WATCH_TO_STRING_COLUMN_ID;

}
