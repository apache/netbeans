/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.prep.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.preferences.CssPreprocessorPreferences;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.spi.ProjectWebRootProvider;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorsUI;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Utilities;

public final class CssPreprocessorUtils {

    private static final Logger LOGGER = Logger.getLogger(CssPreprocessorUtils.class.getName());

    static final String MAPPINGS_DELIMITER = ","; // NOI18N
    static final String MAPPING_DELIMITER = ":"; // NOI18N

    private static final String WEB_ROOT_PARAM = "${web.root}"; // NOI18N
    private static final String CSS_EXTENSION = "css"; // NOI18N


    private CssPreprocessorUtils() {
    }

    @NbBundle.Messages({
        "# {0} - preprocessor name",
        "CssPreprocessorUtils.fileSaved.title=Configure {0}",
        "# {0} - preprocessor name",
        "CssPreprocessorUtils.fileSaved.question=<html>Do you want to configure automatic {0} compilation on save?<br>"
            + "Note that you can always turn this on (or off) later in Project Properties.",
    })
    public static void processSavedFile(Project project, CssPreprocessorType type) {
        assert project != null;
        assert type != null;
        // if not configured, ask user; if YES, prefill preferences and open customizer
        CssPreprocessorPreferences projectPreferences = type.getPreferences();
        assert projectPreferences != null;
        if (projectPreferences.isConfigured(project)) {
            return;
        }
        // we are now configured, in any case
        projectPreferences.setConfigured(project, true);
        String displayName = type.getDisplayName();
        if (!askUser(Bundle.CssPreprocessorUtils_fileSaved_title(displayName), Bundle.CssPreprocessorUtils_fileSaved_question(displayName))) {
            return;
        }
        projectPreferences.setEnabled(project, true);
        projectPreferences.setMappings(project, getDefaultMappings(type));
        CustomizerProvider2 customizerProvider = project.getLookup().lookup(CustomizerProvider2.class);
        // #204164
        if (customizerProvider == null) {
            LOGGER.log(Level.WARNING, "CustomizerProvider2 not found in lookup of project {0}", project.getClass().getName());
        } else {
            customizerProvider.showCustomizer(CssPreprocessorsUI.CUSTOMIZER_IDENT, null);
        }
    }

    public static List<Pair<String, String>> getDefaultMappings(CssPreprocessorType type) {
        return Collections.singletonList(Pair.of("/" + type.getDefaultDirectoryName(), "/" + CSS_EXTENSION)); // NOI18N
    }

