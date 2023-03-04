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

import java.awt.Point;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;
import java.util.Iterator;

import org.netbeans.modules.languages.parser.SyntaxError;
import org.openide.filesystems.FileObject;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.api.languages.ASTToken;

import org.netbeans.modules.languages.Feature.Type;
import org.netbeans.modules.languages.lexer.SLexer;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.modules.languages.parser.StringInput;
import org.netbeans.modules.languages.parser.TokenInputUtils;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
 
    
/**
 *
 * @author Jan Jancura
 */
public class NBSLanguageReader {
    
    public static NBSLanguageReader create (
        FileObject  fo, 
        String      mimeType
    ) throws IOException {
        return create (fo.getInputStream (), fo.getPath (), mimeType);
    }
    
    public static NBSLanguageReader create (
        InputStream is, 
        String      sourceName, 
        String      mimeType
    ) {
        return new NBSLanguageReader (is, sourceName, mimeType);
    }
    
    public static NBSLanguageReader create (
        String      source, 
        String      sourceName, 
        String      mimeType
    ) {
        return new NBSLanguageReader (source, sourceName, mimeType);
    }
    
    
    private String          source;
    private InputStream     inputStream;
    private String          sourceName;
    private String          mimeType;
    private GRNode          grammarTree;
    private List<TokenType> tokenTypes;
    private boolean         containsTokens = false;
    private Map<String,Integer> tokenTypeToID = new HashMap<String,Integer> ();
    private List<Feature>   features;
    private List<Rule>      grammarRules;
    
    
    {
        tokenTypes = new ArrayList<TokenType> ();
        addToken (null, null, SLexer.ERROR_TOKEN_TYPE_NAME, null, null);
        addToken (null, null, SLexer.EMBEDDING_TOKEN_TYPE_NAME, null,null);
        addToken (null, null, LLSyntaxAnalyser.GAP_TOKEN_TYPE_NAME, null, null);

        //HACK adding new token typed does not work because
        // token hierarchy is not updated when TokenProvider fires change
        // so we reserved some default token types at least there
        addToken (null, null, "string", null, null);
        addToken (null, null, "character", null, null);
        addToken (null, null, "identifier", null, null);
        addToken (null, null, "whitespace", null, null);
        addToken (null, null, "number", null, null);
        addToken (null, null, "keyword", null, null);
        addToken (null, null, "comment", null, null);
        addToken (null, null, "operator", null, null);
        containsTokens = false;
    }
    
    private NBSLanguageReader (
        String source, 
        String sourceName, 
        String mimeType
    ) {
        this.source = source;
        this.sourceName = sourceName;
        this.mimeType = mimeType;
    }
    
    private NBSLanguageReader (
        InputStream inputStream, 
        String sourceName, 
        String mimeType
    ) {
        this.inputStream = inputStream;
        this.sourceName = sourceName;
        this.mimeType = mimeType;
    }
    
    public List<TokenType> getTokenTypes () throws ParseException, IOException {
        if (features == null) readNBS ();
        return tokenTypes;
    }
    
    public boolean containsTokens () throws ParseException, IOException {
        if (features == null) readNBS ();
        return containsTokens;
    }
    
    public List<Feature> getFeatures () throws ParseException, IOException {
        if (features == null) readNBS ();
        return features;
    }
    
    public List<Rule> getRules (Language language) throws ParseException {
        if (grammarRules == null)
            grammarRules = createRules (grammarTree, language);
        return grammarRules;
//        Set<Integer> skipTokenIDs = new HashSet<Integer> ();
//        Iterator<String> it2 = skipTokenTypes.iterator ();
//        while (it2.hasNext ()) {
//            String tokenType = it2.next ();
//            skipTokenIDs.add (language.getTokenID (tokenType));
//        }
//        //AnalyserAnalyser.printRules (rules, null);
//        language.setAnalyser (LLSyntaxAnalyser.create (
//            language, rules, skipTokenIDs
//        ));
    }

    
    // private methods .........................................................
    
    
    private void readNBS () throws ParseException, IOException {
        if (source == null) {
            BufferedReader reader = null;
            try {
                InputStreamReader r = new InputStreamReader (inputStream);
                reader = new BufferedReader (r);
                StringBuilder sb = new StringBuilder ();
                String line = reader.readLine ();
                while (line != null) {
                    sb.append (line).append ('\n');
                    line = reader.readLine ();
                }
                source = sb.toString ();
            } finally {
                if (reader != null)
                    reader.close ();
            }
        }
        features = new ArrayList<Feature> ();
        CharInput input = new StringInput (source);
        ASTNode node = null;
        TokenInput tokenInput = null;
        try {
            Language nbsLanguage = NBSLanguage.getNBSLanguage ();
            tokenInput = TokenInputUtils.create (
                nbsLanguage,
                nbsLanguage.getParser (), 
                input
            );
            node = nbsLanguage.getAnalyser ().read (
                tokenInput, 
                false, 
                new ArrayList<SyntaxError> (),
                new boolean[] {false}
            );
        } catch (ParseException ex) {
            //ex.printStackTrace ();
            Point p = Utils.findPosition (source, tokenInput.getOffset ());
            throw new ParseException (
                sourceName + " " + 
                p.x + "," + p.y + ": " + 
                ex.getMessage ()
            );
        }
        readBody (node);
    }
    
