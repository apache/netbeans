/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    public abstract static class Column {

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
