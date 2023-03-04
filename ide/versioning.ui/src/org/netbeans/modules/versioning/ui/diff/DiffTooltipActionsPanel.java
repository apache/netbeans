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
