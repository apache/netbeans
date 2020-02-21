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

package org.netbeans.modules.cnd.refactoring.completion.implmethod;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmDocProvider;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class CsmImplementsMethodCompletionItem implements CompletionItem {

    private final int substitutionOffset;
    private final int priority;
    private final String sortItemText;
    private final boolean supportInstantSubst;
    private static final int PRIORITY = 15;
    private final String appendItemText;
    private final String htmlItemText;
    private final CsmFunction item;
    private final ImageIcon icon;
    private final String right;
    private final boolean isExtractBody;
    private final int startReplacement;
    private final int lengthReplacement;

    private CsmImplementsMethodCompletionItem(CsmFunction item, int substitutionOffset, int priority,
            String sortItemText, String appendItemText, String htmlItemText, boolean supportInstantSubst, String right,
            boolean isExtractBody, int startReplacement, int lengthReplacement) {
        this.substitutionOffset = substitutionOffset;
        this.priority = priority;
        this.supportInstantSubst = supportInstantSubst;
        this.sortItemText = sortItemText;
        this.appendItemText = appendItemText;
        this.htmlItemText = htmlItemText;
        this.item = item;
        icon = (ImageIcon) ImageUtilities.image2Icon((ImageUtilities.mergeImages(ImageUtilities.icon2Image(CsmImageLoader.getIcon(item)),
                                                      ImageUtilities.loadImage("org/netbeans/modules/cnd/refactoring/resources/generate.png"),  // NOI18N
                                                      0, 7)));
        this.right = right;
        this.isExtractBody = isExtractBody;
        this.startReplacement = startReplacement;
        this.lengthReplacement = lengthReplacement;
    }

    public static CsmImplementsMethodCompletionItem createImplementItem(int substitutionOffset, int caretOffset, CsmClass cls, CsmFunction item, CsmScope insertScope) {
        String sortItemText = item.getName().toString();
        String appendItemText = createAppendText(item, cls, "{\n\n}", insertScope); //NOI18N
        String rightText = createRightName(item);
        String coloredItemText = createDisplayName(item, cls, NbBundle.getMessage(CsmImplementsMethodCompletionItem.class, "implement.txt")); //NOI18N
        return new CsmImplementsMethodCompletionItem(item, substitutionOffset, PRIORITY, sortItemText, appendItemText, coloredItemText, true, rightText, false, 0, 0);
    }

    public static CsmImplementsMethodCompletionItem createExtractBodyItem(int substitutionOffset, int caretOffset, CsmClass cls, CsmFunction item, CsmScope insertScope) {
        String sortItemText = item.getName().toString();
        String rightText = createRightName(item);
        String coloredItemText = createDisplayName(item, cls, NbBundle.getMessage(CsmImplementsMethodCompletionItem.class, "extract.txt")); //NOI18N
        CsmFile containingFile = item.getContainingFile();
        final CsmCompoundStatement body = ((CsmFunctionDefinition)item).getBody();
        if (item.getStartOffset() == body.getStartOffset()) {
            // Function definition iside macro expansion.
            // ignore
            return null;
        }
        Document document = CsmUtilities.getDocument(containingFile);
        if (document == null) {
            CloneableEditorSupport support = CsmUtilities.findCloneableEditorSupport(containingFile);
            try {
                document = support.openDocument();
            } catch (IOException ex) {
                return null;
            }
        }
        if (!(document instanceof BaseDocument)) {
            return null;
        }
        final BaseDocument classDoc = (BaseDocument) document;
        final int methodStartOffset = item.getStartOffset();
        if (CsmKindUtilities.isConstructor(item)) {
            CsmConstructor con = (CsmConstructor) item;
            Collection<CsmExpression> initializerList = con.getInitializerList();
            if (initializerList != null && initializerList.size() > 0) {
                final int startOffset = initializerList.iterator().next().getStartOffset();
                final AtomicInteger trueBodyStratOffset = new AtomicInteger(0);
                final String[] res =  new String[1];
                classDoc.render(new Runnable() {
                    @Override
                    public void run() {
                        TokenHierarchy<? extends Document> hi = TokenHierarchy.get(classDoc);
                        TokenSequence<?> ts = hi.tokenSequence();
                        ts.move(startOffset);
                        boolean columnFound = false;
                        while (ts.movePrevious()) {
                            Token<?> token = ts.token();
                            if (ts.offset() < methodStartOffset) {
                                break;
                            }
                            if (columnFound) {
                                if (CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory()) ||
                                    CppTokenId.COMMENT_CATEGORY.equals(token.id().primaryCategory())) {
                                    trueBodyStratOffset.set(ts.offset());
                                    continue;
                                } else {
                                    break;
                                }
                            }
                            if (CppTokenId.COLON.equals(token.id())) {
                                trueBodyStratOffset.set(ts.offset());
                                columnFound = true;
                            }
                        }
                        if (trueBodyStratOffset.get() > 0) {
                            try {
                                res[0] = classDoc.getText(trueBodyStratOffset.get(), body.getEndOffset()-trueBodyStratOffset.get()); //NOI18N
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                });
                if (trueBodyStratOffset.get() > 0) {
                    String bodyText = res[0];
                    String appendItemText = createAppendText(item, cls, bodyText, insertScope);
                    return new CsmImplementsMethodCompletionItem(item, substitutionOffset, PRIORITY, sortItemText, appendItemText, coloredItemText, true, rightText,
                                true, trueBodyStratOffset.get(), bodyText.length());
                }
                return null;
            }
        }
        
        final int startOffset = body.getStartOffset();
        final AtomicInteger trueBodyStratOffset = new AtomicInteger(startOffset);
        final String[] res =  new String[1];
        classDoc.render(new Runnable() {
            @Override
            public void run() {
                TokenHierarchy<? extends Document> hi = TokenHierarchy.get(classDoc);
                TokenSequence<?> ts = hi.tokenSequence();
                ts.move(startOffset);
                while (ts.movePrevious()) {
                    Token<?> token = ts.token();
                    if (ts.offset() < methodStartOffset) {
                        break;
                    }
                    if (CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory()) ||
                        CppTokenId.COMMENT_CATEGORY.equals(token.id().primaryCategory())) {
                        trueBodyStratOffset.set(ts.offset());
                    } else {
                        break;
                    }
                }
                try {
                    res[0] = classDoc.getText(trueBodyStratOffset.get(), body.getEndOffset()-trueBodyStratOffset.get()); //NOI18N
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        String bodyText = res[0];
        if (bodyText == null) {
            return null;
        }
        String appendItemText = createAppendText(item, cls, bodyText, insertScope);
        return new CsmImplementsMethodCompletionItem(item, substitutionOffset, PRIORITY, sortItemText, appendItemText, coloredItemText, true, rightText,
                    true, trueBodyStratOffset.get(), bodyText.length());
    }

    private static String createDisplayName(CsmFunction item,  CsmClass parent, String operation) {
        StringBuilder displayName = new StringBuilder();
        displayName.append(CsmDisplayUtilities.htmlize(parent.getName()));
        displayName.append("::"); //NOI18N
        displayName.append("<b>"); //NOI18N
        displayName.append(CsmDisplayUtilities.htmlize(((CsmFunction)item).getSignature()));
        displayName.append("</b>"); //NOI18N
        if (operation != null) {
            displayName.append(" - "); //NOI18N
            displayName.append(CsmDisplayUtilities.htmlize(operation));
        }
        return displayName.toString();
        //return CsmDisplayUtilities.addHTMLColor(displayName.toString(), 
        //       CsmFontColorManager.instance().getColorAttributes(MIMENames.CPLUSPLUS_MIME_TYPE, FontColorProvider.Entity.FUNCTION));
    }
    
    private static String createRightName(CsmFunction item) {
        if (CsmKindUtilities.isConstructor(item)) {
            return "";
        } else if (CsmKindUtilities.isDestructor(item)) {
            return "";
        } else {
            return ((CsmFunction)item).getReturnType().getText().toString();
        }
    }
    
    private static String createAppendText(CsmFunction item, CsmClass parent, String bodyText, CsmScope insertScope) {
        StringBuilder appendItemText = new StringBuilder("\n"); //NOI18N
        addTemplate(item, parent, appendItemText);
        String type = "";
        if (!CsmKindUtilities.isConstructor(item) && !CsmKindUtilities.isDestructor(item)) {
            final CsmType returnType = ((CsmFunction)item).getReturnType();
            type = returnType.getText().toString()+" "; //NOI18N
            if (!returnType.isTemplateBased()) {
                if (type.indexOf("::") < 0) { //NOI18N
                    CsmClassifier classifier = returnType.getClassifier();
                    if (classifier != null) {
                        String toReplace = classifier.getName().toString();
                        if (type.indexOf(toReplace) == 0) {
                            CsmScope scope = classifier.getScope();
                            if (CsmKindUtilities.isClass(scope)) {
                                type = ((CsmClass)scope).getName()+"::"+type; //NOI18N
                            }
                        } else if (type.startsWith("const "+toReplace)) { //NOI18N
                            CsmScope scope = classifier.getScope();
                            if (CsmKindUtilities.isClass(scope)) {
                                type = "const "+((CsmClass)scope).getName()+"::"+type.substring(6); //NOI18N
                            }
                        }
                    }
                }
            }
        }
        appendItemText.append(type);
        if (!CsmKindUtilities.isFriendMethod(item)) {
            String scope = getQualifiedName(insertScope, parent);
            if (scope.isEmpty()) {
                appendItemText.append(parent.getName());
            } else {
                appendItemText.append(scope);
            }
            if (CsmKindUtilities.isTemplate(parent)) {
                final CsmTemplate template = (CsmTemplate)parent;
                List<CsmTemplateParameter> templateParameters = template.getTemplateParameters();
                if (templateParameters.size() > 0) {
                    appendItemText.append("<");//NOI18N
                    boolean first = true;
                    for(CsmTemplateParameter param : templateParameters) {
                        if (!first) {
                            appendItemText.append(", "); //NOI18N
                        }
                        first = false;
                        appendItemText.append(param.getName());
                    }
                    appendItemText.append(">");//NOI18N
                }
            }
            appendItemText.append("::"); //NOI18N
        }
        addSignature(item, appendItemText);
        appendItemText.append(bodyText);
        appendItemText.append("\n"); //NOI18N
        return appendItemText.toString();
    }
        
    private static String getQualifiedName(CsmScope from, CsmScope to) {
        List<CsmScope> scopes = new ArrayList<>();
        while (!Objects.equals(from, to) && CsmKindUtilities.isScopeElement(to)) {
            scopes.add(0, to);
            to = ((CsmScopeElement) to).getScope();
        }
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (CsmScope scope : scopes) {
            if (CsmKindUtilities.isNamedElement(scope)) {
                CsmNamedElement named = (CsmNamedElement) scope;
                if (!CharSequenceUtils.isNullOrEmpty(named.getName())) {
                    if (!first) {
                        sb.append("::"); // NOI18N
                    } else {
                        first = false;
                    }
                    // TODO: handle instantiations here
                    sb.append(named.getName());
                }
            }
        }
        return sb.toString();
    }

    private static void addTemplate(CsmFunction item, CsmClass parent, StringBuilder sb) {
        if (CsmKindUtilities.isTemplate(parent)) {
            final CsmTemplate template = (CsmTemplate)parent;
            List<CsmTemplateParameter> templateParameters = template.getTemplateParameters();
            if (templateParameters.size() > 0) {
                sb.append("template<");//NOI18N
                boolean first = true;
                for(CsmTemplateParameter param : templateParameters) {
                    if (!first) {
                        sb.append(", "); //NOI18N
                    }
                    first = false;
                    sb.append(param.getText());
                }
                sb.append(">\n");//NOI18N
            }
        }
        if (!CsmKindUtilities.isFriendMethod(item)) {
            if (CsmKindUtilities.isTemplate(item)) {
                final CsmTemplate template = (CsmTemplate)item;
                List<CsmTemplateParameter> templateParameters = template.getTemplateParameters();
                if (templateParameters.size() > 0) {
                    sb.append("template<");//NOI18N
                    boolean first = true;
                    for(CsmTemplateParameter param : templateParameters) {
                        if (!first) {
                            sb.append(", "); //NOI18N
                        }
                        first = false;
                        sb.append(param.getText());
                    }
                    sb.append(">\n");//NOI18N
                }
            }
        }
    }
    
    private static void addSignature(CsmFunction item, StringBuilder sb) {
        //sb.append(item.getSignature());
        sb.append(item.getName());
        //sb.append(parameterList.getText());
        sb.append('('); //NOI18N
        boolean first = true;
        for(CsmParameter param : ((CsmFunction)item).getParameterList().getParameters()) {
            if (!first) {
               sb.append(','); //NOI18N
               sb.append(' '); //NOI18N
            }
            first = false;
            sb.append(param.getDisplayText());
        }
        sb.append(')'); //NOI18N
        if(CsmKindUtilities.isMethod(item) && ((CsmMethod)item).isConst()) {
            sb.append(" const"); // NOI18N
        }
    }
    
    public String getItemText() {
        return appendItemText;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            int caretOffset = component.getSelectionEnd();
            substituteText(component, substitutionOffset, caretOffset - substitutionOffset);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            JTextComponent component = (JTextComponent) evt.getSource();
            int caretOffset = component.getSelectionEnd();
            final int len = caretOffset - substitutionOffset;
            if (len < 0) {
                Completion.get().hideDocumentation();
                Completion.get().hideCompletion();
            }
        }
    }

    @Override
    public boolean instantSubstitution(JTextComponent component) {
        if (supportInstantSubst) {
            defaultAction(component);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public CompletionTask createDocumentationTask() {
        CsmDocProvider p = Lookup.getDefault().lookup(CsmDocProvider.class);
        if (p != null) {
            return p.createDocumentationTask(item);
        }
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(true), getRightHtmlText(true), g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(true), getRightHtmlText(true), g, defaultFont, defaultColor, width, height, selected);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(this.getLeftHtmlText(false));
        out.append(this.getRightHtmlText(false)); 
        return out.toString();
    }

    @Override
    public int getSortPriority() {
        return this.priority;
    }

    @Override
    public CharSequence getSortText() {
        return sortItemText;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return sortItemText;
    }

    protected ImageIcon getIcon() {
        return icon;
    }

    protected String getLeftHtmlText(boolean html) {
        return html ? htmlItemText : getItemText();
    }

    protected String getRightHtmlText(boolean html) {
        return right;
    }

    protected void substituteText(final JTextComponent c, final int offset, final int origLen) {
        final BaseDocument doc = (BaseDocument) c.getDocument();
        doc.runAtomicAsUser(new Runnable() {
            @Override
            public void run() {
                try {
                    if (origLen > 0) {
                        doc.remove(offset, origLen);
                    }
                    String itemText = getItemText();
                    doc.insertString(offset, itemText, null);
                    if (c != null) {
                        if (isExtractBody) {
                            int setDot = offset;
                            c.setCaretPosition(setDot);
                        } else {
                            int setDot = offset + itemText.length() - 3;
                            c.setCaretPosition(setDot);
                        }
                        Reformat reformat = Reformat.get(doc);
                        reformat.lock();
                        try {
                            reformat.reformat(offset+1, offset + itemText.length() - 1);
                        } finally {
                            reformat.unlock();
                        }
                    }
                } catch (BadLocationException e) {
                    // Can't update
                }
            }
        });
        if (isExtractBody) {
            CsmFile containingFile = item.getContainingFile();
            Document document = CsmUtilities.getDocument(containingFile);
            if (document == null) {
                CloneableEditorSupport support = CsmUtilities.findCloneableEditorSupport(containingFile);
                try {
                    document = support.openDocument();
                } catch (IOException ex) {
                }
            }
            if (document instanceof BaseDocument) {
                final BaseDocument classDoc = (BaseDocument) document;
                classDoc.runAtomicAsUser(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            classDoc.remove(startReplacement, lengthReplacement);
                            classDoc.insertString(startReplacement, ";", null); // NOI18N
                        } catch (BadLocationException e) {
                            // Can't update
                        }
                    }
                });
            }
        }
    }
}
