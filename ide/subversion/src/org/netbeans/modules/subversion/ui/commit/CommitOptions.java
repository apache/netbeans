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

package org.netbeans.modules.subversion.ui.commit;

import org.openide.util.NbBundle;

/**
 * @author Maros Sandor
 */
public abstract class CommitOptions {

    public static final CommitOptions ADD_TEXT = new Add("CTL_CommitOption_AddAsText");  // NOI18N
    public static final CommitOptions ADD_BINARY = new Add("CTL_CommitOption_AddAsBinary"); // NOI18N
    public static final CommitOptions ADD_DIRECTORY = new Add("CTL_CommitOption_AddDirectory");  // NOI18N
    public static final CommitOptions COMMIT = new Commit("CTL_CommitOption_Commit"); // NOI18N
    public static final CommitOptions COMMIT_REMOVE = new Commit("CTL_CommitOption_CommitRemove"); // NOI18N
    public static final CommitOptions EXCLUDE = new Commit("CTL_CommitOption_Exclude"); // NOI18N
    
    private final String bundleKey;

    public CommitOptions(String bundleKey) {
        this.bundleKey = bundleKey;
    }

    public String toString() {
        return NbBundle.getMessage(CommitOptions.class, bundleKey);
    }
    
    static class Add extends CommitOptions {
        
        public Add(String bundleKey) {
            super(bundleKey);
        }
    }

    static class Commit extends CommitOptions {
        
        public Commit(String bundleKey) {
            super(bundleKey);
        }
    }
}

