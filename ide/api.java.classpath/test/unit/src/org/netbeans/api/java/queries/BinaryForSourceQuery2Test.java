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

package org.netbeans.api.java.queries;

import java.net.URL;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

public class BinaryForSourceQuery2Test extends NbTestCase {

    private FileObject srcRoot1;
    private FileObject srcRoot2;
    private FileObject binaryRoot2;


    public BinaryForSourceQuery2Test (String n) {
        super(n);
    }

    public void testQuery2() throws Exception {
        MockServices.setServices(TestQuery2.class);

        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject data = FileUtil.createData(fs.getRoot(), "fldr/data.txt");
        URL url = data.toURL();

        assertNull("No query yet", TestQuery2.LAST);

        BinaryForSourceQuery.Result2 result = BinaryForSourceQuery.findBinaryRoots2(url);

        assertNotNull("Queried now", TestQuery2.LAST);
        assertTrue("prefers binaries", result.preferBinaries());

        URL[] roots = result.getRoots();
        assertEquals("One", 1, roots.length);
        assertEquals("Same", roots[0], url);

        ChangeListener listener = (ChangeEvent e) -> {};
        result.addChangeListener(listener);
        assertEquals(TestQuery2.LAST.listener, listener);
        result.removeChangeListener(listener);
        assertNull(TestQuery2.LAST.listener);

    }

    private static final class AnyData {
        final URL url;
        final FileObject fo;

        AnyData(URL url, FileObject fo) {
            assertNotNull("FileObject found", fo);
            this.fo = fo;
            this.url = url;
        }

    }

    public static class TestQuery2 implements BinaryForSourceQueryImplementation2<AnyData>{
        static TestQuery2 LAST;
        ChangeListener listener;

        public TestQuery2() {
        }

        @Override
        public AnyData findBinaryRoots2(URL sourceRoot) {
            LAST = this;
            return new AnyData(sourceRoot, URLMapper.findFileObject(sourceRoot));
        }

        @Override
        public URL[] computeRoots(AnyData result) {
            return new URL[] { result.url };
        }

        @Override
        public boolean computePreferBinaries(AnyData result) {
            return true;
        }

        @Override
        public void computeChangeListener(AnyData result, boolean add, ChangeListener l) {
            if (add) {
                assertNull("No listener yet", this.listener);
                this.listener = l;
            } else {
                assertEquals("Removing", this.listener, l);
                this.listener = null;
            }
        }
    }

}
