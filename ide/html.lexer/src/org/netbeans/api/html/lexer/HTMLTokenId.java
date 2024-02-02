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
package org.netbeans.api.html.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.html.lexer.HtmlLexer;
import org.netbeans.lib.html.lexer.HtmlPlugins;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token ids of HTML language
 *
 * @author Jan Lahoda, Miloslav Metelka, Marek Fukala
 */
public enum HTMLTokenId implements TokenId {

    /** HTML text */
    TEXT("text"),
    /** HTML script e.g. javascript. */
    SCRIPT("script"),
    /** HTML CSS style.*/
    STYLE("style"),
    /** Whitespace in a tag: <code> &lt;BODY" "bgcolor=red&gt;</code>. */
    WS("ws"),
    /** Error token - returned in various erroneous situations. */
    ERROR("error"),
    /** HTML open tag name: <code>&lt;"BODY"/&gt;</code>.*/
    TAG_OPEN("tag"),
    /** HTML close tag name: <code>&lt;/"BODY"&gt;</code>.*/
    TAG_CLOSE("tag"),
    /** HTML tag attribute name: <code> &lt;BODY "bgcolor"=red&gt;</code>.*/
    ARGUMENT("argument"),
    /** Equals sign in HTML tag: <code> &lt;BODY bgcolor"="red&gt;</code>.*/
    OPERATOR("operator"),
    /** Attribute value in HTML tag: <code> &lt;BODY bgcolor="red"&gt;</code>.*/
    VALUE("value"),
    /** HTML javascript attribute value, such as one following onclick etc. */
    VALUE_JAVASCRIPT("value"),
    /** HTML style attribute value */
    VALUE_CSS("value"),
    /** HTML block comment: <code> &lt;!-- xxx --&gt; </code>.*/
    BLOCK_COMMENT("block-comment"),
    /** HTML/SGML comment.*/
    SGML_COMMENT("sgml-comment"),
    /** HTML/SGML declaration: <code> &lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"&gt; </code>.*/
    DECLARATION("sgml-declaration"),
    /** XML processing instruction: <? ... ?>*/
    XML_PI("xml-pi"),
    /** Character reference: <code> &amp;amp; </code>.*/
    CHARACTER("character"),
    /** End of line.*/
    EOL("text"),
    /** HTML open tag symbol: <code> "&lt;"BODY&gt; </code>.*/
    TAG_OPEN_SYMBOL("tag"),
    /** HTML close tag symbol: <code> "&lt;/"BODY&gt; </code>.*/
    TAG_CLOSE_SYMBOL("tag"),
    /**
     * Custom expression language open delimiter.
     * <pre>"{{"var}}</pre>
     * See {@link HtmlLexerELFactory#getOpenDelimiter()}.
     */
    EL_OPEN_DELIMITER("el-delimiter"),
    /**
     * Custom expression language close delimiter.
     * <pre>{{var"}}"</pre>
     * See {@link HtmlLexerELFactory#getCloseDelimiter()}.
     */
    EL_CLOSE_DELIMITER("el-delimiter"),
    /**
     * Custom expression language expression content.
     * <pre>{{"var"}}</pre>
     * See {@link HtmlLexerELFactory#getContentMimeType() }.
     */
    EL_CONTENT("el-content");

    private final String primaryCategory;
    private static final String JAVASCRIPT_MIMETYPE = "text/javascript";//NOI18N
    private static final String SCRIPT_TYPE_MODULE = "module"; //NOI18N
    private static final String BABEL_MIMETYPE = "text/babel"; //NOI18N
    private static final String STYLE_MIMETYPE = "text/css";//NOI18N
    /**
     * Property key of css value tokens determining the token type in more detail.
     * The value may either be null for common embedded
     * css code (<style>...</style> or <div style="..."/>) or "id" or "class" for
     * css id selectors (<div id="..."/> resp. class selectors (<div class="..."/>).
     * The values are defined in VALUE_CSS_TOKEN_TYPE_ID and VALUE_CSS_TOKEN_TYPE_CLASS
     * constants of this class.
     *
     * Typical usage is:
     * <code>
     *      Token<HTMLTokenId> token = ...;
     *      String val = token.getProperty(VALUE_CSS_TOKEN_TYPE_PROPERTY);
     *      if(VALUE_CSS_TOKEN_TYPE_CLASS.equals(val) {
     *          //the token represents a class selector embedded in html
     *          //class attribute of an html tag token
     *      }
     * </code>
     */
    public static final String VALUE_CSS_TOKEN_TYPE_PROPERTY = "valueCssType"; //NOI18N
    /**
     * Token's property value of VALUE_CSS_TOKEN_TYPE_PROPERTY property key marking
     * this token as an id selector.
     */
    public static final String VALUE_CSS_TOKEN_TYPE_ID = "id"; //NOI18N
    /**
     * Token's property value of VALUE_CSS_TOKEN_TYPE_PROPERTY property key marking
     * this token as a class selector.
     */
    public static final String VALUE_CSS_TOKEN_TYPE_CLASS = "class"; //NOI18N

    /**
     * Token property name for the SCRIPT tokens.
     *
     * Allows to get value of the type attribute of script tag.
     */
    public static final String SCRIPT_TYPE_TOKEN_PROPERTY = "type"; //NOI18N

