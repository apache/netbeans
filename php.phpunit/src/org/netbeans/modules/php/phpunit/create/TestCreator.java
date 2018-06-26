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
