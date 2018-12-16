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

package org.netbeans.modules.groovy.editor.completion.provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedElement;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedField;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedMethod;
import org.netbeans.modules.groovy.editor.completion.AccessLevel;
import org.netbeans.modules.groovy.editor.java.Utilities;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.spi.completion.CompletionProvider;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 * @author Martin Janicek
 */
public final class GroovyElementsProvider implements CompletionProvider {

    @Override
    public Map<MethodSignature, CompletionItem> getMethods(CompletionContext context) {
        final GroovyIndex index = getIndex(context);
        final Map<MethodSignature, CompletionItem> result = new HashMap<>();
        
        if (index != null) {
            Set<IndexedMethod> methods;

            if ("".equals(context.getPrefix())) { // NOI18N
                methods = index.getMethods(".*", context.getTypeName(), QuerySupport.Kind.REGEXP); // NOI18N
            } else {
                methods = index.getMethods(context.getPrefix(), context.getTypeName(), QuerySupport.Kind.PREFIX);
            }

            for (IndexedMethod indexedMethod : methods) {
                if (accept(context.access, indexedMethod)) {
                    result.put(getMethodSignature(indexedMethod), CompletionItem.forJavaMethod(
                            context.getTypeName(),
                            indexedMethod.getName(),
                            indexedMethod.getParameterTypes(),
                            indexedMethod.getReturnType(),
                            Utilities.gsfModifiersToModel(indexedMethod.getModifiers(), Modifier.PUBLIC),
                            context.getAnchor(),
                            false,
                            context.isNameOnly()));
                }
            }
        }

        return result;
    }

    @Override
    public Map<MethodSignature, CompletionItem> getStaticMethods(CompletionContext context) {
        return Collections.emptyMap();
    }

    @Override
    public Map<FieldSignature, CompletionItem> getFields(CompletionContext context) {
        final GroovyIndex index = getIndex(context);
        final Map<FieldSignature, CompletionItem> result = new HashMap<>();
        
        if (index != null) {
            Set<IndexedField> fields;

            if ("".equals(context.getPrefix())) { // NOI18N
                fields = index.getAllFields(context.getTypeName());
            } else {
                fields = index.getFields(context.getPrefix(), context.getTypeName(), QuerySupport.Kind.PREFIX);
            }

            for (IndexedField indexedField : fields) {
                result.put(getFieldSignature(indexedField), new CompletionItem.FieldItem(
                        indexedField.getTypeName(),
                        indexedField.getName(),
                        indexedField.getModifiers(),
                        context.getAnchor()));
            }
        }

        return result;
    }

    @Override
    public Map<FieldSignature, CompletionItem> getStaticFields(CompletionContext context) {
        return Collections.emptyMap();
    }
    
    private GroovyIndex getIndex(CompletionContext context) {
        final FileObject fo = context.getSourceFile();
        
        if (fo != null) {
            return GroovyIndex.get(QuerySupport.findRoots(fo, Collections.singleton(ClassPath.SOURCE), null, null));
        }
        return null;
    }

    private MethodSignature getMethodSignature(IndexedMethod method) {
        String[] parameters = method.getParameterTypes().toArray(new String[method.getParameterTypes().size()]);
        return new MethodSignature(method.getName(), parameters);
    }

    private FieldSignature getFieldSignature(IndexedField field) {
        return new FieldSignature(field.getName());
    }

    private boolean accept(Set<AccessLevel> levels, IndexedElement element) {
        for (AccessLevel accessLevel : levels) {
            if (accessLevel.accept(element.getModifiers())) {
                return true;
            }
        }

        return false;
    }
}
