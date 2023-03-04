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
package org.netbeans.modules.php.api.editor;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Class representing a PHP element ({@link PhpType PHP type}, method, field etc.).
 */
public abstract class PhpBaseElement {

    private final String name;
    private final String fullyQualifiedName;
    private FileObject file;
    private final int offset;
    private final String description;
    private final PhpType type;


    protected PhpBaseElement(@NonNull String name, @NullAllowed PhpType type) {
        this(name, null, type, null, -1, null);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName) {
        this(name, fullyQualifiedName, -1, null);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed FileObject file) {
        this(name, fullyQualifiedName, file, -1, null);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed String description) {
        this(name, fullyQualifiedName, -1, description);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset) {
        this(name, fullyQualifiedName, offset, null);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
        this(name, fullyQualifiedName, null, offset, description);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed FileObject file,
            int offset, @NullAllowed String description) {
        this(name, fullyQualifiedName, null, file, offset, description);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed PhpType type,
            @NullAllowed FileObject file, int offset, @NullAllowed String description) {
        Parameters.notEmpty("name", name);

        this.name = name;
        this.fullyQualifiedName = fullyQualifiedName;
        this.type = type;
        this.file = file;
        this.offset = offset;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    @CheckForNull
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    @CheckForNull
    public PhpType getType() {
        return type;
    }

    @CheckForNull
    public FileObject getFile() {
        return file;
    }

    public int getOffset() {
        return offset;
    }

    @CheckForNull
    public String getDescription() {
        return description;
    }

    /**
     * @param file the file to set
     */
    public void setFile(FileObject file) {
        this.file = file;
    }

}
