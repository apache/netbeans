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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.analysis.RunAnalysisPanel.FutureWarnings;
import org.netbeans.modules.analysis.spi.Analyzer.AnalyzerFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.nodes.Node;

/**
 *
 * @author lahvac
 */
public class AnalysisResult {

    public final Map<AnalyzerFactory, List<ErrorDescription>> provider2Hints;
    public final Map<ErrorDescription, Project> errorsToProjects;
    public final FutureWarnings analyzerId2Description;
    public final Collection<Node> extraNodes;

    public AnalysisResult(Map<AnalyzerFactory, List<ErrorDescription>> provider2Hints, Map<ErrorDescription, Project> errorsToProjects, FutureWarnings analyzerId2Description, Collection<Node> extraNodes) {
        this.provider2Hints = provider2Hints;
        this.errorsToProjects = errorsToProjects;
        this.analyzerId2Description = analyzerId2Description;
        this.extraNodes = extraNodes;
    }
    
}
