/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.loaders;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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

        list.sort(c);
        for (int i = 0; i < expectedOrder.length; i++) {
            assertEquals(expectedOrder[i], list.get(i).getName());
        }
    }
}
