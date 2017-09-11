/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.openide.loaders;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.junit.Test;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jhavlin
 */
public class FolderComparatorTest {

    public FolderComparatorTest() {
    }

    @Test
    public void testNaturalComparatorBasic() throws IOException {
        testNaturalComparator(new String[]{
            "b 10.txt",
            "b 9.txt",
            "a2.txt",
            "a 4 9.txt",
            "a10.txt",
            "b0070.txt",
            "a 3.txt",
            "b08.txt"
        }, new String[]{
            "a2.txt",
            "a 3.txt",
            "a 4 9.txt",
            "a10.txt",
            "b08.txt",
            "b 9.txt",
            "b 10.txt",
            "b0070.txt"
        });
    }

    @Test
    public void testNaturalComparatorWithSuffixes() throws IOException {
        testNaturalComparator(new String[]{
            "a01b",
            "a2x",
            "a02",
            "a1"
        }, new String[]{
            "a1",
            "a01b",
            "a02",
            "a2x"
        });
    }

    @Test
    public void testUseCustomComparator() throws IOException,
            InterruptedException, InvocationTargetException {

        FileSystem fs = FileUtil.createMemoryFileSystem();

        fs.getRoot().createData("aaaa.txt");
        fs.getRoot().createData("bbb.txt");
        fs.getRoot().createData("cc.txt");
        fs.getRoot().createData("d.txt");
        fs.getRoot().refresh();

        DataFolder.SortMode custom = new DataFolder.SortMode() {
            @Override
            public int compare(DataObject o1, DataObject o2) {
                return o1.getName().length() - o2.getName().length();
            }
        };

        DataFolder df = DataFolder.findFolder(fs.getRoot());
        df.setSortMode(custom);
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
            }
        });
        DataObject[] children = df.getChildren();
        assertEquals("d.txt", children[0].getName());
        assertEquals("cc.txt", children[1].getName());
        assertEquals("bbb.txt", children[2].getName());
        assertEquals("aaaa.txt", children[3].getName());
    }

    @Test
    public void testNaturalComparatorFallback() throws IOException {
        testNaturalComparator(new String[]{
            "a01.txt",
            "a001.txt",
            "A1.txt"
        }, new String[]{
            "A1.txt",
            "a001.txt",
            "a01.txt"
        });
    }

    private void testNaturalComparator(String[] fileNames,
            String[] expectedOrder) throws IOException {
        FolderComparator c = new FolderComparator(FolderComparator.NATURAL);
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject root = fs.getRoot();
        List<DataObject> list = new ArrayList<DataObject>();
        for (String n : fileNames) {
            FileObject fo = root.createData(n);
            assertNotNull(fo);
            list.add(DataObject.find(fo));
        }

        Collections.sort(list, c);
        for (int i = 0; i < expectedOrder.length; i++) {
            assertEquals(expectedOrder[i], list.get(i).getName());
        }
    }
}
