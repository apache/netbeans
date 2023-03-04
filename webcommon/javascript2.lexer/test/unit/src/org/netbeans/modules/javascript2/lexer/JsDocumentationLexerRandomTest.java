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

package org.netbeans.modules.javascript2.lexer;

import junit.framework.TestCase;
import org.netbeans.lib.lexer.test.*;
import org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId;

/**
 * @author mmetelka
 * @author Martin Adamek
 * @author Martin Fousek
 */
public class JsDocumentationLexerRandomTest extends TestCase {

    public JsDocumentationLexerRandomTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        System.setProperty("netbeans.debug.lexer.test", "true");
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    public void testRandom() throws Exception {
        test(0);
    }

    private void test(long seed) throws Exception {
        TestRandomModify randomModify = new TestRandomModify(seed);
        randomModify.setLanguage(JsDocumentationTokenId.language());

//        randomModify.setDebugOperation(true);
//        randomModify.setDebugDocumentText(true);
//        randomModify.setDebugHierarchy(true);

        FixedTextDescriptor[] fixedTexts = new FixedTextDescriptor[] {
            FixedTextDescriptor.create("/**/", 0.1),
            FixedTextDescriptor.create("/***/", 0.1),
            FixedTextDescriptor.create("/*", 0.1),
            FixedTextDescriptor.create("/**", 0.1),
            FixedTextDescriptor.create("//", 0.1),
            FixedTextDescriptor.create("*/", 0.1),
        };

        RandomCharDescriptor[] regularChars = new RandomCharDescriptor[] {
            RandomCharDescriptor.letter(0.3),
            RandomCharDescriptor.space(0.3),
            RandomCharDescriptor.lf(0.3),
            RandomCharDescriptor.chars(new char[] { '@', '.', ',', '#', '-', '<', '>', '{', '}'}, 0.3),
        };

        RandomTextProvider regularTextProvider = new RandomTextProvider(regularChars, fixedTexts);

        randomModify.test(
            new RandomModifyDescriptor[] {
                new RandomModifyDescriptor(1000, regularTextProvider,
                        0.2, 0.2, 0.1,
                        0.2, 0.2,
                        0.0, 0.0),
            }
        );
    }

}
