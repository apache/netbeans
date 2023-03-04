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

package org.netbeans.spi.debugger.ui;


/**
 * Contains various debuggercore-ui constants.
 *
 * @author   Jan Jancura
 */
public interface Constants {

    /**
     * Thread State column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String THREAD_STATE_COLUMN_ID = "ThreadState";

    /**
     * Thread Suspended column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String THREAD_SUSPENDED_COLUMN_ID = "ThreadSuspended";

    /**
     * Breakpoint Enabled column id.
     *
     * @deprecated Do not use any more, enabled column was removed from Breakpoints view.
     * Replaced with {@link org.netbeans.spi.viewmodel.CheckNodeModel}
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    @Deprecated
    public static final String BREAKPOINT_ENABLED_COLUMN_ID =
        "BreakpointEnabled";

    /**
     * CallStackFrame Location column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String CALL_STACK_FRAME_LOCATION_COLUMN_ID = 
        "CallStackFrameLocation";

    /**
     * Locals toString () column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String LOCALS_TO_STRING_COLUMN_ID = "LocalsToString";

    /**
     * Locals Tyoe column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String LOCALS_TYPE_COLUMN_ID = "LocalsType";

    /**
     * Locals Value column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String LOCALS_VALUE_COLUMN_ID = "LocalsValue";

    /**
     * Watch toString () column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     */
    public static final String WATCH_TO_STRING_COLUMN_ID = "WatchToString";

    /**
     * Watch Tyoe column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String WATCH_TYPE_COLUMN_ID = "WatchType";

    /**
     * Watch Value column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String WATCH_VALUE_COLUMN_ID = "WatchValue";

    /**
     * Session Host Name column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String SESSION_HOST_NAME_COLUMN_ID = "SessionHostName";

    /**
     * Session State column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String SESSION_STATE_COLUMN_ID = "SessionState";

    /**
     * Session Language column id.
     *
     * @see org.netbeans.spi.viewmodel.ColumnModel#getID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getPreviuosColumnID
     * @see org.netbeans.spi.viewmodel.ColumnModel#getNextColumnID
     */
    public static final String SESSION_LANGUAGE_COLUMN_ID = "SessionLanguage";
}
