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

package org.netbeans.modules.gradle.java.coverage;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.codecoverage.api.CoverageActionFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

import static org.netbeans.modules.gradle.api.NbGradleProject.GRADLE_PROJECT_TYPE;

@ActionID(category="Project", id="org.netbeans.modules.gradle.java.coverage.CoveragePopup")
@ActionRegistration(displayName="Test Coverage", lazy=false) // NOI18N
@ActionReference(path="Projects/" + GRADLE_PROJECT_TYPE + "/Actions", position=1205)
public class CoveragePopup extends AbstractAction implements ContextAwareAction {

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public CoveragePopup() {
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup ctx) {
        Project p = ctx.lookup(Project.class);
        if (p == null) {
            return this;
        }
        GradleBaseProject gbp = GradleBaseProject.get(p);
        if ((gbp == null) || !gbp.getPlugins().contains("jacoco")) { //NOI18N
            return this;
        }
        return ((ContextAwareAction) CoverageActionFactory.createCollectorAction(null, null)).createContextAwareInstance(ctx);
    }

}
