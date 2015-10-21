package org.black.kotlin.utils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.black.kotlin.project.KotlinProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.load.kotlin.PackageClassUtils;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.psi.JetFile;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;


/**
 *
 * @author Александр
 */
public class ProjectUtils {
    
    private static final String LIB_FOLDER = "lib";
    private static final String LIB_EXTENSION = "jar";
    public static final String KT_HOME = System.getenv("KT_HOME") + "\\";
    
    public static String findMain(FileObject[] files) throws FileNotFoundException, IOException {
        for (FileObject file : files) {
            if (!file.isFolder()) {
                for (String line : file.asLines()) {
                    if (line.contains("fun main(")) {
                        return file.getPath();
                    }
                }
            } else {
                String main = findMain(file.getChildren());
                if (main != null) {
                    return main;
                }
            }
        }
        
        return null;
    }
    
    public static String getOutputDir(KotlinProject proj){
        
        File path = new File(proj.getProjectDirectory().getPath()+"/build");
        
        if (!path.exists()){
            path.mkdirs();
        }
                
        
        String dir = proj.getProjectDirectory().getPath()+"/build";
        String[] dirs = dir.split("/");
        StringBuilder outputDir = new StringBuilder("");
        
        for (String str : dirs){
            outputDir.append(str);
            outputDir.append("\\");
        }
        outputDir.append(proj.getProjectDirectory().getName()).append(".jar");
        
        return outputDir.toString();
    }

    private static void findSrc(FileObject fo, Collection<String> files){
        if (fo.isFolder()){
            for (FileObject file : fo.getChildren()){
                findSrc(file,files);
            }
        } 
        else {
            if (fo.hasExt("kt")){
                files.add(fo.getParent().getPath());
            }
        }
    }
    
    
    @NotNull
    public static List<String> getSrcDirectories(@NotNull KotlinProject javaProject){
        Set<String> orderedFiles = Sets.newLinkedHashSet();
        
        findSrc(javaProject.getProjectDirectory(), orderedFiles);
       
        
        return Lists.newArrayList(orderedFiles);
    }
    
    @NotNull
    public static List<String> getClasspath(){
        List<String> classpath = new ArrayList(KotlinClasspath.getKotlinClasspath());
        
//        for (String s: classpath){
//            DialogDisplayer.getDefault().notify(new NotifyDescriptor.
//                Message(s));
//        }
        
        return classpath;
    }
    
    @Nullable
    public static String getPackageByFile(FileObject file) {
//        JetFile jetFile = new JetFile(file,true);// = KotlinPsiManager.INSTANCE.getParsedFile(file);
//        
//        assert jetFile != null;
//        
//        return jetFile.getPackageFqName().asString();
        return "stab";
    }
    
    public static FqName createPackageClassName(FileObject file) {
        String filePackage = getPackageByFile(file);
        if (filePackage == null) {
            return null;
        }
        return PackageClassUtils.getPackageClassFqName(new FqName(filePackage));
    }
    
//    public static List<File> collectClasspathWithDependenciesForBuild(@NotNull Project javaProject){
//        return expandClasspath(javaProject, true, false, Predicates.<ClassPath.Entry>alwaysTrue());
//    }
    
//    @NotNull
//    private static List<File> expandClasspath(@NotNull Project javaProject, boolean includeDependencies,
//            boolean includeBinFolders, @NotNull Predicate<ClassPath.Entry> entryPredicate){
//        Set<File> orderedFiles = Sets.newLinkedHashSet();
//        
//        for (ClassPath.Entry classpathEntry : javaProject.getResolvedClasspath(true)) {
//            if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT && includeDependencies) {
//                orderedFiles.addAll(expandDependentProjectClasspath(classpathEntry, includeBinFolders, entryPredicate));
//            } else { // Source folder or library
//                if (entryPredicate.apply(classpathEntry)) {
//                    orderedFiles.addAll(getFileByEntry(classpathEntry, javaProject));
//                }
//            }
//        }
//        
//        return Lists.newArrayList(orderedFiles);
//    }
    
    public static String buildLibPath(String libName) {
        return KT_HOME + buildLibName(libName);
    }
    
    private static String buildLibName(String libName) {
        return LIB_FOLDER + "/" + libName + "." + LIB_EXTENSION;
    }
        
}
