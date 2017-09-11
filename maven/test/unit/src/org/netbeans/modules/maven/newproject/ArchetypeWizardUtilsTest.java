/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.newproject;

import java.io.IOException;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach
 */
public class ArchetypeWizardUtilsTest extends NbTestCase {
   
    public ArchetypeWizardUtilsTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(F.class);
    }
    
    
    
    public void testFindsAllDirectories() throws IOException {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileUtil.createData(fs.getRoot(), "MyPrj/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/a/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/a/b/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/a/b/c/pom.xml");
        FileObject root = fs.findResource("MyPrj");
        
        Set<FileObject> res = ArchetypeWizardUtils.openProjects(root, null);
        
        assertEquals("Four projects found: " + res, 4, res.size());
    }
    
    public void testOpeningOfProjectsSkipsTargetDirectory() throws IOException {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileUtil.createData(fs.getRoot(), "MyPrj/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/a/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/b/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/c/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/target/x/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/target/y/pom.xml");
        FileUtil.createData(fs.getRoot(), "MyPrj/target/z/pom.xml");
        FileObject root = fs.findResource("MyPrj");
        
       Set<FileObject> res = ArchetypeWizardUtils.openProjects(root, null);
        
        assertEquals("Four projects found: " + res, 4, res.size());
    }
    
    public static class F implements ProjectFactory {
        @Override
        public boolean isProject(FileObject projectDirectory) {
            return projectDirectory.getFileObject("pom.xml") != null;
        }

        @Override
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            throw new IOException();
        }

        @Override
        public void saveProject(Project project) throws IOException, ClassCastException {
            throw new IOException();
        }
    }
}
