/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
