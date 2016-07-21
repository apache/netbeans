package org.black.kotlin.project;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.util.containers.HashSet;
import edu.emory.mathcs.backport.java.util.Collections;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import org.black.kotlin.builder.KotlinPsiManager;
import org.black.kotlin.projectsextensions.maven.MavenHelper;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.openide.filesystems.FileObject;

/**
 *
 * @author РђР»РµРєСЃР°РЅРґСЂ
 */
/**
     * This class defines the location of Kotlin sources.
     */
    public final class KotlinSources {

        private final Project kotlinProject;
        
        public KotlinSources(Project kotlinProject){
            this.kotlinProject = kotlinProject;
        }
        
        private void findSrc(FileObject fo, Collection<FileObject> files, KotlinProjectConstants type) {
            if (fo.isFolder()) {
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
            if (!(kotlinProject instanceof NbMavenProjectImpl)){
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
        private Set<FileObject> getSrcDirectoriesOfMavenModules(KotlinProjectConstants type) {
            Set<FileObject> src = new LinkedHashSet<FileObject>();
            
            for (FileObject file : MavenHelper.getAllChildrenSrcDirectoriesOfProject((NbMavenProjectImpl) kotlinProject)) {
                findSrc(file, src, type);
            }
            
            return src;
        }
        
        @NotNull
        public List<FileObject> getSrcDirectories(KotlinProjectConstants type) {
            Set<FileObject> orderedFiles = Sets.newLinkedHashSet();
            
            FileObject srcDir = kotlinProject.getProjectDirectory().getFileObject("src");
            if (srcDir == null) {
                if (!isMavenModuledProject()){
                    return Lists.newArrayList();
                } else {
                    return Lists.newArrayList(getSrcDirectoriesOfMavenModules(type));
                }
            }
            
            findSrc(srcDir, orderedFiles, type);
            return Lists.newArrayList(orderedFiles);

        }

        public List<FileObject> getAllKtFiles(){
            List<FileObject> srcDirs = getSrcDirectories(KotlinProjectConstants.KOTLIN_SOURCE);
            List<FileObject> ktFiles = new ArrayList<FileObject>();
            for (FileObject srcDir : srcDirs){
                for (FileObject file : srcDir.getChildren()){
                    if (!file.hasExt("kt")){
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

        public SourceGroup getSourceGroupForFileObject(FileObject fo){
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
                return new ImageIcon("org/black/kotlin.png");
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
