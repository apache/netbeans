package org.black.kotlin.resolve.lang.kotlin;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.resolve.lang.java.NetBeansJavaProjectElementFinder;
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
import org.netbeans.api.java.classpath.GlobalPathRegistry;
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
        TypeElement type = NetBeansJavaProjectElementFinder.findElement(project,
                classId.asSingleFqName().toString());//not sure
        if (type == null || !isBinaryKotlinClass(type)){
            return null;
        }
        FileObject resource = GlobalPathRegistry.getDefault().findResource(
                type.getQualifiedName().toString().replace(".", System.getProperty("file.separator"))+".class");//not sure at the moment
        String path;
        if (resource != null){
            path = resource.getPath();
        } else {
            path = type.getQualifiedName().toString().replace(".", System.getProperty("file.separator"));// not sure again
        }
        
        if (isClassFileName(path)){
            return KotlinEnvironment.getEnvironment(project).getVirtualFile(path);
        }
        else if (KotlinEnvironment.getEnvironment(project).isJarFile(path)){
            String relativePath = type.getQualifiedName().toString().
                    replace(".", System.getProperty("file.separator"))+".class";
            return KotlinEnvironment.getEnvironment(project).getVirtualFileInJar(path, relativePath);
        } else{
            throw new IllegalArgumentException("Virtual file not found for "+path);
        }
    }

    @Override
    public JvmVirtualFileFinder create(GlobalSearchScope gss) {
        return new NetBeansVirtualFileFinder(project); 
    }

    private boolean isBinaryKotlinClass(TypeElement type){
        return false;//stub
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
            file = file.getParent().findChild(classFileName(javaClass)+".class");
            if (file != null){
                throw new IllegalStateException("Virtual file not found");
            }
        }
        
        return KotlinBinaryClassCache.Companion.getKotlinBinaryClass(file, null);
    }
    
}
