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
package org.netbeans.modules.csl.spi;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;


/**
 * Simple implementation of the Error interface, which can be used for convenience
 * when generating errors during (for example) program parsing.
 *
 * @author Tor Norbye
 */
public class DefaultError implements Error {
    private String displayName;
    private String description;

    //private List<Fix> fixes;
    private FileObject file;
    private int start;
    private int end;
    private boolean lineError;
    private String key;
    private Severity severity;
    private Object[] parameters;

    public static Error createDefaultError(@NullAllowed String key,
            @NonNull String displayName,
            @NullAllowed String description,
            @NonNull FileObject file,
            @NonNull int start,
            @NonNull int end,
            boolean lineError,
            @NonNull Severity severity) {
        return new DefaultError(key, displayName, description, file, start, end, lineError, severity);
    }

    /** Creates a new instance of DefaultError */
    public DefaultError(
            @NullAllowed String key,
            @NonNull String displayName,
            @NullAllowed String description,
            @NonNull FileObject file,
            @NonNull int start,
            @NonNull int end,
            @NonNull Severity severity) {
        this(key, displayName, description, file, start, end, true, severity);
    }
    
    /** Creates a new instance of DefaultError */
    public DefaultError(
            @NullAllowed String key,
            @NonNull String displayName, 
            @NullAllowed String description,
            @NonNull FileObject file, 
            @NonNull int start, 
            @NonNull int end,
            boolean lineError,
            @NonNull Severity severity) {
        this.key = key;
        this.displayName = displayName;
        this.description = description;
        this.file = file;
        this.start = start;
        this.end = end;
        this.lineError = lineError;
        this.severity = severity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    // TODO rename to getStartOffset
    public int getStartPosition() {
        return start;
    }

    // TODO rename to getEndOffset
    public int getEndPosition() {
        return end;
    }

    public boolean isLineError() {
        return lineError;
    }

    @Override
    public String toString() {
        return "DefaultError[" + displayName + ", " + description + ", " + severity + "]";
    }

    public String getKey() {
        return key;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(final Object[] parameters) {
        this.parameters = parameters;
    }

    public Severity getSeverity() {
        return severity;
    }

    public FileObject getFile() {
        return file;
    }

    public void setOffsets(int start, int end) {
        this.start = start;
        this.end = end;
    }
}
