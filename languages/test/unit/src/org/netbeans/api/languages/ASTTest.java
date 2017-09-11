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

package org.netbeans.api.languages;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguageImpl;
import org.netbeans.modules.languages.TestLanguage;


/**
 *
 * @author Jan Jancura
 */
public class ASTTest extends TestCase {
    
    public ASTTest (String testName) {
        super (testName);
    }
    
    public void testAST1 () {
        TestLanguage language = new TestLanguage ();
        language.addToken (0, "a");
        language.addToken (1, "b");
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
        assertEquals (2, n.getChildren ().size ());
        assertEquals ("x", n.getNT ());
        assertEquals ("test/test", n.getMimeType ());
        assertEquals ("bbbaaabbbaaa", n.getAsText ());
        ASTPath path = n.findPath (3);
        assertEquals (3, path.size ());
        ASTToken t = (ASTToken) path.getLeaf ();
        assertEquals ("a", t.getTypeName ());
        assertEquals ("test/test", t.getMimeType ());
        assertEquals ("aaa", t.getIdentifier ());
        assertEquals (6, t.getEndOffset ());
        n = (ASTNode) path.getRoot ();
        assertEquals (n, path.get (0));
        assertEquals (2, n.getChildren ().size ());
        assertEquals ("x", n.getNT ());
        assertEquals ("test/test", n.getMimeType ());
        assertEquals ("bbbaaabbbaaa", n.getAsText ());
        n = (ASTNode) path.get (1);
        assertEquals (2, n.getChildren ().size ());
        assertEquals ("a", n.getNT ());
        assertEquals ("test/test", n.getMimeType ());
        assertEquals ("bbbaaa", n.getAsText ());
    }
}




