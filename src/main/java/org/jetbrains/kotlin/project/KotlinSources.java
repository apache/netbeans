/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.project;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import org.jetbrains.kotlin.builder.KotlinPsiManager;
import org.jetbrains.kotlin.projectsextensions.maven.MavenHelper;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.openide.filesystems.FileObject;

/**
 * This class defines the location of Kotlin sources.
 */
public final class KotlinSources {

    private final Project kotlinProject;
    private boolean lightClasses = false;

    public KotlinSources(Project kotlinProject) {
        this.kotlinProject = kotlinProject;
    }

    public void lightClassesGenerated() {
        lightClasses = true;
    }

    public boolean hasLightClasses() {
        return lightClasses;
    }

    private void findSrc(FileObject fo, Collection<FileObject> files, KotlinProjectConstants type) {
        if (fo.isFolder()) {
            if (fo.getName().equals("resources")) {
                if (fo.getParent().getName().equals("test") ||
                        fo.getParent().getName().equals("main")) {
                    return;
                }
            }
            for (FileObject file : fo.getChildren()) {
                findSrc(file, files, type);
            }
        } else if (type != null) {
            switch (type) {
                case KOTLIN_SOURCE:
                    if (KotlinPsiManager.INSTANCE.isKotlinFile(fo)) {
                        files.add(fo.getParent());
                    }
                    break;
                case JAVA_SOURCE:
                    if (fo.hasExt("java")) {
                        files.add(fo.getParent());
                    }
                    break;
                case JAR:
                    if (fo.hasExt("jar")) {
                        if (!fo.getParent().getName().equals("build")) {
                            files.add(fo.getParent());
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public boolean isMavenModuledProject() {
        if (!(kotlinProject instanceof NbMavenProjectImpl)) {
            return false;
        }

        NbMavenProjectImpl mavenProject = (NbMavenProjectImpl) kotlinProject;

        List modules = mavenProject.getOriginalMavenProject().getModules();
        if (modules.isEmpty()) {
            return false;
        }

        return true;
    }

    @NotNull
    public List<FileObject> getSrcDirectories(KotlinProjectConstants type) {
        Set<FileObject> orderedFiles = Sets.newLinkedHashSet();

        FileObject srcDir = kotlinProject.getProjectDirectory().getFileObject("src");
        if (srcDir == null) {
            return Lists.newArrayList();
        }

        findSrc(srcDir, orderedFiles, type);
        return Lists.newArrayList(orderedFiles);

    }

    public List<FileObject> getAllKtFiles() {
        List<FileObject> srcDirs = getSrcDirectories(KotlinProjectConstants.KOTLIN_SOURCE);
        List<FileObject> ktFiles = new ArrayList<FileObject>();
        for (FileObject srcDir : srcDirs) {
            for (FileObject file : srcDir.getChildren()) {
                if (!file.hasExt("kt")) {
                    continue;
                }
                ktFiles.add(file);
            }
        }
        return ktFiles;
    }

    public SourceGroup[] getSourceGroups(String string) {
        List<SourceGroup> srcGroups = new ArrayList<SourceGroup>();
        if (string.equals(KotlinProjectConstants.JAR.toString())) {
            List<FileObject> src = getSrcDirectories(KotlinProjectConstants.JAR);
            for (FileObject srcFolder : src) {
                srcGroups.add(new KotlinSourceGroup(srcFolder));
            }
        } else if (string.equals(KotlinProjectConstants.JAVA_SOURCE.toString())) {
            List<FileObject> src = getSrcDirectories(KotlinProjectConstants.JAVA_SOURCE);
            for (FileObject srcFolder : src) {
                srcGroups.add(new KotlinSourceGroup(srcFolder));
            }
        } else if (string.equals(KotlinProjectConstants.KOTLIN_SOURCE.toString())) {
            List<FileObject> src = getSrcDirectories(KotlinProjectConstants.KOTLIN_SOURCE);
            for (FileObject srcFolder : src) {
                srcGroups.add(new KotlinSourceGroup(srcFolder));
            }
        }

        return srcGroups.toArray(new SourceGroup[srcGroups.size()]);
    }

    public SourceGroup getSourceGroupForFileObject(FileObject fo) {
        return new KotlinSourceGroup(fo);
    }

    public void addChangeListener(ChangeListener cl) {
    }

    public void removeChangeListener(ChangeListener cl) {
    }

    public class KotlinSourceGroup implements SourceGroup {

        private final FileObject root;

        public KotlinSourceGroup(FileObject root) {
            this.root = root;
        }

        @Override
        public FileObject getRootFolder() {
            return root;
        }

        @Override
        public String getName() {
            return getRootFolder().getPath();
        }

        @Override
        public String getDisplayName() {
            return getRootFolder().getName();
        }

        @Override
        public Icon getIcon(boolean bln) {
            return new ImageIcon("org/jetbrains/kotlin.png");
        }

        @Override
        public boolean contains(FileObject fo) {
            return fo.toURI().toString().startsWith(root.toURI().toString());
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pl) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pl) {
        }

    }

}
