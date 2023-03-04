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
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.completion.beans.FxProperty;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public class IdReferenceCompleter implements Completer, Completer.Factory {
    private static final String ICON_RESOURCE = "org/netbeans/modules/javafx2/editor/resources/variable_ref.png"; // NOI18N
    private CompletionContext ctx;
    
    public IdReferenceCompleter() {
    }

    IdReferenceCompleter(CompletionContext ctx) {
        this.ctx = ctx;
    }
    
    @Override
    public List<? extends CompletionItem> complete() {
        Set<String> names = ctx.getModel().getInstanceNames();
        String prefix = ctx.getPrefix();
        if (prefix.startsWith("$")) {
            prefix = prefix.substring(1);
        }
        
        FxProperty prop = ctx.getEnclosingProperty();
        TypeMirror el = prop.getType().resolve(ctx.getCompilationInfo());
        List<CompletionItem>    items = new ArrayList<CompletionItem>();
        for (String s : names) {
            if (!prefix.isEmpty() && !s.startsWith(prefix)) {
                continue;
            }
            FxInstance inst = ctx.getModel().getInstance(s);
            if (inst == null) {
                // ?? error in model ?
                throw new IllegalStateException();
            }
            FxBean def = inst.getDefinition();
            if (def == null) {
                continue;
            }
            // be conservative, allow unknown items in completion
            if (def.getJavaType() != null) {
                TypeElement clazz = def.getJavaType().resolve(ctx.getCompilationInfo());
                if (clazz != null) {
                    if (!ctx.getCompilationInfo().getTypes().isAssignable(el, clazz.asType())) {
                        continue;
                    }
                }
            }
            
            items.add(new ValueItem(ctx, s, "$", ICON_RESOURCE));
        }
        return items;
    }

    @Override
    public boolean hasMoreItems() {
        return false;
    }

    @Override
    public Completer createCompleter(CompletionContext ctx) {
        switch (ctx.getType()) {
            case VARIABLE:
            case PROPERTY_VALUE:
            case PROPERTY_VALUE_CONTENT:
                break;
                
            default:
                return null;
        }
        FxProperty prop = ctx.getEnclosingProperty();
        if (prop == null) {
            return null;
        }
        return new IdReferenceCompleter(ctx);
    }

    
}
