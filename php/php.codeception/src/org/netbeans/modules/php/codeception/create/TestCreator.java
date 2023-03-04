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
package org.netbeans.modules.php.codeception.create;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.codeception.commands.Codecept;
import org.netbeans.modules.php.codeception.commands.Codecept.GenerateCommand;
import org.netbeans.modules.php.codeception.ui.options.CodeceptionOptionsPanelController;
import org.netbeans.modules.php.spi.testing.create.CreateTestsResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Pair;

public final class TestCreator {

    private static final Logger LOGGER = Logger.getLogger(TestCreator.class.getName());

    public static final String GENERATE_COMMAND_PARAM = "GENERATE_COMMAND_PARAM"; // NOI18N
    public static final String SUITE_PARAM = "SUITE_PARAM"; // NOI18N

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "MS_MUTABLE_COLLECTION", justification = "It is immutable") // NOI18N
    public static final List<GenerateCommand> TEST_COMMANDS = Arrays.asList(
            GenerateCommand.Test,
            GenerateCommand.Phpunit,
            GenerateCommand.Cept,
            GenerateCommand.Cest
    );

    private final PhpModule phpModule;


    public TestCreator(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    public CreateTestsResult createTests(List<FileObject> files, Map<String, Object> configurationPanelProperties) {
        final Set<FileObject> failed = new HashSet<>();
        final Set<FileObject> succeeded = new HashSet<>();
        Pair<GenerateCommand, String> commandSuite = Pair.of((GenerateCommand) configurationPanelProperties.get(GENERATE_COMMAND_PARAM),
                (String) configurationPanelProperties.get(SUITE_PARAM));
        if (commandSuite.first() != null
                && commandSuite.second() != null) {
            try {
                Codecept codeception = Codecept.getForPhpModule(phpModule, true);
                if (codeception != null) {
                    for (FileObject fo : files) {
                        generateTest(codeception, phpModule, fo, commandSuite, failed, succeeded);
                    }
                }
            } catch (ExecutionException ex) {
                LOGGER.log(Level.INFO, null, ex);
                UiUtils.processExecutionException(ex, CodeceptionOptionsPanelController.OPTIONS_SUB_PATH);
            }
        }
        return new CreateTestsResult(succeeded, failed);
    }

    private void generateTest(Codecept codeception, PhpModule phpModule, FileObject fo, Pair<GenerateCommand, String> commandSuite,
            Set<FileObject> failed, Set<FileObject> succeeded) throws ExecutionException {
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        assert editorSupport != null : "Editor support must exist";
        Collection<PhpClass> classes = editorSupport.getClasses(fo);
        if (classes.isEmpty()) {
            failed.add(fo);
            return;
        }
        for (PhpClass phpClass : classes) {
            FileObject testFile = codeception.generateTest(phpModule, fo, commandSuite.first(), commandSuite.second(), phpClass.getFullyQualifiedName());
            if (testFile != null) {
                succeeded.add(testFile);
            } else {
                // test not generated
                failed.add(fo);
            }
        }
    }

}
