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

package org.netbeans.modules.groovy.editor.spi.completion;

import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import java.util.Map;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;

/**
 * Provides additional code completion items for the given {@link CompletionContext}.
 * 
 * @author Petr Hejl
 * @author Martin Janicek
 */
public interface CompletionProvider {

    Map<MethodSignature, CompletionItem> getMethods(CompletionContext context);
    
    Map<MethodSignature, CompletionItem> getStaticMethods(CompletionContext context);

    Map<FieldSignature, CompletionItem> getFields(CompletionContext context);
    
    Map<FieldSignature, CompletionItem> getStaticFields(CompletionContext context);

}
