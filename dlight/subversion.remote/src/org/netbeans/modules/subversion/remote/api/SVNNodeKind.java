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
public enum SVNNodeKind {
    NONE("none"), //NOI18N
    FILE("file"), //NOI18N
    DIR("dir"), //NOI18N
    UNKNOWN("unknown"); //NOI18N
    
    private final String value;
    private SVNNodeKind(String value) {
        this.value = value;
    }
    public static SVNNodeKind fromString(String s) {
        for(SVNNodeKind r : SVNNodeKind.values()) {
            if (r.value.equals(s)) {
                return r;
            }
        }
        if ("directory".equals(s)) { //NOI18N
            return DIR;
        }
        throw new IllegalArgumentException("Unknown node " + s); //NOI18N
    }

    @Override
    public String toString() {
        return value;
    }
}
