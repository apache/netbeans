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



