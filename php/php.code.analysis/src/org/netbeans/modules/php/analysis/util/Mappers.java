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
package org.netbeans.modules.php.analysis.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.analysis.results.Result;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Various mappers.
 */
public final class Mappers {

    private static final String ANALYZER_PREFIX = "phpCodeAnalysis:"; // NOI18N
    private static final LazyFixList EMPTY_LAZY_FIX_LIST = ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList());


    private Mappers() {
    }

    public static Collection<? extends ErrorDescription> map(List<Result> results) {
        List<ErrorDescription> errorDescriptions = new ArrayList<>(results.size());
        FileObject file = null;
        String filePath = null;
        int[] lineMap = null;
        for (Result result : results) {
            String currentFilePath = result.getFilePath();
            if (!currentFilePath.equals(filePath)) {
                filePath = currentFilePath;
                file = FileUtil.toFileObject(new File(currentFilePath));
                assert file != null : "File object not found for " + currentFilePath;
                lineMap = AnalysisUtils.computeLineMap(file, FileEncodingQuery.getEncoding(file));
            }
            assert file != null;
            assert filePath != null;
            assert lineMap != null;
            errorDescriptions.add(map(result, file, lineMap));
        }
        return errorDescriptions;
    }

    private static ErrorDescription map(Result result, FileObject file, int[] lineMap) {
        int line = 2 * (Math.min(result.getLine(), lineMap.length / 2) - 1);
        return ErrorDescriptionFactory.createErrorDescription(ANALYZER_PREFIX + result.getCategory(), Severity.VERIFIER, result.getCategory(),
                result.getDescription(), EMPTY_LAZY_FIX_LIST, file, lineMap[line], lineMap[line + 1]);
    }

}
