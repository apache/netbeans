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
