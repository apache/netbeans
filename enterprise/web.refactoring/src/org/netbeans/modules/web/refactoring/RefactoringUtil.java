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
package org.netbeans.modules.web.refactoring;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Utility methods for refactoring operations.
 *
 * <i>TODO: need to introduce a common utility module for JPA/EJB/Web refactorings.</i>.
 *
 * @author Erno Mononen
 */
public class RefactoringUtil {
    
    private static final String JAVA_MIME_TYPE = "text/x-java"; //NO18N
    
    private RefactoringUtil() {
    }
    
    /**
     * Sets the given <code>toAdd</code> as the following problem for
     * the given <code>existing</code> problem.
     *
     * @param toAdd the problem to add, may be null.
     * @param existing the problem whose following problem should be set, may be null.
     *
     * @return the existing problem with its following problem
     * set to the given problem or null if both of the params
     * were null.
     *
     */
    public static Problem addToEnd(Problem toAdd, Problem existing){
        if (existing == null){
            return toAdd;
        }
        if (toAdd == null){
            return existing;
        }
        
        Problem tail = existing;
        while(tail.getNext() != null){
            tail = tail.getNext();
        }
        tail.setNext(toAdd);
        
        return tail;
    }
    
    // copied from o.n.m.java.refactoring.RetoucheUtils
    public static boolean isJavaFile(FileObject fileObject) {
        return JAVA_MIME_TYPE.equals(fileObject.getMIMEType()); //NOI18N
    }
    
