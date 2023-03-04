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

package org.openide.filesystems;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Enumerations;
import org.openide.util.test.MockLookup;

public class RepositoryIncludeTest extends NbTestCase {
    
    public RepositoryIncludeTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        Repository.reset();
        MockLookup.setInstances();
    }

    public void testRepositoryIncludesAllLayers() throws Exception {
        Thread.currentThread().setContextClassLoader(new ClassLoader() {
            protected @Override Enumeration<URL> findResources(String name) throws IOException {
                if (name.equals("META-INF/MANIFEST.MF")) {
                    return Enumerations.array(literalURL("OpenIDE-Module-Layer: foo/layer.xml\n"), literalURL("OpenIDE-Module-Layer: bar/layer.xml\n"));
                } else {
                    return super.findResources(name);
                }
            }
            protected @Override URL findResource(String name) {
                if (name.equals("foo/layer.xml")) {
                    return RepositoryIncludeTest.class.getResource("test-layer-1.xml");
                } else if (name.equals("bar/layer.xml")) {
                    return RepositoryIncludeTest.class.getResource("test-layer-2.xml");
                } else {
                    return super.findResource(name);
                }
            }
        });
        FileObject r = FileUtil.getConfigRoot();
        assertTrue(r.isValid());
        List<FileObject> charr = Arrays.asList(r.getChildren());
        // org.openide.filesystems.resources.layer.xml, test-layer-1.xml, test-layer-2.xml, test/generated-layer.xml
        assertNotNull(r.getFileObject("foo"));
        assertNotNull(r.getFileObject("bar"));
        assertEquals("Expecting four" + charr, 4, charr.size());  
    }
    static URL literalURL(final String content) {
        try {
            return new URL("literal", null, 0, content, new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL u) throws IOException {
                    return new URLConnection(u) {
                        public @Override
                        InputStream getInputStream() throws IOException {
                            return new ByteArrayInputStream(content.getBytes());
                        }

                        public @Override
                        void connect() throws IOException {
                        }
                    };
                }
            });
        } catch (MalformedURLException x) {
            throw new AssertionError(x);
        }
    }
    
}
