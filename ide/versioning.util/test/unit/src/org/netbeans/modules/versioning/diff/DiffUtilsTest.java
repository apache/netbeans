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
package org.netbeans.modules.versioning.diff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Ondrej Vrabec
 */
public class DiffUtilsTest extends NbTestCase {

    private File workdir;

    public DiffUtilsTest (String arg0) {
        super(arg0);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", new File(getWorkDir().getParentFile(), "userdir").getAbsolutePath());
        // create
        workdir = getWorkDir();
        MockLookup.setLayersAndInstances();
    }
    
    public void testPreviousLineNumberUnmodified () throws Exception {
        File folder = new File(workdir, "folder");
        folder.mkdirs();
        File file = new File(folder, "file");
        write(file, "\n");
        File file2 = new File(folder, "file2");
        write(file2, "ab\ncd\nef\ngh\nij\nkl\n");
        
        int matchingLineNumber = DiffUtils.getMatchingLine(file2, file2, 3);
        assertEquals(3, matchingLineNumber);
    }
    
    public void testPreviousLineNumberDeletedLines () throws Exception {
        File folder = new File(workdir, "folder");
        folder.mkdirs();
        File file = new File(folder, "file");
        write(file, "ab\ncd\nef\ngh\nij\nkl\n");
        File file2 = new File(folder, "file2");
        write(file2, "ab\nef\ngh\nij\nkl\n");
        int matchingLineNumber = DiffUtils.getMatchingLine(file2, file, 2);
        assertEquals(3, matchingLineNumber);
        
        // let's have more than 1 delete
        write(file2, "cd\ngh\nij\nkl\n");
        matchingLineNumber = DiffUtils.getMatchingLine(file2, file, 1);
        assertEquals(3, matchingLineNumber);
    }
    
    public void testPreviousLineNumberAddedLines () throws Exception {
        File folder = new File(workdir, "folder");
        folder.mkdirs();
        File file = new File(folder, "file");
        write(file, "kl\n");
        File file2 = new File(folder, "file2");
        write(file2, "ab\ncd\nef\ngh\nij\nkl\n");
        // test against empty line
        int matchingLineNumber = DiffUtils.getMatchingLine(file2, file, 3);
        assertEquals(-1, matchingLineNumber);
        
        File file3 = new File(folder, "file3");
        write(file3, "ab\ncd\nadded line\nef\ngh\nij\nkl\n");
        matchingLineNumber = DiffUtils.getMatchingLine(file3, file2, 4);
        assertEquals(3, matchingLineNumber);
        
        // let's have more than 1 addition
        write(file3, "added line\nab\nadded line\ncd\nadded line\nef\nadded line\ngh\nij\nkl\n");
        matchingLineNumber = DiffUtils.getMatchingLine(file3, file2, 7);
        assertEquals(3, matchingLineNumber);
        
        // newly added line, should return the last line before the addition
        write(file3, "ab\ncd\nef\nnewly added line1\nnewly added line2\nnewly added line3\ngh\nij\nkl\n");
        matchingLineNumber = DiffUtils.getMatchingLine(file3, file2, 3);
        assertEquals(2, matchingLineNumber);
        matchingLineNumber = DiffUtils.getMatchingLine(file3, file2, 4);
        assertEquals(2, matchingLineNumber);
        matchingLineNumber = DiffUtils.getMatchingLine(file3, file2, 5);
        assertEquals(2, matchingLineNumber);
    }
    
    public void testPreviousLineNumberModifiedLines () throws Exception {
        File folder = new File(workdir, "folder");
        folder.mkdirs();
        File file = new File(folder, "file");
        write(file, "ba\n");
        File file2 = new File(folder, "file2");
        write(file2, "ab\ncd\nef\ngh\nij\nkl\n");
        // test one big change
        int matchingLineNumber = DiffUtils.getMatchingLine(file2, file, 3);
        assertEquals(0, matchingLineNumber);
        
        File file3 = new File(folder, "file3");
        write(file3, "ab\ncd\nmodified line\ngh\nij\nkl\n");
        matchingLineNumber = DiffUtils.getMatchingLine(file3, file2, 4);
        assertEquals(4, matchingLineNumber);
        
        // let's have more than 1 change
        write(file3, "modification1\nmodification2\nmodification3\ncd\nmodification4\nij\nkl\n");
        matchingLineNumber = DiffUtils.getMatchingLine(file3, file2, 5);
        assertEquals(4, matchingLineNumber);
        
        // in the middle of a change
        write(file3, "ab\ncd\nef\nghm3\nijm4\nklm5\n");
        matchingLineNumber = DiffUtils.getMatchingLine(file3, file2, 3);
        assertEquals(3, matchingLineNumber);
        matchingLineNumber = DiffUtils.getMatchingLine(file3, file2, 4);
        assertEquals(3, matchingLineNumber);
        matchingLineNumber = DiffUtils.getMatchingLine(file3, file2, 5);
        assertEquals(3, matchingLineNumber);
    }
    
    public void testPreviousLineNumberComplexChanges () throws Exception {
        File folder = new File(workdir, "folder");
        folder.mkdirs();
        File file = new File(folder, "file");
        write(file, "a\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\nl\nm\nn\n");
        
        // test one big change
        File file2 = new File(folder, "file2");
        write(file2, "addedline\na\nc\nd\nmodif\nmodif\nmodif\nmodif\nmodif\nmodif\nh\nmodif\nk\nl\nm\nn\n");
        // test "m"
        int matchingLineNumber = DiffUtils.getMatchingLine(file2, file, 14);
        assertEquals(12, matchingLineNumber);
    }
    
    private void write(File file, String str) throws IOException {
        FileWriter w = null;
        try {
            w = new FileWriter(file);
            w.write(str);
            w.flush();
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.diff.DiffProvider.class)
    public static class DummyBuiltInDiffProvider extends BuiltInDiffProvider {
        public DummyBuiltInDiffProvider() {
        }
    }
}
