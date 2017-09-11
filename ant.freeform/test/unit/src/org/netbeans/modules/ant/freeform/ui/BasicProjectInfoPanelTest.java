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

package org.netbeans.modules.ant.freeform.ui;

import java.io.IOException;
import java.text.MessageFormat;
import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class BasicProjectInfoPanelTest extends TestBase {

    private File outterProjectDir;
    private FileObject outterProjectDirFO;
    private Project outterProject;
    private File innerProjectDir;
    private File innerAntScript;
    private File simple3Dir;
    private File simple3AntScript;
    private File outterProjectNBProjectDir;
        
    public BasicProjectInfoPanelTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        File outterProjectDirOriginal = FileUtil.normalizeFile(new File(egdir, "freeforminside"));
        File simple3DirOriginal = FileUtil.normalizeFile(new File(egdir, "simple3"));
        
        assertTrue("freeforminside directory (" + outterProjectDirOriginal + ") exists", outterProjectDirOriginal.exists());
        assertTrue("simple3 directory (" + simple3DirOriginal + ") exists", simple3DirOriginal.exists());
        
        outterProjectDir = copyFolder(outterProjectDirOriginal);
        simple3Dir       = copyFolder(simple3DirOriginal);
        
        assertTrue("freeforminside directory copy exists", outterProjectDir.exists());
        assertTrue("simple3 directory copy exists", simple3Dir.exists());
        
        outterProjectDirFO = FileUtil.toFileObject(outterProjectDir);
        assertNotNull("have FileObject for " + outterProjectDir, outterProjectDirFO);
        outterProject = ProjectManager.getDefault().findProject(outterProjectDirFO);
        assertNotNull("have a project", outterProject);
        outterProjectNBProjectDir = FileUtil.normalizeFile(new File(outterProjectDir, "nbproject"));
        assertNotNull("found nbproject directory", outterProjectNBProjectDir);
        innerProjectDir = FileUtil.normalizeFile(new File(outterProjectDir, "FreeForm"));
        assertTrue("inner directory (" + innerProjectDir + ") exists", innerProjectDir.exists());
        innerAntScript = FileUtil.normalizeFile(new File(innerProjectDir, "build.xml"));
        assertTrue("inner ant script (" + innerAntScript + ") exists", innerAntScript.exists());
        simple3AntScript = FileUtil.normalizeFile(new File(simple3Dir, "build.xml"));
        assertTrue("simple3 ant script (" + simple3AntScript + ") exists", simple3AntScript.exists());
    }

    private boolean deepDelete(File f) throws IOException {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            
            for (int cntr = 0; cntr < children.length; cntr++) {
                if (!deepDelete(children[cntr]))
                    return false;
            }
        }
        
        return f.delete();
    }
    
    /**The BasicProjectInfoPanel should allow creation of a freeform project in the directory hierarchy under
     * another project, but should refuse to create a project in place of another (loaded, but deleted)
     * project. See issues #58467 and #55533.
     */
    public void testGetError1() throws Exception {
        BasicProjectInfoPanel panel;
        String                error;
        
        panel = new BasicProjectInfoPanel(innerProjectDir.getAbsolutePath(), innerAntScript.getAbsolutePath(), "NAME", innerProjectDir.getAbsolutePath(), null);
        
        assertNull("allow creation (#58467)", panel.getError());
        
        //delete the outter/nbproject directory and try to create a new project here:
        assertTrue("nbproject deleted succesfully", deepDelete(outterProjectNBProjectDir));
        
        panel = new BasicProjectInfoPanel(outterProjectDir.getAbsolutePath(), innerAntScript.getAbsolutePath(), "NAME", outterProjectDir.getAbsolutePath(), null);
        
        error = MessageFormat.format(NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_Error_9"),
                new Object[] {"Simple Freeform Project"});
                
        assertEquals("do not allow creation (#55533)", error, panel.getError()[0]);
    }
    
    public void testGetError2() throws Exception {
        BasicProjectInfoPanel panel;
        String                error;
        
        panel = new BasicProjectInfoPanel(simple3Dir.getAbsolutePath(), simple3AntScript.getAbsolutePath(), "NAME", innerProjectDir.getAbsolutePath(), null);
        
        assertNull("allow creation (#58467)", panel.getError());
        
        //delete the outter/nbproject directory and try to create a new project here:
        assertTrue("nbproject deleted succesfully", deepDelete(outterProjectNBProjectDir));
        
        panel = new BasicProjectInfoPanel(simple3Dir.getAbsolutePath(), simple3AntScript.getAbsolutePath(), "NAME", outterProjectDir.getAbsolutePath(), null);
        
        error = MessageFormat.format(NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_Error_9"),
                new Object[] {"Simple Freeform Project"});
                
        assertEquals("do not allow creation (#55533)", error, panel.getError()[0]);
    }
    
    public void testGetError3() throws Exception {
        BasicProjectInfoPanel panel;
        String                error;
        
        panel = new BasicProjectInfoPanel(innerProjectDir.getAbsolutePath(), innerAntScript.getAbsolutePath(), "NAME", simple3Dir.getAbsolutePath(), null);
        
        assertNull("allow creation (#58467)", panel.getError());
        
        //delete the outter/nbproject directory and try to create a new project here:
        assertTrue("nbproject deleted succesfully", deepDelete(outterProjectNBProjectDir));
        
        panel = new BasicProjectInfoPanel(outterProjectDir.getAbsolutePath(), innerAntScript.getAbsolutePath(), "NAME", simple3Dir.getAbsolutePath(), null);
        
        error = MessageFormat.format(NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_Error_10"),
                new Object[] {"Simple Freeform Project"});
                
        assertEquals("do not allow creation (#55533)", error, panel.getError()[0]);
    }

    protected boolean runInEQ() {
        return true;
    }
    
}
