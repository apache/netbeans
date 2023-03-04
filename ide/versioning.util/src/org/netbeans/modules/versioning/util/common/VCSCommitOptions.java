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

package org.netbeans.modules.versioning.util.common;

import org.openide.util.NbBundle;

/**
 * @author Maros Sandor
 */
public abstract class VCSCommitOptions {

    public static final VCSCommitOptions COMMIT = new Commit(NbBundle.getMessage(VCSCommitOptions.class, "CTL_CommitOption_Commit")); // NOI18N
    public static final VCSCommitOptions COMMIT_REMOVE = new Commit(NbBundle.getMessage(VCSCommitOptions.class, "CTL_CommitOption_CommitRemove")); // NOI18N
    public static final VCSCommitOptions EXCLUDE = new Commit(NbBundle.getMessage(VCSCommitOptions.class, "CTL_CommitOption_Exclude")); // NOI18N
    
    private final String label;

    private VCSCommitOptions (String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
    
    public static class Add extends VCSCommitOptions {
        
        public Add(String label) {
            super(label);
        }
    }

    public static class Commit extends VCSCommitOptions {
        
        public Commit(String label) {
            super(label);
        }
    }
}

