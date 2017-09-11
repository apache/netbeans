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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java.freeform;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 * @author Milan Kubec
 */
public class FileEncodingQueryTest extends NbTestCase {
    
    /** Creates a new instance of FileEncodingQeuryTest */
    public FileEncodingQueryTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    @Override
    protected int timeOut() {
        return 300000;
    }
    
    public void testFileEncodingQuery() throws Exception {
        
        String projectFolder = "proj1";
        String projectName = "proj-1";
      
        File prjBase = new File(getWorkDir(), projectFolder);
        prjBase.mkdir();
        File antScript = new File(prjBase, "build.xml");
        antScript.createNewFile();
        
        File srcDir = new File(prjBase, "src");
        srcDir.mkdir();
        File srcFile = createFileInPackage(srcDir, "testpackage", "ClassA.java");
        
        File testDir = new File(prjBase, "test");
        testDir.mkdir();
        File testFile = createFileInPackage(testDir, "otherpackage", "ClassB.java");
        
        File libsrcDir = new File(prjBase, "libsrc");
        libsrcDir.mkdir();
        File libFile = createFileInPackage(libsrcDir, "yetanotherpackage", "ClassC.java");
        
        List<JavaProjectGenerator.SourceFolder> sources = new ArrayList<JavaProjectGenerator.SourceFolder>();
        AntProjectHelper helper = FreeformProjectGenerator.createProject(prjBase, prjBase, projectName, null);
        
        JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "src";
        sf.type = "java";
        sf.style = "packages";
        sf.location = "src";
        sf.encoding = "ISO-8859-1";
        sources.add(sf);
        
        sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "test";
        sf.type = "java";
        sf.style = "packages";
        sf.location = "test";
        sf.encoding = "ISO-8859-2";
        sources.add(sf);
        
        sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "proj-1";
        sf.location = ".";
        sf.encoding = "ISO-8859-3";
        sources.add(sf);
        
        JavaProjectGenerator.putSourceFolders(helper, sources, null);
        JavaProjectGenerator.putSourceViews(helper, sources, null);
        
        FileObject prjDir = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(prjDir);
        ProjectManager.getDefault().saveProject(p);
        
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", prjDir, p.getProjectDirectory());
        
        Charset cset = FileEncodingQuery.getEncoding(FileUtil.toFileObject(srcFile));
        assertEquals("ISO-8859-1", cset.name());
        
        cset = FileEncodingQuery.getEncoding(FileUtil.toFileObject(testFile));
        assertEquals("ISO-8859-2", cset.name());
        
        // file not under any src root
        File lonelyFile = new File(prjBase, "testfile.txt");
        lonelyFile.createNewFile();
        cset = FileEncodingQuery.getEncoding(FileUtil.toFileObject(lonelyFile));
        assertEquals("ISO-8859-3", cset.name());
        
        cset = FileEncodingQuery.getEncoding(FileUtil.toFileObject(libFile));
        assertEquals("ISO-8859-3", cset.name());
        
    }
    
    private File createFileInPackage(File root, String pkgName, String fileName) throws IOException {
        File pkg = new File(root, pkgName);
        pkg.mkdir();
        File file = new File(pkg, fileName);
        file.createNewFile();
        return file;
    }
    
}
