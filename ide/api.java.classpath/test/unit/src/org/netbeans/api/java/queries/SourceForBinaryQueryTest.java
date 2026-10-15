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
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.netbeans.spi.java.queries.support.SourceForBinaryQueryImpl2Base;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class SourceForBinaryQueryTest extends NbTestCase {
    
    public SourceForBinaryQueryTest (final String name) {
        super (name);
    }
    
    private FileObject br1;
    private FileObject br2;
    private FileObject sr1;
    private FileObject sr2;
    
    private static final Map<URL,List<FileObject>> map = new HashMap<URL, List<FileObject>>();

    @Override
    protected void setUp() throws Exception {
        this.clearWorkDir();
        super.setUp();
        File wd = this.getWorkDir();
        FileObject wdfo = FileUtil.toFileObject(wd);
        br1 = wdfo.createFolder("bin1");
        br2 = wdfo.createFolder("bin2");
        sr1 = wdfo.createFolder("src1");
        File zf = new File (wd,"src2.zip");
        ZipOutputStream zos = new ZipOutputStream (new FileOutputStream (zf));
        zos.putNextEntry(new ZipEntry("foo.java"));
        zos.closeEntry();
        zos.close();
        sr2 = FileUtil.getArchiveRoot(FileUtil.toFileObject(zf));
        map.put(br1.toURL(), Collections.singletonList(sr1));
        map.put(br2.toURL(), Collections.singletonList(sr2));
    }
    
    public void testSFBQImpl () throws Exception {
        MockServices.setServices(LegacySFBQImpl.class);
        SourceForBinaryQuery.Result2 res = SourceForBinaryQuery.findSourceRoots2(br1.toURL());
        assertNotNull(res);
        assertEquals(1, res.getRoots().length);
        assertEquals(Collections.singletonList(sr1), Arrays.asList(res.getRoots()));
        assertTrue(res.preferSources());
        
        res = SourceForBinaryQuery.findSourceRoots2(br2.toURL());
        assertNotNull(res);
        assertEquals(1, res.getRoots().length);
        assertEquals(Collections.singletonList(sr2), Arrays.asList(res.getRoots()));
        assertFalse(res.preferSources());
        
        res = SourceForBinaryQuery.findSourceRoots2(Utilities.toURI(getWorkDir()).toURL());
        assertNotNull(res);
        assertEquals(0, res.getRoots().length);
        assertFalse(res.preferSources());
    }
    
    public void testSFBQImpl2 () throws Exception {
        MockServices.setServices(LeafSFBQImpl.class);
        SourceForBinaryQuery.Result2 res = SourceForBinaryQuery.findSourceRoots2(br1.toURL());
        assertNotNull(res);
        assertEquals(1, res.getRoots().length);
        assertEquals(Collections.singletonList(sr1), Arrays.asList(res.getRoots()));
        assertFalse(res.preferSources());
        
        res = SourceForBinaryQuery.findSourceRoots2(br2.toURL());
        assertNotNull(res);
        assertEquals(1, res.getRoots().length);
        assertEquals(Collections.singletonList(sr2), Arrays.asList(res.getRoots()));
        assertTrue(res.preferSources());
        
        res = SourceForBinaryQuery.findSourceRoots2(Utilities.toURI(getWorkDir()).toURL());
        assertNotNull(res);
        assertEquals(0, res.getRoots().length);
        assertFalse(res.preferSources());
        
    }
    
    
    public void testSFBQDelegatingImpl () throws Exception {
        DelegatingSFBImpl.impl = new LegacySFBQImpl();
        MockServices.setServices(DelegatingSFBImpl.class);
        SourceForBinaryQuery.Result2 res = SourceForBinaryQuery.findSourceRoots2(br1.toURL());
        assertNotNull(res);
        assertEquals(1, res.getRoots().length);
        assertEquals(Collections.singletonList(sr1), Arrays.asList(res.getRoots()));
        assertTrue(res.preferSources());
        
        res = SourceForBinaryQuery.findSourceRoots2(br2.toURL());
        assertNotNull(res);
        assertEquals(1, res.getRoots().length);
        assertEquals(Collections.singletonList(sr2), Arrays.asList(res.getRoots()));
        assertFalse(res.preferSources());
        
        res = SourceForBinaryQuery.findSourceRoots2(Utilities.toURI(getWorkDir()).toURL());
        assertNotNull(res);
        assertEquals(0, res.getRoots().length);
        assertFalse(res.preferSources());
        
    }
    
    
    public void testSFBQDelegatingImpl2 () throws Exception {
        DelegatingSFBImpl.impl = new LeafSFBQImpl();
        MockServices.setServices(DelegatingSFBImpl.class);
        SourceForBinaryQuery.Result2 res = SourceForBinaryQuery.findSourceRoots2(br1.toURL());
        assertNotNull(res);
        assertEquals(1, res.getRoots().length);
        assertEquals(Collections.singletonList(sr1), Arrays.asList(res.getRoots()));
        assertFalse(res.preferSources());
        
        res = SourceForBinaryQuery.findSourceRoots2(br2.toURL());
        assertNotNull(res);
        assertEquals(1, res.getRoots().length);
        assertEquals(Collections.singletonList(sr2), Arrays.asList(res.getRoots()));
        assertTrue(res.preferSources());
        
        res = SourceForBinaryQuery.findSourceRoots2(Utilities.toURI(getWorkDir()).toURL());
        assertNotNull(res);
        assertEquals(0, res.getRoots().length);
        assertFalse(res.preferSources());
    }
    
    
    public void testListening () throws Exception {
        DelegatingSFBImpl.impl = new LeafSFBQImpl();
        MockServices.setServices(DelegatingSFBImpl.class);
        SourceForBinaryQuery.Result2 res = SourceForBinaryQuery.findSourceRoots2(br1.toURL());
        final AtomicBoolean fired = new AtomicBoolean ();
        ChangeListener l;
        res.addChangeListener( l = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                fired.set(true);
            }
        });
        LeafSFBQImpl.lastResult.fire();
        res.removeChangeListener(l);
        assertTrue(fired.get());
    }
    
    //Prefers sources for archives => behaves opposite to default
    public static class LeafSFBQImpl implements SourceForBinaryQueryImplementation2 {
        
        static R2 lastResult;
        
        public Result findSourceRoots2(URL binaryRoot) {
            lastResult = null;
            List<FileObject> data =  map.get(binaryRoot);
            if (data != null) {
                return lastResult = new R2(data.toArray(new FileObject[0]), prefSources(data));
            }
            return null;
        }
        
        private static boolean prefSources(List<FileObject> l) {
            for (FileObject f : l ) {
                if (FileUtil.getArchiveFile(f) != null) {
                    return true;
                }
            }
            return false;
        }

        public Result findSourceRoots(URL binaryRoot) {
            return this.findSourceRoots2(binaryRoot);
        }
        
    }
    
    public static class DelegatingSFBImpl extends SourceForBinaryQueryImpl2Base {
        
        private static SourceForBinaryQueryImplementation impl; 
        
        
        public Result findSourceRoots2(URL binaryRoot) {
            if (DelegatingSFBImpl.impl == null) {
                throw new IllegalStateException ();
            }
            else if (DelegatingSFBImpl.impl instanceof SourceForBinaryQueryImplementation2) {
                return ((SourceForBinaryQueryImplementation2)DelegatingSFBImpl.impl).findSourceRoots2(binaryRoot);
            }
            else {
                final SourceForBinaryQuery.Result result = DelegatingSFBImpl.impl.findSourceRoots(binaryRoot);
                return result == null ? null : asResult(result);
            }
        }

        public Result findSourceRoots(URL binaryRoot) {
            return this.findSourceRoots2(binaryRoot);
        }
        
    }
    
    public static class LegacySFBQImpl implements SourceForBinaryQueryImplementation {

        public Result findSourceRoots(URL binaryRoot) {
            List<FileObject> data =  map.get(binaryRoot);
            if (data != null) {
                return new R(data.toArray(new FileObject[0]));
            }
            return null;
        }
        
    }
    
    private static class R implements SourceForBinaryQuery.Result {
        
        final FileObject[] roots;
        private final ChangeSupport chs = new ChangeSupport(this);
        
        public R (final FileObject[] roots) {
            this.roots = roots;
        }

        public FileObject[] getRoots() {
            return this.roots;
        }

        public void addChangeListener(ChangeListener l) {
            chs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            chs.removeChangeListener(l);
        }
        
        void fire () {
            chs.fireChange();
        }
        
    }
    
    private static class R2 extends R implements SourceForBinaryQueryImplementation2.Result {
        
        final boolean ps;
        
        public R2 (final FileObject[] roots, final boolean ps) {
            super (roots);
            this.ps = ps;
        }

        public boolean preferSources() {
            return ps;
        }
    }

}
