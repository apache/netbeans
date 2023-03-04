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

package org.netbeans.modules.web.jsf.refactoring;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
/**
 *
 * @author Petr Pisl
 */
public class JSFRefactoringUtils {

    private static final Logger LOGGER = Logger.getLogger(JSFRefactoringUtils.class.getName());
    
    private JSFRefactoringUtils() {
    }
    
    //TODO this is copy from org.netbeans.modules.refactoring.java.RetoucheUtils
    //Probably this methods will be moved to an api 
    public static String getPackageName(FileObject folder) {
        assert folder.isFolder() : "argument must be folder";  //NOI18N
        return ClassPath.getClassPath(
                folder, ClassPath.SOURCE)
                .getResourceName(folder, '.', false);
    }
    
    //TODO this is copy from org.netbeans.modules.refactoring.java.RetoucheUtils
    //Probably this methods will be moved to an api 
    public static String getPackageName(URL url) {
        File file = null;
        try {
            file = FileUtil.normalizeFile(Utilities.toFile(url.toURI()));
        } catch (URISyntaxException uRISyntaxException) {
            throw new IllegalArgumentException("Cannot create package name for url " + url);  //NOI18N
        }
        String suffix = "";
        
        do {
            FileObject fileObject = FileUtil.toFileObject(file);
            if (fileObject != null) {
                if ("".equals(suffix))
                    return getPackageName(fileObject);
                String prefix = getPackageName(fileObject);
                return prefix + ("".equals(prefix)?"":".") + suffix;
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix;
            }
            suffix = URLDecoder.decode(file.getPath().substring(file.getPath().lastIndexOf(File.separatorChar)+1)) + suffix;
            file = file.getParentFile();
        } while (file!=null);
        throw new IllegalArgumentException("Cannot create package name for url " + url);  //NOI18N
    }

    
    public static boolean containsRenamingPackage(String oldFQCN, String oldPackage, boolean renameSubpackages){
        boolean contains = false;
        if (oldFQCN != null && oldPackage != null) {
            if (!renameSubpackages){
                if (oldFQCN.startsWith(oldPackage)
                        && oldFQCN.substring(oldPackage.length()+1).indexOf('.') < 0
                        && oldFQCN.substring(oldPackage.length()).charAt(0) == '.'){
                    contains = true;
                }
            }
            else {
                if (oldFQCN.startsWith(oldPackage) 
                        && oldFQCN.substring(oldPackage.length()).charAt(0) == '.'){
                    contains = true;
                }
            }
        }
        return contains;
    }
    
    public static void renamePackage(AbstractRefactoring refactoring, RefactoringElementsBag refactoringElements, 
            FileObject folder, String oldFQPN, String newFQPN, boolean recursive){
        Project project = FileOwnerQuery.getOwner(folder);
        if (project != null) {
            List <Occurrences.OccurrenceItem> items = Occurrences.getPackageOccurrences(project, oldFQPN, newFQPN, recursive);
            Modifications modification = new Modifications();
            for (Occurrences.OccurrenceItem item : items) {
                Modifications.Difference difference = new Modifications.Difference(
                                Modifications.Difference.Kind.CHANGE, item.getChangePosition().getBegin(),
                                item.getChangePosition().getEnd(), item.getOldValue(), item.getNewValue(), item.getRenamePackageMessage());
                modification.addDifference(item.getFacesConfig(), difference);
                refactoringElements.add(refactoring, new DiffElement.ChangeFQCNElement(difference, item, modification));
            }
        }
    }

    private static final String JAVA_MIME_TYPE = "text/x-java"; //NOI18N

    public static boolean isJavaFile(FileObject f) {
        return JAVA_MIME_TYPE.equals(f.getMIMEType()); 
    }
    
    public static Element resolveElement(final ClasspathInfo cpInfo, final AbstractRefactoring refactoring, final TreePathHandle treePathHandle) {
        final Element[] element = new Element[1];
        JavaSource source = JavaSource.create(cpInfo, new FileObject[]{treePathHandle.getFileObject()});
        try {
            source.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController info) throws Exception {
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    element[0] = treePathHandle.resolveElement(info);
                }
            }, true);
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Exception by refactoring:", exception); //NOI18NN
        }

        return element[0];
    }
}
