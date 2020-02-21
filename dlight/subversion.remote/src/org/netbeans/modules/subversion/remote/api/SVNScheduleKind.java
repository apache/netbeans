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
package org.netbeans.modules.subversion.remote.api;

/**
 *
 * 
 */
public enum SVNScheduleKind {
    NORMAL("normal"), //NOI18N
    ADD("add"), //NOI18N
    DELETE("delete"), //NOI18N
    REPLACE("replace"); //NOI18N
    
    private final String value;
    private SVNScheduleKind(String value) {
        this.value = value;
    }
    public static SVNScheduleKind fromString(String s) {
        for(SVNScheduleKind r : SVNScheduleKind.values()) {
            if (r.value.equals(s)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return value;
    }
}
