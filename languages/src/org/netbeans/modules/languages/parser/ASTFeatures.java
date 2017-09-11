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





