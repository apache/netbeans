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
public class SVNConflictDescriptor {
    public enum Operation {
        _none("none"), //NOI18N
        _update("update"), //NOI18N
        _switch("switch"), //NOI18N
        _merge("merge"); //NOI18N
        
        private final String value;
        private Operation(String value) {
            this.value = value;
        }
        public static Operation fromString(String s) {
            for(Operation o : Operation.values()) {
                if (o.value.equals(s)) {
                    return o;
                }
            }
            return null;
        }
    }

    public enum Reason {
        edited("edited"), //NOI18N
        obstructed("obstructed"), //NOI18N
        deleted("deleted"), //NOI18N
        missing("missing"), //NOI18N
        unversioned("unversioned"), //NOI18N
        added("added"), //NOI18N
        replaced("replaced"), //NOI18N
        moved_away("moved_away"), //NOI18N
        moved_here("moved_here"); //NOI18N
        
        private final String value;
        private Reason(String value) {
            this.value = value;
        }
        public static Reason fromString(String s) {
            for(Reason r : Reason.values()) {
                if (r.value.equals(s)) {
                    return r;
                }
            }
            return null;
        }
    }

    public enum Action {
        edit("edited"), //NOI18N
        add("added"), //NOI18N
        delete("deleted"); //NOI18N
        
        private final String value;
        private Action(String value) {
            this.value = value;
        }
        public static Action fromString(String s) {
            for(Action r : Action.values()) {
                if (r.value.equals(s)) {
                    return r;
                }
            }
            return null;
        }
    }

    public enum Kind {
        text,
        property;
    }
    
    private final String path;
    private final Action action;
    private final Operation operation;
    private final SVNConflictVersion srcLeftVersion;
    private final SVNConflictVersion srcRightVersion;
    
    public SVNConflictDescriptor(String path, Action action, Reason reason, Operation operation, SVNConflictVersion srcLeftVersion, SVNConflictVersion srcRightVersion) {
        this.path = path;
        this.action = action;
        this.operation = operation;
        this.srcLeftVersion = srcLeftVersion;
        this.srcRightVersion = srcRightVersion;
    }

    public String getPath() {
        return path;
    }

    public SVNConflictVersion.NodeKind getNodeKind() {
        return srcLeftVersion.getNodeKind();
    }

    public Action getAction() {
        return action;
    }

    public Operation getOperation() {
        return operation;
    }

    public SVNConflictVersion getSrcLeftVersion() {
        return srcLeftVersion;
    }

    public SVNConflictVersion getSrcRightVersion() {
        return srcRightVersion;
    }
}
