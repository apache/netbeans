/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.el.refactoring;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.el.ELLanguage;
import org.netbeans.modules.web.el.ELParserResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 */
public class ELRefactoringPlugin extends JavaRefactoringPlugin {

    protected final AbstractRefactoring refactoring;

    public ELRefactoringPlugin(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        return null;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        return null;
    }

    protected final TreePathHandle getHandle() {
        return refactoring.getRefactoringSource().lookup(TreePathHandle.class);
    }

    protected final FileObject getFileObject() {
        FileObject fo = refactoring.getRefactoringSource().lookup(FileObject.class);
        if (fo != null) {
            return fo;
        } else {
            fo = getHandle().getFileObject();
            if (fo != null) {
                return fo;
            }
        }
        // seeems we're looking for usages of a class/method that is not declared
        // in the project's sources
        ClasspathInfo cpinfo = getClasspathInfo(refactoring);
        FileObject[] roots = cpinfo.getClassPath(ClasspathInfo.PathKind.SOURCE).getRoots();
        return roots.length > 0 ? roots[1] : null;
    }

    protected static ParserResultHolder getParserResult(FileObject fo) {
        try {
            final Source source = Source.create(fo);
            final AtomicReference<ELParserResult> result = new AtomicReference<>();
            final AtomicReference<Snapshot> snapshot = new AtomicReference<>();
            ParserManager.parse(Collections.singletonList(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator ri = WebUtils.getResultIterator(resultIterator, ELLanguage.MIME_TYPE);
                    snapshot.set(resultIterator.getSnapshot());
                    result.set(ri == null ? null : (ELParserResult) ri.getParserResult());
                }
            });
            return new ParserResultHolder(result.get(), snapshot.get());
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    protected static final class ParserResultHolder {

        final ELParserResult parserResult;
        final Snapshot topLevelSnapshot;

        public ParserResultHolder(ELParserResult parserResult, Snapshot topLevelSnapshot) {
            this.parserResult = parserResult;
            this.topLevelSnapshot = topLevelSnapshot;
        }

    }
}
