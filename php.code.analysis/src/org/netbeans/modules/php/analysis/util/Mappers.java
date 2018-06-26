/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
