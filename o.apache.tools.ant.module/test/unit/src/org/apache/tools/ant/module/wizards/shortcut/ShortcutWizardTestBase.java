/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
