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
