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

package org.netbeans.modules.csl.editor.completion;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.net.URL;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.csl.core.GsfHtmlFormatter;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;


/**
 * Code completion items originating from the language plugin.
 *
 * Based on JavaCompletionItem by Dusan Balek.
 *
 * @author Tor Norbye
 */
public abstract class GsfCompletionItem implements CompletionItem {
    private static CompletionFormatter FORMATTER = new CompletionFormatter();

    /** Cache for looking up tip proposal - usually null (shortlived) */
    static org.netbeans.modules.csl.api.CompletionProposal tipProposal;

    protected ParserResult info;
    protected CodeCompletionResult completionResult;

    protected static int SMART_TYPE = 1000;

    private static class DelegatedItem extends GsfCompletionItem {
        private org.netbeans.modules.csl.api.CompletionProposal item;
        private Integer spCache = null;
        private CharSequence stCache = null;
        //private static ImageIcon iconCache[][] = new ImageIcon[2][4];

        private DelegatedItem(ParserResult info,
                CodeCompletionResult completionResult,
                org.netbeans.modules.csl.api.CompletionProposal item) {
            super(item.getAnchorOffset());
            this.item = item;
            this.completionResult = completionResult;
            this.info = info;
        }

        @Override
        public void defaultAction(JTextComponent component) {
            if(item instanceof DefaultCompletionProposal) {
                boolean cancel = ((DefaultCompletionProposal)item).beforeDefaultAction();
                if(cancel) {
                    return ; //do not invoke the default action
                }
            }
            super.defaultAction(component);
        }

