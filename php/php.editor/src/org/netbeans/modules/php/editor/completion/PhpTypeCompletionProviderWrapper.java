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
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.spi.templates.completion.CompletionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@ServiceProvider(service = CompletionProvider.class, path = "Neon/completion/type") //NOI18N
public final class PhpTypeCompletionProviderWrapper implements CompletionProvider {

    @Override
    public Set<String> getItems(FileObject sourceFile, String prefix) {
        return PhpTypeCompletionProvider.getInstance().getItems(sourceFile, prefix);
    }

    public static final class PhpTypeCompletionProvider implements CompletionProvider {
        private static final PhpTypeCompletionProvider INSTANCE = new PhpTypeCompletionProvider();
        //@GuardedBy("this")
        private Set<TypeElement> cachedElements;

        private PhpTypeCompletionProvider() {
        }

        public static PhpTypeCompletionProvider getInstance() {
            return INSTANCE;
        }

        @Override
        public Set<String> getItems(FileObject sourceFile, String prefix) {
            Set<String> result = new HashSet<>();
            for (TypeElement typeElement : ElementFilter.forName(NameKind.prefix(prefix)).filter(getElements(sourceFile))) {
                if (!CodeUtils.isSyntheticTypeName(typeElement.getName())) {
                    result.add(typeElement.getFullyQualifiedName().toString());
                }
            }
            return result;
        }

        private synchronized Set<TypeElement> getElements(FileObject fileObject) {
            if (cachedElements == null) {
                ElementQuery.Index indexQuery = ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(fileObject));
                cachedElements = indexQuery.getTypes(NameKind.empty());
            }
            return cachedElements;
        }

        public synchronized void clearCache() {
            cachedElements = null;
        }

    }

}
