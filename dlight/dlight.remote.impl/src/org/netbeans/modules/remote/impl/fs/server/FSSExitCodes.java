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

package org.netbeans.modules.remote.impl.fs.server;

import org.netbeans.modules.nativeexecution.api.util.Signal;

/**
 *
 */
public class FSSExitCodes {

    // see "exitcodes.h" in fs_server sources
    
    static final int FAILURE_LOCKING_MUTEX                  = 201;
    static final int FAILURE_UNLOCKING_MUTEX                = 202;
    static final int WRONG_ARGUMENT                         = 203;
    static final int FAILURE_GETTING_HOME_DIR               = 204;
    static final int FAILURE_CREATING_STORAGE_SUPER_DIR     = 205;
    static final int FAILURE_ACCESSING_STORAGE_SUPER_DIR    = 206;
    static final int FAILURE_CREATING_STORAGE_DIR           = 207;
    static final int FAILURE_ACCESSING_STORAGE_DIR          = 208;
    static final int FAILURE_CREATING_TEMP_DIR              = 209;
    static final int FAILURE_ACCESSING_TEMP_DIR             = 210;
    static final int FAILURE_CREATING_CACHE_DIR             = 211;
    static final int FAILURE_ACCESSING_CACHE_DIR            = 212;
    static final int NO_MEMORY_EXPANDING_DIRTAB             = 213;
    static final int FAILED_CHDIR                           = 214;
    static final int FAILURE_OPENING_LOCK_FILE              = 215;
    static final int FAILURE_LOCKING_LOCK_FILE              = 216;
    static final int FAILURE_DIRTAB_DOUBLE_CACHE_OPEN       = 217;

    static final int FS_SPECIFIC_START = FAILURE_LOCKING_MUTEX;
    static final int FS_SPECIFIC_END = FAILURE_DIRTAB_DOUBLE_CACHE_OPEN;

    static final int GENERAL_ERROR = 1;
    
    public static boolean isSignal(int exitCode) {
        return getSignal(exitCode) != null;
    }
    
    public static Signal getSignal(int exitCode) {
        return Signal.valueOf(exitCode - 128);
    }
}
