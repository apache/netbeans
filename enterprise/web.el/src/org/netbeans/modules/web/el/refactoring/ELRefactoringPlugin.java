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
