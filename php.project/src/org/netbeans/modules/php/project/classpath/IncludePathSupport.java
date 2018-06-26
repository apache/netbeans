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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.classpath;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Petr Hrebejk, Tomas Mysik
 */
public class IncludePathSupport extends BaseProjectPathSupport {

    private static final Set<String> WELL_KNOWN_PATHS = new HashSet<>(Arrays.asList(
            "${" + PhpProjectProperties.GLOBAL_INCLUDE_PATH + "}"));

    public IncludePathSupport(PropertyEvaluator evaluator, ReferenceHelper referenceHelper, AntProjectHelper antProjectHelper) {
        super(evaluator, referenceHelper, antProjectHelper);
    }

    @Override
    protected boolean isWellKnownPath(String p) {
        return WELL_KNOWN_PATHS.contains(p);
    }

    //~ Inner classes

    public static final class Validator {

        public static final String ANOTHER_PROJECT_MESSAGE_TYPE = "ANOTHER_PROJECT_MESSAGE_TYPE"; // NOI18N

        private final ValidationResult result = new ValidationResult();


        public ValidationResult getResult() {
            return new ValidationResult(result);
        }

        @NbBundle.Messages({
            "# {0} - file path",
            "IncludePathSupport.Validator.error.notFound=Path {0} does not exist.",
        })
        public Validator validateBroken(List<BasePathSupport.Item> items) {
            for (Item item : items) {
                if (item.isBroken()) {
                    result.addError(new ValidationResult.Message(item, Bundle.IncludePathSupport_Validator_error_notFound(item.getFilePath())));
                }
            }
            return this;
        }

        @NbBundle.Messages({
            "# {0} - file path",
            "IncludePathSupport.Validator.error.projectFile=Path {0} is already part of project.",
            "# {0} - file path",
            "# {1} - project name",
            "IncludePathSupport.Validator.error.anotherProjectSubFile=Path {0} belongs to project {1}. Remove it and add Source Files of that project.",
        })
        public Validator validatePaths(PhpProject project, List<BasePathSupport.Item> items) {
            for (Item item : items) {
                if (item.getType() == BasePathSupport.Item.Type.FOLDER
                        && !item.isBroken()) {
                    FileObject fileObject = item.getFileObject(project.getProjectDirectory());
                    if (fileObject == null) {
                        // not broken but not found?!
                        result.addError(new ValidationResult.Message(item, Bundle.IncludePathSupport_Validator_error_notFound(item.getFilePath())));
                        continue;
                    }
                    if (CommandUtils.isUnderAnySourceGroup(project, fileObject, false)) {
                        result.addError(new ValidationResult.Message(item,
                                Bundle.IncludePathSupport_Validator_error_projectFile(item.getAbsoluteFilePath(project.getProjectDirectory()))));
                        continue;
                    }
                    // #208284
                    if (isPartOfAnotherPhpProject(project, fileObject)
                            && !isSourcesDirectory(fileObject)) {
                        PhpProject phpProject = PhpProjectUtils.getPhpProject(fileObject);
                        assert phpProject != null;
                        result.addError(new ValidationResult.Message(item,
                                Bundle.IncludePathSupport_Validator_error_anotherProjectSubFile(item.getAbsoluteFilePath(project.getProjectDirectory()), phpProject.getName()),
                                ANOTHER_PROJECT_MESSAGE_TYPE));
                    }
                }
            }
            return this;
        }

        private static boolean isPartOfAnotherPhpProject(Project currentProject, FileObject file) {
            PhpProject phpProject = PhpProjectUtils.getPhpProject(file);
            if (phpProject == null) {
                return false;
            }
            return !currentProject.equals(phpProject);
        }

        private static boolean isSourcesDirectory(FileObject file) {
            PhpProject phpProject = PhpProjectUtils.getPhpProject(file);
            if (phpProject == null) {
                return false;
            }
            FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(phpProject);
            if (sourcesDirectory == null) {
                return false;
            }
            return sourcesDirectory.equals(file);
        }

    }

}
