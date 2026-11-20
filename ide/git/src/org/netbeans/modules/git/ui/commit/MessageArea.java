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
package org.netbeans.modules.git.ui.commit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
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

    /**
     * Returns the text as it appears in the text area.
     */
    String getCommitMessage() {
        try {
            return getWrappedText();
        } catch (BadLocationException ex) {
            return getText();
        }
    }

    // still no easier way of doing this?
    private String getWrappedText() throws BadLocationException {
        String plain = getText();
        if (plain.isEmpty()) {
            return plain;
        }
        StringBuilder sb = new StringBuilder();
        int length = getDocument().getLength();

        for (int offset = 0; offset < length; ) {
            int start = Utilities.getRowStart(this, offset);
            int end = Utilities.getRowEnd(this, offset);

            if (start < 0 || end < 0) {
                break;
            }

            int exclusiveEnd = Math.min(end + 1, length);
            String line = getDocument().getText(start, exclusiveEnd - start);

            // check if line has an actual break already
            // note: DefaultEditorKit is normalizing to '\n' internally
            if (line.endsWith("\n")) {
                sb.append(line);
            } else {
                sb.append(line).append("\n");
            }

            if (exclusiveEnd <= offset) {
                break;
            }
            offset = exclusiveEnd;
        }
        if (plain.endsWith("\n")) {
            return sb.toString();
        } else {
            return sb.toString().substring(0, sb.length() - 1); // eagerly added one too much
        }
    }

}
