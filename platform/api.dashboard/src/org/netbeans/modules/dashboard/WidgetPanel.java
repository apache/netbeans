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
package org.netbeans.modules.dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.dashboard.DashboardDisplayer;
import org.netbeans.spi.dashboard.DashboardWidget;
import org.netbeans.spi.dashboard.WidgetElement;
import org.openide.util.Lookup;

/**
 *
 */
final class WidgetPanel extends JPanel {

    private final DashboardDisplayer displayer;
    private final DashboardDisplayer.WidgetReference widgetRef;
    private final DashboardWidget widget;
    private final Accessor accessor;
    private final Color titleColor;

    private String title;
    private List<WidgetElement> elements;

    private WidgetPanel(DashboardDisplayer displayer, DashboardDisplayer.WidgetReference widgetRef) {
        this.displayer = Objects.requireNonNull(displayer);
        this.widgetRef = Objects.requireNonNull(widgetRef);
        widget = widgetRef.widget();
        accessor = new Accessor();
        titleColor = null; //UIManager.getColor("controlDkShadow");

        setLayout(new BorderLayout());

        title = "";
        elements = List.of();
        attachWidget();
    }

    private void attachWidget() {
        widget.attach(accessor);
        reconfigure();
    }

    void notifyShowing() {
        widget.showing(accessor);
    }

    void notifyHidden() {
        widget.hidden(accessor);
    }

    private void reconfigure() {
        String newTitle = widget.title(accessor);
        List<WidgetElement> newElements = List.copyOf(widget.elements(accessor));
        if (!Objects.equals(title, newTitle)
                || !Objects.equals(elements, newElements)) {
            this.title = newTitle;
            this.elements = newElements;
            build();
        }
    }

    private void build() {
        removeAll();
        if (!title.isEmpty()) {
            JComponent titleComponent = WidgetComponents.titleComponentFor(title);
            if (titleColor != null) {
                titleComponent.setForeground(titleColor);
            }
            add(titleComponent, BorderLayout.NORTH);
        }
        JPanel container = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(
                container,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        Insets defaultInsets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        for (int i = 0; i < elements.size(); i++) {
            JComponent cmp = WidgetComponents.componentFor(elements.get(i));
            if (cmp != null) {
                if (cmp instanceof JTextComponent) {
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    int space;
                    Font font = cmp.getFont();
                    if (font != null) {
                        space = font.getSize() / 2;
                    } else {
                        space = 6;
                    }
                    gbc.insets = new Insets(2, 2, space, 2);
                } else {
                    gbc.fill = GridBagConstraints.NONE;
                    gbc.insets = defaultInsets;
                }
                if (i == (elements.size() - 1)) {
                    gbc.weighty = 1;
                }
                container.add(cmp, gbc);
            }
        }
    }

    private class Accessor implements DashboardDisplayer.Panel {

        @Override
        public Lookup getLookup() {
            return displayer.getLookup();
        }

        @Override
        public String id() {
            return widgetRef.id();
        }

        @Override
        public void refresh() {
            EventQueue.invokeLater(WidgetPanel.this::reconfigure);
        }

        @Override
        public DashboardDisplayer.WidgetReference widgetReference() {
            return widgetRef;
        }

    }

    static WidgetPanel create(DashboardDisplayer displayer, DashboardDisplayer.WidgetReference widgetRef) {
        return new WidgetPanel(displayer, widgetRef);
    }

}