        public int getSortPriority() {
            if (spCache == null) {
                if (item.getSortPrioOverride() != 0) {
                    spCache = item.getSortPrioOverride();
                } else {
                    switch (item.getKind()) {
                    case ERROR: spCache = -5000;
                        break;
                    case DB: spCache = item.isSmart() ? 155 - SMART_TYPE : 155;
                        break;
                    case PARAMETER: spCache = item.isSmart() ? 105 - SMART_TYPE : 105;
                        break;
                    case CALL: spCache = item.isSmart() ? 110 - SMART_TYPE : 110;
                        break;
                    case CONSTRUCTOR: spCache = item.isSmart() ? 400 - SMART_TYPE : 400;
                        break;
                    case PACKAGE:
                    case MODULE: spCache = item.isSmart() ? 640 - SMART_TYPE : 640;
                        break;
                    case CLASS:
                    case INTERFACE: spCache = item.isSmart() ? 620 - SMART_TYPE : 620;
                        break;
                    case ATTRIBUTE:
                    case RULE: spCache = item.isSmart() ? 482 - SMART_TYPE : 482;
                        break;
                    case TAG: spCache = item.isSmart() ? 480 - SMART_TYPE : 480;
                        break;
                    case TEST:
                    case PROPERTY:
                    case METHOD: spCache = item.isSmart() ? 500 - SMART_TYPE : 500;
                        break;
                    case FIELD: spCache = item.isSmart() ? 300 - SMART_TYPE : 300;
                        break;
                    case CONSTANT:
                    case GLOBAL:
                    case VARIABLE: spCache = item.isSmart() ? 200 - SMART_TYPE : 200;
                        break;
                    case KEYWORD: spCache = item.isSmart() ? 600 - SMART_TYPE : 600;
                        break;
                    case OTHER:
                    default:
                        spCache = item.isSmart() ? 999 - SMART_TYPE : 999;
                    }
                }
            }
            return spCache;
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            ElementKind kind = item.getKind();
            if (kind == ElementKind.PARAMETER || kind == ElementKind.CLASS || kind == ElementKind.MODULE) {
                // These types of elements aren't ever instant substituted in Java - use same behavior here
                return false;
            }

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

        public CharSequence getSortText() {
            if (stCache == null) {
                stCache = item.getSortText();
                if (stCache == null) {
                    stCache = ""; //NOI18N
                }
            }
            return stCache;
        }

        public CharSequence getInsertPrefix() {
            return item.getInsertPrefix();
        }

        @Override
        protected String getLeftHtmlText() {
            FORMATTER.reset();
            return item.getLhsHtml(FORMATTER);
        }

        @Override
        public String toString() {
            return item.getName();
        }

        @Override
        protected String getRightHtmlText() {
            FORMATTER.reset();
            String rhs = item.getRhsHtml(FORMATTER);

            // Count text length on LHS
            FORMATTER.reset();
            String lhs = item.getLhsHtml(FORMATTER);
            boolean inTag = false;
            int length = 0;
            for (int i = 0, n = lhs.length(); i < n; i++) {
                char c = lhs.charAt(i);
                if (inTag) {
                    if (c == '>') {
                        inTag = false;
                    }
                } else if (c == '<') {
                    inTag = true;
                } else {
                    length++;
                }
            }

            return truncateRhs(rhs, length);
        }

        @Override
        public CompletionTask createDocumentationTask() {
            final ElementHandle element = item.getElement();
            if (element != null) {
                return GsfCompletionProvider.createDocTask(element,info);
            }

            return null;
        }

        protected ImageIcon getIcon() {
            ImageIcon ic = item.getIcon();
            if (ic != null) {
                return ic;
            }

            ImageIcon imageIcon = org.netbeans.modules.csl.navigation.Icons.getElementIcon(item.getKind(), item.getModifiers());
            // TODO - cache!
            return imageIcon;
//
//
//            ElementKind kind = item.getKind();
//            switch (kind) {
//            case CONSTRUCTOR:
//            case METHOD:
//                return getMethodIcon();
//            case ATTRIBUTE:
//            case FIELD:
//                return getFieldIcon();
//            case CLASS:
//            case INTERFACE:
//                return getClassIcon();
//            case MODULE:
//                return getModuleIcon();
//            case CONSTANT:
//                return getConstantIcon();
//            case VARIABLE:
//                return getVariableIcon();
//            case KEYWORD:
//                return getKeywordIcon();
//            case OTHER:
//            }
//
//            return null;
        }

//        protected ImageIcon getMethodIcon() {
//            Set<Modifier> modifiers = item.getModifiers();
//
//            boolean isStatic = modifiers.contains(Modifier.STATIC);
////            int level = getProtectionLevel(elem.getModifiers());
//
//            ImageIcon cachedIcon = icon[isStatic?1:0][level];
//            if (cachedIcon != null) {
//                return cachedIcon;
//            }
//
//            String iconPath = METHOD_PUBLIC;
//            if (isStatic) {
//                switch (level) {
//                    case PRIVATE_LEVEL:
//                        iconPath = METHOD_ST_PRIVATE;
//                        break;
//
//                    case PACKAGE_LEVEL:
//                        iconPath = METHOD_ST_PACKAGE;
//                        break;
//
//                    case PROTECTED_LEVEL:
//                        iconPath = METHOD_ST_PROTECTED;
//                        break;
//
//                    case PUBLIC_LEVEL:
//                        iconPath = METHOD_ST_PUBLIC;
//                        break;
//                }
//            }else{
//                switch (level) {
//                    case PRIVATE_LEVEL:
//                        iconPath = METHOD_PRIVATE;
//                        break;
//
//                    case PACKAGE_LEVEL:
//                        iconPath = METHOD_PACKAGE;
//                        break;
//
//                    case PROTECTED_LEVEL:
//                        iconPath = METHOD_PROTECTED;
//                        break;
//
//                    case PUBLIC_LEVEL:
//                        iconPath = METHOD_PUBLIC;
//                        break;
//                }
//            }
//            ImageIcon newIcon = new ImageIcon(org.openide.util.Utilities.loadImage(iconPath));
//            icon[isStatic?1:0][level] = newIcon;
//            return newIcon;
//        }


        @Override
        protected void substituteText(final JTextComponent c, int offset, int len, String toAdd) {
            if (completionResult != null) {
                completionResult.beforeInsert(item);

                if (!completionResult.insert(item)) {
                    defaultSubstituteText(c, offset, len, toAdd);
                }

                completionResult.afterInsert(item);
            } else {
                defaultSubstituteText(c, offset, len, toAdd);
            }
        }

        private void defaultSubstituteText(final JTextComponent c, final int offset, final int len, String toAdd) {
            final String template = item.getCustomInsertTemplate();
            if (template != null) {
                final BaseDocument doc = (BaseDocument)c.getDocument();
                final CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                if (ctm != null) {
                    doc.runAtomic(new Runnable() {
                        public void run() {
                            try {
                                doc.remove(offset, len);
                                c.getCaret().setDot(offset);
                            } catch (BadLocationException e) {
                                // Can't update
                            }

                            // SHOULD be run here:
                            //ctm.createTemporary(template).insert(c);
                            // (see issue 147494) but running code template inserts
                            // under a document writelock is risky (see issue 147657)
                        }
                    });
                    ctm.createTemporary(template).insert(c);

                    // TODO - set the actual method to be used here so I don't have to
                    // work quite as hard...
                    //tipProposal = item;
                    Completion.get().showToolTip();
                }

                return;
            }

            super.substituteText(c, offset, len, toAdd);
        }
    }

