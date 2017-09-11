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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.completion.model.FxClassUtils;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.text.NbDocument;

/**
 * Completion item for classes, which must be initialized using fx:value. The
 * completion item will add the fx:value attribute, position the caret inside
 * the quotes.
 *
 * @author sdedic
 */
final class ValueClassItem extends SimpleClassItem {
    private static final Logger LOG = Logger.getLogger(ValueClassItem.class.getName());

    private boolean shouldClose;

    public ValueClassItem(CompletionContext ctx, 
                            String text, boolean close) {
        super(ctx, text);
        this.shouldClose = close;
    }
    
    protected boolean isValueAttributePresent() {
        String n = ctx.fxAttributeName(getAttributeName());
        if (n == null) {
            return false;
        }
        return ctx.value(n) != null;
    }
    
    protected String getAttributeName() {
        return "value"; // NOI18N
    }

    protected String getSubstituteText() {
        // the opening < is a part of the replacement area
        return super.getSubstituteText();
    }
    
    @Override
    protected void doSubstituteText(JTextComponent c, BaseDocument d, String text) throws BadLocationException {
        // substitute the class element
        super.doSubstituteText(c, d, text);
        if (!isValueAttributePresent()) {
            addAttribute(c, d, text);
        } else {
            if (!ctx.isTagFinished()) {
                int off = getStartOffset() + text.length();
                d.insertString(off, ">", null); // NOI18N
            }
        }
    }
    
    private void addAttribute(JTextComponent c, Document d, String text) throws BadLocationException {
        String nsPrefix = ctx.findFxmlNsPrefix();
        boolean addNsDecl = false;
        
        // declare the namespace, if not yet present
        if (nsPrefix == null) {
            nsPrefix = ctx.findPrefixString(JavaFXEditorUtils.FXML_FX_NAMESPACE_CURRENT, JavaFXEditorUtils.FXML_FX_PREFIX);
            addNsDecl = true;
        }
        int start = getStartOffset() + text.length();
        // fix the position against mutation
        Position startPos = NbDocument.createPosition(d, start, Position.Bias.Backward);

        StringBuilder sb = new StringBuilder();
        sb.append(" "); // tag-attribute separator

        if (ctx.isRootElement() && addNsDecl) {
            // append NS declaration 
            sb.append(createNsDecl(nsPrefix));
        }

        sb.append(ctx.createNSName(nsPrefix, getAttributeName())).append("=\"");

        int l = sb.length();
        sb.append("\"");
        if (!ctx.isTagFinished()) {
            if (shouldClose) {
                sb.append("/>");
            } else {
                sb.append(">");
            }
        }
        d.insertString(start, sb.toString(), null);
        
        if (!ctx.isRootElement() && addNsDecl) {
            d.insertString(
                    ctx.getRootAttrInsertOffset(), 
                    createNsDecl(nsPrefix), null);
        }

        // position the caret inside '' for the user to enter the value
        c.setCaretPosition(startPos.getOffset() + l);
    }
    
    private String createNsDecl(String nsPrefix) {
        StringBuilder sb2 = new StringBuilder();
        // must start with space!
        sb2.append(" xmlns:").
                append(nsPrefix).
                append("=\"").append(JavaFXEditorUtils.FXML_FX_NAMESPACE_CURRENT).append("\" ");
        return sb2.toString();
    }
    
    /**
     * Returns the initial value of the fx:value/fx:factory attribute
     * @return 
     */
    protected String getValue() {
        return ""; // NOI18N
    }
    
    @MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=ClassItemFactory.class)
    public static class Factory implements ClassItemFactory {
        @Override
        public CompletionItem convert(TypeElement elem, CompletionContext ctx, int priorityHint) {
            CompilationInfo ci = ctx.getCompilationInfo();
            if (!acceptsType(elem, ci)) {
                return null;
            } else {
                String fqn = elem.getQualifiedName().toString();
                FxBean bean = ctx.getBeanInfo(fqn);
                String n = ctx.getSimpleClassName(fqn);
                if (n == null) {
                    n = fqn;
                }
                return SimpleClassItem.setup(
                        new ValueClassItem(
                            ctx, 
                            n,
                            bean != null && bean.getPropertyNames().isEmpty()
                        ), elem, ctx, priorityHint);
            }
        }
    }
    
    /* test */ static boolean acceptsType(TypeElement elem, CompilationInfo ci) {
        LOG.log(Level.FINE, "Checking class: {0}", elem.getQualifiedName());
        return FxClassUtils.findValueOf(elem, ci) != null;
    }
    
    public String toString() {
        return "value-class["  + getFullClassName() + "]";
    }
    
}