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
/*
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.actions.usages;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.maven.artifact.Artifact;
import static org.netbeans.modules.maven.actions.usages.Bundle.*;
import org.netbeans.modules.maven.actions.usages.ui.UsagesUI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Anuradha G
 */
public class FindArtifactUsages extends AbstractAction {

    private Artifact artifact;

    @Messages("LBL_FindartifactUsages=Find Usages")
    public FindArtifactUsages(Artifact artifact) {
        this.artifact = artifact;
        putValue(Action.NAME, LBL_FindartifactUsages());

    }

    @Override
    @Messages("TIT_FindartifactUsages=Usages")
    public void actionPerformed(ActionEvent event) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<b>"); //NOI18N
        buffer.append(artifact.getArtifactId());
        buffer.append("</b>"); //NOI18N
        buffer.append(":"); //NOI18N
        buffer.append("<b>"); //NOI18N
        buffer.append(artifact.getVersion().toString());
        buffer.append("</b>"); //NOI18N

        UsagesUI uI = new UsagesUI(buffer.toString(), artifact);
        DialogDescriptor dd = new DialogDescriptor(uI, TIT_FindartifactUsages());
        uI.initialize(dd.createNotificationLineSupport());
        dd.setClosingOptions(new Object[]{
            DialogDescriptor.CLOSED_OPTION
        });
        dd.setOptions(new Object[]{
            DialogDescriptor.CLOSED_OPTION
        });
        DialogDisplayer.getDefault().notify(dd);

    }
}
