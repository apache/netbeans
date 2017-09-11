/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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




