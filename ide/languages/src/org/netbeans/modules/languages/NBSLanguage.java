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

package org.netbeans.modules.languages;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.Pattern;


/**
 *
 * @author Jan Jancura
 */
public class NBSLanguage extends Language {
    
    static final String NBS_MIME_TYPE = "text/x-nbs";
    
    public static int WHITESPACE_ID;
    public static int COMMENT_ID;
    public static int IDENTIFIER_ID;

    private static Language     nbsLanguage;

    static Language getNBSLanguage () {
        if (nbsLanguage == null)
            nbsLanguage = new NBSLanguage ();
        return nbsLanguage;
    }

    
    private static Rule rule (String nt, Object[] right) {
        return Rule.create (
            nt, 
            Arrays.asList (right)
        );
    }
    
    
    private Parser              parser;
    private LLSyntaxAnalyser    analyser;

    
    /** Creates a new instance of Language */
    private NBSLanguage () {

        // 1) init tokens ......................................................
        
        List<TokenType> tokenTypes = new ArrayList<TokenType> ();
        try {
            tokenTypes.add (new TokenType (
                null, 
                Pattern.create (
                    "'ACTION' |" +
                    "'AST' |" +
                    "'BRACE' |" +
                    "'BUNDLE' |" +
                    "'COLOR' |" +
                    "'COMMENT_LINE' |" +
                    "'COMPLETE' |" +
                    "'COMPLETION' |" +
                    "'FOLD' |" +
                    "'FORMAT' |" +
                    "'HYPERLINK' |" +
                    "'IMPORT' |" +
                    "'INDENT' |" +
                    "'MARK' | " +
                    "'NAVIGATOR' |" +
                    "'PARSE' |" +
                    "'PROPERTIES' |" +
                    "'REFORMAT' |" +
                    "'SELECTION' | " +
                    "'SEMANTIC_CONTEXT' | " +
                    "'SEMANTIC_DECLARATION' | " +
                    "'SEMANTIC_USAGE' | " +
                    "'SKIP' |" +
                    "'SYNTAX_ERROR' |" +
                    "'TOKEN' |" +
                    "'TOOLTIP'"
                ),
                "keyword",
                1,
                null,
                1,
                null
            ));
            tokenTypes.add (new TokenType (
                null, 
                Pattern.create (
                    "['a'-'z' 'A'-'Z'] ['a'-'z' 'A'-'Z' '0'-'9' '_']*"
                ),
                "identifier",
                2,
                null,
                2,
                null
            ));
            tokenTypes.add (new TokenType (
                null, 
                Pattern.create (
                    "':' | '*' | '?' | '+' | '-' | '[' | ']' | '<' | " +
                    "'>' | '^' | '|' | '{' | '}' | '(' | ')' | ',' | " +
                    "'=' | ';' | '.' | '$'"
                ),
                "operator",
                3,
                null,
                3,
                null
            ));
            tokenTypes.add (new TokenType (
                null, 
                Pattern.create (
                    "'\\\"'" +
                    "(" +
                        "[^'\\\"' '\\\\' '\\r' '\\n'] |" +
                        "('\\\\' ['r' 'n' 't' '\\\\' '\\\'' '\\\"']) |" +
                        "('\\\\' 'u' ['0'-'9' 'a'-'f' 'A'-'F'] ['0'-'9' 'a'-'f' 'A'-'F'] ['0'-'9' 'a'-'f' 'A'-'F'] ['0'-'9' 'a'-'f' 'A'-'F'])" +
                    ")*" +
                    "'\\\"'"
                ),
                "string",
                4,
                null,
                4,
                null
            ));
            tokenTypes.add (new TokenType (
                null, 
                Pattern.create (
                    "'\\\''" +
                    "(" +
                        "[^'\\\'' '\\\\' '\\r' '\\n'] |" +
                        "('\\\\' ['r' 'n' 't' '\\\\' '\\\'' '\\\"']) |" +
                        "('\\\\' 'u' ['0'-'9' 'a'-'f' 'A'-'F'] ['0'-'9' 'a'-'f' 'A'-'F'] ['0'-'9' 'a'-'f' 'A'-'F'] ['0'-'9' 'a'-'f' 'A'-'F'])" +
                    ")*" +
                    "'\\\''"
                ),
                "string",
                4,
                null,
                4,
                null
            ));
            tokenTypes.add (new TokenType (
                null, 
                Pattern.create ("'#' [^'\\n' '\\r']* ['\\n' '\\r']+"),
                "comment",
                5,
                null,
                5,
                null
            ));
            tokenTypes.add (new TokenType (
                null, 
                Pattern.create ("'/#' - '#/'"),
                "comment",
                5,
                null,
                5,
                null
            ));
            tokenTypes.add (new TokenType (
                null, 
                Pattern.create ("['\\n' '\\r' ' ' '\\t']+"),
                "whitespace",
                6,
                null,
                6,
                null
            ));
        } catch (ParseException ex) {
            Utils.notify (ex);
        }
        tokenTypeToID.put ("error", 0);
        idToTokenType.put (0, "error");
        tokenTypeCount = 1;
        Iterator<TokenType> it = tokenTypes.iterator ();
        while (it.hasNext ()) {
            TokenType tokenType = it.next ();
            tokenTypeToID.put (tokenType.getType (), tokenType.getTypeID ());
            idToTokenType.put (tokenType.getTypeID (), tokenType.getType ());
            tokenTypeCount = Math.max (tokenTypeCount, tokenType.getTypeID () + 1);
        }
        parser = Parser.create (tokenTypes);

        
        // 2) init grammar .....................................................

        int OPERATOR_ID = getTokenID ("operator");
        ASTToken COLON = ASTToken.create (this, OPERATOR_ID, ":", 0);
        ASTToken PARENTHESIS = ASTToken.create (this, OPERATOR_ID, "(", 0);
        ASTToken PARENTHESIS2 = ASTToken.create (this, OPERATOR_ID, ")", 0);
        ASTToken BRACE = ASTToken.create (this, OPERATOR_ID, "{", 0);
        ASTToken BRACE2 = ASTToken.create (this, OPERATOR_ID, "}", 0);
        ASTToken LT = ASTToken.create (this, OPERATOR_ID, "<", 0);
        ASTToken GT = ASTToken.create (this, OPERATOR_ID, ">", 0);
        ASTToken DOT = ASTToken.create (this, OPERATOR_ID, ".", 0);
        ASTToken PLUS = ASTToken.create (this, OPERATOR_ID, "+", 0);
        ASTToken QUESTION = ASTToken.create (this, OPERATOR_ID, "?", 0);
        ASTToken MULTIPLY = ASTToken.create (this, OPERATOR_ID, "*", 0);
        ASTToken OR = ASTToken.create (this, OPERATOR_ID, "|", 0);
        ASTToken MINUS = ASTToken.create (this, OPERATOR_ID, "-", 0);
        ASTToken BRACKET = ASTToken.create (this, OPERATOR_ID, "[", 0);
        ASTToken BRACKET2 = ASTToken.create (this, OPERATOR_ID, "]", 0);
        ASTToken UPP = ASTToken.create (this, OPERATOR_ID, "^", 0);
        ASTToken EQUAL = ASTToken.create (this, OPERATOR_ID, "=", 0);
        ASTToken SEMICOLON = ASTToken.create (this, OPERATOR_ID, ";", 0);
        ASTToken COMMA = ASTToken.create (this, OPERATOR_ID, ",", 0);
        int KEYWORD_ID = getTokenID ("keyword");
        ASTToken KEYWORD = ASTToken.create (this, KEYWORD_ID, null, 0);
        ASTToken KEYWORD_TOKEN = ASTToken.create (this, KEYWORD_ID, "TOKEN", 0);
        IDENTIFIER_ID = getTokenID ("identifier");
        ASTToken IDENTIFIER = ASTToken.create (this, IDENTIFIER_ID, null, 0);
        ASTToken IDENTIFIER_I = ASTToken.create (this, IDENTIFIER_ID, "i", 0);
        int STRING_ID = getTokenID ("string");
        ASTToken STRING = ASTToken.create (this, STRING_ID, null, 0);
        WHITESPACE_ID = getTokenID ("whitespace");
        COMMENT_ID = getTokenID ("comment");

        List<Rule> rules = new ArrayList<Rule> ();
        rules.add (rule ("S", new Object[] {"token", "S"}));
        rules.add (rule ("S", new Object[] {"tokenState", "S"}));
        rules.add (rule ("S", new Object[] {"grammarRule", "S"}));
        rules.add (rule ("S", new Object[] {"command", "S"}));
        rules.add (rule ("S", new Object[] {}));

        rules.add (rule ("tokenState", new Object[] {"state", "tokenState1"}));
        rules.add (rule ("tokenState1", new Object[] {COLON, "token"}));
        rules.add (rule ("tokenState1", new Object[] {BRACE, "tokenGroup"}));
        rules.add (rule ("token", new Object[] {KEYWORD_TOKEN, COLON, IDENTIFIER, COLON, "token2"}));
        rules.add (rule ("token2", new Object[] {PARENTHESIS, "regularExpression", PARENTHESIS2, "token3"}));
        rules.add (rule ("token2", new Object[] {BRACE, "properties", BRACE2}));
        rules.add (rule ("token3", new Object[] {COLON, "state"}));
        rules.add (rule ("token3", new Object[] {}));
        rules.add (rule ("state", new Object[] {LT, IDENTIFIER, GT}));
        rules.add (rule ("tokenGroup", new Object[] {"tokensInGroup", BRACE2}));
        rules.add (rule ("tokensInGroup", new Object[] {"token", "tokensInGroup"}));
        rules.add (rule ("tokensInGroup", new Object[] {}));

        rules.add (rule ("regularExpression", new Object[] {"reChoice", "regularExpression1"}));
        rules.add (rule ("regularExpression1", new Object[] {OR, "reChoice", "regularExpression1"}));
        rules.add (rule ("regularExpression1", new Object[] {}));
        rules.add (rule ("reChoice", new Object[] {"rePart", "reChoice1"}));
        rules.add (rule ("reChoice1", new Object[] {"rePart", "reChoice1"}));
        rules.add (rule ("reChoice1", new Object[] {}));
        rules.add (rule ("rePart", new Object[] {STRING, "rePartOperatorOrMinus"}));
        rules.add (rule ("rePart", new Object[] {STRING, IDENTIFIER_I, "rePartOperatorOrMinus"}));
        rules.add (rule ("rePart", new Object[] {DOT, "rePartOperator"}));
        rules.add (rule ("rePart", new Object[] {"reClass", "rePartOperator"}));
        rules.add (rule ("rePart", new Object[] {PARENTHESIS, "regularExpression", PARENTHESIS2, "rePartOperator"}));
        rules.add (rule ("rePartOperator", new Object[] {}));
        rules.add (rule ("rePartOperator", new Object[] {PLUS}));
        rules.add (rule ("rePartOperator", new Object[] {QUESTION}));
        rules.add (rule ("rePartOperator", new Object[] {MULTIPLY}));
        rules.add (rule ("rePartOperatorOrMinus", new Object[] {MINUS, STRING}));
        rules.add (rule ("rePartOperatorOrMinus", new Object[] {"rePartOperator"}));
        rules.add (rule ("reClass", new Object[] {BRACKET, "reInClassNegation", "reInClass", BRACKET2}));
        rules.add (rule ("reInClassNegation", new Object[] {UPP}));
        rules.add (rule ("reInClassNegation", new Object[] {}));
        rules.add (rule ("reInClass", new Object[] {STRING, "reInClassMinus", "reInClass1"}));
        rules.add (rule ("reInClass1", new Object[] {STRING, "reInClassMinus", "reInClass1"}));
        rules.add (rule ("reInClass1", new Object[] {}));
        rules.add (rule ("reInClassMinus", new Object[] {MINUS, STRING}));
        rules.add (rule ("reInClassMinus", new Object[] {}));

        rules.add (rule ("grammarRule", new Object[] {IDENTIFIER, EQUAL, "grRightSide", SEMICOLON}));
        rules.add (rule ("grRightSide", new Object[] {"grChoice", "grRightSide1"}));
        rules.add (rule ("grRightSide1", new Object[] {OR, "grChoice", "grRightSide1"}));
        rules.add (rule ("grRightSide1", new Object[] {}));
        rules.add (rule ("grChoice", new Object[] {"grPart", "grChoice"}));
        rules.add (rule ("grChoice", new Object[] {}));
        rules.add (rule ("grPart", new Object[] {IDENTIFIER, "grOperator"}));
        rules.add (rule ("grPart", new Object[] {"tokenDef", "grOperator"}));
        rules.add (rule ("grPart", new Object[] {STRING, "grOperator"}));
        rules.add (rule ("grPart", new Object[] {BRACKET, "grRightSide", BRACKET2}));
        rules.add (rule ("grPart", new Object[] {PARENTHESIS, "grRightSide", PARENTHESIS2, "grOperator"}));
        rules.add (rule ("grOperator", new Object[] {PLUS}));
        rules.add (rule ("grOperator", new Object[] {MULTIPLY}));
        rules.add (rule ("grOperator", new Object[] {QUESTION}));
        rules.add (rule ("grOperator", new Object[] {}));
        rules.add (rule ("tokenDef", new Object[] {LT, IDENTIFIER, "tokenDef1", GT}));
        rules.add (rule ("tokenDef1", new Object[] {COMMA, STRING}));
        rules.add (rule ("tokenDef1", new Object[] {}));

        rules.add (rule ("command", new Object[] {KEYWORD, "command0"}));
        rules.add (rule ("command0", new Object[] {COLON, "selector", "command1"}));
        rules.add (rule ("command0", new Object[] {"value"}));
        rules.add (rule ("command1", new Object[] {COLON, "value"}));
        rules.add (rule ("command1", new Object[] {}));
        rules.add (rule ("value", new Object[] {"class"}));
        rules.add (rule ("value", new Object[] {STRING}));
        rules.add (rule ("value", new Object[] {BRACE, "properties", BRACE2}));
        rules.add (rule ("value", new Object[] {PARENTHESIS, "regularExpression", PARENTHESIS2}));
        rules.add (rule ("properties", new Object[] {"property", "properties"}));
        rules.add (rule ("properties", new Object[] {}));
        rules.add (rule ("property", new Object[] {IDENTIFIER, COLON, "propertyValue", SEMICOLON}));
        rules.add (rule ("propertyValue", new Object[] {STRING}));
        rules.add (rule ("propertyValue", new Object[] {"class"}));
        rules.add (rule ("propertyValue", new Object[] {PARENTHESIS, "regularExpression", PARENTHESIS2}));
        rules.add (rule ("selector", new Object[] {"class", "selector1"}));
        rules.add (rule ("selector1", new Object[] {COMMA, "class", "selector1"}));
        rules.add (rule ("selector1", new Object[] {}));
        rules.add (rule ("class", new Object[] {IDENTIFIER, "class1"}));
        rules.add (rule ("class1", new Object[] {DOT, IDENTIFIER, "class1"}));
        rules.add (rule ("class1", new Object[] {}));

        Set<Integer> skipTokenIDs = new HashSet<Integer> ();
        skipTokenIDs.add (getTokenID ("whitespace"));
        skipTokenIDs.add (getTokenID ("comment"));

        try {
            analyser = LLSyntaxAnalyser.create (
                this, rules, skipTokenIDs
            );
        } catch (ParseException ex) {
            Utils.notify (ex);
        }
    }
    
