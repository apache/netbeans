/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