    /**
     * Constructs a new fully qualified name for the given <code>newName</code>.
     *
     * @param originalFullyQualifiedName the old fully qualified name of the class.
     * @param newName the new unqualified name of the class.
     *
     * @return the new fully qualified name of the class.
     */
    public static String renameClass(String originalFullyQualifiedName, String newName){
        Parameters.notEmpty("originalFullyQualifiedName", originalFullyQualifiedName); //NO18N
        Parameters.notEmpty("newName", newName); //NO18N
        int lastDot = originalFullyQualifiedName.lastIndexOf('.');
        return (lastDot <= 0) ? newName : originalFullyQualifiedName.substring(0, lastDot + 1) + newName;
    }
    
    
    // copied from o.n.m.java.refactoring.RetoucheUtils
    public static boolean isOnSourceClasspath(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p==null) return false;
        if (OpenProjects.getDefault().isProjectOpen(p)) {
            SourceGroup[] gr = ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int j = 0; j < gr.length; j++) {
                if (fo==gr[j].getRootFolder()) return true;
                if (FileUtil.isParentOf(gr[j].getRootFolder(), fo))
                    return true;
            }
            return false;
        }
        return false;
    }
    
    
    /**
     * Recursively collects the java files from the given folder into the
     * given <code>result</code>.
     */
    public static void collectChildren(FileObject folder, List<FileObject> result){
        for(FileObject child : folder.getChildren()){
            if (isJavaFile(child)){
                result.add(child);
            } else if (child.isFolder()){
                collectChildren(child, result);
            }
        }
    }
    
    /**
     * @return true if the given refactoring represents a package rename.
     */
    public static boolean isPackage(RenameRefactoring rename){
        return rename.getRefactoringSource().lookup(NonRecursiveFolder.class) != null;
    }

    /**
     * Gets the new refactored name for the given <code>javaFile</code>.
     *
     * @param javaFile the file object for the class being renamed. Excepts that
     * the target class is the public top level class in the file.
     * @param rename the refactoring, must represent either package or folder rename.
     *
     * @return the new fully qualified name for the class being refactored.
     */
    public static String constructNewName(FileObject javaFile, RenameRefactoring rename){
        final String fqn = JavaIdentifiers.getQualifiedName(javaFile);
        
        if (isPackage(rename)){
            return rename.getNewName() + "." + JavaIdentifiers.unqualify(fqn);
        }
        
        final FileObject folder = rename.getRefactoringSource().lookup(FileObject.class);
        final ClassPath classPath = ClassPath.getClassPath(folder, ClassPath.SOURCE);
        if (classPath == null) {
            return "";
        }
        
        final FileObject root = classPath.findOwnerRoot(folder);
        final String prefix = FileUtil.getRelativePath(root, folder.getParent()).replace('/','.');
        final String oldName = buildName(prefix, folder.getName());
        final String newName = buildName(prefix, rename.getNewName());
        final int oldNameIndex = fqn.lastIndexOf(oldName) + oldName.length();

        return newName + fqn.substring(oldNameIndex);
        
    }
    
    private static String buildName(String prefix, String name){
        if (prefix.length() == 0){
            return name;
        }
        return prefix + "." + name;
    }
    
    // copied from o.n.m.java.refactoring.RetoucheUtils
    public static String getPackageName(URL url) {
        File f = null;
        try {
            f = FileUtil.normalizeFile(new File(url.toURI()));
        } catch (URISyntaxException uRISyntaxException) {
            throw new IllegalArgumentException("Cannot create package name for url " + url);
        }
        String suffix = "";
        
        do {
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null) {
                if ("".equals(suffix))
                    return getPackageName(fo);
                String prefix = getPackageName(fo);
                return prefix + ("".equals(prefix)?"":".") + suffix;
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix;
            }
            suffix = URLDecoder.decode(f.getPath().substring(f.getPath().lastIndexOf(File.separatorChar)+1)) + suffix;
            f = f.getParentFile();
        } while (f!=null);
        throw new IllegalArgumentException("Cannot create package name for url " + url);
    }
    
    // copied from o.n.m.java.refactoring.RetoucheUtils
    private static String getPackageName(FileObject folder) {
        assert folder.isFolder() : "argument must be folder";
        return ClassPath.getClassPath(
                folder, ClassPath.SOURCE)
                .getResourceName(folder, '.', false);
    }
    

    /**
     * Gets the fully qualified names of the classes that are being refactored 
     * by the given <code>refactoring</code>.
     * @return the fully qualified names of the classes being refactored, never null.
     */ 
    public static List<String> getRefactoredClasses(AbstractRefactoring refactoring) {
        // XXX the whole method is not very performant 
        Collection<TreePathHandle> tphs = new HashSet<TreePathHandle>();
        tphs.addAll(refactoring.getRefactoringSource().lookupAll(TreePathHandle.class));
        if (tphs.isEmpty()) {
            //XXX handles are there for safe delete, but not for move
            Collection<? extends FileObject> fos = refactoring.getRefactoringSource().lookupAll(FileObject.class);
            for (FileObject each : fos){
                TreePathHandle handle = resolveHandle(each);
                if (handle != null){
                    tphs.add(handle);
                }
            }
        }

        final List<String> result = new ArrayList<String>();
        for (final TreePathHandle handle : tphs) {
            JavaSource source = JavaSource.forFileObject(handle.getFileObject());
            try {
                source.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController parameter) throws Exception {
                        parameter.toPhase(JavaSource.Phase.RESOLVED);
                        Element element = handle.resolveElement(parameter);

                        result.add(getTypeName( element , parameter ));
                    }
                }, true);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return result;
    }
    
    /*
     * Fix for BZ#176088 - IllegalArgumentException:
     */
    private static String getTypeName( Element element , 
            CompilationController controller )
    {
        if ( element instanceof TypeElement ){
            // if the class is a generic then the <...> will also be
            // returned.  We just want the class name.
            // Accurate fix for IZ#156699 - IllegalArgumentException: The given fqn [pencom.webclient.notification.WebControlAppearance<V>] does not represent a fully qualified class name
            try { // just for sure ( javadoc for "erasure" is not absolutely obvious  
                return controller.getTypes().erasure(element.asType()).toString();
            }
            catch( IllegalArgumentException e){
                // it should never happen, but if unknown case appears it will be handled here
                return element.asType().toString();
            }
        }
        else {
            return element.asType().toString();
        }
    }
    
    private static TreePathHandle resolveHandle(FileObject fileObject){
        final TreePathHandle[] result = new TreePathHandle[1];
        if (!isJavaFile(fileObject)){
            return null;
        }
        JavaSource source = JavaSource.forFileObject(fileObject);
        try {
            source.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController co) throws Exception {
                    co.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cut = co.getCompilationUnit();
                    if (cut.getTypeDecls().isEmpty()) {
                        return;
                    }
                    result[0] = TreePathHandle.create(TreePath.getPath(cut, cut.getTypeDecls().get(0)), co);
                }
            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return result[0];

    }
    
    /**
     * @return true if the given <code>fo</code> represents
     * a <code>package-info.java<code> file; false otherwise.
     */
    public static boolean isPackageInfo(FileObject fo) {
        return "package-info.java".equals(fo.getNameExt()); //NOI18N
    }

}
