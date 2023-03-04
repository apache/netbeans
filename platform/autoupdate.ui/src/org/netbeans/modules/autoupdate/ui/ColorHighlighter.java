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

package org.netbeans.modules.autoupdate.ui;


import java.awt.Color;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Dmitry Lipin
 */

public class ColorHighlighter {
    private JTextComponent comp;
    private Highlighter.HighlightPainter painter;


    public ColorHighlighter(JTextComponent comp, Color c) {
        this.comp = comp;
        this.painter = new ColorHighlightPainter(c);
    }

    public int highlight(String word) {        
        Highlighter highlighter = comp.getHighlighter();

        // remove old highlight before applying new one
        for (Highlighter.Highlight h : highlighter.getHighlights()) {
            if (h.getPainter() instanceof ColorHighlightPainter) {
                highlighter.removeHighlight(h);
            }
        }
        
        if (word == null || word.equals("")) {
            return -1;
        }

        // search for the word, case insentitive
        String content = null;
        try {
            Document d = comp.getDocument();
            content = d.getText(0, d.getLength()).toLowerCase();
        } catch (BadLocationException e) {
            return -1;
        }

        word = word.toLowerCase();
        int lastIndex = 0;
        int firstOffset = -1;
        int wordSize = word.length();

        while ((lastIndex = content.indexOf(word, lastIndex)) != -1) {
            int endIndex = lastIndex + wordSize;
            try {
                highlighter.addHighlight(lastIndex, endIndex, painter);
            } catch (BadLocationException ex) {
                // ignore
            }
            if (firstOffset == -1) {
                firstOffset = lastIndex;
            }
            lastIndex = endIndex;
        }

        return firstOffset;
    }
    
    // create private class to use futher filtering
    class ColorHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public ColorHighlightPainter(Color color) {
            super(color);
        }
    }
}

