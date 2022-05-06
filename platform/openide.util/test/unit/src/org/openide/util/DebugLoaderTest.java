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
package org.openide.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DebugLoaderTest {

    //--------------------------------------------------------------------------
    private ClassLoader newParentLoder() throws Exception {

        return NbBundleTest.fixedLoader("ISO-8859-1",
                "foo/Bundle.properties:k=v\n",
                "bar/Bundle.properties:k=v\n",
                "foo/xyz.properties:k=v2",
                "foo/xyz.txt:k=v2");
    }

    //--------------------------------------------------------------------------
    private BufferedReader buffered(final InputStream in) {

        return new BufferedReader(new InputStreamReader(in));
    }

    //--------------------------------------------------------------------------
    @Test
    public void get_returnsDifferenLoders_forDifferentParents() throws Exception {

        ClassLoader loader1 = DebugLoader.get(newParentLoder());
        ClassLoader loader2 = DebugLoader.get(newParentLoder());

        assertTrue(loader1 instanceof DebugLoader);
        assertTrue(loader2 instanceof DebugLoader);
        assertNotEquals(loader1, loader2);
    }

    //--------------------------------------------------------------------------
    @Test
    public void get_returnsTheSameLoader_forTeSameParent() throws Exception {

        ClassLoader parent = newParentLoder();

        ClassLoader loader1 = DebugLoader.get(parent);
        ClassLoader loader2 = DebugLoader.get(parent);

        assertEquals(loader1, loader2);
    }

    //--------------------------------------------------------------------------
    @Test
    public void getResourceAsStream_returnsAnnotatedBundle() throws Exception {

        ClassLoader loader = DebugLoader.get(newParentLoder());

        try ( BufferedReader in
                = buffered(loader.getResourceAsStream("foo/Bundle.properties"))) {

            // bundle index changes so verify if annotation suffix is present
            assertTrue(in.readLine().endsWith(":1)"));
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void getResourceAsStream_returnsUnannotatedBundle() throws Exception {

        ClassLoader loader = DebugLoader.get(newParentLoder());

        try ( BufferedReader in
                = buffered(loader.getResourceAsStream("foo/xyz.properties"))) {

            assertEquals("k=v2", in.readLine());
        }
        try ( BufferedReader in
                = buffered(loader.getResourceAsStream("foo/xyz.txt"))) {

            assertEquals("k=v2", in.readLine());
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void getResourceAsStream_returnsNull_forNullResource() throws Exception {

        ClassLoader loader = DebugLoader.get(newParentLoder());

        assertNull(loader.getResourceAsStream(null));
    }

    //--------------------------------------------------------------------------
    @Test
    public void getResourceAsStream_returnsNull_forUnknownResource() throws Exception {

        ClassLoader loader = DebugLoader.get(newParentLoder());

        assertNull(loader.getResourceAsStream("unknown"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void getResourceAsStream_returnsBundleAnnotatedWithTheSameIndex_forTheSameResources()
            throws Exception {

        ClassLoader loader = DebugLoader.get(newParentLoder());

        String line1;

        try ( BufferedReader in
                = buffered(loader.getResourceAsStream("foo/Bundle.properties"))) {

            line1 = in.readLine();
        }
        
        String line2;

        try ( BufferedReader in
                = buffered(loader.getResourceAsStream("foo/Bundle.properties"))) {

            line2 = in.readLine();
        }
        
        assertEquals(line1, line2);
    }
    
    //--------------------------------------------------------------------------
    @Test
    public void getResourceAsStream_returnsBundleAnnotatedWithDifferentIndex_forDifferentResources()
            throws Exception {

        ClassLoader loader = DebugLoader.get(newParentLoder());

        String line1;

        try ( BufferedReader in
                = buffered(loader.getResourceAsStream("foo/Bundle.properties"))) {

            line1 = in.readLine();
        }
        
        String line2;

        try ( BufferedReader in
                = buffered(loader.getResourceAsStream("bar/Bundle.properties"))) {

            line2 = in.readLine();
        }
        
        assertNotEquals(line1, line2);
    }
}
