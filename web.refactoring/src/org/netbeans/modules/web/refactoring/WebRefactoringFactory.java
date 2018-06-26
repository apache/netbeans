/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.refactoring;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.refactoring.rename.TldFolderMove;
import org.netbeans.modules.web.refactoring.rename.TldMove;
import org.netbeans.modules.web.refactoring.rename.TldPackageRename;
import org.netbeans.modules.web.refactoring.rename.TldRename;
import org.netbeans.modules.web.refactoring.rename.WebXmlFolderMove;
import org.netbeans.modules.web.refactoring.rename.WebXmlMove;
import org.netbeans.modules.web.refactoring.rename.WebXmlPackageRename;
import org.netbeans.modules.web.refactoring.rename.WebXmlRename;
import org.netbeans.modules.web.refactoring.safedelete.TldSafeDelete;
import org.netbeans.modules.web.refactoring.safedelete.WebXmlSafeDelete;
import org.netbeans.modules.web.refactoring.whereused.TldWhereUsed;
import org.netbeans.modules.web.refactoring.whereused.WebXmlWhereUsed;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * A refactoring factory for Web related refactorings.
 *
 * @author Erno Mononen
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class)
public class WebRefactoringFactory implements RefactoringPluginFactory{
    
    private static final Logger LOGGER = Logger.getLogger(WebRefactoringFactory.class.getName());
    
    public WebRefactoringFactory() {
    }
    
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        
        NonRecursiveFolder pkg = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
        FileObject sourceFO = refactoring.getRefactoringSource().lookup(FileObject.class);
        TreePathHandle handle = resolveTreePathHandle(refactoring);
        
        boolean javaPackage = pkg != null && RefactoringUtil.isOnSourceClasspath(pkg.getFolder());
        boolean folder = sourceFO != null && sourceFO.isFolder();
        
        if (sourceFO == null){
            if (handle != null){
                sourceFO = handle.getFileObject();
            } else if (pkg != null){
                sourceFO = pkg.getFolder();
            }
        }
        
        if (sourceFO == null){
            return null;
        }

        boolean javaFile = sourceFO != null && RefactoringUtil.isJavaFile(sourceFO);

        WebModule wm = WebModule.getWebModule(sourceFO);
        if (wm == null){
            return null;
        }
        FileObject ddFile = wm.getDeploymentDescriptor();

        String clazz = resolveClass(handle);
        
        // if we have a java file, the class name should be resolvable
        // unless it is an empty java file - see #130933
        if (javaFile && clazz == null) {
            LOGGER.fine("Could not resolve the class for: " + sourceFO + ", possibly an empty Java file");
            return null;
        }
        
        List<WebRefactoring> refactorings = new ArrayList<WebRefactoring>();
        
        if (refactoring instanceof RenameRefactoring){
            RenameRefactoring rename = (RenameRefactoring) refactoring;
            if (javaPackage || folder){
                if (ddFile != null) {
                    refactorings.add(new WebXmlPackageRename(ddFile, sourceFO, rename));
                }
                refactorings.add(new TldPackageRename(rename, wm, sourceFO));
            } else if (javaFile) {
                if (ddFile != null) {
                    refactorings.add(new WebXmlRename(clazz, rename, ddFile));
                }
                refactorings.add(new TldRename(clazz, rename, wm));
            }
        } 
        
        if (refactoring instanceof WhereUsedQuery && javaFile){
            WhereUsedQuery whereUsedQuery = (WhereUsedQuery) refactoring;
            if (ddFile != null) {
                refactorings.add(new WebXmlWhereUsed(ddFile, clazz, whereUsedQuery));
            }
            refactorings.add(new TldWhereUsed(clazz, wm, whereUsedQuery));
        } 
        
        if (refactoring instanceof SafeDeleteRefactoring && javaFile){
            SafeDeleteRefactoring safeDelete = (SafeDeleteRefactoring) refactoring;
            if (ddFile != null) {
                refactorings.add(new WebXmlSafeDelete(ddFile, safeDelete));
            }
            refactorings.add(new TldSafeDelete(safeDelete, wm));
        }
        
        if (refactoring instanceof MoveRefactoring){
            MoveRefactoring move = (MoveRefactoring) refactoring;
            if (javaFile){
                if (ddFile != null) {
                    refactorings.add(new WebXmlMove(ddFile, move));
                }
                refactorings.add(new TldMove(move, wm));
            } else if (folder){
                if (ddFile != null) {
                    refactorings.add(new WebXmlFolderMove(ddFile, sourceFO, move));
                }
                refactorings.add(new TldFolderMove(wm, sourceFO, move));
            }
        }
        
        return refactorings.isEmpty() ? null : new WebRefactoringPlugin(refactorings);
    }
    
    private TreePathHandle resolveTreePathHandle(final AbstractRefactoring refactoring){
        
        TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (tph != null) {
            return tph;
        }
        
        FileObject sourceFO = refactoring.getRefactoringSource().lookup(FileObject.class);
        if (sourceFO == null || !RefactoringUtil.isJavaFile(sourceFO)){
            return null;
        }
        final TreePathHandle[] result = new TreePathHandle[1];
        try{
            
            JavaSource source = JavaSource.forFileObject(sourceFO);
            
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                public void cancel() {
                }
                public void run(CompilationController co) throws Exception {
                    co.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cut = co.getCompilationUnit();
                    if (cut.getTypeDecls().isEmpty()){
                        return;
                    }
                    result[0] = TreePathHandle.create(TreePath.getPath(cut, cut.getTypeDecls().get(0)), co);
                }
                
            }, true);
        }catch(IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        
        return result[0];
    }
    
    
    /**
     * @return the fully qualified name of the class that the given
     * TreePathHandle represents or null if the FQN could not be resolved.
     */
    private String resolveClass(final TreePathHandle treePathHandle){
        if(treePathHandle == null){
            return null;
        }
        
        final String[] result = new String[1];
        
        try{
            JavaSource source = JavaSource.forFileObject(treePathHandle.getFileObject());
            if (source == null) {
                return null;
            }
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void cancel() {
                }
                
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(JavaSource.Phase.RESOLVED);
                    Element element = treePathHandle.resolveElement(parameter);
                    // Fix for IZ159330 - NullPointerException at org.netbeans.modules.web.refactoring.WebRefactoringFactory$2.run
                    if ( element == null ){
                        result[0] = null;
                    }
                    else {
                        result[0] = element.asType().toString();
                    }
                }
            }, true);
        }catch(IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        return result[0];
    }
    
    
}
