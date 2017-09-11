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


package org.netbeans.modules.maven.queries;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2.Result;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author mkleint
 */
public class RepositoryForBinaryQueryImplTest extends NbTestCase{
    
    public RepositoryForBinaryQueryImplTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        //synchronous reload of maven project asserts sanoty in some tests..
        System.setProperty("test.reload.sync", "true");
        
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown(); 
        File prj10 = new File(getWorkDir(), "prj10");
        org.codehaus.plexus.util.FileUtils.deleteDirectory(prj10);
    }
    
    
    /**
     * Test of findSourceRoots2 method, of class RepositoryForBinaryQueryImpl.
     */
    @Test
    public void testResultChanging() throws IOException {
        RepositoryForBinaryQueryImpl query = new RepositoryForBinaryQueryImpl();
        File repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        File art10 = new File(repo, "nbtest/testprj/1.0/testprj-1.0.jar");
        org.codehaus.plexus.util.FileUtils.deleteDirectory(art10.getParentFile());
        URL url = FileUtil.getArchiveRoot(art10.toURI().toURL());
        TestFileUtils.writeZipFile(art10, "META-INF/MANIFEST.MF:Version:1.0");
        Result result = query.findSourceRoots2(url);
        assertNotNull(result);
        assertEquals(0, result.getRoots().length);
        

        // now create source jar
        File sourceJar = new File(this.getDataDir(), "source.jar");
        File repoSourceJar = new File(art10.getParentFile(), "testprj-1.0-sources.jar");
        org.codehaus.plexus.util.FileUtils.copyFile(sourceJar, repoSourceJar);
        assertEquals(1, result.getRoots().length);
        assertEquals(FileUtil.getArchiveRoot(FileUtil.toFileObject(repoSourceJar)), result.getRoots()[0]);
        
        
        
        //now create project
        File prj10 = new File(getWorkDir(), "prj10");
        TestFileUtils.writeFile(new File(prj10, "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>nbtest</groupId><artifactId>testprj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version></project>");
        //create main source root.
        File prjroot = new File(new File(new File(prj10, "src"), "main"), "java");
        prjroot.mkdirs();
        
        NbMavenProjectImpl p10 = (NbMavenProjectImpl) ProjectManager.getDefault().findProject(FileUtil.toFileObject(prj10));
        assertNotNull(p10);
        MavenFileOwnerQueryImpl.getInstance().registerProject(p10, true);
        try {
            //preferences changes get fired in different thread, need to wait for it.. 
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        assertEquals(1, result.getRoots().length);
        assertEquals(FileUtil.toFileObject(prjroot), result.getRoots()[0]);
        
        //overwrite the pom file to point to different 
        TestFileUtils.writeFile(new File(prj10, "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>nbtest</groupId><artifactId>testprj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version>"
                + "<build><sourceDirectory>src/main2/java</sourceDirectory></build>"
                + "</project>");
        //manually reload the project, only opened projects listen on pom.xml changes.
        p10.fireProjectReload();
        
        FileObject[] res = result.getRoots();
        assertEquals(Arrays.toString(res), 0, res.length);
        prjroot = new File(new File(new File(prj10, "src"), "main2"), "java");
        prjroot.mkdirs();
        
        assertEquals(1, result.getRoots().length);
        assertEquals(FileUtil.toFileObject(prjroot), result.getRoots()[0]);
        
        
        
    }

}
