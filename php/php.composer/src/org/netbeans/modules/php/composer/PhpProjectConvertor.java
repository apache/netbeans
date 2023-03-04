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
package org.netbeans.modules.php.composer;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.web.common.api.UsageLogger;
import org.netbeans.spi.project.ui.ProjectConvertor;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

@ProjectConvertor.Registration(requiredPattern = PhpProjectConvertor.COMPOSER_JSON_FILENAME, position = 500)
public final class PhpProjectConvertor implements ProjectConvertor {

    static final Logger LOGGER = Logger.getLogger(PhpProjectConvertor.class.getName());

    static final String COMPOSER_JSON_FILENAME = "composer.json"; // NOI18N
    @StaticResource
    private static final String PROJECT_ICON = "org/netbeans/modules/php/composer/ui/resources/phpConvertorProject.png"; // NOI18N


    @Override
    public Result isProject(FileObject projectDirectory) {
        assert projectDirectory != null;
        FileObject file = projectDirectory.getFileObject(COMPOSER_JSON_FILENAME);
        assert file != null : projectDirectory;
        JSONObject content = getContent(file);
        if (content == null) {
            // #250960
            return null;
        }
        String displayName = getDisplayName(content);
        if (!StringUtils.hasText(displayName)) {
            // should not happen often
            displayName = projectDirectory.getNameExt();
        }
        final Lookup transientLkp = ProjectConvertors.createDelegateToOwnerLookup(projectDirectory);
        return new Result(
                Lookups.exclude(transientLkp, ProjectProblemsProvider.class),
                new Factory(projectDirectory, displayName, (Closeable) transientLkp),
                displayName,
                ImageUtilities.image2Icon(ImageUtilities.loadImage(PROJECT_ICON)));
    }

    @CheckForNull
    private JSONObject getContent(FileObject jsonFile) {
        assert jsonFile != null;
        JSONParser parser = new JSONParser();
        try (Reader reader = new BufferedReader(new InputStreamReader(jsonFile.getInputStream(), StandardCharsets.UTF_8))) {
            Object content = parser.parse(reader);
            if (content instanceof JSONObject) {
                return (JSONObject) content;
            }
            LOGGER.log(Level.FINE, "Unexpected content of composer.json: {0}", FileUtil.toFile(jsonFile));
            return null;
        } catch (ParseException | IOException ex) {
            LOGGER.log(Level.FINE, jsonFile.getPath(), ex);
        }
        return null;
    }

    @CheckForNull
    private String getDisplayName(JSONObject content) {
        assert content != null;
        Object name = content.get("name"); // NOI18N
        if (name instanceof String) {
            return (String) name;
        }
        return null;
    }

    //~ Inner classes

    private static final class Factory implements Callable<Project> {

        private static final UsageLogger PROJECT_CONVERTOR_USAGE_LOGGER = new UsageLogger.Builder("org.netbeans.ui.metrics.php.composer") // NOI18N
                .message(PhpProjectConvertor.class, "USG_PROJECT_CONVERTOR") // NOI18N
                .create();

        private static final String[] KNOWN_SOURCE_ROOTS = new String[] {
            "src", // NOI18N
            "lib", // NOI18N
        };

        private final FileObject projectDirectory;
        private final String displayName;
        private final Closeable transientLkp;


        Factory(FileObject projectDirectory, String displayName, Closeable transientLkp) {
            assert projectDirectory != null;
            assert displayName != null : projectDirectory;
            assert transientLkp != null : projectDirectory;
            this.projectDirectory = projectDirectory;
            this.displayName = displayName;
            this.transientLkp = transientLkp;
        }

        @Override
        public Project call() throws Exception {
            transientLkp.close();
            PROJECT_CONVERTOR_USAGE_LOGGER.log("composer.json"); // NOI18N
            deleteNbProject();
            PhpModuleGenerator phpModuleGenerator = Lookup.getDefault().lookup(PhpModuleGenerator.class);
            assert phpModuleGenerator != null;
            phpModuleGenerator.createModule(new PhpModuleGenerator.CreateProperties()
                    .setName(displayName)
                    .setProjectDirectory(FileUtil.toFile(projectDirectory))
                    .setSourcesDirectory(FileUtil.toFile(detectSourceRoot()))
                    .setPhpVersion(PhpVersion.getDefault())
                    .setCharset(StandardCharsets.UTF_8)
                    .setAutoconfigured(true));
            Project project = FileOwnerQuery.getOwner(projectDirectory);
            assert project != null : projectDirectory;
            return project;
        }

        private void deleteNbProject() throws IOException {
            FileObject nbproject = projectDirectory.getFileObject("nbproject"); // NOI18N
            if (nbproject != null
                    && nbproject.isValid()) {
                nbproject.delete();
            }
        }

        private FileObject detectSourceRoot() {
            // first check if there is any *.php file right in project dir
            for (FileObject child : projectDirectory.getChildren()) {
                if (FileUtils.isPhpFile(child)) {
                    return projectDirectory;
                }
            }
            // now, check well known sources
            for (String dir : KNOWN_SOURCE_ROOTS) {
                FileObject srcDir = projectDirectory.getFileObject(dir);
                if (srcDir != null)  {
                    return srcDir;
                }
            }
            return projectDirectory;
        }

    }
}
