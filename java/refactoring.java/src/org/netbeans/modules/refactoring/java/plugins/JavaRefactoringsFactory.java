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

package org.netbeans.modules.refactoring.java.plugins;

import java.util.Collection;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.DocTreePathHandle;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.*;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldsRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class, position=100)
public class JavaRefactoringsFactory implements RefactoringPluginFactory {
   
    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        Lookup look = refactoring.getRefactoringSource();
        FileObject file = look.lookup(FileObject.class);
        NonRecursiveFolder folder = look.lookup(NonRecursiveFolder.class);
        TreePathHandle handle = look.lookup(TreePathHandle.class);
        DocTreePathHandle docHandle = look.lookup(DocTreePathHandle.class);
        if (refactoring instanceof WhereUsedQuery) {
            if (handle!=null) {
                return new JavaWhereUsedQueryPlugin((WhereUsedQuery) refactoring);
            }
        } else if (refactoring instanceof RenameRefactoring) {
            if (handle!=null || docHandle!=null || ((file!=null) && RefactoringUtils.isJavaFile(file))) {
                //rename java file, class, method etc..
                return new RenameRefactoringPlugin((RenameRefactoring)refactoring);
            } else if (file!=null && JavaRefactoringUtils.isOnSourceClasspath(file) && file.isFolder()) {
                //rename folder
                return new MoveFileRefactoringPlugin((RenameRefactoring)refactoring);
            } else if (folder!=null && JavaRefactoringUtils.isOnSourceClasspath(folder.getFolder())) {
                //rename package
                return new MoveFileRefactoringPlugin((RenameRefactoring)refactoring);
            }
        } else if (refactoring instanceof SafeDeleteRefactoring) {
            //TODO: should be implemented better
            if (checkSafeDelete(refactoring.getRefactoringSource())) {
                return new SafeDeleteRefactoringPlugin((SafeDeleteRefactoring)refactoring);
            }
        } else if (refactoring instanceof MoveRefactoring) {
            if (checkMove(refactoring.getRefactoringSource())) {
                return new MoveFileRefactoringPlugin((MoveRefactoring) refactoring);
            } else if (checkMoveMembers(refactoring.getContext())) {
                return new MoveMembersRefactoringPlugin((MoveRefactoring) refactoring);
            }
        } else if (refactoring instanceof SingleCopyRefactoring) {
            if (checkCopy(refactoring.getRefactoringSource())) {
                return new CopyClassRefactoringPlugin((SingleCopyRefactoring) refactoring);
            }
        } else if (refactoring instanceof CopyRefactoring) {
            if (checkCopy(refactoring.getRefactoringSource())) {
                return new CopyClassesRefactoringPlugin((CopyRefactoring) refactoring);
            }
        } else if (handle!=null) {
            if (refactoring instanceof ExtractInterfaceRefactoring) {
                return new ExtractInterfaceRefactoringPlugin((ExtractInterfaceRefactoring) refactoring);
            } else if (refactoring instanceof ExtractSuperclassRefactoring) {
                return new ExtractSuperclassRefactoringPlugin((ExtractSuperclassRefactoring) refactoring);
            } else if (refactoring instanceof IntroduceLocalExtensionRefactoring) {
                return new IntroduceLocalExtensionPlugin((IntroduceLocalExtensionRefactoring) refactoring);
            } else if (refactoring instanceof PullUpRefactoring) {
                return new PullUpRefactoringPlugin((PullUpRefactoring)refactoring);
            } else if (refactoring instanceof PushDownRefactoring) {
                return new PushDownRefactoringPlugin((PushDownRefactoring) refactoring);
            } else if (refactoring instanceof UseSuperTypeRefactoring) {
                return new UseSuperTypeRefactoringPlugin((UseSuperTypeRefactoring) refactoring);
            } else if (refactoring instanceof InnerToOuterRefactoring) {
                return new InnerToOuterRefactoringPlugin((InnerToOuterRefactoring) refactoring);
            } else if (refactoring instanceof ChangeParametersRefactoring) {
                return new ChangeParametersPlugin((ChangeParametersRefactoring) refactoring);
            } else if (refactoring instanceof IntroduceParameterRefactoring) {
                return new IntroduceParameterPlugin((IntroduceParameterRefactoring) refactoring);
            } else if (refactoring instanceof EncapsulateFieldRefactoring) {
                return new EncapsulateFieldRefactoringPlugin((EncapsulateFieldRefactoring) refactoring);
            } else if (refactoring instanceof EncapsulateFieldsRefactoring) {
                return new EncapsulateFieldsPlugin((EncapsulateFieldsRefactoring) refactoring);
            } else if (refactoring instanceof InlineRefactoring) {
                return new InlineRefactoringPlugin((InlineRefactoring) refactoring);
            } else if (refactoring instanceof ReplaceConstructorWithFactoryRefactoring) {
                return new ReplaceConstructorWithFactoryPlugin((ReplaceConstructorWithFactoryRefactoring) refactoring);
            } else if (refactoring instanceof InvertBooleanRefactoring) {
                return new InvertBooleanRefactoringPlugin((InvertBooleanRefactoring) refactoring);
            } else if (refactoring instanceof ReplaceConstructorWithBuilderRefactoring) {
                return new ReplaceConstructorWithBuilderPlugin((ReplaceConstructorWithBuilderRefactoring) refactoring);
            }
        }
        return null;
    }

    private boolean checkMove(Lookup refactoringSource) {
        for (FileObject f:refactoringSource.lookupAll(FileObject.class)) {
            if (RefactoringUtils.isJavaFile(f)) {
                return true;
            }
            if (f.isFolder()) {
                return true;
            }
        }
        Collection<? extends TreePathHandle> tphs = refactoringSource.lookupAll(TreePathHandle.class);
        if(tphs.size() == 1) {
            ElementHandle elementHandle = tphs.iterator().next().getElementHandle();
            if(elementHandle != null &&
                    (elementHandle.getKind().isClass() ||
                     elementHandle.getKind().isInterface())) {
                return true;
            }
        }
        return false;
    }

    //TODO: should be implemented better
    private boolean checkSafeDelete(Lookup object) {
        boolean a=false;
        NonRecursiveFolder folder = object.lookup(NonRecursiveFolder.class);
        if (folder != null){
            return true;
        }
        for (FileObject f:object.lookupAll(FileObject.class)) {
            a=true;
            if (!f.isValid()) {
                return false;
            }
            if (!RefactoringUtils.isJavaFile(f) && !isPackage(f)) {
                return false;
            }
        }
        if (object.lookup(TreePathHandle.class)!=null) {
            return true;
        }
        
        return a;
    }
    
    private boolean checkCopy(Lookup object) {
        Collection<? extends FileObject> fileObjects = object.lookupAll(FileObject.class);
        for (FileObject f : fileObjects) {
            if (f != null && RefactoringUtils.isJavaFile(f)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPackage(FileObject fileObject) {
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fileObject);

        } catch (DataObjectNotFoundException dataObjectNotFoundException) {
            ErrorManager.getDefault().notify(dataObjectNotFoundException);
            return false;
        }
        if ((dataObject instanceof DataFolder) && 
                RefactoringUtils.isFileInOpenProject(fileObject) && 
                JavaRefactoringUtils.isOnSourceClasspath(fileObject) &&
                !RefactoringUtils.isClasspathRoot(fileObject)){
            return true;
        }
        return false;
    }

    private boolean checkMoveMembers(Context context) {
        return context.lookup(JavaMoveMembersProperties.class) != null;
    }
}
