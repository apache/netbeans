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

package org.openide.filesystems;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.test.TestFileUtils;

// XXX should only *unused* mask files be listed when propagateMasks?
// XXX write similar test for ParsingLayerCacheManager (simulate propagateMasks)

/**
 * Test that MultiFileSystem can mask files correctly.
 * @author Jesse Glick
 */
public class MultiFileSystemMaskTest extends NbTestCase {

    public MultiFileSystemMaskTest(String name) {
        super(name);
    }

    // XXX use this!
    private static String childrenNames(FileSystem fs) {
        FileObject folder = fs.findResource("folder");
        return childrenNames(folder);
    }
    
    private static String childrenNames(FileObject folder) {
        FileObject[] kids = folder.getChildren();        
        List<String> l = new ArrayList<String>();
        for (int i = 0; i < kids.length; i++) {
            l.add(kids[i].getNameExt());
        }
        Collections.sort(l);
        StringBuilder b = new StringBuilder();
        Iterator<String> i = l.iterator();
        if (i.hasNext()) {
            b.append(i.next());
            while (i.hasNext()) {
                b.append('/');
                b.append(i.next());
            }
        }
        return b.toString();
    }
    
    /**
     * Check that you can use one mask for more than one instance of a masked file.
     */
    public void testRepeatedMasks() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file1_hidden",
                "folder/file2_hidden",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file1",
                "folder/file2",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file1",
                "folder/file3",
            })
        );
        fs.setPropagateMasks(false);
        try {
            assertEquals("folder/file1_hidden masked two occurrences of folder/file1 and folder/file2_hidden masked one occurrence of folder/file2",
                "file3",
                childrenNames(fs));
        } finally {
            TestUtilHid.destroyXMLFileSystem(getName() + "1");
            TestUtilHid.destroyXMLFileSystem(getName() + "2");
            TestUtilHid.destroyXMLFileSystem(getName() + "3");
        }
    }
    public void testRepeatedMasksPropagate() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file1_hidden",
                "folder/file2_hidden",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file1",
                "folder/file2",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file1",
                "folder/file3",
            })
        );
        fs.setPropagateMasks(true);
        try {
            assertEquals("folder/file1_hidden masked two occurrences of folder/file1 and folder/file2_hidden masked one occurrence of folder/file2",
                "file1_hidden/file2_hidden/file3",
                childrenNames(fs));
        } finally {
            TestUtilHid.destroyXMLFileSystem(getName() + "1");
            TestUtilHid.destroyXMLFileSystem(getName() + "2");
            TestUtilHid.destroyXMLFileSystem(getName() + "3");
        }
    }
    
    /**
     * Check that a mask must precede the masked file in the delegates list.
     */
    public void testOutOfOrderMasks() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file1",
                "folder/file2",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file2_hidden",
                "folder/file3_hidden",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file3",
            })
        );
        fs.setPropagateMasks(false);
        try {
            assertEquals("folder/file2_hidden did not mask an earlier file but folder/file3_hidden masked a later one",
                "file1/file2",
                childrenNames(fs));
        } finally {
            TestUtilHid.destroyXMLFileSystem(getName() + "1");
            TestUtilHid.destroyXMLFileSystem(getName() + "2");
            TestUtilHid.destroyXMLFileSystem(getName() + "3");
        }
    }
    /* XXX never passed, not clear if it should anyway:
    public void testOutOfOrderMasksPropagate() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(new FileSystem[] {
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file1",
                "folder/file2",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file2_hidden",
                "folder/file3_hidden",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file3",
            }),
        });
        fs.setPropagateMasks(true);
        try {
            System.err.println("tOOOMP: " + childrenNames(fs));//XXX
            assertEquals("folder/file2_hidden did not mask an earlier file but folder/file3_hidden masked a later one",
                "file1/file2/file2_hidden/file3_hidden",
                childrenNames(fs));
        } finally {
            TestUtilHid.destroyXMLFileSystem(getName() + "1");
            TestUtilHid.destroyXMLFileSystem(getName() + "2");
            TestUtilHid.destroyXMLFileSystem(getName() + "3");
        }
    }
     */
    
    /**
     * Check that a mask cannot be parallel to the masked file in the delegates list.
     */
    public void testParallelMasks() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file1",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file2",
                "folder/file2_hidden",
                "folder/file3",
            })
        );
        fs.setPropagateMasks(false);
        try {
            assertEquals("folder/file2_hidden does not mask a file from the same layer",
                "file1/file2/file3",
                childrenNames(fs));
        } finally {
            TestUtilHid.destroyXMLFileSystem(getName() + "1");
            TestUtilHid.destroyXMLFileSystem(getName() + "2");
        }
    }
    /* XXX never passed, not clear if it should anyway:
    public void testParallelMasksPropagate() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(new FileSystem[] {
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file1",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file2",
                "folder/file2_hidden",
                "folder/file3",
            }),
        });
        fs.setPropagateMasks(true);
        try {
            System.err.println("tPMP: " + childrenNames(fs));//XXX
            assertEquals("folder/file2_hidden does not mask a file from the same layer",
                "file1/file2/file2_hidden/file3",
                childrenNames(fs));
        } finally {
            TestUtilHid.destroyXMLFileSystem(getName() + "1");
            TestUtilHid.destroyXMLFileSystem(getName() + "2");
        }
    }
     */
    
    // XXX test create -> mask -> recreate in same MFS
    
    @SuppressWarnings("deprecation") // for debugging only
    private static void setSystemName(FileSystem fs, String s) throws PropertyVetoException {
        fs.setSystemName(s);
    }
    public void testWeightedOverrides() throws Exception { // #141925
        FileSystem wr = FileUtil.createMemoryFileSystem();
        setSystemName(wr, "wr");
        FileSystem fs1 = FileUtil.createMemoryFileSystem();
        setSystemName(fs1, "fs1");
        FileObject f = TestFileUtils.writeFile(fs1.getRoot(), "d/f", "1");
        f.setAttribute("a", 1);
        FileSystem fs2 = FileUtil.createMemoryFileSystem();
        setSystemName(fs2, "fs2");
        f = TestFileUtils.writeFile(fs2.getRoot(), "d/f", "2");
        f.setAttribute("a", 2);
        // Test behavior with no weights: first layer wins.
        FileSystem mfs = new MultiFileSystem(wr, fs1, fs2);
        f = mfs.findResource("d/f");
        assertEquals(1, f.getAttribute("a"));
        assertEquals("1", f.asText());
        mfs = new MultiFileSystem(wr, fs2, fs1);
        f = mfs.findResource("d/f");
        assertEquals(2, f.getAttribute("a"));
        assertEquals("2", f.asText());
        // Now test that weighted layer wins over unweighted regardless of order.
        fs2.findResource("d/f").setAttribute("weight", 100);
        mfs = new MultiFileSystem(wr, fs1, fs2);
        f = mfs.findResource("d/f");
        assertEquals(2, f.getAttribute("a"));
        assertEquals("2", f.asText());
        mfs = new MultiFileSystem(wr, fs2, fs1);
        f = mfs.findResource("d/f");
        assertEquals(2, f.getAttribute("a"));
        assertEquals("2", f.asText());
        // And that a higher weight beats a lower weight.
        fs1.findResource("d/f").setAttribute("weight", 200);
        mfs = new MultiFileSystem(wr, fs1, fs2);
        f = mfs.findResource("d/f");
        assertEquals(1, f.getAttribute("a"));
        assertEquals("1", f.asText());
        mfs = new MultiFileSystem(wr, fs2, fs1);
        f = mfs.findResource("d/f");
        assertEquals(1, f.getAttribute("a"));
        assertEquals("1", f.asText());
        // Now test writable layer which should always win regardless of weights.
        mfs = new MultiFileSystem(wr, fs1, fs2);
        f = mfs.findResource("d/f");
        f.setAttribute("a", 0);
        TestFileUtils.writeFile(mfs.getRoot(), "d/f", "0");
        f = wr.findResource("d/f");
        // Oddly, it is null: assertEquals(0, f.getAttribute("a"));
        assertEquals("0", f.asText());
        mfs = new MultiFileSystem(wr, fs1, fs2);
        f = mfs.findResource("d/f");
        assertEquals(0, f.getAttribute("a"));
        assertEquals("0", f.asText());
    }

    public void testMultipleMaskLayers() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file1_hidden",
                "folder/file1",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder2/file2_hidden",
            })
        );
        fs.setPropagateMasks(false);
        try {
            // this is surprising, but it seems to be the behaviour:
            assertEquals("folder/file1 is not hidden !?",
                "file1",
                childrenNames(fs));
        } finally {
            TestUtilHid.destroyXMLFileSystem(getName() + "1");
            TestUtilHid.destroyXMLFileSystem(getName() + "2");
            TestUtilHid.destroyXMLFileSystem(getName() + "3");
        }
    }
    public void testPropagateMultipleMaskLayersSimple() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file1_hidden",
                "folder/file1",
            })
        );
        fs.setPropagateMasks(true);
        assertEquals("folder/file1 is hidden",
            "file1_hidden",
            childrenNames(fs)
        );
    }
    public void testPropagateMultipleMaskLayersComplex() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file1_hidden",
                "folder/file1",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file2_hidden",
            })
        );
        fs.setPropagateMasks(true);
        assertEquals("folder/file1 is hidden",
            "file1_hidden/file2_hidden",
            childrenNames(fs)
        );
    }
}