    public static final GsfCompletionItem createItem(CompletionProposal proposal, CodeCompletionResult result, ParserResult info) {
        return new DelegatedItem(info, result, proposal);
    }

    public static final GsfCompletionItem createTruncationItem() {
        return new TruncationItem();
    }

    /**
     * Special code completion item used to show truncated completion results
     * along with a description.
     */
    private static class TruncationItem extends GsfCompletionItem implements CompletionTask, CompletionDocumentation {

        private TruncationItem() {
            super(0);
        }
        @Override
        protected ImageIcon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/csl/editor/completion/warning.png", false); // NOI18N
        }

        public int getSortPriority() {
            // Sort to the bottom!
            //return Integer.MAX_VALUE;
            // Sort it to the top
            return -20000;
        }

        public CharSequence getSortText() {
            return ""; // NOI18N
        }

        public CharSequence getInsertPrefix() {
            // Length 0 - won't be inserted
            return ""; // NOI18N
        }

        @Override
        protected String getLeftHtmlText() {
            return "<b>" + NbBundle.getMessage(GsfCompletionItem.class, "ListTruncated") + "</b>"; // NOI18N
        }

        @Override
        public CompletionTask createDocumentationTask() {
            return this;
        }

        // Implements CompletionTask

        public void query(CompletionResultSet resultSet) {
            resultSet.setDocumentation(this);
            resultSet.finish();
        }

        public void refresh(CompletionResultSet resultSet) {
            resultSet.setDocumentation(this);
            resultSet.finish();
        }

        public void cancel() {
        }

        // Implements CompletionDocumentation

        public String getText() {
            return NbBundle.getMessage(GsfCompletionItem.class, "TruncatedHelpHtml"); // NOI18N
        }

        public URL getURL() {
            //throw new UnsupportedOperationException("Not supported yet.");
            return null;
        }

        public CompletionDocumentation resolveLink(String link) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Action getGotoSourceAction() {
            //throw new UnsupportedOperationException("Not supported yet.");
            return null;
        }
    }

    public static final String COLOR_END = "</font>"; //NOI18N
    public static final String STRIKE = "<s>"; //NOI18N
    public static final String STRIKE_END = "</s>"; //NOI18N
    public static final String BOLD = "<b>"; //NOI18N
    public static final String BOLD_END = "</b>"; //NOI18N

    protected int substitutionOffset;

