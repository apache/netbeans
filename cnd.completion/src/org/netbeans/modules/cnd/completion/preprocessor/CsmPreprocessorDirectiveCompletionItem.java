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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.preprocessor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;

/**
 *
 *
 */
public class CsmPreprocessorDirectiveCompletionItem implements CompletionItem {

    private final int substitutionOffset;
    private final int priority;
    private final String sortItemText;
    private final boolean supportInstantSubst;
    private static final int PRIORITY = 15;
    private final String appendItemText;
    private final String htmlItemText;

    private CsmPreprocessorDirectiveCompletionItem(int substitutionOffset, int priority,
            String sortItemText, String appendItemText, String htmlItemText, boolean supportInstantSubst) {
        this.substitutionOffset = substitutionOffset;
        this.priority = priority;
        this.supportInstantSubst = supportInstantSubst;
        this.sortItemText = sortItemText;
        this.appendItemText = appendItemText;
        this.htmlItemText = htmlItemText;
    }

    public static CsmPreprocessorDirectiveCompletionItem createItem(int substitutionOffset, int priority, String item) {
        String appendItemText;
        String coloredItemText;
        String sortItemText;
        int newLine = item.indexOf("\n");//NOI18N
        if (newLine > 0) {
            sortItemText = item.substring(0, newLine).replace("#", ""); // NOI18N
            String noNewLine = item.replace("\n", "-").replace("#", ""); // NOI18N
            appendItemText = item.substring(newLine);
            if (CndUtils.isUnitTestMode()) {
                coloredItemText = noNewLine;
            } else {
                //coloredItemText = CsmDisplayUtilities.addHTMLColor(noNewLine, CsmFontColorManager.instance().getColorAttributes(MIMENames.CPLUSPLUS_MIME_TYPE, FontColorProvider.Entity.PREPROCESSOR_DIRECTIVE));
                coloredItemText = CsmDisplayUtilities.addHTMLColor(noNewLine, new Color(79,155,27), true);
            }
        } else {
            appendItemText = "";
            sortItemText = item;
            if (CndUtils.isUnitTestMode()) {
                coloredItemText = sortItemText;
            } else {
                //coloredItemText = CsmDisplayUtilities.addHTMLColor(sortItemText, CsmFontColorManager.instance().getColorAttributes(MIMENames.CPLUSPLUS_MIME_TYPE, FontColorProvider.Entity.PREPROCESSOR_DIRECTIVE));
                coloredItemText = CsmDisplayUtilities.addHTMLColor(sortItemText, new Color(79,155,27), true);
            }
        }
        return new CsmPreprocessorDirectiveCompletionItem(substitutionOffset, PRIORITY, sortItemText, appendItemText, coloredItemText, true);
    }

    public String getItemText() {
        return sortItemText + appendItemText;
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
        return CsmImageLoader.getPreprocessorDirectiveIcon();
    }

    protected String getLeftHtmlText(boolean html) {
        return html ? htmlItemText : getItemText();
    }

    protected String getRightHtmlText(boolean html) {
        return "";
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
                        int setDot = offset + getInsertPrefix().length();
                        c.setCaretPosition(setDot);
                        if (appendItemText.length() > 0) {
                            Indent indent = Indent.get(doc);
                            indent.lock();
                            try {
                                indent.reindent(offset, offset + itemText.length());
                            } finally {
                                indent.unlock();
                            }
                        }
                    }
                } catch (BadLocationException e) {
                    // Can't update
                }
            }
        });
    }
}
