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
import org.netbeans.spi.queries.VersioningQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Tomas Stupka
 */
public class VersioningQueryTest extends NbTestCase {
    
    public VersioningQueryTest(String testMethod) {
        super (testMethod);
    }
    
    private final File home = new File(System.getProperty("user.dir"));
    
    @Override
    public void setUp() throws IOException {
        clearWorkDir();
        MockServices.setServices(VersioningQueryImplementationImpl.class);
    }
    
    public void testIsManaged() throws IOException {
        File file = new File(home, "aFile.vcs");
        assertTrue(VersioningQuery.isManaged(BaseUtilities.toURI(file)));
    }
    
    public void testIsNotManaged() throws IOException {
        File file = new File(home, "aFile.txt");
        assertFalse(VersioningQuery.isManaged(BaseUtilities.toURI(file)));
    }
    
    public void testGetRemoteLocation() throws IOException {
        File file = new File(home, "aFile.vcs");
        assertEquals(BaseUtilities.toURI(file).toString(), VersioningQuery.getRemoteLocation(BaseUtilities.toURI(file)));
    }
    
    public void testNoRemoteLocation() throws IOException {
        File file = new File(home, "aFile.txt");
        assertNull(VersioningQuery.getRemoteLocation(BaseUtilities.toURI(file)));
    }
    
    public void testNormalized() throws IOException {
        File file = new File(home, "../aFile.txt");
        Exception exception = null;
        try {
            VersioningQuery.isManaged(BaseUtilities.toURI(file));
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
        URI uri = BaseUtilities.toURI(file);
        exception = null;
        try {
            VersioningQuery.getRemoteLocation(BaseUtilities.toURI(file));
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    public static class VersioningQueryImplementationImpl implements VersioningQueryImplementation {

        @Override
        public boolean isManaged(URI uri) {
            File file = BaseUtilities.toFile(uri);
            String path = file.getAbsolutePath();
            return path.endsWith(".vcs");
        }

        @Override
        public String getRemoteLocation(URI uri) {
            File file = BaseUtilities.toFile(uri);
            String path = file.getAbsolutePath();
            return path.endsWith(".vcs") ? uri.toString() : null;
        }
    }

}
