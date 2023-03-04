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
package org.netbeans.modules.openide.filesystems.declmime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ReadJustOnceTest extends NbTestCase {
    public ReadJustOnceTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        URL u = this.getClass().getResource ("code-fs.xml");        
        FileSystem fs = new XMLFileSystem(u);
        FileObject resolversRoot = fs.getRoot().getFileObject("root");
        resolversRoot.refresh();
        
        FileObject fos[] = resolversRoot.getChildren();
        List<MIMEResolver> resolvers = new ArrayList<MIMEResolver>();
        for (int i = 0; i<fos.length; i++) {
            resolvers.add(createResolver(fos[i]));
        }
        
        MockLookup.setInstances(resolvers.toArray());
    }

    protected MIMEResolver createResolver(FileObject fo) throws Exception {
        if (fo == null) {
            throw new NullPointerException();
        }
        return MIMEResolverImpl.forDescriptor(fo);
    }
    
    public void testHowManyReads() {
        final AtomicInteger cnt = new AtomicInteger();
        LocalFileSystem lfs = new LocalFileSystem() {
            @Override
            protected String[] children(String name) {
                return new String[] { "data.txt" };
            }

            @Override
            protected boolean folder(String name) {
                return !name.equals("data.txt");
            }
            
            @Override
            protected InputStream inputStream(String name) throws FileNotFoundException {
                return new InputStream() {
                    @Override
                    public int read() throws IOException {
                        cnt.incrementAndGet();
                        throw new IOException();
                    }
                };
            }
        };
        FileObject fo = lfs.findResource("data.txt");
        assertNotNull("File object found", fo);
        assertEquals("No mime type", "content/unknown", fo.getMIMEType());
        assertEquals("One query for input stream", 1, cnt.intValue());
    }
}
