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

import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.ElementKind;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bhaidu
 */
public class PhpFunctionElement extends BladeElement {

    @NullAllowed
    private final String namespace;
    private final List<String> params;

    public PhpFunctionElement(String name, FileObject file,
            ElementType type,
            String namespace,
            List<String> params) {
        super(name, file, type);
        this.namespace = namespace;
        this.params = List.copyOf(params);
    }

    public PhpFunctionElement(String name, FileObject file,
            ElementType type,
            List<String> params) {
        super(name, file, type);
        this.namespace = null;
        this.params = List.copyOf(params);
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.METHOD;
    }

    public String getParamsAsString() {
        if (params == null || params.isEmpty()) {
            return "()"; // NOI18N
        }
        return "(" + String.join(", ", params) + ")"; // NOI18N
    }

    public List<String> getParams() {
        return params;
    }

    @CheckForNull
    public String getNamespace() {
        return namespace;
    }

}
