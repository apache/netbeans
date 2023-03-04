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

package org.netbeans.libs.git;

import org.netbeans.libs.git.jgit.Utils;

/**
 * When there is a merge conflict in a file from a repository, the file's status 
 * provides instance of this class and you can get more information about the conflict.
 * Can be acquired with {@link GitStatus#getConflictDescriptor() } method.
 * 
 * @author Ondra Vrabec
 */
public final class GitConflictDescriptor {
    private Type type;

    GitConflictDescriptor (Type type) {
        this.type = type;
    }
    
    public static enum Type {
        /**
         * Deleted in both branches.
         */
        BOTH_DELETED {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_BOTH_DELETED.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Deleted by both"; //NOI18N
            }
        },
        /**
         * Added by us
         */
        ADDED_BY_US {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_ADDED_BY_US.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Added by us"; //NOI18N
            }
        },
        /**
         * Modified but deleted in other branch
         */
        DELETED_BY_THEM {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_DELETED_BY_THEM.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Deleted by them"; //NOI18N
            }
        },
        /**
         * Added by them
         */
        ADDED_BY_THEM {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_ADDED_BY_THEM.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Added by them"; //NOI18N
            }
        },
        /**
         * Deleted and modified in other branch
         */
        DELETED_BY_US {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_DELETED_BY_US.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Deleted by us"; //NOI18N
            }
        },
        /**
         * Added in two branches simultaneously
         */
        BOTH_ADDED {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_BOTH_ADDED.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Added by both"; //NOI18N
            }
        },
        /**
         * Modified in two branches simultaneously
         */
        BOTH_MODIFIED {
            @Override
            public String getDescription () {
                return Utils.getBundle(GitConflictDescriptor.class).getString("MSG_GitConflictDescriptor_BOTH_MODIFIED.desc"); //NOI18N
            }

            @Override
            public String toString () {
                return "Modified by both"; //NOI18N
            }
        };

        public abstract String getDescription ();
    }

    /**
     * @return type of the merge conflict
     */
    public Type getType () {
        return type;
    }
}
