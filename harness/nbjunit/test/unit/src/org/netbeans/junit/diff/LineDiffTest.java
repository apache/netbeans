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
        content = content.replace("\n", System.getProperty("line.separator"));
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
