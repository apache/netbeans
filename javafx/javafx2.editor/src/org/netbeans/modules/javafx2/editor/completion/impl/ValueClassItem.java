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