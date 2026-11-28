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
package org.netbeans.modules.acp.mcp.tools.refactoring;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.acp.mcp.spi.ToolResult;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class RefactoringExecution {

    private RefactoringExecution() {
    }

    /**
     * Executes a refactoring through the standard preCheck/paramCheck/prepare/doRefactoring
     * pipeline and returns affected files with any non-fatal warnings.
     */
    public static Map<String, Object> execute(AbstractRefactoring refactoring,
            String sessionName) {
        Result rr = run(refactoring, sessionName);
        if (rr.error != null) {
            return rr.error;
        }

        Map<String, Object> out = ToolResult.ok("affectedFiles", rr.affectedFiles,
                "affectedCount", rr.affectedFiles.size());
        if (!rr.warnings.isEmpty()) {
            out.put("warnings", rr.warnings);
        }
        return out;
    }

    public static Result run(AbstractRefactoring refactoring,
            String sessionName) {
        Problem preCheck = refactoring.preCheck();
        String preCheckMsg = collectProblems(preCheck);
        if (hasFatalProblem(preCheck)) {
            return Result.fail("Pre-check failed: " + preCheckMsg);
        }

        Problem paramCheck = refactoring.checkParameters();
        String paramCheckMsg = collectProblems(paramCheck);
        if (hasFatalProblem(paramCheck)) {
            return Result.fail("Parameter check failed: " + paramCheckMsg);
        }

        RefactoringSession session = RefactoringSession.create(sessionName);
        Problem prepare = refactoring.prepare(session);
        String prepareMsg = collectProblems(prepare);
        if (hasFatalProblem(prepare)) {
            return Result.fail("Prepare failed: " + prepareMsg);
        }

        Collection<RefactoringElement> elements = session.getRefactoringElements();
        List<String> affectedFiles = collectAffectedFiles(elements);

        Problem result = session.doRefactoring(true);
        if (hasFatalProblem(result)) {
            return Result.fail("Refactoring failed: " + collectProblems(result));
        }

        return new Result(null, affectedFiles,
                collectWarnings(preCheckMsg, paramCheckMsg, prepareMsg, collectProblems(result)));
    }

    public record Result(Map<String, Object> error, List<String> affectedFiles, List<String> warnings) {

        private static Result fail(String message) {
            return new Result(ToolResult.error(message), List.of(), List.of());
        }
    }

    public static boolean hasFatalProblem(Problem problem) {
        for (Problem current = problem; current != null; current = current.getNext()) {
            if (current.isFatal()) {
                return true;
            }
        }
        return false;
    }

    public static String collectProblems(Problem problem) {
        if (problem == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Problem p = problem;
        while (p != null) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            if (p.isFatal()) {
                sb.append("[FATAL] ");
            }
            sb.append(p.getMessage());
            p = p.getNext();
        }
        return sb.toString();
    }

    private static List<String> collectAffectedFiles(Collection<RefactoringElement> elements) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (RefactoringElement elem : elements) {
            FileObject parent = elem.getParentFile();
            if (parent != null) {
                File f = FileUtil.toFile(parent);
                seen.add(f != null ? f.getAbsolutePath() : parent.getPath());
            }
        }
        return new ArrayList<>(seen);
    }

    public static List<String> collectWarnings(String... messages) {
        List<String> warnings = new ArrayList<>();
        for (String msg : messages) {
            if (msg != null) {
                warnings.add(msg);
            }
        }
        return warnings;
    }
}
