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

import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 */
public abstract class MakefileElement {

    private final Kind kind;
    private final FileObject fileObject;
    private final int startOffset;
    private final int endOffset;

    /*package*/ MakefileElement(Kind kind, FileObject fileObject, int startOffset, int endOffset) {
        Parameters.notNull("kind", kind); // NOI18N
        Parameters.notNull("fileObject", fileObject); // NOI18N
        if (endOffset < startOffset) {
            throw new IllegalArgumentException(String.format(
                    "endOffset:%d < startOffset:%d", endOffset, startOffset)); // NOI18N
        }
        this.kind = kind;
        this.fileObject = fileObject;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public final Kind getKind() {
        return kind;
    }

    public final FileObject getContainingFile() {
        return fileObject;
    }

    public final int getStartOffset() {
        return startOffset;
    }

    public final int getEndOffset() {
        return endOffset;
    }

    public static enum Kind {
        MACRO,
        RULE,
        INCLUDE
    }
}
