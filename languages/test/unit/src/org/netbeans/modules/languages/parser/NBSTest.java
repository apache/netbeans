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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.modules.languages.LanguageImpl;
import org.netbeans.modules.languages.NBSLanguageReader;
import org.netbeans.modules.languages.Rule;
import org.netbeans.modules.languages.TestUtils;


/**
 *
 * @author Jan Jancura
 */
public class NBSTest extends TestCase {
    
    public NBSTest (String testName) {
        super (testName);
    }
    
    public void testFirst () {
        InputStream is = getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/languages/resources/NBS.nbs");
        try {
            NBSLanguageReader reader = NBSLanguageReader.create (is, "test", "test/x-nbs");
            LanguageImpl language = TestUtils.createLanguage (reader);
            language.read ();
            List<Rule> rules = language.getAnalyser ().getRules ();
//            AnalyserAnalyser.printRules (r, null);
//            AnalyserAnalyser.printUndefinedNTs (r, null);
            First first = First.create (rules, language);
//            AnalyserAnalyser.printDepth (f, null);
//            AnalyserAnalyser.printConflicts (f, null);
//            AnalyserAnalyser.printF (f, null);
            //assertFalse (AnalyserAnalyser.hasConflicts (first));
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void test2 () throws ParseException, IOException {
        InputStream is = getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/languages/resources/NBS.nbs");
        NBSLanguageReader reader = NBSLanguageReader.create (is, "test", "test/x-nbs");
        LanguageImpl language = TestUtils.createLanguage (reader);
        language.read ();

        is = getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/languages/resources/NBS.nbs");
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        StringBuilder sb = new StringBuilder ();
        String ln = br.readLine ();
        while (ln != null) {
            sb.append (ln).append ('\n');
            ln = br.readLine ();
        }
        TokenInput ti = TokenInputUtils.create (
            language,
            language.getParser (), 
            new StringInput (sb.toString ())
        );
        ASTNode n = language.getAnalyser ().read (ti, false, new ArrayList<SyntaxError> (), new boolean[] {false});
        assertNotNull (n);
    }
}
