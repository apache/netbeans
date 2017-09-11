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
