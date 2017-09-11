/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.apisupport.project;

import java.io.File;
import static junit.framework.Assert.assertNotNull;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class UseHtml4JavaTest extends TestBase {
    static {
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
    }

    private FileObject dir;
    public UseHtml4JavaTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        System.setProperty("sync.project.execution", "true");
//        MockLookup.setLayersAndInstances(cgpi);
        InstalledFileLocatorImpl.registerDestDir(destDirF);
        ((DialogDisplayerImpl) Lookup.getDefault().lookup(DialogDisplayer.class)).reset();
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
        File prjDir = new File(new File(getDataDir(), "example-external-projects"), "html4j");
        FileObject prj = FileUtil.toFileObject(prjDir);
        FileObject wrk = FileUtil.toFileObject(getWorkDir());
        dir = prj.copy(wrk, "html4j", null);
    }
    
    
    
    public void testCompileTheProject() throws Exception {
        Project prj = ProjectManager.getDefault().findProject(dir);
        assertNotNull("Project found for " + dir, prj);
        assertTrue("It is our project", prj instanceof NbModuleProject);
        ((NbModuleProject)prj).open();
        FileObject buildXML = dir.getFileObject("build.xml");
        assertNotNull("Ant script found", buildXML);
        final ExecutorTask task = ActionUtils.runTarget(buildXML, new String[]{"compile"}, null);
        task.waitFinished();
        assertEquals("Executed successfully", 0, task.result());
        assertNotNull("project was build", dir.getFileObject("build"));
    }
}