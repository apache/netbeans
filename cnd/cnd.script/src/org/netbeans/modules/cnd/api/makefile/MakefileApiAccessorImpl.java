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
package org.netbeans.modules.cnd.api.makefile;

import java.util.List;
import org.netbeans.modules.cnd.makefile.MakefileApiAccessor;
import org.openide.filesystems.FileObject;

/**
 */
/*package*/ final class MakefileApiAccessorImpl extends MakefileApiAccessor {

    @Override
    public MakefileInclude newMakefileInclude(FileObject fileObject, int startOffset, int endOffset, List<String> fileNames) {
        return new MakefileInclude(fileObject, startOffset, endOffset, fileNames);
    }

    @Override
    public MakefileMacro newMakefileMacro(FileObject fileObject, int startOffset, int endOffset, String name, String value) {
        return new MakefileMacro(fileObject, startOffset, endOffset, name, value);
    }

    @Override
    public MakefileRule newMakefileRule(FileObject fileObject, int startOffset, int endOffset, List<String> targets, List<String> prereqs) {
        return new MakefileRule(fileObject, startOffset, endOffset, targets, prereqs);
    }
}
