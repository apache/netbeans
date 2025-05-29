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

package org.netbeans.modules.languages.lexer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.spi.lexer.TokenPropertyProvider;


/**
 *
 * @author Jan Jancura
 */
public class SLexer implements Lexer<STokenId>, Parser.Cookie {
    
    public static final String ERROR_TOKEN_TYPE_NAME = "error";
    public static final String EMBEDDING_TOKEN_TYPE_NAME = "PE";
    public static final Object CONTINUOUS_TOKEN_START = "S";
    public static final Object CONTINUOUS_TOKEN = "C";
    public static final Object INJECTED_CODE = "I";

    
    private Language                language;
    private CharInput               input;
    private TokenFactory<STokenId>  tokenFactory;
    private Map<Integer,STokenId>   tokenIDToType;
    private Parser                  parser;
    private Object                  state;
    
    
    SLexer (
        Language                    language, 
        Map<Integer,STokenId>       tokenIDToType,
        LexerRestartInfo<STokenId>  info
    ) {
        this.language = language;
        this.tokenFactory = info.tokenFactory ();
        this.tokenIDToType = tokenIDToType;
        this.state = info.state ();
        parser = language.getParser ();
        String outerMimeType = info.languagePath ().language (0).mimeType ();
        try {
            Language outerLanguage = LanguagesManager.getDefault ().
                getLanguage (outerMimeType);
            this.input = createInputBridge (info.input (), outerLanguage);
        } catch (ParseException ex) {
            this.input = createInputBridge (info.input (), Language.create (outerMimeType));
        }
    }
    
    public Token<STokenId> nextToken () {
        int index = input.getIndex ();
        
        // HACK...
        if (state != null && "Fukala".equals (state)) {
            if (input.eof ()) return null;
            while (
                (!input.eof ()) &&
                input.next () != '\'' &&
                input.next () != '\"' &&
                input.next () != '\n' &&
                input.next () != '\r'
            ) {
                if (input.next () == '\\')
                    input.read ();
                input.read ();
            }
            input.read ();
            if (!input.eof ()) state = -1;
            if ("text/javascript".equals (language.getMimeType ()))
                return createToken (language.getTokenID ("js_string"), index);
            else
                return createToken (language.getTokenID ("css_string"), index);
        }
        // END HACK...
        
        if (state instanceof Marenka) {
            return createToken ((Marenka) state);
        }
        Object sstate = state;
        if (input.eof ()) 
            return createToken (index);
        ASTToken token = null;
        token = parser.read (this, input, language);

        // HACK...
        if (input.eof () && (
                ( token != null && 
                  "text/javascript".equals (language.getMimeType ()) &&
                  "js_error_string".equals (token.getTypeName ())
                ) ||
                (token == null && "text/css".equals (language.getMimeType ()))
        )) {
            char ch = input.getString (index, index + 1).charAt (0);
            if (ch == '\"' || ch == '\'') {
                state = "Fukala";
                if ("text/javascript".equals (language.getMimeType ()))
                    return createToken (language.getTokenID ("js_string"), index);
                else
                    return createToken (language.getTokenID ("css_string"), index);
            }
        }
        // END HACK...
        
        if (token == null) {
            if (input.getIndex () > (index + 1))
                input.setIndex (index + 1);
            else
            if (input.getIndex () == index)
                input.read ();
            return createToken (language.getTokenID (ERROR_TOKEN_TYPE_NAME), index);
        }
        
        if (state != sstate && 
            index == input.getIndex ()
        )
            // 0 length token (<script></script>
            return nextToken ();
        return createToken (token.getTypeID (), index);
    }

    public Object state () {
        return state;
    }

    public void release() {
    }

    
    // Cookie implementation ...................................................
    
    private Feature         tokenProperties;
    
    public int getState () {
        if (state == null) return -1;
        return ((Integer) state).intValue ();
    }

    public void setState (int state) {
        this.state = state;
    }

    public void setProperties (Feature tokenProperties) {
        this.tokenProperties = tokenProperties;
    }
    
    
    // other methods ...........................................................
    
    private static CharInput createInputBridge (
        LexerInput input, 
        Language language
    ) {
        Feature properties = language.getPreprocessorImport ();
        if (properties != null) {
            return new DelegatingInputBridge (
                new InputBridge (input),
                properties.getPattern ("start"),
                properties.getPattern ("end"),
                language.getTokenID (EMBEDDING_TOKEN_TYPE_NAME)
            );
        }
        return new InputBridge (input);
    }
    
