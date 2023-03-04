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

package org.netbeans.modules.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

@NbBundle.Messages({
    "JavaResolver.Name=Java Files",
    "JavaResolver.FileChooserName=Java Files"
})
@MIMEResolver.ExtensionRegistration(
    position=100,
    displayName="#JavaResolver.Name",
    extension="java",
    mimeType="text/x-java",
    showInFileChooser={"#JavaResolver.FileChooserName"}
)
public final class JavaDataObject extends MultiDataObject {
    
    public JavaDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        registerEditor("text/x-java", true);
    }

    public @Override Node createNodeDelegate() {
        return JavaDataSupport.createJavaNode(getPrimaryFile());
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    protected DataObject handleCopyRename(DataFolder df, String name, String ext) throws IOException {
        FileObject fo = getPrimaryEntry ().copyRename (df.getPrimaryFile (), name, ext);
        DataObject dob = DataObject.find( fo );
        //TODO invoke refactoring here (if needed)
        return dob;
    }
    
    @MultiViewElement.Registration(
        displayName="#CTL_SourceTabCaption",
        iconBase="org/netbeans/modules/java/resources/class.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="java.source",
        mimeType="text/x-java",
        position=2000
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @Override
    protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
        if (name == null) {
            return super.handleCreateFromTemplate(df, name);
        }
        String[] packageAndName = name.split("\\.");
        if (packageAndName.length > 1) {
            verifyJavaNames(packageAndName);
            FileObject f = df.getPrimaryFile();
            for (int i = 0; i < packageAndName.length - 1; i++) {
                f = FileUtil.createFolder(f, packageAndName[i]);
            }
            return super.handleCreateFromTemplate(
                DataFolder.findFolder(f),
                packageAndName[packageAndName.length - 1]
            );
        } else {
            if (!getName().equals(name)) {
                verifyJavaNames(name);
            }
            return super.handleCreateFromTemplate(df, name);
        }
    }
    
    /**
     * XXX: Change this when there will be a write model.
     * When there will be a refactoring it shoud be called only in case of handleCreateFromTemplate
     */    
    static void renameFO(final FileObject fileToUpdate, 
            final String packageName, 
            final String newName, 
            final String originalName) throws IOException 
    {
        JavaSource javaSource = JavaSource.forFileObject (fileToUpdate);
        if (javaSource == null) {
            return;
        }

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree compilationUnitTree = workingCopy.getCompilationUnit();
                // change the package when file was move to different dir.
                CompilationUnitTree cutCopy = make.CompilationUnit(
                        compilationUnitTree.getPackageAnnotations(),
                        "".equals(packageName) ? null : make.Identifier(packageName),
                        compilationUnitTree.getImports(),
                        compilationUnitTree.getTypeDecls(),
                        compilationUnitTree.getSourceFile()
                );
                workingCopy.rewrite(compilationUnitTree, cutCopy);
                // go to rename also the top level class too...
                if (originalName != null && !originalName.equals(newName)) {
                    for (Tree typeDecl : compilationUnitTree.getTypeDecls()) {
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                            ClassTree clazz = (ClassTree) typeDecl;
                            if (originalName.contentEquals(clazz.getSimpleName())) {
                                Tree copy = make.setLabel(typeDecl, newName);
                                workingCopy.rewrite(typeDecl, copy);
                            }
                        }
                    }
                }
            }                
        };
        final ModificationResult taskResult = javaSource.runModificationTask(task);
        taskResult.commit();
    }

    @NbBundle.Messages({
        "# {0} - name of file",
        "MSG_NotIdentifier={0} is not proper Java identifier"
    })
    private static void verifyJavaNames(String... names) throws IOException {
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if ("package-info".equals(name)) { // NOI18N
                continue;
            }
            if ("module-info".equals(name)) { // NOI18N
                continue;
            }
            if (!Utilities.isJavaIdentifier(name)) {
                throw Exceptions.attachLocalizedMessage(new IOException(name + " is not Java identifier"), Bundle.MSG_NotIdentifier(name));
            }
        }
    }
}
