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
package org.netbeans.modules.php.phpunit.create;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.phpunit.commands.SkeletonGenerator;
import org.netbeans.modules.php.phpunit.ui.options.PhpUnitOptionsPanelController;
import org.netbeans.modules.php.spi.testing.create.CreateTestsResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 */
public class TestCreator {

    private static final Logger LOGGER = Logger.getLogger(TestCreator.class.getName());

    private final PhpModule phpModule;


    public TestCreator(PhpModule phpModule) {
        this.phpModule = phpModule;
    }

    public CreateTestsResult createTests(List<FileObject> files) {
        final Set<FileObject> failed = new HashSet<>();
        final Set<FileObject> succeeded = new HashSet<>();

        try {
            SkeletonGenerator skeletonGenerator = SkeletonGenerator.getDefault();
            for (FileObject fo : files) {
                generateTest(skeletonGenerator, phpModule, fo, failed, succeeded);
            }
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(ex.getLocalizedMessage(), PhpUnitOptionsPanelController.OPTIONS_SUB_PATH);
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            UiUtils.processExecutionException(ex, PhpUnitOptionsPanelController.OPTIONS_SUB_PATH);
        }
        return new CreateTestsResult(succeeded, failed);
    }

    private void generateTest(SkeletonGenerator skeletonGenerator, PhpModule phpModule, FileObject fo, Set<FileObject> failed, Set<FileObject> succeeded) throws ExecutionException {
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        assert editorSupport != null : "Editor support must exist";
        Collection<PhpClass> classes = editorSupport.getClasses(fo);
        if (classes.isEmpty()) {
            failed.add(fo);
            return;
        }
        for (PhpClass phpClass : classes) {
            FileObject testFile = skeletonGenerator.generateTest(phpModule, fo, phpClass.getFullyQualifiedName());
            if (testFile != null) {
                succeeded.add(testFile);
            } else {
                // test not generated
                failed.add(fo);
            }
        }
    }

}
