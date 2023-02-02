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
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.beans.FxProperty;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 * This completer handles enumeration values AND j.l.Boolean.
 * The completer triggers on property values, and picks properties with
 * Enum or Boolean type.
 * 
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public final class EnumValueCompleter implements Completer, Completer.Factory {
    private CompletionContext ctx;
    private TypeElement enumType;
    
    private static final String ICON_ENUM_VALUE = "org/netbeans/modules/javafx2/editor/resources/property.png"; // NOI18N

    public EnumValueCompleter() {
    }

    /**
     * Boolean constructor
     * @param ctx 
     */
    EnumValueCompleter(CompletionContext ctx) {
        this.ctx = ctx;
    }
    
    public EnumValueCompleter(CompletionContext ctx, TypeElement enumType) {
        this.ctx = ctx;
        this.enumType = enumType;
    }
    
    private boolean isBooleanType() {
        return enumType == null;
    }

    @Override
    public List<CompletionItem> complete() {
        List<String> valueStrings = new ArrayList<>();
        if (isBooleanType()) {
            valueStrings.add(Boolean.FALSE.toString());
            valueStrings.add(Boolean.TRUE.toString());
        } else {
            for (Element e : enumType.getEnclosedElements()) {
                if (e.getKind() == ElementKind.ENUM_CONSTANT) {
                    valueStrings.add(e.getSimpleName().toString());
                }
            }
        }
        
        String prefix = ctx.getPrefix();
        if (!prefix.isEmpty()) {
            if (prefix.charAt(0) == '"' || prefix.charAt(0) == '\'') {
                prefix = prefix.substring(1);
            }
            for (Iterator<String> it = valueStrings.iterator(); it.hasNext(); ) {
                String s = it.next();
                if (!CompletionUtils.startsWith(s, prefix)) {
                    it.remove();
                }
            }
        }
        
        List<CompletionItem> items = new ArrayList<CompletionItem>();
        for (String v : valueStrings) {
            ValueItem vi = new ValueItem(ctx, v, ICON_ENUM_VALUE);
            items.add(vi);
        }
        
        return items;
    }

    @Override
    public boolean hasMoreItems() {
        return false;
    }

    @Override
    public Completer createCompleter(CompletionContext ctx) {
        FxProperty p = ctx.getEnclosingProperty();
        if (p == null || p.getType() == null) {
            return null;
        }
        TypeMirror m = p.getType().resolve(ctx.getCompilationInfo());
        if (m.getKind() == TypeKind.BOOLEAN) {
            return new EnumValueCompleter(ctx);
        }
        if (m.getKind() != TypeKind.DECLARED) {
            return null;
        }
        DeclaredType t = (DeclaredType)m;
        TypeElement tel = (TypeElement)t.asElement();
        if (tel.getQualifiedName().contentEquals("java.lang.Boolean")) {
            return new EnumValueCompleter(ctx);
        }
        if (tel.getKind() == ElementKind.ENUM) {
            return new EnumValueCompleter(ctx, tel);
        } else {
            return null;
        }
    }

    
}
