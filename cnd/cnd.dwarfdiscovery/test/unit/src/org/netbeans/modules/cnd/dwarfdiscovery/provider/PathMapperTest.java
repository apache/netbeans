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
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.RelocatablePathMapper.ResolvedPath;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.RelocatablePathMapperImpl.MapperEntry;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.utils.CndPathUtilities;

/**
 *
 */
public class PathMapperTest extends NbTestCase {

    public PathMapperTest() {
        super("PathMapperTest");
        Logger.getLogger("cnd.logger").setLevel(Level.SEVERE);
        System.getProperties().put("cnd.dwarfdiscovery.trace.read.errors",Boolean.TRUE);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testLocalBuildMapper() {
        // build locally, binary avaliable by using paths /net/server
        String path = Dwarf.fileFinder("/net/server/home/user/projects/application/dist/Debug/GNU-MacOSX", "/home/user/projects/application/main.cc");
        assertEquals("/net/server/home/user/projects/application/main.cc", path);
    }

    public void testLocalBuildMapperWin() {
        // build locally, binary avaliable by using paths /net/server
        String path = Dwarf.fileFinder("K:/net/server/home/user/projects/application/dist/Debug/GNU-MacOSX", "C:/home/user/projects/application/main.cc");
        assertEquals("K:/net/server/home/user/projects/application/main.cc", path);
    }
    
    public void testNetBuildMapper() {
        // build by using paths /net/server, executable avaliable locally
        String path = Dwarf.fileFinder("/home/user/projects/application/dist/Debug/GNU-MacOSX", "/net/server/home/user/projects/application/main.cc");
        assertEquals("/home/user/projects/application/main.cc", path);
    }

    public void testNetBuildMapperWin() {
        // build by using paths /net/server, executable avaliable locally
        String path = Dwarf.fileFinder("C:/home/user/projects/application/dist/Debug/GNU-MacOSX", "K:/net/server/home/user/projects/application/main.cc");
        assertEquals("C:/home/user/projects/application/main.cc", path);
    }

    public void testTwoLocalLinkMapper() {
        // build by using paths /ade/view/project and /scratch/user/view/project
        String path = Dwarf.fileFinder("/scratch/user/view/project", "/ade/view/project/main.cc");
        assertEquals("/scratch/user/view/project/main.cc", path);
    }

    public void testTwoLocalLinkMapperWin() {
        // build by using paths /ade/view/project and /scratch/user/view/project
        String path = Dwarf.fileFinder("K:/scratch/user/view/project", "K:/ade/view/project/main.cc");
        assertEquals("K:/scratch/user/view/project/main.cc", path);
    }

    public void testFarmMapper() {
        // build by using paths /ade/view/project and /scratch/user/view/project
        String path = Dwarf.fileFinder("/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server", "/net/host1/vol/ifarm_ports/ifarm_views/aime_rdbms_273649/rdbms/src/server/ram/data/kdr.c");
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server/ram/data/kdr.c", path);
    }

//    public void testFarmImcludeMapper() {
//        // build by using paths /ade/view/project and /scratch/user/view/project
//        String path = Dwarf.fileFinder("/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server", "/ade/b/1226108341/oracle/rdbms/src/hdir");
//        assertEquals(path, "/scratch/user1/view_storage/user1_my_rdbms/oracle/rdbms/src/hdir");
//    }
//
//    public void testFarmImcludeOutMapper() {
//        // build by using paths /ade/view/project and /scratch/user/view/project
//        String path = Dwarf.fileFinder("/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server", "/ade/b/1226108341/oracle/oracore/port/include");
//        assertEquals(path, "/scratch/user1/view_storage/user1_my_rdbms/oracle/oracore/port/include");
//    }
    
    public void testMapperDetector() {
        FS fs = new FS("/scratch/user1/view_storage/user1_my_rdbms");
        String root = "/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server";
        String unknown = "/ade/b/1226108341/oracle/oracore/port/include";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.discover(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms/oracle/oracore/port/include", path.getPath());
    }

    public void testMapperDetector2() {
        FS fs = new FS("/scratch/user1/view_storage/user1_my_rdbms");
        String root = "/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server";
        String unknown = "/ade/user1_my_rdbms/oracle/oracore/port/include";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.discover(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms/oracle/oracore/port/include", path.getPath());
    }

    public void testMapperDetector3() {
        FS fs = new FS("/scratch/user1/view_storage/user1_my_rdbms");
        String root = "/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server";
        String unknown = "/net/host1/vol/ifarm_ports/ifarm_views/aime_rdbms_273649/rdbms/src/server/ram/data";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.discover(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server/ram/data", path.getPath());
    }

    public void testMapperDetectorAll() {
        FS fs = new FS("/scratch/user1/view_storage/user1_my_rdbms");
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        int initialSize = new RelocatablePathMapperImpl(null).dump().size();
        String root;
        String unknown;
        ResolvedPath path;
        MapperEntry mapperEntry;
        
        root = "/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server";
        unknown = "/net/host1/vol/ifarm_ports/ifarm_views/aime_rdbms_273649/rdbms/src/server/ram/data";
        path = mapper.getPath(unknown);
        assertNull(path);
        assertTrue(mapper.discover(fs, root, unknown));
        
        root = "/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server";
        unknown = "/ade/user1_my_rdbms/oracle/oracore/port/include";
        path = mapper.getPath(unknown);
        assertNull(path);
        assertTrue(mapper.discover(fs, root, unknown));

        root = "/scratch/user1/view_storage/user1_my_rdbms/rdbms/src/server";
        unknown = "/ade/b/1226108341/oracle/oracore/port/include";
        path = mapper.getPath(unknown);
        assertNull(path);
        assertTrue(mapper.discover(fs, root, unknown));
        
        path = mapper.getPath("/net/host1/vol/ifarm_ports/ifarm_views/aime_rdbms_273649/rdbms/src/server/ram/data1");
        assertNotNull(path);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());

        path = mapper.getPath("/ade/user1_my_rdbms/oracle/oracore/port/include1");
        assertNotNull(path);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());

        path = mapper.getPath("/ade/b/1226108341/oracle/oracore/port/include1");
        assertNotNull(path);
        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());

        assertEquals(3 + initialSize, mapper.dump().size());
        //for(MapperEntry entry : mapper.dump()) {
        //    System.out.println(entry.from+" -> "+entry.to);
        //}
    }

    public void testMapperDetectorHomeLink() {
        FS2 fs = new FS2("/home/user1/tmp-link/pkg-config-0.25");
        String root = "/home/user1/tmp-link/pkg-config-0.25";
        String unknown = "/var/tmp/user1-cnd-test-downloads/pkg-config-0.25/glib-1.2.10/gcache.c";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.discover(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/home/user1/tmp-link", path.getRoot());
        assertEquals("/home/user1/tmp-link/pkg-config-0.25/glib-1.2.10/gcache.c", path.getPath());
    }

    public void testMapperDetectorCTX() {
        FS3 fs = new FS3("/scratch/user1/view_storage/user1_vk_ctx_3");
        String root = "/scratch/user1/view_storage/user1_vk_ctx_3/ctx_src_4/src";
        String unknown = "/ade/user1_vk_ctx_3/oracle/ctx/src/gx/include";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.discover(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/scratch/user1/view_storage/user1_vk_ctx_3", path.getRoot());
        assertEquals("/scratch/user1/view_storage/user1_vk_ctx_3/oracle/ctx/src/gx/include", path.getPath());
    }

    public void testMapperDetectorCTX2() {
        FS3 fs = new FS3("/scratch/user1/view_storage/user1_vk_ctx_3");
        String root = "/scratch/user1/view_storage/user1_vk_ctx_3/ctx_src_4/src/ext/zfm";
        String unknown = "/ade/user1_vk_ctx_3/oracle/ctx/src/gx/include";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.discover(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/scratch/user1/view_storage/user1_vk_ctx_3", path.getRoot());
        assertEquals("/scratch/user1/view_storage/user1_vk_ctx_3/oracle/ctx/src/gx/include", path.getPath());
    }

    public void testMapperDetectorCTX3() {
        FS5 fs = new FS5("/scratch/user1/view_storage/user1_my_ctx");
        String root = "/scratch/user1/view_storage/user1_my_ctx/ctx/src";
        String unknown = "/net/user420/export/ifarm_base/ifarm_views/aime_ctx_286748/ctx_src_2/src/dr/dren";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.discover(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/scratch/user1/view_storage/user1_my_ctx", path.getRoot());
        assertEquals("/scratch/user1/view_storage/user1_my_ctx/ctx_src_2/src/dr/dren", path.getPath());
    }

    public void testMapperDetectorCTX4() {
        FS5 fs = new FS5("/scratch/user1/view_storage/user1_my_ctx");
        String root = "/scratch/user1/view_storage/user1_my_ctx/ctx/src";
        String unknown = "/scratch/user1/view_storage/user1_my_ctx/ctx_src_4/src/ext/zfm/zfma.c";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertFalse(mapper.discover(fs, root, unknown));
    }
    
    public void testMapper0() {
        FS4 fs = new FS4("/scratch/alsimon/view_storage/alsimon_my_rdbms");
        String root = "/scratch/alsimon/view_storage/alsimon_my_rdbms/rdbms";
        String unknown = "odbc";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertFalse(mapper.discover(fs, root, unknown));
    }
    
//    public void testReadMapper() throws IOException {
//        File storage = File.createTempFile("mapper", ".txt");
//        storage.deleteOnExit();
//        System.setProperty("makeproject.pathMapper.file", storage.getPath()); // NOI18N
//        BufferedWriter wr = new BufferedWriter(new FileWriter(storage));
//        wr.append("/net/host1/vol/ifarm_ports/ifarm_views/aime_rdbms_273649=/scratch/user1/view_storage/user1_my_rdbms\n");
//        wr.append("/ade/user1_my_rdbms=/scratch/user1/view_storage/user1_my_rdbms\n");
//        wr.append("/ade/b/1226108341=/scratch/user1/view_storage/user1_my_rdbms\n");
//        wr.close();
//
//        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(new ProjectProxy() {
//
//            @Override
//            public boolean createSubProjects() {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public Project getProject() {
//                return null;
//            }
//
//            @Override
//            public String getMakefile() {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public String getSourceRoot() {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public String getExecutable() {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public String getWorkingFolder() {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public boolean mergeProjectProperties() {
//                throw new UnsupportedOperationException();
//            }
//        });
//        assertEquals(3, mapper.dump().size());
//        ResolvedPath path;
//        
//        path = mapper.getPath("/net/host1/vol/ifarm_ports/ifarm_views/aime_rdbms_273649/rdbms/src/server/ram/data1");
//        assertNotNull(path);
//        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());
//
//        path = mapper.getPath("/ade/user1_my_rdbms/oracle/oracore/port/include1");
//        assertNotNull(path);
//        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());
//
//        path = mapper.getPath("/ade/b/1226108341/oracle/oracore/port/include1");
//        assertNotNull(path);
//        assertEquals("/scratch/user1/view_storage/user1_my_rdbms", path.getRoot());
//    }
    
    private static final class FS implements RelocatablePathMapperImpl.FS {
        Set<String> set = new HashSet<String>();
        private FS(String prefix) {
            set.add(prefix);
            set.add(prefix+"/rdbms");
            set.add(prefix+"/rdbms/src");
            set.add(prefix+"/rdbms/src/server");
            set.add(prefix+"/rdbms/src/server/ram");
            set.add(prefix+"/rdbms/src/server/ram/data");
            set.add(prefix+"/rdbms/src/client");
            set.add(prefix+"/rdbms/src/hdir");
            set.add(prefix+"/rdbms/src/port");
            set.add(prefix+"/rdbms/include");
            set.add(prefix+"/rdbms/port");
            set.add(prefix+"/oracle");
            set.add(prefix+"/oracle/rdbms");
            set.add(prefix+"/oracle/oracore");
            set.add(prefix+"/oracle/oracore/port");
            set.add(prefix+"/oracle/oracore/port/include");
            set.add(prefix+"/oracore");
            set.add(prefix+"/oracore/port");
            set.add(prefix+"/oracore/public");
            while ((prefix = CndPathUtilities.getDirName(prefix)) != null) {
                set.add(prefix);
            }
        }
        
        @Override
        public boolean exists(String path) {
            return set.contains(path);
        }

        @Override
        public List<String> list(String path) {
            throw new UnsupportedOperationException(); 
        }
    }

    private static final class FS2 implements RelocatablePathMapperImpl.FS {
        Set<String> set = new HashSet<String>();
        private FS2(String prefix) {
            set.add(prefix);
            set.add(prefix+"/glib-1.2.10");
            set.add(prefix+"/glib-1.2.10/gcache.c");
            while ((prefix = CndPathUtilities.getDirName(prefix)) != null) {
                set.add(prefix);
            }
        }
        
        @Override
        public boolean exists(String path) {
            return set.contains(path);
        }

        @Override
        public List<String> list(String path) {
            throw new UnsupportedOperationException(); 
        }
    }

    private static final class FS3 implements RelocatablePathMapperImpl.FS {
        Set<String> set = new HashSet<String>();
        private FS3(String prefix) {
            set.add(prefix);
            set.add(prefix+"/ctx_src_4");
            set.add(prefix+"/ctx_src_4/src");
            set.add(prefix+"/oracle/ctx/src/gx/include");
            set.add(prefix+"/oracle/ctx/src/gx");
            set.add(prefix+"/oracle/ctx/src");
            set.add(prefix+"/oracle/ctx");
            set.add(prefix+"/oracle");
            while ((prefix = CndPathUtilities.getDirName(prefix)) != null) {
                set.add(prefix);
            }
        }
        
        @Override
        public boolean exists(String path) {
            return set.contains(path);
        }

        @Override
        public List<String> list(String path) {
            throw new UnsupportedOperationException(); 
        }
    }

    private static final class FS4 implements RelocatablePathMapperImpl.FS {
        Set<String> set = new HashSet<String>();
        private FS4(String prefix) {
            set.add(prefix);
            set.add(prefix+"/rdbms");
            set.add(prefix+"/odbc");
            while ((prefix = CndPathUtilities.getDirName(prefix)) != null) {
                set.add(prefix);
            }
        }
        
        @Override
        public boolean exists(String path) {
            return set.contains(path);
        }

        @Override
        public List<String> list(String path) {
            throw new UnsupportedOperationException(); 
        }
    }

    private static final class FS5 implements RelocatablePathMapperImpl.FS {
        Set<String> set = new HashSet<String>();
        private FS5(String prefix) {
            set.add(prefix);
            set.add(prefix+"/ctx");
            set.add(prefix+"/ctx/src");
            set.add(prefix+"/ctx/src/dr");
            set.add(prefix+"/ctx/src/ext");
            set.add(prefix+"/ctx/src/dr/dren");
            set.add(prefix+"/ctx_src_2");
            set.add(prefix+"/ctx_src_2/src");
            set.add(prefix+"/ctx_src_2/src/dr");
            set.add(prefix+"/ctx_src_2/src/dr/dren");
            set.add(prefix+"/ctx_src_4");
            set.add(prefix+"/ctx_src_4/src");
            set.add(prefix+"/ctx_src_4/src/ext");
            set.add(prefix+"/ctx_src_4/src/ext/zfm");
            set.add(prefix+"/ctx_src_4/src/ext/zfm/zfma.c");
            while ((prefix = CndPathUtilities.getDirName(prefix)) != null) {
                set.add(prefix);
            }
        }
        
        @Override
        public boolean exists(String path) {
            return set.contains(path);
        }

        @Override
        public List<String> list(String path) {
            throw new UnsupportedOperationException(); 
        }
    }
    
    public void testIncludeDetector() {
        FS6 fs = new FS6("/home/user1");
        String root = "/home/user1/my_project";
        String unknown = "/soft/1.1/ssl/include";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.discover(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/soft/1.1/ssl/include", path.getRoot());
        assertEquals("/soft/1.1/ssl/include", path.getPath());
    }

    public void testIncludeDetector2() {
        FS6 fs = new FS6("/home/user1");
        String root = "/home/user1/my_project";
        String unknown = "/soft/1.2/ssl/include";
        RelocatablePathMapperImpl mapper = new RelocatablePathMapperImpl(null);
        assertTrue(mapper.discover(fs, root, unknown));
        final ResolvedPath path = mapper.getPath(unknown);
        assertEquals("/home/user1", path.getRoot());
        assertEquals("/home/user1/include", path.getPath());
    }

    private static final class FS6 implements RelocatablePathMapperImpl.FS {
        Set<String> set = new HashSet<String>();
        private final String prefix;
        private FS6(String prefix) {
            set.add(prefix);
            set.add(prefix+"/my_project");
            set.add(prefix+"/my_project/src");
            set.add(prefix+"/my_project/include");
            set.add(prefix+"/include");
            set.add("/soft/1.1/ssl/include");
            set.add("/soft/1.2/ssl/include");
            this.prefix = prefix;
            while ((prefix = CndPathUtilities.getDirName(prefix)) != null) {
                set.add(prefix);
            }
            prefix = "/soft/1.1/ssl/include";
            while ((prefix = CndPathUtilities.getDirName(prefix)) != null) {
                set.add(prefix);
            }
            prefix = "/soft/1.2/ssl/include";
            while ((prefix = CndPathUtilities.getDirName(prefix)) != null) {
                set.add(prefix);
            }
        }
        
        @Override
        public boolean exists(String path) {
            return set.contains(path);
        }
        
        @Override
        public List<String> list(String path) {
            List<String> res = new ArrayList<String>();
            if (path.equals("/soft/1.1/ssl/include")) {
                res.add("/soft/1.1/ssl/include/gtk.h");
            } else if (path.equals("/soft/1.2/ssl/include")) {
                res.add("/soft/1.1/ssl/include/trash.h");
            } else if (path.equals(prefix+"/include")) {
                res.add(prefix+"/my_project/include/trash.h");
                res.add(prefix+"/my_project/include/config.h");
            }
            return res;
        }
    }
}
