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
package org.netbeans.modules.xml.schema.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.*;
import javax.swing.Icon;
import javax.xml.XMLConstants;
import org.netbeans.editor.BaseDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.util.CompletionContextImpl;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.swing.plaf.LFCustoms;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class CompletionResultItem implements CompletionItem {
    private static final Logger _logger = Logger.getLogger(CompletionResultItem.class.getName());

    private static final Color COLOR = LFCustoms.shiftColor(new Color(64, 64, 255));
    
    public static final String
        ICON_ELEMENT    = "element.png",     //NOI18N
        ICON_ATTRIBUTE  = "attribute.png",   //NOI18N
        ICON_VALUE      = "value.png",       //NOI18N
        ICON_LOCATION   = "org/netbeans/modules/xml/schema/completion/resources/"; //NOI18N

    protected boolean shift = false;
    protected String typedChars;
    protected String itemText;
    protected javax.swing.Icon icon;
    protected CompletionPaintComponent component;
    protected AXIComponent axiComponent;
    protected int extraPaintGap = CompletionPaintComponent.DEFAULT_ICON_WIDTH;
    protected TokenSequence tokenSequence;

    protected final CompletionContextImpl context;

    /**
     * Creates a new instance of CompletionUtil
     */
    public CompletionResultItem(AXIComponent component, CompletionContext context) {
        this(component, context, null);
    }

    public CompletionResultItem(AXIComponent component, CompletionContext context,
        TokenSequence tokenSequence) {
        this.context = (CompletionContextImpl) context;
        this.axiComponent = component;
        setTokenSequence(tokenSequence);
        if (context != null) {
            this.typedChars = context.getTypedChars();
        }
    }

    Icon getIcon(){
        return icon;
    }

    public AXIComponent getAXIComponent() {
        return axiComponent;
    }

    /**
     * The completion item's name.
     */
    public String getItemText() {
        return itemText;
    }

    /**
     * The text user sees in the CC list. Normally some additional info
     * such as cardinality etc. are added to the item's name.
     * 
     */
    public abstract String getDisplayText();

    /**
     * Replacement text is the one that gets inserted into the document when
     * user selects this item from the CC list.
     */
    public abstract String getReplacementText();

    /**
     * Returns the relative caret position.
     * The caller must call this w.r.t. the offset
     * e.g. component.setCaretPosition(offset + getCaretPosition())
     */
    public abstract int getCaretPosition();

    @Override
    public String toString() {
        return getItemText();
    }

    Color getPaintColor() { 
        return LFCustoms.shiftColor(COLOR);
    }

    public int getExtraPaintGap() {
        return extraPaintGap;
    }

    public void setExtraPaintGap(int extraPaintGap) {
        this.extraPaintGap = extraPaintGap;
    }

    public TokenSequence getTokenSequence() {
        return tokenSequence;
    }

    public void setTokenSequence(TokenSequence tokenSequence) {
        this.tokenSequence = tokenSequence;
    }
    
    protected int removeTextLength(JTextComponent component, int offset, int removeLength) {
        return removeLength;
    }
    
    protected int caretOffset() {
        return -1;
    }

    /**
     * Prepares values in the item, by inspecting the Component, token sequence etc.
     * The method is called before the text is changed, and is called under document write lock.
     * 
     * @param component the editing component
     * @param proposedText the proposed text
     * @param offset the offset where the completion occurs
     */
    protected void prepare(JTextComponent component, String proposedText, int offset) {
        
    }
    
    /**
     * Actually replaces a piece of document by passes text.
     * @param component a document source
     * @param text a string to be inserted
     * @param offset the target offset
     * @param len a length that should be removed before inserting text
     */
    protected void replaceText(final JTextComponent component, final String text,
        final int offset, final int len) {
        final BaseDocument doc = (BaseDocument) component.getDocument();
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    int caretPos = component.getCaretPosition();
                    if ((context != null) && (context.canReplace(text))) {
                        prepare(component, text, offset);
                        int l2 = removeTextLength(component, offset, len);
                        String insertingText = getInsertingText(component, offset, text, l2);
                        if (l2 > 0) doc.remove(offset, l2);
                        doc.insertString(offset, insertingText, null);
                        // fix for issue #186007
                        caretPos = offset + getCaretPosition();
                        caretPos -= text.length() - insertingText.length();
                    } else {
                        caretPos = offset + getCaretPosition(); // change the caret position
                    }
                    int docLength = doc.getLength();
                    if (docLength == 0) {
                        caretPos = 0;
                    } else if (caretPos > doc.getLength()) {
                        caretPos = doc.getLength();
                    }
                    component.setCaretPosition(caretPos);
                    
                    String prefix = CompletionUtil.getPrefixFromTag(text);
                    if (prefix == null) {
                        return;
                    }
                    //insert namespace declaration for the new prefix
                    if ((context != null) && (! context.isSpecialCompletion()) &&
                        (! context.isPrefixBeingUsed(prefix))) {
                        String tns = context.getTargetNamespaceByPrefix(prefix);
                        
                        // CC has made a suggestion, so materialize it:
                        if (tns == null) {
                            tns = context.getSuggestedNamespace().get(prefix);
                        }
                        
                        if (tns != null) {
                            doc.insertString(CompletionUtil.getNamespaceInsertionOffset(doc), " " +
                                    XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix + "=\"" +
                                    tns + "\"", null);
                        }
                    }
                } catch (Exception e) {
                    _logger.log(Level.SEVERE,
                        e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e);
                }
            }
        });
    }
    
    protected final TokenSequence createTokenSequence(JTextComponent component) {
        if (tokenSequence == null) {
            TokenHierarchy tokenHierarchy = TokenHierarchy.get(component.getDocument());
            this.tokenSequence = tokenHierarchy.tokenSequence();
        }
        return tokenSequence;
    }
    
    private String stripCommonPrefix(String prefix, String replacement, String original) {
        if (replacement.startsWith(prefix) && original.startsWith(prefix)) {
            return replacement.substring(prefix.length());
        } else {
            return replacement;
        }
    }
    
    private void resetTokenSequence() {
        tokenSequence = null;
    }
    
    protected String getInsertingText(JTextComponent component, int textPos, String primaryText, int removeLen) {
        if ((primaryText == null) || (primaryText.length() < 1)) {
            return primaryText;
        }
        createTokenSequence(component);
        if (tokenSequence.move(textPos) == 0) {
            tokenSequence.movePrevious();
        } else {
            tokenSequence.moveNext();
        }
        Token token = tokenSequence.token();
        boolean isTextTag = CompletionUtil.isTextTag(token);

        if (! (isTextTag || CompletionUtil.isEndTagPrefix(token) ||
            CompletionUtil.isTagFirstChar(token))) {
            return primaryText;
        }

        int tokenOffset = tokenSequence.offset();
        if (isTextTag) {
            String tokenText = token.text().toString();
            boolean isCaretAfterTag =
                (tokenText.startsWith(CompletionUtil.END_TAG_PREFIX) &&
                (textPos == tokenOffset + CompletionUtil.END_TAG_PREFIX.length()))
                ||
                (tokenText.startsWith(CompletionUtil.TAG_FIRST_CHAR) &&
                (textPos == tokenOffset + CompletionUtil.TAG_FIRST_CHAR.length()));
            if (! isCaretAfterTag) {
                return primaryText;
            }
        }
        
        String tokenText = token.text().toString();
        if (removeLen > 0) {
            // in the middle of the tag; must return text without starting / end tag
            primaryText = stripCommonPrefix(CompletionUtil.END_TAG_PREFIX, primaryText, tokenText);
            primaryText = stripCommonPrefix(CompletionUtil.TAG_FIRST_CHAR, primaryText, tokenText);
        }        
        if (primaryText.endsWith(CompletionUtil.TAG_LAST_CHAR)) {
            boolean endPresent = false;
            STOP: while (!endPresent && tokenSequence.moveNext()) {
                Token t = tokenSequence.token();
                switch ((XMLTokenId)t.id()) {
                    case WS:
                    case ARGUMENT:
                    case VALUE:
                    case OPERATOR:
                        break;
                    case TAG: {
                        String tt = t.text().toString();
                        if (tt.equals(CompletionUtil.TAG_LAST_CHAR) || tt.equals("/>")) {
                            endPresent = true;
                            break;
                        }
                    }
                    default:
                        break STOP;
                }
            }
            if (endPresent) {
                primaryText = primaryText.substring(0, primaryText.length() -1);
            }
        }

        if ((tokenOffset > -1) && (tokenOffset < textPos)) {
            textPos = tokenOffset;
        }

        boolean isDifferentTextFound = false;
        int i = 0;
        for (; i < primaryText.length(); ++i, ++textPos) {
            try {
                String strDoc  = component.getText(textPos, 1),
                       strText = primaryText.substring(i, i + 1);
                isDifferentTextFound = (! strDoc.equals(strText));
                if (isDifferentTextFound) break;
            } catch(BadLocationException e) {
                _logger.log(Level.WARNING,
                    e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e);
                isDifferentTextFound = true;
            }
        }
        String text = isDifferentTextFound ? primaryText.substring(Math.max(0, i - removeLen)) : "";
        return text;
    }

    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////methods from CompletionItem interface////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    @Override
    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new DocumentationQuery(this));
    }

    @Override
    public CompletionTask createToolTipTask() {
        return new AsyncCompletionTask(new ToolTipQuery(this));
    }

    @Override
    public void defaultAction(JTextComponent component) {
        String selectedText = component.getSelectedText();
        int charsToRemove = selectedText != null ? selectedText.length() :
                            (typedChars == null ? 0 : typedChars.length()),
            substOffset   = selectedText != null ? component.getSelectionStart() :
                            component.getCaretPosition() - charsToRemove;
        if(!shift) Completion.get().hideAll();
        if(getReplacementText().equals(typedChars))
            return;
        replaceText(component, getReplacementText(), substOffset, charsToRemove);
    }

    @Override
    public CharSequence getInsertPrefix() {
        return getItemText();
    }

    public abstract CompletionPaintComponent getPaintComponent();

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        CompletionPaintComponent renderComponent = getPaintComponent();
        return renderComponent.getPreferredSize().width;
    //return getPaintComponent().getWidth(getItemText(), defaultFont);
    }

    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public CharSequence getSortText() {
        return getItemText();
    }

    @Override
    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }

    @Override
    public void processKeyEvent(KeyEvent e) {
        shift = (e.getKeyCode() == KeyEvent.VK_ENTER &&
                 e.getID() == KeyEvent.KEY_PRESSED && e.isShiftDown());
    }

    @Override
    public void render(Graphics g, Font defaultFont,
            Color defaultColor, Color backgroundColor,
            int width, int height, boolean selected) {
        CompletionPaintComponent renderComponent = getPaintComponent();
        renderComponent.setFont(defaultFont);
        renderComponent.setForeground(defaultColor);
        renderComponent.setBackground(backgroundColor);
        renderComponent.setBounds(0, 0, width, height);
        renderComponent.setSelected(selected);
        renderComponent.paintComponent(g);
    }
}