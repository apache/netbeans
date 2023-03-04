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

import java.util.Collections;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.beans.FxProperty;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public class FxIncludeCompleter implements Completer, Completer.Factory { 
    private CompletionContext   context;

    public FxIncludeCompleter() {
    }

    FxIncludeCompleter(CompletionContext context) {
        this.context = context;
    }

    @Override
    public List<? extends CompletionItem> complete() {
        FxInstructionItem item = new FxInstructionItem("fx:include", context, 
                "<fx:include source=\"\"/>", CompletionUtils.makeFxNamespaceCreator(context));
        return Collections.singletonList(item);
    }

    @Override
    public boolean hasMoreItems() {
        return false;
    }

    @Override
    public Completer createCompleter(CompletionContext ctx) {
        switch (ctx.getType()) {
            case BEAN:
            case ROOT:
            case CHILD_ELEMENT:
            case PROPERTY_ELEMENT:
                break;
            default:
                return null;
        }
        FxProperty prop = ctx.getEnclosingProperty();
        if (prop != null) {
            TypeMirror tm = prop.getType().resolve(ctx.getCompilationInfo());
            // check that the type is a subclass of javafx.scene.Node
            TypeElement te = ctx.getCompilationInfo().getElements().getTypeElement("javafx.scene.Node");
            if (te != null && ctx.getCompilationInfo().getTypes().isAssignable(te.asType(), tm)) {
                return new FxIncludeCompleter(ctx);                
                
            }
        }
        return null;
        
    }

    
    
}
