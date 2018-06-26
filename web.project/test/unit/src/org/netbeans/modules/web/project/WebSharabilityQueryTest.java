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

package org.netbeans.modules.web.project;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.project.ProjectUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class WebSharabilityQueryTest extends NbTestCase {

    public WebSharabilityQueryTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject tests;
    private FileObject docRoot;
    private FileObject dist;
    private FileObject build;
    private WebProject pp;
    private AntProjectHelper helper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockLookup.init();
        Collection<? extends AntBasedProjectType> all = Lookups.forPath("Services/AntBasedProjectTypes").lookupAll(AntBasedProjectType.class);
        Iterator<? extends AntBasedProjectType> it = all.iterator();
        Object t = it.next();
        MockLookup.setInstances(t,
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        );
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication1");
        projdir = FileUtil.toFileObject(f);

        scratch = makeScratchDir(this);
        sources = projdir.getFileObject("src/java");
        tests = projdir.getFileObject("test");
        docRoot = projdir.getFileObject("web");
        dist = FileUtil.createFolder(projdir,"dist");
        build = FileUtil.createFolder(projdir,"build");
        Project p = ProjectManager.getDefault().findProject(projdir);
        assertTrue("Invalid project type",p instanceof WebProject);
        pp = (WebProject) p;
        helper = pp.getAntProjectHelper();
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        sources = null;
        tests = null;
        docRoot = null;
        pp = null;
        helper = null;
        MockLookup.setLookup(Lookup.EMPTY);
        super.tearDown();
    }

    public void testSharability () throws Exception {
        File f = FileUtil.toFile (this.sources);
        int res = SharabilityQuery.getSharability(f);
        assertEquals("Sources must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (this.tests);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Tests must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (this.docRoot);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Web Pages must be sharable", SharabilityQuery.SHARABLE, res);
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
            FileUtil.createFolder(folder);
        }
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nl = data.getElementsByTagNameNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
        assert nl.getLength() == 1;
        Element roots = (Element) nl.item(0);
        Document doc = roots.getOwnerDocument();
        Element root = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");
        root.setAttributeNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"id",propName);
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
            FileUtil.createFolder(folder);
        }
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assert props.containsKey(propName);
        props.put (propName,folder.getAbsolutePath());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        return FileUtil.toFileObject(folder);
    }

    /**
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        boolean warned = false;
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            return fo;
        } else {
            if (!warned) {
                warned = true;
                System.err.println("No FileObject for " + root + " found.\n" +
                                    "Maybe you need ${openide/masterfs.dir}/modules/org-netbeans-modules-masterfs.jar\n" +
                                    "in test.unit.run.cp.extra, or make sure Lookups.metaInfServices is included in Lookup.default, so that\n" +
                                    "Lookup.default<URLMapper>=" + Lookup.getDefault().lookup(new Lookup.Template(URLMapper.class)).allInstances() + " includes MasterURLMapper\n" +
                                    "e.g. by using TestUtil.setLookup(Object[]) rather than TestUtil.setLookup(Lookup).");
            }
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }

}
