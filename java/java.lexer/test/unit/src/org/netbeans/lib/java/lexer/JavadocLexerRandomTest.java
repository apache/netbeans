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

package org.netbeans.lib.java.lexer;

import junit.framework.TestCase;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.lib.lexer.test.FixedTextDescriptor;
import org.netbeans.lib.lexer.test.RandomCharDescriptor;
import org.netbeans.lib.lexer.test.RandomModifyDescriptor;
import org.netbeans.lib.lexer.test.RandomTextProvider;
import org.netbeans.lib.lexer.test.TestRandomModify;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class JavadocLexerRandomTest extends TestCase {

    public JavadocLexerRandomTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        System.setProperty("netbeans.debug.lexer.test", "true");
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testRandom() throws Exception {
        test(0);
    }
    
    private void test(long seed) throws Exception {
        TestRandomModify randomModify = new TestRandomModify(seed);
        randomModify.setLanguage(JavadocTokenId.language());
        
//        randomModify.setDebugOperation(true);
//        randomModify.setDebugDocumentText(true);
//        randomModify.setDebugHierarchy(true);

        FixedTextDescriptor[] fixedTexts = new FixedTextDescriptor[] {
            FixedTextDescriptor.create(" @param ", 0.2),
            FixedTextDescriptor.create(" /> ", 0.2),
        };
        
        RandomCharDescriptor[] regularChars = new RandomCharDescriptor[] {
            RandomCharDescriptor.letter(0.3),
            RandomCharDescriptor.space(0.3),
            RandomCharDescriptor.lf(0.3),
            RandomCharDescriptor.chars(new char[] { '@', '.', '#', '<', '/', '>'}, 0.3),
        };

//        RandomCharDescriptor[] anyChar = new RandomCharDescriptor[] {
//            RandomCharDescriptor.anyChar(1.0),
//        };

        RandomTextProvider regularTextProvider = new RandomTextProvider(regularChars, fixedTexts);
//        RandomTextProvider anyCharTextProvider = new RandomTextProvider(anyChar, fixedTexts);
        
        randomModify.test(
            new RandomModifyDescriptor[] {
                new RandomModifyDescriptor(1000, regularTextProvider,
                        0.2, 0.2, 0.1,
                        0.2, 0.2,
                        0.0, 0.0),
            }
        );

//        randomModify.test(
//            new RandomModifyDescriptor[] {
//                new RandomModifyDescriptor(1000, anyCharTextProvider,
//                        0.4, 0.2, 0.2,
//                        0.1, 0.1,
//                        0.0, 0.0),
//                new RandomModifyDescriptor(1000, anyCharTextProvider,
//                        0.2, 0.2, 0.1,
//                        0.4, 0.3,
//                        0.0, 0.0),
//            }
//        );
    }
    
}
