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
public enum SVNStatusKind {
    NONE("none"), //NOI18N
    NORMAL("normal"), //NOI18N
    ADDED("added"), //NOI18N
    MISSING("missing"), //NOI18N
    INCOMPLETE("incomplete"), //NOI18N
    DELETED("deleted"), //NOI18N
    REPLACED("replaced"), //NOI18N
    MODIFIED("modified"), //NOI18N
    MERGED("merged"), //NOI18N
    CONFLICTED("conflicted"), //NOI18N
    OBSTRUCTED("obstructed"), //NOI18N
    IGNORED("ignored"), //NOI18N
    EXTERNAL("external"), //NOI18N
    UNVERSIONED("unversioned"); //NOI18N

    private final String value;
    private SVNStatusKind(String value) {
        this.value = value;
    }
    public static SVNStatusKind fromString(String s) {
        for(SVNStatusKind r : SVNStatusKind.values()) {
            if (r.value.equals(s)) {
                return r;
            }
        }
        if ("non-svn".equals(s)) { //NOI18N
            return NONE;
        }
        throw new IllegalArgumentException("Unknown status " + s); //NOI18N
    }

    @Override
    public String toString() {
        return value;
    }
}