    private static final Logger LOGGER = Logger.getLogger(HtmlLexer.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    HTMLTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }
    private static final Language<HTMLTokenId> language = new LanguageHierarchy<HTMLTokenId>() {

        @Override
        protected Collection<HTMLTokenId> createTokenIds() {
            return EnumSet.allOf(HTMLTokenId.class);
        }

        @Override
        protected Map<String, Collection<HTMLTokenId>> createTokenCategories() {
            //Map<String,Collection<HTMLTokenId>> cats = new HashMap<String,Collection<HTMLTokenId>>();
            // Additional literals being a lexical error
            //cats.put("error", EnumSet.of());
            return null;
        }

        @Override
        protected Lexer<HTMLTokenId> createLexer(LexerRestartInfo<HTMLTokenId> info) {
            return new HtmlLexer(info);
        }

        @Override
        protected EmbeddingPresence embeddingPresence(HTMLTokenId id) {
            switch(id) {
                case VALUE:
                case VALUE_JAVASCRIPT:
                case VALUE_CSS:
                case SCRIPT:
                case STYLE:
                case EL_CONTENT:
                    return EmbeddingPresence.ALWAYS_QUERY;
                default:
                    return super.embeddingPresence(id);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected LanguageEmbedding embedding(
                Token<HTMLTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            String mimeType = null;
            int startSkipLen = 0;
            int endSkipLen = 0;
            boolean joinSections = true;

            switch (token.id()) {
                case VALUE_JAVASCRIPT:
                    mimeType = JAVASCRIPT_MIMETYPE;

                    PartType ptype = token.partType();
                    startSkipLen = ptype == PartType.COMPLETE || ptype == PartType.START ? 1 : 0;
                    endSkipLen = ptype == PartType.COMPLETE || ptype == PartType.END ? 1 : 0;
                    //do not join css code sections in attribute value between each other, only token parts inside one value
                    joinSections = !(ptype == PartType.END || ptype == PartType.COMPLETE);
                    break;

                case VALUE_CSS:
                    //the VALUE_CSS_TOKEN_TYPE_PROPERTY property is null for the contents
                    //of style attributes and tags. These should be treated as embedded
                    //CSS and passed to the CSS lexer, while class and id attributes
                    //are treated as normal constructs
                    if ((String) token.getProperty(HTMLTokenId.VALUE_CSS_TOKEN_TYPE_PROPERTY) == null) {
                        mimeType = STYLE_MIMETYPE;

                        ptype = token.partType();
                        startSkipLen = ptype == PartType.COMPLETE || ptype == PartType.START ? 1 : 0;
                        endSkipLen = ptype == PartType.COMPLETE || ptype == PartType.END ? 1 : 0;
                        //do not join css code sections in attribute value between each other, only token parts inside one value
                        joinSections = !(ptype == PartType.END || ptype == PartType.COMPLETE);
                        break;
                    }

                case VALUE:
                    //HtmlLexerPlugin can inject a custom embdedding to html tag attributes,
                    //then the embedding mimetype is set to the value token property
                    mimeType = (String)token.getProperty(HtmlLexer.ATTRIBUTE_VALUE_EMBEDDING_MIMETYPE_TOKEN_PROPERTY_KEY);

                    ptype = token.partType();
                    startSkipLen = ptype == PartType.COMPLETE || ptype == PartType.START ? 1 : 0;
                    endSkipLen = ptype == PartType.COMPLETE || ptype == PartType.END ? 1 : 0;
                    //do not join css code sections in attribute value between each other, only token parts inside one value
                    joinSections = !(ptype == PartType.END || ptype == PartType.COMPLETE);
                    break;

                case SCRIPT:
                    String scriptType = (String)token.getProperty(SCRIPT_TYPE_TOKEN_PROPERTY);
                    if (scriptType != null) {
                        if (SCRIPT_TYPE_MODULE.equals(scriptType)) {
                            mimeType = JAVASCRIPT_MIMETYPE;
                        } else {
                            mimeType = scriptType;
                        }
                    } else {
                        mimeType = JAVASCRIPT_MIMETYPE;
                    }
                    // translate text/babel mimetype to the text/javascript
                    mimeType = BABEL_MIMETYPE.equals(mimeType) ? JAVASCRIPT_MIMETYPE : mimeType;
                    break;

                case STYLE:
                    mimeType = STYLE_MIMETYPE;
                    break;

                case EL_CONTENT:
                    Byte elContentProviderIndex = (Byte)token.getProperty(HtmlLexer.EL_CONTENT_PROVIDER_INDEX);
                    if(elContentProviderIndex != null) {
                        //set the token's mimetype
                        mimeType = HtmlPlugins.getDefault().getMimeTypes()[elContentProviderIndex];
                    }
                    break;
            }

            if (LOG) {
                LOGGER.log(Level.FINE,
                        String.format("creating embedding for %s on %s (%s)", mimeType, token.text() != null ? token.text().toString() : "no-text", token.id())); //NOI18N
            }

            if (mimeType != null) {
                if (MimePath.validate(mimeType)) {
                    //valid mimetype
                    Language lang = Language.find(mimeType);
                    if (lang == null) {
                        LOGGER.log(Level.FINE,
                                String.format("can't find language for mimetype %s!", mimeType)); //NOI18N
                        return null; //no language found
                    } else {
                        return LanguageEmbedding.create(lang, startSkipLen, endSkipLen, joinSections);
                    }
                }
            }
            return null;
        }

        @Override
        protected String mimeType() {
            return "text/html";
        }
    }.language();

    /** Gets a LanguageDescription describing a set of token ids
     * that comprise the given language.
     *
     * @return non-null LanguageDescription
     */
    public static Language<HTMLTokenId> language() {
        return language;
    }

    /**
     * Get name of primary token category into which this token belongs.
     * <br/>
     * Other token categories for this id can be defined in the language hierarchy.
     *
     * @return name of the primary token category into which this token belongs
     *  or null if there is no primary category for this token.
     */
    @Override
    public String primaryCategory() {
        return primaryCategory;
    }
}
