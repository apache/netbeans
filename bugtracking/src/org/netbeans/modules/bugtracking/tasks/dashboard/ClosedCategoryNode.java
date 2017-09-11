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
package org.netbeans.modules.bugtracking.tasks.dashboard;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.tasks.DashboardUtils;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;

public class ClosedCategoryNode extends CategoryNode {

    private JPanel panel;
    private TreeLabel lblName;

    public ClosedCategoryNode(Category category) {
        super(category, false, false);
    }

    @Override
    protected List<TreeListNode> createChildren() {
        return Collections.emptyList();
    }

    @Override
    void updateContent() {
    }

    @Override
    void updateCounts() {
    }

    @Override
    public List<IssueImpl> getTasks(boolean includingNodeItself) {
        return Collections.emptyList();
    }

    @Override
    int indexOf(IssueImpl task) {
        return -1;
    }

    @Override
    public boolean isOpened() {
        return false;
    }

    @Override
    void refreshTaskContainer() {
    }

    @Override
    protected void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        super.configure(component, foreground, background, isSelected, hasFocus, rowWidth);
        if (panel != null) {
            lblName.setText(DashboardUtils.getCategoryDisplayText(this));
        }
    }

    @Override
    protected JComponent createComponent(List<IssueImpl> data) {
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        synchronized (LOCK) {
            labels.clear();
            buttons.clear();
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(false);
            final JLabel iconLabel = new JLabel(getIcon()); //NOI18N
            iconLabel.setEnabled(false);
            panel.add(iconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
            lblName = new TreeLabel(DashboardUtils.getCategoryDisplayText(this));
            labels.add(lblName);
            panel.add(lblName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
            panel.add(new JLabel(), new GridBagConstraints(7, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            return panel;
        }
    }

    @Override
    protected Type getType() {
        return Type.CLOSED;
    }
}
