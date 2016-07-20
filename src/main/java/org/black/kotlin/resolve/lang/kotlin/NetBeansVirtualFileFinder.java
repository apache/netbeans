package org.black.kotlin.resolve.lang.kotlin;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import javax.lang.model.element.TypeElement;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.projectsextensions.ClassPathExtender;
import org.black.kotlin.projectsextensions.KotlinProjectHelper;
import org.black.kotlin.resolve.lang.java.NetBeansJavaClassFinder;
import org.black.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
import org.black.kotlin.resolve.lang.java.structure.NetBeansJavaClassifier;
import org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementUtil;
import org.jetbrains.kotlin.load.kotlin.JvmVirtualFileFinder;
import org.jetbrains.kotlin.load.kotlin.VirtualFileKotlinClassFinder;
import org.jetbrains.kotlin.load.kotlin.JvmVirtualFileFinderFactory;
import org.jetbrains.kotlin.name.ClassId;
import org.netbeans.api.project.Project;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.kotlin.KotlinBinaryClassCache;
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryClass;
import org.jetbrains.kotlin.name.FqName;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Александр
 */
public class NetBeansVirtualFileFinder extends VirtualFileKotlinClassFinder implements JvmVirtualFileFinderFactory {

    Project project;
    
    public NetBeansVirtualFileFinder(Project project){
        this.project = project;
    }
    
    private boolean isClassFileName(String name){
        char[] suffixClass = ".class".toCharArray();
        char[] suffixClass2 = ".CLASS".toCharArray();
        int nameLength = name == null ? 0 : name.length();
        int suffixLength = suffixClass.length;
        if (nameLength < suffixLength) return false;
        
        for (int i = 0; i < suffixLength; i++){
            char c = name.charAt(nameLength - i - 1);
            int suffixIndex = suffixLength - i - 1;
            if (c != suffixClass[suffixIndex] && c != suffixClass2[suffixIndex]){
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public VirtualFile findVirtualFileWithHeader(ClassId classId) {
        TypeElement type = NetBeansJavaProjectElementUtils.findTypeElement(project,
                classId.asSingleFqName().asString());
        if (type == null || !isBinaryKotlinClass(type)){
            return null;
        }
        
        ClassPathExtender classpath = KotlinProjectHelper.INSTANCE.getExtendedClassPath(project);
        ClassPath boot = classpath.getProjectSourcesClassPath(ClassPath.BOOT);
        ClassPath compile = classpath.getProjectSourcesClassPath(ClassPath.COMPILE);
        ClassPath source = classpath.getProjectSourcesClassPath(ClassPath.SOURCE);
        
        ClassPath proxy = ClassPathSupport.createProxyClassPath(boot, compile, source);
        
        String rPath = type.getQualifiedName().
                toString().replace(".", "/")+".class";
        
        FileObject resource = proxy.findResource(rPath);
        
        String path;
        if (resource != null){
            path = resource.toURL().getPath();
        } else {
            path = rPath;
        }
        
        if (path.split("!/").length == 2){
            String pathToJar = path.split("!/")[0].replace("file:/", "");
            String classFile = path.split("!/")[1];
            return KotlinEnvironment.getEnvironment(project).getVirtualFileInJar(pathToJar, classFile);
        } else if (isClassFileName(path)){
            return KotlinEnvironment.getEnvironment(project).getVirtualFile(path);
        } else{
            throw new IllegalArgumentException("Virtual file not found for "+path);
        }
    }

    @Override
    public JvmVirtualFileFinder create(GlobalSearchScope gss) {
        return new NetBeansVirtualFileFinder(project); 
    }

    private boolean isBinaryKotlinClass(TypeElement type){
        return !NetBeansJavaClassFinder.isInKotlinBinFolder(type);
    }
    
    private String classFileName(JavaClass jClass){
        JavaClass outerClass = jClass.getOuterClass();
        if (outerClass == null)
            return jClass.getName().asString();
        return classFileName(outerClass) + "$" + jClass.getName().asString();
    }
    
    @Override
    public KotlinJvmBinaryClass findKotlinClass(JavaClass javaClass){
        FqName fqName = javaClass.getFqName();
        if (fqName == null){
            return null;
        }
        
        
        ClassId classId = NetBeansJavaElementUtil.computeClassId(
                (TypeElement)((NetBeansJavaClassifier)javaClass).getBinding());
        if (classId == null){
            return null;
        }
        
        VirtualFile file = findVirtualFileWithHeader(classId);
        if (file == null){
            return null;
        }
        
        if (javaClass.getOuterClass() != null){
            String classFileName = classFileName(javaClass)+".class";
            file = file.getParent().findChild(classFileName);
            if (file != null){
                throw new IllegalStateException("Virtual file not found");
            }
        }
        
        return KotlinBinaryClassCache.Companion.getKotlinBinaryClass(file, null);
    }
    
}
