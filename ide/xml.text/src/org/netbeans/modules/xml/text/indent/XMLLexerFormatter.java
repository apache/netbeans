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
package org.netbeans.modules.xml.text.indent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.xml.text.folding.TokenElement;
import org.netbeans.modules.xml.text.folding.TokenElement.TokenType;
import org.netbeans.modules.xml.text.folding.XmlFoldManager;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.AtomicLockDocument;

/**
 * New XML formatter based on Lexer APIs.
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class XMLLexerFormatter {
    
    private static final Logger logger = Logger.getLogger(XMLLexerFormatter.class.getName());

    private static final String SPACE_PRESERVE = "\"preserve\""; // NOI18N
    private static final String SPACE_DEFAULT = "\"default\""; // NOI18N
    private static final String XML_SPACE_ATTRIBUTE = "xml:space"; // NOI18N
    
    private static final int SPACE_PRESERVE_LEN = SPACE_PRESERVE.length();
    private static final int SPACE_DEFAULT_LEN = SPACE_DEFAULT.length();
    private static final int XML_SPACE_ATTRIBUTE_LEN = XML_SPACE_ATTRIBUTE.length();

    private final LanguagePath languagePath;
    private int spacesPerTab = 4;

    public XMLLexerFormatter(LanguagePath languagePath) {
        this.languagePath = languagePath;
    }

    protected LanguagePath supportedLanguagePath() {
        return languagePath;
    }

// # 170343
    public void reformat(Context context, final int startOffset, final int endOffset)
            throws BadLocationException {
        final Document doc = context.document();
        LineDocumentUtils.asRequired(doc, AtomicLockDocument.class)
                         .runAtomic(() -> doReformat((LineDocument)doc, startOffset, endOffset));
    }

    public LineDocument doReformat(LineDocument doc, int startOffset, int endOffset) {
        spacesPerTab = IndentUtils.indentLevelSize(doc);
        try {
            List<TokenIndent> tags = getTags(doc, startOffset, endOffset);
            for (int i = tags.size() - 1; i >= 0; i--) {
                TokenIndent ti = tags.get(i);
                if (ti.isPreserveIndent()) {
                    continue;
                }
                changePrettyText(doc, ti);
            }
        } catch (BadLocationException ble) {
            //ignore exception
        } catch (IOException iox) {
            //ignore exception
        } finally {
            //((AbstractDocument)doc).readUnlock();
        }
        return doc;
    }

    private void changePrettyText(LineDocument doc, TokenIndent tag) throws BadLocationException {
        //i expected the call IndentUtils.createIndentString() to return
        //the correct string for the indent level, but it doesn't.
        //so this is just a workaround.
        int spaces;
        boolean noNewline;
        
        int so = tag.getStartOffset();
        spaces = tag.getIndentLevel();
        noNewline = tag.isNoNewline();
        String newIndentText = IndentUtils.createIndentString(doc, spaces);
        //String newIndentText = formatter.getIndentString(doc, tag.getIndentLevel());
        int previousEndOffset = LineDocumentUtils.getPreviousNonWhitespace(doc, so) + 1;
        CharSequence temp = org.netbeans.lib.editor.util.swing.DocumentUtilities.getText(doc, previousEndOffset, so - previousEndOffset);
        if(noNewline || so == 0 || CharSequences.indexOf(temp, "\n") != -1){ // NOI18N
            int i = LineDocumentUtils.getLineFirstNonWhitespace(doc, so);
            if (i == -1) {
                i = LineDocumentUtils.getLineEnd(doc, so);
            }
            int rowStart = LineDocumentUtils.getLineStart(doc, so);
            
            String currentIndent = doc.getText(rowStart, i - rowStart);
            if (!currentIndent.equals(newIndentText)) {
                // first insert, then remove - less disruption to Positions in the altered text, i.e.
                // Positions at the beginning of token will stick with the token, not with the whitespace start
                // Because comments start at the line start, not at the non-whitespace, adjust insertion point if nonwhite > startOffset
                if (so < i) {
                    so = i;
                }
                doc.insertString(so, newIndentText, null);
                doc.remove(rowStart, i - rowStart);
            }
        }
        else {
            doc.insertString(so, "\n" + newIndentText, null); // NOI18N
        }
    }
    
    private Stack<TokenIndent> stack = new Stack<TokenIndent>();
    
    // flag to indicate if the current
    // argument is xml:space
    private boolean settingSpaceValue = false;

    // flag that is true if whitespace is currently
    // to not be changed.  That is, xml:space
    // was last set to "preserve".
    private boolean preserveWhitespace = false;
    
    /**
     * Indent level for the PARENT of the current token. For tag content or attributes,
     * the indent level is the level of the tag open brace, so +spacesPerTab must be added
     */
    private int indentLevel;
    
    /**
     * Indent for the 1st attribute of the tag. -1, if outside tag or no attributes were
     * found yet. Will be initialized to the indent of the 1st attribute name
     */
    private int firstAttributeIndent;
    
    /**
     * True, if there was only whitespaces from the last seen newline. Applies to both
     * tags and content.
     */
    private boolean wasNewline;

    /**
     * True, if the token sits in formatted range.
     */
    private boolean tokenInSelectionRange;
    
    /**
     * The token sequence being processed
     */
    private TokenSequence<XMLTokenId> tokenSequence;
    
    /**
     * The processed document
     */
    private LineDocument basedoc;

    private void outsideAttributes() {
        firstAttributeIndent = -1;
        settingSpaceValue = false;
    }
    
    /**
     * The indent of the most recent start/end tagname, so that closing > is indented properly,
     * if alone on newline.
     */
    private int tagIndent;
    
    private void startTag(CharSequence image) throws BadLocationException {
        CharSequence tagName = image.subSequence(1, image.length());
        int begin = currentTokensSize;
        int end = begin + image.length();
        updateIndent(true, -1, preserveWhitespace);
        TokenIndent indent = new TokenIndent(tagName, preserveWhitespace, begin, indentLevel);
        tagIndent = indentLevel;
        stack.push(indent);
        if (tokenInSelectionRange) {
            if (wasNewline || onlyTags) {
                tags.add(indent);
            }
        }
        onlyTags = true;
    }
    
    private void tagClose(CharSequence image) {
        if (wasNewline && tokenInSelectionRange) {
            // 1st item on a new line, will indent according to the opening tag
            tags.add(new TokenIndent(false, tokenSequence.offset(), tagIndent));
        }
        // reset
        contentPresent = false;
    }
    
    private int updateIndent(boolean increase, int targetLevel, boolean preserveAfter) {
        if (preserveAfter) {
            return indentLevel;
        }
        int save = this.indentLevel;
        if (tokenInSelectionRange) {
            if (targetLevel != -1) {
                indentLevel = save = targetLevel;
            }
            if (increase) {
                indentLevel += spacesPerTab;
            } else {
                indentLevel = Math.max(- spacesPerTab, indentLevel - spacesPerTab);
            }
            return save;
        } else {
            try {
                // align with the actual tag:
                indentLevel = Utilities.getVisualColumn((LineDocument)basedoc, 
                        LineDocumentUtils.getNextNonWhitespace(basedoc, 
                        LineDocumentUtils.getLineStart(basedoc, tokenSequence.offset())));
                if (!increase) {
                    indentLevel = Math.max(- spacesPerTab, indentLevel - spacesPerTab);
                }
                return save;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                return indentLevel;
            }
        }
    }
    
    private static boolean startsWith(CharSequence text, CharSequence s) {
        int l = s.length();
        if (text.length() <  l) {
            return false;
        }
        return startsWith0(text, l, s);
    }
    
    private static boolean startsWith0(CharSequence text, int l, CharSequence s) {
        for (int i = 0; i < l; i++) {
            if (text.charAt(i) != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean equals(CharSequence s1, CharSequence s2) {
        return s1.length() == s2.length() && startsWith0(s1, s1.length(), s2);
    }
    
    private void endTag(CharSequence image, boolean selfClosed) throws BadLocationException {
        int begin = currentTokensSize;
        int end = begin + image.length();

        // preserve wh for this end tag
        boolean preserveThis = preserveWhitespace;
        // preservation after the tag closes
        boolean preservingWhitespaceOnClose = preserveWhitespace;
        // look into tag stack, try to find a proper indent level
        CharSequence tagName = image.subSequence(2, image.length());
        int newIndentLevel = -1;
        TokenIndent myIndent = null;
        for (int i = stack.size() - 1; newIndentLevel < 0 && i >= 0; i--) {
            TokenIndent el = stack.get(i);
            if (selfClosed || equals(tagName, el.getName())) {
                myIndent = el;
                newIndentLevel = el.getIndentLevel();
                preservingWhitespaceOnClose = el.isPreserveIndent();
                stack.subList(i, stack.size()).clear();
            }
        }
        int tagLevel = updateIndent(false, newIndentLevel, preservingWhitespaceOnClose);
        tagIndent = tagLevel;
        if (tokenInSelectionRange && !preserveThis) {
            // self-closing tag end does not indent unless it starts a new line
            boolean indent = wasNewline;
            if (!indent) {
                if (!selfClosed && onlyTags) {
                    indent = true;
                    int start = LineDocumentUtils.getLineStart(basedoc, tokenSequence.offset());
                    // do not indent closing tags, if the start & end tag will end up on the same line:
                    if (myIndent != null) {
                        int openStart = myIndent.getStartOffset();
                        if (start <= openStart && !tags.isEmpty()) {
                            // originally on the same line, let's see whether a indent instruction was issued from that time:
                            int last = tags.get(tags.size() - 1).serial;
                            indent = myIndent.serial < last;
                        }
                    }
                }
            }
            if (indent) {
                tags.add(new TokenIndent(preserveThis, begin, tagLevel));
            }
        }
        this.preserveWhitespace = preservingWhitespaceOnClose;
        // content is present for the parent tag
        this.contentPresent = true;
        this.onlyTags = true;
    }
    
    /**
     * Accumulated instructions for formatting
     */
    private List<TokenIndent> tags = new ArrayList<TokenIndent>();
    
    private int currentTokensSize;
    
    /**
     * The currently inspected token
     */
    private org.netbeans.api.lexer.Token<XMLTokenId> token;
    
    private void attributeName() throws BadLocationException {
        TokenType tokenType;
        CharSequence tt = token.text();
        settingSpaceValue = tt.length() == XML_SPACE_ATTRIBUTE_LEN &&
                startsWith0(tt, XML_SPACE_ATTRIBUTE_LEN, XML_SPACE_ATTRIBUTE);
        // fa!ll through !
        if (firstAttributeIndent == -1) {
            firstAttributeIndent = tagIndent;
            if (wasNewline) {
                // indent of a new line
                firstAttributeIndent += spacesPerTab;
            } else {
                // align one space after the tagname:
                TokenIndent tagIndent = stack.peek();
                int current = Utilities.getVisualColumn((LineDocument)basedoc, tokenSequence.offset());
                if (tagIndent == null) {
                    // fallback
                    firstAttributeIndent = current;
                } else {
                    int proposed = firstAttributeIndent + (tagIndent.tagName.length() + 1 /* space */ + 1 /* < char */);
                    // preserve extra indent after tag name
                    firstAttributeIndent = Math.max(current, proposed);
                }
            }
        }
        if (wasNewline) {
            int attrIndent;
            tokenType = TokenType.TOKEN_ATTR_NAME;
            attrIndent = firstAttributeIndent;
            if (tokenInSelectionRange) {
                tags.add(
                    new TokenIndent(
                        false,
                        tokenSequence.offset(), attrIndent
                    )
                );
            }
        }
    }
    
    private void attributeValue() {
        if (settingSpaceValue) {
            CharSequence s = token.text();
            if (s.length() == SPACE_PRESERVE_LEN &&
                startsWith0(s, SPACE_PRESERVE_LEN, SPACE_PRESERVE)) {
                preserveWhitespace = true;
            } else if (s.length() == SPACE_DEFAULT_LEN && startsWith0(s, SPACE_DEFAULT_LEN, SPACE_DEFAULT)) {
                preserveWhitespace = false;
            }
            settingSpaceValue = false;
        }
    }
     
    private int startOffset;
    private int endOffset;
    /*
     * Some content is present between tags, either text content, or a 
     * nested tag. The attribute is set initially to false at tag start,
     * and raised by endtag, processing instruction or char content + CDATA
     */
    private boolean contentPresent;
    private boolean onlyTags;
    
    private void text(CharSequence image, int indentLineStart) throws BadLocationException {
        // must detect newlines. If inside a tag (between attributes), the 1st attribute on the line
        // will emit indent token to the output stream.
        // if outside tags (normal text), each newline in non-ws-preserving tag will emit an indent token
        int lastNewline = lastIndexOf(image, '\n');
        int currentOffset = tokenSequence.offset();

        boolean intersectsWithRange;
        
        int tokenStart = tokenSequence.offset();
        int tokenEnd = tokenStart + image.length();
        
        intersectsWithRange = ((tokenStart <= startOffset && tokenEnd > startOffset) ||
            (tokenEnd >= endOffset && tokenStart < endOffset) ||
            (tokenStart >= startOffset && tokenEnd <= endOffset));

        if (lastNewline == -1 || preserveWhitespace || !intersectsWithRange) {
            // even if outside selection range, we do not update indent; text will not affect following tags
            // we have to set the 'newLine' flag, if the last text line only contains whitespaces. 
            int nonWhitePos = LineDocumentUtils.getNextNonWhitespace(basedoc, currentOffset + Math.max(0, lastNewline), currentOffset + image.length());
            contentPresent |= nonWhitePos > -1;
            wasNewline &= nonWhitePos == -1;
            onlyTags &= nonWhitePos == -1;
            return;
        }
        // emit tag record for each subsequent line
        splitLines(image);
        int lno = indentLineStart; // skip 1st line = up to the 1st newline, this part follows a tag and is always joined
        int nonWhiteStart = -1;
        while (lno < lineCount) {
            currentOffset += lno == 0 ? 0 : lineSizes[lno - 1] + 1; // add 1 for newline
            int lineEnd = currentOffset + lineSizes[lno];
            nonWhiteStart = LineDocumentUtils.getNextNonWhitespace(basedoc, currentOffset, lineEnd);
            // implies a check for nonWhitestart > -1
            if (nonWhiteStart >= startOffset && nonWhiteStart <= endOffset) {
                // emit a tag at this position
                tags.add(new TokenIndent(
                    new TokenElement(TokenType.TOKEN_CHARACTER_DATA, 
                            token.id().name(), 
                            nonWhiteStart, lineEnd,
                            indentLevel + spacesPerTab), 
                    false,
                    nonWhiteStart, indentLevel + spacesPerTab
                ));
            }
            lno++;
        }
        // only last row is taken into account
        contentPresent = !(wasNewline = nonWhiteStart == -1);
        onlyTags &= !contentPresent;
    }
    
    private static int lastIndexOf(CharSequence s, char c) {
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Output variables for splitLines()
     */
    private int lineCount;
    private int[] lineSizes = new int[10];
    
    private void splitLines(CharSequence s) {
        lineCount = 0;
        int len = s.length();
        int l = 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == '\n') {
                addLine(l);
                l = 0;
            } else {
                l++;
            }
        }
        addLine(l);
    }
    
    private void addLine(int len) {
        if (lineSizes.length <= lineCount) {
            int[] lines = new int[lineSizes.length * 2];
            System.arraycopy(lineSizes, 0, lines, 0, lineSizes.length);
            lineSizes = lines;
        }
        lineSizes[lineCount++] = len;
    }
    
    /**
     * This is the core of the formatting algorithm.  It was originally derived
     * from {@link XmlFoldManager#createFolds(org.netbeans.spi.editor.fold.FoldHierarchyTransaction)}.
     * Like that method, this method parses the document using lexer.  Rather
     * than creating folds though, this method reformats by manipulating the
     * whitespace tokens.  To do this it keeps track of the nesting level of the
     * XML and the use of the xml:space attribute.  Together they are used to
     * calculate how much each token should be indented by.
     */
    private List<TokenIndent> getTags(LineDocument basedoc, int startOffset, int endOffset)
            throws BadLocationException, IOException {
        
        
        this.basedoc = basedoc;
        this.startOffset = startOffset;
        this.endOffset = endOffset;

        // this is that 1st PI or tag will increment the level to 0
        indentLevel = -spacesPerTab;
        List[] result = new List[1];
        Exception[] ble = new Exception[1];
        basedoc.render(() -> {
            try {
                result[0] = getTagsLocked(startOffset, endOffset);
            } catch (BadLocationException | IOException ex) {
                ble[0] = ex;
            }
        });
        if (ble[0] != null) {
            if (ble[0] instanceof BadLocationException) {
                throw (BadLocationException)ble[0];
            }
            if (ble[0] instanceof IOException) {
                throw (IOException)ble[0];
            } else {
                throw new IOException(ble[0]);
            }
        }
        return result[0];
    }
    
    private List<TokenIndent> getTagsLocked(int startOffset, int endOffset) throws BadLocationException, IOException {
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(basedoc);
        tokenSequence = tokenHierarchy.tokenSequence();
        token = tokenSequence.token();
        // Add the text token, if any, before xml declaration to document node
        if (token != null && token.id() == XMLTokenId.TEXT) {
            if (tokenSequence.moveNext()) {
                token = tokenSequence.token();
            }
        }
        currentTokensSize = 0;


        // will be set to indent of 1st attribute of a tag. Will be reset to -1 by start tag
        firstAttributeIndent = -1;
        wasNewline = false;

        while (tokenSequence.moveNext()) {
            int indentLineStart = 1;
            token = tokenSequence.token();
            XMLTokenId tokenId = token.id();
            CharSequence image = token.text();
            if (tokenSequence.offset() > endOffset) {
                break;
            }
            tokenInSelectionRange = tokenSequence.offset() >= startOffset || tokenSequence.offset() + token.length() > endOffset;
            switch (tokenId) {
                case TAG: { // Tag is encountered and the required level of indenting determined.
                            // The tokens are only assessed if they are in the selection
                            // range, which is the whole document if no text is selected.
                    int len = image.length();
                    firstAttributeIndent = -1;
                    if (image.charAt(len - 1) == '>') {// '/>' // NOI18N
                        if (len == 2) {
                            endTag(image, true);
                        } else {
                            // end tag name marker
                            tagClose(image);
                        }
                    } else {
                        if (startsWith(image, "</")) { // NOI18N
                            endTag(image, false);
                        } else {
                            startTag(image);
                        }
                        outsideAttributes();
                    }
                    break;
                }
                case PI_START: {
                    updateIndent(true, -1, preserveWhitespace);
                    //indentLevel += spacesPerTab;
                    if (tokenInSelectionRange && !preserveWhitespace) {
                        TokenElement tag = new TokenElement(TokenType.TOKEN_PI_START_TAG, 
                                tokenId.name(), 
                                tokenSequence.offset(), 
                                tokenSequence.offset() + token.length(), indentLevel);
                        TokenIndent ti = new TokenIndent(preserveWhitespace, tokenSequence.offset(), indentLevel);
                        ti.markNoNewline();
                        tags.add(ti);
                    }
                    break;
                }
                case PI_END: {
                    int l = updateIndent(false, -1, preserveWhitespace);
                    if (wasNewline && tokenInSelectionRange) {
                        // 1st item on a new line, will indent according to the opening tag
                        tags.add(new TokenIndent(false, tokenSequence.offset(), l));
                    }
                    break;
                }
                case WS: {
                        // we assume there is nothing except whitespace
                        int lastNewline = lastIndexOf(image, '\n');
                        if (lastNewline == -1) {
                            // nothing special here
                            break;
                        }
                        wasNewline = true;
                        break;
                }
                case PI_CONTENT:
                    indentLineStart = 0;
                    // fall through
                case TEXT: {
                    text(image, indentLineStart);
                    break;
                }

                /**
                 * Block comments are aligned as follows:
                 * - if there is some preceeding non-whitespace, do not format anything. E.g. comments after element. Skip entire comment from formatting
                 * - align 1st and last line at the appropriate indent level
                 * - compute "shift" from the last line & indent level
                 * - shift INTERIOR of the comment by the computed shift
                 * 
                 * This algorithm tries to preserve internal formatting of the comment
                 */
                case BLOCK_COMMENT: {
                    int currentOffset = tokenSequence.offset();

                    splitLines(image);

                    int lineStart = LineDocumentUtils.getLineStart(basedoc, currentOffset);

                    if (lineStart < currentOffset && 
                         LineDocumentUtils.getPreviousNonWhitespace(basedoc, currentOffset, lineStart) > -1) {
                        // we cannot indent comment start, will not touch even the rest of the comment.
                        break;
                    }

                    int lastLineStart = LineDocumentUtils.getLineStart(basedoc, currentOffset + token.length() - 1);
                    int lastIndent = IndentUtils.lineIndent(basedoc, lastLineStart);

                    // align 1st and last row here:
                    int baseIndent = indentLevel + spacesPerTab;
                    // shift the rest of lines by this offset
                    int indentShift = baseIndent - lastIndent;

                    // how much to shift the interior of the comment

                    for (int lno = 0; lno < lineCount; lno++) {
                        // indent 1st comment line, as if it was text:
                        int lineEnd = LineDocumentUtils.getLineEnd(basedoc, currentOffset);

                        int desiredIndent;
                        if (lno == 0 || lno == lineCount -1) {
                            desiredIndent = baseIndent;
                        } else {
                            desiredIndent = IndentUtils.lineIndent(basedoc, currentOffset) + indentShift;
                        }

                        if ((currentOffset >= startOffset || currentOffset + lineSizes[lno] > endOffset) && currentOffset < endOffset) {
                            tags.add(new TokenIndent(
                                false,
                                currentOffset, Math.max(0, desiredIndent)
                            ));
                        }
                        currentOffset += lineSizes[lno] + 1;
                    }
                    break;
                }


                case CDATA_SECTION: {
                    // always form a non-empty content
                    contentPresent = true;
                    onlyTags = false;
                    wasNewline = false;
                }
                case CHARACTER:
                case OPERATOR:
                case PI_TARGET:
                case DECLARATION:
                    break; //Do nothing for above case's
                case ARGUMENT: //attribute of an element
                    attributeName();
                    break;
                case VALUE:
                    attributeValue();
                    break;

                case ERROR:
                case EOL:
                default:
                    throw new IOException("Invalid token found in document: "
                            + "Please use the text editor to resolve the issues...");
            }
            currentTokensSize += image.length();
            if (tokenId != XMLTokenId.WS && tokenId != XMLTokenId.TEXT && tokenId != XMLTokenId.PI_CONTENT) {
                // clear indicator of the newline
                wasNewline = false;
            }
        }
        return tags;
    }

    /**
     * Counter of issued Tag instances. 
     */
    private int counter = 1;

    /**
     * The formatter needs to keep track of when it can remove whitespace and
     * when it must preserve whitespace as defined by the xml:space attribute.
     * This class associates a flag that defines whether whitespace is to be
     * preserved with the other token data that is used in the code folding
     * algorithm.
     */
    private class TokenIndent {

        private TokenElement token;
        /**
         * OLD value of preserveIndent. Saved from previous level at start tag,
         * restored when the tag is popped.
         */
        private boolean preserveIndent;
        
        private int serial = ++counter;
        
        private boolean noNewline;
        
        private int indentLevel;
        
        private int startOffset;
        
        private CharSequence tagName;
        
        public TokenIndent(TokenElement token, boolean preserveIndent, int startOffset, int indentLevel) {
            this(preserveIndent, startOffset, indentLevel);
        }
        
        public TokenIndent(boolean preserveIndent, int startOffset, int indentLevel) {
            this.preserveIndent = preserveIndent;
            this.startOffset = startOffset;
            this.indentLevel = Math.max(0, indentLevel);
        }
        
        public TokenIndent(CharSequence tagName, boolean preserveIndent, int startOffset, int indentLevel) {
            this(preserveIndent, startOffset, indentLevel);
            this.tagName = tagName;
        }                
        
        public int getIndentLevel() {
            return indentLevel;
        }
        
        public void markNoNewline() {
            this.noNewline = true;
        }
        
        public boolean isNoNewline() {
            return noNewline;
        }

        public TokenElement getToken() {
            return token;
        }

        public boolean isPreserveIndent() {
            return preserveIndent;
        }
        
        public int getStartOffset() {
            return startOffset;
        }

        public void setPreserveIndent(boolean preserveIndent) {
            this.preserveIndent = preserveIndent;
        }

        @Override
        public String toString() {
            return "TokenIndent: name=" + token.getName() + " preserveIndent=" + preserveIndent;
        }
        
        public CharSequence getName() {
            return tagName;
        }
    }

    void reformat(Context context) throws BadLocationException{
        reformat(context, context.startOffset(), context.endOffset());
    }
}
