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
package org.netbeans.modules.php.editor.codegen;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeNameResolver;
import org.netbeans.modules.php.editor.elements.TypeNameResolverImpl;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class CodegenUtils {

    private CodegenUtils() {
    }

    /**
     * Creates chain of resolvers in this order: forFullyQualifiedName and forSmartName.
     * <p>
     * If affected file is of version PHP 5.2, {@code TypeNameResolverImpl.forUnqualifiedName()} resolver is used.
     * </p>
     *
     * @param originalElement element, where fully qualified name resolver is applied.
     * @param currentModel model, where smart name resolver is applied.
     * @param caretOffset offset in current model, where action was invoked.
     * @return
     */
    public static TypeNameResolver createSmarterTypeNameResolver(final PhpElement originalElement, final Model currentModel, final int caretOffset) {
        assert originalElement != null;
        assert currentModel != null;
        TypeNameResolver result = TypeNameResolverImpl.forNull();
        final List<TypeNameResolver> typeNameResolvers = new LinkedList<>();
        FileScope fileScope = ModelUtils.getFileScope(originalElement.getFileObject(), 300);
        if (fileScope != null) {
            FileObject fileObject = fileScope.getFileObject();
            if (fileObject != null) {
                if (CodeUtils.isPhpVersionLessThan(fileObject, PhpVersion.PHP_53)) {
                    typeNameResolvers.add(TypeNameResolverImpl.forUnqualifiedName());
                } else {
                    NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(fileScope, originalElement.getOffset());
                    typeNameResolvers.add(TypeNameResolverImpl.forFullyQualifiedName(namespaceScope, originalElement.getOffset()));
                    VariableScope variableScope = currentModel.getVariableScope(caretOffset);
                    if (variableScope != null) {
                        typeNameResolvers.add(TypeNameResolverImpl.forSmartName(variableScope, caretOffset));
                    }
                }
                result = TypeNameResolverImpl.forChainOf(typeNameResolvers);
            }
        }
        return result;
    }

    public static String getUnusedMethodName(List<String> usedMethods, String methodName) {
        if (usedMethods.contains(methodName)) {
            int counter = 1;
            while (usedMethods.contains(methodName + "_" + counter)) {  //NOI18N
                counter++;
            }
            methodName = methodName + "_" + counter;        //NOI18N
        }
        usedMethods.add(methodName);
        return methodName;
    }

    public static String upFirstLetter(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String upFirstLetterWithoutUnderscore(String name) {
        return upFirstLetter(withoutUnderscore(name));
    }

    public static String withoutUnderscore(String name) {
        return (name.length() > 0 && name.charAt(0) == '_') ? name.substring(1) : name;
    }

}