    private GsfCompletionItem(int substitutionOffset) {
        this.substitutionOffset = substitutionOffset;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            // Items with no insert prefix and no custom code template
            // are "read-only" (such as the method call items)
            if (getInsertPrefix().length() == 0) {
                return;
            }
            Completion.get().hideAll();
            int caretOffset = component.getSelectionEnd();
            if (caretOffset >= substitutionOffset) {
                substituteText(component, substitutionOffset, caretOffset - substitutionOffset, null);
            }
        }
    }

    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            switch (evt.getKeyChar()) {
                case ';':
                case ',':
                case '(':
                case '.':
                case '\n':
                    Completion.get().hideAll();
//                case '.':
//                    JTextComponent component = (JTextComponent)evt.getSource();
//                    int caretOffset = component.getSelectionEnd();
//                    substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
//                    evt.consume();
//                    break;
            }
        }
    }

    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }

    public CompletionTask createDocumentationTask() {
        return null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    protected abstract ImageIcon getIcon();

    protected String getLeftHtmlText() {
        return null;
    }

    protected String getRightHtmlText() {
        return null;
    }

    protected void substituteText(JTextComponent c, final int offset, final int len, String toAdd) {
        final BaseDocument doc = (BaseDocument)c.getDocument();
        final String text = getInsertPrefix().toString();
        if (text != null) {
            //int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
            //if (semiPos > -2)
            //    toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
            //if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
            //    TokenSequence<JavaTokenId> sequence = Utilities.getJavaTokenSequence(c, offset + len);
            //    if (sequence == null) {
            //        text += toAdd;
            //        toAdd = null;
            //    }
            //    boolean added = false;
            //    while(toAdd != null && toAdd.length() > 0) {
            //        String tokenText = sequence.token().text().toString();
            //        if (tokenText.startsWith(toAdd)) {
            //            len = sequence.offset() - offset + toAdd.length();
            //            text += toAdd;
            //            toAdd = null;
            //        } else if (toAdd.startsWith(tokenText)) {
            //            sequence.moveNext();
            //            len = sequence.offset() - offset;
            //            text += toAdd.substring(0, tokenText.length());
            //            toAdd = toAdd.substring(tokenText.length());
            //            added = true;
            //        } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
            //            if (!sequence.moveNext()) {
            //                text += toAdd;
            //                toAdd = null;
            //            }
            //        } else {
            //            if (!added)
            //                text += toAdd;
            //            toAdd = null;
            //        }
            //    }
            //}

            //  Update the text
            doc.runAtomic(new Runnable() {
                public void run() {
                    try {
                        int semiPos = -2;
                        String textToReplace = doc.getText(offset, len);
                        if (text.equals(textToReplace)) {
                            if (semiPos > -1) {
                                doc.insertString(semiPos, ";", null); //NOI18N
                            }
                            return;
                        }
                        int common = 0;
                        while (text.regionMatches(0, textToReplace, 0, ++common));
                        common--;
                        Position position = doc.createPosition(offset + common);
                        Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                        doc.remove(offset + common, len - common);
                        doc.insertString(position.getOffset(), text.substring(common), null);
                        if (semiPosition != null) {
                            doc.insertString(semiPosition.getOffset(), ";", null);
                        }
                    } catch (BadLocationException e) {
                        // Can't update
                    }
                }
            });
        }
    }

    private static String truncateRhs(String rhs, int left) {
        if (rhs != null) {
            final int MAX_SIZE = 80;
            int size = MAX_SIZE-left;
            if (size < 10) {
                size = 10;
            }
            if (rhs != null && rhs.length() > size) {
                rhs = rhs.substring(0,size-3) + "<b>&gt;</b>";  // Add a ">" to indicate truncation
            }
        }
        return rhs;
    }


    // TODO: KeywordItem has a postfix:
//    private static class KeywordItem extends GsfCompletionItem {
//        private String postfix;
//        private KeywordItem(ComKeyword keyword, String postfix, int substitutionOffset) {
//            this.postfix = postfix;
//        }
//        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
//            super.substituteText(c, offset, len, toAdd != null ? toAdd : postfix);
//        }



//    private static final int PUBLIC_LEVEL = 3;
//    private static final int PROTECTED_LEVEL = 2;
//    private static final int PACKAGE_LEVEL = 1;
//    private static final int PRIVATE_LEVEL = 0;
//
//    private static int getProtectionLevel(Set<Modifier> modifiers) {
//        if(modifiers.contains(Modifier.PUBLIC))
//            return PUBLIC_LEVEL;
//        if(modifiers.contains(Modifier.PROTECTED))
//            return PROTECTED_LEVEL;
//        if(modifiers.contains(Modifier.PRIVATE))
//            return PRIVATE_LEVEL;
//        return PACKAGE_LEVEL;
//    }


    /** Format parameters in orange etc. */
    private static class CompletionFormatter extends GsfHtmlFormatter {
        private static final String METHOD_COLOR = LFCustoms.getTextFgColorHTML();
        private static final String PARAMETER_NAME_COLOR = getHTMLColor(224, 160, 65);
        private static final String END_COLOR = "</font>"; // NOI18N
        private static final String CLASS_COLOR = getHTMLColor(150, 64, 64);
        private static final String PKG_COLOR = getHTMLColor(192, 192, 192);
        private static final String KEYWORD_COLOR = getHTMLColor(64, 64, 217);
        private static final String FIELD_COLOR = getHTMLColor(64, 198, 88);
        private static final String VARIABLE_COLOR = getHTMLColor(64, 64, 188);
        private static final String CONSTRUCTOR_COLOR = getHTMLColor(242, 203, 64);
        private static final String INTERFACE_COLOR = getHTMLColor(128, 128, 128);
        private static final String PARAMETERS_COLOR = getHTMLColor(192, 192, 192);
        private static final String ACTIVE_PARAMETER_COLOR = LFCustoms.getTextFgColorHTML();

        @Override
        public void parameters(boolean start) {
            assert start != isParameter;
            isParameter = start;

            if (isParameter) {
                sb.append(PARAMETER_NAME_COLOR);
            } else {
                sb.append(END_COLOR);
            }
        }

        @Override
        public void active(boolean start) {
            if (start) {
                sb.append(ACTIVE_PARAMETER_COLOR);
                sb.append("<b>");
            } else {
                sb.append("</b>");
                sb.append(END_COLOR);
            }
        }

        @Override
        public void name(ElementKind kind, boolean start) {
            assert start != isName;
            isName = start;

            if (isName) {
                switch (kind) {
                case CONSTRUCTOR:
                    sb.append(CONSTRUCTOR_COLOR);
                    break;
                case CALL:
                    sb.append(PARAMETERS_COLOR);
                    break;
                case DB:
                case METHOD:
                    sb.append(METHOD_COLOR);
                     break;
                case CLASS:
                case INTERFACE:
                    sb.append(CLASS_COLOR);
                    break;
                case FIELD:
                    sb.append(FIELD_COLOR);
                    break;
                case MODULE:
                    sb.append(PKG_COLOR);
                    break;
                case KEYWORD:
                    sb.append(KEYWORD_COLOR);
                    sb.append("<b>");
                    break;
                case VARIABLE:
                    sb.append(VARIABLE_COLOR);
                    sb.append("<b>");
                    break;
                default:
                    sb.append(LFCustoms.getTextFgColorHTML());
                }
            } else {
                switch (kind) {
                case KEYWORD:
                case VARIABLE:
                    sb.append("</b>");
                    break;
                }
                sb.append(END_COLOR);
            }
        }

    }

    private static String getHTMLColor(int r, int g, int b) {
        Color c = LFCustoms.shiftColor(new Color(r, g, b));
        return "<font color=#" //NOI18N
                + LFCustoms.getHexString(c.getRed())
                + LFCustoms.getHexString(c.getGreen())
                + LFCustoms.getHexString(c.getBlue())
                + ">"; //NOI18N
    }
}
