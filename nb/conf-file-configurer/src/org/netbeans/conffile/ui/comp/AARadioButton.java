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
package org.netbeans.conffile.ui.comp;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JRadioButton;

/**
 *
 * @author Tim Boudreau
 */
public final class AARadioButton extends JRadioButton {

    public AARadioButton() {
        setOpaque(false);
    }

    @Override
    public void paint(Graphics g) {
        UIUtils.withTextAntialiasing(g, super::paint);
    }

    @Override
    public Point getToolTipLocation(MouseEvent event) {
        return new Point(0, getHeight());
    }
}
