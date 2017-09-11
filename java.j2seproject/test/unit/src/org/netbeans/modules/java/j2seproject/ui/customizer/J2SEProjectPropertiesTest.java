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

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

public class J2SEProjectPropertiesTest extends NbTestCase {

    public J2SEProjectPropertiesTest(String name) {
        super(name);
    }

    private J2SEProject p;
    private J2SEProjectProperties pp;

    @Override
    protected void setUp() throws Exception {
        MockLookup.setLayersAndInstances();
        clearWorkDir();
        J2SEProjectGenerator.createProject(getWorkDir(), "test", null, null, null, false);
        p = (J2SEProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir()));
        pp = new J2SEProjectProperties(p, p.getUpdateHelper(), p.evaluator(), p.getReferenceHelper(), null);
    }

    public void testRunConfigs() throws Exception {
        Map<String,Map<String,String>> m = pp.readRunConfigs();
        assertEquals("{null={run.jvmargs=}}", m.toString());
        // Define a new config and set some arguments.
        Map<String,String> c = new TreeMap<String,String>();
        c.put("application.args", "foo");
        m.put("foo", c);
        storeRunConfigs(m);
        m = pp.readRunConfigs();
        assertEquals("{null={run.jvmargs=}, foo={application.args=foo}}", m.toString());
        // Define args in default config.
        m.get(null).put("application.args", "bland");
        storeRunConfigs(m);
        m = pp.readRunConfigs();
        assertEquals("{null={application.args=bland, run.jvmargs=}, foo={application.args=foo}}", m.toString());
        // Reset to default in foo config.
        m.get("foo").put("application.args", null);
        storeRunConfigs(m);
        m = pp.readRunConfigs();
        assertEquals("{null={application.args=bland, run.jvmargs=}, foo={}}", m.toString());
        // Override as blank in foo config.
        m.get("foo").put("application.args", "");
        storeRunConfigs(m);
        m = pp.readRunConfigs();
        assertEquals("{null={application.args=bland, run.jvmargs=}, foo={application.args=}}", m.toString());
    }

    private void storeRunConfigs(Map<String,Map<String,String>> m) throws IOException {
        EditableProperties prj = p.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties prv = p.getUpdateHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        pp.storeRunConfigs(m, prj, prv);
        p.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, prj);
        p.getUpdateHelper().putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, prv);
        ProjectManager.getDefault().saveProject(p);
    }

}
