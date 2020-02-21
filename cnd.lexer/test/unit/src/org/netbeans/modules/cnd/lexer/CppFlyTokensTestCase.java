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

package org.netbeans.modules.cnd.lexer;

import java.io.File;
import java.io.FileReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test how many flyweight tokens gets created over a typical system headers
 * (copy of <istream> and <stdio.h> are used).
 *
 */
public class CppFlyTokensTestCase extends NbTestCase {
    private static final boolean TRACE = false;
    
    public CppFlyTokensTestCase(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testHCpp() throws Exception {
        File testJComponentFile = new File(getDataDir() + "/testfiles/istream.txt");
        FileReader r = new FileReader(testJComponentFile);
        int fileLen = (int)testJComponentFile.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        String text = cb.toString();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        if (TRACE) {
            System.err.println("Flyweight tokens: " + LexerTestUtilities.flyweightTokenCount(ts)
                    + "\nTotal tokens: " + ts.tokenCount()
                    + "\nFlyweight text length: " + LexerTestUtilities.flyweightTextLength(ts)
                    + "\nTotal text length: " + fileLen
                    + "\nDistribution: " + LexerTestUtilities.flyweightDistribution(ts)
            );
        }
        assertEquals(1606, LexerTestUtilities.flyweightTokenCount(ts));
        assertEquals(2822, LexerTestUtilities.flyweightTextLength(ts));
        assertEquals(2485, ts.tokenCount());         
    }
    
    public void testHC() throws Exception {
        File testJComponentFile = new File(getDataDir() + "/testfiles/stdio.h.txt");
        FileReader r = new FileReader(testJComponentFile);
        int fileLen = (int)testJComponentFile.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        String text = cb.toString();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageC());
        TokenSequence<?> ts = hi.tokenSequence();
        
        if (TRACE) {
            System.err.println("Flyweight tokens: " + LexerTestUtilities.flyweightTokenCount(ts)
                    + "\nTotal tokens: " + ts.tokenCount()
                    + "\nFlyweight text length: " + LexerTestUtilities.flyweightTextLength(ts)
                    + "\nTotal text length: " + fileLen
                    + "\nDistribution: " + LexerTestUtilities.flyweightDistribution(ts)
            );
        }
        
        assertEquals(2500, LexerTestUtilities.flyweightTokenCount(ts));
        assertEquals(4656, LexerTestUtilities.flyweightTextLength(ts));
        assertEquals(3577, ts.tokenCount());        
    }
    
    public void testC() throws Exception {
        File testJComponentFile = new File(getDataDir() + "/testfiles/istream.txt");
        FileReader r = new FileReader(testJComponentFile);
        int fileLen = (int)testJComponentFile.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        String text = cb.toString();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        
        LanguagePath path = LanguagePath.get(CppTokenId.languageCpp());
        path = LanguagePath.get(path, CppTokenId.languagePreproc());
        List<TokenSequence<?>> tsList = hi.tokenSequenceList(path, 0, Integer.MAX_VALUE);
        int fwTokenCount = 0;
        int tokenCount = 0;
        int fwTextLength = 0;
        List<Integer> distribution = new ArrayList<Integer>();
        for (TokenSequence<?> ts : tsList) {
            List<Integer> tsDistribution = LexerTestUtilities.flyweightDistribution(ts);
            if (TRACE) {
                System.err.println("Flyweight tokens: " + LexerTestUtilities.flyweightTokenCount(ts)
                        + "\nTotal tokens: " + ts.tokenCount()
                        + "\nFlyweight text length: " + LexerTestUtilities.flyweightTextLength(ts)
                        + "\nDistribution: " + tsDistribution
                );
            }
            fwTokenCount += LexerTestUtilities.flyweightTokenCount(ts);
            tokenCount += ts.tokenCount();
            fwTextLength += LexerTestUtilities.flyweightTextLength(ts);
            for (int i = 0; i < tsDistribution.size(); i++) {
                while (distribution.size() <= i) {
                    distribution.add(0);
                }
                distribution.set(i, distribution.get(i) + tsDistribution.get(i));
            }
            
        }

        if (TRACE) {
            System.err.println("Flyweight tokens: " + fwTokenCount
                    + "\nTotal tokens: " + tokenCount
                    + "\nFlyweight text length: " + fwTextLength
                    + "\nDistribution: " + distribution
            );
        }
        assertEquals(52, fwTokenCount);
        assertEquals(66, tokenCount);
        assertEquals(114, fwTextLength);
        
    }    
}
