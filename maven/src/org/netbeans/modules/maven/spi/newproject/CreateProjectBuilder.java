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

package org.netbeans.modules.maven.spi.newproject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Parent;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.modelcache.MavenProjectCache;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * builder for creating a new pom file/project.
 * @author mkleint
 * @since 2.98
 */
public class CreateProjectBuilder {
    private static final String SKELETON = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
        "    <modelVersion>4.0.0</modelVersion>\n" + "</project>";

    private final File projectDirectory;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private String packageName;
    private String packaging = "jar";
    private boolean updateParent = true;
    private MavenProject parentProject;
    private PomOperationsHandle operations;
    private AdditionalChangeHandle moreWork;
    private ProgressHandle progressHandle;

    public CreateProjectBuilder(File projectDir, String groupId, String artifactId, String version) {
        this.projectDirectory = projectDir;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }
    
    public CreateProjectBuilder setPackaging(String packaging) {
        this.packaging = packaging;
        return this;
    }
    
    public CreateProjectBuilder setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }
    
    /**
     * if true will update the parent project (if found) with the relevant module section addition
     * @param update
     * @return 
     */
    public CreateProjectBuilder setUpdateParent(boolean update) {
        this.updateParent = update;
        return this;
    }
    /**
     * sets the parent project, expected to be of "pom" packaging.
     * if not set explicitly,  will attempt to find the parent pom in the directory hierarchy
     * @param parent
     * @return 
     */
    public CreateProjectBuilder setParentProject(MavenProject parent) {
        this.parentProject = parent;
        return this;
    }
    
    public CreateProjectBuilder setAdditionalOperations(PomOperationsHandle opers) {
        this.operations = opers;
        return this;
    }
    
    public CreateProjectBuilder setAdditionalNonPomWork(AdditionalChangeHandle action) {
        this.moreWork = action;
        return this;
    }
    
    public CreateProjectBuilder setProgressHandle(ProgressHandle handle) {
        this.progressHandle = handle;
        return this;
    }
    
    public void create() {
            projectDirectory.mkdirs();

            List<ModelOperation<POMModel>> pomOpers = new ArrayList<ModelOperation<POMModel>>();
            //TODO is FOQ too adventurous, maybe just ProjectManager.findProject on parent is better heuristics?
            MavenProject parent = parentProject;
            if (parent == null) {
                Project p = FileOwnerQuery.getOwner(org.openide.util.Utilities.toURI(projectDirectory));
                if (p != null) {
                    NbMavenProject nbMavenParent = p.getLookup().lookup(NbMavenProject.class);
                    if (nbMavenParent != null && "pom".equals(nbMavenParent.getMavenProject().getPackaging())) {
                        parent = nbMavenParent.getMavenProject();
                    }
                }
            }
            if (parent != null && MavenProjectCache.isFallbackproject(parent)) {
                //#240989 - guessing that unloadable parent project could be the problem.
                parent = null;
            }
            Context context = new Context(parent, groupId, artifactId, version, packaging, projectDirectory, packageName);
            pomOpers.add(new BasicPropertiesOperation(context));
            if (operations != null) {
                pomOpers.addAll(operations.createPomOperations(context));
            }

            if (progressHandle != null) {
                progressHandle.progress("Writing pom.xml");
            }
            ModelSource model = Utilities.createModelSourceForMissingFile(new File(projectDirectory, "pom.xml"), true, SKELETON, "text/x-maven-pom+xml");
            Utilities.performPOMModelOperations(model, pomOpers);

            if (updateParent && parent != null) {
                if (progressHandle != null) {
                    progressHandle.progress("Updating parent pom.xml");
                }
                FileObject pom = FileUtil.toFileObject(parent.getFile());
                assert pom != null : "parent file:" + parent.getFile() + " for project " + parent.getId() + "  wizard directory: " + projectDirectory; //#240989
                if (pom != null) {
                    ModelSource pmodel = Utilities.createModelSource(pom);
                    Utilities.performPOMModelOperations(pmodel, Collections.singletonList(new AddModuleToParentOperation(pom.getParent(), projectDirectory)));
                }
            }
        
            if (moreWork != null) {
                Runnable action = moreWork.createAdditionalChange(context);
                if (action != null) {
                    action.run();
                }
            }
    }
    
    /**
     * create pom modeloperations based on the passed context
     */
    public static interface PomOperationsHandle {
        
        @NonNull List<ModelOperation<POMModel>> createPomOperations(@NonNull Context context);
    
    }
    
    public static interface AdditionalChangeHandle {
        Runnable createAdditionalChange(@NonNull Context context);
    }
    
    public static final class Context {
        private final String packaging;
        private final String version;
        private final String artifactId;
        private final String groupId;
        private final MavenProject parent;
        private final File projectDirectory;
        private final String packageName;
        
        private Context(MavenProject parent, String groupId, String artifactId, String version, String packaging, File projectDirectory, String packageName) {
            this.parent = parent;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.packaging = packaging;
            this.projectDirectory = projectDirectory;
            this.packageName = packageName;
        }

        public @NonNull String getPackaging() {
            return packaging;
        }

        public @NonNull String getVersion() {
            return version;
        }

        public @NonNull String getArtifactId() {
            return artifactId;
        }

        public @NonNull String getGroupId() {
            return groupId;
        }

        public @CheckForNull MavenProject getParent() {
            return parent;
        }

        public @NonNull File getProjectDirectory() {
            return projectDirectory;
        }

        public @CheckForNull String getPackageName() {
            return packageName;
        }
        
        
    }
    
    private class BasicPropertiesOperation implements ModelOperation<POMModel> {
        private final Context context;

        public BasicPropertiesOperation(Context context) {
            this.context = context;
        }

        @Override
        public void performOperation(POMModel model) {
            org.netbeans.modules.maven.model.pom.Project root = model.getProject();
            if (root != null) {
                MavenProject parent = context.getParent();
                if (parent != null) {
                    Parent parentpom = model.getFactory().createParent();
                    parentpom.setGroupId(parent.getGroupId());
                    parentpom.setArtifactId(parent.getArtifactId());
                    parentpom.setVersion(parent.getVersion());
                    File pfile = FileUtil.normalizeFile(parent.getFile());
                    String rel = FileUtilities.relativizeFile(context.getProjectDirectory(), pfile);
                    if (rel != null) {
                        if ("..".equals(rel) || "../pom.xml".equals(rel)) {
                            
                        } else {
                            parentpom.setRelativePath(rel);
                        }
                    } else {
                        parentpom.setRelativePath("");
                    }
                    root.setPomParent(parentpom);
                    
                }
                if (parent == null || !context.getGroupId().equals(parent.getGroupId())) {
                    root.setGroupId(context.getGroupId());
                }
                root.setArtifactId(context.getArtifactId());
                if (parent == null || !context.getVersion().equals(parent.getVersion())) {
                    root.setVersion(context.getVersion());
                }
                root.setPackaging(packaging);
                
                boolean setEncoding = true;
                if (parent != null) {
                    java.util.Properties parentprops = parent.getProperties();
                    if (parentprops != null && parentprops.containsKey("project.build.sourceEncoding")) {
                        setEncoding = false;
                    }
                }
                if (setEncoding) {
                    Properties props = root.getProperties();
                    if (props == null) {
                        props = model.getFactory().createProperties();
                        root.setProperties(props);
                    }
                    props.setProperty("project.build.sourceEncoding", "UTF-8");
                }
            }
        }
    }    
    
    private static class AddModuleToParentOperation implements ModelOperation<POMModel> {
        private final String relPath;

        public AddModuleToParentOperation(FileObject projectDirectory, File projFile) {
            FileObject dir = FileUtil.toFileObject(projFile);
            relPath = FileUtil.getRelativePath(projectDirectory, dir);
        }

        @Override
        public void performOperation(POMModel model) {
            if (relPath != null && model.getProject() != null) {
                List<String> modules = model.getProject().getModules();
                if (modules == null || !modules.contains(relPath)) {
                    model.getProject().addModule(relPath);
                }
            }
        }
    }
    
}
