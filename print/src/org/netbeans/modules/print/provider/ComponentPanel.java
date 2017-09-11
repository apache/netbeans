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
 * License. When distributing the software, include this License Header
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
package org.netbeans.modules.print.provider;

import java.awt.Dimension;
import java.awt.Graphics;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.api.print.PrintManager;
import static org.netbeans.modules.print.util.UI.*;

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
        Collections.sort(components, new Comparator<JComponent>() {

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
