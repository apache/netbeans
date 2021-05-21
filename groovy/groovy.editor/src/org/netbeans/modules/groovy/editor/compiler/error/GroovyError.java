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

package org.netbeans.modules.groovy.editor.compiler.error;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.Error.Badging;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

/**
 * This is a copy of DefaultError with the additional Groovy information.
 */
public class GroovyError implements Badging {

    private final String displayName;
    private final String description;
    private final FileObject file;
    private final int start;
    private final int end;
    private final String key;
    private final Severity severity;
    private final CompilerErrorID id;


    public GroovyError(
            @NullAllowed String key,
            @NonNull String displayName,
            @NullAllowed String description,
            @NullAllowed FileObject file,
            @NonNull int start,
            @NonNull int end,
            @NonNull Severity severity,
            @NonNull CompilerErrorID id) {
        this.key = key;
        this.displayName = displayName;
        this.description = description;
        this.file = file;
        this.start = start;
        this.end = end;
        this.severity = severity;
        this.id = id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getStartPosition() {
        return start;
    }

    @Override
    public int getEndPosition() {
        return end;
    }

    @Override
    public String toString() {
        return "GroovyError[" + displayName + ", " + description + ", " + severity + "]";
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Object[] getParameters() {
        return null;
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public FileObject getFile() {
        return file;
    }

    public CompilerErrorID getId() {
        return id;
    }

    @Override
    public boolean isLineError() {
        return true;
    }

    @Override
    public boolean showExplorerBadge() {
        return false;
    }
}
