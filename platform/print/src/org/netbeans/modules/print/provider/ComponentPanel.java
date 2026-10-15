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
package org.netbeans.modules.print.provider;

import java.awt.Dimension;
import java.awt.Graphics;

import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.api.print.PrintManager;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.02.22
 */
final class ComponentPanel extends JPanel {

    ComponentPanel(List<JComponent> components) {
        myComponents = sort(components);

        myHeight = 0;
        myWidth = 0;

        for (int i = 0; i < myComponents.size(); i++) {
            JComponent component = myComponents.get(i);
//out();
//out("see: " + component.getClass().getName());
            int width = getWidth(component);
            int height = getHeight(component);
//out("   width: " + width);
//out("  height: " + height);
            myWidth += width;

            if (height > myHeight) {
                myHeight = height;
            }
        }
    }

    @Override
    public void print(Graphics g) {
        for (JComponent component : myComponents) {
            component.print(g);
//          g.setColor(java.awt.Color.green);
//          g.drawRect(0, 0, getWidth(component), getHeight(component));
            g.translate(getWidth(component), 0);
        }
    }

    @Override
    public int getWidth() {
        return myWidth;
    }

    @Override
    public int getHeight() {
        return myHeight;
    }

    private int getWidth(JComponent component) {
        Dimension size = getSize(component);

        if (size == null) {
            return component.getWidth();
        }
        return size.width;
    }

    private int getHeight(JComponent component) {
        Dimension size = getSize(component);

        if (size == null) {
            return component.getHeight();
        }
        return size.height;
    }

    private Dimension getSize(JComponent component) {
        Object object = component.getClientProperty(PrintManager.PRINT_SIZE);

        if (object instanceof Dimension) {
            return (Dimension) object;
        }
        return null;
    }

    private List<JComponent> sort(List<JComponent> components) {
        components.sort(new Comparator<JComponent>() {

            public int compare(JComponent component1, JComponent component2) {
                int order1 = getInteger(component1).intValue();
                int order2 = getInteger(component2).intValue();

                if (order1 < order2) {
                    return -1;
                }
                if (order1 == order2) {
                    return 0;
                }
                return 1;
            }

            private Integer getInteger(JComponent component) {
                Object object = component.getClientProperty(PrintManager.PRINT_ORDER);

                if (object instanceof Integer) {
                    return (Integer) object;
                }
                return Integer.MIN_VALUE;
            }
        });

        return components;
    }

    private int myWidth;
    private int myHeight;
    private List<JComponent> myComponents;
}
