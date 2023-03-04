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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;

/**
 * Base class for PHP type (class, interface, trait).
 * @since 0.28
 */
public abstract class PhpType extends PhpBaseElement {

    private final Collection<Field> fields = new LinkedList<>();
    private final Collection<Method> methods = new LinkedList<>();


    protected PhpType(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed String description) {
        super(name, fullyQualifiedName, description);
    }

    protected PhpType(@NonNull String name, @NullAllowed String fullyQualifiedName) {
        super(name, fullyQualifiedName);
    }

    protected PhpType(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset) {
        this(name, fullyQualifiedName, offset, null);
    }

    protected PhpType(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
        super(name, fullyQualifiedName, offset, description);
    }

    protected PhpType addField(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
        fields.add(new Field(name, fullyQualifiedName, offset, description));
        return this;
    }

    protected PhpType addField(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset) {
        return addField(name, fullyQualifiedName, offset, null);
    }

    protected PhpType addField(@NonNull String name, @NullAllowed String fullyQualifiedName) {
        return addField(name, fullyQualifiedName, -1, null);
    }

    protected PhpType addField(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed String description) {
        return addField(name, fullyQualifiedName, -1, description);
    }

    protected PhpType addField(@NonNull String name, @NullAllowed PhpType type, @NullAllowed FileObject file, int offset) {
        fields.add(new Field(name, type, file, offset));
        return this;
    }

    protected PhpType addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
        methods.add(new Method(name, fullyQualifiedName, offset, description));
        return this;
    }

    protected PhpType addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset) {
        return addMethod(name, fullyQualifiedName, offset, null);
    }

    protected PhpType addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName) {
        return addMethod(name, fullyQualifiedName, -1, null);
    }

    protected PhpType addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed String description) {
        return addMethod(name, fullyQualifiedName, -1, description);
    }

    public Collection<Field> getFields() {
        return new ArrayList<>(fields);
    }

    public Collection<Method> getMethods() {
        return new ArrayList<>(methods);
    }

    /**
     * Class representing a PHP type field.
     */
    public final class Field extends PhpBaseElement {

        Field(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
            super(name, fullyQualifiedName, offset, description);
        }

        Field(@NonNull String name, @NullAllowed PhpType type, @NullAllowed FileObject file, int offset) {
            super(name, null, type, file, offset, null);
        }

        public PhpType getPhpType() {
            return PhpType.this;
        }

    }

    /**
     * Class representing a PHP type method.
     */
    public final class Method extends PhpBaseElement {

        Method(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
            super(name, fullyQualifiedName, offset, description);
        }

        public PhpType getPhpType() {
            return PhpType.this;
        }

    }

}
