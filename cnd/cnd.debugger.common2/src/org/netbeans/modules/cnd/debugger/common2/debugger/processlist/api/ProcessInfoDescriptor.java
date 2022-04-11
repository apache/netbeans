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

package org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api;

/**
 * Describes a chunck of process information [column description in a process
 * list table]
 *
 */
public final class ProcessInfoDescriptor {

    //ids are the same for all platforms
    //ID is used to understand all other staff
    public static final String UID_COLUMN_ID = "uid"; //NOI18N
    public static final String PID_COLUMN_ID = "pid"; //NOI18N
    public static final String PPID_COLUMN_ID = "ppid"; //NOI18N
    public static final String COMMAND_COLUMN_ID = "command"; //NOI18N
    public static final String EXECUTABLE_COLUMN_ID = "executable"; //NOI18N
    public static final String STIME_COLUMN_ID = "stime"; //NOI18N
    public static final String STIME_WINDOWS_COLUMN_ID = "stime_windows"; //NOI18N

    public final String id;
    public final String command;
    public final Class type;
    public final String header;
    public final String shortDescription;
    public final boolean isUserVisible;

    public ProcessInfoDescriptor(String id, String command, Class type,
            String header, String shortDescription) {
        this(true, id, command, type, header, shortDescription);
    }

    public ProcessInfoDescriptor(boolean isUserVisible, String id, String command,
            Class type, String header, String shortDescription) {
        this.isUserVisible = isUserVisible;
        this.id = id;
        this.command = command;
        this.type = type;
        this.header = header;
        this.shortDescription = shortDescription;
    }
}
