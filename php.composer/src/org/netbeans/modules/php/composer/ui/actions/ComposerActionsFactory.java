/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
            add(new ValidateAction());
            addSeparator();
            add(new SelfUpdateAction());
        }

    }

}
