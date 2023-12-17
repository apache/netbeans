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

package org.netbeans.modules.cnd.meson.project;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.meson.lexer.MesonBuildTokenId;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=ProjectFactory.class, position = 200)
public class MesonProjectFactory implements ProjectFactory2 {
    private static final Logger LOGGER = Logger.getLogger(MesonProjectFactory.class.getName());

    @Override
    public ProjectManager.Result isProject2(FileObject projectDirectory) {
        FileObject script = projectDirectory.getFileObject("meson.build"); // NOI18N
        if ((script != null) && script.isValid()) {
            // The first statement in a top-level meson.build file must be a
            // call to project(...).  Leverage that to avoid detecting meson.build
            // files in subdirectories as meson projects.
            try {
                TokenSequence<MesonBuildTokenId> tokens =
                    TokenHierarchy.create(
                        script.asText(),
                        false,
                        MesonBuildTokenId.language(),
                        Stream.of(MesonBuildTokenId.WHITESPACE, MesonBuildTokenId.COMMENT).collect(Collectors.toSet()),
                        new InputAttributes()).tokenSequence(MesonBuildTokenId.language());

                if (tokens.moveNext() && TokenUtilities.equals(tokens.token().text(), "project")) {
                    return new ProjectManager.Result(null, MesonProjectType.TYPE, MesonProject.getIcon());
                }
            }
            catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        return null;
    }

    @Override
    public boolean isProject(FileObject projectDirectory) {
        return isProject2(projectDirectory) != null;
    }

    @Override
    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        if (isProject(projectDirectory)) {
            return new MesonProject(projectDirectory, state);
        }
        return null;
    }

    @Override
    public void saveProject(Project project) throws IOException, ClassCastException {
    }
}