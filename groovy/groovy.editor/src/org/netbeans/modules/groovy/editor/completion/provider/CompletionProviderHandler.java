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

package org.netbeans.modules.groovy.editor.completion.provider;

import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.spi.completion.CompletionProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public final class CompletionProviderHandler implements CompletionProvider {

    @Override
    public Map<MethodSignature, CompletionItem> getMethods(CompletionContext context) {
        Map<MethodSignature, CompletionItem> result = new HashMap<>();

        if (context.getSourceFile() != null) {
            for (CompletionProvider provider : Lookup.getDefault().lookupAll(CompletionProvider.class)) {
                for (Map.Entry<MethodSignature, CompletionItem> entry : provider.getMethods(context).entrySet()) {
                    if (entry.getKey().getName().startsWith(context.getPrefix())) {
                        result.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Map<FieldSignature, CompletionItem> getFields(CompletionContext context) {
        Map<FieldSignature, CompletionItem> result = new HashMap<>();
        
        if (context.getSourceFile() != null) {
            for (CompletionProvider provider : Lookup.getDefault().lookupAll(CompletionProvider.class)) {
                for (Map.Entry<FieldSignature, CompletionItem> entry : provider.getFields(context).entrySet()) {
                    if (entry.getKey().getName().startsWith(context.getPrefix())) {
                        result.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        return result;
    }
    
    @Override
    public Map<MethodSignature, CompletionItem> getStaticMethods(CompletionContext context) {
        Map<MethodSignature, CompletionItem> result = new HashMap<>();
        
        if (context.getSourceFile() != null) {
            for (CompletionProvider provider : Lookup.getDefault().lookupAll(CompletionProvider.class)) {
                for (Map.Entry<MethodSignature, CompletionItem> entry : provider.getStaticMethods(context).entrySet()) {
                    if (entry.getKey().getName().startsWith(context.getPrefix())) {
                        result.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        return result;
    }
    
    @Override
    public Map<FieldSignature, CompletionItem> getStaticFields(CompletionContext context) {
        Map<FieldSignature, CompletionItem> result = new HashMap<>();
        
        if (context.getSourceFile() != null) {
            for (CompletionProvider provider : Lookup.getDefault().lookupAll(CompletionProvider.class)) {
                for (Map.Entry<FieldSignature, CompletionItem> entry : provider.getStaticFields(context).entrySet()) {
                    if (entry.getKey().getName().startsWith(context.getPrefix())) {
                        result.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        return result;
    }
}
