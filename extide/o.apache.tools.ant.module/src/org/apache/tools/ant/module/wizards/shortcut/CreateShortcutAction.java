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

package org.apache.tools.ant.module.wizards.shortcut;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Action to invoke the target shortcut wizard.
 * Used to be a "template", but this is more natural.
 * @see "issue #37374"
 */
@ActionID(category="Build", id="org.apache.tools.ant.module.wizards.shortcut.CreateShortcutAction")
@ActionRegistration(displayName="#LBL_create_shortcut")
@ActionReference(path="org-apache-tools-ant-module/target-actions", position=500)
@Messages(value = "LBL_create_shortcut=Create Shortcut...")
public final class CreateShortcutAction implements ActionListener {

    private final TargetLister.Target target;

    public CreateShortcutAction(TargetLister.Target target) {
        this.target = target;
    }

    @Override public void actionPerformed(ActionEvent e) {
        ShortcutWizard.show(target.getScript(), target.getElement());
    }

}
