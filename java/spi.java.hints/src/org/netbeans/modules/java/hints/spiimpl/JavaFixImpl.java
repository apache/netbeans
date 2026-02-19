/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.hints.spiimpl;

import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSourcePath;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchUtilities;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**TODO: move to better package
 *
 * @author Jan Lahoda
 */
public class JavaFixImpl implements Fix {

    public final JavaFix jf;

    public JavaFixImpl(JavaFix jf) {
        this.jf = jf;
    }

    @Override
    public String getText() {
        return Accessor.INSTANCE.getText(jf);
    }

    @Override
    public ChangeInfo implement() throws Exception {
        JavaSourcePath path = Accessor.INSTANCE.getJavaSourcePath(jf);
        
        BatchUtilities.fixDependencies(path.getFileObject(), List.of(jf), new IdentityHashMap<>());

        Task<WorkingCopy> runFix = new Task<>() {
            @Override
            public void run(WorkingCopy wc) throws Exception {
                if (wc.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                Map<FileObject, byte[]> resourceContentChanges = new HashMap<>();
                Accessor.INSTANCE.process(jf, wc, true, resourceContentChanges, /*Ignored in editor:*/new ArrayList<>());
                Map<FileObject, List<Difference>> resourceContentDiffs = new HashMap<>();
                BatchUtilities.addResourceContentChanges(resourceContentChanges, resourceContentDiffs);
                JavaSourceAccessor.getINSTANCE().createModificationResult(resourceContentDiffs, Map.of()).commit();
            }
        };

        ModificationResult result = ModificationResult.runModificationTask(path, runFix);
        result.commit();

        Function<ModificationResult, ChangeInfo> convertor = Accessor.INSTANCE.getChangeInfoConvertor(jf);

        return convertor != null ? convertor.apply(result) : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaFixImpl javaFixImpl) {
            return jf.equals(javaFixImpl.jf);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return jf.hashCode();
    }

    public static class EnhancedJavaFixImpl extends JavaFixImpl implements EnhancedFix {

        public EnhancedJavaFixImpl(JavaFix jf) {
            super(jf);
        }

        @Override public CharSequence getSortText() {
            return Accessor.INSTANCE.getSortText(jf);
        }
        
    }
    
    public abstract static class Accessor {

        static {
            try {
                Class.forName(JavaFix.class.getCanonicalName(), true, JavaFix.class.getClassLoader());
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        public static Accessor INSTANCE;

        public abstract String getText(JavaFix jf);
        public abstract String getSortText(JavaFix jf);
        public abstract ChangeInfo process(JavaFix jf, WorkingCopy wc, boolean canShowUI, Map<FileObject, byte[]> resourceContent, Collection<? super RefactoringElementImplementation> fileChanges) throws Exception;
        public abstract JavaSourcePath getJavaSourcePath(JavaFix jf);
        public abstract Map<String, String> getOptions(JavaFix jf);
        public abstract Fix rewriteFix(CompilationInfo info, String displayName, TreePath what, final String to, Map<String, TreePath> parameters, Map<String, Collection<? extends TreePath>> parametersMulti, final Map<String, String> parameterNames, Map<String, TypeMirror> constraints, Map<String, String> options, String... imports);
        public abstract Fix createSuppressWarningsFix(CompilationInfo compilationInfo, TreePath treePath, String... keys);
        public abstract List<Fix> createSuppressWarnings(CompilationInfo compilationInfo, TreePath treePath, String... keys);
        public abstract List<Fix> resolveDefaultFixes(HintContext ctx, Fix... provided);
        public abstract void setChangeInfoConvertor(JavaFix jf, Function<ModificationResult, ChangeInfo> modResult2ChangeInfo);
        public abstract Function<ModificationResult, ChangeInfo> getChangeInfoConvertor(JavaFix jf);
    }
}
