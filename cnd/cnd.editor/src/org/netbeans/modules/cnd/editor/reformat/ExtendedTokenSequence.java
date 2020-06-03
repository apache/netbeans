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

package org.netbeans.modules.cnd.editor.reformat;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.editor.reformat.DiffLinkedList.DiffResult;
import org.netbeans.modules.cnd.editor.reformat.Reformatter.Diff;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;

/**
 *
 */
public class ExtendedTokenSequence {
    private final TokenSequence<CppTokenId> ts;
    private final DiffLinkedList diffs;
    private final int tabSize;
    private final boolean expandTabToSpaces;
    /*package local*/ ExtendedTokenSequence(TokenSequence<CppTokenId> ts, DiffLinkedList diffs, int tabSize, boolean expandTabToSpaces){
        this.ts = ts;
        this.diffs = diffs;
        this.tabSize = tabSize;
        this.expandTabToSpaces = expandTabToSpaces;
    }

    /*package local*/ Diff replacePrevious(Token<CppTokenId> previous, int newLines, int spaces, boolean isIndent){
        String old = previous.text().toString();
        if (!Diff.equals(old, newLines, spaces, isIndent, expandTabToSpaces, tabSize)){
            return diffs.addFirst(ts.offset() - previous.length(),
                                  ts.offset(), newLines, spaces, isIndent);
        }
        return null;
    }

    /*package local*/ Diff addBeforeCurrent(int newLines, int spaces, boolean isIndent){
        if (newLines+spaces>0) {
            return diffs.addFirst(ts.offset(),
                                  ts.offset(), newLines, spaces, isIndent);
        }
        return null;
    }

    /*package local*/ Diff replaceCurrent(Token<CppTokenId> current, int newLines, int spaces, boolean isIndent){
        String old = current.text().toString();
        if (!Diff.equals(old, newLines, spaces, isIndent, expandTabToSpaces, tabSize)){
            return diffs.addFirst(ts.offset(),
                                  ts.offset() + current.length(), newLines, spaces, isIndent);
        }
        return null;
    }

    /*package local*/ Diff addAfterCurrent(Token<CppTokenId> current, int newLines, int spaces, boolean isIndent){
        if (newLines+spaces>0) {
            return diffs.addFirst(ts.offset() + current.length(),
                                  ts.offset() + current.length(), newLines, spaces, isIndent);
        }
        return null;
    }

    /*package local*/ Diff replaceNext(Token<CppTokenId> current, Token<CppTokenId> next, int newLines, int spaces, boolean isIndent){
        String old = next.text().toString();
        if (!Diff.equals(old, newLines, spaces, isIndent, expandTabToSpaces, tabSize)){
            return diffs.addFirst(ts.offset()+current.length(),
                                  ts.offset()+current.length()+next.length(), newLines, spaces, isIndent);
        }
        return null;
    }
    
