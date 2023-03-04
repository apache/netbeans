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
package org.netbeans.modules.java.source.ui;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.ui.OpenProjectFastIndex.NameIndex;
import org.netbeans.spi.jumpto.type.JumptoAccessor;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider.Context;
import org.netbeans.spi.jumpto.type.TypeProvider.Result;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.Utilities;

/**
 *
 * @author sdedic
 */
public class FastIndexTest extends NbTestCase {

    public FastIndexTest(String name) {
        super(name);
    }
    
    /**
     * Checks that directory scanner lists all the files into NameIndex.
     * 
     * @throws Exception 
     */
    public void testDirectoryScanner() throws Exception {
        File rtfile = TestUtil.getJdkSources();
        JarFileSystem jfs = new JarFileSystem(rtfile);
        
        FileObject root = jfs.getRoot();
        
        OpenProjectFastIndex.IndexBuilder builder = new OpenProjectFastIndex.IndexBuilder(
                null, Collections.<FileObject>singleton(root), Collections.<FileObject>emptyList());
        
        Map<FileObject, NameIndex> indexes = builder.build();
        NameIndex nameIndex = indexes.values().iterator().next();

        Enumeration<? extends FileObject> contents = root.getData(true);
        
        int fileCount = 0;
        
        c: while (contents.hasMoreElements()) {
            boolean shouldFind = true;
            FileObject f = contents.nextElement();
            if (!Utilities.isJavaIdentifier(f.getName())) {
                shouldFind = false;
            } else if (!"java".equals(f.getExt())) {
                shouldFind = false;
            } else {
                String[] path = f.getPath().split("/");
                for (int i = 0; i < path.length - 1; i++) {
                    String s = path[i];
                    if (!Utilities.isJavaIdentifier(s)) {
                        shouldFind = false;
                        break;
                    }
                }
            }
            
            Pattern p = Pattern.compile("^" + f.getName() + "$", Pattern.MULTILINE);
            
            Matcher m = p.matcher(nameIndex.files());
            
            if (shouldFind) {
                fileCount++;
            }

            while (true) {
                if (shouldFind) {
                    // also covers the case, when there are multiple matches, but the
                    // "right" one is not found
                    assertTrue("Should find file " + f.getPath(), m.find());
                } else {
                    assertFalse(m.find());
                    continue c;
                }
                CharSequence name = nameIndex.getFilename(m.start(), m.end());
                assertEquals(f.getName(), name.toString());
                String path = nameIndex.findPath(m.start());
                if (path.equals(f.getParent().getPath().replace("/", "."))) {
                    break;
                }
            }
        }
        
        // last check: count non-empty lines
        Pattern p = Pattern.compile("^.+$", Pattern.MULTILINE);
        Matcher m = p.matcher(nameIndex.files());

        while (fileCount-- > 0) {
            boolean found = m.find();
            assertTrue(found);
        }
        assertEquals(-1, fileCount);
    }
    
    public void testFastIndexTrigger() {
        
    }
    
    
    
    public void testFastIndexProvider() throws Exception {
        File rtfile = TestUtil.getJdkSources();
        JarFileSystem jfs = new JarFileSystem(rtfile);
        
        FileObject root = jfs.getRoot();
        assertNotNull(root);
        if (root.getFileObject("java") == null) {   //NOI18N
            root = root.getFileObject("src");       //NOI18N
            assertNotNull(root);
            assertNotNull(root.getFileObject("java"));  //NOI18N
        }
        
        OpenProjectFastIndex.IndexBuilder builder = new OpenProjectFastIndex.IndexBuilder(null,
                Collections.<FileObject>singleton(root), Collections.<FileObject>emptyList());
        
        final Map<FileObject, NameIndex> indexes = builder.build();
        
        OpenProjectFastIndex fastIndex = new OpenProjectFastIndex(true) {

            @Override
            public Map<FileObject, NameIndex> copyIndexes() {
                return indexes;
            }
            
        };
        
        FastTypeProvider provider = new FastTypeProvider(fastIndex);

        List<TypeDescriptor> results = new ArrayList<TypeDescriptor>();        
        
        // search by prefix:
        Context c = JumptoAccessor.createContext(null, "ZipException", SearchType.PREFIX);
        Result r = JumptoAccessor.createResult(results, c);
        
        provider.computeTypeNames(c, r);
        assertEquals(1, results.size());
        
        TypeDescriptor d = results.get(0);
        
        // should find java.util.zip.ZipException
        assertEquals("( java.util.zip )", d.getContextName());
        assertNull(d.getOuterName());
        assertEquals("ZipException", d.getSimpleName());
        assertEquals(root.getFileObject("java/util/zip/ZipException.java"), d.getFileObject());
        
        results.clear();
        
        c = JumptoAccessor.createContext(null, "zIpExcePtion", SearchType.CASE_INSENSITIVE_PREFIX);
        provider.computeTypeNames(c, r);
        assertEquals(1, results.size());
        
        results.clear();
        c = JumptoAccessor.createContext(null, "IllForEx", SearchType.CAMEL_CASE);
        provider.computeTypeNames(c, r);
        assertEquals(1, results.size());
        
        d = results.get(0);
        
        // should find java.util.zip.ZipException
        assertEquals("( java.util )", d.getContextName());
        assertNull(d.getOuterName());
        assertEquals("IllegalFormatException", d.getSimpleName());
        assertEquals(root.getFileObject("java/util/IllegalFormatException.java"), d.getFileObject());
    }
    
}
