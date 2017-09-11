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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import org.netbeans.api.project.ProjectUtils;
import org.openide.modules.SpecificationVersion;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.util.test.MockLookup;

public class J2SESharabilityQueryTest extends NbTestCase {

    public J2SESharabilityQueryTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject tests;
    private FileObject dist;
    private FileObject build;
    private ProjectManager pm;
    private J2SEProject pp;
    private AntProjectHelper helper;

    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances(
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        );
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null,null, false); //NOI18N
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        sources = projdir.getFileObject("src");
        tests = projdir.getFileObject("test");
        dist = FileUtil.createFolder(projdir,"dist");
        build = FileUtil.createFolder(projdir,"build");
        pm = ProjectManager.getDefault();
        Project p = pm.findProject(projdir);
        assertTrue("Invalid project type",p instanceof J2SEProject);
        pp = (J2SEProject) p;
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        sources = null;
        tests = null;
        pm = null;
        pp = null;
        helper = null;
        super.tearDown();
    }

    public void testSharability () throws Exception {
        File f = FileUtil.toFile (this.sources);
        int res = SharabilityQuery.getSharability(f);
        assertEquals("Sources must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (this.tests);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Tests must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (this.build);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Build can't be sharable", SharabilityQuery.NOT_SHARABLE, res);
        f = FileUtil.toFile (this.dist);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Dist can't be sharable", SharabilityQuery.NOT_SHARABLE, res);
        FileObject newSourceRoot = addSourceRoot(helper, projdir, "src2.dir",new File(FileUtil.toFile(scratch),"sources2"));
        ProjectUtils.getSources(pp).getSourceGroups(Sources.TYPE_GENERIC);
        f = FileUtil.toFile (newSourceRoot);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Sources2 must be sharable", SharabilityQuery.SHARABLE, res);
        FileObject newSourceRoot2 = changeSourceRoot(helper, projdir, "src2.dir", new File(FileUtil.toFile(scratch),"sources3"));
        f = FileUtil.toFile (newSourceRoot2);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Sources3 must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (newSourceRoot);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Sources2 must be unknown", SharabilityQuery.UNKNOWN, res);
    }

    public static FileObject addSourceRoot (AntProjectHelper helper, FileObject projdir,
                                            String propName, File folder) throws Exception {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nl = data.getElementsByTagNameNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
        assert nl.getLength() == 1;
        Element roots = (Element) nl.item(0);
        Document doc = roots.getOwnerDocument();
        Element root = doc.createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"root");
        root.setAttribute("id", propName);
        roots.appendChild (root);
        helper.putPrimaryConfigurationData (data,true);
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.put (propName,folder.getAbsolutePath());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        return FileUtil.toFileObject(folder);
    }

    public static FileObject changeSourceRoot (AntProjectHelper helper, FileObject projdir,
                                               String propName, File folder) throws Exception {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assert props.containsKey(propName);
        props.put (propName,folder.getAbsolutePath());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        return FileUtil.toFileObject(folder);
    }

}
