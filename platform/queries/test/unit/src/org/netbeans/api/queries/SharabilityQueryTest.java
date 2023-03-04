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
package org.netbeans.api.queries;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.api.queries.SharabilityQuery.Sharability;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Alexander Simon
 */
@SuppressWarnings("deprecation")
public class SharabilityQueryTest extends NbTestCase {
    
    public SharabilityQueryTest(String testMethod) {
        super (testMethod);
    }
    
    private File home = new File(System.getProperty("user.dir"));
    
    @Override
    public void setUp() throws IOException {
        clearWorkDir();
        MockServices.setServices(SharabilityQueryImplementationImpl.class, SharabilityQueryImplementation2Impl.class);
    }
    
    public void testSharableBridge2Old() throws IOException {
        File file = new File(home, "aFile.sharable");
        int sharability = SharabilityQuery.getSharability(file);
        assertEquals(SharabilityQuery.SHARABLE, sharability);
        URI uri = BaseUtilities.toURI(file);
        Sharability sharability2 = SharabilityQuery.getSharability(uri);
        assertEquals(SharabilityQuery.Sharability.SHARABLE, sharability2);
        FileObject fo = FileUtil.toFileObject(getWorkDir()).createData("aFile", "sharable");
        assertEquals(sharability2, SharabilityQuery.getSharability(fo));
    }

    public void testSharableBridge2New() throws IOException {
        File file = new File(home, "aFile.sharable2");
        int sharability = SharabilityQuery.getSharability(file);
        assertEquals(SharabilityQuery.SHARABLE, sharability);
        URI uri = BaseUtilities.toURI(file);
        Sharability sharability2 = SharabilityQuery.getSharability(uri);
        assertEquals(SharabilityQuery.Sharability.SHARABLE, sharability2);
        FileObject fo = FileUtil.toFileObject(getWorkDir()).createData("aFile", "sharable2");
        assertEquals(sharability2, SharabilityQuery.getSharability(fo));
    }

    public void testNotSharableBridge2Old() throws IOException {
        File file = new File(home, "aFile.not_sharable");
        int sharability = SharabilityQuery.getSharability(file);
        assertEquals(SharabilityQuery.NOT_SHARABLE, sharability);
        URI uri = BaseUtilities.toURI(file);
        Sharability sharability2 = SharabilityQuery.getSharability(uri);
        assertEquals(SharabilityQuery.Sharability.NOT_SHARABLE, sharability2);
        FileObject fo = FileUtil.toFileObject(getWorkDir()).createData("aFile", "not_sharable");
        assertEquals(sharability2, SharabilityQuery.getSharability(fo));
    }

    public void testNotSharableBridge2New() throws IOException {
        File file = new File(home, "aFile.not_sharable2");
        int sharability = SharabilityQuery.getSharability(file);
        assertEquals(SharabilityQuery.NOT_SHARABLE, sharability);
        URI uri = BaseUtilities.toURI(file);
        Sharability sharability2 = SharabilityQuery.getSharability(uri);
        assertEquals(SharabilityQuery.Sharability.NOT_SHARABLE, sharability2);
        FileObject fo = FileUtil.toFileObject(getWorkDir()).createData("aFile", "not_sharable2");
        assertEquals(sharability2, SharabilityQuery.getSharability(fo));
    }
    
    public void testMixedBridge2Old() throws IOException {
        File file = new File(home, "aFile.mixed");
        int sharability = SharabilityQuery.getSharability(file);
        assertEquals(SharabilityQuery.MIXED, sharability);
        URI uri = BaseUtilities.toURI(file);
        Sharability sharability2 = SharabilityQuery.getSharability(uri);
        assertEquals(SharabilityQuery.Sharability.MIXED, sharability2);
        FileObject fo = FileUtil.toFileObject(getWorkDir()).createData("aFile", "mixed");
        assertEquals(sharability2, SharabilityQuery.getSharability(fo));
    }

    public void testMixedBridge2New() throws IOException {
        File file = new File(home, "aFile.mixed2");
        int sharability = SharabilityQuery.getSharability(file);
        assertEquals(SharabilityQuery.MIXED, sharability);
        URI uri = BaseUtilities.toURI(file);
        Sharability sharability2 = SharabilityQuery.getSharability(uri);
        assertEquals(SharabilityQuery.Sharability.MIXED, sharability2);
        FileObject fo = FileUtil.toFileObject(getWorkDir()).createData("aFile", "mixed2");
        assertEquals(sharability2, SharabilityQuery.getSharability(fo));
    }

    public void testUnknown() throws IOException {
        File file = new File(home, "aFile.txt");
        int sharability = SharabilityQuery.getSharability(file);
        assertEquals(SharabilityQuery.UNKNOWN, sharability);
        URI uri = BaseUtilities.toURI(file);
        Sharability sharability2 = SharabilityQuery.getSharability(uri);
        assertEquals(SharabilityQuery.Sharability.UNKNOWN, sharability2);
        FileObject fo = FileUtil.toFileObject(getWorkDir()).createData("aFile", "txt");
        assertEquals(sharability2, SharabilityQuery.getSharability(fo));
    }

    public void testNormalized() throws IOException {
        if (BaseUtilities.isMac()) {
            return; //#238760 the test is dubious IMHO as it attempts to test something that only happens when asserts are on.
            //on top of that SharabilityQuery.getSharability() explicitly doesn't throw when BaseUtilities.isMac(), no point in running on mac
        }
        File file = new File(home, "../aFile.txt");
        Exception exception = null;
        try {
            SharabilityQuery.getSharability(file);
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
        URI uri = BaseUtilities.toURI(file);
        exception = null;
        try {
            SharabilityQuery.getSharability(uri);
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    public void testRfs() throws IOException, URISyntaxException {
        URI uri = new URI("rfs", "tester", "localhost", 22, "/home/tester/aFile.sharable", null, null);
        Sharability sharability = SharabilityQuery.getSharability(uri);
        assertEquals(SharabilityQuery.Sharability.UNKNOWN, sharability);
        uri = new URI("rfs", "tester", "localhost", 22, "/home/tester/aFile.sharable2", null, null);
        Sharability sharability2 = SharabilityQuery.getSharability(uri);
        assertEquals(SharabilityQuery.Sharability.SHARABLE, sharability2);
    }

    public static class SharabilityQueryImplementationImpl implements SharabilityQueryImplementation {

        @Override
        public int getSharability(File file) {
            String path = file.getAbsolutePath();
            if (path.endsWith(".sharable")) {
                return SharabilityQuery.SHARABLE;
            } else if (path.endsWith(".not_sharable")) {
                return SharabilityQuery.NOT_SHARABLE;
            } else if (path.endsWith(".mixed")) {
                return SharabilityQuery.MIXED;
            }
            return SharabilityQuery.UNKNOWN;
        }
    }

    public static class SharabilityQueryImplementation2Impl implements SharabilityQueryImplementation2 {

        @Override
        public Sharability getSharability(URI uri) {
            String path = uri.getPath();
            if (path.endsWith(".sharable2")) {
                return SharabilityQuery.Sharability.SHARABLE;
            } else if (path.endsWith(".not_sharable2")) {
                return SharabilityQuery.Sharability.NOT_SHARABLE;
            } else if (path.endsWith(".mixed2")) {
                return SharabilityQuery.Sharability.MIXED;
            }
            return SharabilityQuery.Sharability.UNKNOWN;
        }
    }
}
