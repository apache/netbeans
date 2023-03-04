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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.StringInput;
import org.netbeans.modules.languages.parser.SyntaxError;
import org.netbeans.modules.languages.parser.TokenInputUtils;


/**
 *
 * @author Jan Jancura
 */
public abstract class Language extends org.netbeans.api.languages.Language {

    
    public static Language create (String mimeType) {
        return new EmptyLanguage (mimeType);
    }
    
    
    // public methods ..........................................................
    
    public abstract String getMimeType ();
    public abstract Parser getParser ();
    public abstract LLSyntaxAnalyser getAnalyser ();
    public abstract FeatureList getFeatureList ();
    public abstract void addPropertyChangeListener (PropertyChangeListener l);
    public abstract void removePropertyChangeListener (PropertyChangeListener l);
    
    public abstract int getTokenID (String tokenType);
    public abstract int getTokenTypeCount ();
    public abstract String getTokenType (int tokenTypeID);
    
    public abstract int getNTID (String nt);
    public abstract int getNTCount ();
    public abstract String getNT (int ntid);
    
    public abstract List<Language> getImportedLanguages ();
    public abstract Feature getPreprocessorImport ();
    public abstract Map<String,Feature> getTokenImports ();

    
    // private helper methods ..................................................
    
    public ASTNode parse (InputStream is) throws IOException, ParseException {
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        StringBuilder sb = new StringBuilder ();
        String ln = br.readLine ();
        while (ln != null) {
            sb.append (ln).append ('\n');
            ln = br.readLine ();
        }
        TokenInput ti = TokenInputUtils.create (
            this,
            getParser (), 
            new StringInput (sb.toString ())
        );
        ASTNode root = getAnalyser ().read (
            ti, 
            true, 
            new ArrayList<SyntaxError> (), 
            new boolean[] {false}
        );
        Feature astProperties = getFeatureList ().getFeature ("AST");
        if (astProperties != null && root != null) {
            ASTNode root1 = (ASTNode) astProperties.getValue (
                "process", 
                SyntaxContext.create (null, ASTPath.create (root))
            );
            if (root1 != null)
                root = root1;
        }
        return root;
    }
    
    
    private static class EmptyLanguage extends Language {
        
        private String mimeType;

        EmptyLanguage (String mimeType) {
            this.mimeType = mimeType;
        }
        
        public String getMimeType () {
            return mimeType;
        }
        
        public Parser getParser () {
            return null;
        }

        private LLSyntaxAnalyser analyser = LLSyntaxAnalyser.createEmpty (this);
        
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
        
        public int getTokenID (String tokenType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int getTokenTypeCount () {
            return 0;
        }

        public String getTokenType (int tokenTypeID) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int getNTID (String nt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int getNTCount () {
            return 0;
        }

        public String getNT (int ntid) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
        // imports ...
        
        public List<Language> getImportedLanguages () {
            return Collections.<Language> emptyList ();
        }

        public Feature getPreprocessorImport () {
            return null;
        }

        public Map<String, Feature> getTokenImports () {
            return Collections.<String,Feature> emptyMap ();
        }
    }
}


