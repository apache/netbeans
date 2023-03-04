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
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JToolTip;

/**
 *
 * @author Jan Becicka
 */
public class TooltipLabel extends JLabel {

    public TooltipLabel() {
        setToolTipText(" "); //NOI18N
    }
    
    @Override
    public String getToolTipText(MouseEvent e) {
        FontMetrics metrix = getFontMetrics(getFont());
        String text = getText();
        int textWidth = metrix.stringWidth(text.replaceAll("<[^>]*>", ""));//NOI18N
        return (textWidth > getParent().getSize().width ? text : null);
    }

    @Override
    public Point getToolTipLocation(MouseEvent event) {
        return new Point(-3, 0);
    }

    @Override
    public JToolTip createToolTip() {
        JToolTip tooltp = new JToolTip();
        tooltp.setBackground(SystemColor.control);
        tooltp.setFont(getFont());
        tooltp.setOpaque(true);
        tooltp.setComponent(this);
        tooltp.setBorder(null);
        return tooltp;
    }
}
