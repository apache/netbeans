/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
