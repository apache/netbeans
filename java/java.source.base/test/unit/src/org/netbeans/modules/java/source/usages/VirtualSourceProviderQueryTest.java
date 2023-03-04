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

package org.netbeans.modules.java.source.usages;

import java.net.URL;
import org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.parsing.impl.indexing.DeletedIndexable;
import org.netbeans.modules.parsing.impl.indexing.FileObjectIndexable;
import org.netbeans.modules.parsing.impl.indexing.IndexableImpl;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class VirtualSourceProviderQueryTest extends NbTestCase {
        
    public VirtualSourceProviderQueryTest (final String name) {
        super(name);
    }
    
    @Override
    public void setUp () throws Exception {
        clearWorkDir();
        MockServices.setServices(TestVirtualSourceProvider.class);
    }
    
    public void testVirtualSourceProvider () throws Exception {
        final File root = new File (getWorkDir(),"src");    //NOI18N
        root.mkdir();
        final Indexable[] data = prepareData(Utilities.toURI(root).toURL());
        final Iterable<? extends CompileTuple> res = VirtualSourceProviderQuery.translate(Arrays.asList(data), root);
        assertEquals(new String[] {"a","b","c","d"}, res);      //NOI18N
    }
    
    private static Indexable[] prepareData (final URL root) {
        final Indexable[] result = new Indexable[4];
        result[0] = SPIAccessor.getInstance().create(new DeletedIndexable(root, "a.groovy"));  //NOI18N
        result[1] = SPIAccessor.getInstance().create(new DeletedIndexable(root, "b.groovy"));  //NOI18N
        result[2] = SPIAccessor.getInstance().create(new DeletedIndexable(root, "c.scala"));   //NOI18N
        result[3] = SPIAccessor.getInstance().create(new DeletedIndexable(root, "d.scala"));   //NOI18N
        return result;        
    }
    
    private static void assertEquals (final String[] expected, Iterable<? extends CompileTuple> data) {
        final Set<String> es = new HashSet<String>();
        es.addAll(Arrays.asList(expected));
        for (CompileTuple p : data) {
            assertTrue (es.remove(p.jfo.inferBinaryName()));
        }
        assertTrue(es.isEmpty());
    }
    
    public static class TestVirtualSourceProvider implements VirtualSourceProvider {

        public Set<String> getSupportedExtensions() {
            final Set<String> result = new HashSet<String>();
            result.add("groovy");   //NOI18N
            result.add ("scala");   //NOI18N
            return result;
        }

        public boolean index () {
            return true;
        }

        public void translate(Iterable<File> files, File sourceRoot, VirtualSourceProvider.Result r) {
            final Set<String> ext = new HashSet<String>();
            final CharSequence d = "";  //NOI18N
            for (File f : files) {
                ext.add(FileObjects.getExtension(f.getName()));
                String rp = FileObjects.getRelativePath(sourceRoot, f);
                int index = rp.lastIndexOf('.');    //NOI18N
                if (index >= 0) {
                    rp = rp.substring(0, index);
                }
                r.add(f, "", rp, d);
            }
            assertEquals(1, ext.size());
        }
        
    }
    
}
