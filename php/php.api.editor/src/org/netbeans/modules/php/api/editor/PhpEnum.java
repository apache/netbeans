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
 * Class representing a PHP enum.
 */
public class PhpEnum extends PhpType {

    public PhpEnum(String name, String fullyQualifiedName, String description) {
        super(name, fullyQualifiedName, description);
    }

    public PhpEnum(String name, String fullyQualifiedName) {
        super(name, fullyQualifiedName);
    }

    public PhpEnum(String name, String fullyQualifiedName, int offset) {
        super(name, fullyQualifiedName, offset);
    }

    public PhpEnum(String name, String fullyQualifiedName, int offset, String description) {
        super(name, fullyQualifiedName, offset, description);
    }

    @Override
    public PhpEnum addField(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
        super.addField(name, fullyQualifiedName, offset, description);
        return this;
    }

    @Override
    public PhpEnum addField(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset) {
        return addField(name, fullyQualifiedName, offset, null);
    }

    @Override
    public PhpEnum addField(@NonNull String name, @NullAllowed String fullyQualifiedName) {
        return addField(name, fullyQualifiedName, -1, null);
    }

    @Override
    public PhpEnum addField(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed String description) {
        return addField(name, fullyQualifiedName, -1, description);
    }

    @Override
    public PhpEnum addField(@NonNull String name, @NullAllowed PhpType type, @NullAllowed FileObject file, int offset) {
        super.addField(name, type, file, offset);
        return this;
    }

    @Override
    public PhpEnum addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
        super.addMethod(name, fullyQualifiedName, offset, description);
        return this;
    }

    @Override
    public PhpEnum addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset) {
        return addMethod(name, fullyQualifiedName, offset, null);
    }

    @Override
    public PhpEnum addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName) {
        return addMethod(name, fullyQualifiedName, -1, null);
    }

    @Override
    public PhpEnum addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed String description) {
        return addMethod(name, fullyQualifiedName, -1, description);
    }

}
