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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.refactoring.api.CopyRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * Plugin that implements the core functionality of Copy Classes Refactoring.
 *
 * @author Ralph Ruijs
 */
public class CopyClassesRefactoringPlugin extends JavaRefactoringPlugin {
    /**
     * Reference to the parent refactoring instance
     */
    private final CopyRefactoring refactoring;
    
    /**
     * Creates a new instance of CopyClassesRefactoringPlugin
     *
     * @param refactoring Parent refactoring instance.
     */
    CopyClassesRefactoringPlugin(CopyRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    protected JavaSource getJavaSource(Phase p) {
        return JavaSource.forFileObject(refactoring.getRefactoringSource().lookup(FileObject.class));
    }

    @Override
    public Problem fastCheckParameters() {
        URL target = refactoring.getTarget().lookup(URL.class);
        try {
            target.toURI();
        } catch (URISyntaxException ex) {
            return createProblem(null, true, NbBundle.getMessage(CopyClassesRefactoringPlugin.class, "ERR_InvalidPackage", RefactoringUtils.getPackageName(target)));
        }
        FileObject fo = target != null ? URLMapper.findFileObject(target) : null;
        if (fo == null) {
            return createProblem(null, true, NbBundle.getMessage(CopyClassesRefactoringPlugin.class, "ERR_TargetFolderNotSet"));
        }
        if (!JavaRefactoringUtils.isOnSourceClasspath(fo)) {
            return createProblem(null, true, NbBundle.getMessage(CopyClassesRefactoringPlugin.class, "ERR_TargetFolderNotJavaPackage"));
        }
        String targetPackageName = RefactoringUtils.getPackageName(target);
        if (!RefactoringUtils.isValidPackageName(targetPackageName)) {
            String msg = new MessageFormat(NbBundle.getMessage(CopyClassesRefactoringPlugin.class, "ERR_InvalidPackage")).format(
                    new Object[]{targetPackageName});
            return createProblem(null, true, msg);
        }
        Collection<? extends FileObject> sources = refactoring.getRefactoringSource().lookupAll(FileObject.class);
        Problem p = null;
        for (FileObject fileObject : sources) {
            if (fo.getFileObject(fileObject.getName(), fileObject.getExt()) != null) {
                p = createProblem(p, false, NbBundle.getMessage(CopyClassesRefactoringPlugin.class, "ERR_ClassesToCopyClashes"));
                break;
            }
        }
        return p;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem preCheck() {
        cancelRequest = false;
        cancelRequested.set(false);
        return null;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        for (FileObject fileobject : refactoring.getRefactoringSource().lookupAll(FileObject.class)) {
            refactoringElements.add(refactoring, new CopyClass(fileobject));
        }
        
        return null;
    }

    private class CopyClass extends SimpleRefactoringElementImplementation implements RefactoringElementImplementation {
        private final FileObject fileobject;

        CopyClass(FileObject fileobject) {
            this.fileobject = fileobject;
        }

        @Override
        public String getText() {
            return getDisplayText();
        }

        @Override
        public String getDisplayText() {
            return new MessageFormat(NbBundle.getMessage(CopyClassesRefactoringPlugin.class, "TXT_CopyClassToPackage")).format( // NOI18N
                    new Object[]{fileobject.getName(), getTargetPackageName(), getParentFile().getName()});
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }

        public String getTargetPackageName() {
            return RefactoringUtils.getPackageName(refactoring.getTarget().lookup(URL.class));
        }

        @Override
        public void performChange() {
            try {
                FileObject fo = RefactoringUtils.getOrCreateFolder(refactoring.getTarget().lookup(URL.class));
                FileObject source = fileobject;
                String oldPackage = RefactoringUtils.getPackageName(source.getParent());
                FileObject[] fileObjects = refactoring.getContext().lookup(FileObject[].class);
                FileObject newOne = null;
                for (FileObject fileObject : fileObjects) {
                    String orig = (String) fileObject.getAttribute("originalFile"); //NOI18N
                    if(fileObject.isValid() && source.getNameExt().equals(orig)) {
                        newOne = fileObject;
                        break;
                    }
                }
                if (newOne == null) {
                    // no copy exist
                    return;
                }
                final Collection<ModificationResult> results = processFiles(
                        Collections.singleton(newOne),
                        new UpdateReferences(!fo.equals(source.getParent()) && FileOwnerQuery.getOwner(fo).equals(FileOwnerQuery.getOwner(source)),
                        oldPackage,
                        source.getName()));
                for (ModificationResult result : results) {
                    result.commit();
                }
                DataObject dobj = DataObject.find(newOne);
                EditorCookie editor = dobj.getLookup().lookup(EditorCookie.class);
                if (editor != null) {
                    editor.open();
                }

            } catch (Exception ioe) {
                ErrorManager.getDefault().notify(ioe);
            }

        }

        @Override
        public FileObject getParentFile() {
            return fileobject;
        }
    }

    private class UpdateReferences implements CancellableTask<WorkingCopy> {

        private boolean insertImport;
        private String oldPackage;
        private String oldName;

        public UpdateReferences(boolean insertImport, String oldPackage, String oldName) {
            this.insertImport = insertImport;
            this.oldPackage = oldPackage;
            this.oldName = oldName;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(WorkingCopy compiler) throws IOException {
            compiler.toPhase(JavaSource.Phase.RESOLVED);
            CompilationUnitTree cu = compiler.getCompilationUnit();
            if (cu == null) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler); // NOI18N
                return;
            }
            FileObject file = compiler.getFileObject();
            FileObject folder = file.getParent();
            
            final String newName;
            if(folder.getFileObject(oldName, file.getExt()) != null) {
                newName = file.getName();
            } else {
                newName = oldName;
            }
            
            CopyTransformer findVisitor = new CopyTransformer(compiler, oldName, newName, insertImport, oldPackage);
            findVisitor.scan(compiler.getCompilationUnit(), null);
        }
    }
}
