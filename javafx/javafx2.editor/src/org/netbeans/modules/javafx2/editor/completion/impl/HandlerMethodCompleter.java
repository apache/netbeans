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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.swing.ImageIcon;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.beans.FxEvent;
import org.netbeans.modules.javafx2.editor.completion.model.EventHandler;
import org.netbeans.modules.javafx2.editor.completion.model.FxClassUtils;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.util.ImageUtilities;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public class HandlerMethodCompleter implements Completer, Completer.Factory {
    private static final String ICON_RESOURCE = "org/netbeans/modules/javafx2/editor/resources/handler.png"; // NOI18N
    private static ImageIcon ICON;
    
    private CompletionContext ctx;
    private FxEvent event;
    private ElementHandle<TypeElement> controller;

    public HandlerMethodCompleter() {
    }

    private HandlerMethodCompleter(CompletionContext ctx, FxEvent event, ElementHandle<TypeElement> controller) {
        this.ctx = ctx;
        this.event = event;
        this.controller = controller;
    }
    
    @Override
    public List<? extends CompletionItem> complete() {
        // attempt to find methods in the controller
        TypeElement el = controller.resolve(ctx.getCompilationInfo());
        TypeElement eventType = event.getEventType().resolve(ctx.getCompilationInfo());
        if (el == null || eventType == null) {
            return null;
        }

        List<ExecutableElement> allMethods = ElementFilter.methodsIn(ctx.getCompilationInfo().getElements().getAllMembers(el));
        List<CompletionItem> items = new ArrayList<CompletionItem>();

        for (ExecutableElement em : allMethods) {
            if (!FxClassUtils.isFxmlAccessible(em)) {
                continue;
            }
            
            if (em.getParameters().size() > 1 || em.getReturnType().getKind() != TypeKind.VOID) {
                continue;
            }
            if (!em.getParameters().isEmpty()) {
                VariableElement v = em.getParameters().get(0);
                if (!ctx.getCompilationInfo().getTypes().isAssignable(v.asType(), eventType.asType())) {
                    continue;
                }
            } else if (!FxClassUtils.isFxmlAnnotated(em)) {
                // require FXML annotation, there are many void no-arg methods.
                continue;
            }
            items.add(new ValueItem(ctx, em.getSimpleName().toString(), "#", ICON_RESOURCE));
        }
        return items;
    }

    @Override
    public boolean hasMoreItems() {
        return false;
    }

    @Override
    public Completer createCompleter(CompletionContext ctx) {
        if (ctx.getType() == CompletionContext.Type.HANDLER ||
            ctx.getType() == CompletionContext.Type.PROPERTY_VALUE ||
            ctx.getType() == CompletionContext.Type.PROPERTY_VALUE_CONTENT) {
            
            FxNode parent = ctx.getParents().get(0);
            if (parent.getKind() == FxNode.Kind.Event) {
                // check that controller is defined:
                if (parent.getRoot().getControllerType() == null) {
                    return null;
                }
                FxEvent e = ((EventHandler)parent).getEventInfo();
                if (e != null) {
                    return new HandlerMethodCompleter(ctx, e, parent.getRoot().getControllerType());
                }
            }
        }
        return null;
    }

    
    
}
