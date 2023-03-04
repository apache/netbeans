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




