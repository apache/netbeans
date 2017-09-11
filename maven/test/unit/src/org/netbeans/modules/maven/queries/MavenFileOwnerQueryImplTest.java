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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.queries;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;

public class MavenFileOwnerQueryImplTest extends NbTestCase {

    public MavenFileOwnerQueryImplTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testFindCoordinates() throws Exception {
        File repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        assertEquals("[test, prj, 1.0]", Arrays.toString(MavenFileOwnerQueryImpl.findCoordinates(new File(repo, "test/prj/1.0/prj-1.0.jar"))));
        assertEquals("[my.test, prj, 1.0-SNAPSHOT]", Arrays.toString(MavenFileOwnerQueryImpl.findCoordinates(new File(repo, "my/test/prj/1.0-SNAPSHOT/prj-1.0-SNAPSHOT.pom"))));
        assertEquals("null", Arrays.toString(MavenFileOwnerQueryImpl.findCoordinates(new File(repo, "test/prj/1.0"))));
        assertEquals("null", Arrays.toString(MavenFileOwnerQueryImpl.findCoordinates(getWorkDir())));
    }

    public void testMultipleVersions() throws Exception {
        File prj10 = new File(getWorkDir(), "prj10");
        TestFileUtils.writeFile(new File(prj10, "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version></project>");
        NbMavenProjectImpl p10 = (NbMavenProjectImpl) ProjectManager.getDefault().findProject(FileUtil.toFileObject(prj10));
        File prj11 = new File(getWorkDir(), "prj11");
        TestFileUtils.writeFile(new File(prj11, "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.1</version></project>");
        NbMavenProjectImpl p11 = (NbMavenProjectImpl) ProjectManager.getDefault().findProject(FileUtil.toFileObject(prj11));
        MavenFileOwnerQueryImpl foq = MavenFileOwnerQueryImpl.getInstance();
        File repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        File art10 = new File(repo, "test/prj/1.0/prj-1.0.jar");
        File art11 = new File(repo, "test/prj/1.1/prj-1.1.jar");
        assertEquals(null, foq.getOwner(Utilities.toURI(art10)));
        assertEquals(null, foq.getOwner(Utilities.toURI(art11)));
        foq.registerProject(p10, true);
        assertEquals(p10, foq.getOwner(Utilities.toURI(art10)));
        assertEquals(null, foq.getOwner(Utilities.toURI(art11)));
        foq.registerProject(p11, true);
        assertEquals(p10, foq.getOwner(Utilities.toURI(art10)));
        assertEquals(p11, foq.getOwner(Utilities.toURI(art11)));
    }
    
    public void testOldEntriesGetRemoved() throws Exception {
        URL url = new URL("file:///users/mkleint/aaa/bbb");
        MavenFileOwnerQueryImpl.getInstance().registerCoordinates("a", "b", "0", url, true);
        assertNotNull(MavenFileOwnerQueryImpl.prefs().get("a:b:0", null));
        MavenFileOwnerQueryImpl.getInstance().registerCoordinates("a", "b", "1", url, true);
        assertNotNull(MavenFileOwnerQueryImpl.prefs().get("a:b:1", null));
        assertNull(MavenFileOwnerQueryImpl.prefs().get("a:b:0", null));
        
    }

}
