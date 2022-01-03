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

package org.netbeans.modules.cnd.refactoring.completion.overridemethod;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmDocProvider;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class CsmOverrideMethodCompletionItem implements CompletionItem {

    private final int substitutionOffset;
    private final int priority;
    private final String sortItemText;
    private final boolean supportInstantSubst;
    private static final int PRIORITY = 15;
    private final String appendItemText;
    private final String htmlItemText;
    private final CsmMember item;
    private final ImageIcon icon;
    private final String right;

    private CsmOverrideMethodCompletionItem(CsmMember item, int substitutionOffset, int priority,
            String sortItemText, String appendItemText, String htmlItemText, boolean supportInstantSubst, String right) {
        this.substitutionOffset = substitutionOffset;
        this.priority = priority;
        this.supportInstantSubst = supportInstantSubst;
        this.sortItemText = sortItemText;
        this.appendItemText = appendItemText;
        this.htmlItemText = htmlItemText;
        this.item = item;
        ImageIcon anIicon;
        if (item != null) {
            anIicon = CsmImageLoader.getIcon(item);
        } else {
            anIicon = CsmImageLoader.getIcon(CsmDeclaration.Kind.FUNCTION, CsmUtilities.DESTRUCTOR);
        }
        icon = (ImageIcon) ImageUtilities.image2Icon((ImageUtilities.mergeImages(ImageUtilities.icon2Image(anIicon),
                                                      ImageUtilities.loadImage("org/netbeans/modules/cnd/refactoring/resources/generate.png"),  // NOI18N
                                                      0, 7)));
        this.right = right;
    }

    public static CsmOverrideMethodCompletionItem createImplementItem(int substitutionOffset, int caretOffset, CsmClass cls, CsmMember item) {
        String sortItemText;
        if (item == null || CsmKindUtilities.isDestructor(item)) {
            sortItemText = "~"+cls.getName(); //NOI18N
        } else {
            sortItemText = item.getName().toString();
        }
        String appendItemText = createAppendText(item, cls);
        String rightText = createRightName(item);
        String coloredItemText;
        int priority = PRIORITY;
        if (item == null || CsmKindUtilities.isDestructor(item)) {
            coloredItemText = createDisplayName(item, cls, NbBundle.getMessage(CsmOverrideMethodCompletionItem.class, "destructor.txt")); //NOI18N
        } else {
            if (CsmKindUtilities.isMethod(item) && ((CsmMethod)item).isAbstract()) {
                coloredItemText = createDisplayName(item, cls, NbBundle.getMessage(CsmOverrideMethodCompletionItem.class, "implement.txt")); //NOI18N
                priority--;
            } else {
                coloredItemText = createDisplayName(item, cls, NbBundle.getMessage(CsmOverrideMethodCompletionItem.class, "override.txt")); //NOI18N
            }
        }
        return new CsmOverrideMethodCompletionItem(item, substitutionOffset, priority, sortItemText, appendItemText, coloredItemText, true, rightText);
    }

    private static String createDisplayName(CsmMember item, CsmClass parent, String operation) {
        StringBuilder displayName = new StringBuilder();
        displayName.append("<b>"); //NOI18N
        if (item == null || CsmKindUtilities.isDestructor(item)) {
            displayName.append('~');
            displayName.append(CsmDisplayUtilities.htmlize(parent.getName()));
            displayName.append("()"); //NOI18N
        } else {
            displayName.append(CsmDisplayUtilities.htmlize(((CsmFunction)item).getSignature()));
        }
        displayName.append("</b>"); //NOI18N
        if (operation != null) {
            displayName.append(" - "); //NOI18N
            displayName.append(CsmDisplayUtilities.htmlize(operation));
        }
        return displayName.toString();
    }
    
    private static String createRightName(CsmMember item) {
        if (item == null || CsmKindUtilities.isDestructor(item)) {
            return "";
        } else if (CsmKindUtilities.isConstructor(item)) {
            return "";
        } else {
            return ((CsmFunction)item).getReturnType().getText().toString();
        }
    }
    
    private static String createAppendText(CsmMember item, CsmClass parent) {
        boolean isCpp11 = CsmFileInfoQuery.getDefault().isCpp11OrLater(parent.getContainingFile());
        StringBuilder appendItemText = new StringBuilder();
        String type = "";
        if (item != null && !CsmKindUtilities.isConstructor(item) && !CsmKindUtilities.isDestructor(item)) {
            final CsmType returnType = ((CsmFunction)item).getReturnType();
            type = returnType.getText().toString()+" "; //NOI18N
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
        if (!isCpp11) {
            appendItemText.append("\nvirtual "); //NOI18N
        } else {
            if (item == null || CsmKindUtilities.isDestructor(item)) {
                appendItemText.append("\nvirtual "); //NOI18N
            }
        }
        appendItemText.append(type);
        addSignature(item, parent, appendItemText);
        if (isCpp11 && item != null && !CsmKindUtilities.isConstructor(item) && !CsmKindUtilities.isDestructor(item)) {
            appendItemText.append(" override"); //NOI18N
        }
        if (item == null || CsmKindUtilities.isDestructor(item)) {
            appendItemText.append(" {\n\n}\n"); //NOI18N
        } else {
            appendItemText.append(";\n"); //NOI18N
        }
        return appendItemText.toString();
    }
    
    private static void addSignature(CsmMember item, CsmClass parent, StringBuilder sb) {
        //sb.append(item.getSignature());
        if (item == null || CsmKindUtilities.isDestructor(item)) {
            sb.append('~');
            sb.append(parent.getName());
            sb.append("()"); //NOI18N
        } else {
            sb.append(item.getName());
            if (CsmKindUtilities.isTemplate(item)) {
                List<CsmTemplateParameter> templateParameters = ((CsmTemplate)item).getTemplateParameters();
                // What to do with template?
            }
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
                        int setDot;
                        if (item == null || CsmKindUtilities.isDestructor(item)) {
                            setDot = offset + itemText.length() - 3;
                        } else {
                            setDot = offset + itemText.length() - 1;
                        }
                        c.setCaretPosition(setDot);
                        Reformat reformat = Reformat.get(doc);
                        reformat.lock();
                        try {
                            reformat.reformat(offset, offset + itemText.length() - 1);
                        } finally {
                            reformat.unlock();
                        }
                    }
                } catch (BadLocationException e) {
                    // Can't update
                }
            }
        });
    }
}
