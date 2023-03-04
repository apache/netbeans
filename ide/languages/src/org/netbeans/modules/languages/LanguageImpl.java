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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;

import javax.swing.event.EventListenerList;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.modules.languages.parser.Parser;


/**
 *
 * @author Jan Jancura
 */
public class LanguageImpl extends Language {

    public static final String IMPORT_FEATURE = "IMPORT";
    
    
    private NBSLanguageReader   reader;
    private String              mimeType;
    private Parser              parser;
    private LLSyntaxAnalyser    analyser;
    private FeatureList         featureList = new FeatureList ();

    
    /** Creates a new instance of Language */
    public LanguageImpl (
        String                  mimeType,
        NBSLanguageReader       reader
    ) {
        this.mimeType = mimeType;
        this.reader = reader;
    }
    
    
    // public methods ..........................................................
    
    public String getMimeType () {
        return mimeType;
    }

    public Parser getParser () {
        return parser;
    }
    
    public LLSyntaxAnalyser getAnalyser () {
        if (analyser == null)
            analyser = LLSyntaxAnalyser.createEmpty (this);
        return analyser;
    }
    
    public FeatureList getFeatureList () {
        return featureList;
    }
    
    private EventListenerList listenerList = new EventListenerList ();
    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        listenerList.add (PropertyChangeListener.class, l);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener l) {
        listenerList.remove (PropertyChangeListener.class, l);
    }
    
    // ids ...
    
    private Map<String,Integer> tokenTypeToID;
    private Map<Integer,String> idToTokenType;
    private int                 tokenTypeCount = 0;

    public int getTokenID (String tokenType) {
        if (!tokenTypeToID.containsKey (tokenType))
            return -1;
        return tokenTypeToID.get (tokenType);
    }
    
    public int getTokenTypeCount () {
        return tokenTypeCount;
    }
    
    public String getTokenType (int tokenTypeID) {
        if (idToTokenType == null) return null;
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
    
    private Feature             preprocessorImport;
    private Map<String,Feature> tokenImports = new HashMap<String,Feature> ();
    private List<Language>      importedLangauges = new ArrayList<Language> ();
    
    public Feature getPreprocessorImport () {
        return preprocessorImport;
    }
    
    public Map<String,Feature> getTokenImports () {
        return tokenImports;
    }
    
    public List<Language> getImportedLanguages () {
        return importedLangauges;
    }
    
    void importLanguage (
        Feature feature
    ) {
        String mimeType = (String) feature.getValue ("mimeType");
        if (feature.getPattern ("start") != null) {
            //feature.put ("token", Language.EMBEDDING_TOKEN_TYPE_NAME);
            assert (preprocessorImport == null);
            preprocessorImport = feature;
            try {
                importedLangauges.add (LanguagesManager.getDefault ().getLanguage (mimeType));
            } catch (LanguageDefinitionNotFoundException ex) {
                importedLangauges.add (Language.create (mimeType));
            }
            return;
        }
        if (feature.getValue ("state") == null) {
            String tokenName = feature.getSelector ().getAsString ();
            assert (!tokenImports.containsKey (tokenName));
            tokenImports.put (tokenName, feature);
            try {
                importedLangauges.add (LanguagesManager.getDefault ().getLanguage (mimeType));
            } catch (LanguageDefinitionNotFoundException ex) {
                importedLangauges.add (Language.create (mimeType));
            }
            return;
        }
        try {
            Language language = LanguagesManager.getDefault ().getLanguage (mimeType);

            String state = (String) feature.getValue ("state"); 
            String tokenName = feature.getSelector ().getAsString ();

            // import tokenTypes
    //!!            Iterator<TokenType> it = language.getTokenTypes ().iterator ();
    //            while (it.hasNext ()) {
    //                TokenType tt = it.next ();
    //                String startState = tt.getStartState ();
    //                Pattern pattern = tt.getPattern ().clonePattern ();
    //                String endState = tt.getEndState ();
    //                if (startState == null || Parser.DEFAULT_STATE.equals (startState)) 
    //                    startState = state;
    //                else
    //                    startState = tokenName + '-' + startState;
    //                if (endState == null || Parser.DEFAULT_STATE.equals (endState)) 
    //                    endState = state;
    //                else
    //                    endState = tokenName + '-' + endState;
    //                //!!addToken (startState, tt.getType (), pattern, endState, tt.getProperties ());
    //            }

            // import grammar rues
            if (language.getAnalyser () != null)
                try {
                    analyser = LLSyntaxAnalyser.create (
                        this, 
                        language.getAnalyser ().getRules (), 
                        language.getAnalyser ().getSkipTokenTypes ()
                    );
                } catch (ParseException ex) {
                    ex.printStackTrace ();
                }
            // import features
            featureList.importFeatures (language.getFeatureList ());
            importedLangauges.addAll (language.getImportedLanguages ());
            tokenImports.putAll (language.getTokenImports ());
        } catch (LanguageDefinitionNotFoundException ex) {
            Utils.notify ("Editors/" + mimeType + "/language.nbs:", ex);
        }
    }
    
    
    // other methods ...........................................................
    
    public void read (NBSLanguageReader reader) throws ParseException, IOException {
        this.reader = reader;
        read ();
    }
    
    private Object INIT_LOCK = new Object ();
    
    public ASTNode parse (InputStream is) throws IOException, ParseException {
        synchronized (INIT_LOCK) {
            if (tokenTypeToID == null) {
                try {
                    INIT_LOCK.wait ();
                } catch (InterruptedException ex) {
                }
            }
            return super.parse (is);
        }
    }
    
    public void read () throws ParseException, IOException {
        try {
            tokenTypeToID = new HashMap<String, Integer> ();
            idToTokenType = new HashMap<Integer, String> ();
            featureList = new FeatureList ();
            if (!reader.containsTokens ()) {
                org.netbeans.api.lexer.Language lexerLanguage = org.netbeans.api.lexer.Language.find (getMimeType ());
                if (lexerLanguage != null) {
                    Iterator it = lexerLanguage.tokenIds ().iterator ();
                    while (it.hasNext()) {
                        TokenId tokenId = (TokenId) it.next();
                        int id = tokenId.ordinal ();
                        String name = tokenId.name ();
                        idToTokenType.put (id, name);
                        tokenTypeToID.put (name, id);
                        tokenTypeCount = Math.max (tokenTypeCount, id + 1);
                    }
                } else
                    initLexicalStuff (reader.getTokenTypes ());
            } else
                initLexicalStuff (reader.getTokenTypes ());

            List<Feature> features = reader.getFeatures ();
            Iterator<Feature> it2 = features.iterator ();
            while (it2.hasNext ()) {
                Feature feature = it2.next ();
                if (feature.getFeatureName ().equals (IMPORT_FEATURE))
                    importLanguage (feature);
                featureList.add (feature);
            }
            Set<Integer> skipTokenIDs = new HashSet<Integer> ();
            Iterator<Feature> it = featureList.getFeatures ("SKIP").iterator ();
            while (it.hasNext()) {
                Feature feature = it.next();
                if (feature.getFeatureName ().equals ("SKIP")) {
                    skipTokenIDs.add (tokenTypeToID.get (feature.getSelector ().toString ()));
                }
            }
            List<Rule> rules = reader.getRules (this);
            analyser = LLSyntaxAnalyser.create (
                this, rules, skipTokenIDs
            );
            fire ();
            synchronized (INIT_LOCK) {
                INIT_LOCK.notifyAll ();
            }
        } finally {
            reader = null;
        }
    }
    
    private void initLexicalStuff (List<TokenType> tokenTypes) {
        Iterator<TokenType> it = tokenTypes.iterator ();
        while (it.hasNext()) {
            TokenType tokenType = it.next ();
            int id = tokenType.getTypeID ();
            String name = tokenType.getType ();
            idToTokenType.put (id, name);
            tokenTypeToID.put (name, id);
            tokenTypeCount = Math.max (tokenTypeCount, id + 1);
        }
        parser = Parser.create (tokenTypes);
    }
    
    protected void fire () {
        if (listenerList == null) return;
        Object[] l = listenerList.getListenerList ();
        PropertyChangeEvent event = null;
        for (int i = l.length - 2; i >= 0; i -= 2) {
            if (event == null)
                event = new PropertyChangeEvent (this, null, null, null);
            ((PropertyChangeListener) l [i+1]).propertyChange (event);
        }
    }
    
    public String toString () {
        return "LanguageImpl " + mimeType + " (" + hashCode () + ")";
    }
}