    /*package local*/ int getFirstLineTokenPosition(){
        int index = ts.index();
        try {
            int column = 0;
            while(ts.movePrevious()){
                DiffResult diff = diffs.getDiffs(this, 0);
                if (diff != null){
                    if (diff.before != null){
                        column+=diff.before.spaceLength();
                        if (diff.before.hasNewLine()) {
                            return column;
                        }
                    }
                    if (diff.replace != null){
                        column+=diff.replace.spaceLength();
                        if (diff.replace.hasNewLine()) {
                            return column;
                        }
                        continue;
                    }
                }
                switch (ts.token().id()) {
                    case NEW_LINE:
                    case PREPROCESSOR_DIRECTIVE:
                         return column;
                    case DOXYGEN_COMMENT:
                    case BLOCK_COMMENT:
                    {
                        String text = ts.token().text().toString();
                        int i = text.lastIndexOf('\n');
                        if (i < 0){
                            column = 0;
                            break;
                        } 
                        column += text.length()-i+1;
                        return column;
                    }
                    case WHITESPACE:
                    {
                        String text = ts.token().text().toString();
                        for(int i = 0; i < text.length(); i++){
                            char c = text.charAt(i);
                            if (c == '\t'){
                                column = (column/tabSize+1)* tabSize;
                            } else {
                                column+=1;
                            }
                        }
                        break;
                    }
                    default:
                        column+=ts.token().length();
                        break;
                }
            }
            return column;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }


    /*package local*/ int getTokenPosition(){
        int index = ts.index();
        try {
            int column = 0;
            while(ts.movePrevious()){
                DiffResult diff = diffs.getDiffs(this, 0);
                if (diff != null){
                    if (diff.before != null){
                        column+=diff.before.spaceLength();
                        if (diff.before.hasNewLine()) {
                            return column;
                        }
                    }
                    if (diff.replace != null){
                        column+=diff.replace.spaceLength();
                        if (diff.replace.hasNewLine()) {
                            return column;
                        }
                        continue;
                    }
                }
                switch (ts.token().id()) {
                    case NEW_LINE:
                    case PREPROCESSOR_DIRECTIVE:
                         return column;
                    case DOXYGEN_COMMENT:
                    case BLOCK_COMMENT:
                    {
                        String text = ts.token().text().toString();
                        int i = text.lastIndexOf('\n');
                        if (i < 0){
                            column+=text.length();
                            break;
                        } 
                        column += text.length()-i+1;
                        return column;
                    }
                    case WHITESPACE:
                    {
                        String text = ts.token().text().toString();
                        for(int i = 0; i < text.length(); i++){
                            char c = text.charAt(i);
                            if (c == '\t'){
                                column = (column/tabSize+1)* tabSize;
                            } else {
                                column+=1;
                            }
                        }
                        break;
                    }
                    default:
                        column+=ts.token().length();
                        break;
                }
            }
            return column;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    /*package local*/ Token<CppTokenId> lookNextImportant(){
        int index = ts.index();
        try {
            while(ts.moveNext()){
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        return ts.token();
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<CppTokenId> lookNextImportant(int i){
        int index = ts.index();
        try {
            while(ts.moveNext()){
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        i--;
                        if (i <= 0) {
                            return ts.token();
                        }
                        break;
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<CppTokenId> lookNextLineImportant(){
        int index = ts.index();
        try {
            while(ts.moveNext()){
                switch (ts.token().id()) {
                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                    case NEW_LINE:
                    case ESCAPED_WHITESPACE:
                        return null;
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                        if (ts.token().text().toString().indexOf('\n')>=0) {
                            return null;
                        }
                        break;
                    case WHITESPACE:
                        break;
                    default:
                        return ts.token();
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<CppTokenId> lookPreviousStatement(){
        int index = ts.index();
        int balance = 0;
        if (ts.token().id() == RPAREN){
            balance = 1;
        }
        try {
            while(ts.movePrevious()){
                switch(ts.token().id()) {
                    case LPAREN:
                        if (balance == 0) {
                            return null;
                        }
                        balance--;
                        break;
                    case RPAREN:
                        balance++;
                        break;
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        if (balance == 0) {
                            return ts.token();
                        }
                        break;
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<CppTokenId> lookNext(){
        if (ts.moveNext()) {
            Token<CppTokenId> next = ts.token();
            ts.movePrevious();
            return next;
        }
        return null;
    }

    /*package local*/ Token<CppTokenId> lookNext(int i){
        int index = ts.index();
        try {
            while(i-- > 0) {
                if (!ts.moveNext()){
                    return null;
                }
            }
            return ts.token();
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ boolean isLastLineToken(){
        int index = ts.index();
        try {
            while(true) {
                if (!ts.moveNext()){
                    return true;
                }
                DiffResult diff = diffs.getDiffs(this, 0);
                if (diff != null){
                    if (diff.replace != null){
                        if (diff.replace.hasNewLine()) {
                            return true;
                        }
                        continue;
                    }
                }
                CppTokenId id = ts.token().id();
                switch (id){
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                        if (ts.token().text().toString().indexOf('\n')>=0) {
                            return true;
                        }
                        break;
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                        return true;
                    case WHITESPACE:
                        break;
                    default:
                        return false;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<CppTokenId> lookPreviousLineImportant(){
        int index = ts.index();
        try {
            while(ts.movePrevious()){
                switch (ts.token().id()) {
                    case LINE_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                    case NEW_LINE:
                    case ESCAPED_WHITESPACE:
                    case DOXYGEN_LINE_COMMENT:
                        return null;
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                        if (ts.token().text().toString().indexOf('\n')>=0) {
                            return null;
                        }
                        break;
                    case WHITESPACE:
                        break;
                    default:
                        return ts.token();
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<CppTokenId> lookPreviousLineImportant(CppTokenId ... search){
        int index = ts.index();
        try {
            while(ts.movePrevious()){
                switch (ts.token().id()) {
                    case LINE_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                    case NEW_LINE:
                    case ESCAPED_WHITESPACE:
                    case DOXYGEN_LINE_COMMENT:
                        return null;
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                        if (ts.token().text().toString().indexOf('\n')>=0) {
                            return null;
                        }
                        break;
                    case WHITESPACE:
                        break;
                    default:
                        for(CppTokenId id : search) {
                            if (id == ts.token().id()) {
                                return ts.token();
                            }
                        }
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<CppTokenId> lookPreviousImportant(){
        int index = ts.index();
        try {
            while(ts.movePrevious()){
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        return ts.token();
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<CppTokenId> lookPreviousImportant(int i){
        int index = ts.index();
        try {
            while(ts.movePrevious()){
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        i--;
                        if (i <= 0 ) {
                            return ts.token();
                        }
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<CppTokenId> lookPrevious(){
        if (ts.movePrevious()) {
            Token<CppTokenId> previous = ts.token();
            ts.moveNext();
            return previous;
        }
        return null;
    }

    /*package local*/ Token<CppTokenId> lookPrevious(int i){
        int index = ts.index();
        try {
            while(i-- > 0) {
                if (!ts.movePrevious()){
                    return null;
                }
            }
            return ts.token();
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ boolean isFirstLineToken(){
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return true;
                }
                DiffResult diff = diffs.getDiffs(this, 0);
                if (diff != null){
                    if (diff.after != null){
                        if (diff.after.hasNewLine()) {
                            return true;
                        }
                    }
                    if (diff.replace != null){
                        if (diff.replace.hasNewLine()) {
                            return true;
                        }
                        continue;
                    }
                }
                if (ts.token().id() == NEW_LINE ||
                    ts.token().id() == PREPROCESSOR_DIRECTIVE ||
                    ts.token().id() == ESCAPED_WHITESPACE){
                    return true;
                } else if (ts.token().id() != WHITESPACE){
                    return false;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<CppTokenId> findOpenParenToken(int parenDepth) {
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return null;
                }
                if (ts.token().id() == LPAREN){
                    parenDepth--;
                    if (parenDepth == 0){
                        return lookPreviousImportant();
                    }
                } else if (ts.token().id() == RPAREN){
                    parenDepth++;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ int openParenIndent(int parenDepth) {
        return openBraceIndent(parenDepth, LPAREN, RPAREN);
    }

    /*package local*/ int openBraceIndent(int braceDepth) {
        return openBraceIndent(braceDepth, LBRACE, RBRACE);
    }

    private int openBraceIndent(int braceDepth, CppTokenId open, CppTokenId close) {
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return -1;
                }
                if (ts.token().id() == open){
                    braceDepth--;
                    if (braceDepth == 0){
                        ts.moveNext();
                        return getTokenPosition();
                    }
                } else if (ts.token().id() == close){
                    braceDepth++;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ boolean isOpenBraceLastLineToken(int braceDepth) {
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return false;
                }
                if (ts.token().id() == LBRACE){
                    braceDepth--;
                    if (braceDepth == 0){
                        return isLastLineToken();
                    }
                } else if (ts.token().id() == RBRACE){
                    braceDepth++;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ int[] getNewLinesBeforeDeclaration(int start) {
        int res[] = new int[] {-1,-1, 0};
        int index = ts.index();
        try {
            boolean hasDoc = false;
            ts.moveIndex(start);
            while(true) {
                if (!ts.movePrevious()){
                    res[0] = 0;
                    return res;
                }
                if (ts.token().id() == NEW_LINE || ts.token().id() == WHITESPACE){
                    if(res[1]==-1){
                        res[1] = ts.index();
                    }
                    res[0] = ts.index();
                } else if (ts.token().id() == BLOCK_COMMENT ||
                           ts.token().id() == DOXYGEN_COMMENT){
                    if (isFirstLineToken()) {
                        if (hasDoc) {
                            // second block comment?
                            res[2] = 1;
                            return res;
                        }
                        res[0] = -1;
                        res[1] = -1;
                        hasDoc = true;
                    } else {
                        res[2] = 1;
                        return res;
                    }
                } else if (ts.token().id() == PREPROCESSOR_DIRECTIVE){
                    if (res[0] == -1) {
                        res[0] = ts.index()+1;
                        res[1] = ts.index();
                    }
                    return res;
                } else {
                    res[2] = 1;
                    return res;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    
    /* Stab implementation */
    
    public Language<CppTokenId> language() {
        return ts.language();
    }
    
    public LanguagePath languagePath() {
        return ts.languagePath();
    }

    public Token<CppTokenId> token() {
        return ts.token();
    }

    public Token<CppTokenId> offsetToken() {
        return ts.offsetToken();
    }

    public int offset() {
        return ts.offset();
    }

    public int index() {
        return ts.index();
    }

    public <ET extends TokenId> TokenSequence<ET> embedded(Language<ET> embeddedLanguage) {
        return ts.embedded(embeddedLanguage);
    }

    public boolean moveNext() {
        return ts.moveNext();
    }

    public boolean movePrevious() {
        return ts.movePrevious();
    }

    public int moveIndex(int index) {
        return ts.moveIndex(index);
    }

    public void moveStart() {
        ts.moveStart();
    }

    public void moveEnd() {
        ts.moveEnd();
    }

    public int move(int offset) {
        return ts.move(offset);
    }
    
    public boolean isEmpty() {
        return ts.isEmpty();
    }

    public int tokenCount() {
        return ts.tokenCount();
    }

    @Override
    public String toString() {
        //return ts.toString();
        return apply(diffs, this);
    }

    /*package local*/ String apply(DiffLinkedList diffs, ExtendedTokenSequence ts) {
        int index = ts.index();
        StringBuilder buf = new StringBuilder();
        try {
            ts.moveStart();
            while(ts.moveNext()){
                buf.append(ts.token().text());
            }
            int startOffset = 0;
            int endOffset = buf.length();
            for (Diff diff : diffs.getStorage()) {
                int start = diff.getStartOffset();
                int end = diff.getEndOffset();
                String text = diff.getText(expandTabToSpaces, tabSize);
                if (startOffset > end || endOffset < start) {
                    System.err.println("What?" + startOffset + ":" + start + "-" + end);// NOI18N
                    continue;
                }
                if (endOffset < end) {
                    if (text != null && text.length() > 0) {
                        text = end - endOffset >= text.length() ? null : text.substring(0, text.length() - end + endOffset);
                    }
                    end = endOffset;
                }
                if (end - start > 0) {
                    buf.delete(start, end);
                }
                if (text != null && text.length() > 0) {
                    buf.insert(start, text);
                }
            }
            return buf.toString().replaceAll("\n", "\\\\n\n"); //NOI18N
            //return buf.toString();
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
}