    public String getMimeType () {
        return NBS_MIME_TYPE;
    }

    public Parser getParser () {
        return parser;
    }
    
    public LLSyntaxAnalyser getAnalyser () {
        return analyser;
    }
    
    private FeatureList featureList = new FeatureList ();
    
    public FeatureList getFeatureList () {
        return featureList;
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
    }

    
    // ids ...
    
    private Map<String,Integer> tokenTypeToID = new HashMap<String,Integer> ();
    private Map<Integer,String> idToTokenType = new HashMap<Integer,String> ();
    private int                 tokenTypeCount = 0;

    public int getTokenID (String tokenType) {
        if (!tokenTypeToID.containsKey (tokenType))
            System.err.println ("unknown token type: " + tokenType);
        return tokenTypeToID.get (tokenType);
    }
    
    public int getTokenTypeCount () {
        return tokenTypeCount;
    }
    
    public String getTokenType (int tokenTypeID) {
        return idToTokenType.get (tokenTypeID);
    }

    private Map<String,Integer> ntToNTID;
    private Map<Integer,String> ntidToNt;

    public int getNTID (String nt) {
        if (ntidToNt == null) ntidToNt = new HashMap<Integer,String> ();
        if (ntToNTID == null) ntToNTID = new HashMap<String,Integer> ();
        if (!ntToNTID.containsKey (nt)) {
            int id = ntToNTID.size ();
            ntToNTID.put (nt, id);
            ntidToNt.put (id, nt);
        }
        return ntToNTID.get (nt);
    }
    
    public int getNTCount () {
        if (ntToNTID == null) return 0;
        return ntToNTID.size ();
    }
    
    public String getNT (int ntid) {
        return ntidToNt.get (ntid);
    }
    
    
    // imports ...
    
    public Feature getPreprocessorImport () {
        return null;
    }
    
    public Map<String,Feature> getTokenImports () {
        return Collections.<String,Feature> emptyMap ();
    }

    public List<Language> getImportedLanguages() {
        return Collections.<Language> emptyList ();
    }
}    




