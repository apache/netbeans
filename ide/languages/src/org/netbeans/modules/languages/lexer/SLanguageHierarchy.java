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

package org.netbeans.modules.languages.lexer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.TokenType;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 *
 * @author Jan Jancura
 */
public class SLanguageHierarchy extends LanguageHierarchy<STokenId> {
    
    private Language                    language;
    private List<STokenId>              tokenIDs;
    private Map<Integer,STokenId>       tokenIDToType;
    
    
    public SLanguageHierarchy (Language language) {
        this.language = language;
//        new Listener (this, language);
    }
    
    protected Collection<STokenId> createTokenIds () {
        if (tokenIDs == null) {
            List<TokenType> tokenTypes = language.getParser ().getTokenTypes ();
            tokenIDToType = new HashMap<Integer,STokenId> ();
            tokenIDs = new ArrayList<STokenId> ();
            Set<String> types = new HashSet<String> ();
            int size = tokenTypes.size ();
            for (int i = 0; i < size; i++) {
                TokenType tokenType = tokenTypes.get (i);
                String typeName = tokenType.getType ();
                if (types.contains (typeName)) continue; // there can be more TokenTypes with same name!!
                if (language.getTokenID (typeName) < 0)
                    throw new IndexOutOfBoundsException ();
                types.add (typeName);
                STokenId tokenId = new STokenId (
                    typeName, 
                    language.getTokenID (typeName), 
                    typeName
                );
                tokenIDs.add (tokenId);
                tokenIDToType.put (tokenId.ordinal (), tokenId);
            }
        }
        return tokenIDs;
    }

    protected Lexer<STokenId> createLexer (LexerRestartInfo<STokenId> info) {
        if (tokenIDs == null) createTokenIds ();
        return new SLexer (
            language, 
            tokenIDToType, 
            info
        );
    }

    protected String mimeType () {
        return language.getMimeType ();
    }
    
    public String toString () {
        return getClass ().getName () + "@" + hashCode ();
    }

//    private static class Listener implements PropertyChangeListener {
//
//        private WeakReference<SLanguageHierarchy>   reference;
//        private Language                            language;
//        
//        Listener (SLanguageHierarchy hierarchy, Language language) {
//            reference = new WeakReference<SLanguageHierarchy> (hierarchy);
//            this.language = language;
//            language.addPropertyChangeListener (this);
//        }
//        
//        public void propertyChange (PropertyChangeEvent evt) {
//            SLanguageHierarchy hierarchy = reference.get ();
//            if (hierarchy == null) {
//                language.removePropertyChangeListener (this);
//                return;
//            }
//            hierarchy.tokenIDToType = null;
//            hierarchy.tokenIDs = null;
//        }
//    }
}



