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

package org.netbeans.modules.profiler.snaptracer.impl.swing;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 *
 * @author Jiri Sedlacek
 */
public final class HeaderLabel extends HeaderPanel {

    public static final int DEFAULT_HEIGHT = computeHeight();


    private String text;
    private int hAlign = SwingConstants.CENTER;


    public HeaderLabel() {
        this(""); // NOI18N
    }

    public HeaderLabel(String text) {
        this.text = text;
    }


    public final void setText(String text) {
        this.text = text;
        repaint();
    }

    public final String getText() {
        return text;
    }

    public final void setHorizontalAlignment(int align) {
        hAlign = align;
        repaint();
    }

    public final int getHorizontalAlignment() {
        return hAlign;
    }


    protected Object getRendererValue() {
        return getText();
    }


    protected void setupRenderer(Component renderer) {
        if (renderer instanceof JLabel) {
            JLabel label = (JLabel)renderer;
            label.setHorizontalAlignment(hAlign);
        }
    }


    public Dimension getPreferredSize() {
        Dimension dim = getPreferredSizeSuper();
        dim.height = DEFAULT_HEIGHT;
        return dim;
    }

    private Dimension getPreferredSizeSuper() {
        return super.getPreferredSize();
    }


    private static int computeHeight() {
        int height = new HeaderLabel("X").getPreferredSizeSuper().height; // NOI18N
        if (UIUtils.isMetalLookAndFeel()) height += 4;
//        else if (UISupport.isAquaLookAndFeel()) height += 6;
        return height;
    }

}
