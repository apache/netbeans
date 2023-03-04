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

package org.netbeans.modules.languages.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;


/**
 *
 * @author Jan Jancura
 */
class ASTFeatures {

    private static final ASTFeatures            DEFAULT = new ASTFeatures ();
    private static Map<String,ASTFeatures>      mimeTypeToASTFeatures = new HashMap<String,ASTFeatures> ();
    
    boolean                                     removeEmpty = false;
    boolean                                     removeSimple = false;
    boolean                                     removeEmptyN = true;
    boolean                                     removeSimpleN = true;
    Set<String>                                 empty = new HashSet<String> ();
    Set<String>                                 simple = new HashSet<String> ();

    
    static ASTFeatures get (Language language) {
        if (language == null) return DEFAULT;
        String mimeType = language.getMimeType ();
        ASTFeatures astFeatures = mimeTypeToASTFeatures.get (mimeType);
        if (astFeatures == null) {
            astFeatures = new ASTFeatures ();
            mimeTypeToASTFeatures.put (mimeType, astFeatures);
            Feature optimiseProperty = language.getFeatureList ().getFeature ("AST");
            if (optimiseProperty != null) {

                String s = (String) optimiseProperty.getValue ("removeEmpty");
                if (s != null) {
                    if (s.startsWith ("!")) {
                        astFeatures.removeEmptyN = false;
                        s = s.substring (1);
                    }
                    astFeatures.removeEmpty = "true".equals (s);
                    if (!"false".equals (s)) {
                        StringTokenizer st = new StringTokenizer (s, ",");
                        while (st.hasMoreTokens ())
                            astFeatures.empty.add (st.nextToken ());
                    }
                }

                s = (String) optimiseProperty.getValue ("removeSimple");
                if (s != null) {
                    if (s.startsWith ("!")) {
                        astFeatures.removeSimpleN = false;
                        s = s.substring (1);
                    }
                    astFeatures.removeSimple = "true".equals (s);
                    if (!"false".equals (s)) {
                        StringTokenizer st = new StringTokenizer (s, ",");
                        while (st.hasMoreTokens ())
                            astFeatures.simple.add (st.nextToken ());
                    }
                }
            }
        }
        return astFeatures;
    }
}





