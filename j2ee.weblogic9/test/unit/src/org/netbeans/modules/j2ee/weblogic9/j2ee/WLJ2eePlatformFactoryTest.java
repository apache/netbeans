/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.j2ee;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class WLJ2eePlatformFactoryTest extends NbTestCase {

    public WLJ2eePlatformFactoryTest(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        clearWorkDir();
        super.tearDown();
    }
    
    public void testGetJarClassPath() throws Exception {
        File baseFolder = getWorkDir();
        File libFolder = new File(baseFolder, "some/jars");
        libFolder.mkdirs();
      
        File fileA = new File(libFolder, "a.jar");
        createJar(fileA);
        File fileB = new File(libFolder, "b.jar");
        createJar(fileB);         
        File fileC = new File(baseFolder, "c.jar");
        createJar(fileC); 
        
        File file = new File(baseFolder, "test.jar");
        createJar(file, "Class-Path: some/jars/a.jar some/jars/b.jar c.jar d.jar");
        
        List<URL> urls = WLJ2eePlatformFactory.getJarClassPath(file, null);
        checkJars(urls, fileA, fileB, fileC);
        
        File fileD = new File(baseFolder, "d.jar");
        createJar(fileD);         
        urls = WLJ2eePlatformFactory.getJarClassPath(file, null);
        checkJars(urls, fileA, fileB, fileC, fileD);        
    }
    
    public void testGetJarClassPathWithAbsolutePath() throws Exception {
        File baseFolder = getWorkDir();
        File libFolder = new File(baseFolder, "some/jars");
        libFolder.mkdirs();
      
        File fileA = new File(libFolder, "a.jar");
        createJar(fileA);
        File fileB = new File(libFolder, "b.jar");
        createJar(fileB);
        
        String absolutePathB = FileUtil.normalizeFile(fileB).getAbsolutePath();
        
        File file = new File(baseFolder, "test.jar");
        createJar(file, "Class-Path: some/jars/a.jar " + absolutePathB);
        
        List<URL> urls = WLJ2eePlatformFactory.getJarClassPath(file, null);
        checkJars(urls, fileA, fileB);        
    }
    
    public void testGetJarClassPathViaUrl() throws Exception {
        File baseFolder = getWorkDir();
        File libFolder = new File(baseFolder, "some/jars");
        libFolder.mkdirs();
      
        File fileA = new File(libFolder, "a.jar");
        createJar(fileA);
        File fileB = new File(libFolder, "b.jar");
        createJar(fileB);
        
        File file = new File(baseFolder, "test.jar");
        createJar(file, "Class-Path: some/jars/a.jar some/jars/b.jar");
        
        List<URL> urls = WLJ2eePlatformFactory.getJarClassPath(
                FileUtil.getArchiveRoot(file.toURI().toURL()), null);
        checkJars(urls, fileA, fileB);        
    }    

    private void checkJars(List<URL> urls, File... expected) throws Exception {
        Set<File> jars = new HashSet<File>();
        for (URL url : urls) {
            URL fileUrl = FileUtil.getArchiveFile(url);
            jars.add(new File(fileUrl.toURI()));
        }
        
        for (File file : expected) {
            assertTrue(jars.remove(file));
        }
        assertTrue(jars.isEmpty());        
    }
    
    private void createJar(File file, String... manifestLines) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Manifest-Version: 1.0\n");
        for (String line : manifestLines) {
            stringBuilder.append(line).append("\n");
        }
        
        InputStream is = new ByteArrayInputStream(stringBuilder.toString().getBytes("UTF-8"));
        try {
            new JarOutputStream(new FileOutputStream(file), new Manifest(is)).close();
        } finally {
            is.close();
        }
    }    
}