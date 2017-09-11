/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.repository;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitRepositories;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondra Vrabec
 */
@ActionID(id = "org.netbeans.modules.git.ui.repository.OpenRepositoryAction", category = "Git")
@ActionRegistration(displayName = "#LBL_OpenRepositoryAction_Name")
@NbBundle.Messages({
    "LBL_OpenRepositoryAction_Name=&Open Repository",
    "LBL_OpenRepositoryAction_PopupName=Open Repository",
    "CTL_OpenRepository_okButton=Open",
    "CTL_OpenRepository_ACSD=Choose Git repository to open in the Repository Browser"
})
public class OpenRepositoryAction extends AbstractAction {

    public OpenRepositoryAction () {
        super(Bundle.LBL_OpenRepositoryAction_PopupName());
    }
    
    @Override
    public void actionPerformed (ActionEvent event) {
        final File f = new FileChooserBuilder(OpenRepositoryAction.class).setDirectoriesOnly(true)
                .setApproveText(Bundle.CTL_OpenRepository_okButton())
                .setAccessibleDescription(Bundle.CTL_OpenRepository_ACSD())
                .showOpenDialog();
        if (f == null) {
            return;
        }
        Utils.postParallel(new Runnable () {
            @Override
            public void run() {
                final File repository = Git.getInstance().getRepositoryRoot(f);
                if (repository != null) {
                    GitRepositories.getInstance().add(repository, true);
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run () {
                            GitRepositoryTopComponent rtc = GitRepositoryTopComponent.findInstance();
                            rtc.open();
                            rtc.requestActive();
                            rtc.selectRepository(repository);
                        }
                    });
                }
            }
        }, 0);
    }
    
}
