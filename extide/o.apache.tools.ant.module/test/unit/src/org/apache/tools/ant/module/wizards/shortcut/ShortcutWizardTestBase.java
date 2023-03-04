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

package org.apache.tools.ant.module.wizards.shortcut;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Base class for tests in the package.
 * @author Jesse Glick
 */
public abstract class ShortcutWizardTestBase extends NbTestCase {
    
    protected ShortcutWizardTestBase(String name) {
        super(name);
    }

    private File scratchF;
    protected ShortcutWizard wiz;
    private AntProjectCookie project;
    private Element target1;
    protected ShortcutIterator iter;
    
    private void mkdir(String path) {
        new File(scratchF, path.replace('/', File.separatorChar)).mkdirs();
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        scratchF = getWorkDir();
        mkdir("system/Menu/&File");
        mkdir("system/Menu/&Edit");
        mkdir("system/Menu/&Build");
        mkdir("system/Menu/&Build/Other");
        mkdir("system/Menu/Help");
        mkdir("system/Toolbars/Build");
        mkdir("system/Toolbars/Help");
        mkdir("system/Shortcuts");
        System.setProperty("SYSTEMDIR", new File(scratchF, "system").getAbsolutePath());
        FileObject scratch = FileUtil.toFileObject(scratchF);
        assertNotNull("have a scratch dir", scratch);
        MockServices.setServices(Repo.class);
        FileObject menuFolder = FileUtil.getConfigFile("Menu");
        assertNotNull("have Menu", menuFolder);
        menuFolder.getFileObject("&File").setAttribute("position", 100);
        menuFolder.getFileObject("&Edit").setAttribute("position", 200);
        menuFolder.getFileObject("&Build").setAttribute("position", 300);
        menuFolder.getFileObject("Help").setAttribute("position", 400);
        FileObject toolbarsFolder = FileUtil.getConfigFile("Toolbars");
        assertNotNull("have Toolbars", toolbarsFolder);
        toolbarsFolder.getFileObject("Build").setAttribute("position", 100);
        toolbarsFolder.getFileObject("Help").setAttribute("position", 200);
        assertNotNull("have Shortcuts", FileUtil.getConfigFile("Shortcuts"));
        FileObject buildXml = scratch.createData("build.xml");
        FileLock lock = buildXml.lock();
        OutputStream os = buildXml.getOutputStream(lock);
        PrintWriter pw = new PrintWriter(os);
        pw.println("<project name='my proj' default='whatever' basedir='.'>");
        pw.println(" <target name='targ1'>");
        pw.println("  <echo>hello #1</echo>");
        pw.println(" </target>");
        pw.println(" <target name='targ2'>");
        pw.println("  <echo>hello #2</echo>");
        pw.println(" </target>");
        pw.println("</project>");
        pw.flush();
        os.close();
        lock.releaseLock();
        project = new AntProjectSupport(buildXml);
        Document doc = project.getDocument();
        assertNotNull("parsed " + buildXml, doc);
        Element docEl = doc.getDocumentElement();
        NodeList nl = docEl.getElementsByTagName("target");
        target1 = (Element)nl.item(0);
        assertEquals("target #1", "targ1", target1.getAttribute("name"));
        iter = new ShortcutIterator();
        wiz = new ShortcutWizard(project, target1, iter);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public static final class Repo extends Repository {
        
        public Repo() throws Exception {
            super(mksystem());
        }
        
        private static FileSystem mksystem() throws Exception {
            LocalFileSystem lfs = new LocalFileSystem();
            lfs.setRootDirectory(new File(System.getProperty("SYSTEMDIR")));
            return lfs;
        }
        
    }
    
}
