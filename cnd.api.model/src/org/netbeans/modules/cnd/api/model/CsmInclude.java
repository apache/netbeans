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

package org.netbeans.modules.cnd.api.model;

/**
 * Represents include directive
 */
public interface CsmInclude extends CsmOffsetable {
    public enum IncludeState {
        Success,
        Fail,
        Recursive
    }

    /**
     * Returns the name that of the included file,
     * '"', "<" and ">" characters are not included.
     */
    CharSequence getIncludeName();

    /** Gets include file */
    CsmFile getIncludeFile();

    /** Gets include state */
    IncludeState getIncludeState();

    /**
     * Distinguishes whether this is a "system" include (for example #include <stdio.h>)
     * or user include (#include "MyLib.h")
     */
    boolean isSystem();    
}