    private void readBody (
        ASTNode     root
    ) throws ParseException {
        grammarTree = new GRNode ();
        Set<String> skipTokenTypes = new HashSet<String> ();
        Iterator<ASTItem> it = root.getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem item = it.next ();
            if (item instanceof ASTToken) continue;
            ASTNode node = (ASTNode) item;
            if (node.getNT ().equals ("token"))
                readToken (node, null);
            else
            if (node.getNT ().equals ("tokenState"))
                readTokenState (node);
            else
            if (node.getNT ().equals ("grammarRule"))
                readGrammarRule (node, grammarTree);
            else
            if (node.getNT ().equals ("command"))
                readCommand (node, skipTokenTypes);
            else
                throw new ParseException (
                    "Unknown grammar rule (" + node.getNT () + ")."
                );
        }
        //S ystem.out.println(grammarRules);
    }
    
    private void readToken (
        ASTNode     node, 
        String      state
    ) throws ParseException {
        String startState = null;
        String endState = null;
        Pattern pattern = null;
        Feature properties = null;
        String name  = node.getTokenType ("identifier").getIdentifier ();
        ASTNode pnode = node.getNode ("token2.properties");
        if (pnode != null) {
            properties = readProperties (null, null, pnode);
//            startState = getString (properties, "start_state", false);
//            endState = getString (properties, "end_state", false);
//            pattern = (Pattern) properties.get ("pattern");
            startState = (String) properties.getValue ("start_state");
            endState = (String) properties.getValue ("end_state");
            pattern = properties.getPattern ("pattern");
            if (pattern == null && properties.getType("call") == Type.METHOD_CALL)
                pattern = Pattern.create (".");
        } else {
            ASTNode regularExpressionNode = node.getNode ("token2.regularExpression");
            endState = node.getTokenTypeIdentifier ("token2.token3.state.identifier");
            pattern = readPattern (regularExpressionNode, regularExpressionNode.getOffset ());
        }
        if (startState != null && state != null) 
            throw new ParseException ("Start state should not be specified inside token group block!");
        if (startState == null) startState = state;
        if (endState == null) endState = state;
        addToken (
            startState,
            pattern,
            name,
            endState,
            properties
        );
    }
    
    private void addToken (
        String startState,
        Pattern pattern,
        String typeName,
        String endState,
        Feature properties
    ) {
        containsTokens = true;
        int id = tokenTypeToID.size ();
        if (tokenTypeToID.containsKey (typeName))
            id = tokenTypeToID.get (typeName);
        else
            tokenTypeToID.put (typeName, id);
        tokenTypes.add (new TokenType (
            startState,
            pattern,
            typeName,
            id,
            endState,
            tokenTypes.size (),
            properties
        ));
    }
    
    private void readGrammarRule (
        ASTNode             node, 
        GRNode              grammarRules
    ) {
        String nt = node.getTokenTypeIdentifier ("identifier");
        ASTNode rightSide = node.getNode ("grRightSide");
        if (rightSide.getChildren ().size () == 0) {
            grammarRules.get (nt).setFinal ();
        } else
            resolveGrammarRule (nt, rightSide, new Franta (), grammarRules, null);
    }
    
    private static GRNode resolveGrammarRule (
        String              nt, 
        ASTNode             rightSide, 
        Franta              franta, 
        GRNode              ntToMap,
        GRNode              right
    ) {
        Iterator it = rightSide.getChildren ().iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof ASTToken) continue;
            ASTNode n = (ASTNode) o;
            if (n.getNT ().equals ("grRightSide1"))
                resolveGrammarRule (nt, n, franta, ntToMap, right);
            else
            if (n.getNT ().equals ("grChoice")) {
                right = resolveGrammarRule (nt, n, franta, ntToMap, ntToMap.get (nt));
                right.setFinal ();
            } else
            if (n.getNT ().equals ("grPart")) {
              List<ASTItem> ch = n.getChildren ();
              int i = 0;
              while (i < ch.size () && skip (ch.get (i))) i++;
              if (ch.get (i) instanceof ASTNode) {
                  String token = readToken ((ASTNode) ch.get (i));
                  i++;
                  while (i < ch.size () && skip (ch.get (i))) i++;
                  if (i < ch.size ()) {
                      String op = ((ASTNode) ch.get (i)).getTokenTypeIdentifier ("operator");
                      if (op != null) {
                          String nt1 = franta.next (nt);
                          right = right.get (nt1);
                          if ("*".equals (op)) {
                              GRNode right1 = ntToMap.get (nt1);
                              right1.setFinal ();
                              right1 = right1.get (token);
                              right1 = right1.get (nt1);
                              right1.setFinal ();
                          } else
                          if ("+".equals (op)) {
                              GRNode right1 = ntToMap.get (nt1);
                              String nt2 = franta.next (nt);
                              right1 = right1.get (token);
                              right1 = right1.get (nt2);
                              right1.setFinal ();
                              GRNode right2 = ntToMap.get (nt2);
                              right2.setFinal ();
                              right2 = right2.get (token);
                              right2 = right2.get (nt2);
                              right2.setFinal ();
                          }
                      } else
                          right = right.get (token);
                  } else
                      right = right.get (token);
                  continue;
              }
              ASTToken t = (ASTToken) ch.get (i);
              if (t.getIdentifier ().equals ("(")) {
                  String op = n.getTokenTypeIdentifier ("grOperator.operator");
                  String nt1 = franta.next (nt);
                  right = right.get (nt1);
                  ASTNode nn = n.getNode ("grRightSide");
                  if ("*".equals (op)) {
                      GRNode right1 = ntToMap.get (nt1);
                      String nt2 = franta.next (nt);
                      right1.setFinal ();
                      right1 = right1.get (nt2);
                      right1 = right1.get (nt1);
                      right1.setFinal ();
                      GRNode right2 = ntToMap.get (nt2);
                      resolveGrammarRule (nt2, nn, franta, ntToMap, right2);
                  } else
                  if ("+".equals (op)) {
                      GRNode right1 = ntToMap.get (nt1);
                      String nt2 = franta.next (nt);
                      String nt3 = franta.next (nt);
                      //right1.put (null, null);
                      right1 = right1.get (nt2);
                      right1 = right1.get (nt3);
                      right1.setFinal ();
                      GRNode right3 = ntToMap.get (nt3);
                      right3.setFinal ();
                      right3 = right3.get (nt2);
                      right3 = right3.get (nt3);
                      right3.setFinal ();
                      GRNode right2 = ntToMap.get (nt2);
                      resolveGrammarRule (nt2, nn,  franta, ntToMap, right2);
                  } else {
                      GRNode right1 = ntToMap.get (nt1);
                      resolveGrammarRule (nt1, nn, franta, ntToMap, right1);
                  }
              } else
              if (t.getIdentifier ().equals ("[")) {
                  String nnt = franta.next (nt);
                  right = right.get (nnt);
                  ASTNode nn = n.getNode ("grRightSide");
                  resolveGrammarRule (nnt, nn, franta, ntToMap, null);
                  ntToMap.get (nnt).setFinal ();
              } else {
                  i++;
                  while (i < ch.size () && skip (ch.get (i))) i++;
                  if (i < ch.size ()) {
                      String op = ((ASTNode) ch.get (i)).getTokenTypeIdentifier ("operator");
                      if (op != null) {
                          String nt1 = franta.next (nt);
                          right = right.get (nt1);
                          if ("*".equals (op)) {
                              GRNode right1 = ntToMap.get (nt1);
                              right1.setFinal ();
                              right1 = right1.get (t.getIdentifier ());
                              right1 = right1.get (nt1);
                              right1.setFinal ();
                          } else
                          if ("+".equals (op)) {
                              GRNode right1 = ntToMap.get (nt1);
                              String nt2 = franta.next (nt);
                              right1 = right1.get (t.getIdentifier ());
                              right1 = right1.get (nt2);
                              right1.setFinal ();
                              GRNode right2 = ntToMap.get (nt2);
                              right2.setFinal ();
                              right2 = right2.get (t.getIdentifier ());
                              right2 = right2.get (nt2);
                              right2.setFinal ();
                          }
                      } else
                          right = right.get (t.getIdentifier ());
                  } else
                      right = right.get (t.getIdentifier ());
              }
            }
        }
        return right;
    }
    
    private static boolean skip (ASTItem item) {
        if (item instanceof ASTNode) return false;
        int type = ((ASTToken) item).getTypeID ();
        if (NBSLanguage.WHITESPACE_ID == type) return true;
        return NBSLanguage.COMMENT_ID == type;
    }
    
    private static String readToken (ASTNode node) {
        StringBuilder sb = new StringBuilder ();
        String type = node.getTokenTypeIdentifier ("identifier");
        if (type != null) sb.append (type);
        sb.append ('#');
        String identifier = node.getTokenTypeIdentifier 
            ("tokenDef1.string");
        if (identifier != null) sb.append (identifier);
        return sb.toString ();
    }

    private List<Rule> createRules (
        GRNode              grammar,
        Language            language
    ) throws ParseException {
        List<Rule> rules = new ArrayList<Rule> ();
        Iterator it = grammar.names ().iterator ();
        while (it.hasNext ()) {
            String nt = (String) it.next ();
            GRNode right = grammar.get (nt);
            resolveNT (nt, 0, right, new ArrayList (), rules, language);
        }
        return rules;
    }
    
    private void resolveNT (
        String              nt, 
        int                 id, 
        GRNode              grNode, 
        List<String>        right, 
        List<Rule>          rules,
        Language            language
    ) throws ParseException {
        do {
            Set<String> names = grNode.names ();
            if (!grNode.isFinal () && names.isEmpty ())
                throw new IllegalArgumentException ();
            if (grNode.isFinal ())
                addRule (nt, id, new ArrayList (right), rules);
            if (names.isEmpty ())
                return;
            if (names.size () > 1)
                break;
            String name = names.iterator ().next ();
            addItem (right, name, language);
            grNode = grNode.get (name);
        } while (true);
        if (!right.isEmpty ()) {
            right.add (nt + "#" + (id + 1));
            addRule (nt, id, right, rules); 
            id ++;
            Iterator<String> it = grNode.names ().iterator ();
            while (it.hasNext ()) {
                String name = it.next ();
                right = new ArrayList ();
                addItem (right, name, language);
                resolveNT (nt, id, grNode.get (name), right, rules, language);
            }
        } else {
            Iterator<String> it = grNode.names ().iterator ();
            while (it.hasNext ()) {
                String name = it.next ();
                right = new ArrayList ();
                addItem (right, name, language);
                resolveNT (nt, id, grNode.get (name), right, rules, language);
            }
        }
        
        
//        if (grNode.names ().isEmpty ()) {
//            addRule (nt, id, right, rules);
//            return;
//        }
//        while (grNode.names ().size () == 1 ) {
//            String n = (String) grNode.names ().iterator ().next ();
//            addItem (language, right, n);
//            grNode = grNode.get (n);
//        }
//        if (!right.isEmpty ()) {
//            right.add (nt + "#" + (id + 1));
//            addRule (nt, id, right, rules); 
//            id ++;
//        }
//        Iterator<String> it = grNode.names ().iterator ();
//        while (it.hasNext ()) {
//            String n = it.next ();
//            right = new ArrayList ();
//            addItem (language, right, n);
//            resolveNT (language, nt, id, (Map<String,Map>) grNode.get (n), right, rules);
//        }
    }
    
    private void addItem (
        List            l, 
        String          n,
        Language        language
    ) throws ParseException {
        if (n.startsWith ("\"") || n.startsWith ("'")) {
            l.add (ASTToken.create (
                language,
                -1,
                n.substring (1, n.length () - 1),
                0
            ));
            return;
        }
        int i = n.indexOf ('#');
        if (i < 0) {
            l.add (n); 
            return;
        }
        String type = n.substring (0, i);
        int typeID = type.length () > 0 ? language.getTokenID (type) : -1;
        if (typeID < 0) {
            throw new ParseException (
                sourceName + ": Token '" + type + "' not defined!"
            );
        }
        i++;
        String identifier = n.substring (i);
        if (identifier.length () > 0)
            identifier = identifier.substring (1, identifier.length () - 1);
        l.add (ASTToken.create (
            language,
            typeID,
            identifier.length () > 0 ? identifier : null,
            0
        ));
    }
    
    private static void addRule (String nt, int id, List right, List<Rule> rules) {
        if (id > 0)
            nt += "#" + id;
        rules.add (Rule.create (
            nt,
            right
        ));
    }
    
    private void readTokenState (
        ASTNode     node
    ) throws ParseException {
        String startState = node.getTokenTypeIdentifier ("state.identifier");
        ASTNode n = node.getNode ("tokenState1.token");
        if (n != null)
            readToken (n, startState);
        else
            readTokenGroup (node.getNode ("tokenState1.tokenGroup"), startState);
    }
    
    private void readTokenGroup (
        ASTNode     node, 
        String      startState
    ) throws ParseException {
        Iterator it = node.getNode ("tokensInGroup").getChildren ().iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof ASTToken) continue;
            ASTNode n = (ASTNode) o;
            readToken (n, startState);
        }
    }
    
    private void readCommand (
        ASTNode     commandNode, 
        Set<String> skipTokenTypes
    ) throws ParseException {
        String keyword = commandNode.getTokenTypeIdentifier ("keyword");
        ASTNode command0Node = commandNode.getNode ("command0");
        ASTNode selectorNode = command0Node.getNode ("selector");
        if (selectorNode != null) {
            //ASTNode classNode = selectorNode.getNode ("class");
            Iterator<Selector> it = readSelector (selectorNode).iterator ();
            while (it.hasNext ()) {
                Selector selector =  it.next ();
                ASTNode command1Node = command0Node.getNode ("command1");
                ASTNode valueNode = command1Node.getNode ("value");
                if (valueNode != null)
                    features.add (readValue (keyword, selector, valueNode));
                else {
//                    if (keyword.equals ("SKIP"))
//                        skipTokenTypes.add (selector.getAsString ());
//                    else
                        features.add (Feature.create (keyword, selector));
                }
            }
        } else {
            ASTNode valueNode = command0Node.getNode ("value");
            features.add (readValue (keyword, null, valueNode));
        }
    }
    
    private Feature readValue (
        String      keyword,
        Selector    selector,
        ASTNode     valueNode
    ) throws ParseException {
        ASTNode propertiesNode = valueNode.getNode ("properties");
        if (propertiesNode != null)
            return readProperties (keyword, selector, propertiesNode);
        ASTNode classNode = valueNode.getNode ("class");
        if (classNode != null)
            return Feature.createMethodCallFeature (keyword, selector, readClass (classNode));
        ASTNode regExprNode = valueNode.getNode ("regularExpression");
        if (regExprNode != null) {
            Pattern pat = readPattern (regExprNode, regExprNode.getOffset());
            return Feature.createExpressionFeature (keyword, selector, pat);
        }
        String s = valueNode.getTokenTypeIdentifier ("string");
        s = s.substring (1, s.length () - 1);
        return Feature.createExpressionFeature (keyword, selector, c (s));
    }
    
    private Feature readProperties (
        String      keyword,
        Selector    selector,
        ASTNode     node
    ) throws ParseException {
        Map<String,String> methods = new HashMap<String,String> ();
        Map<String,String> expressions = new HashMap<String,String> ();
        Map<String,Pattern> patterns = new HashMap<String,Pattern> ();
        
        Iterator it = node.getChildren ().iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof ASTToken) continue;
            ASTNode n = (ASTNode) o;
            String key = n.getTokenTypeIdentifier ("identifier");
            String value = n.getTokenTypeIdentifier ("propertyValue.string");
            if (value != null) {
                value = value.substring (1, value.length () - 1);
                expressions.put (key, c (value));
            } else 
            if (n.getNode ("propertyValue.class") != null) {
                value = readClass (n.getNode ("propertyValue.class"));
                methods.put (key, value);
            } else {
                ASTNode regularExpressionNode = n.getNode ("propertyValue.regularExpression");
                Pattern pattern = readPattern (regularExpressionNode, n.getOffset ());
                patterns.put (key, pattern);
            }
        }
        return Feature.create (keyword, selector, expressions, methods, patterns);
    }
    
    
    private static List<Selector> readSelector (ASTNode selectorNode) {
        return readSelector (selectorNode, new ArrayList<Selector> ());
    }
    
    private static List<Selector> readSelector (ASTNode selectorNode, List<Selector> result) {
        Iterator<ASTItem> it = selectorNode.getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem item =  it.next ();
            if (item instanceof ASTNode) {
                ASTNode node = (ASTNode) item;
                if (node.getNT ().equals ("class"))
                    result.add (Selector.create (readClass (node)));
                else
                if (node.getNT ().equals ("selector1"))
                    readSelector (node, result);
            }
        }
        return result;
    }
    
    private static String readClass (ASTNode cls) {
        StringBuilder sb = new StringBuilder ();
        sb.append (cls.getTokenTypeIdentifier ("identifier"));
        Iterator<ASTItem> it = cls.getNode ("class1").getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTToken token = (ASTToken) it.next ();
            if (token.getIdentifier ().equals ("."))
                sb.append ('.');
            else
            if (token.getTypeID () == NBSLanguage.IDENTIFIER_ID)
                sb.append (token.getIdentifier ());
        }
        return sb.toString ();
    }
    
    private Pattern readPattern (
        ASTNode     node, 
        int         offset
    ) throws ParseException {
        StringBuilder sb = new StringBuilder ();
        getText (node, sb);
        String pattern = sb.toString ();
        StringInput input = new StringInput (pattern);
        try {
            return Pattern.create (input);
        } catch (ParseException e) {
            Point p = Utils.findPosition (source, offset + input.getIndex ());
            throw new ParseException (
                sourceName + " " + 
                p.x + "," + p.y + ": " + 
                e.getMessage ()
            );
        }
    }
    
    private static void getText (ASTItem item, StringBuilder sb) {
        Iterator<ASTItem> it = item.getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem elem = it.next ();
            if (elem instanceof ASTNode)
                getText (elem, sb);
            else {
                ASTToken token = (ASTToken) elem;
                int typeID = token.getTypeID ();
                if (typeID == NBSLanguage.COMMENT_ID || 
                    typeID == NBSLanguage.WHITESPACE_ID
                ) 
                    continue;
                sb.append (token.getIdentifier ());
            }
        }
    }
    
    private static String c (String s) {
        s = s.replace ("\\n", "\n");
        s = s.replace ("\\r", "\r");
        s = s.replace ("\\t", "\t");
        s = s.replace ("\\\"", "\"");
        s = s.replace ("\\\'", "\'");
        s = s.replace ("\\\\", "\\");
        return s;
    }
    
    private static class GRNode {
        
        private boolean isFinal = false;
        
        private Map<String,GRNode> map;
        
        GRNode get (String name) {
            if (map == null) 
                map = new HashMap<String,GRNode> ();
            GRNode result = map.get (name);
            if (result == null) {
                result = new GRNode ();
                map.put (name, result);
            }
            return result;
        }
        
        Set<String> names () {
            if (map == null) return Collections.<String>emptySet ();
            return map.keySet ();
        }
        
        void setFinal () {
            isFinal = true;
        }
        
        boolean isFinal () {
            return isFinal;
        }
        
        void put (String name, GRNode node) {
            if (map == null) map = new HashMap<String,GRNode> ();
            map.put (name, node);
        }
        
        public String toString () {
            StringBuilder sb = new StringBuilder ();
            toString (sb, null);
            return sb.toString ();
        }
        
        private void toString (StringBuilder sb, StringBuilder prefix) {
            if (isFinal) sb.append (prefix).append ('\n');
            if (!isFinal && map == null) sb.append (prefix).append ('?').append ('\n');
            if (map == null) return;
            Iterator<String> it = map.keySet ().iterator ();
            while (it.hasNext ()) {
                String name = it.next ();
                if (prefix == null)
                    map.get (name).toString (sb, new StringBuilder (name).append (" ="));
                else
                    map.get (name).toString (sb, new StringBuilder (prefix).append (' ').append (name));
            }
        }
    }
    
    static class Franta {
        private int i = 1;
        
        String next (String nt) {
            return nt + '$' + i++;
        }
    }
}
