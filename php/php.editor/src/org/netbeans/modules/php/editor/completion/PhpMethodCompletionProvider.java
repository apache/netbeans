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
package org.netbeans.modules.php.editor.completion;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.languages.neon.spi.completion.MethodCompletionProvider;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class PhpMethodCompletionProvider implements MethodCompletionProvider {
    private static final PhpMethodCompletionProvider INSTANCE = new PhpMethodCompletionProvider();

    @MethodCompletionProvider.Registration(position = 100)
    public static PhpMethodCompletionProvider getInstance() {
        return INSTANCE;
    }

    private PhpMethodCompletionProvider() {
    }

    @Override
    public Set<String> complete(String prefix, String typeName, FileObject fileObject) {
        Set<String> result = new HashSet<>();
        if (typeName != null && !typeName.isEmpty()) {
            ElementQuery.Index indexQuery = ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(fileObject));
            Set<TypeElement> types = indexQuery.getTypes(NameKind.prefix(typeName));
            for (TypeElement typeElement : types) {
                Set<MethodElement> accessibleMethods = indexQuery.getAccessibleMethods(typeElement, typeElement);
                Set<MethodElement> filteredMethods = ElementFilter.forName(NameKind.prefix(prefix)).filter(accessibleMethods);
                for (MethodElement methodElement : filteredMethods) {
                    result.add(methodElement.asString(BaseFunctionElement.PrintAs.NameAndParamsInvocation).trim());
                }
            }
        }
        return result;
    }

}
