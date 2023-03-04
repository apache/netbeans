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

package org.netbeans.nbbuild.extlibs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.netbeans.junit.NbTestCase;
import org.netbeans.nbbuild.extlibs.MavenCoordinate;

public class DownloadBinariesTest extends NbTestCase {

    public DownloadBinariesTest(String name) {
        super(name);
    }

    private File cache;
    private File server;
    private File workdir;
    private File list;

    private static void write(File f, String contents) throws IOException {
        try (OutputStream os = new FileOutputStream(f)) {
            os.write(contents.getBytes("UTF-8"));
        }
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        File d = getWorkDir();
        cache = new File(d, "cache");
        cache.mkdir();
        server = new File(d, "server");
        server.mkdir();
        workdir = new File(d, "workdir");
        workdir.mkdir();
        list = new File(workdir, "list");
        write(new File(server, "F572D396FAE9206628714FB2CE00F72E94F2258F-hello"), "hello\n");
        write(new File(server, "5C372AB96C721258C5C12BB8EAD291BBBA5DACE6-hello"), "hello!\n");
        write(new File(server, "E7D9B82B45D5833C9DADA13F2379E7B66C823434-goodbye"), "goodbye\n");
        write(new File(server, "27FA3C3F049846BA241021C69CB1E377CABE4087-goodbye"), "goodbye!\n");
    }

    private void downloadBinaries(boolean clean) {
        DownloadBinaries task = new DownloadBinaries();
        task.setProject(new Project());
        task.setCache(cache);
        task.setClean(clean);
        task.setServer(server.toURI().toString());
        FileSet manifest = new FileSet();
        manifest.setFile(list);
        task.addManifest(manifest);
        task.execute();
    }

    public void testUpdatingBinary() throws Exception {
        write(list, "F572D396FAE9206628714FB2CE00F72E94F2258F hello");
        downloadBinaries(false);
        assertEquals(6, new File(workdir, "hello").length());
        write(list, "5C372AB96C721258C5C12BB8EAD291BBBA5DACE6 hello");
        downloadBinaries(false);
        assertEquals(7, new File(workdir, "hello").length());
    }

    public void testIsNormalDownload() {
        boolean is = MavenCoordinate.isMavenFile("hello");
        assertFalse("This is hg.netbeans.org hashed file", is);
    }

    public void testIsMavenDownload() {
        final String id = "org.netbeans.html:xhr4j:1.3";
        boolean is = MavenCoordinate.isMavenFile(id);
        assertTrue("Contains co-ordinates", is);
        String targetName = MavenCoordinate.fromGradleFormat(id).toArtifactFilename();
        assertEquals("xhr4j-1.3.jar", targetName);
    }

    public void testCorruptDownload() throws Exception {
        write(list, "F572D396FAE9206628714FB2CE00F72E94F2258F hello");
        File serverFile = new File(server, "F572D396FAE9206628714FB2CE00F72E94F2258F-hello");
        write(serverFile, "bogus content\n");
        try {
            downloadBinaries(false);
            fail();
        } catch (BuildException x) {/* expected */}
        File cacheFile = new File(cache, "F572D396FAE9206628714FB2CE00F72E94F2258F-hello");
        assertFalse(cacheFile.exists());
        File workdirFile = new File(workdir, "hello");
        assertFalse(workdirFile.exists());
        write(serverFile, "hello\n");
        downloadBinaries(false);
        assertEquals(6, cacheFile.length());
        assertEquals(6, workdirFile.length());
    }

    // XXX test cleaning binary
    // XXX test that overwritten or cleaned binary is backed up somewhere if not found in cache

}
