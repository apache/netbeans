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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import org.netbeans.api.annotations.common.StaticResource;

import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.CompositeCompletionItem;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;

import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.netbeans.swing.plaf.LFCustoms;

import static javax.lang.model.type.TypeKind.VOID;
import javax.swing.Icon;

/**
 *
 * @author Dusan Balek
 */
public abstract class JavaCompletionItem implements CompletionItem {

    protected static int SMART_TYPE = 1000;
    protected static int DEPRECATED = 10;
    private static final String GENERATE_TEXT = NbBundle.getMessage(JavaCompletionItem.class, "generate_Lbl");
    private static final Logger LOGGER = Logger.getLogger(JavaCompletionItem.class.getName());

    public static JavaCompletionItem createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType) {
        return new KeywordItem(kwd, 0, postfix, substitutionOffset, smartType);
    }

    public static JavaCompletionItem createModuleItem(String moduleName, int substitutionOffset) {
        return new ModuleItem(moduleName, substitutionOffset);
    }

    public static JavaCompletionItem createPackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement) {
        return new PackageItem(pkgFQN, substitutionOffset, inPackageStatement);
    }

    public static JavaCompletionItem createTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType, WhiteListQuery.WhiteList whiteList) {
        switch (elem.getKind()) {
            case CLASS:
                return new ClassItem(info, elem, type, 0, substitutionOffset, referencesCount, isDeprecated, insideNew, addTypeVars, addSimpleName, smartType, autoImportEnclosingType, whiteList);
            case INTERFACE:
                return new InterfaceItem(info, elem, type, 0, substitutionOffset, referencesCount, isDeprecated, insideNew, addTypeVars, addSimpleName, smartType, autoImportEnclosingType, whiteList);
            case ENUM:
                return new EnumItem(info, elem, type, 0, substitutionOffset, referencesCount, isDeprecated, insideNew, addSimpleName, smartType, autoImportEnclosingType, whiteList);
            case ANNOTATION_TYPE:
                return new AnnotationTypeItem(info, elem, type, 0, substitutionOffset, referencesCount, isDeprecated, insideNew, addSimpleName, smartType, autoImportEnclosingType, whiteList);
            case RECORD:
                return new RecordItem(info, elem, type, 0, substitutionOffset, referencesCount, isDeprecated, insideNew, addSimpleName, smartType, autoImportEnclosingType, whiteList);
            default:
                throw new IllegalArgumentException("kind=" + elem.getKind());
        }
    }

    public static JavaCompletionItem createRecordPatternItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars) {
        if(elem.getKind() == ElementKind.RECORD) {
            return new RecordPatternItem(info, elem, type, 0, substitutionOffset, referencesCount, isDeprecated, insideNew);
        }
        else {
            throw new IllegalArgumentException("kind=" + elem.getKind());
        }
    }

    public static JavaCompletionItem createArrayItem(CompilationInfo info, ArrayType type, int substitutionOffset, ReferencesCount referencesCount, Elements elements, WhiteListQuery.WhiteList whiteList) {
        int dim = 0;
        TypeMirror tm = type;
        while(tm.getKind() == TypeKind.ARRAY) {
            tm = ((ArrayType)tm).getComponentType();
            dim++;
        }
        if (tm.getKind().isPrimitive()) {
            return new KeywordItem(tm.toString(), dim, null, substitutionOffset, true);
        }
        if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ERROR) {
            DeclaredType dt = (DeclaredType)tm;
            TypeElement elem = (TypeElement)dt.asElement();
            switch (elem.getKind()) {
                case CLASS:
                    return new ClassItem(info, elem, dt, dim, substitutionOffset, referencesCount, elements.isDeprecated(elem), false, false, false, true, false, whiteList);
                case INTERFACE:
                    return new InterfaceItem(info, elem, dt, dim, substitutionOffset, referencesCount, elements.isDeprecated(elem), false, false, false, true, false, whiteList);
                case ENUM:
                    return new EnumItem(info, elem, dt, dim, substitutionOffset, referencesCount, elements.isDeprecated(elem), false, false, true, false, whiteList);
                case ANNOTATION_TYPE:
                    return new AnnotationTypeItem(info, elem, dt, dim, substitutionOffset, referencesCount, elements.isDeprecated(elem), false, false, true, false, whiteList);
            }
        }
        throw new IllegalArgumentException("array element kind=" + tm.getKind());
    }

    public static JavaCompletionItem createTypeParameterItem(TypeParameterElement elem, int substitutionOffset) {
        return new TypeParameterItem(elem, substitutionOffset);
    }

    public static JavaCompletionItem createVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, TypeMirror castType, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset, WhiteListQuery.WhiteList whiteList) {
        switch (elem.getKind()) {
            case LOCAL_VARIABLE:
            case RESOURCE_VARIABLE:
            case BINDING_VARIABLE:
            case PARAMETER:
            case EXCEPTION_PARAMETER:
                return new VariableItem(info, type, elem.getSimpleName().toString(), substitutionOffset, false, smartType, assignToVarOffset);
            case ENUM_CONSTANT:
            case FIELD:
                return new FieldItem(info, elem, type, castType, substitutionOffset, referencesCount, isInherited, isDeprecated, smartType, assignToVarOffset, whiteList);
            default:
                throw new IllegalArgumentException("kind=" + elem.getKind());
        }
    }

    public static JavaCompletionItem createVariableItem(CompilationInfo info, String varName, int substitutionOffset, boolean newVarName, boolean smartType) {
        return new VariableItem(info, null, varName, substitutionOffset, newVarName, smartType, -1);
    }

    public static JavaCompletionItem createExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, TypeMirror castType, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean afterConstructorTypeParams, boolean smartType, int assignToVarOffset, boolean memberRef, WhiteListQuery.WhiteList whiteList) {
        switch (elem.getKind()) {
            case METHOD:
                return new MethodItem(info, elem, type, castType, substitutionOffset, referencesCount, isInherited, isDeprecated, inImport, addSemicolon, smartType, assignToVarOffset, memberRef, whiteList);
            case CONSTRUCTOR:
                return new ConstructorItem(info, elem, type, substitutionOffset, isDeprecated, afterConstructorTypeParams, smartType, null, whiteList);
            default:
                throw new IllegalArgumentException("kind=" + elem.getKind());
        }
    }

    public static JavaCompletionItem createThisOrSuperConstructorItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, String name, WhiteListQuery.WhiteList whiteList) {
        if (elem.getKind() == ElementKind.CONSTRUCTOR) {
            return new ConstructorItem(info, elem, type, substitutionOffset, isDeprecated, false, false, name, whiteList);
        }
        throw new IllegalArgumentException("kind=" + elem.getKind());
    }

    public static JavaCompletionItem createOverrideMethodItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement, WhiteListQuery.WhiteList whiteList) {
        switch (elem.getKind()) {
            case METHOD:
                return new OverrideMethodItem(info, elem, type, substitutionOffset, implement, whiteList);
            default:
                throw new IllegalArgumentException("kind=" + elem.getKind());
        }
    }

    public static JavaCompletionItem createGetterSetterMethodItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, String name, boolean setter) {
        switch (elem.getKind()) {
            case ENUM_CONSTANT:
            case FIELD:
                return new GetterSetterMethodItem(info, elem, type, substitutionOffset, name, setter);
            default:
                throw new IllegalArgumentException("kind=" + elem.getKind());
        }
    }

    public static JavaCompletionItem createDefaultConstructorItem(TypeElement elem, int substitutionOffset, boolean smartType) {
        return new DefaultConstructorItem(elem, substitutionOffset, smartType);
    }

    public static JavaCompletionItem createParametersItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamIndex, String name) {
        return new ParametersItem(info, elem, type, substitutionOffset, isDeprecated, activeParamIndex, name);
    }

    public static JavaCompletionItem createAnnotationItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, WhiteListQuery.WhiteList whiteList) {
        return new AnnotationItem(info, elem, type, substitutionOffset, referencesCount, isDeprecated, true, whiteList);
    }

    public static JavaCompletionItem createAttributeItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated) {
        return new AttributeItem(info, elem, type, substitutionOffset, isDeprecated);
    }

    public static JavaCompletionItem createAttributeValueItem(CompilationInfo info, String value, String documentation, TypeElement element, int substitutionOffset, ReferencesCount referencesCount, WhiteListQuery.WhiteList whiteList) {
        return new AttributeValueItem(info, value, documentation, element, substitutionOffset, referencesCount, whiteList);
    }

    public static JavaCompletionItem createStaticMemberItem(CompilationInfo info, DeclaredType type, Element memberElem, TypeMirror memberType, boolean multipleVersions, int substitutionOffset, boolean isDeprecated, boolean addSemicolon, boolean smartType, WhiteListQuery.WhiteList whiteList) {
        switch (memberElem.getKind()) {
            case METHOD:
            case ENUM_CONSTANT:
            case FIELD:
                return new StaticMemberItem(info, type, memberElem, memberType, multipleVersions, substitutionOffset, isDeprecated, addSemicolon, smartType, whiteList);
            default:
                throw new IllegalArgumentException("kind=" + memberElem.getKind());
        }
    }

    public static JavaCompletionItem createChainedMembersItem(CompilationInfo info, List<? extends Element> chainedElems, List<? extends TypeMirror> chainedTypes, int substitutionOffset, boolean isDeprecated, boolean addSemicolon, WhiteListQuery.WhiteList whiteList) {
        return new ChainedMembersItem(info, chainedElems, chainedTypes, substitutionOffset, isDeprecated, addSemicolon, whiteList);
    }

    public static JavaCompletionItem createInitializeAllConstructorItem(CompilationInfo info, boolean isDefault, Iterable<? extends VariableElement> fields, ExecutableElement superConstructor, TypeElement parent, int substitutionOffset) {
        return new InitializeAllConstructorItem(info, isDefault, fields, superConstructor, parent, substitutionOffset);
    }

    public static JavaCompletionItem createLambdaItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, boolean expression, boolean addSemicolon) {
        return new LambdaCompletionItem(info, elem, type, substitutionOffset, expression, addSemicolon);
    }

    private static CompletionItem createExcludeItem(CharSequence name) {
        if (name == null) {
            ExcludeFromCompletionItem item = ExcludeFromCompletionItem.CONFIGURE_ITEM != null ? ExcludeFromCompletionItem.CONFIGURE_ITEM.get() : null;
            if (item == null) {
                ExcludeFromCompletionItem.CONFIGURE_ITEM = new WeakReference<ExcludeFromCompletionItem>(item = new ExcludeFromCompletionItem(name));
            }
            return item;
        }
        return new ExcludeFromCompletionItem(name);
    }

    public static final String COLOR_END = "</font>"; //NOI18N
    public static final String STRIKE = "<s>"; //NOI18N
    public static final String STRIKE_END = "</s>"; //NOI18N
    public static final String BOLD = "<b>"; //NOI18N
    public static final String BOLD_END = "</b>"; //NOI18N

    protected int substitutionOffset;
    protected boolean showTooltip;

    protected JavaCompletionItem(int substitutionOffset) {
        this.substitutionOffset = substitutionOffset;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            process(component, '\0', false);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            if ((!Utilities.autoPopupOnJavaIdentifierPart() || !(this instanceof VariableItem) || !((VariableItem)this).newVarName)
                    && Utilities.getJavaCompletionSelectors().indexOf(evt.getKeyChar()) >= 0
                    && (' ' != evt.getKeyChar() || (evt.getModifiers() & InputEvent.CTRL_MASK) == 0)) {
                if (evt.getKeyChar() == '(' && !(this instanceof AnnotationItem)
                        && !(this instanceof ConstructorItem)
                        && !(this instanceof DefaultConstructorItem)
                        && !(this instanceof MethodItem)
                        && !(this instanceof GetterSetterMethodItem)
                        && !(this instanceof InitializeAllConstructorItem)
                        && !(this instanceof OverrideMethodItem)
                        && !(this instanceof StaticMemberItem)
                        && !(this instanceof ChainedMembersItem)) {
                    return;
                }
                Completion.get().hideDocumentation();
                Completion.get().hideCompletion();
                process((JTextComponent)evt.getSource(), evt.getKeyChar(), false);
                if (Utilities.getJavaCompletionAutoPopupTriggers().indexOf(evt.getKeyChar()) >= 0) {
                    Completion.get().showCompletion();
                }
                evt.consume();
            }
        } else if (evt.getID() == KeyEvent.KEY_PRESSED && evt.getKeyCode() == KeyEvent.VK_ENTER && (evt.getModifiers() & InputEvent.CTRL_MASK) > 0) {
            JTextComponent component = (JTextComponent)evt.getSource();
            final int caretOffset = component.getSelectionEnd();
            final Document doc = component.getDocument();
            Runnable r = new Runnable() {
                public void run() {
                    TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), caretOffset);
                    if (ts != null && (ts.moveNext() || ts.movePrevious())) {
                        if (ts.token().id() == JavaTokenId.IDENTIFIER
                                || ts.token().id().primaryCategory().startsWith("keyword") //NOI18N
                                || ts.token().id().primaryCategory().startsWith("string")) { //NOI18N
                            try {
                                doc.remove(caretOffset, ts.offset() + ts.token().length() - caretOffset);
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            };
            AtomicLockDocument ald = LineDocumentUtils.as(doc, AtomicLockDocument.class);
            if (ald != null) {
                ald.runAtomic(r);
            } else {
                r.run();
            }
        } else if (evt.getID() == KeyEvent.KEY_PRESSED && evt.getKeyCode() == KeyEvent.VK_ENTER && (evt.getModifiers() & InputEvent.SHIFT_MASK) > 0) {
            JTextComponent component = (JTextComponent)evt.getSource();
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            process(component, '\0', true);
            evt.consume();
        }
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
    public CompletionTask createDocumentationTask() {
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    protected ImageIcon getIcon() {
        return null;
    }

    protected String getLeftHtmlText() {
        return null;
    }

    protected String getRightHtmlText() {
        return null;
    }

    protected CharSequence getInsertPostfix(JTextComponent c) {
        return null;
    }

    protected CharSequence getCastText() {
        return null;
    }

    protected int getCastEndOffset() {
        return -1;
    }

    protected int getAssignToVarOffset() {
        return -1;
    }

    protected CharSequence getAssignToVarText() {
        return null;
    }

    protected final void process(final JTextComponent c, char selector, boolean assignToVar) {
        final BaseDocument doc = (BaseDocument) c.getDocument();
        StringBuilder toAdd = new StringBuilder();
        CharSequence postfix = getInsertPostfix(c);
        if (postfix != null) {
            toAdd.append(postfix);
        }
        if (selector != '\0' && (toAdd.length() == 0 || (selector != toAdd.charAt(0) && selector != toAdd.charAt(toAdd.length() - 1) && ';' != toAdd.charAt(toAdd.length() - 1)))) {
            toAdd.append(selector);
            if ('[' == selector && TypingCompletion.isCompletionSettingEnabled()) {
                toAdd.append(']');
            }
        }
        int caretOffset = c.getSelectionEnd();
        Position startPos = null;
        if (getAssignToVarOffset() >= 0) {
            try {
                startPos = doc.createPosition(getAssignToVarOffset(), Position.Bias.Backward);
            } catch (BadLocationException e) {
            }
        }
        Position assignToVarEndPos = null;
        if (assignToVar) {
            try {
                assignToVarEndPos = doc.createPosition(caretOffset);
                if (toAdd.length() == 0 || ';' != toAdd.charAt(toAdd.length() - 1)) {
                    toAdd.append(';');
                }
            } catch (BadLocationException e) {
            }
        }
        Position castEndPos = null;
        if (getCastEndOffset() >= 0) {
            try {
                castEndPos = doc.createPosition(getCastEndOffset());
            } catch (BadLocationException e) {
            }
        }
        Position semiPos = null;
        if (toAdd.length() > 0 && ';' == toAdd.charAt(toAdd.length() - 1)) {
            int pos = findPositionForSemicolon(c);
            if (pos > -2) {
                toAdd.deleteCharAt(toAdd.length() - 1);
                if (pos > -1) {
                    try {
                        semiPos = doc.createPosition(pos);
                    } catch (BadLocationException e) {
                    }
                }
            }
        }
        CharSequence docText = DocumentUtilities.getText(doc);
        int i = 0;
        int j = caretOffset;
        int length = j - substitutionOffset;
        if (toAdd.length() > 0) {
            boolean partialMatch = false;
            int taNL = -1;
            int docNL = -1;
            while (true) {
                char taChar = '\0';
                while (i < toAdd.length() && (taChar = toAdd.charAt(i++)) <= ' ') {
                    if (taChar == '\n' && taNL < 0) {
                        taNL = i - 1;
                    }
                }
                char docChar = '\0';
                while (j < docText.length() && (docChar = docText.charAt(j++)) <= ' ') {
                    if (docChar == '\n') {
                        if (taNL < 0) {
                            break;
                        } else if (docNL < 0) {
                            docNL = j - 1;
                        }
                    }
                }
                if (taChar <= ' ' || docChar == '\n') {
                    length = j - substitutionOffset - (j <= docText.length() ? 1 : 0);
                    break;
                } else if (taChar != docChar) {
                    if (partialMatch) {
                        if (docNL < 0) {
                            toAdd.delete(i - 1, toAdd.length());
                            length = j - substitutionOffset - (j <= docText.length() ? 1 : 0);
                        } else {
                            toAdd.delete(taNL, toAdd.length());
                            length = docNL - substitutionOffset;
                        }
                    }
                    break;
                } else {
                    partialMatch = true;
                }
            }
        }
        CharSequence template = substituteText(c, substitutionOffset, length, getInsertPrefix(), toAdd);
        if (semiPos != null) {
            final Position finalSemiPos = semiPos;
            doc.runAtomic (new Runnable() {
                @Override
                public void run() {
                    try {
                        int cp = c.getCaretPosition();
                        doc.insertString(finalSemiPos.getOffset(), ";", null);
                        c.setCaretPosition(cp);
                    } catch (BadLocationException e) {
                    }
                }
            });
        }
        if (startPos != null && castEndPos != null) {
            final Position finalStartPos = startPos;
            final Position finalEndPos = castEndPos;
            doc.runAtomic(new Runnable() {
                @Override
                public void run() {
                    try {
                        doc.insertString(finalStartPos.getOffset(), "(" + getCastText(), null); //NOI18N
                        doc.insertString(finalEndPos.getOffset(), ")", null); //NOI18N
                    } catch (BadLocationException e) {
                    }
                }
            });
            
        }
        final StringBuilder sb = new StringBuilder();
        if (startPos != null && assignToVarEndPos != null) {
            sb.append(getAssignToVarText());
            final Position finalStartPos = startPos;
            final Position finalEndPos = assignToVarEndPos;
            doc.runAtomic(new Runnable() {
                @Override
                public void run() {
                    try {
                        sb.append(doc.getText(finalStartPos.getOffset(), finalEndPos.getOffset() - finalStartPos.getOffset()));
                        doc.remove(finalStartPos.getOffset(), finalEndPos.getOffset() - finalStartPos.getOffset());
                    } catch (BadLocationException e) {
                    }
                }
            });
            c.setCaretPosition(startPos.getOffset());
        }
        if (template != null) {
            sb.append(template);
        }
        if (sb.length() > 0) {
            CodeTemplateManager.get(doc).createTemporary(sb.toString()).insert(c);
        }
        if (showTooltip) {
            Completion.get().showToolTip();
        }
    }

    protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
        final StringBuilder sb = new StringBuilder();
        if (text != null) {
            sb.append(text);
        }
        if (toAdd != null) {
            sb.append(toAdd);
        }
        final BaseDocument doc = (BaseDocument) c.getDocument();
        doc.runAtomic (new Runnable() {
            @Override
            public void run() {
                try {
                    String textToReplace = doc.getText(offset, length);
                    if (textToReplace.contentEquals(sb)) {
                        c.setCaretPosition(offset + length);
                    } else {
                        Position pos = doc.createPosition(offset);
                        doc.remove(offset, length);
                        doc.insertString(pos.getOffset(), sb.toString(), null);
                    }
                } catch (BadLocationException e) {
                }
            }
        });
        return null;
    }

    abstract static class WhiteListJavaCompletionItem<T extends Element> extends JavaCompletionItem {

        private static final String WARNING = "org/netbeans/modules/java/editor/resources/warning_badge.gif";   //NOI18N
        private static Icon warningIcon;
        private final WhiteListQuery.WhiteList whiteList;
        private final List<ElementHandle<? extends Element>> handles;
        private Boolean isBlackListed;

        protected WhiteListJavaCompletionItem(
                final int substitutionOffset,
                final ElementHandle<? extends Element> handle,
                final WhiteListQuery.WhiteList whiteList) {
            super(substitutionOffset);
            this.handles = (List<ElementHandle<? extends Element>>) Collections.singletonList(handle);
            this.whiteList = whiteList;
        }

        protected WhiteListJavaCompletionItem(
                final int substitutionOffset,
                final List<? extends Element> elements,
                final WhiteListQuery.WhiteList whiteList) {
            super(substitutionOffset);
            this.handles = new ArrayList<>(elements.size());
            for (Element e : elements) {
                this.handles.add(e.getKind().isField() || e.getKind() == ElementKind.METHOD ? ElementHandle.create(e) : null);
            }
            this.whiteList = whiteList;
        }

        protected final WhiteListQuery.WhiteList getWhiteList() {
            return this.whiteList;
        }

        protected final ElementHandle<T> getElementHandle() {
            return (ElementHandle<T>) (this.handles.isEmpty() ? null : this.handles.get(this.handles.size() - 1));
        }

        protected final List<ElementHandle<? extends Element>> getElementHandles() {
            return this.handles;
        }

        protected final boolean isBlackListed() {
            if (isBlackListed == null) {
                isBlackListed = whiteList == null ? false : !checkIsAllowed();
            }
            return isBlackListed;
        }
        
        private boolean checkIsAllowed() {
            for (ElementHandle<? extends Element> handle : handles) {
                if (handle != null && !whiteList.check(handle, WhiteListQuery.Operation.USAGE).isAllowed())
                    return false;                
            }
            return true;
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return isBlackListed() ? false : super.instantSubstitution(component);
        }

        @Override
        public final ImageIcon getIcon() {
            final ImageIcon base = getBaseIcon();
            if (base == null || !isBlackListed()) {
                return base;
            }
            if (warningIcon == null) {
                warningIcon = ImageUtilities.loadIcon(WARNING);
            }
            assert warningIcon != null;
            return ImageUtilities.icon2ImageIcon(ImageUtilities.mergeIcons(base, warningIcon, 8, 8));
        }

        protected ImageIcon getBaseIcon() {
            return super.getIcon();
        }
    }

    static class KeywordItem extends JavaCompletionItem {

        private static final String JAVA_KEYWORD = "org/netbeans/modules/java/editor/resources/javakw_16.png"; //NOI18N
        private static final String KEYWORD_COLOR = Utilities.getHTMLColor(64, 64, 217);
        private static ImageIcon icon;

        private String kwd;
        private int dim;
        private String postfix;
        private boolean smartType;
        private String leftText;

        private KeywordItem(String kwd, int dim, String postfix, int substitutionOffset, boolean smartType) {
            super(substitutionOffset);
            this.kwd = kwd;
            this.dim = dim;
            this.postfix = postfix;
            this.smartType = smartType;
        }

        @Override
        public int getSortPriority() {
            return smartType ? 670 - SMART_TYPE : 670;
        }

        @Override
        public CharSequence getSortText() {
            return kwd;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return kwd;
        }

        @Override
        protected ImageIcon getIcon(){
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(JAVA_KEYWORD, false);
            }
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(KEYWORD_COLOR);
                sb.append(BOLD);
                sb.append(kwd);
                for(int i = 0; i < dim; i++) {
                    sb.append("[]"); //NOI18N
                }
                sb.append(BOLD_END);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected CharSequence getInsertPostfix(JTextComponent c) {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < dim; i++) {
                sb.append("[]"); //NOI18N
            }
            if (postfix != null) {
                sb.append(postfix);
            }
            return sb.length() > 0 ? sb.toString() : null;
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            if (toAdd != null) {
                CharSequence cs = getInsertPostfix(c);
                if (cs != null) {
                    int postfixLen = cs.length();
                    int toAddLen = toAdd.length();
                    if (toAddLen >= postfixLen) {
                        StringBuilder template = new StringBuilder();
                        int cnt = 1;
                        for(int i = 0; i < dim; i++) {
                            template.append("[${PAR#"); //NOI18N
                            template.append(cnt++);
                            template.append(" instanceof=\"int\" default=\"\"}]"); //NOI18N
                        }
                        if (template.length() > 0) {
                            super.substituteText(c, offset, length, text, null);
                            if (toAddLen > postfixLen) {
                                template.append(toAdd.subSequence(postfixLen, toAddLen));
                            }
                            return template;
                        }
                    }
                }
            }
            return super.substituteText(c, offset, length, text, toAdd);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(kwd);
            for(int i = 0; i < dim; i++) {
                sb.append("[]"); //NOI18N
            }
            return sb.toString();
        }
    }

    static class ModuleItem extends JavaCompletionItem {

        @StaticResource
        private static final String MODULE = "org/netbeans/modules/java/editor/resources/module.png"; // NOI18N
        private static final String MODULE_COLOR = Utilities.getHTMLColor(64, 150, 64);
        private static ImageIcon icon;

        private String name;
        private String leftText;

        private ModuleItem(String moduleName, int substitutionOffset) {
            super(substitutionOffset);
            this.name = moduleName;
        }

        @Override
        public int getSortPriority() {
            return 950;
        }

        @Override
        public CharSequence getSortText() {
            return name;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return name;
        }

        @Override
        protected ImageIcon getIcon(){
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(MODULE, false);
            }
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(MODULE_COLOR);
                sb.append(name);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static class PackageItem extends JavaCompletionItem {

        private static final String PACKAGE = "org/netbeans/modules/java/editor/resources/package.gif"; // NOI18N
        private static final String PACKAGE_COLOR = Utilities.getHTMLColor(64, 150, 64);
        private static ImageIcon icon;

        private boolean inPackageStatement;
        private String simpleName;
        private String sortText;
        private String leftText;

        private PackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement) {
            super(substitutionOffset);
            this.inPackageStatement = inPackageStatement;
            int idx = pkgFQN.lastIndexOf('.');
            this.simpleName = idx < 0 ? pkgFQN : pkgFQN.substring(idx + 1);
            this.sortText = this.simpleName + "#" + pkgFQN; //NOI18N
        }

        @Override
        public int getSortPriority() {
            return 900;
        }

        @Override
        public CharSequence getSortText() {
            return sortText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        @Override
        protected ImageIcon getIcon(){
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(PACKAGE, false);
            }
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(PACKAGE_COLOR);
                sb.append(simpleName);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        public void defaultAction(JTextComponent component) {
            if (component != null) {
                Completion.get().hideDocumentation();
                if (inPackageStatement || Utilities.getJavaCompletionAutoPopupTriggers().indexOf('.') < 0) {
                    Completion.get().hideCompletion();
                }
                process(component, '\0', false);
            }
        }

        @Override
        protected String getInsertPostfix(JTextComponent c) {
            return inPackageStatement ? null : "."; //NOI18N
        }

        @Override
        public String toString() {
            return simpleName;
        }
    }

    static class ClassItem extends WhiteListJavaCompletionItem<TypeElement> implements CompositeCompletionItem {

        private static final String CLASS = "org/netbeans/modules/editor/resources/completion/class_16.png"; //NOI18N
        private static final String CLASS_COLOR = Utilities.getHTMLColor(150, 64, 64);
        private static final String PKG_COLOR = Utilities.getHTMLColor(192, 192, 192);
        private static ImageIcon icon;

        protected TypeMirrorHandle<DeclaredType> typeHandle;
        private int dim;
        private boolean hasTypeArgs;
        private boolean isDeprecated;
        private boolean insideNew;
        private boolean addTypeVars;
        private boolean addSimpleName;
        private boolean smartType;
        private String simpleName;
        private String typeName;
        private String enclName;
        private CharSequence sortText;
        private String leftText;
        private boolean autoImportEnclosingType;
        private List<CompletionItem> subItems = new ArrayList<>();

        private ClassItem(CompilationInfo info, TypeElement elem, DeclaredType type, int dim, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType, WhiteListQuery.WhiteList whiteList) {
            super(substitutionOffset, ElementHandle.create(elem), whiteList);
            this.typeHandle = TypeMirrorHandle.create(type);
            this.dim = dim;
            this.hasTypeArgs = addTypeVars && SourceVersion.RELEASE_5.compareTo(info.getSourceVersion()) <= 0 && !type.getTypeArguments().isEmpty();
            this.isDeprecated = isDeprecated;
            this.insideNew = insideNew;
            this.addTypeVars = addTypeVars;
            this.addSimpleName = addSimpleName;
            this.smartType = smartType;
            this.simpleName = elem.getSimpleName().toString();
            this.typeName = Utilities.getTypeName(info, type, false).toString();
            if (referencesCount != null) {
                this.enclName = info.getElementUtilities().getElementName(elem.getEnclosingElement(), true).toString();
                this.sortText = new LazySortText(this.simpleName, this.enclName, getElementHandle(), referencesCount);
            } else {
                this.enclName = null;
                this.sortText = this.simpleName;
            }
            this.autoImportEnclosingType = autoImportEnclosingType;
            this.subItems.add(createExcludeItem(elem.getQualifiedName()));
            this.subItems.add(createExcludeItem(info.getElements().getPackageOf(elem).getQualifiedName() + ".*")); //NOI18N
            this.subItems.add(createExcludeItem(null));
        }

        @Override
        public int getSortPriority() {
            int p = 800;
            if (smartType)
                p -= SMART_TYPE;
            if (isDeprecated)
                p += DEPRECATED;
            return p;
        }

        @Override
        public CharSequence getSortText() {
            return sortText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        @Override
        public List<CompletionItem> getSubItems() {
            return subItems;
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return typeHandle.getKind() == TypeKind.DECLARED ? JavaCompletionProvider.createDocTask(ElementHandle.from(typeHandle)) : null;
        }

        @Override
        protected ImageIcon getBaseIcon() {
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(CLASS, false);
            }
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(getColor());
                if (isDeprecated || isBlackListed()) {
                    sb.append(STRIKE);
                }
                sb.append(escape(typeName));
                for(int i = 0; i < dim; i++) {
                    sb.append("[]"); //NOI18N
                }
                if (isDeprecated || isBlackListed()) {
                    sb.append(STRIKE_END);
                }
                if (enclName != null && enclName.length() > 0) {
                    sb.append(COLOR_END);
                    sb.append(PKG_COLOR);
                    sb.append(" ("); //NOI18N
                    sb.append(enclName);
                    sb.append(")"); //NOI18N
                }
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        protected String getColor() {
            return CLASS_COLOR;
        }

        @Override
        protected String getInsertPostfix(JTextComponent c) {
            StringBuilder sb = new StringBuilder();
            if (hasTypeArgs) {
                sb.append("<>"); //NOI18N
            }
            for(int i = 0; i < dim; i++) {
                sb.append("[]"); //NOI18N
            }
            return sb.length() > 0 ? sb.toString() : null;
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            final StringBuilder template = new StringBuilder();
            final AtomicBoolean cancel = new AtomicBoolean();
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final BaseDocument doc = (BaseDocument)c.getDocument();
                        ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                if (cancel.get()) {
                                    return;
                                }
                                CompilationController controller = CompilationController.get(resultIterator.getParserResult(offset));
                                controller.toPhase(Phase.RESOLVED);
                                if (cancel.get()) {
                                    return;
                                }
                                DeclaredType type = typeHandle.resolve(controller);
                                TypeElement elem = type != null ? (TypeElement)type.asElement() : null;
                                if (elem == null) {
                                    ClassItem.super.substituteText(c, offset, length, simpleName, toAdd);
                                    return;
                                }
                                CharSequence tail = null;
                                boolean partialMatch = false;
                                int cnt = 1;
                                if (toAdd != null) {
                                    CharSequence cs = getInsertPostfix(c);
                                    int postfixLen = cs != null ? cs.length() : 0;
                                    int toAddLen = toAdd.length();
                                    if (toAddLen >= postfixLen) {
                                        Iterator<? extends TypeMirror> tas = type != null ? type.getTypeArguments().iterator() : null;
                                        StringBuilder sb = new StringBuilder();
                                        boolean asTemplate = false;
                                        if (tas != null && tas.hasNext()) {
                                            sb.append('<'); //NOI18N
                                            if (!insideNew || elem.getModifiers().contains(Modifier.ABSTRACT)
                                                || controller.getSourceVersion().compareTo(SourceVersion.RELEASE_7) < 0
                                                || !allowDiamond(controller, offset, type)) {
                                                while (tas.hasNext()) {
                                                    TypeMirror ta = tas.next();
                                                    sb.append("${PAR#"); //NOI18N
                                                    sb.append(cnt++);
                                                    if (ta.getKind() == TypeKind.TYPEVAR) {
                                                        TypeVariable tv = (TypeVariable)ta;
                                                        if (smartType || elem != tv.asElement().getEnclosingElement()) {
                                                            sb.append(" editable=false default=\""); //NOI18N
                                                            sb.append(Utilities.getTypeName(controller, ta, true));
                                                            asTemplate = true;
                                                        } else {
                                                            sb.append(" typeVar=\""); //NOI18N
                                                            sb.append(tv.asElement().getSimpleName());
                                                            sb.append("\" type=\""); //NOI18N
                                                            ta = tv.getUpperBound();
                                                            sb.append(Utilities.getTypeName(controller, ta, true));
                                                            sb.append("\" default=\""); //NOI18N
                                                            sb.append(Utilities.getTypeName(controller, ta, false));
                                                            if (addTypeVars && SourceVersion.RELEASE_5.compareTo(controller.getSourceVersion()) <= 0) {
                                                                asTemplate = true;
                                                            }
                                                        }
                                                        sb.append("\"}"); //NOI18N
                                                    } else if (ta.getKind() == TypeKind.WILDCARD) {
                                                        sb.append(" type=\""); //NOI18N
                                                        TypeMirror bound = ((WildcardType)ta).getExtendsBound();
                                                        if (bound == null) {
                                                            bound = ((WildcardType)ta).getSuperBound();
                                                        }
                                                        sb.append(bound != null ? Utilities.getTypeName(controller, bound, true) : "Object"); //NOI18N
                                                        sb.append("\" default=\""); //NOI18N
                                                        sb.append(bound != null ? Utilities.getTypeName(controller, bound, false) : "Object"); //NOI18N
                                                        sb.append("\"}"); //NOI18N
                                                        asTemplate = true;
                                                    } else if (ta.getKind() == TypeKind.ERROR) {
                                                        sb.append(" default=\""); //NOI18N
                                                        sb.append(((ErrorType)ta).asElement().getSimpleName());
                                                        sb.append("\"}"); //NOI18N
                                                        asTemplate = true;
                                                    } else {
                                                        sb.append(" type=\""); //NOI18N
                                                        sb.append(Utilities.getTypeName(controller, ta, true));
                                                        sb.append("\" default=\""); //NOI18N
                                                        sb.append(Utilities.getTypeName(controller, ta, false));
                                                        sb.append("\" editable=false}"); //NOI18N
                                                        asTemplate = true;
                                                    }
                                                    if (tas.hasNext()) {
                                                        sb.append(", "); //NOI18N
                                                    }
                                                }
                                            } else {
                                                asTemplate = true;
                                            }
                                            sb.append('>'); //NOI18N
                                        }
                                        if (asTemplate) {
                                            template.append(sb);
                                        } else {
                                            for(int i = 0; i < dim; i++) {
                                                template.append("[${PAR#"); //NOI18N
                                                template.append(cnt++);
                                                template.append(" instanceof=\"int\" default=\"\"}]"); //NOI18N
                                            }
                                        }
                                        if (toAddLen > postfixLen) {
                                            tail = toAdd.subSequence(postfixLen, toAddLen);
                                        }
                                    } else {
                                        partialMatch = true;
                                    }
                                }
                                int o = offset;
                                if (template.length() == 0 && (addSimpleName || enclName == null)) {
                                    ClassItem.super.substituteText(c, offset, length, elem.getSimpleName(), toAdd);
                                    if (insideNew && (toAdd == null || toAdd.length() == 0)) {
                                        Completion.get().showCompletion();
                                    }
                                } else {
                                    Document d = c.getDocument();
                                    Position p = d.createPosition(offset);
                                    StringBuilder sb = new StringBuilder();
                                    if (addSimpleName || enclName == null) {
                                        sb.append(elem.getSimpleName());
                                    } else if (controller.getTreeUtilities().isModuleInfo(controller.getCompilationUnit())) {
                                        sb.append(elem.getQualifiedName());
                                    } else if (!"text/x-java".equals(controller.getSnapshot().getMimePath().getPath())) { //NOI18N
                                        TreePath tp = controller.getTreeUtilities().pathFor(controller.getSnapshot().getEmbeddedOffset(offset));
                                        sb.append(AutoImport.resolveImport(controller, tp, controller.getTypes().getDeclaredType(elem)));
                                    } else {
                                        TreePath tp = controller.getTreeUtilities().pathFor(controller.getSnapshot().getEmbeddedOffset(offset));
                                        if (tp != null && tp.getLeaf().getKind() == Tree.Kind.IMPORT) {
                                            ClassItem.super.substituteText(c, offset, length, elem.getQualifiedName(), toAdd);
                                            return;
                                        }
                                        sb.append("${PAR#0"); //NOI18N
                                        if ((type == null || type.getKind() != TypeKind.ERROR) &&
                                                EnumSet.range(ElementKind.PACKAGE, ElementKind.INTERFACE).contains(elem.getEnclosingElement().getKind())) {
                                            sb.append(" type=\""); //NOI18N
                                            sb.append(elem.getQualifiedName());
                                            sb.append("\" default=\""); //NOI18N
                                            sb.append(elem.getSimpleName());
                                        } else {
                                            sb.append(" default=\""); //NOI18N
                                            sb.append(elem.getQualifiedName());
                                        }
                                        sb.append("\" editable=false}"); //NOI18N
                                    }
                                    template.insert(0, sb);
                                    if (insideNew && dim == 0 && !partialMatch) {
                                        template.append("${cursor completionInvoke}"); //NOI18N
                                    }
                                    if (tail != null) {
                                        template.append(tail);
                                    }
                                    if (partialMatch) {
                                        template.append(toAdd);
                                    }
                                    if (p != null) {
                                        o = p.getOffset();
                                    }
                                    ClassItem.super.substituteText(c, o, length, null, null);
                                }
                                if (autoImportEnclosingType && elem != null) {
                                    TreePath tp = controller.getTreeUtilities().pathFor(controller.getSnapshot().getEmbeddedOffset(o));
                                    AutoImport.resolveImport(controller, tp, elem.getEnclosingElement().asType());
                                }
                            }
                        });
                    } catch (ParseException pe) {
                    }
                }
            }, NbBundle.getMessage(JavaCompletionItem.class, "JCI-import_resolve"), cancel, false); //NOI18N
            return template;
        }

        private boolean allowDiamond(CompilationInfo info, int offset, DeclaredType type) {
            TreeUtilities tu = info.getTreeUtilities();
            TreePath path = tu.pathFor(offset);
            while (path != null && !(path.getLeaf() instanceof StatementTree)) {
                path = path.getParentPath();
            }
            if (path != null) {
                Trees trees = info.getTrees();
                int pos = (int)trees.getSourcePositions().getStartPosition(path.getCompilationUnit(), path.getLeaf().getKind() == Tree.Kind.VARIABLE ? ((VariableTree)path.getLeaf()).getType() : path.getLeaf());
                if (pos >= 0) {
                    Scope scope = tu.scopeFor(pos);
                    String stmt = info.getText().substring(pos, offset);
                    StringBuilder sb = new StringBuilder();
                    sb.append('{').append(stmt).append(Utilities.getTypeName(info, type, true)).append("();}"); //NOI18N;
                    SourcePositions[] sp = new SourcePositions[1];
                    StatementTree st = tu.parseStatement(sb.toString(), sp);
                    tu.attributeTree(st, scope);
                    TreePath tp = tu.pathFor(new TreePath(path, st), offset - pos, sp[0]);
                    TypeMirror tm = tp != null ? trees.getTypeMirror(tp) : null;
                    sb = new StringBuilder();
                    sb.append('{').append(stmt).append(((TypeElement)type.asElement()).getQualifiedName()).append("<>();}"); //NOI18N
                    st = tu.parseStatement(sb.toString(), sp);
                    tu.attributeTree(st, scope);
                    tp = tu.pathFor(new TreePath(path, st), offset - pos, sp[0]);
                    TypeMirror tmd = tp != null ? trees.getTypeMirror(tp) : null;
                    return tm != null && tmd != null && info.getTypes().isSameType(tm, tmd);
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return simpleName;
        }
    }

    static class InterfaceItem extends ClassItem {

        private static final String INTERFACE = "org/netbeans/modules/editor/resources/completion/interface.png"; // NOI18N
        private static final String INTERFACE_COLOR = Utilities.getHTMLColor(128, 128, 128);
        private static ImageIcon icon;

        private InterfaceItem(CompilationInfo info, TypeElement elem, DeclaredType type, int dim, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImport, WhiteListQuery.WhiteList whiteList) {
            super(info, elem, type, dim, substitutionOffset, referencesCount, isDeprecated, insideNew, addTypeVars, addSimpleName, smartType, autoImport, whiteList);
        }

        @Override
        protected ImageIcon getBaseIcon() {
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(INTERFACE, false);
            }
            return icon;
        }

        @Override
        protected String getColor() {
            return INTERFACE_COLOR;
        }
    }

    static class EnumItem extends ClassItem {

        private static final String ENUM = "org/netbeans/modules/editor/resources/completion/enum.png"; // NOI18N
        private static ImageIcon icon;

        private EnumItem(CompilationInfo info, TypeElement elem, DeclaredType type, int dim, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addSimpleName, boolean smartType, boolean autoImport, WhiteListQuery.WhiteList whiteList) {
            super(info, elem, type, dim, substitutionOffset, referencesCount, isDeprecated, insideNew, false, addSimpleName, smartType, autoImport, whiteList);
        }

        @Override
        protected ImageIcon getBaseIcon() {
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(ENUM, false);
            }
            return icon;
        }
    }
    
    static class RecordItem extends ClassItem {

        private static final String RECORD = "org/netbeans/modules/editor/resources/completion/record.png"; // NOI18N
        private static ImageIcon icon;

        private RecordItem(CompilationInfo info, TypeElement elem, DeclaredType type, int dim, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addSimpleName, boolean smartType, boolean autoImport, WhiteListQuery.WhiteList whiteList) {
            super(info, elem, type, dim, substitutionOffset, referencesCount, isDeprecated, insideNew, false, addSimpleName, smartType, autoImport, whiteList);
        }

        @Override
        protected ImageIcon getBaseIcon() {
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(RECORD, false);
            }
            return icon;
        }
    }

    static class RecordPatternItem extends ClassItem {

        private String simpleName;
        private String recordParams;

        private RecordPatternItem(CompilationInfo info, TypeElement elem, DeclaredType type, int dim, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew) {
            super(info, elem, type, dim, substitutionOffset, referencesCount, isDeprecated, insideNew, false, false, false, false, null);
            simpleName = elem.getSimpleName().toString();
            Iterator<? extends RecordComponentElement> it = elem.getRecordComponents().iterator();
            StringBuilder sb = new StringBuilder();
            RecordComponentElement recordComponent;
            sb.append("(");
            while (it.hasNext()) {
                recordComponent = it.next();
                sb.append(Utilities.getTypeName(info, recordComponent.getAccessor().getReturnType(), false));
                sb.append(" ");
                sb.append(recordComponent.getSimpleName());
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(")");
            recordParams = sb.toString();
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            return recordParams;
        }

        @Override
        protected String getLeftHtmlText() {
            return simpleName + recordParams;
        }

        @Override
        public int getSortPriority() {
            return 650;
        }
    }

    static class AnnotationTypeItem extends ClassItem {

        private static final String ANNOTATION = "org/netbeans/modules/editor/resources/completion/annotation_type.png"; // NOI18N
        private static ImageIcon icon;

        private AnnotationTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int dim, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addSimpleName, boolean smartType, boolean autoImport, WhiteListQuery.WhiteList whiteList) {
            super(info, elem, type, dim, substitutionOffset, referencesCount, isDeprecated, insideNew, false, addSimpleName, smartType, autoImport, whiteList);
        }

        @Override
        protected ImageIcon getBaseIcon() {
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(ANNOTATION, false);
            }
            return icon;
        }
    }

    static class TypeParameterItem extends JavaCompletionItem {

        private String simpleName;
        private String leftText;

        private TypeParameterItem(TypeParameterElement elem, int substitutionOffset) {
            super(substitutionOffset);
            this.simpleName = elem.getSimpleName().toString();
        }

        @Override
        public int getSortPriority() {
            return 700;
        }

        @Override
        public CharSequence getSortText() {
            return simpleName;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                leftText = LFCustoms.getTextFgColorHTML() + simpleName + COLOR_END;
            }
            return leftText;
        }

        @Override
        public String toString() {
            return simpleName;
        }
    }

    static class VariableItem extends JavaCompletionItem {

        private static final String LOCAL_VARIABLE = "org/netbeans/modules/editor/resources/completion/localVariable.gif"; //NOI18N
        private static final String PARAMETER_COLOR = Utilities.getHTMLColor(64, 64, 188);
        private static ImageIcon icon;

        private String varName;
        private boolean newVarName;
        private boolean smartType;
        private String typeName;
        private String leftText;
        private String rightText;
        private int assignToVarOffset;
        private CharSequence assignToVarText;

        private VariableItem(CompilationInfo info, TypeMirror type, String varName, int substitutionOffset, boolean newVarName, boolean smartType, int assignToVarOffset) {
            super(substitutionOffset);
            this.varName = varName;
            this.newVarName = newVarName;
            this.smartType = smartType;
            this.typeName = type != null ? Utilities.getTypeName(info, type, false).toString() : null;
            this.assignToVarOffset = assignToVarOffset;
            this.assignToVarText = assignToVarOffset < 0 ? null : createAssignToVarText(info, type, varName);
        }

        @Override
        public int getSortPriority() {
            return smartType ? 200 - SMART_TYPE : 200;
        }

        @Override
        public CharSequence getSortText() {
            return varName;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return varName;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                leftText = PARAMETER_COLOR + BOLD + varName + BOLD_END + COLOR_END;
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            if (rightText == null) {
                rightText = escape(typeName);
            }
            return rightText;
        }

        @Override
        protected ImageIcon getIcon(){
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(LOCAL_VARIABLE, false);
            }
            return icon;
        }

        @Override
        public int getAssignToVarOffset() {
            return assignToVarOffset;
        }

        @Override
        public CharSequence getAssignToVarText() {
            return assignToVarText;
        }

        @Override
        public String toString() {
            return (typeName != null ? typeName + " " : "") + varName; //NOI18N
        }
    }

    static class FieldItem extends WhiteListJavaCompletionItem<VariableElement> {

        private static final String FIELD_PUBLIC = "org/netbeans/modules/editor/resources/completion/field_16.png"; //NOI18N
        private static final String FIELD_PROTECTED = "org/netbeans/modules/editor/resources/completion/field_protected_16.png"; //NOI18N
        private static final String FIELD_PACKAGE = "org/netbeans/modules/editor/resources/completion/field_package_private_16.png"; //NOI18N
        private static final String FIELD_PRIVATE = "org/netbeans/modules/editor/resources/completion/field_private_16.png"; //NOI18N
        private static final String FIELD_ST_PUBLIC = "org/netbeans/modules/editor/resources/completion/field_static_16.png"; //NOI18N
        private static final String FIELD_ST_PROTECTED = "org/netbeans/modules/editor/resources/completion/field_static_protected_16.png"; //NOI18N
        private static final String FIELD_ST_PACKAGE = "org/netbeans/modules/editor/resources/completion/field_static_package_private_16.png"; //NOI18N
        private static final String FIELD_ST_PRIVATE = "org/netbeans/modules/editor/resources/completion/field_static_private_16.png"; //NOI18N
        private static final String FIELD_COLOR = Utilities.getHTMLColor(64, 198, 88);
        private static ImageIcon icon[][] = new ImageIcon[2][4];

        private boolean isInherited;
        private boolean isDeprecated;
        private boolean smartType;
        private String simpleName;
        private Set<Modifier> modifiers;
        private String typeName;
        private String leftText;
        private String rightText;
        private CharSequence sortText;
        private boolean autoImportEnclosingType;
        private CharSequence enclSortText;
        private int castEndOffset;
        private CharSequence castText;
        private int startOffset;
        private CharSequence assignToVarText;

        private FieldItem(CompilationInfo info, VariableElement elem, TypeMirror type, TypeMirror castType, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset, WhiteListQuery.WhiteList whiteList) {
            super(substitutionOffset, ElementHandle.create(elem), whiteList);
            this.isInherited = isInherited;
            this.isDeprecated = isDeprecated;
            this.smartType = smartType;
            this.simpleName = elem.getSimpleName().toString();
            this.modifiers = elem.getModifiers();
            this.typeName = Utilities.getTypeName(info, type, false).toString();
            this.autoImportEnclosingType = referencesCount != null;
            if (this.autoImportEnclosingType) {
                this.enclSortText = new LazySortText(elem.getEnclosingElement().getSimpleName().toString(), null, ElementHandle.create((TypeElement)elem.getEnclosingElement()), referencesCount);
            } else {
                this.enclSortText = ""; //NOI18N
            }
            this.startOffset = assignToVarOffset;
            this.assignToVarText = assignToVarOffset < 0 ? null : createAssignToVarText(info, type, this.simpleName);
            if (castType != null) {
                try {
                    TreePath tp = info.getTreeUtilities().pathFor(substitutionOffset);
                    if (this.startOffset < 0) {
                        if (tp != null && tp.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                            this.startOffset = (int)info.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tp.getLeaf());
                        }
                    }
                    this.castText = "(" + AutoImport.resolveImport(info, tp, castType) + (CodeStyle.getDefault(info.getDocument()).spaceAfterTypeCast() ? ") " : ")"); //NOI18N
                    this.castEndOffset = findCastEndPosition(info.getTokenHierarchy().tokenSequence(JavaTokenId.language()), startOffset, substitutionOffset);
                } catch (IOException ex) {
                }
            } else {
                this.castEndOffset = -1;
            }
        }

        @Override
        public int getSortPriority() {
            int p = 300;
            if (smartType)
                p -= SMART_TYPE;
            if (isDeprecated)
                p += DEPRECATED;
            return p;
        }

        @Override
        public CharSequence getSortText() {
            if (sortText == null) {
                sortText = LazySortText.link(simpleName, enclSortText);
            }
            return sortText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(getElementHandle());
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(FIELD_COLOR);
                if (!isInherited) {
                    sb.append(BOLD);
                }
                if (isDeprecated || isBlackListed()) {
                    sb.append(STRIKE);
                }
                sb.append(simpleName);
                if (isDeprecated || isBlackListed()) {
                    sb.append(STRIKE_END);
                }
                if (!isInherited) {
                    sb.append(BOLD_END);
                }
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            if (rightText == null) {
                rightText = escape(typeName);
            }
            return rightText;
        }

        @Override
        protected ImageIcon getBaseIcon(){
            int level = getProtectionLevel(modifiers);
            boolean isStatic = modifiers.contains(Modifier.STATIC);
            ImageIcon cachedIcon = icon[isStatic?1:0][level];
            if (cachedIcon != null) {
                return cachedIcon;
            }
            String iconPath = FIELD_PUBLIC;
            if (isStatic) {
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = FIELD_ST_PRIVATE;
                        break;
                    case PACKAGE_LEVEL:
                        iconPath = FIELD_ST_PACKAGE;
                        break;
                    case PROTECTED_LEVEL:
                        iconPath = FIELD_ST_PROTECTED;
                        break;
                    case PUBLIC_LEVEL:
                        iconPath = FIELD_ST_PUBLIC;
                        break;
                }
            } else {
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = FIELD_PRIVATE;
                        break;
                    case PACKAGE_LEVEL:
                        iconPath = FIELD_PACKAGE;
                        break;
                    case PROTECTED_LEVEL:
                        iconPath = FIELD_PROTECTED;
                        break;
                    case PUBLIC_LEVEL:
                        iconPath = FIELD_PUBLIC;
                        break;
                }
            }
            ImageIcon newIcon = ImageUtilities.loadImageIcon(iconPath, false);
            icon[isStatic?1:0][level] = newIcon;
            return newIcon;
        }

        @Override
        public CharSequence getCastText() {
            return castText;
        }

        @Override
        protected int getCastEndOffset() {
            return castEndOffset;
        }

        @Override
        protected int getAssignToVarOffset() {
            return startOffset;
        }

        @Override
        public CharSequence getAssignToVarText() {
            return assignToVarText;
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            final AtomicBoolean findPrefix = new AtomicBoolean();
            Runnable r = new Runnable() {
                public void run() {
                    TokenSequence<JavaTokenId> t = findLastNonWhitespaceToken(SourceUtils.getJavaTokenSequence(TokenHierarchy.get(c.getDocument()), offset), 0, offset);
                    findPrefix.set(t == null || t.token().id() != JavaTokenId.DOT);
                }
            };
            AtomicLockDocument ald = LineDocumentUtils.as(c.getDocument(), AtomicLockDocument.class);
            if (ald != null) {
                ald.runAtomic(r);
            } else {
                r.run();
            }
            final String[] prefix = {""}; //NOI18N
            if (findPrefix.get()) {
                final AtomicBoolean cancel = new AtomicBoolean();
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ParserManager.parse(Collections.singletonList(Source.create(c.getDocument())), new UserTask() {
                                @Override
                                public void run(ResultIterator resultIterator) throws Exception {
                                    if (cancel.get()) {
                                        return;
                                    }
                                    final CompilationController controller = CompilationController.get(resultIterator.getParserResult(offset));
                                    controller.toPhase(Phase.RESOLVED);
                                    if (cancel.get()) {
                                        return;
                                    }
                                    Scope scope = controller.getTreeUtilities().scopeFor(offset);
                                    for (Element localElement : scope.getLocalElements()) {
                                        if (!localElement.getKind().isField() && localElement.getSimpleName().contentEquals(text)) {
                                            prefix[0] = modifiers.contains(Modifier.STATIC) ? scope.getEnclosingClass().getSimpleName() + "." : "this."; //NOI18N
                                        }
                                    }
                                }
                            });
                        } catch (ParseException pe) {
                        }
                    }
                }, NbBundle.getMessage(JavaCompletionItem.class, "JCI-find_prefix_if_necessary"), cancel, false); //NOI18N
            }
            CharSequence cs = super.substituteText(c, offset, length, prefix[0] + text, toAdd);
            if (autoImportEnclosingType) {
                final AtomicBoolean cancel = new AtomicBoolean();
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ParserManager.parse(Collections.singletonList(Source.create(c.getDocument())), new UserTask() {
                                @Override
                                public void run(ResultIterator resultIterator) throws Exception {
                                    if (cancel.get()) {
                                        return;
                                    }
                                    final CompilationController controller = CompilationController.get(resultIterator.getParserResult(offset));
                                    controller.toPhase(Phase.RESOLVED);
                                    if (cancel.get()) {
                                        return;
                                    }
                                    VariableElement ve = getElementHandle().resolve(controller);
                                    if (ve != null) {
                                        TreePath tp = controller.getTreeUtilities().pathFor(controller.getSnapshot().getEmbeddedOffset(offset));
                                        TypeMirror toImport = ve.getEnclosingElement().asType();
                                        if (isInherited) {
                                            toImport = typeToImport(controller, tp, toImport);
                                        }
                                        AutoImport.resolveImport(controller, tp, toImport);
                                    }
                                }
                            });
                        } catch (ParseException pe) {
                        }
                    }
                }, NbBundle.getMessage(JavaCompletionItem.class, "JCI-import_resolve"), cancel, false); //NOI18N
            }
            return cs;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(Modifier mod : modifiers) {
               sb.append(mod.toString());
               sb.append(' ');
            }
            sb.append(typeName);
            sb.append(' ');
            sb.append(simpleName);
            return sb.toString();
        }
    }

    static class MethodItem extends WhiteListJavaCompletionItem<ExecutableElement> {

        private static final String METHOD_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_16.png"; //NOI18N
        private static final String METHOD_PROTECTED = "org/netbeans/modules/editor/resources/completion/method_protected_16.png"; //NOI18N
        private static final String METHOD_PACKAGE = "org/netbeans/modules/editor/resources/completion/method_package_private_16.png"; //NOI18N
        private static final String METHOD_PRIVATE = "org/netbeans/modules/editor/resources/completion/method_private_16.png"; //NOI18N
        private static final String METHOD_ST_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_static_16.png"; //NOI18N
        private static final String METHOD_ST_PROTECTED = "org/netbeans/modules/editor/resources/completion/method_static_protected_16.png"; //NOI18N
        private static final String METHOD_ST_PRIVATE = "org/netbeans/modules/editor/resources/completion/method_static_private_16.png"; //NOI18N
        private static final String METHOD_ST_PACKAGE = "org/netbeans/modules/editor/resources/completion/method_static_package_private_16.png"; //NOI18N
        private static final String PARAMETER_NAME_COLOR = Utilities.getHTMLColor(224, 160, 65);
        private static ImageIcon icon[][] = new ImageIcon[2][4];

        private boolean isInherited;
        private boolean isDeprecated;
        private boolean inImport;
        private boolean smartType;
        private boolean memberRef;
        private String simpleName;
        protected Set<Modifier> modifiers;
        protected List<ParamDesc> params;
        private String typeName;
        private boolean addSemicolon;
        private CharSequence sortText;
        private String leftText;
        private String rightText;
        private boolean autoImportEnclosingType;
        private CharSequence enclSortText;
        private int castEndOffset;
        private CharSequence castText;
        private int startOffset;
        private CharSequence assignToVarText;

        private MethodItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, TypeMirror castType, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean smartType, int assignToVarOffset, boolean memberRef, WhiteListQuery.WhiteList whiteList) {
            super(substitutionOffset, ElementHandle.create(elem), whiteList);
            Color c = LFCustoms.getTextFgColor();
            this.isInherited = isInherited;
            this.isDeprecated = isDeprecated;
            this.inImport = inImport;
            this.smartType = smartType;
            this.memberRef = memberRef;
            this.simpleName = elem.getSimpleName().toString();
            this.modifiers = elem.getModifiers();
            this.params = new ArrayList<ParamDesc>();
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null) {
                    break;
                }
                this.params.add(new ParamDesc(tm.toString(), Utilities.getTypeName(info, tm, false, elem.isVarArgs() && !tIt.hasNext()).toString(), it.next().getSimpleName().toString()));
            }
            TypeMirror retType = type.getReturnType();
            this.typeName = Utilities.getTypeName(info, retType, false).toString();
            this.addSemicolon = addSemicolon && retType.getKind() == TypeKind.VOID;
            this.autoImportEnclosingType = referencesCount != null;
            if (this.autoImportEnclosingType) {
                this.enclSortText = new LazySortText(elem.getEnclosingElement().getSimpleName().toString(), null, ElementHandle.create((TypeElement)elem.getEnclosingElement()), referencesCount);
            } else {
                this.enclSortText = ""; //NOI18N
            }
            this.startOffset = type.getReturnType().getKind() == TypeKind.VOID ? -1 : assignToVarOffset;
            this.assignToVarText = this.startOffset < 0 ? null : createAssignToVarText(info, type.getReturnType(), this.simpleName);
            if (castType != null) {
                try {
                    TreePath tp = info.getTreeUtilities().pathFor(substitutionOffset);
                    if (this.startOffset < 0) {
                        if (tp != null && tp.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                            this.startOffset = (int)info.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tp.getLeaf());
                        }
                    }
                    this.castText = "(" + AutoImport.resolveImport(info, tp, castType) + (CodeStyle.getDefault(info.getDocument()).spaceAfterTypeCast() ? ") " : ")"); //NOI18N
                    this.castEndOffset = findCastEndPosition(info.getTokenHierarchy().tokenSequence(JavaTokenId.language()), startOffset, substitutionOffset);
                } catch (IOException ex) {
                }
            } else {
                this.castEndOffset = -1;
            }
        }

        @Override
        public int getSortPriority() {
            int p = 500;
            if (smartType)
                p -= SMART_TYPE;
            if (isDeprecated)
                p += DEPRECATED;
            return p;
        }

        @Override
        public CharSequence getSortText() {
            if (sortText == null) {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('(');
                int cnt = 0;
                for(Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc param = it.next();
                    sortParams.append(param.typeName);
                    if (it.hasNext()) {
                        sortParams.append(',');
                    }
                    cnt++;
                }
                sortParams.append(')');
                sortText = LazySortText.link(simpleName, enclSortText, ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString()); //NOI18N
            }
            return sortText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(LFCustoms.getTextFgColorHTML());
                if (!isInherited) {
                    lText.append(BOLD);
                }
                if (isDeprecated || isBlackListed()) {
                    lText.append(STRIKE);
                }
                lText.append(simpleName);
                if (isDeprecated || isBlackListed()) {
                    lText.append(STRIKE_END);
                }
                if (!isInherited) {
                    lText.append(BOLD_END);
                }
                lText.append(COLOR_END);
                lText.append('(');
                for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc paramDesc = it.next();
                    lText.append(escape(paramDesc.typeName));
                    lText.append(' ');
                    lText.append(PARAMETER_NAME_COLOR);
                    lText.append(paramDesc.name);
                    lText.append(COLOR_END);
                    if (it.hasNext()) {
                        lText.append(", "); //NOI18N
                    }
                }
                lText.append(')');
                return lText.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            if (rightText == null) {
                rightText = escape(typeName);
            }
            return rightText;
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(getElementHandle());
        }

        @Override
        protected ImageIcon getBaseIcon() {
            int level = getProtectionLevel(modifiers);
            boolean isStatic = modifiers.contains(Modifier.STATIC);
            ImageIcon cachedIcon = icon[isStatic?1:0][level];
            if (cachedIcon != null) {
                return cachedIcon;
            }
            String iconPath = METHOD_PUBLIC;
            if (isStatic) {
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = METHOD_ST_PRIVATE;
                        break;
                    case PACKAGE_LEVEL:
                        iconPath = METHOD_ST_PACKAGE;
                        break;
                    case PROTECTED_LEVEL:
                        iconPath = METHOD_ST_PROTECTED;
                        break;
                    case PUBLIC_LEVEL:
                        iconPath = METHOD_ST_PUBLIC;
                        break;
                }
            } else {
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = METHOD_PRIVATE;
                        break;
                    case PACKAGE_LEVEL:
                        iconPath = METHOD_PACKAGE;
                        break;
                    case PROTECTED_LEVEL:
                        iconPath = METHOD_PROTECTED;
                        break;
                    case PUBLIC_LEVEL:
                        iconPath = METHOD_PUBLIC;
                        break;
                }
            }
            ImageIcon newIcon = ImageUtilities.loadImageIcon(iconPath, false);
            icon[isStatic?1:0][level] = newIcon;
            return newIcon;
        }

        @Override
        protected CharSequence getInsertPostfix(JTextComponent c) {
            StringBuilder sb = new StringBuilder();
            if (inImport) {
                sb.append(';');
            } else {
                if (!memberRef) {
                    sb.append(CodeStyle.getDefault(c.getDocument()).spaceBeforeMethodCallParen() ? " ()" : "()"); //NOI18N
                }
                if (addSemicolon) {
                    sb.append(';');
                }
            }
            return sb;
        }

        @Override
        public CharSequence getCastText() {
            return castText;
        }

        @Override
        protected int getCastEndOffset() {
            return castEndOffset;
        }

        @Override
        protected int getAssignToVarOffset() {
            return startOffset;
        }

        @Override
        public CharSequence getAssignToVarText() {
            return assignToVarText;
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            final BaseDocument doc = (BaseDocument)c.getDocument();
            StringBuilder sb = new StringBuilder();
            if (toAdd != null) {
                String toAddText = toAdd.toString();
                int idx = toAddText.indexOf(')');
                if (idx > 0) {
                    if (!params.isEmpty() || text.length() == length) {
                        sb.append(text);
                        if (CodeStyle.getDefault(doc).spaceBeforeMethodCallParen()) {
                            sb.append(' '); //NOI18N
                        }
                        sb.append('('); //NOI18N
                        if (params.isEmpty()) {
                            sb.append("${cursor}"); //NOI18N
                        } else {
                            boolean guessArgs = Utilities.guessMethodArguments();
                            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                                ParamDesc paramDesc = it.next();
                                sb.append("${"); //NOI18N
                                sb.append(paramDesc.name);
                                if (guessArgs) {
                                    sb.append(" named instanceof=\""); //NOI18N
                                    sb.append(paramDesc.fullTypeName);
                                    sb.append("\""); //NOI18N
                                }
                                sb.append('}'); //NOI18N
                                if (it.hasNext()) {
                                    sb.append(", "); //NOI18N
                                }
                            }
                        }
                        sb.append(')');//NOI18N
                        if (toAddText.length() > idx + 1) {
                            sb.append(toAddText.substring(idx + 1));
                        }
                        showTooltip = Utilities.popupPrameterTooltip();
                    }
                }
            }
            if (sb.length() == 0) {
                CharSequence st = super.substituteText(c, offset, length, text, toAdd);
                if (st != null) {
                    sb.append(st);
                }
            } else {
                super.substituteText(c, offset, length, null, null);
            }
            if (autoImportEnclosingType) {
                final AtomicBoolean cancel = new AtomicBoolean();
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                                @Override
                                public void run(ResultIterator resultIterator) throws Exception {
                                    if (cancel.get()) {
                                        return;
                                    }
                                    CompilationController controller = CompilationController.get(resultIterator.getParserResult(offset));
                                    controller.toPhase(Phase.RESOLVED);
                                    if (cancel.get()) {
                                        return;
                                    }
                                    ExecutableElement ee = getElementHandle().resolve(controller);
                                    if (ee != null) {
                                        TreePath tp = controller.getTreeUtilities().pathFor(controller.getSnapshot().getEmbeddedOffset(offset));
                                        TypeMirror toImport = ee.getEnclosingElement().asType();
                                        if (isInherited) {
                                            toImport = typeToImport(controller, tp, toImport);
                                        }
                                        AutoImport.resolveImport(controller, tp, toImport);
                                    }
                                }
                            });
                        } catch (ParseException pe) {
                        }
                    }
                }, NbBundle.getMessage(JavaCompletionItem.class, "JCI-import_resolve"), cancel, false); //NOI18N
            }
            return sb;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Modifier mod : modifiers) {
                sb.append(mod.toString());
                sb.append(' ');
            }
            sb.append(typeName);
            sb.append(' ');
            sb.append(simpleName);
            sb.append('(');
            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                ParamDesc paramDesc = it.next();
                sb.append(paramDesc.typeName);
                sb.append(' ');
                sb.append(paramDesc.name);
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(')');
            return sb.toString();
        }
    }

    static class OverrideMethodItem extends MethodItem {

        private static final String IMPL_BADGE_PATH = "org/netbeans/modules/java/editor/resources/implement_badge.png"; //NOI18N
        private static final String OVRD_BADGE_PATH = "org/netbeans/modules/java/editor/resources/override_badge.png"; //NOI18N

        private static final String OVERRIDE_TEXT = NbBundle.getMessage(JavaCompletionItem.class, "override_Lbl"); //NOI18N
        private static final String IMPLEMENT_TEXT = NbBundle.getMessage(JavaCompletionItem.class, "implement_Lbl"); //NOI18N

        private static ImageIcon implementBadge = ImageUtilities.loadImageIcon(IMPL_BADGE_PATH, false);
        private static ImageIcon overrideBadge = ImageUtilities.loadImageIcon(OVRD_BADGE_PATH, false);
        private static ImageIcon merged_icon[][] = new ImageIcon[2][4];

        private boolean implement;
        private String leftText;

        private OverrideMethodItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement, WhiteListQuery.WhiteList whiteList) {
            super(info, elem, type, null, substitutionOffset, null, false, false, false, false, false, -1, false, whiteList);
            CodeStyle cs = null;
            try {
                cs = CodeStyle.getDefault(info.getDocument());
            } catch (IOException ex) {
            }
            if (cs == null) {
                cs = CodeStyle.getDefault(info.getFileObject());
            }
            for (ParamDesc paramDesc : params) {
                String name = CodeStyleUtils.removePrefixSuffix(paramDesc.name, cs.getParameterNamePrefix(), cs.getParameterNameSuffix());
                paramDesc.name = CodeStyleUtils.addPrefixSuffix(name, cs.getParameterNamePrefix(), cs.getParameterNameSuffix());
            }
            this.implement = implement;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                leftText = super.getLeftHtmlText() + " - "; //NOI18N
                leftText += (implement ? IMPLEMENT_TEXT : OVERRIDE_TEXT);
            }
            return leftText;
        }

        @Override
        protected ImageIcon getBaseIcon() {
            int level = getProtectionLevel(modifiers);
            ImageIcon merged = merged_icon[implement? 0 : 1][level];
            if ( merged != null ) {
                return merged;
            }
            ImageIcon superIcon = super.getBaseIcon();
            merged = ImageUtilities.icon2ImageIcon(ImageUtilities.mergeIcons(
                superIcon,
                implement ? implementBadge : overrideBadge,
                16 - 8,
                16 - 8) );

            merged_icon[implement? 0 : 1][level] = merged;
            return merged;
        }


        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            final BaseDocument doc = (BaseDocument)c.getDocument();
            final Position pos;
            try {
                pos = doc.createPosition(offset);
            } catch (BadLocationException e) {
                return null; // Invalid offset -> do nothing
            }
            CharSequence cs = super.substituteText(c, offset, length, null, null);
            try {
                ModificationResult mr = ModificationResult.runModificationTask(Collections.singletonList(Source.create(doc)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                        copy.toPhase(Phase.ELEMENTS_RESOLVED);
                        final int embeddedOffset = copy.getSnapshot().getEmbeddedOffset(pos.getOffset());
                        TreePath tp = copy.getTreeUtilities().pathFor(embeddedOffset);
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                            if (Utilities.inAnonymousOrLocalClass(tp)) {
                                copy.toPhase(Phase.RESOLVED);
                            }
                            ExecutableElement ee = getElementHandle().resolve(copy);
                            if (ee == null) {
                                Element el = copy.getTrees().getElement(tp);
                                if (el != null && el.getKind().isClass() || el.getKind().isInterface()) {
                                    for (ExecutableElement e : copy.getElementUtilities().findUnimplementedMethods((TypeElement)el)) {
                                        if (getElementHandle().signatureEquals(e)) {
                                            ee = e;
                                        }
                                    }
                                }
                                if (ee == null) {
                                    return;
                                }
                            }
                            if (implement) {
                                GeneratorUtils.generateAbstractMethodImplementation(copy, tp, ee, embeddedOffset);
                            } else {
                                GeneratorUtils.generateMethodOverride(copy, tp, ee, embeddedOffset);
                            }
                        }
                    }
                });
                GeneratorUtils.guardedCommit(c, mr);
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
            return cs;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            sb.append(" - ");
            sb.append(implement ? IMPLEMENT_TEXT : OVERRIDE_TEXT);
            return sb.toString();
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }
    }

    static class GetterSetterMethodItem extends JavaCompletionItem {

        private static final String METHOD_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_16.png"; //NOI18N
        private static final String GETTER_BADGE_PATH = "org/netbeans/modules/java/editor/resources/getter_badge.png"; //NOI18N
        private static final String SETTER_BADGE_PATH = "org/netbeans/modules/java/editor/resources/setter_badge.png"; //NOI18N
        private static final String PARAMETER_NAME_COLOR = Utilities.getHTMLColor(224, 160, 65);

        private static Icon superIcon;
        private static ImageIcon[] merged_icons = new ImageIcon[2];

        protected ElementHandle<VariableElement> elementHandle;
        private boolean setter;
        private String paramName;
        private String name;
        private String typeName;
        private String sortText;
        private String leftText;
        private String rightText;

        private GetterSetterMethodItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, String name, boolean setter) {
            super(substitutionOffset);
            this.elementHandle = ElementHandle.create(elem);
            this.setter = setter;
            CodeStyle cs = null;
            try {
                cs = CodeStyle.getDefault(info.getDocument());
            } catch (IOException ex) {
            }
            if (cs == null) {
                cs = CodeStyle.getDefault(info.getFileObject());
            }
            boolean isStatic = elem.getModifiers().contains(Modifier.STATIC);
            String simpleName = CodeStyleUtils.removePrefixSuffix(elem.getSimpleName(),
                isStatic ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                isStatic ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
            this.paramName = CodeStyleUtils.addPrefixSuffix(
                    simpleName,
                    cs.getParameterNamePrefix(),
                    cs.getParameterNameSuffix());
            this.name = name;
            this.typeName = Utilities.getTypeName(info, type, false).toString();
        }

        @Override
        public int getSortPriority() {
            return 500;
        }

        @Override
        public CharSequence getSortText() {
            if (sortText == null) {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('('); //NOI18N
                if (setter) {
                    sortParams.append(typeName);
                }
                sortParams.append(')'); //NOI18N
                sortText = name + "#" + (setter ? "01" : "00") + "#" + sortParams.toString(); //NOI18N
            }
            return sortText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return name;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(LFCustoms.getTextFgColorHTML());
                lText.append(BOLD);
                lText.append(name);
                lText.append(BOLD_END);
                lText.append(COLOR_END);
                lText.append('(');
                if (setter) {
                    lText.append(escape(typeName));
                    lText.append(' ');
                    lText.append(PARAMETER_NAME_COLOR);
                    lText.append(paramName);
                    lText.append(COLOR_END);
                }
                lText.append(") - "); //NOI18N
                lText.append(GENERATE_TEXT);
                leftText = lText.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            if (rightText == null) {
                rightText = setter ? "void" : escape(typeName); //NOI18N
            }
            return rightText;
        }

        @Override
        protected ImageIcon getIcon() {
            if (merged_icons[setter ? 1 : 0] == null) {
                if (superIcon == null) {
                    superIcon = ImageUtilities.loadIcon(METHOD_PUBLIC);
                }
                if (setter) {
                    Icon setterBadge = ImageUtilities.loadIcon(SETTER_BADGE_PATH);
                    merged_icons[1] = ImageUtilities.icon2ImageIcon(ImageUtilities.mergeIcons(superIcon,
                            setterBadge, 16 - 8, 16 - 8));
                } else {
                    Icon getterBadge = ImageUtilities.loadIcon(GETTER_BADGE_PATH);
                    merged_icons[0] = ImageUtilities.icon2ImageIcon(ImageUtilities.mergeIcons(superIcon,
                            getterBadge, 16 - 8, 16 - 8));
                }
            }
            return merged_icons[setter ? 1 : 0];
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            final BaseDocument doc = (BaseDocument)c.getDocument();
            final Position pos;
            try {
                pos = doc.createPosition(offset);
            } catch (BadLocationException e) {
                return null; // Invalid offset -> do nothing
            }
            CharSequence cs = super.substituteText(c, offset, length, null, null);
            try {
                ModificationResult mr = ModificationResult.runModificationTask(Collections.singletonList(Source.create(doc)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                        copy.toPhase(Phase.ELEMENTS_RESOLVED);
                        VariableElement ve = elementHandle.resolve(copy);
                        if (ve == null) {
                            return;
                        }
                        final int embeddedOffset = copy.getSnapshot().getEmbeddedOffset(pos.getOffset());
                        TreePath tp = copy.getTreeUtilities().pathFor(embeddedOffset);
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                            if (Utilities.inAnonymousOrLocalClass(tp)) {
                                copy.toPhase(Phase.RESOLVED);
                            }
                            TypeElement te = (TypeElement)copy.getTrees().getElement(tp);
                            if (te != null) {
                                GeneratorUtilities gu = GeneratorUtilities.get(copy);
                                MethodTree method = setter ? gu.createSetter(te, ve) : gu.createGetter(te, ve);
                                ClassTree decl = GeneratorUtils.insertClassMember(copy, (ClassTree)tp.getLeaf(), method, embeddedOffset);
                                copy.rewrite(tp.getLeaf(), decl);
                            }
                        }
                    }
                });
                GeneratorUtils.guardedCommit(c, mr);
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
            return cs;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("public "); //NOI18N
            sb.append(setter ? "void" : typeName); //NOI18N
            sb.append(' ');
            sb.append(name);
            sb.append('(');
            if (setter) {
                sb.append(typeName);
                sb.append(' ');
                sb.append(paramName);
            }
            sb.append(") - "); //NOI18N
            sb.append(GENERATE_TEXT);
            return sb.toString();
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }
    }

    static class ConstructorItem extends WhiteListJavaCompletionItem<ExecutableElement> {

        private static final String CONSTRUCTOR_PUBLIC = "org/netbeans/modules/editor/resources/completion/constructor_16.png"; //NOI18N
        private static final String CONSTRUCTOR_PROTECTED = "org/netbeans/modules/editor/resources/completion/constructor_protected_16.png"; //NOI18N
        private static final String CONSTRUCTOR_PACKAGE = "org/netbeans/modules/editor/resources/completion/constructor_package_private_16.png"; //NOI18N
        private static final String CONSTRUCTOR_PRIVATE = "org/netbeans/modules/editor/resources/completion/constructor_private_16.png"; //NOI18N
        private static final String CONSTRUCTOR_COLOR = Utilities.getHTMLColor(242, 203, 64);
        private static final String PARAMETER_NAME_COLOR = Utilities.getHTMLColor(224, 160, 65);
        private static ImageIcon icon[] = new ImageIcon[4];

        private boolean isDeprecated;
        private boolean smartType;
        private String simpleName;
        private String insertPrefix;
        protected Set<Modifier> modifiers;
        private List<ParamDesc> params;
        private boolean isAbstract;
        private boolean isProtected;
        private boolean insertName;
        private String sortText;
        private String leftText;

        private ConstructorItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, boolean afterConstructorTypeParams, boolean smartType, String name, WhiteListQuery.WhiteList whiteList) {
            super(substitutionOffset, ElementHandle.create(elem), whiteList);
            this.isDeprecated = isDeprecated;
            this.smartType = smartType;
            this.simpleName = name != null ? name : elem.getEnclosingElement().getSimpleName().toString();
            this.insertPrefix = !afterConstructorTypeParams ? simpleName : "";
            this.insertName = name != null;
            this.modifiers = elem.getModifiers();
            this.params = new ArrayList<ParamDesc>();
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null) {
                    break;
                }
                this.params.add(new ParamDesc(tm.toString(), Utilities.getTypeName(info, tm, false, elem.isVarArgs() && !tIt.hasNext()).toString(), it.next().getSimpleName().toString()));
            }
            this.isAbstract = !insertName && elem.getEnclosingElement().getModifiers().contains(Modifier.ABSTRACT);
            Scope s = info.getTreeUtilities().scopeFor(substitutionOffset);
            this.isProtected = elem.getModifiers().contains(Modifier.PROTECTED) && !info.getTrees().isAccessible(s, elem, (DeclaredType)elem.getEnclosingElement().asType());
        }

        @Override
        public int getSortPriority() {
            int p = insertName ? 550 : 650;
            if (smartType)
                p -= SMART_TYPE;
            if (isDeprecated)
                p += DEPRECATED;
            return p;
        }

        @Override
        public CharSequence getSortText() {
            if (sortText == null) {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('(');
                int cnt = 0;
                for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc paramDesc = it.next();
                    sortParams.append(paramDesc.typeName);
                    if (it.hasNext()) {
                        sortParams.append(',');
                    }
                    cnt++;
                }
                sortParams.append(')');
                sortText = simpleName + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
            }
            return sortText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return insertPrefix;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(CONSTRUCTOR_COLOR);
                lText.append(BOLD);
                if (isDeprecated || isBlackListed()) {
                    lText.append(STRIKE);
                }
                lText.append(simpleName);
                if (isDeprecated || isBlackListed()) {
                    lText.append(STRIKE_END);
                }
                lText.append(BOLD_END);
                lText.append(COLOR_END);
                lText.append('(');
                for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc paramDesc = it.next();
                    lText.append(escape(paramDesc.typeName));
                    lText.append(' ');
                    lText.append(PARAMETER_NAME_COLOR);
                    lText.append(paramDesc.name);
                    lText.append(COLOR_END);
                    if (it.hasNext()) {
                        lText.append(", "); //NOI18N
                    }
                }
                lText.append(')');
                leftText = lText.toString();
            }
            return leftText;
        }


        @Override
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(getElementHandle());
        }

        @Override
        protected ImageIcon getBaseIcon() {
            int level = getProtectionLevel(modifiers);
            ImageIcon cachedIcon = icon[level];
            if (cachedIcon != null) {
                return cachedIcon;
            }
            String iconPath = CONSTRUCTOR_PUBLIC;
            switch (level) {
                case PRIVATE_LEVEL:
                    iconPath = CONSTRUCTOR_PRIVATE;
                    break;
                case PACKAGE_LEVEL:
                    iconPath = CONSTRUCTOR_PACKAGE;
                    break;
                case PROTECTED_LEVEL:
                    iconPath = CONSTRUCTOR_PROTECTED;
                    break;
                case PUBLIC_LEVEL:
                    iconPath = CONSTRUCTOR_PUBLIC;
                    break;
            }
            ImageIcon newIcon = ImageUtilities.loadImageIcon(iconPath, false);
            icon[level] = newIcon;
            return newIcon;
        }

        @Override
        protected CharSequence getInsertPostfix(JTextComponent c) {
            StringBuilder sb = new StringBuilder();
            sb.append(CodeStyle.getDefault(c.getDocument()).spaceBeforeMethodCallParen() ? " ()" : "()"); //NOI18N
            if ("this".equals(simpleName) || "super".equals(simpleName)) { //NOI18N
                sb.append(';');
            } else if (isAbstract || isProtected) {
                sb.append(getIndent(c, true, true));                        
                sb.append("{\n"); //NOI18N
                sb.append(getIndent(c));
                sb.append("}"); //NOI18N
            }
            return sb;
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            BaseDocument doc = (BaseDocument) c.getDocument();
            boolean inPlace = offset == c.getCaretPosition();
            Position startPos;
            Position endPos;
            try {
                startPos = doc.createPosition(insertName || inPlace ? offset : offset + text.length(), Bias.Backward);
                endPos = doc.createPosition(offset + length);
            } catch (BadLocationException ex) {
                return null; // Invalid offset -> do nothing
            }
            CharSequence cs = insertName
                    ? super.substituteText(c, startPos.getOffset(), length, text, toAdd)
                    : super.substituteText(c, startPos.getOffset(), inPlace ? length : length - text.length(), null, toAdd);
            StringBuilder sb = new StringBuilder();
            if (toAdd != null) {
                CharSequence postfix = getInsertPostfix(c);
                if (postfix != null) {
                    int postfixLen = postfix.length();
                    int toAddLen = toAdd.length();
                    if (toAddLen >= postfixLen) {
                        String toAddText = toAdd.toString();
                        if (isAbstract) {
                            try {
                                final int off = startPos.getOffset() + (insertName ? text.length() : 0) + toAddText.indexOf('{') + 1;
                                ModificationResult mr = ModificationResult.runModificationTask(Collections.singletonList(Source.create(c.getDocument())), new UserTask() {
                                    @Override
                                    public void run(ResultIterator resultIterator) throws Exception {
                                        WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                                        copy.toPhase(Phase.RESOLVED);
                                        final int embeddedOffset = copy.getSnapshot().getEmbeddedOffset(off);
                                        TreePath path = copy.getTreeUtilities().pathFor(embeddedOffset);
                                        while (path.getLeaf() != path.getCompilationUnit()) {
                                            Tree tree = path.getLeaf();
                                            Tree parentTree = path.getParentPath().getLeaf();
                                            if (parentTree.getKind() == Tree.Kind.NEW_CLASS && TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
                                                GeneratorUtils.generateAllAbstractMethodImplementations(copy, path);
                                                break;
                                            }
                                            path = path.getParentPath();
                                        }
                                    }
                                });
                                GeneratorUtils.guardedCommit(c, mr);
                            } catch (Exception ex) {
                                LOGGER.log(Level.FINE, null, ex);
                            }
                        }
                        if (!params.isEmpty()) {
                            boolean guessArgs = Utilities.guessMethodArguments();
                            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                                ParamDesc paramDesc = it.next();
                                sb.append("${"); //NOI18N
                                sb.append(paramDesc.name);
                                if (guessArgs) {
                                    sb.append(" named instanceof=\""); //NOI18N
                                    sb.append(paramDesc.fullTypeName);
                                    sb.append("\""); //NOI18N
                                }
                                sb.append("}"); //NOI18N
                                if (it.hasNext()) {
                                    sb.append(", "); //NOI18N
                                }
                            }
                            c.select(startPos.getOffset() + (insertName ? text.length() : 0) + toAddText.indexOf('(') + 1, endPos.getOffset());
                            sb.append(c.getSelectedText());
                        }
                    }
                }
            }
            if (sb.length() == 0) {
                return cs;
            }
            showTooltip = Utilities.popupPrameterTooltip();
            return sb;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Modifier mod : modifiers) {
                sb.append(mod.toString());
                sb.append(' ');
            }
            sb.append(simpleName);
            sb.append('('); //NOI18N
            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                ParamDesc paramDesc = it.next();
                sb.append(paramDesc.typeName);
                sb.append(' ');
                sb.append(paramDesc.name);
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(')');
            return sb.toString();
        }
    }

    static class DefaultConstructorItem extends JavaCompletionItem {

        private static final String CONSTRUCTOR = "org/netbeans/modules/java/editor/resources/new_constructor_16.png"; //NOI18N
        private static final String CONSTRUCTOR_COLOR = Utilities.getHTMLColor(242, 203, 64);
        private static ImageIcon icon;

        private boolean smartType;
        private String simpleName;
        private boolean isAbstract;
        private String sortText;
        private String leftText;

        private DefaultConstructorItem(TypeElement elem, int substitutionOffset, boolean smartType) {
            super(substitutionOffset);
            this.smartType = smartType;
            this.simpleName = elem.getSimpleName().toString();
            this.isAbstract = elem.getModifiers().contains(Modifier.ABSTRACT);
        }

        @Override
        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        @Override
        public int getSortPriority() {
            return smartType ? 650 - SMART_TYPE : 650;
        }

        @Override
        public CharSequence getSortText() {
            if (sortText == null) {
                sortText = simpleName + "#0#"; //NOI18N
            }
            return sortText;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                leftText = CONSTRUCTOR_COLOR + simpleName + "()" + COLOR_END; //NOI18N
            }
            return leftText;
        }

        @Override
        protected ImageIcon getIcon() {
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(CONSTRUCTOR, false);
            }
            return icon;
        }

        @Override
        protected CharSequence getInsertPostfix(JTextComponent c) {
            StringBuilder sb = new StringBuilder();
            sb.append(CodeStyle.getDefault(c.getDocument()).spaceBeforeMethodCallParen() ? " ()" : "()"); //NOI18N
            if ("this".equals(simpleName) || "super".equals(simpleName)) { //NOI18N
                sb.append(';');
            }
            if (isAbstract) {
                sb.append(getIndent(c, true, true));                        
                sb.append("{\n"); //NOI18N
                sb.append(getIndent(c));
                sb.append("}"); //NOI18N
            }
            return sb;
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            BaseDocument doc = (BaseDocument) c.getDocument();
            boolean inPlace = offset == c.getCaretPosition();
            Position startPos;
            try {
                startPos = doc.createPosition(offset + (inPlace ? 0 : text.length()), Bias.Backward);
            } catch (BadLocationException ex) {
                return null; // Invalid offset -> do nothing
            }
            CharSequence cs = super.substituteText(c, startPos.getOffset(), inPlace ? length : length - text.length(), null, toAdd);
            if (toAdd != null) {
                CharSequence postfix = getInsertPostfix(c);
                if (postfix != null) {
                    int postfixLen = postfix.length();
                    int toAddLen = toAdd.length();
                    if (toAddLen >= postfixLen) {
                        String toAddText = toAdd.toString();
                        if (isAbstract) {
                            try {
                                final int off = startPos.getOffset() + toAddText.indexOf('{') + 1;
                                ModificationResult mr = ModificationResult.runModificationTask(Collections.singletonList(Source.create(c.getDocument())), new UserTask() {
                                    @Override
                                    public void run(ResultIterator resultIterator) throws Exception {
                                        WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                                        copy.toPhase(Phase.RESOLVED);
                                        final int embeddedOffset = copy.getSnapshot().getEmbeddedOffset(off);
                                        TreePath path = copy.getTreeUtilities().pathFor(embeddedOffset);
                                        while (path.getLeaf() != path.getCompilationUnit()) {
                                            Tree tree = path.getLeaf();
                                            Tree parentTree = path.getParentPath().getLeaf();
                                            if (parentTree.getKind() == Tree.Kind.NEW_CLASS && TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
                                                GeneratorUtils.generateAllAbstractMethodImplementations(copy, path);
                                                break;
                                            }
                                            path = path.getParentPath();
                                        }
                                    }
                                });
                                GeneratorUtils.guardedCommit(c, mr);
                            } catch (Exception ex) {
                                LOGGER.log(Level.FINE, null, ex);
                            }
                        }
                    }
                }
            }
            return cs;
        }

        @Override
        public String toString() {
            return simpleName + "()";
        }
    }

    static class ParametersItem extends JavaCompletionItem {

        private static final String PARAMETERS_COLOR = Utilities.getHTMLColor(192, 192, 192);

        protected ElementHandle<ExecutableElement> elementHandle;
        private boolean isDeprecated;
        private int activeParamsIndex;
        private String simpleName;
        private ArrayList<ParamDesc> params;
        private String typeName;
        private String sortText;
        private String leftText;
        private String rightText;

        private ParametersItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamsIndex, String name) {
            super(substitutionOffset);
            this.elementHandle = ElementHandle.create(elem);
            this.isDeprecated = isDeprecated;
            this.activeParamsIndex = activeParamsIndex;
            this.simpleName = name != null ? name : elem.getKind() == ElementKind.CONSTRUCTOR ? elem.getEnclosingElement().getSimpleName().toString() : elem.getSimpleName().toString();
            this.params = new ArrayList<ParamDesc>();
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null) {
                    break;
                }
                this.params.add(new ParamDesc(tm.toString(), Utilities.getTypeName(info, tm, false, elem.isVarArgs() && !tIt.hasNext()).toString(), it.next().getSimpleName().toString()));
            }
            TypeMirror retType = type.getReturnType();
            this.typeName = Utilities.getTypeName(info, retType, false).toString();
        }

        @Override
        public int getSortPriority() {
            return isDeprecated ? 100 - SMART_TYPE + DEPRECATED : 100 - SMART_TYPE;
        }

        @Override
        public CharSequence getSortText() {
            if (sortText == null) {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('(');
                int cnt = 0;
                for(Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc param = it.next();
                    sortParams.append(param.typeName);
                    if (it.hasNext()) {
                        sortParams.append(',');
                    }
                    cnt++;
                }
                sortParams.append(')');
                sortText = "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
            }
            return sortText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return ""; //NOI18N
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(PARAMETERS_COLOR);
                if (isDeprecated) {
                    lText.append(STRIKE);
                }
                lText.append(simpleName);
                if (isDeprecated) {
                    lText.append(STRIKE_END);
                }
                lText.append('(');
                for (int i = 0; i < params.size(); i++) {
                    ParamDesc paramDesc = params.get(i);
                    if (i == activeParamsIndex) {
                        lText.append(COLOR_END).append(LFCustoms.getTextFgColorHTML()).append(BOLD);
                    }
                    lText.append(escape(paramDesc.typeName));
                    lText.append(' ');
                    lText.append(paramDesc.name);
                    if (i < params.size() - 1) {
                        lText.append(", "); //NOI18N
                    } else {
                        lText.append(BOLD_END).append(COLOR_END).append(PARAMETERS_COLOR);
                    }
                }
                lText.append(')');
                lText.append(COLOR_END);
                return lText.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            if (rightText == null) {
                rightText = PARAMETERS_COLOR + escape(typeName) + COLOR_END;
            }
            return rightText;
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(elementHandle);
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }

        @Override
        protected CharSequence getInsertPostfix(JTextComponent c) {
            return ")"; //NOI18N
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            super.substituteText(c, offset, length, null, null);
            StringBuilder sb = new StringBuilder();
            boolean guessArgs = Utilities.guessMethodArguments();
            for (int i = activeParamsIndex; i < params.size(); i++) {
                ParamDesc paramDesc = params.get(i);
                sb.append("${"); //NOI18N
                sb.append(paramDesc.name);
                if (guessArgs) {
                    sb.append(" named instanceof=\""); //NOI18N
                    sb.append(paramDesc.fullTypeName);
                    sb.append("\""); //NOI18N
                }
                sb.append("}"); //NOI18N
                if (i < params.size() - 1) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(toAdd);
            showTooltip = Utilities.popupPrameterTooltip();
            return sb;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(typeName);
            sb.append(' ');
            sb.append(simpleName);
            sb.append('(');
            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                ParamDesc paramDesc = it.next();
                sb.append(paramDesc.typeName);
                sb.append(' ');
                sb.append(paramDesc.name);
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(") - parameters"); //NOI18N
            return sb.toString();
        }
    }

    static class AnnotationItem extends AnnotationTypeItem {

        private AnnotationItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean smartType, WhiteListQuery.WhiteList whiteList) {
            super(info, elem, type, 0, substitutionOffset, referencesCount, isDeprecated, false, false, smartType, false, whiteList);
        }

        @Override
        public CharSequence getInsertPrefix() {
            return "@" + super.getInsertPrefix(); //NOI18N
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            final BaseDocument doc = (BaseDocument)c.getDocument();
            final StringBuilder sb = new StringBuilder();
            final AtomicBoolean cancel = new AtomicBoolean();
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                if (cancel.get()) {
                                    return;
                                }
                                final CompilationController controller = CompilationController.get(resultIterator.getParserResult(offset));
                                controller.toPhase(Phase.RESOLVED);
                                if (cancel.get()) {
                                    return;
                                }
                                final DeclaredType type = typeHandle.resolve(controller);
                                if (type != null) {
                                    TypeElement elem = (TypeElement)type.asElement();
                                    sb.append("@${PAR"); //NOI18N
                                    if (type.getKind() != TypeKind.ERROR &&
                                            EnumSet.range(ElementKind.PACKAGE, ElementKind.INTERFACE).contains(elem.getEnclosingElement().getKind())) {
                                        sb.append(" type=\""); //NOI18N
                                        sb.append(elem.getQualifiedName());
                                        sb.append("\" default=\""); //NOI18N
                                        sb.append(elem.getSimpleName());
                                    } else {
                                        sb.append(" default=\""); //NOI18N
                                        sb.append(elem.getQualifiedName());
                                    }
                                    sb.append("\" editable=false}"); //NOI18N
                                    sb.append(toAdd);
                                }
                            }
                        });
                    } catch (ParseException pe) {
                    }
                }
            }, NbBundle.getMessage(JavaCompletionItem.class, "JCI-import_resolve"), cancel, false); //NOI18N
            if (sb.length() == 0) {
                return super.substituteText(c, offset, length, text, toAdd);
            }
            super.substituteText(c, offset, length, null, null);
            return sb;
        }
    }

    static class AttributeItem extends JavaCompletionItem {

        private static final String ATTRIBUTE = "org/netbeans/modules/java/editor/resources/attribute_16.png"; // NOI18N
        private static final String ATTRIBUTE_COLOR = Utilities.getHTMLColor(128, 128, 128);
        private static final String VALUE_COLOR = Utilities.getHTMLColor(192, 192, 192);
        private static ImageIcon icon;

        private ElementHandle<ExecutableElement> elementHandle;
        private boolean isDeprecated;
        private String simpleName;
        private String typeName;
        private String defaultValue;
        private String leftText;
        private String rightText;

        private AttributeItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated) {
            super(substitutionOffset);
            this.elementHandle = ElementHandle.create(elem);
            this.isDeprecated = isDeprecated;
            this.simpleName = elem.getSimpleName().toString();
            this.typeName = Utilities.getTypeName(info, type.getReturnType(), false).toString();
            AnnotationValue value = elem.getDefaultValue();
            this.defaultValue = value != null ? value.getValue() instanceof TypeMirror ? Utilities.getTypeName(info, (TypeMirror)value.getValue(), false).toString() + ".class" : value.toString() : null; //NOI18N
        }

        @Override
        public int getSortPriority() {
            return isDeprecated ? 100 + DEPRECATED : 100;
        }

        @Override
        public CharSequence getSortText() {
            return simpleName;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(elementHandle);
        }

        @Override
        protected ImageIcon getIcon(){
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(ATTRIBUTE, false);
            }
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(ATTRIBUTE_COLOR);
                if (defaultValue == null) {
                    sb.append(BOLD);
                }
                if (isDeprecated) {
                    sb.append(STRIKE);
                }
                sb.append(simpleName);
                if (isDeprecated) {
                    sb.append(STRIKE_END);
                }
                if (defaultValue == null) {
                    sb.append(BOLD_END);
                } else {
                    sb.append(COLOR_END);
                    sb.append(VALUE_COLOR);                            
                    sb.append(" = "); //NOI18N
                    sb.append(defaultValue);
                }
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            if (rightText == null) {
                rightText = escape(typeName);
            }
            return rightText;
        }

        @Override
        protected CharSequence getInsertPostfix(JTextComponent c) {
            return CodeStyle.getDefault(c.getDocument()).spaceAroundAssignOps() ? " = " : "="; //NOI18N
        }

        @Override
        public String toString() {
            return simpleName;
        }
    }

    static class AttributeValueItem extends WhiteListJavaCompletionItem<TypeElement> {

        private static final String ATTRIBUTE_VALUE = "org/netbeans/modules/java/editor/resources/attribute_value_16.png"; // NOI18N
        private static final String ATTRIBUTE_VALUE_COLOR = Utilities.getHTMLColor(128, 128, 128);
        private static ImageIcon icon;

        private JavaCompletionItem delegate;
        private String value;
        private String documentation;
        private String leftText;

        private AttributeValueItem(CompilationInfo info, String value, String documentation, TypeElement element, int substitutionOffset, ReferencesCount referencesCount, WhiteListQuery.WhiteList whiteList) {
            super(substitutionOffset, element != null ? ElementHandle.create(element) : null, whiteList);
            this.value = value;
            this.documentation = documentation;
            if (element != null) {
                delegate = createTypeItem(info, element, (DeclaredType)element.asType(), substitutionOffset, referencesCount, false, false, false, false, false, false, getWhiteList());
            }
        }

        @Override
        public int getSortPriority() {
            return -SMART_TYPE;
        }

        @Override
        public CharSequence getSortText() {
            return delegate != null ? delegate.getSortText() : value;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return delegate != null ? delegate.getInsertPrefix() : value; //NOI18N
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return documentation == null ? null : new CompletionTask() {
                private CompletionDocumentation cd = new CompletionDocumentation() {
                    @Override
                    public String getText() {
                        return documentation;
                    }

                    @Override
                    public URL getURL() {
                        return null;
                    }

                    @Override
                    public CompletionDocumentation resolveLink(String link) {
                        return null;
                    }

                    @Override
                    public Action getGotoSourceAction() {
                        return null;
                    }
                };

                @Override
                public void query(CompletionResultSet resultSet) {
                    resultSet.setDocumentation(cd);
                    resultSet.finish();
                }

                @Override
                public void refresh(CompletionResultSet resultSet) {
                    resultSet.setDocumentation(cd);
                    resultSet.finish();
                }

                @Override
                public void cancel() {
                }
            };
        }

        @Override
        protected ImageIcon getBaseIcon() {
            if (delegate != null) {
                return delegate.getIcon();
            }
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(ATTRIBUTE_VALUE, false);
            }
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                if (delegate != null) {
                    leftText = delegate.getLeftHtmlText();
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(ATTRIBUTE_VALUE_COLOR);
                    sb.append(escape(getLastLine()));
                    sb.append(COLOR_END);
                    leftText = sb.toString();
                }
            }
            return leftText;
        }

        @Override
        protected CharSequence getInsertPostfix(JTextComponent c) {
            if (delegate != null || value.endsWith(".class")) {
                return ".class"; //NOI18N
            } else if (value.charAt(value.length() - 1) == '\"') {
                return "\""; //NOI18N
            }
            return null;
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            if (delegate != null) {
                return delegate.substituteText(c, offset, length, text, toAdd);
            }
            StringBuilder sb = new StringBuilder(text);
            if (sb.charAt(sb.length() - 1) == '\"') {
                sb.deleteCharAt(sb.length() - 1);
            } else if (sb.toString().endsWith(".class")) { //NOI18N
                sb.delete(sb.length() - 6, sb.length());
            }
            return super.substituteText(c, offset, length, sb, toAdd);
        }

        private String getLastLine() {
            String[] lines = value.split("\n");
            String last = lines.length > 0 ? lines[lines.length - 1] : value;
            return last.trim();
        }

        @Override
        public String toString() {
            return value;
        }
    }

    static class StaticMemberItem extends WhiteListJavaCompletionItem<Element> {

        private static final String FIELD_ST_PUBLIC = "org/netbeans/modules/editor/resources/completion/field_static_16.png"; //NOI18N
        private static final String FIELD_ST_PROTECTED = "org/netbeans/modules/editor/resources/completion/field_static_protected_16.png"; //NOI18N
        private static final String FIELD_ST_PACKAGE = "org/netbeans/modules/editor/resources/completion/field_static_package_private_16.png"; //NOI18N
        private static final String FIELD_ST_PRIVATE = "org/netbeans/modules/editor/resources/completion/field_static_private_16.png"; //NOI18N
        private static final String FIELD_COLOR = Utilities.getHTMLColor(64, 64, 242);
        private static final String METHOD_ST_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_static_16.png"; //NOI18N
        private static final String METHOD_ST_PROTECTED = "org/netbeans/modules/editor/resources/completion/method_static_protected_16.png"; //NOI18N
        private static final String METHOD_ST_PACKAGE = "org/netbeans/modules/editor/resources/completion/method_static_package_private_16.png"; //NOI18N
        private static final String METHOD_ST_PRIVATE = "org/netbeans/modules/editor/resources/completion/method_static_private_16.png"; //NOI18N
        private static final String METHOD_COLOR = Utilities.getHTMLColor(188, 64, 64);
        private static final String PARAMETER_NAME_COLOR = Utilities.getHTMLColor(242, 64, 242);
        private static ImageIcon icon[][] = new ImageIcon[2][4];

        private TypeMirrorHandle<DeclaredType> typeHandle;
        private boolean isDeprecated;
        private String typeName;
        private String memberName;
        private String memberTypeName;
        private boolean addSemicolon;
        private Set<Modifier> modifiers;
        private List<ParamDesc> params;
        private String sortText;
        private String leftText;
        private String rightText;
        private final boolean smartType;

        private StaticMemberItem(CompilationInfo info, DeclaredType type, Element memberElem, TypeMirror memberType, boolean multipleVersions, int substitutionOffset, boolean isDeprecated, boolean addSemicolon, boolean smartType, WhiteListQuery.WhiteList whiteList) {
            super(substitutionOffset, ElementHandle.create(memberElem), whiteList);
            type = (DeclaredType) info.getTypes().erasure(type);
            this.typeHandle = TypeMirrorHandle.create(type);
            this.isDeprecated = isDeprecated;
            this.typeName = Utilities.getTypeName(info, type, false).toString();
            this.memberName = memberElem.getSimpleName().toString();
            TypeMirror mtm = memberElem.getKind().isField() ? memberType : ((ExecutableType)memberType).getReturnType();
            this.memberTypeName = Utilities.getTypeName(info, mtm, false).toString();
            this.addSemicolon = addSemicolon && mtm.getKind() == TypeKind.VOID;
            this.modifiers = memberElem.getModifiers();
            if (!memberElem.getKind().isField() && !multipleVersions) {
                this.params = new ArrayList<ParamDesc>();
                Iterator<? extends VariableElement> it = ((ExecutableElement)memberElem).getParameters().iterator();
                Iterator<? extends TypeMirror> tIt = ((ExecutableType)memberType).getParameterTypes().iterator();
                while(it.hasNext() && tIt.hasNext()) {
                    TypeMirror tm = tIt.next();
                    this.params.add(new ParamDesc(tm.toString(), Utilities.getTypeName(info, tm, false, ((ExecutableElement)memberElem).isVarArgs() && !tIt.hasNext()).toString(), it.next().getSimpleName().toString()));
                }
            }
            this.smartType = smartType;
        }

        @Override
        public int getSortPriority() {
            int p = (getElementHandle().getKind().isField() ? 720 : 750) - (smartType ? SMART_TYPE : 0);
            return isDeprecated ? p + DEPRECATED : p;
        }

        @Override
        public CharSequence getSortText() {
            if (sortText == null) {
                if (getElementHandle().getKind().isField()) {
                    sortText = memberName + "#" + typeName; //NOI18N
                } else {
                    StringBuilder sortParams = new StringBuilder();
                    sortParams.append('(');
                    int cnt = 0;
                    if (params == null) {
                        sortParams.append("..."); //NOI18N
                    } else {
                        for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                            ParamDesc paramDesc = it.next();
                            sortParams.append(paramDesc.typeName);
                            if (it.hasNext()) {
                                sortParams.append(',');
                            }
                            cnt++;
                        }
                    }
                    sortParams.append(')');
                    sortText = memberName + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString() + "#" + typeName; //NOI18N
                }
            }
            return sortText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return typeName + "." + memberName; //NOI18N
        }

        @Override
        protected CharSequence getInsertPostfix(JTextComponent c) {
            return addSemicolon ? new StringBuilder().append(';') : null;
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(getElementHandle());
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(getElementHandle().getKind().isField() ? FIELD_COLOR : METHOD_COLOR);
                lText.append(escape(typeName));
                lText.append('.');
                if (isDeprecated || isBlackListed()) {
                    lText.append(STRIKE);
                }
                lText.append(memberName);
                if (isDeprecated || isBlackListed()) {
                    lText.append(STRIKE_END);
                }
                lText.append(COLOR_END);
                if (!getElementHandle().getKind().isField()) {
                    lText.append('(');
                    if (params == null) {
                        lText.append("..."); //NOI18N
                    } else {
                        for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                            ParamDesc paramDesc = it.next();
                            lText.append(escape(paramDesc.typeName));
                            lText.append(' '); //NOI18N
                            lText.append(PARAMETER_NAME_COLOR);
                            lText.append(paramDesc.name);
                            lText.append(COLOR_END);
                            if (it.hasNext()) {
                                lText.append(", "); //NOI18N
                            }
                        }
                    }
                    lText.append(')');
                }
                leftText = lText.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            if (rightText == null) {
                rightText = escape(memberTypeName);
            }
            return rightText;
        }

        @Override
        protected ImageIcon getBaseIcon(){
            int level = getProtectionLevel(modifiers);
            boolean isField = getElementHandle().getKind().isField();
            ImageIcon cachedIcon = icon[isField ? 0 : 1][level];
            if (cachedIcon != null) {
                return cachedIcon;
            }
            String iconPath = null;
            if (isField) {
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = FIELD_ST_PRIVATE;
                        break;
                    case PACKAGE_LEVEL:
                        iconPath = FIELD_ST_PACKAGE;
                        break;
                    case PROTECTED_LEVEL:
                        iconPath = FIELD_ST_PROTECTED;
                        break;
                    case PUBLIC_LEVEL:
                        iconPath = FIELD_ST_PUBLIC;
                        break;
                }
            } else {
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = METHOD_ST_PRIVATE;
                        break;
                    case PACKAGE_LEVEL:
                        iconPath = METHOD_ST_PACKAGE;
                        break;
                    case PROTECTED_LEVEL:
                        iconPath = METHOD_ST_PROTECTED;
                        break;
                    case PUBLIC_LEVEL:
                        iconPath = METHOD_ST_PUBLIC;
                        break;
                }
            }
            if (iconPath == null) {
                return null;
            }
            ImageIcon newIcon = ImageUtilities.loadImageIcon(iconPath, false);
            icon[isField ? 0 : 1][level] = newIcon;
            return newIcon;
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, CharSequence toAdd) {
            if (toAdd.length() > 0 && toAdd.charAt(toAdd.length() - 1) == '.') {
                if (typeName.length() == length) {
                    return super.substituteText(c, offset + length, 0, ".", null); //NOI18N
                }
                toAdd = toAdd.subSequence(0, toAdd.length() - 1);
            }
            super.substituteText(c, offset, length, null, null);
            final BaseDocument doc = (BaseDocument)c.getDocument();
            final StringBuilder template = new StringBuilder();
            final AtomicBoolean cancel = new AtomicBoolean();
            final CharSequence finalToAdd = toAdd;
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                @Override
                public void run() {
                    try {
                         ModificationResult.runModificationTask(Collections.singletonList(Source.create(doc)), new UserTask() {
                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                if (cancel.get()) {
                                    return;
                                }
                                final WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult(offset));
                                copy.toPhase(Phase.RESOLVED);
                                if (cancel.get()) {
                                    return;
                                }
                                if (CodeStyle.getDefault(doc).preferStaticImports() && !"this".equals(memberName) && !"super".equals(memberName)) {
                                    Element e = getElementHandle().resolve(copy);
                                    if (e != null) {
                                        copy.rewrite(copy.getCompilationUnit(), GeneratorUtilities.get(copy).addImports(copy.getCompilationUnit(), Collections.singleton(e)));
                                    }
                                } else {
                                    DeclaredType type = typeHandle.resolve(copy);
                                    int cnt = 1;
                                    template.append("${PAR#"); //NOI18N
                                    template.append(cnt++);
                                    template.append(" type=\""); //NOI18N
                                    template.append(((TypeElement)type.asElement()).getQualifiedName());
                                    template.append("\" default=\""); //NOI18N
                                    template.append(((TypeElement)type.asElement()).getSimpleName());
                                    template.append("\" editable=false}"); //NOI18N
                                    Iterator<? extends TypeMirror> tas = type.getTypeArguments().iterator();
                                    if (tas.hasNext()) {
                                        template.append('<'); //NOI18N
                                        while (tas.hasNext()) {
                                            TypeMirror ta = tas.next();
                                            template.append("${PAR#"); //NOI18N
                                            template.append(cnt++);
                                            if (ta.getKind() == TypeKind.TYPEVAR) {
                                                template.append(" type=\""); //NOI18N
                                                ta = ((TypeVariable)ta).getUpperBound();
                                                template.append(Utilities.getTypeName(copy, ta, true));
                                                template.append("\" default=\""); //NOI18N
                                                template.append(Utilities.getTypeName(copy, ta, false));
                                                template.append("\"}"); //NOI18N
                                            } else if (ta.getKind() == TypeKind.WILDCARD) {
                                                template.append(" type=\""); //NOI18N
                                                TypeMirror bound = ((WildcardType)ta).getExtendsBound();
                                                if (bound == null) {
                                                    bound = ((WildcardType)ta).getSuperBound();
                                                }
                                                template.append(bound != null ? Utilities.getTypeName(copy, bound, true) : "Object"); //NOI18N
                                                template.append("\" default=\""); //NOI18N
                                                template.append(bound != null ? Utilities.getTypeName(copy, bound, false) : "Object"); //NOI18N
                                                template.append("\"}"); //NOI18N
                                            } else if (ta.getKind() == TypeKind.ERROR) {
                                                template.append(" default=\""); //NOI18N
                                                template.append(((ErrorType)ta).asElement().getSimpleName());
                                                template.append("\"}"); //NOI18N
                                            } else {
                                                template.append(" type=\""); //NOI18N
                                                template.append(Utilities.getTypeName(copy, ta, true));
                                                template.append("\" default=\""); //NOI18N
                                                template.append(Utilities.getTypeName(copy, ta, false));
                                                template.append("\" editable=false}"); //NOI18N
                                            }
                                            if (tas.hasNext()) {
                                                template.append(", "); //NOI18N
                                            }
                                        }
                                        template.append('>'); //NOI18N
                                    }
                                    template.append('.'); //NOI18N
                                }
                                template.append(memberName);
                                if (!getElementHandle().getKind().isField()) {
                                    template.append("("); //NOI18N
                                    if (params == null) {
                                        template.append("${cursor completionInvoke}"); // NOI18N
                                    } else {
                                        boolean guessArgs = Utilities.guessMethodArguments();
                                        for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                                            ParamDesc paramDesc = it.next();
                                            template.append("${"); //NOI18N
                                            template.append(paramDesc.name);
                                            if (guessArgs) {
                                                template.append(" named instanceof=\""); //NOI18N
                                                template.append(paramDesc.fullTypeName);
                                                template.append("\""); //NOI18N
                                            }
                                            template.append("}"); //NOI18N
                                            if (it.hasNext()) {
                                                template.append(", "); //NOI18N
                                            }
                                        }
                                    }
                                    template.append(")");//NOI18N
                                }
                                if (finalToAdd != null) {
                                    template.append(finalToAdd);
                                }
                            }
                        }).commit();
                    } catch (Exception ex) {
                    }
                }
            }, NbBundle.getMessage(JavaCompletionItem.class, "JCI-import_resolve"), cancel, false); //NOI18N
            return template;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(Modifier mod : modifiers) {
               sb.append(mod.toString());
               sb.append(' '); // NOI18N
            }
            sb.append(memberTypeName);
            sb.append(' ');
            sb.append(typeName);
            sb.append('.');
            sb.append(memberName);
            if (!getElementHandle().getKind().isField()) {
                sb.append('('); //NOI18N
                if (params == null) {
                    sb.append("..."); //NOI18N
                } else {
                    for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                        ParamDesc paramDesc = it.next();
                        sb.append(paramDesc.typeName);
                        sb.append(' ');
                        sb.append(paramDesc.name);
                        if (it.hasNext()) {
                            sb.append(", "); //NOI18N
                        }
                    }
                }
                sb.append(')');
            }
            return sb.toString();
        }
    }

    static class ChainedMembersItem extends WhiteListJavaCompletionItem<Element> {

        private static final String FIELD_ST_PUBLIC = "org/netbeans/modules/editor/resources/completion/field_static_16.png"; //NOI18N
        private static final String FIELD_ST_PROTECTED = "org/netbeans/modules/editor/resources/completion/field_static_protected_16.png"; //NOI18N
        private static final String FIELD_ST_PACKAGE = "org/netbeans/modules/editor/resources/completion/field_static_package_private_16.png"; //NOI18N
        private static final String FIELD_ST_PRIVATE = "org/netbeans/modules/editor/resources/completion/field_static_private_16.png"; //NOI18N
        private static final String FIELD_COLOR = Utilities.getHTMLColor(64, 64, 242);
        private static final String METHOD_ST_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_static_16.png"; //NOI18N
        private static final String METHOD_ST_PROTECTED = "org/netbeans/modules/editor/resources/completion/method_static_protected_16.png"; //NOI18N
        private static final String METHOD_ST_PACKAGE = "org/netbeans/modules/editor/resources/completion/method_static_package_private_16.png"; //NOI18N
        private static final String METHOD_ST_PRIVATE = "org/netbeans/modules/editor/resources/completion/method_static_private_16.png"; //NOI18N
        private static final String METHOD_COLOR = Utilities.getHTMLColor(188, 64, 64);
        private static final String PARAMETER_NAME_COLOR = Utilities.getHTMLColor(242, 64, 242);
        private static ImageIcon icon[][] = new ImageIcon[2][4];

        private List<MemberDesc> members;
        private String firstMemberName;
        private String lastMemberTypeName;
        private boolean isLastMethod;
        private boolean isDeprecated;
        private boolean addSemicolon;
        private Set<Modifier> modifiers;
        private String sortText;
        private String leftText;
        private String rightText;

        private ChainedMembersItem(CompilationInfo info, List<? extends Element> chainedElems, List<? extends TypeMirror> chainedTypes, int substitutionOffset, boolean isDeprecated, boolean addSemicolon, WhiteListQuery.WhiteList whiteList) {
            super(substitutionOffset, chainedElems, whiteList);
            assert chainedElems.size() == chainedTypes.size();
            this.isDeprecated = isDeprecated;
            this.members = new ArrayList<>(chainedElems.size());
            Element lastMemberElem = null;
            TypeMirror lastMemberType = null;
            Iterator<? extends TypeMirror> typesIt = chainedTypes.iterator();
            for (Element element : chainedElems) {
                TypeMirror type = typesIt.next();
                String elementName = element.getSimpleName().toString();
                if (this.firstMemberName == null) {
                    this.firstMemberName = elementName;
                }
                List<ParamDesc> params = null;
                if (element.getKind() == ElementKind.METHOD) {
                    params = new ArrayList<ParamDesc>();
                    Iterator<? extends VariableElement> it = ((ExecutableElement)element).getParameters().iterator();
                    Iterator<? extends TypeMirror> tIt = ((ExecutableType)type).getParameterTypes().iterator();
                    while(it.hasNext() && tIt.hasNext()) {
                        TypeMirror tm = tIt.next();
                        params.add(new ParamDesc(tm.toString(), Utilities.getTypeName(info, tm, false, ((ExecutableElement)element).isVarArgs() && !tIt.hasNext()).toString(), it.next().getSimpleName().toString()));
                    }
                }
                this.members.add(new MemberDesc(element.getKind(), elementName, params));
                lastMemberElem = element;
                lastMemberType = type;
            }
            this.isLastMethod = lastMemberElem.getKind() == ElementKind.METHOD;
            TypeMirror mtm = this.isLastMethod ? ((ExecutableType)lastMemberType).getReturnType() : lastMemberType;
            this.lastMemberTypeName = Utilities.getTypeName(info, mtm, false).toString();
            this.addSemicolon = addSemicolon && mtm.getKind() == TypeKind.VOID;
            this.modifiers = lastMemberElem.getModifiers();
        }

        @Override
        public int getSortPriority() {
            int i = 0;
            for (MemberDesc member : members) {
                i *= 3;
                switch (member.kind) {
                    case LOCAL_VARIABLE:
                    case PARAMETER:
                    case RESOURCE_VARIABLE:
                    case EXCEPTION_PARAMETER:
                        i += 1;
                        break;
                    case FIELD:
                    case ENUM_CONSTANT:
                        i += 2;
                        break;
                    default:
                        i += 3;
                }                
            }
            int p = 700 + Math.min(i, 99) - SMART_TYPE;
            return isDeprecated ? p + DEPRECATED : p;
        }

        @Override
        public CharSequence getSortText() {
            if (sortText == null) {
                StringBuilder sb = new StringBuilder();
                for (Iterator<MemberDesc> membersIt = members.iterator(); membersIt.hasNext();) {
                    MemberDesc member = membersIt.next();
                    sb.append(member.name);
                    if (member.params != null) {
                        StringBuilder sortParams = new StringBuilder();
                        sortParams.append("#("); //NOI18N
                        int cnt = 0;
                        for (Iterator<ParamDesc> paramsIt = member.params.iterator(); paramsIt.hasNext();) {
                            ParamDesc paramDesc = paramsIt.next();
                            sortParams.append(paramDesc.typeName);
                            if (paramsIt.hasNext()) {
                                sortParams.append(',');
                            }
                            cnt++;
                        }
                        sortParams.append(')');
                        sb.append(cnt < 10 ? "0" : "").append(cnt).append("#").append(sortParams); //NOI18N
                    }
                    if (membersIt.hasNext()) {
                        sb.append('#');
                    }
                }
                sortText = sb.toString();
            }
            return sortText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return firstMemberName;
        }

        @Override
        protected CharSequence getInsertPostfix(JTextComponent c) {
            StringBuilder sb = new StringBuilder();
            if (isLastMethod) {
                sb.append(CodeStyle.getDefault(c.getDocument()).spaceBeforeMethodCallParen() ? " ()" : "()"); //NOI18N
            }
            if (addSemicolon) {
                sb.append(';');
            }
            return sb;
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(getElementHandle());
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                for (Iterator<MemberDesc> membersIt = members.iterator(); membersIt.hasNext();) {
                    MemberDesc member = membersIt.next();
                    lText.append(member.kind == ElementKind.METHOD ? METHOD_COLOR : member.kind.isField() ? FIELD_COLOR : PARAMETER_NAME_COLOR);
                    if (isDeprecated || isBlackListed()) {
                        lText.append(STRIKE);
                    }
                    lText.append(member.name);
                    if (isDeprecated || isBlackListed()) {
                        lText.append(STRIKE_END);
                    }
                    lText.append(COLOR_END);
                    if (member.params != null) {
                        lText.append('(');
                        for (Iterator<ParamDesc> paramsIt = member.params.iterator(); paramsIt.hasNext();) {
                            ParamDesc paramDesc = paramsIt.next();
                            lText.append(escape(paramDesc.typeName));
                            lText.append(' '); //NOI18N
                            lText.append(PARAMETER_NAME_COLOR);
                            lText.append(paramDesc.name);
                            lText.append(COLOR_END);
                            if (paramsIt.hasNext()) {
                                lText.append(", "); //NOI18N
                            }
                        }
                        lText.append(')');
                    }
                    if (membersIt.hasNext()) {
                        lText.append('.');
                    }
                }
                leftText = lText.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            if (rightText == null) {
                rightText = escape(lastMemberTypeName);
            }
            return rightText;
        }

        @Override
        protected ImageIcon getBaseIcon(){
            int level = getProtectionLevel(modifiers);
            boolean isField = getElementHandle().getKind().isField();
            ImageIcon cachedIcon = icon[isField ? 0 : 1][level];
            if (cachedIcon != null) {
                return cachedIcon;
            }
            String iconPath = null;
            if (isField) {
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = FIELD_ST_PRIVATE;
                        break;
                    case PACKAGE_LEVEL:
                        iconPath = FIELD_ST_PACKAGE;
                        break;
                    case PROTECTED_LEVEL:
                        iconPath = FIELD_ST_PROTECTED;
                        break;
                    case PUBLIC_LEVEL:
                        iconPath = FIELD_ST_PUBLIC;
                        break;
                }
            } else {
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = METHOD_ST_PRIVATE;
                        break;
                    case PACKAGE_LEVEL:
                        iconPath = METHOD_ST_PACKAGE;
                        break;
                    case PROTECTED_LEVEL:
                        iconPath = METHOD_ST_PROTECTED;
                        break;
                    case PUBLIC_LEVEL:
                        iconPath = METHOD_ST_PUBLIC;
                        break;
                }
            }
            if (iconPath == null) {
                return null;
            }
            ImageIcon newIcon = ImageUtilities.loadImageIcon(iconPath, false);
            icon[isField ? 0 : 1][level] = newIcon;
            return newIcon;
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, CharSequence toAdd) {
            if (toAdd.length() > 0 && toAdd.charAt(toAdd.length() - 1) == '.') {
                if (firstMemberName.length() == length) {
                    return super.substituteText(c, offset + length, 0, ".", null); //NOI18N
                }
                toAdd = toAdd.subSequence(0, toAdd.length() - 1);
            }
            StringBuilder sb = new StringBuilder();
            boolean asTemplate = false;
            for (Iterator<MemberDesc> membersIt = members.iterator(); membersIt.hasNext();) {
                MemberDesc member = membersIt.next();
                sb.append(member.name);
                if (member.params != null) {
                    if (asTemplate || membersIt.hasNext() || !member.params.isEmpty()) {
                        sb.append(CodeStyle.getDefault(c.getDocument()).spaceBeforeMethodCallParen() ? " (" : "("); //NOI18N
                        boolean guessArgs = Utilities.guessMethodArguments();
                        for (Iterator<ParamDesc> paramsIt = member.params.iterator(); paramsIt.hasNext();) {
                            ParamDesc paramDesc = paramsIt.next();
                            sb.append("${"); //NOI18N
                            sb.append(paramDesc.name);
                            if (guessArgs) {
                                sb.append(" named instanceof=\""); //NOI18N
                                sb.append(paramDesc.fullTypeName);
                                sb.append("\""); //NOI18N
                            }
                            sb.append('}');
                            if (paramsIt.hasNext()) {
                                sb.append(", "); //NOI18N
                            }
                            asTemplate = true;
                        }
                        sb.append(')');//NOI18N
                    }
                }
                if (membersIt.hasNext()) {
                    sb.append('.');
                }
            }
            if (!asTemplate) {
                return super.substituteText(c, offset, length, sb, toAdd);
            }
            if (toAdd != null) {
                String toAddText = toAdd.toString();
                int idx = toAddText.indexOf(')');
                if (idx > 0) {
                    if (toAddText.length() > idx + 1) {
                        sb.append(toAddText.substring(idx + 1));
                    }
                }
            }
            super.substituteText(c, offset, length, null, null);
            return sb;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(Modifier mod : modifiers) {
               sb.append(mod.toString());
               sb.append(' '); // NOI18N
            }
            for (Iterator<MemberDesc> membersIt = members.iterator(); membersIt.hasNext();) {
                MemberDesc member = membersIt.next();
                sb.append(member.name);
                if (member.params != null) {
                    sb.append('(');
                    for (Iterator<ParamDesc> paramsIt = member.params.iterator(); paramsIt.hasNext();) {
                        ParamDesc paramDesc = paramsIt.next();
                        sb.append(escape(paramDesc.typeName));
                        sb.append(' ');
                        sb.append(paramDesc.name);
                        if (paramsIt.hasNext()) {
                            sb.append(", "); //NOI18N
                        }
                    }
                    sb.append(')');
                }
                if (membersIt.hasNext()) {
                    sb.append('.');
                }
            }
            return sb.toString();
        }
    }

    static class InitializeAllConstructorItem extends JavaCompletionItem {

        private static final String CONSTRUCTOR_PUBLIC = "org/netbeans/modules/java/editor/resources/new_constructor_16.png"; //NOI18N
        private static final String CONSTRUCTOR_COLOR = Utilities.getHTMLColor(242, 203, 64);
        private static final String PARAMETER_NAME_COLOR = Utilities.getHTMLColor(242, 64, 242);
        private static ImageIcon icon;

        private boolean isDefault;
        private List<ElementHandle<VariableElement>> fieldHandles;
        private ElementHandle<TypeElement> parentHandle;
        private ElementHandle<ExecutableElement> superConstructorHandle;
        private String simpleName;
        private List<ParamDesc> params;
        private String sortText;
        private String leftText;

        private InitializeAllConstructorItem(CompilationInfo info, boolean isDefault, Iterable<? extends VariableElement> fields, ExecutableElement superConstructor, TypeElement parent, int substitutionOffset) {
            super(substitutionOffset);
            CodeStyle cs = null;
            try {
                cs = CodeStyle.getDefault(info.getDocument());
            } catch (IOException ex) {
            }
            if (cs == null) {
                cs = CodeStyle.getDefault(info.getFileObject());
            }
            this.isDefault = isDefault;
            this.fieldHandles = new ArrayList<ElementHandle<VariableElement>>();
            this.parentHandle = ElementHandle.create(parent);
            this.params = new ArrayList<ParamDesc>();
            for (VariableElement ve : fields) {
                this.fieldHandles.add(ElementHandle.create(ve));
                if (!isDefault) {
                    boolean isStatic = ve.getModifiers().contains(Modifier.STATIC);
                    String sName = CodeStyleUtils.removePrefixSuffix(ve.getSimpleName(),
                        isStatic ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                        isStatic ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
                    sName = CodeStyleUtils.addPrefixSuffix(
                            sName,
                            cs.getParameterNamePrefix(),
                            cs.getParameterNameSuffix());
                    this.params.add(new ParamDesc(null, Utilities.getTypeName(info, ve.asType(), false).toString(), sName));
                }
            }
            if (superConstructor != null) {
                this.superConstructorHandle = ElementHandle.create(superConstructor);
                if (!isDefault) {
                    for (VariableElement ve : superConstructor.getParameters()) {
                        String sName = CodeStyleUtils.removePrefixSuffix(ve.getSimpleName(), cs.getParameterNamePrefix(), cs.getParameterNameSuffix());
                        sName = CodeStyleUtils.addPrefixSuffix(
                                sName,
                                cs.getParameterNamePrefix(),
                                cs.getParameterNameSuffix());
                        this.params.add(new ParamDesc(null, Utilities.getTypeName(info, ve.asType(), false).toString(), sName));
                    }
                }
            } else {
                this.superConstructorHandle = null;
            }
            this.simpleName = parent.getSimpleName().toString();
        }

        @Override
        public int getSortPriority() {
            return 400;
        }

        @Override
        public CharSequence getSortText() {
            if (sortText == null) {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('('); //NOI18N
                int cnt = 0;
                for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc paramDesc = it.next();
                    sortParams.append(paramDesc.typeName);
                    if (it.hasNext()) {
                        sortParams.append(','); //NOI18N
                    }
                    cnt++;
                }
                sortParams.append(')'); //NOI18N
                sortText = simpleName + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
            }
            return sortText;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(CONSTRUCTOR_COLOR);
                lText.append(simpleName);
                lText.append(COLOR_END);
                lText.append('('); //NOI18N
                for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc paramDesc = it.next();
                    lText.append(escape(paramDesc.typeName));
                    lText.append(' '); //NOI18N
                    lText.append(PARAMETER_NAME_COLOR);
                    lText.append(paramDesc.name);
                    lText.append(COLOR_END);
                    if (it.hasNext()) {
                        lText.append(", "); //NOI18N
                    }
                }
                lText.append(") - "); //NOI18N
                lText.append(GENERATE_TEXT);
                leftText = lText.toString();
            }
            return leftText;
        }

        @Override
        protected ImageIcon getIcon() {
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(CONSTRUCTOR_PUBLIC, false);
            }
            return icon;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            final BaseDocument doc = (BaseDocument)c.getDocument();
            final Position pos;
            try {
                pos = doc.createPosition(offset);
            } catch (BadLocationException e) {
                return null; // Invalid offset -> do nothing
            }
            CharSequence cs = super.substituteText(c, offset, length, null, null);
            try {
                ModificationResult mr = ModificationResult.runModificationTask(Collections.singletonList(Source.create(doc)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                        copy.toPhase(Phase.ELEMENTS_RESOLVED);
                        final int embeddedOffset = copy.getSnapshot().getEmbeddedOffset(pos.getOffset());
                        TreePath tp = copy.getTreeUtilities().pathFor(embeddedOffset);
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                            TypeElement parent = parentHandle.resolve(copy);
                            if (parent != null && parent == copy.getTrees().getElement(tp)) {
                                ArrayList<VariableElement> fieldElements = new ArrayList<VariableElement>();
                                for (ElementHandle<? extends Element> handle : fieldHandles) {
                                    Element fieldElement = handle.resolve(copy);
                                    if (fieldElement != null && fieldElement.getKind().isField()) {
                                        fieldElements.add((VariableElement)fieldElement);
                                    }
                                }
                                ExecutableElement superConstructor = superConstructorHandle != null ? superConstructorHandle.resolve(copy) : null;
                                ClassTree clazz = (ClassTree) tp.getLeaf();
                                GeneratorUtilities gu = GeneratorUtilities.get(copy);
                                MethodTree ctor = isDefault ? gu.createDefaultConstructor(parent, fieldElements, superConstructor)
                                        : gu.createConstructor(parent, fieldElements, superConstructor);
                                ClassTree decl = GeneratorUtils.insertClassMember(copy, clazz, ctor, embeddedOffset);
                                copy.rewrite(clazz, decl);
                            }
                        }
                    }
                });
                GeneratorUtils.guardedCommit(c, mr);
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
            return cs;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("public "); //NOI18N
            sb.append(simpleName);
            sb.append('('); //NOI18N
            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                ParamDesc paramDesc = it.next();
                sb.append(paramDesc.typeName);
                sb.append(' '); //NOI18N
                sb.append(paramDesc.name);
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(") - "); //NOI18N
            sb.append(GENERATE_TEXT);
            return sb.toString();
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }
    }
    
    static class LambdaCompletionItem extends JavaCompletionItem {

        private static final String METHOD_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_16.png"; //NOI18N
        private static final String PARAMETER_NAME_COLOR = Utilities.getHTMLColor(224, 160, 65);
        private static ImageIcon icon;

        private ElementHandle<ExecutableElement> handle;
        private ArrayList<ParamDesc> params;
        private final boolean expression;
        private final boolean addSemicolon;
        private final String typeName;
        private String sortText;
        private String leftText;
        private String rightText;
        
        public LambdaCompletionItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, boolean expression, boolean addSemicolon) {
            super(substitutionOffset);
            ExecutableElement desc = info.getElementUtilities().getDescriptorElement(elem);
            this.handle = ElementHandle.create(desc);
            ExecutableType descType = (ExecutableType)info.getTypes().asMemberOf(type, desc);            
            this.params = new ArrayList<>();
            Iterator<? extends VariableElement> it = desc.getParameters().iterator();            
            Iterator<? extends TypeMirror> tIt = descType.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null) {
                    break;
                }
                this.params.add(new ParamDesc(tm.toString(), Utilities.getTypeName(info, tm, false, desc.isVarArgs() && !tIt.hasNext()).toString(), it.next().getSimpleName().toString()));
            }
            TypeMirror retType = descType.getReturnType();
            this.addSemicolon = addSemicolon && retType.getKind() == TypeKind.VOID;
            this.typeName = Utilities.getTypeName(info, retType, false).toString();
            this.expression = expression;
        }

        @Override
        public int getSortPriority() {
            return 50 - SMART_TYPE - (expression ? 1 : 0);
        }

        @Override
        public CharSequence getSortText() {
            if (sortText == null) {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('(');
                int cnt = 0;
                for(Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc param = it.next();
                    sortParams.append(param.typeName);
                    if (it.hasNext()) {
                        sortParams.append(',');
                    }
                    cnt++;
                }
                sortParams.append(')');
                sortText = "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
            }
            return sortText;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(LFCustoms.getTextFgColorHTML());
                lText.append('(');
                for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc paramDesc = it.next();
                    lText.append(escape(paramDesc.typeName));
                    lText.append(' ');
                    lText.append(PARAMETER_NAME_COLOR);
                    lText.append(paramDesc.name);
                    lText.append(COLOR_END);
                    if (it.hasNext()) {
                        lText.append(", "); //NOI18N
                    }
                }
                if (expression) {
                    lText.append(") -> expr."); //NOI18N
                } else {
                    lText.append(") -> {...}"); //NOI18N
                }
                return lText.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            if (rightText == null) {
                rightText = escape(typeName);
            }
            return rightText;
        }

        @Override
        protected ImageIcon getIcon() {
            if (icon == null) {
                icon = ImageUtilities.loadImageIcon(METHOD_PUBLIC, false);
            }
            return icon;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return ""; //NOI18N
        }        

        @Override
        protected CharSequence getInsertPostfix(JTextComponent c) {
            StringBuilder sb = new StringBuilder();
            sb.append("()"); //NOI18N
            boolean spaceAroundLambdaArrow = CodeStyle.getDefault(c.getDocument()).spaceAroundLambdaArrow();
            sb.append(spaceAroundLambdaArrow ? " ->" : "->"); //NOI18N
            sb.append(getIndent(c, spaceAroundLambdaArrow, false));                        
            sb.append("{"); //NOI18N
//            sb.append(getIndent(c));
            sb.append(addSemicolon ? "};" : "}"); //NOI18N
            return sb;
        }

        @Override
        protected CharSequence substituteText(final JTextComponent c, final int offset, final int length, final CharSequence text, final CharSequence toAdd) {
            BaseDocument doc = (BaseDocument) c.getDocument();
            boolean inPlace = offset == c.getCaretPosition();
            Position startPos;
            Position endPos;
            try {
                startPos = doc.createPosition(inPlace ? offset : offset + text.length(), Bias.Backward);
                endPos = doc.createPosition(offset + length);
            } catch (BadLocationException ex) {
                return null; // Invalid offset -> do nothing
            }
            CharSequence cs = super.substituteText(c, startPos.getOffset(), inPlace ? length : length - text.length(), null, toAdd);
            StringBuilder sb = new StringBuilder();
            if (toAdd != null) {
                CharSequence postfix = getInsertPostfix(c);
                if (postfix != null) {
                    int postfixLen = postfix.length();
                    int toAddLen = toAdd.length();
                    if (toAddLen >= postfixLen) {
                        String toAddText = toAdd.toString();
                        try {
                            final int off = startPos.getOffset() + toAddText.indexOf('{') + 1;
                            ModificationResult mr = ModificationResult.runModificationTask(Collections.singletonList(Source.create(c.getDocument())), new UserTask() {
                                @Override
                                public void run(ResultIterator resultIterator) throws Exception {
                                    WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                                    copy.toPhase(Phase.RESOLVED);
                                    final ExecutableElement method = handle.resolve(copy);
                                    final int embeddedOffset = copy.getSnapshot().getEmbeddedOffset(off);
                                    TreePath path = copy.getTreeUtilities().pathFor(embeddedOffset);
                                    while (method != null && path.getLeaf() != path.getCompilationUnit()) {
                                        if (path.getLeaf().getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
                                            LambdaExpressionTree tree = (LambdaExpressionTree) path.getLeaf();
                                            Tree newBody;
                                            if (expression && method.getReturnType().getKind() != VOID) {
                                                newBody = GeneratorUtilities.get(copy).createDefaultLambdaExpression(tree, method);
                                            } else {
                                                newBody = GeneratorUtilities.get(copy).createDefaultLambdaBody(tree, method);
                                            }
                                            copy.rewrite(tree.getBody(), newBody);
                                            break;
                                        }
                                        path = path.getParentPath();
                                    }
                                }
                            });
                            GeneratorUtils.guardedCommit(c, mr);
                        } catch (Exception ex) {
                            LOGGER.log(Level.FINE, null, ex);
                        }
                        if (!params.isEmpty()) {
                            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                                ParamDesc paramDesc = it.next();
                                sb.append("${"); //NOI18N
                                sb.append(paramDesc.name);
                                sb.append("}"); //NOI18N
                                if (it.hasNext()) {
                                    sb.append(", "); //NOI18N
                                }
                            }
                        }
                        c.select(startPos.getOffset() + toAddText.indexOf('(') + 1, endPos.getOffset());
                        String selected = c.getSelectedText();

                        if (expression && selected.indexOf('{') == -1) {
                            int n = selected.indexOf('>') + 1;
                            sb.append(selected.substring(0, n));
                            sb.append("${");
                            sb.append(selected.substring(n));
                            sb.append("}");
                        } else {
                            if (selected.contains("return ")) {
                                selected = selected.replace("return ", "return ${")
                                                   .replace(";", "};");
                            }
                            if (params.isEmpty()) {
                                c.setCaretPosition(startPos.getOffset() + toAddText.indexOf('{') + 1); // between {}
                            } else {
                                sb.append(selected);
                            }
                        }
                    }
                }
            }
            if (sb.length() == 0) {
                return cs;
            }
            return sb;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('('); //NOI18N
            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                ParamDesc paramDesc = it.next();
                sb.append(paramDesc.typeName);
                sb.append(' ');
                sb.append(paramDesc.name);
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(") -> {...}"); //NOI18N
            return sb.toString();
        }
    }

    @NbBundle.Messages({"exclude_Lbl=Exclude \"{0}\" from completion", "configure_Excludes_Lbl=Configure excludes"}) // NOI18N
    private static class ExcludeFromCompletionItem implements CompletionItem {

        private static final ImageIcon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/editor/hints/resources/suggestion.gif", false); // NOI18N
        private static WeakReference<ExcludeFromCompletionItem> CONFIGURE_ITEM;
        private CharSequence name;
        private String text;

        private ExcludeFromCompletionItem(CharSequence name) {
            this.name = name;
            this.text = name == null ? Bundle.configure_Excludes_Lbl() : Bundle.exclude_Lbl(name);
        }
        
        @Override
        public void defaultAction(JTextComponent component) {
            Completion.get().hideAll();
            if (name == null) {
                OptionsDisplayer.getDefault().open("Editor/CodeCompletion/text/x-java"); //NOI18N
            } else {
                org.netbeans.modules.java.completion.Utilities.exclude(name);
                Completion.get().showCompletion();
            }
        }

        @Override
        public void processKeyEvent(KeyEvent evt) {
        }

        @Override
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            return CompletionUtilities.getPreferredWidth(text, null, g, defaultFont);
        }

        @Override
        public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
            CompletionUtilities.renderHtml(icon, text, null, g, defaultFont, defaultColor, width, height, selected);
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
            return 10;
        }

        @Override
        public CharSequence getSortText() {
            return text;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return null;
        }
    }

    private static final int PUBLIC_LEVEL = 3;
    private static final int PROTECTED_LEVEL = 2;
    private static final int PACKAGE_LEVEL = 1;
    private static final int PRIVATE_LEVEL = 0;

    private static int getProtectionLevel(Set<Modifier> modifiers) {
        if (modifiers.contains(Modifier.PUBLIC)) {
            return PUBLIC_LEVEL;
        }
        if (modifiers.contains(Modifier.PROTECTED)) {
            return PROTECTED_LEVEL;
        }
        if (modifiers.contains(Modifier.PRIVATE)) {
            return PRIVATE_LEVEL;
        }
        return PACKAGE_LEVEL;
    }

    private static String escape(String s) {
        if (s != null) {
            try {
                return XMLUtil.toElementContent(s);
            } catch (Exception ex) {}
        }
        return s;
    }

    private static int findPositionForSemicolon(JTextComponent c) {
        final int[] ret = new int[] {-2};
        final int offset = c.getSelectionEnd();
        final Source s = Source.create(c.getDocument());
        final AtomicBoolean cancel = new AtomicBoolean();
        ProgressUtils.runOffEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ParserManager.parse(Collections.singletonList(s), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            if (cancel.get()) {
                                return;
                            }
                            final CompilationController controller = CompilationController.get(resultIterator.getParserResult(offset));
                            controller.toPhase(Phase.PARSED);
                            if (cancel.get()) {
                                return;
                            }
                            final int embeddedOffset = controller.getSnapshot().getEmbeddedOffset(offset);
                            Tree t = null;
                            TreePath tp = controller.getTreeUtilities().pathFor(embeddedOffset);
                            boolean cont = true;
                            while (cont && tp != null) {
                                switch(tp.getLeaf().getKind()) {
                                    case EXPRESSION_STATEMENT:
                                    case VARIABLE:
                                    case IMPORT:
                                        t = tp.getLeaf();
                                        cont = false;
                                        break;
                                    case RETURN:
                                        t = ((ReturnTree)tp.getLeaf()).getExpression();
                                        cont = false;
                                        break;
                                    case THROW:
                                        t = ((ThrowTree)tp.getLeaf()).getExpression();
                                        cont = false;
                                        break;
                                    case MEMBER_SELECT:
                                        cont = false;
                                        break;
                                }
                                tp = tp.getParentPath();
                            }
                            if (t != null) {
                                SourcePositions sp = controller.getTrees().getSourcePositions();
                                int endPos = (int)sp.getEndPosition(controller.getCompilationUnit(), t);
                                TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(controller.getTokenHierarchy().tokenSequence(JavaTokenId.language()), embeddedOffset, endPos);
                                if (ts != null) {
                                    if (ts.token().id() == JavaTokenId.SEMICOLON) {
                                        ret[0] = -1;
                                    } else if (ts.moveNext()) {
                                        ret[0] = ts.token().id() == JavaTokenId.LINE_COMMENT || ts.token().id() == JavaTokenId.WHITESPACE && ts.token().text().toString().contains("\n") ? ts.offset() : offset;
                                    } else {
                                        ret[0] = ts.offset() + ts.token().length();
                                    }
                                }
                            } else {
                                TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                                ts.move(embeddedOffset);
                                if (ts.moveNext() && ts.token().id() == JavaTokenId.SEMICOLON) {
                                    ret[0] = -1;
                                }
                            }
                        }
                    });
                } catch (ParseException ex) {
                }
            }
        }, NbBundle.getMessage(JavaCompletionItem.class, "JCI-find_semicolon_pos"), cancel, false); //NOI18N
        return ret[0];
    }
    
    private static int findCastEndPosition(TokenSequence<JavaTokenId> ts, int startPos, int endPos) {
        TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(ts, startPos, endPos);
        if (last != null && last.token().id() == JavaTokenId.DOT) {
            last = findLastNonWhitespaceToken(ts, startPos, last.offset());
            if (last != null) {
                return last.offset() + last.token().length();
            }
        }
        return -1;
    }

    private static TokenSequence<JavaTokenId> findLastNonWhitespaceToken(TokenSequence<JavaTokenId> ts, int startPos, int endPos) {
        ts.move(endPos);
        while(ts.movePrevious()) {
            int offset = ts.offset();
            if (offset < startPos) {
                return null;
            }
            switch (ts.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                case JAVADOC_COMMENT:
                case JAVADOC_COMMENT_LINE_RUN:
                    break;
                default:
                    return ts;
            }
        }
        return null;
    }
    
    private static CharSequence getIndent(JTextComponent c, boolean insertSpace, boolean isClass) {
        StringBuilder sb = new StringBuilder();
        try {
            Document doc = c.getDocument();
            CodeStyle cs = CodeStyle.getDefault(doc);
            int indent = IndentUtils.lineIndent(c.getDocument(), IndentUtils.lineStartOffset(c.getDocument(), c.getCaretPosition()));
            switch (isClass? cs.getClassDeclBracePlacement() : cs.getOtherBracePlacement()) {
                case SAME_LINE:
                    indent = insertSpace ? 1 : 0;
                    break;
                case NEW_LINE:
                    sb.append('\n'); //NOI18N
                    break;
                case NEW_LINE_HALF_INDENTED:
                    sb.append('\n'); //NOI18N
                    indent += (cs.getIndentSize() / 2);
                    break;
                case NEW_LINE_INDENTED:
                    sb.append('\n'); //NOI18N
                    indent += cs.getIndentSize();
                    break;
            }
            int tabSize = cs.getTabSize();
            int col = 0;
            if (!cs.expandTabToSpaces()) {
                while (col + tabSize <= indent) {
                    sb.append('\t'); //NOI18N
                    col += tabSize;
                }
            }
            while (col < indent) {
                sb.append(' '); //NOI18N
                col++;
            }
        } catch (BadLocationException ble) {
        }
        return sb;
    }
    
    private static CharSequence getIndent(JTextComponent c) {
        StringBuilder sb = new StringBuilder();
        try {
            Document doc = c.getDocument();
            CodeStyle cs = CodeStyle.getDefault(doc);
            int indent = IndentUtils.lineIndent(c.getDocument(), IndentUtils.lineStartOffset(c.getDocument(), c.getCaretPosition()));
            int tabSize = cs.getTabSize();
            int col = 0;
            if (!cs.expandTabToSpaces()) {
                while (col + tabSize <= indent) {
                    sb.append('\t'); //NOI18N
                    col += tabSize;
                }
            }
            while (col < indent) {
                sb.append(' '); //NOI18N
                col++;
            }
        } catch (BadLocationException ex) {
        }
        return sb;
    }

    private static CharSequence createAssignToVarText(CompilationInfo info, TypeMirror type, String name) {
        name = adjustName(name);
        type = SourceUtils.resolveCapturedType(info, type);
        StringBuilder sb = new StringBuilder();
        sb.append("${TYPE type=\""); //NOI18N
        sb.append(Utilities.getTypeName(info, type, true));
        sb.append("\" default=\""); //NOI18N
        sb.append(Utilities.getTypeName(info, type, false));
        sb.append("\" editable=false}"); //NOI18N
        sb.append(" ${NAME newVarName=\""); //NOI18N
        sb.append(name);
        sb.append("\" default=\""); //NOI18N
        sb.append(name);
        sb.append("\"} = "); //NOI18N
        return sb;
    }
    
    private static String adjustName(String name) {
        if (name == null) {
            return null;
        }
        String shortName = null;
        if (name.startsWith("get") && name.length() > 3) { //NOI18N
            shortName = name.substring(3);
        }
        if (name.startsWith("is") && name.length() > 2) { //NOI18N
            shortName = name.substring(2);
        }
        if (shortName != null) {
            return firstToLower(shortName);
        }
        if (SourceVersion.isKeyword(name)) {
            return "a" + Character.toUpperCase(name.charAt(0)) + name.substring(1); //NOI18N
        } else {
            return name;
        }
    }

    private static String firstToLower(String name) {
        if (name.length() == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean toLower = true;
        char last = Character.toLowerCase(name.charAt(0));
        for (int i = 1; i < name.length(); i++) {
            if (toLower && Character.isUpperCase(name.charAt(i))) {
                result.append(Character.toLowerCase(last));
            } else {
                result.append(last);
                toLower = false;
            }
            last = name.charAt(i);
        }
        result.append(last);
        if (SourceVersion.isKeyword(result)) {
            return "a" + name; //NOI18N
        } else {
            return result.toString();
        }
    }

    private static TypeMirror typeToImport(CompilationInfo info, TreePath tp, TypeMirror origin) {
        if (tp.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
            MemberSelectTree mst = (MemberSelectTree) tp.getLeaf();
            if (mst.getExpression().getKind() == Tree.Kind.IDENTIFIER) {
                ClassIndex index = info.getClasspathInfo().getClassIndex();
                Types types = info.getTypes();
                Trees trees = info.getTrees();
                Scope scope = trees.getScope(tp);
                for (ElementHandle<TypeElement> teHandle : index.getDeclaredTypes(((IdentifierTree)mst.getExpression()).getName().toString(), ClassIndex.NameKind.SIMPLE_NAME, EnumSet.allOf(ClassIndex.SearchScope.class))) {
                    TypeElement te = teHandle.resolve(info);
                    if (te != null && trees.isAccessible(scope, te)) {
                        TypeMirror toImport = te.asType();
                        if (types.isSubtype(toImport, origin)) {
                            return toImport;
                        }
                    }
                }
            }
        }
        return origin;
    }

    static class MemberDesc {
        private final ElementKind kind;
        private final String name;
        private final List<ParamDesc> params;

        public MemberDesc(ElementKind kind, String name, List<ParamDesc> params) {
            this.kind = kind;
            this.name = name;
            this.params = params;
        }
    }
    
    static class ParamDesc {
        private final String fullTypeName;
        private final String typeName;
        private String name;

        public ParamDesc(String fullTypeName, String typeName, String name) {
            this.fullTypeName = fullTypeName;
            this.typeName = typeName;
            this.name = name;
        }
    }
}
