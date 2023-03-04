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
package org.netbeans.modules.php.nette.tester.locate;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.locate.TestLocator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class TesterTestLocator implements TestLocator {

    private final PhpModule phpModule;


    public TesterTestLocator(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    @Override
    public Set<Locations.Offset> findSources(FileObject testFile) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        assert sourceDirectory != null : "Source directory must exist";
        List<FileObject> testDirectories = phpModule.getTestDirectories();
        assert !testDirectories.isEmpty();
        String relativePath = null;
        for (FileObject testDirectory : testDirectories) {
            relativePath = FileUtil.getRelativePath(testDirectory, testFile);
            if (relativePath != null) {
                break;
            }
        }
        assert relativePath != null : "File " + testFile + "must be found underneath " + testDirectories;
        List<String> extensions = FileUtil.getMIMETypeExtensions(FileUtils.PHP_MIME_TYPE);
        Set<Locations.Offset> result = new HashSet<>();
        for (;;) {
            int lastDot = relativePath.lastIndexOf('.'); // NOI18N
            if (lastDot == -1) {
                break;
            }
            relativePath = relativePath.substring(0, lastDot);
            for (String extension : extensions) {
                FileObject fileObject = sourceDirectory.getFileObject(relativePath + "." + extension); // NOI18N
                if (fileObject != null
                        && FileUtils.isPhpFile(fileObject)) {
                    result.add(new Locations.Offset(fileObject, -1));
                }
            }
        }
        return result;
    }

    @Override
    public Set<Locations.Offset> findTests(FileObject testedFile) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        assert sourceDirectory != null : "Source directory must exist";
        List<FileObject> testDirectories = phpModule.getTestDirectories();
        assert !testDirectories.isEmpty();
        String relativePath = FileUtil.getRelativePath(sourceDirectory, testedFile.getParent());
        assert relativePath != null : "File " + testedFile.getParent() + "must be found underneath " + sourceDirectory;
        FileObject parentTestFolder = null;
        for (FileObject testDirectory : testDirectories) {
            parentTestFolder = testDirectory.getFileObject(relativePath);
            if (parentTestFolder != null) {
                break;
            }
        }
        if (parentTestFolder == null) {
            return Collections.emptySet();
        }
        // try to find all tests, e.g. "Assert.contains.phpt, Assert.same.test.phpt, Assert.phpt" for "Assert.php"
        Set<Locations.Offset> result = new HashSet<>();
        for (FileObject child : parentTestFolder.getChildren()) {
            if (child.getName().startsWith(testedFile.getName())
                    && FileUtils.isPhpFile(child)) {
                result.add(new Locations.Offset(child, -1));
            }
        }
        return result;
    }

}
