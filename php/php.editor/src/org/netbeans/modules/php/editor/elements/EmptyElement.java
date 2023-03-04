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
package org.netbeans.modules.php.editor.elements;

import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class EmptyElement extends PhpElementImpl implements TypeMemberElement {
    private final TypeElement enclosingType;

    public EmptyElement(final TypeElement enclosingType) {
        super(
                "__EMPTY__", //NOI18N
                enclosingType.getName(),
                enclosingType.getFilenameUrl(),
                enclosingType.getOffset(),
                enclosingType.getElementQuery(),
                enclosingType.isDeprecated());
        this.enclosingType = enclosingType;
    }

    @Override
    public String getSignature() {
        return "";
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return PhpElementKind.EMPTY;
    }

    @Override
    public TypeElement getType() {
        return enclosingType;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

}
