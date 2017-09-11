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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.versioning.ui.diff;

import org.netbeans.api.diff.Difference;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Contains Diff actions toolbar: Goto Previous, Goto Next, Rollback, Diff.
 * 
 * @author Maros Sandor
 */
class DiffTooltipActionsPanel extends JToolBar implements ActionListener {
    
    private final Icon iconPrevious = ImageUtilities.loadImageIcon("org/netbeans/modules/versioning/ui/diff/diff-prev.png", false); // NOI18N
    private final Icon iconNext = ImageUtilities.loadImageIcon("org/netbeans/modules/versioning/ui/diff/diff-next.png", false); // NOI18N
    private final Icon iconDiff = ImageUtilities.loadImageIcon("org/netbeans/modules/versioning/ui/diff/diff.png", false); // NOI18N
    private final Icon iconRollback = ImageUtilities.loadImageIcon("org/netbeans/modules/versioning/ui/diff/rollback.png", false); // NOI18N

    private final DiffActionTooltipWindow master;
    private final Difference              diff;
    
    private final JButton prevButton;
    private final JButton nextButton;
    private final JButton rollButton;
    private final JButton diffButton;

    public DiffTooltipActionsPanel(DiffActionTooltipWindow master, Difference diff) {
        this.master = master;
        this.diff = diff;

        Color tooltipBackround = UIManager.getColor("ToolTip.background"); // NOI18N
        if (tooltipBackround == null) tooltipBackround = Color.WHITE;
        
        setRollover(true);
        setFloatable(false);
        setBackground(tooltipBackround);

        prevButton = new JButton(iconPrevious);
        nextButton = new JButton(iconNext);
        rollButton = new JButton(iconRollback);
        diffButton = new JButton(iconDiff);
        
        prevButton.setToolTipText(NbBundle.getMessage(DiffTooltipActionsPanel.class, "TT_GoToPreviousDifference"));
        nextButton.setToolTipText(NbBundle.getMessage(DiffTooltipActionsPanel.class, "TT_GoToNextDifference"));
        diffButton.setToolTipText(NbBundle.getMessage(DiffTooltipActionsPanel.class, "TT_Open_Diff_Window"));
        if (diff.getType() == Difference.ADD) {
            rollButton.setToolTipText(NbBundle.getMessage(DiffTooltipActionsPanel.class, "TT_Delete_Added_Text"));
        } else if (diff.getType() == Difference.CHANGE) {
            rollButton.setToolTipText(NbBundle.getMessage(DiffTooltipActionsPanel.class, "TT_Replace_With_Original_Text"));
        } else {
            rollButton.setToolTipText(NbBundle.getMessage(DiffTooltipActionsPanel.class, "TT_Restore_Original_Text"));
        }
        
        prevButton.addActionListener(this);
        nextButton.addActionListener(this);
        rollButton.addActionListener(this);
        diffButton.addActionListener(this);

        prevButton.setBackground(tooltipBackround);
        nextButton.setBackground(tooltipBackround);
        rollButton.setBackground(tooltipBackround);
        diffButton.setBackground(tooltipBackround);

        add(prevButton);
        add(nextButton);
        add(rollButton);
        add(diffButton);

        Difference [] diffs = master.getMaster().getCurrentDiff();
        prevButton.setEnabled(diffs[0] != diff);
        nextButton.setEnabled(diffs[diffs.length - 1] != diff);
        rollButton.setEnabled(master.getMaster().canRollback(diff));
        
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == prevButton) {
            master.shutdown();
            master.getMaster().onPrevious(diff);
        } else if (e.getSource() == nextButton) {
            master.shutdown();
            master.getMaster().onNext(diff);
        } if (e.getSource() == rollButton) {
            master.shutdown();
            master.getMaster().onRollback(diff);
        } else if (e.getSource() == diffButton) {
            master.shutdown();
            master.getMaster().onDiff(diff);
        }
    }

    void focusButton () {
        super.requestFocus();
        for (JButton b : new JButton[] { prevButton, nextButton, rollButton, diffButton }) {
            if (b.isEnabled()) {
                b.requestFocusInWindow();
                break;
            }
        }
    }
        
}
