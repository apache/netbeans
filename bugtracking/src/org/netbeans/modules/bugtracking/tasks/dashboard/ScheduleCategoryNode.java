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
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.tasks.Category;
import org.netbeans.modules.bugtracking.tasks.TaskSchedulingManager;
import org.netbeans.modules.bugtracking.tasks.TaskSorter;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.ImageUtilities;

/**
 *
 * @author jpeska
 */
public class ScheduleCategoryNode extends CategoryNode {

    private static final ImageIcon SCHEDULE_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/category_schedule.png", true);
    private final TaskSchedulingManager schedulingManager;
    private final ScheduleCategoryListener listener;

    public ScheduleCategoryNode(Category category) {
        super(category, false);
        this.schedulingManager = TaskSchedulingManager.getInstance();
        this.listener = new ScheduleCategoryListener();
    }

    @Override
    ImageIcon getIcon() {
        return SCHEDULE_ICON;
    }

    @Override
    List<Action> getCategoryActions(List<TreeListNode> selectedNodes) {
        List<Action> actions = new ArrayList<Action>();
        return actions;
    }

    @Override
    void adjustTaskNode(TaskNode taskNode) {
    }

    @Override
    Comparator<TaskNode> getSpecialComparator() {
        return TaskSorter.getScheduleComparator();
    }

    @Override
    protected void attach() {
        super.attach();
        schedulingManager.addPropertyChangeListener(listener);
    }


    @Override
    protected void dispose() {
        super.dispose();
        schedulingManager.removePropertyChangeListener(listener);
    }

     private class ScheduleCategoryListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(TaskSchedulingManager.PROPERTY_SCHEDULED_TASKS_CHANGED)) {
                ScheduleCategoryNode.this.updateContent();
            }
        }
    }

}
