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

package org.netbeans.modules.j2ee.weblogic9.j2ee;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
        
        InputStream is = new ByteArrayInputStream(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        try {
            new JarOutputStream(new FileOutputStream(file), new Manifest(is)).close();
        } finally {
            is.close();
        }
    }    
}