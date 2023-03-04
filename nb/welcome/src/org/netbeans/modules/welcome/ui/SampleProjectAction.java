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

package org.netbeans.modules.welcome.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.project.ui.api.ProjectTemplates;
import org.netbeans.spi.project.ui.support.CommonProjectActions;

/**
 *
 * @author S. Aubrecht
 */
public class SampleProjectAction extends AbstractAction {

    @Override public void actionPerformed(ActionEvent e) {
        Action sampleProject = CommonProjectActions.newProjectAction();
        sampleProject.putValue(ProjectTemplates.PRESELECT_CATEGORY, "Samples" ); // NOI18N
        sampleProject.actionPerformed( e );
    }

}
