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
package org.netbeans.modules.jshell.maven;

import java.io.IOException;
//import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
//import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.support.SnippetStorage;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(
        service = SnippetStorage.class,
        projectType = "org-netbeans-modules-maven"
)
public class MavenSnippetStorage implements SnippetStorage {
    public static final String SNIPPET_FOLDER = "org.netbeans.jshell.snippetsRoot"; // NOI18N
    private final Project project;
    private final Lookup lookup;

    public MavenSnippetStorage(Project project, Lookup lookup) {
        this.project = project;
        this.lookup = lookup;
    }
    
    private String evaluate(String s) {
        AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        return props.get(s, true);
    }
    
    private String[] snippetsFolderName() {
        String val = evaluate(SNIPPET_FOLDER);
        if (val == null) {
            return null;
        }
        int lastSlash = val.lastIndexOf('/');
        if (lastSlash == -1 || lastSlash == val.length() - 1) {
            return new String[] {
                null, val, val
            };
        } else {
            return new String[] {
                val.substring(0, lastSlash),
                val.substring(lastSlash + 1),
                val
            };
        }
    }
    
    @Override
    public FileObject getStorageFolder(boolean createIfMissing) {
        FileObject root = project.getProjectDirectory();
        String[] place = snippetsFolderName();
        if (place == null) {
            return null;
        }
        FileObject snippets = place[0] == null ? root : root.getFileObject(place[0]);
        if (snippets == null) {
            if (!createIfMissing) {
                return null;
            }
            try {
                // create the folder:
                FileObject target = FileUtil.createFolder(project.getProjectDirectory(),
                        place[2]);
                return target.getParent();
            } catch (IOException ex) {
                return null;
            }
        }
        return snippets;
    }

    @Override
    public String resourcePrefix() {
        String[] place = snippetsFolderName();
        if (place == null) {
            return null;
        }
        return place[1];
    }

    @Override
    public String startupSnippets(String runAction) {
        return "startup";
    }
}
