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
package org.netbeans.modules.cnd.completion.includes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;

/**
 *
 *159170
 */
public class CsmIncludeCompletionItem implements CompletionItem {

    protected final static String QUOTE = "\""; // NOI18N
    protected final static String SYS_OPEN = "<"; // NOI18N
    protected final static String SYS_CLOSE = ">"; // NOI18N
    protected final static String SLASH = "/"; // NOI18N
    protected final static String PARENT_COLOR_TAG = "<font color=\"#557755\">"; // NOI18N
    private final int substitutionOffset;
    private final int substitutionOffsetDelta;
    private final int priority;
    private final String item;
    private final String parentFolder;
    private final String childSubdir;
    private final boolean isSysInclude;
    private final boolean isFolder;
    private final boolean supportInstantSubst;
    private static final int FOLDER_PRIORITY = 30;
    private static final int FILE_PRIORITY = 10;
    private static final int SYS_VS_USR = 5;

    protected CsmIncludeCompletionItem(int substitutionOffset, int substitutionDelta, int priority,
            String parentFolder, String childSubdir, String item,
            boolean sysInclude, boolean isFolder,
            boolean supportInstantSubst) {
        this.substitutionOffset = substitutionOffset;
        this.substitutionOffsetDelta = substitutionDelta;
        this.priority = priority;
        this.parentFolder = parentFolder == null ? "" : parentFolder;
        this.childSubdir = childSubdir == null ? "" : childSubdir;
        this.isSysInclude = sysInclude;
        this.isFolder = isFolder;
        assert item != null;
        this.item = item;
        this.supportInstantSubst = supportInstantSubst;
    }

    public static CsmIncludeCompletionItem createItem(int substitutionOffset, 
            int substitutionDelta,
            String relFileName,
            String dirPrefix, String childSubdir,
            boolean sysInclude,
            boolean highPriority,
            boolean isFolder,
            boolean supportInstantSubst) {
        int priority;
        if (isFolder) {
            if (highPriority) {
                priority = FOLDER_PRIORITY - SYS_VS_USR;
            } else {
                priority = FOLDER_PRIORITY + SYS_VS_USR;
            }
        } else {
            if (highPriority) {
                priority = FILE_PRIORITY - SYS_VS_USR;
            } else {
                priority = FILE_PRIORITY + SYS_VS_USR;
            }
        }
        String item = relFileName;
        return new CsmIncludeCompletionItem(substitutionOffset, substitutionDelta, priority,
                dirPrefix, childSubdir, item, sysInclude, isFolder, supportInstantSubst);
    }

    public String getItemText() {
        return item;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            Completion.get().hideDocumentation();
            boolean folder = this.isFolder();
            if (!folder) {
                Completion.get().hideCompletion();
            }
            int caretOffset = component.getSelectionEnd();
            substituteText(component, substitutionOffset, caretOffset - substitutionOffset, isFolder() ? SLASH : null);
            if (folder) {
                Completion.get().showCompletion();
            }
        }
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            JTextComponent component = (JTextComponent) evt.getSource();
            final BaseDocument doc = (BaseDocument) component.getDocument();
            int caretOffset = component.getSelectionEnd();
            final int len = caretOffset - substitutionOffset;
            if (len < 0) {
                Completion.get().hideDocumentation();
                Completion.get().hideCompletion();
            }
            switch (evt.getKeyChar()) {
                case '>':
                    Completion.get().hideDocumentation();
                    Completion.get().hideCompletion();
                    break;
                case '"':
                    doc.readLock();
                    boolean hide = false;
                    try {
                        if (len > 0) {
                            String toReplace = doc.getText(substitutionOffset, len);
                            if (toReplace.startsWith("\"") && len > 1) { // NOI18N
                                hide = true;
                            }
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        doc.readUnlock();
                    }
                    if (hide) {
                        Completion.get().hideDocumentation();
                        Completion.get().hideCompletion();
                    }
                    break;
                case '/':
                    if (len > 1 && isFolder()) {
                        Completion.get().hideDocumentation();
                        Completion.get().hideCompletion();
                        substituteText(component, substitutionOffset, len, SLASH);
                        evt.consume();
                        Completion.get().showCompletion();
                    }
                    break;
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
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(true), getRightText(false, File.separator), g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(true), PARENT_COLOR_TAG + getRightText(true, File.separator), g, defaultFont, defaultColor, width, height, selected);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(this.isFolder() ? "[D] " : "[F] "); // NOI18N
        out.append(this.isSysInclude() ? "<" : "\""); // NOI18N
        out.append(this.getLeftHtmlText(false));
        out.append(this.isSysInclude() ? ">" : "\""); // NOI18N
        out.append(" : "); // NOI18N
        out.append(this.getRightText(false, "/")); // NOI18N
        return out.toString();
    }

