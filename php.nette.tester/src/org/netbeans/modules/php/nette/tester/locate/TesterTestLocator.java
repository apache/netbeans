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
