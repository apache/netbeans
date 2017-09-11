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

package org.netbeans.junit.diff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jan Lahoda, ehucka
 */
public class LineDiffTest extends NbTestCase {
    
    public LineDiffTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    private void doOutputToFile(File f, String content) throws IOException {
        content = content.replaceAll("\n", System.getProperty("line.separator"));
        Writer w = new FileWriter(f);
        try {
            w.write(content);
        } finally {
            w.close();
        }
    }
    
    private String getFileContent(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        char[] buffer = new char[1024];
        int read = br.read(buffer);
        String t = new String(buffer, 0, read);
        String ls = System.getProperty("line.separator");
        return t.replace(ls, "\n");
    }
    
    public void testSimple() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        
        doOutputToFile(test1, "a\nb\nc\nd\n");
        doOutputToFile(test2, "a\nb\nc\nd\n");
        
        LineDiff diff = new LineDiff();
        
        assertFalse(diff.diff(test1, test2, null));
    }
    
    public void testEmpty1() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        
        doOutputToFile(test1, "a\nb\nc\nd\n");
        doOutputToFile(test2, "");
        
        LineDiff diff = new LineDiff();
        assertTrue(diff.diff(test1, test2, null));
    }
    
    public void testEmpty2() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        
        doOutputToFile(test1, "");
        doOutputToFile(test2, "a\nb\nc\nd\n");
        
        LineDiff diff = new LineDiff();
        assertTrue(diff.diff(test1, test2, null));
    }
    
    public void testIgnoreCase() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        
        doOutputToFile(test1, "A\nb\nC\nd\n");
        doOutputToFile(test2, "a\nB\nc\nD\n");
        
        LineDiff diff = new LineDiff(true);
        assertFalse(diff.diff(test1, test2, null));
    }
    
    public void testIgnoreEmptyLines() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        
        doOutputToFile(test1, "a\n\nb\n\nc\n\n\nd\n\n");
        doOutputToFile(test2, "a\nb\nc\nd\n");
        
        LineDiff diff = new LineDiff(false, true);
        assertFalse(diff.diff(test1, test2, null));
    }
    
    public void testDiff1() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        File test3 = new File(getWorkDir(), "test3");
        
        doOutputToFile(test1, "a\nb\nc\n");
        doOutputToFile(test2, "a\nb\nc\nd\n");
        
        LineDiff diff = new LineDiff();
        assertTrue(diff.diff(test1, test2, test3));
        assertEquals(" 1   a\n 2   b\n 3   c\n 4 - d\n", getFileContent(test3));
    }
    
    public void testDiff2() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        File test3 = new File(getWorkDir(), "test3");
        
        doOutputToFile(test1, "b\nc\nd\n");
        doOutputToFile(test2, "a\nb\nc\nd\n");
        
        LineDiff diff = new LineDiff();
        assertTrue(diff.diff(test1, test2, test3));
        assertEquals(" 1 - a\n 2   b\n 3   c\n 4   d\n", getFileContent(test3));
    }
    
    public void testDiff3() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        File test3 = new File(getWorkDir(), "test3");
        
        doOutputToFile(test1, "a\nb\nb\nc\nc\nc\nb\nd\n");
        doOutputToFile(test2, "a\nb\nc\nd\n");
        
        LineDiff diff = new LineDiff();
        assertTrue(diff.diff(test1, test2, test3));
        assertEquals(" 1   a\n 2   b\n   + b\n 3   c\n   + c\n   + c\n   + b\n 4   d\n", getFileContent(test3));
    }
    
    public void testDiff4() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        File test3 = new File(getWorkDir(), "test3");
        
        doOutputToFile(test1, "a\nb\nb\nd\na\nb\nd\n");
        doOutputToFile(test2, "a\nb\nc\nd\n");
        
        LineDiff diff = new LineDiff();
        assertTrue(diff.diff(test1, test2, test3));
        assertEquals(" 1   a\n 2   b\n 3 - c\n   + b\n 4   d\n   + a\n   + b\n   + d\n", getFileContent(test3));
    }
    
    public void testDiff5() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        File test3 = new File(getWorkDir(), "test3");
        
        doOutputToFile(test1, "0\na\nb\nc\nd\n");
        doOutputToFile(test2, "a\nb\nc\nd\n");
        
        LineDiff diff = new LineDiff();
        assertTrue(diff.diff(test1, test2, test3));
        assertEquals("   + 0\n 1   a\n 2   b\n 3   c\n", getFileContent(test3));
    }
    
    public void testDiff6() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        File test3 = new File(getWorkDir(), "test3");
        
        doOutputToFile(test1, "a\nb\nc\nd\ne\n");
        doOutputToFile(test2, "a\nb\nc\nd\n");
        
        LineDiff diff = new LineDiff();
        assertTrue(diff.diff(test1, test2, test3));
        assertEquals(" 2   b\n 3   c\n 4   d\n   + e\n", getFileContent(test3));
    }
    
    public void testDiff7() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        File test3 = new File(getWorkDir(), "test3");
        
        doOutputToFile(test1, "e\nf\ng\nh\n");
        doOutputToFile(test2, "a\nb\nc\nd\n");
        
        LineDiff diff = new LineDiff();
        assertTrue(diff.diff(test1, test2, test3));
        assertEquals(" 1 - a\n   + e\n 2 - b\n   + f\n 3 - c\n   + g\n 4 - d\n   + h\n", getFileContent(test3));
    }
    
    public void testDiff8() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        File test3 = new File(getWorkDir(), "test3");
        
        doOutputToFile(test1, "a\ne\nc\nd\n\nf\n");
        doOutputToFile(test2, "a\nb\n\nc\nd\nf\n");
        
        LineDiff diff = new LineDiff();
        assertTrue(diff.diff(test1, test2, test3));
        assertEquals(" 1   a\n 2 - b\n   + e\n 3 - \n 4   c\n 5   d\n   + \n 6   f\n", getFileContent(test3));
    }
    
    public void testDiff9() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        File test3 = new File(getWorkDir(), "test3");
        
        doOutputToFile(test1, "a\nx\nc\nd\nb\nf\n");
        doOutputToFile(test2, "a\nb\nc\nd\nb\nf\n");
        
        LineDiff diff = new LineDiff();
        assertTrue(diff.diff(test1, test2, test3));
        assertEquals(" 1   a\n   + x\n 2 - b\n 3   c\n 4   d\n 5   b\n", getFileContent(test3));
    }
    
    public void testDiff10() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        File test3 = new File(getWorkDir(), "test3");
        
        doOutputToFile(test1, "a\nx\nc\nd\nx\ny\n");
        doOutputToFile(test2, "a\nb\nc\nd\nx\ny\n");
        
        LineDiff diff = new LineDiff();
        assertTrue(diff.diff(test1, test2, test3));
        assertEquals(" 1   a\n 2 - b\n   + x\n 3   c\n 4   d\n 5   x\n", getFileContent(test3));
    }
    
    public void testDiff11() throws Exception {
        File test1 = new File(getWorkDir(), "test1");
        File test2 = new File(getWorkDir(), "test2");
        File test3 = new File(getWorkDir(), "test3");
        try {
            doOutputToFile(test1, "a\nx\nc\nd\nx\ny\n");
            doOutputToFile(test2, "a\nb\nc\nd\nx\ny\n");
            
            System.setProperty("nbjunit.linediff.context", "0");
            LineDiff diff = new LineDiff();
            assertTrue(diff.diff(test1, test2, test3));
            assertEquals(" 2 - b\n   + x\n", getFileContent(test3));
        } finally {
            System.setProperty("nbjunit.linediff.context", "");
        }
    }
}
