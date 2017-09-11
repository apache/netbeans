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


