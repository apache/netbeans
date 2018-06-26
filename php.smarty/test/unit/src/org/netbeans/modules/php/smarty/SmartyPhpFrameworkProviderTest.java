/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
