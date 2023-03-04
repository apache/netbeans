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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.Pattern;


/**
 *
 * @author Jan Jancura
 */
public class TestLanguage extends Language {
    
    private String              mimeType = "test/test";
    private Parser              parser; 
    private LLSyntaxAnalyser    analyser;
    private FeatureList         featureList = new FeatureList ();
    private List<TokenType>     tokenTypes;
    private List<Rule>          rules;

    
    /** Creates a new instance of Language */
    public TestLanguage () {
    }
    
    public void addToken (int typeID, String typeName) {
        tokenTypeToID.put (typeName, typeID);
        idToTokenType.put (typeID, typeName);
        tokenTypeCount = idToTokenType.keySet ().size ();
    }
    
    public void addToken (
        int             typeID, 
        String          typeName,
        Pattern         pattern,
        String          startState,
        String          endState,
        int             priority,
        Feature         properties
    ) {
        tokenTypeToID.put (typeName, typeID);
        idToTokenType.put (typeID, typeName);
        tokenTypeCount = idToTokenType.keySet ().size ();
        if (tokenTypes == null) tokenTypes = new ArrayList<TokenType> ();
        tokenTypes.add (new TokenType (startState, pattern, typeName, typeID, endState, priority, properties));
    }
    
    public void addRule (String nt, List rightSide) {
        if (rules == null) rules = new ArrayList<Rule> ();
        rules.add (Rule.create (nt, rightSide));
    }
    
    public void addFeature (Feature feature) {
        featureList.add (feature);
    }
    
    public String getMimeType () {
        return mimeType;
    }

    public Parser getParser () {
        if (tokenTypes != null && parser == null)
            parser = Parser.create (tokenTypes);
        return parser;
    }
    
    public LLSyntaxAnalyser getAnalyser () {
        if (rules != null && analyser == null) {
            Set<Integer> skipTokenIDs = new HashSet<Integer> ();
            Iterator<Feature> it = featureList.getFeatures ("SKIP").iterator ();
            while (it.hasNext()) {
                Feature feature = it.next();
                if (feature.getFeatureName ().equals ("SKIP")) {
                    skipTokenIDs.add (tokenTypeToID.get (feature.getSelector ().toString ()));
                }
            }
            try {
                analyser = LLSyntaxAnalyser.create (this, rules, skipTokenIDs);
            } catch (ParseException ex) {
                ex.printStackTrace ();
            }
        }
        return analyser;
    }
    
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




