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
package org.netbeans.modules.php.smarty;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.util.PhpProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SmartyPhpFrameworkProviderTest extends NbTestCase {

    public SmartyPhpFrameworkProviderTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        MockLookup.setLayersAndInstances();
    }

    public void testProjectWithoutSmarty() throws Exception {
        PhpModule phpModule = getPhpModule(createPhpProject());
        assert phpModule != null : "PHP module must exist!";

        assertTrue(!SmartyPhpFrameworkProvider.getInstance().isInPhpModule(phpModule));
    }

    public void testProjectWithCreatedTemplate() throws Exception {
        PhpModule phpModule = getPhpModule(createPhpProject());
        assert phpModule != null : "PHP module must exist!";

        FileUtil.createData(phpModule.getProjectDirectory(), "template.tpl");
        assertFalse(SmartyPhpFrameworkProvider.getInstance().isInPhpModule(phpModule));
        
        phpModule.getPreferences(SmartyPhpFrameworkProvider.class, true).putBoolean(SmartyPhpFrameworkProvider.PROP_SMARTY_AVAILABLE, true);
        assertTrue(SmartyPhpFrameworkProvider.getInstance().isInPhpModule(phpModule));
    }

    public void testProjectWithNewSmartyPropertyFlag() throws Exception {
        PhpModule phpModule = getPhpModule(createPhpProject());
        assert phpModule != null : "PHP module must exist!";
        Preferences preferences = phpModule.getPreferences(SmartyPhpFrameworkProvider.class, true);
        preferences.putBoolean(SmartyPhpFrameworkProvider.PROP_SMARTY_AVAILABLE, true);

        assertTrue(SmartyPhpFrameworkProvider.getInstance().isInPhpModule(phpModule));
    }

    private PhpModule getPhpModule(Project phpProject) {
        return phpProject.getLookup().lookup(PhpModule.class);
    }

    private Project createPhpProject() throws IOException {
        String projectName = "phpProject" + new Random().nextInt();
        File projectDir = new File(getWorkDir(), projectName);
        File srcDir = projectDir;

        final PhpProjectGenerator.ProjectProperties properties = new PhpProjectGenerator.ProjectProperties()
                .setProjectDirectory(projectDir)
                .setSourcesDirectory(srcDir)
                .setName(projectName)
                .setPhpVersion(PhpVersion.PHP_53)
                .setCharset(Charset.defaultCharset())
                .setUrl("http://localhost/" + projectName); //NOI18N

        AntProjectHelper antProjectHelper = PhpProjectGenerator.createProject(properties, null);

        final Project project = ProjectManager.getDefault().findProject(antProjectHelper.getProjectDirectory());
        ProjectManager.getDefault().saveProject(project);

        // check that
        assert project instanceof PhpProject;
        final PhpModule phpModule = project.getLookup().lookup(PhpModule.class);
        assert phpModule != null : "PHP module must exist!";

        return project;
    }
}
