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

package org.netbeans.lib.java.lexer;

import java.io.File;
import java.io.FileReader;
import java.nio.CharBuffer;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * This is a test that scans the source that is a copy of javax.swing.JComponent
 * and reports the times of creation of all the tokens. It exists
 * mainly because it's easy to run profiler over it.
 *
 * @author mmetelka
 */
public class JavaLexerPerformanceTest extends NbTestCase {
    
    public JavaLexerPerformanceTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        // Disable for performance testing
        // LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
    private static final int PREPARE_COUNT = 10;

    public void testAString() throws Exception {
        String text = readJComponentFile();
        // Possibly some extra prepare runs
        int prepareTokenCount = 0;
        for (int i = 0; i < PREPARE_COUNT; i++) { prepareTokenCount += prepareTestAString(text); }

        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        long tm = System.currentTimeMillis();
        // Force all the tokens to be initialized
        int tokenCount = 0;
        while (ts.moveNext()) { tokenCount++; }
        tm = System.currentTimeMillis() - tm;
        System.err.println("TH over String: " + tokenCount + " tokens created in " + tm
                + " ms over text with " + text.length() + " chars; prepareTokenCount="
                + prepareTokenCount + ".");
    }
    
    public void testDocument() throws Exception {
        String text = readJComponentFile();
        // Possibly some extra prepare runs
        int prepareTokenCount = 0;
        for (int i = 0; i < PREPARE_COUNT; i++) { prepareTokenCount += prepareTestDocument(text); }

        // Create the document and token hierarchy again and measure time
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, JavaTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts = hi.tokenSequence();
        long tm = System.currentTimeMillis();
        int tokenCount = 0;
        // Force all the tokens to be initialized
        while (ts.moveNext()) { tokenCount++; }
        tm = System.currentTimeMillis() - tm;
        System.err.println("TH over Swing Document: " + tokenCount + " tokens created in " + tm
                + " ms over document with " + doc.getLength() + " chars; prepareTokenCount="
                + prepareTokenCount + ".");
    }
    
    private int prepareTestAString(String text) throws Exception {
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        int tokenCount = 0;
        while (ts.moveNext()) { tokenCount++; }
        return tokenCount;
    }
    
    private int prepareTestDocument(String text) throws Exception {
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, JavaTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts = hi.tokenSequence();
        int tokenCount = 0;
        while (ts.moveNext()) { tokenCount++; }
        return tokenCount;
    }

    private String readJComponentFile() throws Exception {
        File testJComponentFile = new File(getDataDir() + "/testfiles/JComponent.java.txt");
        FileReader r = new FileReader(testJComponentFile);
        try {
            int fileLen = (int)testJComponentFile.length();
            CharBuffer cb = CharBuffer.allocate(fileLen);
            r.read(cb);
            cb.rewind();
            return cb.toString();
        } finally {
            r.close();
        }
    }
    
}
