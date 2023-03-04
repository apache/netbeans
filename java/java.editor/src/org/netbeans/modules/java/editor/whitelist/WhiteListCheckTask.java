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
package org.netbeans.modules.java.editor.whitelist;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.api.whitelist.WhiteListQuery.WhiteList;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.api.whitelist.support.WhiteListSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
public class WhiteListCheckTask extends JavaParserResultTask<Result> {

    private static final String ID = "white-list-checker";  //NOI18N
    private final AtomicBoolean canceled = new AtomicBoolean();

    private WhiteListCheckTask() {
        super(Phase.RESOLVED);
    }


    @Override
    public void run(
            final Result result,
            final SchedulerEvent event) {
        canceled.set(false);
        final CompilationInfo info = CompilationInfo.get(result);
        final FileObject file = info.getFileObject();
        if (file == null) {
            return;
        }
        HintsController.setErrors(file, ID, Collections.<ErrorDescription>emptyList());
        final WhiteList whiteList = WhiteListQuery.getWhiteList(file);
        if (whiteList == null) {
            return;
        }
        final CompilationUnitTree cu = info.getCompilationUnit();
        final Map<? extends Tree, ? extends WhiteListQuery.Result> problems = WhiteListSupport.getWhiteListViolations(
                cu,
                whiteList,
                info.getTrees(),
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return canceled.get();
                    }
                });
        if (problems == null) {
            //Canceled
            return;
        }
        final SourcePositions sp = info.getTrees().getSourcePositions();
        final List<ErrorDescription> errors = new ArrayList<ErrorDescription>(problems.size());
        for (Map.Entry<? extends Tree, ? extends WhiteListQuery.Result> problem : problems.entrySet()) {
            if (canceled.get()) {
                return;
            }
            final Tree tree = problem.getKey();
            final int start = (int) sp.getStartPosition(cu, tree);
            final int end = (int) sp.getEndPosition(cu, tree);
            assert !problem.getValue().isAllowed() : problem;
            if (start >= 0 && end >= 0) {
                errors.add(ErrorDescriptionFactory.createErrorDescription(
                        Severity.WARNING,
                        formatViolationDescription(problem.getValue()),
                        file,
                        start,
                        end));
            }
        }
        if (canceled.get()) {
            return;
        }
        HintsController.setErrors(file, ID, errors);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        canceled.set(true);
    }

    @NbBundle.Messages({
        "MSG_Violations=Multiple rules were violated:"
    })
    private static String formatViolationDescription(WhiteListQuery.Result result) {
        assert result.getViolatedRules() != null : result;
        if (result.getViolatedRules().size() == 1) {
            return result.getViolatedRules().get(0).getRuleDescription();
        } else {
            StringBuilder sb = new StringBuilder(Bundle.MSG_Violations());
            for (WhiteListQuery.RuleDescription rule : result.getViolatedRules()) {
                sb.append("\n - "); //NOI18N
                sb.append(rule.getRuleDescription());
            }
            return sb.toString();
        }
    }

    @MimeRegistration(mimeType="text/x-java", service=TaskFactory.class)
    public static class Factory extends TaskFactory {
        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new WhiteListCheckTask());
        }
    }

}
