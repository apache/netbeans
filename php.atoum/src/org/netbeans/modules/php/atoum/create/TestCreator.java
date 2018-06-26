/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.atoum.create;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.spi.testing.create.CreateTestsResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

public class TestCreator {

    private static final Logger LOGGER = Logger.getLogger(TestCreator.class.getName());

    private final PhpModule phpModule;


    public TestCreator(PhpModule phpModule) {
        this.phpModule = phpModule;
    }

    public CreateTestsResult createTests(List<FileObject> files) {
        final Set<FileObject> failed = new HashSet<>();
        final Set<FileObject> succeeded = new HashSet<>();

        FileObject sourceDirectory = phpModule.getSourceDirectory();
        assert sourceDirectory != null : "Source directory should exist for " + phpModule;
        FileObject template = FileUtil.getConfigFile("Templates/Scripting/Tests/AtoumTest.php"); // NOI18N
        assert template != null;

        for (FileObject fo : files) {
            try {
                generateTest(sourceDirectory, template, fo, succeeded);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
                failed.add(fo);
            }
        }
        return new CreateTestsResult(succeeded, failed);
    }

    private void generateTest(FileObject sourceDirectory, FileObject template, FileObject fo, Set<FileObject> succeeded) throws IOException {
        FileObject testDirectory = phpModule.getTestDirectory(fo);
        assert testDirectory != null;
        FileObject dir = getTargetFolder(sourceDirectory, testDirectory, fo);
        String name = fo.getName();

        DataFolder dataFolder = DataFolder.findFolder(dir);
        DataObject dataTemplate = DataObject.find(template);
        DataObject newTest = dataTemplate.createFromTemplate(dataFolder, name, getTemplateParams(name));
        succeeded.add(newTest.getPrimaryFile());
    }

    private FileObject getTargetFolder(FileObject sourceDirectory, FileObject testDirectory, FileObject fo) throws IOException {
        FileObject commonRoot = FileUtils.getCommonRoot(fo, testDirectory);
        if (commonRoot == null
                || !FileUtil.isParentOf(sourceDirectory, commonRoot)) {
            // look only inside project source dir
            commonRoot = sourceDirectory;
        }
        assert commonRoot != null;
        String relativePath = FileUtil.getRelativePath(commonRoot, fo.getParent());
        assert relativePath != null : "Dir " + commonRoot + " must be parent of " + fo;
        FileObject target = testDirectory.getFileObject(relativePath);
        if (target == null) {
            target = FileUtil.createFolder(testDirectory, relativePath);
        }
        return target;
    }

    private Map<String, ? extends Object> getTemplateParams(String name) {
        return Collections.singletonMap("name", name); // NOI18N
    }

}
