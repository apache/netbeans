/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.tasks.dashboard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.tasks.RecentCategory;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.ImageUtilities;


public class RecentCategoryNode extends CategoryNode {

    private final PropertyChangeListener recentListener;
    private static final ImageIcon RECENT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/category_recent.png", true);

    public RecentCategoryNode(Category category) {
        super(category, false);
        recentListener = new RecentCategoryListener();
    }

    @Override
    ImageIcon getIcon() {
        return RECENT_ICON;
    }

    @Override
    List<Action> getCategoryActions(List<TreeListNode> selectedNodes) {
        List<Action> actions = new ArrayList<Action>();
        //TODO add clear action eventually
        return actions;
    }

    @Override
    void adjustTaskNode(TaskNode taskNode) {
    }

    @Override
    protected void attach() {
        super.attach();
        BugtrackingManager.getInstance().addPropertyChangeListener(recentListener);
    }


    @Override
    protected void dispose() {
        super.dispose();
        BugtrackingManager.getInstance().removePropertyChangeListener(recentListener);
    }

    @Override
    Comparator<TaskNode> getSpecialComparator() {
        return ((RecentCategory) getCategory()).getTaskNodeComparator();
    }

    private class RecentCategoryListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(BugtrackingManager.PROP_RECENT_ISSUES_CHANGED)) {
                RecentCategoryNode.this.updateContent();
                DashboardViewer.getInstance().updateCategoryNode(RecentCategoryNode.this);
            }
        }
    }
}
