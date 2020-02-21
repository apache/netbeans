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

/** 
 * Request and response kind 
 */
 public enum FSSResponseKind {
    FS_RSP_LS('l'), 
    FS_RSP_RECURSIVE_LS('r'), 
    FS_RSP_ENTRY('e'), 
    FS_RSP_END('x'),
    FS_RSP_CHANGE('c'),
    FS_RSP_ERROR('E'),
    FS_RSP_REFRESH('R'),
    FS_RSP_SERVER_INFO('i');

    private final char letter;

    private FSSResponseKind(char letter) {
        this.letter = letter;
    }

    public char getChar() {
        return letter;
    }
    
    public static FSSResponseKind fromChar(char c) {
        for (FSSResponseKind v : values()) {
            if (v.letter == c) {
                return v;
            }
        }
        return null;
    }
}
