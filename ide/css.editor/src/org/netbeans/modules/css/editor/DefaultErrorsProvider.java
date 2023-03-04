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
package org.netbeans.modules.css.editor;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.css.editor.csl.CssErrorFactory;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.ErrorsProvider;
import org.netbeans.modules.css.lib.api.FilterableError;
import org.netbeans.modules.css.lib.api.ProblemDescription;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides default lexer/parser errors
 *
 * @author marekfukala
 */
@ServiceProvider(service = ErrorsProvider.class)
public class DefaultErrorsProvider implements ErrorsProvider {

    @Override
    public List<? extends FilterableError> getExtendedDiagnostics(CssParserResult parserResult) {
        return getCslErrorForCss3ProblemDescription(
                parserResult.getSnapshot().getSource().getFileObject(),
                parserResult.getParserDiagnostics());
    }

    public static List<FilterableError> getCslErrorForCss3ProblemDescription(FileObject file, List<ProblemDescription> pds) {
        List<FilterableError> errors = new ArrayList<>();
        for (ProblemDescription pd : pds) {
            errors.add(getCslErrorForCss3ProblemDescription(file, pd));
        }
        return errors;
    }

    private static FilterableError getCslErrorForCss3ProblemDescription(FileObject file, ProblemDescription pd) {
        return CssErrorFactory.createError(
                pd.getKey(),
                pd.getDescription(),
                pd.getDescription(),
                file,
                pd.getFrom(),
                pd.getTo(),
                false,
                getCslSeverityForCss3ProblemType(pd.getType()),
                ParsingErrorsFilter.getEnableFilterAction(file, pd.getDescription()),
                ParsingErrorsFilter.getDisableFilterAction(file, pd.getDescription()));
    }

    private static Severity getCslSeverityForCss3ProblemType(ProblemDescription.Type problemType) {
        switch (problemType) {
            case ERROR:
                return Severity.ERROR;
            case FATAL:
                return Severity.FATAL;
            case INFO:
                return Severity.INFO;
            case WARNING:
                return Severity.WARNING;
        }

        return Severity.ERROR;
    }

}
