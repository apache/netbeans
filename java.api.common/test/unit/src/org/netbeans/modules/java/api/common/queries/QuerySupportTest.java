/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.api.common.queries;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;
import org.w3c.dom.Element;

public class QuerySupportTest extends NbTestCase {

    public QuerySupportTest(String testName) {
        super(testName);
    }
    private FileObject scratch;
    private FileObject projdir;

    @Override
    protected void setUp() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Services");
        if (fo != null) {
            fo.delete();
        }
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        MockLookup.setInstances(
                new AntBasedProjectFactorySingleton(),
                AntBasedTestUtil.testAntBasedProjectType());
    }

    @Override
    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
    }

    public void testAntHelperProject() throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(projdir, "test");
        Project p = ProjectManager.getDefault().findProject(projdir);

        ProjectInformation pi = QuerySupport.createProjectInformation(h, p, null);
        assertEquals("???", pi.getDisplayName());
        assertEquals("___", pi.getName());
        Element data = h.getPrimaryConfigurationData(true);
        Element name = data.getOwnerDocument().createElementNS("urn:test:shared", "name");
        name.setTextContent("Test AntProjectHelper");
        data.appendChild(name);
        h.putPrimaryConfigurationData(data, true);

        assertEquals("Test AntProjectHelper", pi.getDisplayName());
        assertEquals("Test_AntProjectHelper", pi.getName());
    }

    public void testAntUpdateHelperProject() throws IOException {
        AntProjectHelper ah = ProjectGenerator.createProject(projdir, "test");
        Project p = ProjectManager.getDefault().findProject(projdir);
        UpdateImplementation upi = createUpdateImpl(ah);
        UpdateHelper uh = new UpdateHelper(upi, ah);

        ProjectInformation pi = QuerySupport.createProjectInformation(uh, p, null);
        assertEquals("???", pi.getDisplayName());
        assertEquals("___", pi.getName());
        Element data = uh.getPrimaryConfigurationData(true);
        Element name = data.getOwnerDocument().createElementNS("urn:test:shared", "name");
        name.setTextContent("Test UpdateHelper");
        data.appendChild(name);
        uh.putPrimaryConfigurationData(data, true);

        assertEquals("Test UpdateHelper", pi.getDisplayName());
        assertEquals("Test_UpdateHelper", pi.getName());
    }

    private UpdateImplementation createUpdateImpl(final AntProjectHelper ah) {
        return new UpdateImplementation() {

            @Override
            public boolean isCurrent() {
                return false;
            }

            @Override
            public boolean canUpdate() {
                return true;
            }

            @Override
            public void saveUpdate(EditableProperties props) throws IOException {
                // do nothing
            }

            @Override
            public Element getUpdatedSharedConfigurationData() {
                return ah.getPrimaryConfigurationData(true);
            }

            @Override
            public EditableProperties getUpdatedProjectProperties() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
}
