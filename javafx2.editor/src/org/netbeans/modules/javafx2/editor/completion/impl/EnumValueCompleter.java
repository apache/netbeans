/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
        List<String> valueStrings = new ArrayList<String>();
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
