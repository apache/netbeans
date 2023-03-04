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

import junit.framework.TestCase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.Rule;
import org.netbeans.modules.languages.TestLanguage;


/**
 *
 * @author Jan Jancura
 */
public class FirstTest extends TestCase {
    
    public FirstTest (String testName) {
        super (testName);
    }

    
    public void testFirst1 () throws ParseException {
        TestLanguage language = new TestLanguage ();
        language.addToken (0, "a");
        language.addToken (1, "b");
        language.addToken (2, "c");
        List<Rule> rules = new ArrayList<Rule> ();
        rules.add (Rule.create ("S", new ArrayList (Arrays.asList (new Object[] {
            "A", 
            ASTToken.create (language, "a", null, 0, 0, null),
            ASTToken.create (language, "b", null, 0, 0, null)
        }))));
        rules.add (Rule.create ("A", new ArrayList (Arrays.asList (new Object[] {
        }))));
        rules.add (Rule.create ("A", new ArrayList (Arrays.asList (new Object[] {
            ASTToken.create (language, "a", null, 0, 0, null),
            ASTToken.create (language, "c", null, 0, 0, null)
        }))));
        First first = First.create (rules, language);
//        S ystem.out.println(first);
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        assertEquals (2, first.getRule (
            language.getNTID ("A"), 
            TokenInputUtils.create (new ASTToken[] {
                ASTToken.create (language, 0, "a", 0),
                ASTToken.create (language, 1, "b", 0)
            }),
            Collections.<Integer>emptySet ()
        )); // should return 1 if there is follow method computed correctly!!!!!
        assertEquals (2, first.getRule (
            language.getNTID ("A"), 
            TokenInputUtils.create (new ASTToken[] {
                ASTToken.create (language, 0, "a", 0),
                ASTToken.create (language, 2, "c", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
    }
    
    public void testFirst2 () throws ParseException {
        TestLanguage language = new TestLanguage ();
        language.addToken (0, "a");
        language.addToken (1, "b");
        language.addToken (2, "c");
        language.addToken (3, "d");
        List<Rule> rules = new ArrayList<Rule> ();
        rules.add (Rule.create ("S", new ArrayList (Arrays.asList (new Object[] {
            "A", 
            ASTToken.create (language, "b", null, 0, 0, null),
            ASTToken.create (language, "c", null, 0, 0, null)
        }))));
        rules.add (Rule.create ("S", new ArrayList (Arrays.asList (new Object[] {
            ASTToken.create (language, "b", null, 0, 0, null),
            "A", 
            ASTToken.create (language, "c", null, 0, 0, null)
        }))));
        rules.add (Rule.create ("S", new ArrayList (Arrays.asList (new Object[] {
            "B",
            ASTToken.create (language, "c", null, 0, 0, null)
        }))));
        rules.add (Rule.create ("S", new ArrayList (Arrays.asList (new Object[] {
            ASTToken.create (language, "b", null, 0, 0, null),
            ASTToken.create (language, "d", null, 0, 0, null),
            ASTToken.create (language, "a", null, 0, 0, null)
        }))));
        rules.add (Rule.create ("A", new ArrayList (Arrays.asList (new Object[] {
            ASTToken.create (language, "d", null, 0, 0, null)
        }))));
        rules.add (Rule.create ("B", new ArrayList (Arrays.asList (new Object[] {
            ASTToken.create (language, "d", null, 0, 0, null)
        }))));
        First first = First.create (rules, language);
//        S ystem.out.println(first);
        assertEquals (3, first.getRule (
            0,
            TokenInputUtils.create (new ASTToken[] {
                ASTToken.create (language, 1, "b", 0),
                ASTToken.create (language, 3, "d", 0),
                ASTToken.create (language, 0, "a", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
        assertEquals (1, first.getRule (
            0, 
            TokenInputUtils.create(new ASTToken[] {
                ASTToken.create (language, 1, "b", 0),
                ASTToken.create (language, 3, "d", 0),
                ASTToken.create (language, 2, "c", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
        assertEquals (0, first.getRule (
            0, 
            TokenInputUtils.create (new ASTToken[] {
                ASTToken.create (language, 3, "d", 0),
                ASTToken.create (language, 1, "b", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
        assertEquals (2, first.getRule (
            0, 
            TokenInputUtils.create (new ASTToken[] {
                ASTToken.create (language, 3, "d", 0),
                ASTToken.create (language, 2, "c", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
        assertEquals (-2, first.getRule (
            0, 
            TokenInputUtils.create (new ASTToken[] {
                ASTToken.create (language, 3, "d", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
        assertEquals (-2, first.getRule (
            0, 
            TokenInputUtils.create(new ASTToken[] {
                ASTToken.create (language, 1, "b", 0),
                ASTToken.create (language, 1, "b", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
    }
}
