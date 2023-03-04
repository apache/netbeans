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
package org.netbeans.modules.php.nette.tester.create;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
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
        FileObject template = FileUtil.getConfigFile("Templates/Scripting/Tests/NetteTest.phpt"); // NOI18N
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
        DataObject newTest = dataTemplate.createFromTemplate(dataFolder, name + ".test"); // NOI18N
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

}
