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

package org.netbeans.modules.maven.newproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public final class TemplateUtils {
    /**
     * Archetype to be used with the template.
     */
    public static final String PARAM_ARCHETYPE = "archetype"; // NOI18N

    /**
     * Artifact ID for the new project.
     */
    public static final String PARAM_ARTIFACT_ID = "artifactId"; // NOI18N

    /**
     * Group ID for the new project.
     */
    public static final String PARAM_GROUP_ID = "groupId"; // NOI18N

    /**
     * Packaging for a project template.
     */
    public static final String PARAM_PACKAGING = "packaging"; // NOI18N

    /**
     * Version for the new project, will default to {@link #PARAM_VERSION_DEFAULT 1.0-SNAPSHOT} if not specified.
     */
    public static final String PARAM_VERSION = "version"; // NOI18N

    /**
     * Base package for the sources in the project
     */
    public static final String PARAM_PACKAGE = "package"; // NOI18N

    /**
     * Version for the new project, will default to 1.0-SNAPSHOT if not specified.
     */
    public static final String PARAM_VERSION_DEFAULT = "1.0-SNAPSHOT"; // NOI18N

    /**
     * Files to open after project creation. Comma-delimited list of
     * file names, relative to the template directory.
     */
    public static final String PARAM_TO_OPEN = "openFiles"; // NOI18N

    /**
     * If the parameter value is {@link Boolean#TRUE}, will <b>build</b>
     * the project.
     */
    public static final String PARAM_INITIAL_BUILD = "buildAfterCreate"; // NOI18N

    /**
     * If the parameter value is {@link Boolean#TRUE}, will prime the project,
     * downloading the necessary dependent artifacts.
     */
    public static final String PARAM_INITIAL_PRIME = "primedAfterCreate"; // NOI18N

    /**
     * Name of the created main class.
     */
    public static final String PARAM_MAIN_CLASS_NAME = "mainClassName"; // NOI18N

    /**
     * The target directory
     */
    public static final String PARAM_PROJDIR = CommonProjectActions.PROJECT_PARENT_FOLDER;
    
    private TemplateUtils() {}

    /**
     * Performs post-creation tasks, like priming, build or file opens.
     */
    @NbBundle.Messages({
        "# {0} - project name",
        "ProgressTitleBuild=Building project {0}",
        "# {0} - project name",
        "ProgressTitlePrime=Priming project {0}",
        "# {0} - project name",
        "ProgressTitleBuildFailed=Failed to build project {0}",
        "# {0} - project name",
        "ProgressTitlePrimeFailed=Failed to prime project {0}"
    })
    public static List<FileObject> afterTemplateCreation(
        Map<Object, Object> additionalProperties, 
        CreateDescriptor desc, List<FileObject> fos, FileObject fo) throws IOException {
        List<FileObject> pomDirs = new ArrayList<>();
        collectPomDirs(fo, pomDirs);
        if (fos != null) {
            pomDirs.addAll(fos);
        }
        setMainClass(additionalProperties, fo);

        // compatibility with older archetype handler
        if (fos != null) {
            String toOpen = Objects.toString(additionalProperties.get("archetypeOpen"), null); // NOI18N
            if (toOpen == null) {
                toOpen = desc.getValue(TemplateUtils.PARAM_TO_OPEN);
            }
            if (toOpen != null) {
                collectFiles(fo, fos, toOpen.split(",")); // NOI18N
            }
        }
        // compatibility with older archetype handler
        boolean enableBuild = Boolean.valueOf(
                Objects.toString(
                        additionalProperties.getOrDefault("archetypeBuild", // NOI18N
                            desc.getValue(PARAM_INITIAL_BUILD)
                        ),
                        null));

        boolean enablePrime = Boolean.valueOf(desc.getValue(PARAM_INITIAL_PRIME));
        
        if (enableBuild || enablePrime) {
            Project prj = ProjectManager.getDefault().findProject(fo);
            ActionProvider ap = prj == null ? null : prj.getLookup().lookup(ActionProvider.class);
            if (ap != null) {
                Lookup actionLookup = desc.getLookup();
                if (actionLookup == null || actionLookup == Lookup.EMPTY) {
                    actionLookup = prj.getLookup();
                }

                final String command = 
                    enableBuild ? ActionProvider.COMMAND_BUILD :
                        enablePrime ? ActionProvider.COMMAND_PRIME :
                        null;
                if (command != null) {
                    ActionProgress prg = actionLookup.lookup(ActionProgress.class);
                    if (prg == null) {
                        ProgressHandle h = actionLookup.lookup(ProgressHandle.class);
                        if (h != null) {
                            // ProgressHandle is not null ; so let's add reporting based on ActionProgress
                            // that will use ProgressHandle 
                            ProjectInformation info = prj.getLookup().lookup(ProjectInformation.class);
                            String pn = info != null ? info.getDisplayName() : info.getName();
                            if (pn == null) {
                                if (desc.getName() == null) {
                                    pn = desc.getProposedName();
                                }
                            }
                            final String fpn = pn;
                            prg = new ActionProgress() {
                                @Override
                                protected void started() {
                                    h.progress(enableBuild ? 
                                        Bundle.ProgressTitleBuild(fpn) : Bundle.ProgressTitlePrime(fpn)
                                    );
                                }

                                @Override
                                public void finished(boolean success) {
                                    if (success) {
                                        return;
                                    }
                                    h.progress(enableBuild ? 
                                        Bundle.ProgressTitleBuildFailed(fpn) : Bundle.ProgressTitlePrimeFailed(fpn)
                                    );
                                }
                                
                            };
                            actionLookup = new ProxyLookup(actionLookup, Lookups.fixed(prg));
                        }
                    }
                    ap.invokeAction(command, actionLookup);
                }
            }
        }
        return pomDirs;
    }

    private static void collectPomDirs(FileObject dir, Collection<? super FileObject> found) {
        if (dir == null || !dir.isFolder()) {
            return;
        }
        if (dir.getFileObject("pom.xml") == null) { // NOI18N
            return;
        }
        found.add(dir);
        for (FileObject f : dir.getChildren()) {
            collectPomDirs(f, found);
        }
    }

    static void collectFiles(FileObject root, Collection<? super FileObject> found, String... includes) {
        Pattern[] check = new Pattern[includes.length];
        for (int i = 0; i < check.length; i++) {
            check[i] = Pattern.compile(includes[i]);
        }

        Enumeration<? extends FileObject> en = root.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            String relPath = FileUtil.getRelativePath(root, fo);
            if (relPath == null) {
                continue;
            }
            for (Pattern p : check) {
                if (p.matcher(relPath).matches()) {
                    found.add(fo);
                    break;
                }
            }
        }
    }

    private static void setMainClass(Map<Object, Object> properties, FileObject fo) {
        FileObject pom = fo.getFileObject("pom.xml");   // NOI18N
        if (pom != null) {
            ModelSource modelSource = Utilities.createModelSource(pom);
            POMModel model = POMModelFactory.getDefault().getModel(modelSource);
            org.netbeans.modules.maven.model.pom.Project root = model.getProject();
            if (root != null) {
                if (model.getProject().getPackaging() == null || "jar".equals(model.getProject().getPackaging())) {
                    if (model.startTransaction()) {
                        try {
                            org.netbeans.modules.maven.model.pom.Properties props = root.getProperties();
                            if (props == null) {
                                props = model.getFactory().createProperties();
                                root.setProperties(props);
                            }
                            String packageName = (String) properties.get(TemplateUtils.PARAM_PACKAGE);
                            String mainClass = (String) properties.get(TemplateUtils.PARAM_MAIN_CLASS_NAME);
                            if (mainClass == null || mainClass.isEmpty()) {
                                mainClass = "App";  // NOI18N
                            }
                            if (packageName != null && !packageName.isEmpty()) {
                                mainClass = packageName + '.' + mainClass;
                            }
                            props.setProperty("exec.mainClass", mainClass); // NOI18N
                        } finally {
                            model.endTransaction();
                            try {
                                Utilities.saveChanges(model);
                            } catch (IOException ex) {}
                        }
                    }
                }
            }
        }
    }

    @NbBundle.Messages({
        "MSG_NoArtifactId=No artifactId attribute specified for the Maven project"
    })
    public static ProjectInfo createProjectInfo(String name, Map<String, Object> vals) throws IOException {
        String artifact = (String)vals.getOrDefault(TemplateUtils.PARAM_ARTIFACT_ID, name);
        String group = (String)vals.get(TemplateUtils.PARAM_GROUP_ID);
        String version = (String)vals.getOrDefault(TemplateUtils.PARAM_VERSION, TemplateUtils.PARAM_VERSION_DEFAULT);
        String pkg = (String)vals.get(TemplateUtils.PARAM_PACKAGE);

        if (artifact == null) {
            throw new IOException(Bundle.MSG_NoArtifactId());
        }
        if (group == null) {
            group = findGroupId(pkg, artifact);
        }
        if (group == null) {
            throw new IOException(Bundle.MSG_NoGroupId());
        }
        return new ProjectInfo(group, artifact, version, pkg);
    }   

    private static String findGroupId(String pkg, String name) {
        if (pkg != null && pkg.endsWith("." + name)) {
            return pkg.substring(0, pkg.length() - 1 - name.length());
        } else {
            return pkg;
        }
    }
}
