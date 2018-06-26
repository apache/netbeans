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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.copysupport.CopySupport;
import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Tomas Mysik
 */
public final class TestUtils {

    private static final Logger PHP_PROJECT_LOGGER = Logger.getLogger(PhpProject.class.getName());
    private static final Logger COPY_SUPPORT_LOGGER = Logger.getLogger(CopySupport.class.getName());
    private static final TestLogHandler TEST_LOG_HANDLER = new TestLogHandler();


    private TestUtils() {
    }

    public static PhpProject createPhpProject(File workDir) throws IOException {
        String projectName;
        File projectDir;
        do {
            projectName = "phpProject" + new Random().nextLong();
            projectDir = new File(workDir, projectName);
        } while (projectDir.exists());

        final PhpProjectGenerator.ProjectProperties properties = new PhpProjectGenerator.ProjectProperties()
                .setProjectDirectory(projectDir)
                .setSourcesDirectory(projectDir)
                .setName(projectName)
                .setUrl("http://localhost/" + projectName)
                .setCharset(Charset.defaultCharset())
                .setPhpVersion(PhpVersion.PHP_53);

        AntProjectHelper antProjectHelper = PhpProjectGenerator.createProject(properties, null);

        final Project project = ProjectManager.getDefault().findProject(antProjectHelper.getProjectDirectory());
        ProjectManager.getDefault().saveProject(project);
        Assert.assertTrue("Not PhpProject but: " + project.getClass().getName(), project instanceof PhpProject);
        return (PhpProject) project;
    }

    public static PhpProject openPhpProject(PhpProject phpProject) throws Exception {
        PhpProject openedProject = openPhpProject(phpProject.getProjectDirectory());
        Assert.assertEquals("Project names should be same.", phpProject.getName(), openedProject.getName());
        return openedProject;
    }

    public static PhpProject openPhpProject(FileObject projectDir) throws Exception {
        Object openedProject = waitProjectOpened(projectDir);
        Assert.assertTrue("Project should be opened: " + openedProject.getClass().getName(), openedProject instanceof PhpProject);
        PhpProject phpProject = (PhpProject) openedProject;
        return phpProject;
    }

    public static boolean closePhpProject(PhpProject project) throws Exception {
        boolean closed = waitProjectClosed(project);
        Assert.assertTrue("Project should be closed: " + project, closed);
        return closed;
    }

    public static void waitCopySupportFinished() throws Exception {
        waitForMessage(COPY_SUPPORT_LOGGER, "COPY_TASK_FINISHED", null);
    }

    /**
     * Open project and wait for ProjectOpenedHook to finish.
     */
    private static Object waitProjectOpened(final FileObject projectDir) throws Exception {
        return waitForMessage(PHP_PROJECT_LOGGER, "PROJECT_OPENED_FINISHED", new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return ProjectSupport.openProject(FileUtil.toFile(projectDir));
            }
        });
    }

    /**
     * Close project and wait for ProjectClosedHook to finish.
     */
    private static boolean waitProjectClosed(final PhpProject project) throws Exception {
        return waitForMessage(PHP_PROJECT_LOGGER, "PROJECT_CLOSED_FINISHED", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return ProjectSupport.closeProject(project.getName());
            }
        });
    }

    private static <T> T waitForMessage(Logger logger, String message, Callable<T> action) throws Exception {
        T result = null;
        final Level level = logger.getLevel();
        logger.addHandler(TEST_LOG_HANDLER);
        try {
            logger.setLevel(Level.FINEST);
            TEST_LOG_HANDLER.expect(message);
            if (action != null) {
                result = action.call();
            }
            TEST_LOG_HANDLER.await(5000);
        } finally {
            logger.setLevel(level);
            logger.removeHandler(TEST_LOG_HANDLER);
        }
        return result;
    }

}
