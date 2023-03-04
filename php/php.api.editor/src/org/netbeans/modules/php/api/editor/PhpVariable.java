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

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;

/**
 * Class representing a PHP local variable.
 * @author Tomas Mysik
 */
public final class PhpVariable extends PhpBaseElement {

    public PhpVariable(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed String description) {
        super(name, fullyQualifiedName, description);
    }

    public PhpVariable(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed FileObject file) {
        super(name, fullyQualifiedName, file);
    }

    public PhpVariable(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed FileObject file, int offset) {
        super(name, fullyQualifiedName, file, offset, null);
    }

    public PhpVariable(@NonNull String name, @NullAllowed PhpClass type, @NullAllowed FileObject file, int offset) {
        super(name, null, type, file, offset, null);
    }

    public PhpVariable(@NonNull String name, @NullAllowed String fullyQualifiedName) {
        super(name, fullyQualifiedName);
    }

    public PhpVariable(@NonNull String name, @NullAllowed PhpClass type) {
        super(name, type);
    }

    public PhpVariable(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
        super(name, fullyQualifiedName, offset, description);
    }

    public PhpVariable(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset) {
        super(name, fullyQualifiedName, offset);
    }
}
