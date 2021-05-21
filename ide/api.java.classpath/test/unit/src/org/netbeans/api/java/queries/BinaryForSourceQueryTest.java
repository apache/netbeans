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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * @author Tomas Zezula
 */
public class BinaryForSourceQueryTest extends NbTestCase {
    
    private FileObject srcRoot1;
    private FileObject srcRoot2;
    private FileObject binaryRoot2;            
    

    public BinaryForSourceQueryTest (String n) {
        super(n);
    }        
    
    @Override
    protected void setUp () throws IOException {
        MockServices.setServices(new Class<?>[] {CPProvider.class, SFBQImpl.class});
        this.clearWorkDir();
        File wd = this.getWorkDir();
        FileObject root = FileUtil.toFileObject(wd);
        assertNotNull(root);
        srcRoot1 = root.createFolder("src1");
        assertNotNull(srcRoot1);
        srcRoot2 = root.createFolder("src2");
        assertNotNull(srcRoot2);
        binaryRoot2 = root.createFolder("binary2");
        assertNotNull(binaryRoot2);       
        SFBQImpl.clear();
        CPProvider.clear();
        SFBQImpl.register(srcRoot2.toURL(), binaryRoot2.toURL());
        CPProvider.register(srcRoot2, ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[] {srcRoot2}));
        CPProvider.register(srcRoot2, ClassPath.EXECUTE, ClassPathSupport.createClassPath(new FileObject[] {binaryRoot2}));        
    }
    
    public void testQuery() throws Exception {
        BinaryForSourceQuery.Result result = BinaryForSourceQuery.findBinaryRoots(srcRoot1.toURL());
        assertEquals(0,result.getRoots().length);        
        result = BinaryForSourceQuery.findBinaryRoots(srcRoot2.toURL());
        assertEquals(1,result.getRoots().length);
        assertEquals(binaryRoot2.toURL(), result.getRoots()[0]);
    }
    
    public static class SFBQImpl implements SourceForBinaryQueryImplementation {        
                
        private static final Map<URL,URL> data = new HashMap<URL,URL>();
        
        static void clear () {
            data.clear();
        }
        
        static void register (URL source, URL binary) {
            data.put (binary,source);
        }
            
        public Result findSourceRoots(URL binaryRoot) {
            URL src = data.get (binaryRoot);
            if (src == null) {
                return null;
            }
            final FileObject fo = URLMapper.findFileObject(src);
            if (fo == null) {
                return null;
            }
            return new SourceForBinaryQuery.Result () {                
                public FileObject[] getRoots() {
                    return new FileObject[] {fo};
                }                
                public void addChangeListener (ChangeListener l) {}
                
                public void removeChangeListener (ChangeListener l) {}
            };
        }        
    }
    
    public static class CPProvider implements ClassPathProvider {
        
        
        private static final Map<FileObject,Map<String,ClassPath>> data = new HashMap<FileObject,Map<String,ClassPath>>();
        
        static void clear () {
            data.clear();
        }
        
        static void register (FileObject fo, String type, ClassPath cp) {
            Map<String,ClassPath> m = data.get (fo);
            if (m == null) {
                m = new HashMap<String,ClassPath>();
                data.put (fo,m);
            }
            m.put (type,cp);
        }
            
        public ClassPath findClassPath(FileObject file, String type) {
            Map<String,ClassPath> m = data.get (file);
            if (m == null) {
                return null;
            }
            return m.get (type);
        }        
    }
    
    
}
