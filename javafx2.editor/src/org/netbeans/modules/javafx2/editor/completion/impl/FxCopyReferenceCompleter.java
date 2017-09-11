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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.completion.beans.FxProperty;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.modules.javafx2.editor.completion.model.PropertyValue;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.completion.impl.Bundle.*;
import org.netbeans.modules.javafx2.editor.completion.model.FxXmlSymbols;

/**
 * Suggests fx:reference and/or fx:copy appropriate for the context.
 *
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public class FxCopyReferenceCompleter implements Completer, Completer.Factory {
    private CompletionContext   context;

    public FxCopyReferenceCompleter() {
    }

    FxCopyReferenceCompleter(CompletionContext context) {
        this.context = context;
    }
    
    @NbBundle.Messages({
        "FMT_fxReferenceCompletionItem=<fx:reference source=\"\"",
        "FMT_fxCopyCompletionItem=<fx:copy source=\"\""
    })
    @Override
    public List<CompletionItem> complete() {
        Callable<String> fxNs = CompletionUtils.makeFxNamespaceCreator(context);
        List<CompletionItem> items = new ArrayList<CompletionItem>(2);
        
        FxInstructionItem inst = new FxInstructionItem(
                "fx:reference",  // NOI18N
                context, 
                FMT_fxReferenceCompletionItem(), 
                fxNs);
        items.add(inst);

        inst = new FxInstructionItem(
                "fx:copy",  // NOI18N
                context, 
                FMT_fxCopyCompletionItem(), 
                fxNs);
        items.add(inst);
        return items;
    }

    @Override
    public boolean hasMoreItems() {
        return false;
    }

    @Override
    public Completer createCompleter(CompletionContext ctx) {
        switch (ctx.getType()) {
            case BEAN:
            case CHILD_ELEMENT:
            case PROPERTY_ELEMENT:
                break;
            default:
                return null;
        }
        
        String tn = ctx.getTagName();
        if (tn != null && !"".equals(tn)) {
            tn = tn.toLowerCase();
            if (!(FxXmlSymbols.FX_REFERENCE.startsWith(tn) ||
                FxXmlSymbols.FX_COPY.startsWith(tn))) {

                String prefix = ctx.findFxmlNsPrefix();
                if (prefix == null) {
                    prefix = JavaFXEditorUtils.FXML_FX_PREFIX;
                }
                tn = prefix + ":" + tn; // NOI18N
                if (!(FxXmlSymbols.FX_REFERENCE.startsWith(tn) ||
                    FxXmlSymbols.FX_COPY.startsWith(tn))) {
                    return null;
                }
            }
        }
        
        // try to find at least 1 item that matches the contents:
        if (!matchingInstanceExists(ctx)) {
            return null;
        }
        return new FxCopyReferenceCompleter(ctx);
    }
    
    private static boolean matchingInstanceExists(CompletionContext ctx) {
        Collection<String> instanceNames = ctx.getModel().getInstanceNames();
        // check that we have at least SOME named items:
        if (instanceNames.isEmpty()) {
            return false;
        }
        FxNode parentNode = ctx.getElementParent();

        if (parentNode == null) {
            return false;
        }
        TypeMirror t;
        
        if (parentNode.getKind() == FxNode.Kind.Instance) {
            FxBean bi = ctx.getBeanInfo((FxInstance)parentNode);
            if (bi == null) {
                return true;
            }
            FxProperty def = bi.getDefaultProperty();
            if (def == null || def.getType() == null) {
                return false;
            }
            t = def.getType().resolve(ctx.getCompilationInfo());
        } else if (parentNode.getKind() != FxNode.Kind.Property) {
            // !! the Node may have a default property !
            return false; 
        } else {
            PropertyValue v = (PropertyValue)parentNode;
            if (v.getTypeHandle() == null) {
                // can offer any type
                t = null;
            } else {
                t = v.getTypeHandle().resolve(ctx.getCompilationInfo());
            }
        }
        if (t == null) {
            return true;
        }
        
        for (String n : instanceNames) {
            FxInstance inst = ctx.getModel().getInstance(n);
            ElementHandle<TypeElement> tHandle = inst.getJavaType();
            if (t == null) {
                return true;
            }
            if (tHandle == null) {
                continue;
            }
            TypeElement instType = tHandle.resolve(ctx.getCompilationInfo());
            if (instType == null) {
                continue;
            }
            if (ctx.getCompilationInfo().getTypes().isAssignable(
                    instType.asType(),
                    t)) {
                return true;
            }
        }
        return false;
    }
}
