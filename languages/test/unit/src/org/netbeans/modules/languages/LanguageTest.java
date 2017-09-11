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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.Parser.Cookie;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.modules.languages.parser.StringInput;


/**
 *
 * @author Jan Jancura
 */
public class LanguageTest extends TestCase {
    
    public LanguageTest(String testName) {
        super(testName);
    }
    
    public void testFeatures1 () {
        TestLanguage language = new TestLanguage ();
        language.addFeature (Feature.create ("feature", Selector.create ("selector")));
        assertEquals ("selector", language.getFeatureList ().getFeature ("feature").getSelector ().getAsString ());
        assertEquals (1, language.getFeatureList ().getFeatures ("feature").size ());
    }
    
    public void testFeatures2 () {
        TestLanguage language = new TestLanguage ();
        language.addFeature (Feature.create ("feature", Selector.create ("selector")));
        language.addFeature (Feature.create ("feature", Selector.create ("selector2")));
        assertEquals (2, language.getFeatureList ().getFeatures ("feature").size ());
        Feature feature = language.getFeatureList ().getFeature ("feature");
        assertTrue (feature.getSelector ().toString ().equals ("selector") ||
                    feature.getSelector ().toString ().equals ("selector2")
        );
    }
    
    public void testFeatures3 () {
        TestLanguage language = new TestLanguage ();
        language.addToken (0, "a");
        language.addToken (1, "b");
        language.addToken (2, "c");
        language.addFeature (Feature.create ("feature", Selector.create ("a")));
        language.addFeature (Feature.create ("feature", Selector.create ("a.b")));
        language.addFeature (Feature.create ("feature", Selector.create ("c.a")));
        assertEquals ("a", language.getFeatureList ().getFeature ("feature", "a").getSelector ().getAsString ());
        assertNull (language.getFeatureList ().getFeature ("feature", "b"));
        assertNull (language.getFeatureList ().getFeature ("feature", "c"));
        
        ASTNode n = ASTNode.create (language, "x", Arrays.asList (new ASTItem[] {
            ASTNode.create (language, "a", Arrays.asList (new ASTItem[] {
                ASTToken.create (language, "b", "bbb", 0, 3, null),
                ASTToken.create (language, "a", "aaa", 3, 3, null),
            }), 0),
            ASTNode.create (language, "c", Arrays.asList (new ASTItem[] {
                ASTToken.create (language, "b", "bbb", 6, 3, null),
                ASTToken.create (language, "a", "aaa", 9, 3, null),
            }), 6)
        }), 0);
        List<Feature> fs = language.getFeatureList ().getFeatures ("feature", n.findPath (1));
        assertEquals (1, fs.size ());
        assertEquals ("a.b", fs.get (0).getSelector ().getAsString ());
        fs = language.getFeatureList ().getFeatures ("feature", n.findPath (4));
        assertEquals (1, fs.size ());
        assertEquals ("a", fs.get (0).getSelector ().getAsString ());
        fs = language.getFeatureList ().getFeatures ("feature", n.findPath (7));
        assertEquals (0, fs.size ());
        fs = language.getFeatureList ().getFeatures ("feature", n.findPath (10));
        assertEquals (2, fs.size ());
        Set<String> s = new HashSet<String> ();
        s.add (fs.get (0).getSelector ().getAsString ());
        s.add (fs.get (1).getSelector ().getAsString ());
        assertTrue (s.contains ("a"));
        assertTrue (s.contains ("c.a"));
    }
    
    public void testTokens1 () throws ParseException {
        TestLanguage language = new TestLanguage ();
        language.addToken (1, "jedna", Pattern.create ("['0'-'9']+"), null, "number", 0, Feature.create ("cislo", Selector.create ("a")));
        language.addToken (2, "dve", Pattern.create ("['a'-'z']+"), "number", "number and character", 1, Feature.create ("cislo a pismeno", Selector.create ("b")));
        language.addToken (3, "tri", Pattern.create ("'$'"), "number and character", null, 2, Feature.create ("prachy", Selector.create ("c")));
        language.addToken (4, "ctyri", Pattern.create ("['A'-'Z']+"), null, "big character", 3, Feature.create ("velke pismeno", Selector.create ("d")));
        language.addToken (5, "pet", Pattern.create ("'+'"), "big character", null, 4, Feature.create ("plus", Selector.create ("e")));
        language.addFeature (Feature.create ("feature", Selector.create ("a")));
        language.addFeature (Feature.create ("feature", Selector.create ("a.b")));
        language.addFeature (Feature.create ("feature", Selector.create ("c.a")));
        
        assertEquals (5, language.getParser ().getTokenTypes ().size ());
        Parser p = language.getParser ();
        MyCookie cookie = new MyCookie ();
        StringInput input = new StringInput ("10090aas$AAQ+");
        ASTToken t = p.read (cookie, input, language);
        assertEquals ("jedna", t.getTypeName ());
        assertEquals ("10090", t.getIdentifier ());
        assertEquals (cookie.getState (), p.getState ("number"));
        t = p.read (cookie, input, language);
        assertEquals ("dve", t.getTypeName ());
        assertEquals ("aas", t.getIdentifier ());
        assertEquals (cookie.getState (), p.getState ("number and character"));
        t = p.read (cookie, input, language);
        assertEquals ("tri", t.getTypeName ());
        assertEquals ("$", t.getIdentifier ());
        assertEquals (cookie.getState (), -1);
        t = p.read (cookie, input, language);
        assertEquals ("ctyri", t.getTypeName ());
        assertEquals ("AAQ", t.getIdentifier ());
        assertEquals (cookie.getState (), p.getState ("big character"));
        t = p.read (cookie, input, language);
        assertEquals ("pet", t.getTypeName ());
        assertEquals ("+", t.getIdentifier ());
        assertEquals (cookie.getState (), -1);
        assertTrue (input.eof ());
    }
    
    private static class MyCookie implements Cookie {

        private int state = -1;
        
        public int getState() {
            return state;
        }

        public void setState (int state) {
            this.state = state;
        }

        public void setProperties(Feature tokenProperties) {
        }
    }
}




