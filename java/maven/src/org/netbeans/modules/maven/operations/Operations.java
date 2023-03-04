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

package org.netbeans.modules.maven.operations;

import javax.swing.SwingUtilities;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import static org.netbeans.modules.maven.operations.Bundle.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author mkleint
 */
public class Operations {


    @Messages("RenameProjectPanel.lblRename.text=Rename Project")
    public static void renameProject(NbMavenProjectImpl project) {
        assert SwingUtilities.isEventDispatchThread();
        RenameProjectPanel panel = new RenameProjectPanel(project);
        DialogDescriptor dd = new DialogDescriptor(panel, RenameProjectPanel_lblRename_text());
        panel.createValidations(dd);
        if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
            panel.renameProject();
        }
    }

}
