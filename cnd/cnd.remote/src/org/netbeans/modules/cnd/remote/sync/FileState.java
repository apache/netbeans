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

package org.netbeans.modules.cnd.remote.sync;

import org.netbeans.modules.cnd.utils.CndUtils;

public enum FileState {

    /** New on local host */
    INITIAL('i'),

    /** Created ant touched on remote */
    TOUCHED('t'),

    /** Copied to remote */
    COPIED('c'),

    /** Is not controlled  */
    UNCONTROLLED('u'),

    /** The file does not exist on local host (although belongs to the project) */
    INEXISTENT('n'),

    /** Error occured when touching or copying */
    ERROR('e');

    public final char id;

    FileState(char id) {
        this.id = id;
    }

    public static FileState fromId(char c) {
        for (FileState state : FileState.values()) {
            if (state.id == c) {
                return state;
            }
        }
        CndUtils.assertTrue(false, "Unexpected state char: " + c); //NOI18N
        return INITIAL;
    }
}
