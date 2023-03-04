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
 * Class representing a PHP interface.
 */
public final class PhpInterface extends PhpType {

    public PhpInterface(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed String description) {
        super(name, fullyQualifiedName, description);
    }

    public PhpInterface(@NonNull String name, @NullAllowed String fullyQualifiedName) {
        super(name, fullyQualifiedName);
    }

    public PhpInterface(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset) {
        this(name, fullyQualifiedName, offset, null);
    }

    public PhpInterface(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
        super(name, fullyQualifiedName, offset, description);
    }

    @Override
    public PhpInterface addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
        super.addMethod(name, fullyQualifiedName, offset, description);
        return this;
    }

    @Override
    public PhpInterface addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset) {
        return addMethod(name, fullyQualifiedName, offset, null);
    }

    @Override
    public PhpInterface addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName) {
        return addMethod(name, fullyQualifiedName, -1, null);
    }

    @Override
    public PhpInterface addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed String description) {
        return addMethod(name, fullyQualifiedName, -1, description);
    }

}
