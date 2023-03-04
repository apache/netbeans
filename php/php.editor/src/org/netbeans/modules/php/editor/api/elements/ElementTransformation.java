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

package org.netbeans.modules.php.editor.api.elements;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.php.editor.elements.VariableElementImpl;

/**
 *
 * @author rmatous
 */
public abstract class ElementTransformation<S extends PhpElement> {
    public abstract S transform(PhpElement element);

    public final <T extends PhpElement> Set<S> transform(Set<T> original) {
        Set<S> retval = new HashSet<>();
        for (T baseElement : original) {
            final S transformed = transform(baseElement);
            if (transformed != null) {
                retval.add(transformed);
            }
        }
        return Collections.unmodifiableSet(retval);
    }

    public static ElementTransformation<TypeElement> toMemberTypes() {
        return new ElementTransformation<TypeElement>() {
            @Override
            public TypeElement transform(PhpElement element) {
                if (element instanceof TypeMemberElement) {
                    TypeMemberElement typeMemberElement = (TypeMemberElement) element;
                    return typeMemberElement.getType();
                }
                return null;
            }
        };
    }

    /**
     * intended for frameworks.
     * @return
     */
    public static ElementTransformation<VariableElement> fieldsToVariables() {
        return new ElementTransformation<VariableElement>() {
            @Override
            public VariableElement transform(PhpElement element) {
                if (element instanceof FieldElement) {
                    FieldElement field = (FieldElement) element;
                    return VariableElementImpl.create(
                            field.getName(),
                            field.getOffset(),
                            field.getFilenameUrl(),
                            field.getElementQuery(),
                            field.getInstanceTypes(),
                            field.isDeprecated());
                }
                return null;
            }
        };
    }

}
