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
/*
 * EditorToolbarBorder.java
 *
 * Created on March 14, 2004, 4:38 AM
 */

package org.netbeans.swing.plaf.nimbus;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;

/**
 *
 * @author  David Simonek
 */
public class EditorToolbarBorder extends AbstractBorder {
    private static final Insets insets = new Insets(0, 0, 1, 0);

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Color borderC = UIManager.getColor("controlDarkShadow");
        g.setColor(borderC);
        g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }    
}
