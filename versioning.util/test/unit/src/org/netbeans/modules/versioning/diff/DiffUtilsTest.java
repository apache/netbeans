/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
