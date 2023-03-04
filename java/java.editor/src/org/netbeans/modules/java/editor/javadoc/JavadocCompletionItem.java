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

package org.netbeans.modules.java.editor.javadoc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.modules.editor.java.JavaCompletionItem;
import org.netbeans.modules.editor.java.LazyJavaCompletionItem;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 * Javadoc completion items.
 * 
 * @author Jan Pokorsky
 */
final class JavadocCompletionItem implements CompletionItem {
    
    // XXX we need new icons for javadoc completion
    private static final String JAVADOC_TAG_ICON = "org/netbeans/modules/java/editor/resources/javadoctag.gif"; // NOI18N
    private static final String JAVADOC_PARAM_ICON = "org/netbeans/modules/editor/resources/completion/localVariable.gif"; // NOI18N
    private static final String PARAMETER_COLOR = "<font color=#00007c>"; //NOI18N
    private static final String COLOR_END = "</font>"; //NOI18N
    private static final String BOLD = "<b>"; //NOI18N
    private static final String BOLD_END = "</b>"; //NOI18N
    
    private final int substitutionOffset;
    private final String txt;
    private final String leftHtmlText;
    private final String rightHtmlText;
    private final String iconPath;
    private final int sortPriority;
    private ImageIcon icon = null;

    public JavadocCompletionItem(String txt, int substitutionOffset,
            String leftHtmlText, String rightHtmlText, String iconPath,
            int sortPriority) {
        
        this.substitutionOffset = substitutionOffset;
        this.txt = txt;
        this.leftHtmlText = leftHtmlText;
        this.rightHtmlText = rightHtmlText;
        this.iconPath = iconPath;
        this.sortPriority = sortPriority;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        Completion.get().hideAll();
        complete(component, txt + ' ', substitutionOffset);
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
    }

