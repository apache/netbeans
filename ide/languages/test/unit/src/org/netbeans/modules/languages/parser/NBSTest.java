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
