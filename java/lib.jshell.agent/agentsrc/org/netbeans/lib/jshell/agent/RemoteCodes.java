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
package org.netbeans.lib.jshell.agent;

/**
 * Communication constants shared between the main process and the remote
 * execution process
 * @author Robert Field
 */
public class RemoteCodes {
    // Command codes
    public static final int CMD_EXIT       = 0;
    public static final int CMD_LOAD       = 1;
    public static final int CMD_INVOKE     = 3;
    public static final int CMD_CLASSPATH  = 4;
    public static final int CMD_VARVALUE   = 5;

    // Return result codes
    public static final int RESULT_SUCCESS   = 100;
    public static final int RESULT_FAIL      = 101;
    public static final int RESULT_EXCEPTION = 102;
    public static final int RESULT_CORRALLED = 103;
    public static final int RESULT_KILLED    = 104;
}
