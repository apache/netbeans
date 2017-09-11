/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.spiimpl;

import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
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

    public String getText() {
        return Accessor.INSTANCE.getText(jf);
    }

    public ChangeInfo implement() throws Exception {
        FileObject file = Accessor.INSTANCE.getFile(jf);
        
        BatchUtilities.fixDependencies(file, Collections.singletonList(jf), new IdentityHashMap<Project, Set<String>>());

        JavaSource js = JavaSource.forFileObject(file);

        js.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy wc) throws Exception {
                if (wc.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                Map<FileObject, byte[]> resourceContentChanges = new HashMap<FileObject, byte[]>();
                Accessor.INSTANCE.process(jf, wc, true, resourceContentChanges, /*Ignored in editor:*/new ArrayList<RefactoringElementImplementation>());
                Map<FileObject, List<Difference>> resourceContentDiffs = new HashMap<FileObject, List<Difference>>();
                BatchUtilities.addResourceContentChanges(resourceContentChanges, resourceContentDiffs);
                JavaSourceAccessor.getINSTANCE().createModificationResult(resourceContentDiffs, Collections.<Object, int[]>emptyMap()).commit();
            }
        }).commit();

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaFixImpl) {
            return jf.equals(((JavaFixImpl)obj).jf);
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
    
    public static abstract class Accessor {

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
        public abstract FileObject getFile(JavaFix jf);
        public abstract Map<String, String> getOptions(JavaFix jf);
        public abstract Fix rewriteFix(CompilationInfo info, String displayName, TreePath what, final String to, Map<String, TreePath> parameters, Map<String, Collection<? extends TreePath>> parametersMulti, final Map<String, String> parameterNames, Map<String, TypeMirror> constraints, Map<String, String> options, String... imports);
        public abstract Fix createSuppressWarningsFix(CompilationInfo compilationInfo, TreePath treePath, String... keys);
        public abstract List<Fix> createSuppressWarnings(CompilationInfo compilationInfo, TreePath treePath, String... keys);
        public abstract List<Fix> resolveDefaultFixes(HintContext ctx, Fix... provided);
    }
}
