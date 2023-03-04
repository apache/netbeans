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

package org.netbeans.modules.maven.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.netbeans.api.actions.Openable;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ContextAwareAction;
import org.openide.util.NbBundle.Messages;

@ActionID(category=OpenPOMAction.CATEGORY, id=OpenPOMAction.ID)
@ActionRegistration(displayName="#BTN_open_pom")
@ActionReference(path="Projects/org-netbeans-modules-maven/Actions", position=1650, separatorAfter=1655)
@Messages("BTN_open_pom=Open POM")
public class OpenPOMAction implements ActionListener {

    static final String CATEGORY = "Project";
    static final String ID = "org.netbeans.modules.maven.actions.openpom";
    public static ContextAwareAction instance() {
        return (ContextAwareAction) Actions.forID(CATEGORY, ID);
    }

    private final List<NbMavenProjectImpl> projects;

    public OpenPOMAction(List<NbMavenProjectImpl> projects) {
        this.projects = projects;
    }

    @Override public void actionPerformed(ActionEvent e) {
        for (NbMavenProjectImpl project : projects) {
            FileObject pom = FileUtil.toFileObject(project.getPOMFile());
            if (pom != null) {
                DataObject d;
                try {
                    d = DataObject.find(pom);
                } catch (DataObjectNotFoundException x) {
                    continue;
                }
                Openable o = d.getLookup().lookup(Openable.class);
                if (o != null) {
                    o.open();
                }
            }
        }
    }

}
