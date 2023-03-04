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

package org.netbeans.modules.profiler.snaptracer.impl.timeline;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.lib.profiler.charts.ItemSelection;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.lib.profiler.charts.xy.XYItemSelection;
import org.netbeans.modules.profiler.snaptracer.impl.swing.LegendFont;
import org.netbeans.modules.profiler.snaptracer.impl.swing.Spacer;

/**
 *
 * @author Jiri Sedlacek
 */
final class TimelineTooltipPainter extends JPanel {

    private static Color SELECTION_FOREGROUND = Color.BLACK;
    private static Color SELECTION_BACKGROUND = Utils.forceSpeed() ?
                                            new Color(255, 255, 255) :
                                            new Color(255, 255, 255, 225);
    private static Color HOVER_FOREGROUND = Color.WHITE;
    private static Color HOVER_BACKGROUND = Utils.forceSpeed() ?
                                            new Color(80, 80, 80) :
                                            new Color(0, 0, 0, 170);

    private JLabel[] valueNames;
    private JLabel[] valuePainters;
    private JLabel[] unitsPainters;

    private final boolean selection;
    private final Color foreground;
    private final Color background;


    TimelineTooltipPainter(boolean selection) {
        this.selection = selection;

        foreground = selection ? SELECTION_FOREGROUND : HOVER_FOREGROUND;
        background = selection ? SELECTION_BACKGROUND : HOVER_BACKGROUND;

        initUI();
    }


    void update(Model rowModel, List<ItemSelection> selectedItems) {
        int rowsCount = rowModel.getRowsCount();
        if (valueNames == null || valueNames.length != rowsCount)
            initComponents(rowsCount);
        
        for (int i = 0; i < rowsCount; i++) {
            XYItemSelection sel = (XYItemSelection)selectedItems.get(i);
            long itemValue = sel.getItem().getYValue(sel.getValueIndex());
            valueNames[i].setText(rowModel.getRowName(i));
            valuePainters[i].setText(rowModel.getRowValue(i, itemValue));
            unitsPainters[i].setText(rowModel.getRowUnits(i));
        }
    }


    protected void paintComponent(Graphics g) {
        g.setColor(background);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
        if (selection) {
            g.setColor(foreground);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }


    private void initUI() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setLayout(new GridBagLayout());
    }

    private void initComponents(int rowsCount) {
        removeAll();
        
        valueNames = new JLabel[rowsCount];
        valuePainters = new JLabel[rowsCount];
        unitsPainters = new JLabel[rowsCount];
        
        GridBagConstraints constraints;

        for (int i = 0; i < rowsCount; i++) {
            JLabel itemLabel = new JLabel();
            valueNames[i] = itemLabel;
            itemLabel.setFont(new LegendFont());
            itemLabel.setForeground(foreground);
            itemLabel.setOpaque(false);
            constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = i;
            constraints.gridwidth = 1;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.insets = new Insets(0, 0, 0, 0);
            add(itemLabel, constraints);

            JLabel valueLabel = new JLabel();
            valuePainters[i] = valueLabel;
            valueLabel.setFont(new LegendFont());
            valueLabel.setForeground(foreground);
            valueLabel.setOpaque(false);
            constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.gridy = i;
            constraints.gridwidth = 1;
            constraints.anchor = GridBagConstraints.NORTHEAST;
            constraints.insets = new Insets(0, 8, 0, 0);
            add(valueLabel, constraints);

            JLabel unitsLabel = new JLabel();
            unitsPainters[i] = unitsLabel;
            unitsLabel.setFont(new LegendFont());
            unitsLabel.setForeground(foreground);
            unitsLabel.setOpaque(false);
            constraints = new GridBagConstraints();
            constraints.gridx = 2;
            constraints.gridy = i;
            constraints.gridwidth = 1;
            constraints.anchor = GridBagConstraints.NORTHEAST;
            constraints.insets = new Insets(0, 3, 0, 0);
            add(unitsLabel, constraints);

            constraints = new GridBagConstraints();
            constraints.gridx = 3;
            constraints.gridy = i;
            constraints.weightx = 1;
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.anchor = GridBagConstraints.NORTHEAST;
            constraints.insets = new Insets(0, 0, 0, 0);
            add(Spacer.create(), constraints);
        }
    }


    static interface Model {

        public int    getRowsCount      ();
        public String getRowName        (int index);
        public String getRowValue       (int index, long itemValue);
        public String getRowUnits       (int index);

    }

}
