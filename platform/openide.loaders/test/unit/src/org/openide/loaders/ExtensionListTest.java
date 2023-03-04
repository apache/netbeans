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

package org.openide.loaders;

import java.io.IOException;
import java.util.Enumeration;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;

/**
 *
 * @author Jaroslav Tulach
 */
public class ExtensionListTest extends TestCase {
    
    public ExtensionListTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddExtension() {
        ExtensionList instance = new ExtensionList();
        instance.addExtension("html");
        instance.addExtension("txt");
        instance.addExtension("java");
        Enumeration<String> en = instance.extensions();
        
        assertEquals("html", en.nextElement());
        assertEquals("java", en.nextElement());
        assertEquals("txt", en.nextElement());
        assertFalse(en.hasMoreElements());
        
        assertTrue(instance.isRegistered("x.java"));
        assertTrue(instance.isRegistered("x.html"));
        assertTrue(instance.isRegistered("x.txt"));
        assertFalse(instance.isRegistered("x.form"));
    }     

    public void testAddMime() throws IOException {
        ExtensionList instance = new ExtensionList();
        instance.addMimeType("text/x-java");
        instance.addMimeType("text/html");
        instance.addMimeType("text/plain");
        Enumeration<String> en = instance.mimeTypes();
        
        assertEquals("text/html", en.nextElement());
        assertEquals("text/plain", en.nextElement());
        assertEquals("text/x-java", en.nextElement());
        assertFalse(en.hasMoreElements());
        

        MockServices.setServices(MockMimeR.class);
        
        FileObject fo = FileUtil.getConfigRoot().createData("My.xml");
        assertFalse("XML files are not recognized", instance.isRegistered(fo));
        assertEquals("Instantiated", 1, MockMimeR.cnt);
    }   
    
    public static final class MockMimeR extends MIMEResolver {
        static int cnt;
        
        public MockMimeR() {
            super("text/xml");
            cnt++;
        }
        
        @Override
        public String findMIMEType(FileObject fo) {
            fail("Shall not be called at all");
            return null;
        }
        
    }
}
