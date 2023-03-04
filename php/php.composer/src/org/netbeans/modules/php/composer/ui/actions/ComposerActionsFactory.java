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
package org.netbeans.modules.php.composer.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 * Factory for Composer actions.
 */
@ActionID(id="org.netbeans.modules.php.composer.ui.actions.ComposerActionsFactory", category="Project")
@ActionRegistration(displayName="#ActionsFactory.name", lazy=false)
@ActionReference(position=1050, path="Projects/org-netbeans-modules-php-project/Actions")
public final class ComposerActionsFactory extends AbstractAction implements Presenter.Popup {

    private static final long serialVersionUID = 54786435246576574L;

    private JMenu composerActions = null;


    public ComposerActionsFactory() {
        super();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        if (composerActions == null) {
            composerActions = new ComposerActions();
        }
        return composerActions;
    }

    //~ Inner classes

    private static final class ComposerActions extends JMenu {

        private static final long serialVersionUID = -877135786765411L;


        @NbBundle.Messages("ComposerActionsFactory.name=Composer")
        public ComposerActions() {
            super(Bundle.ComposerActionsFactory_name());
            add(new AddDependencyAction());
            addSeparator();
            add(new InitAction());
            add(new InstallDevAction());
            add(new InstallNoDevAction());
            add(new UpdateDevAction());
            add(new UpdateNoDevAction());
            add(new UpdateAutoloaderDevAction());
            add(new UpdateAutoloaderNoDevAction());
            add(new ValidateAction());
            addSeparator();
            add(new SelfUpdateAction());
        }

    }

}
