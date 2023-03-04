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
package org.netbeans.modules.j2ee.ejbverification.fixes;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.ejbverification.JavaUtils;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Tomasz.Slota@Sun.COM
 */
public class ExposeBusinessMethod implements Fix {

    private static final Logger LOG = Logger.getLogger(ExposeBusinessMethod.class.getName());
    private FileObject fileObject;
    private ElementHandle<TypeElement> targetClassHandle;
    private ElementHandle<ExecutableElement> methodHandle;
    private boolean local;

    public ExposeBusinessMethod(FileObject fileObject, ElementHandle<TypeElement> targetClassHandle, ElementHandle<ExecutableElement> methodHandle, boolean local) {
        this.fileObject = fileObject;
        this.targetClassHandle = targetClassHandle;
        this.methodHandle = methodHandle;
        this.local = local;
    }

    @Override
    public ChangeInfo implement() {
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {
            @Override
            public void cancel() {
            }

            @Override
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement targetClass = targetClassHandle.resolve(workingCopy);
                ExecutableElement originalMethod = methodHandle.resolve(workingCopy);
                if (targetClass == null || originalMethod == null) {
                    return;
                }
                ClassTree clazzTree = workingCopy.getTrees().getTree(targetClass);
                TreeMaker make = workingCopy.getTreeMaker();
                //generate the method:
                MethodTree newMethod = GeneratorUtilities.get(workingCopy).createMethod((DeclaredType) targetClass.asType(), originalMethod);
                //clear method body:
                newMethod = make.Method(newMethod.getModifiers(),
                        newMethod.getName(),
                        newMethod.getReturnType(),
                        newMethod.getTypeParameters(),
                        newMethod.getParameters(),
                        newMethod.getThrows(),
                        null,
                        null,
                        originalMethod.isVarArgs());
                GeneratorUtilities generator = GeneratorUtilities.get(workingCopy);
                ClassTree newClass = generator.insertClassMember(clazzTree, newMethod);

                workingCopy.rewrite(clazzTree, newClass);
            }
        };

        ClasspathInfo cpInfo = ClasspathInfo.create(fileObject);
        FileObject targetFileObject = SourceUtils.getFile(targetClassHandle, cpInfo);

        // target file can't be found, don't offer the fix
        if (targetFileObject == null) {
            LOG.log(Level.WARNING,
                    "ExposeBusinessMethod not offered: targetFile={0} not found", targetClassHandle.getQualifiedName());
            return null;
        }
        JavaSource javaSource = JavaSource.create(cpInfo, fileObject, targetFileObject);

        try {
            return commitAndComputeChangeInfo(targetFileObject, javaSource.runModificationTask(task));
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }

        return null;
    }

    // adapted from org.netbeans.modules.java.hints.errors.Utilities
    private ChangeInfo commitAndComputeChangeInfo(final FileObject target, ModificationResult diff) throws IOException {
        List<? extends Difference> differences = diff.getDifferences(target);
        ChangeInfo result = null;

        // need to save the modified doc so that changes are recognized, see #112888
        CloneableEditorSupport docToSave = null;
        try {
            if (differences != null) {
                for (Difference d : differences) {
                    if (d.getNewText() != null) { //to filter out possible removes
                        final Position start = d.getStartPosition();
                        Document doc = d.openDocument();
                        if (docToSave == null) {
                            docToSave = target.getLookup().lookup(CloneableEditorSupport.class);
                        }
                        final Position[] pos = new Position[1];
                        final Document fdoc = doc;

                        doc.render(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    pos[0] = NbDocument.createPosition(fdoc, start.getOffset(), Position.Bias.Backward);
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        });

                        if (pos[0] != null) {
                            result = new ChangeInfo(target, pos[0], pos[0]);
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }

        diff.commit();

        if (docToSave != null) {
            docToSave.saveDocument();
        }

        return result;
    }


    @Override
    public String getText() {
        String className = JavaUtils.getShortClassName(targetClassHandle.getQualifiedName());
        return NbBundle.getMessage(ExposeBusinessMethod.class,
                local ? "LBL_ExposeBusinessMethodLocal" : "LBL_ExposeBusinessMethodRemote",
                className);
    }
}
