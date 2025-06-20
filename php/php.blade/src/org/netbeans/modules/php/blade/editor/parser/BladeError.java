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
package org.netbeans.modules.php.blade.editor.parser;

import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
@org.netbeans.api.annotations.common.SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class BladeError implements org.netbeans.modules.csl.api.Error.Badging {
    private static final boolean SILENT_ERROR_BADGE =  Boolean.getBoolean("nb.php.silent.error.badge"); //NOI18N
    private final String displayName;
    private final FileObject file;
    private final int startPosition;
    private final int endPosition;
    private final Severity severity;

    public BladeError(String displayName, FileObject file, int startPosition, int endPosition, Severity severity) {
        this.displayName = displayName;
        this.file = file;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.severity = severity;
    }


    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getKey() {
        return "[" + startPosition + "," + endPosition + "]-" + displayName;
    }

    @Override
    public FileObject getFile() {
        return this.file;
    }

    @Override
    public int getStartPosition() {
        return this.startPosition;
    }

    @Override
    public int getEndPosition() {
        return this.endPosition;
    }

    @Override
    public Severity getSeverity() {
        return this.severity;
    }

    @Override
    public Object[] getParameters() {
        return new Object[]{};
    }

    @Override
    public boolean isLineError() {
        return true;
    }

    @Override
    public boolean showExplorerBadge() {
        return !SILENT_ERROR_BADGE;
    }
}
