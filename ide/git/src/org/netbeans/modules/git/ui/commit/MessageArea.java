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
package org.netbeans.modules.git.ui.commit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JTextArea;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.netbeans.modules.versioning.util.UndoRedoSupport;

/**
 *
 * @author Ondrej Vrabec
 */
public class MessageArea extends JTextArea {
    private UndoRedoSupport um;
    private int messageWidth;
    private int titleWidth;
    
    @Override
    public void paint (Graphics g) {
        super.paint(g);
        int charWidth = g.getFontMetrics().charWidth(' ');
        int bodyLineStarts = 0;
        int charHeight = g.getFontMetrics().getHeight();
        Rectangle visibleRect = getVisibleRect();
        if (titleWidth > 0) {
            int x = titleWidth * charWidth;
            Rectangle intersect = visibleRect.intersection(new Rectangle(x, 0, 1, charHeight));
            if (!intersect.isEmpty()) {
                g.setColor(Color.red);
                g.drawLine(x, intersect.y, x, intersect.y + intersect.height);
            }
            bodyLineStarts = 2 * charHeight;
        }
        if (messageWidth > 0) {
            int x = messageWidth * charWidth;
            Rectangle intersect = visibleRect.intersection(new Rectangle(x, bodyLineStarts, 1, visibleRect.y + visibleRect.height));
            if (!intersect.isEmpty()) {
                g.setColor(Color.red);
                g.drawLine(x, intersect.y, x, intersect.y + intersect.height);
            }
        }
    }

    /**
     * Must be called when the area is brought to visible.
     */
    public void open () {
        if (um == null) {
            um = UndoRedoSupport.register(this);
            Spellchecker.register(this);
        }
    }

    /**
     * Must be called when the area is being discarded,
     */
    public void close () {
        if (um != null) {
            um.unregister();
            um = null;
        }
    }

    void setNumberOfChars (int numberOfChars) {
        this.messageWidth = numberOfChars;
    }

    void setNumberOfTitleChars (int numberOfTitleChars) {
        this.titleWidth = numberOfTitleChars;
    }
    
}
