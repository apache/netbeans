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

package org.netbeans.modules.editor.structure.formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
/**
 * The contract for the order of calling formatters based on this class
 * 
 *  - isTopLevelLanguage() must return true always and only for the first formatter called.
 *    Note that the HTML formatter can be hacked to act as a top-level formatters by setting 
 *    HTMLLexerFormatter.HTML_FORMATTER_ACTS_ON_TOP_LEVEL document property.
 * 
 *  - all the formatters called *before* any formatter based on this class must maintain
 *    the TRANSFER_DATA_DOC_PROPERTY. All formatters based on this class automatically do it
 * 
 * Handling embedded languages:
 *
 * When formatting a language that is split into several blocks:
 *
 * - for each line in block: line indent = indent calculated by formatter + block offset, taken from the first line, e.g.:
 *
 * XXXX
 *   <p>
 *   Hello, world!
 *    	</p>
 * XXXX
 *
 * will be formatted to:
 *
 * XXXX
 *   <p>
 *   	Hello, world!
 *    </p>
 * XXXX
 *
 * Note that indent of the first line doesn't change, the rest is formatted accordingly. The containing language formatter should always indent at least the first line of the embedded language block  to desired position.
 *
 * - if 2 blocks of the language being formatted surround a block of a different language the whole block will be shifted right by the indent level calculated for the current language, e.g:
 *
 * XXXXX
 *   <p>
 * XXXXX
 *   </p>
 * XXXXX
 *
 * will be formatted to:
 *
 * XXXXX
 *   <p>
 *   	XXXXX
 *   </p>
 * XXXXX
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class TagBasedLexerFormatter {    
    private static final Logger logger = Logger.getLogger(TagBasedLexerFormatter.class.getName());

    protected abstract boolean isClosingTag(JoinedTokenSequence tokenSequence, int tagOffset);

    protected abstract boolean isUnformattableToken(JoinedTokenSequence tokenSequence, int tagOffset);

    protected abstract boolean isUnformattableTag(String tag);

    protected abstract boolean isOpeningTag(JoinedTokenSequence tokenSequence, int tagOffset);

    protected abstract String extractTagName(JoinedTokenSequence tokenSequence, int tagOffset);

    protected abstract boolean areTagNamesEqual(String tagName1, String tagName2);

    protected abstract boolean isClosingTagRequired(BaseDocument doc, String tagName);

    protected abstract int getOpeningSymbolOffset(JoinedTokenSequence tokenSequence, int tagOffset);

    protected abstract int getTagEndingAtPosition(JoinedTokenSequence tokenSequence, int tagOffset) throws BadLocationException;

    protected abstract int getTagEndOffset(JoinedTokenSequence tokenSequence, int tagOffset);

    protected abstract LanguagePath supportedLanguagePath();
    
    public enum EmbeddingType {
        /**
         * The line belongs to the language being currently formatted
         */
        CURRENT_LANG,
        /**
         * The line belongs to a language embedded <em>inside</em> currently formatted one
         */
        INNER,
        /**
         * The line belongs to a languge in which currently formatted languaged is embedded
         */
        OUTER}

    public void process(Context context) throws BadLocationException{
        if (context.isIndent()) {
            //
            // A temporary workaround for issue #178512
            BaseDocument doc = (BaseDocument)context.document();
            int firstLine = Utilities.getLineOffset(doc, context.startOffset());
            int lastLine = Utilities.getLineOffset(doc, context.endOffset());
            if (firstLine == lastLine) {
                enterPressed(context);
            } else {
                reformat(context);
            }
        } else {
            reformat(context);
        }
    }
    
    public void reformat(Context context) throws BadLocationException{
        reformat(context, context.startOffset(), context.endOffset());
    }
    
    public void reformat(Context context, int startOffset, int endOffset) throws BadLocationException {
        BaseDocument doc = (BaseDocument) context.document();
        doc.runAtomic(new FormattingTask(context, startOffset, endOffset));
    }
    
    private class IsJustAfterClosingTagTask implements Runnable{
        private BaseDocument doc;
        private int pos;
        private boolean result = false;

        public IsJustAfterClosingTagTask(BaseDocument doc, int pos) {
            this.doc = doc;
            this.pos = pos;
        }

        public void run() {
            try {
                TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(doc);

                TokenSequence[] tokenSequences = tokenHierarchy.tokenSequenceList(supportedLanguagePath(), 0, Integer.MAX_VALUE).toArray(new TokenSequence[0]);
                TextBounds[] tokenSequenceBounds = new TextBounds[tokenSequences.length];

                for (int i = 0; i < tokenSequenceBounds.length; i++) {
                    tokenSequenceBounds[i] = findTokenSequenceBounds(doc, tokenSequences[i]);
                }

                if (tokenSequences.length > 0) {
                    JoinedTokenSequence tokenSequence = new JoinedTokenSequence(tokenSequences, tokenSequenceBounds);
                    tokenSequence.moveStart();
                    tokenSequence.moveNext();
                    int tagPos = getTagEndingAtPosition(tokenSequence, pos);
                    result = tagPos >= 0 && isClosingTag(tokenSequence, tagPos);
                }
            } catch (BadLocationException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        public boolean getResult() {
            return result;
        }
    }

    public boolean isJustAfterClosingTag(BaseDocument doc, int pos) {
        IsJustAfterClosingTagTask task = new IsJustAfterClosingTagTask(doc, pos);
        doc.runAtomic(task);
        return task.getResult();
    }
    
    private static int getTxtLengthWithoutWhitespaceSuffix(CharSequence txt){
        for (int i = txt.length(); i > 0; i --){
            if (!Character.isWhitespace(txt.charAt(i - 1))){
                return i;
            }
        }
        
        return 0;
    }

    protected int getMatchingOpeningTagStart(JoinedTokenSequence tokenSequence, int closingTagOffset) {
        int originalOffset = tokenSequence.offset();
        tokenSequence.move(closingTagOffset);
        tokenSequence.moveNext();

        String searchedTagName = extractTagName(tokenSequence, closingTagOffset);
        int balance = 0;

        while (tokenSequence.movePrevious()) {
            int currentTokenOffset = tokenSequence.offset();
            if (areTagNamesEqual(searchedTagName, extractTagName(tokenSequence, currentTokenOffset))) {
                if (isOpeningTag(tokenSequence, currentTokenOffset)) {
                    if (balance == 0) {
                        tokenSequence.move(originalOffset);
                        tokenSequence.moveNext();
                        return currentTokenOffset;
                    }

                    balance--;
                } else if (isClosingTag(tokenSequence, currentTokenOffset)) {
                    balance++;
                }
            }
        }

        tokenSequence.move(originalOffset);
        tokenSequence.moveNext();
        return -1;
    }
        protected boolean isWSToken(Token token) {
        return isOnlyWhiteSpaces(token.text());
    }

    protected int getIndentForTagParameter(BaseDocument doc, JoinedTokenSequence tokenSequence, int tagOffset) throws BadLocationException {
        int originalOffset = tokenSequence.offset();
        int tagStartLine = Utilities.getLineOffset(doc, tagOffset);
        tokenSequence.move(tagOffset);
        Token<?> token;
        int tokenOffset;
        boolean thereWasWS = false;
        int shift = doc.getShiftWidth(); // default;

        /*
         * Find the offset of the first attribute if it is specified on the same line as the opening of the tag
         * e.g. <tag   |attr=
         *
         */
        while (tokenSequence.moveNext()) {
            token = tokenSequence.token();
            tokenOffset = tokenSequence.offset();
            boolean isWSToken = isWSToken(token);
            
            if (thereWasWS && (!isWSToken || tagStartLine != Utilities.getLineOffset(doc, tokenOffset))) {
                if (!isWSToken && tagStartLine == Utilities.getLineOffset(doc, tokenOffset)) {
                    
                    shift = tokenOffset - Utilities.getRowIndent(doc, tokenOffset)
                            - LineDocumentUtils.getLineStartOffset(doc, tokenOffset);
                }
                break;
            } else if (isWSToken){
                thereWasWS = true;
            }
        }

        tokenSequence.move(originalOffset);
        tokenSequence.moveNext();

        return shift;
    }

    private boolean calcIndents_processOpeningTag(final BaseDocument doc, final JoinedTokenSequence tokenSequence, final String tagName, final Collection<TagIndentationData> unprocessedOpeningTags, final int[] indentsWithinTags) throws BadLocationException {

        boolean thereAreMoreTokens = true;
        // format content of a tag that spans across multiple lines
        int firstTagLine = Utilities.getLineOffset(doc, tokenSequence.offset());
        int tagEndOffset = getTagEndOffset(tokenSequence, tokenSequence.offset());
        
        if (tagEndOffset == -1){
            return true; // unterminated tag, ignore
        }
        
        int lastTagLine = Utilities.getLineOffset(doc, tagEndOffset);

        TagIndentationData tagData = new TagIndentationData(tagName, lastTagLine);
        unprocessedOpeningTags.add(tagData);

        if (firstTagLine < lastTagLine) {
            // performance!
            int indentWithinTag = getIndentForTagParameter(doc, tokenSequence, tokenSequence.offset());

            for (int i = firstTagLine + 1; i <= lastTagLine; i++) {
                indentsWithinTags[i] = indentWithinTag;
            }
        }

        return thereAreMoreTokens;
    }

    private void calcIndents_processClosingTag(final String tagName, final int tagClosedOnLine, final TransferData transferData, final LinkedList<TagIndentationData> unprocessedOpeningTags, final Collection<TagIndentationData> matchedOpeningTags) throws BadLocationException {
        LinkedList<TagIndentationData> tagsToBeRemoved = new LinkedList<TagIndentationData>();

        while (!unprocessedOpeningTags.isEmpty()) {
            TagIndentationData processedTD = unprocessedOpeningTags.removeLast();

            if (areTagNamesEqual(tagName, processedTD.getTagName())) {
                processedTD.setClosedOnLine(tagClosedOnLine);
                matchedOpeningTags.add(processedTD);

                // mark all the stuff between unformattable tag as unformattable
                if (isUnformattableTag(tagName)) {
                    for (int i = tagClosedOnLine - 1; i > processedTD.getLine(); i--) {
                        transferData.setNonFormattable(i);
                    }
                }

                // forgetting preceding tags permanently
                tagsToBeRemoved.clear();
                break;
            } else {
                tagsToBeRemoved.add(processedTD);
            }
        }

        // if matching opening tag was not found on the stack put all the tags back
        unprocessedOpeningTags.addAll(tagsToBeRemoved);
    }

    protected int getInitialIndentFromPreviousLine(final BaseDocument doc, final int line) throws BadLocationException {

        // get initial indent from the previous line
        int initialIndent = 0;

        if (line > 0) {
            int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
            int previousNonWhiteLineEnd = Utilities.getFirstNonWhiteBwd(doc, lineStart);

            if (previousNonWhiteLineEnd > 0) {
                initialIndent = Utilities.getRowIndent(doc, previousNonWhiteLineEnd);
            }
        }

        return initialIndent;
    }

    protected static int getNumberOfLines(BaseDocument doc) throws BadLocationException {
        return Utilities.getLineOffset(doc, doc.getLength()) + 1;
    }

    protected int getNextClosingTagOffset(JoinedTokenSequence tokenSequence, int offset) throws BadLocationException {
        int originalOffset = tokenSequence.offset();
        tokenSequence.move(offset);
        int currentOffset = -1;

        while (tokenSequence.moveNext()) {
            currentOffset = tokenSequence.offset();

            if (isClosingTag(tokenSequence, currentOffset)) {
                tokenSequence.move(originalOffset);
                tokenSequence.moveNext();
                return currentOffset;
            }
        }

        tokenSequence.move(originalOffset);
        tokenSequence.moveNext();
        return -1;
    }

    protected boolean isJustBeforeClosingTag(JoinedTokenSequence tokenSequence, int pos) throws BadLocationException {
        // default, trivial implementation
        if (isClosingTag(tokenSequence, pos)) {
            return true;
        }

        return false;
    }

    protected Token getTokenAtOffset(JoinedTokenSequence tokenSequence, int tagTokenOffset) {
        if (tokenSequence != null && tagTokenOffset >= 0) {
            int originalOffset = tokenSequence.offset();
            tokenSequence.move(tagTokenOffset);

            if (tokenSequence.moveNext()) {
                Token r = tokenSequence.token();
                tokenSequence.move(originalOffset);
                tokenSequence.moveNext();
                return r;
            }
        }

        return null;
    }

    private TextBounds findTokenSequenceBounds(BaseDocument doc, TokenSequence tokenSequence) throws BadLocationException {
        tokenSequence.moveStart();
        tokenSequence.moveNext();
        int absoluteStart = tokenSequence.offset();
        tokenSequence.moveEnd();
        tokenSequence.movePrevious();
        int absoluteEnd = tokenSequence.offset() + tokenSequence.token().length();

//         trim whitespaces from both ends
        while (isWSToken(tokenSequence.token())) {
            if (!tokenSequence.movePrevious()) {
                return new TextBounds(absoluteStart, absoluteEnd); // a block of empty text
            }
        }

        int whiteSpaceSuffixLen = 0;

        while (Character.isWhitespace(tokenSequence.token().text().charAt(tokenSequence.token().length() - 1 - whiteSpaceSuffixLen))) {
            whiteSpaceSuffixLen++;
        }

        int languageBlockEnd = tokenSequence.offset() + tokenSequence.token().length() - whiteSpaceSuffixLen;

        tokenSequence.moveStart();

        do {
            tokenSequence.moveNext();
        } while (isWSToken(tokenSequence.token()));

        int whiteSpacePrefixLen = 0;

        while (Character.isWhitespace(tokenSequence.token().text().charAt(whiteSpacePrefixLen))) {
            whiteSpacePrefixLen++;
        }

        int languageBlockStart = tokenSequence.offset() + whiteSpacePrefixLen;
        int firstLineOfTheLanguageBlock = Utilities.getLineOffset(doc, languageBlockStart);
        int lastLineOfTheLanguageBlock = Utilities.getLineOffset(doc, languageBlockEnd);
        return new TextBounds(absoluteStart, absoluteEnd, languageBlockStart, languageBlockEnd, firstLineOfTheLanguageBlock, lastLineOfTheLanguageBlock);
    }

    private void markCurrentLanguageLines(BaseDocument doc, TextBounds languageBounds, EmbeddingType[] embeddingType) throws BadLocationException {
        if (languageBounds.getStartPos() == -1){
            return; // only white spaces
        }
        
        int firstLineOfTheLanguageBlock = languageBounds.getStartLine();

        int lineStart = Utilities.getRowStartFromLineOffset(doc, firstLineOfTheLanguageBlock);

        if (Utilities.getFirstNonWhiteFwd(doc, lineStart) < languageBounds.getStartPos()) {
            firstLineOfTheLanguageBlock++;
        }

        for (int i = firstLineOfTheLanguageBlock; i <= languageBounds.getEndLine(); i++) {
            embeddingType[i] = EmbeddingType.CURRENT_LANG;
        }
    }
    
    protected boolean isTopLevelLanguage(BaseDocument doc) {
        return supportedLanguagePath().size() == 1;
    }
    
    protected static int getExistingIndent(BaseDocument doc, int line) throws BadLocationException{
        int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
        return IndentUtils.lineIndent(doc, lineStart);
    }

    private class EnterPressedTask implements Runnable{
        private Context context;

        public EnterPressedTask(Context context) {
            this.context = context;
        }
        
        public void run() {
            try {
                enterPressed();
            } catch (BadLocationException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        private void enterPressed() throws BadLocationException{
            BaseDocument doc = (BaseDocument) context.document();
            
            if (isTopLevelLanguage(doc)) {
                doc.putProperty(TransferData.ORG_CARET_OFFSET_DOCPROPERTY, Integer.valueOf(context.caretOffset()));
            }

            Integer dotPos = (Integer) doc.getProperty(TransferData.ORG_CARET_OFFSET_DOCPROPERTY);
            //assert dotPos != null;
            if(dotPos == null) {
                dotPos = context.caretOffset();
            }
            
            int origDotPos = dotPos.intValue() - 1; // dotPos - "\n".length()
            
            if (indexWithinCurrentLanguage(doc, origDotPos - 1)) {
                if (isSmartEnter(doc, dotPos)) {
                    handleSmartEnter(context);
                } else {
                    int newIndent = 0;
                    int lineNumber = Utilities.getLineOffset(doc, dotPos);
                    boolean firstRow = false;
                    
                    if (LineDocumentUtils.getLineStartOffset(doc, origDotPos) == origDotPos){
                        newIndent = getExistingIndent(doc, lineNumber);
                        firstRow = true;
                    } else if (lineNumber > 0){
                        newIndent = getExistingIndent(doc, lineNumber - 1);
                    }
                    
                    TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(doc);
                    TokenSequence[] tokenSequences = tokenHierarchy.tokenSequenceList(supportedLanguagePath(), 0, Integer.MAX_VALUE).toArray(new TokenSequence[0]);
                    TextBounds[] tokenSequenceBounds = new TextBounds[tokenSequences.length];
                    
                    for (int i = 0; i < tokenSequenceBounds.length; i++) {
                        tokenSequenceBounds[i] = findTokenSequenceBounds(doc, tokenSequences[i]);
                    }

                    JoinedTokenSequence tokenSequence = new JoinedTokenSequence(tokenSequences, tokenSequenceBounds);
                    tokenSequence.moveStart(); tokenSequence.moveNext();

                    int openingTagOffset = getTagEndingAtPosition(tokenSequence, origDotPos - 1);
                    
                    if (isOpeningTag(tokenSequence, openingTagOffset)){
                        newIndent += doc.getShiftWidth();
                    }

                    context.modifyIndent(Utilities.getRowStart(doc, dotPos), newIndent);
                    
                    if (firstRow){
                        context.setCaretOffset(context.caretOffset() - newIndent);
                    }
                }
            }
        }
    }
    
    public void enterPressed(Context context) {
        BaseDocument doc = (BaseDocument)context.document();   
        doc.runAtomic(new EnterPressedTask(context));
    }
    
    private boolean indexWithinCurrentLanguage(BaseDocument doc, int index) throws BadLocationException{
        TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence[] tokenSequences = tokenHierarchy.tokenSequenceList(supportedLanguagePath(), 0, Integer.MAX_VALUE).toArray(new TokenSequence[0]);
        
        for (TokenSequence tokenSequence: tokenSequences){
            TextBounds languageBounds = findTokenSequenceBounds(doc, tokenSequence);
            
            if (languageBounds.getAbsoluteStart() <= index && languageBounds.getAbsoluteEnd() >= index){
                tokenSequence.move(index);
                
                if (tokenSequence.moveNext()){
                    // the newly entered \n character may or may not
                    // form a separate token - work it around
                    if (isWSToken(tokenSequence.token())){
                        tokenSequence.movePrevious(); 
                    }
                    
                    return tokenSequence.embedded() == null && !isWSToken(tokenSequence.token());
                }
            }
        }
        
        return false;
    }
    
    public boolean handleSmartEnter(Context context) throws BadLocationException {
        boolean wasSmartEnter = false;
        BaseDocument doc = (BaseDocument)context.document();
        int dotPos = context.caretOffset();

        wasSmartEnter = isSmartEnter(doc, dotPos);

        if (wasSmartEnter) {
            int line = Utilities.getLineOffset(doc, dotPos);
            assert line > 0;
            int baseIndent = getExistingIndent(doc, line - 1);
            doc.insertString(dotPos, "\n", null); //NOI18N
            Position position = doc.createPosition(dotPos);
            context.modifyIndent(Utilities.getRowStartFromLineOffset(doc, line), baseIndent + doc.getShiftWidth());
            context.modifyIndent(Utilities.getRowStartFromLineOffset(doc, line + 1), baseIndent);
            context.setCaretOffset(position.getOffset());
        }

        return wasSmartEnter;
    }
    
    public boolean isSmartEnter(BaseDocument doc, int dotPos) {
        
        TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence[] tokenSequences = tokenHierarchy.tokenSequenceList(supportedLanguagePath(), 0, Integer.MAX_VALUE).toArray(new TokenSequence[0]);
        TextBounds[] tokenSequenceBounds = new TextBounds[tokenSequences.length];
        try {

            for (int i = 0; i < tokenSequenceBounds.length; i++) {
                tokenSequenceBounds[i] = findTokenSequenceBounds(doc, tokenSequences[i]);
            }
            
            JoinedTokenSequence tokenSequence = new JoinedTokenSequence(tokenSequences, tokenSequenceBounds);
            
            if (tokenSequence.move(dotPos) != Integer.MIN_VALUE) { // ignore if dotPos not within current language
                tokenSequence.moveNext();

                if (isJustBeforeClosingTag(tokenSequence, dotPos)) {
                    int closingTagOffset = getNextClosingTagOffset(tokenSequence, dotPos);
                    int matchingOpeningTagOffset = getMatchingOpeningTagStart(tokenSequence, closingTagOffset);
                    int openingTagEnd = getTagEndOffset(tokenSequence, matchingOpeningTagOffset);

                    return openingTagEnd + 1 == dotPos;
                }
            }
            
        } catch (BadLocationException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        

        return false;
    }
    
    protected boolean isOnlyWhiteSpaces(CharSequence txt){
        for (int i = 0; i < txt.length(); i ++){
            if (!Character.isWhitespace(txt.charAt(i))){
                return false;
            }
        }
        
        return true;
    }

    private String formatterName(){
        return getClass().getSimpleName();
    }
    
    private class FormattingTask implements Runnable{
        private Context context;
        private int startOffset;
        private int endOffset;

        public FormattingTask(Context context, int startOffset, int endOffset) {
            this.context = context;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        public void run() {
            try {
                reformat();
            } catch (BadLocationException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        private void reformat() throws BadLocationException{
            BaseDocument doc = (BaseDocument) context.document();
            LinkedList<TagIndentationData> unprocessedOpeningTags = new LinkedList<TagIndentationData>();
            List<TagIndentationData> matchedOpeningTags = new ArrayList<TagIndentationData>();
            TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(doc);

            if (tokenHierarchy == null) {
                logger.severe("Could not retrieve TokenHierarchy for document " + doc);
                return;
            }
            
            TransferData transferData = null;
            
            if (isTopLevelLanguage(doc)){
                // store data for compatible formatters that will be called later
                transferData = new TransferData();
                transferData.init(doc);
            } else {
                // read data from compatible formatters called before
                transferData = TransferData.readFromDocument(doc);
                assert transferData != null;
            }
            
            // PASS 1: Calculate EmbeddingType and AbsoluteIndentLevel 
            // (determined by the tags of the current language) for each line
            
            int firstRefBlockLine = Utilities.getLineOffset(doc, startOffset);
            int lastRefBlockLine = Utilities.getLineOffset(doc, endOffset);
            int firstUnformattableLine = -1;

            EmbeddingType embeddingType[] = new EmbeddingType[transferData.getNumberOfLines()];
            Arrays.fill(embeddingType, EmbeddingType.OUTER);
            int[] indentsWithinTags = new int[transferData.getNumberOfLines()];

            TokenSequence[] tokenSequences = tokenHierarchy.tokenSequenceList(
                    supportedLanguagePath(), 0, Integer.MAX_VALUE).toArray(new TokenSequence[0]);
            
            TextBounds[] tokenSequenceBounds = new TextBounds[tokenSequences.length];

            for (int i = 0; i < tokenSequenceBounds.length; i++) {
                tokenSequenceBounds[i] = findTokenSequenceBounds(doc, tokenSequences[i]);

                if (tokenSequenceBounds[i].getStartLine() > -1) {
                    // skip white-space blocks
                    markCurrentLanguageLines(doc, tokenSequenceBounds[i], embeddingType);
                }
            }

            if (tokenSequences.length > 0) {
                JoinedTokenSequence tokenSequence = new JoinedTokenSequence(tokenSequences, tokenSequenceBounds);
                tokenSequence.moveStart();
                boolean thereAreMoreTokens = tokenSequence.moveNext();
                
                do {
                    boolean isOpenTag = isOpeningTag(tokenSequence, tokenSequence.offset());
                    boolean isCloseTag = isClosingTag(tokenSequence, tokenSequence.offset());

                    if (isOpenTag || isCloseTag) {

                        String tagName = extractTagName(tokenSequence, tokenSequence.offset());

                        if (isOpenTag) {

                            thereAreMoreTokens &= calcIndents_processOpeningTag(doc,
                                    tokenSequence, tagName, unprocessedOpeningTags, indentsWithinTags);
                        } else {
                            int tagLine = Utilities.getLineOffset(doc, tokenSequence.offset());
                            
                            calcIndents_processClosingTag(tagName, tagLine, transferData,
                                    unprocessedOpeningTags, matchedOpeningTags);
                        }
                    }

                    // process a block of unformattable tokens (which may be separated with white spaces)
                    boolean wasPreviousTokenUnformattable = firstUnformattableLine != -1 && isWSToken(tokenSequence.token())
                            && !tokenSequence.isJustAfterGap()
                            || isUnformattableToken(tokenSequence, tokenSequence.offset());

                    if (wasPreviousTokenUnformattable && firstUnformattableLine == -1) {
                        firstUnformattableLine = Utilities.getLineOffset(doc, tokenSequence.offset());
                    }

                    // detect the end of an unformattable block; mark it
                    if (firstUnformattableLine > -1 && (!wasPreviousTokenUnformattable || !thereAreMoreTokens)) {

                        int lastUnformattableLine = thereAreMoreTokens ?
                            Utilities.getLineOffset(doc, tokenSequence.offset() - 1) : transferData.getNumberOfLines() - 1;

                        for (int i = firstUnformattableLine + 1; i < lastUnformattableLine; i++) {
                            transferData.setNonFormattable(i);
                        }

                        firstUnformattableLine = -1;
                    }
                    
                    // Mark blocks of embedded language
                    if (tokenSequence.embedded() != null && !isWSToken(tokenSequence.token())) {
                        int firstLineOfEmbeddedBlock = Utilities.getLineOffset(doc, tokenSequence.offset());

                        int lastLineOfEmbeddedBlock = Utilities.getLineOffset(doc,
                                tokenSequence.offset() + getTxtLengthWithoutWhitespaceSuffix(tokenSequence.token().text()));

                        if (Utilities.getFirstNonWhiteFwd(doc, Utilities.getRowStartFromLineOffset(doc,
                                firstLineOfEmbeddedBlock)) < Utilities.getFirstNonWhiteFwd(doc, tokenSequence.offset())) {

                            firstLineOfEmbeddedBlock++;
                        }

                        for (int i = firstLineOfEmbeddedBlock; i <= lastLineOfEmbeddedBlock; i++) {
                            embeddingType[i] = EmbeddingType.INNER;
                        }
                    }
                    
                    thereAreMoreTokens &= tokenSequence.moveNext();

                } while (thereAreMoreTokens);
            }
            
            // PASS 2: handle formatting order for languages on the same level of mime-hierarchy
            
            for (int line = 0; line < transferData.getNumberOfLines(); line ++){
                if (embeddingType[line] == EmbeddingType.CURRENT_LANG){
                    transferData.setProcessedByNativeFormatter(line);
                } else if (embeddingType[line] == EmbeddingType.OUTER){
                    // play master
                    if (!transferData.wasProcessedByNativeFormatter(line)){
                        embeddingType[line] = EmbeddingType.INNER; 
                    }
                }
            }
            
            //****************
            // PASS 3: calc line indents
            // TODO: optimize it
            int[] indentLevels = new int[transferData.getNumberOfLines()];
            Arrays.fill(indentLevels, 0);

            for (TagIndentationData td : matchedOpeningTags) {
                // increase indent from one line after the opening tag
                // up to one line before the closing tag
                for (int i = td.getLine() + 1; i <= td.getClosedOnLine() - 1; i++) {
                    indentLevels[i]++;
                }
            }
            
            int[] previousIndents = transferData.getTransformedOffsets();
            int[] absoluteIndents = new int[transferData.getNumberOfLines()];
            
            for (int i = 0; i < transferData.getNumberOfLines(); i++) {
                absoluteIndents[i] = indentLevels[i] * doc.getShiftWidth() + indentsWithinTags[i];
            }
            
            int lastCrossPoint = 0;
            int lastOuterCrossPoint = 0;
            EmbeddingType lastEmbeddingType = null;
            
            int[] newIndents = new int[transferData.getNumberOfLines()];
            boolean topLevel = isTopLevelLanguage(doc);

            for (int i = 0; i < transferData.getNumberOfLines(); i++) {
                if (lastEmbeddingType != embeddingType[i]){
                    lastCrossPoint = i;
                    
                    if (lastEmbeddingType == EmbeddingType.OUTER){
                        lastOuterCrossPoint = i;
                    }
                    
                    lastEmbeddingType = embeddingType[i];
                }

                if (!transferData.isFormattable(i)) {
                    newIndents[i] = transferData.getOriginalIndent(i);
                } else {
                    if (embeddingType[i] == EmbeddingType.OUTER) {
                        newIndents[i] = previousIndents[i] + absoluteIndents[i];
                    } else if (embeddingType[i] == EmbeddingType.INNER) { // INNER
                        if (lastCrossPoint == i){ // first line of inner embedding
                            int previousLineIndent = i > 0 ? newIndents[lastCrossPoint - 1] : 0;
                            int absDiff = absoluteIndents[i] - (i > 0 ? absoluteIndents[i - 1] : 0);
                            newIndents[i] = previousLineIndent + absDiff;
                        } else {
                            int diff = topLevel ? (transferData.getOriginalIndent(i) - transferData.getOriginalIndent(lastCrossPoint))
                                    : (previousIndents[i] - previousIndents[lastCrossPoint]);
                            
                            newIndents[i] = newIndents[lastCrossPoint] + diff;
                        }
                    } else { // embeddingType[i] == EmbeddingType.CURRENT_LANG
                        newIndents[i] = previousIndents[lastOuterCrossPoint] + absoluteIndents[i];
                    }
                }
            }
            
            int lineBeforeSelectionBias = 0;
            
            if (firstRefBlockLine > 0){
                lineBeforeSelectionBias = transferData.getOriginalIndent(firstRefBlockLine - 1) - newIndents[firstRefBlockLine - 1];
            }
            
            // PASS 4: apply line indents
            
            for (int line = firstRefBlockLine; line <= lastRefBlockLine; line++) {
                int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
                int newIndent = newIndents[line] + lineBeforeSelectionBias;
                context.modifyIndent(lineStart, newIndent > 0 ? newIndent : 0);
            }
            
            transferData.setTransformedOffsets(newIndents);

            if (logger.isLoggable(Level.FINE)) {
                StringBuilder buff = new StringBuilder();

                for (int i = 0; i < transferData.getNumberOfLines(); i++) {
                    int lineStart = Utilities.getRowStartFromLineOffset(doc, i);

                    char formattingTypeSymbol = 0;
                    
                    if (!transferData.isFormattable(i)){
                        formattingTypeSymbol = '-';
                    } else if (embeddingType[i] == EmbeddingType.INNER){
                        formattingTypeSymbol = 'I';
                    } else if (embeddingType[i] == EmbeddingType.OUTER) {
                        formattingTypeSymbol = 'O';
                    } else {
                        formattingTypeSymbol = 'C';
                    }
                    
                    char formattingRange = (i >= firstRefBlockLine && i <= lastRefBlockLine) 
                            ? '*' : ' ';

                    buff.append(i + ":" + formattingRange + ":" + indentLevels[i] + ":" + formattingTypeSymbol + ":" + doc.getText(lineStart, LineDocumentUtils.getLineEndOffset(doc, lineStart) - lineStart) + ".\n"); //NOI18N
                }

                buff.append("\n-------------\n"); //NOI18N
                logger.fine(formatterName() + ":\n" + buff);
            }
        }
    }
    

    protected static class TagIndentationData {

        private final String tagName;
        private final int line;
        private int closedOnLine;

        public TagIndentationData(String tagName, int line) {
            this.tagName = tagName;
            this.line = line;
        }

        public String getTagName() {
            return tagName;
        }

        public int getLine() {
            return line;
        }

        public int getClosedOnLine() {
            return closedOnLine;
        }

        public void setClosedOnLine(int closedOnLine) {
            this.closedOnLine = closedOnLine;
        }
    }
}
