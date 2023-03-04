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
package org.netbeans.modules.versioning.ui.diff;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.StringReader;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import org.netbeans.api.diff.Difference;
import org.netbeans.editor.EditorUI;
import org.openide.text.CloneableEditorSupport;

/**
 * @author Maros Sandor
 */
class DiffTooltipContentPanel extends JComponent {
    private JEditorPane originalTextPane;
    private final Color color;
    private int maxWidth;
    private final JScrollPane jsp;

    public DiffTooltipContentPanel(final JTextComponent parentPane, final String mimeType, final Difference diff) {
        
        originalTextPane = new JEditorPane();

        EditorKit kit = CloneableEditorSupport.getEditorKit(mimeType);
        originalTextPane.setEditorKit(kit);

        Document xdoc = kit.createDefaultDocument();
        if (!(xdoc instanceof StyledDocument)) {
            xdoc = new DefaultStyledDocument(new StyleContext());
            kit = new StyledEditorKit();
            originalTextPane.setEditorKit(kit);
        }

        StyledDocument doc = (StyledDocument) xdoc;
        try {
            kit.read(new StringReader(diff.getFirstText()), doc, 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        originalTextPane.setDocument(doc);
        originalTextPane.setEditable(false);
        color = getBackgroundColor(diff.getType());
        originalTextPane.setBackground(color);
        EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(originalTextPane);
        Element rootElement = org.openide.text.NbDocument.findLineRootElement(doc);
        int lineCount = rootElement.getElementCount();
        int height = eui.getLineHeight() * lineCount;
        maxWidth = 0;
        for(int line = 0; line < lineCount; line++) {
            Element lineElement = rootElement.getElement(line);
            String text = null;
            try {
                text = doc.getText(lineElement.getStartOffset(), lineElement.getEndOffset() - lineElement.getStartOffset());
            } catch (BadLocationException e) {
                //
            }
            text = replaceTabs(mimeType, text);
            int lineLength = parentPane.getFontMetrics(parentPane.getFont()).stringWidth(text);
            if (lineLength > maxWidth) {
                maxWidth = lineLength;
            }
        }
        if (maxWidth < 50) maxWidth = 50;   // too thin component causes repaint problems
        else if (maxWidth < 150) maxWidth += 10;
        maxWidth = Math.min(maxWidth * 7 / 6, parentPane.getVisibleRect().width);
        originalTextPane.setPreferredSize(new Dimension(maxWidth, height));

        if (!originalTextPane.isEditable()) {
            originalTextPane.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$"); //NOI18N
        }

        jsp = new JScrollPane(originalTextPane);
        jsp.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

        setLayout(new BorderLayout());
        add(jsp);
    }

    void resize () {
        if (originalTextPane == null) return;
        originalTextPane.setBackground(color);
        Element rootElement = org.openide.text.NbDocument.findLineRootElement((StyledDocument) originalTextPane.getDocument());
        int lineCount = rootElement.getElementCount();

        int height = 0;
        assert lineCount > 0;
        Element lineElement = rootElement.getElement(lineCount - 1);
        Rectangle rec;
        try {
            rec = originalTextPane.modelToView(lineElement.getEndOffset() - 1);
            height = rec.y + rec.height;
        } catch (BadLocationException ex) {
        }
        if (height > 0) {
            Dimension size = originalTextPane.getPreferredSize();
            Dimension scrollpaneSize = jsp.getPreferredSize();
            height += jsp.getHorizontalScrollBar().getPreferredSize().height;

            jsp.setPreferredSize(new Dimension(maxWidth + Math.max(0, scrollpaneSize.width - size.width), height + Math.max(0, scrollpaneSize.height - size.height)));
            SwingUtilities.getWindowAncestor(originalTextPane).pack();
        }
        originalTextPane = null;
    }

    private Color getBackgroundColor (int key) {
        org.netbeans.modules.diff.DiffModuleConfig config = org.netbeans.modules.diff.DiffModuleConfig.getDefault();
        return key == Difference.DELETE ? config.getSidebarDeletedColor() : config.getSidebarChangedColor();
    }

    private Integer spacesPerTab;
    private String replaceTabs(String mimeType, String text) {
        if (text.contains("\t")) {                                      //NOI18N
            if (spacesPerTab == null) {
                spacesPerTab = org.netbeans.modules.diff.DiffModuleConfig.getDefault().getSpacesPerTabFor(mimeType);
            }
            text = text.replace("\t", strCharacters(' ', spacesPerTab)); //NOI18N
        }
        return text;
    }
    
    private static String strCharacters(char c, int num) {
        StringBuffer s = new StringBuffer();
        while(num-- > 0) {
            s.append(c);
        }
        return s.toString();
    }
}
