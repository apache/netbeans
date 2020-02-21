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
 public enum FSSRequestKind {
    FS_REQ_LS('l'), 
    FS_REQ_RECURSIVE_LS('r'), 
    FS_REQ_STAT('S'), 
    FS_REQ_LSTAT('s'),
    FS_REQ_QUIT('q'),
    FS_REQ_SLEEP('P'),
    FS_REQ_ADD_WATCH('W'),
    FS_REQ_REMOVE_WATCH('w'),
    FS_REQ_REFRESH('R'),
    FS_REQ_DELETE('d'),
    FS_REQ_DELETE_ON_DISCONNECT('D'),
    FS_REQ_COPY('C'),
    FS_REQ_MOVE('m'),
    FS_REQ_SERVER_INFO('i'),
    FS_REQ_OPTION('o');

    private final char letter;

    private FSSRequestKind(char letter) {
        this.letter = letter;
    }

    public char getChar() {
        return letter;
    }
    
}
