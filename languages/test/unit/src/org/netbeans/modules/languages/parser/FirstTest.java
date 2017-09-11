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
