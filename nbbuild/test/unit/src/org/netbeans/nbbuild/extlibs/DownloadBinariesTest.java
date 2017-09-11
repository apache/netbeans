/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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

public class DownloadBinariesTest extends NbTestCase {

    public DownloadBinariesTest(String name) {
        super(name);
    }

    private File cache;
    private File server;
    private File workdir;
    private File list;

    private static void write(File f, String contents) throws IOException {
        OutputStream os = new FileOutputStream(f);
        os.write(contents.getBytes("UTF-8"));
        os.close();
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
        boolean is = DownloadBinaries.isMavenFile("5C372AB96C721258C5C12BB8EAD291BBBA5DACE6", "hello");
        assertFalse("This is hg.netbeans.org hashed file", is);
    }

    public void testIsMavenDownload() {
        final String[] hashAndId = new String[] { "CEC2829EC391CB404AD32EB2D08F879C418B745B", "org.netbeans.html:xhr4j:1.3" };
        boolean is = DownloadBinaries.isMavenFile(hashAndId);
        assertTrue("Contains co-ordinates", is);
        String targetName = DownloadBinaries.mavenFileName(hashAndId);
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
