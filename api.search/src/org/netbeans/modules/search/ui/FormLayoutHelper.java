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
package org.netbeans.modules.search.ui;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Helper class for adding components to panels with GroupLayout.
 *
 * @author jhavlin
 */
public class FormLayoutHelper {

    public static final Column DEFAULT_COLUMN = new DefaultColumn();
    public static final Column EAGER_COLUMN = new EagerColumn();
    public static final Column EXPANDING_COLUMN = new ExpandingColumn();
    JPanel panel;
    GroupLayout layout;
    Column[] columns;
    Group[] columnGroups;
    private Group horizontalGroup;
    private Group verticalGroup;

    //Collection<Column> columns;
    public FormLayoutHelper(JPanel panel, Column... columns) {

        this.panel = panel;
        layout = new GroupLayout(panel);
        panel.setLayout(layout);

        horizontalGroup = layout.createSequentialGroup();
        verticalGroup = layout.createSequentialGroup();

        this.columns = columns;
        columnGroups = new Group[columns.length];

        for (int i = 0; i < columns.length; i++) {
            Group columnGroup = columns[i].createParallelGroup(layout);
            columnGroups[i] = columnGroup;
            horizontalGroup.addGroup(columnGroup);
        }

        layout.setHorizontalGroup(horizontalGroup);
        layout.setVerticalGroup(verticalGroup);
    }

    public void setAllGaps(boolean gaps) {
        setInlineGaps(gaps);
        setContainerGaps(gaps);
    }

    public void setInlineGaps(boolean gaps) {
        layout.setAutoCreateGaps(gaps);
    }

    public void setContainerGaps(boolean gaps) {
        layout.setAutoCreateContainerGaps(gaps);
    }

    public GroupLayout getLayout() {
        return layout;
    }

    public void addRow(int min, int pref, int max, JComponent... component) {

        Group newRowGroup = layout.createParallelGroup(
                GroupLayout.Alignment.BASELINE);

        for (int i = 0; i < component.length && i < columns.length; i++) {
            JComponent cmp = component[i];
            newRowGroup.addComponent(cmp, min, pref, max);
            columns[i].addComponent(cmp, columnGroups[i]);
        }
        verticalGroup.addGroup(newRowGroup);
    }

    public void addRow(JComponent... component) {
        addRow(GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                GroupLayout.DEFAULT_SIZE, component);
    }

    public static abstract class Column {

        protected abstract void addComponent(
                JComponent component, Group parallelColumnGroup);

        protected abstract Group createParallelGroup(GroupLayout layout);
    }

    /**
     * Default column.
     */
    private static class DefaultColumn extends Column {

        @Override
        protected void addComponent(
                JComponent component, Group parallelColumnGroup) {
            parallelColumnGroup.addComponent(component);
        }

        @Override
        protected Group createParallelGroup(GroupLayout layout) {
            return layout.createParallelGroup();
        }
    }

    /**
     * Columns where components are as wide as the widest one of them.
     */
    private static class ExpandingColumn extends Column {

        @Override
        protected void addComponent(
                JComponent component, Group parallelColumnGroup) {

            parallelColumnGroup.addComponent(component,
                    GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE);
        }

        @Override
        protected Group createParallelGroup(GroupLayout layout) {
            return layout.createParallelGroup(
                    GroupLayout.Alignment.LEADING, false);
        }
    }

    /**
     * Column that takes as much space as possible.
     */
    private static class EagerColumn extends DefaultColumn {

        @Override
        protected void addComponent(
                JComponent component, Group parallelColumnGroup) {
            parallelColumnGroup.addComponent(component,
                    GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE);
        }
    }
}