    private Token<STokenId> createToken (int type, int start) {
        STokenId tokenId = tokenIDToType.get (type);
        assert tokenId != null : "Unknown token type \"" + type + "\"";
        if (!(input instanceof DelegatingInputBridge)) {
            return tokenFactory.createToken (tokenId);
        }
        List embeddings = ((DelegatingInputBridge) input).getEmbeddings ();
        if (embeddings.isEmpty ())
            return tokenFactory.createToken (tokenId);
        Map<String,Feature> imports = language.getTokenImports ();
        if (imports.containsKey (type))   
            // no preprocessor imports in token import.
            return tokenFactory.createToken (tokenId);
        Marenka marenka = new Marenka ((Integer) state);
        Object property = CONTINUOUS_TOKEN_START;
        Iterator it = embeddings.iterator ();
        while(it.hasNext ()) {
            Vojta v = (Vojta) it.next ();
            if (start < v.startOffset) {
                marenka.add (new Vojta (type, start, v.startOffset, property));
                property = CONTINUOUS_TOKEN;
            }
            marenka.add (v);
            start = v.endOffset;
        }
        if (start < input.getIndex ())
            marenka.add (new Vojta (type, start, input.getIndex (), property));
        return createToken (marenka);
    }
    
    private Token<STokenId> createToken (int start) {
        if (!(input instanceof DelegatingInputBridge)) {
            return null;
        }
        List embeddings = ((DelegatingInputBridge) input).getEmbeddings ();
        if (embeddings.isEmpty ())
            return null;
        Marenka marenka = new Marenka ((Integer) state);
        Object property = CONTINUOUS_TOKEN_START;
        Iterator it = embeddings.iterator ();
        while(it.hasNext ()) {
            Vojta v = (Vojta) it.next ();
            assert start == v.startOffset;
            marenka.add (v);
            start = v.endOffset;
        }
        assert start == input.getIndex ();
        return createToken (marenka);
    }
    
    private Token<STokenId> createToken (Marenka marenka) {
        Vojta v = marenka.removeFirst ();
        STokenId tokenId = tokenIDToType.get (v.type);
        assert tokenId != null : "Unknown type " + v.type;
        input.setIndex (v.endOffset);
        if (marenka.isEmpty ())
            this.state = marenka.getState ();
        else
            this.state = marenka;
        //S ystem.out.println("nextToken <" + v.type + "," + e (input.getString (v.startOffset, v.endOffset)) + "," + v.startOffset + "," + v.endOffset);
        if (v.property instanceof TokenProperties)
            return tokenFactory.createPropertyToken (
                tokenId,
                v.endOffset - v.startOffset,
                (TokenProperties) v.property,
                PartType.COMPLETE
            );
        else
            return tokenFactory.createPropertyToken (
                tokenId,
                v.endOffset - v.startOffset,
                new TokenPropProvider(v.property),
                PartType.COMPLETE
            );
    }
        
    private static String e (CharSequence t) {
        StringBuilder sb = new StringBuilder ();
        int i, k = t.length ();
        for (i = 0; i < k; i++) {
            if (t.charAt (i) == '\t')
                sb.append ("\\t");
            else
            if (t.charAt (i) == '\r')
                sb.append ("\\r");
            else
            if (t.charAt (i) == '\n')
                sb.append ("\\n");
            else
                sb.append (t.charAt (i));
        }
        return sb.toString ();
    }
    
    
    // innerclasses ............................................................
    
    private static final class TokenPropProvider implements TokenPropertyProvider {
        
        private final Object value;
        
        TokenPropProvider(Object value) {
            this.value = value;
        }
        
        public Object getValue (Token token, Object key) {
            if ("type".equals(key))
                return value;
            return null;
        }

    }
    
    static class TokenProperties implements TokenPropertyProvider {
        
        private Object      type;
        private int         startSkipLength;
        private int         endSkipLength;
        
        TokenProperties (
            Object          type,
            int             startSkipLength,
            int             endSkipLength
        ) {
            this.type =     type;
            this.startSkipLength = startSkipLength;
            this.endSkipLength = endSkipLength;
        }
        
        public Object getValue (Token token, Object key) {
            if ("type".equals (key)) return type;
            if ("startSkipLength".equals (key)) return startSkipLength;
            if ("endSkipLength".equals (key)) return endSkipLength;
            return null;
        }

    };
    
    static class Vojta {
        
        int         type;
        int         startOffset;
        int         endOffset;
        Object      property;
        
        Vojta (
            int     type, 
            int     startOffset, 
            int     endOffset,
            Object  property
        ) {
            this.type =         type;
            this.startOffset =  startOffset;
            this.endOffset =    endOffset;
            this.property =     property;
        }
        
        int size () {
            return endOffset - startOffset;
        }
    }
    
    static class Marenka {
        
        Integer state;
        LinkedList<Vojta> vojta = new LinkedList<Vojta> ();
        
        Marenka (Integer state) {
            this.state = state;
        }
        
        void add (Vojta vojta) {
            this.vojta.add (vojta);
        }
        
        Vojta removeFirst () {
            return vojta.removeFirst ();
        }
        
        boolean isEmpty () {
            return vojta.isEmpty ();
        }
        
        Integer getState () {
            return state;
        }
    }
}


