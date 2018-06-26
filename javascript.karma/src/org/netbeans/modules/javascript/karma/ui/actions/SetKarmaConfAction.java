/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.karma.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferences;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@NbBundle.Messages("SetKarmaConfAction.name=Set Karma Configuration")
@ActionID(id = "org.netbeans.modules.javascript.karma.ui.actions.SetKarmaConfAction", category = "Unit Tests")
@ActionRegistration(displayName = "#SetKarmaConfAction.name", lazy = false)
@ActionReferences({
    @ActionReference(path = "Editors/text/karmaconf+javascript/Popup", position = 907),
    @ActionReference(path = "Loaders/text/karmaconf+javascript/Actions", position = 157),
})
public final class SetKarmaConfAction extends AbstractAction implements ContextAwareAction {

    @NullAllowed
    private final Project project;
    @NullAllowed
    private final FileObject karmaConfJs;


    public SetKarmaConfAction() {
        this(null, null);
    }

    public SetKarmaConfAction(Project project, FileObject karmaConfJs) {
        this.project = project;
        this.karmaConfJs = karmaConfJs;
        setEnabled(karmaConfJs != null);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        putValue(Action.NAME, Bundle.SetKarmaConfAction_name());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert project != null;
        assert karmaConfJs != null;
        KarmaPreferences.setConfig(project, FileUtil.toFile(karmaConfJs).getAbsolutePath());
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        FileObject file = context.lookup(FileObject.class);
        if (file == null) {
            DataObject dataObject = context.lookup(DataObject.class);
            if (dataObject != null) {
                file = dataObject.getPrimaryFile();
            }
        }
        if (file == null) {
            return this;
        }
        Project owner = FileOwnerQuery.getOwner(file);
        if (owner == null) {
            return this;
        }
        if (!KarmaPreferences.isEnabled(owner)) {
            return this;
        }
        return new SetKarmaConfAction(owner, file);
    }

}
