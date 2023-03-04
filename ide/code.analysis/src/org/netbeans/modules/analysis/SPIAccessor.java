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
package org.netbeans.modules.analysis;

import java.awt.Image;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.analysis.spi.Analyzer.AnalyzerFactory;
import org.netbeans.modules.analysis.spi.Analyzer.Context;
import org.netbeans.modules.analysis.spi.Analyzer.CustomizerContext;
import org.netbeans.modules.analysis.spi.Analyzer.MissingPlugin;
import org.netbeans.modules.analysis.spi.Analyzer.Result;
import org.netbeans.modules.analysis.spi.Analyzer.WarningDescription;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public abstract class SPIAccessor {

    public static SPIAccessor ACCESSOR;

    static {
        try {
            Class.forName(Analyzer.Context.class.getName(), true, Analyzer.Context.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public abstract Context createContext(Scope scope, Preferences settings, String singleWarningId, ProgressHandle progress, int bucketStart, int bucketSize);

    public abstract Result createResult(List<ErrorDescription> errors, Map<ErrorDescription, Project> errorsToProjects, Collection<AnalysisProblem> analysisProblems);

    public abstract String getDisplayName(MissingPlugin missing);

    public abstract String getCNB(MissingPlugin missing);

    public abstract String getWarningId(WarningDescription description);
    public abstract String getWarningDisplayName(WarningDescription description);
    public abstract String getWarningCategoryId(WarningDescription description);
    public abstract String getWarningCategoryDisplayName(WarningDescription description);

    public abstract String getSelectedId(CustomizerContext<?, ?> cc);

    public abstract String getAnalyzerId(AnalyzerFactory selected);
    public abstract String getAnalyzerDisplayName(AnalyzerFactory a);
    public abstract Image getAnalyzerIcon(AnalyzerFactory analyzer);

    public abstract Collection<? extends AnalysisProblem> getAnalysisProblems(Context context);

}