    @Override
    public final int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }

    @Override
    public final void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        if (icon == null) {
            icon = createIcon();
        }
        CompletionUtilities.renderHtml(icon, getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }
    
    protected ImageIcon createIcon() {
        return ImageUtilities.loadImageIcon(iconPath, false);
    }
    
    protected String getLeftHtmlText() {
        return leftHtmlText == null ? txt : leftHtmlText;
    }
    
    protected String getRightHtmlText() {
        return rightHtmlText;
    }
    
    @Override
    public CompletionTask createDocumentationTask() {
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent component) {
        return false;
    }

    @Override
    public int getSortPriority() {
        return sortPriority;
    }

    @Override
    public CharSequence getSortText() {
        return txt;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return txt;
    }

    @Override
    public String toString() {
        return super.toString() + String.format("[txt:%1$s, substitutionOffset:%2$d]",
                txt, substitutionOffset);
    }
    
    private static void complete(final JTextComponent comp, final String what, final int where) {
        try {
            Document doc = comp.getDocument();
            NbDocument.runAtomicAsUser((StyledDocument) doc, new Runnable() {

                public void run() {
                    completeAsUser(comp, what, where);
                }
            });
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static void completeAsUser(JTextComponent comp, String what, int where) {
        Document doc = comp.getDocument();
        try {
            int end = comp.getSelectionEnd();
            int len = end - where;
            if (len > 0) {
                doc.remove(where, len);
            }
            doc.insertString(where, what, null);
            comp.setCaretPosition(where + what.length());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static final class Factory implements JavadocCompletionTask.ItemFactory<CompletionItem> {

        @Override
        public CompletionItem createTagItem(String name, int startOffset) {
            return new JavadocCompletionItem(name, startOffset, null, null, JAVADOC_TAG_ICON, 500);
        }

        @Override
        public CompletionItem createNameItem(String name, int startOffset) {
            // escape type variable name
            String html = name.charAt(0) == '<'
                    ? "&lt;" + name.substring(1, name.length() - 1) + "&gt;" // NOI18N
                    : name;
            return new JavadocCompletionItem(name, startOffset,
                    PARAMETER_COLOR + BOLD + html + BOLD_END + COLOR_END,
                    null, JAVADOC_PARAM_ICON, 100);
        }

        @Override
        public CompletionItem createJavadocExecutableItem(CompilationInfo info, ExecutableElement e, ExecutableType et, int startOffset, boolean isInherited, boolean isDeprecated) {
            CompletionItem delegate = JavaCompletionItem.createExecutableItem(
                    info, e, et, null, startOffset, null, isInherited, isDeprecated, false, false, false, -1, false, null);
            return new JavadocExecutableItem(delegate, e, startOffset);
        }

        @Override
        public CompletionItem createJavadocTypeItem(CompilationInfo info, TypeElement elem, int startOffset, boolean isDeprecated) {
            CompletionItem delegate = JavaCompletionItem.createTypeItem(
                    info, elem, (DeclaredType) elem.asType(), startOffset, null, isDeprecated, false, false, false, false, false, null);
            return new JavadocTypeItem(delegate, startOffset);
        }

        @Override
        public CompletionItem createJavaTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int startOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean smartType) {
            return JavaCompletionItem.createTypeItem(info, elem, type, startOffset, referencesCount, isDeprecated, false, false, false, smartType, false, null);
        }

        @Override
        public CompletionItem createLazyTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int startOffset, ReferencesCount referencesCount, Source source) {
            return LazyJavaCompletionItem.createTypeItem(handle, kinds, startOffset, referencesCount, source, false, false, false, null);
        }

        @Override
        public CompletionItem createJavaVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int startOffset, boolean isInherited, boolean isDeprecated) {
            return JavaCompletionItem.createVariableItem(info, elem, type, null, startOffset, null, isInherited, isDeprecated, false, -1, null);
        }

        @Override
        public CompletionItem createPackageItem(String pkgFQN, int startOffset) {
            return JavaCompletionItem.createPackageItem(pkgFQN, startOffset, false);
        }
    }

    /**
     * Reuses UI of {@code JavaCompletionItem.createExecutableItem} and
     * implements own text substitution.
     */
    private static class JavadocExecutableItem implements CompletionItem {
        
        private final CompletionItem delegate;
        private final String[] paramTypes;
        private final CharSequence name;
        private final int substitutionOffset;

        public JavadocExecutableItem(CompletionItem jmethod,
                ExecutableElement ee, int substitutionOffset) {
            
            this.delegate = jmethod;
            this.substitutionOffset = substitutionOffset;
            
            this.name = ee.getKind() == ElementKind.METHOD
                    ? ee.getSimpleName()
                    : ee.getEnclosingElement().getSimpleName();
            
            List<? extends VariableElement> params = ee.getParameters();
            this.paramTypes = new String[params.size()];
            int i = 0;
            for (VariableElement p : params) {
                TypeMirror asType = p.asType();
                this.paramTypes[i++] = resolveTypeName(asType, ee.isVarArgs()).toString();
            }
        }
        
        /**
         * uses FQNs where possible since javadoc does not match imports for
         * parameter types
         */
        private CharSequence resolveTypeName(TypeMirror asType, boolean isVarArgs) {
            CharSequence ptype;
            if (asType.getKind() == TypeKind.DECLARED) {
                // snip generics
                Element e = ((DeclaredType) asType).asElement();
                ptype = e.getKind().isClass() || e.getKind().isInterface()
                        ? ((TypeElement) e).getQualifiedName()
                        : e.getSimpleName();
            } else if (asType.getKind() == TypeKind.TYPEVAR) {
                do {
                    // Type Erasure JLS 4.6
                    asType = ((TypeVariable) asType).getUpperBound();
                } while (asType.getKind() == TypeKind.TYPEVAR);
                ptype = resolveTypeName(asType, isVarArgs);
            } else if (isVarArgs && asType.getKind() == TypeKind.ARRAY) {
                ptype = resolveTypeName(((ArrayType)asType).getComponentType(), false) + "..."; //NOI18N
            } else {
                ptype = asType.toString();
            }
            
            return ptype;
        }

        @Override
        public void defaultAction(JTextComponent component) {
            if (component != null) {
                Completion.get().hideDocumentation();
                Completion.get().hideCompletion();
                
                StringBuilder sb = new StringBuilder();
                sb.append(this.name);
                if (this.paramTypes.length == 0) {
                    sb.append("() "); // NOI18N
                } else {
                    sb.append('(');
                    for (String pt : paramTypes) {
                        sb.append(pt).append(", "); // NOI18N
                    }
                    sb.setCharAt(sb.length() - 2, ')');
                }
                
                complete(component, sb.toString(), substitutionOffset);
            }
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
            // nothing special
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return delegate.getPreferredWidth(g, defaultFont);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            delegate.render(g, defaultFont, defaultColor, backgroundColor, width, height, selected);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return delegate.createDocumentationTask();
        }

        @Override
        public CompletionTask createToolTipTask() {
            return delegate.createToolTipTask();
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            if (component != null) {
                try {
                    int caretOffset = component.getSelectionEnd();
                    if (caretOffset > substitutionOffset) {
                        String text = component.getDocument().getText(substitutionOffset, caretOffset - substitutionOffset);
                        if (!getInsertPrefix().toString().startsWith(text)) {
                            return false;
                        }
                    }
                }
                catch (BadLocationException ble) {}
            }
            defaultAction(component);
            return true;
        }

        @Override
        public int getSortPriority() {
            return delegate.getSortPriority();
        }

        @Override
        public CharSequence getSortText() {
            return delegate.getSortText();
        }

        @Override
        public CharSequence getInsertPrefix() {
            return delegate.getInsertPrefix();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }        
    }
    
    /**
     * Reuses UI of {@code JavaCompletionItem.createTypeItem} and
     * implements own text substitution.
     */
    private static class JavadocTypeItem implements CompletionItem {
        
        private final CompletionItem delegate;

        public JavadocTypeItem(CompletionItem item, int substitutionOffset) {
            this.delegate = item;
        }
        
        @Override
        public void defaultAction(JTextComponent component) {
            delegate.defaultAction(component);
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
            if (evt.getID() == KeyEvent.KEY_TYPED) {
                if (Utilities.getJavadocCompletionSelectors().indexOf(evt.getKeyChar()) >= 0) {
                    JTextComponent comp = (JTextComponent) evt.getSource();
                    delegate.defaultAction(comp);
                    if (Utilities.getJavadocCompletionAutoPopupTriggers().indexOf(evt.getKeyChar()) >= 0) {
                        Completion.get().showCompletion();
                    }
                    evt.consume();
                }
            }
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return delegate.getPreferredWidth(g, defaultFont);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor,
                Color backgroundColor, int width, int height, boolean selected) {
            delegate.render(g, defaultFont, defaultColor, backgroundColor, width, height, selected);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return delegate.createDocumentationTask();
        }

        @Override
        public CompletionTask createToolTipTask() {
            return delegate.createToolTipTask();
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return delegate.instantSubstitution(component);
        }

        @Override
        public int getSortPriority() {
            return delegate.getSortPriority();
        }

        @Override
        public CharSequence getSortText() {
            return delegate.getSortText();
        }

        @Override
        public CharSequence getInsertPrefix() {
            return delegate.getInsertPrefix();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}
