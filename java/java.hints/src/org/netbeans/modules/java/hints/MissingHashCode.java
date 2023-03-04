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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.swing.JEditorPane;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.EqualsHashCodeGenerator;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav tulach
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.MissingHashCode", description = "#DESC_org.netbeans.modules.java.hints.MissingHashCode", id="org.netbeans.modules.java.hints.MissingHashCode", category="general", enabled=true, options=Options.QUERY, suppressWarnings="EqualsAndHashcode")
public class MissingHashCode {

    @TriggerTreeKind(Kind.CLASS)
    public static List<ErrorDescription> run(HintContext ctx) {
        CompilationInfo compilationInfo = ctx.getInfo();
        TreePath treePath = ctx.getPath();
        Element clazz = compilationInfo.getTrees().getElement(treePath);
        if (clazz == null || !clazz.getKind().isClass()) {
            return null;
        }

        ExecutableElement[] ret = EqualsHashCodeGenerator.overridesHashCodeAndEquals(compilationInfo, clazz, new EqualsHashCodeGenerator.Cancel() {
            @Override public boolean isCanceled() {
                return false;
            }
        });
        ExecutableElement warningToElement = null;

        String addHint = null;
        if (ret[0] == null && ret[1] != null) {
            addHint = "MSG_GenEquals"; // NOI18N
            warningToElement = ret[1];
        }
        if (ret[1] == null && ret[0] != null) {
            addHint = "MSG_GenHashCode"; // NOI18N
            warningToElement = ret[0];
        }

        if (addHint != null) {
            assert warningToElement != null;

            TreePath warningTo = compilationInfo.getTrees().getPath(warningToElement);

            if (warningTo == null || warningTo.getLeaf().getKind() != Kind.METHOD) {
                //XXX: should not happen, log
                return null;
            }
            
            List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(
                addHint, 
                TreePathHandle.create(clazz, compilationInfo),
                compilationInfo.getFileObject()
            ));

            int[] span = compilationInfo.getTreeUtilities().findNameSpan((MethodTree) warningTo.getLeaf());

            if (span != null) {
                ErrorDescription ed = ErrorDescriptionFactory.forName(
                    ctx,
                    warningTo,
                    NbBundle.getMessage(MissingHashCode.class, addHint), 
                    fixes.toArray(new Fix[0])
                );

                return Collections.singletonList(ed);
            }
        }
        
        return null;
    }

    public String getId() {
        return getClass().getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(MissingHashCode.class, "MSG_MissingHashCode");
    }

    public String getDescription() {
        return NbBundle.getMessage(MissingHashCode.class, "HINT_MissingHashCode");
    }
    
    private static final class FixImpl implements Fix, Runnable, Task<WorkingCopy> {
        private TreePathHandle handle;
        private FileObject file;
        private String msg;
        private boolean fieldFound;
        
        public FixImpl(String type, TreePathHandle handle, FileObject file) {
            this.handle = handle;
            this.file = file;
            this.msg = type;
        }
        
        public String getText() {
            return NbBundle.getMessage(MissingHashCode.class, msg);
        }
        
        public ChangeInfo implement() throws IOException {
            ModificationResult result = JavaSource.forFileObject(file).runModificationTask(this);
            if (fieldFound) {
                EventQueue.invokeLater(this);
            } else {
                result.commit();
            }
            
            return null;
        }
        
        public void run() {
            try {
                EditorCookie cook = DataObject.find(file).getLookup().lookup(EditorCookie.class);
                JEditorPane[] arr = cook.getOpenedPanes();
                if (arr == null) {
                    return;
                }
                EqualsHashCodeGenerator.invokeEqualsHashCode(handle, arr[0]);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        public void run(WorkingCopy wc) throws Exception {
            wc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            for (Element elem : handle.resolveElement(wc).getEnclosedElements()) {
                if (elem.getKind() == ElementKind.FIELD) {
                    if (!elem.getModifiers().contains(Modifier.STATIC)) {
                        fieldFound = true;
                        return;
                    }
                }
            }
            EqualsHashCodeGenerator.generateEqualsAndHashCode(wc, handle.resolve(wc));
        }
        
        @Override
        public String toString() {
            return "Fix";
        }
    }
    
}
