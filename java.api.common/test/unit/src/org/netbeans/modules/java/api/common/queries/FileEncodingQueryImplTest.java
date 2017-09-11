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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.api.common.queries;

import java.io.File;
import java.nio.charset.Charset;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.test.MockLookup;

/**
 * Tests for {@link FileEncodingQueryImpl}.
 *
 * @author Tomas Mysik
 */
public class FileEncodingQueryImplTest extends NbTestCase {

    private FileObject projdir;
    private AntProjectHelper helper;
    private PropertyEvaluator eval;
    private Project prj;
    private static final String SOURCE_ENCODING = "source.encoding";

    public FileEncodingQueryImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType());
        super.setUp();
        this.clearWorkDir();
        File wd = getWorkDir();
        FileObject scratch = FileUtil.toFileObject(wd);
        assertNotNull(wd);
        projdir = scratch.createFolder("proj");
        helper = ProjectGenerator.createProject(projdir, "test");
        assertNotNull(helper);
        eval = helper.getStandardPropertyEvaluator();
        assertNotNull(eval);
        prj = ProjectManager.getDefault().findProject(projdir);
        assertNotNull(prj);
    }

    public void testFileEncodingQuery() throws Exception {
        final Charset UTF_8 = Charset.forName("UTF-8");
        final Charset ISO_8859_2 = Charset.forName("ISO-8859-2");
        
        FileObject dummy = projdir.createData("Dummy.java");
        
        setProjectCharset(UTF_8);
        FileEncodingQueryImplementation fileEncodingQuery = QuerySupport.createFileEncodingQuery(eval, SOURCE_ENCODING);
        
        Charset enc = fileEncodingQuery.getEncoding(dummy);
        assertEquals(UTF_8, enc);
        
        setProjectCharset(ISO_8859_2);
        enc = fileEncodingQuery.getEncoding(dummy);
        assertEquals(ISO_8859_2, enc);
    }

    private void setProjectCharset(final Charset charset) throws Exception {
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
                EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(SOURCE_ENCODING, charset.name());
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(prj);
                return null;
            }
        });
    }
}
