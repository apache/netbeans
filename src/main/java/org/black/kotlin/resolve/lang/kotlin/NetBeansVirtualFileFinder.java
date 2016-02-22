package org.black.kotlin.resolve.lang.kotlin;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.kotlin.load.kotlin.JvmVirtualFileFinder;
import org.jetbrains.kotlin.load.kotlin.VirtualFileKotlinClassFinder;
import org.jetbrains.kotlin.load.kotlin.JvmVirtualFileFinderFactory;
import org.jetbrains.kotlin.name.ClassId;
import org.netbeans.api.project.Project;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryClass;
import org.jetbrains.kotlin.name.FqName;

/**
 *
 * @author Александр
 */
public class NetBeansVirtualFileFinder extends VirtualFileKotlinClassFinder implements JvmVirtualFileFinderFactory {

    Project project;
    
    public NetBeansVirtualFileFinder(Project project){
        this.project = project;
    }
    
    @Override
    public VirtualFile findVirtualFileWithHeader(ClassId ci) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JvmVirtualFileFinder create(GlobalSearchScope gss) {
        return this; 
    }

    private boolean isBinaryKotlinClass(){
        return true;//stub
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
        if (fqName == null)
            return null;
        //TODO 
        return null;
    }
    
}
