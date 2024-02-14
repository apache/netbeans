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

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.Assert;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author  pj97932, Tomas Mysik
 */
public final class TestUtil extends ProxyLookup {

    static {
        TestUtil.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", TestUtil.class.getName());
        Assert.assertEquals(TestUtil.class, Lookup.getDefault().getClass());
        Lookup p = Lookups.forPath("Services/AntBasedProjectTypes/");
        p.lookupAll(AntBasedProjectType.class);
        projects = p;
        setLookup(new Object[0]);
    }

    private static TestUtil DEFAULT;
    private static final int BUFFER = 2048;
    private static final Lookup projects;


    public TestUtil() {
        Assert.assertNull(DEFAULT);
        DEFAULT = this;
        ClassLoader l = TestUtil.class.getClassLoader();
        setLookups(new Lookup[] {
            Lookups.metaInfServices(l),
            Lookups.singleton(l)
        });
    }

    /**
     * Set the global default lookup.
     * Caution: if you don't include Lookups.metaInfServices, you may have trouble,
     * e.g. {@link #makeScratchDir} will not work.
     */
    public static void setLookup(Lookup l) {
        DEFAULT.setLookups(new Lookup[] {l});
    }

    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    public static void setLookup(Object... instances) {
        ClassLoader l = TestUtil.class.getClassLoader();
        DEFAULT.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(l),
            Lookups.singleton(l),
            projects
        });
    }

    static void setup(NbTestCase test) throws Exception {
        test.clearWorkDir();
        Logger.getLogger("org.netbeans.modules.web.jspparser_ext").setLevel(Level.FINE);
        // unzip test project
        TestUtil.getProject(test, "project3");
        TestUtil.initParserJARs();
    }

    static FileObject getFileInWorkDir(String path, NbTestCase test) throws Exception {
        File f = test.getDataDir();
        FileObject workDirFO = FileUtil.toFileObject(f);
        return FileUtil.createData(workDirFO, path);
    }

    static WebModule getWebModule(FileObject fo) {
        WebModule wm =  WebModule.getWebModule(fo);
        if (wm == null) {
            return null;
        }
        FileObject wmRoot = wm.getDocumentBase();
        if (fo == wmRoot || FileUtil.isParentOf(wmRoot, fo)) {
            return WebModule.getWebModule(fo);
        }
        return null;
    }

    static Project getProject(NbTestCase test, String projectFolderName) throws Exception {
        File f = getProjectAsFile(test, projectFolderName);
        FileObject projectPath = FileUtil.toFileObject(f);
        Project project = ProjectManager.getDefault().findProject(projectPath);
        NbTestCase.assertNotNull("Project should exist", project);
        return project;
    }

    static FileObject getProjectFile(NbTestCase test, String projectFolderName, String filePath) throws Exception {
        Project project = getProject(test, projectFolderName);
        FileObject fo = project.getProjectDirectory().getFileObject(filePath);
        NbTestCase.assertNotNull("Project file should exist: " + filePath, fo);

        return fo;
    }

    static WebModule createWebModule(FileObject documentRoot) {
        WebModuleImplementation webModuleImpl = new WebModuleImpl(documentRoot);
        return WebModuleFactory.createWebModule(webModuleImpl);
    }

    static void copyFolder(FileObject source, FileObject dest) throws IOException {
        for (FileObject child : source.getChildren()) {
            if (child.isFolder()) {
                FileObject created = FileUtil.createFolder(dest, child.getNameExt());
                copyFolder(child, created);
            } else {
                FileUtil.copyFile(child, dest, child.getName(), child.getExt());
            }
        }
    }

    static File getProjectAsFile(NbTestCase test, String projectFolderName) throws Exception {
        File f = new File(test.getDataDir(), projectFolderName);
        if (!f.exists()) {
            // maybe it's zipped
            File archive = new File(test.getDataDir(), projectFolderName + ".zip");
            unZip(archive, test.getDataDir());
        }
        NbTestCase.assertTrue("project directory has to exists: " + f, f.exists());
        return f;
    }

    private static void unZip(File archive, File destination) throws Exception {
        if (!archive.exists()) {
            throw new FileNotFoundException(archive + " does not exist.");
        }
        ZipFile zipFile = new ZipFile(archive);
        Enumeration<? extends ZipEntry> all = zipFile.entries();
        while (all.hasMoreElements()) {
            extractFile(zipFile, all.nextElement(), destination);
        }
    }

    private static void extractFile(ZipFile zipFile, ZipEntry e, File destination) throws IOException {
        String zipName = e.getName();
        if (zipName.startsWith("/")) {
            zipName = zipName.substring(1);
        }
        if (zipName.endsWith("/")) {
            return;
        }
        int ix = zipName.lastIndexOf('/');
        if (ix > 0) {
            String dirName = zipName.substring(0, ix);
            File d = new File(destination, dirName);
            if (!(d.exists() && d.isDirectory())) {
                if (!d.mkdirs()) {
                    NbTestCase.fail("Warning: unable to mkdir " + dirName);
                }
            }
        }
        FileOutputStream os = new FileOutputStream(destination.getAbsolutePath() + "/" + zipName);
        InputStream is = zipFile.getInputStream(e);
        int n = 0;
        byte[] buff = new byte[8192];
        while ((n = is.read(buff)) > 0) {
            os.write(buff, 0, n);
        }
        is.close();
        os.close();
    }
    
    public static void initParserJARs() throws MalformedURLException {
        List<URL> list = getJARs("jsp.parser.jars");
        JspParserImpl.setParserJARs(list.toArray(new URL[0]));
    }

    public static List<URL> getJARs(String propertyName) throws MalformedURLException {
        String path = System.getProperty(propertyName);
        String[] paths = PropertyUtils.tokenizePath(path);
        List<URL> list = new ArrayList<>();
        for (int i = 0; i< paths.length; i++) {
            String token = paths[i];
            File f = new File(token);
            if (!f.exists()) {
                throw new RuntimeException("cannot find file "+token);
            }
            list.add(f.toURI().toURL());
        }
        return list;
    }

}
