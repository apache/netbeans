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
package org.netbeans.modules.cnd.makefile;

import java.util.List;
import org.netbeans.modules.cnd.api.makefile.MakefileInclude;
import org.netbeans.modules.cnd.api.makefile.MakefileMacro;
import org.netbeans.modules.cnd.api.makefile.MakefileRule;
import org.netbeans.modules.cnd.api.makefile.MakefileSupport;
import org.openide.filesystems.FileObject;

/**
 */
public abstract class MakefileApiAccessor {

    private static volatile MakefileApiAccessor INSTANCE;

    public static MakefileApiAccessor getInstance() {
        if (INSTANCE == null) {
            try {
                Class.forName(MakefileSupport.class.getName());
            } catch (ClassNotFoundException ex) {
                // should not happen
            }
            if (INSTANCE == null) {
                throw new IllegalStateException("Accessor not set"); // NOI18N
            }
        }
        return INSTANCE;
    }

    public static void setInstance(MakefileApiAccessor instance) {
        if (instance == null) {
            throw new IllegalArgumentException();
        } else if (INSTANCE != null) {
            throw new IllegalStateException("Accessor already set"); // NOI18N
        } else {
            INSTANCE = instance;
        }
    }

    public abstract MakefileInclude newMakefileInclude(
            FileObject fileObject, int startOffset, int endOffset,
            List<String> fileNames);

    public abstract MakefileMacro newMakefileMacro(
            FileObject fileObject, int startOffset, int endOffset,
            String name, String value);

    public abstract MakefileRule newMakefileRule(
            FileObject fileObject, int startOffset, int endOffset,
            List<String> targets, List<String> prereqs);
}
