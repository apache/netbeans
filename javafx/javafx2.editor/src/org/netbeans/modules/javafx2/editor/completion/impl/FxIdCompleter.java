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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.model.FxClassUtils;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public class FxIdCompleter implements Completer, Completer.Factory {
    private CompletionContext ctx;
    private ElementHandle<TypeElement> controllerType;
    
    private static final String ICON_RESOURCE = "org/netbeans/modules/javafx2/editor/resources/fxid-value.gif"; // NOI18N
    
    public FxIdCompleter() {
    }

    FxIdCompleter(CompletionContext ctx, ElementHandle<TypeElement> controllerType) {
        this.ctx = ctx;
        this.controllerType = controllerType;
    }

    @Override
    public List<CompletionItem> complete() {
        // get type of the enclosing instance
        FxInstance inst = (FxInstance)ctx.getElementParent();
        TypeElement instType = inst.getJavaType().resolve(ctx.getCompilationInfo());
        TypeElement ct = controllerType.resolve(ctx.getCompilationInfo());
        if (ct == null) {
            return null;
        }
        
        List<CompletionItem> items = new ArrayList<CompletionItem>();
        for (VariableElement v : ElementFilter.fieldsIn(
                ctx.getCompilationInfo().getElements().getAllMembers(ct))) {
            if (!FxClassUtils.isFxmlAccessible(v)) {
                continue;
            }
            // do not suggest a variable, whose ID is used elsewhere:
            String sn = v.getSimpleName().toString();
            if (ctx.getModel().getInstance(sn) != null) {
                continue;
            }
            // check that the instance is assignable to the var
            if (ctx.getCompilationInfo().getTypes().isAssignable(instType.asType(), v.asType())) {
                ValueItem vi = new ValueItem(
                    ctx,
                    v.getSimpleName().toString(),
                    ICON_RESOURCE
                );
                vi.setAttribute(ctx.getType() == CompletionContext.Type.PROPERTY_VALUE);
                items.add(vi);
            }
        }
        
        return items;
    }

    @Override
    public boolean hasMoreItems() {
        return false;
    }

    @Override
    public Completer createCompleter(CompletionContext ctx) {
        if (ctx.getType() != CompletionContext.Type.PROPERTY_VALUE ||
            ctx.getElementParent().getKind() != FxNode.Kind.Instance) {
            return null;
        }
        
        // check if the property is the fx:id
        String propName = ctx.getPropertyName();
        String prefix = ctx.findFxmlNsPrefix();
        if (prefix == null || !(prefix + ":id").equals(propName)) {
            return null;
        }
        // check that the controller is defined && resolved
        ElementHandle<TypeElement> controllerType = ctx.getModel().getControllerType();
        if (controllerType != null) {
            return new FxIdCompleter(ctx, controllerType);
        } else {
            return null;
        }
    }
    
}
