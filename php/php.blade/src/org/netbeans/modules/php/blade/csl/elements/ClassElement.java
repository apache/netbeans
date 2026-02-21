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
package org.netbeans.modules.php.blade.csl.elements;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.ElementKind;
import org.openide.filesystems.FileObject;

/**
 *
 * Used for php class element completion
 */
public class ClassElement extends BladeElement {

    @NullAllowed
    private final String namespace;

    public ClassElement(String name, FileObject file) {
        super(name, file, ElementType.PHP_CLASS);
        this.namespace = null;
    }

    public ClassElement(String name, String namespace,
            FileObject file) {
        super(name, file, ElementType.PHP_CLASS);
        this.namespace = namespace;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.CLASS;
    }

    @CheckForNull
    public String getNamespace() {
        return namespace;
    }
}
