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
package org.netbeans.modules.web.clientproject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.clientproject.createprojectapi.ClientSideProjectGenerator;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.modules.web.common.api.UsageLogger;
import org.netbeans.spi.project.ui.ProjectConvertor;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * The {@link ProjectConvertor} implements for web client project.
 * @author Tomas Mysik
 * @author Tomas Zezula
 */
@ProjectConvertor.Registration(requiredPattern = "(bower|package)\\.json", position = 1000)
public final class ClientSideProjectConvertor implements ProjectConvertor {

    static final Logger LOGGER = Logger.getLogger(ClientSideProjectConvertor.class.getName());

    @StaticResource
    private static final String PROJECT_ICON = "org/netbeans/modules/web/clientproject/ui/resources/html5-convertor-project.png"; // NOI18N

    private static final String[] JSON_FILES = new String[] {
        "package.json", // NOI18N
        "bower.json", // NOI18N
    };


    @Override
    public Result isProject(FileObject projectDirectory) {
        assert projectDirectory != null;
        String displayName = null;
        String fileName = null;
        for (String jsonFile : JSON_FILES) {
            FileObject file = projectDirectory.getFileObject(jsonFile);
            if (file == null) {
                continue;
            }
            fileName = file.getNameExt();
            displayName = getDisplayName(file);
            if (StringUtilities.hasText(displayName)) {
                break;
            }
        }
        if (fileName == null) {
            // #262428
            LOGGER.log(Level.INFO, "None of {0} found in {1}", new Object[] {Arrays.toString(JSON_FILES), projectDirectory.getNameExt()});
            return null;
        }
        if (!StringUtilities.hasText(displayName)) {
            // should not happen often
            displayName = projectDirectory.getNameExt();
        }
        final Lookup transientLkp = ProjectConvertors.createDelegateToOwnerLookup(projectDirectory);
        return new Result(
                Lookups.exclude(transientLkp, ProjectProblemsProvider.class),
                new Factory(projectDirectory, displayName, (Closeable) transientLkp, fileName),
                displayName,
                ImageUtilities.image2Icon(ImageUtilities.loadImage(PROJECT_ICON)));
    }

    @CheckForNull
    private String getDisplayName(FileObject jsonFile) {
        assert jsonFile != null;
        JSONParser parser = new JSONParser();
        try (Reader reader = new BufferedReader(new InputStreamReader(jsonFile.getInputStream(), StandardCharsets.UTF_8))) {
            JSONObject content = (JSONObject) parser.parse(reader);
            Object name = content.get("name"); // NOI18N
            if (name instanceof String) {
                return (String) name;
            }
        } catch (ParseException | IOException ex) {
            LOGGER.log(Level.FINE, jsonFile.getPath(), ex);
        }
        return null;
    }

    //~ Inner classes

    private static final class Factory implements Callable<Project> {

        private static final UsageLogger PROJECT_CONVERTOR_USAGE_LOGGER = new UsageLogger.Builder(ClientSideProjectUtilities.USAGE_LOGGER_NAME)
                .message(ClientSideProjectConvertor.class, "USG_PROJECT_CONVERTOR") // NOI18N
                .firstMessageOnly(false)
                .create();

        private static final String[] KNOWN_SITE_ROOTS = new String[] {
            "public", // NOI18N
            "app", // NOI18N
            "web", // NOI18N
            "www", // NOI18N
            "public_html", // NOI18N
        };

        private final FileObject projectDirectory;
        private final String displayName;
        private final Closeable transientLkp;
        private final String fileName;


        Factory(FileObject projectDirectory, String displayName, Closeable transientLkp, String fileName) {
            assert projectDirectory != null;
            assert displayName != null : projectDirectory;
            assert transientLkp != null : projectDirectory;
            assert fileName != null : projectDirectory;
            this.projectDirectory = projectDirectory;
            this.displayName = displayName;
            this.transientLkp = transientLkp;
            this.fileName = fileName;
        }

        @Override
        public Project call() throws Exception {
            transientLkp.close();
            PROJECT_CONVERTOR_USAGE_LOGGER.log(fileName);
            deleteNbProject();
            return ClientSideProjectGenerator.createProject(new CreateProjectProperties(projectDirectory, displayName)
                    .setSourceFolder("") // NOI18N
                    .setSiteRootFolder(detectSiteRoot())
                    .setAutoconfigured(true));
        }

        private void deleteNbProject() throws IOException {
            FileObject nbproject = projectDirectory.getFileObject("nbproject"); // NOI18N
            if (nbproject != null
                    && nbproject.isValid()) {
                nbproject.delete();
            }
        }

        private String detectSiteRoot() {
            for (String dir : KNOWN_SITE_ROOTS) {
                FileObject fo = projectDirectory.getFileObject(dir);
                if (fo != null
                        && fo.isFolder())  {
                    return dir;
                }
            }
            return ""; // NOI18N
        }

    }
}
