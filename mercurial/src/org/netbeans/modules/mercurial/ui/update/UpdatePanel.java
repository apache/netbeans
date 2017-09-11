/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.mercurial.ui.update;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.repository.ChangesetPickerPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author  Padraig O'Briain
 */
public class UpdatePanel extends ChangesetPickerPanel {

    private JCheckBox forcedUpdateChxBox;
    private final HgLogMessage fixedRevision;

    /** Creates new form ReverModificationsPanel */
    public UpdatePanel(File repo, HgLogMessage fixedRevision) {
        super(repo, null);
        this.fixedRevision = fixedRevision;
        initComponents();
        loadRevisions();
    }

    public boolean isForcedUpdateRequested() {
        return forcedUpdateChxBox.isSelected();
    }

    @Override
    protected HgLogMessage getDisplayedRevision () {
        return fixedRevision;
    }

    @Override
    protected String getRefreshLabel() {
        return NbBundle.getMessage(UpdatePanel.class, "MSG_Refreshing_Update_Versions"); //NOI18N
    }

    private void initComponents() {
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RevertModificationsPanel.class, "UpdatePanel.infoLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RevertModificationsPanel.class, "UpdatePanel.infoLabel2.text")); // NOI18N
        forcedUpdateChxBox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(forcedUpdateChxBox, org.openide.util.NbBundle.getMessage(UpdatePanel.class, "UpdatePanel.forcedUpdateChxBox.text")); // NOI18N
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.add(forcedUpdateChxBox, BorderLayout.NORTH);
        optionsPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        setOptionsPanel(optionsPanel, null);
    }
}
