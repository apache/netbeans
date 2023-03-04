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
package org.netbeans.modules.web.jsf.hints.rules;

import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.beans.CdiUtil;
import org.netbeans.modules.web.jsf.hints.JsfHintsContext;
import org.netbeans.modules.web.jsf.hints.JsfHintsUtils;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Checks FlowScoped bean in non-CDI project.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#FlowScopedBeanWithoutCdi.display.name",
        description = "#FlowScopedBeanWithoutCdi.err",
        id = "o.n.m.web.jsf.hints.FlowScopedBeanWithoutCdi",
        category = "javaee/jsf",
        enabled = true,
        suppressWarnings = "FlowScopedBeanWithoutCdi")
@NbBundle.Messages({
    "FlowScopedBeanWithoutCdi.display.name=@FlowScoped bean in the non-CDI capable project",
    "FlowScopedBeanWithoutCdi.err=@FlowScoped bean in the non-CDI capable project"
})
public class FlowScopedBeanWithoutCdi {

    private static final String FLOW_SCOPED = "javax.faces.flow.FlowScoped"; //NOI18N

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static Collection<ErrorDescription> run(HintContext hintContext) {
        List<ErrorDescription> problems = new ArrayList<>();
        final JsfHintsContext ctx = JsfHintsUtils.getOrCacheContext(hintContext);

        Project project = ctx.getProject();
        if (project == null) {
            return problems;
        }

        CompilationInfo info = hintContext.getInfo();
        for (TypeElement typeElement : info.getTopLevelElements()) {
            for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
                if (FLOW_SCOPED.equals(annotationMirror.getAnnotationType().toString())) {
                    // it's FlowScoped bean -> check the CDI
                    CdiUtil cdiUtil = project.getLookup().lookup(CdiUtil.class);
                    if (cdiUtil == null || !cdiUtil.isCdiEnabled()) {
                        Tree tree = info.getTrees().getTree(typeElement, annotationMirror);
                        problems.add(JsfHintsUtils.createProblem(
                                tree,
                                info,
                                Bundle.FlowScopedBeanWithoutCdi_display_name(),
                                Severity.WARNING,
                                Arrays.<Fix>asList(new FixCdiAvailability(project))));
                    }
                }
            }
        }
        return problems;
    }

    /**
     * Fix for enabling CDI in the project.
     */
    private static class FixCdiAvailability implements Fix {

        private final Project project;

        public FixCdiAvailability(Project project) {
            this.project = project;
        }

        @Messages({
            "# {0} - project display name",
            "FixCdiAvailability.lbl.enable.cdi=Enable CDI in project {0}"
        })
        @Override
        public String getText() {
            ProjectInformation information = ProjectUtils.getInformation(project);
            return Bundle.FixCdiAvailability_lbl_enable_cdi(information.getDisplayName());
        }

        @Override
        public ChangeInfo implement() throws Exception {
            CdiUtil cdiUtil = project.getLookup().lookup(CdiUtil.class);
            if (cdiUtil != null) {
                cdiUtil.enableCdi();
            }
            return null;
        }
    }
}
