/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.html.editor.indent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.html.editor.lib.api.HtmlParserFactory;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.HtmlParser;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.web.indent.api.support.IndenterContextData;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public class HtmlIndenter extends MarkupAbstractIndenter<HTMLTokenId> {

    private HtmlModel model;
    
    private Map<String, Set<String>> tagsChildren = new HashMap<>();

    public HtmlIndenter(Context context) {
        super(HTMLTokenId.language(), context);
        try {
            Document doc = context.document();
            FileObject file = DataLoadersBridge.getDefault().getFileObject(doc);
            CharSequence code = doc.getText(0, doc.getLength());
            HtmlSource source = new HtmlSource(code, null, file);
            SyntaxAnalyzerResult result = SyntaxAnalyzer.create(source).analyze();
            model = result.getHtmlModel();
            HtmlVersion version = result.getHtmlVersion();            
            //workaround for [Bug 204163] [71cat] wrong formatting
            if(version == HtmlVersion.XHTML5) {
                //we do not have a special model for xhtml5, just html5 model => 
                //use xhtml1.0 model for formatting
                version = HtmlVersion.XHTML10_TRANSATIONAL;
            }
            
            model = HtmlModelFactory.getModel(version);
            
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        assert model != null;
    }

    @Override
    protected boolean isWhiteSpaceToken(Token<HTMLTokenId> token) {
        return token.id() == HTMLTokenId.WS ||
                (token.id() == HTMLTokenId.TEXT && token.text().toString().trim().length() == 0);
    }

    @Override
    protected boolean isOpenTagNameToken(Token<HTMLTokenId> token) {
        return token.id() == HTMLTokenId.TAG_OPEN ||
                (token.id() == HTMLTokenId.DECLARATION && token.text().toString().toUpperCase().startsWith("<!"));
    }

    @Override
    protected boolean isCloseTagNameToken(Token<HTMLTokenId> token) {
        return token.id() == HTMLTokenId.TAG_CLOSE;
    }

    @Override
    protected boolean isStartTagSymbol(Token<HTMLTokenId> token) {
        return (token.id() == HTMLTokenId.TAG_OPEN_SYMBOL && token.text().toString().equals("<"));
    }

    @Override
    protected boolean isStartTagClosingSymbol(Token<HTMLTokenId> token) {
        return token.id() == HTMLTokenId.TAG_OPEN_SYMBOL &&
                token.text().toString().equals("</");
    }

    @Override
    protected boolean isEndTagSymbol(Token<HTMLTokenId> token) {
        return token.id() == HTMLTokenId.TAG_CLOSE_SYMBOL &&
                token.text().toString().equals(">");
    }

    @Override
    protected boolean isEndTagClosingSymbol(Token<HTMLTokenId> token) {
        return (token.id() == HTMLTokenId.TAG_CLOSE_SYMBOL &&
                token.text().toString().equals("/>")) ||
                (token.id() == HTMLTokenId.DECLARATION && token.text().toString().startsWith(">"));
    }

    @Override
    protected boolean isTagArgumentToken(Token<HTMLTokenId> token) {
        return token.id() == HTMLTokenId.ARGUMENT;
    }

    @Override
    protected boolean isBlockCommentToken(Token<HTMLTokenId> token) {
        return token.id() == HTMLTokenId.BLOCK_COMMENT;
    }

    @Override
    protected boolean isTagContentToken(Token<HTMLTokenId> token) {
        return token.id() == HTMLTokenId.TEXT;
    }

    @Override
    protected boolean isClosingTagOptional(CharSequence tagName) {
        HtmlTag elem = model.getTag(tagName.toString());
        if (elem == null) {
            return false;
        }
        return elem.hasOptionalEndTag();
    }

    @Override
    protected boolean isOpeningTagOptional(CharSequence tagName) {
        HtmlTag elem = model.getTag(tagName.toString());
        if (elem == null) {
            return false;
        }
        return elem.hasOptionalOpenTag();
    }

    @Override
    protected Boolean isEmptyTag(CharSequence tagName) {
       HtmlTag elem = model.getTag(tagName.toString());
        if (elem == null) {
            return false;
        }
        return elem.isEmpty();
    }

    private static final String[] TAGS_WITH_UNFORMATTABLE_CONTENT = new String[]{"pre", "textarea"}; //NOI18N
    
    @Override
    protected boolean isTagContentUnformattable(CharSequence tagName) {
        for (String t : TAGS_WITH_UNFORMATTABLE_CONTENT) {
            if (t.equalsIgnoreCase(tagName.toString())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Set<String> getTagChildren(CharSequence tagName) {
        String tagNameS = tagName.toString();
        Set<String> cache = tagsChildren.get(tagNameS);
        if(cache != null) {
            return cache;
        }
        
        HtmlTag tag = model.getTag(tagNameS);
        if(tag == null) {
            return null;
        }
        Set<String> set = new HashSet<>();
        for(HtmlTag child : tag.getChildren()) {
            String name = child.getName().toUpperCase(Locale.ENGLISH); //the MarkupAbstractIndenter needs the names to be uppercase
            set.add(name);
        }
        tagsChildren.put(tagNameS, set);
        
        return set;
    }

    @Override
    protected boolean isPreservedLine(Token<HTMLTokenId> token, IndenterContextData<HTMLTokenId> context) {
        if (isBlockCommentToken(token)) {
            String comment = token.text().toString().trim();
            if (!comment.startsWith("<!--") && !comment.startsWith("-->")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getPreservedLineInitialIndentation(JoinedTokenSequence<HTMLTokenId> ts)
            throws BadLocationException {
        int[] index = ts.index();
        boolean found = false;
        do {
            if (isBlockCommentToken(ts.token())) {
                String comment = ts.token().text().toString().trim();
                if (comment.startsWith("<!--")) {
                    found = true;
                    break;
                }
            } else {
                break;
            }
        } while (ts.movePrevious());
        int indent = 0;
        if (found) {
            int lineStart = Utilities.getRowStart(getDocument(), ts.offset());
            // TODO: can comment token start with spaces?? if yes then adjust
            // column to point to first non-whitespace
            int column = ts.offset();
            indent = column - lineStart;
        }
        ts.moveIndex(index);
        ts.moveNext();
        return indent;
    }

    private boolean isOpeningTag(JoinedTokenSequence<HTMLTokenId> ts) {
        int[] index = ts.index();
        boolean found = false;
        while (ts.moveNext()) {
            if (isEndTagSymbol(ts.currentTokenSequence().token())) {
                found = true;
                break;
            } else if (isEndTagClosingSymbol(ts.currentTokenSequence().token())) {
                break;
            }
        }
        ts.moveIndex(index);
        ts.moveNext();
        return found;
    }

    @Override
    protected boolean isForeignLanguageStartToken(Token<HTMLTokenId> token, JoinedTokenSequence<HTMLTokenId> ts) {
        return isOpenTagNameToken(token) &&
                (token.text().toString().equalsIgnoreCase("style") ||
                 token.text().toString().equalsIgnoreCase("script")) && isOpeningTag(ts);
    }

    @Override
    protected boolean isForeignLanguageEndToken(Token<HTMLTokenId> token, JoinedTokenSequence<HTMLTokenId> ts) {
        return isCloseTagNameToken(token) &&
                (token.text().toString().equalsIgnoreCase("style") ||
                 token.text().toString().equalsIgnoreCase("script"));
    }

}
