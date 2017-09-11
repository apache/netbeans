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
