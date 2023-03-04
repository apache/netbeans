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

import java.util.List;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem.ConstructorItem;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem.TypeItem;
import org.netbeans.modules.groovy.editor.api.elements.common.MethodElement;
import org.netbeans.modules.groovy.editor.api.elements.common.MethodElement.MethodParameter;
import org.netbeans.modules.groovy.editor.java.JavaElementHandle;
import org.openide.util.Pair;

/**
 * Avoids publishing additional API methods; the API needs a thorough review, I am not 
 * capable of at the moment.
 * 
 * @author sdedic
 */
public abstract class CompletionAccessor {
    private static CompletionAccessor INSTANCE;
    
    static {
        new TypeItem("", "", 0, ElementKind.CLASS);
    }
    
    public static void setInstance(CompletionAccessor acc) {
        synchronized (CompletionAccessor.class) {
            if (INSTANCE != null && INSTANCE != acc) {
                throw new IllegalStateException();
            }
            INSTANCE = acc;
        }
    }
    
    public static CompletionAccessor instance() {
        return INSTANCE;
    }
    
    /**
     * Assigns a Handle to a java element item. The passed item must be an instance of JavaElementItem
     * Created by forMethod() or forField() factories.
     */
    public abstract CompletionItem assignHandle(CompletionItem item, JavaElementHandle jh);
    
    public abstract ConstructorItem createConstructor(JavaElementHandle h, List<MethodElement.MethodParameter> parameters, int anchorOffset, boolean expand);
    
    public abstract TypeItem createType(JavaElementHandle h, String qn, String n,  int anchorOffset, javax.lang.model.element.ElementKind ek);
    
    public abstract CompletionItem createJavaMethod(String className, String simpleName, List<MethodParameter> parameters,
            String returnType, Set<javax.lang.model.element.Modifier> modifiers, int anchorOffset,
            boolean emphasise, boolean nameOnly);
    
    public abstract CompletionItem createDynamicMethod(int anchorOffset, String name, List<MethodParameter> parameters, String returnType, boolean prefix);
    
    public abstract Pair<String, List<MethodParameter>> getParametersAndType(CompletionItem item);
    
    public abstract CompletionItem sortOverride(CompletionItem item, int override);
}
