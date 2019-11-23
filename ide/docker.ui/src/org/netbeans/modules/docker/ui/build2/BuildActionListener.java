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
package org.netbeans.modules.docker.ui.build2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Petr Hejl
 */
@ActionID(category = "File", id = "org.netbeans.modules.docker.ui.build2.BuildActionListener")
@ActionRegistration(displayName = "#LBL_Build")
@ActionReference(path = "Loaders/text/x-dockerfile/Actions", position = 150)
@Messages("LBL_Build=Build...")
public class BuildActionListener implements ActionListener {

    private final FileObject fo;

    public BuildActionListener(FileObject fo) {
        this.fo = fo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Logger.getLogger(BuildActionListener.class.getName()).log(Level.INFO, "Building {0}", fo.getPath());
        BuildImageWizard wizard = new BuildImageWizard();
        wizard.setDockerfile(fo);
        wizard.show();
    }

}
