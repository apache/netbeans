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
package org.netbeans.swing.laf.flatlaf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

/**
 * A border similar to {@link MatteBorder}, but which avoids visual artifacts from rounding errors
 * under non-integral HiDPI scaling factors (e.g. 150%).
 *
 * @author Eirik Bakke (ebakke@ultorg.com)
 */
final class DPISafeBorder implements Border {
    private final Insets insets;
    private final Color color;

    /**
     * Create a new instance with the same semantics as that produced by
     * {@link MatteBorder#MatteBorder(int, int, int, int, java.awt.Color)}.
     *
     * @param color may not be null
     */
    public static Border matte(int top, int left, int bottom, int right, Color color) {
        return new DPISafeBorder(new Insets(top, left, bottom, right), color);
    }

    private DPISafeBorder(Insets insets, Color color) {
        if (insets == null)
            throw new NullPointerException();
        if (color == null)
            throw new NullPointerException();
        this.insets = new Insets(insets.top, insets.left, insets.bottom, insets.right);
        this.color = color;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        HiDPIUtils.paintAtScale1x(g, x, y, width, height, this::paintBorderAtScale1x);
    }

    private void paintBorderAtScale1x(Graphics2D g, int deviceWidth, int deviceHeight, double scale) {
        final Color oldColor = g.getColor();
        final int deviceLeft   = HiDPIUtils.deviceBorderWidth(scale, insets.left);
        final int deviceRight  = HiDPIUtils.deviceBorderWidth(scale, insets.right);
        final int deviceTop    = HiDPIUtils.deviceBorderWidth(scale, insets.top);
        final int deviceBottom = HiDPIUtils.deviceBorderWidth(scale, insets.bottom);

        g.setColor(color);

        // Top border.
        g.fillRect(0, 0, deviceWidth - deviceRight, deviceTop);
        // Left border.
        g.fillRect(0, deviceTop, deviceLeft, deviceHeight - deviceTop);
        // Bottom border.
        g.fillRect(deviceLeft, deviceHeight - deviceBottom, deviceWidth - deviceLeft, deviceBottom);
        // Right border.
        g.fillRect(deviceWidth - deviceRight, 0, deviceRight, deviceHeight - deviceBottom);

        g.setColor(oldColor);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(insets.top, insets.left, insets.bottom, insets.right);
    }

    @Override
    public boolean isBorderOpaque() {
        /* Set this to false to be safe, since we might not fill in the entire designated logical
        area due to rounding errors in the conversion to device pixels. */
        return false;
    }
}