    @Override
    public int getSortPriority() {
        return this.priority;
    }

    @Override
    public CharSequence getSortText() {
        return item;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return item;
    }

    protected ImageIcon getIcon() {
        return CsmImageLoader.getIncludeImageIcon(isSysInclude(), isFolder());
    }

    protected String getLeftHtmlText(boolean html) {
        return (html ? (isFolder() ? "<i>" : "") : "") + this.getItemText(); // NOI18N
    }

    protected String getRightText(boolean shrink, String separator) {
        return CsmDisplayUtilities.shrinkPath(this.getParentFolder() + separator + getChildSubdir(), shrink, separator, 35, 2, 2);
    }
    
    protected void substituteText(final JTextComponent c, final int origOffset, final int origLen, final String toAdd) {
        final BaseDocument doc = (BaseDocument) c.getDocument();
        final String itemText = getItemText();
        if (itemText != null) {
            doc.runAtomic(new Runnable() {

                @Override
                public void run() {
                    try {
                        int offset = origOffset + substitutionOffsetDelta;
                        int len = origLen;
                        String text = itemText;
                        if (toAdd != null) {
                            text += toAdd;
                        }
                        TokenItem<TokenId> token = CndTokenUtilities.getToken(doc, offset, true);
                        String pref = QUOTE;
                        String post = QUOTE;
                        if (token != null) {
                            boolean changeLength = false;
                            TokenId id = token.id();
                            if(id instanceof CppTokenId) {
                                switch ((CppTokenId)id) {
                                    case WHITESPACE:
                                    case PREPROCESSOR_IDENTIFIER:
                                        pref = isSysInclude ? SYS_OPEN : QUOTE;
                                        post = isSysInclude ? SYS_CLOSE : QUOTE;
                                        break;
                                    case PREPROCESSOR_USER_INCLUDE:
                                        pref = QUOTE;
                                        post = QUOTE;
                                        changeLength = true;
                                        break;
                                    case PREPROCESSOR_SYS_INCLUDE:
                                        pref = SYS_OPEN;
                                        post = SYS_CLOSE;
                                        changeLength = true;
                                        break;
                                }
                            }
                            if (changeLength) {
                                len = (token.offset() + token.length()) - offset - (token.partType() == PartType.COMPLETE ? 0 : 1);
                            }
                        }
                        // Update the text
                        String parent = getChildSubdir();
                        if (parent.length() > 0 && !parent.endsWith(SLASH)) {
                            parent += SLASH;
                        }
                        text = pref + parent + text + post;
                        Position position = doc.createPosition(offset);
                        Position lastPosition = doc.createPosition(offset + len);
                        doc.remove(offset, len);
                        doc.insertString(position.getOffset(), text, null);
                        if (c != null && isFolder()) {
                            c.setCaretPosition(lastPosition.getOffset() - 1);
                        }
                    } catch (BadLocationException e) {
                        // Can't update
                    }
                }
            });
        }
    }

    protected boolean isFolder() {
        return isFolder;
    }

    protected String getParentFolder() {
        return parentFolder;
    }

    protected String getChildSubdir() {
        return childSubdir;
    }

    protected boolean isSysInclude() {
        return isSysInclude;
    }
}
