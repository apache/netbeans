/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
