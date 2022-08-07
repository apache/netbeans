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
import java.util.Objects;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

public class BinaryForSourceQuery2Test extends NbTestCase {
    public BinaryForSourceQuery2Test (String n) {
        super(n);
    }

    public void testQuery2() throws Exception {
        MockServices.setServices(SampleQuery.class);
        SampleQuery sampleQuery = Lookup.getDefault().lookup(SampleQuery.class);
        assertNotNull("Instance of SampleQuery is registered", sampleQuery);

        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject data = FileUtil.createData(fs.getRoot(), "fldr/data.txt");
        URL url = data.toURL();


        BinaryForSourceQuery.Result2 result = BinaryForSourceQuery.findBinaryRoots2(url);

        assertNotNull("Queried now", sampleQuery);
        assertTrue("prefers binaries", result.preferBinaries());

        URL[] roots = result.getRoots();
        assertEquals("One", 1, roots.length);
        assertEquals("Same", roots[0], url);

        SampleQuery.PrivateData privateData = (SampleQuery.PrivateData) BinaryForSourceQuery.CACHE.findRegistered(new SampleQuery.PrivateData(url, null));
        assertNotNull("Internal data found", privateData);

        ChangeListener listener = (ChangeEvent e) -> {};
        result.addChangeListener(listener);
        assertEquals(privateData.listener, listener);
        result.removeChangeListener(listener);
        assertNull(privateData.listener);

        BinaryForSourceQuery.Result2 result2 = BinaryForSourceQuery.findBinaryRoots2(url);
        assertSame("The result should be cached", result, result2);
    }

    @SuppressWarnings("PackageVisibleField")
    // @start region="SampleQuery"
    @ServiceProvider(service = BinaryForSourceQueryImplementation.class)
    public static final class SampleQuery
    implements BinaryForSourceQueryImplementation2<SampleQuery.PrivateData> {
        public SampleQuery() {
        }

        @Override
        public PrivateData findBinaryRoots2(URL sourceRoot) {
            final FileObject fo = URLMapper.findFileObject(sourceRoot);
            assertNotNull("FileObject found", fo);
            return new PrivateData(sourceRoot, fo);
        }

        @Override
        public URL[] computeRoots(PrivateData result) {
            return new URL[] { result.url };
        }

        @Override
        public boolean computePreferBinaries(PrivateData result) {
            return true;
        }

        @Override
        public void computeChangeListener(PrivateData data, boolean add, ChangeListener l) {
            if (add) {
                assertNull("No listener yet", data.listener);
                data.listener = l;
            } else {
                assertEquals("Removing", data.listener, l);
                data.listener = null;
            }
        }

        public static final class PrivateData {
            final URL url;
            final FileObject fo;
            ChangeListener listener;

            PrivateData(URL url, FileObject fo) {
                this.fo = fo;
                this.url = url;
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 97 * hash + Objects.hashCode(this.url);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final PrivateData other = (PrivateData) obj;
                return Objects.equals(this.url, other.url);
            }
        }
    }
    // @end region="SampleQuery"
}
