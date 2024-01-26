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
package org.netbeans.modules.php.editor.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.model.impl.Type;

public final class ElementUtils {

    private ElementUtils() {
    }

    public static boolean isAbstractTraitMethod(MethodElement method) {
        return method.getType().isTrait() && method.isAbstract();
    }

    public static boolean isVoidOrNeverType(Collection<TypeResolver> types) {
        if (types.size() == 1) {
            for (TypeResolver returnType : types) {
                String rawTypeName = returnType.getRawTypeName();
                if (Type.VOID.equals(rawTypeName) || Type.NEVER.equals(rawTypeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isToStringMagicMethod(MethodElement method) {
        return method.getName().equals("__toString"); // NOI18N
    }

    public static String getToStringMagicMethodBody(TypeElement type, Index index) {
        StringBuilder sb = new StringBuilder();
        if (index != null) {
            List<FieldElement> allFields = new ArrayList<>(index.getAlllFields(type));
            allFields.sort((f1, f2) -> Integer.compare(f1.getOffset(), f2.getOffset()));
            sb.append("return \"").append(type.getName()).append("["); // NOI18N
            if (allFields.isEmpty()) {
                sb.append("]\";"); // NOI18N
            } else {
                boolean isFirst = true;
                for (FieldElement field : allFields) {
                    if (field.isStatic()) {
                        continue;
                    }
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        sb.append("\n. \", "); // NOI18N
                    }
                    sb.append(field.getName(false)).append("=\"").append(" . $this->").append(field.getName(false));
                }
                sb.append("\n. \"]\";"); // NOI18N
            }
        }
        return sb.toString();
    }
}