    private static boolean askUser(String title, String question) {
        Object result = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(question, title, NotifyDescriptor.YES_NO_OPTION));
        return result == NotifyDescriptor.YES_OPTION;
    }

    public static String encodeMappings(List<Pair<String, String>> mappings) {
        StringBuilder buffer = new StringBuilder(200);
        for (Pair<String, String> mapping : mappings) {
            if (buffer.length() > 0) {
                buffer.append(MAPPINGS_DELIMITER);
            }
            buffer.append(mapping.first());
            buffer.append(MAPPING_DELIMITER);
            buffer.append(mapping.second());
        }
        return buffer.toString();
    }

    public static List<Pair<String, String>> decodeMappings(String mappings) {
        List<String> pairs = StringUtils.explode(mappings, MAPPINGS_DELIMITER);
        List<Pair<String, String>> result = new ArrayList<>(pairs.size());
        for (String pair : pairs) {
            List<String> paths = StringUtils.explode(pair, MAPPING_DELIMITER);
            result.add(Pair.of(paths.get(0), paths.get(1)));
        }
        return result;
    }

    public static List<String> parseCompilerOptions(@NullAllowed String compilerOptions, @NullAllowed FileObject webRoot) {
        if (!StringUtils.hasText(compilerOptions)) {
            return Collections.emptyList();
        }
        String[] parsedCompilerParams = Utilities.parseParameters(processCompilerOptions(compilerOptions, webRoot));
        return Arrays.asList(parsedCompilerParams);
    }

    private static String processCompilerOptions(String compilerOptions, @NullAllowed FileObject webRoot) {
        if (webRoot == null
                || compilerOptions.indexOf(WEB_ROOT_PARAM) == -1) {
            return compilerOptions;
        }
        return compilerOptions.replace(WEB_ROOT_PARAM, FileUtil.toFile(webRoot).getAbsolutePath());
    }

    @CheckForNull
    public static FileObject getWebRoot(Project project, FileObject fileObject) {
        ProjectWebRootProvider projectWebRootProvider = getProjectWebRootProvider(project);
        if (projectWebRootProvider == null) {
            if (projectWebRootProvider == null) {
                return project.getProjectDirectory();
            }
            return null;
        }
        return projectWebRootProvider.getWebRoot(fileObject);
    }

    @CheckForNull
    public static FileObject getWebRoot(Project project) {
        ProjectWebRootProvider projectWebRootProvider = getProjectWebRootProvider(project);
        if (projectWebRootProvider == null) {
            return project.getProjectDirectory();
        }
        Collection<FileObject> webRoots = projectWebRootProvider.getWebRoots();
        if (webRoots.isEmpty()) {
            return null;
        }
        return webRoots.iterator().next();
    }

    @CheckForNull
    private static ProjectWebRootProvider getProjectWebRootProvider(Project project) {
        ProjectWebRootProvider projectWebRootProvider = project.getLookup().lookup(ProjectWebRootProvider.class);
        if (projectWebRootProvider == null) {
            LOGGER.log(Level.INFO, "ProjectWebRootProvider should be found in project lookup of {0}", project.getClass().getName());
            return null;
        }
        return projectWebRootProvider;
    }

    @CheckForNull
    public static File resolveTarget(FileObject webRoot, List<Pair<String, String>> mappings, FileObject source) {
        return resolveTarget(FileUtil.toFile(webRoot), mappings, FileUtil.toFile(source));
    }

    @CheckForNull
    public static File resolveTarget(FileObject webRoot, List<Pair<String, String>> mappings, File source) {
        return resolveTarget(FileUtil.toFile(webRoot), mappings, source);
    }

    public static File resolveInput(FileObject webRoot, Pair<String, String> mapping) {
        return resolveFile(FileUtil.toFile(webRoot), mapping.first());
    }

    @CheckForNull
    static File resolveTarget(File root, List<Pair<String, String>> mappings, File file) {
        String name = file.getName();
        String extension = FileUtil.getExtension(name);
        if (!extension.isEmpty()) {
            name = name.substring(0, name.length() - (extension.length() + 1));
        }
        for (Pair<String, String> mapping : mappings) {
            File from = resolveFile(root, mapping.first());
            if (from.equals(file)) {
                // exact file match
                File to = resolveFile(root, mapping.second());
                if (isFileMapping(to, CSS_EXTENSION)) {
                    // 'to' is file
                    return to;
                }
                // 'to' is directory
                return resolveFile(to, makeCssFilename(name));
            } else if (isFileMapping(from, extension)) {
                continue;
            }
            // 'from' is directory
            String relpath;
            try {
                relpath = PropertyUtils.relativizeFile(from, file.getParentFile());
            } catch (IllegalArgumentException ex) {
                // #237525
                LOGGER.log(Level.INFO, "Incorrect mapping [input is existing file but directory expected]", ex);
                continue;
            }
            if (relpath == null
                    || relpath.startsWith("..")) { // NOI18N
                // unrelated
                continue;
            }
            File to = PropertyUtils.resolveFile(resolveFile(root, mapping.second()), relpath);
            assert !isFileMapping(to, CSS_EXTENSION) : to;
            return resolveFile(to, makeCssFilename(name));
        }
        // no mapping
        return null;
    }

    static boolean isFileMapping(File file, String extension) {
        if (file.isFile()) {
            return true;
        }
        if (file.isDirectory()) {
            return false;
        }
        return file.getName().toLowerCase().endsWith("." + extension); // NOI18N
    }

    static File resolveFile(File directory, String subpath) {
        subpath = subpath.trim();
        if (subpath.startsWith("/")) { // NOI18N
            subpath = subpath.substring(1);
        }
        return PropertyUtils.resolveFile(directory, subpath);
    }

    private static String makeCssFilename(String name) {
        return name + "." + CSS_EXTENSION; // NOI18N
    }

    //~ Inner classes

    public static final class MappingsValidator {

        private static final Pattern MAPPING_PATTERN = Pattern.compile("[^" + MAPPING_DELIMITER + "]+"); // NOI18N

        private final String extension;
        private final ValidationResult result = new ValidationResult();


        public MappingsValidator(String extension) {
            assert extension != null;
            this.extension = extension;
        }

        public ValidationResult getResult() {
            return result;
        }

        public MappingsValidator validate(@NullAllowed FileObject root, List<Pair<String, String>> mappings) {
            File f = null;
            if (root != null) {
                f = FileUtil.toFile(root);
            }
            return validate(f, mappings);
        }

        public MappingsValidator validate(@NullAllowed File root, List<Pair<String, String>> mappings) {
            validateMappings(root, mappings);
            return this;
        }

        @NbBundle.Messages({
            "MappingsValidator.warning.root.invalid=Web/site root is invalid.",
            "MappingsValidator.warning.none=At least one input and output path must be set.",
            "MappingsValidator.warning.input.empty=Input path cannot be empty.",
            "MappingsValidator.warning.output.empty=Output path cannot be empty.",
            "# {0} - mapping",
            "MappingsValidator.warning.input.format=Input path \"{0}\" is incorrect.",
            "# {0} - mapping",
            "MappingsValidator.warning.output.format=Output path \"{0}\" is incorrect.",
            "# {0} - directory mapping",
            "# {1} - file mapping",
            "MappingsValidator.warning.io.conflict=Directory \"{0}\" cannot be mapped to file \"{1}\".",
        })
        private MappingsValidator validateMappings(@NullAllowed File root, List<Pair<String, String>> mappings) {
            if (root == null) {
                result.addError(new ValidationResult.Message("root", Bundle.MappingsValidator_warning_root_invalid())); // NOI18N
            }
            if (mappings.isEmpty()) {
                result.addError(new ValidationResult.Message("mappings", Bundle.MappingsValidator_warning_none())); // NOI18N
            }
            for (Pair<String, String> mapping : mappings) {
                // input
                String input = mapping.first();
                if (!StringUtils.hasText(input)) {
                    result.addError(new ValidationResult.Message("mapping." + input, Bundle.MappingsValidator_warning_input_empty())); // NOI18N
                } else if (!MAPPING_PATTERN.matcher(input).matches()) {
                    result.addError(new ValidationResult.Message("mapping." + input, Bundle.MappingsValidator_warning_input_format(input))); // NOI18N
                }
                // output
                String output = mapping.second();
                if (!StringUtils.hasText(output)) {
                    result.addError(new ValidationResult.Message("mapping." + output, Bundle.MappingsValidator_warning_output_empty())); // NOI18N
                } else if (!MAPPING_PATTERN.matcher(output).matches()) {
                    result.addError(new ValidationResult.Message("mapping." + output, Bundle.MappingsValidator_warning_output_format(output))); // NOI18N
                }
                // dir -> file?
                if (root != null) {
                    File inputFile = resolveFile(root, input);
                    File outputFile = resolveFile(root, output);
                    if (!isFileMapping(inputFile, extension)
                            && isFileMapping(outputFile, CSS_EXTENSION)) {
                        result.addError(new ValidationResult.Message("mapping.io." + output, Bundle.MappingsValidator_warning_io_conflict(input, output))); // NOI18N
                    }
                }
            }
            return this;
        }

    }

}
