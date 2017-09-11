/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.editor.javadoc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
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
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.modules.editor.java.JavaCompletionItem;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.javadoc.TagRegistery.TagEntry;
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
    
    private int substitutionOffset;
    private String txt;
    private String leftHtmlText;
    private String rightHtmlText;
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

    public void defaultAction(JTextComponent component) {
        Completion.get().hideAll();
        complete(component, txt + ' ', substitutionOffset);
    }

    public void processKeyEvent(KeyEvent evt) {
    }

    public final int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(
                getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }

    public final void render(Graphics g, Font defaultFont, Color defaultColor,
            Color backgroundColor, int width, int height, boolean selected) {
        if (icon == null) {
            icon = createIcon();
        }
        CompletionUtilities.renderHtml(icon, getLeftHtmlText(), getRightHtmlText(),
                g, defaultFont, defaultColor, width, height, selected);
    }
    
    protected ImageIcon createIcon() {
        return ImageUtilities.loadImageIcon(iconPath, false);
    }
    
    protected String getLeftHtmlText() {
        if (leftHtmlText == null) {
            leftHtmlText = txt;
        }
        return leftHtmlText;
    }
    
    protected String getRightHtmlText() {
        return rightHtmlText;
    }
    
    public CompletionTask createDocumentationTask() {
        return null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public boolean instantSubstitution(JTextComponent component) {
        return false;
    }

    public int getSortPriority() {
        return sortPriority;
    }

    public CharSequence getSortText() {
        return txt;
    }

    public CharSequence getInsertPrefix() {
        return txt;
    }

    @Override
    public String toString() {
        return super.toString() + String.format("[txt:%1$s, substitutionOffset:%2$d]",
                txt, substitutionOffset);
    }

    public static List<CompletionItem> addBlockTagItems(ElementKind kind,
            String prefix, int startOffset) {
        
        List<TagEntry> tags = TagRegistery.getDefault().getTags(kind, false);
        List<CompletionItem> items = new ArrayList<CompletionItem>(tags.size());
        for (TagEntry tagEntry : tags) {
            if (tagEntry.name.startsWith(prefix)) {
                items.add(new JavadocCompletionItem(tagEntry.name, startOffset,
                        null, null, JAVADOC_TAG_ICON, 500));
            }
        }
        return items;
    }

    public static List<CompletionItem> addInlineTagItems(ElementKind kind,
            String prefix, int startOffset) {
        
        List<TagEntry> tags = TagRegistery.getDefault().getTags(kind, true);
        List<CompletionItem> items = new ArrayList<CompletionItem>(tags.size());
        for (TagEntry tagEntry : tags) {
            if (tagEntry.name.startsWith(prefix)) {
                items.add(new JavadocCompletionItem(tagEntry.name, startOffset,
                        null, null, JAVADOC_TAG_ICON, 500));
            }
        }
        return items;
    }
    
    public static CompletionItem createNameItem(String name, int startOffset) {
        // escape type variable name
        String html = name.charAt(0) == '<'
                ? "&lt;" + name.substring(1, name.length() - 1) + "&gt;" // NOI18N
                : name;
        return new JavadocCompletionItem(name, startOffset,
                PARAMETER_COLOR + BOLD + html + BOLD_END + COLOR_END,
                null, JAVADOC_PARAM_ICON, 100);
    }
    
    public static CompletionItem createExecutableItem(CompilationInfo info, ExecutableElement e,
            ExecutableType et, int startOffset, boolean isInherited,
            boolean isDeprecated) {
        
        CompletionItem delegate = JavaCompletionItem.createExecutableItem(
                info, e, et, null, startOffset, null, isInherited, isDeprecated, false, false, false, -1, false, null);
        return new JavadocExecutableItem(delegate, e, startOffset);
    }
    
    public static CompletionItem createTypeItem(CompilationInfo info, TypeElement elem,
            int startOffset, ReferencesCount referencesCount, boolean isDeprecated) {
        
        CompletionItem delegate = JavaCompletionItem.createTypeItem(
                info, elem, (DeclaredType) elem.asType(), startOffset,
                referencesCount, isDeprecated, false, false, false, false, false, null);
        return new JavadocTypeItem(delegate, startOffset);
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

        public void processKeyEvent(KeyEvent evt) {
            // nothing special
        }

        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return delegate.getPreferredWidth(g, defaultFont);
        }

        public void render(Graphics g, Font defaultFont, Color defaultColor,
                Color backgroundColor, int width, int height, boolean selected) {
            
            delegate.render(g, defaultFont, defaultColor, backgroundColor,
                    width, height, selected);
        }

        public CompletionTask createDocumentationTask() {
            return delegate.createDocumentationTask();
        }

        public CompletionTask createToolTipTask() {
            return delegate.createToolTipTask();
        }

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

        public int getSortPriority() {
            return delegate.getSortPriority();
        }

        public CharSequence getSortText() {
            return delegate.getSortText();
        }

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
        
        public void defaultAction(JTextComponent component) {
            delegate.defaultAction(component);
        }

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

        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return delegate.getPreferredWidth(g, defaultFont);
        }

        public void render(Graphics g, Font defaultFont, Color defaultColor,
                Color backgroundColor, int width, int height, boolean selected) {
            
            delegate.render(g, defaultFont, defaultColor, backgroundColor,
                    width, height, selected);
        }

        public CompletionTask createDocumentationTask() {
            return delegate.createDocumentationTask();
        }

        public CompletionTask createToolTipTask() {
            return delegate.createToolTipTask();
        }

        public boolean instantSubstitution(JTextComponent component) {
            return delegate.instantSubstitution(component);
        }

        public int getSortPriority() {
            return delegate.getSortPriority();
        }

        public CharSequence getSortText() {
            return delegate.getSortText();
        }

        public CharSequence getInsertPrefix() {
            return delegate.getInsertPrefix();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}
